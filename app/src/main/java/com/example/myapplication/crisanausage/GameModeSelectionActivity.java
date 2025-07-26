package com.example.myapplication.crisanausage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.EnhancedQuestionModel;
import com.example.myapplication.maramuresusage.GameModeAdapter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.example.myapplication.utils.SyncManager;

/**
 * Activitate pentru selectarea modului de joc în quiz-ul Crișana
 */
public class GameModeSelectionActivity extends AppCompatActivity {
    
    private RecyclerView gameModeRecyclerView;
    private GameModeAdapter gameModeAdapter;
    private MaterialTextView statsTextView;
    private MaterialButton startGameButton;
    private MaterialCardView playerStatsCard;
    
    private GameModeManager gameModeManager;
    private PlayerProgressTracker progressTracker;
    private SyncManager syncManager;
    
    private com.example.myapplication.maramuresusage.GameModeManager.GameMode selectedGameMode;
    private EnhancedQuestionModel.Category selectedCategory;
    private final String selectedDataSource = "always_database"; // Always use database
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode_selection);
        
        initializeComponents();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadPlayerStats();
        saveDataSourcePreference(); // Always save as database
    }
    
    private void initializeComponents() {
        gameModeManager = new GameModeManager(this);
        progressTracker = new PlayerProgressTracker(this);
        syncManager = SyncManager.getInstance(this);
        
        gameModeRecyclerView = findViewById(R.id.gameModeRecyclerView);
        statsTextView = findViewById(R.id.statsTextView);
        startGameButton = findViewById(R.id.startGameButton);
        playerStatsCard = findViewById(R.id.playerStatsCard);
        
        // Setează modul implicit
        selectedGameMode = com.example.myapplication.maramuresusage.GameModeManager.GameMode.CLASSIC;
        selectedCategory = null;
    }
    
    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Selectează Modul de Joc - Crișana");
        }
    }
    
    private void setupRecyclerView() {
        // Convert local GameMode values to the adapter's expected type
        List<com.example.myapplication.maramuresusage.GameModeManager.GameMode> gameModes = new ArrayList<>();
        for (GameModeManager.GameMode localMode : GameModeManager.GameMode.values()) {
            gameModes.add(com.example.myapplication.maramuresusage.GameModeManager.GameMode.valueOf(localMode.name()));
        }
        
        gameModeAdapter = new GameModeAdapter(gameModes, 
            new GameModeAdapter.OnGameModeClickListener() {
                @Override
                public void onGameModeClick(com.example.myapplication.maramuresusage.GameModeManager.GameMode gameMode) {
                    selectedGameMode = gameMode;
                    updateGameModeInfo();
                    
                    // Dacă este modul Category Focus, afișează dialog pentru selectare categorie
                    if (gameMode == com.example.myapplication.maramuresusage.GameModeManager.GameMode.CATEGORY_FOCUS) {
                        showCategorySelectionDialog();
                    }
                }
            });
        
        gameModeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        gameModeRecyclerView.setAdapter(gameModeAdapter);
    }
    
    private void setupClickListeners() {
        startGameButton.setOnClickListener(v -> startSelectedGame());
        
        playerStatsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlayerStatsActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadPlayerStats() {
        StringBuilder statsBuilder = new StringBuilder();
        
        // Statistici generale
        statsBuilder.append("🎮 Jocuri jucate: ").append(progressTracker.getTotalGames()).append("\n");
        statsBuilder.append("📊 Întrebări răspunse: ").append(progressTracker.getTotalQuestions()).append("\n");
        statsBuilder.append("✅ Acuratețe: ").append(Math.round(progressTracker.getOverallAccuracy() * 100)).append("%\n");
        statsBuilder.append("🔥 Streak curent: ").append(progressTracker.getCurrentStreak()).append(" zile\n");
        
        // Timp total
        long totalTimeMinutes = progressTracker.getTotalTimeSpent() / (1000 * 60);
        statsBuilder.append("⏱️ Timp total: ").append(totalTimeMinutes).append(" minute");
        
        statsTextView.setText(statsBuilder.toString());
    }
    
    private void saveDataSourcePreference() {
        SharedPreferences prefs = getSharedPreferences("CrisanaGamePrefs", MODE_PRIVATE);
        prefs.edit()
             .putString("data_source_preference", selectedDataSource)
             .apply();
    }
    
    private boolean checkIfLocalCacheExists() {
        String cacheKey = "questions_cache_crisana_quiz";
        String cachedJson = getSharedPreferences("HybridStorage", MODE_PRIVATE).getString(cacheKey, null);
        return cachedJson != null && !cachedJson.isEmpty();
    }
    
    private void updateGameModeInfo() {
        // Actualizează informațiile despre modul de joc selectat
        GameModeManager.GameMode localMode = GameModeManager.GameMode.valueOf(selectedGameMode.name());
        String description = localMode.description;
            
        Toast.makeText(this, description, Toast.LENGTH_SHORT).show();
    }
    
    private void showCategorySelectionDialog() {
        String[] categories = {
            "Istorie", "Geografie", "Cultură", "Personalități", "Tradiții", "Gastronomie", "Natură"
        };
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Selectează Categoria")
            .setItems(categories, (dialog, which) -> {
                selectedCategory = EnhancedQuestionModel.Category.values()[which];
                Toast.makeText(this, "Categorie selectată: " + categories[which], Toast.LENGTH_SHORT).show();
            })
            .show();
    }
    
    private void startSelectedGame() {
        Intent intent = new Intent(this, CrisanaGameActivity.class);
        
        // Transmitem modul de joc selectat
        intent.putExtra("GAME_MODE", selectedGameMode.name());
        
        // Transmitem categoria selectată dacă este cazul
        if (selectedCategory != null) {
            intent.putExtra("CATEGORY", selectedCategory.name());
        }
        
        // Transmitem preferința pentru sursa de date - întotdeauna baza de date
        intent.putExtra("data_source_preference", selectedDataSource);
        
        startActivity(intent);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadPlayerStats();
    }
} 