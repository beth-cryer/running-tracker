package com.example.psybc5_mdp_cw2.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.psybc5_mdp_cw2.ui.data.DataViewModel;

public class NoteAnnotation extends MyDialogFragment {

    private static final String ARG_NOTE = "note";

    private String mNote;
    private EditText et;

    public NoteAnnotation() {
        // Required empty public constructor
    }

    public static NoteAnnotation newInstance(int id, String prevNote) {
        NoteAnnotation fragment = new NoteAnnotation();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putString(ARG_NOTE, prevNote);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //super fetches the ID as well
        if (getArguments() != null) {
            mNote = getArguments().getString(ARG_NOTE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dataViewModel = new ViewModelProvider(getActivity()).get(DataViewModel.class);
        et = new EditText(getContext());
        et.setText(mNote);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder.setMessage("Add/Edit Note")
                .setNegativeButton("Cancel",cancel)
                .setPositiveButton("Submit",submit)
                .setView(et)
                .create();
    }

    protected DialogInterface.OnClickListener submit = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            ContentValues cvs = new ContentValues();
            cvs.put("note", et.getText().toString());
            updateDB(cvs);
            dataViewModel.setRefresh(true);
        }
    };

}
