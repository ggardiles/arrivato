package com.example.gabriel.mapsstarter2.fragments.share;


import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.gabriel.mapsstarter2.interfaces.OnDataListener;
import com.example.gabriel.mapsstarter2.R;
import com.example.gabriel.mapsstarter2.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserSelectFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    // Global Constant Fields
    private static final String TAG = "UserSelectFragment";

    // Global variables
    private ArrayList<String> users;
    private HashSet<String> selectedEmails = new HashSet<>();
    private double[] origin, destination;
    private ArrayAdapter<String> adapter, autoTVAdapter;
    private OnDataListener mCallback;
    private FirebaseFirestore db;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView()");

        if (container != null) {
            container.removeAllViews();
        }

        // Set Page State in MainActivity
        mCallback.setPageState(getString(R.string.user_select));

        // Inflate the layout for this fragment
        View userSelectView = inflater.inflate(R.layout.fragment_user_select, container, false);

        // Instantiate UI Widgets
        btnSubmit = (Button) userSelectView.findViewById(R.id.btnContinue2);
        autoTvSearchUser = (AutoCompleteTextView) userSelectView.findViewById(R.id.autoTvSearchUser);
        lvUsers = (ListView) userSelectView.findViewById(R.id.lvUsers);

        // Register Listeners
        btnSubmit.setOnClickListener(this);
        autoTvSearchUser.setOnItemClickListener(this);

        // Register Adapter
        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<String>(selectedEmails));
        lvUsers.setAdapter(adapter);


        // Modify Widgets
        btnSubmit.setEnabled(false);

        // Load User List
        loadUserList();

        return userSelectView;
    }

    private void loadUserList(){
        // Get Firestore Instance
        db = FirebaseFirestore.getInstance();

        // Query Firestore for users
        db.collection(getString(R.string.users_collection))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            HashSet<String> usersSet = new HashSet<>();

                            // For every user add its email to list
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                User user = document.toObject(User.class);
                                usersSet.add(user.getEmail());
                            }

                            if (getActivity() == null){
                                return;
                            }
                            // Load AutoTextView with users email content
                            Log.d(TAG, usersSet.toString());
                            users = new ArrayList<>(usersSet);
                            autoTVAdapter = new ArrayAdapter<>(getActivity(),
                                    android.R.layout.simple_dropdown_item_1line, users);
                            autoTvSearchUser.setAdapter(autoTVAdapter);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnContinue2:
                Log.d(TAG, "Button submit pressed");
                if (selectedEmails.isEmpty()){
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Select viewers first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Pass data to activity
                mCallback.onEmailsReady(selectedEmails);

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

        // Get email
        String selected = (String) parent.getItemAtPosition(position);
        Log.d(TAG, "AutoCompleteTextview Item Selected: " + selected);
        //int pos = users.indexOf(selected);

        // Add email to Set
        selectedEmails.add(selected);

        // If users are selected enable continue to next page
        if (!selectedEmails.isEmpty()){
            btnSubmit.setEnabled(true);
        }
        // Update ListView
        adapter.clear();
        adapter.addAll(new ArrayList<String>(selectedEmails));
        adapter.notifyDataSetChanged();

        // Clear SearchBar
        autoTvSearchUser.setText("");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!selectedEmails.isEmpty()){
            btnSubmit.setEnabled(true);
        }
    }
}
