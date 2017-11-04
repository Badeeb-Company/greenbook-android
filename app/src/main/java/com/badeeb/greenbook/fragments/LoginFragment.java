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

import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.lang.reflect.Type;
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
    private GoogleApiClient mGoogleApiClient;
    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener;
    private static final int RC_SIGN_IN = 9001;

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
        mEmail = view.findViewById(R.id.etEmail);
        mPassword = view.findViewById(R.id.etPassword);
        tvLogin = view.findViewById(R.id.tvLogin);
        tvSignup = view.findViewById(R.id.tvSignup);
        tvSkip = view.findViewById(R.id.tvSkip);
        tvForgetPassword = view.findViewById(R.id.tvForgetPassword);
        tvGoogleLogin = view.findViewById(R.id.tvGoogleLogin);
        tvFacebbokLogin = view.findViewById(R.id.tvFacebbokLogin);

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
                prepareGooglePlusLogin();
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

    //------------------------------ Google Login ------------------------------------
    private GoogleApiClient.OnConnectionFailedListener createOnConnectionFailedListener() {
        return new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Log.d(TAG, "createOnConnectionFailedListener - onConnectionFailed: " + connectionResult);
            }
        };
    }

    private void prepareGooglePlusLogin() {
        Log.d(TAG, "googlePlusSignIn - Start");

        mProgressDialog.show();

        onConnectionFailedListener = createOnConnectionFailedListener();

        initGoogleApiClientForLogin();

        showGoogleLoginIntent();

        Log.d(TAG, "googlePlusSignIn - End");
    }

    private void initGoogleApiClientForLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
//                .requestIdToken(getString(R.string.server_client_id))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .enableAutoManage(mActivity /* FragmentActivity */, onConnectionFailedListener /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void showGoogleLoginIntent() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult - Start");

        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult - Google activity result");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

        Log.d(TAG, "onActivityResult - End");
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult - Start");

        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();
            String idToken = acct.getIdToken();

            mUser.setEmail(personEmail);
            mUser.setName(personName);
            mUser.setImageURL(personPhoto.toString());
            mUser.setSocialAcctId(personId);
            mUser.setSocialAcctToken(idToken);
            mUser.setAccountType("google");

            callSocialLoginApi();

        } else {
            // Signed out, show unauthenticated UI.
            mActivity.getmSnackBarDisplayer().displayError("Login error, please try later.");
            mProgressDialog.dismiss();
        }

        stopGoogleApiClientForLogin();

        Log.d(TAG, "handleSignInResult - End");
    }

    private void stopGoogleApiClientForLogin() {
        mGoogleApiClient.clearDefaultAccountAndReconnect();
        mGoogleApiClient.stopAutoManage(mActivity);
        mGoogleApiClient.disconnect();
    }


    private void callSocialLoginApi() {

        String url = Constants.BASE_URL + "/users/social_login";

        Log.d(TAG, "callSocialLoginApi - url: " + url);

        NonAuthorizedCallback<JsonResponse<JsonUser>> callback = new NonAuthorizedCallback<JsonResponse<JsonUser>>() {
            @Override
            public void onSuccess(JsonResponse<JsonUser> jsonResponse) {
                Log.d(TAG, "callSocialLoginApi - onSuccess - Start");

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

                Log.d(TAG, "callSocialLoginApi - onSuccess - End");
            }

            @Override
            public void onError() {
                Log.d(TAG, "callSocialLoginApi - onError - Start");

                mProgressDialog.dismiss();

                Log.d(TAG, "callSocialLoginApi - onError - End");
            }
        };

        // Prepare response type
        Type responseType = new TypeToken<JsonResponse<JsonUser>>() {}.getType();

        JsonUser jsonUser = new JsonUser();
        jsonUser.setUser(mUser);

        JsonRequest<JsonUser> request = new JsonRequest<>(jsonUser);

        VolleyWrapper<JsonRequest<JsonUser>, JsonResponse<JsonUser>> volleyWrapper = new VolleyWrapper<>(request, responseType, Request.Method.POST, url,
                callback, getContext(), mActivity.getmSnackBarDisplayer(), mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();

    }

}
