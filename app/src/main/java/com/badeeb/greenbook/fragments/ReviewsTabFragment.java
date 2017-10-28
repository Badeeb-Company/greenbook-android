package com.badeeb.greenbook.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badeeb.greenbook.R;

public class ReviewsTabFragment extends Fragment {

    public final static String TAG = ReviewsTabFragment.class.getName();

    private RecyclerView mRecyclerView;

    public ReviewsTabFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_reviews_tab, container, false);

        init(view);

        Log.d(TAG, "onCreateView - End");

        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");


        setupListeners(view);

        Log.d(TAG, "init - End");
    }

    private void setupListeners(View view) {
        Log.d(TAG, "setupListeners - Start");

        Log.d(TAG, "setupListeners - End");
    }

}
