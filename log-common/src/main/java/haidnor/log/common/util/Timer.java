package haidnor.log.common.util;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

public class Timer {

    private static final HashedWheelTimer timer = new HashedWheelTimer();

    public static Timeout newTimeout(TimerTask task, long delay, TimeUnit unit) {
        return timer.newTimeout(task, delay, unit);
    }

    public static long pendingTimeouts() {
        return timer.pendingTimeouts();
    }

}