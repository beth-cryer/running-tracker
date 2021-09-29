package com.example.psybc5_mdp_cw2.ui.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.psybc5_mdp_cw2.DBHelper;
import com.example.psybc5_mdp_cw2.R;
import com.example.psybc5_mdp_cw2.ui.dialog.StatsDialog;
import com.example.psybc5_mdp_cw2.ui.dialog.CalendarDialog;
import com.example.psybc5_mdp_cw2.ui.dialog.FilterDialog;
import com.example.psybc5_mdp_cw2.ui.dialog.NoteAnnotation;
import com.example.psybc5_mdp_cw2.ui.dialog.WeatherAnnotation;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DataFragment extends Fragment {

    private static final String ARG_SELECTED = "selectedItems";
    private DataViewModel dataViewModel;

    public SQLiteDatabase db;
    RecyclerViewAdapter adapterTable;
    Cursor cursor;

    Button btnCompare, btnDeselect;
    ImageButton btnFilter, btnSelect1, btnSelect2;
    Spinner spinner_select;

    TextView tv_dateStart, tv_dateEnd;

    ArrayList<Integer> selectedItems;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Get viewmodel, with NavigationActivity as owner
        dataViewModel = new ViewModelProvider(requireActivity()).get(DataViewModel.class);

        View root = inflater.inflate(R.layout.fragment_data, container, false);

        tv_dateStart = root.findViewById(R.id.tv_selDateStart);
        tv_dateEnd = root.findViewById(R.id.tv_selDateEnd);
        spinner_select = root.findViewById(R.id.spinner_select);

        btnCompare = root.findViewById(R.id.button_compare);
        btnDeselect = root.findViewById(R.id.button_deselect);
        btnFilter = root.findViewById(R.id.button_orderby);
        btnSelect1 = root.findViewById(R.id.btnSelectDates);
        btnSelect2 = root.findViewById(R.id.btnSelectDate2);

        btnFilter.setOnClickListener(filterClicked);
        btnCompare.setOnClickListener(compareClicked);
        btnDeselect.setOnClickListener(deselectClicked);
        btnSelect1.setOnClickListener(select1);
        btnSelect2.setOnClickListener(select2);


        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            selectedItems = savedInstanceState.getIntegerArrayList(ARG_SELECTED);
        }else{
            selectedItems = new ArrayList<>();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList(ARG_SELECTED,selectedItems);
    }

    @Override
    public void onResume() {
        super.onResume();

        //Observe start and end dates for selection criteria, update text upon value change
        dataViewModel.getDateStart().observe(getViewLifecycleOwner(), aLong -> tv_dateStart.setText(longToDate(aLong)));
        dataViewModel.getDateEnd().observe(getViewLifecycleOwner(), aLong -> tv_dateEnd.setText(longToDate(aLong)));

        //selectedItems contains a list of ids of all the selected rows in the table.
        //These will be placed at the top of the SQL selection query

        DBHelper dbHelper = DBHelper.getInstance(getContext());
        db = dbHelper.getWritableDatabase();
        cursor = queryTable();

        RecyclerView recyclerView = getActivity().findViewById(R.id.recyclerView_table);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapterTable = new RecyclerViewAdapter(getContext(), cursor,this);
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.setAdapter(adapterTable);

        //Set OnClicks for the two date-setter dialogs
        tv_dateStart.setOnClickListener(view -> {
            CalendarDialog dialog = CalendarDialog.newInstance(tv_dateStart.getText().toString(),"start");
            dialog.show(getActivity().getSupportFragmentManager(),"calendar");
        });

        tv_dateEnd.setOnClickListener(view -> {
            CalendarDialog dialog = CalendarDialog.newInstance(tv_dateStart.getText().toString(),"end");
            dialog.show(getActivity().getSupportFragmentManager(),"calendar");
        });

        //Refresh the RecyclerView if another fragment sets mRefresh to true
        dataViewModel.getRefresh().observe(getViewLifecycleOwner(), b -> {
            if (b) {
                updateRecyclerView();
                dataViewModel.setRefresh(false);
            }
        });

        //Link new adapter to string array
        ArrayAdapter<CharSequence> selectAdapter = ArrayAdapter.createFromResource(getContext(), R.array.array_select, android.R.layout.simple_spinner_item);
        selectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_select.setAdapter(selectAdapter);

    }

    //Reusable function for querying with parameters
    private Cursor queryTable() {
        String orderBy;
        String order = "LOWER (" + dataViewModel.getFieldOrderBy().getValue() + ") " + dataViewModel.getDirOrderBy().getValue();
        Log.d("g53mdp",order);

        //Do a normal query if there are no selections
        if (selectedItems.isEmpty()) {
            orderBy = order;
        }else{
            //Otherwise prioritise selections in ordering
            orderBy = "_id IN (";
            for (int s = 0; s < selectedItems.size(); s++) {
                orderBy += selectedItems.get(s);
                if (s < selectedItems.size() - 1)
                    orderBy += ", "; //add a comma for all but the last item
            }
            orderBy += ") DESC, " + order;
        }

        //Query database for list of Runs
        return db.query("runs", null, null, null, null, null, orderBy);
    }

    //Do a query then update RecyclerView with the new results
    public void updateRecyclerView() {
        setButtonsVisibility();

        Cursor c = queryTable();
        adapterTable.setCursor(c);
        adapterTable.notifyDataSetChanged();
        Log.d("g53mdp","Table updated");
    }

    //Set button visibility (for view/compare and deselect buttons) depending on if items are selected
    private void setButtonsVisibility() {
        if (selectedItems.isEmpty()) {
            btnCompare.setVisibility(View.GONE);
            btnDeselect.setVisibility(View.GONE);
        }else{
            btnCompare.setVisibility(View.VISIBLE);
            btnDeselect.setVisibility(View.VISIBLE);
        }
    }


    //Helper functions

    public static String longToDate(long l) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        return sdf.format(new Date(l));
    }

    public static String longToDateTime(long l) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.UK);
        return sdf.format(new Date(l));
    }

    public static String longsToDuration(long sl, long el) {
        long d = el - sl;
        return String.format(Locale.UK, "%02d min, %02d sec", TimeUnit.MILLISECONDS.toMinutes(d), TimeUnit.MILLISECONDS.toSeconds(d) % 60);
    }


    //Button onClickListeners:

    View.OnClickListener filterClicked = view -> {
        FilterDialog dialog = FilterDialog.newInstance();
        dialog.show(getActivity().getSupportFragmentManager(),"filter");
    };

    View.OnClickListener compareClicked = view -> {
        StatsDialog dialog = StatsDialog.newInstance(selectedItems);
        dialog.show(getActivity().getSupportFragmentManager(),"stats");
    };

    View.OnClickListener deselectClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            selectedItems.clear();
            updateRecyclerView();
        }
    };

    View.OnClickListener select1 = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //Clear currently-selected items
            selectedItems.clear();

            long now = System.currentTimeMillis();
            long start = dataViewModel.getDateStart().getValue();
            long end = dataViewModel.getDateEnd().getValue();

            if (cursor.moveToFirst()) {
                do {
                    long dateStart = cursor.getLong(cursor.getColumnIndex("dateStart"));
                    //If in selected range: Add this _id to selectedItems
                    boolean valid = dateStart >= start && dateStart <= end;
                    if (valid) selectedItems.add(cursor.getInt(cursor.getColumnIndex("_id")));

                } while(cursor.moveToNext());
            }
            //Update view after selection(s) complete
            updateRecyclerView();
            }
    };

    View.OnClickListener select2 = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //Get new selection parameter
            String select = spinner_select.getSelectedItem().toString();

            //Clear currently-selected items
            selectedItems.clear();

            long year_ms = TimeUnit.DAYS.toMillis(365);
            long month_ms = TimeUnit.DAYS.toMillis(30);
            long week_ms = TimeUnit.DAYS.toMillis(7);
            long day_ms = TimeUnit.HOURS.toMillis(24);
            long now = System.currentTimeMillis();

            if (cursor.moveToFirst()) {
                do {
                    long dateStart = cursor.getLong(cursor.getColumnIndex("dateStart"));
                    long selectedTime = 0;
                    //Select items
                    switch (select) {
                        case ("Today"): selectedTime = day_ms; break;
                        case ("7 Days"): selectedTime = week_ms; break;
                        case ("30 Days"): selectedTime = month_ms; break;
                        case ("365 Days"): selectedTime = year_ms;break;
                    }
                    //If in selected range: Add this _id to selectedItems
                    boolean valid = now - dateStart <= selectedTime;
                    if (valid) selectedItems.add(cursor.getInt(cursor.getColumnIndex("_id")));

                } while (cursor.moveToNext());
            }
            //Update view after selection(s) complete
            updateRecyclerView();
        }
    };


}


