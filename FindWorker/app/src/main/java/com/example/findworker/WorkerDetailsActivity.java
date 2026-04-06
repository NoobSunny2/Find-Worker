package com.example.findworker;

import static com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.RatingBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class WorkerDetailsActivity extends AppCompatActivity {

    private TextView nameTextView, professionTextView, ratingTextView, locationTextView , joinDate;
    private ImageView workerImageView;
    private Button callBtn, whatsappBtn, bookBtn;
    private RatingBar ratingBar;
    private Button submitReviewBtn;
    private Toolbar workerToolbar;
    private EditText feedbackInput;
    private String USERNAME = "";
    private String MOBILE = "";
    private FusedLocationProviderClient fused;
    TextView btnReviews, btnGiveRating;
    CardView ratingCard, reviewsCard;
    RecyclerView feedbackRecycler;
    TextView txtNoFeedback;
    List<Feedback> feedbackList = new ArrayList<>();
    FeedbackAdapter adapter;
    TextInputEditText problemEditText ;
    private Worker worker;
    private String startOtp = "";
    private String completeOtp = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_details);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white)); // your light color
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // make icons dark
        }

        nameTextView = findViewById(R.id.workerName);
        professionTextView = findViewById(R.id.workerProfession);
        ratingTextView = findViewById(R.id.workerRatings);
        locationTextView = findViewById(R.id.workerLocation);
        joinDate = findViewById(R.id.workerJoinDate);
        workerImageView = findViewById(R.id.workerImage);
        callBtn = findViewById(R.id.callBtn);
        whatsappBtn = findViewById(R.id.whatsappBtn);
        bookBtn = findViewById(R.id.bookBtn);
        workerToolbar = findViewById(R.id.workerToolbar);
        feedbackInput = findViewById(R.id.feedbackInput);
        problemEditText = findViewById(R.id.problemEditText);
        ratingBar = findViewById(R.id.ratingBar);
        submitReviewBtn = findViewById(R.id.submitReviewBtn);
        feedbackInput = findViewById(R.id.feedbackInput);

        workerToolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        txtNoFeedback = findViewById(R.id.txtNoFeedback);
        feedbackRecycler = findViewById(R.id.feedbackRecycler);
        feedbackRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FeedbackAdapter(WorkerDetailsActivity.this, feedbackList);
        feedbackRecycler.setAdapter(adapter);

        reviewsCard = findViewById(R.id.reviewsCard);
        ratingCard  = findViewById(R.id.ratingCard);

        btnReviews = findViewById(R.id.btnReviews);
        btnGiveRating = findViewById(R.id.btnGiveRating);

        if (btnReviews == null || btnGiveRating == null) {
            Toast.makeText(this, "Toggle buttons not found", Toast.LENGTH_LONG).show();
            return;
        }

        // 🔹 CLICK LISTENERS
        btnReviews.setOnClickListener(v -> {
            reviewsCard.setVisibility(View.VISIBLE);
            ratingCard.setVisibility(View.GONE);

            btnReviews.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnReviews.setBackgroundResource(R.drawable.toggle_selected);

            btnGiveRating.setTextColor(Color.parseColor("#555555"));
            btnGiveRating.setBackground(null);
        });

        btnGiveRating.setOnClickListener(v -> {
            reviewsCard.setVisibility(View.GONE);
            ratingCard.setVisibility(View.VISIBLE);

            btnGiveRating.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnGiveRating.setBackgroundResource(R.drawable.toggle_selected);

            btnReviews.setTextColor(Color.parseColor("#555555"));
            btnReviews.setBackground(null);
        });


        loadUserName();

        callBtn.setEnabled(false);
        whatsappBtn.setEnabled(false);
        callBtn.setAlpha(0.4f);
        whatsappBtn.setAlpha(0.4f);


        fused = LocationServices.getFusedLocationProviderClient(this);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        worker = (Worker) getIntent().getSerializableExtra("worker");

        checkExistingRequest(worker.getId());
        loadWorkerFullData(worker.getId());
        loadFeedbacks(worker.getId());


        if (worker != null) {
            nameTextView.setText(worker.getName());
            professionTextView.setText("Profession: " + worker.getProfession());
            locationTextView.setText("\uD83D\uDCCD" + worker.getLocation());
            joinDate.setText("Joined : " + worker.getJoinDateTime());
            ratingTextView.setText(String.format("%.1f", worker.getAverageRating()));
            workerImageView.setImageResource(R.drawable.woker_image);


            callBtn.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + worker.getPhone()));
                startActivity(intent);
            });

            // WhatsApp button
            whatsappBtn.setOnClickListener(v -> {
                String phone = worker.getPhone();
                phone = phone.replaceAll("[^0-9]", "");
                String message = "Hi " + worker.getName() + ", I'm contacting you via Find Worker app.";

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String url = "https://wa.me/" + phone.replace("+", "") + "?text=" + Uri.encode(message);
                    intent.setPackage("com.whatsapp");
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show();
                }
            });

            bookBtn.setOnClickListener(v -> {

                String problem = problemEditText.getText().toString().trim();

                if (problem.isEmpty()) {
                    problemEditText.setError("Please describe your problem");
                    return;
                }

                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            100
                    );
                    return;
                }

                fused.getLastLocation().addOnSuccessListener(loc -> {

                    double userLat = 0.0;
                    double userLng = 0.0;

                    if (loc != null) {
                        userLat = loc.getLatitude();
                        userLng = loc.getLongitude();
                    }
                    if (loc == null) {
                        fused.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)
                                .addOnSuccessListener(loc2 -> {
                                    if (loc2 != null) {
                                        sendRequestToWorker(loc2.getLatitude(), loc2.getLongitude() , problem);
                                    } else {
                                        Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        return;
                    }

                    sendRequestToWorker(userLat, userLng , problem);
                });
            });



        } else {
            Toast.makeText(this, "Worker data not found!", Toast.LENGTH_SHORT).show();
            finish();
        }

        submitReviewBtn.setOnClickListener(v -> {

            float userRating = ratingBar.getRating();
            String feedbackText = feedbackInput.getText().toString().trim();

            if (userRating == 0) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            if (feedbackText.isEmpty()) {
                Toast.makeText(this, "Please write feedback", Toast.LENGTH_SHORT).show();
                return;
            }

            String workerId = worker.getId();
            DatabaseReference workerRef = database.getReference("workers").child(workerId);

            // 1️⃣ Update Rating
            workerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    double currentAvg = 0.0;
                    long currentCount = 0;

                    if (snapshot.exists()) {
                        if (snapshot.child("averageRating").getValue() != null) {
                            currentAvg = snapshot.child("averageRating").getValue(Double.class);
                        }
                        if (snapshot.child("ratingCount").getValue() != null) {
                            currentCount = snapshot.child("ratingCount").getValue(Long.class);
                        }
                    }

                    double newAverage = ((currentAvg * currentCount) + userRating) / (currentCount + 1);
                    long newCount = currentCount + 1;

                    workerRef.child("averageRating").setValue(newAverage);
                    workerRef.child("ratingCount").setValue(newCount);

                    // 2️⃣ Store Feedback inside worker → feedback node
                    DatabaseReference feedbackRef = workerRef.child("feedbacks");

                    String feedbackId = feedbackRef.push().getKey();

                    Map<String, Object> feedbackData = new HashMap<>();
                    feedbackData.put("userName", USERNAME);
                    feedbackData.put("rating", userRating);
                    feedbackData.put("feedback", feedbackText);
                    feedbackData.put("time", System.currentTimeMillis());

                    feedbackRef.child(feedbackId).setValue(feedbackData)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(WorkerDetailsActivity.this,
                                        "Review Submitted Successfully!", Toast.LENGTH_SHORT).show();

                                ratingBar.setRating(0);
                                feedbackInput.setText("");
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(WorkerDetailsActivity.this,
                                            "Failed to upload feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(WorkerDetailsActivity.this,
                            "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void sendRequestToWorker(double userLat, double userLng, String problem) {

        worker = (Worker) getIntent().getSerializableExtra("worker");

        if (worker == null) {
            Toast.makeText(this, "Worker not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        String workerId = worker.getId();
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) {
            Toast.makeText(this, "Please login first!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (USERNAME == null || USERNAME.isEmpty()) {
            USERNAME = "User";
        }

        if (MOBILE == null || MOBILE.isEmpty()) {
            MOBILE = "No Phone";
        }

        DatabaseReference reqRef = FirebaseDatabase.getInstance()
                .getReference("requests")
                .push();

        String requestId = reqRef.getKey();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", requestId);
        requestMap.put("workerId", workerId);
        requestMap.put("userId", userId);
        requestMap.put("userLat", userLat);
        requestMap.put("userPhone", MOBILE);
        requestMap.put("userLng", userLng);
        requestMap.put("userName", USERNAME);
        requestMap.put("status", "pending");
        requestMap.put("workerName", worker.getName());
        requestMap.put("timestamp", System.currentTimeMillis());
        requestMap.put("problem" , problem);
        requestMap.put("startOtp", startOtp);
        requestMap.put("completeOtp", completeOtp);

        reqRef.setValue(requestMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Request Sent Successfully!", Toast.LENGTH_SHORT).show();

                    notifyWorker(workerId, USERNAME);
                    // Update UI
                    bookBtn.setText("Pending...");
                    bookBtn.setEnabled(false);

                    listenForRequestStatus(requestId);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );

    }


    private void notifyWorker(String workerId, String userName) {

        FirebaseDatabase.getInstance()
                .getReference("workers")
                .child(workerId)
                .child("fcmToken")
                .get()
                .addOnSuccessListener(snapshot -> {

                    String token = snapshot.getValue(String.class);

                    if (token == null) return;

                    // 🔥 CALL YOUR CLOUD FUNCTION HERE
                    FirebaseDatabase.getInstance()
                            .getReference("notifications")
                            .push()
                            .setValue(new HashMap<String, Object>() {{
                                put("token", token);
                                put("title", "New Service Request");
                                put("body", userName + " sent you a request");
                            }});
                });
    }


    private void listenForRequestStatus(String requestId) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("requests")
                .child(requestId);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snap) {

                if (!snap.exists()) return;

                String status = snap.child("status").getValue(String.class);

                if (status == null) return;

                switch (status) {

                    case "pending":
                        bookBtn.setText("Pending...");
                        bookBtn.setEnabled(false);
                        break;

                    case "accepted":
                        bookBtn.setText("Accepted");
                        bookBtn.setEnabled(false);
                        callBtn.setEnabled(true);
                        whatsappBtn.setEnabled(true);
                        callBtn.setAlpha(1f);
                        whatsappBtn.setAlpha(1f);
                        break;

                    case "started":
                        bookBtn.setText("Started");
                        bookBtn.setEnabled(false);
                        callBtn.setEnabled(true);
                        whatsappBtn.setEnabled(true);
                        callBtn.setAlpha(1f);
                        whatsappBtn.setAlpha(1f);// LOCK BUTTON
                        break;

                    case "reached":
                        bookBtn.setText("Reached");
                        bookBtn.setEnabled(false);
                        callBtn.setEnabled(true);
                        whatsappBtn.setEnabled(true);
                        callBtn.setAlpha(1f);
                        whatsappBtn.setAlpha(1f);
                        break;

                    case "rejected":
                        bookBtn.setText("Rejected");
                        bookBtn.setEnabled(false);
                        break;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void loadUserName() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        ref.get().addOnSuccessListener(snap -> {

            USERNAME = snap.child("name").getValue(String.class);
            MOBILE = snap.child("phone").getValue(String.class);
            startOtp = snap.child("startOtp").getValue(String.class);
            completeOtp = snap.child("completeOtp").getValue(String.class);
            if (USERNAME == null) USERNAME = "User";
            if (MOBILE == null) MOBILE = "No Number";

        });
    }


    private void checkExistingRequest(String workerId) {

        String userId = FirebaseAuth.getInstance().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("requests");

        ref.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        boolean canBook = true;

                        for (DataSnapshot req : snapshot.getChildren()) {

                            String wId = req.child("workerId").getValue(String.class);

                            if (wId != null && wId.equals(workerId)) {

                                String status = req.child("status").getValue(String.class);
                                String reqId = req.child("requestId").getValue(String.class);

                                if (status == null) status = "pending";

                                switch (status) {

                                    case "pending":
                                        bookBtn.setText("Pending...");
                                        bookBtn.setEnabled(false);
                                        canBook = false;
                                        break;

                                    case "accepted":
                                        bookBtn.setText("Accepted");
                                        bookBtn.setEnabled(false);
                                        canBook = false;
                                        break;

                                    case "started":
                                        bookBtn.setText("Started");
                                        bookBtn.setEnabled(false);
                                        canBook = false;
                                        break;

                                    case "reached":
                                        bookBtn.setText("Reached");
                                        bookBtn.setEnabled(false);
                                        canBook = false;
                                        break;

                                    case "rejected":
                                        bookBtn.setText("Book Again");
                                        bookBtn.setEnabled(true);
                                        canBook = true;
                                        break;
                                }

                                listenForRequestStatus(reqId);
                                return;
                            }
                        }

                        if (canBook) {
                            bookBtn.setText("Book Now");
                            bookBtn.setEnabled(true);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }


    private void loadWorkerFullData(String workerId) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("workers")
                .child(workerId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    Toast.makeText(WorkerDetailsActivity.this, "Worker not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 🔥 Load Worker Object
                worker = snapshot.getValue(Worker.class);

                if (worker == null) return;

                nameTextView.setText(worker.getName());
                professionTextView.setText(worker.getProfession());
                locationTextView.setText("📍 " + worker.getLocation());
                joinDate.setText("Joined: " + worker.getJoinDateTime());
                ratingTextView.setText(String.format("%.1f", worker.getAverageRating()));

                feedbackList.clear();

                DataSnapshot feedbackSnap = snapshot.child("feedbacks");

                for (DataSnapshot f : feedbackSnap.getChildren()) {
                    Feedback feedback = f.getValue(Feedback.class);
                    if (feedback != null) feedbackList.add(feedback);
                }

                if (feedbackList.isEmpty()) {
                    txtNoFeedback.setVisibility(View.VISIBLE);
                } else {
                    txtNoFeedback.setVisibility(View.GONE);
                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadFeedbacks(String workerId) {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("workers")
                .child(workerId)
                .child("feedbacks");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (txtNoFeedback == null || feedbackRecycler == null) return;

                feedbackList.clear();

                if (!snapshot.exists()) {
                    txtNoFeedback.setVisibility(View.VISIBLE);
                    feedbackRecycler.setVisibility(View.GONE);
                    return;
                }

                for (DataSnapshot s : snapshot.getChildren()) {
                    Feedback f = s.getValue(Feedback.class);
                    if (f != null) feedbackList.add(f);
                }

                if (feedbackList.isEmpty()) {
                    txtNoFeedback.setVisibility(View.VISIBLE);
                    feedbackRecycler.setVisibility(View.GONE);
                } else {
                    txtNoFeedback.setVisibility(View.GONE);
                    feedbackRecycler.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }






}
