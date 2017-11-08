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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.adaptors.PlaceAutocompleteAdapter;
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
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.*;
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
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 100;

    private MainActivity mActivity;

    private List<Category> mCategoryList;
    private Category mSelectedCategory;
    private String mSelectedPlaceName;
    private double mSelectedPlacesLatitude;
    private double mSelectedPlaceLongitude;

    // UI Fields
    private RecyclerView rvCategoryList;
    private CategoryRecyclerViewAdapter mCategoryListAdaptor;
    private SwipeRefreshLayout srlCategoryList;
    private AutoCompleteTextView actvLocationSearch;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;

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

        if (container != null) {
            // this code is used to prevent fragment overlapping
            container.removeAllViews();
        }

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

        mActivity.connectPlaceGoogleApiClient();

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

        actvLocationSearch = (AutoCompleteTextView) view.findViewById(R.id.actvLocation);
        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(mActivity,mActivity.getmPlaceGoogleApiClient(),Constants.BOUNDS_MIDDLE_EAST, null);
        actvLocationSearch.setAdapter(mPlaceAutocompleteAdapter);

        mActivity.setSearchButtonAsChecked();
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

        actvLocationSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "actvLocationSearch - setOnItemClickListener - start ");
                final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
                final String placeId = item.getPlaceId();
                final CharSequence primaryText = item.getPrimaryText(null);

                Log.i(TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mActivity.getmPlaceGoogleApiClient(), placeId);
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

                Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
                Log.d(TAG, "actvLocationSearch - setOnItemClickListener - end ");
            }
        });

        Log.d(TAG, "setupListeners - Start");
    }

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            Log.d(TAG, "mUpdatePlaceDetailsCallback - onResult - start ");
            if (!places.getStatus().isSuccess()) {
                Log.d(TAG, "mUpdatePlaceDetailsCallback - onResult - status not success ");
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            Log.i(TAG, "Place details received: " + place.getName());

            mSelectedPlaceName = place.getName().toString();
            mSelectedPlacesLatitude = place.getLatLng().latitude;
            mSelectedPlaceLongitude = place.getLatLng().longitude;

            Log.i(TAG, "location after autocomplete - lat: " + mSelectedPlacesLatitude+" - lng: "+mSelectedPlaceLongitude);

            places.release();

            Log.d(TAG, "mUpdatePlaceDetailsCallback - onResult - end ");
        }
    };

    private void goToShopListResultFragment(){
        Log.d(TAG, "goToShopResultListFragment - Start");
        Log.d(TAG, "mSelectedPlace: "+mSelectedPlaceName);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ShopListResultFragment.EXTRA_SELECTED_CATEGORY, Parcels.wrap(mSelectedCategory));
        bundle.putString(ShopListResultFragment.EXTRA_SELECTED_ADRESS, mSelectedPlaceName);
        bundle.putParcelable(ShopListResultFragment.EXTRA_SELECTED_CATEGORY_LIST, Parcels.wrap(mCategoryList));
        bundle.putDouble(ShopListResultFragment.EXTRA_SELECTED_LATITUDE, mSelectedPlacesLatitude);
        bundle.putDouble(ShopListResultFragment.EXTRA_SELECTED_LONGITUDE, mSelectedPlaceLongitude);

        ShopListResultFragment shopListResultFragment = new ShopListResultFragment();
        shopListResultFragment.setArguments(bundle);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, shopListResultFragment, shopListResultFragment.TAG);

        fragmentTransaction.addToBackStack(TAG);

        fragmentTransaction.commit();

        mActivity.disconnectPlaceGoogleApiClient();

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
                        mProgressDialog.dismiss();
                    }

                }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getActivity(), "API client connection failed", Toast.LENGTH_LONG).show();
                        mProgressDialog.dismiss();
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


    @Override
    public void onDestroy() {
        mActivity.disconnectPlaceGoogleApiClient();
        super.onDestroy();
    }
}