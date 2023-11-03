package io;

import haidnor.remoting.core.NettyRemotingServer;
import haidnor.remoting.core.NettyServerConfig;
import lombok.extern.slf4j.Slf4j;
import server.Command;
import server.HeartbeatProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Server {

    public static void main(String[] args) {
        NettyServerConfig nettyConfig = new NettyServerConfig();
        nettyConfig.setTimeoutMillis(1000 * 30);
        nettyConfig.setListenPort(8085);
        nettyConfig.setServerChannelMaxAllIdleTimeSeconds(60);
        nettyConfig.setFrameMaxLength(Integer.MAX_VALUE);
        nettyConfig.setTimeoutMillis(60 * 1000);
        NettyRemotingServer server = new NettyRemotingServer(nettyConfig, Command.class);


        ExecutorService executorService = Executors.newFixedThreadPool(4);
        server.registerProcessor(Command.HEARTBEAT, new HeartbeatProcessor(), executorService);

        server.start();
    }


}


