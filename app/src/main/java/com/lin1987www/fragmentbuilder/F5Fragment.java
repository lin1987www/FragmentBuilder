package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.FragmentFix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.lin1987www.fragmentbuilder.widget.ShowEnterTextView;


/**
 * Created by Administrator on 2015/6/26.
 */
public class F5Fragment extends FragmentFix {
    public String result;

    TextView mTextView;
    EditText mNumberEditText;
    ShowEnterTextView mShowEnterTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f5, container, false);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mTextView.setText(String.format("%s", getTag()));
        mNumberEditText = (EditText) view.findViewById(R.id.editText);
        mShowEnterTextView = (ShowEnterTextView) view.findViewById(R.id.showNameTextView);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        result = String.format("Name:[%s] Number:[%s]", mShowEnterTextView.getEnterName(), mNumberEditText.getText().toString());
        super.onDestroyView();
    }
}
