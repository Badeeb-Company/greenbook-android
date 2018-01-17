package com.badeeb.greenbook.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.adaptors.ShopRecyclerViewAdapter;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.models.ShopInquiry;
import com.badeeb.greenbook.network.NonAuthorizedCallback;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.LocationFinder;
import com.badeeb.greenbook.shared.OnPermissionsGrantedHandler;
import com.badeeb.greenbook.shared.PermissionsChecker;
import com.badeeb.greenbook.shared.UiUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.reflect.TypeToken;

import org.parceler.Parcels;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ShopListResultFragment extends Fragment {
    public final static String TAG = ShopListResultFragment.class.getName();

    public final static String EXTRA_SELECTED_ADDRESS = "EXTRA_SELECTED_ADRESS";
    public final static String EXTRA_SELECTED_LATITUDE = "EXTRA_SELECTED_LATITUDE";
    public final static String EXTRA_SELECTED_LONGITUDE = "EXTRA_SELECTED_LONGITUDE";

    public final static String MODE_INITIAL = "MODE_INITIAL";
    public final static String MODE_CURRENT_LOCATION = "MODE_CURRENT_LOCATION";
    public final static String MODE_OTHER_LOCATION = "MODE_OTHER_LOCATION";


    // Class Attributes
    private MainActivity mActivity;
    private ProgressDialog mProgressDialog;
    private List<Shop> mShopList;
    private boolean initialMode = true;

    // search parameters
    private double mLatitude;
    private double mLongitude;

    // UI Fields
    private RecyclerView rvShopList;
    private ShopRecyclerViewAdapter mShopListAdaptor;
    private SwipeRefreshLayout srlShopList;
    private EditText etSearch;
    private ImageView ivBack;
    private TextView tvLocationSearch;
    private TextView tvFirstSearch;

    private ImageView ivMap;
    private LinearLayout llEmptyResult;
    private LocationFinder locationFinder;

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
        View view = inflater.inflate(R.layout.fragment_shop_list_result, container, false);

        init(view);

        mActivity.showBottomNavigationActionBar();

        locationFinder = new LocationFinder(mActivity, this) {
            protected void onLocationFound() {
                if(!customLocationSelected()){
                    mLatitude = locationFinder.getCurrentLocation().getLatitude();
                    mLongitude = locationFinder.getCurrentLocation().getLongitude();
                }
                initialMode = false;
                callSearchApi();
            }

            protected void onLocationNotFound() {
                mActivity.getmSnackBarDisplayer().displayError("Your location cannot be detected!");
            }
        };

        return view;
    }

    private boolean customLocationSelected(){
        return !TextUtils.isEmpty(tvLocationSearch.getText().toString());
    }

    public Location getCurrentLocation(){
        return locationFinder.getCurrentLocation();
    }

    private void init(View view) {
        mActivity = (MainActivity) getActivity();
        mProgressDialog = UiUtils.createProgressDialog(mActivity);
        mActivity.hideToolbar();
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mShopList = new ArrayList<>();

        initUi(view);

        loadBundleData();

        setupListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(initialMode){
            UiUtils.hide(rvShopList);
            UiUtils.hide(llEmptyResult);
            UiUtils.show(tvFirstSearch);
        } else {
            locationFinder.find();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LocationFinder.PERM_LOCATION_RQST_CODE:
                if(PermissionsChecker.permissionsGranted(grantResults)){
                    locationFinder.find();
                }
                break;
        }
    }

    private void loadBundleData() {
        if (getArguments() == null) {
            return;
        }

        String searchAddress = getArguments().getString(EXTRA_SELECTED_ADDRESS);
        double lat = getArguments().getDouble(EXTRA_SELECTED_LATITUDE);
        double lng = getArguments().getDouble(EXTRA_SELECTED_LONGITUDE);

        if (!TextUtils.isEmpty(searchAddress)) {
            tvLocationSearch.setText(searchAddress);
            mLatitude = lat;
            mLongitude = lng;

        }
        initialMode = false;
    }

    private void initUi(View view) {
        rvShopList = (RecyclerView) view.findViewById(R.id.rvShopList);
        RecyclerView.LayoutManager mShopLayoutManager = new LinearLayoutManager(mActivity);
        rvShopList.setLayoutManager(mShopLayoutManager);
        rvShopList.setItemAnimator(new DefaultItemAnimator());

        mShopListAdaptor = new ShopRecyclerViewAdapter(mActivity, mShopList, this);
        rvShopList.setAdapter(mShopListAdaptor);


        etSearch = view.findViewById(R.id.etSearch);
        tvFirstSearch = view.findViewById(R.id.tvFirstSearch);

        srlShopList = (SwipeRefreshLayout) view.findViewById(R.id.shopList_form);

        tvLocationSearch = (TextView) view.findViewById(R.id.tvLocationSearch);

        ivBack = (ImageView) view.findViewById(R.id.ivBack);
        ivMap = (ImageView) view.findViewById(R.id.ivMap);

        llEmptyResult = (LinearLayout) view.findViewById(R.id.llEmptyResult);

        mActivity.setSearchButtonAsChecked();
    }

    private void setupListener() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        ivMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMapFragment();
            }
        });
        srlShopList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                locationFinder.find();
            }
        });

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    locationFinder.find();
                    return true;
                }
                return false;
            }
        });


        tvLocationSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goPlaceFilter();
            }
        });
    }

    private void goPlaceFilter() {
        Log.d(TAG, "goPlaceFilter - Start");

        PlaceFilterFragment placeFilterFragment = new PlaceFilterFragment();

        Bundle bundle = new Bundle();
        placeFilterFragment.setArguments(bundle);


        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, placeFilterFragment, placeFilterFragment.TAG);

        fragmentTransaction.addToBackStack(TAG);

        fragmentTransaction.commit();

        Log.d(TAG, "goPlaceFilter - End");
    }

    private void callSearchApi() {
        UiUtils.hideKeyboardIfShown(mActivity);
        mProgressDialog.show();

        String queryText = etSearch.getText().toString();

        String url = Constants.BASE_URL + "/shops/search?query=" + queryText
                + "&lat=" + mLatitude + "&lng=" + mLongitude;

        NonAuthorizedCallback<JsonResponse<ShopInquiry>> callback = new NonAuthorizedCallback<JsonResponse<ShopInquiry>>() {
            @Override
            public void onSuccess(JsonResponse<ShopInquiry> jsonResponse) {
                mShopList.clear();
                if (jsonResponse != null
                        && jsonResponse.getResult() != null
                        && jsonResponse.getResult().getShopList() != null
                        && !jsonResponse.getResult().getShopList().isEmpty()) {

                    mShopList.addAll(jsonResponse.getResult().getShopList());
                    mShopListAdaptor.notifyDataSetChanged();
                }
                mProgressDialog.dismiss();
                srlShopList.setRefreshing(false);

                if (mShopList != null && mShopList.size() == 0) {
                    enableNoSearchFoundScreen();
                } else {
                    disableNoSearchFoundScreen();
                }

            }

            @Override
            public void onError() {
                Log.d(TAG, "callSearchApi - NonAuthorizedCallback - onError");
                mActivity.getmSnackBarDisplayer().displayError("Error while getting shops from the server");
                mProgressDialog.dismiss();
                srlShopList.setRefreshing(false);
            }
        };

        Type responseType = new TypeToken<JsonResponse<ShopInquiry>>() {
        }.getType();

        VolleyWrapper<Object, JsonResponse<ShopInquiry>> volleyWrapper = new VolleyWrapper<>(null, responseType, Request.Method.GET, url,
                callback, getContext(), mActivity.getmSnackBarDisplayer(), mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();

        Log.d(TAG, "callSearchApi - Start");
    }

    public void enableNoSearchFoundScreen() {
        UiUtils.hide(rvShopList);
        UiUtils.hide(tvFirstSearch);
        UiUtils.show(llEmptyResult);
    }

    public void disableNoSearchFoundScreen() {
        UiUtils.show(rvShopList);
        UiUtils.hide(tvFirstSearch);
        UiUtils.hide(llEmptyResult);
    }

    // callled by the listener in ShopRecyclerViewAdapter
    public void goToSelectedShop(int position) {

        Shop selectShop = mShopList.get(position);
        mActivity.hideBottomNavigationActionBar();
        ShopDetailsFragment shopDetailsFragment = new ShopDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(ShopDetailsFragment.EXTRA_SHOP_OBJECT, Parcels.wrap(selectShop));
        shopDetailsFragment.setArguments(bundle);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, shopDetailsFragment, shopDetailsFragment.TAG);

        fragmentTransaction.addToBackStack(TAG);


        fragmentTransaction.commit();


        Log.d(TAG, "goToSelectedShop - End");
    }

    public void addToFavourite(int position) {
        Log.d(TAG, "addToFavourite - Start");

        mActivity.addToFavourite(mShopList.get(position), null);

        Log.d(TAG, "addToFavourite - End");
    }

    public void removeFromFavourite(int position) {
        Log.d(TAG, "removeFromFavourite - Start");

        mActivity.removeFromFavourite(mShopList.get(position), null);

        Log.d(TAG, "removeFromFavourite - End");
    }


    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    private void goToMapFragment() {

        MapFragment mapFragment = new MapFragment();

        Bundle bundle = new Bundle();
        bundle.putDouble(MapFragment.EXTRA_CURRENT_LATITUDE, mLatitude);
        bundle.putDouble(MapFragment.EXTRA_CURRENT_LONGITUDE, mLongitude);
        bundle.putString(MapFragment.EXTRA_SEARCH_KEYWORD, etSearch.getText().toString());
        bundle.putParcelable(MapFragment.EXTRA_SHOPS_LIST, Parcels.wrap(mShopList));
        mapFragment.setArguments(bundle);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, mapFragment, mapFragment.TAG);
        fragmentTransaction.addToBackStack(TAG);
        fragmentTransaction.commit();

    }

}
