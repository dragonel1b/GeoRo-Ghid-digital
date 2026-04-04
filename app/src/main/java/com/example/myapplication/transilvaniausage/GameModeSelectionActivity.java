package com.example.myapplication.transilvaniausage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.core.domain.model.EnhancedQuestionModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

/**
 * Activitate pentru selectarea modului de joc și configurarea opțiunilor avansate
 */
public class GameModeSelectionActivity extends AppCompatActivity {
    
    private RecyclerView gameModeRecyclerView;
    private Spinner categorySpinner;
    private Spinner difficultySpinner;
    private TextView playerStatsTextView;
    private TextView recommendationsTextView;
    private MaterialButton startGameButton;
    private MaterialButton viewStatsButton;
    
    private DifficultyManager difficultyManager;
    private PlayerProgressTracker progressTracker;
    private GameModeAdapter gameModeAdapter;
    private GameModeManager.GameMode selectedGameMode = GameModeManager.GameMode.CLASSIC;
    private EnhancedQuestionModel.Category selectedCategory = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode_selection);
        
        initializeSystems();
        initializeViews();
        setupGameModeSelector();
        setupSpinners();
        loadPlayerStats();
        setupButtons();
    }
    
    private void initializeSystems() {
        difficultyManager = new DifficultyManager(this);
        progressTracker = new PlayerProgressTracker(this);
    }
    
    private void initializeViews() {
        gameModeRecyclerView = findViewById(R.id.gameModeRecyclerView);
        categorySpinner = findViewById(R.id.categorySpinner);
        difficultySpinner = findViewById(R.id.difficultySpinner);
        playerStatsTextView = findViewById(R.id.playerStatsTextView);
        recommendationsTextView = findViewById(R.id.recommendationsTextView);
        startGameButton = findViewById(R.id.startGameButton);
        viewStatsButton = findViewById(R.id.viewStatsButton);
    }
    
    private void setupGameModeSelector() {
        List<GameModeItem> gameModes = new ArrayList<>();
        
        for (GameModeManager.GameMode mode : GameModeManager.GameMode.values()) {
            gameModes.add(new GameModeItem(mode));
        }
        
        gameModeAdapter = new GameModeAdapter(gameModes, this::onGameModeSelected);
        gameModeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        gameModeRecyclerView.setAdapter(gameModeAdapter);
    }
    
    private void setupSpinners() {
        // Category spinner
        List<String> categories = new ArrayList<>();
        categories.add("Toate categoriile");
        for (EnhancedQuestionModel.Category category : EnhancedQuestionModel.Category.values()) {
            categories.add(category.emoji + " " + category.displayName);
        }
        
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        
        // Difficulty spinner
        List<String> difficulties = new ArrayList<>();
        difficulties.add("Dificultate automată");
        for (DifficultyManager.DifficultyLevel level : DifficultyManager.DifficultyLevel.values()) {
            difficulties.add(level.displayName + " - " + level.description);
        }
        
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, difficulties);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);
        
        // Set current difficulty as selected
        DifficultyManager.DifficultyLevel currentDifficulty = difficultyManager.getCurrentDifficulty();
        int difficultyIndex = currentDifficulty.ordinal() + 1; // +1 because first item is "auto"
        difficultySpinner.setSelection(difficultyIndex);
    }
    
    private void loadPlayerStats() {
        PlayerProgressTracker.QuizStats stats = progressTracker.getCurrentStats();
        
        StringBuilder statsText = new StringBuilder();
        statsText.append("📊 Statistici personale:\n\n");
        
        if (stats.totalGamesPlayed > 0) {
            statsText.append("🎮 Jocuri jucate: ").append(stats.totalGamesPlayed).append("\n");
            statsText.append("🎯 Acuratețe: ").append(String.format("%.1f%%", stats.overallAccuracy * 100)).append("\n");
            statsText.append("🔥 Cel mai lung streak: ").append(stats.longestStreak).append("\n");
            statsText.append("⏱️ Timp mediu/întrebare: ").append(String.format("%.1fs", stats.averageTimePerQuestion / 1000)).append("\n\n");
            
            // Show category performance
            statsText.append("📚 Performanță pe categorii:\n");
            for (PlayerProgressTracker.CategoryStats categoryStats : stats.categoryStats.values()) {
                if (categoryStats.questionsAnswered >= 3) {
                    String status = categoryStats.isStrong ? " 💪" : (categoryStats.isWeak ? " 📈" : " ⚖️");
                    statsText.append("• ").append(categoryStats.categoryName)
                             .append(": ").append(String.format("%.0f%%", categoryStats.accuracy * 100))
                             .append(status).append("\n");
                }
            }
        } else {
            statsText.append("🌟 Bine ai venit!\n");
            statsText.append("Acesta va fi primul tău quiz din Transilvania.\n");
            statsText.append("Succes!");
        }
        
        playerStatsTextView.setText(statsText.toString());
        
        // Load recommendations
        loadRecommendations(stats);
    }
    
    private void loadRecommendations(PlayerProgressTracker.QuizStats stats) {
        StringBuilder recommendationsText = new StringBuilder();
        recommendationsText.append("💡 Recomandări personalizate:\n\n");
        
        if (stats.recommendations.isEmpty()) {
            recommendationsText.append("Joacă câteva quiz-uri pentru a primi recomandări personalizate!");
        } else {
            for (PlayerProgressTracker.LearningRecommendation rec : stats.recommendations) {
                recommendationsText.append("• ").append(rec.title).append("\n");
                recommendationsText.append("  ").append(rec.description).append("\n\n");
            }
        }
        
        // Add difficulty recommendation
        DifficultyManager.DifficultyLevel recommendedDifficulty = difficultyManager.getCurrentDifficulty();
        recommendationsText.append("🎯 Dificultate recomandată: ").append(recommendedDifficulty.displayName).append("\n");
        recommendationsText.append("📝 ").append(difficultyManager.getPerformanceRecommendation());
        
        recommendationsTextView.setText(recommendationsText.toString());
    }
    
    private void setupButtons() {
        startGameButton.setOnClickListener(v -> startSelectedGame());
        viewStatsButton.setOnClickListener(v -> openStatsActivity());
    }
    
    private void onGameModeSelected(GameModeManager.GameMode gameMode) {
        selectedGameMode = gameMode;
        gameModeAdapter.setSelectedMode(gameMode);
        
        // Update UI based on selected mode
        updateUIForSelectedMode(gameMode);
    }
    
    private void updateUIForSelectedMode(GameModeManager.GameMode gameMode) {
        // Enable/disable category selection based on game mode
        boolean categorySelectionEnabled = gameMode == GameModeManager.GameMode.CATEGORY_FOCUS;
        categorySpinner.setEnabled(categorySelectionEnabled);
        
        // Update start button text
        startGameButton.setText("🚀 Începe " + gameMode.displayName);
    }
    
    private void startSelectedGame() {
        Intent intent = new Intent(this, TransilvaniaGameActivity.class);
        
        // Add game mode
        intent.putExtra("GAME_MODE", selectedGameMode.name());
        
        // Add category if selected
        if (categorySpinner.getSelectedItemPosition() > 0) {
            EnhancedQuestionModel.Category category = EnhancedQuestionModel.Category.values()[
                categorySpinner.getSelectedItemPosition() - 1];
            intent.putExtra("FOCUS_CATEGORY", category.name());
        }
        
        // Add manual difficulty if selected
        if (difficultySpinner.getSelectedItemPosition() > 0) {
            DifficultyManager.DifficultyLevel difficulty = DifficultyManager.DifficultyLevel.values()[
                difficultySpinner.getSelectedItemPosition() - 1];
            intent.putExtra("MANUAL_DIFFICULTY", difficulty.name());
        }
        
        startActivity(intent);
    }
    
    private void openStatsActivity() {
        // TODO: Implement detailed stats activity
        Intent intent = new Intent(this, PlayerStatsActivity.class);
        startActivity(intent);
    }
    
    /**
     * Data class pentru game mode items
     */
    public static class GameModeItem {
        public GameModeManager.GameMode gameMode;
        public boolean isSelected;
        
        public GameModeItem(GameModeManager.GameMode gameMode) {
            this.gameMode = gameMode;
            this.isSelected = false;
        }
    }
    
    /**
     * Adapter pentru game mode selection
     */
    public static class GameModeAdapter extends RecyclerView.Adapter<GameModeAdapter.ViewHolder> {
        private List<GameModeItem> gameModes;
        private OnGameModeSelectedListener listener;
        private GameModeManager.GameMode selectedMode = GameModeManager.GameMode.CLASSIC;
        
        public interface OnGameModeSelectedListener {
            void onGameModeSelected(GameModeManager.GameMode gameMode);
        }
        
        public GameModeAdapter(List<GameModeItem> gameModes, OnGameModeSelectedListener listener) {
            this.gameModes = gameModes;
            this.listener = listener;
        }
        
        public void setSelectedMode(GameModeManager.GameMode mode) {
            this.selectedMode = mode;
            notifyDataSetChanged();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game_mode, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            GameModeItem item = gameModes.get(position);
            GameModeManager.GameMode mode = item.gameMode;
            boolean isSelected = mode == selectedMode;
            
            holder.titleTextView.setText(mode.emoji + " " + mode.displayName);
            holder.descriptionTextView.setText(mode.description);
            
            // Update card appearance based on selection
            if (isSelected) {
                holder.cardView.setCardBackgroundColor(
                    android.graphics.Color.parseColor("#E3F2FD"));
                holder.cardView.setStrokeColor(
                    android.graphics.Color.parseColor("#2196F3"));
                holder.cardView.setStrokeWidth(4);
            } else {
                holder.cardView.setCardBackgroundColor(
                    android.graphics.Color.parseColor("#FFFFFF"));
                holder.cardView.setStrokeColor(
                    android.graphics.Color.parseColor("#E0E0E0"));
                holder.cardView.setStrokeWidth(2);
            }
            
            holder.cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGameModeSelected(mode);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return gameModes.size();
        }
        
        public static class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardView;
            TextView titleTextView;
            TextView descriptionTextView;
            
            public ViewHolder(View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.gameModeCard);
                titleTextView = itemView.findViewById(R.id.gameModeTitle);
                descriptionTextView = itemView.findViewById(R.id.gameModeDescription);
            }
        }
    }
} 