package android.support.v4.app;

import android.os.Bundle;

/**
 * Created by Administrator on 2016/6/3.
 */
public class DialogFragArgs extends FragmentArgs {
    private static final String KEY_fragClassName = "KEY_fragClassName";
    private static final String KEY_fragTag = "KEY_fragTag";
    private static final String KEY_fragArgs = "KEY_fragArgs";
    private static final String KEY_message = "KEY_message";

    public DialogFragArgs() {
        super();
    }

    public DialogFragArgs(Bundle bundle) {
        super(bundle);
    }

    public DialogFragArgs setFragClass(Class<? extends Fragment> fragClass) {
        bundle.putString(KEY_fragClassName, fragClass.getName());
        return this;
    }

    public String getFragClassName() {
        return bundle.getString(KEY_fragClassName);
    }

    public DialogFragArgs setFragTag(String value) {
        bundle.putString(KEY_fragTag, value);
        return this;
    }

    public String getFragTag() {
        return bundle.getString(KEY_fragTag);
    }

    public DialogFragArgs setFragArgs(Bundle value) {
        bundle.putParcelable(KEY_fragArgs, value);
        return this;
    }

    public Bundle getFragArgs() {
        return bundle.getBundle(KEY_fragArgs);
    }

    public DialogFragArgs setMessage(String value) {
        bundle.putString(KEY_message, value);
        return this;
    }

    public String getMessage() {
        return bundle.getString(KEY_message);
    }
}
