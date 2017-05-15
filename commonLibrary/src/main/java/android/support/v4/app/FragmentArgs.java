package android.support.v4.app;

import android.os.Bundle;

import com.lin1987www.common.Utility;

/**
 * Created by Administrator on 2015/7/8.
 */
public class FragmentArgs {
    private final static String suffix = FragmentArgs.class.getSimpleName();
    private final static String KEY_fragmentBuilderText = key("KEY_fragmentBuilderText");
    private final static String KEY_isUserVisible = key("KEY_isUserVisible");
    private final static String KEY_consumeReady = key("KEY_consumeReady");

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

    public String getFragmentBuilderText() {
        return bundle.getString(KEY_fragmentBuilderText);
    }

    void setFragmentBuilderText(String value) {
        bundle.putString(KEY_fragmentBuilderText, value);
    }

    boolean getUserVisible() {
        boolean value = bundle.getBoolean(KEY_isUserVisible, true);
        return value;
    }

    void setUserVisible(boolean value) {
        bundle.putBoolean(KEY_isUserVisible, value);
    }

    boolean isConsumeReady() {
        boolean value = bundle.getBoolean(KEY_consumeReady, false);
        if (bundle.containsKey(KEY_consumeReady)) {
            bundle.remove(KEY_consumeReady);
        }
        return value;
    }

    void consumeReady() {
        bundle.putBoolean(KEY_consumeReady, true);
    }
}
