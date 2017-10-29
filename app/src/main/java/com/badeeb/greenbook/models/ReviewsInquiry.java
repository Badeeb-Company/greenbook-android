package com.badeeb.greenbook.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amr Alghawy on 10/28/2017.
 */

public class ReviewsInquiry {

    @Expose(serialize = false, deserialize = true)
    @SerializedName("reviews")
    List<Review> reviewsList;

    public ReviewsInquiry() {
        this.reviewsList = new ArrayList<>();
    }

    public List<Review> getReviewsList() {
        return reviewsList;
    }

    public void setReviewsList(List<Review> reviewsList) {
        this.reviewsList = reviewsList;
    }
}
