package com.badeeb.greenbook.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.models.Category;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.network.NonAuthorizedCallback;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.UiUtils;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShopSearchFragment extends Fragment {

    public static final String TAG = ShopSearchFragment.class.getSimpleName();

    private MainActivity mActivity;

    private ProgressDialog mProgressDialog;

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

        // Fetch list of available categories from server
        callCategoryListApi();

        Log.d(TAG, "init - End");
    }

    private void callCategoryListApi() {
        Log.d(TAG, "callCategoryListApi - Start");

        String url = Constants.BASE_URL + "/categories";

        Log.d(TAG, "callCategoryListApi - url: " + url);

        NonAuthorizedCallback<JsonResponse<Category>> callback = new NonAuthorizedCallback<JsonResponse<Category>>() {
            @Override
            public void onSuccess(JsonResponse<Category> jsonResponse) {
                Log.d(TAG, "callCategoryListApi - onSuccess - Start");



                mProgressDialog.dismiss();

                Log.d(TAG, "callCategoryListApi - onSuccess - End");
            }

            @Override
            public void onError() {
                Log.d(TAG, "callCategoryListApi - onError - Start");

                mProgressDialog.dismiss();

                Log.d(TAG, "callCategoryListApi - onError - End");
            }
        };

        // Prepare response type
        Type responseType = new TypeToken<JsonResponse<Category>>() {}.getType();

        VolleyWrapper<Object, JsonResponse<Category>> volleyWrapper = new VolleyWrapper<>(null, responseType, Request.Method.GET, url, callback, getContext(), true, mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();

        Log.d(TAG, "callCategoryListApi - End");
    }

}
