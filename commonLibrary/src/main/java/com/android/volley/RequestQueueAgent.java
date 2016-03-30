package com.android.volley;

import android.content.Context;
import android.support.v4.app.ContextHelper;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.OkHttpHurlStack;
import com.lin1987www.http.cookie.CookieHandlerFactory;
import com.lin1987www.http.cookie.CookieKeeper;
import com.lin1987www.os.HandlerHelper;

import java.net.CookieHandler;

public class RequestQueueAgent {
    private static RequestQueue mRequestQueue;
    private static ImageLoader.ImageCache mImageCache;
    private static ImageLoader mImageLoader;
    private static OkHttpHurlStack mOkHttpStack;
    private static CookieKeeper mCookieKeeper;

    public static RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            synchronized (RequestQueueAgent.class) {
                if (mRequestQueue == null) {
                    // 取得 Context 並且進行其設定
                    Context context = ContextHelper.getApplication();
                    // 建立 HttpStack
                    OkHttpHurlStack httpStack = getOkHttpHurlStack();
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

    public static ImageLoader.ImageCache getImageCache() {
        if (mImageCache == null) {
            synchronized (RequestQueueAgent.class) {
                if (mImageCache == null) {
                    mImageCache = new BitmapLruCache();
                }
            }
        }
        return mImageCache;
    }

    public static ImageLoader getImageLoader() {
        if (mImageLoader == null) {
            synchronized (RequestQueueAgent.class) {
                if (mImageLoader == null) {
                    mImageLoader = new ImageLoaderCache(getRequestQueue(), getImageCache());
                }
            }
        }
        return mImageLoader;
    }

    public static CookieHandler getCookieHandler() {
        return getCookieKeeper().manager;
    }

    public static OkHttpHurlStack getOkHttpHurlStack() {
        if (mOkHttpStack == null) {
            synchronized (RequestQueueAgent.class) {
                if (mOkHttpStack == null) {
                    // 建立 HttpStack
                    mOkHttpStack = new OkHttpHurlStack();
                    mOkHttpStack.getOkHttpClientBuilder().cookieJar(getCookieKeeper().cookieJar);
                }
            }
        }
        return mOkHttpStack;
    }

    public static CookieKeeper getCookieKeeper() {
        if (mCookieKeeper == null) {
            synchronized (RequestQueueAgent.class) {
                if (mCookieKeeper == null) {
                    // 建立 HttpStack
                    Context context = ContextHelper.getApplication();
                    mCookieKeeper = CookieHandlerFactory.openCookieHandler(context);
                }
            }
        }
        return mCookieKeeper;
    }
}
