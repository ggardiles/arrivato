package com.example.gabriel.mapsstarter2.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.gabriel.mapsstarter2.Manifest;
import com.example.gabriel.mapsstarter2.R;
import com.example.gabriel.mapsstarter2.adapters.TripsAdapter;
import com.example.gabriel.mapsstarter2.fragments.share.ConfirmationFragment;
import com.example.gabriel.mapsstarter2.fragments.share.PathFragment;
import com.example.gabriel.mapsstarter2.fragments.share.SharingFragment;
import com.example.gabriel.mapsstarter2.fragments.track.TrackArrivalFragment;
import com.example.gabriel.mapsstarter2.fragments.share.UserSelectFragment;
import com.example.gabriel.mapsstarter2.interfaces.OnDataListener;
import com.example.gabriel.mapsstarter2.models.Trip;
import com.example.gabriel.mapsstarter2.models.User;
import com.example.gabriel.mapsstarter2.services.GeolocationService;
import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity
        implements TabLayout.OnTabSelectedListener, OnDataListener {

    // Global Constants
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 201;
    private static final int SETTINGS_CODE = 202;

    // Global Fields
    protected HashSet<String> selectedEmails;
    protected LatLng origin, destination;
    protected String originAddress, destinationAddress;
    private String fragmentState = "PathFragment";
    private String tripID;
    private RxSharedPreferences rxPreferences;

    // Global UI Widgets
    private TabLayout tabLayout;
    private Fragment pathFragment, trackFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ask for Geolocation permissions if not granted
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location Permission NOT Granted yet");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
        }

        // Check if GPS is turned on
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Toast.makeText(getApplicationContext(), "GPS Required. Please turn on", Toast.LENGTH_SHORT).show();

        }

        // Check Internet Connection
        ReactiveNetwork.observeInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override public void accept(Boolean isConnectedToInternet) {
                        // do something with isConnectedToInternet value
                        if (!isConnectedToInternet){
                            Toast.makeText(getApplicationContext(), "Internet required. Please reconnect", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // Set listeners on tabs
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(this);

        // SharedPreferences
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        rxPreferences = RxSharedPreferences.create(preferences);

        // Listeners
        setRxPreferencesListeners();

        // Create Base Fragments
        pathFragment = new PathFragment();
        trackFragment = new TrackArrivalFragment();

        // Authenticate
        if (!isAuthenticated()) {
            Log.d(TAG, "Authentication is required");
            authenticate();
        } else {
            Log.d(TAG, "Already Logged In");
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Toast.makeText(this, "Welcome, " + user.getEmail(), Toast.LENGTH_SHORT).show();
        }

        // Unless we are sharing a trip, launch initial fragment
        Preference<String> pagePref = rxPreferences.getString(getString(R.string.pref_page));
        String page = pagePref.get();

        if (page.equalsIgnoreCase(getString(R.string.page_sharing))){
            launchLastPageOnTab(0);
        } else {
            launchInitialFragment();
        }
    }

    private void setRxPreferencesListeners() {
        Preference<String> origPref  = rxPreferences.getString(getString(R.string.pref_origin));
        Preference<String> destPref = rxPreferences.getString(getString(R.string.pref_destination));

        Observable<String> observOrigin = origPref.asObservable();
        observOrigin.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(String origin) {
                Log.d(TAG, "Origin: " + String.valueOf(origin));
                if (origin.isEmpty()){
                    return;
                }
                // Parse String to LatLng
                String[] latlong =  origin.split(",");
                double latitude = Double.parseDouble(latlong[0]);
                double longitude = Double.parseDouble(latlong[1]);
                LatLng originLatLng = new LatLng(latitude, longitude);

                Log.d(TAG, "Origin refactored: " + originLatLng.toString());
                // Get reference to origin Address String
                Preference<String> originStrPref = rxPreferences.getString(getString(R.string.pref_origin_str));

                // Update Textview with addressess on separate thread
                new Thread(new TranslateToAddress(originLatLng, originStrPref)).start();
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
                LatLng destLatLng = new LatLng(latitude, longitude);

                Log.d(TAG, "Destination refactored: " + destLatLng.toString());


                Preference<String> destStrPref = rxPreferences.getString(getString(R.string.pref_destination_str));

                // Update Textview with addressess on separate thread
                new Thread(new TranslateToAddress(destLatLng, destStrPref)).start();
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }

    private void launchLastPageOnTab(int tab){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = null;

        Preference<String> pagePref = rxPreferences.getString(getString(R.string.pref_page));
        String page = pagePref.get();

        if (tab == 0){
            if (page.equalsIgnoreCase(getString(R.string.page_user_select))){
                fragment = new UserSelectFragment();
            } else if (page.equalsIgnoreCase(getString(R.string.page_confirmation))){
                fragment = new ConfirmationFragment();
            } else if (page.equalsIgnoreCase(getString(R.string.page_sharing))){
                fragment = new SharingFragment();
            } else {
                fragment = pathFragment;
            }
        } else if(tab == 1){
            fragment = trackFragment;
        }

        transaction.replace(R.id.fragmentWrap, fragment);
        transaction.commit();
    }


    @Override
    public void launchFirstFragment() {
        pathFragment = new PathFragment();
        launchLastPageOnTab(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == ResultCodes.OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.d(TAG, "Sign in worked: " + user.toString());

                // Save User to DB
                saveUserToDB(user);
            } else {
                Log.e(TAG, "Sign in failed");
                Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == SETTINGS_CODE){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Log.d(TAG, "USER: " + user);

            if (user == null){
                Log.d(TAG, "Signed Out");
                // If trip in progress, cancel it
                cancelTripIfInProgress();

                // Stop Geolocation Service
                Intent intent = new Intent(getApplicationContext(), GeolocationService.class);
                stopService(intent);

                // Clear all session variables
                rxPreferences.clear();

                // Terminate App
                finish();
            }
        }
    }
    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back Pressed");
        if (getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        // Launch Last page
        launchLastPageOnTab(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        //Log.d(TAG, "onTabUnselected");
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        //Log.d(TAG, "onTabReselected");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, SETTINGS_CODE);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private boolean isAuthenticated(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            Log.d(TAG, user.toString());
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }
    private void authenticate(){
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

        // Start authentication activity
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    private void saveUserToDB(FirebaseUser user) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create User
        final User userModel = new User(user.getDisplayName(), user.getEmail());

        Query query = db.collection(getString(R.string.users_collection))
                .whereEqualTo("email", user.getEmail());

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                Log.d(TAG, "onEvent()");
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (value.isEmpty()){
                    Log.d(TAG, "Document Empty, creating user");
                    db.collection(getString(R.string.users_collection))
                            .add(userModel.getCustomHashMap())
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });
                } else {
                   Log.d(TAG, "Document not empty, no need to create user");
                }
            }
        });
    }

    private void launchInitialFragment(){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace fragment and add to back stack
        transaction.add(R.id.fragmentWrap, pathFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    private void cancelTripIfInProgress(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Preference<String> tripIDPref = rxPreferences.getString(getString(R.string.pref_trip_id));
        String tripID = tripIDPref.get();
        if (tripID == null || tripID.isEmpty()){
            Log.d(TAG, "Trip was not started");
            return;
        }
        Log.d(TAG, "Cancelling Trip: "+tripID);
        DocumentReference documentReference = db.collection("trips")
                .document(tripID);
        documentReference.update("status", Trip.CANCELED)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "Success deleting trip");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failure deleting trip");
                    }
                });

    }

    private String latlngToAddress(LatLng position){
        if (getApplicationContext() == null){
            return "";
        }
        Geocoder geocoder = new Geocoder(getApplicationContext());
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

    private class TranslateToAddress implements Runnable {
        private LatLng latLng;
        private Preference<String> pref;
        public TranslateToAddress(LatLng latLng, Preference<String> pref) {
            super();
            this.latLng = latLng;
            this.pref = pref;
        }

        @Override
        public void run() {
            // Get Address
            if (latLng == null){
                return;
            }
            String address = latlngToAddress(latLng);
            if (address != null){
                pref.set(address);
            }
        }
    }

}


