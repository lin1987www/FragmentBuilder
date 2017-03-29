package rx;

import android.support.annotation.CallSuper;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/12/20.
 */

public abstract class Task<THIS, DATA> extends Tasks<THIS> {
    protected abstract Observable<DATA> getObservable();

    private DATA data;

    public DATA getData() {
        return data;
    }

    protected void beforeDone() {
    }

    @CallSuper
    private void onDone(DATA data) {
        this.data = data;
        try {
            beforeDone();
            if (!isCompleted()) {
                done();
            }
        } catch (Throwable throwable) {
            fail(throwable);
        }
    }

    @CallSuper
    @Override
    protected void subscribeActual() {
        subTask(getObservable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation()),
                this::onDone);
    }
}
