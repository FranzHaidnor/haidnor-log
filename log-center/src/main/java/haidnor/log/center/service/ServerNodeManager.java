package haidnor.log.center.service;

import haidnor.remoting.util.RemotingHelper;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ServerNodeManager {

    private final Map<String, Channel> channelMap = new HashMap<>();

    public synchronized void register(Channel channel) {
        String ip = getIPAddress(channel);
        channelMap.put(ip, channel);
        log.info("{}", channelMap);
    }

    public synchronized void unregister(Channel channel) {
        String ip = getIPAddress(channel);
        channelMap.remove(ip);
        log.info("{}", channelMap);
    }

    public Channel getChannel(String ip) {
        return channelMap.get(ip);
    }

    private static String getIPAddress(Channel channel) {
        String addrRemote = RemotingHelper.parseChannelRemoteAddr(channel);
        int colonIndex = addrRemote.indexOf(":");
        if (colonIndex != -1) {
            return addrRemote.substring(0, colonIndex);
        }
        return addrRemote;
    }

}
