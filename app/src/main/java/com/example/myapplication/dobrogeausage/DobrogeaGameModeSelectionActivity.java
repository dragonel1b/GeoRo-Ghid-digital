package com.example.myapplication.dobrogeausage;

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
import com.example.myapplication.models.EnhancedQuestionModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

/**
 * Activitate pentru selectarea modului de joc și configurarea opțiunilor avansate pentru Dobrogea
 * Tematică specifică: Marea Neagră, Delta Dunării, navigație
 */
public class DobrogeaGameModeSelectionActivity extends AppCompatActivity {
    
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
        setContentView(R.layout.activity_dobrogea_game_mode_selection);
        
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
        // Category spinner cu tematică maritimă
        List<String> categories = new ArrayList<>();
        categories.add("🌊 Toate categoriile");
        categories.add("🏛️ Istorie și Arheologie");
        categories.add("🗺️ Geografia Dobrogei");
        categories.add("🦢 Natura și Delta");
        categories.add("🏰 Arhitectură");
        categories.add("🎣 Cultură Maritimă");
        categories.add("🍽️ Gastronomie");
        categories.add("👑 Personalități");
        categories.add("🐉 Legende");
        
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        
        // Difficulty spinner cu ranguri maritime
        List<String> difficulties = new ArrayList<>();
        difficulties.add("🧭 Dificultate automată");
        for (DifficultyManager.DifficultyLevel level : DifficultyManager.DifficultyLevel.values()) {
            difficulties.add(level.emoji + " " + level.displayName + " - " + level.description);
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
        statsText.append("🌊 Statistici Maritime Dobrogea:\n\n");
        
        if (stats.totalGamesPlayed > 0) {
            statsText.append("⚓ Jocuri jucate: ").append(stats.totalGamesPlayed).append("\n");
            statsText.append("🎯 Acuratețe: ").append(String.format("%.1f%%", stats.overallAccuracy * 100)).append("\n");
            statsText.append("🔥 Cel mai lung streak: ").append(stats.longestStreak).append("\n");
            statsText.append("⏱️ Timp mediu/întrebare: ").append(String.format("%.1fs", stats.averageTimePerQuestion / 1000)).append("\n\n");
            
            // Show specialized Dobrogea stats
            statsText.append("🦢 Delta Dunării: ");
            if (stats.deltaQuestionsAnswered > 0) {
                statsText.append(String.format("%.0f%% acuratețe (%d întrebări)", 
                    stats.deltaAccuracy * 100, stats.deltaQuestionsAnswered));
            } else {
                statsText.append("Neexplorat");
            }
            statsText.append("\n");
            
            statsText.append("⚓ Tema Maritimă: ");
            if (stats.maritimeQuestionsAnswered > 0) {
                statsText.append(String.format("%.0f%% acuratețe (%d întrebări)", 
                    stats.maritimeAccuracy * 100, stats.maritimeQuestionsAnswered));
            } else {
                statsText.append("Neexplorat");
            }
            statsText.append("\n");
            
            statsText.append("🏛️ Arheologie: ");
            if (stats.archaeologyQuestionsAnswered > 0) {
                statsText.append(String.format("%.0f%% acuratețe (%d întrebări)", 
                    stats.archaeologyAccuracy * 100, stats.archaeologyQuestionsAnswered));
            } else {
                statsText.append("Neexplorat");
            }
            statsText.append("\n");
            
        } else {
            statsText.append("🌟 Bine ai venit pe litoralul cunoștințelor!\n");
            statsText.append("Acesta va fi primul tău quiz din Dobrogea.\n");
            statsText.append("Explorează tainele Mării Negre și ale Deltei Dunării!");
        }
        
        playerStatsTextView.setText(statsText.toString());
        
        // Load recommendations
        loadRecommendations(stats);
    }
    
