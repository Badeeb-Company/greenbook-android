package com.badeeb.greenbook.fragments;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.shared.UiUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    public static final String TAG = LoginFragment.class.getSimpleName();

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        UiUtils.showSnackBar(getActivity().findViewById(R.id.ll_main_view), "Message", Snackbar.LENGTH_INDEFINITE, getResources().getColor(R.color.orange), R.drawable.btn_close, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });



        return view;
    }

}
