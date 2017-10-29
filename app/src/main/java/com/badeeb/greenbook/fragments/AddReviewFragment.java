package com.badeeb.greenbook.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
public class AddReviewFragment extends Fragment {

    public final static String TAG = AddReviewFragment.class.getName();

    public final static String EXTRA_SHOP_OBJECT = "EXTRA_SHOP_OBJECT";

    private MainActivity mActivity;
    private ProgressDialog mProgressDialog;
    private FragmentManager mFragmentManager;
    private Context mContext;
    private Shop mShop;
    private Review mReview;


    private ImageView ivToolbarBack;
    private TextView tvToolbarAddReview;
    private RoundedImageView rivImage;
    private TextView tvReviewerName;
    private EditText etReviewDescription;
    private RatingBar rbShopRate;

    public AddReviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_review, container, false);

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
        mShop = Parcels.unwrap(getArguments().getParcelable(AddReviewFragment.EXTRA_SHOP_OBJECT));
    }

    private void initUi(View view) {
        ivToolbarBack = view.findViewById(R.id.ivToolbarBack);
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
    }

    private void prepareAddReview() {
        mProgressDialog.show();

        mReview.setRate(rbShopRate.getRating());
        mReview.setDescription(etReviewDescription.getText().toString());

        callAddReviewApi();
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

}
