package com.example.gabriel.mapsstarter2;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class SharingFragment extends Fragment  implements View.OnClickListener{

    // UI Widgets
    private Button btnCancel;
    private TextView tvArrivalTime, tvDestination;

    public SharingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sharing, container, false);

        // Reference to UI Widgets
        btnCancel     = (Button) v.findViewById(R.id.btnCancel);
        tvArrivalTime = (TextView) v.findViewById(R.id.tvArrivalTime);
        tvDestination = (TextView) v.findViewById(R.id.tvDestinationFinal);

        // Register Listeners
        btnCancel.setOnClickListener(this);

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
}
