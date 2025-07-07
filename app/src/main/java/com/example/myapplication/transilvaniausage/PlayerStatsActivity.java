package com.example.myapplication.transilvaniausage;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;

/**
 * Activity to display detailed player statistics for Transilvania quiz
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
            getSupportActionBar().setTitle("Player Statistics");
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
        overallStats.append("🎯 Total Games Played: ").append(quizStats.totalGamesPlayed).append("\n");
        overallStats.append("📊 Overall Accuracy: ").append(String.format("%.1f%%", quizStats.overallAccuracy * 100)).append("\n");
        overallStats.append("⏱️ Average Time per Question: ").append(String.format("%.1fs", quizStats.averageTimePerQuestion / 1000.0)).append("\n");
        overallStats.append("🔥 Best Streak: ").append(quizStats.longestStreak).append("\n");
        overallStats.append("📝 Total Questions Answered: ").append(quizStats.totalQuestionsAnswered).append("\n");
        overallStats.append("✅ Total Correct Answers: ").append(quizStats.totalCorrectAnswers);
        
        overallStatsText.setText(overallStats.toString());
        
        // Load category statistics
        StringBuilder categoryStats = new StringBuilder();
        categoryStats.append("📚 Category Performance:\n\n");
        
        for (String category : new String[]{"HISTORY", "GEOGRAPHY", "CULTURE", "ARCHITECTURE", 
                                          "GASTRONOMY", "LEGENDS", "PERSONALITIES", "NATURE"}) {
            PlayerProgressTracker.CategoryStats stats = quizStats.categoryStats.get(category);
            if (stats != null && stats.questionsAnswered > 0) {
                categoryStats.append("• ").append(category).append(": ")
                           .append(stats.correctAnswers).append("/").append(stats.questionsAnswered)
                           .append(" (").append(String.format("%.1f%%", stats.accuracy * 100)).append(")\n");
            }
        }
        
        if (categoryStats.length() == "📚 Category Performance:\n\n".length()) {
            categoryStats.append("No category data available yet. Play some quizzes to see your performance!");
        }
        
        categoryStatsText.setText(categoryStats.toString());
        
        // Load difficulty statistics
        StringBuilder difficultyStats = new StringBuilder();
        difficultyStats.append("⚡ Difficulty Performance:\n\n");
        
        for (String difficulty : new String[]{"EASY", "MEDIUM", "HARD", "EXPERT"}) {
            PlayerProgressTracker.DifficultyStats stats = quizStats.difficultyStats.get(difficulty);
            if (stats != null && stats.questionsAnswered > 0) {
                difficultyStats.append("• ").append(difficulty).append(": ")
                            .append(stats.correctAnswers).append("/").append(stats.questionsAnswered)
                            .append(" (").append(String.format("%.1f%%", stats.accuracy * 100)).append(")\n");
            }
        }
        
        if (difficultyStats.length() == "⚡ Difficulty Performance:\n\n".length()) {
            difficultyStats.append("No difficulty data available yet. Play some quizzes to see your performance!");
        }
        
        difficultyStatsText.setText(difficultyStats.toString());
        
        // Load learning recommendations and achievements
        StringBuilder achievements = new StringBuilder();
        achievements.append("🏆 Learning Recommendations:\n\n");
        
        // Get learning recommendations from the QuizStats
        if (quizStats.recommendations != null && !quizStats.recommendations.isEmpty()) {
            for (PlayerProgressTracker.LearningRecommendation rec : quizStats.recommendations) {
                achievements.append("• ").append(rec.title).append("\n")
                          .append("  ").append(rec.description).append("\n\n");
            }
        } else {
            achievements.append("No recommendations available yet. Play more quizzes to get personalized suggestions!");
        }
        
        // Add weak and strong topics
        if (!quizStats.weakTopics.isEmpty()) {
            achievements.append("\n📉 Areas for Improvement:\n");
            for (String topic : quizStats.weakTopics) {
                achievements.append("• ").append(topic).append("\n");
            }
        }
        
        if (!quizStats.strongTopics.isEmpty()) {
            achievements.append("\n📈 Your Strong Areas:\n");
            for (String topic : quizStats.strongTopics) {
                achievements.append("• ").append(topic).append("\n");
            }
        }
        
        achievementsText.setText(achievements.toString());
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 