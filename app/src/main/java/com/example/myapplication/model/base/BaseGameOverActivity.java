package com.example.myapplication.model.base;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.utils.PointsManager;

/**
 * Clasă de bază pentru activitățile de game over din aplicație.
 * Oferă funcționalități comune pentru toate ecranele de final de joc din diferite regiuni.
 */
public abstract class BaseGameOverActivity extends AppCompatActivity {
    // UI Components
    protected TextView gameOverTitle;
    protected TextView scoreLabel;
    protected TextView scoreTextView;
    protected TextView statsTextView;
    protected TextView achievementsTextView;
    protected Button playAgainButton;
    protected Button shareButton;
    protected Button backToMapButton;
    protected ImageView medalImage;
    
    // Game state
    protected int correctAnswers = 0;
    protected int totalQuestions = 0;
    protected int score = 0;
    protected int maxStreak = 0;
    protected String region = "";
    protected PointsManager pointsManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inițializăm managerul de puncte
        pointsManager = PointsManager.getInstance(this);
        
        // Obținem datele din intent
        Intent intent = getIntent();
        correctAnswers = intent.getIntExtra("correctAnswers", 0);
        totalQuestions = intent.getIntExtra("totalQuestions", 10);
        score = intent.getIntExtra("score", 0);
        maxStreak = intent.getIntExtra("maxStreak", 0);
        region = intent.getStringExtra("region");
        
        // Calculăm procentajul
        float percentage = totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;
        
        // Inițializarea este făcută în subclase
    }
    
    /**
     * Inițializează elementele UI comune pentru toate activitățile de game over
     */
    protected void initializeCommonViews() {
        gameOverTitle = findViewById(R.id.gameOverTitle);
        scoreLabel = findViewById(R.id.scoreLabel);
        scoreTextView = findViewById(R.id.scoreTextView);
        statsTextView = findViewById(R.id.statsTextView);
        achievementsTextView = findViewById(R.id.achievementsTextView);
        playAgainButton = findViewById(R.id.playAgainButton);
        shareButton = findViewById(R.id.shareButton);
        backToMapButton = findViewById(R.id.backToMapButton);
        medalImage = findViewById(R.id.medalImage);
        
        // Configurăm butoanele
        if (playAgainButton != null) {
            playAgainButton.setOnClickListener(v -> restartGame());
        }
        
        if (shareButton != null) {
            shareButton.setOnClickListener(v -> shareResults());
        }
        
        if (backToMapButton != null) {
            backToMapButton.setOnClickListener(v -> returnToMap());
        }
    }
    
    /**
     * Actualizează UI-ul cu rezultatele jocului
     */
    protected void updateUI() {
        // Animăm scorul
        if (scoreTextView != null) {
            Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
            scoreTextView.setText(String.valueOf(score));
            scoreTextView.startAnimation(scaleUp);
        }
        
        // Calculăm acuratețea
        float accuracy = (float) correctAnswers / totalQuestions * 100;
        float percentage = accuracy; // Calculăm procentajul pentru compatibilitate
        
        // Afișăm statisticile
        if (statsTextView != null) {
            String statsText = correctAnswers + " din " + totalQuestions + " răspunsuri corecte (" + Math.round(accuracy) + "%)";
            statsTextView.setText(statsText);
        }
        
        // Afișăm realizările
        if (achievementsTextView != null) {
            String achievements = generateAchievements(accuracy);
            achievementsTextView.setText(achievements);
        }
        
        // Determinăm rezultatul și feedback-ul în funcție de procentaj
        if (medalImage != null) {
            if (percentage >= 90) {
                medalImage.setImageResource(R.drawable.ic_medal_gold);
            } else if (percentage >= 70) {
                medalImage.setImageResource(R.drawable.ic_medal_silver);
            } else if (percentage >= 50) {
                medalImage.setImageResource(R.drawable.ic_medal_bronze);
            } else {
                medalImage.setVisibility(View.INVISIBLE);
            }
        }
        
        // Salvăm rezultatul
        saveResult();
    }
    
    /**
     * Generează textul pentru realizări în funcție de performanță
     * @param accuracy Acuratețea răspunsurilor (procentaj)
     * @return Textul cu realizări
     */
    protected String generateAchievements(float accuracy) {
        StringBuilder achievements = new StringBuilder("Realizări:\n");
        
        // Realizări bazate pe acuratețe
        if (accuracy >= 80) {
            achievements.append("• Expert al regiunii ").append(region).append(" (").append(correctAnswers).append(" din ").append(totalQuestions).append(" corecte)\n");
        } else if (accuracy >= 60) {
            achievements.append("• Cunoscător al regiunii ").append(region).append(" (").append(correctAnswers).append(" din ").append(totalQuestions).append(" corecte)\n");
        } else {
            achievements.append("• Explorator al regiunii ").append(region).append(" (").append(correctAnswers).append(" din ").append(totalQuestions).append(" corecte)\n");
        }
        
        // Realizări bazate pe scor
        if (score > 100) {
            achievements.append("• Punctaj impresionant: ").append(score).append(" puncte!\n");
        }
        
        // Realizări bazate pe streak
        if (maxStreak >= 5) {
            achievements.append("• Serie de ").append(maxStreak).append(" răspunsuri corecte consecutive!");
        }
        
        return achievements.toString();
    }
    
    /**
     * Salvează rezultatul jocului
     */
    protected void saveResult() {
        SharedPreferences prefs = getSharedPreferences("GameResults", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Salvăm rezultatul curent
        editor.putInt(region + "_last_score", (int) Math.round(correctAnswers / (float) totalQuestions * 100));
        
        // Actualizăm scorul maxim dacă este cazul
        int highScore = prefs.getInt(region + "_high_score", 0);
        if ((int) Math.round(correctAnswers / (float) totalQuestions * 100) > highScore) {
            editor.putInt(region + "_high_score", (int) Math.round(correctAnswers / (float) totalQuestions * 100));
        }
        
        // Incrementăm numărul de jocuri jucate
        int gamesPlayed = prefs.getInt(region + "_games_played", 0);
        editor.putInt(region + "_games_played", gamesPlayed + 1);
        
        editor.apply();
    }
    
    /**
     * Distribuie rezultatele jocului
     */
    protected void shareResults() {
        String shareText = "Am explorat " + region + " și am obținut " + score + " puncte în jocul de cunoștințe! " +
                "Am răspuns corect la " + correctAnswers + " din " + totalQuestions + " întrebări.";
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Distribuie rezultatul"));
    }
    
    /**
     * Repornește jocul
     * Această metodă trebuie implementată de fiecare activitate de game over specifică regiunii
     */
    protected abstract void restartGame();
    
    /**
     * Revine la harta regiunii
     * Această metodă trebuie implementată de fiecare activitate de game over specifică regiunii
     */
    protected abstract void returnToMap();
} 