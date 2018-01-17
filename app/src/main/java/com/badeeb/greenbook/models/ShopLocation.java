package com.badeeb.greenbook.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by Amr Alghawy on 10/14/2017.
 */
@Parcel(Parcel.Serialization.BEAN)
public class ShopLocation {

    @Expose
    @SerializedName("lat")
    private double lat;

    @Expose
    @SerializedName("long")
    private double lng;

    @Expose
    @SerializedName("address")
    private String address;

    public ShopLocation() {
        this.lat = 0;
        this.lng = 0;
        this.address = "";
    }

    public LatLng getPosition(){
        return new LatLng(lat, lng);
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
