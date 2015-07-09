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

    public final static String key_isSkipPerformResume = key("key_isSkipPerformResume");
    public final static String key_isSkipReAttach = key("key_isSkipReAttach");

    //
    public final Bundle bundle;
    //

    public FragmentArgs() {
        bundle = new Bundle();
    }

    public FragmentArgs(Bundle bundle) {
        this.bundle = bundle;
    }

    public boolean consumeOnResume() {
        boolean isSkipPerformResume = bundle.getBoolean(key_isSkipPerformResume, false);
        if (bundle.containsKey(key_isSkipPerformResume)) {
            bundle.remove(key_isSkipPerformResume);
        }
        return isSkipPerformResume;
    }

    /**
     * Use Case: When  popBackState  all  detach Fragment will attach again, we ignore it onResume.
     */
    public void skipOnResume() {
        bundle.putBoolean(key_isSkipPerformResume, true);
    }

    public boolean consumeReAttach() {
        boolean isSkipReAttach = bundle.getBoolean(key_isSkipReAttach, false);
        if (bundle.containsKey(key_isSkipReAttach)) {
            bundle.remove(key_isSkipReAttach);
        }
        return isSkipReAttach;
    }

    public void skipReAttach() {
        bundle.putBoolean(key_isSkipReAttach, true);
    }
}
