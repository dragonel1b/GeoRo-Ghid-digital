package com.example.myapplication.banatusage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.core.domain.model.EnhancedQuestionModel;
import com.example.myapplication.utils.SyncManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * Activitate pentru selectarea modului de joc și configurarea opțiunilor avansate în Banat
 */
public class GameModeSelectionActivity extends AppCompatActivity {
    
    private RecyclerView gameModeRecyclerView;
    private Spinner categorySpinner;
    private Spinner difficultySpinner;
    private TextView playerStatsTextView;
    private TextView recommendationsTextView;
    private MaterialButton startGameButton;
    private MaterialButton viewStatsButton;
    private MaterialButton dataSourceButton;
    
    private DifficultyManager difficultyManager;
    private PlayerProgressTracker progressTracker;
    private GameModeAdapter gameModeAdapter;
    private SyncManager syncManager;
    private GameModeManager.GameMode selectedGameMode = GameModeManager.GameMode.CLASSIC;
    private EnhancedQuestionModel.Category selectedCategory = null;
    private String selectedDataSource = "ask_every_time"; // Default
    
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
        loadDataSourcePreference();
    }
    
    private void initializeSystems() {
        difficultyManager = new DifficultyManager(this);
        progressTracker = new PlayerProgressTracker(this);
        syncManager = SyncManager.getInstance(this);
    }
    
    private void initializeViews() {
        gameModeRecyclerView = findViewById(R.id.gameModeRecyclerView);
        categorySpinner = findViewById(R.id.categorySpinner);
        difficultySpinner = findViewById(R.id.difficultySpinner);
        playerStatsTextView = findViewById(R.id.playerStatsTextView);
        recommendationsTextView = findViewById(R.id.recommendationsTextView);
        startGameButton = findViewById(R.id.startGameButton);
        viewStatsButton = findViewById(R.id.viewStatsButton);
        dataSourceButton = findViewById(R.id.dataSourceButton);
        
        if (dataSourceButton == null) {
            // If the button doesn't exist in the layout, create it programmatically
            dataSourceButton = new MaterialButton(this);
            dataSourceButton.setId(View.generateViewId());
            dataSourceButton.setText("🔍 Selectează sursa întrebărilor");
            
            // Find a container to add the button to
            View container = findViewById(R.id.gameSelectionContainer);
            if (container != null && container instanceof android.view.ViewGroup) {
                ((android.view.ViewGroup) container).addView(dataSourceButton);
            }
        }
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
        statsText.append("📊 Statistici personale - Banat:\n\n");
        
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
            statsText.append("🌟 Bine ai venit în Banat!\n");
            statsText.append("Acesta va fi primul tău quiz din această regiune.\n");
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
        
        if (dataSourceButton != null) {
            dataSourceButton.setOnClickListener(v -> showDataSourceDialog());
        }
    }
    
    private void loadDataSourcePreference() {
        SharedPreferences prefs = getSharedPreferences("BanatGamePrefs", MODE_PRIVATE);
        selectedDataSource = prefs.getString("data_source_preference", "ask_every_time");
        updateDataSourceButtonText();
    }
    
    private void updateDataSourceButtonText() {
        if (dataSourceButton == null) return;
        
        String sourceText;
        
        switch (selectedDataSource) {
            case "always_database":
                sourceText = "🌐 Baza de Date";
                break;
            case "always_local":
                sourceText = "📱 Cache Local";
                break;
            case "auto":
                sourceText = "🎯 Automat";
                break;
            case "ask_every_time":
            default:
                sourceText = "🤔 Întreabă de fiecare dată";
                break;
        }
        
        dataSourceButton.setText("📚 Sursa întrebărilor: " + sourceText);
    }
    
    private void showDataSourceDialog() {
        boolean hasInternet = syncManager.isInternetAvailable();
        boolean hasLocalCache = checkIfLocalCacheExists();
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("📚 Alegeți sursa întrebărilor");
        
        String[] options = {
            "🌐 Întotdeauna din Baza de Date",
            "📱 Întotdeauna din Cache Local",
            "🎯 Automat - Cea mai bună opțiune",
            "🤔 Întreabă de fiecare dată"
        };
        
        int selectedIndex = 3; // Default to "Ask every time"
        switch (selectedDataSource) {
            case "always_database": selectedIndex = 0; break;
            case "always_local": selectedIndex = 1; break;
            case "auto": selectedIndex = 2; break;
            case "ask_every_time": selectedIndex = 3; break;
        }
        
        if (!hasInternet) {
            builder.setMessage("⚠️ Nu sunteți conectat la internet.\n" +
                      "Opțiunea pentru baza de date va funcționa doar când aveți internet.");
        }
        
        if (!hasLocalCache) {
            if (!hasInternet) {
                builder.setMessage("⚠️ Nu sunteți conectat la internet și nu există cache local.\n" +
                          "Vă recomandăm să vă conectați la internet pentru primul joc.");
            } else {
                builder.setMessage("⚠️ Nu există încă un cache local.\n" +
                          "Acesta va fi creat după primul joc cu întrebări din baza de date.");
            }
        }
        
        builder.setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
            switch (which) {
                case 0: selectedDataSource = "always_database"; break;
                case 1: selectedDataSource = "always_local"; break;
                case 2: selectedDataSource = "auto"; break;
                case 3: selectedDataSource = "ask_every_time"; break;
            }
        });
        
        builder.setPositiveButton("Salvează", (dialog, which) -> {
            saveDataSourcePreference();
            updateDataSourceButtonText();
        });
        
        builder.setNegativeButton("Anulează", (dialog, which) -> dialog.dismiss());
        
        builder.show();
    }
    
    private void saveDataSourcePreference() {
        SharedPreferences prefs = getSharedPreferences("BanatGamePrefs", MODE_PRIVATE);
        prefs.edit()
             .putString("data_source_preference", selectedDataSource)
             .apply();
        
        Toast.makeText(this, "✅ Preferință salvată: " + getDataSourceDisplayName(), Toast.LENGTH_SHORT).show();
    }
    
    private String getDataSourceDisplayName() {
        switch (selectedDataSource) {
            case "always_database": return "Baza de Date";
            case "always_local": return "Cache Local";
            case "auto": return "Automat";
            case "ask_every_time": return "Întreabă de fiecare dată";
            default: return "Necunoscut";
        }
    }
    
    private boolean checkIfLocalCacheExists() {
        String cacheKey = "questions_cache_banat_quiz";
        String cachedJson = getSharedPreferences("HybridStorage", MODE_PRIVATE).getString(cacheKey, null);
        return cachedJson != null && !cachedJson.isEmpty();
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
        Intent intent = new Intent(this, BanatGameActivity.class);
        
        // Add game mode
        intent.putExtra("GAME_MODE", selectedGameMode.name());
        
        // Add category if selected
        if (categorySpinner.getSelectedItemPosition() > 0) {
            EnhancedQuestionModel.Category category = EnhancedQuestionModel.Category.values()[
                categorySpinner.getSelectedItemPosition() - 1];
            intent.putExtra("FOCUS_CATEGORY", category.name());
        }
        
        // Add difficulty if manually selected
        if (difficultySpinner.getSelectedItemPosition() > 0) {
            DifficultyManager.DifficultyLevel difficulty = DifficultyManager.DifficultyLevel.values()[
                difficultySpinner.getSelectedItemPosition() - 1];
            intent.putExtra("MANUAL_DIFFICULTY", difficulty.name());
        }
        
        // Add data source preference
        intent.putExtra("data_source_preference", selectedDataSource);
        
        startActivity(intent);
    }
    
    private void openStatsActivity() {
        Intent intent = new Intent(this, PlayerStatsActivity.class);
        startActivity(intent);
    }
    
    // Inner classes for game mode selection
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
                .inflate(R.layout.item_game_mode, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            GameModeItem item = gameModes.get(position);
            holder.titleTextView.setText(item.gameMode.displayName);
            holder.descriptionTextView.setText(item.gameMode.description);
            
            // Highlight selected mode
            boolean isSelected = item.gameMode == selectedMode;
            holder.cardView.setStrokeWidth(isSelected ? 4 : 0);
            holder.cardView.setStrokeColor(isSelected ? 
                holder.itemView.getContext().getResources().getColor(R.color.banat_primary) : 0);
            
            holder.cardView.setOnClickListener(v -> {
                listener.onGameModeSelected(item.gameMode);
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
    
    @Override
    protected void onResume() {
        super.onResume();
        loadPlayerStats();
        loadDataSourcePreference();
    }
} 