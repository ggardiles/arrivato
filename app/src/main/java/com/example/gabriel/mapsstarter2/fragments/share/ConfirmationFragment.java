package com.example.gabriel.mapsstarter2.fragments.share;


import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
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

import com.example.gabriel.mapsstarter2.activities.MainActivity;
import com.example.gabriel.mapsstarter2.interfaces.OnDataListener;
import com.example.gabriel.mapsstarter2.R;
import com.example.gabriel.mapsstarter2.models.Trip;
import com.example.gabriel.mapsstarter2.services.GeolocationService;
import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
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
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class ConfirmationFragment extends Fragment implements View.OnClickListener{

    // Global Constant fields
    private static final String TAG = "ConfirmationFragment";

    // Global Fields
    private ArrayAdapter<String> adapter;
    private String originAddress, destAddress;
    private LatLng origin, destination;
    private FirebaseFirestore db;
    private String tripID;
    private RxSharedPreferences rxPreferences;

    // UI Widgets
    private ListView lvViewers;
    private TextView tvFrom, tvTo;
    private HashSet<String> emails;
    private Button btnConfirm;
    private ProgressBar pbAddress;

    public ConfirmationFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        rxPreferences = RxSharedPreferences.create(preferences);

        // Set Listeners
        setRxPreferencesListeners();
    }

    private void saveDataToRxPrefs(){
        Preference<String> tripIDPref  =
                rxPreferences.getString(getString(R.string.pref_trip_id));

        tripIDPref.set(tripID);
    }

    private void setRxPreferencesListeners(){
        Preference<String> origPref  = rxPreferences.getString(getString(R.string.pref_origin));
        Preference<String> destPref = rxPreferences.getString(getString(R.string.pref_destination));
        Preference<String> originStrPref  = rxPreferences.getString(getString(R.string.pref_origin_str));
        Preference<String> destStrPref  = rxPreferences.getString(getString(R.string.pref_destination_str));
        Preference<Set<String>> userEmailsPref  = rxPreferences.getStringSet(getString(R.string.pref_email_list));


        Observable<String> observOrigin = origPref.asObservable();
        observOrigin.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(String originLocal) {
                Log.d(TAG, "Origin: " + String.valueOf(originLocal));
                if (originLocal.isEmpty()){
                    return;
                }
                // Parse String to LatLng
                String[] latlong =  originLocal.split(",");
                double latitude = Double.parseDouble(latlong[0]);
                double longitude = Double.parseDouble(latlong[1]);
                origin = new LatLng(latitude, longitude);
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });

        Observable<String> obserDest = destPref.asObservable();
        obserDest.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(String dest) {
                Log.d(TAG, "Destination: " + String.valueOf(dest));
                if (dest.isEmpty()){
                    return;
                }
                // Parse String to LatLng
                String[] latlong =  dest.split(",");
                double latitude = Double.parseDouble(latlong[0]);
                double longitude = Double.parseDouble(latlong[1]);
                destination = new LatLng(latitude, longitude);
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });

        Observable<String> observOriginStr = originStrPref.asObservable();
        observOriginStr.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(String origin) {
                Log.d(TAG, "Origin STR: " + origin);
                if (origin.isEmpty()){
                    return;
                }
                originAddress = origin;

                if (tvFrom == null){
                    Log.d(TAG, "Origing STR: view not created yet, skipping");
                    return;
                }
                if (originAddress != null && destAddress != null) {
                    // Remove Spinner
                    pbAddress.setVisibility(ProgressBar.GONE);

                    // Enable Confirm Button
                    btnConfirm.setEnabled(true);
                }
                // Update Textviews
                tvFrom.setText(originAddress);

            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });

        Observable<String> observDestStr = destStrPref.asObservable();
        observDestStr.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(String dest) {
                Log.d(TAG, "Dest STR: " + dest);
                if (dest.isEmpty()){
                    return;
                }
                destAddress = dest;

                if (tvTo == null){
                    Log.d(TAG, "Dest STR: view not created yet, skipping");
                    return;
                }
                if (originAddress != null && destAddress != null) {
                    // Remove Spinner
                    pbAddress.setVisibility(ProgressBar.GONE);

                    // Enable Confirm Button
                    btnConfirm.setEnabled(true);
                }
                // Update Textview
                tvTo.setText(destAddress);

            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });

        Observable<Set<String>> observEmails = userEmailsPref.asObservable();
        observEmails.subscribe(new Observer<Set<String>>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(Set<String> emailsLocal) {
                Log.d(TAG, "user emails: " + emailsLocal.toString());
                if (emailsLocal.isEmpty()){
                    return;
                }

                emails = new HashSet<>(emailsLocal);

                if (adapter != null) {
                    adapter.clear();
                    adapter.addAll(new ArrayList<>(emails));
                    adapter.notifyDataSetChanged();
                }

            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView()");

        if (container != null) {
            container.removeAllViews();
        }

        // Set Page State in MainActivity
        Preference<String> pagePref = rxPreferences.getString(getString(R.string.pref_page));
        pagePref.set(getString(R.string.page_confirmation));

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_confirmation, container, false);

        // Initialize UI Widgets
        lvViewers = (ListView) v.findViewById(R.id.lvViewers);
        tvFrom = (TextView) v.findViewById(R.id.tvFrom);
        tvTo = (TextView) v.findViewById(R.id.tvTo);
        btnConfirm = (Button) v.findViewById(R.id.btnConfirm);
        pbAddress = (ProgressBar) v.findViewById(R.id.pb_address);

        // Configure UI Widgets
        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, new ArrayList<>(emails));
        lvViewers.setAdapter(adapter);

        // Set Button Click Listeners
        btnConfirm.setOnClickListener(this);
        btnConfirm.setEnabled(false);

        // Spinner Visible
        pbAddress.setVisibility(ProgressBar.VISIBLE);

        // If not null change textview
        if (originAddress != null){
            tvFrom.setText(originAddress);
        }
        if (destAddress != null){
            tvTo.setText(destAddress);
        }
        if (originAddress != null && destAddress != null) {
            // Remove Spinner
            pbAddress.setVisibility(ProgressBar.GONE);

            // Enable Confirm Button
            btnConfirm.setEnabled(true);
        }

        return v;
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

                        // Save Data
                        saveDataToRxPrefs();

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

    private void startGeolocationService() {
        Log.d(TAG, "startGeolocationService()");
        Intent intent = new Intent(getActivity(), GeolocationService.class);
        intent.putExtra("trip_id", tripID);
        intent.putExtra("origin_latitude",  origin.latitude);
        intent.putExtra("origin_longitude", origin.longitude);
        intent.putExtra("destination_latitude",  destination.latitude);
        intent.putExtra("destination_longitude", destination.longitude);

        getActivity().startService(intent);
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
