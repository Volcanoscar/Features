package com.android.settings.visitormode;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MltHideAppsProvider extends ContentProvider {
    private static final String TAG = "MalataHideAppsProvider";
    
    private static final String AUTHORITY = "com.android.visitor";

    private static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    
    private static final String DATABASE_NAME = "visitor.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "visitor";

    private SQLiteDatabase mDatabase;
    private DatabaseHelper mDatabaseHelper;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "Enter DatabaseHelper.onCreate()");
            db.execSQL("Create table " + TABLE_NAME + "( _id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "package_name TEXT," + "activity_name TEXT);");
            Log.d(TAG, "Leave DatabaseHelper.onCreate()");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri url, String selection, String[] selectionArgs) {
        Log.d(TAG, "Enter delete()");
        mDatabase = mDatabaseHelper.getWritableDatabase();
        return mDatabase.delete(TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public String getType(Uri url) {
        return null;
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        Log.d(TAG, "Enter insert()");
        mDatabase = mDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues(initialValues);
        long rowId = mDatabase.insert(TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri rowUri = ContentUris.appendId(CONTENT_URI.buildUpon(), rowId).build();
            Log.d(TAG, "Leave insert()");
            return rowUri;
        }
        throw new SQLException("Failed to insert row into " + url);
    }

    @Override
    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Log.d(TAG, "Enter query()");
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        qb.setTables(TABLE_NAME);
        Log.d(TAG, "query(): uri: " + url.toString());

        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), url);
        }
        Log.d(TAG, "Leave query()");
        return c;
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        return 0;
    }
}
