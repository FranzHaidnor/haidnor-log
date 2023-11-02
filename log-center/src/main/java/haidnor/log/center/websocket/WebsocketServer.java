package haidnor.log.center.websocket;

import cn.hutool.extra.spring.SpringUtil;
import haidnor.log.center.model.param.GetLogRequestParam;
import haidnor.log.center.service.LogClientService;
import haidnor.log.center.util.Jackson;
import org.springframework.beans.factory.annotation.Autowired;
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
public class WebsocketServer {

    private LogClientService logClientService = SpringUtil.getBean(LogClientService.class);

    private GetLogRequestParam param = new GetLogRequestParam();

    private boolean status = false;
    /**
     * webSocket 会话
     */
    Session session;

    /**
     * 建立连接成功调用,返回客户端 session id
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        this.session = session;
        session.getBasicRemote().sendText("connection succeeded");
        new Thread(() -> {
            while (this.session != null) {
                try {
                    TimeUnit.MILLISECONDS.sleep(3 * 1000);  // 默认 3 秒查询一次
                } catch (InterruptedException e) {
                }
                if (status) {
                    try {
                        if (this.session != null) {
                            // 查询日志
                            String log = logClientService.getLog(param);
                            session.getBasicRemote().sendText(log);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
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
        String log = logClientService.getLog(param);
        session.getBasicRemote().sendText(log);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        this.session = null;
    }

}
