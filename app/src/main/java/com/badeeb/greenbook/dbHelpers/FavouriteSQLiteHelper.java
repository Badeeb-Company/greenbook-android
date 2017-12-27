package com.badeeb.greenbook.dbHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.badeeb.greenbook.models.Shop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ahmed on 11/4/2017.
 */

public class FavouriteSQLiteHelper  {
    private final static String TAG = FavouriteSQLiteHelper.class.getName();

    private SQLiteHelper sqLiteHelper;

    public FavouriteSQLiteHelper(Context context){
        sqLiteHelper = new SQLiteHelper(context);
    }

    public void addFavourite(String shopId){
        Log.d(TAG, "addFavourite - start");

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(sqLiteHelper.SHOP_ID, shopId);

        long insertedID = db.insert(sqLiteHelper.TABLE_FAVOURITE, null, contentValues);
        Log.d(TAG, "addFavourite - insert ID : "+insertedID);

        db.close();

        Log.d(TAG, "addFavourite - end");
    }

    public void removeFavourite(String shopId){
        Log.d(TAG, "removeFavourite - start");

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        int deletedRowCount = db.delete(sqLiteHelper.TABLE_FAVOURITE, sqLiteHelper.SHOP_ID + " = ?",
                new String[] { String.valueOf(shopId) });
        db.close();

        Log.d(TAG, "removeFavourite - number of deleted rows: "+deletedRowCount);

        Log.d(TAG, "removeFavourite - end");
    }

    public List<String> getAllFavouriteIds(){
        Log.d(TAG, "getAllFavouriteIds - start");

        List<String> favList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + sqLiteHelper.TABLE_FAVOURITE;

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                favList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        Log.d(TAG, "getAllFavouriteIds - favList: "+ Arrays.toString(favList.toArray()));

        Log.d(TAG, "getAllFavouriteIds - end");

        // return contact list
        return favList;



    }

}
