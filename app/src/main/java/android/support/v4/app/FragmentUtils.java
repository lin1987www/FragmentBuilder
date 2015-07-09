package android.support.v4.app;

import android.os.Bundle;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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

}
