package com.example.gabriel.mapsstarter2.fragments.track;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gabriel.mapsstarter2.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class TrackArrivalFragment extends Fragment {


    public TrackArrivalFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_track_arrival, container, false);

        return v;
    }

}
