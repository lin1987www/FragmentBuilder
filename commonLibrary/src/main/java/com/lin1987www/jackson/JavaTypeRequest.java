package com.lin1987www.jackson;

import android.text.TextUtils;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.RequestExtra;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.fasterxml.jackson.databind.JavaType;

public class JavaTypeRequest<T> extends RequestExtra<T> {
    // Handler 依附在目前的 Thread所提供的 Looper 當中，Handler可處理的Message 跟 Runnable恐怕都會
    // implicit reference 到 Activity 或者 Fragment，造成當 Context被釋放時，因為有參考到早成 Memory
    // leak。
    // 為了避免此情形，可以使用 Event Bus 的方式來傳遞，以處理完的結果。
    private final JavaType mJavaType;

    public JavaTypeRequest(int method, String url, JavaType javaType) {
        this(method, url, null, null, javaType);
    }

    public JavaTypeRequest(int method, String url, Listener<T> listener,
                           ErrorListener errorListener, JavaType javaType) {
        super(method, url, listener, errorListener);
        mJavaType = javaType;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        beforeParseNetworkResponse(response);

        Response<T> returnValue;
        setResponseText(response);
        String body = getResponseText();

        if (TextUtils.isEmpty(body)) {
            returnValue = Response.error(new VolleyError(
                    new NullPointerException("Response Text empty.回應內文空白!")));

            return returnValue;
        }
        T result;
        try {
            result = JacksonHelper.Parse(body, mJavaType);
        } catch (Throwable e) {
            returnValue = Response.error(new ParseError(e));
            return returnValue;
        }
        returnValue = Response.success(result,
                HttpHeaderParser.parseCacheHeaders(response));

        return returnValue;
    }
}
