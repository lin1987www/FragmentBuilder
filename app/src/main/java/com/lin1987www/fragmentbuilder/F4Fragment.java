package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import lin1987www.com.fragmentbuilder.R;

/**
 * Created by Administrator on 2015/6/26.
 */
public class F4Fragment extends Fragment {
    public String result;

    TextView mTextView;
    EditText mNumberEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f4, container, false);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mTextView.setText(String.format("%s", getTag()));
        mNumberEditText = (EditText) view.findViewById(R.id.editText);
        return view;
    }

    @Override
    public void onDestroyView() {
        result = String.format("Number:[%s]", mNumberEditText.getText().toString());
        super.onDestroyView();
    }
}
