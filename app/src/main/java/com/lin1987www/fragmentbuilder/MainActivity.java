package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lin1987www.app.FragmentBuilder;

import lin1987www.com.fragmentbuilder.R;

public class MainActivity extends FragmentActivity {
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Class<? extends Fragment> fragClass = MainFragment.class;
                Fragment hereFrag = getSupportFragmentManager().findFragmentById(R.id.container);
                if (hereFrag != null) {
                    if (hereFrag.getClass().equals(PagerFragment.class)) {
                        fragClass = MainFragment.class;
                    } else {
                        fragClass = PagerFragment.class;
                    }
                }
                FragmentBuilder
                        .create(MainActivity.this)
                        .setContainerViewId(R.id.container)
                        .setFragment(fragClass)
                        .reset()
                        .replace()
                        .build();
            }
        });

        FragmentBuilder.defaultContainerViewId = R.id.container;

        Fragment hereFrag = getSupportFragmentManager().findFragmentById(R.id.container);
        if (hereFrag == null) {
            FragmentBuilder
                    .create(this)
                    .setContainerViewId(R.id.container)
                    .setFragment(MainFragment.class)
                    .add()
                    .untraceable()
                    .build();
        }
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
