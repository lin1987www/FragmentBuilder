package com.lin1987www.fragmentbuilder.widget;

import android.content.Context;
import android.support.v4.app.FragmentBuilder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.lin1987www.fragmentbuilder.F11Fragment;
import com.lin1987www.fragmentbuilder.F12Fragment;
import com.lin1987www.fragmentbuilder.F12NewFragment;
import com.lin1987www.fragmentbuilder.F13Fragment;

/**
 * Created by Administrator on 2015/6/30.
 */
public class WizardStepsTextView extends TextView implements View.OnClickListener {
    public boolean isFinish = false;
    public String result = "";
    private String f11text = "";
    private String f12text = "";
    private String f13text = "";

    public WizardStepsTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        isFinish = false;
        result = null;
        FragmentBuilder
                .create(this)
                .backContainer()
                .replace()
                .setFragment(F11Fragment.class, F11Fragment.class.getSimpleName())
                .addToBackStack(F11Fragment.BACK_STACK_NAME)
                .build();
    }

    private void refresh() {
        setText(result);
    }

    public void onPopFragment(F11Fragment fragment) {
        f11text = fragment.result;
        if (isFinish) {
            result = String.format("WizardStepsTextView Result:\n%s\n%s\n%s", f11text, f12text, f13text);
        }
        refresh();
    }

    public void onPopFragment(F12Fragment fragment) {
        f12text = fragment.result;
    }

    public void onPopFragment(F12NewFragment fragment) {
        f12text = fragment.result;
    }

    public void onPopFragment(F13Fragment fragment) {
        isFinish = fragment.isFinish;
        f13text = fragment.result;
    }
}
