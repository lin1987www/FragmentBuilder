package okhttp3;

import com.lin1987www.http.cookie.CookieKeeper;

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
