package haidnor.remoting;

import haidnor.remoting.common.Pair;
import haidnor.remoting.core.NettyRequestProcessor;
import haidnor.remoting.protocol.RemotingCommand;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;

import java.util.concurrent.ExecutorService;

public interface RemotingServer extends RemotingService {

    /**
     * 注册请求处理器
     * 将处理器放入 HashMap<Integer, Pair<NettyRequestProcessor, ExecutorService>> processorTable 中
     */
    <T extends Enum<T>> void  registerProcessor(T commandEnum, final NettyRequestProcessor processor, final ExecutorService executor);

    void registerDefaultProcessor(final NettyRequestProcessor processor, final ExecutorService executor);

    int localListenPort();

    Pair<NettyRequestProcessor, ExecutorService> getProcessorPair(final int requestCode);

    RemotingCommand invokeSync(final Channel channel, final RemotingCommand request, final long timeoutMillis);

    RemotingCommand invokeSync(final Channel channel, final RemotingCommand request);

    void invokeAsync(final Channel channel, final RemotingCommand request, final long timeoutMillis, final InvokeCallback invokeCallback);

    void invokeAsync(final Channel channel, final RemotingCommand request, final InvokeCallback invokeCallback);

    void invokeOneway(final Channel channel, final RemotingCommand request, final long timeoutMillis);

    void invokeOneway(final Channel channel, final RemotingCommand request);

    void addFirstChannelHandler(ChannelHandler channelHandler);
    void addLastChannelHandler(ChannelHandler channelHandler);

}
