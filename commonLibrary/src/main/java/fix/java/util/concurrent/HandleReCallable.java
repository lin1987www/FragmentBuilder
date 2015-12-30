package fix.java.util.concurrent;

import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2015/3/6.
 */
public class HandleReCallable<T> implements Callable<T> {
    private IReCallable<T> task;

    public HandleReCallable(IReCallable<T> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        this.task = task;
    }

    @Override
    public T call() throws Exception {
        T returnedValue = null;
        while (true) {
            try {
                returnedValue = task.call();
                break;
            } catch (Throwable ex) {
                if (task.handleException(ex)) {
                    continue;
                } else {
                    ExceptionHelper.throwException(task.toString(), ex);
                }
            }
        }
        return returnedValue;
    }
}
