package com.badeeb.greenbook.adaptors;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.fragments.ReviewsTabFragment;
import com.badeeb.greenbook.models.Review;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.models.User;
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
    private ReviewsTabFragment reviewsTabFragment;
    private User user;
    private MainActivity mActivity;

    public ReviewRecyclerViewAdapter(Context context, Shop shop, List<Review> reviewsList, ReviewsTabFragment reviewsTabFragment, User user, MainActivity mActivity) {
        this.context = context;
        this.shop = shop;
        this.reviewsList = reviewsList;
        this.reviewsTabFragment = reviewsTabFragment;
        this.user = user;
        this.mActivity = mActivity;
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
        Glide.with(context)
                .load(review.getUser().getImageURL())
                .asBitmap()
                .placeholder(R.drawable.def_usr_img)
                .into(holder.getIvImage());

        holder.getTvReviewerName().setText(review.getUser().getName());
        holder.getRbReviewRate().setRating((float) review.getRate());
        holder.getTvReviewRating().setText(review.getRate() + "");
        holder.getTvReviewDescription().setText(review.getDescription());

        holder.getEtShopOwnerReplyText().setText(review.getReply());

        if (review.getReply() == null || review.getReply().isEmpty()) {
            hideShopOwnerUI(holder);
        }
        else {
            showShopOwnerUI(holder);
            holder.getEtShopOwnerReplyText().setVisibility(View.GONE);
            holder.getTvShopOwnerReplyView().setText(review.getReply());
        }

        if (user == null || user.getId() != review.getUser().getId()) {
            holder.getTvReviewEdit().setVisibility(View.GONE);
            holder.getTvReviewDelete().setVisibility(View.GONE);
        }
        else if (user != null && user.getId() == review.getUser().getId()) {
            holder.getTvReviewEdit().setVisibility(View.VISIBLE);
            holder.getTvReviewDelete().setVisibility(View.VISIBLE);
        }


        if (user != null && mActivity.getmOwnedShopsSet().contains(shop.getId())) {
            // Check if owner is replied before or not
            if (review != null && (review.getReply() == null || review.getReply().isEmpty())) {
                showShopOwnerUI(holder);
                holder.getTvShopOwnerReplyView().setVisibility(View.GONE);
            }
//            else if (review != null && review.getReply() != null && ! review.getReply().isEmpty()) {
//                // show reply
//            }
        }

        setupListeners(holder, review);
    }

    @Override
    public int getItemCount() {
        if (this.reviewsList == null)
            return 0;

        return this.reviewsList.size();
    }

    private void setupListeners(ReviewViewHolder holder, final Review review) {

        holder.getTvReviewEdit().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reviewsTabFragment.goToEditReview(review);
            }
        });

        holder.getTvReviewDelete().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reviewsTabFragment.prepareDeleteReview(review);
            }
        });

        holder.getEtShopOwnerReplyText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reviewsTabFragment.goToShopOwnerReply(review);
            }
        });
    }

    private void hideShopOwnerUI(ReviewViewHolder holder) {
        holder.getIvReplyIcon().setVisibility(View.GONE);
        holder.getTvShopOwnerReplyView().setVisibility(View.GONE);
        holder.getEtShopOwnerReplyText().setVisibility(View.GONE);
        holder.getLlOwnerReply().setVisibility(View.GONE);
        holder.getTvReviewReply().setVisibility(View.GONE);

    }

    private void showShopOwnerUI(ReviewViewHolder holder) {
        holder.getEtShopOwnerReplyText().setVisibility(View.VISIBLE);
        holder.getIvReplyIcon().setVisibility(View.VISIBLE);
        holder.getTvReviewReply().setVisibility(View.VISIBLE);
        holder.getLlOwnerReply().setVisibility(View.VISIBLE);

        holder.getTvShopOwnerReplyView().setVisibility(View.VISIBLE);
    }
}
