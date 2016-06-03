package android.support.v4.app;

import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lin1987www.common.R;

import java.lang.ref.WeakReference;

import okhttp3.OkHttpHelper;

/**
 * Created by Administrator on 2016/5/25.
 */
public class StatusFrag extends FragmentFix {
    ContentLoadingProgressBar progressBar;
    TextView textView;

    StatusDuty statusDuty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_status, container, false);
        progressBar = (ContentLoadingProgressBar) contentView.findViewById(R.id.progressBar);
        textView = (TextView) contentView.findViewById(android.R.id.message);
        progressBar.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.GONE);
        statusDuty = new StatusDuty(this);
        container.postDelayed(statusDuty, 500L);
        return contentView;
    }

    public static class StatusDuty implements Runnable {
        WeakReference<StatusFrag> fragmentFixWeakReference;

        public StatusDuty(StatusFrag fragmentFix) {
            fragmentFixWeakReference = new WeakReference<>(fragmentFix);
        }

        @Override
        public void run() {
            StatusFrag fragmentFix = fragmentFixWeakReference.get();
            if (FragmentUtils.isFragmentAvailable(fragmentFix)) {
                if (OkHttpHelper.getOkHttpClient().dispatcher().runningCallsCount() > 0) {
                    fragmentFix.progressBar.setVisibility(View.VISIBLE);
                } else {
                    fragmentFix.progressBar.setVisibility(View.INVISIBLE);
                }
                fragmentFix.getView().postDelayed(this, 500L);
            }
        }
    }
}
