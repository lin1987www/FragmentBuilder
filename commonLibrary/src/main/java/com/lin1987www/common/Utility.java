package com.lin1987www.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentActivity;
import android.text.Layout;
import android.text.format.DateUtils;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
