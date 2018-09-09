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

    private Context mContext;
    private List<Category> mCategoryList;
    private View.OnClickListener onItemClickListener;

    public CategoryRecyclerViewAdapter(Context context, List<Category> categoryList) {
        Log.d(TAG, " inside Constructor");
        mContext = context;
        mCategoryList = categoryList;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, " onCreateViewHolder - Start");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.categry_card_view, parent, false);
        Log.d(TAG, " onCreateViewHolder - End");
        return new CategoryViewHolder(itemView);
    }

    public void setOnItemClickListener(View.OnClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        Category category = mCategoryList.get(position);
        if (onItemClickListener != null) {
            holder.itemView.setTag(category.getName());
            holder.itemView.setOnClickListener(onItemClickListener);
        }
        holder.getTvCategoryName().setText(category.getName());
    }


    @Override
    public int getItemCount() {
        return mCategoryList.size();
    }

}
