package android.support.v4.app;

import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import fix.java.util.concurrent.Take;
import fix.java.util.concurrent.TakeWrap;
import fix.java.util.concurrent.Takeout;

/**
 * Created by Administrator on 2015/7/14.
 */
public class FragmentTakeout<T extends Take<?>> extends Takeout<T> {
    public final static String TAG = FragmentTakeout.class.getSimpleName();
    protected FragmentFix fragment;

    public FragmentTakeout(Object target, TakeWrap<T> takeWrap, ExecutorService onService, ExecutorService toService, FragmentFix fragment) {
        super(target, takeWrap, onService, toService);
        this.fragment = fragment;
    }

    @Override
    public Takeout call() throws Exception {
        Future<T> future = onService.submit(takeWrap);
        future.get();
        if (take.isCancelled()) {
            fragment = null;
            if (DEBUG) {
                System.err.println(String.format("Calling OutTaker is canceled. %s", take));
            }
        } else {
            FragmentOutTaker<T> outTaker = new FragmentOutTaker<>(take, targetWeak, fragment);
            toService.submit(outTaker);
        }

        return this;
    }

    public class FragmentOutTaker<T extends Take<?>> extends OutTaker<T> {
        protected FragmentFix fragment;

        public FragmentOutTaker(T take, WeakReference<Object> targetWeak, FragmentFix fragment) {
            super(take, targetWeak);
            this.fragment = fragment;
        }

        @Override
        public void run() {
            if (fragment.afterAnimTakeout(this)) {
                Log.e(TAG, String.format("Defer takeout %s to %s.", take, fragment));
            } else {
                super.run();
            }
        }
    }
}
