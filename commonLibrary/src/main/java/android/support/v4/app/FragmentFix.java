package android.support.v4.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.lin1987www.common.Utility;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import fix.java.util.concurrent.Duty;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by Administrator on 2015/7/8.
 */
public class FragmentFix extends Fragment {
    public static boolean DEBUG = Utility.DEBUG;
    public final static String TAG = FragmentFix.class.getSimpleName();
    protected static View.OnTouchListener doNothingOnTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return true;
        }
    };

    protected String FORMAT = String.format("%s %%s", toString());

    protected ArrayList<Duty<?>> mDutyList = new ArrayList<>();
    protected final AtomicBoolean mIsEnterAnim = new AtomicBoolean();

    private boolean mIsDuringResume = false;
    // 滿足 Ready 條件時，onResume就會被執行，同時只會執行這一次，因此　mDidOnReady　跟　mIsReady　同樣狀態，因此省略共用
    // Ready 的條件是 動畫完成, getUserVisibleHint true, Fragment state RESUMED
    private boolean mDidReady = false;
    private boolean mDidUserVisible = false;
    private boolean mDidSaveState = false;

    protected LinkedList<ConnectableObservable<?>> mOnResumeObservableList = new LinkedList<>();
    protected LinkedList<ConnectableObservable<?>> mOnReadyObservableList = new LinkedList<>();
    protected CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    protected FragmentArgs mFragmentArgs;
    Animation.AnimationListener mFragmentAnimListener;

    protected void prepareAnim() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onCreateAnimation prepareAnim state: " + mState));
        }
        mIsEnterAnim.set(true);
    }

    protected void endAnim() {
        mIsEnterAnim.set(false);
        performResumeIfReady("endAnim");
    }

    public FragmentArgs getFragmentArgs() {
        if (mFragmentArgs == null) {
            if (getArguments() == null) {
                mFragmentArgs = new FragmentArgs();
                mArguments = mFragmentArgs.bundle;
            } else {
                mFragmentArgs = new FragmentArgs(getArguments());
            }
        }
        return mFragmentArgs;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        FragmentUtils.log(this, "onCreateAnimation");
        Animation anim = null;
        if (anim == null) {
            // Use customs
            if (nextAnim != 0) {
                anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
            }
        }
        if (anim == null) {
            // Use transit
            if (transit != 0) {
                int styleIndex = FragmentManagerImpl.transitToStyleIndex(transit, enter);
                if (styleIndex >= 0) {
                    switch (styleIndex) {
                        case FragmentManagerImpl.ANIM_STYLE_OPEN_ENTER:
                            anim = FragmentManagerImpl.makeOpenCloseAnimation(getActivity(), 1.125f, 1.0f, 0, 1);
                            break;
                        case FragmentManagerImpl.ANIM_STYLE_OPEN_EXIT:
                            anim = FragmentManagerImpl.makeOpenCloseAnimation(getActivity(), 1.0f, .975f, 1, 0);
                            break;
                        case FragmentManagerImpl.ANIM_STYLE_CLOSE_ENTER:
                            anim = FragmentManagerImpl.makeOpenCloseAnimation(getActivity(), .975f, 1.0f, 0, 1);
                            break;
                        case FragmentManagerImpl.ANIM_STYLE_CLOSE_EXIT:
                            anim = FragmentManagerImpl.makeOpenCloseAnimation(getActivity(), 1.0f, 1.075f, 1, 0);
                            break;
                        case FragmentManagerImpl.ANIM_STYLE_FADE_ENTER:
                            //anim = FragmentManagerImpl.makeFadeAnimation(getActivity(), 0, 1);
                            anim = new AlphaAnimation(0, 1);
                            anim.setInterpolator(FragmentManagerImpl.DECELERATE_CUBIC);
                            anim.setDuration(220);
                            break;
                        case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT:
                            //anim = FragmentManagerImpl.makeFadeAnimation(getActivity(), 1, 0);
                            anim = new AlphaAnimation(1, 0);
                            anim.setInterpolator(FragmentManagerImpl.DECELERATE_CUBIC);
                            anim.setDuration(220);
                            break;
                    }
                }
            }
        }
        if (anim != null && enter) {
            prepareAnim();
            mFragmentAnimListener = new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (DEBUG) {
                        Log.d(TAG, String.format(FORMAT, "onAnimationStart state: " + mState));
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (DEBUG) {
                        Log.d(TAG, String.format(FORMAT, "onAnimationEnd state: " + mState));
                    }
                    endAnim();
                    mFragmentAnimListener = null;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            };
            anim.setAnimationListener(mFragmentAnimListener);
        }
        return anim;
    }

    @Override
    void performCreate(Bundle savedInstanceState) {
        if (getTag() != null) {
            FORMAT = String.format("%s %%s", toString().replace("%", "%%"));
        }
        FragmentUtils.log(this, "performCreate before");
        super.performCreate(savedInstanceState);
        FragmentUtils.log(this, "performCreate after");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // 不要因為mHost = null 而使用instantiateChildFragmentManager()
        // 會導致Activity Restore 的時候發生錯誤
        /*  根據是否要回復 再做決定
        if (FragmentUtils.getFragmentHostCallback(getChildFragmentManager()) == null) {
            instantiateChildFragmentManager();
        }
        */
        if (savedInstanceState == null) {
            if (mChildFragmentManager != null && mChildFragmentManager.mHost == null) {
                mChildFragmentManager.mHost = mHost;
            }
        } else {
            FragmentManagerState fms = savedInstanceState.getParcelable(FragmentActivity.FRAGMENTS_TAG);
            if (fms != null) {
                if (mChildFragmentManager != null) {
                    if (mChildFragmentManager.mHost == null ||
                            (mChildFragmentManager.mHost != null && mChildFragmentManager.mHost.getContext() == null)) {
                        mChildFragmentManager.mHost = mHost;
                    }
                    if (mChildFragmentManager.mActive != null && mChildFragmentManager.mActive.size() > 0) {
                        ArrayList<Integer> didNotAdded = new ArrayList<>();
                        for (int i = 0; i < fms.mAdded.length; i++) {
                            Fragment f = mChildFragmentManager.mActive.get(fms.mAdded[i]);
                            if (mChildFragmentManager.mAdded.contains(f)) {
                                Log.d(TAG, String.format("%s Already added!", f));
                            } else {
                                didNotAdded.contains(i);
                            }
                        }
                        if (didNotAdded.size() != fms.mAdded.length) {
                            int didNotAddedArray[] = new int[didNotAdded.size()];
                            for (int i = 0; i < didNotAddedArray.length; i++) {
                                didNotAddedArray[i] = didNotAdded.get(i);
                            }
                        }
                    }
                }
            } else {
                if (mChildFragmentManager != null && mChildFragmentManager.mHost == null) {
                    mChildFragmentManager.mHost = mHost;
                }
            }
        }
        if (mChildFragmentManager != null) {
            if (mChildFragmentManager.mHost == null) {
                Log.e(TAG, "mChildFragmentManager.mHost = null");
            } else {
                if (mChildFragmentManager.mHost.getContext() == null) {
                    Log.e(TAG, "mChildFragmentManager.mHost.getContext() = null");
                }
            }
        }
        super.onCreate(savedInstanceState);
        ExecCommit.wrap(mChildFragmentManager);
    }

    @Override
    View performCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        FragmentUtils.log(this, "performCreateView before");
        view = super.performCreateView(inflater, container, savedInstanceState);
        FragmentUtils.log(this, "performCreateView after");
        return view;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentUtils.log(this, "onCreateView");
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onCreate"));
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onActivityResult"));
        }
    }

    @Override
    void performActivityCreated(Bundle savedInstanceState) {
        FragmentUtils.log(this, "performActivityCreated before");
        super.performActivityCreated(savedInstanceState);
        FragmentUtils.log(this, "performActivityCreated after");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        FragmentUtils.log(this, "onViewStateRestored");
        if (savedInstanceState != null) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, "onViewStateRestored"));
            }
            boolean isUserVisible = getFragmentArgs().getUserVisible();
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, "onViewStateRestored setUserVisibleHint"));
            }
            setUserVisibleHint(isUserVisible);
        }
        super.onViewStateRestored(savedInstanceState);
        ExecCommit.wrap(mChildFragmentManager);
    }

    @Override
    void performStart() {
        FragmentUtils.log(this, "performStart before");
        super.performStart();
        FragmentUtils.log(this, "performStart after");
        // v25.3.0 開始動畫會在 OnResume 後才執行，避免卡住
        if (getNextTransition() != 0 || getNextTransitionStyle() != 0) {
            prepareAnim();
        }
    }

    @Override
    public void onStart() {
        FragmentUtils.log(this, "onStart");
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onStart"));
        }
        super.onStart();
    }

    @Override
    void performResume() {
        // 修改原本的 performResume 行為，保持 onResume 被呼叫一次
        // super.performResume();
        FragmentUtils.log(this, "performResume before");
        if (mChildFragmentManager != null) {
            mChildFragmentManager.noteStateNotSaved();
            mChildFragmentManager.execPendingActions();
        }
        mState = RESUMED;
//        mCalled = false;
        // ---------
        mIsDuringResume = true;
        mDidSaveState = false;
        if (mOnResumeObservableList.size() > 0) {
            while (null != mOnResumeObservableList.peekFirst()) {
                ConnectableObservable connectableObservable = mOnResumeObservableList.pollFirst();
                add(connectableObservable.connect());
            }
        }
        performResumeIfReady("performResume");
        // ---------
//        if (!mCalled) {
//            throw new SuperNotCalledException("Fragment " + this
//                    + " did not call through to super.onResume()");
//        }
        if (mChildFragmentManager != null) {
            mChildFragmentManager.dispatchResume();
            mChildFragmentManager.execPendingActions();
        }
        FragmentUtils.log(this, "performResume after");
    }

    void performResumeIfReady(String tag) {
        boolean toDo = false;
        if (!getUserVisibleHint()) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("skip performResumeIfReady for getUserVisibleHint false by %s", tag)));
            }
        } else if (!FragmentUtils.getUserVisibleHintAllParent(this)) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("skip performResumeIfReady getUserVisibleHintAllParent() false by %s", tag)));
            }

        } else if (!FragmentUtils.isFragmentAvailable(this)) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("skip performResumeIfReady isFragmentAvailable false by %s", tag)));
            }
        } else if (mIsEnterAnim.get()) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("skip performResumeIfReady for enter anim by %s", tag)));
            }
        } else if (getFragmentArgs().isConsumeReady()) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("skip performResumeIfReady isConsumeReady by %s", tag)));
            }
        } else if (!isResumed()) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("skip performResumeIfReady for is not Resumed by %s", tag)));
            }
        } else if (mDidReady) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("skip performResumeIfReady mDidReady true by %s", tag)));
            }
        } else {
            toDo = true;
        }
        if (toDo) {
            mDidReady = true;
            mCalled = false;
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("performResumeIfReady by %s", tag)));
            }
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("onResume by %s", tag)));
            }
            onResume();
            if (!mCalled) {
                throw new SuperNotCalledException("Fragment " + this + " did not call through to super.onResume()");
            }
            if (mDutyList.size() > 0) {
                for (Duty duty : mDutyList) {
                    if (!duty.isSubmitted()) {
                        duty.submit();
                        if (DEBUG) {
                            Log.d(TAG, String.format(FORMAT, "perform pending duty " + duty.toString()));
                        }
                    }
                }
            }
            if (mOnReadyObservableList.size() > 0) {
                while (null != mOnReadyObservableList.peekFirst()) {
                    ConnectableObservable connectableObservable = mOnReadyObservableList.pollFirst();
                    add(connectableObservable.connect());
                }
            }
        }
        performUserVisible(tag);
    }

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onResume"));
        }
        FragmentUtils.log(this, "onResume");
        super.onResume();
    }

    void performUserVisible(String tag) {
        boolean toDo = false;
        if (!getUserVisibleHint()) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("skip performUserVisible for getUserVisibleHint false by %s", tag)));
            }
        } else if (!FragmentUtils.getUserVisibleHintAllParent(this)) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("skip performUserVisible getUserVisibleHintAllParent() false by %s", tag)));
            }
        } else if (!mDidReady) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("skip performUserVisible for is not ready by %s", tag)));
            }
        } else if (mDidUserVisible) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("skip performUserVisible mDidUserVisible true by %s", tag)));
            }
        } else {
            toDo = true;
        }
        if (toDo) {
            mDidUserVisible = true;
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("performUserVisible by %s", tag)));
            }
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("onUserVisible by %s", tag)));
            }
            onUserVisible();
        }
    }

    @CallSuper
    public void onUserVisible() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onUserVisible"));
        }
        FragmentUtils.log(this, "onUserVisible");
    }

    @Override
    void performPause() {
        FragmentUtils.log(this, "performPause before");
        mIsDuringResume = false;
        mDidReady = false;
        mDidUserVisible = false;
        if (mDutyList.size() > 0) {
            // Cancel all take
            int isNotDone = 0;
            for (Duty<?> duty : mDutyList) {
                if (!duty.isDone()) {
                    isNotDone = isNotDone + 1;
                }
                duty.cancel();
            }
            if (DEBUG) {
                if (isNotDone > 0) {
                    Log.d(TAG, String.format(FORMAT, String.format("clear Duty that is not done %s.", isNotDone)));
                }
            }
            mDutyList.clear();
        }
        mCompositeDisposable.clear();
        mOnReadyObservableList.clear();
        mOnResumeObservableList.clear();
        if (!isRemoving()) {
            performSaveState("performPause is not removing");
        }
        super.performPause();
        FragmentUtils.log(this, "performPause after");
    }

    @Override
    public void onPause() {
        FragmentUtils.log(this, "onPause");
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onPause"));
        }
        super.onPause();
    }

    @Override
    void performSaveInstanceState(Bundle outState) {
        performSaveState("performSaveInstanceState");
        FragmentUtils.log(this, "performSaveInstanceState before");
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "performSaveInstanceState"));
        }
        super.performSaveInstanceState(outState);
        FragmentUtils.log(this, "performSaveInstanceState after");
    }

    @CallSuper
    @Override
    public void onSaveInstanceState(Bundle outState) {
        FragmentUtils.log(this, "onSaveInstanceState");
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onSaveInstanceState"));
        }
        super.onSaveInstanceState(outState);
    }

    @CallSuper
    void performSaveState(String tag) {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, String.format("performSaveState by %s", tag)));
        }
        if (!mDidSaveState) {
            mDidSaveState = true;
            onSaveState();
        }
    }

    @CallSuper
    public void onSaveState() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onSaveState"));
        }
    }

    @Override
    void performStop() {
        FragmentUtils.log(this, "performStop before");
        super.performStop();
        FragmentUtils.log(this, "performStop after");
    }

    @Override
    void performReallyStop() {
        FragmentUtils.log(this, "performReallyStop before");
        super.performReallyStop();
        FragmentUtils.log(this, "performReallyStop after");
    }

    @Override
    void performDestroyView() {
        FragmentUtils.log(this, "performDestroyView before");
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "performDestroyView"));
        }
        super.performDestroyView();
        FragmentUtils.log(this, "performDestroyView after");
    }

    @Override
    public void onDestroyView() {
        FragmentUtils.log(this, "onDestroyView");
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onDestroyView"));
        }
        super.onDestroyView();
    }

    @Override
    void performDestroy() {
        FragmentUtils.log(this, "performDestroy before");
        super.performDestroy();
        FragmentUtils.log(this, "performDestroy after");
    }

    @Override
    public boolean getUserVisibleHint() {
        boolean superUserVisibleHint = super.getUserVisibleHint();
        boolean isUserVisible = getFragmentArgs().getUserVisible();
        if (superUserVisibleHint != isUserVisible) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("getUserVisibleHint is not equal saved value. %s -> %s", superUserVisibleHint, isUserVisible)));
            }
            super.setUserVisibleHint(isUserVisible);
        }
        return isUserVisible;
    }

    @Override
    public void setUserVisibleHint(boolean isVisible) {
        // 重復被設定為 true 並不影響動作
        boolean lastIsVisible = getUserVisibleHint();
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, String.format("setUserVisibleHint %s -> %s", lastIsVisible, isVisible)));
        }
        getFragmentArgs().setUserVisible(isVisible);
        if (!isVisible) {
            mDidUserVisible = false;
        }
        super.setUserVisibleHint(isVisible);
        if (isVisible && isResumed()) {
            performResumeIfReady("setUserVisibleHint");
            // children fragment 會因為未顯示跳過 preformResume，因此需要設定children
            if (mChildFragmentManager != null && mChildFragmentManager.getFragments() != null) {
                for (Fragment fragment : mChildFragmentManager.getFragments()) {
                    if (fragment != null && fragment instanceof FragmentFix) {
                        FragmentFix f = (FragmentFix) fragment;
                        if (f.isResumed() && f.getUserVisibleHint() && !f.mDidReady) {
                            f.setUserVisibleHint(true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(this, intent, requestCode);
    }

    public void startActivityForResult(Object object, Intent intent, int requestCode) {
        FragmentActivityFix fragmentActivity = (FragmentActivityFix) getContext();
        fragmentActivity.startActivityForResult(object, intent, requestCode);
    }

    public void duty(Duty duty) {
        // If fragment will removed don't accept any duty.
        // Don't care isResumed true or false. Because duty be pended and wait perform
        if (!isAdded()) {
            return;
        }
        mDutyList.add(duty);
        if (mDidReady) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, "submit duty " + duty.toString()));
            }
            duty.submit();
        } else {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, "add duty " + duty.getClass().getSimpleName()));
            }
        }
    }

    public void add(Disposable disposable) {
        mCompositeDisposable.add(disposable);
    }

    public void resume(Runnable task) {
        Disposable disposable = resume(
                Observable.timer(0, TimeUnit.SECONDS).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
        ).subscribe(new ConsumerTask<>(task));
        add(disposable);
    }

    public <T> ConnectableObservable<T> resume(Observable<T> observable) {
        ConnectableObservable<T> connectableObservable;
        if (observable instanceof ConnectableObservable) {
            connectableObservable = (ConnectableObservable<T>) observable;
        } else {
            connectableObservable = observable.publish();
        }
        if (mIsDuringResume) {
            add(connectableObservable.connect());
        } else {
            mOnResumeObservableList.add(connectableObservable);
        }
        return connectableObservable;
    }

    public <T> void resume(Observable<T> observable, Consumer<? super T> onNext) {
        resume(observable, onNext, Functions.ON_ERROR_MISSING);
    }

    public <T> void resume(Observable<T> observable, Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        resume(observable, onNext, onError, Functions.EMPTY_ACTION);
    }

    public <T> void resume(Observable<T> observable, Consumer<? super T> onNext, Consumer<? super Throwable> onError, Action onComplete) {
        ConnectableObservable<T> connectableObservable = resume(observable);
        Disposable disposable = connectableObservable.subscribe(onNext, onError, onComplete);
        add(disposable);
    }

    public void ready(Runnable task) {
        Disposable disposable = ready(
                Observable.timer(0, TimeUnit.SECONDS).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
        ).subscribe(new ConsumerTask<>(task));
        add(disposable);
    }

    public <T> ConnectableObservable<T> ready(Observable<T> observable) {
        ConnectableObservable<T> connectableObservable;
        if (observable instanceof ConnectableObservable) {
            connectableObservable = (ConnectableObservable<T>) observable;
        } else {
            connectableObservable = observable.publish();
        }
        if (mDidReady) {
            add(connectableObservable.connect());
        } else {
            mOnReadyObservableList.add(connectableObservable);
        }
        return connectableObservable;
    }

    public <T> void ready(Observable<T> observable, Consumer<? super T> onNext) {
        ready(observable, onNext, Functions.ON_ERROR_MISSING);
    }

    public <T> void ready(Observable<T> observable, Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        ready(observable, onNext, onError, Functions.EMPTY_ACTION);
    }

    public <T> void ready(Observable<T> observable, Consumer<? super T> onNext, Consumer<? super Throwable> onError, Action onComplete) {
        ConnectableObservable<T> connectableObservable = ready(observable);
        Disposable disposable = connectableObservable.subscribe(onNext, onError, onComplete);
        add(disposable);
    }

    private static class ConsumerTask<T> implements Consumer<T> {
        private Runnable task;

        public ConsumerTask(Runnable task) {
            this.task = task;
        }

        @Override
        public void accept(T t) throws Exception {
            task.run();
        }
    }
}
