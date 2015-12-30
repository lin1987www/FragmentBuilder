package com.android.volley;

import android.graphics.Bitmap;

import com.android.volley.toolbox.ImageRequest;

/**
 * Created by lin on 2014/12/30.
 */
public class ImageRequestCache extends ImageRequest {
    public static Long MIN_MAX_AGE = 0L;

    public ImageRequestCache(String url, Response.Listener<Bitmap> listener, int maxWidth, int maxHeight, Bitmap.Config decodeConfig, Response.ErrorListener errorListener) {
        super(url, listener, maxWidth, maxHeight, decodeConfig, errorListener);
    }

    @Override
    protected Response<Bitmap> parseNetworkResponse(NetworkResponse response) {
        RequestExtra.putMinMaxAge(response, MIN_MAX_AGE);
        return super.parseNetworkResponse(response);
    }
}
