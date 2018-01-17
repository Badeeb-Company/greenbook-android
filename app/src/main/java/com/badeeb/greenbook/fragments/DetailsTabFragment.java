package com.badeeb.greenbook.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.models.WorkingDay;
import com.badeeb.greenbook.shared.OnPermissionsGrantedHandler;
import com.badeeb.greenbook.shared.PermissionsChecker;
import com.badeeb.greenbook.shared.Utils;

import org.parceler.Parcels;

import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailsTabFragment extends Fragment {

    public final static String TAG = DetailsTabFragment.class.getName();
    private static final int REQUEST_CALL_PERMISSION = 100;

    private static List<String> dayNames;

    static {
        dayNames = new ArrayList<>();
        dayNames.add("Sunday");
        dayNames.add("Monday");
        dayNames.add("Tuesday");
        dayNames.add("Wednesday");
        dayNames.add("Thursday");
        dayNames.add("Friday");
        dayNames.add("Saturday");
    }

    private MainActivity mActivity;
    private DetailsTabFragment mCurrentFragment;
    private Shop mShop;

    // listener
    OnPermissionsGrantedHandler onCallPermissionGrantedHandler;

    // UI Fields
    private Spinner sWorkingHours;
    private TextView tvPhone;
    private ImageView ivCall;
    private TextView tvWebsite;
    private String mSelectedMobileNumber;

    private ArrayAdapter<String> spinnerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_details_tab, container, false);

        init(view);
        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        mActivity = (MainActivity) getActivity();
        mCurrentFragment = this;
        mShop = Parcels.unwrap(getArguments().getParcelable(ShopDetailsFragment.EXTRA_SHOP_OBJECT));

        if(mShop == null){
            mActivity.getmSnackBarDisplayer().displayError("Shop details missing!");
            return;
        }

        onCallPermissionGrantedHandler = createOnCallPermissionGrantedHandler();

        initUi(view);

        fillUiFields();

        setupListener();

        Log.d(TAG, "init - Start");
    }

    private void initUi(View view) {
        Log.d(TAG, "initUi - Start");

        sWorkingHours = (Spinner) view.findViewById(R.id.sWorkingHours);
        tvPhone = (TextView) view.findViewById(R.id.tvPhone);
        ivCall = (ImageView) view.findViewById(R.id.ivCall);
        tvWebsite = view.findViewById(R.id.tvWebsite);

        spinnerAdapter =  new ArrayAdapter<>(mActivity, R.layout.spinner_item);

        sWorkingHours.setAdapter(spinnerAdapter);

        sWorkingHours.setEnabled(false);

        Log.d(TAG, "initUi - Start");
    }

    private void fillUiFields(){
        Log.d(TAG, "fillUiFields - Start");

        tvPhone.setText(mShop.getPhoneNumber());

        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("EEEE");
        String dayName = format.format(date);
        int dayIndex = dayNames.indexOf(dayName);

        spinnerAdapter.addAll(getHoursList(dayIndex));
        spinnerAdapter.notifyDataSetChanged();

        tvWebsite.setText(mShop.getWebsite());
    }

    private List<String> getHoursList(int dayIndex){
        List<String> result = new ArrayList<>();
        for (int i = dayIndex; i < dayIndex + 7; i++) {
            String dayName = dayNames.get(i%7);
            WorkingDay wDay = searchWorkingDay(dayName);
            result.add(wDay.getOpeningHours());
        }
        return result;
    }

    private WorkingDay searchWorkingDay(String dayName) {
        for (WorkingDay day : mShop.getWorkingDays()) {
            if(day.getName().equals(dayName)){
                return day;
            }
        }
        return null;
    }

    private void setupListener() {
        Log.d(TAG, "setupListener - Start");

        ivCall.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if("".equals(mShop.getPhoneNumber())){
                    mActivity.getmSnackBarDisplayer().displayError("There is no phone number to dial");
                    return;
                }
                mSelectedMobileNumber = mShop.getPhoneNumber();
                PermissionsChecker.checkPermissions(mCurrentFragment,onCallPermissionGrantedHandler,REQUEST_CALL_PERMISSION, Manifest.permission.CALL_PHONE);
            }
        });

        Log.d(TAG, "setupListener - end");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult - start");
        if(requestCode == REQUEST_CALL_PERMISSION) {
            if(grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                mActivity.getmSnackBarDisplayer().displayError("You must allow permission to call the shop");
            }else {
                goCall();
            }
        }
    }

    private OnPermissionsGrantedHandler createOnCallPermissionGrantedHandler() {
        return new OnPermissionsGrantedHandler() {
            @Override
            public void onPermissionsGranted() {
                goCall();
            }
        };

    }

    @SuppressWarnings("MissingPermission")
    private void goCall() {
        Log.d(TAG, "goCall - start");
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + mSelectedMobileNumber));
        mActivity.startActivity(callIntent);
    }
}
