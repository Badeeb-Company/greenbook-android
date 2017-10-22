package com.badeeb.greenbook.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.adaptors.FragmentViewPagerAdapter;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.shared.UiUtils;

public class ShopDetailsFragment extends Fragment {

    private final static String TAG = ShopDetailsFragment.class.getName();

    private MainActivity mActivity;
    private ProgressDialog mProgressDialog;

    private Shop mShop;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    public ShopDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_shop_details, container, false);

        init(view);

        return view;
    }

    public void init(View view){
        mActivity = (MainActivity) getActivity();
        mProgressDialog = UiUtils.createProgressDialog(mActivity);

        // ViewPager
        this.mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        this.setupViewPager(this.mViewPager);    // Defines the number of tabs by setting appropriate fragment and tab name

        // Tabs
        this.mTabLayout = (TabLayout) view.findViewById(R.id.tabs);
        this.mTabLayout.setupWithViewPager(this.mViewPager);        // Assigns the ViewPager to TabLayout.

        setupListener();
    }

    public void setupListener(){

    }

    private void setupViewPager(ViewPager viewPager) {
        Log.d(TAG, "setupViewPager - Start");

        FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(getFragmentManager());

        adapter.addFragment(new DetailsTabFragment(), "Details");
        adapter.addFragment(new GalleryTabFragment(), "Gallery");
        adapter.addFragment(new ReviewsTabFragment(), "Reviews");

        viewPager.setAdapter(adapter);

        Log.d(TAG, "setupViewPager - End");
    }

}
