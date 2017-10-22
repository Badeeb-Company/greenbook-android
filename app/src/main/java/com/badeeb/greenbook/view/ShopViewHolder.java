package com.badeeb.greenbook.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.badeeb.greenbook.R;

/**
 * Created by ahmed on 10/21/2017.
 */

public class ShopViewHolder extends RecyclerView.ViewHolder{
    private final static String TAG = ShopViewHolder.class.getName();

    private ImageView ivShopMainPhoto;
    private TextView tvShopName;
    private TextView tvDescription;


    public ShopViewHolder(View itemView) {
        super(itemView);

        ivShopMainPhoto = (ImageView) itemView.findViewById(R.id.ivShopMainPhoto);
        tvShopName = (TextView) itemView.findViewById(R.id.tvShopName);
        tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);

    }

    public static String getTAG() {
        return TAG;
    }

    public ImageView getIvShopMainPhoto() {
        return ivShopMainPhoto;
    }

    public void setIvShopMainPhoto(ImageView ivShopMainPhoto) {
        this.ivShopMainPhoto = ivShopMainPhoto;
    }

    public TextView getTvShopName() {
        return tvShopName;
    }

    public void setTvShopName(TextView tvShopName) {
        this.tvShopName = tvShopName;
    }

    public TextView getTvDescription() {
        return tvDescription;
    }

    public void setTvDescription(TextView tvDescription) {
        this.tvDescription = tvDescription;
    }
}
