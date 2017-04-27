package okhttp3;

import com.lin1987www.common.Utility;
import com.lin1987www.http.cookie.CookieKeeper;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Administrator on 2016/5/23.
 */
public class OkHttpHelper {
    private static OkHttpClient.Builder mOkHttpClientBuilder;

    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        if (mOkHttpClientBuilder == null) {
            synchronized (OkHttpHelper.class) {
                if (mOkHttpClientBuilder == null) {
                    mOkHttpClientBuilder = new OkHttpClient.Builder();
                    mOkHttpClientBuilder.cookieJar(CookieKeeper.getInstance().cookieJar);
                    mOkHttpClientBuilder.retryOnConnectionFailure(true);
                    mOkHttpClientBuilder.addInterceptor(new ConnectionResetByPeerInterceptor());
                    if (Utility.DEBUG) {
                        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
                        mOkHttpClientBuilder.addInterceptor(logging);
                    }
                }
            }
        }
        return mOkHttpClientBuilder;
    }

    private static OkHttpClient mOkHttpClient;

    public static OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            synchronized (OkHttpHelper.class) {
                if (mOkHttpClient == null) {
                    mOkHttpClient = getOkHttpClientBuilder().build();
                }
            }
        }
        return mOkHttpClient;
    }
}
