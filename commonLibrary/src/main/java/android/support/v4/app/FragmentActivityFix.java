package android.support.v4.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.CallSuper;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lin1987www.common.Utility;
import com.lin1987www.jackson.JacksonHelper;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import fix.java.util.concurrent.ExceptionHelper;

public class FragmentActivityFix extends FragmentActivity {
    public static boolean DEBUG = Utility.DEBUG;
    public final static String TAG = FragmentActivityFix.class.getSimpleName();
    public final static String KEY_startActivityFragContentPath = "KEY_startActivityFragContentPath";
    protected final String KEY_savedInstanceState = "KEY_savedInstanceState_" + getClass().getSimpleName();
    protected final String FORMAT = String.format("%s %s", toString(), "%s");

    public boolean enableDoubleBackPressed = true;
    private String mStartActivityFromFragContentPath = null;
    private long mLastBackPressedTime = 0L;
    private ArrayList<WeakReference<OnBackPressedListener>> mOnPreBackPressedListenerList = new ArrayList<>();
    private WeakReference<OnBackPressedListener> mOnPostBackPressedListener;
    private ArrayList<PuppetActivity> puppetActivities = new ArrayList<>();

    public void addOnPreBackPressedListener(OnBackPressedListener onBackPressedListener) {
        WeakReference<OnBackPressedListener> wfOnBackPressedListener = new WeakReference<>(onBackPressedListener);
        mOnPreBackPressedListenerList.add(wfOnBackPressedListener);
    }

