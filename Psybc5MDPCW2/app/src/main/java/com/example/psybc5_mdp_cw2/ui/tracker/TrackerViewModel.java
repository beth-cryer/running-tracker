package com.example.psybc5_mdp_cw2.ui.tracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TrackerViewModel extends ViewModel {

    private MutableLiveData<Integer> mSteps;
    private MutableLiveData<Long> mTimer;
    private MutableLiveData<Double> mLat, mLon, mDistance;
    private MutableLiveData<Boolean> btnClicked;

    public MutableLiveData<Integer> getSteps() { return mSteps; }
    public void setSteps(int steps) { mSteps.setValue(steps); }

    public MutableLiveData<Long> getTimer() { return mTimer; }
    public void setTimer(long timer) { mTimer.setValue(timer);}

    public MutableLiveData<Double> getLat() { return mLat; }
    public void setLat(double lat) { mLat.setValue(lat); }

    public MutableLiveData<Double> getLon() { return mLon; }
    public void setLon(double lon) { mLon.setValue(lon); }

    public MutableLiveData<Double> getDistance() { return mDistance; }
    public void setDistance(double distance) { mDistance.setValue(distance); }

    public MutableLiveData<Boolean> getBtnClicked() { return btnClicked; }
    public void setBtnClicked(boolean clicked) { btnClicked.setValue(clicked); }

    public TrackerViewModel() {
        btnClicked = new MutableLiveData<>();
        setBtnClicked(false);

        mSteps = new MutableLiveData<>();
        setSteps(0);

        mTimer = new MutableLiveData<>();
        setTimer(0);

        mLat = new MutableLiveData<>();
        setLat(0.0);

        mLon = new MutableLiveData<>();
        setLon(0.0);

        mDistance = new MutableLiveData<>();
        setDistance(0.0);

        //mSpeed = new MutableLiveData<>();
        //mSpeed.setValue("Speed: ");
    }

}