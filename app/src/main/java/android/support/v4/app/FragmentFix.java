package android.support.v4.app;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.lin1987www.app.FragmentArgs;

/**
 * Created by Administrator on 2015/7/8.
 */
public class FragmentFix extends Fragment {
    FragmentArgs fragmentArgs;

    public FragmentArgs getFragmentArgs() {
        if (fragmentArgs == null) {
            fragmentArgs = new FragmentArgs(getArguments());
        }
        return fragmentArgs;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        // TODO 之後要改成每個Fragment 都要有 Arguments
        super.onViewStateRestored(savedInstanceState);
        //getArguments().getBoolean("hasViewStateRestored", true);
        // 如果是回復狀態  就略過一次 Fragment 的建立
        if (savedInstanceState != null) {
            getFragmentArgs().skipReAttach();
        }
    }

    @Override
    void performResume() {
        if (mChildFragmentManager != null) {
            mChildFragmentManager.noteStateNotSaved();
            mChildFragmentManager.execPendingActions();
        }
        if (getFragmentArgs().consumeOnResume()) {
            // Consume OnResume Event
        } else {
            mCalled = false;
            onResume();
            if (!mCalled) {
                throw new SuperNotCalledException("Fragment " + this
                        + " did not call through to super.onResume()");
            }
        }
        if (mChildFragmentManager != null) {
            mChildFragmentManager.dispatchResume();
            mChildFragmentManager.execPendingActions();
        }
    }
}
