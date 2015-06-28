package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lin1987www.app.FragmentBuilder;

import lin1987www.com.fragmentbuilder.R;

/**
 * Created by Administrator on 2015/6/26.
 */
public class F11Fragment extends Fragment {
    public String result;

    TextView mTextView;
    FrameLayout mContainer;
    Button mNextStepButton;
    EditText mNumberEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f11, container, false);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mTextView.setText(String.format("%s id:[%s]", getTag(), getId()));
        mContainer = (FrameLayout) view.findViewById(R.id.container_f11);
        mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentBuilder
                        .create(F11Fragment.this)
                        .setContainerViewId(R.id.container_f11)
                        .setFragment(F111Fragment.class, F111Fragment.class.getSimpleName())
                        .add()
                        .traceable()
                        .build();
            }
        });
        mNextStepButton = (Button) view.findViewById(R.id.nextStepButton);
        mNextStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentBuilder
                        .create(F11Fragment.this)
                        .back()
                        .setFragment(F12Fragment.class, F12Fragment.class.getSimpleName())
                        .build();
            }
        });
        mNumberEditText = (EditText) view.findViewById(R.id.editText);
        return view;
    }

    @Override
    public void onDestroyView() {
        result = String.format("Number:[%s]", mNumberEditText.getText().toString());
        super.onDestroyView();
    }

    public void onPopFragment(F111Fragment fragment) {
        mTextView.setText(String.format("->%s %s", fragment.getClass().getSimpleName(), fragment.result));
    }
}
