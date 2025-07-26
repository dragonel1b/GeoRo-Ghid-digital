package com.example.myapplication.bucovinausage;

import android.content.Intent;
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
import com.google.android.material.textview.MaterialTextView;

/**
 * Activitate pentru selectarea modului de joc în quiz-ul Bucovina
 */
public class GameModeSelectionActivity extends AppCompatActivity {
    
    private RecyclerView gameModeRecyclerView;
    private GameModeAdapter gameModeAdapter;
    private MaterialTextView statsTextView;
    private MaterialButton startGameButton;
    private MaterialCardView playerStatsCard;
    
    private GameModeManager gameModeManager;
    private PlayerProgressTracker progressTracker;
    
    private com.example.myapplication.maramuresusage.GameModeManager.GameMode selectedGameMode;
    private EnhancedQuestionModel.Category selectedCategory;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode_selection);
        
        initializeComponents();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadPlayerStats();
    }
    
    private void initializeComponents() {
        gameModeManager = new GameModeManager(this);
        progressTracker = new PlayerProgressTracker(this);
        
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
            getSupportActionBar().setTitle("Selectează Modul de Joc - Bucovina");
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
    
    private void updateGameModeInfo() {
        // Actualizează interfața pentru a reflecta modul selectat
        if (gameModeAdapter != null) {
            gameModeAdapter.setSelectedGameMode(selectedGameMode);
        }
        
        // Actualizează textul butonului
        String buttonText = "Începe " + selectedGameMode.displayName;
        if (selectedCategory != null) {
            buttonText += " (" + selectedCategory.displayName + ")";
        }
        startGameButton.setText(buttonText);
    }
    
    private void showCategorySelectionDialog() {
        // Creează un dialog pentru selectarea categoriei
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Selectează Categoria");
        
        EnhancedQuestionModel.Category[] categories = EnhancedQuestionModel.Category.values();
        String[] categoryNames = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            categoryNames[i] = categories[i].displayName;
        }
        
        builder.setItems(categoryNames, (dialog, which) -> {
            selectedCategory = categories[which];
            updateGameModeInfo();
        });
        
        builder.setNegativeButton("Anulează", (dialog, which) -> {
            // Resetează la modul clasic dacă anulează
            selectedGameMode = com.example.myapplication.maramuresusage.GameModeManager.GameMode.CLASSIC;
            selectedCategory = null;
            updateGameModeInfo();
        });
        
        builder.show();
    }
    
    private void startSelectedGame() {
        if (selectedGameMode == com.example.myapplication.maramuresusage.GameModeManager.GameMode.CATEGORY_FOCUS && selectedCategory == null) {
            Toast.makeText(this, "Te rog selectează o categorie pentru modul Focus Categorie", 
                          Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Inițializează modul de joc
        // Convert the selected game mode to the local GameModeManager type
        GameModeManager.GameMode localGameMode = GameModeManager.GameMode.valueOf(selectedGameMode.name());
        gameModeManager.initializeGameMode(localGameMode, selectedCategory);
        
        // Pornește jocul
        Intent intent = new Intent(this, BucovinaGameActivity.class);
        intent.putExtra("game_mode", selectedGameMode.name());
        if (selectedCategory != null) {
            intent.putExtra("focus_category", selectedCategory.name());
        }
        startActivity(intent);
        finish();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reîncarcă statisticile când utilizatorul se întoarce
        loadPlayerStats();
    }
} 