package com.lin1987www.fragmentbuilder.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.lin1987www.app.FragmentBuilder;
import com.lin1987www.fragmentbuilder.F11Fragment;
import com.lin1987www.fragmentbuilder.F12Fragment;

/**
 * Created by Administrator on 2015/6/30.
 */
public class WizardStepsTextView extends TextView implements View.OnClickListener {
    public WizardStepsTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        FragmentBuilder
                .create(this)
                .replace()
                .setFragment(F11Fragment.class, F11Fragment.class.getSimpleName())
                .addToBackStack("Wizard Steps Test")
                .build();
    }

    public String result;

    private String f11text = "";
    private String f12text = "";

    private void refresh() {
        setText(String.format("%s\n%s", f11text, f12text));
    }

    public void onPopFragment(F11Fragment fragment) {
        f11text = fragment.result;
        refresh();
    }

    public void onPopFragment(F12Fragment fragment) {
        f12text = fragment.result;
        refresh();
    }
}