    public void setOnPostBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.mOnPostBackPressedListener = new WeakReference<>(onBackPressedListener);
    }

    public void addPuppetActivityPreCreated(PuppetActivity puppetActivity) {
        puppetActivities.add(puppetActivity);
    }

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = restoreFromPreferences();
        if (savedInstanceState == null) {
            savedInstanceState = bundle;
        } else {
            savedInstanceState.setClassLoader(Utility.getClassLoader());
            if (bundle != null) {
                savedInstanceState.putAll(bundle);
            }
        }
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onCreate"));
        }
        super.onCreate(savedInstanceState);
        ContextHelper.setFragmentActivity(this);
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onCreate(savedInstanceState);
        }
        ExecCommit.wrap(getSupportFragmentManager());
    }

    @CallSuper
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onActivityResult"));
        }
        super.onActivityResult(requestCode, resultCode, data);
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onActivityResult(requestCode, resultCode, data);
        }
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, String.format("mStartActivityFromFragContentPath %s", mStartActivityFromFragContentPath)));
        }
        if (null != mStartActivityFromFragContentPath) {
            FragContentPath path = JacksonHelper.Parse(mStartActivityFromFragContentPath, JacksonHelper.GenericType(FragContentPath.class));
            Object object = FragContentPath.findObject(this, path);
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("mStartActivityFromFragContentPath %s %s", mStartActivityFromFragContentPath, object)));
            }
            if (object instanceof Fragment) {
                ((Fragment) object).onActivityResult(requestCode, resultCode, data);
            } else if (object instanceof OnActivityResultListener) {
                ((OnActivityResultListener) object).onActivityResult(requestCode, resultCode, data);
            } else {
                Class<?> targetClass = object.getClass();
                try {
                    Method method = targetClass.getDeclaredMethod("onActivityResult", int.class, int.class, Intent.class);
                    if (method != null) {
                        method.invoke(object, requestCode, resultCode, data);
                        return;
                    }
                } catch (Throwable ex) {
                    System.err.println(String.format("Miss onActivityResult() on %s throwable:\n%s", object, ExceptionHelper.getPrintStackTraceString(ex)));
                }
            }
            mStartActivityFromFragContentPath = null;
        }
    }

    public interface OnActivityResultListener {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    @Override
    protected void onRestart() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onRestart"));
        }
        super.onRestart();
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onRestart();
        }
    }

    @Override
    protected void onStart() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onStart"));
        }
        super.onStart();
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onStart();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onRestoreInstanceState"));
        }
        if (null != savedInstanceState) {
            if (savedInstanceState.containsKey(KEY_startActivityFragContentPath)) {
                mStartActivityFromFragContentPath = savedInstanceState.getString(KEY_startActivityFragContentPath);
                if (DEBUG) {
                    Log.d(TAG, String.format(FORMAT, String.format("onRestoreInstanceState mStartActivityFromFragContentPath %s", mStartActivityFromFragContentPath)));
                }
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onResume"));
        }
        super.onResume();
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onPause"));
        }
        super.onPause();
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onPause();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onSaveInstanceState"));
        }
        super.onSaveInstanceState(outState);
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onSaveInstanceState(outState);
        }
        if (null != mStartActivityFromFragContentPath) {
            outState.putString(KEY_startActivityFragContentPath, mStartActivityFromFragContentPath);
            if (DEBUG) {
                Log.d(TAG, String.format(FORMAT, String.format("onSaveInstanceState mStartActivityFromFragContentPath %s", mStartActivityFromFragContentPath)));
            }
        }
        saveToPreferences(outState);
        outState.clear();
    }

    @Override
    protected void onStop() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onStop"));
        }
        super.onStop();
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "onDestroy"));
        }
        super.onDestroy();
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        if (DEBUG) {
            Log.e(TAG, String.format(FORMAT, "onLowMemory"));
        }
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        if (DEBUG) {
            Log.e(TAG, String.format(FORMAT, String.format("onTrimMemory level %s", level)));
        }
        super.onTrimMemory(level);
    }

    @Override
    public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode) {
        startActivityForResult(fragment, intent, requestCode);
    }

    public void startActivityForResult(Object object, Intent intent, int requestCode) {
        if (DEBUG) {
            Log.d(TAG, String.format(FORMAT, "startActivityForResult"));
        }
        FragContent content = FragContent.create(object);
        if (content != null) {
            FragContentPath path = content.getFragContentPath();
            mStartActivityFromFragContentPath = JacksonHelper.toJson(path);
            super.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * 直接在Layout上使用 android:onClick="onBackPressed"
     */
    public void onBackPressed(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        // 隱藏鍵盤
        ContextHelper.hideKeyboard(this);

        if (mOnPreBackPressedListenerList.size() > 0) {
            for (int i = mOnPreBackPressedListenerList.size() - 1; i > -1; i--) {
                WeakReference<OnBackPressedListener> wfOnBackPressedListener = mOnPreBackPressedListenerList.get(i);
                OnBackPressedListener listener = wfOnBackPressedListener.get();
                if (listener != null && listener.onBackPressed()) {
                    return;
                } else if (listener == null) {
                    // 2015.04.14 修改如果找不到就移除
                    mOnPreBackPressedListenerList.remove(i);
                }
            }
        }
        //
        FragmentBuilder.FragCarrier carrier = FragmentBuilder.popBackStackRecord(this);
        if (carrier.getWillPopRecord() != null) {
            carrier.popBackStack();
            return;
        }
        // 都沒有Pop Fragment時會用到，大概是用於SideMenu上
        if (mOnPostBackPressedListener != null &&
                mOnPostBackPressedListener.get() != null &&
                mOnPostBackPressedListener.get().onBackPressed()) {
            return;
        }

        if (isTaskRoot() && enableDoubleBackPressed) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                if (System.currentTimeMillis() > (mLastBackPressedTime + 5 * 1000)) {
                    Toast.makeText(getBaseContext(), "再按返回以退出", Toast.LENGTH_SHORT).show();
                    mLastBackPressedTime = System.currentTimeMillis();
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    public interface OnBackPressedListener {
        // 如果回傳true代表已處理
        boolean onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result;
        result = super.onKeyDown(keyCode, event);
        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                ((keyCode == KeyEvent.KEYCODE_ENTER) || (keyCode == KeyEvent.KEYCODE_ESCAPE || (keyCode == KeyEvent.KEYCODE_BACK)))) {
            boolean needHideKeyboard = true;
            if (getCurrentFocus() instanceof EditText) {
                EditText editText = (EditText) getCurrentFocus();
                int inputType = editText.getInputType();
                boolean isMultiLine = (inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) == InputType.TYPE_TEXT_FLAG_MULTI_LINE || (inputType & InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE) == InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE;
                needHideKeyboard = !isMultiLine;
            }
            if (needHideKeyboard) {
                ContextHelper.hideKeyboard(this);
            }
        }
        return result;
    }

    private void saveToPreferences(Bundle in) {
        String serialized = serializeBundle(in);
        if (serialized != null) {
            SharedPreferences settings = getSharedPreferences(KEY_savedInstanceState, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(KEY_savedInstanceState, serialized);
            editor.commit();
        }
    }

    private Bundle restoreFromPreferences() {
        Bundle bundle = null;
        SharedPreferences settings = getSharedPreferences(KEY_savedInstanceState, MODE_PRIVATE);
        String serialized = settings.getString(KEY_savedInstanceState, null);
        if (serialized != null) {
            bundle = deserializeBundle(serialized);
        }
        return bundle;
    }

    public static String serializeBundle(final Bundle bundle) {
        String base64 = null;
        final Parcel parcel = Parcel.obtain();
        try {
            parcel.writeBundle(bundle);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(bos));
            zos.write(parcel.marshall());
            zos.close();
            base64 = Base64.encodeToString(bos.toByteArray(), 0);
        } catch (IOException e) {
            e.printStackTrace();
            base64 = null;
        } finally {
            parcel.recycle();
        }
        return base64;
    }

    public static Bundle deserializeBundle(final String base64) {
        Bundle bundle = null;
        final Parcel parcel = Parcel.obtain();
        try {
            final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            final GZIPInputStream zis = new GZIPInputStream(new ByteArrayInputStream(Base64.decode(base64, 0)));
            int len = 0;
            while ((len = zis.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            zis.close();
            parcel.unmarshall(byteBuffer.toByteArray(), 0, byteBuffer.size());
            parcel.setDataPosition(0);
            bundle = parcel.readBundle(Utility.getClassLoader());
        } catch (IOException e) {
            e.printStackTrace();
            bundle = null;
        } finally {
            parcel.recycle();
        }
        return bundle;
    }
}
