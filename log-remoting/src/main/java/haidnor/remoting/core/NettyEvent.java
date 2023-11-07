package haidnor.remoting.core;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class NettyEvent {

    private final NettyEventType type;

    private final String remoteAddr;

    private final Channel channel;

}
