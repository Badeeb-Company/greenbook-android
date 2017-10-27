package com.badeeb.greenbook.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.models.JsonMeta;
import com.badeeb.greenbook.models.JsonRequest;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.models.User;
import com.badeeb.greenbook.network.NonAuthorizedCallback;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.UiUtils;
import com.badeeb.greenbook.shared.Utils;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;


/**
 * A simple {@link DialogFragment} subclass.
 */
public class ForgetPasswordDialogFragment extends DialogFragment {

    public static final String TAG = ForgetPasswordDialogFragment.class.getSimpleName();

    private MainActivity mActivity;

    private LinearLayout llForgetPassword;
    private LinearLayout llForgetPasswordMessage;
    private EditText mEmail;
    private TextView mCancel;
    private TextView mSend;
    private TextView mOk;
    private ProgressDialog mProgressDialog;

    public ForgetPasswordDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forget_password_dialog, container, false);

        init(view);

        Log.d(TAG, "onCreateView - End");

        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        mActivity = (MainActivity) getActivity();

        llForgetPassword = view.findViewById(R.id.llForgetPassword);
        llForgetPasswordMessage = view.findViewById(R.id.llForgetPasswordMessage);
        mEmail = view.findViewById(R.id.etEmail);
        mCancel = view.findViewById(R.id.tvCancel);
        mSend = view.findViewById(R.id.tvSend);
        mOk = view.findViewById(R.id.tvOk);
        mProgressDialog = UiUtils.createProgressDialog(mActivity);

        setupListeners();

        Log.d(TAG, "init - End");
    }

    private void setupListeners() {
        Log.d(TAG, "setupListeners - Start");

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Validate Email address
                if (validateInput()) {
                    callForgetPasswordApi();
                }
            }
        });

        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        Log.d(TAG, "setupListeners - End");
    }

    private boolean validateInput() {

        boolean valid = true;

        if (mEmail.getText().toString().isEmpty()) {
            // Empty Email
            mEmail.setError(getString(R.string.error_field_required));
            valid = false;
        } else if (!Utils.isEmailValid(mEmail.getText().toString())) {
            // Email is wrong
            mEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        }

        return valid;
    }

    private void callForgetPasswordApi() {

//        String url = Constants.BASE_URL + "/users/password";
        String url = "http://192.168.1.103:8080/api/v1" + "/users/password";

        Log.d(TAG, "callForgetPasswordApi - url: " + url);

        NonAuthorizedCallback<JsonResponse<Object>> callback = new NonAuthorizedCallback<JsonResponse<Object>>() {
            @Override
            public void onSuccess(JsonResponse<Object> jsonResponse) {
                Log.d(TAG, "callForgetPasswordApi - onSuccess - Start");

                JsonMeta jsonMeta = jsonResponse.getJsonMeta();

                // Show success message
                if (jsonMeta.getStatus().equals("200")) {
                    // Success response
                    llForgetPassword.setVisibility(View.GONE);
                    llForgetPasswordMessage.setVisibility(View.VISIBLE);
                }

                mActivity.hideKeyboard(getView());

                mProgressDialog.dismiss();

                Log.d(TAG, "callForgetPasswordApi - onSuccess - End");
            }

            @Override
            public void onError() {
                Log.d(TAG, "callForgetPasswordApi - onError - Start");

                // Show error message
                dismiss();

                mProgressDialog.dismiss();

                Log.d(TAG, "callForgetPasswordApi - onError - End");
            }
        };

        // Prepare response type
        Type responseType = new TypeToken<JsonResponse<Object>>() {}.getType();

        User user = new User();
        user.setEmail(mEmail.getText().toString());

        JsonRequest<User> request = new JsonRequest<>(user);

        VolleyWrapper<JsonRequest<User>, JsonResponse<Object>> volleyWrapper = new VolleyWrapper<>(request, responseType,
                Request.Method.POST, url, callback, getContext(),
                mActivity.getmSnackBarDisplayer(), mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();
    }

}
