package haidnor.remoting;


import haidnor.remoting.core.NettyRequestProcessor;
import haidnor.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandler;

import java.util.concurrent.ExecutorService;

public interface RemotingClient extends RemotingService {

    RemotingCommand invokeSync(final String addr, final RemotingCommand request);

    RemotingCommand invokeSync(final String addr, final RemotingCommand request, final long timeoutMillis);

    void invokeAsync(final String addr, final RemotingCommand request, final InvokeCallback invokeCallback);

    void invokeAsync(final String addr, final RemotingCommand request, final long timeoutMillis, final InvokeCallback invokeCallback);

    void invokeOneway(final String addr, final RemotingCommand request);

    void invokeOneway(final String addr, final RemotingCommand request, final long timeoutMillis);
    /**
     * 注册请求处理器
     * 将处理器放入 HashMap<Integer, Pair<NettyRequestProcessor, ExecutorService>> processorTable 中
     */
    <T extends Enum<T>> void  registerProcessor(T commandEnum, final NettyRequestProcessor processor, final ExecutorService executor);

    ExecutorService getCallbackExecutor();

    void setCallbackExecutor(final ExecutorService callbackExecutor);

    boolean isChannelWritable(final String addr);

    void addFirstChannelHandler(ChannelHandler channelHandler);
    void addLastChannelHandler(ChannelHandler channelHandler);

}
