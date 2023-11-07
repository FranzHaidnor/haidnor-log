package haidnor.remoting.core;

public enum NettyEventType {

    // Override ChannelOutboundHandler ------------------------------------------------------------------------------
    OUT_BOUND_CONNECT,
    OUT_BOUND_DISCONNECT,
    OUT_BOUND_CLOSE,

    // Override ChannelInboundHandler ------------------------------------------------------------------------------
    IN_BOUND_ACTIVE,
    IN_BOUND_INACTIVE,
    IN_BOUND_EXCEPTION_CAUGHT,

    IN_BOUND_READER_IDLE,
    IN_BOUND_WRITER_IDLE,
    IN_BOUND_ALL_IDLE
}
