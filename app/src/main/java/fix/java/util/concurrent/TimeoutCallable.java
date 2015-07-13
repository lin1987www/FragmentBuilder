package fix.java.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2015/3/8.
 */
public class TimeoutCallable<T> implements Callable<T> {
    private final static ExecutorService defaultService = Executors
            .newCachedThreadPool();
    private Callable<T> task;
    private long timeout;
    private TimeUnit unit;
    private ExecutorService service = null;

    public TimeoutCallable(Callable<T> task, long timeout, TimeUnit unit) {
        this(task, timeout, unit, defaultService);
    }

    public TimeoutCallable(Callable<T> task, long timeout, TimeUnit unit,
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
    public T call() throws Exception {
        T returnedValue = null;
        Future<T> future = service.submit(task);
        try {
            returnedValue = future.get(timeout, unit);
        } catch (Throwable ex) {
            future.cancel(true);
            ExceptionHelper.throwException(task.toString(), ex);
        }
        return returnedValue;
    }
}
