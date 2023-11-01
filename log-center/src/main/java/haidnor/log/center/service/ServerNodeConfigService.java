package haidnor.log.center.service;

import haidnor.log.center.application.ServerNodeConfig;
import org.springframework.stereotype.Service;

@Service
public class ServerNodeConfigService {

    public void refreshConfig(String json) {
        ServerNodeConfig.refreshConfig(json);
    }

}
