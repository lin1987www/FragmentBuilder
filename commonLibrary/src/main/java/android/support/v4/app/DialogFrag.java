package android.support.v4.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lin1987www.common.R;
import com.lin1987www.common.Utility;

/**
 * Created by Administrator on 2016/6/3.
 */
public class DialogFrag extends FragmentFix implements View.OnClickListener, FragmentBuilder.OnPopBackStackListener {
    DialogFragArgs fragArgs;

    ViewGroup layout;
    TextView messageTextView;
    Button cancelButton;
    Button okButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog, container, false);
        fragArgs = new DialogFragArgs(getArguments());

        layout = (ViewGroup) view.findViewById(R.id.layout);
        layout.setOnClickListener(this);

        messageTextView = (TextView) view.findViewById(R.id.messageTextView);
        messageTextView.setText(fragArgs.getMessage());

        cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);

        okButton = (Button) view.findViewById(R.id.okButton);
        okButton.setOnClickListener(this);

        if (TextUtils.isEmpty(fragArgs.getFragClassName())) {
            cancelButton.setVisibility(View.GONE);
        } else {
            cancelButton.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        if (view == layout || view == cancelButton) {
            FragmentBuilder.hasPopBackStack(getActivity());
        } else if (view == okButton) {
            if (TextUtils.isEmpty(fragArgs.getFragClassName())) {
                FragmentBuilder.hasPopBackStack(getActivity());
                return;
            }
            FragmentBuilder
                    .popBackStackRecord(getActivity())
                    .setPopBackStackListener(this)
                    .popBackStack();
        }
    }

    @Override
    public void onPopBackStack(Object recipient, Fragment packageFragment, FragmentBuilder.FragCarrier fragCarrier) {
        try {
            Class fragClass = Class.forName(fragArgs.getFragClassName(), false, Utility.getClassLoader());
            FragmentBuilder contentBuilder = FragmentBuilder.findFragmentBuilder(new FragContent(this));
            FragmentBuilder
                    .create(recipient)
                    .replace()
                    .addToBackStack()
                    .setContainerViewId(contentBuilder.getContainerViewId())
                    .setFragment(fragClass, fragArgs.getFragTag())
                    .setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out)
                    .setArgs(fragArgs.getFragArgs())
                    .build();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
