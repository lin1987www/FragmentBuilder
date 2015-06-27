package fix.android.support.v4.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2015/4/2.
 */
public class FragmentUtils {
    private static final String TAG = FragmentUtils.class.getName();
    private static final Field sChildFragmentManagerField;

    static {
        /**
         * BUG 說明 causing a java.IllegalStateException error, No Activity, only
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

    private static final Field sArgumentsField;

    static {
        Field f = null;
        try {
            f = Fragment.class.getDeclaredField("mArguments");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Error getting mArguments field", e);
        }
        sArgumentsField = f;
    }

    public static void onDetachFix(Fragment fragment) {
        if (sChildFragmentManagerField != null) {
            try {
                sChildFragmentManagerField.set(fragment, null);
            } catch (Exception e) {
                Log.e(TAG, "Error setting mChildFragmentManager field", e);
            }
        }
    }

    public static void setArguments(Fragment fragment, Bundle arguments) {
        if (fragment.getArguments() == null || arguments == null) {
            if (arguments != null) {
                arguments.setClassLoader(fragment.getClass().getClassLoader());
            }
            if (sArgumentsField != null) {
                try {
                    sArgumentsField.set(fragment, arguments);
                } catch (Exception e) {
                    Log.e(TAG, "Error setting mArguments field", e);
                }
            }
        } else {
            fragment.getArguments().clear();
            fragment.getArguments().putAll(arguments);
        }
    }

    public static void putArguments(Fragment fragment, Bundle arguments) {
        if (fragment.getArguments() == null || arguments == null) {
            if (arguments != null) {
                arguments.setClassLoader(fragment.getClass().getClassLoader());
            }
            if (sArgumentsField != null) {
                try {
                    sArgumentsField.set(fragment, arguments);
                } catch (Exception e) {
                    Log.e(TAG, "Error setting mArguments field", e);
                }
            }
        } else {
            // Same key will be overwrite, whatever different type.
            fragment.getArguments().putAll(arguments);
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
}
