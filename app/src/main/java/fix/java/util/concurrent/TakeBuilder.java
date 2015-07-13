package fix.java.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2015/3/12.
 */
public class TakeBuilder<T extends Take<?>> implements Callable<T> {
    public final T task;
    private Callable<T> wrapper;

    public T getTask() {
        return task;
    }

    public TakeBuilder(T task) {
        if (task == null) {
            throw new NullPointerException();
        }
        this.task = task;
        this.wrapper = (Callable<T>) task;
    }

    public TakeBuilder<T> retry(int maxTimes) {
        this.wrapper = new RetryCallable<T>(this.wrapper, maxTimes);
        return this;
    }

    public TakeBuilder<T> timeout(long timeout, TimeUnit unit) {
        this.wrapper = new TimeoutCallable<T>(this.wrapper, timeout, unit);
        return this;
    }

    @Override
    public T call() throws Exception {
        try {
            task.startTimeMillis = System.currentTimeMillis();
            wrapper.call();
        } catch (Throwable ex) {
            task.setThrowable(ex);
        } finally {
            task.stopTimeMillis = System.currentTimeMillis();
        }
        return (T) task;
    }
}
