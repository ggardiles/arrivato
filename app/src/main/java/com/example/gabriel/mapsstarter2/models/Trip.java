package com.example.gabriel.mapsstarter2.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

/**
 * Created by gabriel on 24/11/17.
 */

public class Trip {
    public static final String FINISHED = "FINISHED";
    public static final String STARTED = "STARTED";
    public static final String CANCELED = "CANCELED";
    private String id;
    private String traveler;
    private LatLng location;
    private LatLng destination;
    private String destinationStr;
    private String TTD;
    private HashSet<String> viewers;
    private String status;

    public Trip(String traveler, LatLng location, LatLng destination, String destinationStr, HashSet<String> viewers) {
        this.traveler = traveler;
        this.location = location;
        this.destination = destination;
        this.destinationStr = destinationStr;
        this.viewers = viewers;
        this.status = STARTED;
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
        map.put("status", status);
        for (String email : viewers){
            map.put("user_" + email.replace(".",""), true);
        }
        return map;
    }

    public String getId() {
        return id;
    }

    public String getTTD() {
        return TTD;
    }

    public void setTTD(String TTD) {
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
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "id='" + id + '\'' +
                ", traveler='" + traveler + '\'' +
                ", location=" + location +
                ", destination=" + destination +
                ", destinationStr='" + destinationStr + '\'' +
                ", TTD='" + TTD + '\'' +
                ", viewers=" + viewers +
                ", status='" + status + '\'' +
                '}';
    }
}

