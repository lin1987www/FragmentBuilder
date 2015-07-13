package fix.java.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2015/3/6.
 */
public class ScheduledExecutorFix extends ScheduledThreadPoolExecutor {
    public ScheduledExecutorFix(int corePoolSize) {
        super(corePoolSize);
    }

    public ScheduledExecutorFix(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public ScheduledExecutorFix(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public ScheduledExecutorFix(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command,
                                       long delay,
                                       TimeUnit unit) {
        return super.schedule(wrap(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           long delay,
                                           TimeUnit unit) {
        return super.schedule(wrap(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit) {
        return super.scheduleAtFixedRate(wrap(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit) {
        return super.scheduleWithFixedDelay(wrap(command), initialDelay, delay, unit);
    }

    public static Runnable wrap(Runnable task) {
        if (!(task instanceof CatchRunnable)) {
            task = new CatchRunnable(task);
        }
        return task;
    }

    public static <T> Callable<T> wrap(Callable<T> task) {
        if (!(task instanceof CatchCallable)) {
            task = new CatchCallable<T>(task);
        }
        return task;
    }
}
