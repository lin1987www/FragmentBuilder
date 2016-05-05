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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import fix.java.util.concurrent.Duty;
import fix.java.util.concurrent.ExceptionHelper;


/**
 * Created by Administrator on 2015/7/8.
 */
public class FragmentFix extends Fragment {
    public static boolean DEBUG = true;
    public final static String TAG = FragmentFix.class.getSimpleName();
    public final static String key_startActivityFromViewId = "key_startActivityFromViewId";
    protected final String FORMAT = String.format("%s %s", toString(), "%s");

    protected ArrayList<Duty<?>> mDutyList = new ArrayList<>();
    protected final AtomicBoolean mIsEnterAnim = new AtomicBoolean();

    protected int mStartActivityFromViewId = 0;
    protected FragmentArgs mFragmentArgs;
    Animation.AnimationListener mFragmentAnimListener;

    private boolean mHasResumed = false;

    protected void prepareAnim() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onCreateAnimation prepareAnim state: " + mState));
        }
        mIsEnterAnim.set(true);
    }

    protected void endAnim() {
        mIsEnterAnim.set(false);
        performPendingDuty();
    }

    protected void performPendingDuty() {
        if (!mIsEnterAnim.get()) {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        if (0 != mStartActivityFromViewId) {
            View view = getView().findViewById(mStartActivityFromViewId);
            dispatchViewOnActivityResult(view, requestCode, resultCode, data);
            mStartActivityFromViewId = 0;
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if (DEBUG && null != savedInstanceState) {
            Log.d(TAG, String.format(FORMAT, "onViewStateRestored"));
        }
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            mStartActivityFromViewId = savedInstanceState.getInt(key_startActivityFromViewId);
        }
    }

    @Override
    public void onStart() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onStart"));
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onResume"));
        }
        super.onResume();
        // 更新畫面的一切行為
    }

    @Override
    public void onPause() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onPause"));
        }
        mHasResumed = false;
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
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onSaveInstanceState"));
        }
        super.onSaveInstanceState(outState);
        if (0 != mStartActivityFromViewId) {
            outState.putInt(key_startActivityFromViewId, mStartActivityFromViewId);
        }
    }

    @Override
    public void onDestroyView() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onDestroyView"));
        }
        super.onDestroyView();
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
        /* TODO test remove
        if (!lastUserVisibleHint && isVisibleToUser) {
            int state = FragmentUtils.getFragmentState(this);
            if (state == FragmentFix.RESUMED) {
                Log.d(TAG, String.format(FORMAT, "doResume by setUserVisibleHint"));
                doResume();
            }
        }
        */
        if (isVisibleToUser) {
            performPendingDuty();
        }
    }

    Duty mDoResumeDuty = new Duty() {
        @Override
        public void doTask(Object context, Duty previousDuty) throws Throwable {
            if (!FragmentUtils.isFragmentAvailable(FragmentFix.this)) {
                return;
            }
            if (!mHasResumed) {
                mHasResumed = true;
                ////// origin start
                mCalled = false;
                onResume();
                if (!mCalled) {
                    throw new SuperNotCalledException("Fragment " + this
                            + " did not call through to super.onResume()");
                }
                ////// origin end
                // child fragment resume
                List<Fragment> children = getChildFragmentManager().getFragments();
                if (children != null) {
                    for (Fragment f : children) {
                        if (FragmentUtils.isFragmentAvailable(FragmentFix.this)) {
                            if (f instanceof FragmentFix) {
                                Log.d(TAG, String.format(FORMAT, "doResume by children DoResumeDuty"));
                                ((FragmentFix) f).doResume();
                            }
                        }
                    }
                }
            }
            performPendingDuty();
        }
    }.setExecutorService(ExecutorSet.mainThreadExecutor);

    void doResume() {
        // do resume
        mDutyList.add(mDoResumeDuty);
        mDoResumeDuty.submit();
    }

    @Override
    void performResume() {
        if (mChildFragmentManager != null) {
            mChildFragmentManager.noteStateNotSaved();
            mChildFragmentManager.execPendingActions();
        }
        if (getFragmentArgs().consumePopOnResume()) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, "Consume PopOnResume"));
            }
        } else if (!getFragmentArgs().isUseNormalResume() && !FragmentUtils.getUserVisibleHintAllParent(this)) {
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, "Skip OnResume. getUserVisibleHintAllParent() is false."));
            }
        } else {
            Log.d(TAG, String.format(FORMAT, "doResume by performResume"));
            doResume();
        }
        if (mChildFragmentManager != null) {
            mChildFragmentManager.dispatchResume();
            mChildFragmentManager.execPendingActions();
        }
    }

    public void startActivityFromFragment(View view, Intent intent,
                                          int requestCode) {
        if (view.getId() == 0) {
            String msg = String.format("%s's id must being set!", view);
            Log.e(TAG, msg);
            throw new RuntimeException(msg);
        }
        mStartActivityFromViewId = view.getId();
        startActivityForResult(intent, requestCode);
    }

    public interface onActivityResultListener {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    private void dispatchViewOnActivityResult(View view, int requestCode, int resultCode, Intent data) {
        if (view instanceof onActivityResultListener) {
            ((onActivityResultListener) view).onActivityResult(requestCode, resultCode, data);
        } else {
            Class<?> targetClass = view.getClass();
            try {
                Method method = targetClass.getDeclaredMethod("onActivityResult", int.class, int.class, Intent.class);
                if (method != null) {
                    method.invoke(view, requestCode, resultCode, data);
                    return;
                }
            } catch (Throwable ex) {
                System.err.println(String.format("Miss onActivityResult() on %s throwable:\n%s", view, ExceptionHelper.getPrintStackTraceString(ex)));
            }
        }
    }

    public void duty(Duty duty) {
        if (!FragmentUtils.isFragmentAvailable(this)) {
            return;
        }
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
