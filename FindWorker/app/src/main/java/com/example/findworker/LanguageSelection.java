package com.example.findworker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class LanguageSelection extends AppCompatActivity {

    private MaterialCardView langTelugu, langHindi, langEnglish;
    private MaterialCardView selectedCard = null;
    private String selectedLanguage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_language_selection);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        langTelugu = findViewById(R.id.langTelugu);
        langHindi = findViewById(R.id.langHindi);
        langEnglish = findViewById(R.id.langEnglish);
        MaterialButton btnNext = findViewById(R.id.btnNext);

        langTelugu.setOnClickListener(v -> selectLanguage(langTelugu, "Telugu"));
        langHindi.setOnClickListener(v -> selectLanguage(langHindi, "Hindi"));
        langEnglish.setOnClickListener(v -> selectLanguage(langEnglish, "English"));

        btnNext.setOnClickListener(v -> {
            if (!selectedLanguage.isEmpty()) {
                Intent intent = new Intent(LanguageSelection.this, SignUp.class);
                intent.putExtra("selectedLanguage", selectedLanguage);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select a language", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectLanguage(MaterialCardView card, String language) {

        // Reset previous selection
        if (selectedCard != null) {
            selectedCard.setStrokeColor(Color.parseColor("#E0E0E0"));
            selectedCard.setCardBackgroundColor(Color.WHITE);
        }

        // Highlight selected
        selectedCard = card;
        selectedCard.setStrokeColor(Color.parseColor("#FF5722"));
        selectedCard.setCardBackgroundColor(Color.parseColor("#FFF3E0"));

        selectedLanguage = language;
    }
}