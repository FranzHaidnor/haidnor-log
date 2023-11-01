package haidnor.log.center.controller;

import haidnor.log.center.application.ServerNodeConfig;
import haidnor.log.center.service.ServerNodeConfigService;
import haidnor.log.center.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system")
public class SystemController {

    @Autowired
    private ServerNodeConfigService nodeConfigService;

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

}
