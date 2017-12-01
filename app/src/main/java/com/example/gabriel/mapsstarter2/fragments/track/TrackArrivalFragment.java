package com.example.gabriel.mapsstarter2.fragments.track;


import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gabriel.mapsstarter2.R;
import com.example.gabriel.mapsstarter2.adapters.TripsAdapter;
import com.example.gabriel.mapsstarter2.models.Trip;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TrackArrivalFragment extends Fragment {

    // Global Constants
    private final static String TAG = "TrackArrivalFragment";

    // Global Fields
    private String username;
    private ListenerRegistration registration;
    private TripsAdapter adapter;

    // Global UI Widgets
    private RecyclerView rv;

    public TrackArrivalFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_track_arrival, container, false);

        // Initialize UI Widgets
        rv = (RecyclerView) v.findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        // Set Linear layout for recycler view
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        // TODO: Should dynamically get current username making request
        this.username = "gabriel";

        // Query Trip Data shared with this user
        trackMyTrips();

        // Associate array of trips with trip adapter
        adapter = new TripsAdapter(new ArrayList<Trip>());

        // Link adapter with recycler view
        rv.setAdapter(adapter);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (registration != null){
            registration.remove();
        }
    }

    private void trackMyTrips(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            Log.e(TAG, "User is not logged in");
            return;
        }
        Query query = db.collection("trips")
                .whereEqualTo("user_"+user.getEmail().replace(".", ""), true);
        registration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        Log.d(TAG, "onEvent()");
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        ArrayList<Trip> trips = new ArrayList<>();
                        for (DocumentSnapshot doc : value) {
                            Trip trip = new Trip();
                            if (doc.contains("traveler")) {
                                trip.setTraveler(doc.getString("traveler"));
                            }
                            if (doc.contains("destinationStr")){
                                trip.setDestinationStr(doc.getString("destinationStr"));
                            }
                            if (doc.contains("ttd")){
                                trip.setTTD(doc.getString("ttd"));
                            }
                            if (doc.contains("status")){
                                trip.setStatus(doc.getString("status"));
                            }
                            trips.add(trip);
                        }
                        Log.d(TAG, "Current trips: " + trips);
                        TripsAdapter localAdapter = new TripsAdapter(trips);
                        rv.swapAdapter(localAdapter, false);

                    }
                });
    }
}
