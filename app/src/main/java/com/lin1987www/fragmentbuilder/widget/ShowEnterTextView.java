package com.lin1987www.fragmentbuilder.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentBuilder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.lin1987www.fragmentbuilder.EnterTextFragment;

/**
 * Created by Administrator on 2015/6/26.
 */
public class ShowEnterTextView extends TextView implements View.OnClickListener, FragmentBuilder.OnPopFragmentListener {
    private String enterName = "";

    public ShowEnterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (getId() == 0) {
            throw new RuntimeException("Didn't assign a id to ShowEnterTextView");
        }
        setOnClickListener(this);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("state", super.onSaveInstanceState());
        bundle.putString("text", enterName);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        state = bundle.getParcelable("state");
        super.onRestoreInstanceState(state);
        enterName = bundle.getString("text");
        show();
    }

    @Override
    public void onPopFragment(Fragment fragment) {
        if (fragment instanceof EnterTextFragment) {
            onPopFragment((EnterTextFragment) fragment);
        }
    }

    public void onPopFragment(EnterTextFragment fragment) {
        enterName = fragment.enterName;
        show();
    }

    private void show() {
        setText(String.format("Enter:%s", enterName));
    }

    public String getEnterName() {
        return enterName;
    }

    @Override
    public void onClick(View view) {
        if (this == view) {
            FragmentBuilder
                    .create(this)
                    .setFragment(EnterTextFragment.class, getClass().getName() + " " + getId())
                    .replace()
                    .addToBackStack()
                    .build();
        }
    }
}
