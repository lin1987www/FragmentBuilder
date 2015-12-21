package fix.java.util.concurrent;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2015/12/17.
 */
public class DutyTo extends Duty<Void> {
    public static boolean DEBUG = true;

    private WeakReference<Object> mWrTarget;

    public DutyTo(Object target) {
        mWrTarget = new WeakReference<>(target);
    }

    @Override
    public void doTask(Void context, Duty previousDuty) throws Throwable {
        Object targetObject = mWrTarget.get();
        if (targetObject == null) {
            if (DEBUG) {
                System.err.println(String.format("Miss onDuty target. %s", targetObject));
            }
            return;
        }
        if (previousDuty.isCancelled()) {
            if (DEBUG) {
                System.err.println(String.format("Duty is canceled. %s", previousDuty));
            }
            return;
        }
        Class<?> targetClass = targetObject.getClass();
        Method method = targetClass.getDeclaredMethod("onDuty", previousDuty.getClass());
        if (method != null) {
            method.invoke(targetObject, previousDuty);
            return;
        }
        if (targetObject instanceof DutyListener) {
            ((DutyListener) targetObject).onDuty(previousDuty);
        } else {
            System.out.println(String.format("Miss onDuty(%s) on %s", previousDuty, targetObject));
        }
    }

    public interface DutyListener<T extends Duty> {
        void onDuty(T take);
    }
}
