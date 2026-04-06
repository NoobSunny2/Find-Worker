package com.example.findworker;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import android.speech.tts.TextToSpeech;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


import android.Manifest;

public class Homepage extends AppCompatActivity {
    private static final double INVALID_COORDINATE = 0.0;
    private Toolbar toolbar;
    private ProgressBar loadingSpinner;
    private RecyclerView recyclerView;
    private WorkerAdapter workerAdapter;
    private ArrayList<Worker> workerList;
    private ArrayList<Worker> filteredWorkerList;
    private EditText searchView;
    private TextView locationText, emptyView, small_location, listeningText;
    private SharedPreferences preferences;
    private static final String PREFS_NAME = "MyAppPrefs";
    static final String LOCATION_KEY = "user_location";
    private ImageView btnMic;
    private static final int VOICE_INPUT_CODE = 100;
    private FusedLocationProviderClient fusedLocationClient;

    private TextToSpeech tts;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;
    private FloatingActionButton fab;
    private View aiOverlay, pulseRing;
    private String USERNAME = "";

    SpeechRecognizer speechRecognizer;
    Intent speechIntent;

    private boolean isAwaitingConfirmation = false;
    private GeminiPro geminiPro;


    private LinearLayout arrivalTracker;
    private TextView trackerWorkerName, trackerEta, trackerStatus , trackerOtp;;
    private Button btnViewLive;

    private TextView tabFindWorkers, tabMyBookings;
    private RecyclerView bookingsRecyclerView;
    private UserBookingAdapter bookingAdapter;
    private List<UserBooking> bookingList;
    private SwipeRefreshLayout swipeRefreshLayout; // Redeclare to ensure visibility access
    private String activeRequestId = null;
    private List<Worker> nearbyWorkers = new ArrayList<>();

    RecyclerView featureRecycler;
    FeatureAdapter featureAdapter;
    List<FeatureModel> featureList;

    Handler sliderHandler = new Handler();
    int currentPosition = 0;

