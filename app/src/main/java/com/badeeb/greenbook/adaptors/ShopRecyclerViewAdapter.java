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

    public ShopRecyclerViewAdapter(MainActivity mActivity, List<Shop> shopList, ShopListResultFragment parentFragment) {
        this.mActivity = mActivity;
        mParentFragment = parentFragment;
        mShopList = shopList;
    }

    @Override
    public ShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, " onCreateViewHolder - Start");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.shop_card_view, parent, false);
        Log.d(TAG, " onCreateViewHolder - End");
        return new ShopViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ShopViewHolder holder, int position) {
        Log.d(TAG, " onBindViewHolder - Start - position: " + position);
        Shop shop = mShopList.get(position);

        Log.d(TAG, " onBindViewHolder - ShopName: " + shop.getName());
        holder.getTvShopName().setText(shop.getName());
        holder.getTvAddress().setText(shop.getLocation().getAddress());

        DecimalFormat df = new DecimalFormat("0.0");
        holder.getTvRateValue().setText(df.format(shop.getRate()));
        holder.getRbShopRate().setRating((float) shop.getRate());

        if (!mActivity.getFavSet().isEmpty() && mActivity.getFavSet().contains(shop.getGooglePlaceId())) {
            Log.d(TAG, " onBindViewHolder - shop is fav: " + shop.getName());
            holder.getIvFavShop().setImageDrawable(mActivity.getResources().getDrawable(R.drawable.btn_fav_prassed));
        } else {
            Log.d(TAG, " onBindViewHolder - shop not fav: " + shop.getName());
            holder.getIvFavShop().setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_fav));
        }

        int distance = (int) Utils.distance(shop.getLocation().getLat(),
                shop.getLocation().getLng(), mParentFragment.getCurrentLocation().getLatitude(),
                mParentFragment.getCurrentLocation().getLongitude());
        holder.getTvNearLocation().setText(distance + " miles from you");

        Glide.with(mActivity)
                .load(shop.getMainPhotoURL())
                .asBitmap()
                .placeholder(new ColorDrawable(mActivity.getResources().getColor(R.color.light_gray)))
                .into(holder.getRivShopMainPhoto());

        holder.getTvNumberOfReviews().setText("(" + shop.getNumOfReviews() + ")");

        setupListener(holder, position);

        Log.d(TAG, " onBindViewHolder - End ");
    }

    private void setupListener(ShopViewHolder holder, final int position) {

        holder.getShopDetailsLinearLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListener - shop details linear layout - on click position:" + position);
                mParentFragment.goToSelectedShop(position);
            }
        });

        holder.getIvFavShop().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListener - ivFavShop - icon pressed");
                Shop shop = mShopList.get(position);
                ImageView selectedFav = (ImageView) view.findViewById(R.id.ivFav);
                if (!mActivity.getFavSet().isEmpty() && mActivity.getFavSet().contains(shop.getId())) {
                    Log.d(TAG, "setupListener - ivFavShop - change to not pressed");
                    selectedFav.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_fav));
                    mActivity.removeFromFavourite(shop, null);
                } else {
                    Log.d(TAG, "setupListener - ivFavShop - change to pressed");
                    selectedFav.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.btn_fav_prassed));
                    mActivity.addToFavourite(shop, null);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        if (mShopList == null)
            return 0;
        return mShopList.size();
    }
}
