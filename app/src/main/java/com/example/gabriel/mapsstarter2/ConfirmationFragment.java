package com.example.gabriel.mapsstarter2;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;


public class ConfirmationFragment extends Fragment {

    // Global Constant fields
    private static final String TAG = "ConfirmationFragment";

    // Global Fields
    private OnDataListener mCallback;
    private ArrayAdapter<String> adapter;

    // UI Widgets
    private ListView lvViewers;

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

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_confirmation, container, false);

        // Initialize UI Widgets
        lvViewers = (ListView) v.findViewById(R.id.lvViewers);
        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line);
        lvViewers.setAdapter(adapter);

        // Ask Main Activity for Confirmation Data
        mCallback.getConfirmationData();

        return v;
    }

    public void onConfirmationData(LatLng origin, LatLng destination, HashSet<String> usernames){
        Log.d(TAG, new ArrayList<String>(usernames).toString());

        //TODO: Set Origin and Destination to TextView (Maybe change to addresses???)

        // Load usernames to ListView
        adapter.clear();
        adapter.addAll(new ArrayList<String>(usernames));
        adapter.notifyDataSetChanged();

    }

}
