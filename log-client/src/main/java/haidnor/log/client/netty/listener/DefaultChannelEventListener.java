package haidnor.log.client.netty.listener;

import haidnor.remoting.ChannelEventListener;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultChannelEventListener implements ChannelEventListener {

    @Override
    public void onInBoundActive(String remoteAddr, Channel channel) {
        log.info("连接服务器成功");
    }

    @Override
    public void onOutBoundClose(String remoteAddr, Channel channel) {
        log.info("已关闭与服务器的连接");
    }

}
