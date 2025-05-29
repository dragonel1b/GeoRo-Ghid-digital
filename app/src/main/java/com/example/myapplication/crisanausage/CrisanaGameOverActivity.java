package com.example.myapplication.crisanausage;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;

public class CrisanaGameOverActivity extends AppCompatActivity {
    private TextView gameOverTitle, scoreTextView, statsTextView, achievementsTextView;
    private MaterialButton playAgainButton, shareButton, backToMapButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crisana_game_over);
        
        // Initialize views
        initializeViews();
        
        // Get data from intent
        Intent intent = getIntent();
        int score = intent.getIntExtra("score", 0);
        int totalQuestions = intent.getIntExtra("totalQuestions", 10);
        int correctAnswers = intent.getIntExtra("correctAnswers", 0);
        int maxStreak = intent.getIntExtra("maxStreak", 0);
        
        // Set score
        if (scoreTextView != null) {
            scoreTextView.setText(String.valueOf(score));
            scoreTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
        }
        
        // Calculate percentage
        int percentage = totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;
        
        // Set stats text
        if (statsTextView != null) {
            statsTextView.setText(String.format("%d din %d răspunsuri corecte (%d%%)", 
                    correctAnswers, totalQuestions, percentage));
        }
        
        // Build achievements text
        StringBuilder achievementsBuilder = new StringBuilder();
        
        String achievements = intent.getStringExtra("ACHIEVEMENTS");
        if (achievements != null && !achievements.isEmpty()) {
            achievementsBuilder.append(achievements);
        } else {
            if (percentage >= 80) {
                achievementsBuilder.append("• Expert în Crișana!\n");
            } else if (percentage >= 60) {
                achievementsBuilder.append("• Bun cunoscător al Crișanei\n");
            }
            
            if (maxStreak >= 5) {
                achievementsBuilder.append("• Neînvins! Serie de ").append(maxStreak).append(" răspunsuri corecte consecutive\n");
            } else if (maxStreak >= 3) {
                achievementsBuilder.append("• Cărturar! Serie de ").append(maxStreak).append(" răspunsuri corecte consecutive\n");
            }
        }
        
        // Set achievements text
        if (achievementsTextView != null) {
            if (achievementsBuilder.length() > 0) {
                achievementsTextView.setText(achievementsBuilder.toString());
            } else {
                achievementsTextView.setText("Continuă să explorezi Crișana pentru a obține realizări!");
            }
        }
        
        // Setup button listeners
        setupButtonListeners();
    }
    
    private void initializeViews() {
        gameOverTitle = findViewById(R.id.gameOverTitle);
        scoreTextView = findViewById(R.id.scoreTextView);
        statsTextView = findViewById(R.id.statsTextView);
        achievementsTextView = findViewById(R.id.achievementsTextView);
        playAgainButton = findViewById(R.id.playAgainButton);
        shareButton = findViewById(R.id.shareButton);
        backToMapButton = findViewById(R.id.backToMapButton);
        
        // Apply custom typeface for better readability
        Typeface customTypeface = Typeface.create("sans-serif-medium", Typeface.BOLD);
        if (gameOverTitle != null) {
            gameOverTitle.setTypeface(customTypeface);
        }
        if (scoreTextView != null) {
            scoreTextView.setTypeface(customTypeface);
        }
        
        // Make buttons pop with elevation and animation
        applyButtonStyles();
    }
    
    private void applyButtonStyles() {
        // Set elevation and ripple effect for buttons
        if (playAgainButton != null) {
            playAgainButton.setElevation(12f);
        }
        if (shareButton != null) {
            shareButton.setElevation(12f);
        }
        if (backToMapButton != null) {
            backToMapButton.setElevation(12f);
        }
        
        // Add click animation
        View.OnClickListener animateClickListener = v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click));
        };
        
        if (playAgainButton != null) {
            playAgainButton.setOnTouchListener((v, event) -> {
                animateClickListener.onClick(v);
                return false;
            });
        }
        
        if (shareButton != null) {
            shareButton.setOnTouchListener((v, event) -> {
                animateClickListener.onClick(v);
                return false;
            });
        }
        
        if (backToMapButton != null) {
            backToMapButton.setOnTouchListener((v, event) -> {
                animateClickListener.onClick(v);
                return false;
            });
        }
    }
    
    private void setupButtonListeners() {
        // Play Again button
        if (playAgainButton != null) {
            playAgainButton.setOnClickListener(v -> {
                Intent intent = new Intent(CrisanaGameOverActivity.this, CrisanaGameActivity.class);
                startActivity(intent);
                finish();
            });
        }
        
        // Share button
        if (shareButton != null) {
            shareButton.setOnClickListener(v -> {
                String scoreText = scoreTextView != null ? scoreTextView.getText().toString() : "0";
                String statsText = statsTextView != null ? statsTextView.getText().toString() : "";
                
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Scorul meu la Quizul despre Crișana");
                shareIntent.putExtra(Intent.EXTRA_TEXT, 
                        "Am obținut " + scoreText + " puncte în Quizul despre Crișana! " +
                        statsText + " #ExplorandRomania");
                
                startActivity(Intent.createChooser(shareIntent, "Distribuie rezultatul prin"));
            });
        }
        
        // Back to Map button
        if (backToMapButton != null) {
            backToMapButton.setOnClickListener(v -> {
                finish();
            });
        }
    }
} 