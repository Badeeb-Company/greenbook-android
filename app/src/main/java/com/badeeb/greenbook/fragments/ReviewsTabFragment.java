package com.badeeb.greenbook.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.adaptors.ReviewRecyclerViewAdapter;
import com.badeeb.greenbook.models.JsonRequest;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.models.Review;
import com.badeeb.greenbook.models.ReviewsInquiry;
import com.badeeb.greenbook.models.Shop;
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

public class ReviewsTabFragment extends Fragment {

    public final static String TAG = ReviewsTabFragment.class.getName();

    private MainActivity mActivity;
    private ProgressDialog mProgressDialog;
    private FragmentManager mFragmentManager;
    private Shop mShop;

    private RecyclerView mRecyclerView;
    private ReviewRecyclerViewAdapter mReviewRecyclerViewAdapter;
    private List<Review> mReviewsList;
    private int mReviewsPerLine;

    private FloatingActionButton mFabAddReview;


    public ReviewsTabFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_reviews_tab, container, false);

        init(view);

        Log.d(TAG, "onCreateView - End");

        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        mActivity = (MainActivity) getActivity();
        mProgressDialog = UiUtils.createProgressDialog(mActivity);
        mFragmentManager = getFragmentManager();
        mReviewsList = new ArrayList<>();
        mFabAddReview = view.findViewById(R.id.fabAddReview);

        loadBundleData();

        initRecyclerView(view);

        setupListeners();

        prepareReviewsList();

        Log.d(TAG, "init - End");
    }

    private void initRecyclerView(View view) {

        mRecyclerView = view.findViewById(R.id.rvReviews);
        mReviewRecyclerViewAdapter = new ReviewRecyclerViewAdapter(getContext(), mShop, mReviewsList);
        mReviewsPerLine = 1;

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), mReviewsPerLine);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Link adapter with recycler view
        mRecyclerView.setAdapter(mReviewRecyclerViewAdapter);
    }

    private void loadBundleData() {

        mShop = Parcels.unwrap(getArguments().getParcelable(ShopDetailsFragment.EXTRA_SHOP_OBJECT));

    }

    private void setupListeners() {
        Log.d(TAG, "setupListeners - Start");

        mFabAddReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivity.getUser() != null) {
                    goToAddReview();
                }
                else {
                    askUserToLoginDialog();
                }
            }
        });

        Log.d(TAG, "setupListeners - End");
    }

    private void askUserToLoginDialog() {
        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mActivity.setState(Constants.GO_TO_ADD_REVIEW);
                goToLogin();
            }
        };

        DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        };

        UiUtils.showDialog(getContext(), R.style.DialogTheme,
                R.string.ask_user_to_login_title, R.string.ask_user_to_login_msg,
                R.string.ok_btn_dialog, positiveListener, R.string.cancel, negativeListener);
    }

    private void prepareReviewsList() {
        mProgressDialog.show();
        callListReviewsApi();
    }

    private void callListReviewsApi() {

        String url = Constants.BASE_URL + "/shops/" + mShop.getId() + "/reviews";

        Log.d(TAG, "callListReviewsApi - url: " + url);

        NonAuthorizedCallback<JsonResponse<ReviewsInquiry>> callback = new NonAuthorizedCallback<JsonResponse<ReviewsInquiry>>() {
            @Override
            public void onSuccess(JsonResponse<ReviewsInquiry> jsonResponse) {
                Log.d(TAG, "callListReviewsApi - onSuccess - Start");

                if (jsonResponse != null && jsonResponse.getResult() != null && jsonResponse.getResult().getReviewsList() != null) {
                    mReviewsList.clear();
                    mReviewsList.addAll(jsonResponse.getResult().getReviewsList());
                    mReviewRecyclerViewAdapter.notifyDataSetChanged();

                    Log.d(TAG, "callListReviewsApi - onSuccess - mReviewsList: "+ Arrays.toString(mReviewsList.toArray()));

                } else {
                    mActivity.getmSnackBarDisplayer().displayError("Categories not loaded from the server");
                }

                mProgressDialog.dismiss();

                Log.d(TAG, "callListReviewsApi - onSuccess - End");
            }

            @Override
            public void onError() {
                Log.d(TAG, "callListReviewsApi - onError - Start");

                mProgressDialog.dismiss();

                Log.d(TAG, "callListReviewsApi - onError - End");
            }
        };

        // Prepare response type
        Type responseType = new TypeToken<JsonResponse<ReviewsInquiry>>() {}.getType();

        VolleyWrapper<Object, JsonResponse<ReviewsInquiry>> volleyWrapper = new VolleyWrapper<>
                (null, responseType, Request.Method.GET,
                url, callback, getContext(),
                mActivity.getmSnackBarDisplayer(), mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();

    }

    private void goToAddReview() {

        Log.d(TAG, "goToAddReview - Start");

        AddReviewFragment addReviewFragment = new AddReviewFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(AddReviewFragment.EXTRA_SHOP_OBJECT, Parcels.wrap(mShop));
        addReviewFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.main_frame, addReviewFragment, addReviewFragment.TAG);

        fragmentTransaction.addToBackStack(TAG);

        fragmentTransaction.commit();

        Log.d(TAG, "goToAddReview - End");
    }

    private void goToLogin() {

        Log.d(TAG, "goToAddReview - Start");

        LoginFragment loginFragment = new LoginFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(AddReviewFragment.EXTRA_SHOP_OBJECT, Parcels.wrap(mShop));
        loginFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.main_frame, loginFragment, loginFragment.TAG);

        fragmentTransaction.addToBackStack(TAG);

        fragmentTransaction.commit();

        Log.d(TAG, "goToAddReview - End");
    }

}
