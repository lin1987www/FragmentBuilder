package com.lin1987www.app;

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
    private final static String key_isDeferAnimOnResume = key("key_isDeferAnimOnResume");

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

    public boolean consumeRestoreOnResume() {
        boolean isConsumed = bundle.getBoolean(key_isSkipRestoreOnResume, false);
        if (bundle.containsKey(key_isSkipRestoreOnResume)) {
            bundle.remove(key_isSkipRestoreOnResume);
        }
        return isConsumed;
    }

    public void skipRestoreOnResume() {
        bundle.putBoolean(key_isSkipRestoreOnResume, true);
    }

    public boolean consumeOnResume() {
        boolean restoreOnResume = consumeRestoreOnResume();
        boolean popOnResume = consumePopOnResume();
        return (restoreOnResume || popOnResume);
    }

    public void deferAnimOnResume() {
        bundle.putBoolean(key_isDeferAnimOnResume, true);
    }

    public boolean consumeAnimOnResume() {
        boolean isConsumed = bundle.getBoolean(key_isDeferAnimOnResume, false);
        if (bundle.containsKey(key_isDeferAnimOnResume)) {
            bundle.remove(key_isDeferAnimOnResume);
        }
        return isConsumed;
    }
}
