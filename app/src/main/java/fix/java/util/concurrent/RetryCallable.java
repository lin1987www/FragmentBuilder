package fix.java.util.concurrent;

import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2015/3/8.
 */
public class RetryCallable<T> implements Callable<T> {
    private Callable<T> task;
    private int maxTimes = 0;

    public RetryCallable(Callable<T> task, int maxTimes) {
        if (task == null) {
            throw new NullPointerException();
        }
        this.task = task;
        this.maxTimes = maxTimes;
    }

    @Override
    public T call() throws Exception {
        T returnedValue = null;
        int retryTimes = maxTimes;
        while (true) {
            try {
                returnedValue = task.call();
                break;
            } catch (Throwable ex) {
                if (--retryTimes >= 0) {
                    continue;
                } else {
                    ExceptionHelper.throwException(task.toString(), ex);
                }
            }
        }
        return returnedValue;
    }
}
