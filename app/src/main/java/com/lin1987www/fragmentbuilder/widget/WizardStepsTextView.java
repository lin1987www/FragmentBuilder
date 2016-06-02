package com.lin1987www.fragmentbuilder.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivityFix;
import android.support.v4.app.FragmentBuilder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lin1987www.fragmentbuilder.F11Fragment;
import com.lin1987www.fragmentbuilder.F12Fragment;
import com.lin1987www.fragmentbuilder.F12NewFragment;
import com.lin1987www.fragmentbuilder.F13Fragment;

/**
 * Created by Administrator on 2015/6/30.
 */
public class WizardStepsTextView extends TextView implements View.OnClickListener, View.OnLongClickListener, Runnable, FragmentActivityFix.onActivityResultListener {
    public boolean isFinish = false;
    public String result = "";
    private String f11text = "";
    private String f12text = "";
    private String f13text = "";
    final static int request_code_ask_question = 100;

    public WizardStepsTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
        setOnLongClickListener(this);
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

    @Override
    public boolean onLongClick(View view) {
        Intent intent = new Intent(getContext(), com.lin1987www.fragmentbuilder.AskQuestionActivity.class);
        ((FragmentActivityFix) getContext()).startActivityForResult(this, intent, request_code_ask_question);
        /*
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        ((FragmentActivityFix) getContext()).startActivityForResult(this, cameraIntent, request_code_ask_question);
        */
        return true;
    }

    private void refresh() {
        setText(result);
    }

    @Override
    protected void onAttachedToWindow() {
        delayRun();
        super.onAttachedToWindow();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        delayRun();
        super.onRestoreInstanceState(state);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        delayRun();
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == request_code_ask_question) {
                Toast.makeText(
                        getContext(),
                        String.format("Select %s", data.getStringExtra("select")),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public void onPopFragment(F11Fragment fragment) {
        delayRun();
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

    private boolean isPostRun = false;

    private void delayRun() {
        if (!isPostRun) {
            isPostRun = true;
            post(this);
        }
    }

    @Override
    public void run() {
        isPostRun = false;
        Toast.makeText(getContext(), "WizardStepsTextView run()", Toast.LENGTH_SHORT).show();
    }
}
