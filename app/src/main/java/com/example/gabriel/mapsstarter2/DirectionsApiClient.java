package com.example.gabriel.mapsstarter2;

/**
 * Created by gabriel on 8/11/17.
 */

import android.content.res.Resources;

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
        /*String url = BASE_URL
                + "origin="
                + String.valueOf(origin.latitude) + "," + String.valueOf(origin.longitude)
                + "&destination="
                + String.valueOf(destination.latitude) + "," + String.valueOf(destination.longitude)
                + "&key=" + KEY;*/
        client.get(BASE_URL, params, responseHandler);
    }
    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
