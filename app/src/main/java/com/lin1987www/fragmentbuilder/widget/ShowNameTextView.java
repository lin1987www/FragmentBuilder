package com.lin1987www.fragmentbuilder.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.lin1987www.app.FragmentBuilder;
import com.lin1987www.fragmentbuilder.EnterNameFragment;

/**
 * Created by Administrator on 2015/6/26.
 */
public class ShowNameTextView extends TextView implements View.OnClickListener {
    public ShowNameTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (getId() == 0) {
            throw new RuntimeException("Didn't assign a id to ShowNameTextView");
        }
        setOnClickListener(this);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("state", super.onSaveInstanceState());
        bundle.putString("text", getText().toString());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        state = bundle.getParcelable("state");
        super.onRestoreInstanceState(state);
        setText(bundle.getString("text"));
    }

    public void onPopFragment(EnterNameFragment fragment) {
        setText(String.format("Name:%s", fragment.enterName));
    }

    @Override
    public void onClick(View view) {
        FragmentBuilder
                .create(this)
                .setFragment(EnterNameFragment.class, getClass().getName() + " " + getId())
                .replace()
                .traceable()
                .build();
    }
}
