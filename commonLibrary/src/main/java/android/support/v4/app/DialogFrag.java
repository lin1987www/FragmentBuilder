package android.support.v4.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lin1987www.common.R;

/**
 * Created by Administrator on 2016/6/3.
 */
public class DialogFrag extends FragmentFix implements View.OnClickListener {
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
            try {
                if (TextUtils.isEmpty(fragArgs.getFragClassName())) {
                    FragmentBuilder.hasPopBackStack(getActivity());
                    return;
                }
                final Class fragClass = Class.forName(fragArgs.getFragClassName(), false, getContext().getClassLoader());
                final FragmentBuilder contentBuilder = FragmentBuilder.findFragmentBuilder(new FragContent(this));
                FragmentBuilder.popBackStackRecord(getActivity())
                        .setTailPopStackListener(new FragmentBuilder.PopBackStackListener() {
                            @Override
                            public void onPopBackStack(Object onPopFragmentObject, Fragment popFragment) {
                                FragmentBuilder
                                        .create(onPopFragmentObject)
                                        .replace()
                                        .addToBackStack()
                                        .setContainerViewId(contentBuilder.getContainerViewId())
                                        .setFragment(fragClass, fragArgs.getFragTag())
                                        .setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out)
                                        .setArgs(fragArgs.getFragArgs())
                                        .build();
                            }
                        }).popBackStack();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
}
