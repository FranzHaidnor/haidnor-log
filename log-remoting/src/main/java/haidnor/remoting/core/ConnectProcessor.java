package haidnor.remoting.core;

import io.netty.channel.ChannelHandlerContext;

public interface ConnectProcessor {

    void connect(ChannelHandlerContext ctx);

    void disconnect(ChannelHandlerContext ctx, Throwable cause);
}
