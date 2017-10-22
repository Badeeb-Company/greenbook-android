package com.badeeb.greenbook.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed on 10/20/2017.
 */

public class CategoryInquiry {

    @Expose(serialize = false, deserialize = true)
    @SerializedName("categories")
    List<Category> categoryList;

    public CategoryInquiry(){
        categoryList = new ArrayList<Category>();
    }

    public List<Category> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<Category> categoryList) {
        this.categoryList = categoryList;
    }
}
