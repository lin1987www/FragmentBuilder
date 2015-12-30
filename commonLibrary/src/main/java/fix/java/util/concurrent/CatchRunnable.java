package fix.java.util.concurrent;

/**
 * Created by Administrator on 2015/3/6.
 */
public class CatchRunnable implements Runnable {
    private Runnable task;

    public CatchRunnable(Runnable task) {
        if (task == null) {
            throw new NullPointerException();
        }
        this.task = task;
    }

    @Override
    public void run() {
        try {
            task.run();
        } catch (Throwable ex) {
            ExceptionHelper.throwRuntimeException(task.toString(), ex);
        }
    }
}
