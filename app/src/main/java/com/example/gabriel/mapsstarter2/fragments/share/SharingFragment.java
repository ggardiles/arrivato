package com.example.gabriel.mapsstarter2.fragments.share;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gabriel.mapsstarter2.R;
import com.example.gabriel.mapsstarter2.activities.OnDataListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class SharingFragment extends Fragment  implements View.OnClickListener{

    // Global Constant Fields
    private static final String TAG = "SharingFragment";

    // Global Fields
    private OnDataListener mCallback;

    // UI Widgets
    private Button btnCancel;
    private TextView tvArrivalTime, tvDestination;
    private String tripID;
    private String destinationAddress;

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

        // Register Listeners
        btnCancel.setOnClickListener(this);

        // Get Data from Main Activity
        mCallback.getSharingData();
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnCancel:
                Toast.makeText(getActivity(), "CANCEL TO BE IMPl", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    public void onSharingData(String tripID, String destination){
        Log.d(TAG, "onSharingData() TripID: " + tripID);
        this.tripID = tripID;
        this.destinationAddress = destination;
        tvDestination.setText(destination);
    }
}
