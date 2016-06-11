package android.support.v4.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.lin1987www.common.Utility;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import fix.java.util.concurrent.Duty;


/**
 * Created by Administrator on 2015/7/8.
 */
public class FragmentFix extends Fragment {
    public static boolean DEBUG = Utility.DEBUG;
    public final static String TAG = FragmentFix.class.getSimpleName();
    protected final String FORMAT = String.format("%s %s", toString(), "%s");

    protected ArrayList<Duty<?>> mDutyList = new ArrayList<>();
    protected final AtomicBoolean mIsEnterAnim = new AtomicBoolean();

    protected FragmentArgs mFragmentArgs;
    Animation.AnimationListener mFragmentAnimListener;

    private boolean mIsReady = false;

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
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        FragmentUtils.log(this, "onViewStateRestored");
        if (DEBUG && null != savedInstanceState) {
            Log.d(TAG, String.format(FORMAT, "onViewStateRestored"));
        }
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStart() {
        FragmentUtils.log(this, "onStart before");
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onStart"));
        }
        super.onStart();
        FragmentUtils.log(this, "onStart after");
    }

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onResume"));
        }
        FragmentUtils.log(this, "onResume before");
        super.onResume();
        FragmentUtils.log(this, "onResume after");
    }

    @Override
    public void onPause() {
        FragmentUtils.log(this, "onPause");
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onPause"));
        }
        mIsReady = false;
        if (mDutyList.size() > 0) {
            // Cancel all take
            for (Duty<?> duty : mDutyList) {
                duty.cancel();
            }
            mDutyList.clear();
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, "clear Duty."));
            }
        }
        super.onPause();
        FragmentUtils.log(this, "onPause after");
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
    public void onDestroyView() {
        FragmentUtils.log(this, "onDestroyView before");
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onDestroyView"));
        }
        super.onDestroyView();
        FragmentUtils.log(this, "onDestroyView after");
    }

    @Override
    void performActivityCreated(Bundle savedInstanceState) {
        if (FragmentUtils.getFragmentManagerActivity(mChildFragmentManager) == null) {
            FragmentUtils.setChildFragmentManager(this, null);
        }
        super.performActivityCreated(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, String.format("setUserVisibleHint %s -> %s", getUserVisibleHint(), isVisibleToUser)));
        }
        boolean lastUserVisibleHint = getUserVisibleHint();
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            performPendingDuty("setUserVisibleHint");
        }
        if (getFragmentArgs().isUserVisibleHintOnResume() && !isVisibleToUser) {
            Log.d(TAG, String.format(FORMAT, String.format("isUserVisibleHintOnResume set mIsReady false")));
            mIsReady = false;
        }
    }

    @Override
    void performResume() {
        FragmentUtils.log(this, "performResume");
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
        // TODO  isFragmentAvailable 可能移除
        if (!FragmentUtils.isFragmentAvailable(this)) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("performPendingDuty skip for isFragmentAvailable false by %s", tag)));
            }
            return;
        }
        if (getFragmentArgs().consumePopOnResume()) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("performPendingDuty consume by %s", tag)));
            }
            return;
        }
        if (mIsEnterAnim.get()) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("performPendingDuty skip for anim true by %s", tag)));
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
                        Log.d(TAG, String.format(FORMAT, "perform pending duty " + duty.getClass().getSimpleName()));
                    }
                }
            }
        }
    }

    public void startActivityForResult(Object object, Intent intent, int requestCode) {
        FragmentActivityFix fragmentActivity = (FragmentActivityFix) getContext();
        fragmentActivity.startActivityForResult(object, intent, requestCode);
    }

    public void duty(Duty duty) {
        // TODO 修改
        if (isRemoving() | isDetached()) {
            return;
        }
//        if (!FragmentUtils.isFragmentAvailable(this)) {
//            return;
//        }
        mDutyList.add(duty);
        boolean availableState = isResumed();
        if (availableState && !mIsEnterAnim.get()) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, "submit duty " + duty.getClass().getName()));
            }
            duty.submit();
        } else {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, "add duty " + duty.getClass().getSimpleName()));
            }
        }
    }

    public static void duty(Duty duty, View view) {
        FragContent content = new FragContent(view);
        FragmentFix fragmentFix = (FragmentFix) content.getSrcFragment();
        if (fragmentFix != null) {
            fragmentFix.duty(duty);
        }
    }
}
