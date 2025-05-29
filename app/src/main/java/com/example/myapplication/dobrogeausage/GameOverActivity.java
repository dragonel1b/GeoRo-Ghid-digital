package com.example.myapplication.dobrogeausage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.MainActivity;

public class GameOverActivity extends AppCompatActivity {
    private TextView finalScoreText;
    private TextView streakText;
    private TextView timeText;
    private TextView accuracyText;
    private TextView achievementsText;
    private MaterialButton playAgainButton;
    private MaterialButton exitButton;
    private MaterialButton shareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dobrogea_game_over);

        initializeViews();
        setupButtonListeners();
        loadGameStats();
        setupAnimations();
    }

    private void initializeViews() {
        finalScoreText = findViewById(R.id.finalScoreText);
        streakText = findViewById(R.id.streakText);
        timeText = findViewById(R.id.timeText);
        accuracyText = findViewById(R.id.accuracyText);
        achievementsText = findViewById(R.id.achievementsText);
        playAgainButton = findViewById(R.id.playAgainButton);
        exitButton = findViewById(R.id.exitButton);
        shareButton = findViewById(R.id.shareButton);
    }

    private void loadGameStats() {
        Intent intent = getIntent();
        int finalScore = intent.getIntExtra("finalScore", 0);
        int longestStreak = intent.getIntExtra("longestStreak", 0);
        float averageTime = intent.getFloatExtra("averageTime", 0);
        float accuracy = intent.getFloatExtra("accuracy", 0);
        String achievements = intent.getStringExtra("achievements");

        if (finalScoreText != null) finalScoreText.setText("Scor Final: " + finalScore);
        if (streakText != null) streakText.setText("Cel mai lung streak: " + longestStreak);
        if (timeText != null) timeText.setText(String.format("Timp mediu: %.1f secunde", averageTime));
        if (accuracyText != null) accuracyText.setText(String.format("Acuratețe: %.1f%%", accuracy * 100));
        if (achievementsText != null) achievementsText.setText("Realizări: " + achievements);
    }

    private void setupAnimations() {
        View[] views = {finalScoreText, streakText, timeText, accuracyText, achievementsText};
        for (int i = 0; i < views.length; i++) {
            if (views[i] != null) {
                views[i].setAlpha(0f);
                views[i].animate()
                        .alpha(1f)
                        .setDuration(500)
                        .setStartDelay(i * 200)
                        .start();
            }
        }
    }

    private void setupButtonListeners() {
        if (playAgainButton != null) {
            playAgainButton.setOnClickListener(v -> {
                Intent gameIntent = new Intent(this, DobrogeaGameActivity.class);
                gameIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(gameIntent);
                finish();
            });
        }

        if (exitButton != null) {
            exitButton.setOnClickListener(v -> {
                Intent mainIntent = new Intent(this, MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
                finish();
            });
        }

        if (shareButton != null) {
            shareButton.setOnClickListener(v -> {
                String shareText = String.format("Am terminat jocul Dobrogea cu un scor de %s puncte! Poți să-mi depășești scorul?",
                        finalScoreText.getText().toString().split(": ")[1]);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, "Distribuie via"));
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 