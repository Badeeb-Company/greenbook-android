package com.badeeb.greenbook.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.shared.Constants;

public class SplashActivity extends AppCompatActivity {

    private final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate - Start");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        int splashTimeOut = Constants.SPLASH_TIME_OUT;

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {

                Log.d(TAG, "run - Start");

                // This method will be executed once the timer is over
                // Start your app main activity
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);

                // close this activity
                finish();

                Log.d(TAG, "run - End");
            }
        }, splashTimeOut);

        Log.d(TAG, "onCreate - End");
    }
}
