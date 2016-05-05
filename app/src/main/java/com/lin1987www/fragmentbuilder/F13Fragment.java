package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.FragmentBuilder;
import android.support.v4.app.FragmentFix;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lin1987www.fragmentbuilder.widget.ShowEnterTextView;


/**
 * Created by Administrator on 2015/6/26.
 */
public class F13Fragment extends FragmentFix {
    private final static String TAG = F13Fragment.class.getSimpleName();
    public boolean isFinish = false;
    public String result;
    TextView mTextView;
    ShowEnterTextView mShowEnterTextView;
    Button mFinishButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f13, container, false);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mTextView.setText(String.format("%s", getTag()));
        mShowEnterTextView = (ShowEnterTextView) view.findViewById(R.id.showNameTextView);
        mFinishButton = (Button) view.findViewById(R.id.finishButton);
        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFinish = true;
                FragmentBuilder.popBackStack(
                        getActivity(),
                        F11Fragment.BACK_STACK_NAME,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE,
                        true
                );
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
        result = String.format("F13:[%s]", mShowEnterTextView.getEnterName());
        super.onDestroyView();
    }
}
