package fix.java.util.concurrent;

import android.support.annotation.CallSuper;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by Administrator on 2015/12/24.
 */
public class DutyJoin<T> extends Duty<T> {
    private boolean mIsOrderSubmit = false;

    public boolean isOrderSubmit() {
        return mIsOrderSubmit;
    }

    public DutyJoin<T> setIsOrderSubmit(boolean value) {
        mIsOrderSubmit = value;
        return this;
    }

    protected Duty mDoneDuty = new Duty() {
        @Override
        public void doTask(Object context, Duty previousDuty) throws Throwable {
            if (!isFinished()) {
                synchronized (mDoneDuty) {
                    if (!isFinished()) {
                        if (isOrderSubmit() && hasOrderSubmit()) {
                            return;
                        }
                        boolean isAllFinished = true;
                        for (Duty duty : mDutyArray) {
                            if (!duty.isFinished()) {
                                isAllFinished = false;
                                break;
                            }
                        }
                        if (isAllFinished) {
                            for (Duty duty : getDutyArray()) {
                                if (!duty.isDone()) {
                                    fail(duty.getThrowable());
                                    DutyJoin.this.fail(duty.getThrowable());
                                    return;
                                }
                            }
                            done();
                            DutyJoin.this.done();
                        }
                    }
                }
            }
        }
    };

    protected ArrayList<Duty> mDutyArray = new ArrayList<>();

    public ArrayList<Duty> getDutyArray() {
        return mDutyArray;
    }

    public DutyJoin<T> join(Duty duty) {
        if (isRan()) {
            submitDuty(duty);
        } else {
            mDutyArray.add(duty);
        }
        return this;
    }

    public DutyJoin() {
        setAsync(true);
    }

    @Override
    public Throwable getThrowable() {
        Throwable ex = super.getThrowable();
        if (ex == null) {
            for (Duty duty : mDutyArray) {
                ex = duty.getThrowable();
                if (ex != null) {
                    break;
                }
            }
        }
        return ex;
    }

    @CallSuper
    @Override
    public void doTask(T context, Duty previousDuty) throws Throwable {
        mDoneDuty.setExecutorService(getExecutorService());
        if (isOrderSubmit()) {
            hasOrderSubmit();
        } else {
            for (Duty duty : mDutyArray) {
                submitDuty(duty);
            }
        }
    }

    private void submitDuty(Duty duty) {
        if (!duty.isRan()) {
            duty.always(mDoneDuty);
            duty.submit(this);
        }
    }

    private boolean hasOrderSubmit() {
        boolean hasOrderSubmit = false;
        ListIterator<Duty> iterator = mDutyArray.listIterator();
        while (iterator.hasNext()) {
            Duty duty = iterator.next();
            if (!duty.isSubmitted()) {
                submitDuty(duty);
                hasOrderSubmit = true;
                break;
            }
        }
        return hasOrderSubmit;
    }
}
