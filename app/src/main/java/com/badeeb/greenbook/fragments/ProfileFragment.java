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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.shared.AppSettings;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.UiUtils;
import com.badeeb.greenbook.shared.Utils;
import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private MainActivity mActivity;
    private ProgressDialog mProgressDialog;
    private FragmentManager mFragmentManager;
    private Context mContext;
    private AppSettings mAppSettings;

    private ImageView ivToolbarBack;
    private ImageView rivImage;
    private TextView tvName;
    private TextView tvEmail;
    private TextView tvEditProfile;
    private TextView tvLogout;
    private TextView tvInviteFriend;
    private TextView tvAppRate;
    private TextView tvChangePassword;
    private View vChangePasswordSeparator;


    public ProfileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        init(view);

        Log.d(TAG, "onCreateView - End");

        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - End");

        mActivity = (MainActivity) getActivity();
        mProgressDialog = UiUtils.createProgressDialog(mActivity);
        mFragmentManager = getFragmentManager();
        mContext = getContext();
        mAppSettings = AppSettings.getInstance();

        initUi(view);

        setupListeners();

        Log.d(TAG, "init - End");
    }

    private void initUi(View view) {

        rivImage = (ImageView) view.findViewById(R.id.rivImage);
        Glide.with(mContext)
                .load(mActivity.getUser().getImageURL())
                .asBitmap()
                .placeholder(R.drawable.def_usr_img)
                .into(rivImage);

        tvName = (TextView) view.findViewById(R.id.tvName);
        tvName.setText(mActivity.getUser().getName());

        tvEmail = (TextView) view.findViewById(R.id.tvEmail);
        tvEmail.setText(mActivity.getUser().getEmail());

        tvEditProfile =(TextView)  view.findViewById(R.id.tvEditProfile);
        tvLogout = (TextView) view.findViewById(R.id.tvLogout);
        tvInviteFriend =(TextView)  view.findViewById(R.id.tvInviteFriend);
        tvAppRate = (TextView) view.findViewById(R.id.tvAppRate);
        tvChangePassword = (TextView) view.findViewById(R.id.tvChangePassword);
        vChangePasswordSeparator = view.findViewById(R.id.vChangePasswordSeparator);

        if (! mActivity.getUser().getAccountType().equals("email")) {
            tvChangePassword.setVisibility(View.GONE);
            vChangePasswordSeparator.setVisibility(View.GONE);
        }

        mActivity.setProfileButtonAsChecked();
        mActivity.showBottomNavigationActionBar();
    }

    private void setupListeners() {
        Log.d(TAG, "setupListeners - Start");


        tvEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "setupListeners - tvEditProfile - onClick");
                goToEditProfileDetailsFragment();
            }
        });

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "setupListeners - tvLogout - onClick");
                logout();
            }
        });

        tvInviteFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "setupListeners - tvInviteFriend - onClick");
                Utils.shareAppWithFriend(mContext, getResources());
            }
        });

        tvAppRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.rateAppOnPlayStore(mContext);
            }
        });

        tvChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChangePasswordDialog();
            }
        });

        Log.d(TAG, "setupListeners - End");
    }


    private void goToEditProfileDetailsFragment() {

        EditProfileDetailsFragment editProfileDetailsFragment = new EditProfileDetailsFragment();

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, editProfileDetailsFragment, editProfileDetailsFragment.TAG);

        fragmentTransaction.addToBackStack(TAG);

        fragmentTransaction.commit();
    }

    private void goToChangePasswordDialog() {
        ChangePasswordDialogFragment changePasswordDialogFragment = new ChangePasswordDialogFragment();
        changePasswordDialogFragment.setCancelable(false);
        changePasswordDialogFragment.show(mFragmentManager, changePasswordDialogFragment.TAG);
    }

    private void logout() {
        mAppSettings.clearUserInfo();
        mAppSettings.clearLoginCounter();
        mActivity.setUser(null);
        mActivity.updateFavouriteSet();
//        goToShopSearchFragment();
        goToNotLoggedInProfileFragment();
    }

    private void goToNotLoggedInProfileFragment() {

        mFragmentManager.popBackStack();

        NotLoggedInProfileFragment notLoggedInProfileFragment = new NotLoggedInProfileFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, notLoggedInProfileFragment, NotLoggedInProfileFragment.TAG);
        fragmentTransaction.addToBackStack(TAG);
        fragmentTransaction.commit();

        mActivity.changeNavigationIconsState(R.id.aiProfile);
    }

    private void goToShopSearchFragment() {

        ShopSearchFragment shopSearchFragment = new ShopSearchFragment();

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, shopSearchFragment, shopSearchFragment.TAG);

        fragmentTransaction.commit();

        mActivity.changeNavigationIconsState(R.id.aiSearch);
    }

}
