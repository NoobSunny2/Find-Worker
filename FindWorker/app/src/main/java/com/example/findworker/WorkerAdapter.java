package com.example.findworker;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder> {

    private ArrayList<Worker> workerList;

    public WorkerAdapter(ArrayList<Worker> workerList) {
        this.workerList = workerList;
    }
    @NonNull
    @Override
    public WorkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.worker_item, parent, false);
        return new WorkerViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull WorkerViewHolder holder, int position) {
        Worker worker = workerList.get(position);
        holder.name.setText(worker.getName());
        holder.profession.setText(worker.getProfession());
        holder.ratingText.setText(String.format("%.1f", worker.getAverageRating()));
        holder.distanceTextView.setText(String.format("%.2f km away", worker.getDistanceFromUser()));

        if (worker.isFavorite()) {
            holder.favoriteButton.setImageResource(R.drawable.ic_favorite);
            holder.favoriteButton.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150)
                    .withEndAction(() -> holder.favoriteButton.animate().scaleX(1f).scaleY(1f).setDuration(150));
        } else {
            holder.favoriteButton.setImageResource(R.drawable.ic_favorite_border);
            holder.favoriteButton.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150)
                    .withEndAction(() -> holder.favoriteButton.animate().scaleX(1f).scaleY(1f).setDuration(150));
        }

        holder.favoriteButton.setOnClickListener(v -> {
            boolean newFavoriteStatus = !worker.isFavorite();
            worker.setFavorite(newFavoriteStatus);

            if (newFavoriteStatus) {
                holder.favoriteButton.setImageResource(R.drawable.ic_favorite);
            } else {
                holder.favoriteButton.setImageResource(R.drawable.ic_favorite_border);
            }

            updateFavoriteInUser(worker.getId(), newFavoriteStatus);

        });

        String profession = worker.getProfession() != null
                ? worker.getProfession().toLowerCase()
                : "";

        if (profession.contains("plumber")) {
            holder.profileImageView.setImageResource(R.drawable.ic_plumber);
        } else if (profession.contains("mason") || profession.contains("construction")) {
            holder.profileImageView.setImageResource(R.drawable.ic_construction);
        } else if (profession.contains("barber")) {
            holder.profileImageView.setImageResource(R.drawable.ic_barber);
        } else if (profession.contains("tailor")) {
            holder.profileImageView.setImageResource(R.drawable.ic_tailor);
        } else if (profession.contains("driver") || profession.contains("auto") || profession.contains("taxi") || profession.contains("truck")) {
            holder.profileImageView.setImageResource(R.drawable.ic_driver);
        } else if (profession.contains("electrician")) {
            holder.profileImageView.setImageResource(R.drawable.ic_electrician);
        } else if (profession.contains("carpenter")) {
            holder.profileImageView.setImageResource(R.drawable.ic_carpenter);
        } else if (profession.contains("painter")) {
            holder.profileImageView.setImageResource(R.drawable.ic_painter);
        } else {
            holder.profileImageView.setImageResource(R.drawable.woker_image); // default icon
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), WorkerDetailsActivity.class);
            intent.putExtra("worker", worker);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return workerList.size();
    }

    public static class WorkerViewHolder extends RecyclerView.ViewHolder {

        TextView name, profession, distanceTextView , ratingText;
        ImageButton favoriteButton;
        ImageView profileImageView;

        public WorkerViewHolder(View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            name = itemView.findViewById(R.id.nameTextView);
            profession = itemView.findViewById(R.id.professionTextView);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            ratingText = itemView.findViewById(R.id.ratingText);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
        }
    }

    private void updateFavoriteInUser(String workerUid, boolean isFavorite) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference favRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUserId)
                .child("favorites")
                .child(workerUid);

        if (isFavorite) {
            favRef.setValue(true);
        } else {
            favRef.removeValue();
        }
    }

    public void updateList(List<Worker> newList) {
        workerList.clear();
        workerList.addAll(newList);
        notifyDataSetChanged();
    }



}
