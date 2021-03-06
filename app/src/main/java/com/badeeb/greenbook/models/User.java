package com.badeeb.greenbook.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;

/**
 * Created by Amr Alghawy on 10/14/2017.
 */
@Parcel(Parcel.Serialization.BEAN)
public class User {

    @Expose(serialize = false, deserialize = true)
    private int id;

    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("email")
    private String email;

    @Expose
    @SerializedName("password")
    private String password;

    @Expose
    @SerializedName("image_url")
    private String imageURL;

    @Expose
    @SerializedName("token")
    private String token;

    @Expose
    @SerializedName("owned_shops")
    private ArrayList<Shop> ownedShops;

    @Expose
    @SerializedName("account_token")
    private String socialAcctToken;

    @Expose
    @SerializedName("account_id")
    private String socialAcctId;

    @Expose
    @SerializedName("account_type")
    private String accountType;

    public User() {
        this.name = "";
        this.email = "";
        this.password = "";
        this.imageURL = "";
        this.socialAcctToken = "";
        this.socialAcctId = "";
        this.accountType = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ArrayList<Shop> getOwnedShops() {
        return ownedShops;
    }

    public void setOwnedShops(ArrayList<Shop> ownedShops) {
        this.ownedShops = ownedShops;
    }

    public String getSocialAcctToken() {
        return socialAcctToken;
    }

    public void setSocialAcctToken(String socialAcctToken) {
        this.socialAcctToken = socialAcctToken;
    }

    public String getSocialAcctId() {
        return socialAcctId;
    }

    public void setSocialAcctId(String socialAcctId) {
        this.socialAcctId = socialAcctId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
