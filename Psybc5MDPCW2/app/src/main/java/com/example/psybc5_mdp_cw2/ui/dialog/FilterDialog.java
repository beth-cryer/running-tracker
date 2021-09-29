package com.example.psybc5_mdp_cw2.ui.dialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.example.psybc5_mdp_cw2.R;
import com.example.psybc5_mdp_cw2.ui.data.DataViewModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class FilterDialog extends MyDialogFragment {

    Spinner spinner_orderBy;
    Spinner spinner_filter;
    ToggleButton toggle_dir;

    FilterDialog() {

    }

    public static FilterDialog newInstance() {
        return new FilterDialog();
    }

    //Converts between the dropdown Spinner strings in UI and the actual DB fields they refer to
    private String convert(String s) {
        HashMap<String,String> map = new HashMap<>();
        map.put("Date","dateStart");
        map.put("Duration","dateEnd - dateStart");
        map.put("Distance","cast (distance as decimal(18,2))");

        //Get corresponding value from key, or key from value (pairs are unique here)
        String val = map.get(s);
        if (val == null) {
            for (Map.Entry<String,String> entry : map.entrySet()) {
                if (Objects.equals(s, entry.getValue())) {
                    val = entry.getKey();
                }
            }
        }
        return val;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_filter, null);

        spinner_orderBy = view.findViewById(R.id.spinner_orderBy);
        spinner_filter = view.findViewById(R.id.spinner_filter);
        toggle_dir = view.findViewById(R.id.toggleButton_dir);

        dataViewModel = new ViewModelProvider(getActivity()).get(DataViewModel.class);

        //Get current values from the viewmodel
        String dirOrderBy = dataViewModel.getDirOrderBy().getValue();
        String fieldOrderBy = convert(dataViewModel.getFieldOrderBy().getValue());

        //Set Spinner dropdown adapter using string-array resource
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.array_orderBy, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_orderBy.setAdapter(adapter);
        spinner_filter.setAdapter(adapter);

        //Set dropdown to default value
        int index = 0;
        for (int i = 0; i < spinner_orderBy.getCount(); i++) {
            if (spinner_orderBy.getItemAtPosition(i).equals(fieldOrderBy)) { index = i; break; }
        }
        spinner_orderBy.setSelection(index);

        //Set checkbox to default value
        toggle_dir.setChecked(!dirOrderBy.equals("ASC"));

        //Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder.setNegativeButton("Cancel",cancel)
                .setPositiveButton("Submit",submit)
                .setView(view)
                .create();
    }

    protected DialogInterface.OnClickListener submit = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            String dir = "ASC";
            if (toggle_dir.getText().equals(getString(R.string.desc))) dir = "DESC";

            String fieldOrderBy = spinner_orderBy.getSelectedItem().toString();

            dataViewModel.setDirOrderBy(dir);
            dataViewModel.setFieldOrderBy(convert(fieldOrderBy));
            dataViewModel.setRefresh(true);
        }
    };
}