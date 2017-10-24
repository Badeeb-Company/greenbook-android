package com.badeeb.greenbook.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.listener.RecyclerItemClickListener;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.adaptors.CategoryRecyclerViewAdaptor;
import com.badeeb.greenbook.models.Category;
import com.badeeb.greenbook.models.CategoryInquiry;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.network.NonAuthorizedCallback;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.UiUtils;
import com.google.gson.reflect.TypeToken;
import org.parceler.Parcels;
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
    private Category mSelectedCategory;

    // UI Fields
    private RecyclerView rvCategoryList;
    private CategoryRecyclerViewAdaptor mCategoryListAdaptor;
    private SwipeRefreshLayout srlCategoryList;
    private EditText etLocationSearch;


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

        initUIComponents(view);

        setupListener();

        prepareCategoryList();

        Log.d(TAG, "init - End");
    }


    public void initUIComponents(View view) {
        rvCategoryList = (RecyclerView) view.findViewById(R.id.rvCategoryList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        rvCategoryList.setLayoutManager(mLayoutManager);
        rvCategoryList.setItemAnimator(new DefaultItemAnimator());

        mCategoryListAdaptor = new CategoryRecyclerViewAdaptor(mActivity, mCategoryList);
        rvCategoryList.setAdapter(mCategoryListAdaptor);

        srlCategoryList = (SwipeRefreshLayout) view.findViewById(R.id.category_form);
        srlCategoryList.setVisibility(View.VISIBLE);

        etLocationSearch = (EditText) view.findViewById(R.id.etLocationSearch);
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
                        goToShopResultListFragment();

                        Log.d(TAG, "setupListeners - rvCategoryList - End");
                    }
                })
        );
        Log.d(TAG, "setupListeners - Start");
    }

    private void goToShopResultListFragment(){
        Log.d(TAG, "goToShopResultListFragment - Start");

        Bundle bundle = new Bundle();
        bundle.putParcelable(ShopListResultFragment.EXTRA_SELECTED_CATEGORY, Parcels.wrap(mSelectedCategory));
        bundle.putString(ShopListResultFragment.EXTRA_SELECTED_ADRESS, etLocationSearch.getText().toString());

        ShopListResultFragment shopListResultFragment = new ShopListResultFragment();
        shopListResultFragment.setArguments(bundle);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, shopListResultFragment, shopListResultFragment.TAG);

        fragmentTransaction.addToBackStack(TAG);

        fragmentTransaction.commit();

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
}