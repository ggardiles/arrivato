package com.example.gabriel.mapsstarter2.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

/**
 * Created by gabriel on 24/11/17.
 */

public class Trip {
    private String id;
    private String traveler;
    private LatLng location;
    private LatLng destination;
    private String destinationStr;
    private int TTD;
    private HashSet<String> viewers;

    public Trip(String traveler, LatLng location, LatLng destination, String destinationStr, HashSet<String> viewers) {
        this.traveler = traveler;
        this.location = location;
        this.destination = destination;
        this.destinationStr = destinationStr;
        this.viewers = viewers;
    }

    public Trip() {
    }

    public HashMap<String, Object> getCustomHashMap(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("traveler", traveler);
        map.put("location",
                String.format(Locale.US, "%f, %f", location.latitude, location.longitude));
        map.put("destination",
                String.format(Locale.US, "%f, %f", destination.latitude, destination.longitude));
        map.put("destinationStr", destinationStr);
        for (String username : viewers){
            map.put("user_" + username, true);
        }
        return map;
    }

    public String getId() {
        return id;
    }

    public int getTTD() {
        return TTD;
    }

    public void setTTD(int TTD) {
        this.TTD = TTD;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTraveler() {
        return traveler;
    }

    public void setTraveler(String traveler) {
        this.traveler = traveler;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public LatLng getDestination() {
        return destination;
    }

    public void setDestination(LatLng destination) {
        this.destination = destination;
    }

    public String getDestinationStr() {
        return destinationStr;
    }

    public void setDestinationStr(String destinationStr) {
        this.destinationStr = destinationStr;
    }

    public HashSet<String> getViewers() {
        return viewers;
    }

    public void setViewers(HashSet<String> viewers) {
        this.viewers = viewers;
    }
}

