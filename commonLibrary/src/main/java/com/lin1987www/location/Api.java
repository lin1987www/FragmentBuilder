package com.lin1987www.location;

import com.lin1987www.location.google.geocode.GoogleGeocodeResults;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Administrator on 2016/5/26.
 */
public interface Api {
    @GET("maps/api/geocode/json")
    Call<GoogleGeocodeResults> googleGeocodeGet(
            @Query("sensor") boolean sensor,
            @Query("language") String language,
            @Query("language") String latlng
    );

    @GET("maps/api/geocode/json")
    Call<GoogleGeocodeResults> googleGeocodeGetByAddress(
            @Query("sensor") boolean sensor,
            @Query("language") String language,
            @Query("address") String address
    );
}
