package com.example.gabriel.mapsstarter2.fragments.share;


import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.gabriel.mapsstarter2.utils.DirectionsApiClient;
import com.example.gabriel.mapsstarter2.interfaces.OnDataListener;
import com.example.gabriel.mapsstarter2.R;
import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import cz.msebera.android.httpclient.Header;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class PathFragment extends Fragment
        implements OnMapReadyCallback,
        View.OnClickListener,
        GoogleMap.OnMyLocationButtonClickListener{

    // Global Constant Fields
    private static final String TAG = "PathFragment";
    private static final LatLng HALSTED = new LatLng(41.942683, -87.649343);
    private static final LatLng STRATFORD = new LatLng(41.94561420000001, -87.64343509999998);
    private static final int PADDING = 350;
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final LatLng CHICAGO = new LatLng(41.878307, -87.635005);

    // Global Variables
    private GoogleMap gMap;
    private Marker origin, destination;
    private FusedLocationProviderClient mFusedLocationClient;
    private List<LatLng> waypoints;
    private Polyline polyline;
    private RxSharedPreferences rxPreferences;

    // Global UI Widgets
    private Button btnContinue;
    private FloatingActionButton btnDestination;

    public PathFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        rxPreferences = RxSharedPreferences.create(preferences);
    }

    private void saveDataToRxPrefs(){
        Preference<String> origPref  = rxPreferences.getString(getString(R.string.pref_origin));
        Preference<String> destPref = rxPreferences.getString(getString(R.string.pref_destination));

        String olat  = String.valueOf(origin.getPosition().latitude);
        String olong = String.valueOf(origin.getPosition().longitude);
        String dlat  = String.valueOf(destination.getPosition().latitude);
        String dlong = String.valueOf(destination.getPosition().longitude);

        origPref.set(olat+","+olong);
        destPref.set(dlat+","+dlong);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        View mapView = inflater.inflate(R.layout.fragment_path, container, false);

        // Initialize UI Widgets
        btnDestination = (FloatingActionButton) mapView.findViewById(R.id.btnDestination);
        btnContinue = (Button) mapView.findViewById(R.id.btnContinue);

        // Register Listeners
        btnDestination.setOnClickListener(this);
        btnContinue.setOnClickListener(this);

        // Disable button until destination is selected
        btnContinue.setEnabled(false);

        // Set Page State in MainActivity
        Preference<String> pagePref = rxPreferences.getString(getString(R.string.pref_page));
        pagePref.set(getString(R.string.page_path));

        // Get Location Client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        /**
         * MANAGE CHILD FRAGMENTS
         */
        // Create new fragment and transaction
        MapFragment gMapFragment = new MapFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        // Replace fragment and add to back stack
        transaction.replace(R.id.gMap, gMapFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

        // Load Map Async
        if (gMapFragment != null) {
            gMapFragment.getMapAsync(this);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
            return null;
        }

        return mapView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Event: onMapReady");

        if (googleMap == null) {
            Toast.makeText(getActivity().getApplicationContext(), "Error - Map was null!!", Toast.LENGTH_SHORT).show();
            return;
        }

        gMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location Permissions were NOT granted");
            return;
        }
        // Display Button Return Map to my location
        gMap.setMyLocationEnabled(true);
        gMap.setOnMyLocationButtonClickListener(this);

        CameraUpdate firstCameraUpdate = CameraUpdateFactory.newLatLngZoom(CHICAGO, 8.0f);
        gMap.moveCamera(firstCameraUpdate);
        // Map is Loaded
        gMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                Log.d(TAG, "Event: onMapLoaded");

                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "Location Permissions were NOT granted");
                    return;
                }
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    origin = gMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                                            .title("Origin")
                                            .snippet("Current Location"));

                                    // Check if view is restored
                                    if (waypoints != null){
                                        drawRoute(waypoints);
                                    }
                                    if (destination != null){
                                        destination = gMap.addMarker(new MarkerOptions().position(destination.getPosition())
                                                .title("Destination"));
                                    }

                                    autoCameraUpdate();
                                }
                            }
                        });
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Log.d(TAG, "OnActivityResult: RESULT NOT OK");
            return;
        }

        if (requestCode == PLACE_PICKER_REQUEST) {
            Log.d(TAG, "OnActivityResult: PLACE_PICKER_REQUEST");

            // Remove previous route if defined
            if (polyline != null) polyline.remove();
            if (destination != null) destination.remove();

            Place place = PlacePicker.getPlace(getContext(), data);

            destination = gMap.addMarker(new MarkerOptions().position(place.getLatLng())
                    .title("Destination"));

            Log.d(TAG, "Destination LatLong: " + place.getLatLng().toString());

            // Draw route
            queryDirectionsAPI(origin.getPosition(), place.getLatLng());

            btnContinue.setEnabled(true);

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btnDestination:
                Log.d(TAG, "OnClick: btnDestination");
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btnContinue:
                Log.d(TAG, "OnClick: btnContinue");
                if (destination == null){
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Set destination first", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save Data
                saveDataToRxPrefs();

                // Next Fragment
                nextFragment();
                break;
        }
    }


    @Override
    public boolean onMyLocationButtonClick() {
        autoCameraUpdate();
        return false;
    }

    private void nextFragment() {
        // Prepare Fragment Transition
        UserSelectFragment userSelectFragment = new UserSelectFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace fragment and add to back stack
        transaction.replace(R.id.fragmentWrap, userSelectFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
    
    private void autoCameraUpdate(){

        ArrayList<Marker> markers = new ArrayList<Marker>();
        if (origin != null) markers.add(origin);
        if (destination != null) markers.add(destination);


        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate =
                CameraUpdateFactory.newLatLngBounds(bounds, PADDING);
        if (markers.size() == 1){
            cameraUpdate =
                    CameraUpdateFactory.newLatLngZoom(origin.getPosition(), 15.0f);
        }
        //gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
        gMap.animateCamera(cameraUpdate);
    }

    private void queryDirectionsAPI(LatLng origin, LatLng destination) {
        Log.d(TAG, "queryDirectionsAPI: " + origin.toString() + " to " + destination.toString());
        DirectionsApiClient.getDirections(origin, destination, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try {

                    // Parse JSON to find encoded polyline
                    JSONObject routes = (JSONObject) response.getJSONArray("routes").get(0);
                    JSONObject overviewPolyline = routes.getJSONObject("overview_polyline");
                    String encodedPolyline = overviewPolyline.getString("points");
                    Log.d(TAG, encodedPolyline);

                    // Get waypoints from encoded polyline
                    waypoints = decodePoly(encodedPolyline);

                    // Draw waypoints as a single polyline
                    drawRoute(waypoints);

                    // Update Camera
                    autoCameraUpdate();
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }


    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private void drawRoute(List<LatLng> waypoints){
        PolylineOptions options = new PolylineOptions()
                .clickable(true)
                .width(7)
                .color(Color.RED)
                .geodesic(true);
        for(LatLng point: waypoints){
            options.add(point);
        }

        polyline = gMap.addPolyline(options);
    }
}
