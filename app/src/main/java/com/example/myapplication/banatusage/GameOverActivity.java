package com.example.myapplication.banatusage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

/**
 * Activity to display game over screen for Banat quiz
 */
public class GameOverActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);
        
        setupViews();
    }
    
    private void setupViews() {
        TextView gameOverText = findViewById(R.id.scoreTextView);
        Button playAgainButton = findViewById(R.id.playAgainButton);
        Button backToMenuButton = findViewById(R.id.backToMapButton);
        
        // Get game results from intent
        Intent intent = getIntent();
        int finalScore = intent.getIntExtra("FINAL_SCORE", 0);
        int correctAnswers = intent.getIntExtra("CORRECT_ANSWERS", 0);
        int totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 0);
        
        // Display results
        String resultText = "🎮 Joc terminat!\n\n" +
                           "📊 Scor final: " + finalScore + "\n" +
                           "✅ Răspunsuri corecte: " + correctAnswers + "/" + totalQuestions + "\n" +
                           "🎯 Acuratețe: " + String.format("%.1f%%", (correctAnswers * 100.0) / totalQuestions);
        
        gameOverText.setText(resultText);
        
        // Setup buttons
        playAgainButton.setOnClickListener(v -> {
            Intent gameIntent = new Intent(this, BanatGameActivity.class);
            startActivity(gameIntent);
            finish();
        });
        
        backToMenuButton.setOnClickListener(v -> {
            finish();
        });
    }
} 