package fix.java.util.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The default thread factory
 */
public class CatchThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final String tag;

    public CatchThreadFactory() {
        this("");
    }

    public CatchThreadFactory(String tag) {
        this.tag = tag;
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = String.format("%s-pool-%s-thread-", tag, poolNumber.getAndIncrement());
    }

    public Thread newThread(Runnable r) {
        if (!(r instanceof CatchRunnable)) {
            r = new CatchRunnable(r);
        }
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        t.setUncaughtExceptionHandler(
                UncaughtExceptionHandlerWrapper.wrap(
                        t.getUncaughtExceptionHandler()
                )
        );
        return t;
    }
}
