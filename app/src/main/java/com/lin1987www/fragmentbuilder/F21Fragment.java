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
public class F21Fragment extends Fragment {
    TextView mTextView;
    ShowNameTextView mShowNameTextView;

    public String enterName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f21, container, false);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mShowNameTextView = (ShowNameTextView) view.findViewById(R.id.showNameTextView);
        mTextView.setText(String.format("tag: %s id: %s", getTag(), getId()));
        return view;
    }

    @Override
    public void onDestroyView() {
        enterName = mShowNameTextView.getText().toString();
        super.onDestroyView();
    }
}
