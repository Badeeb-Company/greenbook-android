package com.badeeb.greenbook.fragments;

import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.adaptors.FragmentViewPagerAdapter;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.network.NonAuthorizedCallback;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.UiUtils;
import com.badeeb.greenbook.shared.Utils;
import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;
import com.makeramen.roundedimageview.RoundedImageView;

import org.parceler.Parcels;

import java.lang.reflect.Type;
import java.text.DecimalFormat;

public class ShopDetailsFragment extends Fragment {

    public final static String TAG = ShopDetailsFragment.class.getName();
    public final static String EXTRA_SHOP_OBJECT = "EXTRA_SHOP_OBJECT";
    public final static String EXTRA_OPEN_TAB = "EXTRA_OPEN_TAB";

    private MainActivity mActivity;
    private ProgressDialog mProgressDialog;
    private FragmentManager fragmentManager;

    private Shop mShop;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private RoundedImageView rivShopMainPhoto;
    private ImageView ivFavShop;
    private RatingBar rbShopRate;
    private TextView tvRatingValue;
    private TextView tvShopName;
    private TextView tvAddress;
    private TextView tvNearLocation;
    private ImageView ivToolbarBack;
    private TextView tvToolbarShopName;
    private TextView tvNumberOfReviews;
    private ProgressBar loadingProgressBar;


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
        if (container != null) {
            // this code is used to prevent fragment overlapping
            container.removeAllViews();
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shop_details, container, false);

        init(view);

