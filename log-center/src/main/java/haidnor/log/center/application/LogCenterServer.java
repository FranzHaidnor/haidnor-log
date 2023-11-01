package haidnor.log.center.application;

import haidnor.log.center.config.Configuration;
import haidnor.log.center.netty.ChannelConnectionEventListener;
import haidnor.log.center.service.ServerNodeManager;
import haidnor.log.center.netty.processor.HeartbeatService;
import haidnor.log.common.command.LogCenterCommand;
import haidnor.remoting.core.NettyRemotingServer;
import haidnor.remoting.core.NettyServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class LogCenterServer {

    private NettyRemotingServer server;

    @Autowired
    private Configuration config;

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
        server.registerProcessor(LogCenterCommand.HEARTBEAT, new HeartbeatService(serverNodeManager), executorService);

        server.start();
    }

    public NettyRemotingServer getNettyServer() {
        return server;
    }

}
