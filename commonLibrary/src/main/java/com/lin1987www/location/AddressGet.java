package com.lin1987www.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ContextHelper;
import android.text.TextUtils;
import android.widget.Toast;

import com.lin1987www.jackson.JacksonHelper;
import com.lin1987www.location.google.geocode.GoogleGeocodeResults;
import com.lin1987www.os.HandlerHelper;

import java.util.Locale;

import fix.java.util.concurrent.Take;

/**
 * Created by Administrator on 2015/5/11.
 */
public class AddressGet extends Take<AddressGet> {
    public static final String SP_ADDRESS = AddressGet.class.getName() + "_address";
    public static final String SP_GoogleGeocodeResults = AddressGet.class.getName() + "_GoogleGeocodeResults";
    public static final String SP_GoogleGeocodeResults_Text = AddressGet.class.getName() + "_GoogleGeocodeResults_Text";

    private Context mContext;
    private Locale mLocale;
    private Location mLocation;
    private Address mAddress;

    public final boolean mUsingLastKnownAddress;

    public Location getLocation() {
        return mLocation;
    }

    public Address getAddress() {
        return mAddress;
    }

    public AddressGet(Context context) {
        this(context, Locale.TAIWAN);
    }

    public AddressGet(Context context, Locale locale) {
        this(context, locale, null, true);
    }

    public AddressGet(Context context, Locale locale, Location location) {
        this(context, locale, location, false);
    }

    public AddressGet(Context context, Locale locale, Location location, boolean usingLastKnownAddress) {
        this.mContext = context;
        this.mLocale = locale;
        this.mLocation = location;
        this.mUsingLastKnownAddress = usingLastKnownAddress;
    }

    @Override
    public AddressGet take() throws Throwable {
        if (mLocation == null) {
            mLocation = new LocationGet(mContext).takeSafe().getLocation();
        }
        GoogleGeocodeResults results = null;
        if (mLocation != null) {
            results = new GoogleGeocodeGet(mLocation).takeSafe().getGoogleGeocodeResults();
            if (results != null) {
                mAddress = results.getAddress(mLocale, mLocation.getLatitude(), mLocation.getLongitude());
            }
        }
        if (mAddress == null) {
            if (mUsingLastKnownAddress) {
                //load last known address
                mAddress = loadAddressFromSharedPreferences();
            }
        } else {
            // save last known address
            Bundle bundle = null;
            if (mAddress.getExtras() == null) {
                bundle = new Bundle();
            } else {
                bundle = mAddress.getExtras();
            }
            bundle.putString(SP_GoogleGeocodeResults_Text, results.getResponseText());
            bundle.putParcelable(SP_GoogleGeocodeResults, results);
            mAddress.setExtras(bundle);
            saveAddressInSharedPreferences(mAddress);
        }
        return this;
    }

    private Address loadAddressFromSharedPreferences() {
        SharedPreferences settings = mContext.getSharedPreferences(SP_ADDRESS,
                Context.MODE_PRIVATE);
        String locale = settings.getString("locale", null);
        if (TextUtils.isEmpty(locale)) {
            return null;
        }
        String postalCode = settings.getString("postalcode", null);
        String country = settings.getString("countryname", null);
        String administrative_area_level_1 = settings.getString(
                "administrative_area_level_1", null);
        String administrative_area_level_2 = settings.getString(
                "administrative_area_level_2", null);
        String locality = settings.getString("locality", null);
        String subLocality = settings.getString("sublocality", null);
        double latitude = settings.getFloat("latitude", 0);
        double longitude = settings.getFloat("longitude", 0);
        String responseText = settings.getString(SP_GoogleGeocodeResults_Text, null);

        Address address;
        address = new Address(new Locale(locale));
        address.setPostalCode(postalCode);
        address.setCountryName(country);
        address.setAdminArea(administrative_area_level_1);
        address.setSubAdminArea(administrative_area_level_2);
        address.setLocality(locality);
        address.setSubLocality(subLocality);
        address.setLatitude(latitude);
        address.setLongitude(longitude);
        Bundle bundle = new Bundle();
        bundle.putString(SP_GoogleGeocodeResults_Text, responseText);
        try {
            GoogleGeocodeResults results = JacksonHelper.Parse(responseText);
            bundle.putParcelable(SP_GoogleGeocodeResults, results);
        } catch (Throwable throwable) {
        }
        address.setExtras(bundle);
        HandlerHelper.runMainThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ContextHelper.getApplication(), "Load last known address record.", Toast.LENGTH_SHORT)
                        .show();
            }
        });
        return address;
    }

    private void saveAddressInSharedPreferences(Address address) {
        SharedPreferences settings = mContext.getApplicationContext()
                .getSharedPreferences(SP_ADDRESS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("locale", address.getLocale().toString());
        editor.putString("postalcode", address.getPostalCode());
        editor.putString("countryname", address.getCountryName());
        editor.putString("administrative_area_level_1", address.getAdminArea());
        editor.putString("administrative_area_level_2", address.getSubAdminArea());
        editor.putString("locality", address.getLocality());
        editor.putString("sublocality", address.getSubLocality());
        editor.putFloat("latitude", (float) address.getLatitude());
        editor.putFloat("longitude", (float) address.getLongitude());
        Bundle bundle = address.getExtras();
        String responseText = bundle.getString(SP_GoogleGeocodeResults_Text);
        editor.putString(SP_GoogleGeocodeResults_Text, responseText);
        editor.commit();
    }

    public static String getStaticMapUrl(Locale pLocale, Location pLocation,
                                         int zoom, int width, int heigh, int scale) {
        return getStaticMapUrl(pLocale, pLocation.getLatitude(),
                pLocation.getLongitude(), zoom, width, heigh, scale);
    }

    public static String getStaticMapUrl(Locale pLocale, double pLatitude,
                                         double pLongitude, int zoom, int width, int heigh, int scale) {
        // google geocoding 文件
        // https://developers.google.com/maps/documentation/geocoding/?hl=zh-tw
        // 靜態地圖
        // https://developers.google.com/maps/documentation/staticmaps/?hl=zh-tw
        String url = null;
        url = String
                .format("https://maps.googleapis.com/maps/api/staticmap?center=%1$s,%2$s&zoom=%3$s&size=%4$sx%5$s&language=%6$s&sensor=false&markers=color:red|color:red|label:|%1$s,%2$s&scale=%7$s",
                        pLatitude, pLongitude, zoom, width, heigh,
                        pLocale.toString(), scale);
        return url;
    }

    @Override
    public boolean handleException(Throwable ex) {
        return false;
    }

    @Override
    public void onCompleted() {
        mContext = null;
    }
}
