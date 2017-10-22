package com.badeeb.greenbook.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahmed on 10/21/2017.
 */

public class ShopInquiry {

    @Expose(serialize = false, deserialize = true)
    @SerializedName("shops")
    List<Shop> shopList;

    public ShopInquiry(){
        shopList = new ArrayList<>();
    }

    public List<Shop> getShopList() {
        return shopList;
    }

    public void setShopList(List<Shop> shopList) {
        this.shopList = shopList;
    }
}
