package com.badeeb.greenbook.fragments;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.gson.reflect.TypeToken;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileDetailsFragment extends Fragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();

    private final static int IMAGE_GALLERY_REQUEST = 10;

    private MainActivity mActivity;
    private ProgressDialog mProgressDialog;
    private FragmentManager mFragmentManager;
    private Context mContext;
    private AppSettings mAppSettings;

    private FirebaseStorage mFirebaseStorage;
    private OnPermissionsGrantedHandler onStoragePermissionGrantedHandler;
    private boolean mPhotoChosen;
    private Uri mPhotoUri;              // Image path on device
    private String mUploadedPhotoUrl; // URL of image after uploading it to firebase

    private RoundedImageView rivImage;
    private TextView tvUploadImage;
    private EditText etFullName;
    private ImageView ivToolbarBack;
    private TextView tvToolbarSave;

    public EditProfileDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_profile_details, container, false);

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

        onStoragePermissionGrantedHandler = createOnStoragePermissionGrantedHandler();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mUploadedPhotoUrl = "";

        initUi(view);

        setupListeners();

        Log.d(TAG, "init - End");
    }

    private void initUi(View view) {
        ivToolbarBack = (ImageView) view.findViewById(R.id.ivToolbarBack);

        tvToolbarSave = (TextView) view.findViewById(R.id.tvToolbarSave);

        rivImage = (RoundedImageView) view.findViewById(R.id.rivImage);
        Glide.with(mContext)
                .load(mActivity.getUser().getImageURL())
                .asBitmap()
                .placeholder(R.drawable.def_usr_img)
                .into(rivImage);

        tvUploadImage = (TextView) view.findViewById(R.id.tvUploadImage);

        etFullName = (EditText) view.findViewById(R.id.etFullName);
        etFullName.setText(mActivity.getUser().getName());

        mActivity.setProfileButtonAsChecked();
        mActivity.hideBottomNavigationActionBar();
    }

    private void setupListeners() {
        Log.d(TAG, "setupListeners - Start");

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "setupListeners - ivToolbarBack - onClick");
                mFragmentManager.popBackStack();
            }
        });

        tvToolbarSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "setupListeners - tvToolbarSave - onClick");
                prepareEditProfileApi();
            }
        });

        etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "setupListeners - etFullName - TextChangedListener - beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "setupListeners - etFullName - TextChangedListener - onTextChanged");

                String data = new String(s.toString());

                if (data.length() > 0 && ! data.equals(mActivity.getUser().getName())) {
                    changeSaveButtonStatus(true);
                }
                else {
                    changeSaveButtonStatus(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "setupListeners - etFullName - TextChangedListener - afterTextChanged");
            }
        });

        tvUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "setupListeners - tvUploadImage - setOnClickListener");

                PermissionsChecker.checkPermissions(EditProfileDetailsFragment.this, onStoragePermissionGrantedHandler,
                        IMAGE_GALLERY_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });


        Log.d(TAG, "setupListeners - End");
    }

    private void changeSaveButtonStatus(boolean enable) {
        if (enable) {
            tvToolbarSave.setEnabled(true);
            tvToolbarSave.setTextColor(getResources().getColor(R.color.white));
        }
        else {
            tvToolbarSave.setEnabled(false);
            tvToolbarSave.setTextColor(getResources().getColor(R.color.gray));
        }
    }

    private void prepareEditProfileApi() {

        if (! validateInput())
            return;

        if (mPhotoChosen) {
            if(Utils.isAllowedFileSize(mActivity, mPhotoUri)) {
                uploadToFirebase();
            }
        }else {
            callEditProfileApi();
        }

    }

    private boolean validateInput() {

        boolean valid = true;
        int redColor = getResources().getColor(R.color.red);

        if (etFullName.getText().toString().isEmpty()) {
            // Empty name
            etFullName.setError(getString(R.string.error_field_required));
            valid = false;
        }

        return valid;
    }

    private void callEditProfileApi() {

        mProgressDialog.show();

        String url = Constants.BASE_URL + "/users";

        Log.d(TAG, "callEditProfileApi - url: " + url);

        AuthorizedCallback<JsonResponse<Object>> callback = new AuthorizedCallback<JsonResponse<Object>>(mActivity.getUser().getToken()) {
            @Override
            public void onSuccess(JsonResponse<Object> jsonResponse) {
                Log.d(TAG, "callEditProfileApi - onSuccess - Start");

                if (jsonResponse.getJsonMeta().getStatus().equals("200") ) {
                    mActivity.getmSnackBarDisplayer().displayError("Profile is updated");
                    mActivity.getUser().setName(etFullName.getText().toString());
                    if (! mUploadedPhotoUrl.isEmpty()) {
                        mActivity.getUser().setImageURL(mUploadedPhotoUrl);
                    }

                    mAppSettings.saveUser(mActivity.getUser());
                }
                else {
                    mActivity.getmSnackBarDisplayer().displayError("Unexpected error, please try later.");
                }

                mActivity.hideKeyboard();

                mProgressDialog.dismiss();

                Log.d(TAG, "callEditProfileApi - onSuccess - End");
            }

            @Override
            public void onError() {
                Log.d(TAG, "callEditProfileApi - onError - Start");

                mProgressDialog.dismiss();

                Log.d(TAG, "callEditProfileApi - onError - End");
            }
        };

        // Prepare response type
        Type responseType = new TypeToken<JsonResponse<Object>>() {}.getType();


        User user = mActivity.getUser();
        user.setName(etFullName.getText().toString());
        if (! mUploadedPhotoUrl.isEmpty()) {
            user.setImageURL(mUploadedPhotoUrl);
        }

        ProfileEditInquiry profileEditInquiry = new ProfileEditInquiry();
        profileEditInquiry.setUser(user);
        profileEditInquiry.getUser().setPassword(null);

        JsonRequest<ProfileEditInquiry> request = new JsonRequest<>(profileEditInquiry);

        VolleyWrapper<JsonRequest<ProfileEditInquiry>, JsonResponse<Object>> volleyWrapper =
                new VolleyWrapper<>(request, responseType, Request.Method.PUT, url,
                        callback, getContext(), mActivity.getmSnackBarDisplayer(), mActivity.findViewById(R.id.ll_main_view));
        volleyWrapper.execute();
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case IMAGE_GALLERY_REQUEST: {
                if (PermissionsChecker.permissionsGranted(grantResults)) {
                    onStoragePermissionGrantedHandler.onPermissionsGranted();
                }
            }
        }
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

            changeSaveButtonStatus(true);

            Glide.with(mActivity)
                    .load(mPhotoUri)
                    .asBitmap()
                    .into(rivImage);
        }
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

        StorageReference storageRef = mFirebaseStorage.getReference();
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
                callEditProfileApi();
            }
        });
        Log.d(TAG, "uploadToFirebase - End");
    }
}
