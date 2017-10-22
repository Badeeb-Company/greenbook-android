package com.badeeb.greenbook.view;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.badeeb.greenbook.R;

/**
 * Created by ahmed on 10/21/2017.
 */

public class CategoryViewHolder extends RecyclerView.ViewHolder {

    private final static String TAG = CategoryViewHolder.class.getName();

    private TextView tvCategoryName;

    public CategoryViewHolder(View itemView) {
        super(itemView);
        Log.d(TAG, " CategoryViewHolder - Start");
        tvCategoryName = (TextView) itemView.findViewById(R.id.tvCategoryName);
        Log.d(TAG, " CategoryViewHolder - End");
    }

    public static String getTAG() {
        return TAG;
    }

    public TextView getTvCategoryName() {
        return tvCategoryName;
    }

    public void setTvCategoryName(TextView tvCategoryName) {
        this.tvCategoryName = tvCategoryName;
    }
}
