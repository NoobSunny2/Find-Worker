package com.example.findworker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> implements android.widget.Filterable {


    private List<String> locationList;
    private OnLocationClickListener listener;
    private List<String> locationListFull;

    public interface OnLocationClickListener {
        void onLocationClick(String location);
    }

    public LocationAdapter(List<String> locationList, OnLocationClickListener listener) {
        this.locationList = locationList;
        this.locationListFull = new ArrayList<>(locationList);
        Collections.sort(locationListFull);
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        String location = locationList.get(position);
        holder.textView.setText(location);
        holder.itemView.setOnClickListener(v -> listener.onLocationClick(location));
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    @Override
    public Filter getFilter() {
        return locationFilter;
    }

    private Filter locationFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<String> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(locationListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (String location : locationListFull) {
                    if (location.toLowerCase().contains(filterPattern)) {
                        filteredList.add(location);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }


        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            locationList.clear();
            if (results.values != null) {
                locationList.addAll((List<String>) results.values);
            }
            Collections.sort(locationList);
            notifyDataSetChanged();
        }

    };


    public void updateData(List<String> newLocations) {
        Collections.sort(newLocations);
        locationList.clear();
        locationList.addAll(newLocations);

        locationListFull.clear();
        locationListFull.addAll(newLocations);

        notifyDataSetChanged();
    }


    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.location_text);
        }
    }
}

