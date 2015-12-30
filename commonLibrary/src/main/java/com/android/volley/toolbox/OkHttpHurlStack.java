package com.android.volley.toolbox;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestExtra;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

public class OkHttpHurlStack extends HurlStack {
    private static final String TAG = OkHttpHurlStack.class.getName();
    private final OkUrlFactory okUrlFactory;
    private final UrlRewriter mUrlReWriter;
    private final SSLSocketFactory mSslSocketFactory;

    public OkHttpHurlStack() {
        this(null);
    }

    public OkHttpHurlStack(UrlRewriter urlRewriter) {
        this(urlRewriter, null);
    }

    public OkHttpHurlStack(UrlRewriter urlRewriter, SSLSocketFactory sslSocketFactory) {
        super(urlRewriter, sslSocketFactory);
        this.mUrlReWriter = urlRewriter;
        this.mSslSocketFactory = sslSocketFactory;
        this.okUrlFactory = new OkUrlFactory(new OkHttpClient());
    }

    public OkHttpClient getOkHttpClient() {
        return okUrlFactory.client();
    }

    @Override
    protected HttpURLConnection createConnection(URL url) throws IOException {
        return okUrlFactory.open(url);
    }

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {
        HttpResponse response = super.performRequest(request, additionalHeaders);
        // 記錄 Request Header
        if (request instanceof RequestExtra) {
            request.getHeaders().putAll(additionalHeaders);
        }
        return response;
    }
}