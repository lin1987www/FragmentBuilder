package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.FragContent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentBuilder;
import android.support.v4.app.FragmentFix;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends FragmentFix implements FragmentBuilder.OnPopFragmentListener, View.OnClickListener {
    private final static String TAG = MainFragment.class.getSimpleName();
    FrameLayout mContainerMain4;
    FrameLayout mContainerMain5;
    TextView mTextView;
    Button mTestCodeButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        FragmentBuilder
                .create(this)
                .setContainerViewId(R.id.container_main_1)
                .setFragment(F1Fragment.class, F1Fragment.class.getSimpleName())
                .add()
                .build();

        FragmentBuilder
                .create(this)
                .setContainerViewId(R.id.container_main_2)
                .setFragment(F2Fragment.class, F2Fragment.class.getSimpleName())
                .add()
                .build();

        FragmentBuilder
                .create(this)
                .setContainerViewId(R.id.container_main_3)
                .setFragment(F3Fragment.class, F3Fragment.class.getSimpleName())
                .add()
                .build();

        mContainerMain4 = (FrameLayout) view.findViewById(R.id.container_main_4);
        mContainerMain4.setOnClickListener(this);

        mContainerMain5 = (FrameLayout) view.findViewById(R.id.container_main_5);
        mContainerMain5.setOnClickListener(this);

        mTextView = (TextView) view.findViewById(R.id.textView);
        mTestCodeButton = (Button) view.findViewById(R.id.testCodeButton);
        mTestCodeButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        if (mContainerMain4 == view) {
            FragmentBuilder
                    .create(MainFragment.this)
                    .setContainerViewId(R.id.container_main_4)
                    .setFragment(F4Fragment.class, F4Fragment.class.getSimpleName())
                    .attach()
                    .build();
        } else if (mContainerMain5 == view) {
            FragmentBuilder
                    .create(MainFragment.this)
                    .setContainerViewId(R.id.container_main_5)
                    .setFragment(F5Fragment.class, F5Fragment.class.getSimpleName())
                    .attach()
                    .build();
        } else if (mTestCodeButton == view) {
            FragmentBuilder builder = FragmentBuilder.findFragmentBuilder(new FragContent(MainFragment.this));
            Log.d(TAG, String.format("BackStackEntry: %s", builder));
        }
    }

    @Override
    public void onPopFragment(Fragment fragment) {
        if (fragment instanceof F4Fragment) {
            onPopFragment((F4Fragment) fragment);
        } else if (fragment instanceof F5Fragment) {
            onPopFragment((F5Fragment) fragment);
        }
    }

    public void onPopFragment(F4Fragment fragment) {
        mTextView.setText(String.format("%s\n%s %s", getClass().getSimpleName(), fragment.getClass().getSimpleName(), fragment.result));
    }

    public void onPopFragment(F5Fragment fragment) {
        mTextView.setText(String.format("%s\n%s %s", getClass().getSimpleName(), fragment.getClass().getSimpleName(), fragment.result));
    }
}
