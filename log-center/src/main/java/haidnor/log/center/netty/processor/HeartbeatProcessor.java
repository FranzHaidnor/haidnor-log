package haidnor.log.center.netty.processor;

import haidnor.log.center.service.ServerNodeManager;
import haidnor.remoting.core.NettyRequestProcessor;
import haidnor.remoting.protocol.RemotingCommand;
import haidnor.remoting.protocol.RemotingSysResponseCode;
import io.netty.channel.ChannelHandlerContext;

public class HeartbeatProcessor implements NettyRequestProcessor {

    private final ServerNodeManager serverNodeManager;

    public HeartbeatProcessor(ServerNodeManager serverNodeManager) {
        this.serverNodeManager = serverNodeManager;
    }

    @Override
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {
        serverNodeManager.register(ctx.channel());
        return RemotingCommand.createResponse(RemotingSysResponseCode.SUCCESS);
    }

}
