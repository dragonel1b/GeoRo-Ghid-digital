package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class FirebaseManagerLauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_manager_launcher);

        setupButtons();
    }

    private void setupButtons() {
        // Adăugăm butonul pentru managerul de întrebări Firebase
        Button firebaseManagerButton = findViewById(R.id.firebaseManagerButton);
        if (firebaseManagerButton != null) {
            firebaseManagerButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, FirebaseQuizManagerActivity.class);
                startActivity(intent);
            });
        }
    }
} 