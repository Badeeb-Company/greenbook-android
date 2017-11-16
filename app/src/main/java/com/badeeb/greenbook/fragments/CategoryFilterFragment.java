package com.badeeb.greenbook.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.badeeb.greenbook.R;
import com.badeeb.greenbook.activities.MainActivity;
import com.badeeb.greenbook.models.Category;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryFilterFragment extends Fragment {
    public   static final String TAG = CategoryFilterFragment.class.getName();

    private MainActivity mActivity;
    private List<Category> mCategoryList;
    private Category mSelectedCategory;
    private String mSelectedLocation;

    private ImageView ivBack;
    private EditText etCategorySearch;
    private ListView lvCategoryList;
    private ArrayAdapter<Category> mCategoryArrayAdapter;
    private FragmentManager mFragmentManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container != null) {
            // this code is used to prevent fragment overlapping
            container.removeAllViews();
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_category_filter, container, false);

        init(view);

        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - start");

        mActivity = (MainActivity) getActivity();
        mFragmentManager = getFragmentManager();
        mCategoryList = new ArrayList<>();
//        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        loadBundleData();

        initUi(view);

        setupListener();

        mActivity.showKeyboard(etCategorySearch);

        Log.d(TAG, "init - end");
    }

    private void loadBundleData() {
        Log.d(TAG, "loadBundleData - start");

        Bundle bundle = getArguments();

        mCategoryList = Parcels.unwrap(bundle.getParcelable(ShopListResultFragment.EXTRA_SELECTED_CATEGORY_LIST));
        Log.d(TAG, "Bundle mCategoryList : "+ Arrays.toString(mCategoryList.toArray()));

        mSelectedLocation = bundle.getString(ShopListResultFragment.EXTRA_SELECTED_ADDRESS);

        Log.d(TAG, "loadBundleData - end");
    }

    private void  initUi(View view){
        Log.d(TAG, "initUi - start");

        ivBack = (ImageView) view.findViewById(R.id.ivBack);

        etCategorySearch = (EditText) view.findViewById(R.id.etCategorySearch) ;
        if(etCategorySearch.requestFocus()) {
            mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        lvCategoryList = (ListView) view.findViewById(R.id.lvCategoryList) ;

        mCategoryArrayAdapter = new ArrayAdapter<Category>(mActivity, R.layout.categry_card_view, R.id.tvCategoryName, mCategoryList);

        lvCategoryList.setAdapter(mCategoryArrayAdapter);

        Log.d(TAG, "first item in  mCategoryArrayAdapter : "+ mCategoryArrayAdapter.getItem(0));


        Log.d(TAG, "initUi - end");
    }

    private void setupListener() {
        Log.d(TAG, "setupListener - start");

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentManager.popBackStack();
            }
        });

        etCategorySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "addTextChangedListener - afterTextChanged - seq: "+s);

                mCategoryArrayAdapter.getFilter().filter(s);
            }
        });

        lvCategoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mSelectedCategory = mCategoryList.get(i);
                goToShopListResultFragment();
            }
        });

        Log.d(TAG, "setupListener - end");
    }

    private void goToShopListResultFragment(){
        Log.d(TAG, "goToShopResultListFragment - Start");
        Log.d(TAG, "mSelectedPlace: "+mSelectedLocation);
        Bundle bundle = new Bundle();
        bundle.putParcelable(ShopListResultFragment.EXTRA_SELECTED_CATEGORY, Parcels.wrap(mSelectedCategory));
        bundle.putString(ShopListResultFragment.EXTRA_SELECTED_ADDRESS, mSelectedLocation);
        bundle.putParcelable(ShopListResultFragment.EXTRA_SELECTED_CATEGORY_LIST, Parcels.wrap(mCategoryList));

        ShopListResultFragment shopListResultFragment = new ShopListResultFragment();
        shopListResultFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.main_frame, shopListResultFragment, shopListResultFragment.TAG);

        fragmentTransaction.commit();

        Log.d(TAG, "goToShopResultListFragment - End");
    }


}
