package com.badeeb.greenbook.fragments;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.content.res.AppCompatResources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.models.Category;
import com.badeeb.greenbook.models.Shop;
import com.bumptech.glide.Glide;
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
import com.makeramen.roundedimageview.RoundedImageView;

import org.parceler.Parcels;

import java.text.DecimalFormat;
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

    private MainActivity mActivity;
    private FragmentManager fragmentManager;

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

        // Map Initialization
        MapsInitializer.initialize(getContext());
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(mOnMapReadyCallback);

        mActivity = (MainActivity) getActivity();
        fragmentManager = getFragmentManager();

        setupListeners(view);

        Log.d(TAG, "init - End");
    }

    private void setupListeners(View view) {

        ImageView back = (ImageView) view.findViewById(R.id.ivback);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentManager.popBackStack();
            }
        });
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
            marker.infoWindowAnchor(-4.4f, 0.3f);

            mMap.addMarker(marker);
        }

        // adding vendor info when marker clicked
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){

            @Override
            public View getInfoWindow(final Marker marker) {
                Log.d(TAG, "onMapReady - getInfoContents - Start");
                if (! marker.getTitle().equalsIgnoreCase(HOME_MARKER_TITLE)) {

                    final View v = getActivity().getLayoutInflater().inflate(R.layout.marker_info,null);
                    // Get Vendor index
                    int index = Integer.parseInt(marker.getTitle());

                    Shop shop = mShopsList.get(index);

                    DecimalFormat df = new DecimalFormat("0.0");


                    ((TextView) v.findViewById(R.id.tvShopName)).setText(shop.getName());
                    ((TextView) v.findViewById(R.id.tvRating)).setText(df.format(shop.getRate())+"");
                    ((RatingBar) v.findViewById(R.id.rbShopRate)).setRating((float)shop.getRate());

                    RoundedImageView shopImage = (RoundedImageView) v.findViewById(R.id.rivShopImage);

                    Glide.with(getContext())
                            .load(shop.getMainPhotoURL())
                            .asBitmap()
                            .placeholder(R.drawable.pic_img)
                            .into(shopImage);

                    // Add Listener for close icon
//                    ImageView ivClose = v.findViewById(R.id.ivClose);
//                    ivClose.setOnTouchListener(new View.OnTouchListener() {
//                        @Override
//                        public boolean onTouch(View v1, MotionEvent event) {
//                            Log.d(TAG, "ivClose - onTouch - Close Button");
//                            marker.hideInfoWindow();
//                            return true;
//                        }
//                    });
//                    ivClose.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v1) {
//                            Log.d(TAG, "ivClose - onClick - Close Button");
//                            marker.hideInfoWindow();
//                        }
//                    });

                    return v;
                }

                Log.d(TAG, "onMapReady - getInfoContents - End");
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
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
