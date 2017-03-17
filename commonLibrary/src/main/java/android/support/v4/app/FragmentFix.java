package android.support.v4.app;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.concurrent.atomic.AtomicBoolean;

import fix.java.util.concurrent.Duty;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.observers.DisposableObserver;


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

    protected final String FORMAT = String.format("%s %s", toString(), "%s");

    protected ArrayList<Duty<?>> mDutyList = new ArrayList<>();
    protected final AtomicBoolean mIsEnterAnim = new AtomicBoolean();

    protected LinkedList<ConnectableObservable<?>> mConnectableObservableList = new LinkedList<>();
    protected CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    protected FragmentArgs mFragmentArgs;
    Animation.AnimationListener mFragmentAnimListener;

    private boolean mIsReady = false;

    public boolean isReady() {
        return mIsReady;
    }

    protected void prepareAnim() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onCreateAnimation prepareAnim state: " + mState));
        }
        mIsEnterAnim.set(true);
    }

    protected void endAnim() {
        mIsEnterAnim.set(false);
        performPendingDuty("endAnim");
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
        BackStackRecordShell.wrap(this, getChildFragmentManager());
    }

    @Override
    void performCreate(Bundle savedInstanceState) {
        FragmentUtils.log(this, "performCreate before");
        super.performCreate(savedInstanceState);
        FragmentUtils.log(this, "performCreate after");
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
        if (DEBUG && null != savedInstanceState) {
            Log.d(TAG, String.format(FORMAT, "onViewStateRestored"));
        }
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    void performStart() {
        FragmentUtils.log(this, "performStart before");
        super.performStart();
        FragmentUtils.log(this, "performStart after");
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
        super.performResume();
        FragmentUtils.log(this, "performResume before");
        if (mChildFragmentManager != null) {
            mChildFragmentManager.noteStateNotSaved();
            mChildFragmentManager.execPendingActions();
        }
        mState = RESUMED;
//        mCalled = false;
        performPendingDuty("performResume");
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

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onResume"));
        }
        FragmentUtils.log(this, "onResume");
        super.onResume();
    }

    @Override
    void performPause() {
        FragmentUtils.log(this, "performPause before");
        mIsReady = false;
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
        mConnectableObservableList.clear();
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
        FragmentUtils.log(this, "performSaveInstanceState before");
        super.performSaveInstanceState(outState);
        FragmentUtils.log(this, "performSaveInstanceState after");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        FragmentUtils.log(this, "onSaveInstanceState");
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onSaveInstanceState"));
        }
        super.onSaveInstanceState(outState);
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

    private boolean mUserVisibleHint = true;

    @Override
    public boolean getUserVisibleHint() {
        boolean superUserVisibleHint = super.getUserVisibleHint();
        if (superUserVisibleHint != mUserVisibleHint) {
            super.setUserVisibleHint(mUserVisibleHint);
        }
        return mUserVisibleHint;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        boolean lastUserVisibleHint = getUserVisibleHint();
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, String.format("setUserVisibleHint %s -> %s", lastUserVisibleHint, isVisibleToUser)));
        }
        mUserVisibleHint = isVisibleToUser;
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            performPendingDuty("setUserVisibleHint");
        }
        if (getFragmentArgs().isUserVisibleHintOnResume() && !isVisibleToUser) {
            Log.d(TAG, String.format(FORMAT, String.format("isUserVisibleHintOnResume set mIsReady false")));
            mIsReady = false;
        }
    }

    protected void performPendingDuty(String tag) {
        if (!getUserVisibleHint()) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("performPendingDuty skip for getUserVisibleHint false by %s", tag)));
            }
            return;
        }
        if (!FragmentUtils.getUserVisibleHintAllParent(this)) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("performPendingDuty skip for getUserVisibleHintAllParent() is false by %s", tag)));
            }
            return;
        }
        if (!FragmentUtils.isFragmentAvailable(this)) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("performPendingDuty skip for isFragmentAvailable false by %s", tag)));
            }
            return;
        }
        if (mIsEnterAnim.get()) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("performPendingDuty skip for anim true by %s", tag)));
            }
            return;
        }
        if (getFragmentArgs().consumePopOnResume()) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("performPendingDuty consume by %s", tag)));
            }
            return;
        }
        if (mIsReady) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("performPendingDuty skip for mIsReady true by %s", tag)));
            }
            return;
        }
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, String.format("onResume by %s", tag)));
        }
        mIsReady = true;
        mCalled = false;
        onResume();
        if (!mCalled) {
            throw new SuperNotCalledException("Fragment " + this
                    + " did not call through to super.onResume()");
        }
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, String.format("performPendingDuty by %s", tag)));
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
        if (mConnectableObservableList.size() > 0) {
            while (null != mConnectableObservableList.peekFirst()) {
                ConnectableObservable connectableObservable = mConnectableObservableList.pollFirst();
                mCompositeDisposable.add(connectableObservable.connect());
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
        if (mIsReady) {
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

    public <T> ConnectableObservable<T> wrap(Observable<T> observable) {
        ConnectableObservable<T> connectableObservable;
        if (observable instanceof ConnectableObservable) {
            connectableObservable = (ConnectableObservable<T>) observable;
        } else {
            connectableObservable = observable.publish();
        }
        if (mIsReady) {
            mCompositeDisposable.add(connectableObservable.connect());
        } else {
            mConnectableObservableList.add(connectableObservable);
        }
        return connectableObservable;
    }

    public <T> void subscribe(Observable<T> observable, DisposableObserver<T> observer) {
        // If fragment will removed don't accept any duty.
        // Don't care isResumed true or false. Because duty be pended and wait perform
        if (!isAdded()) {
            return;
        }
        if (mIsReady) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, "subscribe " + observable.toString()));
            }
            mCompositeDisposable.add(observable.subscribeWith(observer));
        } else {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, "pending subscribe " + observable.toString()));
            }
            ConnectableObservable connectableObservable;
            if (observable instanceof ConnectableObservable) {
                connectableObservable = (ConnectableObservable) observable;
            } else {
                connectableObservable = observable.publish();
            }
            connectableObservable.subscribeWith(observer);
            mConnectableObservableList.add(connectableObservable);
        }
    }
}
