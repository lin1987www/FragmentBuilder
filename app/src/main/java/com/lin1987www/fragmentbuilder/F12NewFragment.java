package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.FragmentBuilder;
import android.support.v4.app.FragmentFix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lin1987www.fragmentbuilder.widget.ShowEnterTextView;

/**
 * Created by Administrator on 2015/6/26.
 */
public class F12NewFragment extends FragmentFix {
    private final static String TAG = F12NewFragment.class.getSimpleName();

    public String result;
    TextView mTextView;
    ShowEnterTextView mShowEnterTextView;
    Button mNextStepButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f12, container, false);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mTextView.setText(String.format("%s", getTag()));
        mShowEnterTextView = (ShowEnterTextView) view.findViewById(R.id.showNameTextView);
        mNextStepButton = (Button) view.findViewById(R.id.nextStepButton);
        mNextStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentBuilder
                        .create(F12NewFragment.this)
                        .back()
                        .setFragment(F13Fragment.class, F13Fragment.class.getSimpleName())
                        .build();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        result = String.format("F12 New:[%s]", mShowEnterTextView.getEnterName());
        super.onDestroyView();
    }
}
