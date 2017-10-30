package com.badeeb.greenbook.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.models.JsonOwnerReply;
import com.badeeb.greenbook.models.JsonRequest;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.models.Review;
import com.badeeb.greenbook.models.ReviewManage;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.network.AuthorizedCallback;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.UiUtils;
import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;
import com.makeramen.roundedimageview.RoundedImageView;

import org.parceler.Parcels;

import java.lang.reflect.Type;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewMngFragment extends Fragment {

    public final static String TAG = ReviewMngFragment.class.getName();

    public final static String EXTRA_SHOP_OBJECT = "EXTRA_SHOP_OBJECT";
    public final static String EXTRA_ACTION = "EXTRA_ACTION";
    public final static String ACTION_EDIT  = "ACTION_EDIT";
    public final static String ACTION_ADD = "ACTION_ADD";
    public final static String ACTION_OWNER_REPLY = "ACTION_OWNER_REPLY";
    public final static String EXTRA_REVIEW_OBJECT = "EXTRA_REVIEW_OBJECT";

    private MainActivity mActivity;
    private ProgressDialog mProgressDialog;
    private FragmentManager mFragmentManager;
    private Context mContext;
    private Shop mShop;
    private Review mReview;
    private String mAction;


    private ImageView ivToolbarBack;
    private TextView tvToolbarAddReview;
    private RoundedImageView rivImage;
    private TextView tvReviewerName;
    private EditText etReviewDescription;
    private RatingBar rbShopRate;
    private TextView tvToolbarEditReview;
    private TextView tvToolbarShopOwnerTitle;
    private TextView tvToolbarTitle;
    private TextView tvTapAstarToRate;

    public ReviewMngFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mng_review, container, false);

        init(view);

        Log.d(TAG, "onCreateView - End");
        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        mActivity = (MainActivity) getActivity();
        mProgressDialog = UiUtils.createProgressDialog(mActivity);
        mFragmentManager = getFragmentManager();
        mContext = getContext();

        mReview = new Review();

        loadBundleData();

        initUi(view);

        setupListeners();

        Log.d(TAG, "init - End");
    }

    private void loadBundleData() {
        mShop = Parcels.unwrap(getArguments().getParcelable(ReviewMngFragment.EXTRA_SHOP_OBJECT));

        mAction = getArguments().getString(ReviewMngFragment.EXTRA_ACTION);

        if (mAction.equals(ReviewMngFragment.ACTION_EDIT) || ReviewMngFragment.ACTION_OWNER_REPLY.equals(mAction)) {
            // load review
            mReview = Parcels.unwrap(getArguments().getParcelable(ReviewMngFragment.EXTRA_REVIEW_OBJECT));
        }
    }

    private void initUi(View view) {
        ivToolbarBack = view.findViewById(R.id.ivToolbarBack);
        tvToolbarShopOwnerTitle = view.findViewById(R.id.tvToolbarShopOwnerTitle);
        tvToolbarTitle = view.findViewById(R.id.tvToolbarTitle);
        tvToolbarAddReview = view.findViewById(R.id.tvToolbarAddReview);

        rivImage = view.findViewById(R.id.rivImage);
        Glide.with(getContext())
                .load(mActivity.getUser().getImageURL())
                .placeholder(R.drawable.pic_img)
                .into(rivImage);

        tvReviewerName = view.findViewById(R.id.tvReviewerName);
        tvReviewerName.setText(mActivity.getUser().getName());

        etReviewDescription = view.findViewById(R.id.etReviewDescription);
        rbShopRate = view.findViewById(R.id.rbShopRate);

        tvToolbarEditReview = view.findViewById(R.id.tvToolbarEditReview);
        tvTapAstarToRate = view.findViewById(R.id.tvTapAstarToRate);

        if (ReviewMngFragment.ACTION_EDIT.equals(mAction)) {
            tvToolbarAddReview.setVisibility(View.GONE);
            tvToolbarEditReview.setVisibility(View.VISIBLE);

            etReviewDescription.setText(mReview.getDescription());
            rbShopRate.setRating((float) mReview.getRate());
        }
        else if (ReviewMngFragment.ACTION_OWNER_REPLY.equals(mAction)) {
            applyShopOwnerUiChanges();
        }
        else {
            tvToolbarAddReview.setVisibility(View.VISIBLE);
            tvToolbarEditReview.setVisibility(View.GONE);
        }
    }

    private void applyShopOwnerUiChanges() {
        tvToolbarShopOwnerTitle.setVisibility(View.VISIBLE);
        tvToolbarTitle.setVisibility(View.GONE);
        rivImage.setVisibility(View.GONE);
        rbShopRate.setVisibility(View.GONE);
        tvTapAstarToRate.setVisibility(View.GONE);
        tvReviewerName.setText("Shop Owner");
        etReviewDescription.setHint("Write a Reply");
    }

    private void setupListeners() {

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentManager.popBackStack();
            }
        });

        tvToolbarAddReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                prepareAddReview();
            }
        });

        tvToolbarEditReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareEditReview();
            }
        });
    }

    private void prepareAddReview() {
        mProgressDialog.show();

        if (ReviewMngFragment.ACTION_OWNER_REPLY.equals(mAction)) {
            // Add a reply
            callAddOwnerReplyApi();
        }
        else {
            // Add a review
            mReview.setRate(rbShopRate.getRating());
            mReview.setDescription(etReviewDescription.getText().toString());

            callAddReviewApi();
        }
    }

    private boolean validateInput() {

        boolean valid = true;

        if (etReviewDescription.getText().toString().isEmpty()) {
            // Empty Email
            etReviewDescription.setError(getString(R.string.error_field_required));
            valid = false;
        }

//        if (rbShopRate.getRating() == 0) {
//            // Empty Rating
//            DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                }
//            };
//
//            UiUtils.showDialog(getContext(), R.style.DialogTheme,
//                    R.string.GPS_disabled_warning_title, R.string.GPS_disabled_warning_msg,
//                    R.string.ok_btn_dialog, positiveListener);
//            valid = false;
//        }

        return valid;
    }

    private void callAddReviewApi() {

        if (! validateInput()) {
            mProgressDialog.dismiss();
            return;
        }

        String url = Constants.BASE_URL + "/shops/" + mShop.getId() + "/reviews";

        Log.d(TAG, "callAddReviewApi - url: " + url);

        AuthorizedCallback<JsonResponse<ReviewManage>> callback = new AuthorizedCallback<JsonResponse<ReviewManage>>(mActivity.getUser().getToken()) {
            @Override
            public void onSuccess(JsonResponse<ReviewManage> jsonResponse) {
                Log.d(TAG, "callAddReviewApi - onSuccess - Start");

                mActivity.getmSnackBarDisplayer().displayError("Thanks");
                mFragmentManager.popBackStack();

                mActivity.hideKeyboard();

                mProgressDialog.dismiss();

                Log.d(TAG, "callAddReviewApi - onSuccess - End");
            }

            @Override
            public void onError() {
                Log.d(TAG, "callAddReviewApi - onError - Start");

                mProgressDialog.dismiss();

                Log.d(TAG, "callAddReviewApi - onError - End");
            }
        };

        // Prepare response type
        Type responseType = new TypeToken<JsonResponse<ReviewManage>>() {}.getType();

        ReviewManage reviewManage = new ReviewManage();
        reviewManage.setReview(mReview);

        JsonRequest<ReviewManage> request = new JsonRequest<>(reviewManage);

        VolleyWrapper<JsonRequest<ReviewManage>, JsonResponse<ReviewManage>> volleyWrapper =
                new VolleyWrapper<>(request, responseType, Request.Method.POST, url,
                callback, getContext(), mActivity.getmSnackBarDisplayer(), mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();

    }

    private void prepareEditReview() {
        mProgressDialog.show();

        mReview.setRate(rbShopRate.getRating());
        mReview.setDescription(etReviewDescription.getText().toString());

        callEditReviewApi();
    }

    private void callEditReviewApi() {
        if (! validateInput()) {
            mProgressDialog.dismiss();
            return;
        }

        String url = Constants.BASE_URL + "/shops/" + mShop.getId() + "/reviews/" + mReview.getId();

        Log.d(TAG, "callAddReviewApi - url: " + url);

        AuthorizedCallback<JsonResponse<ReviewManage>> callback = new AuthorizedCallback<JsonResponse<ReviewManage>>(mActivity.getUser().getToken()) {
            @Override
            public void onSuccess(JsonResponse<ReviewManage> jsonResponse) {
                Log.d(TAG, "callAddReviewApi - onSuccess - Start");

                mActivity.getmSnackBarDisplayer().displayError("Review is updated");
                mFragmentManager.popBackStack();

                mActivity.hideKeyboard();

                mProgressDialog.dismiss();

                Log.d(TAG, "callAddReviewApi - onSuccess - End");
            }

            @Override
            public void onError() {
                Log.d(TAG, "callAddReviewApi - onError - Start");

                mProgressDialog.dismiss();

                Log.d(TAG, "callAddReviewApi - onError - End");
            }
        };

        // Prepare response type
        Type responseType = new TypeToken<JsonResponse<ReviewManage>>() {}.getType();

        ReviewManage reviewManage = new ReviewManage();
        reviewManage.setReview(mReview);

        JsonRequest<ReviewManage> request = new JsonRequest<>(reviewManage);

        VolleyWrapper<JsonRequest<ReviewManage>, JsonResponse<ReviewManage>> volleyWrapper =
                new VolleyWrapper<>(request, responseType, Request.Method.PUT, url,
                        callback, getContext(), mActivity.getmSnackBarDisplayer(), mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();
    }

    private void callAddOwnerReplyApi() {

        if (! validateInput()) {
            mProgressDialog.dismiss();
            return;
        }

        String url = Constants.BASE_URL + "/shops/" + mShop.getId() + "/reviews/" + mReview.getId() + "/reply";

        Log.d(TAG, "callAddOwnerReplyApi - url: " + url);

        AuthorizedCallback<JsonResponse<Object>> callback = new AuthorizedCallback<JsonResponse<Object>>(mActivity.getUser().getToken()) {
            @Override
            public void onSuccess(JsonResponse<Object> jsonResponse) {
                Log.d(TAG, "callAddOwnerReplyApi - onSuccess - Start");

                mActivity.getmSnackBarDisplayer().displayError("Reply is published");
                mFragmentManager.popBackStack();

                mActivity.hideKeyboard();

                mProgressDialog.dismiss();

                Log.d(TAG, "callAddOwnerReplyApi - onSuccess - End");
            }

            @Override
            public void onError() {
                Log.d(TAG, "callAddOwnerReplyApi - onError - Start");

                mProgressDialog.dismiss();

                Log.d(TAG, "callAddOwnerReplyApi - onError - End");
            }
        };

        // Prepare response type
        Type responseType = new TypeToken<JsonResponse<Object>>() {}.getType();

        JsonOwnerReply jsonOwnerReply = new JsonOwnerReply();
        jsonOwnerReply.setReply(etReviewDescription.getText().toString());

        JsonRequest<JsonOwnerReply> request = new JsonRequest<>(jsonOwnerReply);

        VolleyWrapper<JsonRequest<JsonOwnerReply>, JsonResponse<Object>> volleyWrapper =
                new VolleyWrapper<>(request, responseType, Request.Method.POST, url,
                        callback, getContext(), mActivity.getmSnackBarDisplayer(), mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();

    }

}
