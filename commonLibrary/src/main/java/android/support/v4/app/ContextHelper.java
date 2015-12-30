package android.support.v4.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

public class ContextHelper {
    public static final String TAG = ContextHelper.class.getName();
    private static WeakReference<Application> mApplication = null;
    private static WeakReference<FragmentActivity> mFragmentActivity = null;
    private static WeakReference<Activity> mActivity = null;
    private static LocationManager mLocationManager = null;

    public static Application getApplication() {
        if (mApplication == null) {
            return null;
        }
        return mApplication.get();
    }

    public static void setApplication(Application pContext) {
        if (pContext == null) {
            throw new NullPointerException();
        }
        mApplication = new WeakReference<Application>(pContext);
    }

    public static FragmentActivity getFragmentActivity() {
        if (mFragmentActivity == null) {
            return null;
        }
        return mFragmentActivity.get();
    }

    public static void setFragmentActivity(FragmentActivity activity) {
        if (activity == null) {
            throw new NullPointerException();
        }
        mFragmentActivity = new WeakReference<FragmentActivity>(activity);
    }

    public static Activity getActivity() {
        if (mActivity == null) {
            return null;
        }
        return mActivity.get();
    }

    public static void setActivity(Activity activity) {
        if (activity == null) {
            throw new NullPointerException();
        }
        mActivity = new WeakReference<Activity>(activity);
    }

    public static LocationManager getLocationManager() {
        if (mLocationManager == null) {
            synchronized (ContextHelper.class) {
                if (mLocationManager == null) {
                    mLocationManager = (LocationManager) getApplication()
                            .getSystemService(Context.LOCATION_SERVICE);
                }
            }
        }
        return mLocationManager;
    }

    public static String readRawTextFile(int resId) {
        return readRawTextFile(getApplication(), resId);
    }

    public static String readRawTextFile(Context context, int resId) {
        String text = null;
        InputStream inputStream = context.getResources()
                .openRawResource(resId);
        byte buffer[];
        try {
            buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            inputStream = null;
            text = new String(buffer, "UTF-8");
            // ByteBuffer uniBuf = ByteBuffer.wrap(buffer);
            // CharsetDecoder utf8Decoder =
            // Charset.forName("UTF-8").newDecoder();
            // CharBuffer charBuf = utf8Decoder.decode(uniBuf);
            // text = charBuf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        final List<ActivityManager.RunningServiceInfo> services = activityManager
                .getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(
                    serviceClass.getName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static void hideKeyboard(Activity activity) {
        View currentFocusView = null;
        if (activity == null) {
            return;
        }
        currentFocusView = activity.getCurrentFocus();
        if (currentFocusView == null) {
            currentFocusView = new View(activity);
        }
        // Don't need clearFocus!
        //currentFocusView.clearFocus();
        InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(currentFocusView.getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);
        currentFocusView = null;
    }

    public static void showKeyboard(final Activity activity, final EditText editText) {
        editText.post(new Runnable() {
            @Override
            public void run() {
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            }
        });
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Bitmap scaleImage(String imagePath, long maxWidthHeightSize) {
        File file = new File(imagePath);
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true; // 使用這個屬性, 就只會計算，但不會分配記憶體
            bitmap = BitmapFactory.decodeFile(file.getPath(), o);
            int scale = 1;
            if (o.outHeight > maxWidthHeightSize || o.outWidth > maxWidthHeightSize) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.round(Math.log(maxWidthHeightSize
                                / (double) Math.max(o.outHeight, o.outWidth))
                                / Math.log(0.5))
                );
            }
            // 重新縮放
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            bitmap = BitmapFactory.decodeFile(file.getPath(), o2);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return bitmap;
    }

    public static ByteArrayInputStream bitmapToInputStream(Bitmap bitmap, Bitmap.CompressFormat compressFormat, int quality) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(compressFormat, quality /*ignored for PNG*/, bos);
        byte[] bitmapData = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapData);
        return bs;
    }
}
