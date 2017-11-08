package android.support.v7.widget;

import android.os.Bundle;
import android.support.v4.app.FragmentArgs;

/**
 * Created by Administrator on 2016/7/6.
 */
public class SelectControllerFragArgs extends FragmentArgs {
    public final static String KEY_selectController = "KEY_selectController";

    public SelectControllerFragArgs() {
        super();
    }

    public SelectControllerFragArgs(Bundle bundle) {
        super(bundle);
    }

    private SelectController mSelectController;

    public SelectController getSelectController() {
        if (mSelectController == null) {
            mSelectController = bundle.getParcelable(KEY_selectController);
        }
        return mSelectController;
    }

    public SelectControllerFragArgs setSelectController(SelectController value) {
        bundle.putParcelable(KEY_selectController, value);
        return this;
    }
}
