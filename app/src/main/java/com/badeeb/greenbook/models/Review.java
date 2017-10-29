package com.badeeb.greenbook.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by Amr Alghawy on 10/14/2017.
 */
@Parcel(Parcel.Serialization.BEAN)
public class Review {

    @Expose
    @SerializedName("id")
    private int id;

    @Expose
    @SerializedName("description")
    private String description;

    @Expose
    @SerializedName("rate")
    private double rate;

    @Expose
    @SerializedName("created_at")
    private String createdAt;

    @Expose
    @SerializedName("user")
    private User user;

    @Expose
    @SerializedName("reply")
    private String reply;

    @Expose
    @SerializedName("date_replied")
    private String replyDate;

    public Review() {
        this.id = 0;
        this.description = "";
        this.rate = 0;
        this.createdAt = "";
        this.user  = new User();
        this.reply = "";
        this.replyDate = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getReplyDate() {
        return replyDate;
    }

    public void setReplyDate(String replyDate) {
        this.replyDate = replyDate;
    }
}
