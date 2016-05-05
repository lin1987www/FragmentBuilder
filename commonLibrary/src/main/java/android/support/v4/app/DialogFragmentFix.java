package android.support.v4.app;

import android.os.Bundle;

public class DialogFragmentFix extends DialogFragment {
    @Override
    void performActivityCreated(Bundle savedInstanceState) {
        if (FragmentUtils.getFragmentManagerActivity(mChildFragmentManager) == null) {
            FragmentUtils.setChildFragmentManager(this, null);
        }
        super.performActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        // 如果是 setRetainInstance(true) 的情況時
        // Work around bug:
        // http://code.google.com/p/android/issues/detail?id=17423
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setOnDismissListener(null);
        }
        super.onDestroyView();
    }
}