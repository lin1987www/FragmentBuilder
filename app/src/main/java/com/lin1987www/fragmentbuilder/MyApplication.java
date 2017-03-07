package com.lin1987www.fragmentbuilder;

import android.app.ApplicationFix;

import com.lin1987www.common.Utility;

/**
 * Created by Administrator on 2016/5/6.
 */
public class MyApplication extends ApplicationFix {
    public MyApplication() {
        Utility.DEBUG = BuildConfig.DEBUG;
    }
}
