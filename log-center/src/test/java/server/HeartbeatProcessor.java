package server;

import haidnor.remoting.core.NettyRequestProcessor;
import haidnor.remoting.protocol.RemotingCommand;
import haidnor.remoting.protocol.RemotingSysResponseCode;
import io.netty.channel.ChannelHandlerContext;

public class HeartbeatProcessor implements NettyRequestProcessor {
    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {
        return RemotingCommand.createResponse(RemotingSysResponseCode.SUCCESS, "OK");
    }
}