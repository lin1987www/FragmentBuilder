package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lin1987www.fragmentbuilder.widget.ShowNameTextView;

import lin1987www.com.fragmentbuilder.R;

/**
 * Created by Administrator on 2015/6/26.
 */
public class F12Fragment extends Fragment {
    public String result;
    TextView mTextView;
    ShowNameTextView mShowNameTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f12, container, false);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mTextView.setText(String.format("%s id:[%s]", getTag(), getId()));
        mShowNameTextView = (ShowNameTextView) view.findViewById(R.id.showNameTextView);
        return view;
    }

    @Override
    public void onDestroyView() {
        result = mShowNameTextView.getText().toString();
        super.onDestroyView();
    }
}
