package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.FragmentFix;
import android.support.v4.app.FragmentStatePagerAdapterFix;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Administrator on 2015/7/8.
 */
public class PagerFragment extends FragmentFix implements View.OnClickListener {
    ViewPager mPager;

    FragmentStatePagerAdapterFix mPagerAdapter;
    Button mAppendPageButton;
    Button mRemoveLastPageButton;
    Button mClearPageButton;

    Class<?>[] mFragArray = new Class[]{
            F1Fragment.class,
            Pager2Fragment.class,
            F2Fragment.class,
            F3Fragment.class,
            F4Fragment.class,
            RecyclePanelFrag.class,
            F5Fragment.class
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pager, container, false);
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mAppendPageButton = (Button) view.findViewById(R.id.appendPageButton);
        mRemoveLastPageButton = (Button) view.findViewById(R.id.removeLastPageButton);
        mClearPageButton = (Button) view.findViewById(R.id.clearPageButton);

        mPager.setOffscreenPageLimit(1);

        mPagerAdapter = new FragmentStatePagerAdapterFix(this);
        mPager.setAdapter(mPagerAdapter);

        mAppendPageButton.setOnClickListener(this);
        mRemoveLastPageButton.setOnClickListener(this);
        mClearPageButton.setOnClickListener(this);

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

    @Override
    public void onClick(View view) {
        if (mAppendPageButton == view) {
            int index = mPagerAdapter.getCount() % mFragArray.length;
            Class fragClass = mFragArray[index];
            String tag = fragClass.getSimpleName() + "_" + mPagerAdapter.getCount();
            mPagerAdapter.add(fragClass, tag);
            mPagerAdapter.notifyDataSetChanged();
        } else if (mRemoveLastPageButton == view) {
            int index = mPagerAdapter.getCount() - 1;
            if (index >= 0) {
                mPagerAdapter.remove(index);
                mPagerAdapter.notifyDataSetChanged();
            }
        } else if (mClearPageButton == view) {
            mPagerAdapter.clear();
            mPagerAdapter.notifyDataSetChanged();
        }
    }
}
