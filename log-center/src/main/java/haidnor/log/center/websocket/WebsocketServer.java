package haidnor.log.center.websocket;

import cn.hutool.extra.spring.SpringUtil;
import haidnor.log.center.config.Configuration;
import haidnor.log.center.model.param.GetLogRequestParam;
import haidnor.log.center.service.LogClientService;
import haidnor.log.common.util.Jackson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author wang xiang
 */
@ServerEndpoint("/webSocket")
@Service
@Slf4j
public class WebsocketServer {

    private final LogClientService clientService = SpringUtil.getBean(LogClientService.class);

    private final Configuration config = SpringUtil.getBean(Configuration.class);

    private GetLogRequestParam param = null;

    private boolean status = false;

    Session session;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        this.session = session;
        session.getBasicRemote().sendText("connection succeeded");
        new Thread(() -> {
            while (this.session != null) {
                try {
                    TimeUnit.MILLISECONDS.sleep(config.getLogPushInterval() * 1000);
                } catch (InterruptedException e) {
                    log.error("", e);
                }
                if (status) {
                    try {
                        if (this.session != null) {
                            String log = clientService.getLog(param);
                            session.getBasicRemote().sendText(log);
                        }
                    } catch (IOException e) {
                        log.error("", e);
                    }
                }
            }
        }).start();
    }

    @OnClose
    public void onClose(Session session) {
        this.session = null;
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        this.param = Jackson.toBean(message, GetLogRequestParam.class);
        this.status = param.isStatus();
        if (status) {
            String log = clientService.getLog(param);
            session.getBasicRemote().sendText(log);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        this.session = null;
    }

}
