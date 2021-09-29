package com.example.psybc5_mdp_cw2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    //Using singleton pattern
    private static DBHelper dbInstance = null;

    private final String SQL_CREATE_RUN = "CREATE TABLE runs (" +
            " _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            " dateStart INTEGER NOT NULL," +
            " dateEnd INTEGER NOT NULL," +
            " distance VARCHAR (128) NOT NULL," +
            " rating INTEGER," +
            " note VARCHAR (512)," +
            " weather VARCHAR (128)); ";


    //Static constructor, checks if an instance of DBHelper already exists before instantiating a new one
    public static synchronized DBHelper getInstance(Context context) {
        if (dbInstance == null)
            dbInstance = new DBHelper(context.getApplicationContext());
        return dbInstance;
    }

    public DBHelper(Context context) {
        super(context, TrackerContract.DB_NAME, null, TrackerContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_RUN);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    //If the database version has changed, just delete the local one and recreate it
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS run");
            onCreate(db);
        }
    }
}
