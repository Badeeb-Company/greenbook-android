package com.badeeb.greenbook.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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

import com.android.volley.Request;
import com.badeeb.greenbook.R;
import com.badeeb.greenbook.dbHelpers.FavouriteSQLiteHelper;
import com.badeeb.greenbook.fragments.FavoriteFragment;
import com.badeeb.greenbook.fragments.LoginFragment;
import com.badeeb.greenbook.fragments.NotLoggedInProfileFragment;
import com.badeeb.greenbook.fragments.ProfileFragment;
import com.badeeb.greenbook.fragments.ShopSearchFragment;
import com.badeeb.greenbook.models.FavouriteInquiry;
import com.badeeb.greenbook.models.JsonRequest;
import com.badeeb.greenbook.models.JsonResponse;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.models.User;
import com.badeeb.greenbook.network.AuthorizedCallback;
import com.badeeb.greenbook.network.VolleyWrapper;
import com.badeeb.greenbook.shared.AdapterNotifier;
import com.badeeb.greenbook.shared.AppSettings;
import com.badeeb.greenbook.shared.Constants;
import com.badeeb.greenbook.shared.ErrorDisplayHandler;
import com.badeeb.greenbook.shared.UiUtils;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    private Toolbar mtoolbar;
    private FragmentManager mFragmentManager;
    private BottomNavigationView mBottomNavigationView;
    private ProgressDialog mProgressDialog;

    private User mUser;
    private HashSet<Integer> mOwnedShopsSet;
    private AppSettings mAppSettings;
    private ErrorDisplayHandler mSnackBarDisplayer;
    private Location mCurrentLocation;

    private Set<Integer> mFavSet;
    private AdapterNotifier mFavAdapterNotifier;
    private FavouriteSQLiteHelper favouriteSQLiteHelper;

    // PlaceAutoComplete Attributes
    private GoogleApiClient mPlaceGoogleApiClient;

    private String mState;
    private Shop mShopUnderReview;

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
        mProgressDialog = UiUtils.createProgressDialog(this);

        // Toolbar
        mtoolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mtoolbar);
        // Bottom Navigation bar
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.btvNavigation);

        mState = "";
        mOwnedShopsSet = new HashSet<>();

        mFavSet = new HashSet<>();
        favouriteSQLiteHelper = new FavouriteSQLiteHelper(this);


        // Check if user was logged in before or not
        mAppSettings.clearUserInfo();
        if (mAppSettings.isLoggedIn()) {
            mUser = mAppSettings.getUser();
            // Go to Search screen directly
        }
        else {
            // Go to login screen
        }

        updateFavouriteSet();

        initPlaceAutoComplete();

        goToLogin();

        setupListener();

        Log.d(TAG, "init - End");
    }



    public void initPlaceAutoComplete(){
        Log.d(TAG, "initPlaceAutoComplete - start ");
        mPlaceGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build();


        Log.d(TAG, "initPlaceAutoComplete - end ");
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

        if (user != null) {
            // Fill owned shops
            for (Shop shop : user.getOwnedShops()) {
                this.mOwnedShopsSet.add(shop.getId());
            }
        }
    }

    public HashSet<Integer> getmOwnedShopsSet() {
        return mOwnedShopsSet;
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
                        if (mUser == null)
                            goToNotLoggedInProfileFragment();
                        else
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

    private void goToNotLoggedInProfileFragment() {
        Fragment fragment = mFragmentManager.findFragmentByTag(NotLoggedInProfileFragment.TAG);
        if (fragment != null && fragment instanceof NotLoggedInProfileFragment && fragment.isVisible())
            return;

        NotLoggedInProfileFragment notLoggedInProfileFragment = new NotLoggedInProfileFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, notLoggedInProfileFragment, NotLoggedInProfileFragment.TAG);
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

    public GoogleApiClient getmPlaceGoogleApiClient() {
        return mPlaceGoogleApiClient;
    }

    public void connectPlaceGoogleApiClient(){
        if(mPlaceGoogleApiClient != null && !mPlaceGoogleApiClient.isConnected()){
            mPlaceGoogleApiClient.connect();
        }
    }

    public void disconnectPlaceGoogleApiClient(){
        if(mPlaceGoogleApiClient != null && mPlaceGoogleApiClient.isConnected()){
            mPlaceGoogleApiClient.disconnect();
        }
    }

    public String getState() {
        return mState;
    }

    public void setState(String state) {
        this.mState = state;
    }

    public Shop getmShopUnderReview() {
        return mShopUnderReview;
    }

    public void setmShopUnderReview(Shop mShopUnderReview) {
        this.mShopUnderReview = mShopUnderReview;
    }

    public Set<Integer> getFavSet() {
        if(mFavSet == null)
            mFavSet = new HashSet<>();

        return mFavSet;
    }


    public void updateFavouriteSet() {
        Log.d(TAG, "updateFavouriteSet - Start");
        if(getUser() != null){
            Log.d(TAG, "getFavouriteList - in login mode");
            callFavApi();
        }else{
            Log.d(TAG, "getFavouriteList - in offline mode");
            updateFavShopListFromDb();
        }
        Log.d(TAG, "updateFavouriteSet - end");
    }

    public void addToFavourite(Shop selectedShop, AdapterNotifier adapterNotifier){
        Log.d(TAG, "addToFavourite - Start");
        mFavAdapterNotifier = adapterNotifier;
        if(getUser() != null){
            callAddFavouriteApi(selectedShop);
        }else{
            favouriteSQLiteHelper.addFavourite(selectedShop.getId());
            updateFavShopListFromDb();
        }

        Log.d(TAG, "addToFavourite - End");
    }

    public void removeFromFavourite(Shop selectedShop, AdapterNotifier adapterNotifier){
        Log.d(TAG, "removeFromFavourite - Start");
        mFavAdapterNotifier = adapterNotifier;
        if(getUser() != null){
            callRemoveFavouriteApi(selectedShop);
        }else{
            favouriteSQLiteHelper.removeFavourite(selectedShop.getId());
            updateFavShopListFromDb();
        }



        Log.d(TAG, "removeFromFavourite - End");
    }


    private void updateFavShopListFromDb() {
        Log.d(TAG, "updateFavShopListFromDb - Start");

        List<Integer> updatedList = favouriteSQLiteHelper.getAllFavouriteIds();
        Log.d(TAG, "updateFavShopListFromDb - updatedList : "+Arrays.toString(updatedList.toArray()));

        mFavSet = new HashSet<>();
        mFavSet.addAll(updatedList);

        Log.d(TAG, "updateFavShopListFromDb - mFavSet : "+Arrays.toString(mFavSet.toArray()));




        if(mFavAdapterNotifier != null ) {
            if (mFavSet.isEmpty()) {
                Log.d(TAG, "updateFavShopListFromDb - notify empty list");
                mFavAdapterNotifier.notifyEmptyList();
            } else {
                Log.d(TAG, "updateFavShopListFromDb - notify adapter");
                mFavAdapterNotifier.notifyAdapter();
            }
        }

        Log.d(TAG, "updateFavShopListFromDb - End");
    }


    private void callFavApi() {
        Log.d(TAG, "callFavApi - Start");
        mProgressDialog.show();
        String url = Constants.BASE_URL + "/shops/favourites" ;
        Log.d(TAG, "callFavApi - Request URL: " + url);

        AuthorizedCallback<JsonResponse<FavouriteInquiry>> callback = new AuthorizedCallback<JsonResponse<FavouriteInquiry>>(getUser().getToken()) {

            @Override
            public void onSuccess(JsonResponse<FavouriteInquiry> jsonResponse) {
                Log.d(TAG, "callFavApi - AuthorizedCallback - onSuccess");

                if (jsonResponse != null
                        && jsonResponse.getResult() != null
                        && jsonResponse.getResult().getShopList() != null
                        && !jsonResponse.getResult().getShopList().isEmpty()) {

                    Log.d(TAG, "callFavApi - AuthorizedCallback - onSuccess - shopList: "
                            + Arrays.toString(jsonResponse.getResult().getShopList().toArray()));


                    if(mFavAdapterNotifier != null)
                        mFavAdapterNotifier.notifyAdapter();

                    mFavSet = new HashSet<>();
                    for(Shop fav : jsonResponse.getResult().getShopList()){
                        if(fav != null){
                            Log.d(TAG, "updateFavouriteSet - insert fav: "+fav.getName()+" in set");
                            getFavSet().add(fav.getId());
                        }
                    }

                } else {
                    //switch to empty search
                    mFavSet = new HashSet<>();
                    if(mFavAdapterNotifier != null)
                        mFavAdapterNotifier.notifyEmptyList();
                    Log.d(TAG, "callFavApi - AuthorizedCallback - onSuccess - empty search ");
                }
                mProgressDialog.dismiss();

            }

            @Override
            public void onError() {
                Log.d(TAG, "callFavApi - AuthorizedCallback - onError");
                mProgressDialog.dismiss();
            }
        };

        Type responseType = new TypeToken<JsonResponse<FavouriteInquiry>>() {
        }.getType();

        VolleyWrapper<Object, JsonResponse<FavouriteInquiry>> volleyWrapper = new VolleyWrapper<>(null, responseType, Request.Method.GET, url,
                callback, this, getmSnackBarDisplayer(), findViewById(R.id.ll_main_view));
        volleyWrapper.execute();

        Log.d(TAG, "callFavApi - end");
    }

    private void callAddFavouriteApi(Shop selectedShop){
        mProgressDialog.show();

        final Shop shop = selectedShop;

        String url = Constants.BASE_URL + "/shops/" + shop.getId() + "/favourite";

        Log.d(TAG, "callAddFavouriteApi - url: " + url);

        AuthorizedCallback<JsonResponse<FavouriteInquiry>> callback = new AuthorizedCallback<JsonResponse<FavouriteInquiry>>(getUser().getToken()) {
            @Override
            public void onSuccess(JsonResponse<FavouriteInquiry> jsonResponse) {
                Log.d(TAG, "callAddFavouriteApi - onSuccess - Start");


                hideKeyboard();


                mProgressDialog.dismiss();

                updateFavouriteSet();

                Log.d(TAG, "callAddFavouriteApi - onSuccess - End");
            }

            @Override
            public void onError() {
                Log.d(TAG, "callAddFavouriteApi - onError - Start");

                getmSnackBarDisplayer().displayError(shop.getName()+" couldn't be added to favourite");
                mProgressDialog.dismiss();

                Log.d(TAG, "callAddFavouriteApi - onError - End");
            }
        };

        // Prepare response type
        Type responseType = new TypeToken<JsonResponse<FavouriteInquiry>>() {}.getType();

        VolleyWrapper<JsonRequest<FavouriteInquiry>, JsonResponse<FavouriteInquiry>> volleyWrapper =
                new VolleyWrapper<>(null, responseType, Request.Method.POST, url,
                        callback, this, getmSnackBarDisplayer(), findViewById(R.id.ll_main_view));
        volleyWrapper.execute();

    }

    private void callRemoveFavouriteApi(Shop selectedShop){
        mProgressDialog.show();

        final Shop shop = selectedShop;

        String url = Constants.BASE_URL + "/shops/" + shop.getId() + "/favourite";

        Log.d(TAG, "callRemoveFavouriteApi - url: " + url);

        AuthorizedCallback<JsonResponse<FavouriteInquiry>> callback = new AuthorizedCallback<JsonResponse<FavouriteInquiry>>(getUser().getToken()) {
            @Override
            public void onSuccess(JsonResponse<FavouriteInquiry> jsonResponse) {
                Log.d(TAG, "callRemoveFavouriteApi - onSuccess - Start");

                hideKeyboard();
                mProgressDialog.dismiss();

                updateFavouriteSet();

                Log.d(TAG, "callRemoveFavouriteApi - onSuccess - End");
            }

            @Override
            public void onError() {
                Log.d(TAG, "callRemoveFavouriteApi - onError - Start");

                getmSnackBarDisplayer().displayError(shop.getName()+" couldn't be deleted!");
                mProgressDialog.dismiss();

                Log.d(TAG, "callRemoveFavouriteApi - onError - End");
            }
        };

        // Prepare response type
        Type responseType = new TypeToken<JsonResponse<FavouriteInquiry>>() {}.getType();

        VolleyWrapper<JsonRequest<FavouriteInquiry>, JsonResponse<FavouriteInquiry>> volleyWrapper =
                new VolleyWrapper<>(null, responseType, Request.Method.DELETE, url,
                        callback, this, getmSnackBarDisplayer(), findViewById(R.id.ll_main_view));
        volleyWrapper.execute();

    }



    public void clearBackStack() {
        mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//        int counter = mFragmentManager.getBackStackEntryCount();
//        for (int i = 0; i < counter; i++)
//            mFragmentManager.popBackStack();
    }
}
