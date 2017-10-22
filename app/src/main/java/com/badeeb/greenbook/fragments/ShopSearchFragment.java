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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.RecyclerItemClickListener;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.adaptors.CategoryRecyclerViewAdaptor;
import com.badeeb.greenbook.adaptors.ShopRecyclerViewAdaptor;
import com.badeeb.greenbook.models.Category;
import com.badeeb.greenbook.models.CategoryInquiry;
import com.badeeb.greenbook.models.JsonRequest;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.models.ShopInquiry;
import com.badeeb.greenbook.network.NonAuthorizedCallback;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.ErrorDisplayHandler;
import com.badeeb.greenbook.shared.UiUtils;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShopSearchFragment extends Fragment {

    public static final String TAG = ShopSearchFragment.class.getSimpleName();

    private MainActivity mActivity;

    private ProgressDialog mProgressDialog;

    private List<Category> mCategoryList;
    private List<Shop> mShopList;
    private int mSelectedCategoryId;
    private double mLatitude;
    private double mLongitude;

    private RecyclerView rvCategoryList;
    private CategoryRecyclerViewAdaptor mCategoryListAdaptor;
    private RecyclerView rvShopList;
    private ShopRecyclerViewAdaptor mShopListAdaptor;

    private SwipeRefreshLayout srlCategoryList;
    private SwipeRefreshLayout srlShopList;
    private AutoCompleteTextView actvCategorySearch;
    private ArrayAdapter<Category> mAutoCategorySearchAdaptor;
    private ImageView ivCategory;
    private EditText etLocationSearch;
    private ImageView ivMap;



    public ShopSearchFragment() {
        // Required empty public constructor
    }


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
        mShopList = new ArrayList<Shop>();

        initUIComponents(view);

        setupListener();

        prepareCategoryList();

        Log.d(TAG, "init - End");
    }

    public void initUIComponents(View view){
        rvCategoryList = (RecyclerView) view.findViewById(R.id.rvCategoryList) ;
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        rvCategoryList.setLayoutManager(mLayoutManager);
        rvCategoryList.setItemAnimator(new DefaultItemAnimator());

        mCategoryListAdaptor = new CategoryRecyclerViewAdaptor(mActivity, mCategoryList);
        rvCategoryList.setAdapter(mCategoryListAdaptor);

        rvShopList = (RecyclerView) view.findViewById(R.id.rvShopList) ;
        RecyclerView.LayoutManager mShopLayoutManager = new LinearLayoutManager(mActivity);
        rvShopList.setLayoutManager(mShopLayoutManager);
        rvShopList.setItemAnimator(new DefaultItemAnimator());

        mShopListAdaptor = new ShopRecyclerViewAdaptor(mActivity,mShopList );
        rvShopList.setAdapter(mShopListAdaptor);

        mAutoCategorySearchAdaptor = new ArrayAdapter<Category>(mActivity, android.R.layout.select_dialog_item, mCategoryList);
        actvCategorySearch = (AutoCompleteTextView) view.findViewById(R.id.actvCategorySearch) ;
        actvCategorySearch.setAdapter(mAutoCategorySearchAdaptor);
        actvCategorySearch.setThreshold(Constants.THRESHOLD);

        srlCategoryList = (SwipeRefreshLayout) view.findViewById(R.id.category_form) ;
        srlCategoryList.setVisibility(View.VISIBLE);
        srlShopList = (SwipeRefreshLayout) view.findViewById(R.id.shopList_form) ;
        srlShopList.setVisibility(View.GONE);

        etLocationSearch = (EditText) view.findViewById(R.id.etLocationSearch) ;
        ivCategory = (ImageView)  view.findViewById(R.id.ivCategory);
        ivMap = (ImageView) view.findViewById(R.id.ivMap) ;
    }

    public void setupListener(){
        ivCategory.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.d(TAG, "ivCategory onClick - Start");
                goSearch();
                Log.d(TAG, "ivCategory onClick - End");
            }
        });

        ivMap.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

            }
        });

        srlCategoryList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                mCategoryList.clear();
                mCategoryListAdaptor.notifyDataSetChanged();
                prepareCategoryList();
            }
        });

        srlShopList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                mShopList.clear();
                mShopListAdaptor.notifyDataSetChanged();
                goSearch();
            }
        });

        rvCategoryList.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Log.d(TAG, "setupListeners - mRecyclerView:onItemClick - Start");
                        mSelectedCategoryId = mCategoryList.get(position).getId();
                        goSearch();

                        Log.d(TAG, "setupListeners - onItemClick - End");
                    }
                })
        );

    }

    private void goSearch() {
        Log.d(TAG, "goSearch - Start");
        prepareLocation();

        String selectedCategory = actvCategorySearch.getText().toString();
        if(!"".equals(selectedCategory)){
            Log.d(TAG, "goSearch - categorySearch selected : "+selectedCategory);

            for(Category category : mCategoryList){
                if(category.getName().toUpperCase().equals(selectedCategory.toUpperCase())){
                    Log.d(TAG, "goSearch - selectCategoryId: "+category.getId());
                    mSelectedCategoryId = category.getId();
                }
            }
        }

        callSearchApi();
        Log.d(TAG, "goSearch - end");
    }

    private void callSearchApi(){
        Log.d(TAG, "callSearchApi - Start");
        srlShopList.setRefreshing(true);
        String url = Constants.BASE_URL+"/shops/search?category_id="+mSelectedCategoryId+"&lat="+mLatitude+"&lng="+mLongitude;
        Log.d(TAG, "callSearchApi - Request URL: "+url);

        NonAuthorizedCallback<JsonResponse<ShopInquiry>> callback = new NonAuthorizedCallback<JsonResponse<ShopInquiry>>() {

            @Override
            public void onSuccess(JsonResponse<ShopInquiry> jsonResponse) {
                Log.d(TAG, "callSearchApi - NonAuthorizedCallback - onSuccess");

                if(jsonResponse != null
                        && jsonResponse.getResult() != null
                        && jsonResponse.getResult().getShopList() != null
                        && !jsonResponse.getResult().getShopList().isEmpty()){

                    Log.d(TAG, "callSearchApi - NonAuthorizedCallback - onSuccess - shopList: "
                            + Arrays.toString(jsonResponse.getResult().getShopList().toArray()));
                    mShopList.clear();
                    mShopList.addAll(jsonResponse.getResult().getShopList());
                    mShopListAdaptor.notifyDataSetChanged();
                    switchToShopList();
                }else{
                    //switch to empty search page
                }

            }

            @Override
            public void onError() {
                Log.d(TAG, "callSearchApi - NonAuthorizedCallback - onError");
                mActivity.getmSnackBarDisplayer().displayError("Error while getting shops from the server");
            }
        };

        Type responseType = new TypeToken<JsonResponse<ShopInquiry>>() {}.getType();

        VolleyWrapper<Object, JsonResponse<ShopInquiry>> volleyWrapper = new VolleyWrapper<>(null, responseType, Request.Method.GET, url, callback, getContext(), true, mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();

        srlShopList.setRefreshing(false);

        Log.d(TAG, "callSearchApi - Start");
    }

    private void switchToShopList(){
        Log.d(TAG, "switchToShopList - Start");
        srlCategoryList.setVisibility(View.GONE);
        srlShopList.setVisibility(View.VISIBLE);
        ivMap.setVisibility(View.VISIBLE);
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        Log.d(TAG, "switchToShopList - end");
    }

    private void prepareCategoryList(){
        callCategoryListApi();
    }

    private void callCategoryListApi() {
        Log.d(TAG, "callCategoryListApi - Start");
        srlCategoryList.setRefreshing(true);
        String url = Constants.BASE_URL + "/categories";

        Log.d(TAG, "callCategoryListApi - url: " + url);

        NonAuthorizedCallback<JsonResponse<CategoryInquiry>> callback = new NonAuthorizedCallback<JsonResponse<CategoryInquiry>>() {
            @Override
            public void onSuccess(JsonResponse<CategoryInquiry> jsonResponse) {
                Log.d(TAG, "callCategoryListApi - onSuccess - Start");

                if(jsonResponse != null && jsonResponse.getResult() != null && jsonResponse.getResult().getCategoryList() != null){
                    mCategoryList.addAll(jsonResponse.getResult().getCategoryList());
                    mCategoryListAdaptor.notifyDataSetChanged();
                    mAutoCategorySearchAdaptor.notifyDataSetChanged();
                }else{
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
        Type responseType = new TypeToken<JsonResponse<CategoryInquiry>>() {}.getType();

        VolleyWrapper<Object, JsonResponse<CategoryInquiry>> volleyWrapper = new VolleyWrapper<>(null, responseType, Request.Method.GET, url, callback, getContext(), true, mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();
        srlCategoryList.setRefreshing(false);
        Log.d(TAG, "callCategoryListApi - End");
    }

    private void prepareLocation(){
        Log.d(TAG, "prepareLocation - Start");
        String searchLocation = etLocationSearch.getText().toString();
        if(!"".equals(searchLocation)){
            fetchLocationFromAddress(searchLocation);
        }else{
            fetchCurrentLocation();
        }
        Log.d(TAG, "prepareLocation - End");
    }

    private void fetchLocationFromAddress(String address) {
        Log.d(TAG, "fetchLocationFromAddress - Start");
        Geocoder geocoder = new Geocoder(mActivity);
        try {
            List<Address> addressList = geocoder.getFromLocationName(address,1);

            Address addressLocation = addressList.get(0);
            mLatitude = addressLocation.getLatitude();
            mLongitude = addressLocation.getLongitude();

            Log.d(TAG, "fetchLocationFromAddress - Address Lat: "+mLatitude+" - Long: "+mLongitude);

        } catch (IOException e) {
            Log.d(TAG, "fetchLocationFromAddress - IOException");

            mActivity.getmSnackBarDisplayer().displayError("The entered address couldn't be find");
        }
        Log.d(TAG, "fetchLocationFromAddress - End");
    }

    private void fetchCurrentLocation(){
        Log.d(TAG, "fetchCurrentLocation - Start");
        mLatitude = 24.66514 ;
        mLongitude = 46.7333436;
        Log.d(TAG, "fetchCurrentLocation - End");
    }


}
