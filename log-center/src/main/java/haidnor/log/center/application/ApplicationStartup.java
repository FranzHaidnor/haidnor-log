package haidnor.log.center.application;

import haidnor.log.center.core.LogCenterServer;
import haidnor.log.center.core.ServerNodeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

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
        ServerNodeConfig.loadConfig();
        server.start();
    }

}