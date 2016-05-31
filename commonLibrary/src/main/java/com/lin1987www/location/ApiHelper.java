package com.lin1987www.location;

import android.support.v4.app.ExecutorSet;

import retrofit2.Retrofit;
import retrofit2.RetrofitHelper;

/**
 * Created by Administrator on 2016/5/26.
 */
public class ApiHelper {
    private static Retrofit mRetrofit;

    public static Retrofit getRetrofit() {
        if (mRetrofit == null) {
            synchronized (ApiHelper.class) {
                if (mRetrofit == null) {
                    mRetrofit = RetrofitHelper.getRetrofitBuilder()
                            .baseUrl("https://maps.googleapis.com/")
                            .callbackExecutor(ExecutorSet.nonBlockExecutor)
                            .build();
                }
            }
        }
        return mRetrofit;
    }

    private static Api mInstance;

    public static Api instance() {
        if (mInstance == null) {
            synchronized (ApiHelper.class) {
                if (mInstance == null) {
                    mInstance = getRetrofit().create(Api.class);
                }
            }
        }
        return mInstance;
    }
}
