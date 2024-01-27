package haidnor.log.center.config;

import cn.hutool.core.io.FileUtil;
import haidnor.log.center.model.ServerNodeLog;
import haidnor.log.common.util.Timer;
import haidnor.log.common.util.Jackson;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class ServerNodeConfig {

    public static List<ServerNodeLog> serverNodeLogs;

    public static Map<String/*服务节点ip*/, Map<String/*服务名*/, ServerNodeLog/*服务配置信息*/>> serverNodeMap;

    public static String configJson;

    // 日志文件上一次的修改时间
    private static long lastModified;

    @SneakyThrows
    public static void loadConfig(String configPath) {
        try {
            File file = FileUtil.file(configPath);
            if (lastModified == 0L) {
                lastModified = file.lastModified();
                refreshConfig(file);
            }
            // 判断文件修改时间是否发生变化
            if (file.lastModified() != lastModified) {
                lastModified = file.lastModified();
                refreshConfig(file);
            }
        } catch (Exception exception) {
            log.error("load log center config filed", exception);
        } finally {
            Timer.newTimeout(timeout -> loadConfig(configPath), 10, TimeUnit.SECONDS);
        }
    }

    /**
     * 刷新配置
     */
    public static void refreshConfig(File file) {
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            String json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

            ServerNodeConfig.serverNodeLogs = Jackson.toList(json, ServerNodeLog.class);
            ServerNodeConfig.serverNodeMap = serverNodeLogs.stream().collect(Collectors.groupingBy(ServerNodeLog::getIp, Collectors.toMap(ServerNodeLog::getServer, Function.identity())));
            ServerNodeConfig.configJson = json;
            log.info("load log center config succeeded");
        } catch (Exception exception) {
            log.error("load log center config filed", exception);
        }
    }

    public static ServerNodeLog getServerNodeLog(String ip, String serverName) {
        Map<String, ServerNodeLog> map = serverNodeMap.get(ip);
        if (map == null) {
            throw new RuntimeException("无法找到节点配置");
        }
        ServerNodeLog nodeLog = map.get(serverName);
        if (nodeLog == null) {
            throw new RuntimeException("无法找到节点服务配置");
        }
        return nodeLog;
    }

}
