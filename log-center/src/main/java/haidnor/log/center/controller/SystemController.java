package haidnor.log.center.controller;

import haidnor.log.center.config.ServerNodeConfig;
import haidnor.log.center.model.ServerNodeLog;
import haidnor.log.center.service.ServerNodeConfigService;
import haidnor.log.center.service.system.LoggerService;
import haidnor.log.center.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/system")
public class SystemController {

    @Autowired
    private ServerNodeConfigService nodeConfigService;

    @Autowired
    private LoggerService loggerService;

    /**
     * 刷新节点配置
     */
    @PostMapping("/refreshConfig")
    public Result refreshConfig(String json) {
        nodeConfigService.refreshConfig(json);
        return Result.success();
    }

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
        return Result.success(collect);
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
