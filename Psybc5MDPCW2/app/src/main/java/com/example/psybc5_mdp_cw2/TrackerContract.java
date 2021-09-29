package com.example.psybc5_mdp_cw2;

import android.net.Uri;

public class TrackerContract {

    public static final String AUTHORITY = "psybc5_mdp_cw2.tracker";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final Uri RUN_URI = Uri.parse("content://"+AUTHORITY+"/runs/");

    public static final String CONTENT_TYPE_SINGLE = "vnd.android.cursor.item/runs.data.text";
    public static final String CONTENT_TYPE_MULTIPLE = "vnd.android.cursor.dir/runs.data.text";

    public static final String DB_NAME = "tracker_db";
    public static final int DB_VERSION = 1;

}
