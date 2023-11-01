package haidnor.log.client.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class LogCenterConfig {

    @Value("${haidnor.log.center.address}")
    private String address;

}
