package com.badeeb.greenbook.shared;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.controllers.GreenBookApplication;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Amr Alghawy on 10/15/17.
 */

public class AppSettings {

    private final static String PREF_USER_ID = "PREF_USER_ID";
    private final static String PREF_USER_NAME = "PREF_USER_NAME";
    private final static String PREF_USER_EMAIL = "PREF_USER_EMAIL";
    private final static String PREF_USER_IMAGE_URL = "PREF_USER_IMAGE_URL";
    private final static String PREF_USER_TOKEN = "PREF_USER_TOKEN";
    private final static String PREF_USER_OWNED_SHOPS = "PREF_USER_OWNED_SHOPS";
    private final static String PREF_USER_SOCIAL_ACCT_TOKEN = "PREF_USER_SOCIAL_ACCT_TOKEN";
    private final static String PREF_USER_SOCIAL_ACCT_ID = "PREF_USER_SOCIAL_ACCT_ID";
    private final static String PREF_USER_ACCT_TYPE = "PREF_USER_ACCT_TYPE";
    private final static String PREF_APP_LOGIN_COUNTER = "PREF_APP_LOGIN_COUNTER";

    private static AppSettings sInstance;

    private SharedPreferences sPreferences;

    public static AppSettings getInstance() {
        if (sInstance == null) {
            sInstance = new AppSettings(GreenBookApplication.getInstance());
        }
        return sInstance;
    }


    private AppSettings(Context context) {
        String fileName = context.getString(R.string.app_name);
        this.sPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    // String value
    private void putValue(String key, String value) {
        sPreferences.edit().putString(key, value).commit();
    }

    private String getValue(String key, String defaultValue) {
        return sPreferences.getString(key, defaultValue);
    }

    // Integer value
    private void putValue(String key, int value) {
        sPreferences.edit().putInt(key, value).commit();
    }

    private int getValue(String key, int defaultValue) {
        return sPreferences.getInt(key, defaultValue);
    }

    // Double value
    private void putValue(String key, double value) {
        sPreferences.edit().putString(key, value+"").commit();
    }

    private double getValue(String key, double defaultValue) {
        return Double.parseDouble(sPreferences.getString(key, defaultValue+""));
    }

    // List of generic object as a value
    private <T> void putValue(String key, List<T> value) {
        //
        Gson gson = new Gson();
        String json = gson.toJson(value);

        sPreferences.edit().putString(key, json).commit();
    }

    private <T> ArrayList<T> getValue(String key, Type type) {
        String json = sPreferences.getString(key, "");

        Gson gson = new Gson();
        ArrayList<T> list = gson.fromJson(json, type);

        return list;
    }


    // Save data into shared preferences
    public void setAppLoginCounter(int prefUserToken) {
        putValue(PREF_APP_LOGIN_COUNTER, prefUserToken);
    }

    public int getAppLoginCounter() {
        return getValue(PREF_APP_LOGIN_COUNTER, 0);
    }


    public void setUserId(int userId) {
        putValue(PREF_USER_ID, userId);
    }

    public int getUserId() {
        return getValue(PREF_USER_ID, 0);
    }

    public void setUserName(String prefFirstName) {
        putValue(PREF_USER_NAME, prefFirstName);
    }

    public String getUserName() {
        return getValue(PREF_USER_NAME, "");
    }

    public void setUserEmail(String prefEmail) {
        putValue(PREF_USER_EMAIL, prefEmail);
    }

    public String getUserEmail() {
        return getValue(PREF_USER_EMAIL, "");
    }

    public void setUserImageUrl(String prefUserImageUrl) {
        putValue(PREF_USER_IMAGE_URL, prefUserImageUrl);
    }

    public String getUserImageUrl() {
        return getValue(PREF_USER_IMAGE_URL, "");
    }

    public void setUserToken(String prefUserToken) {
        putValue(PREF_USER_TOKEN, prefUserToken);
    }

    public String getUserToken() {
        return getValue(PREF_USER_TOKEN, "");
    }

    public void setUserOwnedShops(ArrayList<Shop> shops) {
        putValue(PREF_USER_OWNED_SHOPS, shops);
    }

    public ArrayList<Shop> getUserOwnedShops() {
        Type type = new TypeToken<ArrayList<Shop>>() {}.getType();
        return getValue(PREF_USER_OWNED_SHOPS, type);
    }

    public void setUserSocialAcctToken(String prefSocialAcctToken) {
        putValue(PREF_USER_SOCIAL_ACCT_TOKEN, prefSocialAcctToken);
    }

    public String getUserSocialAcctToken() {
        return getValue(PREF_USER_SOCIAL_ACCT_TOKEN, "");
    }

    public void setUserSocialAcctId(String prefUserSocialAcctId) {
        putValue(PREF_USER_SOCIAL_ACCT_ID, prefUserSocialAcctId);
    }

    public String getUserSocialAcctId() {
        return getValue(PREF_USER_SOCIAL_ACCT_ID, "");
    }

    public void setUserAccountType(String prefUserAccountType) {
        putValue(PREF_USER_ACCT_TYPE, prefUserAccountType);
    }

    public String getUserAccountType() {
        return getValue(PREF_USER_ACCT_TYPE, "");
    }

    public void saveUser(User user) {
        setUserId(user.getId());
        setUserName(user.getName());
        setUserEmail(user.getEmail());
        setUserImageUrl(user.getImageURL());
        setUserToken(user.getToken());
        setUserOwnedShops(user.getOwnedShops());
        setUserSocialAcctToken(user.getSocialAcctToken());
        setUserSocialAcctId(user.getSocialAcctId());
        setUserAccountType(user.getAccountType());
    }

    public void clearUserInfo() {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.remove(PREF_USER_ID)
                .remove(PREF_USER_NAME)
                .remove(PREF_USER_EMAIL)
                .remove(PREF_USER_IMAGE_URL)
                .remove(PREF_USER_TOKEN)
                .remove(PREF_USER_OWNED_SHOPS)
                .remove(PREF_USER_SOCIAL_ACCT_TOKEN)
                .remove(PREF_USER_SOCIAL_ACCT_ID)
                .remove(PREF_USER_ACCT_TYPE);
        editor.commit();
    }

    public User getUser() {
        User user = new User();
        user.setId(getUserId());
        user.setName(getUserName());
        user.setEmail(getUserEmail());
        user.setImageURL(getUserImageUrl());
        user.setToken(getUserToken());
        user.setOwnedShops(getUserOwnedShops());
        user.setAccountType(getUserAccountType());
        user.setSocialAcctToken(getUserSocialAcctToken());
        user.setSocialAcctId(getUserSocialAcctId());
        return user;
    }

    public boolean isLoggedIn() {
        String authenticationToken = getUserToken();
        return !TextUtils.isEmpty(authenticationToken);
    }

    public boolean isShowLoginRequired() {
        return getAppLoginCounter() >= 1;
    }

    public void saveLoginCounter(int counter) {
        setAppLoginCounter(counter);
    }

    public void clearLoginCounter() {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.remove(PREF_APP_LOGIN_COUNTER);
        editor.commit();
    }
}

