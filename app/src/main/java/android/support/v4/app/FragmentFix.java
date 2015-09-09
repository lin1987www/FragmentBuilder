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
import java.util.concurrent.atomic.AtomicBoolean;

import fix.java.util.concurrent.ExceptionHelper;
import fix.java.util.concurrent.Take;
import fix.java.util.concurrent.TakeWrap;
import fix.java.util.concurrent.Takeout;


/**
 * Created by Administrator on 2015/7/8.
 */
public class FragmentFix extends Fragment {
    public static boolean DEBUG = true;
    public final static String TAG = FragmentFix.class.getSimpleName();
    public final static String key_startActivityFromViewId = "key_startActivityFromViewId";
    protected final String ID = String.format("%s", toString());

    protected ArrayList<Take<?>> mTakeList = new ArrayList<>();
    protected ArrayList<Takeout.OutTaker> mTakeoutList = new ArrayList<>();
    protected final AtomicBoolean mIsAnim = new AtomicBoolean();

    protected int mStartActivityFromViewId = 0;
    protected FragmentArgs mFragmentArgs;
    Animation.AnimationListener mFragmentAnimListener;

    public boolean afterAnimTakeout(Takeout.OutTaker outTaker) {
        if (mIsAnim.get()) {
            synchronized (this) {
                mTakeoutList.add(outTaker);
            }
            return true;
        } else {
            if (getParentFragment() instanceof FragmentFix) {
                return ((FragmentFix) getParentFragment()).afterAnimTakeout(outTaker);
            }
            return false;
        }
    }

    protected void prepareAnim() {
        mIsAnim.set(true);
    }

    protected void endAnim() {
        mIsAnim.set(false);
        if (mTakeoutList.size() > 0) {
            synchronized (this) {
                for (Takeout.OutTaker outTaker : mTakeoutList) {
                    outTaker.run();
                }
                mTakeoutList.clear();
            }
        }
    }

    public FragmentArgs getFragmentArgs() {
        if (mFragmentArgs == null) {
            mFragmentArgs = new FragmentArgs(getArguments());
        }
        return mFragmentArgs;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Animation anim = null;
        if (anim == null) {
            // Use customs
            if (mNextAnim != 0) {
                anim = AnimationUtils.loadAnimation(mActivity, mNextAnim);
            }
        }
        if (anim == null) {
            // Use transit
            if (transit != 0) {
                int styleIndex = FragmentManagerImpl.transitToStyleIndex(transit, enter);
                if (styleIndex >= 0) {
                    switch (styleIndex) {
                        case FragmentManagerImpl.ANIM_STYLE_OPEN_ENTER:
                            anim = FragmentManagerImpl.makeOpenCloseAnimation(mActivity, 1.125f, 1.0f, 0, 1);
                            break;
                        case FragmentManagerImpl.ANIM_STYLE_OPEN_EXIT:
                            anim = FragmentManagerImpl.makeOpenCloseAnimation(mActivity, 1.0f, .975f, 1, 0);
                            break;
                        case FragmentManagerImpl.ANIM_STYLE_CLOSE_ENTER:
                            anim = FragmentManagerImpl.makeOpenCloseAnimation(mActivity, .975f, 1.0f, 0, 1);
                            break;
                        case FragmentManagerImpl.ANIM_STYLE_CLOSE_EXIT:
                            anim = FragmentManagerImpl.makeOpenCloseAnimation(mActivity, 1.0f, 1.075f, 1, 0);
                            break;
                        case FragmentManagerImpl.ANIM_STYLE_FADE_ENTER:
                            //anim = FragmentManagerImpl.makeFadeAnimation(mActivity, 0, 1);
                            // TODO 效果進行測試
                            anim = new AlphaAnimation(0, 1);
                            anim.setInterpolator(FragmentManagerImpl.DECELERATE_CUBIC);
                            anim.setDuration(3000);
                            break;
                        case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT:
                            //anim = FragmentManagerImpl.makeFadeAnimation(mActivity, 1, 0);
                            // TODO 效果進行測試
                            anim = new AlphaAnimation(1, 0);
                            anim.setInterpolator(FragmentManagerImpl.DECELERATE_CUBIC);
                            anim.setDuration(3000);
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

                }

                @Override
                public void onAnimationEnd(Animation animation) {
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
            Log.e(TAG, "onCreate " + ID);
        }

        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.e(TAG, "onActivityResult " + ID);
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
            Log.e(TAG, "onViewStateRestored " + ID);
        }
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            mStartActivityFromViewId = savedInstanceState.getInt(key_startActivityFromViewId);
        }
    }

    @Override
    public void onStart() {
        if (DEBUG) {
            Log.e(TAG, "onStart " + ID);
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        if (DEBUG) {
            Log.e(TAG, "onResume " + ID);
        }
        super.onResume();
        // 更新畫面的一切行為
    }

    @Override
    public void onPause() {
        if (DEBUG) {
            Log.e(TAG, "onPause " + ID);
        }
        if (mTakeList.size() > 0) {
            // Cancel all take
            for (Take<?> take : mTakeList) {
                take.cancel();
            }
            mTakeList.clear();
            Log.e(TAG, "clear Take.");
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (DEBUG) {
            Log.e(TAG, "onSaveInstanceState " + ID);
        }
        super.onSaveInstanceState(outState);
        if (0 != mStartActivityFromViewId) {
            outState.putInt(key_startActivityFromViewId, mStartActivityFromViewId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    void doResume() {
        mCalled = false;
        onResume();
        if (!mCalled) {
            throw new SuperNotCalledException("Fragment " + this
                    + " did not call through to super.onResume()");
        }
    }

    @Override
    void performActivityCreated(Bundle savedInstanceState) {
        if (FragmentUtils.getFragmentManagerActivity(mChildFragmentManager) == null) {
            FragmentUtils.setChildFragmentManager(this, null);
        }
        super.performActivityCreated(savedInstanceState);
    }

    @Override
    void performResume() {
        if (mChildFragmentManager != null) {
            mChildFragmentManager.noteStateNotSaved();
            mChildFragmentManager.execPendingActions();
        }
        if (getFragmentArgs().consumePopOnResume()) {
            Log.w(TAG, String.format("Consume OnResume. %s", this));
        } else {
            doResume();
        }
        if (mChildFragmentManager != null) {
            mChildFragmentManager.dispatchResume();
            mChildFragmentManager.execPendingActions();
        }
    }

    public TakeoutBuilder take(Take<?> take) {
        return take(new TakeWrap<Take<?>>(take));
    }

    public TakeoutBuilder take(TakeWrap<Take<?>> takeWrap) {
        TakeoutBuilder builder = TakeoutBuilder.create(this, takeWrap, this);
        mTakeList.add(takeWrap.getTask());
        return builder;
    }

    public static TakeoutBuilder take(Take<?> take, View view) {
        return take(new TakeWrap<Take<?>>(take), view);
    }

    public static TakeoutBuilder take(TakeWrap<Take<?>> takeWrap, View view) {
        FragmentFix fragmentFix = (FragmentFix) FragmentBuilder.FragmentPath.findFragmentByView(view);
        TakeoutBuilder builder = TakeoutBuilder.create(view, takeWrap, fragmentFix);
        fragmentFix.mTakeList.add(takeWrap.getTask());
        return builder;
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

}
