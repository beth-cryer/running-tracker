package com.example.psybc5_mdp_cw2.ui.dialog;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.psybc5_mdp_cw2.DBHelper;
import com.example.psybc5_mdp_cw2.R;
import com.example.psybc5_mdp_cw2.ui.data.DataViewModel;

public class MyDialogFragment extends DialogFragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_ID = "id";

    protected int mId;

    protected DataViewModel dataViewModel;

    public MyDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mId = getArguments().getInt(ARG_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_annotation, container);
    }

    //Called by child dialogs on submit to update the DB
    protected void updateDB(ContentValues cvs) {
        SQLiteDatabase db = DBHelper.getInstance(getContext()).getWritableDatabase();
        db.update("runs",cvs,"_id = ?",new String[]  { Integer.toString(mId) });
    }

    //To be overridden by inheritors
    protected DialogInterface.OnClickListener cancel = (dialogInterface, i) -> { };
    protected DialogInterface.OnClickListener submit = (dialogInterface, i) -> { };

}