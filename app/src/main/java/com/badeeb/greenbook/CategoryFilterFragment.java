package com.badeeb.greenbook;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.fragments.ShopListResultFragment;
import com.badeeb.greenbook.models.Category;

import org.parceler.Parcels;

import java.util.List;

public class CategoryFilterFragment extends Fragment {
    public   static final String TAG = CategoryFilterFragment.class.getName();

    private MainActivity mActivity;
    private List<Category> mCategoryList;
    private String mSelectedLocation;

    private TextView tvCategorySearch;
    private ListView lvCategoryList;
    private ArrayAdapter<Category> mCategoryArrayAdapter;
    private AutoCompleteTextView actvLocationSearch;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_category_filter, container, false);

        init(view);

        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - start");

        mActivity = (MainActivity) getActivity();

        loadBundleData();

        initUi(view);

        setupListener();

        Log.d(TAG, "init - end");
    }

    private void loadBundleData() {
        Log.d(TAG, "loadBundleData - start");

        Bundle bundle = getArguments();

        mCategoryList = Parcels.unwrap(bundle.getParcelable(ShopListResultFragment.EXTRA_SELECTED_CATEGORY_LIST));

        mSelectedLocation = bundle.getString(ShopListResultFragment.EXTRA_SELECTED_ADDRESS);

        Log.d(TAG, "loadBundleData - end");
    }

    private void  initUi(View view){
        Log.d(TAG, "initUi - start");

        tvCategorySearch = (TextView) view.findViewById(R.id.tvCategorySearch) ;

        actvLocationSearch = (AutoCompleteTextView) view.findViewById(R.id.actvLocationSearch) ;
        if(mSelectedLocation != null && !mSelectedLocation.isEmpty()){
            actvLocationSearch.setText(mSelectedLocation);
        }

        mCategoryArrayAdapter = new ArrayAdapter<Category>(mActivity, R.layout.categry_card_view, R.id.lvCategoryList, mCategoryList);
        lvCategoryList = (ListView) view.findViewById(R.id.lvCategoryList) ;
        lvCategoryList.setAdapter(mCategoryArrayAdapter);


        Log.d(TAG, "initUi - end");
    }

    private void setupListener() {
        Log.d(TAG, "setupListener - start");

        tvCategorySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "addTextChangedListener - beforeTextChanged - seq: "+s);

                mCategoryArrayAdapter.getFilter().filter(s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Log.d(TAG, "setupListener - end");
    }


}
