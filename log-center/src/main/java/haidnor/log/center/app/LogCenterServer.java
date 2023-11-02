package haidnor.log.center.app;

import haidnor.log.center.config.Configuration;
import haidnor.log.center.netty.listener.DefaultChannelEventListener;
import haidnor.log.center.service.ServerNodeManager;
import haidnor.log.center.netty.processor.HeartbeatProcessor;
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
    private DefaultChannelEventListener defaultChannelEventListener;

    @Autowired
    private ServerNodeManager serverNodeManager;

    public void start() {
        NettyServerConfig nettyConfig = new NettyServerConfig();
        nettyConfig.setTimeoutMillis(1000 * 30);
        nettyConfig.setListenPort(config.getPort());
        nettyConfig.setServerChannelMaxAllIdleTimeSeconds(60);
        nettyConfig.setFrameMaxLength(Integer.MAX_VALUE);
        nettyConfig.setTimeoutMillis(60 * 1000);
        server = new NettyRemotingServer(nettyConfig, LogCenterCommand.class);

        /* ------------------------------------------------------------------------------------------------------------ */
        server.registerChannelEventListener(defaultChannelEventListener);
        /* ------------------------------------------------------------------------------------------------------------ */

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        server.registerProcessor(LogCenterCommand.HEARTBEAT, new HeartbeatProcessor(serverNodeManager), executorService);

        server.start();
    }

    public NettyRemotingServer getNettyServer() {
        return server;
    }

}
