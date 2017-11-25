package com.example.gabriel.mapsstarter2.fragments.share;


import android.app.FragmentTransaction;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.gabriel.mapsstarter2.interfaces.OnDataListener;
import com.example.gabriel.mapsstarter2.R;
import com.example.gabriel.mapsstarter2.models.Trip;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class ConfirmationFragment extends Fragment implements View.OnClickListener{

    // Global Constant fields
    private static final String TAG = "ConfirmationFragment";

    // Global Fields
    private OnDataListener mCallback;
    private ArrayAdapter<String> adapter;
    private String originAddress, destAddress;
    private LatLng origin, destination;
    private FirebaseFirestore db;

    // UI Widgets
    private ListView lvViewers;
    private TextView tvFrom, tvTo;
    private HashSet<String> usernames;
    private Button btnConfirm;

    public ConfirmationFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
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
        mCallback.setPageState(getString(R.string.confirmation));

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_confirmation, container, false);

        // Initialize UI Widgets
        lvViewers = (ListView) v.findViewById(R.id.lvViewers);
        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line);
        lvViewers.setAdapter(adapter);

        tvFrom = (TextView) v.findViewById(R.id.tvFrom);
        tvTo = (TextView) v.findViewById(R.id.tvTo);
        btnConfirm = (Button) v.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(this);

        // Ask Main Activity for Confirmation Data
        mCallback.getConfirmationData();

        return v;
    }
    private String latlngToAddress(LatLng position){
        Geocoder geocoder = new Geocoder(getActivity());
        try {
            List<Address> addresses =
                    geocoder.getFromLocation(position.latitude,
                            position.longitude, 1);
            if (addresses.isEmpty()){
                Log.w(TAG, "Empty addresses for position: " + position.toString());
            }
            Address address = addresses.get(0);
            return address.getAddressLine(0);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
    public void onConfirmationData(LatLng origin, LatLng destination, HashSet<String> usernames){
        Log.d(TAG, "onConfirmationData(): " + new ArrayList<>(usernames).toString());

        this.origin = origin;
        this.destination = destination;
        this.originAddress = latlngToAddress(origin);
        this.destAddress = latlngToAddress(destination);
        this.usernames = usernames;

        tvFrom.setText("From: " + this.originAddress);
        tvTo.setText("To:   " + this.destAddress);

        // Load usernames to ListView
        adapter.clear();
        adapter.addAll(new ArrayList<>(usernames));
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "Button pressed");
        switch (v.getId()){
            case R.id.btnConfirm:
                Log.d(TAG, "Button confirmation pressed");

                publishTrip();

                // Prepare Fragment Transition
                SharingFragment fragment = new SharingFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                // Replace fragment and add to back stack
                transaction.replace(R.id.fragmentWrap, fragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
                break;
        }
    }

    private void publishTrip() {
        Log.d(TAG, "publishTrip()");
        // Get Firestore Instance
        db = FirebaseFirestore.getInstance();

        Trip trip = new Trip("ID", origin, destination, destAddress, usernames);
        
        // Query Firestore for users
        db.collection(getString(R.string.trips_collection))
                .add(trip.getCustomHashMap())
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        mCallback.setTripID(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }
}
