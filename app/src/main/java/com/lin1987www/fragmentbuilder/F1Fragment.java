package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.FragmentBuilder;
import android.support.v4.app.FragmentFix;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import lin1987www.com.fragmentbuilder.R;

/**
 * Created by Administrator on 2015/6/26.
 */
public class F1Fragment extends FragmentFix {
    TextView mTextView;
    FrameLayout mContainer;

    public boolean isFinish = false;
    public String result = "";
    private String f11text = "";
    private String f12text = "";
    private String f13text = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f1, container, false);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mTextView.setText(String.format("%s", getTag()));
        mContainer = (FrameLayout) view.findViewById(R.id.container_f1);
        mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentBuilder
                        .create(F1Fragment.this)
                        .setContainerViewId(R.id.container_f1)
                        .setFragment(F11Fragment.class, F11Fragment.class.getSimpleName())
                        .replace()
                        .addToBackStack(F11Fragment.BACK_STACK_NAME)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .build();
            }
        });
        return view;
    }

    public void onPopFragment(F11Fragment fragment) {
        f11text = fragment.result;
        if (isFinish) {
            result = String.format("F1 Result:\n%s\n%s\n%s", f11text, f12text, f13text);
            Toast.makeText(this.getActivity(), result, Toast.LENGTH_SHORT).show();
        }
    }

    public void onPopFragment(F12Fragment fragment) {
        f12text = fragment.result;
    }

    public void onPopFragment(F12NewFragment fragment) {
        f12text = fragment.result;
    }

    public void onPopFragment(F13Fragment fragment) {
        isFinish = fragment.isFinish;
        f13text = fragment.result;
    }
}
