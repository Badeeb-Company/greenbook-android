package com.badeeb.greenbook.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by Amr Alghawy on 10/14/2017.
 */
@Parcel(Parcel.Serialization.BEAN)
public class WorkingDay {

    @Expose
    @SerializedName("day_name")
    private String name;

    @Expose
    @SerializedName("opened_at")
    private String openingHours;


    public WorkingDay() {
        this.name = "";
        this.openingHours = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

}
