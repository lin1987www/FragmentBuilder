package android.support.v4.app;

import android.os.Bundle;

import com.lin1987www.common.Utility;

/**
 * Created by Administrator on 2015/7/8.
 */
public class FragmentArgs {
    private final static String suffix = FragmentArgs.class.getName();
    private final static String key_userVisibleHintOnResume = key("key_userVisibleHintOnResume");
    private final static String key_fragmentBuilderText = key("key_fragmentBuilderText");
    private final static String key_disableReady = key("key_disableReady");

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
        bundle.setClassLoader(Utility.getClassLoader());
    }

    boolean consumeDisableReady() {
        boolean disableReady = bundle.getBoolean(key_disableReady, false);
        if (bundle.containsKey(key_disableReady)) {
            bundle.remove(key_disableReady);
        }
        return disableReady;
    }

    void disableReady() {
        bundle.putBoolean(key_disableReady, true);
    }

    public boolean isUserVisibleHintOnResume() {
        boolean useNormalResume = bundle.getBoolean(key_userVisibleHintOnResume, false);
        return useNormalResume;
    }

    public void userVisibleHintOnResume() {
        bundle.putBoolean(key_userVisibleHintOnResume, true);
    }

    public String getFragmentBuilderText() {
        return bundle.getString(key_fragmentBuilderText);
    }

    void setFragmentBuilderText(String value) {
        bundle.putString(key_fragmentBuilderText, value);
    }
}
