package com.example.psybc5_mdp_cw2.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.psybc5_mdp_cw2.DBHelper;
import com.example.psybc5_mdp_cw2.R;
import com.example.psybc5_mdp_cw2.ui.data.DataViewModel;

import java.util.ArrayList;

public class StatsDialog extends DialogFragment {

    private static final String ARG_IDS = "ids";

    DataViewModel dataViewModel;
    TextView tv_fastest, tv_improvement, tv_total_distance, tv_total_runs;
    DBHelper dbHelper;
    SQLiteDatabase db;
    ArrayList<Integer> runIds;

    StatsDialog() {

    }

    public static StatsDialog newInstance(ArrayList<Integer> ids) {
        StatsDialog fragment = new StatsDialog();
        Bundle args = new Bundle();
        args.putIntegerArrayList(ARG_IDS, ids);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            runIds = getArguments().getIntegerArrayList(ARG_IDS);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_stats, null);

        dataViewModel = new ViewModelProvider(requireActivity()).get(DataViewModel.class);

        //Get all TextView objects
        tv_fastest = view.findViewById(R.id.tv_fastest);
        tv_improvement = view.findViewById(R.id.tv_improvement);
        tv_total_distance = view.findViewById(R.id.tv_total_distance);
        tv_total_runs = view.findViewById(R.id.tv_total_runs);

        //Get database object
        dbHelper = DBHelper.getInstance(getActivity().getApplicationContext());
        db = dbHelper.getWritableDatabase();

        //Retrieve id from intent extras, use for queries:
        Cursor c = queryTable();
        calcStats(c);

        //Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder.setView(view)
                .create();

    }

    //Return only the selected IDs from database
    private Cursor queryTable() {
        //Get relevant data from recipes table
        String idsQuery = "_id IN (";
        for (int s = 0; s < runIds.size(); s++) {
            idsQuery += runIds.get(s);
            if (s < runIds.size() - 1)
                idsQuery += ", "; //add a comma for all but the last item
        }
        idsQuery += ")";

        return db.query("runs", null, idsQuery, null, null, null, null);
    }

    //Calculate overall statistics from the selected runs
    private void calcStats(Cursor c) {
        double fastest = 0, slowest = 99999, t_distance = 0;
        int t_runs = 0;

        if (c.moveToFirst()) {
            do {
                long dateStart = c.getLong(c.getColumnIndex("dateStart"));

                long dateEnd = c.getLong(c.getColumnIndex("dateEnd"));
                long time = dateEnd - dateStart;
                double distance = Double.parseDouble(c.getString(c.getColumnIndex("distance")));
                double speed = distance / (time / 1000);

                if (speed > fastest) fastest = speed;
                if (speed < slowest) slowest = speed;
                t_runs++;
                t_distance += distance;

            } while (c.moveToNext());
        }
        Resources r = getResources();
        tv_fastest.setText(String.format(r.getString(R.string.stat_fastest),fastest));
        tv_improvement.setText(String.format(r.getString(R.string.stat_improv),fastest - slowest));
        tv_total_distance.setText(String.format(r.getString(R.string.stat_distance),t_distance));
        tv_total_runs.setText(String.format(r.getString(R.string.stat_runs),t_runs));
    }

}

