package haidnor.log.center.controller;

import haidnor.log.center.core.ServerNodeConfig;
import haidnor.log.center.service.IServerNodeConfigService;
import haidnor.log.center.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system")
public class SystemController {

    @Autowired
    private IServerNodeConfigService serverNodeConfigService;

    /**
     * 刷新节点配置
     */
    @PostMapping("/refreshConfig")
    public Result refreshConfig(String json) {
        serverNodeConfigService.refreshConfig(json);
        return Result.success();
    }

    @PostMapping("/getConfig")
    public String getConfig() {
        return ServerNodeConfig.configJson;
    }

}
