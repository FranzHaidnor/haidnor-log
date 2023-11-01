package haidnor.log.center.service.impl;

import haidnor.log.center.core.ServerNodeConfig;
import haidnor.log.center.service.IServerNodeConfigService;
import org.springframework.stereotype.Service;

@Service
public class ServerNodeConfigServiceImpl implements IServerNodeConfigService {

    @Override
    public void refreshConfig(String json) {
        ServerNodeConfig.refreshConfig(json);
    }

}
