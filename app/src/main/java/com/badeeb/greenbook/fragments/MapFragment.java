package com.badeeb.greenbook.fragments;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.models.ShopInquiry;
import com.badeeb.greenbook.network.NonAuthorizedCallback;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.UiUtils;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.reflect.TypeToken;
import com.makeramen.roundedimageview.RoundedImageView;

import org.parceler.Parcels;

import java.lang.reflect.Type;
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
    public static final String EXTRA_SEARCH_KEYWORD = "EXTRA_SEARCH_KEYWORD";
    public final static String HOME_MARKER_TITLE = "Search Location";

    private OnMapReadyCallback mOnMapReadyCallback;
    private GoogleMap mMap;
    private MapView mMapView;
    private TextView tvRedoSearch;

    private double mLatitude;      // Current Latitude
    private double mLongitude;      // Current Longitude

    private boolean redoClicked = false;

    private List<Shop> mShopList;

    private MainActivity mActivity;
    private FragmentManager fragmentManager;
    private String mSearchKeyword;

    public MapFragment() {
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
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        init(view, savedInstanceState);

        Log.d(TAG, "onCreateView - End");

        return view;
    }

    private void init(View view, Bundle savedInstanceState) {
        Log.d(TAG, "init - Start");

        loadBundleData();

        mOnMapReadyCallback = createOnMapReadyCallback();

        // Map Initialization
        MapsInitializer.initialize(getContext());
        mMapView = (MapView) view.findViewById(R.id.map);
        tvRedoSearch = view.findViewById(R.id.tvRedoSearch);

        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(mOnMapReadyCallback);

        mActivity = (MainActivity) getActivity();
        fragmentManager = getFragmentManager();

        mActivity.setSearchButtonAsChecked();

        setupListeners(view);
    }

    private void setupListeners(View view) {

        ImageView back = view.findViewById(R.id.ivback);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(redoClicked){
                    Bundle bundle = new Bundle();
                    bundle.putString(ShopListResultFragment.EXTRA_SELECTED_ADDRESS, "Current Map Area");
                    bundle.putDouble(ShopListResultFragment.EXTRA_SELECTED_LATITUDE, mLatitude);
                    bundle.putDouble(ShopListResultFragment.EXTRA_SELECTED_LONGITUDE, mLongitude);

                    ShopListResultFragment shopListResultFragment = new ShopListResultFragment();
                    shopListResultFragment.setArguments(bundle);

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

                    fragmentTransaction.replace(R.id.main_frame, shopListResultFragment, shopListResultFragment.TAG);

                    fragmentTransaction.commit();
                } else {
                    fragmentManager.popBackStack();
                }
            }
        });

        tvRedoSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.hide(tvRedoSearch);
                callSearchApi();
            }
        });
    }

    private void callSearchApi() {
        String url = Constants.BASE_URL + "/shops/search?query=" + mSearchKeyword
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
                }
                redoClicked = true;
                mMap.clear();
                drawMarkers();
            }

            @Override
            public void onError() {
                mActivity.getmSnackBarDisplayer().displayError("Error while getting shops from the server");
            }
        };

        Type responseType = new TypeToken<JsonResponse<ShopInquiry>>() {
        }.getType();

        VolleyWrapper<Object, JsonResponse<ShopInquiry>> volleyWrapper = new VolleyWrapper<>(null, responseType, Request.Method.GET, url,
                callback, getContext(), mActivity.getmSnackBarDisplayer(), mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();
    }

    private OnMapReadyCallback createOnMapReadyCallback() {
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        LatLng mapTargetLatLng = mMap.getCameraPosition().target;

                        Location currentMapLocation = new Location("");
                        currentMapLocation.setLatitude(mapTargetLatLng.latitude);
                        currentMapLocation.setLongitude(mapTargetLatLng.longitude);

                        Location previousMapLocations = new Location("");
                        previousMapLocations.setLatitude(mLatitude);
                        previousMapLocations.setLongitude(mLongitude);

                        double distance = currentMapLocation.distanceTo(previousMapLocations);

                        if(distance > 100){
                            mLatitude = mapTargetLatLng.latitude;
                            mLongitude = mapTargetLatLng.longitude;
                            UiUtils.show(tvRedoSearch);
                        }
                    }
                });
                initCamera(mMap);
                drawMarkers();
            }
        };
    }

    private void drawMarkers(){
        LatLng currentLocation = new LatLng(mLatitude, mLongitude);
        drawCurrentLocationOnMap(currentLocation);

        LatLngBounds.Builder builder = LatLngBounds.builder();
        builder.include(currentLocation);
        for (Shop s: mShopList) {
            builder.include(s.getLocation().getPosition());
        }
        final LatLngBounds bounds = builder.build();

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));

        drawShopsOnMap();
    }

    private void initCamera( GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled( true );
    }


    private void drawCurrentLocationOnMap(LatLng currentLocation) {

        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.cir_location);
        Bitmap b = bitmapdraw.getBitmap();

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

        for (int i = 0; i < mShopList.size(); i++) {
            MarkerOptions marker = new MarkerOptions();
            marker.position(new LatLng(mShopList.get(i).getLocation().getLat(), mShopList.get(i).getLocation().getLng()));
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

                    Shop shop = mShopList.get(index);

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

                    return v;
                }

                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }


        });
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
        mSearchKeyword = getArguments().getString(EXTRA_SEARCH_KEYWORD);
        mShopList = Parcels.unwrap(getArguments().getParcelable(EXTRA_SHOPS_LIST));
    }
}
