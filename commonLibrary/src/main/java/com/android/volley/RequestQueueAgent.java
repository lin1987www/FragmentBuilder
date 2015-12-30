package com.android.volley;

import android.content.Context;
import android.support.v4.app.ContextHelper;
import android.support.v4.app.ExecutorSet;

import com.android.volley.RequestQueue.RequestFilter;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.OkHttpHurlStack;
import com.lin1987www.http.cookie.CookieHandlerFactory;

import java.net.CookieHandler;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author lin 在Context 還沒被建立前，先將所有Request動作先儲存起來，等待 Context建立後，產生真正的
 *         RequestQueue，在執行之前尚未執行的動作。
 */
public abstract class RequestQueueAgent {
    public final static String TAG = RequestQueueAgent.class.getName();

    public RequestQueueAgent() {
    }

    private final Object mLock = new Object();
    private final ConcurrentLinkedQueue<doRequestQueue> mActionQueue = new ConcurrentLinkedQueue<doRequestQueue>();
    private RequestQueue mRequestQueue;

    protected abstract RequestQueue createRequestQueue();

    private boolean mIsWaitingContext = false;
    private final Runnable mInitContext = new Runnable() {
        @Override
        public void run() {
            RequestQueue requestQueue = createRequestQueue();
            setRequestQueue(requestQueue);
        }
    };

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    protected RequestQueue getRequestQueueIfNullInit() {
        if (mRequestQueue == null && mIsWaitingContext == false) {
            synchronized (mLock) {
                if (mRequestQueue == null && mIsWaitingContext == false) {
                    mIsWaitingContext = true;
                    ExecutorSet.nonBlockExecutor.submit(mInitContext);
                }
            }
        }
        return mRequestQueue;
    }

    protected RequestQueue getRequestQueueSync() {
        if (mRequestQueue == null) {
            synchronized (mLock) {
                while (mRequestQueue == null) {
                    try {
                        mLock.wait();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return mRequestQueue;
    }

    public void setRequestQueue(RequestQueue requestQueue) {
        synchronized (mLock) {
            mRequestQueue = requestQueue;
            doRequestQueue action = null;
            do {
                action = mActionQueue.poll();
                if (action != null) {
                    action.apply(getRequestQueueIfNullInit());
                }
            } while (action != null);
            mLock.notifyAll();
        }
    }

    public <T> Request<T> add(Request<T> request) {
        if (getRequestQueueIfNullInit() == null) {
            synchronized (mLock) {
                if (getRequestQueueIfNullInit() == null) {
                    mActionQueue.add(new addAction(request));
                    return request;
                }
            }
        }

        if (getRequestQueueIfNullInit() != null) {
            return getRequestQueueIfNullInit().add(request);
        }

        return null;
    }

    public void cancelAll(final Object tag) {
        if (getRequestQueueIfNullInit() == null) {
            synchronized (mLock) {
                if (getRequestQueueIfNullInit() == null) {
                    mActionQueue.add(new cancelAllAction(tag));
                    return;
                }
            }
        }

        if (getRequestQueueIfNullInit() != null) {
            getRequestQueueIfNullInit().cancelAll(tag);
            return;
        }
    }

    public void cancelAll(RequestFilter filter) {
        if (getRequestQueueIfNullInit() == null) {
            synchronized (mLock) {
                if (getRequestQueueIfNullInit() == null) {
                    mActionQueue.add(new cancelAllAction(filter));
                    return;
                }
            }
        }

        if (getRequestQueueIfNullInit() != null) {
            getRequestQueueIfNullInit().cancelAll(filter);
            return;
        }
    }

    public interface doRequestQueue {
        void apply(RequestQueue queue);
    }

    public static class addAction implements doRequestQueue {
        private Request<?> mRequest;

        public addAction(Request<?> request) {
            mRequest = request;
        }

        public void apply(RequestQueue queue) {
            queue.add(mRequest);
        }
    }

    public static class cancelAllAction implements doRequestQueue {
        private RequestFilter mFilter;

        public cancelAllAction(RequestFilter filter) {
            mFilter = filter;
        }

        public cancelAllAction(final Object tag) {
            mFilter = new RequestFilterObject(tag);
        }

        public void apply(RequestQueue queue) {
            queue.cancelAll(mFilter);
        }
    }

    public static class RequestFilterObject implements RequestFilter {
        private Object tag;

        public RequestFilterObject(Object tag) {
            this.tag = tag;
        }

        @Override
        public boolean apply(Request<?> request) {
            return request.getTag() == tag;
        }
    }

    private ImageLoader.ImageCache imageCache;

    public ImageLoader.ImageCache getImageCache() {
        if (imageCache == null) {
            imageCache = new BitmapLruCache();
        }
        return imageCache;
    }

    private ImageLoader imageLoader;

    public ImageLoader getImageLoader() {
        if (imageLoader == null) {
            getRequestQueueIfNullInit();
            imageLoader = new ImageLoaderCache(getRequestQueueSync(), getImageCache());
        }
        return imageLoader;
    }

    private static final RequestQueueAgent mDefault = new RequestQueueAgent() {
        @Override
        protected RequestQueue createRequestQueue() {
            // 取得 Context 並且進行其設定
            Context context = ContextHelper.getApplication();
            // 建立 Cookie 處理者
            CookieHandler cookieHandler = CookieHandlerFactory.openCookieHandler(context);
            // 建立 HttpStack
            OkHttpHurlStack httpStack = new OkHttpHurlStack();
            httpStack.getOkHttpClient().setCookieHandler(cookieHandler);
            RequestQueue requestQueue = RequestQueueExtra.newRequestQueue(context, httpStack);
            return requestQueue;
        }
    };

    public static RequestQueueAgent getDefault() {
        return mDefault;
    }
}
