package com.badeeb.greenbook.controllers;

import android.app.Application;

/**
 * Created by Amr Alghawy on 10/15/2017.
 */

public class GreenBookApplication extends Application {

    private static GreenBookApplication sGreenBookApplication;

    @Override
    public void onCreate() {
        super.onCreate();

        sGreenBookApplication = this;
    }

    public static GreenBookApplication getInstance() {
        if (sGreenBookApplication == null) {
            sGreenBookApplication = new GreenBookApplication();
        }
        return sGreenBookApplication;
    }
}
