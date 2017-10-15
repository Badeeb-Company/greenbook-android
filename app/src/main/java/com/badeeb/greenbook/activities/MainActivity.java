package com.badeeb.greenbook.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.fragments.LoginFragment;
import com.badeeb.greenbook.models.User;
import com.badeeb.greenbook.shared.AppSettings;
import com.badeeb.greenbook.shared.UiUtils;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

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

        setupListener();

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

    private void setupListener() {


        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                changeNavigationIconsState(item);

                switch (item.getItemId()) {
                    case R.id.aiSearch:
                        goToShopSearch();
                        break;
                    case R.id.aiFavorite:
                        goToFavorite();
                        break;
                    case R.id.aiProfile:
                        goToProfileEdit();
                        break;
                }
                return true;
            }
        });
    }

    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void goToFavorite() {
    }

    private void goToProfileEdit() {
    }

    private void goToShopSearch() {
    }

    private void changeNavigationIconsState(MenuItem item) {
        Menu menu = mBottomNavigationView.getMenu();
        MenuItem favorite = menu.findItem(R.id.aiFavorite);
        MenuItem search = menu.findItem(R.id.aiSearch);
        MenuItem profile = menu.findItem(R.id.aiProfile);

        switch (item.getItemId()) {
            case R.id.aiSearch:
                search.setIcon(getResources().getDrawable(R.drawable.ic_maps_pressed));
                favorite.setIcon(getResources().getDrawable(R.drawable.ic_fav_dimmed));
                profile.setIcon(getResources().getDrawable(R.drawable.ic_profile_dimmed));
                break;
            case R.id.aiFavorite:
                favorite.setIcon(getResources().getDrawable(R.drawable.ic_fav_pressed));
                search.setIcon(getResources().getDrawable(R.drawable.ic_maps_dimmed));
                profile.setIcon(getResources().getDrawable(R.drawable.ic_profile_dimmed));
                break;
            case R.id.aiProfile:
                profile.setIcon(getResources().getDrawable(R.drawable.ic_profile_pressed));
                favorite.setIcon(getResources().getDrawable(R.drawable.ic_fav_dimmed));
                search.setIcon(getResources().getDrawable(R.drawable.ic_maps_dimmed));
                break;
        }
    }

    private enum PreviousStatus {NONE, AVAILABLE, CANCELLED}

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed - Start");
        super.onBackPressed();




        Log.d(TAG, "onBackPressed - End");
    }
}
