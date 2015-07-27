package fix.java.util.concurrent;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 2015/7/9.
 */
public class Takeout<T extends Take<?>> implements Callable<Takeout<T>> {
    public static boolean DEBUG = true;
    public final TakeWrap<T> takeWrap;
    public final T take;
    public final ExecutorService onService;
    public final ExecutorService toService;
    public final WeakReference<Object> targetWeak;

    public Takeout(Object target, TakeWrap<T> takeWrap, ExecutorService onService, ExecutorService toService) {
        this.takeWrap = takeWrap;
        this.take = takeWrap.getTask();
        this.onService = onService;
        this.toService = toService;
        this.targetWeak = new WeakReference<>(target);
    }

    @Override
    public Takeout call() throws Exception {
        Future<T> future = onService.submit(takeWrap);
        future.get();
        if (take.isCancelled()) {
            if (DEBUG) {
                System.err.println(String.format("Calling OutTaker is canceled. %s", take));
            }
        } else {
            OutTaker<T> outTaker = new OutTaker<>(take, targetWeak);
            toService.submit(outTaker);
        }
        return this;
    }

    public class OutTaker<T extends Take<?>> implements Runnable {
        public final T take;
        public final WeakReference<Object> targetWeak;

        public OutTaker(T take, WeakReference<Object> targetWeak) {
            this.take = take;
            this.targetWeak = targetWeak;
        }

        @Override
        public void run() {
            Throwable throwable = null;
            Object targetObject = targetWeak.get();
            if (targetObject == null) {
                if (DEBUG) {
                    System.err.println(String.format("Miss target. %s", take));
                }
                return;
            }
            if (take.isCancelled()) {
                if (DEBUG) {
                    System.err.println(String.format("Take is canceled. %s", take));
                }
                return;
            }
            Class<?> targetClass = targetObject.getClass();
            try {
                Method method = targetClass.getDeclaredMethod("onTake", take.getClass());
                if (method != null) {
                    method.invoke(targetObject, take);
                    return;
                }
            } catch (Throwable ex) {
                throwable = ex;
            }
            if (targetObject instanceof TakeoutListener) {
                ((TakeoutListener) targetObject).onTake(take);
            } else {
                System.err.println(String.format("Miss onTake(%s) on %s throwable:\n%s", take, targetObject, ExceptionHelper.getPrintStackTraceString(throwable)));
            }
        }
    }

    public interface TakeoutListener<T> {
        void onTake(T take);
    }
}
