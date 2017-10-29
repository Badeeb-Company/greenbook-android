package com.badeeb.greenbook.adaptors;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amr Alghawy on 6/7/2017.
 * Custom adapter class provides fragments required for the view pager.
 */

public class FragmentViewPagerAdapter extends FragmentStatePagerAdapter {

    // Define class attributes
    private final List<Fragment> mFragmentList;
    private final List<String> mFragmentTitleList;

    // Constructor
    public FragmentViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);

        // Attributes initialization
        this.mFragmentList = new ArrayList<>();
        this.mFragmentTitleList = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        return this.mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return this.mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        return this.mFragmentTitleList.get(position);
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }
}
