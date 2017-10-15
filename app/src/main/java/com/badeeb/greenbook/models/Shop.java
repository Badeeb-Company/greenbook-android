package com.badeeb.greenbook.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Amr Alghawy on 10/14/2017.
 */

public class Shop {

    @Expose
    @SerializedName("id")
    private int id;

    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("description")
    private String description;

    @Expose
    @SerializedName("rate")
    private int rate;

    @Expose
    @SerializedName("working_days")
    private ArrayList<WorkingDay> workingDays;

    @Expose
    @SerializedName("location")
    private ShopLocation location;

    @Expose
    @SerializedName("phone_number")
    private String phoneNumber;

    @Expose
    @SerializedName("main_photo_url")
    private String mainPhotoURL;

    @Expose
    @SerializedName("photos")
    private ArrayList<String> photos;
}
