package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentFix;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lin1987www.fragmentbuilder.widget.ShowEnterTextView;

import lin1987www.com.fragmentbuilder.R;

/**
 * Created by Administrator on 2015/6/26.
 */
public class F111Fragment extends FragmentFix {
    private final static String TAG = F111Fragment.class.getSimpleName();
    public String result;

    TextView mTextView;
    ShowEnterTextView mShowEnterTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f111, container, false);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mTextView.setText(String.format("%s", getTag()));
        mShowEnterTextView = (ShowEnterTextView) view.findViewById(R.id.showNameTextView);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, String.format("F111 onResume. %s", isHidden()));
    }

    @Override
    public void onDestroyView() {
        result = String.format("%s", mShowEnterTextView.getEnterName());
        super.onDestroyView();
    }
}
