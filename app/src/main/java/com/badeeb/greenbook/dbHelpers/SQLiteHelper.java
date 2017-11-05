package com.badeeb.greenbook.dbHelpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ahmed on 11/4/2017.
 */

public class SQLiteHelper extends SQLiteOpenHelper{
    private final static String TAG = SQLiteHelper.class.getName();
    private final static int DATABASE_VERSION = 1;

    private final static String DATABASE_NAME = "GREENBOOK_DB";

    public final static String TABLE_FAVOURITE = "FAVOURITE";
    public final static String SHOP_ID = "SHOP_ID";


    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, " onCreate - Start");

        String CREATE_FAVOURITE_TABLE = "CREATE TABLE " + TABLE_FAVOURITE + "("
                + SHOP_ID + " INTEGER PRIMARY KEY)";

        sqLiteDatabase.execSQL(CREATE_FAVOURITE_TABLE);

        Log.d(TAG, " onCreate - Start");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.d(TAG, " onUpgrade - Start");

        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVOURITE);

        // Create tables again
        onCreate(sqLiteDatabase);

        Log.d(TAG, " onUpgrade - end");
    }
}
