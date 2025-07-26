package com.example.myapplication.bucovinausage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.utils.ConfettiHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * Activitate modernizată pentru sfârșitul jocului în quiz-ul Bucovina
 */
public class GameOverActivity extends AppCompatActivity {
    
    private TextView scoreText;
    private TextView accuracyText;
    private TextView timeText;
    private TextView gameModeText;
    private TextView performanceText;
    private TextView recommendationText;
    private TextView achievementsTextView;
    private TextView achievementText;
    
    private MaterialButton playAgainButton;
    private MaterialButton shareButton;
    private MaterialButton backToMapButton;
    
    private MaterialCardView achievementCard;
    
    private PlayerProgressTracker progressTracker;
    private DifficultyManager difficultyManager;
    
    // Date din joc
    private int finalScore;
    private int totalQuestions;
    private long totalTimeSpent;
    private String gameMode;
    private float accuracy;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);
        
        initializeComponents();
        getGameData();
        setupUI();
        setupClickListeners();
        showCelebration();
    }
    
    private void initializeComponents() {
        progressTracker = new PlayerProgressTracker(this);
        difficultyManager = new DifficultyManager(this);
        
        scoreText = findViewById(R.id.scoreTextView);
        accuracyText = findViewById(R.id.accuracyText);
        timeText = findViewById(R.id.timeText);
        gameModeText = findViewById(R.id.gameModeText);
        performanceText = findViewById(R.id.performanceText);
        recommendationText = findViewById(R.id.recommendationText);
        achievementsTextView = findViewById(R.id.achievementsTextView);
        achievementText = findViewById(R.id.achievementText);
        
        playAgainButton = findViewById(R.id.playAgainButton);
        shareButton = findViewById(R.id.shareButton);
        backToMapButton = findViewById(R.id.backToMapButton);
        
        achievementCard = findViewById(R.id.achievementCard);
    }
    
    private void getGameData() {
        Intent intent = getIntent();
        finalScore = intent.getIntExtra("final_score", 0);
        totalQuestions = intent.getIntExtra("total_questions", 10);
        totalTimeSpent = intent.getLongExtra("total_time_spent", 0);
        gameMode = intent.getStringExtra("game_mode");
        
        if (gameMode == null) gameMode = "Quiz Clasic";
        
        accuracy = totalQuestions > 0 ? (float) finalScore / totalQuestions : 0;
    }
    
    private void setupUI() {
        // Afișează scorul principal
        scoreText.setText(finalScore + "/" + totalQuestions);
        
        // Afișează acuratețea
        accuracyText.setText(Math.round(accuracy * 100) + "%");
        
        // Afișează timpul
        long minutes = totalTimeSpent / (1000 * 60);
        long seconds = (totalTimeSpent % (1000 * 60)) / 1000;
        timeText.setText(String.format("%02d:%02d", minutes, seconds));
        
        // Afișează modul de joc
        gameModeText.setText(gameMode);
        
        // Afișează evaluarea performanței
        String performanceMessage = getPerformanceMessage();
        performanceText.setText(performanceMessage);
        
        // Afișează recomandarea
        String recommendation = difficultyManager.getPerformanceRecommendation();
        recommendationText.setText(recommendation);
        
        // Verifică și afișează achievement-uri
        checkAndShowAchievements();
    }
    
    private String getPerformanceMessage() {
        if (accuracy >= 0.95f) {
            return "🏆 Performanță perfectă! Ești un adevărat expert al Bucovinei!";
        } else if (accuracy >= 0.85f) {
            return "🌟 Excelent! Cunoști foarte bine istoria și cultura Bucovinei!";
        } else if (accuracy >= 0.75f) {
            return "👍 Foarte bine! Continuă să exersezi pentru a deveni expert!";
        } else if (accuracy >= 0.60f) {
            return "📚 Bine! Mai ai de învățat despre frumoasa regiune Bucovina!";
        } else {
            return "💪 Nu te descuraja! Fiecare joc te ajută să înveți mai multe!";
        }
    }
    
    private void checkAndShowAchievements() {
        StringBuilder achievements = new StringBuilder();
        boolean hasAchievements = false;
        
        // Verifică achievement-uri bazate pe scor
        if (finalScore == totalQuestions) {
            achievements.append("🏆 Scor Perfect!\n");
            hasAchievements = true;
        }
        
        // Verifică achievement-uri bazate pe viteză
        long averageTimePerQuestion = totalTimeSpent / totalQuestions;
        if (averageTimePerQuestion < 10000) { // Sub 10 secunde per întrebare
            achievements.append("⚡ Viteza Fulgerului!\n");
            hasAchievements = true;
        }
        
        // Verifică streak-ul
        int currentStreak = progressTracker.getCurrentStreak();
        if (currentStreak >= 7) {
            achievements.append("🔥 Streak de " + currentStreak + " zile!\n");
            hasAchievements = true;
        }
        
        // Verifică numărul total de jocuri
        int totalGames = progressTracker.getTotalGames();
        if (totalGames == 10) {
            achievements.append("🎮 10 Jocuri Jucate!\n");
            hasAchievements = true;
        } else if (totalGames == 50) {
            achievements.append("🎯 50 Jocuri Jucate!\n");
            hasAchievements = true;
        }
        
        if (hasAchievements) {
            achievementCard.setVisibility(View.VISIBLE);
            achievementText.setText(achievements.toString());
        } else {
            achievementCard.setVisibility(View.GONE);
        }
    }
    
    private void setupClickListeners() {
        if (playAgainButton != null) {
            playAgainButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, GameModeSelectionActivity.class);
                startActivity(intent);
                finish();
            });
        }
        if (shareButton != null) {
            shareButton.setOnClickListener(v -> shareResults());
        }
        if (backToMapButton != null) {
            backToMapButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, BucovinaMapActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
    
    private void showCelebration() {
        if (accuracy >= 0.8f) {
            // Afișează confetti pentru performanțe bune
            ConfettiHelper.showConfetti(this, findViewById(R.id.rootLayout));
        }
    }
    
    private void shareResults() {
        String shareText = "🎯 Am terminat un quiz despre Bucovina!\n\n" +
                          "📊 Scor: " + finalScore + "/" + totalQuestions + "\n" +
                          "✅ Acuratețe: " + Math.round(accuracy * 100) + "%\n" +
                          "🎮 Mod: " + gameMode + "\n\n" +
                          "Încearcă și tu să testezi cunoștințele despre România! 🇷🇴";
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Distribuie rezultatul"));
    }
    
    @Override
    public void onBackPressed() {
        // Navighează către meniul principal în loc să se întoarcă la joc
        Intent intent = new Intent(this, BucovinaMapActivity.class);
        startActivity(intent);
        finish();
    }
} 