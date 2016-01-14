package com.lin1987www.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lin on 2014/9/11.
 */
public class Utility {
    private final static SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy/MM/dd a hh:mm");

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
        result = result.replaceAll("^(<[^>]*>|\\s*)*|<[^>]*>|(<[^>]*>|\\s*)*$", "");
        return result;
    }
}
