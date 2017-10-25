package com.badeeb.greenbook.controllers;

import android.app.Application;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

/**
 * Created by Amr Alghawy on 10/15/2017.
 */

public class GreenBookApplication extends Application {

    private static GreenBookApplication sGreenBookApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        sGreenBookApplication = this;
    }

    public static GreenBookApplication getInstance() {
        if (sGreenBookApplication == null) {
            sGreenBookApplication = new GreenBookApplication();
        }
        return sGreenBookApplication;
    }
}
