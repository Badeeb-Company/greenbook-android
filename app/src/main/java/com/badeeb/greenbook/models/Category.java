package com.badeeb.greenbook.models;

import com.google.gson.annotations.Expose;

import org.parceler.Parcel;

/**
 * Created by Amr Alghawy on 10/14/2017.
 */
@Parcel(Parcel.Serialization.BEAN)
public class Category {

    @Expose
    private int id;

    @Expose
    private String name;

    public Category() {
        this.id = 0;
        this.name = "";
    }

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
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
}
