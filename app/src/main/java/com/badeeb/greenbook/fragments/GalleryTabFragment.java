package com.badeeb.greenbook.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.adaptors.GalleryRecyclerViewAdapter;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.shared.Constants;

import org.parceler.Parcels;

import java.util.Arrays;

public class GalleryTabFragment extends Fragment {
    private final static String TAG = GalleryTabFragment.class.getName();

    private MainActivity mActivity;
    private Shop mShop;

    private RecyclerView rvGalleryPhotos;
    private GalleryRecyclerViewAdapter rvGalleryPhotosAdaptor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_gallery_tab, container, false);

        init(view);

        return view;
    }

    private void init(View view) {
        mActivity = (MainActivity) getActivity();

        mShop = Parcels.unwrap(getArguments().getParcelable(ShopDetailsFragment.EXTRA_SHOP_OBJECT));

        if(mShop == null){
            mActivity.getmSnackBarDisplayer().displayError("Shop details couldn't be loaded");
            return;
        }

        Log.d(TAG, "photos : "+ Arrays.toString(mShop.getPhotos().toArray()));

        rvGalleryPhotos = (RecyclerView) view.findViewById(R.id.rvPhotoList);
//        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), Constants.GALLERY_PHOTOS_PER_LINE);
        rvGalleryPhotos.setLayoutManager(mLayoutManager);
        rvGalleryPhotos.setItemAnimator(new DefaultItemAnimator());

        rvGalleryPhotosAdaptor = new GalleryRecyclerViewAdapter(getContext(), mShop.getPhotos());
        rvGalleryPhotos.setAdapter(rvGalleryPhotosAdaptor);

    }

}
