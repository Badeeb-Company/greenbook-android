package com.badeeb.greenbook.models;

/**
 * Created by Amr Alghawy on 10/14/2017.
 */

public class Category {

    private int id;
    private String name;

    public Category() {
        this.id = 0;
        this.name = "";
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
