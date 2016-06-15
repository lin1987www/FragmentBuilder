package android.support.v4.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2015/7/5.
 * <p/>
 * BackStackRecord   implements  FragmentManager.BackStackEntry
 * http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.0.1_r1/android/support/v4/app/BackStackRecord.java#BackStackRecord.Op
 * <p/>
 * Show Fragment
 * http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.0.1_r1/android/support/v4/app/FragmentManager.java#FragmentManagerImpl.showFragment%28android.support.v4.app.Fragment%2Cint%2Cint%29
 * <p/>
 * BackStackRecord  run 的時候塞進去 Fragment.mNextAnim    enterAnim
 * <p/>
 * <p/>
 * 執行動畫
 * http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.0.1_r1/android/support/v4/app/FragmentManager.java#1131
 */

public class FragmentUtils {
    private static final String TAG = FragmentUtils.class.getName();
    private static final Field sChildFragmentManagerField;

    static {
        /**
         * BUG : causing a java.IllegalStateException error, No Activity, only
         * when navigating to Fragment for the SECOND time
         * http://stackoverflow.com /questions/15207305/getting-the-error-java-lang-illegalstateexception-activity-has-been-destroyed
         * http://stackoverflow.com/questions/14929907/causing-a-java-illegalstateexception-error-no-activity-only-when-navigating-to
         */
        Field f = null;
        try {
            f = Fragment.class.getDeclaredField("mChildFragmentManager");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Error getting mChildFragmentManager field", e);
        }
        sChildFragmentManagerField = f;
    }

    public static void setChildFragmentManager(Fragment fragment, FragmentManager fragmentManager) {
        if (sChildFragmentManagerField != null) {
            try {
                sChildFragmentManagerField.set(fragment, fragmentManager);
            } catch (Exception e) {
                Log.e(TAG, "Error setting mChildFragmentManager field", e);
            }
        }
    }

    public static void setArguments(Fragment fragment, Bundle args) {
        fragment.mArguments = args;
    }

    public static void putArguments(Fragment fragment, Bundle args) {
        if (fragment.mArguments == null) {
            setArguments(fragment, args);
        } else {
            if (args != null) {
                fragment.mArguments.putAll(args);
            }
        }
    }

    public static boolean getUserVisibleHintAllParent(Fragment f) {
        // 如果 Parent 無法被使用者看到，那childFragment就視為不會被看到
        if (f.getUserVisibleHint()) {
            if (f.getParentFragment() == null) {
                return true;
            } else {
                return getUserVisibleHintAllParent(f.getParentFragment());
            }
        } else {
            return false;
        }
    }

    // SDK 22.0.1
    /*
    public static FragmentActivity getFragmentManagerActivity(FragmentManager fragmentManager) {
        FragmentManagerImpl fm = (FragmentManagerImpl) fragmentManager;
        return fm.mActivity;
    }
    */

    public static FragmentActivity getFragmentManagerActivity(FragmentManager fragmentManager) {
        FragmentHostCallback hostCallback = getFragmentHostCallback(fragmentManager);
        if (hostCallback == null) {
            return null;
        }
        return (FragmentActivity) hostCallback.mContext;
    }

    public static FragmentHostCallback getFragmentHostCallback(FragmentManager fragmentManager) {
        FragmentManagerImpl fm = (FragmentManagerImpl) fragmentManager;
        return fm.mHost;
    }


    public static boolean isFragmentExist(Fragment fragment) {
        if (fragment == null) {
            return false;
        }
        // 顯示中判斷 isAdded
        // 位於 BackStack 中 ( 但可能沒顯示 )
        return fragment.isAdded() || fragment.isInBackStack();
    }

    public static boolean isFragmentAvailable(View view) {
        FragContent content = new FragContent(view);
        Fragment fragment = content.getSrcFragment();
        return isFragmentAvailable(fragment);
    }

    public static boolean isFragmentAvailable(Fragment fragment) {
        if (fragment == null) {
            return false;
        }
        return fragment.isAdded() && fragment.isResumed();
    }

