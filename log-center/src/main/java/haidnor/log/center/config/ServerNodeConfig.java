package haidnor.log.center.config;

import cn.hutool.core.io.FileUtil;
import haidnor.log.center.model.ServerNodeLog;
import haidnor.log.common.util.Jackson;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServerNodeConfig {

    public static List<ServerNodeLog> serverNodeLogs;

    public static Map<String/*服务节点ip*/, Map<String/*服务名*/, ServerNodeLog/*服务配置信息*/>> serverNodeMap;

    public static String configJson;

    @SneakyThrows
    public static void loadConfig(String configPath) {
        InputStream inputStream = FileUtil.getInputStream(configPath);
        assert inputStream != null;
        String json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        refreshConfig(json);
    }

    /**
     * 刷新配置
     *
     * @param json 配置文件 JSON
     */
    public static void refreshConfig(String json) {
        ServerNodeConfig.serverNodeLogs = Jackson.toList(json, ServerNodeLog.class);
        ServerNodeConfig.serverNodeMap = serverNodeLogs.stream().collect(Collectors.groupingBy(ServerNodeLog::getIp, Collectors.toMap(ServerNodeLog::getServer, Function.identity())));
        ServerNodeConfig.configJson = json;
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
