package com.example.gabriel.mapsstarter2.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.gabriel.mapsstarter2.Manifest;
import com.example.gabriel.mapsstarter2.R;
import com.example.gabriel.mapsstarter2.fragments.share.ConfirmationFragment;
import com.example.gabriel.mapsstarter2.fragments.share.PathFragment;
import com.example.gabriel.mapsstarter2.fragments.share.SharingFragment;
import com.example.gabriel.mapsstarter2.fragments.track.TrackArrivalFragment;
import com.example.gabriel.mapsstarter2.fragments.share.UserSelectFragment;
import com.example.gabriel.mapsstarter2.interfaces.OnDataListener;
import com.example.gabriel.mapsstarter2.models.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, OnDataListener {

    // Global Constants
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 201;

    // Global Fields
    protected HashSet<String> selectedEmails;
    protected LatLng origin, destination;
    protected String originAddress, destinationAddress;
    private String fragmentState = "PathFragment";
    private String tripID;

    private Fragment pathFragment, trackFragment;

    // Global UI Widgets
    private TabLayout tabLayout;

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

        // Set listeners on tabs
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(this);

        authenticate();
        /*
        if (isAuthenticated()) {

        } else {
            Log.d(TAG, "Already Logged In");
            launchInitialFragment();
        }*/


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
                Log.e(TAG, "Sign in worked: " + user.toString());

                // Save User to DB
                saveUserToDB(user);

                // Go to Initial Page
                launchInitialFragment();
            } else {
                Log.e(TAG, "Sign in failed");
                Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        //Log.d(TAG, String.valueOf(tab.getPosition()));
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = null;

/*        for(int i = 0; i < getFragmentManager().getBackStackEntryCount(); ++i) {
            getFragmentManager().popBackStack();
        }*/
        if (tab.getPosition() == 0){
            if (fragmentState.equalsIgnoreCase(getString(R.string.user_select))){
                fragment = new UserSelectFragment();
            } else if (fragmentState.equalsIgnoreCase(getString(R.string.confirmation))){
                fragment = new ConfirmationFragment();
            } else if (fragmentState.equalsIgnoreCase(getString(R.string.sharing))){
                fragment = new SharingFragment();
            } else {
                fragment = pathFragment;
            }
        } else if(tab.getPosition() == 1){
            fragment = trackFragment;//new TrackArrivalFragment();
        }

        transaction.replace(R.id.fragmentWrap, fragment);
        //transaction.addToBackStack(null);
        transaction.commit();
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
    public void onLocationReady(LatLng origin, LatLng destination) {
        Log.d(TAG, "onLocationReady: " + String.valueOf(origin) + ", " + destination.toString());
        this.origin = origin;
        this.destination = destination;

    }

    @Override
    public void onEmailsReady(HashSet<String> emails) {
        Log.d(TAG, "onUsernameReady: " + String.valueOf(selectedEmails));
        this.selectedEmails = emails;
    }

    @Override
    public void setPageState(String fragmentName) {
        Log.d(TAG, "setPageState: " + fragmentName);
        this.fragmentState = fragmentName;
    }

    @Override
    public void setStrAddresses(String origin, String destination) {
        Log.d(TAG, "setStrAddresses()");
        this.originAddress = origin;
        this.destinationAddress = destination;
    }

    @Override
    public void setTripID(String id) {
        Log.d(TAG, "setTripID: " + id);
        this.tripID = id;
    }

    @Override
    public void getConfirmationData() {
        Log.d(TAG, "getConfirmationData: " + selectedEmails.toString());
        ConfirmationFragment frag = (ConfirmationFragment)
                getFragmentManager().findFragmentById(R.id.fragmentWrap);

        if (frag != null){
            frag.onConfirmationData(origin, destination, selectedEmails);
        }
    }

    @Override
    public void getSharingData() {
        Log.d(TAG, "getSharingData()");
        SharingFragment frag = (SharingFragment)
                getFragmentManager().findFragmentById(R.id.fragmentWrap);

        if (frag != null){
            frag.onSharingData(tripID, destinationAddress);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
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
//                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create User
        User userModel = new User(user.getDisplayName(), user.getEmail());

        // Query Firestore for users
        db.collection(getString(R.string.users_collection))
                .add(userModel.getCustomHashMap())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void launchInitialFragment(){
        pathFragment = new PathFragment();// new SharingFragment();
        trackFragment = new TrackArrivalFragment();
        //Fragment tabFragment = new UserSelectFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace fragment and add to back stack
        transaction.add(R.id.fragmentWrap, pathFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
}


