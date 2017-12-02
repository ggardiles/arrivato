package com.example.gabriel.mapsstarter2.fragments.share;


import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gabriel.mapsstarter2.R;
import com.example.gabriel.mapsstarter2.adapters.TripsAdapter;
import com.example.gabriel.mapsstarter2.interfaces.OnDataListener;
import com.example.gabriel.mapsstarter2.models.Trip;
import com.example.gabriel.mapsstarter2.services.GeolocationService;
import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SharingFragment extends Fragment  implements View.OnClickListener{

    // Global Constant Fields
    private static final String TAG = "SharingFragment";

    // Global Fields
    private OnDataListener mCallback;
    private String tripID;
    private String destinationAddress;

    // UI Widgets
    private Button btnCancel;
    private TextView tvArrivalTime, tvDestination;
    private ProgressBar pvArrivalTime;

    public SharingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnDataListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        RxSharedPreferences rxPreferences = RxSharedPreferences.create(preferences);
        Preference<Float> foo = rxPreferences.getFloat("foo");
        foo.set(2.0f);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        // Set Page State in MainActivity
        mCallback.setPageState(getString(R.string.sharing));

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sharing, container, false);

        // Reference to UI Widgets
        btnCancel     = (Button) v.findViewById(R.id.btnCancel);
        tvArrivalTime = (TextView) v.findViewById(R.id.tvArrivalTime);
        tvDestination = (TextView) v.findViewById(R.id.tvDestinationFinal);
        pvArrivalTime = (ProgressBar) v.findViewById(R.id.pv_arrivaltime);

        // Register Listeners
        btnCancel.setOnClickListener(this);
        pvArrivalTime.setVisibility(ProgressBar.VISIBLE);

        // Get Data from Main Activity
        mCallback.getSharingData();

        // Remove Backstack
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnCancel:
                // Stop Geolocation Service
                Intent intent = new Intent(getActivity(), GeolocationService.class);
                getActivity().stopService(intent);

                // Cancel Trip
                cancelTrip();

                // To Initial Fragment
                initialFragment();
                break;
        }
    }
    public void onSharingData(String tripID, String destination){
        Log.d(TAG, "onSharingData() TripID: " + tripID
                + " - destinationAddress: " + destination);
        this.tripID = tripID;
        this.destinationAddress = destination;
        tvDestination.setText(destination);

        // Start tracking location
        trackTrip();
    }

    private void cancelTrip(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (tripID == null){
            Log.e(TAG, "TripID cannot be null");
            return;
        }

        DocumentReference documentReference = db.collection("trips")
                .document(tripID);
        documentReference.update("status", Trip.CANCELED);

    }
    private void trackTrip(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (tripID == null){
            Log.e(TAG, "TripID cannot be null");
            return;
        }

        DocumentReference documentReference = db.collection("trips")
                .document(tripID);

        documentReference.addSnapshotListener(snapshotListener);

    }

    private final EventListener<DocumentSnapshot> snapshotListener =
            new EventListener<DocumentSnapshot>() {

                @Override
                public void onEvent(DocumentSnapshot snapshot,
                                    FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Log.d(TAG, "Current data: " + snapshot.getData());

                        // Hide ProgressBar
                        pvArrivalTime.setVisibility(ProgressBar.GONE);

                        // Set Textview Arrival Time
                        String status = snapshot.getString("status");
                        if (status.equals(Trip.STARTED)){
                            String ttd = snapshot.getString("ttd");
                            tvArrivalTime.setText(ttd);
                        } else {
                            tvArrivalTime.setText(snapshot.getString("status"));
                        }


                    } else {
                        Log.d(TAG, "Current data: null");
                    }

                }
            };
    private void initialFragment(){
        // Prepare Fragment Transition
        Fragment fragment = new PathFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace fragment and add to back stack
        transaction.replace(R.id.fragmentWrap, fragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
    }
}
