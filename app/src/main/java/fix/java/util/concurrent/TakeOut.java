package fix.java.util.concurrent;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 2015/7/9.
 */
public class Takeout<T> implements Callable<Takeout<T>> {
    public final Take<T> take;
    public final ExecutorService onService;
    public final ExecutorService toService;
    public final WeakReference<Object> targetWeak;

    public Takeout(Object target, Take<T> take, ExecutorService onService, ExecutorService toService) {
        this.take = take;
        this.onService = onService;
        this.toService = toService;
        this.targetWeak = new WeakReference<>(target);
    }

    @Override
    public Takeout call() throws Exception {
        Future<Take<T>> future = onService.submit(take);
        future.get();
        if (!take.isCancelled()) {
            OutTaker outTaker = new OutTaker<T>(take, targetWeak);
            toService.submit(outTaker);
        }
        return this;
    }

    public class OutTaker<T> implements Runnable {
        public final Take<T> take;
        public final WeakReference<Object> targetWeak;

        public OutTaker(Take<T> take, WeakReference<Object> targetWeak) {
            this.take = take;
            this.targetWeak = targetWeak;
        }

        @Override
        public void run() {
            Object targetObject = targetWeak.get();
            if (targetObject == null) {
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
            }
            if (targetObject instanceof TakeoutListener) {
                ((TakeoutListener) targetObject).onTake(take);
            } else {
                System.err.println(String.format("Miss onTake(%s) on %s", take, targetObject));
            }
        }
    }

    public interface TakeoutListener<T> {
        void onTake(T take);
    }
}
