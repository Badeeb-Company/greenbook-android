package com.badeeb.greenbook.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.models.Shop;

public class DetailsTabFragment extends Fragment {

    private MainActivity mActivity;
    private Shop mShop;

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
        mActivity = (MainActivity) getActivity();
        mShop = getArguments().getParcelable(ShopDetailsFragment.EXTRA_SHOP_OBJECT);

        if(mShop == null){
            mActivity.getmSnackBarDisplayer().displayError("Shop details missing!");
            return;
        }



    }

}
