package com.lin1987www.location;

import android.location.Location;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueueAgent;
import com.lin1987www.common.util.concurrent.Result;
import com.lin1987www.jackson.JacksonHelper;
import com.lin1987www.jackson.JavaTypeRequest;
import com.lin1987www.location.google.geocode.GoogleGeocodeResults;

import java.util.Locale;
import java.util.Map;

import fix.java.util.concurrent.Take;

/**
 * Created by Administrator on 2015/4/14.
 */
public class GoogleGeocodeGet extends Take<GoogleGeocodeGet> {
    public final Double mLatitude;
    public final Double mLongitude;
    public final String mAddress;
    public final Locale mLocale;

    private GoogleGeocodeResults mGoogleGeocodeResults;

    public GoogleGeocodeResults getGoogleGeocodeResults() {
        return mGoogleGeocodeResults;
    }

    public GoogleGeocodeGet(Location location) {
        this(location.getLatitude(), location.getLongitude());
    }

    public GoogleGeocodeGet(Double lat, Double lng) {
        this(lat, lng, null);
        if (mLongitude == null && mLatitude == null) {
            throw new NullPointerException();
        }
    }

    public GoogleGeocodeGet(String address) {
        this(null, null, address);
        if (TextUtils.isEmpty(address)) {
            throw new NullPointerException();
        }
    }

    private GoogleGeocodeGet(Double lat, Double lng, String address) {
        this(lat, lng, address, Locale.TAIWAN);
    }

    private GoogleGeocodeGet(Double lat, Double lng, String address, Locale locale) {
        this.mLatitude = lat;
        this.mLongitude = lng;
        this.mAddress = address;
        this.mLocale = locale;
    }

    @Override
    public GoogleGeocodeGet take() throws Throwable {
        JavaTypeRequest<GoogleGeocodeResults> request = new JavaTypeRequest<GoogleGeocodeResults>(
                Request.Method.GET,
                "https://maps.googleapis.com/maps/api/geocode/json?sensor=false",
                JacksonHelper.GenericType(GoogleGeocodeResults.class)
        );
        Map<String, String> query = request.getQuery();
        query.put("language", mLocale.toString());
        if (mLongitude != null && mLatitude != null) {
            query.put("latlng", String.format("%1$s,%2$s", mLatitude, mLongitude));
        } else if (!TextUtils.isEmpty(mAddress)) {
            query.put("address", mAddress);
        }
        RequestQueueAgent.getRequestQueue().add(request);
        Result<GoogleGeocodeResults> result = request.getResultSync();
        mGoogleGeocodeResults = result.result;
        ex = result.error;
        if (mGoogleGeocodeResults != null) {
            mGoogleGeocodeResults.setResponseText(request.getResponseText());
        }
        return this;
    }

    @Override
    public boolean handleException(Throwable ex) {
        return false;
    }
}
