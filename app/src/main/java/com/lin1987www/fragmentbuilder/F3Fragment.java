package com.lin1987www.fragmentbuilder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentFix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lin1987www.fragmentbuilder.widget.ShowEnterTextView;

/**
 * Created by Administrator on 2015/6/26.
 */
public class F3Fragment extends FragmentFix implements View.OnClickListener {
    TextView mTextView;
    ShowEnterTextView mShowEnterTextView;
    Button mStartActivityResultButton;
    final static int request_code_ask_question = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f3, container, false);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mTextView.setText(String.format("%s", getTag()));
        mShowEnterTextView = (ShowEnterTextView) view.findViewById(R.id.showNameTextView);
        mStartActivityResultButton = (Button) view.findViewById(R.id.startActivityResultButton);
        mStartActivityResultButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == request_code_ask_question) {
                Toast.makeText(
                        getActivity(),
                        String.format("Select %s", data.getStringExtra("select")),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (mStartActivityResultButton == view) {
            Intent intent = new Intent(getActivity(), AskQuestionActivity.class);
            startActivityForResult(intent, request_code_ask_question);
        }
    }
}
