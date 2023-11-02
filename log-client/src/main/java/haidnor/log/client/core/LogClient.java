package haidnor.log.client.core;

import haidnor.log.client.config.Configuration;
import haidnor.log.client.netty.listener.DefaultChannelEventListener;
import haidnor.log.client.netty.processor.GetLogProcessor;
import haidnor.log.common.command.LogCenterCommand;
import haidnor.remoting.RemotingClient;
import haidnor.remoting.core.NettyClientConfig;
import haidnor.remoting.core.NettyRemotingClient;
import haidnor.remoting.protocol.RemotingCommand;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class LogClient {

    /**
     * RPC 服务端配置参数
     */
    @Autowired
    private Configuration serverConfig;

    /**
     * 启动注册中心客户端, 向注册中心注册此服务信息
     */
    public void start() {
        NettyClientConfig config = new NettyClientConfig();
        RemotingClient client = new NettyRemotingClient(config);

        client.registerChannelEventListener(new DefaultChannelEventListener());

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        client.registerProcessor(LogCenterCommand.GET_LOG, new GetLogProcessor(), executorService);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sendHeartbeat(client);
            }
        }, 0, 5 * 1000);
    }

    @SneakyThrows
    private void sendHeartbeat(RemotingClient client) {
        try {
            RemotingCommand request = RemotingCommand.creatRequest(LogCenterCommand.HEARTBEAT);
            client.invokeSync(serverConfig.getAddress(), request);
        } catch (Exception exception) {
            log.error("Failed to connect to the log center! Retry after 5 seconds");
        }
    }

}
