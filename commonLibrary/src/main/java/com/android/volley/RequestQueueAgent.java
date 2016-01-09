package com.android.volley;

import android.content.Context;
import android.support.v4.app.ContextHelper;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.OkHttpHurlStack;
import com.lin1987www.http.cookie.CookieHandlerFactory;
import com.lin1987www.os.HandlerHelper;

import java.net.CookieHandler;

public class RequestQueueAgent {
    private static RequestQueue mRequestQueue;
    private static ImageLoader.ImageCache mImageCache;
    private static ImageLoader mImageLoader;

    public static RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            synchronized (RequestQueueAgent.class) {
                if (mRequestQueue == null) {
                    // 取得 Context 並且進行其設定
                    Context context = ContextHelper.getApplication();
                    // 建立 Cookie 處理者
                    CookieHandler cookieHandler = CookieHandlerFactory.openCookieHandler(context);
                    // 建立 HttpStack
                    OkHttpHurlStack httpStack = new OkHttpHurlStack();
                    httpStack.getOkHttpClient().setCookieHandler(cookieHandler);
                    mRequestQueue = RequestQueueExtra.newRequestQueue(
                            context,
                            httpStack,
                            RequestQueueExtra.DEFAULT_NETWORK_THREAD_POOL_SIZE,
                            new ExecutorDelivery(HandlerHelper.getAnotherThreadHandler())
                    );
                }
            }
        }
        return mRequestQueue;
    }

    public static ImageLoader.ImageCache getmImageCache() {
        if (mImageCache == null) {
            synchronized (RequestQueueAgent.class) {
                if (mImageCache == null) {
                    mImageCache = new BitmapLruCache();
                }
            }
        }
        return mImageCache;
    }

    public static ImageLoader getmImageLoader() {
        if (mImageLoader == null) {
            synchronized (RequestQueueAgent.class) {
                if (mImageLoader == null) {
                    mImageLoader = new ImageLoaderCache(getRequestQueue(), getmImageCache());
                }
            }
        }
        return mImageLoader;
    }
}
