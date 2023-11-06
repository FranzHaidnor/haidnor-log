package haidnor.log.client.app;

import haidnor.log.client.config.Configuration;
import haidnor.log.client.core.LogClient;
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
    private Configuration configuration;

    @Autowired
    private LogClient logClient;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!args.containsOption("centerAddress")) {
            throw new RuntimeException("缺少日志中心地址启动参数");
        }
        List<String> values = args.getOptionValues("centerAddress");
        configuration.setAddress(values.get(0));
        log.info("log center address: {}", configuration.getAddress());


        logClient.start();
    }
}