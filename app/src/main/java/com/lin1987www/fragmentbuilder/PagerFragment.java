package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.FragmentFix;
import android.support.v4.app.FragmentStatePagerAdapterFix;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import lin1987www.com.fragmentbuilder.R;

/**
 * Created by Administrator on 2015/7/8.
 */
public class PagerFragment extends FragmentFix {
    ViewPager mPager;

    FragmentStatePagerAdapterFix mPagerAdapter;
    Button mAppendPageButton;
    Button mRemoveLastPageButton;

    Class<?>[] mFragArray = new Class[]{
            F1Fragment.class,
            F2Fragment.class,
            F3Fragment.class,
            F4Fragment.class,
            F5Fragment.class
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pager, container, false);
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mAppendPageButton = (Button) view.findViewById(R.id.appendPageButton);
        mRemoveLastPageButton = (Button) view.findViewById(R.id.removeLastPageButton);
        mPager.setOffscreenPageLimit(1);

        mPagerAdapter = new FragmentStatePagerAdapterFix(this);
        mPager.setAdapter(mPagerAdapter);

        mAppendPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = mPagerAdapter.getCount() % mFragArray.length;
                Class fragClass = mFragArray[index];
                String tag = fragClass.getSimpleName() + "_" + mPagerAdapter.getCount();
                mPagerAdapter.add(fragClass, tag);
                mPagerAdapter.notifyDataSetChanged();
            }
        });

        mRemoveLastPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = mPagerAdapter.getCount() - 1;
                if (index >= 0) {
                    mPagerAdapter.remove(index);
                    mPagerAdapter.notifyDataSetChanged();
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPagerAdapter.getCount() == 0) {
            for (Class fragClass : mFragArray) {
                String tag = fragClass.getSimpleName() + "_" + mPagerAdapter.getCount();
                mPagerAdapter.add(fragClass, tag);
            }
            mPagerAdapter.notifyDataSetChanged();
        }
    }
}
