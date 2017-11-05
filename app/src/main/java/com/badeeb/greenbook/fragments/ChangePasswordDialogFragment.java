package com.badeeb.greenbook.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.models.JsonRequest;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.models.ProfileEditInquiry;
import com.badeeb.greenbook.models.User;
import com.badeeb.greenbook.network.AuthorizedCallback;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.AppSettings;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.UiUtils;
import com.badeeb.greenbook.shared.Utils;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChangePasswordDialogFragment extends DialogFragment {

    public static final String TAG = ChangePasswordDialogFragment.class.getSimpleName();

    private MainActivity mActivity;
    private ProgressDialog mProgressDialog;
    private FragmentManager mFragmentManager;
    private Context mContext;
    private AppSettings mAppSettings;

    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmNewPassword;
    private TextView tvCancel;
    private TextView tvChange;

    public ChangePasswordDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_password_dialog, container, false);

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
        mAppSettings = AppSettings.getInstance();

        initUi(view);

        setupListeners(view);

        Log.d(TAG, "init - End");
    }

    private void initUi(View view) {
        etCurrentPassword =(EditText) view.findViewById(R.id.etCurrentPassword);
        etNewPassword = (EditText) view.findViewById(R.id.etNewPassword);
        etConfirmNewPassword = (EditText) view.findViewById(R.id.etConfirmNewPassword);
        tvCancel = (TextView) view.findViewById(R.id.tvCancel);
        tvChange = (TextView) view.findViewById(R.id.tvChange);
    }


    private void setupListeners(View view) {

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        tvChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareChangePassword();
            }
        });
    }

    private boolean validateInput() {

        boolean valid = true;
        int redColor = getResources().getColor(R.color.red);

        if (etCurrentPassword.getText().toString().isEmpty()) {
            // Empty Password
            etCurrentPassword.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else if (! Utils.isPasswordValid(etCurrentPassword.getText().toString())) {
            etCurrentPassword.setTextColor(redColor);
            etCurrentPassword.setError(getString(R.string.error_invalid_password));
            valid = false;
        }

        if (etNewPassword.getText().toString().isEmpty()) {
            // Empty Password
            etNewPassword.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else if (! Utils.isPasswordValid(etNewPassword.getText().toString())) {
            etNewPassword.setTextColor(redColor);
            etNewPassword.setError(getString(R.string.error_invalid_password));
            valid = false;
        }

        if(etConfirmNewPassword.getText().toString().isEmpty()){
            etConfirmNewPassword.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else if(!etConfirmNewPassword.getText().toString().equals(etNewPassword.getText().toString())){
            etConfirmNewPassword.setTextColor(redColor);
            etNewPassword.setTextColor(redColor);
            etConfirmNewPassword.setError(getString(R.string.error_password_not_match));
            valid = false;
        }

        return valid;
    }

    private void prepareChangePassword() {

        if (! validateInput())
            return;

        mProgressDialog.show();
        callChangePasswordApi();
    }

    private void callChangePasswordApi() {

        String url = Constants.BASE_URL + "/users";

        Log.d(TAG, "callChangePasswordApi - url: " + url);

        AuthorizedCallback<JsonResponse<Object>> callback = new AuthorizedCallback<JsonResponse<Object>>(mActivity.getUser().getToken()) {
            @Override
            public void onSuccess(JsonResponse<Object> jsonResponse) {
                Log.d(TAG, "callChangePasswordApi - onSuccess - Start");

                if (jsonResponse.getJsonMeta().getStatus().equals("200") ) {
                    mActivity.getmSnackBarDisplayer().displayError("Password is changed");

                    dismiss();

                    mAppSettings.clearUserInfo();

                    goToLoginFragment();
                }
                else {
                    mActivity.getmSnackBarDisplayer().displayError("Unexpected error, please try later.");
                }

                mActivity.hideKeyboard();

                mProgressDialog.dismiss();

                Log.d(TAG, "callChangePasswordApi - onSuccess - End");
            }

            @Override
            public void onError() {
                Log.d(TAG, "callChangePasswordApi - onError - Start");

                mProgressDialog.dismiss();

                Log.d(TAG, "callChangePasswordApi - onError - End");
            }
        };

        // Prepare response type
        Type responseType = new TypeToken<JsonResponse<Object>>() {}.getType();

        ProfileEditInquiry profileEditInquiry =  new ProfileEditInquiry();
        profileEditInquiry.setUser(mActivity.getUser());
        profileEditInquiry.getUser().setPassword(etNewPassword.getText().toString());

        JsonRequest<ProfileEditInquiry> request = new JsonRequest<>(profileEditInquiry);

        VolleyWrapper<JsonRequest<ProfileEditInquiry>, JsonResponse<Object>> volleyWrapper =
                new VolleyWrapper<>(request, responseType, Request.Method.PUT, url,
                        callback, getContext(), mActivity.getmSnackBarDisplayer(), mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();
    }

    private void goToLoginFragment() {
        Log.d(TAG, "goToLogin - Start");

        LoginFragment loginFragment = new LoginFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.main_frame, loginFragment, loginFragment.TAG);

//        fragmentTransaction.addToBackStack(TAG);

        fragmentTransaction.commit();

        Log.d(TAG, "goToLogin - End");
    }

}
