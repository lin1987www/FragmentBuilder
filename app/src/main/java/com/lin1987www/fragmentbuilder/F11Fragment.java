package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lin1987www.app.FragmentTransactionBuilder2;

import lin1987www.com.fragmentbuilder.R;

/**
 * Created by Administrator on 2015/6/26.
 */
public class F11Fragment extends Fragment {
    TextView mTextView;
    FrameLayout mContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f11, container, false);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mTextView.setText(String.format("tag: %s id: %s", getTag(), getId()));
        mContainer = (FrameLayout) view.findViewById(R.id.container_f11);
        mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransactionBuilder2
                        .create(F11Fragment.this)
                        .setContainerViewId(R.id.container_f11)
                        .setFragment(F111Fragment.class, F111Fragment.class.getSimpleName())
                        .add()
                        .traceable()
                        .build();
            }
        });
        return view;
    }
}
