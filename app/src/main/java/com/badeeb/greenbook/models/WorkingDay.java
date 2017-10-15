package com.badeeb.greenbook.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Amr Alghawy on 10/14/2017.
 */

public class WorkingDay {

    @Expose
    @SerializedName("day_name")
    private String name;

    @Expose
    @SerializedName("opened_at")
    private String openedAt;

    @Expose
    @SerializedName("closed_at")
    private String closedAt;

    @Expose
    @SerializedName("state")
    private String state;

    public WorkingDay() {
        this.name = "";
        this.openedAt = "";
        this.closedAt = "";
        this.state = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(String openedAt) {
        this.openedAt = openedAt;
    }

    public String getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(String closedAt) {
        this.closedAt = closedAt;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
