package haidnor.log.center.controller;

import cn.hutool.core.text.StrBuilder;
import haidnor.log.center.application.LogCenterServer;
import haidnor.log.center.config.ServerNodeConfig;
import haidnor.log.center.service.ServerNodeManager;
import haidnor.log.center.model.ServerNodeLog;
import haidnor.log.center.model.param.GetLogRequestParam;
import haidnor.log.common.command.LogCenterCommand;
import haidnor.log.common.model.GetLogRequest;
import haidnor.log.common.model.LogLine;
import haidnor.log.common.util.Jackson;
import haidnor.log.common.util.LogUtil;
import haidnor.remoting.core.NettyRemotingServer;
import haidnor.remoting.protocol.RemotingCommand;
import haidnor.remoting.protocol.RemotingSysResponseCode;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @Autowired
    private LogCenterServer centerServer;

    @Autowired
    private ServerNodeManager nodeManager;

    @PostMapping("/getLog")
    public String getLog(@RequestBody GetLogRequestParam param) throws ExecutionException, InterruptedException {
        List<String> errorContext = new CopyOnWriteArrayList<>();

        List<CompletableFuture<List<LogLine>>> futureList = new ArrayList<>();
        for (String ip : param.getIps()) {
            // 发送并发请求
            CompletableFuture<List<LogLine>> future = CompletableFuture.supplyAsync(() -> {
                // 从连接器中获取客户端通道
                Channel channel = nodeManager.getChannel(ip);
                if (channel == null) {
                    errorContext.add(ip + " 节点已离线. 无法读取日志信息");
                    return null;
                }
                // 构建 netty 请求参数
                ServerNodeLog serverNodeLog = ServerNodeConfig.getServerNodeLog(ip, param.getServer());
                NettyRemotingServer nettyServer = centerServer.getNettyServer();
                GetLogRequest getLogRequest = new GetLogRequest();
                getLogRequest.setDay(param.getDay());
                getLogRequest.setPath(serverNodeLog.getPath());
                getLogRequest.setRows(param.getRows());
                getLogRequest.setFileName(param.getFileName());
                RemotingCommand request = RemotingCommand.creatRequest(LogCenterCommand.GET_LOG, Jackson.toJsonBytes(getLogRequest));
                // 同步请求
                RemotingCommand response = nettyServer.invokeSync(channel, request);
                if (response.getCode() == RemotingSysResponseCode.SYSTEM_ERROR) {
                    errorContext.add(ip + " " + response.getRemark());
                    return null;
                }
                String content = new String(response.getBody());

                List<LogLine> result = new ArrayList<>();
                String[] logLines = content.split("\n");

                String ipStr = ipv4Format(ip);
                for (String logLine : logLines) {
                    result.add(new LogLine(logLine, ipStr));
                }
                return result;
            });
            futureList.add(future);
        }

        // 合并多个节点的日志信息
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();

        if (!errorContext.isEmpty()) {
            log.error("节点异常信息 {}", errorContext);
        }

        // 所有的日志行信息
        List<LogLine> logLineList = new ArrayList<>();
        for (CompletableFuture<List<LogLine>> future : futureList) {
            List<LogLine> content = future.get();
            if (content != null) {
                logLineList.addAll(content);
            }
        }
        List<String> log = LogUtil.margeLog(logLineList, param.isShowIp());

        return String.join("\n", log);
    }

    /**
     * ip v4 地址格式化, 补全 15 位字符串
     */
    private String ipv4Format(String ipV4) {
        String ip = ipV4;
        if (ipV4.length() < 15) {
            int n = 15 - ipV4.length();
            StrBuilder strBuilder = new StrBuilder(ipV4);
            for (int i = 0; i < n; i++) {
                strBuilder.append(" ");
            }
            ip = strBuilder.toString();
        }
        return ip;
    }
}
