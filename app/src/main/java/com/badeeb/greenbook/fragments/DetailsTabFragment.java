package com.badeeb.greenbook.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.models.WorkingDay;
import com.badeeb.greenbook.shared.OnPermissionsGrantedHandler;
import com.badeeb.greenbook.shared.PermissionsChecker;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DetailsTabFragment extends Fragment {
    public final static String TAG = DetailsTabFragment.class.getName();
    private static final int REQUEST_CALL_PERMISSION = 100;

    private MainActivity mActivity;
    private DetailsTabFragment mCurrentFragment;
    private Shop mShop;

    // listener
    OnPermissionsGrantedHandler onCallPermissionGrantedHandler;

    // UI Fields
    private TextView tvAddress;
    private TextView tvWorkingHours;
    private TextView tvPhone;
    private ImageView ivCall;
    private String mSelectedMobileNumber;

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

        tvAddress = (TextView) view.findViewById(R.id.tvAddress) ;
        tvWorkingHours = (TextView) view.findViewById(R.id.tvWorkingHours);
        tvPhone = (TextView) view.findViewById(R.id.tvPhone);
        ivCall = (ImageView) view.findViewById(R.id.ivCall);

        Log.d(TAG, "initUi - Start");
    }

    private void fillUiFields(){
        Log.d(TAG, "fillUiFields - Start");

        tvAddress.setText(mShop.getLocation().getAddress());
        tvWorkingHours.setText(getShopWorkingStatus());
        tvPhone.setText(mShop.getPhoneNumber());

        Log.d(TAG, "fillUiFields - Start");
    }

    private String getShopWorkingStatus() {
        Log.d(TAG, "getShopWorkingStatus - Start");

        String[] daysOfWeek = {"","SUNDAY","MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY"};
        SimpleDateFormat timeParser = new SimpleDateFormat("HH:mm");
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        List<WorkingDay> shopWorkingDays = mShop.getWorkingDays();
        if(shopWorkingDays != null) {
            try {
                for(WorkingDay workDay : shopWorkingDays) {
                    if (daysOfWeek[day].equals(workDay.getName())){

                        Date currentDate = calendar.getTime();
                        Date openAt = timeParser.parse(workDay.getOpenedAt());
                        Date closeAt = timeParser.parse(workDay.getClosedAt()) ;

                        Log.d(TAG, "getShopWorkingStatus - openAt: "+workDay.getOpenedAt()+" - current: "+timeParser.format(currentDate)+" - closeAt: "+workDay.getClosedAt());

                        if(currentDate.after(openAt) && currentDate.before(closeAt)){
                            return "Opened now";
                        }else{
                            return "Closed now";
                        }


                    }
                }
            } catch (ParseException e) {
                Log.d(TAG, "getShopWorkingStatus - ParseException");
                e.printStackTrace();
                return "Not Determined";
            }
        }
        Log.d(TAG, "getShopWorkingStatus - end");
        return "Not Determined";
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
