package fix.java.util.concurrent;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/17.
 */
public class DutyOn extends Duty<Void> {
    public static boolean DEBUG = true;

    private WeakReference<Object> mWrTarget;

    public Object getTarget() {
        return (mWrTarget != null) ? mWrTarget.get() : null;
    }

    public DutyOn(Object target) {
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
            boolean consumed = (Boolean) method.invoke(targetObject, previousDuty);
            if (consumed) {
                // Do nothing
            } else {
                for (DutyListener<DutyOn> listener : mGlobalDutyOnListenerArray) {
                    listener.onDuty(this);
                }
            }
            return;
        }
        if (targetObject instanceof DutyListener) {
            ((DutyListener) targetObject).onDuty(previousDuty);
        } else {
            System.out.println(String.format("Miss onDuty(%s) on %s", previousDuty, targetObject));
        }
    }

    public interface DutyListener<T extends Duty> {
        /**
         * @param duty
         * @return True if the listener has consumed the event, false otherwise.
         */
        boolean onDuty(T duty);
    }

    private final static ArrayList<DutyListener<DutyOn>> mGlobalDutyOnListenerArray = new ArrayList<>();

    public static void addGlobalOnDuty(DutyListener<DutyOn> listener) {
        if (!mGlobalDutyOnListenerArray.contains(listener)) {
            mGlobalDutyOnListenerArray.add(listener);
        }
    }

    public static void removeGlobalOnDuty(DutyListener<DutyOn> listener) {
        if (mGlobalDutyOnListenerArray.contains(listener)) {
            mGlobalDutyOnListenerArray.remove(listener);
        }
    }
}
