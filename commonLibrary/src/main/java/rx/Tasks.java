package rx;

import android.os.Looper;
import android.support.annotation.CallSuper;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Created by Administrator on 2016/12/20.
 */

public abstract class Tasks<THIS> extends Observable<THIS> {
    public final static String TAG = Tasks.class.getSimpleName();
    protected final String format = String.format("%s %s", this.getClass().getSimpleName(), "%s");

    private Observer<? super THIS> mObserver;
    private ArrayList<SubTaskObserver> subTaskObservers;
    private Throwable mThrowable;
    private CompositeDisposable mCompositeDisposable;
    private boolean mIsDone = false;
    private boolean mIsFailed = false;
    private boolean mIsCompleted = false;

    public Throwable getThrowable() {
        return mThrowable;
    }

    protected CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }

    public boolean isDone() {
        return mIsDone;
    }

    public boolean isFailed() {
        return mIsFailed;
    }

    public boolean isCompleted() {
        return mIsCompleted;
    }

    protected void done() {
        complete();
        mIsDone = true;
        mObserver.onNext((THIS) this);
        mObserver.onComplete();
    }

    protected void fail(Throwable e) {
        complete();
        mThrowable = e;
        mIsFailed = true;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mObserver.onError(e);
        } else {
            AndroidSchedulers.mainThread().scheduleDirect(
                    () -> {
                        mObserver.onError(e);
                    }
            );
        }
    }

    private void complete() {
        if (mIsCompleted) {
            throw new RuntimeException(String.format("%s is completed.", this));
        }
        mIsCompleted = true;
    }

    protected abstract void subscribeActual();

    @CallSuper
    @Override
    protected void subscribeActual(Observer<? super THIS> observer) {
        mObserver = observer;
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
            subTaskObservers = new ArrayList<>();
        } else {
            throw new RuntimeException("Can't connect again after disposed.");
        }
        // 如果 Observer 取消訂閱的話  我們可以透過 observer.onSubscribe 得知，因此傳入 Disposable
        // 如果 Observable 將 Disposable 設定為 dispose 但不會影響 observer
        observer.onSubscribe(mCompositeDisposable);
        subscribeActual();
    }

    protected <DATA> void subTask(Observable<DATA> observable) {
        subTask(observable, null);
    }

    protected <DATA> void subTask(Observable<DATA> observable, Consumer<DATA> onNext) {
        subTask(observable, onNext, null);
    }

    protected <DATA> void subTask(Observable<DATA> observable, Consumer<DATA> onNext, Consumer<Throwable> onError) {
        subTask(observable, onNext, onError, null);
    }

    protected <DATA> void subTask(Observable<DATA> observable, Consumer<DATA> onNext, Consumer<Throwable> onError, Action onComplete) {
        SubTaskObserver<DATA> subTaskObserver = new SubTaskObserver<>(observable, onNext, onError, onComplete);
        subTaskObservers.add(subTaskObserver);
    }

    private class SubTaskObserver<DATA> implements Observer<DATA> {
        protected Observable<DATA> observable;
        protected Consumer<DATA> onNext;
        protected Consumer<Throwable> onError;
        protected Action onComplete;

        public SubTaskObserver(Observable<DATA> observable, Consumer<DATA> onNext, Consumer<Throwable> onError, Action onComplete) {
            this.observable = observable;
            this.onNext = onNext;
            this.onError = onError;
            this.onComplete = onComplete;
            observable
                    .subscribe(this);
        }

        @Override
        public void onSubscribe(Disposable d) {
            // 使用此 Disposable 可以讓 Observable 知道我取消訂閱了
            getCompositeDisposable().add(d);
        }

        @Override
        public void onNext(DATA t) {
            if (onNext != null) {
                try {
                    onNext.accept(t);
                } catch (Throwable throwable) {
                    fail(throwable);
                }
            } else {
                done();
            }
        }

        @Override
        public void onError(Throwable e) {
            if (onError != null) {
                try {
                    onError.accept(e);
                } catch (Throwable throwable) {
                    fail(throwable);
                }
            } else {
                fail(e);
            }
        }

        @Override
        public void onComplete() {
            if (onComplete != null) {
                try {
                    onComplete.run();
                } catch (Throwable throwable) {
                    fail(throwable);
                }
            }
        }
    }
}
