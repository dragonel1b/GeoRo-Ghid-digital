package com.example.myapplication.model.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myapplication.Joc1.RomMainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.example.myapplication.R;
import com.example.myapplication.Joc1.AchievementManager;
import com.example.myapplication.repository.QuizResultRepository;
import com.example.myapplication.model.QuizResult;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

/**
 * Activitate de bază modulară pentru afișarea rezultatelor quiz-urilor din toate regiunile
 * Design îmbunătățit cu culori contrastante și compatibilitate completă cu baza de date
 */
public abstract class BaseGameResultActivity extends AppCompatActivity {
    
    private static final String TAG = "BaseGameResultActivity";
    
    // UI Components
    protected TextView scoreTextView;
    protected TextView accuracyTextView;
    protected TextView streakTextView;
    protected TextView timeTextView;
    protected TextView achievementsTextView;
    protected MaterialCardView achievementsCard;
    protected MaterialButton playAgainButton;
    protected MaterialButton achievementsButton;
    protected MaterialButton homeButton;
    
    // Data from Intent
    protected int score;
    protected int correctAnswers;
    protected int totalQuestions;
    protected int maxStreak;
    protected long totalTime;
    protected int lifelinesUsed;
    protected String regionName;
    protected String gameType;
    
    // Database integration
    protected QuizResultRepository quizResultRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_result_modular);
        
        // Initialize database repository
        quizResultRepository = QuizResultRepository.getInstance();
        
        // Get data from intent
        getIntentData();
        
        // Apply region-specific theme
        applyRegionTheme();
        
        // Initialize views
        initializeViews();
        
        // Display results
        displayResults();
        
        // Setup buttons
        setupButtons();
        
        // Save to database
        saveResultToDatabase();
    }
    
    private void getIntentData() {
        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);
        correctAnswers = intent.getIntExtra("correctAnswers", 0);
        totalQuestions = intent.getIntExtra("totalQuestions", 0);
        maxStreak = intent.getIntExtra("maxStreak", 0);
        totalTime = intent.getLongExtra("totalTime", 0);
        lifelinesUsed = intent.getIntExtra("lifelinesUsed", 0);
        regionName = getRegionName();
        gameType = intent.getStringExtra("gameType");
        if (gameType == null) gameType = "quiz";
    }
    
    /**
     * Aplică tema specifică regiunii cu culori contrastante
     */
    protected void applyRegionTheme() {
        RegionTheme theme = getRegionTheme();
        
        // Set status bar and navigation bar colors
        getWindow().setStatusBarColor(getResources().getColor(theme.getPrimaryDarkColor(), getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(theme.getPrimaryColor(), getTheme()));
    }
    
    private void initializeViews() {
        scoreTextView = findViewById(R.id.scoreTextView);
        accuracyTextView = findViewById(R.id.accuracyTextView);
        streakTextView = findViewById(R.id.streakTextView);
        timeTextView = findViewById(R.id.timeTextView);
        achievementsTextView = findViewById(R.id.achievementsTextView);
        achievementsCard = findViewById(R.id.achievementsCard);
        playAgainButton = findViewById(R.id.playAgainButton);
        achievementsButton = findViewById(R.id.achievementsButton);
        homeButton = findViewById(R.id.homeButton);
    }
    
    private void displayResults() {
        // Display score
        scoreTextView.setText(String.valueOf(score));
        
        // Display accuracy with correct answers info
        float accuracy = totalQuestions > 0 ? ((float) correctAnswers / totalQuestions) * 100 : 0;
        accuracyTextView.setText(String.format("%.1f%% (%d/%d)", accuracy, correctAnswers, totalQuestions));
        
        // Display streak
        streakTextView.setText(String.valueOf(maxStreak));
        
        // Display time
        long minutes = totalTime / 60000;
        long seconds = (totalTime % 60000) / 1000;
        timeTextView.setText(String.format("%02d:%02d", minutes, seconds));
        
        // Check for new achievements
        checkAndDisplayAchievements();
    }
    
    private void checkAndDisplayAchievements() {
        AchievementManager achievementManager = AchievementManager.getInstance(this);
        
        // Get newly unlocked achievements for this region
        List<AchievementManager.Achievement> newlyUnlocked = achievementManager.getUnlockedAchievements()
            .stream()
            .filter(achievement -> achievement.getRegion() != null && 
                   achievement.getRegion().equalsIgnoreCase(regionName))
            .collect(java.util.stream.Collectors.toList());
        
        if (!newlyUnlocked.isEmpty()) {
            achievementsCard.setVisibility(View.VISIBLE);
            StringBuilder achievementText = new StringBuilder();
            achievementText.append("🏆 Achievement-uri noi deblocate:\n\n");
            
            for (AchievementManager.Achievement achievement : newlyUnlocked) {
                achievementText.append("• ").append(achievement.getTitle()).append("\n");
                achievementText.append("  ").append(achievement.getDescription()).append("\n\n");
            }
            
            achievementsTextView.setText(achievementText.toString());
        } else {
            achievementsCard.setVisibility(View.GONE);
        }
    }
    
    private void setupButtons() {
        // Play again button
        playAgainButton.setOnClickListener(v -> {
            Intent intent = getPlayAgainIntent();
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            finish();
        });
        
        // Achievements button
        achievementsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.myapplication.Joc1.RomAchievementsActivity.class);
            startActivity(intent);
        });
        
        // Home button
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RomMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
    
    /**
     * Salvează rezultatul în baza de date cu informații complete
     */
    private void saveResultToDatabase() {
        try {
            QuizResult result = new QuizResult();
            result.setScore(score);
            result.setCorrectAnswers(correctAnswers);
            result.setTotalQuestions(totalQuestions);
            result.setMaxStreak(maxStreak);
            result.setTotalTime(totalTime);
            result.setLifelinesUsed(lifelinesUsed);
            result.setRegion(regionName);
            result.setGameType(gameType);
            result.setAccuracy(totalQuestions > 0 ? ((float) correctAnswers / totalQuestions) * 100 : 0);
            result.setCompletedAt(new Date(System.currentTimeMillis()));
            
            // Generate unique ID
            String resultId = generateResultId();
            result.setId(resultId);
            
            // Additional metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sessionDuration", totalTime);
            metadata.put("averageTimePerQuestion", totalQuestions > 0 ? totalTime / totalQuestions : 0);
            metadata.put("difficultyLevel", getDifficultyLevel());
            metadata.put("deviceInfo", getDeviceInfo());
            result.setMetadata(metadata);
            
            // Save to repository (handles both local and cloud storage)
            quizResultRepository.saveQuizResult(result);
            
        } catch (Exception e) {
            // Log error but don't crash the activity
            android.util.Log.e(TAG, "Error saving quiz result to database", e);
        }
    }
    
    private String generateResultId() {
        return regionName.toLowerCase() + "_" + gameType + "_" + System.currentTimeMillis();
    }
    
    private String getDifficultyLevel() {
        float accuracy = totalQuestions > 0 ? ((float) correctAnswers / totalQuestions) * 100 : 0;
        if (accuracy >= 90) return "easy";
        else if (accuracy >= 70) return "medium";
        else return "hard";
    }
    
    private Map<String, Object> getDeviceInfo() {
        Map<String, Object> deviceInfo = new HashMap<>();
        deviceInfo.put("sdk", android.os.Build.VERSION.SDK_INT);
        deviceInfo.put("model", android.os.Build.MODEL);
        deviceInfo.put("manufacturer", android.os.Build.MANUFACTURER);
        return deviceInfo;
    }
    
    @Override
    public void onBackPressed() {
        // Override back press to go to main activity
        Intent intent = new Intent(this, RomMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    // Abstract methods to be implemented by region-specific activities
    protected abstract String getRegionName();
    protected abstract Intent getPlayAgainIntent();
    protected abstract RegionTheme getRegionTheme();
    
    /**
     * Clasa pentru tema unei regiuni cu culori contrastante
     */
    public static class RegionTheme {
        private final int primaryColor;
        private final int primaryDarkColor;
        private final int accentColor;
        private final int backgroundColor;
        private final int cardBackgroundColor;
        private final int textColor;
        
        public RegionTheme(int primaryColor, int primaryDarkColor, int accentColor, 
                          int backgroundColor, int cardBackgroundColor, int textColor) {
            this.primaryColor = primaryColor;
            this.primaryDarkColor = primaryDarkColor;
            this.accentColor = accentColor;
            this.backgroundColor = backgroundColor;
            this.cardBackgroundColor = cardBackgroundColor;
            this.textColor = textColor;
        }
        
        public int getPrimaryColor() { return primaryColor; }
        public int getPrimaryDarkColor() { return primaryDarkColor; }
        public int getAccentColor() { return accentColor; }
        public int getBackgroundColor() { return backgroundColor; }
        public int getCardBackgroundColor() { return cardBackgroundColor; }
        public int getTextColor() { return textColor; }
    }
} 