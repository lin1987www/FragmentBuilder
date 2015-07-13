package com.lin1987www.fragmentbuilder.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.lin1987www.app.FragmentBuilder;
import com.lin1987www.fragmentbuilder.WizardStepsFragment;

/**
 * Created by Administrator on 2015/7/3.
 */
public class WizardStepsBackButton extends Button implements View.OnClickListener {
    public WizardStepsBackButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Bundle args = new Bundle();
        args.putBoolean("openStep", true);
        FragmentBuilder
                .create(this)
                .back()
                .replace()
                .traceable()
                .setFragment(WizardStepsFragment.class)
                .setArgs(args)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .build();
    }
}
