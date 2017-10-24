package com.badeeb.greenbook.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by ahmed on 10/21/2017.
 */
@Parcel(Parcel.Serialization.BEAN)
public class Photo {
    @Expose
    @SerializedName("id")
    private int id;

    @Expose
    @SerializedName("working_days")
    private String photoURL;

    public Photo(){
        photoURL = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }
}
