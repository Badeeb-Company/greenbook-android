package com.badeeb.greenbook.shared;

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
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.fragments.ShopListResultFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by meldeeb on 1/17/18.
 */

public abstract class LocationFinder {
    private static final long FETCH_LOCATION_TIMEOUT = 10 * 1000;
    public static final int PERM_LOCATION_RQST_CODE = 100;

    private OnPermissionsGrantedHandler onLocationPermissionGrantedHandler;
    private AlertDialog locationDisabledWarningDialog;
    private GoogleApiClient mGoogleApiClient;
    private TimerTask cancelFetchLocationTask;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private LocationChangeReceiver locationChangeReceiver;
    private ProgressDialog mProgressDialog;
    private MainActivity mActivity;
    private ShopListResultFragment mFragment;

    private Location mCurrentLocation;

    public LocationFinder(MainActivity mActivity, ShopListResultFragment mFragment){
        this.mActivity = mActivity;
        this.mFragment = mFragment;
        mProgressDialog = UiUtils.createProgressDialog(mActivity);
        onLocationPermissionGrantedHandler = createOnLocationPermissionGrantedHandler();
        locationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        locationChangeReceiver = new LocationChangeReceiver();
        locationListener = createLocationListener();
        initGoogleApiClient();
    }

    public void find(){
        PermissionsChecker.checkPermissions(mFragment,
                onLocationPermissionGrantedHandler, PERM_LOCATION_RQST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
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
                mActivity.registerReceiver(locationChangeReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
            }
        } else {
            if (locationDisabledWarningDialog != null && locationDisabledWarningDialog.isShowing()) {
                locationDisabledWarningDialog.dismiss();
            } else {
                mProgressDialog.show();
                checkClientConnectionAndFetchCurrentLocation();
            }
        }

        return gpsEnabled;
    }

    private void checkClientConnectionAndFetchCurrentLocation() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        } else {
            fetchCurrentLocation();
        }
    }

    @SuppressWarnings({"MissingPermission"})
    private void fetchCurrentLocation() {
        cancelFetchLocationTask = new TimerTask() {
            @Override
            public void run() {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationListener);
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        if (mCurrentLocation == null) {
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
    private void registerLocationUpdate() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setSmallestDisplacement(0);
        request.setInterval(0);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, locationListener);
    }

    private LocationListener createLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mProgressDialog.dismiss();
                if (cancelFetchLocationTask != null) {
                    cancelFetchLocationTask.cancel();
                }
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                mCurrentLocation = location;
                onLocationFound();
            }
        };
    }

    private void showGPSDisabledWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.DialogTheme);
        builder.setTitle(R.string.GPS_disabled_warning_title);
        builder.setMessage(R.string.GPS_disabled_warning_msg);
        builder.setPositiveButton(R.string.ok_btn_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mActivity.startActivity(i);
            }
        });
        builder.setCancelable(false);
        locationDisabledWarningDialog = builder.create();
        locationDisabledWarningDialog.show();
    }

    private void initGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        fetchCurrentLocation();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Toast.makeText(mActivity, "API client connection suspended", Toast.LENGTH_LONG).show();
                        mProgressDialog.dismiss();
                    }

                }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(mActivity, "API client connection failed", Toast.LENGTH_LONG).show();
                        mProgressDialog.dismiss();
                    }
                })
                .build();
    }

    protected abstract void onLocationFound();
    protected abstract void onLocationNotFound();

    private final class LocationChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                checkLocationService();
                context.unregisterReceiver(this);
            }
        }
    }

}
