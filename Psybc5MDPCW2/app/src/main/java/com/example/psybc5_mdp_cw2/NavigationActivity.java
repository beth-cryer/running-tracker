package com.example.psybc5_mdp_cw2;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.psybc5_mdp_cw2.ui.data.DataViewModel;
import com.example.psybc5_mdp_cw2.ui.tracker.TrackerViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class NavigationActivity extends AppCompatActivity {

    private static final int PERM_CODE_LOCATION = 1;

    //Variables for the LocationService
    private ServiceUpdateReceiver receiver;

    //Variables for the Navigation bar
    private AppBarConfiguration mAppBarConfiguration;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawer;
    private FloatingActionButton fab;
    private NavController navController;

    private TrackerViewModel trackerViewModel;
    private DataViewModel dataViewModel;

    public static int S_DELAY, S_DISTANCE, S_STEP_THRESH;
    public static boolean S_MAP_ENABLED = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        //Request permissions
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERM_CODE_LOCATION);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //Action button onclick: Start or stop the timer
        fab.setOnClickListener(view -> trackerViewModel.setBtnClicked(true));

        //Creatr drawer toggle object
        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        {

        };

        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_tracker, R.id.nav_data, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //Set item selected listener on navigation view
        navigationView.setNavigationItemSelectedListener(item -> {
            //Check if item clicked is a valid id
            ArrayList<Integer> items = new ArrayList<>();
            items.add(R.id.nav_tracker);
            items.add(R.id.nav_data);
            items.add(R.id.nav_settings);

            int id = item.getItemId();
            if (items.contains(id)) {
                navController.navigate(id); //go to item
                drawer.closeDrawers();
            }

            //Toggle if item is currently Checked (selected)
            item.setChecked(!item.isChecked());

            drawer.closeDrawer(GravityCompat.START);

            return true;
        });

        trackerViewModel = new ViewModelProvider(this).get(TrackerViewModel.class);
        dataViewModel = new ViewModelProvider(this).get(DataViewModel.class);

        //Set up listener that handles clicks for each of the several start/stop-timer buttons in various parts of the app
        trackerViewModel.getBtnClicked().observe(this, b -> {
            if (b) {
                toggleTimer();
                trackerViewModel.setBtnClicked(false);
            } });

    }

    //Start or stop the timer service
    private void toggleTimer() {
        //Set up service and bind to this activity (if it doesn't already exist)
        if (!isServiceRunning(LocationService.class)) {
            startTimer();
        }else{
            //If service does exist, end it:
            stopTimer();
        }
    }

    //Returns true if the given service (always LocationService in this app) is currently running
    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //Start LocationService
    private void startTimer() {
        Intent locIntent = new Intent(this, LocationService.class);
        startForegroundService(locIntent);
        fab.setImageResource(R.drawable.baseline_stop_black_24dp);
    }

    //Stop LocationService
    private void stopTimer() {
        //Set action for service intent telling it to stop
        //(Have to do this as service is in foreground and not bound)
        Intent locIntent = new Intent(this, LocationService.class);
        locIntent.setAction(LocationService.ACTION_CLOSE);
        stopService(locIntent);

        fab.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        dataViewModel.setRefresh(true); //refresh recyclelist
    }

    @Override
    public void onBackPressed() {
        //Makes sure the Activity isn't closed when Back is pressed, while Navbar is open: just the Navbar should close instead
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the navbar layout when opened
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        toggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    //Save data to SharedPreferences when activity is suspended or stopped
    protected void onPause() {
        saveSharedPreferences();
        super.onPause();
    }

    protected void onStop() {
        saveSharedPreferences();
        super.onStop();
    }

    protected void onResume() {
        //Restore settings data from SharedPreferences (use defaults if none saved)
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        dataViewModel.setDirOrderBy(sharedPreferences.getString("dirOrderBy","DESC"));
        dataViewModel.setFieldOrderBy(sharedPreferences.getString("fieldOrderBy","dateStart"));
        dataViewModel.setDateStart(sharedPreferences.getLong("dateStart",System.currentTimeMillis()));
        dataViewModel.setDateEnd(sharedPreferences.getLong("dateEnd",System.currentTimeMillis()));
        S_DELAY = sharedPreferences.getInt("delay",5);
        S_DISTANCE = sharedPreferences.getInt("distance",5);
        S_STEP_THRESH = sharedPreferences.getInt("step",4);
        S_MAP_ENABLED = sharedPreferences.getBoolean("map",true);

        //Set up broadcast receiver for the service to use
        if (receiver == null) receiver = new ServiceUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationService.ACTION_RUN_DONE);
        intentFilter.addAction(LocationService.ACTION_REFRESH_UI);
        registerReceiver(receiver, intentFilter);

        super.onResume();
    }

    //Saves a small amount of data to a local file
    public void saveSharedPreferences() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putString("fieldOrderBy",dataViewModel.getFieldOrderBy().getValue());
        editor.putString("dirOrderBy", dataViewModel.getDirOrderBy().getValue());
        editor.putLong("dateStart",dataViewModel.getDateStart().getValue());
        editor.putLong("dateEnd",dataViewModel.getDateEnd().getValue());
        editor.putInt("delay",S_DELAY);
        editor.putInt("distance",S_DISTANCE);
        editor.putInt("step",S_STEP_THRESH);
        editor.putBoolean("map",S_MAP_ENABLED);
        editor.apply();
    }

    //Request a Permission, if it has not already been granted
    public void checkPermission(String permission, int requestCode)  {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[] { permission }, requestCode);
    }

    //Receiver for broadcasts from the LocationService
    private class ServiceUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case(LocationService.ACTION_REFRESH_UI):
                    //Immediately update the UI fragment ViewModel when the Service tells it to
                    Bundle ex = intent.getExtras();
                    trackerViewModel.setSteps(ex.getInt("steps"));
                    trackerViewModel.setTimer(ex.getLong("time"));
                    trackerViewModel.setLat(ex.getDouble("lat"));
                    trackerViewModel.setLon(ex.getDouble("lon"));
                    trackerViewModel.setDistance(ex.getDouble("distance"));
                    break;
                case (LocationService.ACTION_RUN_DONE):
                    //If opening after destruction of LocationService, then navigate immediately to Data fragment to show results
                    navController.navigate(R.id.nav_data);
                    drawer.closeDrawers();
                    break;
            }
        }
    }

}