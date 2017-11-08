package android.support.v4.app;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;

import com.lin1987www.common.Utility;

/**
 * Created by Administrator on 2015/7/8.
 */
public class FragmentArgs {
    private final static String suffix = FragmentArgs.class.getSimpleName();
    private final static String KEY_fragmentBuilderText = key("KEY_fragmentBuilderText");
    private final static String KEY_isUserVisible = key("KEY_isUserVisible");
    private final static String KEY_consumeReady = key("KEY_consumeReady");
    private final static String KEY_saveViewState = key("KEY_saveViewState");

    private static String key(String name) {
        return String.format("%s_%s", name, suffix);
    }

    //
    public final Bundle bundle;
    //

    public FragmentArgs() {
        bundle = new Bundle();
    }

    public FragmentArgs(Bundle bundle) {
        this.bundle = bundle;
        bundle.setClassLoader(Utility.getClassLoader());
    }

    public String getFragmentBuilderText() {
        return bundle.getString(KEY_fragmentBuilderText);
    }

    void setFragmentBuilderText(String value) {
        bundle.putString(KEY_fragmentBuilderText, value);
    }

    boolean getUserVisible() {
        boolean value = bundle.getBoolean(KEY_isUserVisible, true);
        return value;
    }

    void setUserVisible(boolean value) {
        bundle.putBoolean(KEY_isUserVisible, value);
    }

    boolean isConsumeReady() {
        boolean value = bundle.getBoolean(KEY_consumeReady, false);
        if (bundle.containsKey(KEY_consumeReady)) {
            bundle.remove(KEY_consumeReady);
        }
        return value;
    }

    void consumeReady() {
        bundle.putBoolean(KEY_consumeReady, true);
    }

    public String getSaveStateKey(View view) {
        if (view.getId() == View.NO_ID) {
            throw new RuntimeException(String.format("View must set a id. %s", view));
        }
        return String.format("%s", view.getId());
    }

    public String getSaveStateKey(Fragment fragment) {
        return fragment.getClass().getName();
    }

    public void saveViewState(Fragment fragment) {
        saveViewState(fragment.getView(), getSaveStateKey(fragment));
    }

    public void saveViewState(View view) {
        saveViewState(view, getSaveStateKey(view));
    }

    public void saveViewState(View view, String key) {
        Bundle viewStateBundle = bundle.getBundle(KEY_saveViewState);
        if (viewStateBundle == null) {
            viewStateBundle = new Bundle();
        }
        SparseArray<Parcelable> container = new SparseArray<>();
        view.saveHierarchyState(container);
        viewStateBundle.putSparseParcelableArray(key, container);
        bundle.putBundle(KEY_saveViewState, viewStateBundle);
    }

    public void restoreViewState(Fragment fragment) {
        restoreViewState(fragment.getView(), getSaveStateKey(fragment));
    }

    public void restoreViewState(View view) {
        restoreViewState(view, getSaveStateKey(view));
    }

    public void restoreViewState(View view, String key) {
        if (bundle.containsKey(KEY_saveViewState)) {
            Bundle viewStateBundle = bundle.getBundle(KEY_saveViewState);
            if (viewStateBundle != null) {
                if (viewStateBundle.containsKey(key)) {
                    SparseArray<Parcelable> container = viewStateBundle.getSparseParcelableArray(key);
                    if (container != null) {
                        view.restoreHierarchyState(container);
                    }
                }
            }
        }
    }

    public void removeViewState(Fragment fragment) {
        removeViewState(getSaveStateKey(fragment));
    }

    public void removeViewState(View view) {
        removeViewState(getSaveStateKey(view));
    }

    public void removeViewState(String key) {
        if (bundle.containsKey(KEY_saveViewState)) {
            Bundle viewStateBundle = bundle.getBundle(KEY_saveViewState);
            if (viewStateBundle != null) {
                if (viewStateBundle.containsKey(key)) {
                    viewStateBundle.remove(key);
                }
            }
        }
    }
}
