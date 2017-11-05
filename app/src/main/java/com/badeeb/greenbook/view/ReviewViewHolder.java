package com.badeeb.greenbook.view;

import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.badeeb.greenbook.R;

/**
 * Created by Amr Alghawy on 10/28/2017.
 */

public class ReviewViewHolder extends RecyclerView.ViewHolder {

    private ImageView ivImage;
    private TextView tvReviewerName;
    private RatingBar rbReviewRate;
    private TextView tvReviewRating;
    private TextView tvReviewDescription;
    private LinearLayout llOfReviewReply;
    private ImageView ivReplyIcon;
    private LinearLayout llOwnerReply;
    private TextView tvReviewEdit;
    private TextView tvReviewDelete;
    private TextView tvShopOwnerReplyView;
    private TextView etShopOwnerReplyText;
    private TextView tvReviewReply;
    private TextView tvReviewTime;

    public ReviewViewHolder(View itemView) {
        super(itemView);

        ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
        tvReviewerName = (TextView) itemView.findViewById(R.id.tvReviewerName);
        rbReviewRate = (RatingBar) itemView.findViewById(R.id.rbReviewRate);
        tvReviewRating = (TextView) itemView.findViewById(R.id.tvReviewRating);
        tvReviewDescription = (TextView) itemView.findViewById(R.id.tvReviewDescription);

        llOfReviewReply = (LinearLayout) itemView.findViewById(R.id.llOfReviewReply);
        ivReplyIcon = (ImageView) itemView.findViewById(R.id.ivReplyIcon);

        llOwnerReply = (LinearLayout) itemView.findViewById(R.id.llOwnerReply);
        tvReviewReply = (TextView) itemView.findViewById(R.id.tvReviewReply);
        tvShopOwnerReplyView = (TextView) itemView.findViewById(R.id.tvShopOwnerReplyView);
        etShopOwnerReplyText = (TextView) itemView.findViewById(R.id.etShopOwnerReplyText);

        tvReviewEdit = (TextView) itemView.findViewById(R.id.tvReviewEdit);
        tvReviewDelete = (TextView) itemView.findViewById(R.id.tvReviewDelete);

        tvReviewTime = (TextView) itemView.findViewById(R.id.tvReviewTime);
    }

    public ImageView getIvImage() {
        return ivImage;
    }

    public void setIvImage(ImageView ivImage) {
        this.ivImage = ivImage;
    }

    public TextView getTvReviewerName() {
        return tvReviewerName;
    }

    public void setTvReviewerName(TextView tvReviewerName) {
        this.tvReviewerName = tvReviewerName;
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

    public LinearLayout getLlOfReviewReply() {
        return llOfReviewReply;
    }

    public void setLlOfReviewReply(LinearLayout llOfReviewReply) {
        this.llOfReviewReply = llOfReviewReply;
    }

    public TextView getTvReviewEdit() {
        return tvReviewEdit;
    }

    public void setTvReviewEdit(TextView tvReviewEdit) {
        this.tvReviewEdit = tvReviewEdit;
    }

    public TextView getTvReviewDelete() {
        return tvReviewDelete;
    }

    public void setTvReviewDelete(TextView tvReviewDelete) {
        this.tvReviewDelete = tvReviewDelete;
    }

    public ImageView getIvReplyIcon() {
        return ivReplyIcon;
    }

    public void setIvReplyIcon(ImageView ivReplyIcon) {
        this.ivReplyIcon = ivReplyIcon;
    }

    public LinearLayout getLlOwnerReply() {
        return llOwnerReply;
    }

    public void setLlOwnerReply(LinearLayout llOwnerReply) {
        this.llOwnerReply = llOwnerReply;
    }

    public TextView getTvShopOwnerReplyView() {
        return tvShopOwnerReplyView;
    }

    public void setTvShopOwnerReplyView(TextView tvShopOwnerReplyView) {
        this.tvShopOwnerReplyView = tvShopOwnerReplyView;
    }

    public TextView getEtShopOwnerReplyText() {
        return etShopOwnerReplyText;
    }

    public void setEtShopOwnerReplyText(TextView etShopOwnerReplyText) {
        this.etShopOwnerReplyText = etShopOwnerReplyText;
    }

    public TextView getTvReviewReply() {
        return tvReviewReply;
    }

    public void setTvReviewReply(TextView tvReviewReply) {
        this.tvReviewReply = tvReviewReply;
    }

    public TextView gettvReviewTime() {
        return tvReviewTime;
    }

    public void setvReviewTime(TextView tvReviewTime) {
        this.tvReviewTime = tvReviewTime;
    }
}
