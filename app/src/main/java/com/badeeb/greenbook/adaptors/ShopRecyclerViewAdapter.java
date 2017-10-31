package com.badeeb.greenbook.adaptors;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.fragments.ShopListResultFragment;
import com.badeeb.greenbook.models.Shop;
import com.badeeb.greenbook.shared.Utils;
import com.badeeb.greenbook.view.ShopViewHolder;
import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by ahmed on 10/21/2017.
 */

public class ShopRecyclerViewAdapter extends RecyclerView.Adapter<ShopViewHolder> {
    private final static String TAG = ShopRecyclerViewAdapter.class.getName();

    private MainActivity mActivity;
    private ShopListResultFragment mParentFragment;
    private List<Shop> mShopList;

    private boolean isFav = true;

    public ShopRecyclerViewAdapter(MainActivity mActivity, List<Shop> shopList, ShopListResultFragment parentFragment){
        this.mActivity = mActivity;
        mParentFragment = parentFragment;
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

        DecimalFormat df = new DecimalFormat("#.#");
        holder.getTvRateValue().setText(df.format(shop.getRate()));
        holder.getRbShopRate().setRating((float)shop.getRate());

        int distance = (int) Utils.distance(shop.getLocation().getLat(), shop.getLocation().getLng() ,
                mActivity.getCurrentLocation().getLatitude(),mActivity.getCurrentLocation().getLongitude());
        holder.getTvNearLocation().setText(distance+" Km around you");

        Glide.with(mActivity)
                .load(shop.getMainPhotoURL())
                .asBitmap()
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
            }
        });

        holder.getIvFavShop().setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListener - ivFavShop - icon pressed");
                Drawable iconPressed = mActivity.getResources().getDrawable(R.drawable.btn_fav_prassed);
                Drawable iconNotPressed = mActivity.getResources().getDrawable(R.drawable.ic_fav);
                ImageView selectedFav = view.findViewById(R.id.ivFav);
                if(isFav) {
                    Log.d(TAG, "setupListener - ivFavShop - change to pressed");
                    // TODO - add favorite action
                    selectedFav.setImageDrawable(iconPressed);
                    isFav = false;
                }else{
                    Log.d(TAG, "setupListener - ivFavShop - change to not pressed");
                    // TODO - remove from favorite action
                    selectedFav.setImageDrawable(iconNotPressed);
                    isFav = true;
                }
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
