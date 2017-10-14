package com.badeeb.greenbook.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.models.User;
import com.badeeb.greenbook.shared.AppPreferences;
import com.badeeb.greenbook.shared.OnPermissionsGrantedHandler;
import com.badeeb.greenbook.shared.UiUtils;
import com.makeramen.roundedimageview.RoundedImageView;


public class SignUpFragment extends Fragment {

    public final static String TAG = SignUpFragment.class.getName();
    private final static int IMAGE_GALLERY_REQUEST = 10;

    private MainActivity mActivity;
    private User mUser;
    private OnPermissionsGrantedHandler onStoragePermissionGrantedHandler;
    private ProgressDialog progressDialog;

    private Button bSignUp;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private RoundedImageView rivProfileImage;




    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_sign_up, container, false);

        init(view);

        return view;
    }

    public void init(View view){
        mActivity = (MainActivity) getActivity();
        onStoragePermissionGrantedHandler = createOnStoragePermissionGrantedHandler();
        progressDialog = UiUtils.createProgressDialog(getActivity(), "Signing up...",R.style.DialogTheme);

        bSignUp = (Button) view.findViewById(R.id.signUp_bttn);
        etUsername = (EditText) view.findViewById(R.id.username);
        etPassword = (EditText) view.findViewById(R.id.password);
        etEmail = (EditText) view.findViewById(R.id.email);
        etConfirmPassword = (EditText) view.findViewById(R.id.confirmPassword);
        rivProfileImage = (RoundedImageView) view.findViewById(R.id.rivProfilePhoto);

        setupListeners(view);


    }

    private void setupListeners(View view) {
        Log.d(TAG, "setupListeners - Start");

        bSignUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (! validateInput()) {
                    return;
                }

            }
        });

        Log.d(TAG, "setupListeners - End");
    }

    private boolean validateInput() {

        boolean valid = true;
        View mainActivityView = mActivity.findViewById(R.id.main_frame);

//        if (etEmail.getText().toString().isEmpty()) {
//            // Empty Email
//            UiUtils.showSnackBar(mainActivityView,getResources().getString(R.string.error_email_required),
//                    Snackbar.LENGTH_SHORT,getResources().getColor(R.color.orange));
//            valid = false;
//        }
//        else if (! AppPreferences.isEmailValid(etEmail.getText().toString())) {
//            // Email is wrong
//            etEmail.setBackgroundColor(getResources().getColor(R.color.red));
//            UiUtils.showSnackBar(mainActivityView,getResources().getString(R.string.error_invalid_email),
//                    Snackbar.LENGTH_SHORT,getResources().getColor(R.color.orange));
//            valid = false;
//        }
//
//        if (password.getText().toString().isEmpty()) {
//            // Empty Password
//            password.setError(getString(R.string.error_field_required));
//            valid = false;
//        }
//        else if (! AppPreferences.isPasswordValid(password.getText().toString())) {
//            password.setError(getString(R.string.error_invalid_password));
//            valid = false;
//        }
//
//        if (name.getText().toString().isEmpty()) {
//            // Empty name
//            name.setError(getString(R.string.error_field_required));
//            valid = false;
//        }
//
//        if (phone.getText().toString().isEmpty()) {
//            // Empty Phone
//            phone.setError(getString(R.string.error_field_required));
//            valid = false;
//        }
//        else if (! AppPreferences.isPhoneNumberValid(phone.getText().toString())) {
//            phone.setError(getString(R.string.error_invalid_phone_number));
//            valid = false;
//        }

        return valid;
    }


    private OnPermissionsGrantedHandler createOnStoragePermissionGrantedHandler() {
        return new OnPermissionsGrantedHandler() {
            @Override
            public void onPermissionsGranted() {
                openSelectPhotoScreen();
            }
        };
    }

    private void openSelectPhotoScreen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Photo"), IMAGE_GALLERY_REQUEST);

    }

}
