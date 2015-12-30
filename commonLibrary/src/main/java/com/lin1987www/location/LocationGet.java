package com.lin1987www.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ContextHelper;
import android.widget.Toast;

import com.lin1987www.os.HandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import fix.java.util.concurrent.Take;

/**
 * Created by Administrator on 2015/5/8.
 */
public class LocationGet extends Take<LocationGet> {
    private static final String SP_KEY = LocationGet.class.getName() + "_sp_location";
    public static long defaultWaitMilliSec = 3000L;
    public final long mWaitMilliSec;
    private Context mContext;
    private LocationManager mLocationManager;
    private Location mLocation;
    private LocationListenerImpl mLocationListener;

    private boolean isNonProvider = true;

    public boolean isNonProvider() {
        return isNonProvider;
    }

    public Location getLocation() {
        return mLocation;
    }

    private static final String[] providers = {
            LocationManager.PASSIVE_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER
    };

    public LocationGet(Context context) {
        this(context, defaultWaitMilliSec);
    }

    public LocationGet(Context context, long waitMilliSec) {
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        this.mWaitMilliSec = waitMilliSec;
    }

    @Override
    public LocationGet take() throws Throwable {
        //讀取最後已知的位置
        setLocationFromLastKnown();
        if (mLocation == null) {
            //開啟偵測
            setLocationFromProvider();
        }
        if (mLocation == null) {
            // 從SharedPreferences取得
            setLocationFromSharedPreferences();
        } else {
            //記錄於SharedPreferences當中
            saveLocationInSharedPreferences(mLocation);
        }
        return this;
    }

    @Override
    public boolean handleException(Throwable ex) {
        return false;
    }

    @Override
    public void onCompleted() {
        mContext = null;
        mLocationManager = null;
        mLocationListener = null;
    }

    private void setLocationFromLastKnown() {
        // 使用最後知道的位置
        for (int i = 0; i < providers.length; i++) {
            String provider = providers[i];
            Location lastKnownLocation = mLocationManager.getLastKnownLocation(provider);
            if (lastKnownLocation != null) {
                mLocation = lastKnownLocation;
            }
        }
    }

    private void setLocationFromProvider() throws Throwable {
        // 確定可使用的Provider
        List<String> providerList = new ArrayList<String>();
        for (int i = 0; i < providers.length; i++) {
            String provider = providers[i];
            boolean isProviderEnabled = mLocationManager.isProviderEnabled(provider);
            if (isProviderEnabled) {
                providerList.add(provider);
            }
        }
        isNonProvider = (providerList.size() == 0);
        if (!isNonProvider) {
            mLocationListener = new LocationListenerImpl(mLocationManager, providerList);
            mLocation = mLocationListener.waitForResult(mWaitMilliSec);
        }
    }

    private void setLocationFromSharedPreferences() {
        SharedPreferences settings = mContext.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);
        if (settings.getString("provider", null) != null) {
            Location location = new Location(settings.getString("provider", null));
            location.setAccuracy(settings.getFloat("accuracy", 0));
            location.setAltitude(settings.getFloat("altitude", 0));
            location.setBearing(settings.getFloat("bearing", 0));
            // API 17 才支援
            // location.setElapsedRealtimeNanos(settings.getLong("elapsed",
            // 0));
            location.setLatitude(settings.getFloat("latitude", 0));
            location.setLongitude(settings.getFloat("longitude", 0));
            location.setSpeed(settings.getFloat("speed", 0));
            location.setTime(settings.getLong("time", 0));
            mLocation = location;
            HandlerHelper.runMainThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ContextHelper.getApplication(), "Load last known location record.", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
    }

    private void saveLocationInSharedPreferences(Location location) {
        SharedPreferences settings = mContext.getApplicationContext()
                .getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("accuracy", location.getAccuracy());
        editor.putFloat("altitude", (float) location.getAltitude());
        editor.putFloat("bearing", location.getBearing());
        // API 17 才支援
        editor.putLong("elapsed", 0);
        editor.putFloat("latitude", (float) location.getLatitude());
        editor.putFloat("longitude", (float) location.getLongitude());
        editor.putString("provider", location.getProvider());
        editor.putFloat("speed", location.getSpeed());
        editor.putLong("time", location.getTime());
        editor.commit();
    }

    private static class LocationListenerImpl implements Runnable, LocationListener {
        public final AtomicBoolean mIsTimeout = new AtomicBoolean(false);
        public final AtomicBoolean mIsRequestLocation = new AtomicBoolean(false);
        public final LocationManager mLocationManager;
        public final List<String> mProviderList;

        private Location mLocation;

        public LocationListenerImpl(LocationManager locationManager, List<String> providerList) {
            this.mLocationManager = locationManager;
            this.mProviderList = providerList;
        }

        private Runnable removeUpdatesRunnable = new Runnable() {
            @Override
            public void run() {
                if (mIsRequestLocation.getAndSet(false)) {
                    mLocationManager.removeUpdates(LocationListenerImpl.this);
                }
            }
        };

        public Location waitForResult(long waitMilliSec) throws Throwable {
            HandlerHelper.getMainThreadExecutor().submit(this);
            // 等待結果
            synchronized (this) {
                this.wait(waitMilliSec);
                mIsTimeout.set(true);
            }
            removeUpdates();
            return mLocation;
        }

        private void removeUpdates() {
            if (mIsRequestLocation.get()) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    removeUpdatesRunnable.run();
                } else {
                    HandlerHelper.getMainThreadExecutor().submit(removeUpdatesRunnable);
                }
            }
        }

        @Override
        public void run() {
            if (mIsTimeout.get()) {
                return;
            }
            // 註冊可用Provider，必須在MainThread中執行
            for (String provider : mProviderList) {
                mLocationManager.requestLocationUpdates(provider, 0, 0, this);
            }
            mIsRequestLocation.set(true);
        }

        @Override
        public void onLocationChanged(Location location) {
            if (mIsTimeout.get()) {
                return;
            }
            // 由Non Main Thread 觸發，但也可以指定Looper來執行
            if (mLocation == null) {
                synchronized (LocationListenerImpl.this) {
                    if (mLocation == null) {
                        mLocation = location;
                        // 移除所有 Provider
                        if (mIsRequestLocation.getAndSet(false)) {
                            mLocationManager.removeUpdates(this);
                        }
                        LocationListenerImpl.this.notify();
                    }
                }
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    }
}
