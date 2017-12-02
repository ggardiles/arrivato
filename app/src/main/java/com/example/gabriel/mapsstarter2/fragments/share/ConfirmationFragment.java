package com.example.gabriel.mapsstarter2.fragments.share;


import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.gabriel.mapsstarter2.interfaces.OnDataListener;
import com.example.gabriel.mapsstarter2.R;
import com.example.gabriel.mapsstarter2.models.Trip;
import com.example.gabriel.mapsstarter2.services.GeolocationService;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private String tripID;

    // UI Widgets
    private ListView lvViewers;
    private TextView tvFrom, tvTo;
    private HashSet<String> emails;
    private Button btnConfirm;
    private ProgressBar pbAddress;

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

        if (container != null) {
            container.removeAllViews();
        }

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
        btnConfirm.setEnabled(false);
        pbAddress = (ProgressBar) v.findViewById(R.id.pb_address);
        pbAddress.setVisibility(ProgressBar.VISIBLE);

        // Ask Main Activity for Confirmation Data
        mCallback.getConfirmationData();

        return v;
    }
    private String latlngToAddress(LatLng position){
        if (getActivity() == null){
            return "";
        }
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
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }

    }
    public void onConfirmationData(LatLng origin, LatLng destination, HashSet<String> emails){
        Log.d(TAG, "onConfirmationData(): " + new ArrayList<>(emails).toString());

        this.origin = origin;
        this.destination = destination;

        // Update Textview with addressess on separate thread
        new Thread(new TranslateToAddress()).start();

        // Load emails to ListView
        this.emails = emails;
        adapter.clear();
        adapter.addAll(new ArrayList<>(emails));
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "Button pressed");
        switch (v.getId()){
            case R.id.btnConfirm:
                Log.d(TAG, "Button confirmation pressed");

                // Publish trip to Firebase And Start GeoLocation Service
                publishTrip();

                break;
        }
    }

    private void startGeolocationService() {
        Intent intent = new Intent(getActivity(), GeolocationService.class);
        intent.putExtra("trip_id", tripID);
        intent.putExtra("origin_latitude",  origin.latitude);
        intent.putExtra("origin_longitude", origin.longitude);
        intent.putExtra("destination_latitude",  destination.latitude);
        intent.putExtra("destination_longitude", destination.longitude);

        getActivity().startService(intent);
    }

    private void publishTrip() {
        Log.d(TAG, "publishTrip()");
        // Get Firestore Instance
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            Log.e(TAG, "User is not authenticated");
            return;
        }
        // Create Trip Object
        Trip trip = new Trip(user.getEmail(), origin, destination, destAddress, emails);
        
        // Query Firestore for users
        db.collection(getString(R.string.trips_collection))
                .add(trip.getCustomHashMap())
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());

                        // Save Trip ID
                        tripID = documentReference.getId();
                        mCallback.setTripID(documentReference.getId());

                        // Start GeolocationService
                        startGeolocationService();

                        // Next Fragment
                        nextFragment();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private class TranslateToAddress implements Runnable {
        @Override
        public void run() {
            // Get Addressess
            originAddress = latlngToAddress(origin);
            destAddress = latlngToAddress(destination);

            // Notify MainActivity
            mCallback.setStrAddresses(originAddress, destAddress);

            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pbAddress.setVisibility(ProgressBar.GONE);

                        // Update Textviews
                        tvFrom.setText(originAddress);
                        tvTo.setText(destAddress);

                        // Enable Confirm Button
                        btnConfirm.setEnabled(true);
                    }
                });
            }


        }
    }

    private void nextFragment(){
        // Prepare Fragment Transition
        SharingFragment fragment = new SharingFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace fragment and add to back stack
        transaction.replace(R.id.fragmentWrap, fragment);

        // Clear all other fragments in backstack
        for(int i = 0; i < getFragmentManager().getBackStackEntryCount(); ++i) {
            getFragmentManager().popBackStack();
        }

        // Commit the transaction
        transaction.commit();
    }
}
