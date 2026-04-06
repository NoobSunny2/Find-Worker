package com.example.findworker;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavoritesActivity extends AppCompatActivity {
    private ProgressBar loading_spinner;
    RecyclerView recyclerView;
    WorkerAdapter adapter;
    TextView noFavoritesText;
    ArrayList<Worker> favoriteWorkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white)); // Replace with your color
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        loading_spinner = findViewById(R.id.loading_spinner);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
        noFavoritesText = findViewById(R.id.textNoFavorites);
        recyclerView = findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new WorkerAdapter(favoriteWorkers);
        recyclerView.setAdapter(adapter);

        loadFavoriteWorkersFromFirebase();
    }

    private void loadFavoriteWorkersFromFirebase() {
        loading_spinner.setVisibility(View.VISIBLE);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference favRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUserId)
                .child("favorites");

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteWorkers.clear();
                if (!snapshot.exists()) {
                    loading_spinner.setVisibility(View.GONE);
                    noFavoritesText.setVisibility(View.VISIBLE);
                    Toast.makeText(FavoritesActivity.this, "No favorite workers found", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot favSnapshot : snapshot.getChildren()) {
                    String workerId = favSnapshot.getKey();
                    fetchWorkerDetails(workerId);
                }
                loading_spinner.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loading_spinner.setVisibility(View.GONE);
                Toast.makeText(FavoritesActivity.this, "Failed to load favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchWorkerDetails(String workerId) {
        DatabaseReference workerRef = FirebaseDatabase.getInstance()
                .getReference("workers")
                .child(workerId);

        workerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Worker worker = snapshot.getValue(Worker.class);
                if (worker != null) {
                    // Mark as favorite because this worker is from favorites node
                    worker.setFavorite(true);
                    favoriteWorkers.add(worker);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optionally handle errors
            }
        });
    }
}
