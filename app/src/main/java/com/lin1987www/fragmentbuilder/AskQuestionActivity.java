package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.FragmentActivityFix;
import android.view.View;
import android.widget.Button;

/**
 * Created by Administrator on 2015/7/15.
 */
public class AskQuestionActivity extends FragmentActivityFix implements View.OnClickListener {
    Button mSelect1Button;
    Button mSelect2Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_question);
        mSelect1Button = (Button) findViewById(R.id.button1);
        mSelect2Button = (Button) findViewById(R.id.button2);
        mSelect1Button.setOnClickListener(this);
        mSelect2Button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mSelect1Button)) {
            getIntent().putExtra("select", "1");
        } else {
            getIntent().putExtra("select", "2");
        }
        setResult(RESULT_OK, getIntent());
        finish();
    }
}
