package com.example.psybc5_mdp_cw2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Locale;

public class LocationService extends Service {

    private final String CHANNEL = "LocationServiceChannel";
    private final int NOT_ID = 2;

    public static final String ACTION_CLOSE = "CLOSE";
    public static final String ACTION_PAUSE = "PAUSE";
    public static final String ACTION_REFRESH_UI = "REFRESH";
    public static final String ACTION_RUN_DONE = "DONE";

    private final IBinder ibind = new LocationBinder();

    private Handler timer;
    private Runnable timerTask;
    long seconds = -1;
    boolean timerPaused = false;

    public double lat, lon;
    public double speed;
    public double distanceTotal = 0;

    double mPrevious = 0;
    public int steps = 0;

    long prevTime = System.currentTimeMillis();
    Location prevLoc;

    long dateStart = 0;

    LocationManager locationManager;
    MyLocationListener locationListener;
    NotificationManager notManager;
    NotificationCompat.Builder notBuilder;
    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener stepDetector;

    public LocationService() {

    }

    //BINDER SUBCLASS (empty as we don't bind this)
    public class LocationBinder extends Binder {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return ibind;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //GPS
        //Create LocationManager object and a corresponding listener
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    NavigationActivity.S_DELAY, // minimum time interval between updates
                    NavigationActivity.S_DISTANCE, // minimum distance between updates, in metres
                    locationListener);
        } catch (SecurityException e) {
            Log.d("g53mdp", e.toString());
        }

        //ACCELEROMETER
        //Register sensor in a SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Create corresponding listener
        stepDetector = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent != null) {
                    float xAccel = sensorEvent.values[0];
                    float yAccel = sensorEvent.values[1];
                    float zAccel = sensorEvent.values[2];

                    //Calculate the movement magnitude and the change in magnitude since last sensor update
                    double m = Math.sqrt(xAccel * xAccel + yAccel * yAccel + zAccel * zAccel);
                    double mChange = m - mPrevious;

                    //Don't have time to add DetectedActivity stuff for checking if we're walking or running, just using a constant here sadly :(
                    if (mChange > NavigationActivity.S_STEP_THRESH) steps++;

                    //Update mPrevious for next onSensorChanged event
                    mPrevious = m;
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) { }
        };

        sensorManager.registerListener(stepDetector, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean createService = true;

        //Handle any action provided:
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                createService = false;
                switch (action) {
                    case (ACTION_CLOSE):
                        //Stop self
                        kill();
                        //Return to NavigationActivity and open DataFragment:
                        Intent navIntent = new Intent(this, NavigationActivity.class);
                        navIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP); //clear everything and become new root of task
                        startActivity(navIntent);
                        broadcastEnd();
                        break;
                    case (ACTION_PAUSE):
                        //Toggle timer between pause/unpause
                        timerPaused = !timerPaused;
                        if (timerPaused) timer.removeCallbacks(timerTask);
                        else startTimer();

                        //Update Notification button (have to recreate notification from scratch)
                        setNotificationContent();
                        Notification not = notBuilder.build();
                        notManager.notify(NOT_ID,not);

                        break;
                }
            }
        }

        //Only create this service if necessary
        if (createService) {

            //Create NotificationChannel and add it to the NotificationManager
            notManager = getSystemService(NotificationManager.class);
            NotificationChannel locChannel = new NotificationChannel(CHANNEL, "Location Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notManager.createNotificationChannel(locChannel);

            setNotificationContent();

            Notification not = notBuilder.build();
            startForeground(NOT_ID, not);

            dateStart = System.currentTimeMillis();

            startTimer();

        }

        return Service.START_NOT_STICKY;
    }

    //Creates a new Notification
    private void setNotificationContent() {
        //Return to MainActivity on notification click
        Intent notIntent = new Intent(this, NavigationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notIntent, 0);

        //End service on X click. End the Run and store results first, though.
        final Intent notIntent_close = new Intent(this, LocationService.class);
        notIntent_close.setAction(ACTION_CLOSE);
        PendingIntent closeIntent = PendingIntent.getService(this, 0, notIntent_close, PendingIntent.FLAG_CANCEL_CURRENT);

        //Pause timer and stop recording data until pressed again.
        final Intent notIntent_pause = new Intent(this, LocationService.class);
        notIntent_pause.setAction(ACTION_PAUSE);
        PendingIntent pauseIntent = PendingIntent.getService(this, 0, notIntent_pause, PendingIntent.FLAG_UPDATE_CURRENT);

        //Determine whether pause icon displays Pause or Unpause
        int pauseIcon;
        String pauseText;
        if (timerPaused) {
            pauseIcon = R.drawable.ic_play_arrow_black_24dp;
            pauseText = "Resume";
        } else {
            pauseIcon = R.drawable.ic_pause_black_24dp;
            pauseText = "Pause";
        }

        //Sets default attributes of the notification
        notBuilder = new NotificationCompat.Builder(this, CHANNEL)
                .setContentTitle("Running App")
                .setContentText("Time: " + seconds)
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(new NotificationCompat.Action(pauseIcon, pauseText, pauseIntent))
                .addAction(new NotificationCompat.Action(R.drawable.ic_clear_black_24dp, "Close", closeIntent))
                .setContentIntent(pendingIntent);
    }

    private void updateNotificationContent() {
        notBuilder.setContentTitle("Running App");
        notBuilder.setContentText("Time: " + seconds);
    }

    private void startTimer() {
        //Set up Handler that updates UI regularly
        timer = new Handler();
        timerTask = () -> {
            //Get time between now and the start time to calculate elapsed seconds
            long now = System.currentTimeMillis();
            long timeElapsed = now - dateStart;
            seconds = timeElapsed / 1000;

            //Update notification and UI
            updateNotificationContent();
            notManager.notify(NOT_ID,notBuilder.build());
            broadcastUI();
            timer.postDelayed(timerTask, 1000); //1000 ms = 1 sec
        };
        timerTask.run();
    }

    private void broadcastUI() {
        Intent intent = new Intent(ACTION_REFRESH_UI);
        Bundle b = new Bundle();
        b.putInt("steps",steps);
        b.putLong("time",seconds);
        b.putDouble("lat",lat);
        b.putDouble("lon",lon);
        b.putDouble("distance",distanceTotal);

        intent.putExtras(b);
        sendBroadcast(intent); //tell activity to update its UI too
    }

    private void broadcastEnd() {
        Intent intent = new Intent(ACTION_RUN_DONE);
        sendBroadcast(intent);
    }

    //When the service end: upload this run's info to the SQL database and stopSelf()
    public void kill() {
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        //Make sure we save the results of this run here.
        //this will also run if the service ends prematurely for whatever reason.

        //Save current data to the SQL database
        DBHelper dbHelper = DBHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues recValues = new ContentValues();
        recValues.put("dateStart", dateStart);
        recValues.put("dateEnd", System.currentTimeMillis());
        recValues.put("distance", String.format(Locale.getDefault(),"%.2f",distanceTotal));
        db.insert("runs", null, recValues);

        //Reset values and update the UI viewmodel
        lat = 0; lon = 0; distanceTotal = 0; steps = 0; seconds = 0;
        broadcastUI();

        prevLoc = null;

        //Need to delete notification and stop any attached process currently running on a thread (eg. timer)
        if (notManager != null) notManager.cancel(NOT_ID);
        timer.removeCallbacks(timerTask);
        sensorManager.unregisterListener(stepDetector,sensor);

        super.onDestroy();
    }

    //LOCATION LISTENER, tracks gps movement over time
    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            Log.d("g53mdp", location.getLatitude() + " " + location.getLongitude());

            //Get distance travelled since last location update:
            double distance = 0;
            if (prevLoc != null) {
                distance = location.distanceTo(prevLoc);
            }

            //Update total distance
            distanceTotal += distance;

            //Update prevLoc to current coordinates
            prevLoc = new Location("");
            prevLoc.setLatitude(lat);
            prevLoc.setLongitude(lon);

            /*
            //Get time elapsed since last location update:
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - prevTime;
            prevTime = currentTime;

            //Calculate current speed from this (use average of last 10 updates), speed = distance / time
            speed = distance / (elapsedTime / 1000);
             */

        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // information about the signal, i.e. number of satellites
            Log.d("g53mdp", "onStatusChanged: " + provider + " " + status);
        }
        @Override
        public void onProviderEnabled(String provider) {
            // the user enabled (for example) the GPS
            prevTime = System.currentTimeMillis();      //initialise prevTime to now
            Log.d("g53mdp", "onProviderEnabled: " + provider);
        }
        @Override
        public void onProviderDisabled(String provider) {
            // the user disabled (for example) the GPS
            Log.d("g53mdp", "onProviderDisabled: " + provider);
            //if this happens the service stops working lol. oh well
        }
    }

}