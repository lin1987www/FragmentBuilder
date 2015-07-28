package android.support.v4.app;

import android.os.Bundle;

/**
 * Created by Administrator on 2015/7/8.
 */
public class FragmentArgs {
    private final static String suffix = FragmentArgs.class.getName();

    private static String key(String name) {
        return String.format("%s_%s", name, suffix);
    }

    private final static String key_isSkipPopOnResume = key("key_isSkipPopOnResume");
    private final static String key_isSkipRestoreOnResume = key("key_isSkipRestoreOnResume");

    //
    public final Bundle bundle;
    //

    public FragmentArgs() {
        bundle = new Bundle();
    }

    public FragmentArgs(Bundle bundle) {
        this.bundle = bundle;
    }

    public boolean consumePopOnResume() {
        boolean isConsumed = bundle.getBoolean(key_isSkipPopOnResume, false);
        if (bundle.containsKey(key_isSkipPopOnResume)) {
            bundle.remove(key_isSkipPopOnResume);
        }
        return isConsumed;
    }

    /**
     * Use Case: When  popBackState  all  detach Fragment will attach again, we ignore it onResume.
     */
    public void skipPopOnResume() {
        bundle.putBoolean(key_isSkipPopOnResume, true);
    }

    public boolean consumeOnResume() {
        boolean popOnResume = consumePopOnResume();
        return (popOnResume);
    }
}