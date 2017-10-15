package com.badeeb.greenbook.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Amr Alghawy on 10/15/2017.
 */

public class JsonUser {

    @Expose(serialize = false, deserialize = true)
    @SerializedName("user")
    private User user;

    public JsonUser() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
