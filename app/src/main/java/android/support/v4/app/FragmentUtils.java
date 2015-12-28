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
        FragmentManagerImpl fm = (FragmentManagerImpl) fragmentManager;
        return (FragmentActivity) fm.mHost.mContext;
    }

    public static FragmentHostCallback getFragmentHostCallback(FragmentManager fragmentManager) {
        FragmentManagerImpl fm = (FragmentManagerImpl) fragmentManager;
        return fm.mHost;
    }

    public static boolean isFragmentAvailable(View view) {
        Fragment fragment = FragmentBuilder.FragmentPath.findFragmentByView(view);
        return isFragmentAvailable(fragment);
    }

    public static boolean isFragmentAvailable(Fragment fragment) {
        boolean isAvailable = true;
        if (fragment == null) {
            isAvailable = false;
        } else {
            if (fragment.isRemoving() || fragment.isDetached() || fragment.getActivity() == null) {
                isAvailable = false;
            } else if (getFragmentManagerActivity(fragment.getChildFragmentManager()) == null) {
                isAvailable = false;
            }
        }
        return isAvailable;
    }
}
