package com.example.findworker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.ViewHolder> {

    List<FeatureModel> list;

    public FeatureAdapter(List<FeatureModel> list) {
        this.list = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView title, desc;

        public ViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.featureIcon);
            title = v.findViewById(R.id.featureTitle);
            desc = v.findViewById(R.id.featureDesc);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feature_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        FeatureModel f = list.get(position);

        holder.icon.setImageResource(f.icon);
        holder.title.setText(f.title);
        holder.desc.setText(f.desc);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(f);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnFeatureClick {
        void onClick(FeatureModel model);
    }

    OnFeatureClick listener;

    public FeatureAdapter(List<FeatureModel> list, OnFeatureClick listener) {
        this.list = list;
        this.listener = listener;
    }


}