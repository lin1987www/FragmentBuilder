package fix.java.util.concurrent;

/**
 * Created by Administrator on 2015/3/8.
 */
public class RetryRunnable implements Runnable {
    private Runnable task;
    private int maxTimes = 0;

    public RetryRunnable(Runnable task, int maxTimes) {
        if (task == null) {
            throw new NullPointerException();
        }
        this.task = task;
        this.maxTimes = maxTimes;
    }

    @Override
    public void run() {
        int retryTimes = maxTimes;
        while (true) {
            try {
                task.run();
                break;
            } catch (Throwable ex) {
                if (--retryTimes >= 0) {
                    continue;
                } else {
                    ExceptionHelper.throwRuntimeException(task.toString(), ex);
                }
            }
        }
    }
}