    private void loadRecommendations(PlayerProgressTracker.QuizStats stats) {
        StringBuilder recommendationsText = new StringBuilder();
        recommendationsText.append("🧭 Recomandări de navigație:\n\n");
        
        if (stats.recommendations.isEmpty()) {
            recommendationsText.append("Joacă câteva quiz-uri pentru a primi recomandări personalizate de navigație!");
        } else {
            for (PlayerProgressTracker.LearningRecommendation rec : stats.recommendations) {
                String themeIcon = getThemeIcon(rec.theme);
                recommendationsText.append(themeIcon).append(" ").append(rec.title).append("\n");
                recommendationsText.append("  ").append(rec.description).append("\n\n");
            }
        }
        
        // Add difficulty recommendation with maritime terminology
        DifficultyManager.DifficultyLevel recommendedDifficulty = difficultyManager.getCurrentDifficulty();
        recommendationsText.append("🎯 Rang recomandat: ").append(recommendedDifficulty.emoji)
                          .append(" ").append(recommendedDifficulty.displayName).append("\n");
        recommendationsText.append("📝 ").append(difficultyManager.getPerformanceRecommendation());
        
        recommendationsTextView.setText(recommendationsText.toString());
    }
    
    private String getThemeIcon(String theme) {
        switch (theme) {
            case "delta": return "🦢";
            case "maritime": return "⚓";
            case "archaeology": return "🏛️";
            default: return "🌊";
        }
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
        boolean categorySelectionEnabled = gameMode == GameModeManager.GameMode.MARITIME ||
                                          gameMode == GameModeManager.GameMode.DELTA_EXPLORER ||
                                          gameMode == GameModeManager.GameMode.ARCHAEOLOGICAL_DIG ||
                                          gameMode == GameModeManager.GameMode.FISHERMAN_WISDOM;
        categorySpinner.setEnabled(categorySelectionEnabled);
        
        // Update start button text with maritime theme
        String buttonText = "🚀 Începe " + gameMode.displayName;
        if (gameMode == GameModeManager.GameMode.MARITIME) {
            buttonText = "⚓ Navighează în " + gameMode.displayName;
        } else if (gameMode == GameModeManager.GameMode.DELTA_EXPLORER) {
            buttonText = "🦢 Explorează " + gameMode.displayName;
        } else if (gameMode == GameModeManager.GameMode.STORM_SURVIVAL) {
            buttonText = "⛈️ Supraviețuiește " + gameMode.displayName;
        }
        
        startGameButton.setText(buttonText);
    }
    
    private void startSelectedGame() {
        Intent intent = new Intent(this, DobrogeaGameActivity.class);
        
        // Add game mode
        intent.putExtra("GAME_MODE", selectedGameMode.name());
        
        // Add category if selected and relevant
        if (categorySpinner.getSelectedItemPosition() > 0) {
            // Map spinner position to category
            int categoryIndex = categorySpinner.getSelectedItemPosition() - 1;
            EnhancedQuestionModel.Category[] categories = EnhancedQuestionModel.Category.values();
            if (categoryIndex < categories.length) {
                intent.putExtra("FOCUS_CATEGORY", categories[categoryIndex].name());
            }
        }
        
        // Add manual difficulty if selected
        if (difficultySpinner.getSelectedItemPosition() > 0) {
            DifficultyManager.DifficultyLevel[] levels = DifficultyManager.DifficultyLevel.values();
            int levelIndex = difficultySpinner.getSelectedItemPosition() - 1;
            if (levelIndex < levels.length) {
                intent.putExtra("MANUAL_DIFFICULTY", levels[levelIndex].name());
            }
        }
        
        startActivity(intent);
    }
    
    private void openStatsActivity() {
        Intent intent = new Intent(this, DobrogeaPlayerStatsActivity.class);
        startActivity(intent);
    }
    
    // Inner classes for game mode management
    public static class GameModeItem {
        public GameModeManager.GameMode gameMode;
        public boolean isSelected;
        
        public GameModeItem(GameModeManager.GameMode gameMode) {
            this.gameMode = gameMode;
            this.isSelected = false;
        }
    }
    
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
                .inflate(R.layout.item_dobrogea_game_mode, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            GameModeItem item = gameModes.get(position);
            GameModeManager.GameMode mode = item.gameMode;
            
            holder.titleTextView.setText(mode.emoji + " " + mode.displayName);
            holder.descriptionTextView.setText(mode.description);
            
            // Update card appearance based on selection
            boolean isSelected = mode == selectedMode;
            holder.cardView.setCardBackgroundColor(
                holder.itemView.getContext().getResources().getColor(
                    isSelected ? R.color.dobrogea_primary_light : R.color.dobrogea_card_bg,
                    holder.itemView.getContext().getTheme()
                )
            );
            
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
                cardView = (MaterialCardView) itemView;
                titleTextView = itemView.findViewById(R.id.gameModeTitle);
                descriptionTextView = itemView.findViewById(R.id.gameModeDescription);
            }
        }
    }
} 