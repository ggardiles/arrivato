package com.example.gabriel.mapsstarter2.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.gabriel.mapsstarter2.R;
import com.example.gabriel.mapsstarter2.interfaces.AsyncTimeDistanceCallback;
import com.example.gabriel.mapsstarter2.models.Trip;
import com.example.gabriel.mapsstarter2.utils.DirectionsApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class GeolocationService extends Service {

    // Global Static Fields
    private static final String TAG = "GeolocationService";
    private static final double FINISH_DISTANCE = 50.0; // Distance considered to have reached destination

    // Global Fields
    private FusedLocationProviderClient mFusedLocationClient;
    private LatLng position, destination;
    private LocationCallback mLocationCallback;
    private FirebaseFirestore db;
    private String tripID;

    // Constructor
    public GeolocationService() {}

    @Override
    public void onCreate() {
        // Get Reference to Firestore DB
        db = FirebaseFirestore.getInstance();

        // Define Location Variables and Callbacks
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Callback
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                Log.d(TAG, "position received");

                for (Location location : locationResult.getLocations()) {
                    position = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.d(TAG, "position: " + position.toString());
                    Log.d(TAG, "distance: " + distance(position, destination));
                    if (distance(position, destination) < FINISH_DISTANCE){
                        Log.d(TAG, "Reached destination");

                        // Set TTD and Distance
                        updateTripAndFinish();
                        return;
                    }

                    // Get Time and Distance and update DB
                    DirectionsApiClient.getTimeAndDistance(position, destination,
                            null, asyncTimeDistanceCallback);

                }
            };
        };

        // Register Location Updates
        startLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get coordinates for origin and destination
        tripID = intent.getStringExtra("trip_id");
        Double olat = intent.getDoubleExtra("origin_latitude", 0.0);
        Double olong = intent.getDoubleExtra("origin_longitude", 0.0);
        Double dlat = intent.getDoubleExtra("destination_latitude", 0.0);
        Double dlong = intent.getDoubleExtra("destination_longitude", 0.0);

        // Initialize global objects
        position = new LatLng(olat, olong);
        destination = new LatLng(dlat, dlong);
        Log.d(TAG, "origin: " + position.toString());
        Log.d(TAG, "destination: " + destination.toString());

        // Get Time and Distance and update DB
        DirectionsApiClient.getTimeAndDistance(position, destination,
                null, asyncTimeDistanceCallback);

        // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service Done");

        // TODO: Remove trip or similar

        // Remove registered callback
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void startLocationUpdates() {
        // Check Permissions
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location Permission has not been granted");
            return;
        }

        // Build Location Request Params
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000L); // ms
        mLocationRequest.setFastestInterval(5000L); // ms
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(10); // meters

        // Register Location Updates Listener
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    private static double distance(LatLng position, LatLng destination) {

        double lat1 = position.latitude,  lat2 = destination.latitude;
        double lon1 = position.longitude, lon2 = destination.longitude;

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }

    private final AsyncTimeDistanceCallback asyncTimeDistanceCallback = new AsyncTimeDistanceCallback(){
        @Override
        public void onTimeDistance(String ttd, String distance) {
            Log.d(TAG, "Received ttd and distance: " + ttd + "  -  " + distance);
            updateTrip(ttd, distance);
        }
    };

    private void updateTrip(String ttd, String distance) {

        if (tripID == null){
            Log.e(TAG, "TripID cannot be null");
            return;
        }

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("ttd", ttd);
        if (distance != null){
            updateParams.put("distance", distance);
        }
        // Query Firestore for users
        db.collection(getString(R.string.trips_collection))
                .document(tripID)
                .update(updateParams)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Document with id " + tripID + " successfully updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void updateTripAndFinish() {

        if (tripID == null){
            Log.e(TAG, "TripID cannot be null");
            return;
        }

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("ttd", "0 min");
        updateParams.put("distance", "0 km");
        updateParams.put("status", Trip.FINISHED);

        // Query Firestore for users
        db.collection(getString(R.string.trips_collection))
                .document(tripID)
                .update(updateParams)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Document with id " + tripID + " successfully updated");
                        stopSelf();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }



}
