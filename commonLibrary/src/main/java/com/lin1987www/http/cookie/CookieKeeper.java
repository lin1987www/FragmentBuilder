package com.lin1987www.http.cookie;

import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java6.net.CookieManager;
import java6.net.CookiePolicy;
import java6.net.HttpCookie;
import java6.net.InMemoryCookieStore;
import okhttp3.CookieJar;
import okhttp3.JavaNetCookieJar;

/**
 * Created by Administrator on 2016/3/23.
 */
public class CookieKeeper {
    public static boolean DEBUG = true;
    private static final String TAG = CookieHandlerFactory.class.getName();

    public Dao<Cookie, String> cookieDao;

    public final InMemoryCookieStore store = new InMemoryCookieStore() {
        @Override
        public void add(final URI uri, final HttpCookie cookie) {
            super.add(uri, cookie);
            boolean isDeleted = "deleted".endsWith(cookie.getValue());
            if (!isDeleted && cookie.hasExpired()) {
                Log.w(TAG, String.format("加入的Cookie已過期[%s][%s][%s][%s]",
                        cookie.getName(), cookie.getValue(),
                        cookie.getDomain(), cookie.getPath()));
                return;
            }
            if (DEBUG) {
                Log.d(TAG, String.format("\n%1$s\nAdd cookie %2$s\n", uri,
                        cookie));
            }
            // 儲存 Cookie
            Cookie c = new Cookie(cookie);
            c.debugURL = uri.toString();
            try {
                if (cookieDao == null) {
                    throw new NullPointerException(String.format("%s %s",
                            TAG, "cookieDao is null!"));
                }
                synchronized (this) {
                    if (isDeleted) {
                        cookieDao.delete(c);
                    } else {
                        cookieDao.createOrUpdate(c);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }
        }
    };

    public final CookieManager manager = new CookieManager(store, CookiePolicy.ACCEPT_ALL) {
        @Override
        public Map<String, List<String>> get(URI uri,
                                             Map<String, List<String>> requestHeaders)
                throws IOException {
            // make sure our args are valid
            if ((uri == null) || (requestHeaders == null)) {
                throw new IllegalArgumentException("Argument is null");
            }
            // save our url once
            String url = uri.toString();

            // prepare our response
            requestHeaders = super.get(uri, requestHeaders);
            if (DEBUG) {
                if (requestHeaders != null) {
                    Iterator<String> keySet = requestHeaders.keySet().iterator();

                    Log.d(TAG, String.format("%1$s", url));
                    while (keySet.hasNext()) {
                        String name = keySet.next();
                        String value = requestHeaders.get(name).toString();
                        Log.d(TAG, String.format("%1$s: %2$s\n", name, value));
                    }
                }
            }
            return requestHeaders;
        }

        @Override
        public void put(URI uri, Map<String, List<String>> responseHeaders)
                throws IOException {
            super.put(uri, responseHeaders);
            // make sure our args are valid
            if ((uri == null) || (responseHeaders == null)) {
                return;
            }
            // save our url once
            String url = uri.toString();
            if (DEBUG) {
                // go over the headers
                for (String headerKey : responseHeaders.keySet()) {
                    // ignore headers which aren't cookie related
                    if ((headerKey == null)
                            || !(headerKey.equalsIgnoreCase("Set-Cookie2") || headerKey
                            .equalsIgnoreCase("Set-Cookie"))) {
                        continue;
                    }
                    Log.d(TAG, String.format("%1$s", url));
                    // process each of the headers
                    for (String headerValue : responseHeaders.get(headerKey)) {
                        Log.d(TAG, String
                                .format("%1$s%2$s\n",
                                        (headerKey == null) ? ""
                                                : headerKey + ": ",
                                        headerValue));
                    }
                }
            }
        }
    };

    public final CookieJar cookieJar;

    public CookieKeeper(Dao<Cookie, String> dao) {
        cookieDao = dao;
        if (cookieDao == null) {
            throw new NullPointerException(String.format("%s %s", TAG, "cookieDao is null!"));
        }
        // 加入原有的 cookie，判斷是否過期，如果過期則不加入
        try {
            if (DEBUG) {
                Log.w(TAG, "Restore CookieStore.");
            }
            for (Cookie cookie : cookieDao.queryForAll()) {
                HttpCookie httpCookie = cookie.getHttpCookie();
                if (httpCookie.hasExpired()) {
                    // 已過期 cookie 保留
                } else {
                    store.add(new URI(cookie.debugURL), httpCookie);
                }
            }
            if (DEBUG) {
                Log.w(TAG, "Restore CookieStore finish.");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        cookieJar = new JavaNetCookieJar(manager);
    }
}
