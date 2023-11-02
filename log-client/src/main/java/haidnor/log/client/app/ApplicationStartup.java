package haidnor.log.client.app;

import haidnor.log.client.core.LogClient;
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
    private LogClient logClient;

    @Override
    public void run(ApplicationArguments args) {
        logClient.start();
    }

}