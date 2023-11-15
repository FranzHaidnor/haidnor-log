package haidnor.remoting.core;

import haidnor.remoting.ChannelEventListener;
import haidnor.remoting.InvokeCallback;
import haidnor.remoting.RPCHook;
import haidnor.remoting.RemotingClient;
import haidnor.remoting.common.Pair;
import haidnor.remoting.exception.RemotingConnectException;
import haidnor.remoting.exception.RemotingSendRequestException;
import haidnor.remoting.exception.RemotingTimeoutException;
import haidnor.remoting.exception.RemotingTooMuchRequestException;
import haidnor.remoting.protocol.RandomAccessFileResponseCommand;
import haidnor.remoting.protocol.RemotingCommand;
import haidnor.remoting.util.RemotingHelper;
import haidnor.remoting.util.RemotingUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class NettyRemotingClient extends NettyRemotingAbstract implements RemotingClient {

    private static final long LOCK_TIMEOUT_MILLIS = 3000;

    private final Bootstrap bootstrap = new Bootstrap();

    private final NettyClientConfig nettyClientConfig;

    /**
     * 是否已经调用了 start 方法
     */
    private final AtomicBoolean startFlag = new AtomicBoolean(false);

    private final EventLoopGroup eventLoopGroupWorker;

    private final Lock lockChannelTables = new ReentrantLock();

    /**
     * key:addr
     */
    private final ConcurrentMap<String, ChannelWrapper> channelTables = new ConcurrentHashMap<>();

    private final Timer timer = new Timer("ClientHouseKeepingService", true);

    private final ExecutorService publicExecutor;

    private ChannelEventListener channelEventListener;

    /**
     * Invoke the callback methods in this executor when process response.
     */
    private ExecutorService callbackExecutor;

    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    private final List<ChannelHandler> customerFirstChannelHandlerList = new ArrayList<>();
    private final List<ChannelHandler> customerLastChannelHandlerList = new ArrayList<>();

    public NettyRemotingClient(final NettyClientConfig nettyClientConfig) {
        super(nettyClientConfig.getClientOnewaySemaphoreValue(), nettyClientConfig.getClientAsyncSemaphoreValue());

        this.nettyClientConfig = nettyClientConfig;

        int publicThreadNums = nettyClientConfig.getClientCallbackExecutorThreads();
        if (publicThreadNums <= 0) {
            publicThreadNums = 4;
        }

        this.publicExecutor = Executors.newFixedThreadPool(publicThreadNums, new ThreadFactory() {
            private final AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "NettyClientPublicExecutor_" + this.threadIndex.incrementAndGet());
            }
        });

        this.eventLoopGroupWorker = new NioEventLoopGroup(1, new ThreadFactory() {
            private final AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClientSelector_%d", this.threadIndex.incrementAndGet()));
            }
        });

        if (nettyClientConfig.isUseTLS()) {
            try {
                sslContext = TlsHelper.buildSslContext(true);
                log.debug("SSL enabled for client");
            } catch (IOException e) {
                log.error("Failed to create SSLContext", e);
            } catch (CertificateException e) {
                log.error("Failed to create SSLContext", e);
                throw new RuntimeException("Failed to create SSLContext", e);
            }
        }
    }

    @Override
    public void start() {
        if (startFlag.compareAndSet(false, true)) {
            this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                    nettyClientConfig.getClientWorkerThreads(),
                    new ThreadFactory() {
                        private final AtomicInteger threadIndex = new AtomicInteger(0);

                        @Override
                        public Thread newThread(Runnable r) {
                            return new Thread(r, "NettyClientWorkerThread_" + this.threadIndex.incrementAndGet());
                        }
                    });

            this.bootstrap
                    .group(this.eventLoopGroupWorker).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, false)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, nettyClientConfig.getConnectTimeoutMillis())
                    .option(ChannelOption.SO_SNDBUF, nettyClientConfig.getClientSocketSndBufSize())
                    .option(ChannelOption.SO_RCVBUF, nettyClientConfig.getClientSocketRcvBufSize())
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            for (ChannelHandler channelHandler : customerFirstChannelHandlerList) {
                                pipeline.addLast(defaultEventExecutorGroup, channelHandler);
                            }

                            if (nettyClientConfig.isUseTLS()) {
                                if (null != sslContext) {
                                    pipeline.addFirst(defaultEventExecutorGroup, "sslHandler", sslContext.newHandler(ch.alloc()));
                                    log.debug("Prepend SSL handler");
                                } else {
                                    log.warn("Connections are insecure as SSLContext is null!");
                                }
                            }
                            pipeline.addLast(
                                    defaultEventExecutorGroup,
                                    new ChannelOutboundHandlerAdapter() {
                                        @Override
                                        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                            if (msg instanceof RandomAccessFileResponseCommand) {
                                                RandomAccessFileResponseCommand response = (RandomAccessFileResponseCommand) msg;
                                                ByteBuffer header = response.encode();
                                                ctx.writeAndFlush(Unpooled.wrappedBuffer(header));

                                                RandomAccessFile accessFile = response.getAccessFile();
                                                int length = response.getLength();

                                                // 最大数据包长度
                                                int packageLength = 1024 * 1024;
                                                while (length > 0) {
                                                    byte[] bytes;
                                                    if (length < packageLength) {
                                                        bytes = new byte[length];
                                                    } else {
                                                        bytes = new byte[packageLength];
                                                    }
                                                    int read = accessFile.read(bytes);
                                                    System.out.println(read);
                                                    ctx.writeAndFlush(Unpooled.wrappedBuffer(bytes));
                                                    length -= packageLength;
                                                }
                                                accessFile.close();
                                            } else {
                                                super.write(ctx, msg, promise);
                                            }
                                        }
                                    },
                                    new NettyEncoder(),
                                    new NettyDecoder(nettyClientConfig.getFrameMaxLength()),
                                    new IdleStateHandler(nettyClientConfig.getClientChannelMaxReaderIdleTimeSeconds(), nettyClientConfig.getClientChannelMaxWriterIdleTimeSeconds(), nettyClientConfig.getClientChannelMaxAllIdleTimeSeconds()),
                                    new NettyConnectManageHandler(),
                                    new NettyClientHandler());

                            for (ChannelHandler channelHandler : customerLastChannelHandlerList) {
                                pipeline.addLast(defaultEventExecutorGroup, channelHandler);
                            }
                        }
                    });

            this.timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        NettyRemotingClient.this.scanResponseTable();
                    } catch (Throwable e) {
                        log.error("scanResponseTable exception", e);
                    }
                }
            }, 1000 * 3, 1000);

            if (this.channelEventListener != null) {
                this.nettyEventExecutor.start();
            }
        }
    }

    @Override
    public void shutdown() {
        try {
            this.timer.cancel();

            for (ChannelWrapper cw : this.channelTables.values()) {
                this.closeChannel(null, cw.getChannel());
            }

            this.channelTables.clear();

            this.eventLoopGroupWorker.shutdownGracefully();

            this.nettyEventExecutor.shutdown();

            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            log.error("NettyRemotingClient shutdown exception, ", e);
        }
        if (this.publicExecutor != null) {
            try {
                this.publicExecutor.shutdown();
            } catch (Exception e) {
                log.error("NettyRemotingServer shutdown exception, ", e);
            }
        }
    }

    public void closeChannel(final String addr, final Channel channel) {
        if (null == channel)
            return;

        final String addrRemote = null == addr ? RemotingHelper.parseChannelRemoteAddr(channel) : addr;

        try {
            if (this.lockChannelTables.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                try {
                    boolean removeItemFromTable = true;
                    final ChannelWrapper prevCW = this.channelTables.get(addrRemote);

                    log.debug("closeChannel: begin close the channel[{}] Found: {}", addrRemote, prevCW != null);

                    if (null == prevCW) {
                        log.debug("closeChannel: the channel[{}] has been removed from the channel table before", addrRemote);
                        removeItemFromTable = false;
                    } else if (prevCW.getChannel() != channel) {
                        log.debug("closeChannel: the channel[{}] has been closed before, and has been created again, nothing to do.",
                                addrRemote);
                        removeItemFromTable = false;
                    }

                    if (removeItemFromTable) {
                        this.channelTables.remove(addrRemote);
                        log.debug("closeChannel: the channel[{}] was removed from channel table", addrRemote);
                    }

                    RemotingUtil.closeChannel(channel);
                } catch (Exception e) {
                    log.error("closeChannel: close the channel exception", e);
                } finally {
                    this.lockChannelTables.unlock();
                }
            } else {
                log.warn("closeChannel: try to lock channel table, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
            }
        } catch (InterruptedException e) {
            log.error("closeChannel exception", e);
        }
    }

    @Override
    public void registerRPCHook(RPCHook rpcHook) {
        if (rpcHook != null && !rpcHooks.contains(rpcHook)) {
            rpcHooks.add(rpcHook);
        }
    }

    @Override
    public void registerChannelEventListener(ChannelEventListener channelEventListener) {
        this.channelEventListener = channelEventListener;
    }

    public void closeChannel(final Channel channel) {
        if (null == channel) {
            return;
        }
        try {
            if (this.lockChannelTables.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                try {
                    boolean removeItemFromTable = true;
                    ChannelWrapper prevCW = null;
                    String addrRemote = null;
                    for (Map.Entry<String, ChannelWrapper> entry : channelTables.entrySet()) {
                        String key = entry.getKey();
                        ChannelWrapper prev = entry.getValue();
                        if (prev.getChannel() != null) {
                            if (prev.getChannel() == channel) {
                                prevCW = prev;
                                addrRemote = key;
                                break;
                            }
                        }
                    }

                    if (null == prevCW) {
                        log.debug("eventCloseChannel: the channel[{}] has been removed from the channel table before", addrRemote);
                        removeItemFromTable = false;
                    }

                    if (removeItemFromTable) {
                        this.channelTables.remove(addrRemote);
                        log.debug("closeChannel: the channel[{}] was removed from channel table", addrRemote);

                        channel.close().addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                log.debug("closeChannel: close the connection to remote address[{}] result: {}", RemotingHelper.parseChannelRemoteAddr(channel), future.isSuccess());
                                // 发送 OUT_BOUND_CLOSE 事件
                                if (NettyRemotingClient.this.channelEventListener != null) {
                                    final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);
                                    NettyRemotingClient.this.putNettyEvent(new NettyEvent(NettyEventType.OUT_BOUND_CLOSE, remoteAddress, channel));
                                }
                            }
                        });

                    }
                } catch (Exception e) {
                    log.error("closeChannel: close the channel exception", e);
                } finally {
                    this.lockChannelTables.unlock();
                }
            } else {
                log.warn("closeChannel: try to lock channel table, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
            }
        } catch (InterruptedException e) {
            log.error("closeChannel exception", e);
        }
    }

    @SneakyThrows
    @Override
    public RemotingCommand invokeSync(final String addr, RemotingCommand request) {
        return invokeSync(addr, request, nettyClientConfig.getTimeoutMillis());
    }

    @SneakyThrows
    @Override
    public RemotingCommand invokeSync(final String addr, final RemotingCommand request, long timeoutMillis) {
        start();

        long beginStartTime = System.currentTimeMillis();
        final Channel channel = this.getAndCreateChannel(addr);
        if (channel != null && channel.isActive()) {
            try {
                doBeforeRpcHooks(addr, request);
                long costTime = System.currentTimeMillis() - beginStartTime;
                if (timeoutMillis < costTime) {
                    throw new RemotingTimeoutException("invokeSync call timeout");
                }
                RemotingCommand response = this.invokeSyncImpl(channel, request, timeoutMillis - costTime);
                doAfterRpcHooks(RemotingHelper.parseChannelRemoteAddr(channel), request, response);
                return response;
            } catch (RemotingSendRequestException e) {
                log.warn("invokeSync: send request exception, so close the channel[{}]", addr);
                this.closeChannel(addr, channel);
                throw e;
            } catch (RemotingTimeoutException e) {
                if (nettyClientConfig.isClientCloseSocketIfTimeout()) {
                    this.closeChannel(addr, channel);
                    log.warn("invokeSync: close socket because of timeout, {}ms, {}", timeoutMillis, addr);
                }
                log.warn("invokeSync: wait response timeout exception, the channel[{}]", addr);
                throw e;
            }
        } else {
            this.closeChannel(addr, channel);
            throw new RemotingConnectException(addr);
        }
    }

    @SneakyThrows
    @Override
    public void invokeAsync(final String addr, RemotingCommand request, InvokeCallback invokeCallback) {
        invokeAsync(addr, request, nettyClientConfig.getTimeoutMillis(), invokeCallback);
    }

    private Channel getAndCreateChannel(final String addr) throws InterruptedException {
        ChannelWrapper cw = this.channelTables.get(addr);
        if (cw != null && cw.isOK()) {
            return cw.getChannel();
        }
        return this.createChannel(addr);
    }

    private Channel createChannel(final String addr) throws InterruptedException {
        ChannelWrapper cw = this.channelTables.get(addr);
        if (cw != null && cw.isOK()) {
            return cw.getChannel();
        }

        if (this.lockChannelTables.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            try {
                boolean createNewConnection;
                cw = this.channelTables.get(addr);
                if (cw != null) {

                    if (cw.isOK()) {
                        return cw.getChannel();
                    } else if (!cw.getChannelFuture().isDone()) {
                        createNewConnection = false;
                    } else {
                        this.channelTables.remove(addr);
                        createNewConnection = true;
                    }
                } else {
                    createNewConnection = true;
                }

                if (createNewConnection) {
                    ChannelFuture channelFuture = this.bootstrap.connect(RemotingHelper.string2SocketAddress(addr));
                    log.debug("createChannel: begin to connect remote host[{}] asynchronously", addr);
                    cw = new ChannelWrapper(channelFuture);
                    this.channelTables.put(addr, cw);
                }
            } catch (Exception e) {
                log.error("createChannel: create channel exception", e);
            } finally {
                this.lockChannelTables.unlock();
            }
        } else {
            log.warn("createChannel: try to lock channel table, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
        }

        if (cw != null) {
            ChannelFuture channelFuture = cw.getChannelFuture();
            if (channelFuture.awaitUninterruptibly(this.nettyClientConfig.getConnectTimeoutMillis())) {
                if (cw.isOK()) {
                    log.debug("createChannel: connect remote host[{}] success, {}", addr, channelFuture.toString());
                    return cw.getChannel();
                } else {
                    log.warn("createChannel: connect remote host[" + addr + "] failed, " + channelFuture.toString(), channelFuture.cause());
                }
            } else {
                log.warn("createChannel: connect remote host[{}] timeout {}ms, {}", addr, this.nettyClientConfig.getConnectTimeoutMillis(),
                        channelFuture.toString());
            }
        }

        return null;
    }

    @SneakyThrows
    @Override
    public void invokeAsync(final String addr, RemotingCommand request, long timeoutMillis, InvokeCallback invokeCallback) {
        start();
        long beginStartTime = System.currentTimeMillis();
        final Channel channel = this.getAndCreateChannel(addr);
        if (channel != null && channel.isActive()) {
            try {
                doBeforeRpcHooks(addr, request);
                long costTime = System.currentTimeMillis() - beginStartTime;
                if (timeoutMillis < costTime) {
                    throw new RemotingTooMuchRequestException("invokeAsync call timeout");
                }
                this.invokeAsyncImpl(channel, request, timeoutMillis - costTime, invokeCallback);
            } catch (RemotingSendRequestException e) {
                log.warn("invokeAsync: send request exception, so close the channel[{}]", addr);
                this.closeChannel(addr, channel);
                throw e;
            }
        } else {
            this.closeChannel(addr, channel);
            throw new RemotingConnectException(addr);
        }
    }

    @SneakyThrows
    @Override
    public void invokeOneway(final String addr, RemotingCommand request) {
        invokeOneway(addr, request, nettyClientConfig.getTimeoutMillis());
    }

    @SneakyThrows
    @Override
    public void invokeOneway(final String addr, RemotingCommand request, long timeoutMillis) {
        start();
        final Channel channel = this.getAndCreateChannel(addr);
        if (channel != null && channel.isActive()) {
            try {
                doBeforeRpcHooks(addr, request);
                this.invokeOnewayImpl(channel, request, timeoutMillis);
            } catch (RemotingSendRequestException e) {
                log.warn("invokeOneway: send request exception, so close the channel[{}]", addr);
                this.closeChannel(addr, channel);
                throw e;
            }
        } else {
            this.closeChannel(addr, channel);
            throw new RemotingConnectException(addr);
        }
    }

    @Override
    public <T extends Enum<T>> void registerProcessor(T command, NettyRequestProcessor processor, ExecutorService executor) {
        ExecutorService executorThis = executor;
        if (null == executor) {
            executorThis = this.publicExecutor;
        }

        Pair<NettyRequestProcessor, ExecutorService> pair = new Pair<NettyRequestProcessor, ExecutorService>(processor, executorThis);
        this.processorTable.put(command.name().hashCode(), pair);
    }

    @Override
    public boolean isChannelWritable(String addr) {
        ChannelWrapper cw = this.channelTables.get(addr);
        if (cw != null && cw.isOK()) {
            return cw.isWritable();
        }
        return true;
    }

    @Override
    public void addFirstChannelHandler(ChannelHandler channelHandler) {
        this.customerFirstChannelHandlerList.add(channelHandler);
    }

    @Override
    public void addLastChannelHandler(ChannelHandler channelHandler) {
        this.customerLastChannelHandlerList.add(channelHandler);
    }

    @Override
    public ChannelEventListener getChannelEventListener() {
        return channelEventListener;
    }

    @Override
    public ExecutorService getCallbackExecutor() {
        return callbackExecutor != null ? callbackExecutor : publicExecutor;
    }

    @Override
    public void setCallbackExecutor(final ExecutorService callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
    }

    static class ChannelWrapper {
        private final ChannelFuture channelFuture;

        public ChannelWrapper(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        public boolean isOK() {
            return this.channelFuture.channel() != null && this.channelFuture.channel().isActive();
        }

        public boolean isWritable() {
            return this.channelFuture.channel().isWritable();
        }

        private Channel getChannel() {
            return this.channelFuture.channel();
        }

        public ChannelFuture getChannelFuture() {
            return channelFuture;
        }
    }

    class NettyClientHandler extends SimpleChannelInboundHandler<RemotingCommand> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {
            processMessageReceived(ctx, msg);
        }
    }

    class NettyConnectManageHandler extends ChannelDuplexHandler {

        // Override ChannelInboundHandler ------------------------------------------------------------------------------

        /**
         * ChannelHandlerContext的Channel现在处于活动状态
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("NETTY CLIENT PIPELINE: channelActive {}", remoteAddress);
            super.channelActive(ctx);
            if (NettyRemotingClient.this.channelEventListener != null) {
                NettyRemotingClient.this.putNettyEvent(new NettyEvent(NettyEventType.IN_BOUND_ACTIVE, remoteAddress, ctx.channel()));
            }
        }

        /**
         * 已注册的 {@link ChannelHandlerContext} 的 {@link Channel} 现在处于非活动状态并已达到其生命周期终点。
         */
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("NETTY CLIENT PIPELINE: channelInactive {}", remoteAddress);
            closeChannel(ctx.channel());
            super.channelActive(ctx);
            if (NettyRemotingClient.this.channelEventListener != null) {
                NettyRemotingClient.this.putNettyEvent(new NettyEvent(NettyEventType.IN_BOUND_INACTIVE, remoteAddress, ctx.channel()));
            }
        }

        /**
         * 如果触发用户事件则调用
         */
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state().equals(IdleState.READER_IDLE)) {
                    final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
                    log.warn("NETTY CLIENT PIPELINE: READER_IDLE [{}]", remoteAddress);
                    if (NettyRemotingClient.this.channelEventListener != null) {
                        NettyRemotingClient.this.putNettyEvent(new NettyEvent(NettyEventType.IN_BOUND_READER_IDLE, remoteAddress, ctx.channel()));
                    }
                } else if (event.state().equals(IdleState.WRITER_IDLE)) {
                    final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
                    log.debug("NETTY CLIENT PIPELINE: WRITER_IDLE [{}]", remoteAddress);
                    if (NettyRemotingClient.this.channelEventListener != null) {
                        NettyRemotingClient.this.putNettyEvent(new NettyEvent(NettyEventType.IN_BOUND_WRITER_IDLE, remoteAddress, ctx.channel()));
                    }
                } else if (event.state().equals(IdleState.ALL_IDLE)) {
                    final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
                    log.debug("NETTY CLIENT PIPELINE: IDLE [{}]", remoteAddress);
                    if (NettyRemotingClient.this.channelEventListener != null) {
                        NettyRemotingClient.this.putNettyEvent(new NettyEvent(NettyEventType.IN_BOUND_ALL_IDLE, remoteAddress, ctx.channel()));
                    }
                }
            }
            ctx.fireUserEventTriggered(evt);
        }

        /**
         * 如果抛出 {@link Throwable} 则调用
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("NETTY CLIENT PIPELINE: exceptionCaught {}", remoteAddress);
            log.debug("NETTY CLIENT PIPELINE: exceptionCaught exception.", cause);
            closeChannel(ctx.channel());
            if (NettyRemotingClient.this.channelEventListener != null) {
                NettyRemotingClient.this.putNettyEvent(new NettyEvent(NettyEventType.IN_BOUND_EXCEPTION_CAUGHT, remoteAddress, ctx.channel()));
            }
        }

        // Override ChannelOutboundHandler ------------------------------------------------------------------------------

        /**
         * 进行连接操作后调用
         */
        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            final String local = localAddress == null ? "UNKNOWN" : RemotingHelper.parseSocketAddressAddr(localAddress);
            final String remote = remoteAddress == null ? "UNKNOWN" : RemotingHelper.parseSocketAddressAddr(remoteAddress);
            log.debug("NETTY CLIENT PIPELINE: CONNECT  {} => {}", local, remote);
            super.connect(ctx, remoteAddress, localAddress, promise);
            if (NettyRemotingClient.this.channelEventListener != null) {
                NettyRemotingClient.this.putNettyEvent(new NettyEvent(NettyEventType.OUT_BOUND_CONNECT, remote, ctx.channel()));
            }
        }

        /**
         * 进行断开连接操作后调用
         */
        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("NETTY CLIENT PIPELINE: DISCONNECT {}", remoteAddress);
            closeChannel(ctx.channel());
            super.disconnect(ctx, promise);
            if (NettyRemotingClient.this.channelEventListener != null) {
                NettyRemotingClient.this.putNettyEvent(new NettyEvent(NettyEventType.OUT_BOUND_DISCONNECT, remoteAddress, ctx.channel()));
            }
        }

        /**
         * 进行关闭操作后调用
         */
        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            log.debug("NETTY CLIENT PIPELINE: CLOSE {}", remoteAddress);
            closeChannel(ctx.channel());
            super.close(ctx, promise);
            NettyRemotingClient.this.failFast(ctx.channel());
        }

    }

}
