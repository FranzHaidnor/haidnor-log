package haidnor.log.center.core;

import haidnor.log.center.config.LogCenterConfig;
import haidnor.log.common.command.LogCenterCommand;
import haidnor.remoting.core.NettyRemotingServer;
import haidnor.remoting.core.NettyRequestProcessor;
import haidnor.remoting.core.NettyServerConfig;
import haidnor.remoting.protocol.RemotingCommand;
import haidnor.remoting.protocol.RemotingSysResponseCode;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class LogCenterServer {

    private NettyRemotingServer server;

    @Autowired
    private LogCenterConfig config;

    @Autowired
    private ChannelConnectionEventListener channelConnectionEventListener;

    @Autowired
    private ServerNodeManager serverNodeManager;

    public void start() {
        NettyServerConfig nettyConfig = new NettyServerConfig();
        nettyConfig.setTimeoutMillis(1000 * 30);
        nettyConfig.setListenPort(config.getPort());
        nettyConfig.setServerChannelMaxAllIdleTimeSeconds(60);
        server = new NettyRemotingServer(nettyConfig, LogCenterCommand.class);

        /* ------------------------------------------------------------------------------------------------------------ */
        server.registerChannelEventListener(channelConnectionEventListener);
        /* ------------------------------------------------------------------------------------------------------------ */

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        server.registerProcessor(LogCenterCommand.HEARTBEAT, new NettyRequestProcessor() {
            @Override
            public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {
                serverNodeManager.register(ctx.channel());
                return RemotingCommand.createResponse(RemotingSysResponseCode.SUCCESS, "OK");
            }
        }, executorService);

        server.start();
    }

    public NettyRemotingServer getNettyServer() {
        return server;
    }

}
