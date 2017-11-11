package com.badeeb.greenbook.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Amr Alghawy on 10/29/2017.
 */

public class ReviewManage {

    @Expose(serialize = true, deserialize = false)
    @SerializedName("review")
    private Review review;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("id")
    private int id;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("shop_rate")
    private double shopRate;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("num_of_reviews")
    private int numberOfReviews;

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getShopRate() {
        return shopRate;
    }

    public void setShopRate(double shopRate) {
        this.shopRate = shopRate;
    }

    public int getNumberOfReviews() {
        return numberOfReviews;
    }

    public void setNumberOfReviews(int numberOfReviews) {
        this.numberOfReviews = numberOfReviews;
    }
}
