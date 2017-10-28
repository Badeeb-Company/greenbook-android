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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.listener.RecyclerItemClickListener;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.adaptors.CategoryRecyclerViewAdapter;
import com.badeeb.greenbook.models.Category;
import com.badeeb.greenbook.models.CategoryInquiry;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.network.NonAuthorizedCallback;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.Constants;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class ShopSearchFragment extends Fragment {

    public static final String TAG = ShopSearchFragment.class.getSimpleName();

    private MainActivity mActivity;

    private List<Category> mCategoryList;
    private Category mSelectedCategory;

    // UI Fields
    private RecyclerView rvCategoryList;
    private CategoryRecyclerViewAdapter mCategoryListAdaptor;
    private SwipeRefreshLayout srlCategoryList;
    private EditText etLocationSearch;

    // Location Attributes
    private static final int PERM_LOCATION_RQST_CODE = 100;
    private static final long FETCH_LOCATION_TIMEOUT = 10 * 1000; // last known location = null
    private OnPermissionsGrantedHandler onLocationPermissionGrantedHandler;
    private LocationManager locationManager;
    private AlertDialog locationDisabledWarningDialog;
    private LocationChangeReceiver locationChangeReceiver;
    private GoogleApiClient mGoogleApiClient;
    private TimerTask cancelFetchLocationTask;
    private LocationListener locationListener;
    private ProgressDialog mProgressDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shop_search, container, false);

        init(view);

        Log.d(TAG, "onCreateView - End");
        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - End");

        mActivity = (MainActivity) getActivity();
        mProgressDialog = UiUtils.createProgressDialog(mActivity);
        mActivity.showBottomNavigationActionBar();
        mActivity.hideToolbar();
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mCategoryList = new ArrayList<Category>();

        initUIComponents(view);

        setupListener();

        prepareCategoryList();

        // location preparation
        onLocationPermissionGrantedHandler = createOnLocationPermissionGrantedHandler();
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        locationChangeReceiver = new LocationChangeReceiver();
        locationListener = createLocationListener();

        Log.d(TAG, "init - End");
    }


    public void initUIComponents(View view) {
        rvCategoryList = (RecyclerView) view.findViewById(R.id.rvCategoryList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        rvCategoryList.setLayoutManager(mLayoutManager);
        rvCategoryList.setItemAnimator(new DefaultItemAnimator());

        mCategoryListAdaptor = new CategoryRecyclerViewAdapter(mActivity, mCategoryList);
        rvCategoryList.setAdapter(mCategoryListAdaptor);

        srlCategoryList = (SwipeRefreshLayout) view.findViewById(R.id.category_form);
        srlCategoryList.setVisibility(View.VISIBLE);

        etLocationSearch = (EditText) view.findViewById(R.id.etLocationSearch);
    }

    public void setupListener() {
        Log.d(TAG, "setupListeners - Start");

        srlCategoryList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "setupListeners - srlCategoryList:onItemClick - Start");

                prepareCategoryList();

                Log.d(TAG, "setupListeners - srlCategoryList:onItemClick - Start");
            }
        });

        rvCategoryList.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Log.d(TAG, "setupListeners - rvCategoryList:onItemClick - Start");

                        mSelectedCategory = mCategoryList.get(position);

                        PermissionsChecker.checkPermissions(ShopSearchFragment.this, onLocationPermissionGrantedHandler,
                                PERM_LOCATION_RQST_CODE, Manifest.permission.ACCESS_FINE_LOCATION);

                        Log.d(TAG, "setupListeners - rvCategoryList - End");
                    }
                })
        );
        Log.d(TAG, "setupListeners - Start");
    }

    private void goToShopListResultFragment(){
        Log.d(TAG, "goToShopResultListFragment - Start");

        Bundle bundle = new Bundle();
        bundle.putParcelable(ShopListResultFragment.EXTRA_SELECTED_CATEGORY, Parcels.wrap(mSelectedCategory));
        bundle.putString(ShopListResultFragment.EXTRA_SELECTED_ADRESS, etLocationSearch.getText().toString());

        ShopListResultFragment shopListResultFragment = new ShopListResultFragment();
        shopListResultFragment.setArguments(bundle);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, shopListResultFragment, shopListResultFragment.TAG);

        fragmentTransaction.addToBackStack(TAG);

        fragmentTransaction.commit();

        Log.d(TAG, "goToShopResultListFragment - End");
    }

    private void prepareCategoryList() {
        mProgressDialog.show();
        callCategoryListApi();
    }

    private void callCategoryListApi() {
        Log.d(TAG, "callCategoryListApi - Start");
        String url = Constants.BASE_URL + "/categories";

        Log.d(TAG, "callCategoryListApi - url: " + url);

        NonAuthorizedCallback<JsonResponse<CategoryInquiry>> callback = new NonAuthorizedCallback<JsonResponse<CategoryInquiry>>() {
            @Override
            public void onSuccess(JsonResponse<CategoryInquiry> jsonResponse) {
                Log.d(TAG, "callCategoryListApi - onSuccess - Start");

                if (jsonResponse != null && jsonResponse.getResult() != null && jsonResponse.getResult().getCategoryList() != null) {
                    mCategoryList.clear();
                    mCategoryList.addAll(jsonResponse.getResult().getCategoryList());
                    mCategoryListAdaptor.notifyDataSetChanged();

                    Log.d(TAG, "callCategoryListApi - onSuccess - mCategoryList: "+ Arrays.toString(mCategoryList.toArray()));

                } else {
                    mActivity.getmSnackBarDisplayer().displayError("Categories not loaded from the server");
                }

                mProgressDialog.dismiss();
                srlCategoryList.setRefreshing(false);

                Log.d(TAG, "callCategoryListApi - onSuccess - End");
            }

            @Override
            public void onError() {
                Log.d(TAG, "callCategoryListApi - onError - Start");

                mActivity.getmSnackBarDisplayer().displayError("Error loading categories from the server");

                mProgressDialog.dismiss();
                srlCategoryList.setRefreshing(false);

                Log.d(TAG, "callCategoryListApi - onError - End");
            }
        };

        // Prepare response type
        Type responseType = new TypeToken<JsonResponse<CategoryInquiry>>() {
        }.getType();

        VolleyWrapper<Object, JsonResponse<CategoryInquiry>> volleyWrapper = new VolleyWrapper<>(null, responseType, Request.Method.GET, url,
                callback, getContext(), mActivity.getmSnackBarDisplayer(), mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();
        Log.d(TAG, "callCategoryListApi - End");
    }

    //----------------------------------------------------------------------------------------------

    private final class LocationChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)){
                Log.d(TAG, "LocationChangeReceiver - onReceive - Start");

                checkLocationService();
                getActivity().unregisterReceiver(this);

                Log.d(TAG, "LocationChangeReceiver - onReceive - End");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult - Start");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERM_LOCATION_RQST_CODE:
                if(PermissionsChecker.permissionsGranted(grantResults)){
                    onLocationPermissionGrantedHandler.onPermissionsGranted();
                }
                break;
        }

        Log.d(TAG, "onRequestPermissionsResult - End");
    }

    private OnPermissionsGrantedHandler createOnLocationPermissionGrantedHandler() {

        return new OnPermissionsGrantedHandler() {
            @Override
            public void onPermissionsGranted() {
                checkLocationService();
            }
        };

    }

    private boolean checkLocationService() {
        Log.d(TAG, "checkLocationService - Start");

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            if (locationDisabledWarningDialog == null || !locationDisabledWarningDialog.isShowing()) {
                Log.d(TAG, "checkLocationService - showGPSDisabledWarningDialog");
                showGPSDisabledWarningDialog();
                getActivity().registerReceiver(locationChangeReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
            }
        } else {
            if (locationDisabledWarningDialog != null && locationDisabledWarningDialog.isShowing()) {
                // This part is only used to dismiss alert dialog when GPS is enabled but we don't get location in same time.
                Log.d(TAG, "checkLocationService - showGPSDisabledWarningDialog - Dismiss");
                locationDisabledWarningDialog.dismiss();
            }
            else {
                // Get current location
                Log.d(TAG, "checkLocationService - Get current location");
                initGoogleApiClient();
                onFindingLocation();
            }
        }

        Log.d(TAG, "checkLocationService - End");

        return gpsEnabled;
    }

    private void showGPSDisabledWarningDialog() {

        Log.d(TAG, "showGPSDisabledWarningDialog - Start");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
        builder.setTitle(R.string.GPS_disabled_warning_title);
        builder.setMessage(R.string.GPS_disabled_warning_msg);
        builder.setPositiveButton(R.string.ok_btn_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        });
        builder.setCancelable(false);
        locationDisabledWarningDialog = builder.create();
        locationDisabledWarningDialog.show();

        Log.d(TAG, "showGPSDisabledWarningDialog - End");
    }

    @SuppressWarnings({"MissingPermission"})
    private void onFindingLocation() {
        Log.d(TAG, "onFindingLocation - Start");
        mProgressDialog.show();
        disconnectGoogleApiClient();
        mGoogleApiClient.connect();
        Log.d(TAG, "onFindingLocation - End");
    }

    private void initGoogleApiClient() {

        Log.d(TAG, "initGoogleApiClient - Start");

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "initGoogleApiClient - onConnected - Start");
                        fetchUserCurrentLocation();
                        Log.d(TAG, "initGoogleApiClient - onConnected - End");
                    }
                    @Override
                    public void onConnectionSuspended(int i) {
                        Toast.makeText(getActivity(), "API client connection suspended", Toast.LENGTH_LONG).show();
                    }

                }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getActivity(), "API client connection failed", Toast.LENGTH_LONG).show();
                    }
                })
                .build();

        Log.d(TAG, "initGoogleApiClient - End");
    }

    /*
    We try to get the last known location if found then no problem, if not found then we
    will set current location to default location(Australia) and register location update
    to get the current location whenever received.
 */
    @SuppressWarnings({"MissingPermission"})
    private void fetchUserCurrentLocation() {

        Log.d(TAG, "fetchUserCurrentLocation - Start");

        cancelFetchLocationTask = new TimerTask() {
            @Override
            public void run() {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationListener);
                mActivity.setCurrentLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mActivity.getCurrentLocation() == null) {
                            Log.d(TAG, "fetchUserCurrentLocation - run - onLocationNotFound");
                            onLocationNotFound();
                        } else {
                            Log.d(TAG, "fetchUserCurrentLocation - run - onLocationFound");
                            onLocationFound();
                        }
                    }
                });
            }
        };
        registerLocationUpdate();
        Timer timer = new Timer();
        timer.schedule(cancelFetchLocationTask, FETCH_LOCATION_TIMEOUT);

        Log.d(TAG, "fetchUserCurrentLocation - End");
    }

    @SuppressWarnings({"MissingPermission"})
    protected void registerLocationUpdate() {
        Log.d(TAG, "registerLocationUpdate - Start");

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setSmallestDisplacement(0);
        request.setInterval(0);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, locationListener);

        Log.d(TAG, "registerLocationUpdate - End");
    }

    private void onLocationFound() {
        Log.d(TAG, "onLocationFound - Start");

        // Go to next fragment
        goToShopListResultFragment();

        mProgressDialog.dismiss();

        Log.d(TAG, "onLocationFound - End");
    }

    private void onLocationNotFound() {
        Log.d(TAG, "onLocationNotFound - Start");
        mProgressDialog.dismiss();
        disconnectGoogleApiClient();

        UiUtils.showDialog(getContext(), R.style.DialogTheme, R.string.location_not_found, R.string.ok_btn_dialog, null);
        Log.d(TAG, "onLocationNotFound - End");
    }

    private void disconnectGoogleApiClient(){
        Log.d(TAG, "disconnectGoogleApiClient - Start");

        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }

        Log.d(TAG, "disconnectGoogleApiClient - End");
    }

    private LocationListener createLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged - Start");

                if(cancelFetchLocationTask != null){
                    cancelFetchLocationTask.cancel();
                }
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                mActivity.setCurrentLocation(location);
                onLocationFound();

                Log.d(TAG, "onLocationChanged - End");
            }
        };
    }
    //----------------------------------------------------------------------------------------------
}