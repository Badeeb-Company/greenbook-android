package com.badeeb.greenbook.shared;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Amr Alghawy on 6/12/2017.
 */

public class Constants {

    public static final String BASE_URL = "https://staging-greenbook.herokuapp.com/api/v1/";

    // Volley constants
    public static final int VOLLEY_TIME_OUT = 8000; // Milliseconds
    public static final int VOLLEY_RETRY_COUNTER = 0;

    // Location updates constants
    public static final int UPDATE_TIME = 10 * 1000;    // Millisecoonds
    public static final int UPDATE_DISTANCE = 10;        // meters

    // Splash Screen timeout
    public static final int SPLASH_TIME_OUT = 3000;

    //AutoComplete Constant
    public static final int THRESHOLD = 1;
}
