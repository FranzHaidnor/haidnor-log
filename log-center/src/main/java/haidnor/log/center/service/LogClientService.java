package haidnor.log.center.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ZipUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import haidnor.log.center.app.LogCenterServer;
import haidnor.log.center.config.ServerNodeConfig;
import haidnor.log.center.model.ServerNodeLog;
import haidnor.log.center.model.param.GetLogFolderRequestParam;
import haidnor.log.center.model.param.GetLogRequestParam;
import haidnor.log.common.command.LogCenterCommand;
import haidnor.log.common.model.GetLogFolderRequest;
import haidnor.log.common.model.GetLogRequest;
import haidnor.log.common.model.LogLine;
import haidnor.log.common.util.Jackson;
import haidnor.log.common.util.LogUtil;
import haidnor.remoting.core.NettyRemotingServer;
import haidnor.remoting.protocol.RemotingCommand;
import haidnor.remoting.protocol.RemotingSysResponseCode;
import io.netty.channel.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LogClientService {
    @Autowired
    private LogCenterServer centerServer;

    @Autowired
    private ServerNodeManager nodeManager;

    @SneakyThrows
    public List<String> getLogFolder(GetLogFolderRequestParam param) {
        List<String> errorContext = new CopyOnWriteArrayList<>();
        List<CompletableFuture<Set<String>>> futureList = new ArrayList<>();
        for (String ip : param.getIp()) {
            // 发送并发请求
            CompletableFuture<Set<String>> future = CompletableFuture.supplyAsync(() -> {
                // 从连接器中获取客户端通道
                Channel channel = nodeManager.getChannel(ip);
                if (channel == null) {
                    errorContext.add(ip + " 节点已离线. 无法读取日志信息");
                    return new HashSet<>();
                }
                // 构建 netty 请求参数
                ServerNodeLog serverNodeLog = ServerNodeConfig.getServerNodeLog(ip, param.getServer());
                NettyRemotingServer nettyServer = centerServer.getNettyServer();
                GetLogFolderRequest getLogRequest = new GetLogFolderRequest();
                getLogRequest.setPath(serverNodeLog.getPath());
                RemotingCommand request = RemotingCommand.creatRequest(LogCenterCommand.GET_LOG_FOLDER, Jackson.toJsonBytes(getLogRequest));
                // 同步请求
                RemotingCommand response = nettyServer.invokeSync(channel, request);
                if (response.getResponseCode() == RemotingSysResponseCode.SYSTEM_ERROR) {
                    errorContext.add(ip + " " + response.getRemark());
                    return new HashSet<>();
                }
                return Jackson.toBean(response.getBody(), new TypeReference<HashSet<String>>() {
                });
            });
            futureList.add(future);
        }

        // 合并多个节点的日志信息
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();

        if (!errorContext.isEmpty()) {
            log.error("节点异常信息 {}", errorContext);
        }

        // 合并所有服务节点的日志文件夹名称
        Set<String> folderNameSet = new HashSet<>();
        for (CompletableFuture<Set<String>> future : futureList) {
            Set<String> content = future.get();
            folderNameSet.addAll(content);
        }

        // 按照日期降序排序
        List<String> list = new ArrayList<>(folderNameSet);
        return list.stream().sorted(Comparator.comparing(DateUtil::parse, Comparator.reverseOrder())).collect(Collectors.toList());
    }

    @SneakyThrows
    public String getLog(GetLogRequestParam param) {
        log.debug("haidnor.log.center.service.LogClientService.getLog");
        List<String> errorContext = new CopyOnWriteArrayList<>();

        // 并发请求多个服务器节点, 获取日志信息
        TimeInterval timer = DateUtil.timer();
        List<CompletableFuture<List<LogLine>>> futureList = new ArrayList<>();
        for (String ip : param.getIp()) {
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
                RemotingCommand response = nettyServer.invokeSync(channel, request,1000 * 30);
                if (response.getResponseCode() == RemotingSysResponseCode.SYSTEM_ERROR) {
                    errorContext.add(ip + " " + response.getRemark());
                    return null;
                }

                String content = new String(response.getBody(), StandardCharsets.UTF_8);

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

        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        log.debug("请求日志耗时 {} MS", timer.intervalMs());

        if (!errorContext.isEmpty()) {
            log.error("节点异常信息 {}", errorContext);
        }

        timer.restart();
        // 所有的日志行信息
        List<LogLine> logLineList = new ArrayList<>();
        for (CompletableFuture<List<LogLine>> future : futureList) {
            List<LogLine> content = future.get();
            if (content != null) {
                logLineList.addAll(content);
            }
        }

        String content = LogUtil.margeLog(logLineList, param.isShowIp());
        log.debug("合并日志耗时 {} MS", timer.intervalMs());

        return content;
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
