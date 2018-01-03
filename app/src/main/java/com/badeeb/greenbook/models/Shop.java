package com.badeeb.greenbook.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;

/**
 * Created by Amr Alghawy on 10/14/2017.
 */
@Parcel(Parcel.Serialization.BEAN)
public class Shop {

    @Expose
    @SerializedName("id")
    private int id;

    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("google_place_id")
    private String googlePlaceId;

    @Expose
    @SerializedName("rate")
    private double rate;

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
    private ArrayList<Photo> photos;

    @Expose
    @SerializedName("num_of_reviews")
    private int numOfReviews;

    @Expose
    @SerializedName("open_now")
    private boolean openNow;

    public Shop(){
        name = "";
        googlePlaceId = "";
        workingDays = new ArrayList<>();
        location = new ShopLocation();
        phoneNumber = "";
        mainPhotoURL = "";
        photos = new ArrayList<>();
        numOfReviews = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGooglePlaceId() {
        return googlePlaceId;
    }

    public void setGooglePlaceId(String googlePlaceId) {
        this.googlePlaceId = googlePlaceId;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public ArrayList<WorkingDay> getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(ArrayList<WorkingDay> workingDays) {
        this.workingDays = workingDays;
    }

    public ShopLocation getLocation() {
        return location;
    }

    public void setLocation(ShopLocation location) {
        this.location = location;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMainPhotoURL() {
        return mainPhotoURL;
    }

    public void setMainPhotoURL(String mainPhotoURL) {
        this.mainPhotoURL = mainPhotoURL;
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<Photo> photos) {
        this.photos = photos;
    }

    @Override
    public String toString() {
        return "["+id+" - "+name+"] ";
    }

    public int getNumOfReviews() {
        return numOfReviews;
    }

    public void setNumOfReviews(int numOfReviews) {
        this.numOfReviews = numOfReviews;
    }

    public boolean isOpenNow() {
        return openNow;
    }

    public void setOpenNow(boolean openNow) {
        this.openNow = openNow;
    }
}
