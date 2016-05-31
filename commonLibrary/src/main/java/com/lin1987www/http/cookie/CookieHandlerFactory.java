package com.lin1987www.http.cookie;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.util.Map;
import java.util.WeakHashMap;

public class CookieHandlerFactory {
    private static final String TAG = CookieHandlerFactory.class.getName();
    private static final Map<Context, CookieKeeper> mCookieHandlerMap = new WeakHashMap<>();
    private static final Object mLock = new Object();
    public static boolean enableApplicationContext = false;

    public static CookieKeeper openCookieHandler(Context context) {
        // TODO 記得改成
        Context targetContext = context;
        if (enableApplicationContext) {
            targetContext = targetContext.getApplicationContext();
        }
        if (!mCookieHandlerMap.containsKey(targetContext)) {
            synchronized (mLock) {
                if (!mCookieHandlerMap.containsKey(targetContext)) {
                    mCookieHandlerMap.put(targetContext,
                            generateCookieHandler(targetContext));
                }
            }
        }
        return mCookieHandlerMap.get(targetContext);
    }

    public static void closeCookieHandler(Context context) {
        // TODO 記得改成
        Context targetContext = context;
        if (enableApplicationContext) {
            targetContext = targetContext.getApplicationContext();
        }
        if (mCookieHandlerMap.containsKey(targetContext)) {
            synchronized (mLock) {
                if (mCookieHandlerMap.containsKey(targetContext)) {
                    mCookieHandlerMap.remove(targetContext);
                }
            }
        }
    }

    private static CookieKeeper generateCookieHandler(final Context context) {
        Dao<Cookie, String> dao = null;
        try {
            dao = (new DatabaseHelper(context)).getCookieDao();
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            return null;
        }
        CookieKeeper keeper = new CookieKeeper(dao);
        return keeper;
    }
}
