package android.support.v4.app;

import android.os.Bundle;

/**
 * Created by Administrator on 2017/5/11.
 */

public class NumberPickerFragArgs extends FragmentArgs {
    public static final int NULL = Integer.MIN_VALUE;
    private static final String KEY_min = "KEY_min";
    private static final String KEY_max = "KEY_max";
    private static final String KEY_value = "KEY_value";

    public NumberPickerFragArgs() {
    }

    public NumberPickerFragArgs(Bundle bundle) {
        super(bundle);
    }

    public int value() {
        return bundle.getInt(KEY_value, NULL);
    }

    public NumberPickerFragArgs value(int value) {
        bundle.putInt(KEY_value, value);
        return this;
    }

    public int min() {
        return bundle.getInt(KEY_min, 0);
    }

    public NumberPickerFragArgs min(int value) {
        bundle.putInt(KEY_min, value);
        return this;
    }

    public int max() {
        return bundle.getInt(KEY_max, 0);
    }

    public NumberPickerFragArgs max(int value) {
        bundle.putInt(KEY_max, value);
        return this;
    }
}
