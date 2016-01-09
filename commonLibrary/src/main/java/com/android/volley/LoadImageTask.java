package com.android.volley;

import android.graphics.Bitmap;
import android.support.v4.app.ExecutorSet;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;

import fix.java.util.concurrent.Take;

/**
 * Created by Administrator on 2015/9/9.
 */
public class LoadImageTask extends Take<LoadImageTask> {
    public final String imageUrl;
    public final int maxBoundSize;
    public Bitmap mBitmap;
    ImageLoader.ImageContainer mImageContainer;

    public LoadImageTask(String imageUrl, int maxBoundSize) {
        this.imageUrl = imageUrl;
        this.maxBoundSize = maxBoundSize;
    }

    @Override
    public LoadImageTask take() throws Throwable {
        ExecutorSet.mainThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                mImageContainer = RequestQueueAgent.getmImageLoader().get(imageUrl, new ImageLoader.ImageListener() {
                            @Override
                            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                                if (response.getBitmap() == null) {
                                    return;
                                }
                                mBitmap = response.getBitmap();
                                synchronized (LoadImageTask.this) {
                                    LoadImageTask.this.notify();
                                }
                            }

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                synchronized (LoadImageTask.this) {
                                    LoadImageTask.this.notify();
                                }
                            }
                        },
                        maxBoundSize,
                        maxBoundSize,
                        ImageView.ScaleType.CENTER_CROP
                );
            }
        });
        synchronized (LoadImageTask.this) {
            LoadImageTask.this.wait();
        }
        return this;
    }

    @Override
    public boolean handleException(Throwable ex) {
        return false;
    }
}
