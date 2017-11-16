package com.example.gabriel.mapsstarter2;


import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.gabriel.mapsstarter2.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserSelectFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    // Global Constant Fields
    private static final String LOG_TAG = "UserSelectFragment";

    // Global variables
    private ArrayList<String> users;
    private HashSet<String> selectedUsernames = new HashSet<>();
    private double[] origin, destination;
    private ArrayAdapter<String> adapter;
    private OnDataListener mCallback;

    // UI Widgets
    private Button btnSubmit;
    private AutoCompleteTextView autoTvSearchUser;
    private ListView lvUsers;


    public UserSelectFragment() {
        // Required empty public constructor
    }

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

        View userSelectView = inflater.inflate(R.layout.fragment_user_select, container, false);

        // Instantiate UI Widgets
        btnSubmit = (Button) userSelectView.findViewById(R.id.btnContinue2);
        btnSubmit.setEnabled(false);

        autoTvSearchUser = (AutoCompleteTextView) userSelectView.findViewById(R.id.autoTvSearchUser);
        lvUsers = (ListView) userSelectView.findViewById(R.id.lvUsers);

        // Register Listeners
        btnSubmit.setOnClickListener(this);
        autoTvSearchUser.setOnItemClickListener(this);

        // Register Adapter
        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, new ArrayList<String>(selectedUsernames));
        lvUsers.setAdapter(adapter);

        // Load User List
        loadUserList();

        return userSelectView;
    }

    private void loadUserList(){
        // Get Firestore Instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query Firestore for users
        db.collection(getString(R.string.users_collection))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            users = new ArrayList<String>();

                            // For every user add its username to list
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(LOG_TAG, document.getId() + " => " + document.getData());
                                User user = document.toObject(User.class);
                                users.add(user.getUsername());
                            }

                            // Load AutoTextView with users username content
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                                    android.R.layout.simple_dropdown_item_1line, users);
                            autoTvSearchUser.setAdapter(adapter);

                        } else {
                            Log.d(LOG_TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnContinue2:
                Log.d(LOG_TAG, "Button submit pressed");
                if (selectedUsernames.isEmpty()){
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Select viewers first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Pass data to activity
                mCallback.onUsernameReady(selectedUsernames);

                // Prepare Fragment Transition
                ConfirmationFragment confirmationFragment = new ConfirmationFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                // Replace fragment and add to back stack
                transaction.replace(R.id.fragmentWrap, confirmationFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // Get Username
        String selected = (String) parent.getItemAtPosition(position);
        Log.d(LOG_TAG, "AutoCompleteTextview Item Selected: " + selected);
        //int pos = users.indexOf(selected);

        // Add Username to Set
        selectedUsernames.add(selected);

        // If users are selected enable continue to next page
        if (!selectedUsernames.isEmpty()){
            btnSubmit.setEnabled(true);
        }
        // Update ListView
        adapter.clear();
        adapter.addAll(new ArrayList<String>(selectedUsernames));
        adapter.notifyDataSetChanged();

        // Clear SearchBar
        autoTvSearchUser.setText("");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!selectedUsernames.isEmpty()){
            btnSubmit.setEnabled(true);
        }
    }
}
