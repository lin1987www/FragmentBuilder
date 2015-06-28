package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lin1987www.app.FragmentBuilder;

import lin1987www.com.fragmentbuilder.R;

/**
 * Created by Administrator on 2015/6/26.
 */
public class F1Fragment extends Fragment {
    TextView mTextView;
    FrameLayout mContainer;
    Button mButton;
    TextView mF11TextView;
    TextView mF12TextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f1, container, false);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mTextView.setText(String.format("%s id:[%s]", getTag(), getId()));
        mContainer = (FrameLayout) view.findViewById(R.id.container_f1);
        mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentBuilder
                        .create(F1Fragment.this)
                        .setContainerViewId(R.id.container_f1)
                        .setFragment(F11Fragment.class, F11Fragment.class.getSimpleName())
                        .add()
                        .traceable()
                        .build();
            }
        });
        mButton = (Button) view.findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentBuilder
                        .create(F1Fragment.this)
                        .setContainerViewId(R.id.container_f1)
                        .setFragment(F12Fragment.class, F12Fragment.class.getSimpleName())
                        .replace()
                        .traceable()
                        .build();
            }
        });
        mF11TextView = (TextView) view.findViewById(R.id.f11TextView);
        mF12TextView = (TextView) view.findViewById(R.id.f12TextView);
        return view;
    }


    public void onPopFragment(F11Fragment fragment) {
        mF11TextView.setText(String.format("->%s %s", fragment.getClass().getSimpleName(), fragment.result));
    }

    public void onPopFragment(F12Fragment fragment) {
        mF12TextView.setText(String.format("->%s %s", fragment.getClass().getSimpleName(), fragment.result));
    }
}
