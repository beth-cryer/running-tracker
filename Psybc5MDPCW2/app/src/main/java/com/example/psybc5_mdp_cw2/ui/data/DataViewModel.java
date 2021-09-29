package com.example.psybc5_mdp_cw2.ui.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

public class DataViewModel extends ViewModel {
    private SavedStateHandle state;

    private MutableLiveData<Boolean> mRefresh;
    private MutableLiveData<String> mNote, mWeather;
    private MutableLiveData<Long> mDateStart, mDateEnd;
    private MutableLiveData<String> mDirOrderBy, mFieldOrderBy;
    private MutableLiveData<Integer> mId, mRating;

    public DataViewModel(SavedStateHandle savedStateHandle) {
        state = savedStateHandle;

        mRefresh = new MutableLiveData<>();
        mRefresh.setValue(false);

        mWeather = new MutableLiveData<>();
        setWeather("");

        mNote = new MutableLiveData<>();
        setNote("");

        mFieldOrderBy = new MutableLiveData<>();
        setFieldOrderBy("dateStart");

        mDirOrderBy = new MutableLiveData<>();
        setDirOrderBy("ASC");

        mDateStart = new MutableLiveData<>();
        setDateStart(System.currentTimeMillis());

        mDateEnd = new MutableLiveData<>();
        setDateEnd(System.currentTimeMillis());

        mRating = new MutableLiveData<>();
        setRating(0);

        mId = new MutableLiveData<>();
        setId(-1);
    }

    public MutableLiveData<Boolean> getRefresh() { return mRefresh; }
    public void setRefresh(boolean refresh) { mRefresh.setValue(refresh); }

    public MutableLiveData<String> getNote() { return mNote; }
    public void setNote(String note) { mNote.setValue(note); }

    public MutableLiveData<String> getWeather() { return mWeather;}
    public void setWeather(String weather) {  mWeather.setValue(weather); }

    public MutableLiveData<Long> getDateStart() { return mDateStart; }
    public void setDateStart(long dateStart) { mDateStart.setValue(dateStart); }

    public MutableLiveData<Long> getDateEnd() { return mDateEnd; }
    public void setDateEnd(long dateEnd) { mDateEnd.setValue(dateEnd); }

    public MutableLiveData<String> getFieldOrderBy() { return mFieldOrderBy; }
    public void setFieldOrderBy(String fieldOrderBy) { mFieldOrderBy.setValue(fieldOrderBy); }

    public MutableLiveData<String> getDirOrderBy() { return mDirOrderBy; }
    public void setDirOrderBy(String dirOrderBy) { mDirOrderBy.setValue(dirOrderBy); }

    public MutableLiveData<Integer> getId() { return mId; }
    public void setId(int id) { mId.setValue(id); }

    public MutableLiveData<Integer> getRating() { return mRating; }
    public void setRating(int rating) { mRating.setValue(rating); }

}