package haidnor.remoting;

import io.netty.channel.Channel;


public interface ChannelEventListener {
    default void onOutBoundConnect(final String remoteAddr, final Channel channel) {
    }

    default void onOutBoundDisconnect(final String remoteAddr, final Channel channel) {
    }

    default void onOutBoundClose(final String remoteAddr, final Channel channel) {
    }

    default void onInBoundActive(final String remoteAddr, final Channel channel) {
    }

    default void onInBoundInactive(final String remoteAddr, final Channel channel) {
    }

    default void onInBoundExceptionCaught(final String remoteAddr, final Channel channel) {
    }

    default void onInBoundReaderIdle(final String remoteAddr, final Channel channel) {
    }

    default void onInBoundWriterIdle(final String remoteAddr, final Channel channel) {
    }

    default void onInBoundAllIdle(final String remoteAddr, final Channel channel) {
    }
}
