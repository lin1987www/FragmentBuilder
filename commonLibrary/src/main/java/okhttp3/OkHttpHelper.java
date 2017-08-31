package okhttp3;

import com.lin1987www.common.Utility;
import com.lin1987www.http.cookie.CookieKeeper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Administrator on 2016/5/23.
 */
public class OkHttpHelper {
    private static HttpLoggingInterceptor mHttpLogging;

    public static HttpLoggingInterceptor getHttpLogging() {
        if (mHttpLogging == null) {
            synchronized (OkHttpHelper.class) {
                if (mHttpLogging == null) {
                    mHttpLogging = new HttpLoggingInterceptor();
                    mHttpLogging.setLevel(HttpLoggingInterceptor.Level.BODY);
                }
            }
        }
        return mHttpLogging;
    }

    public static Dns HTTP_DNS = new Dns() {
        @Override
        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            if (hostname == null) throw new UnknownHostException("hostname == null");
            List<InetAddress> addressList = null;
            // 騰訊 DNS 服務
            /*
            if (addressList == null) {
                try {
                    HttpUrl httpUrl = new HttpUrl.Builder().scheme("http")
                            .host("119.29.29.29")
                            .addPathSegment("d")
                            .addQueryParameter("dn", hostname)
                            .build();
                    Request dnsRequest = new Request.Builder().url(httpUrl).get().build();
                    String ipString = getHttpDnsClient().newCall(dnsRequest).execute().body().string();
                    // ipString => XXX.XXX.XXX.XXX
                    if (ipString.matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b")) {
                        addressList = Arrays.asList(InetAddress.getAllByName(ipString));
                    }
                } catch (Throwable e) {
                }
            }
            */
            // Google DNS https://dns.google.com/resolve?name=
            //Default
            if (addressList == null) {
                addressList = Dns.SYSTEM.lookup(hostname);
            }
            return addressList;
        }
    };

    private static ConnectionPool mConnectionPool;

    public static ConnectionPool getConnectionPool() {
        if (mConnectionPool == null) {
            // 使用很短時間的保持 keep-alive 幾乎等於使用 Connection: close
            mConnectionPool = new ConnectionPool(5, 1, TimeUnit.NANOSECONDS);
        }
        return mConnectionPool;
    }

    private static OkHttpClient mHttpDnsClient;

    public static OkHttpClient getHttpDnsClient() {
        if (mHttpDnsClient == null) {
            synchronized (OkHttpHelper.class) {
                if (mHttpDnsClient == null) {
                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    builder.dns(HTTP_DNS);
                    if (Utility.DEBUG) {
                        builder.addInterceptor(getHttpLogging());
                    }
                    mHttpDnsClient = builder.build();
                }
            }
        }
        return mHttpDnsClient;
    }

    private static OkHttpClient.Builder mOkHttpClientBuilder;

    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        if (mOkHttpClientBuilder == null) {
            synchronized (OkHttpHelper.class) {
                if (mOkHttpClientBuilder == null) {
                    mOkHttpClientBuilder = new OkHttpClient.Builder();
                    mOkHttpClientBuilder.connectionPool(getConnectionPool());
                    mOkHttpClientBuilder.cookieJar(CookieKeeper.getInstance().cookieJar);
                    mOkHttpClientBuilder.retryOnConnectionFailure(true);
                    mOkHttpClientBuilder.dns(HTTP_DNS);
                    ConnectionCloseInterceptor cci = new ConnectionCloseInterceptor();
                    mOkHttpClientBuilder.addNetworkInterceptor(cci);
                    mOkHttpClientBuilder.addInterceptor(cci);
                    ConnectionResetByPeerInterceptor cr = new ConnectionResetByPeerInterceptor();
                    mOkHttpClientBuilder.addNetworkInterceptor(cr);
                    mOkHttpClientBuilder.addInterceptor(cr);
                    if (Utility.DEBUG) {
                        mOkHttpClientBuilder.addInterceptor(getHttpLogging());
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
