package fix.java.util.concurrent;

/**
 * Created by Administrator on 2015/3/6.
 */
public class HandleReRunnable implements Runnable {
    private IReRunnable task;

    public HandleReRunnable(IReRunnable task) {
        if (task == null) {
            throw new NullPointerException();
        }
        this.task = task;
    }

    @Override
    public void run() {
        while (true) {
            try {
                task.run();
                break;
            } catch (Throwable ex) {
                if (task.handleException(ex)) {
                    continue;
                } else {
                    ExceptionHelper.throwRuntimeException(task.toString(), ex);
                }
            }
        }
    }
}
