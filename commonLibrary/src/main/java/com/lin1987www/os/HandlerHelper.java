package com.lin1987www.os;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import fix.java.util.concurrent.UncaughtExceptionHandlerWrapper;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class HandlerHelper {
	private final static String TAG = HandlerHelper.class.getName();

    private static StartedHandler anotherThreadHandler;
    public static Handler getAnotherThreadHandler() {
        if (null == anotherThreadHandler) {
            synchronized (HandlerHelper.class) {
                if (null == anotherThreadHandler) {
                    anotherThreadHandler = StartedHandler.create("AnotherThreadHandler-"+TAG, null);
                }
            }
        }
        return anotherThreadHandler;
    }

    private static Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
	public static Handler getMainThreadHandler() {
		if (null == mMainThreadHandler) {
			synchronized (HandlerHelper.class) {
				if (null == mMainThreadHandler) {
					mMainThreadHandler = new Handler(Looper.getMainLooper());
				}
			}
		}
		return mMainThreadHandler;
	}

    static {
        getMainThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                Thread t = Thread.currentThread();
                t.setUncaughtExceptionHandler(UncaughtExceptionHandlerWrapper.wrap(t.getUncaughtExceptionHandler()));
            }
        });
    }

    private static ExecutorService mMainThreadExecutor;
    public static ExecutorService getMainThreadExecutor(){
        if(mMainThreadExecutor==null){
            synchronized (HandlerHelper.class) {
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

	public static void runMainThread(Runnable pRunnable) {
        getMainThreadHandler().post(pRunnable);
	}
    public static void runMainThreadDelayed(Runnable pRunnable,long delayMillis) {
        getMainThreadHandler().postDelayed(pRunnable,delayMillis);
    }

	public static void runAnotherThread(Runnable pRunnable) {
        getAnotherThreadHandler().post(pRunnable);
	}

    public static void assertMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            String message = "Must run on Main Thread!";
            Log.e(TAG, message);
            throw new RuntimeException(message);
        }
    }

    public static void assertNonMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            String message = "Must run on Non-Main Thread!";
            Log.e(TAG, message);
            throw new RuntimeException(message);
        }
    }
}
