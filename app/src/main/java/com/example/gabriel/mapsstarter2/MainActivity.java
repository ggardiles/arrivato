package com.example.gabriel.mapsstarter2;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, OnDataListener{

    private static final String LOG_TAG = "MAIN_ACTIVITY";
    private TabLayout tabLayout;

    protected HashSet<String> selectedUsernames;
    protected LatLng origin, destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //PathFragment gMapFragment = new PathFragment();
        UserSelectFragment gMapFragment = new UserSelectFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace fragment and add to back stack
        transaction.add(R.id.fragmentWrap, gMapFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

        // Set listeners on tabs
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(this);
    }


    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        Log.d(LOG_TAG, String.valueOf(tab.getPosition()));
        FragmentTransaction transaction = getFragmentManager().beginTransaction();


        if (tab.getPosition() == 0){
            UserSelectFragment fragment = new UserSelectFragment();
            transaction.replace(R.id.fragmentWrap, fragment);
        } else if(tab.getPosition() == 1){
            TrackArrivalFragment fragment = new TrackArrivalFragment();
            transaction.replace(R.id.fragmentWrap, fragment);
        }

        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        Log.d(LOG_TAG, "onTabUnselected");
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        Log.d(LOG_TAG, "onTabReselected");
    }

    @Override
    public void onLocationReady(LatLng origin, LatLng destination) {
        Log.d(LOG_TAG, "onLocationReady: " + String.valueOf(origin) + ", " + destination.toString());
        this.origin = origin;
        this.destination = destination;
    }

    @Override
    public void onUsernameReady(HashSet<String> usernames) {
        Log.d(LOG_TAG, "onUsernameReady: " + String.valueOf(selectedUsernames));
        this.selectedUsernames = usernames;
    }

    @Override
    public void getConfirmationData() {
        Log.d(LOG_TAG, "getConfirmationData: " + selectedUsernames.toString());
        ConfirmationFragment frag = (ConfirmationFragment)
                getFragmentManager().findFragmentById(R.id.fragmentWrap);

        if (frag != null){
            frag.onConfirmationData(origin, destination, selectedUsernames);
        }
    }

}


