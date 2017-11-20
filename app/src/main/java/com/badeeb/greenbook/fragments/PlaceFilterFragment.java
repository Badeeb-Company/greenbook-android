package com.badeeb.greenbook.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.adaptors.PlaceAutocompleteAdapter;
import com.badeeb.greenbook.models.Category;
import com.badeeb.greenbook.shared.Constants;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import org.parceler.Parcels;

import java.util.Arrays;
import java.util.List;

public class PlaceFilterFragment extends Fragment {
    public final static String TAG = PlaceFilterFragment.class.getName();

    private MainActivity mActivity;
    private String mAddress;
    private double mLatitude;
    private double mLongitude;
    private Category mSelectedCategory;
    private List<Category> mCategoryList;

    private ImageView ivBack;
    private TextView tvCurrentLocation;
    private EditText etPlaceSearch;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private ListView lvPlaceList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container != null) {
            // this code is used to prevent fragment overlapping
            container.removeAllViews();
        }

        View view = inflater.inflate(R.layout.fragment_place_filter, container, false);

        init(view);

        return view;
    }

    private void init(View view) {

        mActivity = (MainActivity) getActivity();

        loadBundle();

        initUi(view);

        mActivity.connectPlaceGoogleApiClient();

        setupListener();
    }

    private void loadBundle(){
        Bundle bundle = getArguments();
        mSelectedCategory = Parcels.unwrap(bundle.getParcelable(ShopListResultFragment.EXTRA_SELECTED_CATEGORY));
        mCategoryList = Parcels.unwrap(bundle.getParcelable(ShopListResultFragment.EXTRA_SELECTED_CATEGORY_LIST));
    }

    private void initUi(View view){
        etPlaceSearch = (EditText) view.findViewById(R.id.etPlaceSearch);

        tvCurrentLocation = (TextView) view.findViewById(R.id.tvCurrentLocation) ;

        ivBack = (ImageView) view.findViewById(R.id.ivBack);

        lvPlaceList = (ListView) view.findViewById(R.id.lvPlaceList) ;
        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(mActivity,mActivity.getmPlaceGoogleApiClient(), Constants.BOUNDS_MIDDLE_EAST, null);
        lvPlaceList.setAdapter(mPlaceAutocompleteAdapter);
    }

    private void setupListener(){
        tvCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddress = ""; //empty address will force to search with current location as default

                goToShopListResultFragment();
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        etPlaceSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "addTextChangedListener - afterTextChanged - seq: "+s);

                mPlaceAutocompleteAdapter.getFilter().filter(s);
            }
        });

        lvPlaceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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


            mAddress = place.getAddress().toString();
            mLatitude = place.getLatLng().latitude;
            mLongitude = place.getLatLng().longitude;

            Log.i(TAG, "location after autocomplete - lat: " + mLatitude+" - lng: "+mLongitude);

            places.release();

            goToShopListResultFragment();

            Log.d(TAG, "mUpdatePlaceDetailsCallback - onResult - end ");
        }
    };

    private void goToShopListResultFragment(){
        Log.d(TAG, "goToShopResultListFragment - Start");
        Log.d(TAG, "mSelectedPlace: "+mAddress);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ShopListResultFragment.EXTRA_SELECTED_CATEGORY, Parcels.wrap(mSelectedCategory));
        bundle.putParcelable(ShopListResultFragment.EXTRA_SELECTED_CATEGORY_LIST, Parcels.wrap(mCategoryList));
        bundle.putString(ShopListResultFragment.EXTRA_SELECTED_ADDRESS, mAddress);
        bundle.putDouble(ShopListResultFragment.EXTRA_SELECTED_LATITUDE, mLatitude);
        bundle.putDouble(ShopListResultFragment.EXTRA_SELECTED_LONGITUDE, mLongitude);


        ShopListResultFragment shopListResultFragment = new ShopListResultFragment();
        shopListResultFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, shopListResultFragment, shopListResultFragment.TAG);

        fragmentTransaction.commit();

        Log.d(TAG, "goToShopResultListFragment - End");
    }


    @Override
    public void onDestroy() {
        mActivity.disconnectPlaceGoogleApiClient();
        super.onDestroy();
    }

}
