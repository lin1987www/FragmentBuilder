package com.lin1987www.fragmentbuilder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentFix;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import lin1987www.com.fragmentbuilder.R;

/**
 * Created by Administrator on 2015/7/8.
 */
public class PagerFragment extends FragmentFix {
    ViewPager mPager;
    PagerAdapter mPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pager, container, false);
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(1);
        mPagerAdapter = new PagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        return view;
    }

    class PagerAdapter extends FragmentPagerAdapter {
        ArrayList<Class<? extends Fragment>> fragClassList = new ArrayList<>();

        public PagerAdapter(FragmentManager fm) {
            super(fm);
            fragClassList.add(F1Fragment.class);
            fragClassList.add(F2Fragment.class);
            fragClassList.add(F3Fragment.class);
            fragClassList.add(F4Fragment.class);
            fragClassList.add(F5Fragment.class);
        }

        @Override
        public Fragment getItem(int position) {
            Class<? extends Fragment> fragClass = fragClassList.get(position);
            Fragment frag = Fragment.instantiate(
                    getActivity(),
                    fragClass.getName(),
                    null
            );
            return frag;
        }

        @Override
        public int getCount() {
            return fragClassList.size();
        }
    }
}
