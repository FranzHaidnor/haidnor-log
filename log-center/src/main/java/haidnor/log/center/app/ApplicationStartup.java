package haidnor.log.center.app;

import haidnor.log.center.config.ServerNodeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 启动远程调用服务端. 连接注册注册中心注册此服务
 */
@Service
@Slf4j
public class ApplicationStartup implements ApplicationRunner {

    @Autowired
    private LogCenterServer server;

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption("configPath")) {
            throw new RuntimeException("缺少服务器节点配置文件路径");
        }
        List<String> values = args.getOptionValues("configPath");
        String configPath = values.get(0);
        log.info("config file path: {}", configPath);
        ServerNodeConfig.loadConfig(configPath);

        server.start();
    }

}