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
import java.util.HashSet;
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



        init(view);

        Log.d(TAG, "onCreateView - End");

        return view;
    }

    private void init(View view) {
        Log.d(TAG, "inti - Start");

        mActivity = (MainActivity) getActivity();
        mProgressDialog = UiUtils.createProgressDialog(mActivity);

        mShopList = new ArrayList<>();

        rvFavList = (RecyclerView) view.findViewById(R.id.rvFavList) ;
        RecyclerView.LayoutManager mShopLayoutManager = new LinearLayoutManager(mActivity);
        rvFavList.setLayoutManager(mShopLayoutManager);
        rvFavList.setItemAnimator(new DefaultItemAnimator());

        mFavAdapter = new FavouriteRecyclerViewAdapter(mActivity, mShopList,this);
        rvFavList.setAdapter(mFavAdapter);

        mAdapterNotifier = createAdapterNotifier();

        srlFavList = (SwipeRefreshLayout) view.findViewById(R.id.favList_form) ;

        llEmptyFavourite = (LinearLayout) view.findViewById(R.id.llEmptyFavourite);

        mActivity.setFavoriteButtonAsChecked();
        mActivity.showBottomNavigationActionBar();

        prepareFavouriteShopList();

        adjustVisiablity();
        SetupListener();



        Log.d(TAG, "inti - end");

    }

    private void prepareFavouriteShopList() {
        callFavShopsApi();
    }


    private void adjustVisiablity() {
        if(mActivity.getFavSet().isEmpty()){
            enableNoFavouriteFoundScreen();
        }else {
            disableNoFavouriteFoundScreen();
        }
    }

    private AdapterNotifier createAdapterNotifier() {
        return new AdapterNotifier(){

            @Override
            public void notifyAdapter() {
                Log.d(TAG, "notifyAdapter - Start");

                Log.d(TAG, "notifyAdapter - Favourite Fragment FavShopList : "+Arrays.toString(mShopList.toArray()));

                if(!mActivity.getFavSet().isEmpty()){
                    for(int i = mShopList.size() - 1 ; i >= 0  ; i--){
                        Log.d(TAG, "inside check - shop: "+mShopList.get(i)+"");
                        if(!mActivity.getFavSet().contains(mShopList.get(i).getId())){
                            Log.d(TAG, "inside check - shop: "+mShopList.get(i)+" removed");
                            mShopList.remove(i);
                        }
                    }
                }
                mFavAdapter.notifyDataSetChanged();
//                mProgressDialog.dismiss();
                Log.d(TAG, "notifyAdapter - Start");
            }

            @Override
            public void notifyEmptyList() {
                mShopList.clear();
                mFavAdapter.notifyDataSetChanged();
                enableNoFavouriteFoundScreen();
                mProgressDialog.dismiss();
            }
        };
    }

    private void SetupListener() {
        Log.d(TAG, "SetupListener - Start");

        srlFavList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                prepareFavouriteShopList();
            }
        });

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

    private void callFavShopsApi() {
        Log.d(TAG, "callFavShopsApi - Start");

        if(mActivity.getFavSet().isEmpty()){
            Log.d(TAG, "callFavShopsApi - Start");
            enableNoFavouriteFoundScreen();
            return;
        }
        srlFavList.setRefreshing(true);
//        mProgressDialog.show();
        String url = Constants.BASE_URL + "/shops?ids=" ;

        for(String shopId: mActivity.getFavSet()){
            url += shopId + ",";
        }

        if(url.lastIndexOf(',') != -1) {
            url = url.substring(0, url.lastIndexOf(',')); // remove the last comma
        }

        Log.d(TAG, "callFavApi - Request URL: " + url);

        NonAuthorizedCallback<JsonResponse<ShopInquiry>> callback = new NonAuthorizedCallback<JsonResponse<ShopInquiry>>() {

            @Override
            public void onSuccess(JsonResponse<ShopInquiry> jsonResponse) {
                Log.d(TAG, "callFavApi - NonAuthorizedCallback - onSuccess");

                if (jsonResponse != null
                        && jsonResponse.getResult() != null
                        && jsonResponse.getResult().getShopList() != null
                        && !jsonResponse.getResult().getShopList().isEmpty()) {

                    Log.d(TAG, "callFavApi - NonAuthorizedCallback - onSuccess - shopList: "
                            + Arrays.toString(jsonResponse.getResult().getShopList().toArray()));

                    mShopList.clear();
                    mShopList.addAll(jsonResponse.getResult().getShopList());

                    mFavAdapter.notifyDataSetChanged();
                    disableNoFavouriteFoundScreen();

                } else {
                    //switch to empty search
                    enableNoFavouriteFoundScreen();
                    Log.d(TAG, "callFavApi - NonAuthorizedCallback - onSuccess - empty search ");
                }
//                mProgressDialog.dismiss();
                srlFavList.setRefreshing(false);
            }

            @Override
            public void onError() {
                Log.d(TAG, "callFavApi - NonAuthorizedCallback - onError");
//                mProgressDialog.dismiss();
                srlFavList.setRefreshing(false);
            }

        };

        Type responseType = new TypeToken<JsonResponse<ShopInquiry>>() {
        }.getType();

        VolleyWrapper<Object, JsonResponse<ShopInquiry>> volleyWrapper = new VolleyWrapper<>(null, responseType, Request.Method.GET, url,
                callback, mActivity, mActivity.getmSnackBarDisplayer(), mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();

        Log.d(TAG, "callFavApi - end");

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


    public void removeFavourite(int selectedShopIndex){
//        mProgressDialog.show();
        mActivity.removeFromFavourite(mShopList.get(selectedShopIndex),mAdapterNotifier);
//        mShopList.remove(selectedShopIndex);
        mFavAdapter.notifyDataSetChanged();
    }

}
