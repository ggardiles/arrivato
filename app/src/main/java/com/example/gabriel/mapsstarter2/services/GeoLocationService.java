package com.example.gabriel.mapsstarter2.services;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.gabriel.mapsstarter2.utils.DirectionsApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executor;

import cz.msebera.android.httpclient.Header;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class GeoLocationService extends IntentService {

    // Global Static Fields
    private static final double FINISH_DISTANCE = 50.0; // We arrived to destination if distance is less than this number
    private static final double STEP_TO_SYNC = 100.0; // Total meters required from last position to sync to db
    private static final String TAG = "GeoLocationService";

    // Global Fields
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private LatLng position, destination;

    public GeoLocationService() {
        super("GeoLocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        // Get coordinates for origin and destination
        Double olat = intent.getDoubleExtra("origin_latitude", 0.0);
        Double olong = intent.getDoubleExtra("origin_longitude", 0.0);
        Double dlat = intent.getDoubleExtra("destination_latitude", 0.0);
        Double dlong = intent.getDoubleExtra("destination_longitude", 0.0);

        // Initialize global objects
        position = new LatLng(olat, olong);
        destination = new LatLng(dlat, dlong);

        // Define Location Variables and Callbacks
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                    if (distance(pos, destination) < STEP_TO_SYNC){
                        position = pos; // New position
                        Log.d(TAG, "position: " + String.valueOf(pos));
                    }
                }
            };
        };

        startLocationUpdates();

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location Permission has not been granted");
            return;
        }
        // Build Location Request Params
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000L);
        mLocationRequest.setFastestInterval(5000L);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(10);

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

}
