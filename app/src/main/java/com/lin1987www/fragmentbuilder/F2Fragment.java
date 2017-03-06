package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentArgs;
import android.support.v4.app.FragmentBuilder;
import android.support.v4.app.FragmentFix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;


/**
 * Created by Administrator on 2015/6/26.
 */
public class F2Fragment extends FragmentFix implements FragmentBuilder.OnPopFragmentListener, View.OnClickListener {
    TextView mTextView;
    FrameLayout mContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f2, container, false);
        FragmentArgs fragmentArgs = new FragmentArgs(getArguments());
        fragmentArgs.userVisibleHintOnResume();

        mTextView = (TextView) view.findViewById(R.id.textView);
        mTextView.setText(String.format("%s", getTag()));
        mContainer = (FrameLayout) view.findViewById(R.id.container_f2);
        mContainer.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        if (mContainer == view) {
            FragmentBuilder
                    .create(F2Fragment.this)
                    .setFragment(F21Fragment.class, F21Fragment.class.getSimpleName())
                    .attach()
                    .build();
        }
    }

    @Override
    public void onPopFragment(Fragment fragment) {
        if (fragment instanceof F21Fragment) {
            onPopFragment((F21Fragment) fragment);
        }
    }

    public void onPopFragment(F21Fragment fragment) {
        mTextView.setText(String.format("%s\nResult: %s", fragment.getClass().getSimpleName(), fragment.result));
    }
}
