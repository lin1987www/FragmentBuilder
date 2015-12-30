package com.lin1987www.http.cookie;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import java6.net.CookieManager;
import java6.net.CookiePolicy;
import java6.net.HttpCookie;
import java6.net.InMemoryCookieStore;

public class CookieHandlerFactory {
	public static boolean DEBUG = true;
	private static final String TAG = CookieHandlerFactory.class.getName();
	private static final Map<Context, CookieHandler> mCookieHandlerMap =new WeakHashMap<Context, CookieHandler>();
	private static final Object mLock = new Object();
	public static boolean enableApplicationContext = false;

	public static CookieHandler openCookieHandler(Context context) {
		// TODO 記得改成
		Context targetContext = context;
		if (enableApplicationContext) {
			targetContext = targetContext.getApplicationContext();
		}
		if (!mCookieHandlerMap.containsKey(targetContext)) {
			synchronized (mLock) {
				if (!mCookieHandlerMap.containsKey(targetContext)) {
					mCookieHandlerMap.put(targetContext,
							generateCookieHandler(targetContext));
				}
			}
		}
		return mCookieHandlerMap.get(targetContext);
	}

	public static void closeCookieHandler(Context context) {
		// TODO 記得改成
		Context targetContext = context;
		if (enableApplicationContext) {
			targetContext = targetContext.getApplicationContext();
		}
		if (mCookieHandlerMap.containsKey(targetContext)) {
			synchronized (mLock) {
				if (mCookieHandlerMap.containsKey(targetContext)) {
					mCookieHandlerMap.remove(targetContext);
				}
			}
		}
	}

	private static CookieHandler generateCookieHandler(Context context) {
		Dao<Cookie, String> dao = null;
		try {
			dao = (new DatabaseHelper(context)).getCookieDao();
		} catch (Throwable e) {
			e.printStackTrace();
			Log.e(TAG, e.toString());
			return null;
		}

		final Dao<Cookie, String> cookieDao = dao;

		InMemoryCookieStore store = new InMemoryCookieStore() {
			@Override
			public void add(final URI uri, final HttpCookie cookie) {
				super.add(uri, cookie);
				if (cookie.hasExpired()) {
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
						cookieDao.createOrUpdate(c);
					}
				} catch (Throwable e) {
					e.printStackTrace();
					Log.e(TAG, e.toString());
				}
			}
		};

		if (cookieDao == null) {
			throw new NullPointerException(String.format("%s %s", TAG,
					"cookieDao is null!"));
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

		CookieManager manager = new CookieManager(store,
				CookiePolicy.ACCEPT_ALL) {
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
						Iterator<String> keySet = requestHeaders.keySet()
								.iterator();

						Log.d(TAG, String.format("%1$s", url));
						while (keySet.hasNext()) {
							String name = keySet.next();
							String value = requestHeaders.get(name).toString();
							Log.d(TAG,
									String.format("%1$s: %2$s\n", name, value));
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
						for (String headerValue : responseHeaders
								.get(headerKey)) {
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
		return manager;
	}

}
