package com.example.psybc5_mdp_cw2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TrackerContentProvider extends ContentProvider {

    private SQLiteDatabase db;
    private UriMatcher uriMatcher;

    static final int ID_RUN = 1;

    //(shouldn't need a ContentObserver, since the ContentProvider only allows external apps to read from the database not make any changes)

    @Override
    public boolean onCreate() {
        DBHelper dbHelper = DBHelper.getInstance(getContext());
        db = dbHelper.getWritableDatabase();

        //Basic URI matcher, just allows one to specify which table to query this ContentProvider for
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(TrackerContract.AUTHORITY,"runs",ID_RUN);

        //uriMatcher.addURI(RecipeContract.AUTHORITY,"recipe/#",ID_SINGLE);
        //uriMatcher.addURI(RecipeContract.AUTHORITY,"ingredient/#",ID_SINGLE);
        //uriMatcher.addURI(RecipeContract.AUTHORITY,"recipeingredient/#",ID_SINGLE);

        if (db == null) return false; else return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        //Get table to query from URI:
        switch(uriMatcher.match(uri)) {
            case (ID_RUN): qb.setTables("runs"); break;
            default: throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor c = qb.query (db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(),uri);
        return c;

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        if (uri.getLastPathSegment() == null) {
            return TrackerContract.CONTENT_TYPE_SINGLE;
        }else{
            return TrackerContract.CONTENT_TYPE_MULTIPLE;
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) { return 0; }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) { return 0; }

}
