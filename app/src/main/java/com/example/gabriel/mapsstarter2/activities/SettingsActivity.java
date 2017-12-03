package com.example.gabriel.mapsstarter2.activities;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.gabriel.mapsstarter2.R;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    // Global Constants
    private static final String TAG = "SettingsActivity";

    // Global Fields
    private RxSharedPreferences rxPreferences;

    // Global UI Widgets
    private Button btnSignout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize UI Widgets
        btnSignout = (Button) findViewById(R.id.btn_sign_out);
        btnSignout.setOnClickListener(this);

        // SharedPreferences
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        rxPreferences = RxSharedPreferences.create(preferences);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_sign_out:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getApplicationContext(), "Signed out",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                break;
        }
    }
}
