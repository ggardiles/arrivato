package com.example.gabriel.mapsstarter2;

import com.google.android.gms.maps.model.LatLng;
import java.util.HashSet;

/**
 * Created by gabriel on 15/11/17.
 */
public interface OnDataListener {
    void onLocationReady(LatLng origin, LatLng destination);
    void onUsernameReady(HashSet<String> usernames);
    void getConfirmationData();
}