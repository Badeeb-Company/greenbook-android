package com.badeeb.greenbook.shared;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.badeeb.greenbook.BuildConfig;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Amr Alghawy on 6/12/2017.
 */

public class Constants {

    public static final String BASE_URL = BuildConfig.BASE_URL;
    public static final String OAUTH_WEB_CLIENT_ID = BuildConfig.OAUTH_WEB_CLIENT_ID;

    public static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.org.badeeb.greenbook";

    // Volley constants
    public static final int VOLLEY_TIME_OUT = 8000; // Milliseconds
    public static final int VOLLEY_RETRY_COUNTER = 2;

    // Location updates constants
    public static final int UPDATE_TIME = 0;    // Millisecoonds
    public static final int UPDATE_DISTANCE = 0;        // meters

    // Splash Screen timeout
    public static final int SPLASH_TIME_OUT = 3000;

    //AutoComplete Constant
    public static final int THRESHOLD = 1;
    public static final int GALLERY_PHOTOS_PER_LINE = 2;

    public static final LatLngBounds BOUNDS_MIDDLE_EAST = new LatLngBounds(
            new LatLng(22.001018, 25.000663), new LatLng(31.487536, 35.096937));


    public static final String GO_TO_ADD_REVIEW = "GO_TO_ADD_REVIEW";
    public static final String GO_TO_PROFILE_TAB = "GO_TO_PROFILE_TAB";
}
