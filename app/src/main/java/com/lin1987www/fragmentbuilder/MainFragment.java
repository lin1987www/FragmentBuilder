package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lin1987www.app.FragmentBuilder;

import lin1987www.com.fragmentbuilder.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment implements FragmentBuilder.PopFragmentListener {
    FrameLayout mContainerMain4;
    FrameLayout mContainerMain5;
    TextView mTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);



        FragmentBuilder
                .create(this)
                .setContainerViewId(R.id.container_main_1)
                .setFragment(F1Fragment.class, F1Fragment.class.getSimpleName())
                .add()
                .untraceable()
                .build();

        FragmentBuilder
                .create(this)
                .setContainerViewId(R.id.container_main_2)
                .setFragment(F2Fragment.class, F2Fragment.class.getSimpleName())
                .add()
                .untraceable()
                .build();

        FragmentBuilder
                .create(this)
                .setContainerViewId(R.id.container_main_3)
                .setFragment(F3Fragment.class, F3Fragment.class.getSimpleName())
                .add()
                .untraceable()
                .build();

        mContainerMain4 = (FrameLayout) view.findViewById(R.id.container_main_4);
        mContainerMain4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentBuilder
                        .create(MainFragment.this)
                        .setContainerViewId(R.id.container_main_4)
                        .setFragment(F4Fragment.class, F4Fragment.class.getSimpleName())
                        .add()
                        .traceable()
                        .build();
            }
        });

        mContainerMain5 = (FrameLayout) view.findViewById(R.id.container_main_5);
        mContainerMain5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentBuilder
                        .create(MainFragment.this)
                        .setContainerViewId(R.id.container_main_5)
                        .setFragment(F5Fragment.class, F5Fragment.class.getSimpleName())
                        .add()
                        .traceable()
                        .build();
            }
        });

        mTextView = (TextView) view.findViewById(R.id.textView);
        return view;
    }

    @Override
    public void onPopFragment(Fragment fragment) {
        mTextView.setText(String.format("%s", fragment.getClass().getSimpleName()));
    }

    public void onPopFragment(F4Fragment fragment) {
        mTextView.setText(String.format("->%s %s", fragment.getClass().getSimpleName(), fragment.result));
    }

    public void onPopFragment(F5Fragment fragment) {
        mTextView.setText(String.format("->%s %s", fragment.getClass().getSimpleName(), fragment.result));
    }
}
