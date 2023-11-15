package haidnor.remoting.core;

import haidnor.remoting.protocol.RandomAccessFileResponseCommand;
import haidnor.remoting.protocol.RemotingCommand;
import haidnor.remoting.util.RemotingHelper;
import haidnor.remoting.util.RemotingUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@ChannelHandler.Sharable
@Slf4j
public class NettyEncoder extends MessageToByteEncoder<RemotingCommand> {

    @Override
    public void encode(ChannelHandlerContext ctx, RemotingCommand remotingCommand, ByteBuf out) {
        try {
            if (!(remotingCommand instanceof RandomAccessFileResponseCommand)) {
                ByteBuffer header = remotingCommand.encode();
                out.writeBytes(header);
            } else {
                ctx.writeAndFlush(remotingCommand);
            }
        } catch (Exception e) {
            log.error("encode exception, " + RemotingHelper.parseChannelRemoteAddr(ctx.channel()), e);
            if (remotingCommand != null) {
                log.error(remotingCommand.toString());
            }
            RemotingUtil.closeChannel(ctx.channel());
        }
    }

}
