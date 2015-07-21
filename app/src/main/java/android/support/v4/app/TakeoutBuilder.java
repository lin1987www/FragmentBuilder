package android.support.v4.app;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import fix.java.util.concurrent.CatchThreadFactory;
import fix.java.util.concurrent.ScheduledExecutorFix;
import fix.java.util.concurrent.Take;
import fix.java.util.concurrent.TakeCancelListener;

/**
 * Created by Administrator on 2015/7/13.
 */
public class TakeoutBuilder implements TakeCancelListener {
    private static Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    public static Handler getMainThreadHandler() {
        if (null == mMainThreadHandler) {
            synchronized (TakeoutBuilder.class) {
                if (null == mMainThreadHandler) {
                    mMainThreadHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return mMainThreadHandler;
    }

    private static ExecutorService mMainThreadExecutor;

    public static ExecutorService getMainThreadExecutor() {
        if (mMainThreadExecutor == null) {
            synchronized (TakeoutBuilder.class) {
                if (mMainThreadExecutor == null) {
                    mMainThreadExecutor = new AbstractExecutorService() {
                        @Override
                        public void shutdown() {
                            throw new RuntimeException();
                        }

                        @NonNull
                        @Override
                        public List<Runnable> shutdownNow() {
                            return null;
                        }

                        @Override
                        public boolean isShutdown() {
                            return false;
                        }

                        @Override
                        public boolean isTerminated() {
                            return false;
                        }

                        @Override
                        public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
                            return false;
                        }

                        @Override
                        public void execute(Runnable runnable) {
                            getMainThreadHandler().post(runnable);
                        }
                    };
                }
            }
        }
        return mMainThreadExecutor;
    }

    public final static ThreadFactory blockExecutorThreadFactory = new CatchThreadFactory("blockExecutor");
    public final static ThreadFactory nonBlockExecutorThreadFactory = new CatchThreadFactory("nobBlockExecutor");
    public final static ScheduledExecutorFix blockExecutor = new ScheduledExecutorFix(4, blockExecutorThreadFactory);
    public final static ExecutorService nonBlockExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), nonBlockExecutorThreadFactory);

    private Object target;
    private Take<?> take;
    private FragmentFix fragment;
    private ExecutorService onService = nonBlockExecutor;
    private ExecutorService toService = getMainThreadExecutor();
    private FragmentTakeout<?> takeout;

    private TakeoutBuilder() {
    }

    public TakeoutBuilder onMainThread() {
        onService = getMainThreadExecutor();
        return this;
    }

    public TakeoutBuilder onBlockThread() {
        onService = blockExecutor;
        return this;
    }

    public TakeoutBuilder onNonBlockThread() {
        onService = nonBlockExecutor;
        return this;
    }

    public TakeoutBuilder toBlockThread() {
        toService = blockExecutor;
        return this;
    }

    public TakeoutBuilder toNonBlockThread() {
        toService = nonBlockExecutor;
        return this;
    }

    public TakeoutBuilder toMainThread() {
        toService = getMainThreadExecutor();
        return this;
    }

    public void build() {
        takeout = new FragmentTakeout<>(target, take, onService, toService, fragment);
        nonBlockExecutor.submit(takeout);
    }

    public static TakeoutBuilder create(Object target, Take<?> take, FragmentFix fragment) {
        TakeoutBuilder builder = new TakeoutBuilder();
        builder.target = target;
        builder.take = take;
        builder.fragment = fragment;
        take.addTakeCancelListener(builder);
        return builder;
    }

    @Override
    public void takeCancel() {
        this.target = null;
        this.take = null;
        this.onService = null;
        this.toService = null;
        this.takeout = null;
        this.fragment = null;
    }
}