        return view;
    }

    public void init(View view) {
        Log.d(TAG, "init - start ");
        mActivity = (MainActivity) getActivity();
        mProgressDialog = UiUtils.createProgressDialog(mActivity);
        fragmentManager = getFragmentManager();

        mShop = Parcels.unwrap(getArguments().getParcelable(EXTRA_SHOP_OBJECT));
        int selectedTab = getArguments().getInt(EXTRA_OPEN_TAB, 0);

        // ViewPager
        this.mViewPager = view.findViewById(R.id.viewpager);
        // Defines the number of tabs by setting appropriate fragment and tab name

        mViewPager.setCurrentItem(selectedTab);

        // Tabs
        this.mTabLayout = (TabLayout) view.findViewById(R.id.tabs);
        this.mTabLayout.setupWithViewPager(this.mViewPager);        // Assigns the ViewPager to TabLayout.

        initUiFields(view);

        setupListener();

        callGetShopDetailsApi();
    }

    private void callGetShopDetailsApi() {
//        mProgressDialog.show();
        UiUtils.show(loadingProgressBar);

        String url = Constants.BASE_URL + "/shops/" + mShop.getGooglePlaceId();

        NonAuthorizedCallback<JsonResponse<Shop>> callback = new NonAuthorizedCallback<JsonResponse<Shop>>() {
            @Override
            public void onSuccess(JsonResponse<Shop> jsonResponse) {
                if (jsonResponse != null && jsonResponse.getResult() != null) {
                    mShop = jsonResponse.getResult();
                    setupViewPager(mViewPager);
                }
                mProgressDialog.dismiss();
                UiUtils.hide(loadingProgressBar);
            }

            @Override
            public void onError() {
                mActivity.getmSnackBarDisplayer().displayError("Error while getting shops from the server");
                mProgressDialog.dismiss();
                UiUtils.hide(loadingProgressBar);
            }
        };

        Type responseType = new TypeToken<JsonResponse<Shop>>() {
        }.getType();

        VolleyWrapper<Object, JsonResponse<Shop>> volleyWrapper = new VolleyWrapper<>(null, responseType, Request.Method.GET, url,
                callback, getContext(), mActivity.getmSnackBarDisplayer(), mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();
    }

    private void initUiFields(View view) {
        rivShopMainPhoto = (RoundedImageView) view.findViewById(R.id.rivShopMainPhoto);
        ivFavShop = (ImageView) view.findViewById(R.id.ivFav);
        rbShopRate = (RatingBar) view.findViewById(R.id.rbShopRate);
        tvRatingValue = (TextView) view.findViewById(R.id.tvRatingValue);
        tvShopName = (TextView) view.findViewById(R.id.tvShopName);
        tvAddress = (TextView) view.findViewById(R.id.tvAddress);
        tvNearLocation = (TextView) view.findViewById(R.id.tvNearLocation);
        ivToolbarBack = (ImageView) view.findViewById(R.id.ivToolbarBack);
        tvToolbarShopName = (TextView) view.findViewById(R.id.tvToolbarShopName);
        tvNumberOfReviews = (TextView) view.findViewById(R.id.tvNumberOfReviews);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);

        mActivity.setSearchButtonAsChecked();

        fillUiFields();
    }

    private void fillUiFields() {
        Log.d(TAG, "fillUiFields - Start");

        Glide.with(mActivity)
                .load(mShop.getMainPhotoURL())
                .asBitmap()
                .placeholder(new ColorDrawable(mActivity.getResources().getColor(R.color.light_gray)))
                .into(rivShopMainPhoto);

        DecimalFormat df = new DecimalFormat("0.0");
        tvRatingValue.setText(df.format(mShop.getRate()));
        Log.d(TAG, "shop Rate in - fillUiFields : " + mShop.getRate());
        rbShopRate.setRating((float) mShop.getRate());

        if (!mActivity.getFavSet().isEmpty() && mActivity.getFavSet().contains(mShop.getId())) {
            ivFavShop.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.btn_fav_prassed));
        } else {
            ivFavShop.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_fav));
        }

        tvShopName.setText(mShop.getName());
        tvAddress.setText(mShop.getLocation().getAddress());
        tvNearLocation.setVisibility(View.GONE);
        tvToolbarShopName.setText(mShop.getName());

        tvNumberOfReviews.setText("(" + mShop.getNumOfReviews() + ")");

        Log.d(TAG, "fillUiFields - end");
    }

    public void updateTotalRateBar() {
        Log.d(TAG, "updateTotalRateBar - Rate Value: " + mShop.getRate());
        DecimalFormat df = new DecimalFormat("0.0");
        tvRatingValue.setText(df.format(mShop.getRate()));
        rbShopRate.setRating((float) mShop.getRate());
        tvNumberOfReviews.setText("(" + mShop.getNumOfReviews() + ")");
    }

    public void setupListener() {
        Log.d(TAG, "setupListener - Start");

        ivFavShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListener - ivFavShop - icon pressed");
                Drawable iconPressed = getResources().getDrawable(R.drawable.btn_fav_prassed);
                Drawable iconNotPressed = getResources().getDrawable(R.drawable.ic_fav);

                if (!mActivity.getFavSet().isEmpty() && mActivity.getFavSet().contains(mShop.getId())) {
                    Log.d(TAG, "setupListener - ivFavShop - change to not pressed");
                    mActivity.removeFromFavourite(mShop, null);
                    ivFavShop.setImageDrawable(iconNotPressed);
                } else {
                    Log.d(TAG, "setupListener - ivFavShop - change to pressed");
                    mActivity.addToFavourite(mShop, null);
                    ivFavShop.setImageDrawable(iconPressed);
                }
            }
        });

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentManager.popBackStack();
            }
        });

        Log.d(TAG, "setupListener - end");
    }

    private void setupViewPager(ViewPager viewPager) {
        Log.d(TAG, "setupViewPager - Start");

        FragmentViewPagerAdapter adapter = new FragmentViewPagerAdapter(getChildFragmentManager());

        DetailsTabFragment detailsTabFragment = new DetailsTabFragment();
        GalleryTabFragment galleryTabFragment = new GalleryTabFragment();
        ReviewsTabFragment reviewsTabFragment = new ReviewsTabFragment();

        Log.d(TAG, "setupViewPager - mShop" + mShop.getName());

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
