package haidnor.log.client.core;

import haidnor.log.client.config.LogCenterConfig;
import haidnor.log.client.processor.GetLogService;
import haidnor.log.common.command.LogCenterCommand;
import haidnor.remoting.ChannelEventListener;
import haidnor.remoting.RemotingClient;
import haidnor.remoting.core.NettyClientConfig;
import haidnor.remoting.core.NettyRemotingClient;
import haidnor.remoting.protocol.RemotingCommand;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LogClient {

    /**
     * RPC 服务端配置参数
     */
    @Autowired
    private LogCenterConfig serverConfig;

    /**
     * 启动注册中心客户端, 向注册中心注册此服务信息
     */
    public void start() {
        NettyClientConfig config = new NettyClientConfig();
        RemotingClient client = new NettyRemotingClient(config);

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        client.registerChannelEventListener(new ChannelEventListener() {
            @Override
            public void onChannelClose(String remoteAddr, Channel channel) {
                log.info("与服务器断开连接");
            }
        });

        // 服务器请求读取日志内容
        client.registerProcessor(LogCenterCommand.GET_LOG, new GetLogService(), executorService);

        // 向日志中心注册
        registerCenter(client);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                registerCenter(client);
            }
        }, 20000, 20000);
    }

    @SneakyThrows
    private void registerCenter(RemotingClient client) {
        try {
            RemotingCommand request = RemotingCommand.creatRequest(LogCenterCommand.HEARTBEAT);
            client.invokeSync(serverConfig.getAddress(), request);
        } catch (Exception exception) {
            log.error("Failed to connect to the log center! Retry after 5 seconds.", exception);
            TimeUnit.SECONDS.sleep(5);
            CompletableFuture.runAsync(() -> registerCenter(client));
        }
    }

}
