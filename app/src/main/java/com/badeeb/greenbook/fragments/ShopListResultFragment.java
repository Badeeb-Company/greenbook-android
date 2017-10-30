package com.badeeb.greenbook.fragments;

import android.app.ProgressDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.adaptors.PlaceAutocompleteAdapter;
import com.badeeb.greenbook.listener.RecyclerItemClickListener;
import com.badeeb.greenbook.adaptors.ShopRecyclerViewAdapter;
import com.badeeb.greenbook.models.Category;
import com.badeeb.greenbook.models.CategoryInquiry;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.models.ShopInquiry;
import com.badeeb.greenbook.network.NonAuthorizedCallback;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.UiUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.reflect.TypeToken;

import org.parceler.Parcels;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopListResultFragment extends Fragment {
    public final static String TAG = ShopListResultFragment.class.getName();
    public final static String EXTRA_SELECTED_CATEGORY = "EXTRA_SELECTED_CATEGORY";
    public final static String EXTRA_SELECTED_CATEGORY_LIST = "EXTRA_SELECTED_CATEGORY_LIST";
    public final static String EXTRA_SELECTED_ADRESS = "EXTRA_SELECTED_ADRESS";
    public final static String EXTRA_SELECTED_LATITUDE = "EXTRA_SELECTED_LATITUDE";
    public final static String EXTRA_SELECTED_LONGITUDE = "EXTRA_SELECTED_LONGITUDE";


    // Class Attributes
    private MainActivity mActivity;
    private ProgressDialog mProgressDialog;
    private List<Shop> mShopList;
    private List<Category> mCategoryList;

    // search parameters
    private Category mSelectedCategory;
    private double mLatitude;
    private double mLongitude;
    private boolean isLocationSelected;

    // UI Fields
    private RecyclerView rvShopList;
    private ShopRecyclerViewAdapter mShopListAdaptor;
    private SwipeRefreshLayout srlShopList;
    private AutoCompleteTextView actvCategorySearch;
    private ArrayAdapter<Category> mAutoCategorySearchAdaptor;
    private ImageView ivSearch;
    private AutoCompleteTextView actvLocationSearch;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private ImageView ivMap;
    private LinearLayout llEmptyResult;






    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_shop_list_result, container, false);

        init(view);

        return view;
    }

    private void init(View view){
        mActivity = (MainActivity) getActivity();
        mProgressDialog = UiUtils.createProgressDialog(mActivity);
        mActivity.showBottomNavigationActionBar();
        mActivity.hideToolbar();
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mCategoryList = new ArrayList<>();
        mShopList = new ArrayList<>();

        initUi(view);

        loadBundleData();

//        prepareCategoryList();

        setupListener();

        goSearch();

        mActivity.connectPlaceGoogleApiClient();

    }

    private void initUi(View view){
        rvShopList = (RecyclerView) view.findViewById(R.id.rvShopList);
        RecyclerView.LayoutManager mShopLayoutManager = new LinearLayoutManager(mActivity);
        rvShopList.setLayoutManager(mShopLayoutManager);
        rvShopList.setItemAnimator(new DefaultItemAnimator());

        mShopListAdaptor = new ShopRecyclerViewAdapter(mActivity, mShopList, this);
        rvShopList.setAdapter(mShopListAdaptor);

        mAutoCategorySearchAdaptor = new ArrayAdapter<Category>(mActivity, android.R.layout.select_dialog_item, mCategoryList);

        actvCategorySearch = (AutoCompleteTextView) view.findViewById(R.id.actvCategorySearch);
        actvCategorySearch.setAdapter(mAutoCategorySearchAdaptor);
        actvCategorySearch.setThreshold(Constants.THRESHOLD);

        srlShopList = (SwipeRefreshLayout) view.findViewById(R.id.shopList_form);

        actvLocationSearch = (AutoCompleteTextView) view.findViewById(R.id.actvLocationSearch);
        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(mActivity,mActivity.getmPlaceGoogleApiClient(),Constants.BOUNDS_MIDDLE_EAST, null);
        actvLocationSearch.setAdapter(mPlaceAutocompleteAdapter);

        ivSearch = (ImageView) view.findViewById(R.id.ivSearch);
        ivMap = (ImageView) view.findViewById(R.id.ivMap);

        llEmptyResult = (LinearLayout) view.findViewById(R.id.llEmptyResult);
    }

    private void loadBundleData(){
        String searchAddress =getArguments().getString(EXTRA_SELECTED_ADRESS);
        double lat = getArguments().getDouble(EXTRA_SELECTED_LATITUDE);
        double lng = getArguments().getDouble(EXTRA_SELECTED_LONGITUDE);

        Category category = Parcels.unwrap(getArguments().getParcelable(EXTRA_SELECTED_CATEGORY));
        mCategoryList = Parcels.unwrap(getArguments().getParcelable(EXTRA_SELECTED_CATEGORY_LIST));
        mAutoCategorySearchAdaptor.clear();
        mAutoCategorySearchAdaptor.addAll(mCategoryList);

        if(category != null){
            Log.d(TAG, "category selected EXTRA: "+category.getName());
            mSelectedCategory = category;
            actvCategorySearch.setText(category.getName());
        }
        if(!"".equals(searchAddress)){
            Log.d(TAG, "location address search EXTRA: "+searchAddress);
            actvLocationSearch.setText(searchAddress);
            mLatitude = lat;
            mLongitude = lng;
            isLocationSelected = true;
            Log.d(TAG, "location address search EXTRA - lat: "+mLatitude+" - lng: "+mLongitude);
        }
    }



    private void setupListener(){

        actvCategorySearch.setOnEditorActionListener(new AutoCompleteTextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    goSearch();
                    return true;
                }
                return false;
            }
        });

        actvCategorySearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mSelectedCategory = mCategoryList.get(i);
                goSearch();
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

                Toast.makeText(mActivity, "Clicked: " + primaryText,
                        Toast.LENGTH_SHORT).show();
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

            mLatitude = place.getLatLng().latitude;
            mLongitude = place.getLatLng().longitude;
            isLocationSelected = true;


            Log.i(TAG, "location after autocomplete - lat: " + mLatitude+" - lng: "+mLongitude);

            places.release();

            goSearch();

            Log.d(TAG, "mUpdatePlaceDetailsCallback - onResult - end ");
        }
    };


    private void goSearch() {
        Log.d(TAG, "goSearch - Start");

        mActivity.hideKeyboard();
        mProgressDialog.show();

        prepareSearchLocation();

        if (!isLocationSelected) {
            Log.d(TAG, "goSearch - location not selected");
            mProgressDialog.dismiss();
            mActivity.getmSnackBarDisplayer().displayError("Search Failed because search can not be determined!");
            return;
        }

        if(!fetchSelectedCategory()){
            mActivity.getmSnackBarDisplayer().displayError("The entered category does not exist");
            mProgressDialog.dismiss();
            enableNoSearchFoundScreen();
            return;
        }

        callSearchApi();
        Log.d(TAG, "goSearch - end");
    }

    private boolean fetchSelectedCategory() {
        String selectedCategory = actvCategorySearch.getText().toString();

        if (selectedCategory != null && !selectedCategory.isEmpty()) {
            Log.d(TAG, "goSearch - categorySearch selected : " + selectedCategory+" - category list: "+Arrays.toString(mCategoryList.toArray()));
            for (Category category : mCategoryList) {
                Log.d(TAG, "goSearch - categorySearch - category.getName() : " + category.getName().toUpperCase()
                +" - selectedCategory: "+selectedCategory.toUpperCase());
                if (category.getName().toUpperCase().equals(selectedCategory.toUpperCase())) {
                    Log.d(TAG, "goSearch - selectCategoryId: " + category.getName());
                    mSelectedCategory = category;
                    return true;
                }
            }
        }


        return false;
    }


    private void callSearchApi() {
        Log.d(TAG, "callSearchApi - Start");
        String url = Constants.BASE_URL + "/shops/search?category_id=" + mSelectedCategory.getId() + "&lat=" + mLatitude + "&lng=" + mLongitude;
        Log.d(TAG, "callSearchApi - Request URL: " + url);

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
                }else{
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
                    Log.d(TAG, "callCategoryListApi - updated category list: "+ Arrays.toString(mCategoryList.toArray()));
                    mAutoCategorySearchAdaptor.clear();
                    mAutoCategorySearchAdaptor.addAll(mCategoryList);
                } else {
                    mActivity.getmSnackBarDisplayer().displayError("Categories not loaded from the server");
                }

                mProgressDialog.dismiss();

                Log.d(TAG, "callCategoryListApi - onSuccess - End");
            }

            @Override
            public void onError() {
                Log.d(TAG, "callCategoryListApi - onError - Start");

                mActivity.getmSnackBarDisplayer().displayError("Error loading categories from the server");

                mProgressDialog.dismiss();

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

    private void prepareSearchLocation() {
        Log.d(TAG, "prepareLocation - Start");

        String searchLocation = actvLocationSearch.getText().toString();

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

    @Override
    public void onDestroy() {
        mActivity.disconnectPlaceGoogleApiClient();
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

}
