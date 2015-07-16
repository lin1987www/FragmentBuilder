package android.support.v4.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lin1987www.app.FragmentBuilder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class FragmentActivityFix extends FragmentActivity {
    public static boolean DEBUG = true;
    public final static String TAG = FragmentActivityFix.class.getSimpleName();
    public final static String key_startActivityFromFragmentPath = "key_startActivityFromFragmentPath";
    protected final String ID = String.format("%s", toString());

    public boolean enableDoubleBackPressed = true;
    private String mStartActivityFromFragmentPath = null;
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

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        if (DEBUG) {
            Log.e(TAG, "onCreate " + ID);
        }
        super.onCreate(savedInstanceState);
        ContextHelper.setFragmentActivity(this);
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onCreate(savedInstanceState);
        }
        if (savedInstanceState != null) {
            mStartActivityFromFragmentPath = savedInstanceState.getString(key_startActivityFromFragmentPath);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.e(TAG, "onActivityResult " + ID);
        }
        super.onActivityResult(requestCode, resultCode, data);
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onActivityResult(requestCode, resultCode, data);
        }
        if (null != mStartActivityFromFragmentPath) {
            FragmentBuilder.FragmentPath
                    .findFragment(this, mStartActivityFromFragmentPath)
                    .onActivityResult(requestCode, resultCode, data);
            mStartActivityFromFragmentPath = null;
        }
    }

    @Override
    protected void onRestart() {
        if (DEBUG) {
            Log.e(TAG, "onRestart " + ID);
        }
        super.onRestart();
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onRestart();
        }
    }

    @Override
    protected void onStart() {
        if (DEBUG) {
            Log.e(TAG, "onStart " + ID);
        }
        super.onStart();
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onStart();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.e(TAG, "onRestoreInstanceState " + ID);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.e(TAG, "onResume " + ID);
        }
        super.onResume();
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.e(TAG, "onPause " + ID);
        }
        super.onPause();
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onPause();
        }
    }

    @Override
    protected void onSaveInstanceState(android.os.Bundle outState) {
        if (DEBUG) {
            Log.e(TAG, "onSaveInstanceState " + ID);
        }
        super.onSaveInstanceState(outState);
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onSaveInstanceState(outState);
        }
        if (null != mStartActivityFromFragmentPath) {
            outState.putString(key_startActivityFromFragmentPath, mStartActivityFromFragmentPath);
        }
    }

    @Override
    protected void onStop() {
        if (DEBUG) {
            Log.e(TAG, "onStop " + ID);
        }
        super.onStop();
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.e(TAG, "onDestroy " + ID);
        }
        super.onDestroy();
        for (PuppetActivity puppetActivity : puppetActivities) {
            puppetActivity.onDestroy();
        }
    }


    @Override
    public void startActivityFromFragment(Fragment fragment, Intent intent,
                                          int requestCode) {
        mStartActivityFromFragmentPath = FragmentBuilder.FragmentPath.getFragmentPathString(new FragmentBuilder.Content(fragment));
        super.startActivityForResult(intent, requestCode);
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
        if (FragmentBuilder.hasPopBackStack(this)) {
            return;
        }
        // 都沒有Pop Fragment時會用到，大概是用於SideMenu上
        if (mOnPostBackPressedListener != null &&
                mOnPostBackPressedListener.get() != null &&
                mOnPostBackPressedListener.get().onBackPressed()) {
            return;
        }

        if (enableDoubleBackPressed) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                if (System.currentTimeMillis() > (mLastBackPressedTime + 5 * 1000)) {
                    Toast.makeText(getBaseContext(), "再按返回以退出",
                            Toast.LENGTH_SHORT).show();
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
}
