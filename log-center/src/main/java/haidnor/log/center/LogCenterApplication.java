package haidnor.log.center;

import cn.hutool.extra.spring.EnableSpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableSpringUtil
public class LogCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogCenterApplication.class, args);
    }

}
