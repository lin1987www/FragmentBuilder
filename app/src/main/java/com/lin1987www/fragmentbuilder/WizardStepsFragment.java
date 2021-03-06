package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentBuilder;
import android.support.v4.app.FragmentFix;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2015/7/3.
 */
public class WizardStepsFragment extends FragmentFix implements FragmentBuilder.OnPopFragmentListener {
    private final static String TAG = WizardStepsFragment.class.getSimpleName();
    public boolean isFinish = false;
    public String result = "";
    private String f11text = "";
    private String f12text = "";
    private String f13text = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_steps, container, false);
        if (getArguments().getBoolean("openStep")) {
            getArguments().putBoolean("openStep", false);
            FragmentBuilder
                    .create(this)
                    .backContainer()
                    .setFragment(F11Fragment.class, F11Fragment.class.getSimpleName())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(F11Fragment.BACK_STACK_NAME)
                    .build();
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onPopFragment(Fragment fragment) {
        if (fragment instanceof F11Fragment) {
            onPopFragment((F11Fragment) fragment);
        } else if (fragment instanceof F12Fragment) {
            onPopFragment((F12Fragment) fragment);
        } else if (fragment instanceof F12NewFragment) {
            onPopFragment((F12NewFragment) fragment);
        } else if (fragment instanceof F13Fragment) {
            onPopFragment((F13Fragment) fragment);
        }
    }

    public void onPopFragment(F11Fragment fragment) {
        f11text = fragment.result;
        if (isFinish) {
            result = String.format("WizardStepsTextView Result:\n%s\n%s\n%s", f11text, f12text, f13text);
        }
        FragmentBuilder.popBackStackRecord(getActivity()).popBackStack();
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
