package haidnor.remoting.core;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NettyClientConfig {

    /**
     * Worker thread number
     */
    private int clientWorkerThreads = 4;
    private int clientCallbackExecutorThreads = Runtime.getRuntime().availableProcessors();
    private int clientOnewaySemaphoreValue = 65535;
    private int clientAsyncSemaphoreValue = 65535;
    private int connectTimeoutMillis = 3000;
    private long channelNotActiveInterval = 1000 * 60;
    private long timeoutMillis = 5000;

    /**
     * IdleStateEvent will be triggered when neither read nor write was performed for
     * the specified period of this time. Specify {@code 0} to disable
     */
    private int clientChannelMaxReaderIdleTimeSeconds = 0;
    /**
     * IdleStateEvent will be triggered when neither read nor write was performed for
     * the specified period of this time. Specify {@code 0} to disable
     */
    private int clientChannelMaxWriterIdleTimeSeconds = 0;
    /**
     * IdleStateEvent will be triggered when neither read nor write was performed for
     * the specified period of this time. Specify {@code 0} to disable
     */
    private int clientChannelMaxAllIdleTimeSeconds = 0;

    private int clientSocketSndBufSize = 65535;
    private int clientSocketRcvBufSize = 65535;
    private int frameMaxLength = 65535;
    private boolean clientPooledByteBufAllocatorEnable = false;
    private boolean clientCloseSocketIfTimeout = true;
    private boolean useTLS;

}
