package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lin1987www.app.FragmentBuilder;
import com.lin1987www.fragmentbuilder.widget.ShowNameTextView;

import lin1987www.com.fragmentbuilder.R;

/**
 * Created by Administrator on 2015/6/26.
 */
public class F12Fragment extends Fragment {
    private final static String TAG = F12Fragment.class.getSimpleName();
    public String result;
    TextView mTextView;
    ShowNameTextView mShowNameTextView;
    Button mFinishButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f12, container, false);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mTextView.setText(String.format("%s id:[%s]", getTag(), getId()));
        mShowNameTextView = (ShowNameTextView) view.findViewById(R.id.showNameTextView);
        mFinishButton = (Button) view.findViewById(R.id.finishButton);
        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentBuilder.popBackStack(getActivity(), "Wizard Steps Test", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        result = String.format("F12:[%s]", mShowNameTextView.getText().toString());
        super.onDestroyView();
    }
}
