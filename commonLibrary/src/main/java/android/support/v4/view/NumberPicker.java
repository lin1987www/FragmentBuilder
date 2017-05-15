package android.support.v4.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentBuilder;
import android.support.v4.app.NumberPickerFrag;
import android.support.v4.app.NumberPickerFragArgs;
import android.util.AttributeSet;
import android.view.View;

import com.lin1987www.common.R;

/**
 * Created by Administrator on 2017/5/12.
 */

public class NumberPicker extends android.support.v7.widget.AppCompatTextView implements View.OnClickListener, FragmentBuilder.OnPopFragmentListener {
    private static final String KEY_onSaveInstanceState = "KEY_onSaveInstanceState";
    private static final String KEY_min = "KEY_min";
    private static final String KEY_max = "KEY_max";
    private static final String KEY_value = "KEY_value";

    private int mMin;
    private int mMax;
    private int mValue;

    protected OnChangeListener mListener;

    public void setOnChangeListener(OnChangeListener listener) {
        mListener = listener;
    }

    public NumberPicker(Context context) {
        this(context, null);
    }

    public NumberPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOnClickListener(this);
        setFreezesText(true);
    }

    public int min() {
        return mMin;
    }

    public NumberPicker min(int value) {
        mMin = value;
        return this;
    }

    public int max() {
        return mMax;
    }

    public NumberPicker max(int value) {
        mMax = value;
        return this;
    }

    public int value() {
        return mValue;
    }

    public NumberPicker value(int value) {
        if (mValue != value) {
            mValue = value;
            loadData();
            if (mListener != null) {
                mListener.onChangeNumber(this, value);
            }
        }
        return this;
    }

    private void loadData() {
        setText(String.format("%s", value()));
        setEnabled(max() != min());
    }

    public void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        Parcelable saveInstanceState = null;
        saveInstanceState = bundle.getParcelable(KEY_onSaveInstanceState);
        super.onRestoreInstanceState(saveInstanceState);
        int min = bundle.getInt(KEY_min);
        int max = bundle.getInt(KEY_max);
        int value = bundle.getInt(KEY_value);
        min(min).max(max).value(value);
    }

    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_onSaveInstanceState, super.onSaveInstanceState());
        bundle.putInt(KEY_min, mMin);
        bundle.putInt(KEY_max, mMax);
        bundle.putInt(KEY_value, mValue);
        return bundle;
    }

    @Override
    public void onClick(View view) {
        if (view == this) {
            if (max() != min()) {
                NumberPickerFragArgs args = new NumberPickerFragArgs().min(min()).max(max()).value(value());
                FragmentBuilder
                        .create(this)
                        .add()
                        .addToBackStack()
                        .setFragment(NumberPickerFrag.class)
                        .setArgs(args.bundle)
                        .setCustomAnimations(R.anim.zoom_in, R.anim.zoom_out, R.anim.zoom_in, R.anim.zoom_out)
                        .build();
            }
        }
    }

    @Override
    public void onPopFragment(Fragment fragment) {
        if (fragment instanceof NumberPickerFrag) {
            NumberPickerFrag numberPickerFrag = (NumberPickerFrag) fragment;
            if (numberPickerFrag.value != null) {
                int value = numberPickerFrag.value;
                value(value);
            }
        }
    }

    public interface OnChangeListener {
        void onChangeNumber(NumberPicker picker, int number);
    }
}
