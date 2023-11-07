package haidnor.log.center.controller;

import haidnor.log.center.config.ServerNodeConfig;
import haidnor.log.center.model.ServerNodeLog;
import haidnor.log.center.service.ServerNodeManager;
import haidnor.log.center.service.system.LoggerService;
import haidnor.log.center.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/system")
public class SystemController {

    @Autowired
    private LoggerService loggerService;

    @Autowired
    private ServerNodeManager serverNodeManager;

    @PostMapping("/getConfig")
    public String getConfig() {
        return ServerNodeConfig.configJson;
    }

    /**
     * 查询所有的服务名称
     */
    @PostMapping("/getServerList")
    public Result getServerList() {
        Set<String> collect = ServerNodeConfig.serverNodeLogs.stream()
                .map(ServerNodeLog::getServer)
                .collect(Collectors.toSet());
        return Result.success(collect);
    }

    @PostMapping("/getNodeIp")
    public Result getNodeIp(@RequestParam String serverName) {
        Set<String> collect = ServerNodeConfig.serverNodeLogs.stream()
                .filter(serverNodeLog -> serverNodeLog.getServer().equals(serverName))
                .map(ServerNodeLog::getIp)
                .collect(Collectors.toSet());

        List<Map<String, Object>> list = new ArrayList<>();
        for (String ip : collect) {
            Map<String, Object> item = new HashMap<>();
            list.add(item);
            item.put("value", ip);
            boolean register = serverNodeManager.isRegister(ip);
            if (register) {
                item.put("label", ip);
                item.put("disabled", false);
            } else {
                item.put("label", ip + " (已离线)");
                item.put("disabled", true);
            }
        }

        return Result.success(list);
    }

    @PostMapping("/updateLogLevel")
    public Result updateLogLevel(String packageName, String level) {
        loggerService.updateLogLevel(packageName, level);
        return Result.success();
    }

    @PostMapping("/getLogLevel")
    public String getLogLevel(String packageName) {
        return loggerService.getLogLevel(packageName);
    }

}
