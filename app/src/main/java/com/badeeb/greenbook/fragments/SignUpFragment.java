package com.badeeb.greenbook.fragments;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.badeeb.greenbook.R;


public class SignUpFragment extends Fragment {

    public final static String TAG = SignUpFragment.class.getName();

    private Button bSignUp;


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
        ScrollView s = (ScrollView)  view.findViewById(R.id.signUp_form);

        bSignUp = (Button) view.findViewById(R.id.signUp_bttn);
        bSignUp.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Snackbar snackbar = Snackbar
                        .make(view, "No internet connection!", Snackbar.LENGTH_LONG)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        });

                // Changing message text color
                                snackbar.setActionTextColor(Color.RED);
                                View snackBarView = snackbar.getView();
                                snackBarView.setBackgroundColor(getResources().getColor(R.color.yellow));


                // Changing action button text color
                                View sbView = snackbar.getView();
                                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                sbView.set
                                textView.setTextColor(Color.WHITE);
                                snackbar.show();
            }
        });
        return view;
    }

}
