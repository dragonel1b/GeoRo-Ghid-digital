package com.example.myapplication.crisanausage;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.core.domain.model.EnhancedQuestionModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import java.util.Map;
import java.util.List;

/**
 * Activitate pentru afișarea statisticilor detaliate ale jucătorului în quiz-ul Crișana
 */
public class PlayerStatsActivity extends AppCompatActivity {
    
    private PlayerProgressTracker progressTracker;
    
    // Views pentru statistici generale
    private TextView totalGamesText;
    private TextView totalQuestionsText;
    private TextView accuracyText;
    private TextView currentStreakText;
    private TextView bestStreakText;
    private TextView totalTimeText;
    
    // Views pentru statistici pe categorii
    private TextView categoryStatsText;
    
    // Views pentru statistici pe dificultăți
    private TextView difficultyStatsText;
    
    // Views pentru statistici moduri de joc
    private TextView gameModeStatsText;
    
    // Views pentru recomandări
    private TextView recommendationsText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_stats);
        
        initializeComponents();
        setupToolbar();
        loadAllStats();
    }
    
    private void initializeComponents() {
        progressTracker = new PlayerProgressTracker(this);
        
        // Inițializează view-urile
        totalGamesText = findViewById(R.id.totalGamesText);
        totalQuestionsText = findViewById(R.id.totalQuestionsText);
        accuracyText = findViewById(R.id.accuracyText);
        currentStreakText = findViewById(R.id.currentStreakText);
        bestStreakText = findViewById(R.id.bestStreakText);
        totalTimeText = findViewById(R.id.totalTimeText);
        
        categoryStatsText = findViewById(R.id.categoryStatsText);
        difficultyStatsText = findViewById(R.id.difficultyStatsText);
        gameModeStatsText = findViewById(R.id.gameModeStatsText);
        recommendationsText = findViewById(R.id.recommendationsText);
    }
    
    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Statistici Jucător - Crișana");
        }
    }
    
    private void loadAllStats() {
        loadGeneralStats();
        loadCategoryStats();
        loadDifficultyStats();
        loadGameModeStats();
        loadRecommendations();
    }
    
    private void loadGeneralStats() {
        totalGamesText.setText(String.valueOf(progressTracker.getTotalGames()));
        totalQuestionsText.setText(String.valueOf(progressTracker.getTotalQuestions()));
        accuracyText.setText(Math.round(progressTracker.getOverallAccuracy() * 100) + "%");
        currentStreakText.setText(progressTracker.getCurrentStreak() + " zile");
        bestStreakText.setText(progressTracker.getBestStreak() + " zile");
        
        // Convertește timpul în format citibil
        long totalTimeMs = progressTracker.getTotalTimeSpent();
        long hours = totalTimeMs / (1000 * 60 * 60);
        long minutes = (totalTimeMs % (1000 * 60 * 60)) / (1000 * 60);
        totalTimeText.setText(hours + "h " + minutes + "m");
    }
    
    private void loadCategoryStats() {
        Map<EnhancedQuestionModel.Category, PlayerProgressTracker.CategoryStats> categoryStats = 
            progressTracker.getCategoryStats();
        
        StringBuilder statsBuilder = new StringBuilder();
        
        if (categoryStats.isEmpty()) {
            statsBuilder.append("Nu există încă statistici pe categorii.\nJoacă câteva quiz-uri pentru a vedea progresul!");
        } else {
            for (Map.Entry<EnhancedQuestionModel.Category, PlayerProgressTracker.CategoryStats> entry : 
                 categoryStats.entrySet()) {
                
                EnhancedQuestionModel.Category category = entry.getKey();
                PlayerProgressTracker.CategoryStats stats = entry.getValue();
                
                statsBuilder.append("📚 ").append(category.displayName).append("\n");
                statsBuilder.append("   Întrebări: ").append(stats.totalQuestions).append("\n");
                statsBuilder.append("   Corecte: ").append(stats.correctAnswers).append("\n");
                statsBuilder.append("   Acuratețe: ").append(Math.round(stats.accuracy * 100)).append("%\n");
                statsBuilder.append("   Timp mediu: ").append(stats.averageTime / 1000).append("s\n\n");
            }
        }
        
        categoryStatsText.setText(statsBuilder.toString());
    }
    
    private void loadDifficultyStats() {
        Map<EnhancedQuestionModel.Difficulty, PlayerProgressTracker.DifficultyStats> difficultyStats = 
            progressTracker.getDifficultyStats();
        
        StringBuilder statsBuilder = new StringBuilder();
        
        if (difficultyStats.isEmpty()) {
            statsBuilder.append("Nu există încă statistici pe dificultăți.\nJoacă câteva quiz-uri pentru a vedea progresul!");
        } else {
            for (Map.Entry<EnhancedQuestionModel.Difficulty, PlayerProgressTracker.DifficultyStats> entry : 
                 difficultyStats.entrySet()) {
                
                EnhancedQuestionModel.Difficulty difficulty = entry.getKey();
                PlayerProgressTracker.DifficultyStats stats = entry.getValue();
                
                String emoji = getDifficultyEmoji(difficulty);
                statsBuilder.append(emoji).append(" ").append(difficulty.displayName).append("\n");
                statsBuilder.append("   Întrebări: ").append(stats.totalQuestions).append("\n");
                statsBuilder.append("   Corecte: ").append(stats.correctAnswers).append("\n");
                statsBuilder.append("   Acuratețe: ").append(Math.round(stats.accuracy * 100)).append("%\n");
                statsBuilder.append("   Timp mediu: ").append(stats.averageTime / 1000).append("s\n\n");
            }
        }
        
        difficultyStatsText.setText(statsBuilder.toString());
    }
    
    private void loadGameModeStats() {
        Map<String, PlayerProgressTracker.GameModeStats> gameModeStats = 
            progressTracker.getGameModeStats();
        
        StringBuilder statsBuilder = new StringBuilder();
        
        if (gameModeStats.isEmpty()) {
            statsBuilder.append("Nu există încă statistici pe moduri de joc.\nÎncearcă diferite moduri pentru a vedea progresul!");
        } else {
            for (Map.Entry<String, PlayerProgressTracker.GameModeStats> entry : 
                 gameModeStats.entrySet()) {
                
                String gameMode = entry.getKey();
                PlayerProgressTracker.GameModeStats stats = entry.getValue();
                
                statsBuilder.append("🎮 ").append(gameMode).append("\n");
                statsBuilder.append("   Jocuri: ").append(stats.gamesPlayed).append("\n");
                statsBuilder.append("   Cel mai bun scor: ").append(stats.bestScore).append("\n");
                statsBuilder.append("   Scor mediu: ").append(Math.round(stats.averageScore * 100)).append("%\n");
                statsBuilder.append("   Timp mediu: ").append(stats.averageTime / 1000).append("s\n\n");
            }
        }
        
        gameModeStatsText.setText(statsBuilder.toString());
    }
    
    private void loadRecommendations() {
        List<String> recommendations = progressTracker.getLearningRecommendations();
        
        StringBuilder recommendationsBuilder = new StringBuilder();
        
        if (recommendations.isEmpty()) {
            recommendationsBuilder.append("💡 Joacă mai multe quiz-uri pentru a primi recomandări personalizate de învățare!");
        } else {
            for (int i = 0; i < recommendations.size(); i++) {
                recommendationsBuilder.append("💡 ").append(recommendations.get(i));
                if (i < recommendations.size() - 1) {
                    recommendationsBuilder.append("\n\n");
                }
            }
        }
        
        recommendationsText.setText(recommendationsBuilder.toString());
    }
    
    private String getDifficultyEmoji(EnhancedQuestionModel.Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return "🟢";
            case MEDIUM:
                return "🟡";
            case HARD:
                return "🟠";
            case EXPERT:
                return "🔴";
            default:
                return "⚪";
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 