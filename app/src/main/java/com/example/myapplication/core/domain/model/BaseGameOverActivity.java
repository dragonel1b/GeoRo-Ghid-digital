package com.example.myapplication.core.domain.model;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

/**
 * Activitate de bază modulară pentru afișarea rezultatelor quiz-urilor din toate regiunile
 * Poate fi extinsă pentru fiecare regiune cu configurări specifice
 */
public abstract class BaseGameOverActivity extends AppCompatActivity {
    
    private static final String TAG = "BaseGameOverActivity";
    
    // UI Components
    protected TextView gameOverTitle, scoreTextView, statsTextView, achievementsTextView, percentageTextView;
    protected MaterialButton playAgainButton, shareButton, backToMapButton;
    protected ProgressBar circularProgressBar;
    protected ImageView performanceBadge;
    protected View confettiView;
    
    // Data from Intent
    protected int score;
    protected int totalQuestions;
    protected int correctAnswers;
    protected int maxStreak;
    protected long totalTime;
    protected String achievements;
    protected String regionName;
    protected String gameType;
    protected String quizTitle;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        
        // Extract data from intent
        extractIntentData();
        
        // Initialize views
        initializeViews();
        
        // Set up content
        setupContent();
        
        // Set up button listeners
        setupButtonListeners();
    }
    
    /**
     * Extrage datele din Intent - poate fi override pentru date suplimentare
     */
    protected void extractIntentData() {
        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);
        totalQuestions = intent.getIntExtra("totalQuestions", 10);
        correctAnswers = intent.getIntExtra("correctAnswers", 0);
        maxStreak = intent.getIntExtra("maxStreak", 0);
        totalTime = intent.getLongExtra("totalTime", 0);
        achievements = intent.getStringExtra("ACHIEVEMENTS");
        regionName = intent.getStringExtra("regionName");
        gameType = intent.getStringExtra("gameType");
        quizTitle = intent.getStringExtra("quizTitle");
        
        // Set defaults if not provided
        if (regionName == null) regionName = getDefaultRegionName();
        if (gameType == null) gameType = "quiz";
        if (quizTitle == null) quizTitle = getDefaultQuizTitle();
        
        Log.d(TAG, "Game Over Data - Region: " + regionName + ", Score: " + score + ", Type: " + gameType);
    }
    
    /**
     * Inițializează view-urile - poate fi override pentru view-uri suplimentare
     */
    protected void initializeViews() {
        gameOverTitle = findViewById(R.id.gameOverTitle);
        scoreTextView = findViewById(R.id.scoreTextView);
        statsTextView = findViewById(R.id.statsTextView);
        achievementsTextView = findViewById(R.id.achievementsTextView);
        playAgainButton = findViewById(R.id.playAgainButton);
        shareButton = findViewById(R.id.shareButton);
        backToMapButton = findViewById(R.id.backToMapButton);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        percentageTextView = findViewById(R.id.percentageTextView);
        performanceBadge = findViewById(R.id.performanceBadge);
        confettiView = findViewById(R.id.confettiView);
        
        // Apply custom styling
        applyCustomStyling();
        applyButtonStyles();
    }
    
    /**
     * Configurează conținutul bazat pe datele primite
     */
    protected void setupContent() {
        // Set title
        if (gameOverTitle != null) {
            gameOverTitle.setText(getGameOverTitle());
        }
        
        // Set score with animation
        if (scoreTextView != null) {
            scoreTextView.setText(String.valueOf(score));
            scoreTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
        }
        
        // Set circular progress and percentage
        int percentage = totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;
        if (circularProgressBar != null) {
            circularProgressBar.setProgress(percentage);
        }
        if (percentageTextView != null) {
            percentageTextView.setText(percentage + "%");
        }
        
        // Set performance badge based on percentage
        if (performanceBadge != null) {
            if (percentage >= 90) {
                performanceBadge.setImageResource(R.drawable.achievement_badge_gold);
            } else if (percentage >= 70) {
                performanceBadge.setImageResource(R.drawable.achievement_badge_silver);
            } else {
                performanceBadge.setImageResource(R.drawable.achievement_badge_bronze);
            }
            performanceBadge.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up));
        }
        
        // Show confetti for excellent performance
        if (percentage >= 80 && confettiView != null) {
            confettiView.setVisibility(View.VISIBLE);
            // Aici ar trebui integrată o librărie de confetti
        }
        
        // Set statistics
        setupStatistics();
        
        // Set achievements
        setupAchievements();
    }
    
    /**
     * Configurează statisticile afișate
     */
    protected void setupStatistics() {
        if (statsTextView == null) return;
        
        int percentage = totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;
        
        StringBuilder statsBuilder = new StringBuilder();
        statsBuilder.append(String.format("✅ %d din %d răspunsuri corecte (%d%%)", 
                correctAnswers, totalQuestions, percentage));
        
        if (maxStreak > 0) {
            statsBuilder.append(String.format("\n🔥 Serie maximă: %d răspunsuri consecutive", maxStreak));
        }
        
        if (totalTime > 0) {
            String timeFormatted = formatTime(totalTime);
            statsBuilder.append(String.format("\n⏱️ Timp total: %s", timeFormatted));
        }
        
        // Add accuracy rating
        String accuracyRating = getAccuracyRating(percentage);
        statsBuilder.append(String.format("\n⭐ Evaluare: %s", accuracyRating));
        
        statsTextView.setText(statsBuilder.toString());
    }
    
    /**
     * Configurează realizările afișate
     */
    protected void setupAchievements() {
        if (achievementsTextView == null) return;
        
        String achievementText;
        if (achievements != null && !achievements.trim().isEmpty()) {
            achievementText = achievements;
        } else {
            achievementText = generateAchievements();
        }
        
        achievementsTextView.setText(achievementText);
    }
    
    /**
     * Generează realizările bazate pe performanță
     */
    protected String generateAchievements() {
        int percentage = totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;
        StringBuilder achievementsBuilder = new StringBuilder();
        
        // Performance achievements
        if (percentage == 100) {
            achievementsBuilder.append("🌟 Perfect! Ai răspuns corect la toate întrebările!\n");
        } else if (percentage >= 90) {
            achievementsBuilder.append(String.format("🏆 Maestru al %s (%d din %d corecte)\n", 
                    getRegionGenitive(), correctAnswers, totalQuestions));
        } else if (percentage >= 80) {
            achievementsBuilder.append(String.format("🎓 Expert al %s (%d din %d corecte)\n", 
                    getRegionGenitive(), correctAnswers, totalQuestions));
        } else if (percentage >= 60) {
            achievementsBuilder.append(String.format("📚 Cunoscător al %s (%d din %d corecte)\n", 
                    getRegionGenitive(), correctAnswers, totalQuestions));
        } else {
            achievementsBuilder.append(String.format("🗺️ Explorator al %s (%d din %d corecte)\n", 
                    getRegionGenitive(), correctAnswers, totalQuestions));
        }
        
        // Streak achievements
        if (maxStreak >= 7) {
            achievementsBuilder.append(String.format("🔥 Imparabil! (serie de %d răspunsuri consecutive)\n", maxStreak));
        } else if (maxStreak >= 5) {
            achievementsBuilder.append(String.format("⚡ Rapid și precis! (serie de %d răspunsuri corecte)\n", maxStreak));
        } else if (maxStreak >= 3) {
            achievementsBuilder.append(String.format("📈 Pe drumul cel bun! (serie de %d răspunsuri corecte)\n", maxStreak));
        }
        
        // Score achievements
        if (score >= 300) {
            achievementsBuilder.append(String.format("💎 Maestru collector! (%d puncte)\n", score));
        } else if (score >= 200) {
            achievementsBuilder.append(String.format("💰 Colecționar de puncte! (%d puncte)\n", score));
        } else if (score >= 100) {
            achievementsBuilder.append(String.format("🥉 Acumulator de puncte! (%d puncte)\n", score));
        }
        
        // Time-based achievements (if available)
        if (totalTime > 0) {
            long averageTimePerQuestion = totalTime / totalQuestions;
            if (averageTimePerQuestion < 10000) { // Under 10 seconds average
                achievementsBuilder.append("⚡ Rapid ca fulgerul! (timp mediu sub 10 secunde)\n");
            } else if (averageTimePerQuestion < 15000) { // Under 15 seconds average
                achievementsBuilder.append("🏃 Ritm excelent! (timp mediu sub 15 secunde)\n");
            }
        }
        
        String finalAchievements = achievementsBuilder.toString().trim();
        if (finalAchievements.isEmpty()) {
            finalAchievements = String.format("🎯 Ai participat la %s!\n📖 Continuă să înveți despre această regiune fascinantă!", quizTitle);
        }
        
        return finalAchievements;
    }
    
    /**
     * Formatează timpul în format citibil
     */
    protected String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%d minute și %d secunde", minutes, seconds);
        } else {
            return String.format("%d secunde", seconds);
        }
    }
    
    /**
     * Returnează evaluarea bazată pe procentaj
     */
    protected String getAccuracyRating(int percentage) {
        if (percentage == 100) return "Perfect! 🌟";
        if (percentage >= 90) return "Excelent! 🏆";
        if (percentage >= 80) return "Foarte bine! 👏";
        if (percentage >= 70) return "Bine! 👍";
        if (percentage >= 60) return "Decent! 📈";
        return "Încearcă din nou! 💪";
    }
    
    /**
     * Aplică stiluri personalizate
     */
    protected void applyCustomStyling() {
        Typeface customTypeface = Typeface.create("sans-serif-medium", Typeface.BOLD);
        if (gameOverTitle != null) {
            gameOverTitle.setTypeface(customTypeface);
        }
        if (scoreTextView != null) {
            scoreTextView.setTypeface(customTypeface);
        }
    }
    
    /**
     * Aplică stiluri pentru butoane
     */
    protected void applyButtonStyles() {
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
    
    /**
     * Configurează listener-ii pentru butoane
     */
    protected void setupButtonListeners() {
        // Play Again button
        if (playAgainButton != null) {
            playAgainButton.setOnClickListener(v -> onPlayAgainClicked());
        }
        
        // Share button
        if (shareButton != null) {
            shareButton.setOnClickListener(v -> onShareClicked());
        }
        
        // Back to Map button
        if (backToMapButton != null) {
            backToMapButton.setOnClickListener(v -> onBackToMapClicked());
        }
    }
    
    /**
     * Handler pentru butonul "Joacă din nou"
     */
    protected void onPlayAgainClicked() {
        Intent intent = getPlayAgainIntent();
        if (intent != null) {
            startActivity(intent);
            finish();
        } else {
            Log.w(TAG, "Play again intent not implemented for " + regionName);
            finish();
        }
    }
    
    /**
     * Handler pentru butonul "Share"
     */
    protected void onShareClicked() {
        String scoreText = scoreTextView != null ? scoreTextView.getText().toString() : "0";
        String statsText = statsTextView != null ? statsTextView.getText().toString() : "";
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getShareSubject());
        shareIntent.putExtra(Intent.EXTRA_TEXT, getShareText(scoreText, statsText));
        
        startActivity(Intent.createChooser(shareIntent, "Distribuie rezultatul prin"));
    }
    
    /**
     * Handler pentru butonul "Înapoi la hartă"
     */
    protected void onBackToMapClicked() {
        finish();
    }
    
    /**
     * Returnează Intent pentru a juca din nou - trebuie implementat în subclase
     */
    protected abstract Intent getPlayAgainIntent();
    
    /**
     * Returnează ID-ul resursei layout - poate fi override pentru layout-uri personalizate
     */
    protected int getLayoutResourceId() {
        return R.layout.activity_game_over;
    }
    
    /**
     * Returnează numele regiunii - trebuie implementat în subclase
     */
    protected abstract String getDefaultRegionName();
    
    /**
     * Returnează titlul quiz-ului - trebuie implementat în subclase
     */
    protected abstract String getDefaultQuizTitle();
    
    /**
     * Returnează titlul pentru Game Over
     */
    protected String getGameOverTitle() {
        return "🏆 Rezultatele Tale";
    }
    
    /**
     * Returnează forma genitivă a regiunii pentru realizări
     */
    protected abstract String getRegionGenitive();
    
    /**
     * Returnează subiectul pentru share
     */
    protected String getShareSubject() {
        return String.format("Scorul meu la %s", quizTitle);
    }
    
    /**
     * Returnează textul pentru share
     */
    protected String getShareText(String scoreText, String statsText) {
        return String.format(
            "🏆 Am obținut %s puncte la %s!\n\n" +
            "📊 %s\n\n" +
            "🗺️ Testează-ți și tu cunoștințele despre România! #ExplorandRomania #Quiz%s",
            scoreText, 
            quizTitle,
            statsText.replaceAll("\n", " | "),
            regionName.replaceAll("\\s+", "")
        );
    }
    
    /**
     * Returnează maparea regiune -> configurații pentru modularitate
     */
    public static Map<String, RegionConfig> getRegionConfigs() {
        Map<String, RegionConfig> configs = new HashMap<>();
        
        configs.put("transilvania", new RegionConfig(
            "Transilvania", 
            "Transilvaniei", 
            "Quiz Transilvania",
            "com.example.myapplication.transilvaniausage.TransilvaniaGameActivity"
        ));
        
        configs.put("banat", new RegionConfig(
            "Banat", 
            "Banatului", 
            "Quiz Banat",
            "com.example.myapplication.banatusage.BanatGameActivity"
        ));
        
        configs.put("bucovina", new RegionConfig(
            "Bucovina", 
            "Bucovinei", 
            "Quiz Bucovina",
            "com.example.myapplication.bucovinausage.BucovinaGameActivity"
        ));
        
        configs.put("crisana", new RegionConfig(
            "Crișana", 
            "Crișanei", 
            "Quiz Crișana",
            "com.example.myapplication.crisanausage.CrisanaGameActivity"
        ));
        
        configs.put("dobrogea", new RegionConfig(
            "Dobrogea", 
            "Dobrogei", 
            "Quiz Dobrogea",
            "com.example.myapplication.dobrogeausage.DobrogeaGameActivity"
        ));
        
        configs.put("maramures", new RegionConfig(
            "Maramureș", 
            "Maramureșului", 
            "Quiz Maramureș",
            "com.example.myapplication.maramuresusage.MaramuresGameActivity"
        ));
        
        configs.put("moldova", new RegionConfig(
            "Moldova", 
            "Moldovei", 
            "Quiz Moldova",
            "com.example.myapplication.moldovausage.MoldovaGameActivity"
        ));
        
        configs.put("muntenia", new RegionConfig(
            "Muntenia", 
            "Munteniei", 
            "Quiz Muntenia",
            "com.example.myapplication.munteniausage.MunteniaGameActivity"
        ));
        
        configs.put("oltenia", new RegionConfig(
            "Oltenia", 
            "Olteniei", 
            "Quiz Oltenia",
            "com.example.myapplication.olteniausage.OlteniaGameActivity"
        ));
        
        return configs;
    }
    
    /**
     * Clasa de configurare pentru fiecare regiune
     */
    public static class RegionConfig {
        public final String displayName;
        public final String genitiveName;
        public final String quizTitle;
        public final String gameActivityClass;
        
        public RegionConfig(String displayName, String genitiveName, String quizTitle, String gameActivityClass) {
            this.displayName = displayName;
            this.genitiveName = genitiveName;
            this.quizTitle = quizTitle;
            this.gameActivityClass = gameActivityClass;
        }
    }
} 