package com.badeeb.greenbook.adaptors;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.view.ShopViewHolder;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by ahmed on 10/21/2017.
 */

public class ShopRecyclerViewAdaptor extends RecyclerView.Adapter<ShopViewHolder> {
    private final static String TAG = ShopRecyclerViewAdaptor.class.getName();

    private Context mContext;
    private List<Shop> mShopList;

    public ShopRecyclerViewAdaptor(Context context, List<Shop> shopList){
        mContext = context;
        mShopList = shopList;
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
        Glide.with(mContext).load(shop.getMainPhotoURL()).into(holder.getIvShopMainPhoto());
        Log.d(TAG, " onBindViewHolder - End ");
    }

    @Override
    public int getItemCount() {
        if(mShopList == null)
            return 0;
        return mShopList.size();
    }
}
