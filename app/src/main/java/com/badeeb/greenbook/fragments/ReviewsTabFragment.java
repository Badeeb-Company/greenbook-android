package com.badeeb.greenbook.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.badeeb.greenbook.models.ReviewManage;
import com.badeeb.greenbook.models.ReviewsInquiry;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.network.AuthorizedCallback;
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
    private Context mContext;
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
        mContext = getContext();
        mProgressDialog = UiUtils.createProgressDialog(mActivity);
        mFragmentManager = mActivity.getSupportFragmentManager();
        mReviewsList = new ArrayList<>();
        mFabAddReview = (FloatingActionButton) view.findViewById(R.id.fabAddReview);

        loadBundleData();

        initRecyclerView(view);

        setupListeners();

        prepareReviewsList();

        Log.d(TAG, "init - End");
    }

    private void initRecyclerView(View view) {

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rvReviews);
        mReviewRecyclerViewAdapter = new ReviewRecyclerViewAdapter(getContext(), mShop, mReviewsList, this, mActivity.getUser(), mActivity);
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
                    mActivity.setmShopUnderReview(mShop);
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

        UiUtils.showDialog(mContext, R.style.DialogTheme,
                R.string.ask_user_to_login_msg,
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

                    disableOrEnableAddReview();

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

        ReviewMngFragment reviewMngFragment = new ReviewMngFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(ReviewMngFragment.EXTRA_SHOP_OBJECT, Parcels.wrap(mShop));
        bundle.putString(ReviewMngFragment.EXTRA_ACTION, ReviewMngFragment.ACTION_ADD);
        reviewMngFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, reviewMngFragment, reviewMngFragment.TAG);

        fragmentTransaction.addToBackStack(TAG);

        fragmentTransaction.commit();

        Log.d(TAG, "goToAddReview - End");
    }

    private void goToLogin() {

        Log.d(TAG, "goToAddReview - Start");

        LoginFragment loginFragment = new LoginFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(ReviewMngFragment.EXTRA_SHOP_OBJECT, Parcels.wrap(mShop));
        loginFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, loginFragment, loginFragment.TAG);

        fragmentTransaction.addToBackStack(TAG);

        fragmentTransaction.commit();

        Log.d(TAG, "goToAddReview - End");
    }

    public void prepareDeleteReview(final Review review) {
        Log.d(TAG, "goToAddReview - Start");

        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                prepareDeleteReviewApi(review);
            }
        };

        DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // TODO
            }
        };

        UiUtils.showDialog(getContext(), R.style.DialogTheme, R.string.review_delete_title, R.string.review_delete_msg,
                R.string.yes_msg, positiveListener, R.string.no_msg, negativeListener);

        Log.d(TAG, "goToAddReview - End");
    }

    private void prepareDeleteReviewApi(Review review) {
        mProgressDialog.show();
        callDeleteReviewApi(review);
    }

    private void callDeleteReviewApi(Review review) {

        String url = Constants.BASE_URL + "/shops/" + mShop.getId() + "/reviews/" + review.getId();

        Log.d(TAG, "callDeleteReviewApi - url: " + url);

        AuthorizedCallback<JsonResponse<ReviewManage>> callback = new AuthorizedCallback<JsonResponse<ReviewManage>>(mActivity.getUser().getToken()) {
            @Override
            public void onSuccess(JsonResponse<ReviewManage> jsonResponse) {
                Log.d(TAG, "callDeleteReviewApi - onSuccess - Start");

                UiUtils.showDialog(mContext, R.style.DialogTheme, R.string.succes_review_deletion, R.string.ok_btn_dialog, null);

                // Refresh reviews list
                prepareReviewsList();

                mActivity.hideKeyboard();

                mProgressDialog.dismiss();

                Log.d(TAG, "callDeleteReviewApi - onSuccess - End");
            }

            @Override
            public void onError() {
                Log.d(TAG, "callDeleteReviewApi - onError - Start");

                mProgressDialog.dismiss();

                Log.d(TAG, "callDeleteReviewApi - onError - End");
            }
        };

        // Prepare response type
        Type responseType = new TypeToken<JsonResponse<ReviewManage>>() {}.getType();
        ReviewManage reviewManage = new ReviewManage();
        JsonRequest<ReviewManage> request = new JsonRequest<>(reviewManage);

        VolleyWrapper<JsonRequest<ReviewManage>, JsonResponse<ReviewManage>> volleyWrapper = new VolleyWrapper<>
                (request, responseType, Request.Method.DELETE,
                        url, callback, getContext(),
                        mActivity.getmSnackBarDisplayer(), mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();
    }

    public void goToEditReview(Review review) {
        Log.d(TAG, "goToEditReview - Start");

        ReviewMngFragment reviewMngFragment = new ReviewMngFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(ReviewMngFragment.EXTRA_SHOP_OBJECT, Parcels.wrap(mShop));
        bundle.putString(ReviewMngFragment.EXTRA_ACTION, ReviewMngFragment.ACTION_EDIT);
        bundle.putParcelable(ReviewMngFragment.EXTRA_REVIEW_OBJECT, Parcels.wrap(review));
        reviewMngFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, reviewMngFragment, reviewMngFragment.TAG);

        fragmentTransaction.addToBackStack(TAG);

        fragmentTransaction.commit();

        Log.d(TAG, "goToEditReview - End");
    }

    public void goToShopOwnerReply(Review review) {
        Log.d(TAG, "goToShopOwnerReply - Start");

        ReviewMngFragment reviewMngFragment = new ReviewMngFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(ReviewMngFragment.EXTRA_SHOP_OBJECT, Parcels.wrap(mShop));
        bundle.putString(ReviewMngFragment.EXTRA_ACTION, ReviewMngFragment.ACTION_OWNER_REPLY);
        bundle.putParcelable(ReviewMngFragment.EXTRA_REVIEW_OBJECT, Parcels.wrap(review));
        reviewMngFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, reviewMngFragment, reviewMngFragment.TAG);

        fragmentTransaction.addToBackStack(TAG);

        fragmentTransaction.commit();

        Log.d(TAG, "goToShopOwnerReply - End");
    }

    private void disableOrEnableAddReview() {

        if (mActivity.getUser() != null) {
            boolean userReviewExist = false;
            for (Review review : mReviewsList) {
                if (review.getUser().getId() == mActivity.getUser().getId()) {
                    userReviewExist = true;
                    break;
                }
            }

            if (userReviewExist) {
                mFabAddReview.setVisibility(View.GONE);
            }
            else {
                mFabAddReview.setVisibility(View.VISIBLE);
            }
        }
    }
}
