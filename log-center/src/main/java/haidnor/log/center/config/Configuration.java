package haidnor.log.center.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class Configuration {

    @Value("${haidnor.log.center.port}")
    private int port;

    @Value("${haidnor.log.center.logPushIntervalSecond}")
    private long logPushInterval;

}
