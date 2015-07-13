package fix.java.util.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2015/3/8.
 */
public class TimeoutRunnable implements Runnable {
    private final static ExecutorService defaultService = Executors
            .newCachedThreadPool();
    private Runnable task;
    private long timeout;
    private TimeUnit unit;
    private ExecutorService service = null;

    public TimeoutRunnable(Runnable task, long timeout, TimeUnit unit) {
        this(task, timeout, unit, defaultService);
    }

    public TimeoutRunnable(Runnable task, long timeout, TimeUnit unit,
                           ExecutorService service) {
        if (task == null) {
            throw new NullPointerException();
        }
        this.task = task;
        this.timeout = timeout;
        this.unit = unit;
        this.service = service;
    }

    @Override
    public void run() {
        Future<?> future = service.submit(task);
        try {
            future.get(timeout, unit);
        } catch (Throwable ex) {
            future.cancel(true);
            ExceptionHelper.throwRuntimeException(task.toString(), ex);
        }
    }
}
