package com.example.gabriel.mapsstarter2.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gabriel.mapsstarter2.R;
import com.example.gabriel.mapsstarter2.models.Trip;

import java.util.ArrayList;

/**
 * Created by gabriel on 25/11/17.
 */

public class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.TripViewHolder> {

    ArrayList<Trip> trips;

    public TripsAdapter(ArrayList<Trip> trips) {
        this.trips = trips;
    }

    @Override
    public TripViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);

        TripViewHolder tvh = new TripViewHolder(v);
        return tvh;
    }

    @Override
    public void onBindViewHolder(TripViewHolder tvh, int position) {
        Trip trip = trips.get(position);
        tvh.tvUsername.setText("@"+trip.getTraveler());
        tvh.tvDestination.setText(trip.getDestinationStr());
        tvh.tvTTD.setText(trip.getTTD());
        tvh.tvTTD.setTextIsSelectable(true);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView tvUsername;
        TextView tvDestination;
        TextView tvTTD;

        public TripViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            tvUsername = (TextView) itemView.findViewById(R.id.tv_username);
            tvDestination = (TextView) itemView.findViewById(R.id.tv_destination);
            tvTTD = (TextView) itemView.findViewById(R.id.tv_ttd);

        }
    }
}
