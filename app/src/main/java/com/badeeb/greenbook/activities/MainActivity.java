package com.badeeb.greenbook.activities;

import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.fragments.LoginFragment;
import com.badeeb.greenbook.models.User;
import com.badeeb.greenbook.shared.AppSettings;
import com.badeeb.greenbook.shared.UiUtils;

public class MainActivity extends AppCompatActivity {

    private final String TAG = SplashActivity.class.getSimpleName();

    private Toolbar mtoolbar;
    private FragmentManager mFragmentManager;
    private BottomNavigationView mBottomNavigationView;

    private User mUser;
    private AppSettings mAppSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate - Start");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        Log.d(TAG, "onCreate - End");
    }

    private void init() {
        Log.d(TAG, "init - Start");

        mFragmentManager = getSupportFragmentManager();
        mAppSettings = AppSettings.getInstance();

        // Toolbar
        mtoolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mtoolbar);
        // Bottom Navigation bar
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.btvNavigation);

        // Check if user was logged in before or not
        if (mAppSettings.isLoggedIn()) {
            mUser = mAppSettings.getUser();
            // Go to Search screen directly
        }
        else {
            // Go to login screen
        }
        goToLogin();

        Log.d(TAG, "init - End");
    }

    private void goToLogin() {
        LoginFragment loginFragment = new LoginFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_frame, loginFragment, loginFragment.TAG);
        fragmentTransaction.commit();
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        this.mUser = user;
    }

    public void hideToolbar() {
        mtoolbar.setVisibility(View.GONE);
    }

    public void showToolbar() {
        mtoolbar.setVisibility(View.VISIBLE);
    }

    public void hideBottomNavigationActionBar() {
        mBottomNavigationView.setVisibility(View.GONE);
    }

    public void showBottomNavigationActionBar() {
        mBottomNavigationView.setVisibility(View.VISIBLE);
    }
}
