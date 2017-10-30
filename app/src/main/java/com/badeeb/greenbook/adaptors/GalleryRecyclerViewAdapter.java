package com.badeeb.greenbook.adaptors;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.models.Photo;
import com.badeeb.greenbook.view.GalleryViewHolder;
import com.bumptech.glide.Glide;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ahmed on 10/24/2017.
 */

public class GalleryRecyclerViewAdapter extends RecyclerView.Adapter<GalleryViewHolder>{
    private final static String TAG = GalleryRecyclerViewAdapter.class.getName();

    private List<Photo> mPhotoList;
    private Context mActivity;

    public GalleryRecyclerViewAdapter(Context activity, List<Photo> photoList){
        mPhotoList = photoList;
        mActivity = activity;
    }


    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_card_view,parent,false);

        return new GalleryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder - start ");

        if(mPhotoList == null){
            Log.d(TAG, "mPhotoList is null ");
            return;
        }

        Log.d(TAG, "mPhotoList is "+ Arrays.toString(mPhotoList.toArray()));

        Photo currentPhoto = mPhotoList.get(position);
        Glide.with(mActivity)
                .load(currentPhoto.getPhotoURL())
                .placeholder(R.drawable.no_img)
                .into(holder.getIvPhoto());

    }

    @Override
    public int getItemCount() {
        if(mPhotoList != null) return mPhotoList.size();
        return 0;
    }
}
