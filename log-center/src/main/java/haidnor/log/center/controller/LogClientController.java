package haidnor.log.center.controller;

import haidnor.log.center.model.param.GetLogFolderRequestParam;
import haidnor.log.center.service.LogClientService;
import haidnor.log.center.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logClient")
public class LogClientController {

    @Autowired
    private LogClientService logClientService;

    @PostMapping("/getLogFolder")
    public Result getLogFolder(@RequestBody GetLogFolderRequestParam param) {
        List<String> logFolder = logClientService.getLogFolder(param);
        return Result.success(logFolder);
    }

}
