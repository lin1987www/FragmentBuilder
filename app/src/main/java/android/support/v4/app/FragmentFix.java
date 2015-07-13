package android.support.v4.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.lin1987www.app.FragmentArgs;

/**
 * Created by Administrator on 2015/7/8.
 */
public class FragmentFix extends Fragment {
    public final static String TAG = FragmentFix.class.getSimpleName();
    FragmentArgs fragmentArgs;
    Animation.AnimationListener fragmentAnimListener;


    public FragmentArgs getFragmentArgs() {
        if (fragmentArgs == null) {
            fragmentArgs = new FragmentArgs(getArguments());
        }
        return fragmentArgs;
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
                            anim = FragmentManagerImpl.makeFadeAnimation(mActivity, 0, 1);
                            break;
                        case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT:
                            anim = FragmentManagerImpl.makeFadeAnimation(mActivity, 1, 0);
                            break;
                    }
                }
            }
        }
        if (anim != null && enter) {
            fragmentAnimListener = new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    doResume();
                    fragmentAnimListener = null;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            };
            anim.setAnimationListener(fragmentAnimListener);
        }
        return anim;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // 如果是回復狀態  就略過一次 Fragment 的建立
        // TODO 有點不需要的功能
        if (savedInstanceState != null) {
            getFragmentArgs().skipRestoreOnResume();
        }

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
    void performResume() {
        if (mChildFragmentManager != null) {
            mChildFragmentManager.noteStateNotSaved();
            mChildFragmentManager.execPendingActions();
        }
        if (getFragmentArgs().consumePopOnResume()) {
            Log.w(TAG, String.format("Consume OnResume. %s", this));
        } else if (getFragmentArgs().consumeRestoreOnResume()) {
            Log.w(TAG, String.format("Consume reAttach. %s", this));
        } else {
            //  動畫執行結束後，在執行 onResume
            if (fragmentArgs.consumeAnimOnResume()) {
                // Defer onResume
            } else {
                doResume();
            }
        }
        if (mChildFragmentManager != null) {
            mChildFragmentManager.dispatchResume();
            mChildFragmentManager.execPendingActions();
        }
    }
}