    public static boolean hasSavedViewState(View view) {
        FragContent content = new FragContent(view);
        return hasSavedViewState(content.getSrcFragment());
    }

    public static boolean hasSavedViewState(Fragment fragment) {
        boolean hasSaveState = false;
        if (fragment != null) {
            if (fragment.mSavedViewState != null) {
                hasSaveState = true;
            }
        }
        return hasSaveState;
    }

    public static int getFragmentState(Fragment fragment) {
        return fragment.mState;
    }

    public static boolean isStateLoss(FragmentManager fragmentManager) {
        FragmentManagerImpl fm = (FragmentManagerImpl) fragmentManager;
        boolean isStateLoss = fm.mStateSaved | (fm.mNoTransactionsBecause != null);
        return isStateLoss;
    }

    public static void putAnim(FragmentManager.BackStackEntry backStackEntry, int transit, int styleRes, int enter,
                               int exit, int popEnter, int popExit) {
        // fix rotation screen cause BackStack animation lose.
        BackStackRecord backStackRecord = (BackStackRecord) backStackEntry;
        backStackRecord.setTransition(transit);
        backStackRecord.setTransitionStyle(styleRes);
        backStackRecord.setCustomAnimations(enter, exit, popEnter, popExit);

        BackStackRecord.Op op = backStackRecord.mHead;
        while (op != null) {
            op.enterAnim = enter;
            op.exitAnim = exit;
            op.popEnterAnim = popEnter;
            op.popExitAnim = popExit;
            op = op.next;
        }
    }

    public static boolean isInBackStack(Fragment fragment) {
        return fragment.isInBackStack();
    }

    private static final String key_fragmentLog = "key_fragmentLog";

    public static void log(Fragment fragment, String name) {
        if (fragment.getArguments() == null) {
            putArguments(fragment, new Bundle());
        }
        StringBuilder stringBuilder = new StringBuilder();

        if (fragment.getArguments().getBundle(key_fragmentLog) == null) {
            fragment.getArguments().putBundle(key_fragmentLog, new Bundle());
        }
        Bundle logBundle = fragment.getArguments().getBundle(key_fragmentLog);

        log(logBundle, stringBuilder, "State", String.valueOf(fragment.mState));
        log(logBundle, stringBuilder, "isAdded", String.valueOf(fragment.isAdded()));
        log(logBundle, stringBuilder, "isResumed", String.valueOf(fragment.isResumed()));
        log(logBundle, stringBuilder, "isInBackStack", String.valueOf(fragment.isInBackStack()));
        log(logBundle, stringBuilder, "isDetached", String.valueOf(fragment.isDetached()));
        log(logBundle, stringBuilder, "isRemoving", String.valueOf(fragment.isRemoving()));
        log(logBundle, stringBuilder, "isMenuVisible", String.valueOf(fragment.isMenuVisible()));
        log(logBundle, stringBuilder, "isInLayout", String.valueOf(fragment.isInLayout()));
        log(logBundle, stringBuilder, "isVisible", String.valueOf(fragment.isVisible()));
        log(logBundle, stringBuilder, "isHidden", String.valueOf(fragment.isHidden()));
        log(logBundle, stringBuilder, "getActivity is null", String.valueOf(fragment.getActivity() == null));
        log(logBundle, stringBuilder, "getChildFragmentManager activity is null", String.valueOf(getFragmentManagerActivity(fragment.getChildFragmentManager()) == null));

        if (stringBuilder.length() > 0) {
            stringBuilder.insert(0, String.format("FragmentLog: %s\nMethod: %s\n", (fragment.getTag() == null) ? fragment.toString() : fragment.getTag(), name));
            String log = stringBuilder.toString();
            Log.d(TAG, log);
        }
    }

    private static void log(Bundle logBundle, StringBuilder builder, String key, String value) {
        key = "    " + key;
        if (!logBundle.containsKey(key)
                || !logBundle.getString(key).equals(value)
                ) {
            builder.append(String.format("%s: %s\n", key, value));
            logBundle.putString(key, value);
        }
    }
}
