package com.badeeb.greenbook.fragments;

import android.app.ProgressDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.adaptors.ShopRecyclerViewAdaptor;
import com.badeeb.greenbook.listener.RecyclerItemClickListener;
import com.badeeb.greenbook.models.Category;
import com.badeeb.greenbook.models.CategoryInquiry;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.models.ShopInquiry;
import com.badeeb.greenbook.network.NonAuthorizedCallback;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.UiUtils;
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
    public final static String EXTRA_SELECTED_ADRESS = "EXTRA_SELECTED_ADRESS";

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
    private ShopRecyclerViewAdaptor mShopListAdaptor;
    private SwipeRefreshLayout srlShopList;
    private AutoCompleteTextView actvCategorySearch;
    private ArrayAdapter<Category> mAutoCategorySearchAdaptor;
    private ImageView ivSearch;
    private EditText etLocationSearch;
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

        prepareCategoryList();

        setupListener();

        goSearch();

    }

    private void initUi(View view){
        rvShopList = (RecyclerView) view.findViewById(R.id.rvShopList);
        RecyclerView.LayoutManager mShopLayoutManager = new LinearLayoutManager(mActivity);
        rvShopList.setLayoutManager(mShopLayoutManager);
        rvShopList.setItemAnimator(new DefaultItemAnimator());

        mShopListAdaptor = new ShopRecyclerViewAdaptor(mActivity, mShopList);
        rvShopList.setAdapter(mShopListAdaptor);

        mAutoCategorySearchAdaptor = new ArrayAdapter<Category>(mActivity, android.R.layout.select_dialog_item, mCategoryList);

        actvCategorySearch = (AutoCompleteTextView) view.findViewById(R.id.actvCategorySearch);
        actvCategorySearch.setAdapter(mAutoCategorySearchAdaptor);
        actvCategorySearch.setThreshold(Constants.THRESHOLD);

        srlShopList = (SwipeRefreshLayout) view.findViewById(R.id.shopList_form);

        etLocationSearch = (EditText) view.findViewById(R.id.etLocationSearch);
        ivSearch = (ImageView) view.findViewById(R.id.ivSearch);
        ivMap = (ImageView) view.findViewById(R.id.ivMap);

        llEmptyResult = (LinearLayout) view.findViewById(R.id.llEmptyResult);
    }

    private void loadBundleData(){
        String searchAddress =getArguments().getString(EXTRA_SELECTED_ADRESS);
        Category category = Parcels.unwrap(getArguments().getParcelable(EXTRA_SELECTED_CATEGORY));

        if(category != null){
            Log.d(TAG, "category selected EXTRA: "+category.getName());
            mSelectedCategory = category;
            actvCategorySearch.setText(category.getName());
        }
        if(!"".equals(searchAddress)){
            Log.d(TAG, "location address search EXTRA: "+searchAddress);
            etLocationSearch.setText(searchAddress);
        }
    }

    private void setupListener(){
        ivSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d(TAG, "ivCategory onClick - Start");
                goSearch();
                Log.d(TAG, "ivCategory onClick - End");
            }
        });

        ivMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        srlShopList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                goSearch();
            }
        });

        // Adding OnItemTouchListener to recycler view
        rvShopList.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Log.d(TAG, "setupListeners - rvShopList:onItemClick - Start");
                        // Get item that is selected
                        goToSelectedShop(position);

                        Log.d(TAG, "setupListeners - rvShopList:onItemClick - End");
                    }
                })
        );

    }

    private void goSearch() {
        Log.d(TAG, "goSearch - Start");

        mProgressDialog.show();

        prepareSearchLocation();

        if (!isLocationSelected) {
            Log.d(TAG, "goSearch - location not selected");
            mProgressDialog.dismiss();
            mActivity.getmSnackBarDisplayer().displayError("Search Failed because search can not be determined!");
            return;
        }

        fetchSelectedCategory();

        callSearchApi();
        Log.d(TAG, "goSearch - end");
    }

    private void fetchSelectedCategory() {
        String selectedCategory = actvCategorySearch.getText().toString();
        if (!"".equals(selectedCategory)) {
            Log.d(TAG, "goSearch - categorySearch selected : " + selectedCategory);

            for (Category category : mCategoryList) {
                if (category.getName().toUpperCase().equals(selectedCategory.toUpperCase())) {
                    Log.d(TAG, "goSearch - selectCategoryId: " + category.getName());
                    mSelectedCategory = category;
                }
            }
        }
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

                if (mShopList != null && mShopList.size() == 0) {
                    enableNoSearchFoundScreen();
                }

            }

            @Override
            public void onError() {
                Log.d(TAG, "callSearchApi - NonAuthorizedCallback - onError");
                mActivity.getmSnackBarDisplayer().displayError("Error while getting shops from the server");
                mProgressDialog.dismiss();
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

        String searchLocation = etLocationSearch.getText().toString();

        if (!"".equals(searchLocation)) {
            Log.d(TAG, "prepareLocation - fetchLocationFromAddress");
            fetchLocationFromAddress(searchLocation);
        } else {
            Log.d(TAG, "prepareLocation - fetchCurrentLocation");

            fetchCurrentLocation();
        }
    }

    private void fetchLocationFromAddress(String address) {
        Log.d(TAG, "fetchLocationFromAddress - Start");
        Geocoder geocoder = new Geocoder(mActivity);
        try {
            List<Address> addressList = geocoder.getFromLocationName(address, 1);

            Address addressLocation = addressList.get(0);
            mLatitude = addressLocation.getLatitude();
            mLongitude = addressLocation.getLongitude();
            isLocationSelected = true;
            Log.d(TAG, "fetchLocationFromAddress - Address Lat: " + mLatitude + " - Long: " + mLongitude);

        } catch (IOException e) {
            Log.d(TAG, "fetchLocationFromAddress - IOException");

            mActivity.getmSnackBarDisplayer().displayError("The entered address couldn't be find");
            isLocationSelected = false;
        }

        Log.d(TAG, "fetchLocationFromAddress - End");
    }

    private void fetchCurrentLocation() {
        mLatitude = mActivity.getCurrentLocation().getLatitude();
        mLongitude = mActivity.getCurrentLocation().getLongitude();
        isLocationSelected = true;
    }


    // callled by the listener in ShopRecyclerViewAdaptor
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
//        disconnectGoogleApiClient();
        Log.d(TAG, "goToSelectedShop - End");
    }


}
