package com.lin1987www.location;

import android.location.Location;
import android.support.v4.app.ExecutorSet;
import android.text.TextUtils;

import com.lin1987www.location.google.geocode.GoogleGeocodeResults;

import java.util.Locale;

import fix.java.util.concurrent.Duty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Administrator on 2015/4/14.
 */
public class GoogleGeocodeGet extends Duty<GoogleGeocodeGet> implements Callback<GoogleGeocodeResults> {
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
        setExecutorService(ExecutorSet.nonBlockExecutor);
        setAsync(true);
    }

    @Override
    public void doTask(GoogleGeocodeGet context, Duty previousDuty) throws Throwable {
        Call<GoogleGeocodeResults> call = null;
        if (mLongitude != null && mLatitude != null) {
            call = ApiHelper.instance().googleGeocodeGet(false, mLocale.toString(), String.format("%1$s,%2$s", mLatitude, mLongitude));
        } else if (!TextUtils.isEmpty(mAddress)) {
            call = ApiHelper.instance().googleGeocodeGetByAddress(false, mLocale.toString(), mAddress);
        }
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<GoogleGeocodeResults> call, Response<GoogleGeocodeResults> response) {
        mGoogleGeocodeResults = response.body();
        done();
    }

    @Override
    public void onFailure(Call<GoogleGeocodeResults> call, Throwable t) {
        fail(t);
    }
}
