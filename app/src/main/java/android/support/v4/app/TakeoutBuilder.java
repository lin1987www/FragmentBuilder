package android.support.v4.app;

import java.util.concurrent.ExecutorService;

import fix.java.util.concurrent.Take;
import fix.java.util.concurrent.TakeCancelListener;
import fix.java.util.concurrent.TakeWrap;

/**
 * Created by Administrator on 2015/7/13.
 */
public class TakeoutBuilder implements TakeCancelListener {
    private Object target;
    private TakeWrap<Take<?>> takeWrap;
    private FragmentFix fragment;
    private ExecutorService onService = ExecutorSet.nonBlockExecutor;
    private ExecutorService toService = ExecutorSet.mainThreadExecutor;
    private FragmentTakeout<?> takeout;

    private TakeoutBuilder() {
    }

    public TakeoutBuilder onMainThread() {
        onService = ExecutorSet.mainThreadExecutor;
        return this;
    }

    public TakeoutBuilder onBlockThread() {
        onService = ExecutorSet.blockExecutor;
        return this;
    }

    public TakeoutBuilder onNonBlockThread() {
        onService = ExecutorSet.nonBlockExecutor;
        return this;
    }

    public TakeoutBuilder toBlockThread() {
        toService = ExecutorSet.blockExecutor;
        return this;
    }

    public TakeoutBuilder toNonBlockThread() {
        toService = ExecutorSet.nonBlockExecutor;
        return this;
    }

    public TakeoutBuilder toMainThread() {
        toService = ExecutorSet.mainThreadExecutor;
        return this;
    }

    public void build() {
        takeout = new FragmentTakeout<>(target, takeWrap, onService, toService, fragment);
        ExecutorSet.nonBlockExecutor.submit(takeout);
    }

    public static TakeoutBuilder create(Object target, Take<?> take, FragmentFix fragment) {
        return create(target, new TakeWrap<Take<?>>(take), fragment);
    }

    public static TakeoutBuilder create(Object target, TakeWrap<Take<?>> takeWrap, FragmentFix fragment) {
        TakeoutBuilder builder = new TakeoutBuilder();
        builder.target = target;
        builder.takeWrap = takeWrap;
        builder.fragment = fragment;
        takeWrap.getTask().addTakeCancelListener(builder);
        return builder;
    }

    @Override
    public void takeCancel() {
        this.target = null;
        this.takeWrap = null;
        this.onService = null;
        this.toService = null;
        this.takeout = null;
        this.fragment = null;
    }
}
