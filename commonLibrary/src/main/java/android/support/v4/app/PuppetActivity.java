package android.support.v4.app;

import android.content.Intent;
import android.os.Bundle;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2015/4/15.
 */
public abstract class PuppetActivity {
    private WeakReference<FragmentActivity> fragmentActivityWeakReference;

    public FragmentActivity getActivity() {
        if (fragmentActivityWeakReference == null) {
            return null;
        }
        return fragmentActivityWeakReference.get();
    }

    public android.content.Context getApplicationContext() {
        if (fragmentActivityWeakReference == null) {
            return null;
        }
        return fragmentActivityWeakReference.get().getApplicationContext();
    }

    public PuppetActivity setActivity(FragmentActivity activity){
        fragmentActivityWeakReference =new WeakReference<FragmentActivity>(activity);
        return this;
    }

    public abstract void onCreate(Bundle savedInstanceState);

    public abstract void onRestart();

    public abstract void onStart();

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);

    public abstract void onResume();

    public abstract void onPause();

    public abstract void onSaveInstanceState(Bundle outState);

    public abstract void onStop();

    public abstract void onDestroy();
}
