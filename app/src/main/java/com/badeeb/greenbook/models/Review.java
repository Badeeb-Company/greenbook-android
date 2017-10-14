package com.badeeb.greenbook.models;

/**
 * Created by Amr Alghawy on 10/14/2017.
 */

public class Review {

    private int id;
    private String description;
    private int rate;
    private String createdAt;
    private User user;
    private String reply;

    public Review() {
        this.id = 0;
        this.description = "";
        this.rate = 0;
        this.createdAt = "";
        this.user  = new User();
        this.reply = "";
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

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
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
}
