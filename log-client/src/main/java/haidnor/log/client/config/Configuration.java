package haidnor.log.client.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class Configuration {

    /**
     * 日志中心地址
     */
    private String address;

}
