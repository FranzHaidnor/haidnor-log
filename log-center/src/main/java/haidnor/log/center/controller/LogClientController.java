package haidnor.log.center.controller;

import haidnor.log.center.model.param.GetLogFolderRequestParam;
import haidnor.log.center.model.param.GetLogRequestParam;
import haidnor.log.center.service.LogClientService;
import haidnor.log.center.util.Result;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
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

    @SneakyThrows
    @PostMapping("/download")
    public void downloadLog(HttpServletResponse response, @RequestBody GetLogRequestParam param) {
//        String type = new MimetypesFileTypeMap().getContentType(param.getFileName());
//        response.setHeader("Content-type", type);
        response.setHeader("Content-Disposition", "attachment;filename=" + param.getFileName());
        String string = logClientService.getLog(param);
        response.getOutputStream().write(string.getBytes(StandardCharsets.UTF_8));
    }

}
