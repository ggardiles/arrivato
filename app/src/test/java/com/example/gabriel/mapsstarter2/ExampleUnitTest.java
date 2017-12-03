package com.example.gabriel.mapsstarter2;

import com.example.gabriel.mapsstarter2.models.Trip;
import com.example.gabriel.mapsstarter2.models.User;
import com.example.gabriel.mapsstarter2.services.GeolocationService;
import com.example.gabriel.mapsstarter2.utils.DirectionsApiClient;
import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.mockito.internal.matchers.Null;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void trip_testFirebaseHashmap() throws Exception {
        Trip trip = new Trip("t", new LatLng(1,2), new LatLng(3,4), "d", new HashSet<String>());
        HashMap<String, Object> hm = trip.getCustomHashMap();

        assertEquals(trip.getTraveler(), hm.get("traveler"));
        assertEquals(String.format(Locale.US, "%f, %f",
                trip.getLocation().latitude, trip.getLocation().longitude),
                hm.get("location"));
        assertEquals(String.format(Locale.US, "%f, %f",
                trip.getDestination().latitude, trip.getDestination().longitude),
                hm.get("destination"));
        assertEquals(trip.getStatus(), hm.get("status"));
    }

    @Test(expected = NullPointerException.class)
    public void trip_expectExceptionFirebaseHashmap() throws Exception {
        Trip trip = new Trip("t", new LatLng(1,2), new LatLng(3,4), "d",null);
        HashMap<String, Object> hm = trip.getCustomHashMap();

        assertEquals(trip.getTraveler(), hm.get("traveler"));
        assertEquals(String.format(Locale.US, "%f, %f",
                trip.getLocation().latitude, trip.getLocation().longitude),
                hm.get("location"));
        assertEquals(String.format(Locale.US, "%f, %f",
                trip.getDestination().latitude, trip.getDestination().longitude),
                hm.get("destination"));
        assertEquals(trip.getStatus(), hm.get("status"));
    }

    @Test
    public void user_testFirebaseHashmap() throws Exception {
        User user = new User("G", "g@g.com");
        HashMap<String, Object> hm = user.getCustomHashMap();

        assertEquals(user.getName(),  hm.get("name"));
        assertEquals(user.getEmail(), hm.get("email"));
    }

    @Test
    public void user_shouldNotThrowErrorFirebaseHashmap() throws Exception {
        User user = new User("G", null);
        HashMap<String, Object> hm = user.getCustomHashMap();

        assertEquals(user.getName(),  hm.get("name"));
        assertEquals(user.getEmail(), hm.get("email"));
    }

    @Test
    public void user_shouldNotThrowErrorFirebaseHashmapB() throws Exception {
        User user = new User(null, null);
        HashMap<String, Object> hm = user.getCustomHashMap();

        assertEquals(user.getName(),  hm.get("name"));
        assertEquals(user.getEmail(), hm.get("email"));
    }

    @Test
    public void service_shouldCalculateDistance() throws Exception {
        double distance = GeolocationService.distance(new LatLng(1,2), new LatLng(2, 3));
        assertEquals(157225.43203d, distance, 0.1d);

        distance = GeolocationService.distance(new LatLng(2, 5), new LatLng(4, 5));
        assertEquals(222389.8d, distance, 0.1d);

        distance = GeolocationService.distance(new LatLng(3, 14), new LatLng(18, 21));
        assertEquals(1834115.5d, distance, 0.1d);

        distance = GeolocationService.distance(new LatLng(54, -10), new LatLng(53, -8));
        assertEquals(172797.4d, distance, 0.1d);
    }

    @Test(expected = Exception.class)
    public void service_shouldThrowDistance() throws Exception {
        double distance = GeolocationService.distance(new LatLng(1,2), null);
    }

}