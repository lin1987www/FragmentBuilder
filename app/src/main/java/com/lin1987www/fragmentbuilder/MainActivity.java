package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.lin1987www.app.FragmentBuilder;

import lin1987www.com.fragmentbuilder.R;

public class MainActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentBuilder.defaultContainerViewId = R.id.container;
        FragmentBuilder
                .create(this)
                .setContainerViewId(R.id.container)
                .setFragment(MainFragment.class)
                .add()
                .untraceable()
                .build();
    }

    public void onPopFragment(WizardStepsFragment fragment) {
        if (fragment.isFinish) {
            Toast.makeText(this, fragment.result, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (FragmentBuilder.hasPopBackStack(this)) {
            return;
        }
        super.onBackPressed();
    }
}
