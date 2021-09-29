package com.example.psybc5_mdp_cw2.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.psybc5_mdp_cw2.R;
import com.example.psybc5_mdp_cw2.ui.data.DataViewModel;

public class WeatherAnnotation extends MyDialogFragment {

    private static final String ARG_WEATHER = "weather";

    private String mWeather;
    private Spinner sp;

    public WeatherAnnotation() {
        // Required empty public constructor
    }

    public static WeatherAnnotation newInstance(int id, String prevWeather) {
        WeatherAnnotation fragment = new WeatherAnnotation();
        Bundle args = new Bundle();
        args.putInt(ARG_ID, id);
        args.putString(ARG_WEATHER, prevWeather);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //super fetches the ID as well
        if (getArguments() != null) {
            mWeather = getArguments().getString(ARG_WEATHER);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dataViewModel = new ViewModelProvider(getActivity()).get(DataViewModel.class);

        sp = new Spinner(getContext());
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.array_weather, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder.setMessage("Add/Edit Weather")
                .setNegativeButton("Cancel",cancel)
                .setPositiveButton("Submit",submit)
                .setView(sp)
                .create();
    }

    protected DialogInterface.OnClickListener submit = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            ContentValues cvs = new ContentValues();
            cvs.put("weather", sp.getSelectedItem().toString());
            updateDB(cvs);
            dataViewModel.setRefresh(true);
        }
    };

}
