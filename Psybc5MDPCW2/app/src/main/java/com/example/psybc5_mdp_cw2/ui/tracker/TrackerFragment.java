package com.example.psybc5_mdp_cw2.ui.tracker;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.psybc5_mdp_cw2.LocationService;
import com.example.psybc5_mdp_cw2.NavigationActivity;
import com.example.psybc5_mdp_cw2.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class TrackerFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback {

    TrackerViewModel trackerViewModel;

    View map;
    GoogleMap gmap;
    Marker marker;

    protected TextView tvSteps, tvTimer, tvLat, tvLon, tvDistance;
    protected Button bStartTimer;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tracker, container, false);

        trackerViewModel = new ViewModelProvider(requireActivity()).get(TrackerViewModel.class);

        //Get layout view ids
        tvSteps = root.findViewById(R.id.tv_steps);
        tvTimer = root.findViewById(R.id.tv_timer);
        tvLat = root.findViewById(R.id.tv_lat);
        tvLon = root.findViewById(R.id.tv_lon);
        tvDistance = root.findViewById(R.id.tv_distanceTracked);

        bStartTimer = root.findViewById(R.id.button_timer);
        bStartTimer.setOnClickListener(this);

        Resources r = getResources();

        if (NavigationActivity.S_MAP_ENABLED) {
            SupportMapFragment map = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
            map.getMapAsync(this);
        }
        map = root.findViewById(R.id.map);


        //OBSERVERS
        trackerViewModel.getLat().observe(getViewLifecycleOwner(), f -> {
            tvLat.setText(String.format(r.getString(R.string.latitude),f));
            updateMap();
        });
        trackerViewModel.getSteps().observe(getViewLifecycleOwner(), s -> tvSteps.setText(String.format(r.getString(R.string.stepstaken),s)));
        trackerViewModel.getTimer().observe(getViewLifecycleOwner(), s -> tvTimer.setText(String.format(r.getString(R.string.timetaken),s)));
        trackerViewModel.getLon().observe(getViewLifecycleOwner(), f -> tvLon.setText(String.format(r.getString(R.string.longitude),f)));
        trackerViewModel.getDistance().observe(getViewLifecycleOwner(), f -> tvDistance.setText(String.format(r.getString(R.string.distance),f)));
        trackerViewModel.getBtnClicked().observe(getViewLifecycleOwner(), b -> refreshUI());

        return root;
    }

    private void refreshUI() {
        if (!NavigationActivity.S_MAP_ENABLED) map.setVisibility(View.INVISIBLE);

        //Toggle map visibility and button text
        NavigationActivity act = (NavigationActivity) getActivity();
        if (act.isServiceRunning(LocationService.class)) {
            if (NavigationActivity.S_MAP_ENABLED) map.setVisibility(View.VISIBLE);
            bStartTimer.setText(R.string.button_runend);
        }else{
            map.setVisibility(View.INVISIBLE);
            bStartTimer.setText(R.string.button_runstart);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //Refresh UI (eg. for if the user gets here using Back button but location is no longer running)
        refreshUI();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void onClick(View v) {
        //Update main Activity by changing LiveData in the ViewModel
        trackerViewModel.setBtnClicked(true);
    }

    //Centers map on the camera and places a marker there
    private void updateMap() {
        if (gmap != null) {
            //Get current location, update marker and camera positions
            LatLng pos = new LatLng(trackerViewModel.getLat().getValue(),trackerViewModel.getLon().getValue());
            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos,16));
            marker.setPosition(pos); //update marker position
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap; //store map reference

        //Create marker, add it to map and center camera on it
        LatLng pos = new LatLng(trackerViewModel.getLat().getValue(),trackerViewModel.getLon().getValue());
        MarkerOptions mpos = new MarkerOptions().position(pos);
        marker = gmap.addMarker(mpos);
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos,16));
    }
}