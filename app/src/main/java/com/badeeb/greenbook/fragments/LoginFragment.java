package com.badeeb.greenbook.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.models.LoginInquiry;
import com.badeeb.greenbook.models.User;
import com.badeeb.greenbook.network.NonAuthorizedCallback;
import com.badeeb.greenbook.network.VolleyResponse;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.UiUtils;
import com.badeeb.greenbook.shared.Utils;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    public static final String TAG = LoginFragment.class.getSimpleName();

    private MainActivity mActivity;
    private User mUser;

    private EditText mEmail;
    private EditText mPassword;
    private ProgressDialog mProgressDialog;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        init(view);

        Log.d(TAG, "onCreateView - End");

        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        mActivity = (MainActivity) getActivity();
        mUser = new User();

        mEmail = view.findViewById(R.id.etEmail);
        mPassword = view.findViewById(R.id.etPassword);
        mProgressDialog = UiUtils.createProgressDialog(mActivity);

        mActivity.hideToolbar();

        setupListeners(view);

        Log.d(TAG, "init - End");
    }

    private void setupListeners(View view) {
        Log.d(TAG, "setupListeners - Start");

        TextView login = view.findViewById(R.id.tvLogin);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgressDialog.show();

                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                mUser.setEmail(email);
                mUser.setPassword(password);

                callLoginApi();
            }
        });

        TextView signup = view.findViewById(R.id.tvSignup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSignup();
            }
        });

        Log.d(TAG, "setupListeners - End");
    }

    private void goToSignup() {

        SignUpFragment signupFragment = new SignUpFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_frame, signupFragment, signupFragment.TAG);
        fragmentTransaction.addToBackStack(TAG);
        fragmentTransaction.commit();

    }

    private void callLoginApi() {

        if (! validateInput()) {
            mProgressDialog.dismiss();
            return;
        }

        String url = Constants.BASE_URL + "/users/sign_in";

        Log.d(TAG, "callLoginApi - url: " + url);

        NonAuthorizedCallback<JsonResponse<LoginInquiry>> callback = new NonAuthorizedCallback<JsonResponse<LoginInquiry>>() {
            @Override
            public void onSuccess(JsonResponse<LoginInquiry> jsonResponse) {
                Log.d(TAG, "callLoginApi - onSuccess - Start");

                goToSearch();

                mProgressDialog.dismiss();

                Log.d(TAG, "callLoginApi - onSuccess - End");
            }

            @Override
            public void onError() {
                Log.d(TAG, "callLoginApi - onError - Start");

                mProgressDialog.dismiss();

                Log.d(TAG, "callLoginApi - onError - End");
            }
        };

        // Prepare response type
        Type responseType = new TypeToken<JsonResponse<LoginInquiry>>() {}.getType();

        VolleyWrapper<User, JsonResponse<LoginInquiry>> volleyWrapper = new VolleyWrapper<>(mUser, responseType, Request.Method.POST, url, callback, getContext());
        volleyWrapper.execute();
    }

    private void goToSearch() {

    }

    private boolean validateInput() {

        boolean valid = true;

        if (mEmail.getText().toString().isEmpty()) {
            // Empty Email
            mEmail.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else if (! Utils.isEmailValid(mEmail.getText().toString())) {
            // Email is wrong
            mEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        }

        if (mPassword.getText().toString().isEmpty()) {
            // Empty Password
            mPassword.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else if (! Utils.isPasswordValid(mPassword.getText().toString())) {
            mPassword.setError(getString(R.string.error_invalid_password));
            valid = false;
        }

        return valid;
    }

}
