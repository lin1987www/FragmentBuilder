package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.FragmentBuilder;
import android.support.v4.app.FragmentFix;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import lin1987www.com.fragmentbuilder.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends FragmentFix {
    private final static String TAG = MainFragment.class.getSimpleName();
    FrameLayout mContainerMain4;
    FrameLayout mContainerMain5;
    TextView mTextView;
    Button mTestCodeButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // TODO 因為每次都會覆蓋上去，所致被蓋上去了!!，因為遮住螢幕 使用 MainFragment 重新建立覆蓋上去!
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
        mTestCodeButton = (Button) view.findViewById(R.id.testCodeButton);
        mTestCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager.BackStackEntry backStackEntry =
                        FragmentBuilder.findBackStackEntry(MainFragment.this);

                Log.e(TAG, String.format("BackStackEntry: %s", backStackEntry));
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onPopFragment(F4Fragment fragment) {
        mTextView.setText(String.format("%s\n%s %s", getClass().getSimpleName(), fragment.getClass().getSimpleName(), fragment.result));
    }

    public void onPopFragment(F5Fragment fragment) {
        mTextView.setText(String.format("%s\n%s %s", getClass().getSimpleName(), fragment.getClass().getSimpleName(), fragment.result));
    }
}
