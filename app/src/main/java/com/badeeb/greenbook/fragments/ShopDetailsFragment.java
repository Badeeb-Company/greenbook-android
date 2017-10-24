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
import android.widget.ImageView;
import android.widget.TextView;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.adaptors.FragmentViewPagerAdapter;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.shared.UiUtils;
import com.bumptech.glide.Glide;

import org.parceler.Parcels;
import org.w3c.dom.Text;

public class ShopDetailsFragment extends Fragment {

    public final static String TAG = ShopDetailsFragment.class.getName();
    public final static String EXTRA_SHOP_OBJECT = "EXTRA_SHOP_OBJECT";

    private MainActivity mActivity;
    private ProgressDialog mProgressDialog;

    private Shop mShop;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private ImageView ivShopMainPhoto;
    private TextView tvShopName;
    private TextView tvDescription;
    private TextView tvNearLocation;


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

        mShop = Parcels.unwrap(getArguments().getParcelable(EXTRA_SHOP_OBJECT));

        // ViewPager
        this.mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        this.setupViewPager(this.mViewPager);    // Defines the number of tabs by setting appropriate fragment and tab name

        // Tabs
        this.mTabLayout = (TabLayout) view.findViewById(R.id.tabs);
        this.mTabLayout.setupWithViewPager(this.mViewPager);        // Assigns the ViewPager to TabLayout.

        initUiFields(view);

        setupListener();
    }

    private void initUiFields(View view) {
        ivShopMainPhoto = (ImageView)  view.findViewById(R.id.ivShopMainPhoto);
        tvShopName = (TextView) view.findViewById(R.id.tvShopName);
        tvDescription = (TextView) view.findViewById(R.id.tvDescription);
        tvNearLocation = (TextView) view.findViewById(R.id.tvNearLocation);

        fillUiFields();
    }

    private void fillUiFields() {
        Log.d(TAG, "fillUiFields - Start");

        Glide.with(mActivity).load(mShop.getMainPhotoURL()).into(ivShopMainPhoto);
        tvShopName.setText(mShop.getName());
        tvDescription.setText(mShop.getDescription());
        tvNearLocation.setText(getShopDistance());

        Log.d(TAG, "fillUiFields - end");
    }

    private String getShopDistance(){
        return "5 Km around you";
    }


    public void setupListener(){

    }

    private void setupViewPager(ViewPager viewPager) {
        Log.d(TAG, "setupViewPager - Start");

        FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(getFragmentManager());

        DetailsTabFragment detailsTabFragment = new DetailsTabFragment();
        GalleryTabFragment galleryTabFragment = new GalleryTabFragment();
        ReviewsTabFragment reviewsTabFragment = new ReviewsTabFragment();

        Log.d(TAG, "setupViewPager - mShop"+mShop.getName());

        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_SHOP_OBJECT, Parcels.wrap(mShop));

        detailsTabFragment.setArguments(bundle);
        galleryTabFragment.setArguments(bundle);
        reviewsTabFragment.setArguments(bundle);

        adapter.addFragment(detailsTabFragment, "Details");
        adapter.addFragment(galleryTabFragment, "Gallery");
        adapter.addFragment(reviewsTabFragment, "Reviews");

        viewPager.setAdapter(adapter);

        Log.d(TAG, "setupViewPager - End");
    }

}
