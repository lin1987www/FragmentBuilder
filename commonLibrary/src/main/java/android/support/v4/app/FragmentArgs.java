package android.support.v4.app;

import android.os.Bundle;

/**
 * Created by Administrator on 2015/7/8.
 */
public class FragmentArgs {
    private final static String suffix = FragmentArgs.class.getName();
    private final static String key_isSkipPopOnReady = key("key_isSkipPopOnReady");
    private final static String key_userVisibleHintOnReady = key("key_userVisibleHintOnReady");
    private final static String key_fragmentBuilderText = key("key_fragmentBuilderText");

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

    boolean consumePopOnReady() {
        boolean isConsumed = bundle.getBoolean(key_isSkipPopOnReady, false);
        if (bundle.containsKey(key_isSkipPopOnReady)) {
            bundle.remove(key_isSkipPopOnReady);
        }
        return isConsumed;
    }

    /**
     * Use Case: When  popBackState  all  detach Fragment will attach again, we ignore it onResume.
     */
    void skipPopOnReady() {
        bundle.putBoolean(key_isSkipPopOnReady, true);
    }

    boolean isUserVisibleHintOnReady() {
        boolean useNormalResume = bundle.getBoolean(key_userVisibleHintOnReady, false);
        return useNormalResume;
    }

    public void userVisibleHintOnReady() {
        bundle.putBoolean(key_userVisibleHintOnReady, true);
    }

    public String getFragmentBuilderText() {
        return bundle.getString(key_fragmentBuilderText);
    }

    void setFragmentBuilderText(String value) {
        bundle.putString(key_fragmentBuilderText, value);
    }
}
