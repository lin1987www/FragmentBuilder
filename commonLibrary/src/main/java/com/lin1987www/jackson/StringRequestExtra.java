package com.lin1987www.jackson;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestExtra;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

public class StringRequestExtra extends RequestExtra<String> {
    public StringRequestExtra(int method, String url) {
        this(method, url, null, null);
    }

    public StringRequestExtra(int method, String url,
                              Listener<String> listener, ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        beforeParseNetworkResponse(response);
        Response<String> returnValue;
        setResponseText(response);
        returnValue = Response.success(getResponseText(),
                HttpHeaderParser.parseCacheHeaders(response));
        return returnValue;
    }
}
