package fix.java.util.concurrent;

import android.support.annotation.CallSuper;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/24.
 */
public class DutyJoin<T> extends Duty<T> {
    protected Duty mDoneDuty = new Duty() {
        @Override
        public void doTask(Object context, Duty previousDuty) throws Throwable {
            if (!isFinished()) {
                synchronized (mDoneDuty) {
                    if (!isFinished()) {
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
        mDutyArray.add(duty);
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
        for (Duty duty : mDutyArray) {
            duty.always(mDoneDuty);
            duty.submit(this);
        }
    }
}
