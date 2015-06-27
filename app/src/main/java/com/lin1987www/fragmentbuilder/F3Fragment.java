package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import lin1987www.com.fragmentbuilder.R;

/**
 * Created by Administrator on 2015/6/26.
 */
public class F3Fragment extends Fragment {
    TextView mTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f3, container, false);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mTextView.setText(String.format("tag: %s id: %s",getTag(),getId()));
        return view;
    }
}
