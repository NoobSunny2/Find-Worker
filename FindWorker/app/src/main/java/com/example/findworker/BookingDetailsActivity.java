package com.example.findworker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingDetailsActivity extends AppCompatActivity {

    private TextView workerNameText, statusText, dateText;
    private TextView acceptedStep, startedStep, reachedStep, completedStep;
    private ProgressBar loadingProgress;

    private DatabaseReference trackingRef;
    private ValueEventListener trackingListener;
    private View circleAccepted, circleStarted, circleReached, circleCompleted;
    private View lineAccepted, lineStarted, lineReached;
    private TextView otpText , completeOtpText;
    private double userLat, userLng;

    private TextView distanceText, lastUpdatedText;

    private CardView reviewCard;
    private ImageView workImageView;
    private Button uploadImageBtn, submitReviewBtn;
    private RatingBar ratingBar;
    private EditText feedbackInput;

    private String requestId;
    private String workerId;
    private boolean reviewSubmitted = false;
    private static String USERNAME = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        requestId = getIntent().getStringExtra("requestId");
        if (requestId == null || requestId.isEmpty()) {
            Toast.makeText(this, "Invalid booking", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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


        initViews();
        loadBookingDetails();

        uploadImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 101);
        });

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

            if (workerId == null) {
                Toast.makeText(this, "Worker not found", Toast.LENGTH_SHORT).show();
                return;
            }

            saveFeedbackLikeWorkerDetails(workerId, userRating, feedbackText);
        });

        circleAccepted = findViewById(R.id.circleAccepted);
        circleStarted = findViewById(R.id.circleStarted);
        circleReached = findViewById(R.id.circleReached);
        circleCompleted = findViewById(R.id.circleCompleted);

        lineAccepted = findViewById(R.id.lineAccepted);
        lineStarted = findViewById(R.id.lineStarted);
        lineReached = findViewById(R.id.lineReached);


    }

    private void initViews() {

        workerNameText = findViewById(R.id.workerNameText);
        statusText = findViewById(R.id.statusText);
        dateText = findViewById(R.id.dateText);

        acceptedStep = findViewById(R.id.stepAccepted);
        startedStep = findViewById(R.id.stepStarted);
        reachedStep = findViewById(R.id.stepReached);
        completedStep = findViewById(R.id.stepCompleted);

        distanceText = findViewById(R.id.distanceText);
        lastUpdatedText = findViewById(R.id.lastUpdatedText);

        loadingProgress = findViewById(R.id.loadingProgress);

        reviewCard = findViewById(R.id.reviewCard);
        workImageView = findViewById(R.id.workImageView);
        uploadImageBtn = findViewById(R.id.uploadImageBtn);
        ratingBar = findViewById(R.id.ratingBar);
        feedbackInput = findViewById(R.id.feedbackInput);
        submitReviewBtn = findViewById(R.id.submitReviewBtn);
        otpText = findViewById(R.id.otpText);
        completeOtpText = findViewById(R.id.completeOtpText);

        loadingProgress.setVisibility(View.VISIBLE);
        reviewCard.setVisibility(View.GONE);
    }

    private void loadBookingDetails() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("requests")
                .child(requestId);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                loadingProgress.setVisibility(View.GONE);

                if (!snapshot.exists()) {
                    Toast.makeText(BookingDetailsActivity.this,
                            "Booking not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                workerId = snapshot.child("workerId").getValue(String.class);
                String workerName = snapshot.child("workerName").getValue(String.class);
                String status = snapshot.child("status").getValue(String.class);
                Long timestamp = snapshot.child("timestamp").getValue(Long.class);
                USERNAME = snapshot.child("userName").getValue(String.class);
                String startOtp = snapshot.child("startOtp").getValue(String.class);
                String completeOtp = snapshot.child("completeOtp").getValue(String.class);
                workerNameText.setText(
                        workerName != null ? workerName : "Assigned Worker"
                );

                updateStatusUI(status, startOtp, completeOtp);;

                if (timestamp != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a", Locale.getDefault()
                    );
                    dateText.setText("Booked on " + sdf.format(new Date(timestamp)));
                }

                Double lat = snapshot.child("userLat").getValue(Double.class);
                Double lng = snapshot.child("userLng").getValue(Double.class);

                if (lat != null && lng != null) {
                    userLat = lat;
                    userLng = lng;
                    startTrackingWorker();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(BookingDetailsActivity.this,
                        "Failed to load booking details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatusUI(String status, String startOtp, String completeOtp) {

        resetAllSteps();
        if (status == null) return;

        status = status.toUpperCase();
        statusText.setText(status);

        switch (status) {

            case "PENDING":
                setStatusStyle("#FF9800", R.drawable.bg_status_pending);
                break;

            case "ACCEPTED":
                highlightSteps(acceptedStep);

                setCircleActive(circleAccepted);
                animateCircle(circleAccepted);

                setStatusStyle("#4CAF50", R.drawable.bg_status_active);
                break;

            case "STARTED":

                otpText.setVisibility(View.VISIBLE);
                otpText.setText("Tell this OTP to worker: " + startOtp);


                highlightSteps(acceptedStep, startedStep);

                setCircleActive(circleAccepted);
                animateWithDelay(circleAccepted, 0);

                setLineActive(lineAccepted);
                animateLine(lineAccepted);

                setCircleActive(circleStarted);
                animateWithDelay(circleStarted, 150);

                setStatusStyle("#4CAF50", R.drawable.bg_status_active);
                break;

            case "REACHED":

                completeOtpText.setVisibility(View.VISIBLE);
                completeOtpText.setText("Completion OTP: " + completeOtp);

                highlightSteps(acceptedStep, startedStep, reachedStep);

                setCircleActive(circleAccepted);
                animateWithDelay(circleAccepted, 0);

                setLineActive(lineAccepted);
                animateLine(lineAccepted);

                setCircleActive(circleStarted);
                animateWithDelay(circleStarted, 150);

                setLineActive(lineStarted);
                animateLine(lineStarted);

                setCircleActive(circleReached);
                animateWithDelay(circleReached, 300);

                setStatusStyle("#388E3C", R.drawable.bg_status_reached);
                break;

            case "COMPLETED":

                highlightSteps(acceptedStep, startedStep, reachedStep, completedStep);

                setCircleActive(circleAccepted);
                animateWithDelay(circleAccepted, 0);

                setLineActive(lineAccepted);
                animateLine(lineAccepted);

                setCircleActive(circleStarted);
                animateWithDelay(circleStarted, 150);

                setLineActive(lineStarted);
                animateLine(lineStarted);

                setCircleActive(circleReached);
                animateWithDelay(circleReached, 300);

                setLineActive(lineReached);
                animateLine(lineReached);

                setCircleActive(circleCompleted);
                animateWithDelay(circleCompleted, 450);

                setStatusStyle("#0288D1", R.drawable.bg_status_completed);
                reviewCard.setVisibility(View.VISIBLE);
                break;

            case "REJECTED":
                setStatusStyle("#D32F2F", R.drawable.bg_status_rejected);
                break;
        }
    }


    private void setCircleActive(View circle) {
        circle.setBackgroundResource(R.drawable.ic_circle_green);
    }

    private void setCircleInactive(View circle) {
        circle.setBackgroundResource(R.drawable.ic_circle_grey);
    }

    private void setLineActive(View line) {
        line.setBackgroundColor(Color.parseColor("#4CAF50"));
    }

    private void setLineInactive(View line) {
        line.setBackgroundColor(Color.parseColor("#BDBDBD"));
    }


    private void setStatusStyle(String color, int background) {
        statusText.setTextColor(Color.parseColor(color));
        statusText.setBackgroundResource(background);
    }

    private void resetAllSteps() {

        // Reset text
        resetStep(acceptedStep);
        resetStep(startedStep);
        resetStep(reachedStep);
        resetStep(completedStep);

        // Reset circles
        setCircleInactive(circleAccepted);
        setCircleInactive(circleStarted);
        setCircleInactive(circleReached);
        setCircleInactive(circleCompleted);

        // Reset lines
        setLineInactive(lineAccepted);
        setLineInactive(lineStarted);
        setLineInactive(lineReached);
    }


    private void resetStep(TextView step) {
        step.setTextColor(Color.parseColor("#9E9E9E"));
        step.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_circle_grey, 0, 0, 0
        );
    }

    private void highlightSteps(TextView... steps) {
        for (TextView step : steps) {
            step.setTextColor(Color.parseColor("#4CAF50"));
            step.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_circle_green, 0, 0, 0
            );
        }
    }

    private void startTrackingWorker() {

        trackingRef = FirebaseDatabase.getInstance()
                .getReference("tracking")
                .child(requestId);

        trackingListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    distanceText.setText("Worker tracking stopped");
                    return;
                }

                Double workerLat = snapshot.child("workerLat").getValue(Double.class);
                Double workerLng = snapshot.child("workerLng").getValue(Double.class);
                Long updatedAt = snapshot.child("updatedAt").getValue(Long.class);

                if (workerLat == null || workerLng == null) return;

                float[] result = new float[1];
                android.location.Location.distanceBetween(
                        workerLat, workerLng, userLat, userLng, result
                );

                distanceText.setText(
                        String.format(Locale.getDefault(),
                                "Worker is %.2f km away", result[0] / 1000f)
                );

                if (updatedAt != null) {
                    String time = android.text.format.DateFormat
                            .format("hh:mm a", updatedAt).toString();
                    lastUpdatedText.setText("Last updated: " + time);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookingDetailsActivity.this,
                        "Tracking error", Toast.LENGTH_SHORT).show();
            }
        };

        trackingRef.addValueEventListener(trackingListener);
    }

    private void saveFeedbackLikeWorkerDetails(
            String workerId,
            float userRating,
            String feedbackText
    ) {

        DatabaseReference workerRef = FirebaseDatabase.getInstance()
                .getReference("workers")
                .child(workerId);

        workerRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                double currentAvg = 0.0;
                long currentCount = 0;

                if (snapshot.exists()) {

                    Double avg = snapshot.child("averageRating").getValue(Double.class);
                    Long count = snapshot.child("ratingCount").getValue(Long.class);

                    if (avg != null) currentAvg = avg;
                    if (count != null) currentCount = count;
                }

                double newAverage =
                        ((currentAvg * currentCount) + userRating) / (currentCount + 1);
                long newCount = currentCount + 1;

                // 1️⃣ Update rating
                workerRef.child("averageRating").setValue(newAverage);
                workerRef.child("ratingCount").setValue(newCount);

                // 2️⃣ Save feedback under worker → feedbacks
                DatabaseReference feedbackRef = workerRef
                        .child("feedbacks")
                        .child(requestId); // one feedback per booking

                feedbackRef.child("userName").setValue(USERNAME);
                feedbackRef.child("rating").setValue(userRating);
                feedbackRef.child("feedback").setValue(feedbackText);
                feedbackRef.child("time").setValue(System.currentTimeMillis());

                // 3️⃣ Update UI
                submitReviewBtn.setEnabled(false);
                submitReviewBtn.setText("Reviewed ✔");
                ratingBar.setIsIndicator(true);
                feedbackInput.setEnabled(false);

                Toast.makeText(BookingDetailsActivity.this,
                        "Review submitted successfully!",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookingDetailsActivity.this,
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void animateCircle(View view) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setAlpha(0f);

        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(250)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }

    private void animateLine(View line) {
        line.setScaleX(0f);
        line.setPivotX(0f); // animate from left

        line.animate()
                .scaleX(1f)
                .setDuration(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }

    private void animateWithDelay(View view, long delay) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setAlpha(0f);

        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setStartDelay(delay)
                .setDuration(250)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }



}
