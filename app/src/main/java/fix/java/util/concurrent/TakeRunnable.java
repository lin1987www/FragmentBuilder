package fix.java.util.concurrent;

/**
 * Created by Administrator on 2015/4/21.
 */
public class TakeRunnable extends Take {
    public final Runnable runnable;

    public TakeRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public TakeRunnable take() throws Throwable {
        if (!isCancelled()) {
            runnable.run();
        }
        return this;
    }

    @Override
    public boolean handleException(Throwable ex) {
        return false;
    }
}
