package com.example.gabriel.mapsstarter2.utils;

/**
 * Created by gabriel on 8/11/17.
 */

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.*;

public class DirectionsApiClient {
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String KEY = "AIzaSyB4fDbBhLDLa-Pn8VDPfYmhMkZU-oWRXLs";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void getDirections(LatLng origin, LatLng destination, AsyncHttpResponseHandler responseHandler){
        RequestParams params = new RequestParams();
        params.put("origin", String.valueOf(origin.latitude) + "," + String.valueOf(origin.longitude));
        params.put("destination", String.valueOf(destination.latitude) + "," + String.valueOf(destination.longitude));
        params.put("key", KEY);
        client.get(BASE_URL, params, responseHandler);
    }
}
