package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.FragContent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivityFix;
import android.support.v4.app.FragmentBuilder;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends FragmentActivityFix {
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

        FragContent content = new FragContent(this);
        FragmentManager fragmentManager = content.getContainerFragmentManager(R.id.container);
        if (fragmentManager.findFragmentById(R.id.container) == null) {
            FragmentBuilder
                    .create(this)
                    .add()
                    .setContainerViewId(R.id.container)
                    .setFragment(RecyclePanelFrag.class)
                    .build();
        }
    }

    public void onPopFragment(WizardStepsFragment fragment) {
        if (fragment.isFinish) {
            Toast.makeText(this, fragment.result, Toast.LENGTH_SHORT).show();
        }
    }
}
