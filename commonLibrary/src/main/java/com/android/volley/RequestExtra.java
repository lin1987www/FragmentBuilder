package com.android.volley;

import android.text.TextUtils;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.lin1987www.common.util.concurrent.Result;
import com.lin1987www.jackson.HttpRequestRecord;
import com.lin1987www.jackson.JacksonHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.boye.httpclientandroidlib.Consts;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.entity.ContentType;
import ch.boye.httpclientandroidlib.entity.mime.HttpMultipartMode;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntityBuilder;

/**
 * 可以使用 HttpBin 做測試 ， OkHttp 會自動使用 gzip，因此Header當中會找不到相關資料。
 * http://httpbin.org/
 *
 * @param <T>
 */
public abstract class RequestExtra<T> extends Request<T> {
    public final static String TAG = RequestExtra.class.getName();
    private static final String BOUNDARY = "00content0boundary00";
    // ex: multipart/form-data; boundary=00content0boundary00; charset=UTF-8
    // P.S. 使用  [; ] 做分隔  分號+空白
    // PHP 5.2.17 不能在 ContentType 使用 charset=  這是BUG!!
    // 正確用法: multipart/form-data; boundary=00content0boundary00
    // https://bugs.php.net/bug.php?id=55504
    // https://bugs.php.net/bug.php?id=60898
    private static final String CONTENT_TYPE_MULTIPART_FORMAT = "multipart/form-data; boundary=%s";
    // http://tekeye.biz/2013/android-debug-vs-release-build
    // 可以根據 BuildConfig.DEBUG 判斷是否要紀錄
    public static boolean enableRecord = true;
    // #endregion
    private static RetryPolicy DEFAULT_RETRY_POLICY = new DefaultRetryPolicy(
            (int) TimeUnit.SECONDS.toMillis(30),
            2,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    private final Listener<T> mListener;
    private Result<T> mResult = null;
    private String mResponseText = null;
    private Long mMinMaxAge = 0L;
    private Map<String, String> mQueryMap = new HashMap<String, String>();
    private Map<String, String> mParamsMap = new HashMap<String, String>();
    private Map<String, String> mRequestHeaders = new HashMap<String, String>();
    private Map<String, String> mResponseHeaders = new HashMap<String, String>();

    private boolean mHasMultipartEntity = true;
    private MultipartEntityBuilder mEntity = null;
    private String mDefaultAcceptCharset = HTTP.UTF_8;
    private boolean mShouldRemoveCache = false;

    public RequestExtra(int method, String url) {
        this(method, url, null, null);
    }

    public RequestExtra(int method, String url, Listener<T> listener,
                        ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        setRetryPolicy(DEFAULT_RETRY_POLICY);
        setDefaultAcceptCharset(HTTP.UTF_8);
    }

    public static void setDefaultRetryPolicy(RetryPolicy defaultRetryPolicy) {
        DEFAULT_RETRY_POLICY = defaultRetryPolicy;
    }

    public RequestExtra<T> setShouldRemoveCache(boolean shouldRemoveCache) {
        mShouldRemoveCache = shouldRemoveCache;
        return this;
    }

    public boolean shouldRemoveCache() {
        return mShouldRemoveCache;
    }

    private static String appendQuery(String url, String query) {
        String prefix;
        if (Pattern.compile("\\?.*$").matcher(url).find()) {
            // 帶有其他參數
            prefix = "&";
        } else {
            // url沒有帶其他query
            prefix = "?";
        }
        String result;
        result = String.format("%s%s%s", url, prefix, query);
        return result;
    }

    public static NetworkResponse putDefaultContextType(NetworkResponse response) {
        // 應該用不到了 ! 因為之前是 HeaderName 因為大小寫沒有Match 到的關係
        // Using UTF-8 for Content-Type
        String contentType = response.headers.get(HTTP.CONTENT_TYPE);
        if (contentType != null) {
            Pattern p = Pattern.compile(";\\s*charset=");
            Matcher m = p.matcher(contentType);
            if (0 == m.groupCount()) {
                contentType = contentType + "; charset=" + Consts.UTF_8.name();
                response.headers.put(HTTP.CONTENT_TYPE, contentType);
            }
        }
        return response;
    }

    private static HashMap<String, String> mHeaderNameMap = new HashMap<String, String>();

    public static void putHeaderNameMap(String uppercaseHeaderName) {
        mHeaderNameMap.put(uppercaseHeaderName.toLowerCase(), uppercaseHeaderName);
    }

    public static HashMap<String, String> getHeaderNameMap() {
        if (mHeaderNameMap.size() == 0) {
            putHeaderNameMap(HTTP.CONTENT_TYPE);
            putHeaderNameMap("ETag");
            putHeaderNameMap("Last-Modified");
            putHeaderNameMap("Expires");
            putHeaderNameMap("Cache-Control");
            putHeaderNameMap("Date");
        }
        return mHeaderNameMap;
    }

    /**
     * 因為 HttpHeaderParser 的HeaderName會判別大小寫，因此造成錯誤。所以為特定HeaderName做修正。
     *
     * @param response
     * @return
     */
    public static NetworkResponse correctHeaderNameForHttpHeaderParser(NetworkResponse response) {
        // 將 HeaderName 轉成 大寫的的規範，這樣 一來HttpHeaderParser 才能對應的到
        HashMap<String, String> uppercaseMap = new HashMap<String, String>();
        for (String headerName : response.headers.keySet()) {
            if (getHeaderNameMap().containsKey(headerName)) {
                String value = response.headers.get(headerName);
                String uppercaseHeaderName = getHeaderNameMap().get(headerName);
                uppercaseMap.put(uppercaseHeaderName, value);
            }
        }
        if (uppercaseMap.size() > 0) {
            for (String uppercaseHeaderName : uppercaseMap.keySet()) {
                response.headers.remove(uppercaseHeaderName.toLowerCase());
            }
            response.headers.putAll(uppercaseMap);
        }
        return response;
    }

    public static NetworkResponse putMinMaxAge(NetworkResponse response, Long minMaxAge) {
        if (minMaxAge.equals(0L)) {
            return response;
        }
        // Using UTF-8 for Content-Type
        Map<String, String> headers = response.headers;
        String headerValue;
        // second
        long maxAge = 0;
        headerValue = headers.get("Cache-Control");
        if (headerValue != null) {
            String[] tokens = headerValue.split(",");
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i].trim();
                if (token.equals("no-cache") || token.equals("no-store")) {
                    maxAge = 0;
                } else if (token.startsWith("max-age=")) {
                    try {
                        maxAge = Long.parseLong(token.substring(8));
                    } catch (Exception e) {
                        // Do nothing
                    }
                } else if (token.equals("must-revalidate") || token.equals("proxy-revalidate")) {
                    maxAge = 0;
                }
            }
        } else {
            headerValue = "";
        }
        if (minMaxAge <= maxAge) {
            return response;
        }
        String newHeaderValue = headerValue;
        newHeaderValue = newHeaderValue.replace("no-cache", "");
        newHeaderValue = newHeaderValue.replace("no-store", "");
        newHeaderValue = newHeaderValue.replace("must-revalidate", "");
        newHeaderValue = newHeaderValue.replace("proxy-revalidate", "");
        // 移除 HTTP 1.0 Cache 規範
        headers.remove("Pragma");
        headers.remove("Expires");
        // 清除現有maxAge
        newHeaderValue = newHeaderValue.replaceAll("max-age=\\d+", "");
        // 清除連續逗號
        newHeaderValue = newHeaderValue.replaceAll("(,\\s){2,}", ", ");
        // 清除結尾逗號
        newHeaderValue = newHeaderValue.replaceAll("(,\\s*)$", "");
        // 清除開頭逗號
        newHeaderValue = newHeaderValue.replaceAll("^\\s*(,\\s*)", "");
        // 加入標記
        if (!newHeaderValue.contains("max-age-by=lin1987www")) {
            newHeaderValue = newHeaderValue + ", max-age-by=lin1987www";
        }
        // 寫入 max-Age
        newHeaderValue = newHeaderValue + String.format(", max-age=%s", minMaxAge);
        headers.put("Cache-Control", newHeaderValue);
        return response;
    }

    public Result<T> getResultSync() {
        if (mResult == null) {
            synchronized (this) {
                while (mResult == null) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return mResult;
    }

    protected void setResult(Result<T> pResult) {
        synchronized (this) {
            mResult = pResult;
            record(pResult);
            this.notifyAll();
        }
    }

    public String getResponseText() {
        return mResponseText;
    }

    protected void setResponseText(NetworkResponse response) {
        String body;
        try {
            body = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, mDefaultAcceptCharset));
        } catch (UnsupportedEncodingException e) {
            body = new String(response.data);
        }
        mResponseText = body;
    }

    public void setDefaultAcceptCharset(String charset) {
        mDefaultAcceptCharset = charset;
        //mRequestHeaders.put("Accept-Charset", charset);
    }

    public Long getMinMaxAge() {
        return mMinMaxAge;
    }

    // #region support MultipartEntity

    public void setMinMaxAge(Long minMaxAge) {
        mMinMaxAge = minMaxAge;
    }

    protected void beforeParseNetworkResponse(NetworkResponse response) {
        //putDefaultContextType(response);
        correctHeaderNameForHttpHeaderParser(response);
        putMinMaxAge(response, getMinMaxAge());
        mResponseHeaders.putAll(response.headers);
    }

    protected void record(Result<T> result) {
        if (!enableRecord) {
            return;
        }
        String request_headers = JacksonHelper.toJson(mRequestHeaders);
        String response_headers = JacksonHelper.toJson(mResponseHeaders);
        String body = getResponseText();
        if (body == null && result.isNotNull()) {
            body = result.result.toString();
        }
        String method = null;
        switch (this.getMethod()) {
            case Method.DELETE:
                method = "DELETE";
                break;
            case Method.DEPRECATED_GET_OR_POST:
                method = "DEPRECATED_GET_OR_POST";
                break;
            case Method.GET:
                method = "GET";
                break;
            case Method.HEAD:
                method = "HEAD";
                break;
            case Method.OPTIONS:
                method = "OPTIONS";
                break;
            case Method.PATCH:
                method = "PATCH";
                break;
            case Method.POST:
                method = "POST";
                break;
            case Method.PUT:
                method = "PUT";
                break;
            case Method.TRACE:
                method = "TRACE";
                break;
        }
        Long sequence = (long) getSequence();
        String url = this.toString();
        String params = null;
        if (getParams() != null && getParams().size() > 0) {
            params = getParams().toString();
        }
        StringWriter errorWriter;
        String error;
        if (result.isSuccess()) {
            HttpRequestRecord.success(method, url, params, body, sequence,
                    request_headers, response_headers);
        } else {
            errorWriter = new StringWriter();
            result.error.printStackTrace(new PrintWriter(errorWriter));
            error = errorWriter.toString();
            HttpRequestRecord.error(method, url, params, body, error, sequence,
                    request_headers, response_headers);
        }
    }

    @Override
    protected void deliverResponse(final T response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
        Result<T> result = Result.success(response);
        setResult(result);
    }

    @Override
    public void deliverError(VolleyError error) {
        super.deliverError(error);
        Result<T> result = Result.error(error);
        setResult(result);
    }

    @Override
    public String getUrl() {
        String url = super.getUrl();
        Map<String, String> queryMap = getQuery();
        if (queryMap != null && queryMap.size() > 0) {
            List<String> queryList = new ArrayList<String>();
            Iterator<Entry<String, String>> q = queryMap.entrySet().iterator();
            while (q.hasNext()) {
                Entry<String, String> entry = q.next();
                queryList.add(String.format("%s=%s", entry.getKey(),
                        entry.getValue()));
            }
            String query = TextUtils.join("&", queryList);
            url = appendQuery(url, query);
        }
        return url;
    }

    public Map<String, String> getQuery() {
        return mQueryMap;
    }

    public RequestExtra<T> q(String key, Object value) {
        getQuery().put(key, String.valueOf(value));
        return this;
    }

    @Override
    public Map<String, String> getParams() {
        return mParamsMap;
    }

    public RequestExtra<T> p(String key, Object value) {
        getParams().put(key, String.valueOf(value));
        return this;
    }

    public boolean hasMultipartEntity() {
        return mHasMultipartEntity;
    }

    public RequestExtra<T> useMultipartEntity() {
        if (!mHasMultipartEntity) {
            switch (this.getMethod()) {
                case Method.POST:
                case Method.PUT:
                case Method.PATCH:
                case Method.DELETE:
                    break;
                default:
                    throw new RuntimeException("只有POST PUT PATCH DELETE可以上傳檔案!");
            }
            mHasMultipartEntity = true;
        }
        return this;
    }

    private MultipartEntityBuilder getEntity() {
        if (mEntity == null) {
            mEntity = MultipartEntityBuilder.create();
            mEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            // UTF-8 必須跟 HttpMultipartMode.BROWSER_COMPATIBLE 搭配使用
            mEntity.setCharset(Charset.forName(getParamsEncoding()));
            mEntity.setBoundary(BOUNDARY);
            if (!"UTF-8".equals(getParamsEncoding())) {
                throw new RuntimeException("必須使用UTF-8編碼!");
            }
        }
        return mEntity;
    }

    public RequestExtra<T> part(String name, byte[] bytes) {
        useMultipartEntity().getEntity().addBinaryBody(name, bytes);
        return this;
    }

    public RequestExtra<T> part(String name, byte[] bytes,
                                ContentType contentType, String filename) {
        useMultipartEntity().getEntity().addBinaryBody(name, bytes,
                contentType, filename);
        return this;
    }

    public RequestExtra<T> part(String name, File file) {
        useMultipartEntity().getEntity().addBinaryBody(name, file);
        return this;
    }

    public RequestExtra<T> part(String name, File file,
                                ContentType contentType, String filename) {
        useMultipartEntity().getEntity().addBinaryBody(name, file, contentType,
                filename);
        return this;
    }

    public RequestExtra<T> part(String name, InputStream stream) {
        useMultipartEntity().getEntity().addBinaryBody(name, stream);
        return this;
    }

    public RequestExtra<T> part(String name, InputStream stream,
                                ContentType contentType, String filename) {
        useMultipartEntity().getEntity().addBinaryBody(name, stream,
                contentType, filename);
        return this;
    }

    private HttpEntity buildHttpEntity() {
        ContentType contentType = ContentType.create("text/plain",
                Charset.forName(getParamsEncoding()));
        for (String name : getParams().keySet()) {
            getEntity().addTextBody(name, getParams().get(name), contentType);
        }
        return getEntity().build();
    }

    @Override
    public Request<?> setRequestQueue(RequestQueue requestQueue) {
        super.setRequestQueue(requestQueue);
        if (shouldRemoveCache()) {
            requestQueue.getCache().remove(getCacheKey());
            //requestQueue.getCache().invalidate(getCacheKey(), true);
        }
        return this;
    }

    @Override
    public String getBodyContentType() {
        String contentType;
        if (hasMultipartEntity()) {
            // PHP 5.2.17 不能在 ContentType 使用 charset=  這是BUG!!
            //contentType = String.format(CONTENT_TYPE_MULTIPART_FORMAT, BOUNDARY, getParamsEncoding());
            contentType = String.format(CONTENT_TYPE_MULTIPART_FORMAT, BOUNDARY);
        } else {
            contentType = super.getBodyContentType();
        }
        return contentType;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        byte[] b;
        if (hasMultipartEntity()) {
            try {
                ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
                buildHttpEntity().writeTo(baoStream);
                b = baoStream.toByteArray();
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else {
            b = super.getBody();
        }
        return b;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mRequestHeaders;
    }

    public Map<String, String> getResponseHeaders() {
        return mResponseHeaders;
    }

    public RequestExtra<T> userAgentMobile() {
        mRequestHeaders.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.4.4; Nexus 5 Build/KTU84P) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        return this;
    }

    public RequestExtra<T> userAgentDesktop() {
        mRequestHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
        return this;
    }
}
