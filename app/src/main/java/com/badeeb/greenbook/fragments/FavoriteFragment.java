package com.badeeb.greenbook.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.adaptors.FavouriteRecyclerViewAdapter;
import com.badeeb.greenbook.models.FavouriteInquiry;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.models.ShopInquiry;
import com.badeeb.greenbook.network.AuthorizedCallback;
import com.badeeb.greenbook.network.NonAuthorizedCallback;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.AdapterNotifier;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.UiUtils;
import com.google.gson.reflect.TypeToken;

import org.parceler.Parcels;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoriteFragment extends Fragment {

    public static final String TAG = FavoriteFragment.class.getSimpleName();

    private MainActivity mActivity;
    private ProgressDialog mProgressDialog;
    private List<Shop> mShopList;

    private RecyclerView rvFavList;
    private FavouriteRecyclerViewAdapter mFavAdapter;
    private SwipeRefreshLayout srlFavList;
    private LinearLayout llEmptyFavourite;

    private AdapterNotifier mAdapterNotifier;

    public FavoriteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");
        if (container != null) {
            // this code is used to prevent fragment overlapping
            container.removeAllViews();
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);



        inti(view);

        Log.d(TAG, "onCreateView - End");

        return view;
    }

    private void inti(View view) {
        Log.d(TAG, "inti - Start");

        mActivity = (MainActivity) getActivity();
        mProgressDialog = UiUtils.createProgressDialog(mActivity);
        mShopList = mActivity.getFavShopList();

        rvFavList = (RecyclerView) view.findViewById(R.id.rvFavList) ;
        RecyclerView.LayoutManager mShopLayoutManager = new LinearLayoutManager(mActivity);
        rvFavList.setLayoutManager(mShopLayoutManager);
        rvFavList.setItemAnimator(new DefaultItemAnimator());

        mFavAdapter = new FavouriteRecyclerViewAdapter(mActivity,mShopList,this);
        rvFavList.setAdapter(mFavAdapter);

        mAdapterNotifier = createAdapterNotifier();

        srlFavList = (SwipeRefreshLayout) view.findViewById(R.id.favList_form) ;

        llEmptyFavourite = (LinearLayout) view.findViewById(R.id.llEmptyFavourite);

        SetupListener();



        Log.d(TAG, "inti - end");

    }

    private AdapterNotifier createAdapterNotifier() {
        return new AdapterNotifier(){

            @Override
            public void notifyAdapter() {
                mFavAdapter.notifyDataSetChanged();
            }
        };
    }

    private void SetupListener() {
        Log.d(TAG, "SetupListener - Start");


        Log.d(TAG, "SetupListener - end");
    }



    private void enableNoFavouriteFoundScreen(){
        srlFavList.setVisibility(View.GONE);
        llEmptyFavourite.setVisibility(View.VISIBLE);
    }

    private void disableNoFavouriteFoundScreen() {
        srlFavList.setVisibility(View.VISIBLE);
        llEmptyFavourite.setVisibility(View.GONE);
    }


    // callled by the listener in ShopRecyclerViewAdapter
    public void goToSelectedShop(int position) {
        Log.d(TAG, "goToSelectedShop - Start");

        Shop selectShop = mShopList.get(position);

        ShopDetailsFragment shopDetailsFragment = new ShopDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(ShopDetailsFragment.EXTRA_SHOP_OBJECT, Parcels.wrap(selectShop));
        shopDetailsFragment.setArguments(bundle);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, shopDetailsFragment, shopDetailsFragment.TAG);

        fragmentTransaction.addToBackStack(TAG);

        fragmentTransaction.commit();

        mActivity.disconnectPlaceGoogleApiClient();
        Log.d(TAG, "goToSelectedShop - End");
    }

    public void fetchSotredFavourite() {
        enableNoFavouriteFoundScreen();
        mProgressDialog.dismiss();
    }

    public void removeFavourite(Shop selectedShop){
        mActivity.removeFromFavourite(selectedShop,mAdapterNotifier);
    }

}
