package fix.android.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.v4.app.ContextHelper;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

import fix.java.util.concurrent.ExceptionHelper;

/**
 * Created by lin on 2014/8/28.
 */
public class ApplicationFix extends Application implements ExceptionHelper.ExceptionPrinter {
    public static final String TAG = ApplicationFix.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();
        ContextHelper.setApplication(this);
        LeakCanary.install(this);
        ExceptionHelper.addExceptionPrinter(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void printException(String taskName, Throwable ex) {
        String error = String.format("%s\n%s", taskName, ExceptionHelper.getPrintStackTraceString(ex));
        Log.e(TAG, error);
    }
}
