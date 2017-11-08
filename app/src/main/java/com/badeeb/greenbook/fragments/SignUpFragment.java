package com.badeeb.greenbook.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.models.JsonRequest;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.models.JsonUser;
import com.badeeb.greenbook.models.User;
import com.badeeb.greenbook.network.NonAuthorizedCallback;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.AppSettings;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.ErrorDisplayHandler;
import com.badeeb.greenbook.shared.OnPermissionsGrantedHandler;
import com.badeeb.greenbook.shared.PermissionsChecker;
import com.badeeb.greenbook.shared.UiUtils;
import com.badeeb.greenbook.shared.Utils;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.UUID;


public class SignUpFragment extends Fragment {

    public final static String TAG = SignUpFragment.class.getName();
    private final static int IMAGE_GALLERY_REQUEST = 10;

    private MainActivity mActivity;
    private FragmentManager mFragmentManager;
    private User mUser;
    private OnPermissionsGrantedHandler onStoragePermissionGrantedHandler;
    private ProgressDialog mProgressDialog;
    private FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();

    private Button bSignUp;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private RoundedImageView rivProfileImage;
    private TextView tvUploadImage;

    private boolean mPhotoChosen;
    private Uri mPhotoUri;
    private String mUploadedPhotoUrl;


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

        if (container != null) {
            // this code is used to prevent fragment overlapping
            container.removeAllViews();
        }

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_sign_up, container, false);

        init(view);

        return view;
    }

    public void init(View view){
        mActivity = (MainActivity) getActivity();
        mFragmentManager = getFragmentManager();

        mUser = new User();

        onStoragePermissionGrantedHandler = createOnStoragePermissionGrantedHandler();
        mProgressDialog = UiUtils.createProgressDialog(getActivity(), "Signing up...");
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        bSignUp = (Button) view.findViewById(R.id.signUp_bttn);
        etUsername = (EditText) view.findViewById(R.id.username);
        etPassword = (EditText) view.findViewById(R.id.password);
        etEmail = (EditText) view.findViewById(R.id.email);
        etConfirmPassword = (EditText) view.findViewById(R.id.confirmPassword);
        rivProfileImage = (RoundedImageView) view.findViewById(R.id.rivProfilePhoto);
        tvUploadImage = (TextView) view.findViewById(R.id.tvUploadImage);

        mActivity.hideBottomNavigationActionBar();

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

                if (mPhotoChosen) {
                    if(Utils.isAllowedFileSize(mActivity, mPhotoUri)) {
                        uploadToFirebase();
                    }
                }else {
                    callSignUp();
                }
            }
        });

        tvUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionsChecker.checkPermissions(SignUpFragment.this, onStoragePermissionGrantedHandler,
                        IMAGE_GALLERY_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });

        Log.d(TAG, "setupListeners - End");
    }

    private void uploadToFirebase() {
        Log.d(TAG, "uploadToFirebase - Start");
        InputStream inputStream = null;
        try {
            inputStream = mActivity.getContentResolver().openInputStream(mPhotoUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] inputData = Utils.getBytes(inputStream);

        StorageReference storageRef = mFirebaseStorage.getReferenceFromUrl("gs://greenbook-e6f97.appspot.com/");
        StorageReference imageReference = storageRef.child("clients/" + UUID.randomUUID());
        final ProgressDialog uploadPhotoProgressDialog = UiUtils.createProgressDialog(mActivity, "Uploading photo...");

        uploadPhotoProgressDialog.show();

        UploadTask uploadTask = imageReference.putBytes(inputData);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "uploadToFirebase - upload photo failed !!");

                mActivity.getmSnackBarDisplayer().displayError(getString(R.string.error_uploading_image));
                uploadPhotoProgressDialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "uploadToFirebase - photo uploaded successfully.");

                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                mUploadedPhotoUrl = downloadUrl.toString();

                Log.d(TAG, "uploadToFirebase - mUploadedPhotoUrl: "+mUploadedPhotoUrl);

                uploadPhotoProgressDialog.dismiss();
                mPhotoChosen = false;
                callSignUp();
            }
        });
        Log.d(TAG, "uploadToFirebase - End");
    }

    private void callSignUp() {
        Log.d(TAG, "callSignup - Start");
        mProgressDialog.show();

        reflectUiData();

        NonAuthorizedCallback<JsonResponse<JsonUser>> signUpCallBack = new NonAuthorizedCallback<JsonResponse<JsonUser>>() {
            @Override
            public void onSuccess(JsonResponse<JsonUser> jsonResponse) {
                Log.d(TAG, "signup - onResponse - Start");


                Log.d(TAG, "signup - onResponse - Status: " + jsonResponse.getJsonMeta().getStatus());
                Log.d(TAG, "signup - onResponse - Message: " + jsonResponse.getJsonMeta().getMessage());

                // check status  code of response
                if (jsonResponse.getJsonMeta().getStatus().equals("200")) {
                    // Success login
                    // Move to next screen --> Main Activity
                    UiUtils.showDialog(getContext(), R.style.DialogTheme, R.string.success_sign_up, R.string.ok_btn_dialog, null);
                    goToLogin();
                }
                else {
                    // Invalid Signup
                    mActivity.getmSnackBarDisplayer().displayError(getString(R.string.signup_error));
                }

                // Disable Progress bar
                mProgressDialog.dismiss();

                Log.d(TAG, "signup - onResponse - End");
            }

            @Override
            public void onError() {
                Log.d(TAG, "SignUp request failed !!");
                mProgressDialog.dismiss();
            }
        };

        // Prepare response type
        String url = Constants.BASE_URL+"/users";
        Type responseType = new TypeToken<JsonResponse<JsonUser>>() {}.getType();

        JsonUser jsonUser = new JsonUser();
        jsonUser.setUser(mUser);

        JsonRequest<JsonUser> jsonRequest = new JsonRequest<JsonUser>(jsonUser);

        // Create Gson object
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        final Gson gson = gsonBuilder.create();

        Log.d(TAG, "execute - Json Request"+ gson.toJson(jsonRequest));

        VolleyWrapper<JsonRequest<JsonUser>, JsonResponse<JsonUser>> volleyWrapper = new VolleyWrapper<>(jsonRequest, responseType,Request.Method.POST,
                                                                                        url, signUpCallBack, getContext(), mActivity.getmSnackBarDisplayer(),
                                                                                        mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();

        Log.d(TAG, "callSignup - End");
    }

    private void reflectUiData(){
        mUser.setName(etUsername.getText().toString());
        mUser.setEmail(etEmail.getText().toString());
        mUser.setPassword(etPassword.getText().toString());
        mUser.setImageURL(mUploadedPhotoUrl);
    }

    private void goToLogin() {
        LoginFragment loginFragment = new LoginFragment();

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, loginFragment, loginFragment.TAG);

        fragmentTransaction.commit();
    }

    private boolean validateInput() {

        boolean valid = true;
        View mainActivityView = mActivity.findViewById(R.id.main_frame);
        int redColor = getResources().getColor(R.color.red);

        if (etEmail.getText().toString().isEmpty()) {
            // Empty Email
            etEmail.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else if (! Utils.isEmailValid(etEmail.getText().toString())) {
            // Email is wrong
            etEmail.setTextColor(redColor);
            etEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        }

        if (etPassword.getText().toString().isEmpty()) {
            // Empty Password
            etPassword.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else if (! Utils.isPasswordValid(etPassword.getText().toString())) {
            etPassword.setTextColor(redColor);
            etPassword.setError(getString(R.string.error_invalid_password));
            valid = false;
        }

        if(etConfirmPassword.getText().toString().isEmpty()){
            etConfirmPassword.setError(getString(R.string.error_field_required));
            valid = false;
        }else if(!etConfirmPassword.getText().toString().equals(etPassword.getText().toString())){
            etConfirmPassword.setTextColor(redColor);
            etPassword.setTextColor(redColor);
            etConfirmPassword.setError(getString(R.string.error_password_not_match));
            valid = false;
        }

        if (etUsername.getText().toString().isEmpty()) {
            // Empty name
            etUsername.setError(getString(R.string.error_field_required));
            valid = false;
        }


        return valid;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case IMAGE_GALLERY_REQUEST: {
                if (PermissionsChecker.permissionsGranted(grantResults)) {
                    onStoragePermissionGrantedHandler.onPermissionsGranted();
                }
            }
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                mActivity.getmSnackBarDisplayer().displayError(getString(R.string.error_uploading_image));
                return;
            }
            mPhotoChosen = true;
            mPhotoUri = data.getData();
            Glide.with(mActivity)
                    .load(mPhotoUri)
                    .asBitmap()
                    .into(rivProfileImage);
        }
    }

}
