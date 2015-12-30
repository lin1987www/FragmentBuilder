package fix.java.util.concurrent;

import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2015/3/6.
 */
public class CatchCallable<T> implements Callable<T> {
    private Callable<T> task;

    public CatchCallable(Callable<T> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        this.task = task;
    }

    @Override
    public T call() throws Exception {
        T returnedValue = null;
        try {
            returnedValue = task.call();
        } catch (Throwable ex) {
            ExceptionHelper.throwException(task.toString(), ex);
        }
        return returnedValue;
    }
}
