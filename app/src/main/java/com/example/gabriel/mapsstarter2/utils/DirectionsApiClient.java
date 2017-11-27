package com.example.gabriel.mapsstarter2.utils;

/**
 * Created by gabriel on 8/11/17.
 */

import android.util.Log;

import com.example.gabriel.mapsstarter2.interfaces.AsyncTimeDistanceCallback;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.*;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class DirectionsApiClient {

    private static final String BASE_URL = "https://maps.googleapis.com/maps/api";
    private static final String KEY = "AIzaSyB4fDbBhLDLa-Pn8VDPfYmhMkZU-oWRXLs";
    private static final String TAG = "DirectionsApiClient";
    private static final String INVALID_REQUEST = "INVALID_REQUEST";
    private static final String ZERO_RESULTS = "ZERO_RESULTS";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void getDirections(LatLng origin, LatLng destination, AsyncHttpResponseHandler responseHandler){
        RequestParams params = new RequestParams();
        params.put("origin", String.valueOf(origin.latitude) + "," + String.valueOf(origin.longitude));
        params.put("destination", String.valueOf(destination.latitude) + "," + String.valueOf(destination.longitude));
        params.put("key", KEY);
        client.get(BASE_URL+"/directions/json?", params, responseHandler);
    }

    public static void getTimeAndDistance(LatLng origin, LatLng destination, String mode,
                                          final AsyncTimeDistanceCallback handler){
        RequestParams params = new RequestParams();
        params.put("origins", String.valueOf(origin.latitude) + "," + String.valueOf(origin.longitude));
        params.put("destinations", String.valueOf(destination.latitude) + "," + String.valueOf(destination.longitude));
        if (mode != null){
            params.put("mode", mode);
        }
        params.put("key", KEY);
        client.get(BASE_URL+"/distancematrix/json?", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try {

                    Log.d(TAG, "Received response");

                    if (response.getString("status").equals(INVALID_REQUEST)){
                        Log.e(TAG, "Invalid Request");
                        return;
                    }
                    // Parse JSON to find distance and ttd
                    JSONObject row  = (JSONObject) response.getJSONArray("rows").get(0);
                    JSONObject elem = (JSONObject) row.getJSONArray("elements").get(0);
                    if (elem.getString("status").equals(ZERO_RESULTS)){
                        Log.e(TAG, "Zero results for those coordinates");
                        return;
                    }
                    JSONObject distance = (JSONObject) elem.get("distance");
                    JSONObject duration = (JSONObject) elem.get("duration");

                    // Get Distance and TTD strings
                    String distanceStr = distance.getString("text");
                    String ttdStr = duration.getString("text");
                    Log.d(TAG, "ttd: " + ttdStr + " - distance: " + distanceStr);

                    // Pass to callback
                    handler.onTimeDistance(ttdStr, distanceStr);

                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }
}
