package com.badeeb.greenbook.adaptors;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.fragments.ShopListResultFragment;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.shared.Utils;
import com.badeeb.greenbook.view.ShopViewHolder;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by ahmed on 10/21/2017.
 */

public class ShopRecyclerViewAdaptor extends RecyclerView.Adapter<ShopViewHolder> {
    private final static String TAG = ShopRecyclerViewAdaptor.class.getName();

    private MainActivity mActivity;
    private ShopListResultFragment mParentFragment;
    private List<Shop> mShopList;

    public ShopRecyclerViewAdaptor(MainActivity mActivity, List<Shop> shopList, ShopListResultFragment parentFragment){
        this.mActivity = mActivity;
        mShopList = shopList;
        mParentFragment = parentFragment;
    }

    @Override
    public ShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, " onCreateViewHolder - Start");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.shop_card_view,parent,false);
        Log.d(TAG, " onCreateViewHolder - End");
        return new ShopViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ShopViewHolder holder, int position) {
        Log.d(TAG, " onBindViewHolder - Start - position: "+position);
        Shop shop = mShopList.get(position);

        Log.d(TAG, " onBindViewHolder - ShopName: "+shop.getName());
        holder.getTvShopName().setText(shop.getName());
        holder.getTvDescription().setText(shop.getDescription());

        int distance = (int) Utils.distance(shop.getLocation().getLat(), shop.getLocation().getLng() ,
                mActivity.getCurrentLocation().getLatitude(),mActivity.getCurrentLocation().getLongitude());
        holder.getTvNearLocation().setText(distance+" Km around you");
        Glide.with(mActivity).load(shop.getMainPhotoURL()).into(holder.getIvShopMainPhoto());

        setupListener(holder,position);
        Log.d(TAG, " onBindViewHolder - End ");
    }

    private void setupListener(ShopViewHolder holder, final int position) {
        holder.getTvShopName().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d(TAG, "shop on select - Start");
                Log.d(TAG, "shop on select - selected position: "+position);

                mParentFragment.goToSelectedShop(position);

                Log.d(TAG, "shop on select - end");
            }
        });
    }

    @Override
    public int getItemCount() {
        if(mShopList == null)
            return 0;
        return mShopList.size();
    }
}