//Adapter class for the RecyclerView table

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private final Context context;
    private final LayoutInflater inflater;
    Cursor cursor;

    DataFragment parent;

    RecyclerViewAdapter(Context context, Cursor cursor, DataFragment parent) {
        this.inflater = LayoutInflater.from(context);
        this.cursor = cursor;
        this.context = context;
        this.parent = parent;
    }

    public void setCursor (Cursor cursor) {
        this.cursor = cursor;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.activity_browse, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, int position) {

        //Populate viewholder UI with fields from the row at table[position]
        if (cursor.moveToPosition(position)) {
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            holder._id = id;

            long dateStart = cursor.getLong(cursor.getColumnIndex("dateStart"));
            holder.dateStart.setText(DataFragment.longToDateTime(dateStart));

            long dateEnd = cursor.getLong(cursor.getColumnIndex("dateEnd"));
            holder.duration.setText(DataFragment.longsToDuration(dateStart,dateEnd));

            double distance = cursor.getDouble(cursor.getColumnIndex("distance"));
            holder.distance.setText(String.format(Locale.getDefault(),"%.2f",distance));

            //Populate optional annotation fields. Set them to blank if they are null in the DB
            int r = cursor.getColumnIndex("rating");
            int rating = 0;
            if (!cursor.isNull(r)) rating = cursor.getInt(r);
            holder.rating.setRating(rating);

            int n = cursor.getColumnIndex("note");
            String note = "";
            if (!cursor.isNull(n)) note = cursor.getString(n);
            holder.note.setText(note);

            int w = cursor.getColumnIndex("weather");
            String weather = "";
            if (!cursor.isNull(w)) weather = cursor.getString(w);
            holder.weather.setText(weather);

            //Highlight this row if it is selected
            TableRow row = holder.itemView.findViewById(R.id.table_row);
            if (parent.selectedItems.contains(id)) row.setBackgroundColor(context.getColor(R.color.purple_200));
            else row.setBackgroundColor(Color.TRANSPARENT);

        }else{
            //If database is empty, set everything to blank
            holder.dateStart.setText("");
            holder.duration.setText("");
            holder.distance.setText("");
            holder.rating.setRating(0);
            holder.note.setText("");
            holder.weather.setText("");
        }
    }

    @Override
    public int getItemCount() { return cursor.getCount(); }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public int _id;
        TextView dateStart, duration, distance, note, weather;
        RatingBar rating;

        ViewHolder(View itemView) {
            super(itemView);

            dateStart = (TextView) itemView.findViewById(R.id.tv_dateStart);
            duration = (TextView) itemView.findViewById(R.id.tv_duration);
            distance = (TextView) itemView.findViewById(R.id.tv_distance);
            rating = itemView.findViewById(R.id.ratingBar);
            note = itemView.findViewById(R.id.tv_note);
            weather = itemView.findViewById(R.id.tv_weather);

            //Open Annotation edit-dialogs onClick:

            LinearLayout noteField = itemView.findViewById(R.id.field_note);
            noteField.setOnClickListener(view -> {
                Log.d("g53mdp","Clicked note");
                NoteAnnotation dialog = NoteAnnotation.newInstance(_id, note.getText().toString());
                dialog.show(parent.getActivity().getSupportFragmentManager(),"note");
            });

            LinearLayout weatherField = itemView.findViewById(R.id.field_weather);
            weatherField.setOnClickListener(view -> {
                Log.d("g53mdp","Clicked weather");
                WeatherAnnotation dialog = WeatherAnnotation.newInstance(_id, weather.getText().toString());
                dialog.show(parent.getActivity().getSupportFragmentManager(),"weather");
            });

            //Update Rating in the database when the user changes the RatingBar value
            rating.setOnRatingBarChangeListener((ratingBar, v, b) -> {
                ContentValues cvs = new ContentValues();
                cvs.put("rating",v);
                parent.db.update("runs",cvs,"_id = ?",new String[]  { Integer.toString(_id) });
            });

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //Add or remove from Selected list
            if (parent.selectedItems.contains(_id))
                parent.selectedItems.remove(Integer.valueOf(_id));
            else parent.selectedItems.add(_id);

            //Update recyclerview and button visibility
            parent.updateRecyclerView();
        }
    }

}