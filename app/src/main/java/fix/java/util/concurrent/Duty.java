package fix.java.util.concurrent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 使得每件事情在不同的Executor裡面執行。
 * 如果使用 非同步，則由其他的Thread去觸發 done 或 fail 。
 * 支援 Async
 * 參考 https://api.jquery.com/category/deferred-object/
 * <p/>
 * Created by Administrator on 2015/12/16.
 */
public abstract class Duty<T> implements Callable<Duty<T>> {
    public static boolean DEBUG = true;
    protected Throwable mThrowable;
    protected WeakReference<ExecutorService> mExecutorService;
    protected T mContext;

    // 必須等待使用者回應的操作，則附著在Fragment上，等使用者回應後，在Submit出去
    protected boolean mIsAsync = false;
    protected boolean mIsRan = false;
    protected AtomicBoolean mIsFinished = new AtomicBoolean(false);
    protected boolean mIsDone = false;
    protected boolean mIsCancelled = false;
    protected long mSubmitTimeMillis = 0;
    protected long mStartTimeMillis = 0;
    protected long mStopTimeMillis = 0;

    protected Duty mPreviousDuty;
    private List<Duty> mDoneDutyList;
    private List<Duty> mFailDutyList;
    private List<Duty> mAlwaysDutyList;

    public Throwable getThrowable() {
        return mThrowable;
    }

    protected void setThrowable(Throwable throwable) {
        this.mThrowable = throwable;
    }

    public ExecutorService getExecutorService() {
        return mExecutorService.get();
    }

    public Duty<T> setExecutorService(ExecutorService executorService) {
        this.mExecutorService = new WeakReference<>(executorService);
        return this;
    }

    public Duty getPreviousDuty() {
        return mPreviousDuty;
    }

    public T getContext() {
        return mContext;
    }

    public Duty<T> setContext(T context) {
        this.mContext = context;
        return this;
    }

    public Duty<T> setAsync(boolean isAsync) {
        mIsAsync = isAsync;
        return this;
    }

    public Duty<T> done(Duty duty) {
        if (isFinished()) {
            if (isDone()) {
                duty.submit(this);
            }
        } else {
            if (mDoneDutyList == null) {
                mDoneDutyList = new ArrayList<>();
            }
            mDoneDutyList.add(duty);
        }
        return this;
    }

    public Duty<T> fail(Duty duty) {
        if (isFinished()) {
            if (!isDone()) {
                duty.submit(this);
            }
        } else {
            if (mFailDutyList == null) {
                mFailDutyList = new ArrayList<>();
            }
            mFailDutyList.add(duty);
        }
        return this;
    }

    public Duty<T> always(Duty duty) {
        if (isFinished()) {
            duty.submit(this);
        } else {
            if (mAlwaysDutyList == null) {
                mAlwaysDutyList = new ArrayList<>();
            }
            mAlwaysDutyList.add(duty);
        }
        return this;
    }

    public boolean isSubmitted() {
        return mSubmitTimeMillis != 0;
    }

    public boolean isSync() {
        return !mIsAsync;
    }

    public boolean isAsync() {
        return mIsAsync;
    }

    public boolean isRan() {
        return mIsRan;
    }

    public boolean isFinished() {
        return mIsFinished.get();
    }

    public boolean isDone() {
        return mIsDone;
    }

    public boolean isCancelled() {
        return mIsCancelled;
    }

    public boolean isCancelledCast() {
        if (isCancelled()) {
            return true;
        }
        if (getPreviousDuty() != null) {
            if (getPreviousDuty().isCancelledCast()) {
                return true;
            }
        }
        return false;
    }

    public void cancel() {
        mIsCancelled = true;
    }

    public void done() {
        mIsFinished.set(true);
        mStopTimeMillis = System.currentTimeMillis();
        onPostExecute();
        if (!isCancelled() && mDoneDutyList != null) {
            for (Duty duty : mDoneDutyList) {
                duty.submit(this);
            }
        }
        always();
    }

    public void fail(Throwable ex) {
        setThrowable(ex);
        mIsFinished.set(true);
        mStopTimeMillis = System.currentTimeMillis();
        onPostExecute();
        if (!isCancelled() && mFailDutyList != null) {
            for (Duty duty : mFailDutyList) {
                duty.submit(this);
            }
        }
        always();
    }

    protected void always() {
        if (mStopTimeMillis == 0) {
            mIsFinished.set(true);
            mStopTimeMillis = System.currentTimeMillis();
            onPostExecute();
        }
        if (mAlwaysDutyList != null) {
            for (Duty duty : mAlwaysDutyList) {
                duty.submit(this);
            }
        }
    }

    protected Duty<T> init() {
        mIsRan = false;
        mIsFinished.set(false);
        mIsDone = false;
        mIsCancelled = false;
        mSubmitTimeMillis = 0;
        mStartTimeMillis = 0;
        mStopTimeMillis = 0;
        mThrowable = null;
        return this;
    }

    public Duty<T> submit(Duty previousDuty) {
        ExecutorService es = getExecutorService();
        if (es != null) {
            init();
            mSubmitTimeMillis = System.currentTimeMillis();
            mPreviousDuty = previousDuty;
            es.submit(this);
        } else {
            if (DEBUG) {
                System.err.println(String.format("Duty lose ExecutorService. %s", this));
            }
        }
        return this;
    }

    public Duty<T> submit() {
        return submit(null);
    }

    // 如果可以處理錯誤的話，就回傳null，不行就回傳Throwable
    // 也未必能在此 Thread 當中進行處理 有點雞肋
    public Throwable handleThrowable(Throwable ex) {
        return ex;
    }

    public void onPreExecute() {
    }

    public abstract void doTask(T context, Duty previousDuty) throws Throwable;

    public void onPostExecute() {
    }

    @Override
    public Duty<T> call() throws Exception {
        mIsRan = true;
        mStartTimeMillis = System.currentTimeMillis();
        while (true) {
            try {
                onPreExecute();
                if (!isCancelled()) {
                    doTask(getContext(), getPreviousDuty());
                    if (isSync()) {
                        done();
                    }
                } else {
                    always();
                }
            } catch (Throwable ex) {
                if (!isCancelled()) {
                    if (null == handleThrowable(ex)) {
                        continue;
                    } else {
                        ExceptionHelper.printException(this.toString(), ex);
                        fail(ex);
                    }
                } else {
                    setThrowable(ex);
                    always();
                }
            }
            break;
        }
        return this;
    }
}
