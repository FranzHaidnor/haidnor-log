package haidnor.log.center.core;

import haidnor.remoting.ChannelEventListener;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChannelConnectionEventListener implements ChannelEventListener {

    @Autowired
    private ServerNodeManager serverNodeManager;

    @Override
    public void onChannelClose(String s, Channel channel) {
        channel.close();
        serverNodeManager.unregister(channel);
    }

    @Override
    public void onChannelAllIdle(String remoteAddr, Channel channel) {
        channel.close();
        serverNodeManager.unregister(channel);
    }

}
