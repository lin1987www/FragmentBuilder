package com.lin1987www.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Base64;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.lin1987www.common.Utility;

import im.delight.android.webview.AdvancedWebView;

/**
 * Created by Administrator on 2016/1/11.
 */
public class WebViewFix extends AdvancedWebView {

    public WebViewFix(Context context) {
        super(context);
        init();
    }

    public WebViewFix(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WebViewFix(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        WebSettings settings = getSettings();
        settings.setDefaultTextEncodingName("utf-8");
        settings.setJavaScriptEnabled(false);
        settings.setLoadWithOverviewMode(true);
        setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        setScrollbarFadingEnabled(false);
    }

    public void loadData(String htmlString) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            String base64 = Base64.encodeToString(htmlString.getBytes(), Base64.DEFAULT);
            loadData(base64, "text/html; charset=utf-8", "base64");
        } else {
            String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
            loadData(header + htmlString, "text/html; charset=UTF-8", null);
        }
    }

    public void loadDataWithBaseURL(String htmlString, String historyUrl) {
        String baseUrl = Utility.getBaseUrl(historyUrl);
        loadDataWithBaseURL(baseUrl, htmlString, historyUrl);
    }

    public void loadDataWithBaseURL(String baseUrl, String htmlString, String historyUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            loadDataWithBaseURL(baseUrl, htmlString, "text/html", "utf-8", historyUrl);
        } else {
            String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
            loadDataWithBaseURL(baseUrl, header + htmlString, "text/html; charset=UTF-8", null, historyUrl);
        }
    }

    public void injectJs(String js) {
        loadUrl("javascript:" + js);
    }

    public void injectCss(String css) {
        String encoded = Base64.encodeToString(css.getBytes(), Base64.NO_WRAP);
        injectJs("(function() {" +
                "var parent = document.getElementsByTagName('head').item(0);" +
                "var style = document.createElement('style');" +
                "style.type = 'text/css';" +
                // Tell the browser to BASE64-decode the string into your script !!!
                "style.innerHTML = window.atob('" + encoded + "');" +
                "parent.appendChild(style)" +
                "})();");
    }
}
