package com.example.myapplication.munteniausage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.MainActivity;
import com.example.myapplication.RomApp.Muntenia;

public class MunteniaGameOverActivity extends AppCompatActivity {
    private TextView finalScoreText;
    private TextView streakText;
    private TextView correctAnswersText;
    private TextView accuracyText;
    private TextView achievementsText;
    private MaterialButton playAgainButton;
    private MaterialButton exitButton;
    private MaterialButton shareButton;
    private LinearLayout highScoreBanner;

    private int finalScore;
    private int longestStreak;
    private int correctAnswers;
    private int totalQuestions;
    private String achievements;
    private boolean isNewHighScore = false;

    private static final String PREFS_NAME = "MunteniaGamePrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muntenia_game_over);

        initializeViews();
        setupButtonListeners();
        loadGameStats();
        setupAnimations();
    }

    private void initializeViews() {
        finalScoreText = findViewById(R.id.finalScoreText);
        streakText = findViewById(R.id.streakText);
        correctAnswersText = findViewById(R.id.correctAnswersText);
        accuracyText = findViewById(R.id.accuracyText);
        achievementsText = findViewById(R.id.achievementsText);
        playAgainButton = findViewById(R.id.playAgainButton);
        exitButton = findViewById(R.id.exitButton);
        shareButton = findViewById(R.id.shareButton);
        highScoreBanner = findViewById(R.id.high_score_banner);
    }

    private void loadGameStats() {
        Intent intent = getIntent();
        if (intent != null) {
            finalScore = intent.getIntExtra("finalScore", 0);
            longestStreak = intent.getIntExtra("longestStreak", 0);
            correctAnswers = intent.getIntExtra("correctAnswers", 0);
            totalQuestions = intent.getIntExtra("totalQuestions", 0);
            achievements = intent.getStringExtra("achievements");
            
            checkForHighScore();
            
            if (finalScoreText != null) finalScoreText.setText("Scor Final: " + finalScore);
            if (streakText != null) streakText.setText("Cel mai lung streak: " + longestStreak);
            if (correctAnswersText != null) correctAnswersText.setText("Răspunsuri corecte: " + correctAnswers + "/" + totalQuestions);
            
            float accuracy = totalQuestions > 0 ? (float) correctAnswers / totalQuestions * 100 : 0;
            if (accuracyText != null) accuracyText.setText(String.format("Acuratețe: %.1f%%", accuracy));
            
            if (achievementsText != null) {
                if (achievements != null && !achievements.isEmpty()) {
                    achievementsText.setText("Realizări:\n" + achievements);
                    achievementsText.setVisibility(View.VISIBLE);
                } else {
                    achievementsText.setVisibility(View.GONE);
                }
            }
        }
    }

    private void checkForHighScore() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int highScore = prefs.getInt("highScore", 0);
        
        if (finalScore > highScore) {
            isNewHighScore = true;
            
            prefs.edit().putInt("highScore", finalScore).apply();
        }
    }

    private void setupAnimations() {
        View[] views = {finalScoreText, streakText, correctAnswersText, accuracyText, achievementsText};
        for (int i = 0; i < views.length; i++) {
            if (views[i] != null && views[i].getVisibility() == View.VISIBLE) {
                views[i].setAlpha(0f);
                views[i].animate()
                        .alpha(1f)
                        .setDuration(500)
                        .setStartDelay(i * 200)
                        .start();
            }
        }

        if (isNewHighScore) {
            highScoreBanner.setVisibility(View.VISIBLE);
        }
    }

    private void setupButtonListeners() {
        if (playAgainButton != null) {
            playAgainButton.setOnClickListener(v -> {
                Intent gameIntent = new Intent(this, MunteniaGameActivity.class);
                gameIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(gameIntent);
                finish();
            });
        }

        if (exitButton != null) {
            exitButton.setOnClickListener(v -> {
                Intent mainIntent = new Intent(this, Muntenia.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
                finish();
            });
        }

        if (shareButton != null) {
            shareButton.setOnClickListener(v -> {
                String shareText = String.format("Am terminat jocul Muntenia cu un scor de %s puncte! Poți să-mi depășești scorul?",
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
        Intent intent = new Intent(this, Muntenia.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 