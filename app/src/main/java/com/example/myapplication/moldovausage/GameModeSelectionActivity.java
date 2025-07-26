package com.example.myapplication.moldovausage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * Activitate pentru selecția modului de joc și dificultății în quiz-ul Moldova
 * Similar cu cel din Transilvania pentru consistență
 */
public class GameModeSelectionActivity extends AppCompatActivity {
    
    private GameModeManager gameModeManager;
    private DifficultyManager difficultyManager;
    private PlayerProgressTracker progressTracker;
    
    private Spinner gameModeSpinner;
    private Spinner difficultySpinner;
    private TextView playerStatsTextView;
    private TextView recommendationsTextView;
    private MaterialButton startGameButton;
    private MaterialButton backButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_game_mode_selection);
        
        initializeManagers();
        initializeViews();
        setupSpinners();
        loadPlayerStats();
        setupButtons();
    }
    
    private void initializeManagers() {
        gameModeManager = new GameModeManager(this);
        difficultyManager = new DifficultyManager(this);
        progressTracker = new PlayerProgressTracker(this);
    }
    
    private void initializeViews() {
        gameModeSpinner = findViewById(R.id.gameModeSpinner);
        difficultySpinner = findViewById(R.id.difficultySpinner);
        playerStatsTextView = findViewById(R.id.playerStatsTextView);
        recommendationsTextView = findViewById(R.id.recommendationsTextView);
        startGameButton = findViewById(R.id.startGameButton);
        backButton = findViewById(R.id.backButton);
    }
    
    private void setupSpinners() {
        // Setup Game Mode Spinner
        GameModeManager.GameMode[] gameModes = gameModeManager.getAvailableModes();
        String[] gameModeNames = new String[gameModes.length];
        for (int i = 0; i < gameModes.length; i++) {
            GameModeManager.GameMode mode = gameModes[i];
            if (gameModeManager.isModeUnlocked(mode)) {
                gameModeNames[i] = mode.getDisplayName();
            } else {
                gameModeNames[i] = mode.getDisplayName() + " (🔒 " + mode.getRequiredQuizzes() + " quiz-uri)";
            }
        }
        
        ArrayAdapter<String> gameModeAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, gameModeNames);
        gameModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gameModeSpinner.setAdapter(gameModeAdapter);
        
        // Set current game mode
        GameModeManager.GameMode currentMode = gameModeManager.getCurrentMode();
        for (int i = 0; i < gameModes.length; i++) {
            if (gameModes[i] == currentMode) {
                gameModeSpinner.setSelection(i);
                break;
            }
        }
        
        gameModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                GameModeManager.GameMode selectedMode = gameModes[position];
                if (gameModeManager.isModeUnlocked(selectedMode)) {
                    gameModeManager.setCurrentMode(selectedMode);
                    updateModeDescription(selectedMode);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Setup Difficulty Spinner
        DifficultyManager.DifficultyLevel[] difficulties = difficultyManager.getAvailableDifficulties();
        String[] difficultyNames = new String[difficulties.length];
        for (int i = 0; i < difficulties.length; i++) {
            DifficultyManager.DifficultyLevel difficulty = difficulties[i];
            if (difficultyManager.isDifficultyUnlocked(difficulty)) {
                difficultyNames[i] = difficulty.getDisplayName();
            } else {
                difficultyNames[i] = difficulty.getDisplayName() + " (🔒 " + difficulty.getRequiredQuizzes() + " quiz-uri)";
            }
        }
        
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, difficultyNames);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);
        
        // Set current difficulty
        DifficultyManager.DifficultyLevel currentDifficulty = difficultyManager.getCurrentDifficulty();
        for (int i = 0; i < difficulties.length; i++) {
            if (difficulties[i] == currentDifficulty) {
                difficultySpinner.setSelection(i);
                break;
            }
        }
        
        difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DifficultyManager.DifficultyLevel selectedDifficulty = difficulties[position];
                if (difficultyManager.isDifficultyUnlocked(selectedDifficulty)) {
                    difficultyManager.setCurrentDifficulty(selectedDifficulty);
                    updateDifficultyDescription(selectedDifficulty);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Set initial descriptions
        updateModeDescription(gameModeManager.getCurrentMode());
        updateDifficultyDescription(difficultyManager.getCurrentDifficulty());
    }
    
    private void updateModeDescription(GameModeManager.GameMode mode) {
        TextView modeDescriptionTextView = findViewById(R.id.modeDescriptionTextView);
        if (modeDescriptionTextView != null) {
            modeDescriptionTextView.setText(mode.getDescription());
        }
    }
    
    private void updateDifficultyDescription(DifficultyManager.DifficultyLevel difficulty) {
        TextView difficultyDescriptionTextView = findViewById(R.id.difficultyDescriptionTextView);
        if (difficultyDescriptionTextView != null) {
            String description = String.format("Timp: %ds | Multiplicator: %.1fx", 
                difficulty.getTimeSeconds(), difficulty.getScoreMultiplier());
            difficultyDescriptionTextView.setText(description);
        }
    }
    
    private void loadPlayerStats() {
        PlayerProgressTracker.QuizStats stats = progressTracker.getCurrentStats();
        
        StringBuilder statsText = new StringBuilder();
        statsText.append("📊 Statistici personale:\n\n");
        
        if (stats.totalGamesPlayed > 0) {
            statsText.append("🎮 Jocuri jucate: ").append(stats.totalGamesPlayed).append("\n");
            statsText.append("🎯 Acuratețe: ").append(String.format("%.1f%%", stats.overallAccuracy * 100)).append("\n");
            statsText.append("🔥 Cel mai lung streak: ").append(stats.longestStreak).append("\n");
            statsText.append("⏱️ Timp mediu/întrebare: ").append(String.format("%.1fs", (float) stats.averageTimePerQuestion / 1000.0f)).append("\n\n");
            
            // Show category performance
            statsText.append("📚 Performanță pe categorii:\n");
            for (PlayerProgressTracker.CategoryStats categoryStats : progressTracker.getCategoryStats().values()) {
                if (categoryStats.questionsAnswered >= 3) {
                    String status = categoryStats.isStrong ? " 💪" : (categoryStats.isWeak ? " 📈" : " ⚖️");
                    statsText.append("• ").append(categoryStats.categoryName)
                             .append(": ").append(String.format("%.0f%%", categoryStats.accuracy * 100))
                             .append(status).append("\n");
                }
            }
        } else {
            statsText.append("🌟 Bine ai venit!\n");
            statsText.append("Acesta va fi primul tău quiz din Moldova.\n");
            statsText.append("Succes!");
        }
        
        playerStatsTextView.setText(statsText.toString());
        
        // Load recommendations
        loadRecommendations(stats);
    }
    
    private void loadRecommendations(PlayerProgressTracker.QuizStats stats) {
        StringBuilder recommendations = new StringBuilder();
        recommendations.append("💡 Recomandări:\n\n");
        
        if (stats.totalGamesPlayed == 0) {
            recommendations.append("• Începe cu modul Clasic și dificultatea Ușor\n");
            recommendations.append("• Folosește lifeline-urile pentru a învăța\n");
            recommendations.append("• Concentrează-te pe acuratețe, nu pe viteză\n");
        } else if (stats.overallAccuracy < 0.6f) {
            recommendations.append("• Exersează mai mult pe dificultatea Ușor\n");
            recommendations.append("• Revizuiește întrebările greșite\n");
            recommendations.append("• Încearcă modul Clasic pentru a învăța\n");
        } else if (stats.overallAccuracy > 0.8f) {
            recommendations.append("• Încearcă dificultăți mai mari\n");
            recommendations.append("• Testează modul Cronometrat\n");
            recommendations.append("• Poți încerca modul Expert\n");
        } else {
            recommendations.append("• Continuă să exersezi pe dificultatea actuală\n");
            recommendations.append("• Încearcă moduri noi pentru varietate\n");
            recommendations.append("• Concentrează-te pe categoriile slabe\n");
        }
        
        recommendationsTextView.setText(recommendations.toString());
    }
    
    private void setupButtons() {
        startGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MoldovaGameActivity.class);
            intent.putExtra("GAME_MODE", gameModeManager.getCurrentMode().name());
            intent.putExtra("DIFFICULTY", difficultyManager.getCurrentDifficulty().name());
            startActivity(intent);
        });
        
        backButton.setOnClickListener(v -> finish());
    }
} 