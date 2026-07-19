package com.example.myapplication.transilvaniausage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class TransilvaniaGameOverActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);
        
        // Obținem scorul din intent
        int score = getIntent().getIntExtra("score", 0);
        
        // Afișăm scorul
        TextView scoreText = findViewById(R.id.scoreTextView);
        if (scoreText != null) {
            scoreText.setText("Scor: " + score);
        }
        
        // Configurăm butonul de restart
        Button restartButton = findViewById(R.id.playAgainButton);
        if (restartButton != null) {
            restartButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, TransilvaniaGameActivity.class);
                startActivity(intent);
                finish();
            });
        }
        
        // Configurăm butonul de întoarcere la hartă
        Button mapButton = findViewById(R.id.backToMapButton);
        if (mapButton != null) {
            mapButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, TransilvaniaMapActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
} 