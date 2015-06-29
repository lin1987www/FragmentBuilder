package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.lin1987www.app.FragmentBuilder;

import lin1987www.com.fragmentbuilder.R;

public class MainActivity extends FragmentActivity {

    String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentBuilder.defaultContainerViewId = android.R.id.content;
        FragmentBuilder
                .create(this)
                .setContainerViewId(R.id.container)
                .setFragment(MainFragment.class)
                .add()
                .untraceable()
                .build();
    }

    @Override
    public void onBackPressed() {
        if (FragmentBuilder.hasPopBackStack(this)) {
            return;
        }
        super.onBackPressed();
    }
}
