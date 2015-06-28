package com.lin1987www.fragmentbuilder.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.lin1987www.app.FragmentTransactionBuilder2;
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

    public void onPopFragment(EnterNameFragment fragment) {
        setText(String.format("Name: %s", fragment.enterName));
    }

    @Override
    public void onClick(View view) {
        FragmentTransactionBuilder2
                .create(this)
                .setFragment(EnterNameFragment.class, getClass().getName() + " " + getId())
                .replace()
                .traceable()
                .build();
    }
}
