package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.ExecutorSet;
import android.support.v4.app.FragmentBuilder;
import android.support.v4.app.FragmentFix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lin1987www.fragmentbuilder.api.GetUser;

import fix.java.util.concurrent.Duty;
import fix.java.util.concurrent.DutyOn;


/**
 * Created by Administrator on 2015/6/26.
 */
public class F11Fragment extends FragmentFix {
    public final static String BACK_STACK_NAME = F11Fragment.class.getSimpleName();
    private final static String TAG = F11Fragment.class.getSimpleName();
    public String result;
    String f111Result = "";

    TextView mTextView;
    FrameLayout mContainer;
    EditText mEditText;
    Button mNextStepButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_f11, container, false);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mTextView.setText(String.format("%s", getTag()));
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
        mEditText = (EditText) view.findViewById(R.id.editText);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Duty duty = new GetUser().setExecutorService(ExecutorSet.nonBlockExecutor);
        duty.always(new DutyOn(this).setExecutorService(ExecutorSet.mainThreadExecutor));
        duty(duty);
    }

    @Override
    public void onDestroyView() {
        result = String.format("F11:[%s] F111:[%s]", mEditText.getText().toString(), f111Result);
        super.onDestroyView();
    }

    public void onPopFragment(F111Fragment fragment) {
        f111Result = fragment.result;
    }

    public boolean onDuty(GetUser task) {
        if (task.isDone()) {
            Toast.makeText(getActivity(), String.format("UserName: %s", task.userName), Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }
}
