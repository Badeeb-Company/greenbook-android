package com.badeeb.greenbook.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.models.JsonRequest;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.models.JsonUser;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.models.User;
import com.badeeb.greenbook.network.NonAuthorizedCallback;
import com.badeeb.greenbook.network.VolleyResponse;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.AppSettings;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.UiUtils;
import com.badeeb.greenbook.shared.Utils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    public static final String TAG = LoginFragment.class.getSimpleName();

    private MainActivity mActivity;
    private FragmentManager mFragmentManager;
    private User mUser;
    private AppSettings mAppSettings;

    private Shop mShop;

    private EditText mEmail;
    private EditText mPassword;
    private ProgressDialog mProgressDialog;

    private TextView tvLogin;
    private TextView tvSignup;
    private TextView tvSkip;
    private TextView tvForgetPassword;
    private TextView tvFacebbokLogin;

    private TextView tvGoogleLogin;




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
        mFragmentManager = getFragmentManager();
        mUser = new User();
        mAppSettings = AppSettings.getInstance();
        mProgressDialog = UiUtils.createProgressDialog(mActivity);

        initUi(view);

        setupListeners(view);

        Log.d(TAG, "init - End");
    }

    private void initUi(View view) {
        mEmail = (EditText) view.findViewById(R.id.etEmail);
        mPassword = (EditText) view.findViewById(R.id.etPassword);
        tvLogin = (TextView) view.findViewById(R.id.tvLogin);
        tvSignup = (TextView) view.findViewById(R.id.tvSignup);
        tvSkip = (TextView) view.findViewById(R.id.tvSkip);
        tvForgetPassword = (TextView) view.findViewById(R.id.tvForgetPassword);
        tvGoogleLogin = (TextView) view.findViewById(R.id.tvGoogleLogin);
        tvFacebbokLogin = (TextView) view.findViewById(R.id.tvFacebbokLogin);

        mActivity.hideToolbar();
        mActivity.hideBottomNavigationActionBar();
    }

    private void setupListeners(View view) {
        Log.d(TAG, "setupListeners - Start");

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgressDialog.show();

                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                mUser.setEmail(email);
                mUser.setPassword(password);
                mUser.setAccountType("normal");

                callLoginApi();
            }
        });

        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSignup();
            }
        });

        tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.hideKeyboard();
                goToShopSearch();
            }
        });

        tvForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotToForgetPasswordDialog();
            }
        });

        tvGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.prepareGooglePlusLogin();
            }
        });

        tvFacebbokLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.prepareFacebookLogin();
            }
        });

        Log.d(TAG, "setupListeners - End");
    }

    private void goToSignup() {

        SignUpFragment signupFragment = new SignUpFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, signupFragment, signupFragment.TAG);
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

        NonAuthorizedCallback<JsonResponse<JsonUser>> callback = new NonAuthorizedCallback<JsonResponse<JsonUser>>() {
            @Override
            public void onSuccess(JsonResponse<JsonUser> jsonResponse) {
                Log.d(TAG, "callLoginApi - onSuccess - Start");

                mUser = jsonResponse.getResult().getUser();

                mAppSettings.saveUser(mUser);
                mActivity.setUser(mUser);

                if (mActivity.getState().equals(Constants.GO_TO_ADD_REVIEW)) {
                    // pop back stack
                    goToReviewsTab();
                }
                else {
                    // Clear back stack
                    mActivity.clearBackStack();
                    goToShopSearch();
                }

                mActivity.updateFavouriteSet();

                mActivity.hideKeyboard();

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
        Type responseType = new TypeToken<JsonResponse<JsonUser>>() {}.getType();

        JsonRequest<User> request = new JsonRequest<>(mUser);

        VolleyWrapper<JsonRequest<User>, JsonResponse<JsonUser>> volleyWrapper = new VolleyWrapper<>(request, responseType, Request.Method.POST, url,
                        callback, getContext(), mActivity.getmSnackBarDisplayer(), mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();
    }

    private void goToShopSearch() {
        Fragment fragment = getFragmentManager().findFragmentByTag(ShopSearchFragment.TAG);
        if (fragment != null && fragment instanceof ShopSearchFragment && fragment.isVisible())
            return;

        ShopSearchFragment shopSearchFragment = new ShopSearchFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, shopSearchFragment, shopSearchFragment.TAG);
        fragmentTransaction.commit();

        mActivity.changeNavigationIconsState(R.id.aiSearch);
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

    private void gotToForgetPasswordDialog() {
        ForgetPasswordDialogFragment forgetPasswordDialogFragment = new ForgetPasswordDialogFragment();
        forgetPasswordDialogFragment.setCancelable(false);
        forgetPasswordDialogFragment.show(mFragmentManager, forgetPasswordDialogFragment.TAG);
    }

    public void goToReviewsTab() {
        Log.d(TAG, "goToReviewsTab - Start");

        mFragmentManager.popBackStack();
        mFragmentManager.popBackStack();
        mFragmentManager.popBackStack();
        mFragmentManager.popBackStack();



        ShopDetailsFragment shopDetailsFragment = new ShopDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(ShopDetailsFragment.EXTRA_SHOP_OBJECT, Parcels.wrap(mActivity.getmShopUnderReview()));
        bundle.putInt(ShopDetailsFragment.EXTRA_OPEN_TAB, 2);
        shopDetailsFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, shopDetailsFragment, shopDetailsFragment.TAG);

        fragmentTransaction.addToBackStack(TAG);

        fragmentTransaction.commit();

        mActivity.disconnectPlaceGoogleApiClient();
        Log.d(TAG, "goToReviewsTab - End");
    }

}
