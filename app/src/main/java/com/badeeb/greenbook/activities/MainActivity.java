package com.badeeb.greenbook.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.fragments.FavoriteFragment;
import com.badeeb.greenbook.fragments.LoginFragment;
import com.badeeb.greenbook.fragments.ProfileFragment;
import com.badeeb.greenbook.fragments.ShopSearchFragment;
import com.badeeb.greenbook.models.User;
import com.badeeb.greenbook.shared.AppSettings;
import com.badeeb.greenbook.shared.ErrorDisplayHandler;
import com.badeeb.greenbook.shared.OnPermissionsGrantedHandler;
import com.badeeb.greenbook.shared.UiUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;

import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    private Toolbar mtoolbar;
    private FragmentManager mFragmentManager;
    private BottomNavigationView mBottomNavigationView;

    private User mUser;
    private AppSettings mAppSettings;
    private ErrorDisplayHandler mSnackBarDisplayer;
    private Location mCurrentLocation;

    private String mState;

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
        mSnackBarDisplayer = createSnackBarDisplayer();
        // Toolbar
        mtoolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mtoolbar);
        // Bottom Navigation bar
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.btvNavigation);

        mState = "";

        // Check if user was logged in before or not
        mAppSettings.clearUserInfo();
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
        fragmentTransaction.replace(R.id.main_frame, loginFragment, loginFragment.TAG);
        fragmentTransaction.commit();
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        this.mUser = user;

        // Fill owned shops
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

                changeNavigationIconsState(item.getItemId());

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

    public void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void goToFavorite() {

        Fragment fragment = mFragmentManager.findFragmentByTag(FavoriteFragment.TAG);
        if (fragment != null && fragment instanceof FavoriteFragment && fragment.isVisible())
            return;

        FavoriteFragment favoriteFragment = new FavoriteFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, favoriteFragment, favoriteFragment.TAG);
        fragmentTransaction.commit();

        changeNavigationIconsState(R.id.aiFavorite);
    }

    private void goToProfileEdit() {
        Fragment fragment = mFragmentManager.findFragmentByTag(ProfileFragment.TAG);
        if (fragment != null && fragment instanceof ProfileFragment && fragment.isVisible())
            return;

        ProfileFragment profileFragment = new ProfileFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, profileFragment, profileFragment.TAG);
        fragmentTransaction.commit();

        changeNavigationIconsState(R.id.aiProfile);
    }

    private void goToShopSearch() {
        Log.d(TAG, "goTo shop search - start");
        Fragment fragment = mFragmentManager.findFragmentByTag(ShopSearchFragment.TAG);
        if (fragment != null && fragment instanceof ShopSearchFragment && fragment.isVisible())
            return;

        ShopSearchFragment shopSearchFragment = new ShopSearchFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, shopSearchFragment, shopSearchFragment.TAG);
        fragmentTransaction.commit();

        changeNavigationIconsState(R.id.aiSearch);
        Log.d(TAG, "goTo shop search - end");

    }

    public void changeNavigationIconsState(int itemId) {
        Menu menu = mBottomNavigationView.getMenu();
        MenuItem favorite = menu.findItem(R.id.aiFavorite);
        MenuItem search = menu.findItem(R.id.aiSearch);
        MenuItem profile = menu.findItem(R.id.aiProfile);

        switch (itemId) {
            case R.id.aiSearch:
                search.setIcon(getResources().getDrawable(R.drawable.ic_search_pressed));
                favorite.setIcon(getResources().getDrawable(R.drawable.ic_fav_dimmed));
                profile.setIcon(getResources().getDrawable(R.drawable.ic_profile_dimmed));
                break;
            case R.id.aiFavorite:
                favorite.setIcon(getResources().getDrawable(R.drawable.ic_fav_pressed));
                search.setIcon(getResources().getDrawable(R.drawable.ic_search_dimmed));
                profile.setIcon(getResources().getDrawable(R.drawable.ic_profile_dimmed));
                break;
            case R.id.aiProfile:
                profile.setIcon(getResources().getDrawable(R.drawable.ic_profile_pressed));
                favorite.setIcon(getResources().getDrawable(R.drawable.ic_fav_dimmed));
                search.setIcon(getResources().getDrawable(R.drawable.ic_search_dimmed));
                break;
        }
    }

    public ErrorDisplayHandler getmSnackBarDisplayer() {
        return mSnackBarDisplayer;
    }

    private ErrorDisplayHandler createSnackBarDisplayer() {
        return new ErrorDisplayHandler(){
            @Override
            public void displayError(String message) {
                UiUtils.showSnackBar(findViewById(R.id.main_frame),message, Snackbar.LENGTH_LONG,
                        getResources().getColor(R.color.orange),
                        R.drawable.btn_close,new View.OnClickListener(){
                            @Override
                            public void onClick(View view) {
                            }
                        });
            }


            @Override
            public void displayErrorWithAction(String message, int icon, View.OnClickListener onClickListener) {
                UiUtils.showSnackBar(findViewById(R.id.main_frame),message,Snackbar.LENGTH_LONG,
                        getResources().getColor(R.color.orange),
                        icon,onClickListener);
            }
        };
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.mCurrentLocation = currentLocation;
    }

    public String getState() {
        return mState;
    }

    public void setState(String state) {
        this.mState = state;
    }
}
