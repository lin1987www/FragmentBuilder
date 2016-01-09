package com.android.volley;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Message;
import android.support.v4.app.ContextHelper;
import android.view.animation.AlphaAnimation;

import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.devspark.appmsg.AppMsg;
import com.lin1987www.common.R;
import com.lin1987www.os.HandlerHelper;
import com.lin1987www.os.StartedHandler;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;


public class RequestQueueExtra extends RequestQueue {
    public static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;
    public final static String TAG = RequestQueueExtra.class.getName();

    public RequestQueueExtra(Cache cache, Network network, int threadPoolSize,
                             ResponseDelivery delivery) {
        super(cache, network, threadPoolSize, delivery);
    }

    public RequestQueueExtra(Cache cache, Network network, int threadPoolSize) {
        super(cache, network, threadPoolSize);
    }

    public RequestQueueExtra(Cache cache, Network network) {
        super(cache, network);
    }

    @Override
    public <T> Request<T> add(Request<T> request) {
        if (requestCounter.incrementAndGet() == 1) {
            postDaley(DO_SOMETHING, 100L);
        }
        return super.add(request);
    }

    @Override
    <T> void finish(Request<T> request) {
        super.finish(request);
        if (requestCounter.decrementAndGet() == 0) {
            postDaley(DO_SOMETHING, 1000L);
        }
    }

    private static void postDaley(int what, Long delay) {
        handler.removeMessages(what);
        handler.sendEmptyMessageDelayed(what, delay);
    }

    private final static AtomicInteger requestCounter = new AtomicInteger(0);
    private static AppMsg loadingMsg;
    private final static Integer DO_SOMETHING = 0x1;
    private final static android.os.Handler.Callback handlerCallback = new android.os.Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == DO_SOMETHING) {
                responseState();
            }
            // 回傳 true 就不進行預設的行為了
            return true;
        }
    };
    private final static StartedHandler handler = StartedHandler.create(TAG, handlerCallback);

    private static void responseState() {
        if (ContextHelper.getFragmentActivity() == null) {
            return;
        }

        if (requestCounter.get() > 0 && loadingMsg == null) {
            loadingMsg = AppMsg.makeText(
                    ContextHelper.getFragmentActivity(),
                    R.string.app_msg_loading,
                    new AppMsg.Style(AppMsg.LENGTH_STICKY, android.R.color.black),
                    R.layout.app_msg_loading);
            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 1.0f);
            loadingMsg.setAnimation(alphaAnimation, alphaAnimation);
            HandlerHelper.runMainThread(new Runnable() {
                @Override
                public void run() {
                    loadingMsg.show();
                }
            });
        } else if (requestCounter.get() == 0 && loadingMsg != null) {
            HandlerHelper.runMainThread(new Runnable() {
                @Override
                public void run() {
                    loadingMsg.cancel();
                    loadingMsg = null;
                }
            });
        }
    }

    /**
     * Default on-disk cache directory.
     */
    private static final String DEFAULT_CACHE_DIR = "volley";

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @param stack   An {@link HttpStack} to use for the network, or null for default.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context, HttpStack stack, int threadPoolSize,
                                               ResponseDelivery delivery) {
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);

        String userAgent = "volley/0";
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            userAgent = packageName + "/" + info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }

        if (stack == null) {
            if (Build.VERSION.SDK_INT >= 9) {
                stack = new HurlStack();
            } else {
                // Prior to Gingerbread, HttpUrlConnection was unreliable.
                // See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
                stack = new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
            }
        }

        Network network = new BasicNetwork(stack);
        // Using RequestQueueExtra
        RequestQueue queue = new RequestQueueExtra(new DiskBasedCache(cacheDir), network, threadPoolSize, delivery);
        queue.start();

        return queue;
    }
}
