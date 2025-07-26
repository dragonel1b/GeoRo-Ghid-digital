package com.example.myapplication.moldovausage;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;
import com.example.myapplication.transilvaniausage.PlayerProgressTracker;
import java.util.Map;

/**
 * Activity to display detailed player statistics for Moldova quiz
 */
public class PlayerStatsActivity extends AppCompatActivity {
    
    private PlayerProgressTracker progressTracker;
    private TextView overallStatsText;
    private TextView categoryStatsText;
    private TextView difficultyStatsText;
    private TextView achievementsText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_stats);
        
        setupToolbar();
        initializeViews();
        loadPlayerStats();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Statistici Moldova");
        }
    }
    
    private void initializeViews() {
        overallStatsText = findViewById(R.id.overallStatsText);
        categoryStatsText = findViewById(R.id.categoryStatsText);
        difficultyStatsText = findViewById(R.id.difficultyStatsText);
        achievementsText = findViewById(R.id.achievementsText);
        
        progressTracker = new PlayerProgressTracker(this);
    }
    
    private void loadPlayerStats() {
        // Load overall statistics using the actual QuizStats class
        PlayerProgressTracker.QuizStats quizStats = progressTracker.getCurrentStats();
        StringBuilder overallStats = new StringBuilder();
        overallStats.append("🎯 Total Jocuri Jucate: ").append(quizStats.totalGamesPlayed).append("\n");
        overallStats.append("📊 Acuratețe Generală: ").append(String.format("%.1f%%", quizStats.overallAccuracy * 100)).append("\n");
        overallStats.append("⏱️ Timp Mediu per Întrebare: ").append(String.format("%.1fs", quizStats.averageTimePerQuestion / 1000.0)).append("\n");
        overallStats.append("🔥 Cea Mai Bună Serie: ").append(quizStats.longestStreak).append("\n");
        overallStats.append("📝 Total Întrebări Răspunse: ").append(quizStats.totalQuestionsAnswered).append("\n");
        overallStats.append("✅ Total Răspunsuri Corecte: ").append(quizStats.totalCorrectAnswers);
        
        overallStatsText.setText(overallStats.toString());
        
        // Load category statistics
        StringBuilder categoryStats = new StringBuilder();
        categoryStats.append("📚 Performanță pe Categorii:\n\n");
        
        Map<String, PlayerProgressTracker.CategoryStats> categoryStatsMap = quizStats.categoryStats;
        for (Map.Entry<String, PlayerProgressTracker.CategoryStats> entry : categoryStatsMap.entrySet()) {
            String category = entry.getKey();
            PlayerProgressTracker.CategoryStats stats = entry.getValue();
            if (stats != null && stats.questionsAnswered > 0) {
                String categoryName = getCategoryNameInRomanian(category);
                categoryStats.append("• ").append(categoryName).append(": ")
                           .append(stats.correctAnswers).append("/").append(stats.questionsAnswered)
                           .append(" (").append(String.format("%.1f%%", stats.accuracy * 100)).append(")\n");
            }
        }
        
        if (categoryStats.length() == "📚 Performanță pe Categorii:\n\n".length()) {
            categoryStats.append("Nu sunt date de categorii disponibile încă. Joacă câteva quiz-uri pentru a vedea performanța ta!");
        }
        
        categoryStatsText.setText(categoryStats.toString());
        
        // Load difficulty statistics (simplified since we don't have difficulty stats method)
        StringBuilder difficultyStats = new StringBuilder();
        difficultyStats.append("⚡ Performanță pe Dificultăți:\n\n");
        difficultyStats.append("Nu sunt date de dificultăți disponibile încă. Joacă câteva quiz-uri pentru a vedea performanța ta!");
        difficultyStatsText.setText(difficultyStats.toString());
        
        // Load achievements
        StringBuilder achievements = new StringBuilder();
        achievements.append("🏆 Realizări:\n\n");
        
        // Check for achievements based on available stats
        if (quizStats.longestStreak >= 5) {
            achievements.append("• 🔥 Streak record: ").append(quizStats.longestStreak).append(" răspunsuri consecutive\n");
        }
        if (quizStats.overallAccuracy >= 0.8f) {
            achievements.append("• 🎯 Acuratețe excelentă: ").append(String.format("%.1f%%", quizStats.overallAccuracy * 100)).append("\n");
        }
        if (quizStats.totalGamesPlayed >= 10) {
            achievements.append("• 🎮 Jucător dedicat: ").append(quizStats.totalGamesPlayed).append(" jocuri jucate\n");
        }
        if (quizStats.totalCorrectAnswers >= 50) {
            achievements.append("• ✅ Expert: ").append(quizStats.totalCorrectAnswers).append(" răspunsuri corecte\n");
        }
        
        if (achievements.length() == "🏆 Realizări:\n\n".length()) {
            achievements.append("🎮 Începe să joci pentru a câștiga realizări!");
        }
        
        achievementsText.setText(achievements.toString());
    }
    
    private String getCategoryNameInRomanian(String category) {
        switch (category) {
            case "HISTORY": return "Istorie";
            case "GEOGRAPHY": return "Geografie";
            case "CULTURE": return "Cultură";
            case "ARCHITECTURE": return "Arhitectură";
            case "GASTRONOMY": return "Gastronomie";
            case "LEGENDS": return "Legende";
            case "PERSONALITIES": return "Personalități";
            case "NATURE": return "Natură";
            default: return category;
        }
    }
    
    private String getDifficultyNameInRomanian(String difficulty) {
        switch (difficulty) {
            case "EASY": return "Ușor";
            case "MEDIUM": return "Mediu";
            case "HARD": return "Greu";
            case "EXPERT": return "Expert";
            default: return difficulty;
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 