package fix.android.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.v4.app.ContextHelper;

/**
 * Created by lin on 2014/8/28.
 */
public class ApplicationFix extends Application {
    public static final String TAG = ApplicationFix.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();
        ContextHelper.setApplication(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
