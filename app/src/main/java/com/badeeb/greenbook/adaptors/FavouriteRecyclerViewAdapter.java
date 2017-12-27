package com.badeeb.greenbook.adaptors;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.fragments.FavoriteFragment;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.view.ShopViewHolder;
import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by ahmed on 10/21/2017.
 */

public class FavouriteRecyclerViewAdapter extends RecyclerView.Adapter<ShopViewHolder> {
    private final static String TAG = FavouriteRecyclerViewAdapter.class.getName();

    private MainActivity mActivity;
    private FavoriteFragment mParentFragment;
    private List<Shop> mShopList;

    private boolean isFav = true;

    public FavouriteRecyclerViewAdapter(MainActivity mActivity, List<Shop> shopList, FavoriteFragment parentFragment){
        this.mActivity = mActivity;
        mParentFragment = parentFragment;
        mShopList = shopList;
    }

    @Override
    public ShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, " onCreateViewHolder - Start");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.favourite_card_view,parent,false);
        Log.d(TAG, " onCreateViewHolder - End");
        return new ShopViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ShopViewHolder holder, int position) {
        Log.d(TAG, " onBindViewHolder - Start - position: "+position);
        Shop shop = mShopList.get(position);

        Log.d(TAG, " onBindViewHolder - ShopName: "+shop.getName());
        holder.getTvShopName().setText(shop.getName());
        holder.getTvAddress().setText(shop.getLocation().getAddress());

        holder.getIvFavShop().setImageDrawable(mActivity.getResources().getDrawable(R.drawable.btn_fav_prassed));

        DecimalFormat df = new DecimalFormat("0.0");
        holder.getTvRateValue().setText(df.format(shop.getRate()));
        holder.getRbShopRate().setRating((float)shop.getRate());

        Glide.with(mActivity)
                .load(shop.getMainPhotoURL())
                .asBitmap()
                .placeholder(new ColorDrawable(mActivity.getResources().getColor(R.color.light_gray)))
                .into(holder.getRivShopMainPhoto());

        setupListener(holder, position);

        Log.d(TAG, " onBindViewHolder - End ");
    }

    private void setupListener(ShopViewHolder holder, final int position) {

        holder.getShopDetailsLinearLayout().setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListener - shop details linear layout - on click position:"+position);
                mParentFragment.goToSelectedShop(position);
                mActivity.hideBottomNavigationActionBar();
            }
        });

        holder.getIvFavShop().setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListener - ivFavShop - icon pressed");
                ImageView selectedFav = (ImageView) view.findViewById(R.id.ivFav);

                Log.d(TAG, "setupListener - ivFavShop - change to not pressed");
                mParentFragment.removeFavourite(position);
                selectedFav.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_fav));
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
