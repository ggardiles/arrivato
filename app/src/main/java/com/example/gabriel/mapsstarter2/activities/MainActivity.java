package com.example.gabriel.mapsstarter2.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.gabriel.mapsstarter2.Manifest;
import com.example.gabriel.mapsstarter2.R;
import com.example.gabriel.mapsstarter2.fragments.share.ConfirmationFragment;
import com.example.gabriel.mapsstarter2.fragments.share.PathFragment;
import com.example.gabriel.mapsstarter2.fragments.share.SharingFragment;
import com.example.gabriel.mapsstarter2.fragments.track.TrackArrivalFragment;
import com.example.gabriel.mapsstarter2.fragments.share.UserSelectFragment;
import com.example.gabriel.mapsstarter2.interfaces.OnDataListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashSet;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, OnDataListener {

    private static final String TAG = "MainActivity";
    private TabLayout tabLayout;

    protected HashSet<String> selectedUsernames;
    protected LatLng origin, destination;
    protected String originAddress, destinationAddress;
    private String fragmentState = "PathFragment";
    private String tripID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ask for Geolocation permissions if not granted
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location Permission NOT Granted yet");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
        }

        // TODO: Remove when initial fragment is MAP
        origin = new LatLng(41.942683, -87.649343);
        destination = new LatLng(41.94561420000001,-87.64343509999998);

        Fragment tabFragment = new PathFragment();
        //Fragment tabFragment = new UserSelectFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace fragment and add to back stack
        transaction.add(R.id.fragmentWrap, tabFragment);
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
        //Log.d(TAG, String.valueOf(tab.getPosition()));
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = null;

        for(int i = 0; i < getFragmentManager().getBackStackEntryCount(); ++i) {
            getFragmentManager().popBackStack();
        }
        if (tab.getPosition() == 0){
            /*if (fragmentState.equalsIgnoreCase(getString(R.string.user_select))){
                fragment = new UserSelectFragment();
            } else if (fragmentState.equalsIgnoreCase(getString(R.string.confirmation))){
                fragment = new ConfirmationFragment();
            } else if (fragmentState.equalsIgnoreCase(getString(R.string.sharing))){
                fragment = new SharingFragment();
            } else {
                fragment = new PathFragment();
            }*/
            fragment = new PathFragment();
        } else if(tab.getPosition() == 1){
            fragment = new TrackArrivalFragment();
        }

        transaction.replace(R.id.fragmentWrap, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        //Log.d(TAG, "onTabUnselected");
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        //Log.d(TAG, "onTabReselected");
    }

    @Override
    public void onLocationReady(LatLng origin, LatLng destination) {
        Log.d(TAG, "onLocationReady: " + String.valueOf(origin) + ", " + destination.toString());
        this.origin = origin;
        this.destination = destination;

    }

    @Override
    public void onUsernameReady(HashSet<String> usernames) {
        Log.d(TAG, "onUsernameReady: " + String.valueOf(selectedUsernames));
        this.selectedUsernames = usernames;
    }

    @Override
    public void setPageState(String fragmentName) {
        Log.d(TAG, "setPageState: " + fragmentName);
        this.fragmentState = fragmentName;
    }

    @Override
    public void setStrAddresses(String origin, String destination) {
        Log.d(TAG, "setStrAddresses()");
        this.originAddress = origin;
        this.destinationAddress = destination;
    }

    @Override
    public void setTripID(String id) {
        Log.d(TAG, "setTripID: " + id);
        this.tripID = id;
    }

    @Override
    public void getConfirmationData() {
        Log.d(TAG, "getConfirmationData: " + selectedUsernames.toString());
        ConfirmationFragment frag = (ConfirmationFragment)
                getFragmentManager().findFragmentById(R.id.fragmentWrap);

        if (frag != null){
            frag.onConfirmationData(origin, destination, selectedUsernames);
        }
    }

    @Override
    public void getSharingData() {
        Log.d(TAG, "getSharingData()");
        SharingFragment frag = (SharingFragment)
                getFragmentManager().findFragmentById(R.id.fragmentWrap);

        if (frag != null){
            frag.onSharingData(tripID, destinationAddress);
        }
    }

}


