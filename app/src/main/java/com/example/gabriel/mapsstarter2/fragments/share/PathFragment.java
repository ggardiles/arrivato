package com.example.gabriel.mapsstarter2.fragments.share;


import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.gabriel.mapsstarter2.utils.DirectionsApiClient;
import com.example.gabriel.mapsstarter2.interfaces.OnDataListener;
import com.example.gabriel.mapsstarter2.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class PathFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener{
    private static final String TAG = "PathFragment";
    private static final LatLng HALSTED = new LatLng(41.942683, -87.649343);
    private static final LatLng STRATFORD = new LatLng(41.94561420000001,-87.64343509999998);
    private static final int PADDING = 350;
    private static final int PLACE_PICKER_REQUEST = 1;

    // Global Views
    private Button btnContinue;
    private FloatingActionButton btnDestination;

    // Global Variables
    private GoogleMap gMap;
    private Marker origin, destination;
    private OnDataListener mCallback;

    public PathFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnDataListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        // Set Page State in MainActivity
        mCallback.setPageState(getString(R.string.path));

        /**
         * MANAGE CHILD FRAGMENTS
         */
        // Create new fragment and transaction
        MapFragment gMapFragment = new MapFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        // Replace fragment and add to back stack
        transaction.replace(R.id.gMap, gMapFragment);
        //transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

        // Load Map Async
        if (gMapFragment != null){
            gMapFragment.getMapAsync(this);
            //return gMapFragment.getView();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
            return null;
        }

        View mapView = inflater.inflate(R.layout.fragment_path, container, false);

        // Initialize UI Widgets
        btnDestination = (FloatingActionButton) mapView.findViewById(R.id.btnDestination);
        btnContinue = (Button) mapView.findViewById(R.id.btnContinue);
        btnContinue.setEnabled(false);

        // Register Listeners
        btnDestination.setOnClickListener(this);
        btnContinue.setOnClickListener(this);

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
        gMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                Log.d(TAG, "Event: onMapLoaded");

                origin = gMap.addMarker(new MarkerOptions().position(HALSTED)
                        .title("Origin")
                        .snippet("Current Location"));

                autoCameraUpdate();
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

                // Pass data to activity
                mCallback.onLocationReady(origin.getPosition(), destination.getPosition());

                // Prepare Fragment Transition
                UserSelectFragment userSelectFragment = new UserSelectFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                // Replace fragment and add to back stack
                transaction.replace(R.id.fragmentWrap, userSelectFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
                break;
        }
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
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, PADDING);
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
                    List<LatLng> waypoints = decodePoly(encodedPolyline);

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

        gMap.addPolyline(options);
    }
}
