package com.badeeb.greenbook.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.shared.AppSettings;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.UiUtils;
import com.badeeb.greenbook.shared.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotLoggedInProfileFragment extends Fragment {

    public static final String TAG = NotLoggedInProfileFragment.class.getSimpleName();

    private MainActivity mActivity;
    private ProgressDialog mProgressDialog;
    private FragmentManager mFragmentManager;
    private Context mContext;

    private TextView tvInviteFriend;
    private TextView tvAppRate;
    private TextView tvLogin;
    private TextView tvSignup;
    private TextView tvFacebbokLogin;
    private TextView tvGoogleLogin;

    public NotLoggedInProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");

        if (container != null) {
            // this code is used to prevent fragment overlapping
            container.removeAllViews();
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_not_logged_in_profile, container, false);

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

        initUi(view);

        setupListeners();

        Log.d(TAG, "init - End");
    }

    private void initUi(View view) {
        tvInviteFriend = (TextView) view.findViewById(R.id.tvInviteFriend);
        tvAppRate = (TextView) view.findViewById(R.id.tvAppRate);
        tvLogin = (TextView) view.findViewById(R.id.tvLogin);
        tvSignup = (TextView) view.findViewById(R.id.tvSignup);
        tvFacebbokLogin = (TextView) view.findViewById(R.id.tvFacebbokLogin);
        tvGoogleLogin = (TextView) view.findViewById(R.id.tvGoogleLogin);

        mActivity.showBottomNavigationActionBar();
    }

    private void setupListeners() {
        Log.d(TAG, "setupListeners - End");

        tvInviteFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.shareAppWithFriend(mContext, getResources());
            }
        });

        tvAppRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.rateAppOnPlayStore(mContext);
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });

        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSignup();
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

    private void goToLogin() {
        LoginFragment loginFragment = new LoginFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, loginFragment, loginFragment.TAG);
        fragmentTransaction.addToBackStack(TAG);
        fragmentTransaction.commit();
    }

    private void goToSignup() {
        SignUpFragment signUpFragment = new SignUpFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, signUpFragment, signUpFragment.TAG);
        fragmentTransaction.addToBackStack(TAG);
        fragmentTransaction.commit();
    }
}
