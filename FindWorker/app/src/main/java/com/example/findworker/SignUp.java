package com.example.findworker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.widget.Toolbar;
import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    EditText name,phone,email,password,cpassword;
    private FirebaseAuth mAuth;
    Toolbar toolbar;
    ProgressBar progressBar;
    String selectedLanguage = "";
    private RadioGroup genderGroup;
    private String selectedGender = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // make icons dark
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        mAuth = FirebaseAuth.getInstance();

        name = findViewById(R.id.username);
        phone = findViewById(R.id.userphonenumber);
        email = findViewById(R.id.useremail);
        password = findViewById(R.id.userpassword);
        cpassword = findViewById(R.id.cpassword);
        progressBar = findViewById(R.id.progressBar);
        genderGroup = findViewById(R.id.genderGroup);



        selectedLanguage = getIntent().getStringExtra("selectedLanguage");

        Toast.makeText(this, "Selected Language: " + selectedLanguage, Toast.LENGTH_SHORT).show();


        int selectedId = genderGroup.getCheckedRadioButtonId();


    }

    public void SignUpUser(View view) {

        String name = ((EditText) findViewById(R.id.username)).getText().toString().trim();
        String email = ((EditText) findViewById(R.id.useremail)).getText().toString().trim();
        String phone = ((EditText) findViewById(R.id.userphonenumber)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.userpassword)).getText().toString().trim();
        String confirmPassword = ((EditText) findViewById(R.id.cpassword)).getText().toString().trim();
        boolean agreed = ((CheckBox) findViewById(R.id.checkbox_terms)).isChecked();
        int startOtp = (int)(Math.random() * 9000) + 1000;
        int completeOtp = (int)(Math.random() * 9000) + 1000;


        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!agreed) {
            Toast.makeText(this, "Please agree to the Terms and Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = genderGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton genderBtn = findViewById(selectedId);
        selectedGender = genderBtn.getText().toString();

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Get UID from FirebaseAuth
                        String uid = mAuth.getCurrentUser().getUid();

                        // Create user map
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);
                        user.put("phone", phone);
                        user.put("language", selectedLanguage);
                        user.put("gender", selectedGender);
                        user.put("startOtp", String.valueOf(startOtp));
                        user.put("completeOtp", String.valueOf(completeOtp));

                        // Reference to Realtime DB
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users");

                        // Save user under their UID
                        dbRef.child(uid).setValue(user)
                                .addOnSuccessListener(unused -> {
                                    clearForm();
                                    Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "This email is already registered.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void clearForm() {
        name.setText("");
        email.setText("");
        phone.setText("");
        password.setText("");
        cpassword.setText("");
    }


}