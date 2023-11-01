package haidnor.log.center.netty.processor;

import haidnor.log.center.service.ServerNodeManager;
import haidnor.remoting.core.NettyRequestProcessor;
import haidnor.remoting.protocol.RemotingCommand;
import haidnor.remoting.protocol.RemotingSysResponseCode;
import io.netty.channel.ChannelHandlerContext;

public class HeartbeatService implements NettyRequestProcessor {

    private final ServerNodeManager serverNodeManager;

    public HeartbeatService(ServerNodeManager serverNodeManager) {
        this.serverNodeManager = serverNodeManager;
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {
        serverNodeManager.register(ctx.channel());
        return RemotingCommand.createResponse(RemotingSysResponseCode.SUCCESS, "OK");
    }
}
