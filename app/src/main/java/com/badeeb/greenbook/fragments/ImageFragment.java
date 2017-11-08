package com.badeeb.greenbook.fragments;


import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;


/**
 * A simple {@link Fragment} subclass.
 */
public class ImageFragment extends Fragment {

    public final static String TAG = ImageFragment.class.getName();

    private MainActivity mActivity;
    private FragmentManager mFragmentManager;
    private ImageView ivShopImage;

    public final static String EXTRA_IMAGE_URL = "EXTRA_IMAGE_URL";

    public ImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView - Start");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        init(view);

        Log.d(TAG, "onCreateView - End");
        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        mActivity = (MainActivity) getActivity();
        mFragmentManager = mActivity.getSupportFragmentManager();

        ivShopImage = (ImageView) view.findViewById(R.id.ivShopImage);

        String imageUrl = getArguments().getString(EXTRA_IMAGE_URL);

        Glide.with(mActivity)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(new ColorDrawable(mActivity.getResources().getColor(R.color.light_gray)))
                .into(ivShopImage);

        Log.d(TAG, "init - End");
    }


}
