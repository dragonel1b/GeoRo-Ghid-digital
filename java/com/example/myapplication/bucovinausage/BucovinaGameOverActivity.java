package com.example.myapplication.bucovinausage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;

public class BucovinaGameOverActivity extends AppCompatActivity {

    private TextView gameOverTitle;
    private TextView scoreLabel;
    private TextView scoreTextView;
    private TextView statsTextView;
    private TextView achievementsTextView;
    private MaterialButton playAgainButton;
    private MaterialButton shareButton;
    private MaterialButton backToMapButton;

    private int score;
    private int correctAnswers;
    private int totalQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucovina_game_over);

        // Apply theme colors
        getWindow().setStatusBarColor(getResources().getColor(R.color.bucovina_primary_dark, getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.bucovina_primary_dark, getTheme()));

        // Initialize views
        gameOverTitle = findViewById(R.id.gameOverTitle);
        scoreLabel = findViewById(R.id.scoreLabel);
        scoreTextView = findViewById(R.id.scoreTextView);
        statsTextView = findViewById(R.id.statsTextView);
        achievementsTextView = findViewById(R.id.achievementsTextView);
        playAgainButton = findViewById(R.id.playAgainButton);
        shareButton = findViewById(R.id.shareButton);
        backToMapButton = findViewById(R.id.backToMapButton);

        // Get score data from intent
        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);
        correctAnswers = intent.getIntExtra("correctAnswers", 0);
        totalQuestions = intent.getIntExtra("totalQuestions", 10);

        // Display results
        displayResults();

        // Set up button click listeners
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameIntent = new Intent(BucovinaGameOverActivity.this, BucovinaGameActivity.class);
                startActivity(gameIntent);
                finish();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareResults();
            }
        });

        backToMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(BucovinaGameOverActivity.this, BucovinaMapActivity.class);
                startActivity(mapIntent);
                finish();
            }
        });
    }

    private void displayResults() {
        // Animate the score text
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        scoreTextView.setText(String.valueOf(score));
        scoreTextView.startAnimation(scaleUp);

        // Calculate accuracy percentage
        float accuracy = (float) correctAnswers / totalQuestions * 100;
        
        // Display stats
        String statsText = correctAnswers + " din " + totalQuestions + " răspunsuri corecte (" + Math.round(accuracy) + "%)";
        statsTextView.setText(statsText);

        // Display achievements based on performance
        StringBuilder achievements = new StringBuilder("Realizări:\n");
        
        if (accuracy >= 80) {
            achievements.append("• Expert al Bucovinei (").append(correctAnswers).append(" din ").append(totalQuestions).append(" corecte)\n");
        } else if (accuracy >= 60) {
            achievements.append("• Cunoscător al Bucovinei (").append(correctAnswers).append(" din ").append(totalQuestions).append(" corecte)\n");
        } else {
            achievements.append("• Explorator al Bucovinei (").append(correctAnswers).append(" din ").append(totalQuestions).append(" corecte)\n");
        }
        
        if (score > 100) {
            achievements.append("• Punctaj impresionant: ").append(score).append(" puncte!");
        }
        
        achievementsTextView.setText(achievements.toString());
    }

    private void shareResults() {
        String shareText = "Am explorat Bucovina și am obținut " + score + " puncte în jocul de cunoștințe! " +
                "Am răspuns corect la " + correctAnswers + " din " + totalQuestions + " întrebări.";
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Distribuie rezultatul"));
    }
} 