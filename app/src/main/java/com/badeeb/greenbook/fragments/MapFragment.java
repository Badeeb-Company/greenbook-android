package com.badeeb.greenbook.fragments;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.models.Category;
import com.badeeb.greenbook.models.Shop;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.parceler.Parcels;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {

    public final static String TAG = MapFragment.class.getName();

    public final static String EXTRA_SHOPS_LIST = "EXTRA_SHOPS_LIST";
    public final static String EXTRA_CURRENT_LATITUDE = "EXTRA_CURRENT_LATITUDE";
    public final static String EXTRA_CURRENT_LONGITUDE = "EXTRA_CURRENT_LONGITUDE";
    public final static String HOME_MARKER_TITLE = "Search Location";

    private OnMapReadyCallback mOnMapReadyCallback;
    private GoogleMap mMap;
    private MapView mMapView;

    private double mLatitude;      // Current Latitude
    private double mLongitude;      // Current Longitude

    private List<Shop> mShopsList;

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        init(view, savedInstanceState);

        Log.d(TAG, "onCreateView - End");

        return view;
    }

    private void init(View view, Bundle savedInstanceState) {
        Log.d(TAG, "init - Start");

        loadBundleData();

        mOnMapReadyCallback = createOnMapReayCallback();

//        markerInfoLayout = (LinearLayout) view.findViewById(R.id.marker_info_layout);

        // Map Initialization
        MapsInitializer.initialize(getContext());
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(mOnMapReadyCallback);

        Log.d(TAG, "init - End");
    }

    private OnMapReadyCallback createOnMapReayCallback() {
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "onMapReady - Start");

                LatLng currentLocation = new LatLng(mLatitude, mLongitude);

                mMap = googleMap;

                drawCurrentLocationOnMap(currentLocation);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

                drawShopsOnMap();

                // to adjust the zoom and the map options
                initCamera(mMap);

                Log.d(TAG, "onMapReady - End");
            }
        };
    }


    private void drawCurrentLocationOnMap(LatLng currentLocation) {

        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.cir_location);
        Bitmap b = bitmapdraw.getBitmap();
//                Bitmap curLocIcon = Bitmap.createScaledBitmap(b, 200, 200, false);

        MarkerOptions currentLocationMarker = new MarkerOptions();
        currentLocationMarker.position(currentLocation);
        currentLocationMarker.title(HOME_MARKER_TITLE);
        currentLocationMarker.icon(BitmapDescriptorFactory.fromBitmap(b));
        currentLocationMarker.anchor(0.5f, 0.5f);

        mMap.addMarker(currentLocationMarker);
    }

    private void drawShopsOnMap() {

        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_map_location);
        Bitmap b = bitmapdraw.getBitmap();
//                Bitmap curLocIcon = Bitmap.createScaledBitmap(b, 200, 200, false);

        for (int i = 0; i < mShopsList.size(); i++) {
            MarkerOptions marker = new MarkerOptions();
            marker.position(new LatLng(mShopsList.get(i).getLocation().getLat(), mShopsList.get(i).getLocation().getLng()));
            marker.title(i+"");
            marker.icon(BitmapDescriptorFactory.fromBitmap(b));
            marker.anchor(0.5f, 0.5f);

            mMap.addMarker(marker);
        }

        // adding vendor info when marker clicked
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                Log.d(TAG, "onMapReady - getInfoContents - Start");
                if (! marker.getTitle().equalsIgnoreCase(HOME_MARKER_TITLE)) {

                    View v = getActivity().getLayoutInflater().inflate(R.layout.marker_info,null);
                    // Get Vendor index
                    int index = Integer.parseInt(marker.getTitle());
//                    Vendor vendor = mVendorList.get(index);
//                    // set vendor info
//                    ((TextView) v.findViewById(R.id.vendor_name)).setText(vendor.getName());
//                    ((TextView) v.findViewById(R.id.vendor_address)).setText(vendor.getAddress());

                    return v;
                }

                Log.d(TAG, "onMapReady - getInfoContents - End");
                return null;
            }
        });

    }

    private void initCamera( GoogleMap googleMap) {

        Log.d(TAG, "initCamera - Start");

        CameraPosition position = CameraPosition.builder()
                .target(new LatLng(mLatitude,mLongitude))
                .zoom(13f)
                .bearing(0.0f)
                .tilt(0.0f)
                .build();

        googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), null);

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled( true );

        Log.d(TAG, "initCamera - End");
    }

    //----------------------------------------------------------------------------------------------
    // Following methods are called to handle mapView lifecycle
    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState); mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
    //----------------------------------------------------------------------------------------------


    private void loadBundleData(){
        mLatitude = getArguments().getDouble(EXTRA_CURRENT_LATITUDE);
        mLongitude = getArguments().getDouble(EXTRA_CURRENT_LONGITUDE);
        mShopsList = Parcels.unwrap(getArguments().getParcelable(EXTRA_SHOPS_LIST));
        Log.d(TAG, "Size: " + mShopsList.size());
    }
}
