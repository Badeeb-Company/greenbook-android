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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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

    private static final long FETCH_LOCATION_TIMEOUT = 10 * 1000;
    private static final int PERM_LOCATION_RQST_CODE = 100;

    public final static String EXTRA_SELECTED_ADDRESS = "EXTRA_SELECTED_ADRESS";
    public final static String EXTRA_SELECTED_LATITUDE = "EXTRA_SELECTED_LATITUDE";
    public final static String EXTRA_SELECTED_LONGITUDE = "EXTRA_SELECTED_LONGITUDE";

    // Class Attributes
    private MainActivity mActivity;
    private ProgressDialog mProgressDialog;
    private List<Shop> mShopList;

    // search parameters
    private double mLatitude;
    private double mLongitude;
    private boolean isLocationSelected;

    // UI Fields
    private RecyclerView rvShopList;
    private ShopRecyclerViewAdapter mShopListAdaptor;
    private SwipeRefreshLayout srlShopList;
    private EditText etSearch;
    private ImageView ivBack;
    private TextView tvLocationSearch;

    private ImageView ivMap;
    private LinearLayout llEmptyResult;

    private OnPermissionsGrantedHandler onLocationPermissionGrantedHandler;
    private AlertDialog locationDisabledWarningDialog;
    private GoogleApiClient mGoogleApiClient;
    private TimerTask cancelFetchLocationTask;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private LocationChangeReceiver locationChangeReceiver;

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

        return view;
    }

    private void init(View view) {
        mActivity = (MainActivity) getActivity();
        mProgressDialog = UiUtils.createProgressDialog(mActivity);
        mActivity.hideToolbar();
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mShopList = new ArrayList<>();

        initUi(view);

        loadBundleData();

        // location preparation
        onLocationPermissionGrantedHandler = createOnLocationPermissionGrantedHandler();
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        locationChangeReceiver = new LocationChangeReceiver();
        locationListener = createLocationListener();

        if (isLocationSelected) {
            goSearch();
        } else {
            mActivity.connectPlaceGoogleApiClient();
        }

        setupListener();
    }

    private LocationListener createLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (cancelFetchLocationTask != null) {
                    cancelFetchLocationTask.cancel();
                }
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                mActivity.setCurrentLocation(location);
                onLocationFound();
            }
        };
    }

    private void loadBundleData() {
        if (getArguments() == null) {
            return;
        }

        String searchAddress = getArguments().getString(EXTRA_SELECTED_ADDRESS);
        double lat = getArguments().getDouble(EXTRA_SELECTED_LATITUDE);
        double lng = getArguments().getDouble(EXTRA_SELECTED_LONGITUDE);

        if (!"".equals(searchAddress)) {
            Log.d(TAG, "location address search EXTRA: " + searchAddress);
            tvLocationSearch.setText(searchAddress);
            mLatitude = lat;
            mLongitude = lng;
            isLocationSelected = true;
            Log.d(TAG, "location address search EXTRA - lat: " + mLatitude + " - lng: " + mLongitude);
        }
    }

    private void onLocationFound() {
        // Go to next fragment
//        goToShopListResultFragment();
        mProgressDialog.dismiss();
        goSearch();
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

    private void initUi(View view) {
        rvShopList = (RecyclerView) view.findViewById(R.id.rvShopList);
        RecyclerView.LayoutManager mShopLayoutManager = new LinearLayoutManager(mActivity);
        rvShopList.setLayoutManager(mShopLayoutManager);
        rvShopList.setItemAnimator(new DefaultItemAnimator());

        mShopListAdaptor = new ShopRecyclerViewAdapter(mActivity, mShopList, this);
        rvShopList.setAdapter(mShopListAdaptor);


        etSearch = view.findViewById(R.id.etSearch);

        srlShopList = (SwipeRefreshLayout) view.findViewById(R.id.shopList_form);

        tvLocationSearch = (TextView) view.findViewById(R.id.tvLocationSearch);

        ivBack = (ImageView) view.findViewById(R.id.ivBack);
        ivMap = (ImageView) view.findViewById(R.id.ivMap);

        llEmptyResult = (LinearLayout) view.findViewById(R.id.llEmptyResult);

        mActivity.setSearchButtonAsChecked();
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
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            if (locationDisabledWarningDialog == null || !locationDisabledWarningDialog.isShowing()) {
                showGPSDisabledWarningDialog();
                getActivity().registerReceiver(locationChangeReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
            }
        } else {
            if (locationDisabledWarningDialog != null && locationDisabledWarningDialog.isShowing()) {
                // This part is only used to dismiss alert dialog when GPS is enabled but we don't get location in same time.
                Log.d(TAG, "checkLocationService - showGPSDisabledWarningDialog - Dismiss");
                locationDisabledWarningDialog.dismiss();
            } else {
                // Get current location
                initGoogleApiClient();
                onFindingLocation();
            }
        }

        Log.d(TAG, "checkLocationService - End");

        return gpsEnabled;
    }

    @SuppressWarnings({"MissingPermission"})
    private void onFindingLocation() {
        mProgressDialog.show();
        disconnectGoogleApiClient();
        mGoogleApiClient.connect();
    }

    private void disconnectGoogleApiClient() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void initGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        fetchUserCurrentLocation();
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
    }

    /*
    We try to get the last known location if found then no problem, if not found then we
    will set current location to default location(Australia) and register location update
    to get the current location whenever received.
 */
    @SuppressWarnings({"MissingPermission"})
    private void fetchUserCurrentLocation() {
        cancelFetchLocationTask = new TimerTask() {
            @Override
            public void run() {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationListener);
                mActivity.setCurrentLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mActivity.getCurrentLocation() == null) {
                            onLocationNotFound();
                        } else {
                            onLocationFound();
                        }
                    }
                });
            }
        };
        registerLocationUpdate();
        Timer timer = new Timer();
        timer.schedule(cancelFetchLocationTask, FETCH_LOCATION_TIMEOUT);
    }

    @SuppressWarnings({"MissingPermission"})
    protected void registerLocationUpdate() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setSmallestDisplacement(0);
        request.setInterval(0);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, locationListener);
    }

    private void onLocationNotFound() {
        mProgressDialog.dismiss();
        disconnectGoogleApiClient();
        UiUtils.showDialog(getContext(), R.style.DialogTheme, R.string.location_not_found, R.string.ok_btn_dialog, null);
    }

    private void setupListener() {
        PermissionsChecker.checkPermissions(ShopListResultFragment.this,
                onLocationPermissionGrantedHandler, PERM_LOCATION_RQST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION);

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
                goSearch();
            }
        });

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    UiUtils.hideKeyboardIfShown(mActivity);
                    goSearch();
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

    private void goCategoryFilter() {
        Log.d(TAG, "goCategoryFilter - Start");

        CategoryFilterFragment categoryFilterFragment = new CategoryFilterFragment();

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SELECTED_ADDRESS, tvLocationSearch.getText().toString());
        categoryFilterFragment.setArguments(bundle);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, categoryFilterFragment, categoryFilterFragment.TAG);

        fragmentTransaction.addToBackStack(TAG);


        fragmentTransaction.commit();

        Log.d(TAG, "goCategoryFilter - End");
    }


    private void goSearch() {
        mActivity.hideKeyboard();
        mProgressDialog.show();

        prepareSearchLocation();

        if (!isLocationSelected) {
            Log.d(TAG, "goSearch - location not selected");
            mProgressDialog.dismiss();
            mActivity.getmSnackBarDisplayer().displayError("Your location cannot be detected!");
            return;
        }

        callSearchApi();
    }

    private void callSearchApi() {
        String queryText = etSearch.getText().toString();

        String url = Constants.BASE_URL + "/shops/search?query=" + queryText
                + "&lat=" + mLatitude + "&lng=" + mLongitude;

        NonAuthorizedCallback<JsonResponse<ShopInquiry>> callback = new NonAuthorizedCallback<JsonResponse<ShopInquiry>>() {
            @Override
            public void onSuccess(JsonResponse<ShopInquiry> jsonResponse) {
                Log.d(TAG, "callSearchApi - NonAuthorizedCallback - onSuccess");

                if (jsonResponse != null
                        && jsonResponse.getResult() != null
                        && jsonResponse.getResult().getShopList() != null
                        && !jsonResponse.getResult().getShopList().isEmpty()) {

                    Log.d(TAG, "callSearchApi - NonAuthorizedCallback - onSuccess - shopList: "
                            + Arrays.toString(jsonResponse.getResult().getShopList().toArray()));
                    mShopList.clear();
                    mShopList.addAll(jsonResponse.getResult().getShopList());
                    Log.d(TAG, "callSearchApi - NonAuthorizedCallback - onSuccess - mShopList: "
                            + Arrays.toString(mShopList.toArray()));
                    mShopListAdaptor.notifyDataSetChanged();
                } else {
                    //switch to empty search page
                    Log.d(TAG, "callSearchApi - NonAuthorizedCallback - onSuccess - empty search ");
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
        srlShopList.setVisibility(View.GONE);
        llEmptyResult.setVisibility(View.VISIBLE);
    }

    public void disableNoSearchFoundScreen() {
        srlShopList.setVisibility(View.VISIBLE);
        llEmptyResult.setVisibility(View.GONE);
    }

    private void prepareSearchLocation() {
        String searchLocation = tvLocationSearch.getText().toString();
        if (searchLocation == null || searchLocation.isEmpty()) {
            fetchCurrentLocation();
        }
    }

    private void fetchCurrentLocation() {
        mLatitude = mActivity.getCurrentLocation().getLatitude();
        mLongitude = mActivity.getCurrentLocation().getLongitude();
        isLocationSelected = true;
    }


    // callled by the listener in ShopRecyclerViewAdapter
    public void goToSelectedShop(int position) {
        Log.d(TAG, "goToSelectedShop - Start");

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
        bundle.putParcelable(MapFragment.EXTRA_SHOPS_LIST, Parcels.wrap(mShopList));
        mapFragment.setArguments(bundle);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Animation part
//        fragmentTransaction.setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out,
//                R.animator.card_flip_left_in, R.animator.card_flip_left_out);

        fragmentTransaction.replace(R.id.main_frame, mapFragment, mapFragment.TAG);
        fragmentTransaction.addToBackStack(TAG);
        fragmentTransaction.commit();

    }

    private final class LocationChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                Log.d(TAG, "LocationChangeReceiver - onReceive - Start");

                checkLocationService();
                getActivity().unregisterReceiver(this);

                Log.d(TAG, "LocationChangeReceiver - onReceive - End");
            }
        }
    }

}
