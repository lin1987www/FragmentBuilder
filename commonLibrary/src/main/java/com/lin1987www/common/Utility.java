package com.lin1987www.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lin on 2014/9/11.
 */
public class Utility {
    public static boolean DEBUG = true;

    private final static SimpleDateFormat mDateFormat;

    static {
        mDateFormat = new SimpleDateFormat("yyyy/MM/dd a hh:mm");
        mDateFormat.setTimeZone(TimeZone.getDefault());
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
}
