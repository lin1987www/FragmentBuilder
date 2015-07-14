package fix.java.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2015/3/12.
 */
public class TakeWrap<T extends Take<?>> implements Callable<T> {
    public final T task;
    private Callable<T> wrap;

    public T getTask() {
        return task;
    }

    public TakeWrap(T task) {
        if (task == null) {
            throw new NullPointerException();
        }
        this.task = task;
        this.wrap = (Callable<T>) task;
    }

    public TakeWrap<T> retry(int maxTimes) {
        this.wrap = new RetryCallable<T>(this.wrap, maxTimes);
        return this;
    }

    public TakeWrap<T> timeout(long timeout, TimeUnit unit) {
        this.wrap = new TimeoutCallable<T>(this.wrap, timeout, unit);
        return this;
    }

    @Override
    public T call() throws Exception {
        try {
            task.startTimeMillis = System.currentTimeMillis();
            wrap.call();
        } catch (Throwable ex) {
            task.setThrowable(ex);
        } finally {
            task.stopTimeMillis = System.currentTimeMillis();
        }
        return (T) task;
    }
}
