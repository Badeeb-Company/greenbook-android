package com.badeeb.greenbook.adaptors;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.models.Review;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.view.ReviewViewHolder;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by Amr Alghawy on 10/28/2017.
 */

public class ReviewRecyclerViewAdapter extends RecyclerView.Adapter<ReviewViewHolder>  {

    private Context context;
    private Shop shop;
    private List<Review> reviewsList;

    public ReviewRecyclerViewAdapter(Context context, Shop shop, List<Review> reviewsList) {
        this.context = context;
        this.shop = shop;
        this.reviewsList = reviewsList;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View itemView = layoutInflater.inflate(R.layout.review_card_view, parent, false);

        ReviewViewHolder reviewViewHolder = new ReviewViewHolder(itemView);

        return reviewViewHolder;
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {

        Review review = this.reviewsList.get(position);
        // Set review values in holder
        Glide.with(context).load(review.getUser().getImageURL()).placeholder(R.drawable.def_usr_img).into(holder.getIvImage());
        holder.getTvShopName().setText(shop.getName());
        holder.getRbReviewRate().setRating((float) review.getRate());
        holder.getTvReviewRating().setText(review.getRate() + "");
        holder.getTvReviewDescription().setText(review.getDescription());
        holder.getTvShopOwnerReply().setText(review.getReply());
    }

    @Override
    public int getItemCount() {
        if (this.reviewsList == null)
            return 0;

        return this.reviewsList.size();
    }
}