    private SeekBar seekRange;
    private TextView txtRange;
    double selectedRange = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize TextToSpeech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.getDefault());
            }
        });

        setContentView(R.layout.activity_homepage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emptyView = findViewById(R.id.empty_view);

        // TABS INIT
        tabFindWorkers = findViewById(R.id.tabFindWorkers);
        tabMyBookings = findViewById(R.id.tabMyBookings);
        bookingsRecyclerView = findViewById(R.id.bookingsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        bookingList = new ArrayList<>();
        bookingAdapter = new UserBookingAdapter(this, bookingList);
        bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookingsRecyclerView.setAdapter(bookingAdapter);


        arrivalTracker = findViewById(R.id.arrivalTracker);
        trackerWorkerName = findViewById(R.id.trackerWorkerName);
        trackerEta = findViewById(R.id.trackerEta);
        trackerStatus = findViewById(R.id.trackerStatus);
        trackerOtp = findViewById(R.id.trackerOtp);
        btnViewLive = findViewById(R.id.btnViewLive);

        // TAB LISTENERS
        tabFindWorkers.setOnClickListener(v -> {
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            bookingsRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE); // Reset empty view

            tabFindWorkers.setTextColor(ContextCompat.getColor(this, R.color.tab));
            tabFindWorkers.setBackgroundResource(R.drawable.tab_selected_underline);

            tabMyBookings.setTextColor(Color.parseColor("#999999"));
            tabMyBookings.setBackground(null);
        });

        tabMyBookings.setOnClickListener(v -> {
            swipeRefreshLayout.setVisibility(View.GONE);
            bookingsRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE); // Reset

            tabMyBookings.setTextColor(ContextCompat.getColor(this, R.color.tab));
            tabMyBookings.setBackgroundResource(R.drawable.tab_selected_underline);

            tabFindWorkers.setTextColor(Color.parseColor("#999999"));
            tabFindWorkers.setBackground(null);

            fetchUserBookings();
        });
        MobileAds.initialize(this, initializationStatus -> {
        });

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

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        loadUserName();

        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }




        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();
        detectLocationAutomatically();


        featureRecycler = findViewById(R.id.featureRecycler);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        featureRecycler.setLayoutManager(layoutManager);

        featureList = new ArrayList<>();

        featureList.add(new FeatureModel(R.drawable.ic_assistant, "Ask AI", "Describe problem"));
        featureList.add(new FeatureModel(R.drawable.woker_image, "Instant Worker", "Get fast help"));
        featureList.add(new FeatureModel(R.drawable.ic_call, "Verified", "Trusted workers"));





        featureAdapter = new FeatureAdapter(featureList, model -> {

            if (model.title.equals("Ask AI")) {

                aiOverlay.setVisibility(View.VISIBLE);
                listeningText.setText("Listening...");
                pulseRing.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.pulse)
                );

                speechRecognizer.startListening(speechIntent);

            } else if (model.title.equals("Instant Worker")) {

                getUserLocationAndFilterNearbyWorkers();
                Toast.makeText(this, "Showing nearby workers", Toast.LENGTH_SHORT).show();

            } else if (model.title.equals("Verified")) {

                startActivity(new Intent(this, FavoritesActivity.class));

            }
        });

        featureRecycler.setAdapter(featureAdapter);


        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        } else {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_LONG).show();
        }

        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        fab = findViewById(R.id.fabAssistant);
        FloatingActionButton fabCamera = findViewById(R.id.fabCamera); // Init Camera FAB

        aiOverlay = findViewById(R.id.aiOverlay);
        pulseRing = findViewById(R.id.pulseRing);
        listeningText = findViewById(R.id.listeningText);


        geminiPro = new GeminiPro();

        fabCamera.setOnClickListener(v -> {
            Toast.makeText(this, "Camera button clicked", Toast.LENGTH_SHORT).show();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 200);
            } else {
                openCamera();
            }
        });

        fab.setOnClickListener(v -> {
            if (speechRecognizer == null) {
                Toast.makeText(this, "Speech recognizer not ready", Toast.LENGTH_SHORT).show();
                return;
            }

            aiOverlay.setVisibility(View.VISIBLE);
            listeningText.setText("Listening...");
            pulseRing.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.pulse)
            );

            speechRecognizer.startListening(speechIntent);
        });

        startLocationMonitoring();

        if (speechRecognizer != null) {
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onResults(Bundle results) {
                    stopAI();
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                    if (matches != null && !matches.isEmpty()) {
                        String userQuery = matches.get(0);

                        if (isAwaitingConfirmation) {
                            handleBookingConfirmation(userQuery);
                            return;
                        }

                        Toast.makeText(Homepage.this, "Processing: " + userQuery, Toast.LENGTH_SHORT).show();

                        Set<String> professions = new HashSet<>();
                        for (Worker w : nearbyWorkers) {
                            if (w.getProfession() != null) {
                                professions.add(w.getProfession());
                            }
                        }
                        List<String> professionList = new ArrayList<>(professions);

                        geminiPro.detectProfession(userQuery, professionList, new GeminiPro.GeminiCallback() {

                            @Override
                            public void onResult(String profession, boolean booking, String message) {

                                runOnUiThread(() -> {

                                    Toast.makeText(Homepage.this, message, Toast.LENGTH_LONG).show();

                                    // 🔥 Filter workers by AI profession
                                    filterWorkers(profession);

                                    if (booking) {

                                        if (!filteredWorkerList.isEmpty()) {

                                            Worker best = filteredWorkerList.get(0);

                                            performBooking(best);

                                        } else {

                                            Toast.makeText(Homepage.this,
                                                    "No workers found",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() ->
                                        Toast.makeText(Homepage.this,
                                                "AI Error: " + error,
                                                Toast.LENGTH_SHORT).show());
                            }
                        });
                    }
                }

                private void handleBookingConfirmation(String response) {
                    isAwaitingConfirmation = false;
                    // Simple multi-lingual "Yes" check
                    List<String> yesWords = java.util.Arrays.asList("yes", "yeah", "yep", "avunu", "sare", "theek hai", "haan", "ok", "okay");
                    boolean isYes = false;
                    for (String word : yesWords) {
                        if (response.toLowerCase().contains(word)) isYes = true;
                    }

                    if (isYes) {
                        // "Book" the top worker
                        if (!filteredWorkerList.isEmpty()) {
                            Worker topWorker = filteredWorkerList.get(0);
                            Toast.makeText(Homepage.this, "Booking sent for " + topWorker.getName() + " (" + topWorker.getProfession() + ")", Toast.LENGTH_LONG).show();
                            performBooking(topWorker);
                        } else {
                            Toast.makeText(Homepage.this, "No worker available to book.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Homepage.this, "Booking Cancelled.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(int error) {
                    stopAI();
                    isAwaitingConfirmation = false; // Reset state on error
                    String message;
                    switch (error) {
                        case SpeechRecognizer.ERROR_AUDIO:
                            message = "Audio recording error";
                            break;
                        case SpeechRecognizer.ERROR_CLIENT:
                            message = "Client side error";
                            break;
                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                            message = "Insufficient permissions";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK:
                            message = "Network error";
                            break;
                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                            message = "Network timeout";
                            break;
                        case SpeechRecognizer.ERROR_NO_MATCH:
                            message = "No match";
                            break;
                        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                            message = "RecognitionService busy";
                            break;
                        case SpeechRecognizer.ERROR_SERVER:
                            message = "error from server";
                            break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                            message = "No speech input";
                            break;
                        default:
                            message = "Didn't understand, please try again.";
                            break;
                    }
                    Toast.makeText(Homepage.this, message, Toast.LENGTH_SHORT).show();
                }

                // Required empty methods
                @Override
                public void onReadyForSpeech(Bundle params) {
                }

                @Override
                public void onBeginningOfSpeech() {
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                }
            });
        }


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        btnMic = findViewById(R.id.btnMic);
        loadingSpinner = findViewById(R.id.loading_spinner);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);

        locationText = findViewById(R.id.location_text);
//        location_icon = findViewById(R.id.location_icon);
        small_location = findViewById(R.id.small_location);
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        workerList = new ArrayList<>();
        filteredWorkerList = new ArrayList<>();
        workerAdapter = new WorkerAdapter(filteredWorkerList);
        recyclerView.setAdapter(workerAdapter);

        String savedLocation = preferences.getString(LOCATION_KEY, "All");
        locationText.setText(savedLocation);
        locationText.setOnClickListener(v -> openLocationPicker());
        small_location.setOnClickListener(v -> openLocationPicker());
//        location_icon.setOnClickListener(v -> openLocationPicker());


        btnViewLive.setOnClickListener(v -> {

            if (activeRequestId != null) {

                Intent i = new Intent(Homepage.this, BookingDetailsActivity.class);
                i.putExtra("requestId", activeRequestId);
                startActivity(i);

            } else {
                Toast.makeText(this, "No active booking found", Toast.LENGTH_SHORT).show();
            }
        });


        btnMic.setOnClickListener(v -> startVoiceInput());

        searchView.clearFocus();
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterWorkers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (!isNetworkAvailable()) {
            showNoInternetAlert();
            showLocationServicesDisabledAlert();
        } else {
            loadDataFromFirebase();
            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadDataFromFirebase();

                new Handler().postDelayed(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                }, 1500);
            });
        }


    }

    private void startInternetMonitoring() {

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if (!isNetworkAvailable()) {
                    showNoInternetAlert();
                }

                handler.postDelayed(this, 3000); // check every 3 sec
            }
        };

        handler.post(runnable);
    }

    private void startLocationMonitoring() {

        Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if (!isLocationEnabled()) {
                    showLocationServicesDisabledAlert();
                }

                handler.postDelayed(this, 3000);
            }
        };

        handler.post(runnable);
    }


    Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {

            if (currentPosition == featureList.size())
                currentPosition = 0;

            featureRecycler.smoothScrollToPosition(currentPosition++);
            sliderHandler.postDelayed(this, 5000); // 2.5 seconds
        }
    };

    String[] searchHints = {
            "Search for 'Electrician'",
            "Search for 'Plumber'",
            "Search for 'Carpenter'",
            "Search for 'Painter'",
            "Search for 'AC Mechanic'",
            "Search for 'Driver'"
    };

    int hintIndex = 0;
    Handler hintHandler = new Handler();
    Runnable hintRunnable = new Runnable() {
        @Override
        public void run() {

            searchView.setHint(searchHints[hintIndex]);

            hintIndex++;
            if (hintIndex == searchHints.length)
                hintIndex = 0;

            hintHandler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 4000);
        hintHandler.post(hintRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
        hintHandler.removeCallbacks(hintRunnable);
    }

    private void stopAI() {
        pulseRing.clearAnimation();
        aiOverlay.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.refresh) {
            loadDataFromFirebase();
        } else if (itemId == R.id.MyFavourites) {
            Intent intent = new Intent(Homepage.this, FavoritesActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.nearBy) {
            if (!isLocationEnabled()) {
                showLocationServicesDisabledAlert();
            } else {
                    View view = getLayoutInflater().inflate(R.layout.range_popup, null);

                    SeekBar seekBar = view.findViewById(R.id.seekRange);
                    TextView txt = view.findViewById(R.id.txtRange);

                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setView(view)
                            .setPositiveButton("Apply", null)
                            .create();

                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                            if(progress < 1) progress = 1;

                            selectedRange = progress;
                            txt.setText("Search Range: " + progress + " KM");
                        }

                        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
                    });

                    dialog.show();
            }
            Toast.makeText(this, "NearBy Workers", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.share) {
            Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.rateus) {
            Toast.makeText(this, "Thanks For Rating Us", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(Homepage.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    private void openLocationPicker() {
        if (!isNetworkAvailable()) {
            showNoInternetAlert();
            return;
        }
        View sheetView = getLayoutInflater().inflate(R.layout.location_picker, null);
        LinearLayout useCurrentLocationLayout = sheetView.findViewById(R.id.use_current_location);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(sheetView);
        useCurrentLocationLayout.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(Homepage.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Homepage.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                return;
            }
            if (!isLocationEnabled()) {
                showLocationServicesDisabledAlert();
            } else {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        updateCityAndDismiss(location);
                        bottomSheetDialog.dismiss();
                    } else {
                        LocationRequest locationRequest = LocationRequest.create()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(1000)
                                .setFastestInterval(500)
                                .setNumUpdates(1);

                        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                Location location1 = locationResult.getLastLocation();
                                if (location1 != null) {
                                    updateCityAndDismiss(location1);
                                    bottomSheetDialog.dismiss();
                                }
                            }
                        }, Looper.getMainLooper());
                    }
                });
            }

        });

        RecyclerView locationRecycler = sheetView.findViewById(R.id.location_recycler);
        locationRecycler.setLayoutManager(new LinearLayoutManager(this));

        List<String> locations = new ArrayList<>();
        LocationAdapter adapter = new LocationAdapter(locations, selectedLocation -> {
            preferences.edit().putString(LOCATION_KEY, selectedLocation).apply();
            locationText.setText(selectedLocation);
//            filterWorkers(searchView.getQuery().toString(), selectedLocation);
            bottomSheetDialog.dismiss();
        });
        locationRecycler.setAdapter(adapter);

        FirebaseDatabase.getInstance().getReference("locations")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Set<String> locationSet = new HashSet<>();
                        for (DataSnapshot locationSnapshot : snapshot.getChildren()) {
                            String location = locationSnapshot.getKey();
                            Boolean isActive = locationSnapshot.getValue(Boolean.class);
                            if (Boolean.TRUE.equals(isActive)) {
                                locationSet.add(location);
                            }
                        }
                        List<String> updatedLocations = new ArrayList<>();
                        updatedLocations.add("All");
                        updatedLocations.addAll(locationSet);

                        adapter.updateData(updatedLocations);
                        bottomSheetDialog.show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Homepage.this, "Failed to load locations", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    

    private void loadDataFromFirebase() {

        loadingSpinner.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);


        FirebaseDatabase.getInstance()
                .getReference("workers")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        workerList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Worker worker = ds.getValue(Worker.class);
                            if (worker != null) {
                                workerList.add(worker);
                            }
                        }
                        loadingSpinner.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);

                        // AFTER loading workers → detect nearby
                        getUserLocationAndFilterNearbyWorkers();

                        if (workerList.isEmpty()) {
                            emptyView.setVisibility(View.VISIBLE);
                            emptyView.setText("No workers found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loadingSpinner.setVisibility(View.GONE);
                    }
                });
    }


    private void getUserLocationAndFilterNearbyWorkers() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location == null) return;

                    double userLat = location.getLatitude();
                    double userLng = location.getLongitude();

                    nearbyWorkers.clear();

                    for (Worker worker : workerList) {

                        if (worker.getLatitude() == 0 || worker.getLongitude() == 0)
                            continue;

                        float[] results = new float[1];

                        Location.distanceBetween(
                                userLat,
                                userLng,
                                worker.getLatitude(),
                                worker.getLongitude(),
                                results
                        );

                        double distance = results[0];

                        if (distance <= selectedRange * 1000) {
                            worker.setDistanceFromUser(distance / 1000);
                            nearbyWorkers.add(worker);
                        }

                        if(nearbyWorkers.isEmpty()){
                            Toast.makeText(this,
                                    "No workers found in " + selectedRange + " KM. Try increasing range.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    // SORT NEAREST FIRST
                    Collections.sort(nearbyWorkers,
                            (w1, w2) -> Double.compare(
                                    w1.getDistanceFromUser(),
                                    w2.getDistanceFromUser()
                            ));

//                    workerAdapter.updateList(nearbyWorkers);
                    filteredWorkerList.clear();
                    filteredWorkerList.addAll(nearbyWorkers);
                    workerAdapter.notifyDataSetChanged();
                });
    }


    private void filterWorkers(String professionQuery) {

        if (professionQuery.isEmpty()) {
            filteredWorkerList.clear();
            filteredWorkerList.addAll(nearbyWorkers);
            workerAdapter.notifyDataSetChanged();
            return;
        }

        filteredWorkerList.clear();

        for (Worker worker : nearbyWorkers) {

            if (worker.getProfession() != null &&
                    worker.getProfession().toLowerCase()
                            .contains(professionQuery.toLowerCase())) {

                filteredWorkerList.add(worker);
            }
        }

        workerAdapter.notifyDataSetChanged();
    }



    private void startVoiceInput() {


        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your profession");

        try {
            startActivityForResult(intent, VOICE_INPUT_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Your device doesn't support voice input", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void showLocationServicesDisabledAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Location Services Disabled")
                .setMessage("Your location services are turned off. Please enable them to continue.")
                .setCancelable(false)
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void showNoInternetAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setCancelable(false)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isNetworkAvailable()) {
                            loadDataFromFirebase();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(Homepage.this, "Still no connection!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(); // Close the app
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void openCamera() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, 201);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Camera not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_INPUT_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                filterWorkers(result.get(0));
            }
        }

        if (requestCode == 201 && resultCode == RESULT_OK && data != null) {
            android.graphics.Bitmap imageBitmap = (android.graphics.Bitmap) data.getExtras().get("data");
            Toast.makeText(this, "Analyzing image...", Toast.LENGTH_LONG).show();

            // Collect unique professions
            Set<String> professions = new HashSet<>();
            for (Worker w : nearbyWorkers) {
                if (w.getProfession() != null) {
                    professions.add(w.getProfession());
                }
            }
            List<String> professionList = new ArrayList<>(professions);

            geminiPro.detectProfessionFromImage(imageBitmap, professionList, new GeminiPro.GeminiCallback() {

                @Override
                public void onResult(String profession, boolean booking, String message) {

                    runOnUiThread(() -> {

                        Toast.makeText(Homepage.this, message, Toast.LENGTH_LONG).show();

                        filterWorkers(profession);

                    });
                }

                @Override
                public void onError(String error) {

                    runOnUiThread(() ->
                            Toast.makeText(Homepage.this,
                                    "AI Error: " + error,
                                    Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openLocationPicker(); // Re-open dialog to retry
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission needed to use feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void updateCityAndDismiss(Location location) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses =
                    geocoder.getFromLocation(location.getLatitude(),
                            location.getLongitude(), 1);

            if (addresses != null && !addresses.isEmpty()) {

                Address address = addresses.get(0);

                String area = address.getSubLocality();
                String landmark = address.getFeatureName();
                String city = address.getLocality();

                if (city == null)
                    city = address.getAdminArea();

                String displayLocation = "";
                String fullLocation = "";

                if (landmark != null)
                    displayLocation += landmark + ", ";
                if (area != null)
                    displayLocation += area + ", ";

                if (city != null)
                    displayLocation += city;

                fullLocation += area + " , " +city;

                preferences.edit().putString(LOCATION_KEY, displayLocation).apply();

                locationText.setText(fullLocation);
                small_location.setText(displayLocation);
                getUserLocationAndFilterNearbyWorkers();

            }

        } catch (Exception e) {
            Toast.makeText(this, "Location error", Toast.LENGTH_SHORT).show();
        }
    }


    private void performBooking(Worker worker) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission needed to book", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(loc -> {
            double userLat = 0.0;
            double userLng = 0.0;
            if (loc != null) {
                userLat = loc.getLatitude();
                userLng = loc.getLongitude();
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

            DatabaseReference reqRef = FirebaseDatabase.getInstance()
                    .getReference("requests")
                    .push();

            String requestId = reqRef.getKey();
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("requestId", requestId);
            requestMap.put("workerId", workerId);
            requestMap.put("userId", userId);
            requestMap.put("userLat", userLat);
            requestMap.put("userLng", userLng);
            requestMap.put("userName", USERNAME);
            requestMap.put("status", "pending");
            requestMap.put("workerName", worker.getName());
            requestMap.put("timestamp", System.currentTimeMillis());

            reqRef.setValue(requestMap)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Request Confirmed in Database!", Toast.LENGTH_SHORT).show();
                        if (tts != null) tts.speak("Booking Confirmed. Opening details.", TextToSpeech.QUEUE_FLUSH, null, null);

                        // Redirect to WorkerDetailsActivity
                        Intent intent = new Intent(Homepage.this, WorkerDetailsActivity.class);
                        intent.putExtra("worker", worker);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void fetchUserBookings() {
        if (currentUser == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("requests");

        ref.orderByChild("userId").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        bookingList.clear();
                        String otpToShow = "";
                        boolean hasAcceptedBooking = false;
                        String acceptedWorkerName = "";
                        String acceptedWorkerStatus = "";

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            String reqId = snap.child("requestId").getValue(String.class);
                            String wId = snap.child("workerId").getValue(String.class);
                            String wName = snap.child("workerName").getValue(String.class);
                            String status = snap.child("status").getValue(String.class);
                            Long time = snap.child("timestamp").getValue(Long.class);
                            Double lat = snap.child("userLat").getValue(Double.class);
                            Double lng = snap.child("userLng").getValue(Double.class);
                            String startOtp = snap.child("startOtp").getValue(String.class);

                            if (reqId != null) {

                                UserBooking booking = new UserBooking(
                                        reqId,
                                        wId,
                                        wName,
                                        status,
                                        time != null ? time : 0,
                                        lat != null ? lat : 0.0,
                                        lng != null ? lng : 0.0
                                );

                                bookingList.add(booking);



                                if (status != null &&
                                        (status.equalsIgnoreCase("accepted") ||
                                                status.equalsIgnoreCase("started") ||
                                                status.equalsIgnoreCase("reached"))) {

                                    hasAcceptedBooking = true;
                                    acceptedWorkerName = wName;
                                    acceptedWorkerStatus = status;
                                    activeRequestId = reqId;

                                    if (status.equalsIgnoreCase("started")) {
                                        otpToShow = startOtp;
                                    }
                                }
                            }
                        }

                        if (hasAcceptedBooking) {
                            arrivalTracker.setVisibility(View.VISIBLE);
                            trackerWorkerName.setText(acceptedWorkerName + " is on the way");
                            trackerStatus.setText(acceptedWorkerStatus);
                            trackerEta.setText("Arriving Soon");

                            if (acceptedWorkerStatus.equalsIgnoreCase("started")) {
                                trackerOtp.setVisibility(View.VISIBLE);
                                trackerOtp.setText("Tell this OTP: " + otpToShow);
                            } else {
                                trackerOtp.setVisibility(View.GONE);
                            }
                        }

                        Collections.sort(bookingList,
                                (b1, b2) -> Long.compare(b2.getTimestamp(), b1.getTimestamp()));

                        bookingAdapter.notifyDataSetChanged();

                        if (bookingList.isEmpty()) {
                            emptyView.setVisibility(View.VISIBLE);
                            emptyView.setText("No bookings found");
                            bookingsRecyclerView.setVisibility(View.GONE);
                        } else {
                            emptyView.setVisibility(View.GONE);
                            bookingsRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Homepage.this,
                                "Failed to load bookings",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserName() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid);
        ref.get().addOnSuccessListener(snap -> {
            USERNAME = snap.child("name").getValue(String.class);
            if (USERNAME == null) USERNAME = "User";
        });
    }


    private void detectLocationAutomatically() {

        if (!isLocationEnabled()) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location != null) {
                        updateCityAndDismiss(location);
                    } else {

                        LocationRequest locationRequest = LocationRequest.create()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(1000)
                                .setFastestInterval(500)
                                .setNumUpdates(1);

                        fusedLocationClient.requestLocationUpdates(locationRequest,
                                new LocationCallback() {
                                    @Override
                                    public void onLocationResult(@NonNull LocationResult locationResult) {

                                        Location loc = locationResult.getLastLocation();
                                        if (loc != null) {
                                            updateCityAndDismiss(loc);
                                        }
                                    }
                                }, Looper.getMainLooper());
                    }

                });
    }


}
