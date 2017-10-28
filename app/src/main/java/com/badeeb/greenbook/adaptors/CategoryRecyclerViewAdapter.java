package com.badeeb.greenbook.adaptors;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.models.Category;
import com.badeeb.greenbook.view.CategoryViewHolder;

import java.util.List;

/**
 * Created by ahmed on 10/20/2017.
 */

public class CategoryRecyclerViewAdapter extends RecyclerView.Adapter<CategoryViewHolder> {
    private final static String TAG = CategoryRecyclerViewAdapter.class.getName();

    public Context mContext;
    public List<Category> mCategoryList;

    public CategoryRecyclerViewAdapter(Context context, List<Category> categoryList){
        Log.d(TAG, " inside Constructor");
        mContext = context;
        mCategoryList = categoryList;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, " onCreateViewHolder - Start");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.categry_card_view,parent,false);
        Log.d(TAG, " onCreateViewHolder - End");
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        Log.d(TAG, " onBindViewHolder - Start - position: "+position);
        Category category = mCategoryList.get(position);

        Log.d(TAG, " onBindViewHolder - CategoryName: "+category.getName());
        holder.getTvCategoryName().setText(category.getName());

        Log.d(TAG, " onBindViewHolder - End ");
    }



    @Override
    public int getItemCount() {
        return mCategoryList.size();
    }

}
