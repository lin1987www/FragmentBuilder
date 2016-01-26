package android.support.v4.app;

import android.os.Bundle;

/**
 * Created by Administrator on 2015/7/8.
 */
public class FragmentArgs {
    private final static String suffix = FragmentArgs.class.getName();
    private final static String key_isSkipPopOnResume = key("key_isSkipPopOnResume");
    private final static String key_useNormalResume = key("key_useNormalResume");

    private static String key(String name) {
        return String.format("%s_%s", name, suffix);
    }

    //
    public final Bundle bundle;
    //

    public FragmentArgs() {
        bundle = new Bundle();
    }

    public FragmentArgs(Bundle bundle) {
        this.bundle = bundle;
    }

    boolean consumePopOnResume() {
        boolean isConsumed = bundle.getBoolean(key_isSkipPopOnResume, false);
        if (bundle.containsKey(key_isSkipPopOnResume)) {
            bundle.remove(key_isSkipPopOnResume);
        }
        return isConsumed;
    }
    /**
     * Use Case: When  popBackState  all  detach Fragment will attach again, we ignore it onResume.
     */
    void skipPopOnResume() {
        bundle.putBoolean(key_isSkipPopOnResume, true);
    }

    boolean isUseNormalResume(){
        boolean useNormalResume = bundle.getBoolean(key_useNormalResume, false);
        return useNormalResume;
    }

    public void useNormalResume(){
        bundle.putBoolean(key_useNormalResume, true);
    }
}
