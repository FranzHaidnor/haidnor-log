package haidnor.log.client.core;

import haidnor.log.client.config.Configuration;
import haidnor.log.client.netty.listener.DefaultChannelEventListener;
import haidnor.log.client.netty.processor.GetLogFolderProcessor;
import haidnor.log.client.netty.processor.GetLogProcessor;
import haidnor.log.common.command.LogCenterCommand;
import haidnor.log.common.util.Timer;
import haidnor.remoting.RemotingClient;
import haidnor.remoting.core.NettyClientConfig;
import haidnor.remoting.core.NettyRemotingClient;
import haidnor.remoting.protocol.RemotingCommand;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LogClient {

    @Autowired
    private Configuration serverConfig;

    /**
     * 启动注册中心客户端, 向注册中心注册此服务信息
     */
    public void start() {
        NettyClientConfig config = new NettyClientConfig();
        config.setFrameMaxLength(Integer.MAX_VALUE);
        RemotingClient client = new NettyRemotingClient(config);

        client.registerChannelEventListener(new DefaultChannelEventListener());

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        client.registerProcessor(LogCenterCommand.GET_LOG, new GetLogProcessor(), executorService);
        client.registerProcessor(LogCenterCommand.GET_LOG_FOLDER, new GetLogFolderProcessor(), executorService);

        // 每 3 秒向日志中心发送一次心跳
        sendHeartbeat(client);
    }

    @SneakyThrows
    private void sendHeartbeat(RemotingClient client) {
        try {
            RemotingCommand request = RemotingCommand.creatRequest(LogCenterCommand.HEARTBEAT);
            client.invokeSync(serverConfig.getAddress(), request, 10 * 1000);
        } catch (Exception exception) {
            log.error("Failed to connect to the log center! Retry after 3 seconds");
        } finally {
            Timer.newTimeout(timeout -> sendHeartbeat(client), 3, TimeUnit.SECONDS);
        }
    }

}
