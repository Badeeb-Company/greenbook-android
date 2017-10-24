package com.badeeb.greenbook.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.badeeb.greenbook.R;

/**
 * Created by ahmed on 10/24/2017.
 */

public class GalleryViewHolder extends RecyclerView.ViewHolder{

    private ImageView ivPhoto;

    public GalleryViewHolder(View itemView) {
        super(itemView);

        ivPhoto = (ImageView) itemView.findViewById(R.id.ivPhoto);
    }

    public ImageView getIvPhoto() {
        return ivPhoto;
    }

    public void setIvPhoto(ImageView ivPhoto) {
        this.ivPhoto = ivPhoto;
    }
}
