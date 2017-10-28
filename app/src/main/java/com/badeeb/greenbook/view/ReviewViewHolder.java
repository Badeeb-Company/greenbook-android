package com.badeeb.greenbook.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.badeeb.greenbook.R;

/**
 * Created by Amr Alghawy on 10/28/2017.
 */

public class ReviewViewHolder extends RecyclerView.ViewHolder {

    private ImageView ivImage;
    private TextView tvShopName;
    private RatingBar rbReviewRate;
    private TextView tvReviewRating;
    private TextView tvReviewDescription;
    private TextView tvShopOwnerReply;

    public ReviewViewHolder(View itemView) {
        super(itemView);

        ivImage = itemView.findViewById(R.id.ivImage);
        tvShopName = itemView.findViewById(R.id.tvShopName);
        rbReviewRate = itemView.findViewById(R.id.rbReviewRate);
        tvReviewRating = itemView.findViewById(R.id.tvReviewRating);
        tvReviewDescription = itemView.findViewById(R.id.tvReviewDescription);
        tvShopOwnerReply = itemView.findViewById(R.id.tvShopOwnerReply);
    }

    public ImageView getIvImage() {
        return ivImage;
    }

    public void setIvImage(ImageView ivImage) {
        this.ivImage = ivImage;
    }

    public TextView getTvShopName() {
        return tvShopName;
    }

    public void setTvShopName(TextView tvShopName) {
        this.tvShopName = tvShopName;
    }

    public RatingBar getRbReviewRate() {
        return rbReviewRate;
    }

    public void setRbReviewRate(RatingBar rbReviewRate) {
        this.rbReviewRate = rbReviewRate;
    }

    public TextView getTvReviewRating() {
        return tvReviewRating;
    }

    public void setTvReviewRating(TextView tvReviewRating) {
        this.tvReviewRating = tvReviewRating;
    }

    public TextView getTvReviewDescription() {
        return tvReviewDescription;
    }

    public void setTvReviewDescription(TextView tvReviewDescription) {
        this.tvReviewDescription = tvReviewDescription;
    }

    public TextView getTvShopOwnerReply() {
        return tvShopOwnerReply;
    }

    public void setTvShopOwnerReply(TextView tvShopOwnerReply) {
        this.tvShopOwnerReply = tvShopOwnerReply;
    }
}
