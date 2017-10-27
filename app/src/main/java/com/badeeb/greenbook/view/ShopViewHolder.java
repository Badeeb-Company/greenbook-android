package com.badeeb.greenbook.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.badeeb.greenbook.R;
import com.makeramen.roundedimageview.RoundedImageView;

/**
 * Created by ahmed on 10/21/2017.
 */

public class ShopViewHolder extends RecyclerView.ViewHolder{
    private final static String TAG = ShopViewHolder.class.getName();

    private RoundedImageView rivShopMainPhoto;
    private TextView tvShopName;
    private TextView tvDescription;
    private TextView tvNearLocation;


    public ShopViewHolder(View itemView) {
        super(itemView);

        rivShopMainPhoto = (RoundedImageView) itemView.findViewById(R.id.rivShopMainPhoto);
        tvShopName = (TextView) itemView.findViewById(R.id.tvShopName);
        tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
        tvNearLocation = (TextView) itemView.findViewById(R.id.tvNearLocation);
    }

    public static String getTAG() {
        return TAG;
    }

    public RoundedImageView getRivShopMainPhoto() {
        return rivShopMainPhoto;
    }

    public void setRivShopMainPhoto(RoundedImageView rivShopMainPhoto) {
        this.rivShopMainPhoto = rivShopMainPhoto;
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

    public TextView getTvNearLocation() {
        return tvNearLocation;
    }

    public void setTvNearLocation(TextView tvNearLocation) {
        this.tvNearLocation = tvNearLocation;
    }
}
