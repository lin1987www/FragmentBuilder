package com.lin1987www.fragmentbuilder.widget;

import android.content.Context;
import android.support.v4.app.FragContent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentBuilder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.lin1987www.fragmentbuilder.F12Fragment;
import com.lin1987www.fragmentbuilder.F12NewFragment;

/**
 * Created by Administrator on 2015/7/2.
 */
public class F12SwitchButton extends Button implements View.OnClickListener {
    public F12SwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (this == view) {
            FragContent content = new FragContent(view);
            Class<? extends Fragment> fragClass = content.getSrcFragment().getClass();
            if (fragClass.equals(F12Fragment.class)) {
                fragClass = F12NewFragment.class;
            } else {
                fragClass = F12Fragment.class;
            }
            FragmentBuilder
                    .create(this)
                    .reset()
                    .setFragment(fragClass, fragClass.getSimpleName())
                    .build();
        }
    }
}
