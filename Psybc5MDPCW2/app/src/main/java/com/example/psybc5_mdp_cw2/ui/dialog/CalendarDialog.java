package com.example.psybc5_mdp_cw2.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import android.widget.CalendarView;

import com.example.psybc5_mdp_cw2.ui.data.DataViewModel;

import java.util.Calendar;

public class CalendarDialog extends MyDialogFragment {

    protected static final String ARG_CALENDAR = "calendar";

    protected String mCalendar;
    protected CalendarView cal;
    Calendar calendar;

    long date;

    private final String returnField;

    public CalendarDialog(String returnField) {
        this.returnField = returnField;
    }

    public static CalendarDialog newInstance(String prevDate, String returnField) {
        CalendarDialog fragment = new CalendarDialog(returnField);
        Bundle args = new Bundle();
        args.putString(ARG_CALENDAR, prevDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //super fetches the ID as well
        if (getArguments() != null) {
            mCalendar = getArguments().getString(ARG_CALENDAR);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dataViewModel = new ViewModelProvider(getActivity()).get(DataViewModel.class);

        cal = new CalendarView(getContext());

        long defaultTime = 0;
        switch(returnField) {
            case("start"): defaultTime = dataViewModel.getDateStart().getValue(); break;
            case("end"): defaultTime = dataViewModel.getDateEnd().getValue(); break;
        }
        cal.setDate(defaultTime);

        calendar = Calendar.getInstance();
        date = defaultTime; //set return date to default until changed

        //When a new date is picked, update return date
        cal.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            date = calendar.getTimeInMillis();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder.setNegativeButton("Cancel",cancel)
                .setPositiveButton("Submit",submit)
                .setView(cal)
                .create();
    }

    protected DialogInterface.OnClickListener submit = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            switch(returnField) {
                case("start"): dataViewModel.getDateStart().setValue(date); break;
                case("end"): dataViewModel.getDateEnd().setValue(date); break;
            }

        }
    };

}