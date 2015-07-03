package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lin1987www.app.FragmentBuilder;

import lin1987www.com.fragmentbuilder.R;

/**
 * Created by Administrator on 2015/7/3.
 */
public class WizardStepsFragment extends Fragment {
    public boolean isFinish = false;
    public String result = "";
    private String f11text = "";
    private String f12text = "";
    private String f13text = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_steps, container, false);
        if (getArguments().getBoolean("openStep")) {
            getArguments().putBoolean("openStep", false);
            FragmentBuilder
                    .create(this)
                    .backContainer()
                    .setFragment(F11Fragment.class, F11Fragment.class.getSimpleName())
                    .addToBackStack(F11Fragment.BACK_STACK_NAME)
                    .build();
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onPopFragment(F11Fragment fragment) {
        f11text = fragment.result;
        if (isFinish) {
            result = String.format("WizardStepsTextView Result:\n%s\n%s\n%s", f11text, f12text, f13text);
        }
        getActivity().onBackPressed();
    }

    public void onPopFragment(F12Fragment fragment) {
        f12text = fragment.result;
    }

    public void onPopFragment(F12NewFragment fragment) {
        f12text = fragment.result;
    }

    public void onPopFragment(F13Fragment fragment) {
        this.isFinish = fragment.isFinish;
        f13text = fragment.result;
    }
}
