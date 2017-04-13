package com.lin1987www.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentActivity;
import android.text.Layout;
import android.text.format.DateUtils;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fix.java.util.concurrent.ExceptionHelper;

/**
 * Created by lin on 2014/9/11.
 */
public class Utility {
    public static boolean DEBUG = BuildConfig.DEBUG;

    private final static SimpleDateFormat mDateFormat;

    static {
        mDateFormat = new SimpleDateFormat("yyyy/MM/dd a hh:mm");
        mDateFormat.setTimeZone(TimeZone.getDefault());
    }

    public static boolean hasBeenEllipsized(TextView textView) {
        boolean ellipsize = false;
        Layout layout = textView.getLayout();
        if (layout != null) {
            int lines = layout.getLineCount();
            if (lines > 0) {
                for (int i = 0; i < lines; i++) {
                    if (layout.getEllipsisCount(i) > 0) {
                        ellipsize = true;
                        break;
                    }
                }
            }
        }
        return ellipsize;
    }

    public static boolean hasBeenCutOff(TextView textView) {
        boolean hasBeenCutOff = false;
        int height = textView.getHeight();
        int scrollY = textView.getScrollY();
        Layout layout = textView.getLayout();
        if (layout != null) {
            int firstVisibleLineNumber = layout.getLineForVertical(scrollY);
            int lastVisibleLineNumber = layout.getLineForVertical(height + scrollY);
            //check is latest line fully visible
            if (textView.getHeight() < layout.getLineBottom(lastVisibleLineNumber)) {
                hasBeenCutOff = true;
            }
        }
        return hasBeenCutOff;
    }

    public static void detectIsShowAllText(TextView textView, TextViewShowAllTextListener listener) {
        DetectShowAllText detectShowAllText = new DetectShowAllText(textView, listener);
    }


    public static class DetectShowAllText implements ViewTreeObserver.OnGlobalLayoutListener {
        private TextView mTextView;
        private TextViewShowAllTextListener listener;

        public DetectShowAllText(TextView textView, TextViewShowAllTextListener listener) {
            mTextView = textView;
            this.listener = listener;
            if (mTextView != null) {
                mTextView.getViewTreeObserver().addOnGlobalLayoutListener(this);
            }
        }

        @Override
        public void onGlobalLayout() {
            boolean isShowAllText = !hasBeenCutOff(mTextView) && !hasBeenEllipsized(mTextView);
            listener.onLayoutIsShowAllText(mTextView, isShowAllText);
        }
    }

    public interface TextViewShowAllTextListener {
        void onLayoutIsShowAllText(TextView textView, boolean isShowAllText);
    }

    public static String getDateTimeAgoString(Long unixTime) {
        return getDateTimeAgoStringByTime(unixTime, new Date().getTime());
    }

    public static String getDateTimeAgoString(Long unixTime, Long millisTime) {
        return getDateTimeAgoStringByTime(unixTime, millisTime * 1000L);
    }

    public static String getDateTimeAgoStringByTime(Long unixTime, Long millisTime) {
        // Unix time 所以乘 1000
        String dateString = DateUtils.getRelativeTimeSpanString(
                unixTime * 1000, millisTime, DateUtils.SECOND_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL).toString();
        return dateString;
    }

    public static String getDateTimeString(Long unixTime) {
        return getDateTimeString(unixTime, mDateFormat);
    }

    public static String getDateTimeString(Long unixTime, SimpleDateFormat dateFormat) {
        Date date = new Date(unixTime * 1000);

        String dateString = dateFormat.format(date);
        return dateString;
    }

    public static String getVersionString(FragmentActivity activity) {
        String version = "unknown";
        try {
            Context context = activity.getApplication();
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo pInfo = packageManager.getPackageInfo(packageName, 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public static String removeHtmlAndTrim(String value) {
        String result = value;
        result = result.replaceAll("(?><[^>]*>|\\s*)*", "");
        return result;
    }

    public static String getBaseUrl(String value) {
        Pattern pattern = Pattern.compile("(?>[^\\/]+\\/{1,2})*");
        String result = value;
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            result = matcher.group();
        }
        return result;
    }

    private final static String headTag = "<head>";

    public static String insertCss(String html, String css) {
        String style = String.format("<style type=\"text/css\">%s</style>", css);
        StringBuffer htmlBuffer = new StringBuffer();
        htmlBuffer.append(html);
        int headIndex = htmlBuffer.indexOf(headTag);
        htmlBuffer.insert(headIndex + headTag.length(), style);
        String result = htmlBuffer.toString();
        return result;
    }

    public static ClassLoader getClassLoader() {
        // 解決 BadParcelableException 問題
        return BuildConfig.class.getClassLoader();
    }

    public static LinkedHashMap<String, String> splitQuery(String urlString) {
        final LinkedHashMap<String, String> query_pairs = new LinkedHashMap<>();
        try {
            String urlQueryString;
            int queryStartIndex = urlString.indexOf("?");
            if (queryStartIndex >= 0) {
                urlQueryString = urlString.split("\\?")[1];
            } else {
                urlQueryString = urlString;
            }
            final String[] pairs = urlQueryString.split("&");
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                if (query_pairs.containsKey(key)) {
                    throw new RuntimeException(String.format("Duplicate key: %s", key));
                }
                final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
                query_pairs.put(key, value);
            }
        } catch (Throwable e) {
            ExceptionHelper.throwRuntimeException(e);
        }
        return query_pairs;
    }

    public static LinkedHashMap<String, List<String>> splitQueryToList(URL url) throws UnsupportedEncodingException {
        final LinkedHashMap<String, List<String>> query_pairs = new LinkedHashMap<>();
        final String[] pairs = url.getQuery().split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.get(key).add(value);
        }
        return query_pairs;
    }

    public static void removeDuplicate(ArrayList<?> arrayList) {
        int size = arrayList.size();
        for (int i = size - 1; i > -1; i--) {
            Object obj = arrayList.get(i);
            if (i != arrayList.indexOf(obj)) {
                arrayList.remove(i);
            }
        }
    }

    public static Field findUnderlying(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        do {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
            }
        } while ((current = current.getSuperclass()) != null);
        return null;
    }

    public static void setFieldValue(Object obj, String fieldName, Object value) {
        Field field = Utility.findUnderlying(obj.getClass(), fieldName);
        setFieldValue(obj, field, value);
    }

    public static void setFieldValue(Object obj, String fieldName, Object value, Class<?> objClass) {
        Field field = Utility.findUnderlying(objClass, fieldName);
        setFieldValue(obj, field, value);
    }

    public static void setFieldValue(Object obj, Field field, Object value) {
        try {
            field.setAccessible(true);
            // NOTE: Normally, a field that is final and static may not be modified.
            //
            // Below code is modify field is not final, but didn't work. :(
            // Android:accessFlags(?),  Java:modifiers(X)
            /*
            Field modifiersField = Field.class.getDeclaredField("accessFlags");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            */
            field.set(obj, value);
        } catch (Throwable e) {
            ExceptionHelper.throwRuntimeException(e);
        }
    }

    public static boolean isHtml(String text) {
        Pattern pattern = Pattern.compile("(?><(\\w+)>.*</\\1>)");
        Matcher matcher = pattern.matcher(text);
        boolean isHtml = matcher.find();
        return isHtml;
    }
}
