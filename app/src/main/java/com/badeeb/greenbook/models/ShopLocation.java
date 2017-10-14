package com.badeeb.greenbook.models;

/**
 * Created by Amr Alghawy on 10/14/2017.
 */

public class ShopLocation {

    private double lat;
    private double lng;
    private String address;

    public ShopLocation() {
        this.lat = 0;
        this.lng = 0;
        this.address = "";
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
