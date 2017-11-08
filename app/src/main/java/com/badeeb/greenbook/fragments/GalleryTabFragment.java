package com.badeeb.greenbook.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.adaptors.GalleryRecyclerViewAdapter;
import com.badeeb.greenbook.listener.RecyclerItemClickListener;
import com.badeeb.greenbook.models.Photo;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.shared.Constants;

import org.parceler.Parcels;

import java.util.Arrays;

public class GalleryTabFragment extends Fragment {
    private final static String TAG = GalleryTabFragment.class.getName();

    private MainActivity mActivity;
    private FragmentManager mFragmentManager;
    private Shop mShop;

    private RecyclerView rvGalleryPhotos;
    private GalleryRecyclerViewAdapter rvGalleryPhotosAdaptor;
    private LinearLayout llNoGallery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreate - Start");

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_gallery_tab, container, false);

        init(view);

        Log.d(TAG, "onCreate - End");
        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        mActivity = (MainActivity) getActivity();
        mFragmentManager = mActivity.getSupportFragmentManager();

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

        llNoGallery = (LinearLayout) view.findViewById(R.id.llNoGallery);

        if (mShop.getPhotos() != null && mShop.getPhotos().size() > 0) {
            llNoGallery.setVisibility(View.GONE);
        }
        else {
            llNoGallery.setVisibility(View.VISIBLE);
        }

        mActivity.setSearchButtonAsChecked();

        setupListeners();

        Log.d(TAG, "init - End");
    }

    private void setupListeners() {
        Log.d(TAG, "setupListeners - End");

        rvGalleryPhotos.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Photo selectedPhoto = mShop.getPhotos().get(position);

                goToImageFragment(selectedPhoto);
            }
        }));

        Log.d(TAG, "setupListeners - End");
    }

    private void goToImageFragment(Photo selectedPhoto) {

        ImageFragment imageFragment = new ImageFragment();

        Bundle bundle = new Bundle();
        bundle.putString(ImageFragment.EXTRA_IMAGE_URL, selectedPhoto.getPhotoURL());
        imageFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, imageFragment, imageFragment.TAG);

        fragmentTransaction.addToBackStack(TAG);

        fragmentTransaction.commit();

    }

}
