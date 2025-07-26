package com.example.myapplication.moldovausage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.myapplication.models.EnhancedQuestionModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager pentru gestionarea modurilor de joc în quiz-ul Moldova
 * Similar cu cel din Transilvania pentru consistență
 */
public class GameModeManager {
    private static final String TAG = "MoldovaGameModeManager";
    private static final String PREFS_NAME = "MoldovaGameModePrefs";
    private static final String KEY_CURRENT_MODE = "current_mode";
    private static final String KEY_MODE_PROGRESS = "mode_progress_";
    private static final String KEY_UNLOCKED_MODES = "unlocked_modes";
    
    public enum GameMode {
        CLASSIC("Clasic", "Quiz tradițional cu întrebări despre Moldova", 0, 1.0f, true, true, 10),
        TIMED("Cronometrat", "Răspunde cât mai rapid posibil", 3, 1.2f, false, false, 15),
        SURVIVAL("Supraviețuire", "Fără greșeli permise", 5, 1.5f, true, false, 20),
        EXPERT("Expert", "Întrebări dificile cu bonusuri mari", 10, 2.0f, false, false, 25);
        
        public final String displayName;
        public final String description;
        public final int requiredQuizzes;
        public final float scoreMultiplier;
        public final boolean allowLifelines;
        public final boolean allowSkip;
        public final int questionsCount;
        
        GameMode(String displayName, String description, int requiredQuizzes, float scoreMultiplier, 
                boolean allowLifelines, boolean allowSkip, int questionsCount) {
            this.displayName = displayName;
            this.description = description;
            this.requiredQuizzes = requiredQuizzes;
            this.scoreMultiplier = scoreMultiplier;
            this.allowLifelines = allowLifelines;
            this.allowSkip = allowSkip;
            this.questionsCount = questionsCount;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public int getRequiredQuizzes() { return requiredQuizzes; }
        public float getScoreMultiplier() { return scoreMultiplier; }
        public boolean areLifelinesAllowed() { return allowLifelines; }
        public boolean isSkipAllowed() { return allowSkip; }
        public int getQuestionsCount() { return questionsCount; }
    }
    
    private Context context;
    private SharedPreferences prefs;
    private GameMode currentMode;
    private int wrongAnswers = 0;
    
    public GameModeManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadCurrentMode();
    }
    
    private void loadCurrentMode() {
        String modeName = prefs.getString(KEY_CURRENT_MODE, GameMode.CLASSIC.name());
        try {
            currentMode = GameMode.valueOf(modeName);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Invalid game mode saved, defaulting to CLASSIC");
            currentMode = GameMode.CLASSIC;
        }
    }
    
    public GameMode getCurrentGameMode() {
        return currentMode;
    }
    
    public GameMode getCurrentMode() {
        return currentMode;
    }
    
    public void setCurrentMode(GameMode mode) {
        if (isModeUnlocked(mode)) {
            this.currentMode = mode;
            prefs.edit().putString(KEY_CURRENT_MODE, mode.name()).apply();
            Log.d(TAG, "Game mode set to: " + mode.getDisplayName());
        } else {
            Log.w(TAG, "Attempted to set locked mode: " + mode.getDisplayName());
        }
    }
    
    public boolean isModeUnlocked(GameMode mode) {
        if (mode == GameMode.CLASSIC) return true;
        
        String unlockedModes = prefs.getString(KEY_UNLOCKED_MODES, "");
        return unlockedModes.contains(mode.name());
    }
    
    public void unlockMode(GameMode mode) {
        if (mode == GameMode.CLASSIC) return;
        
        String unlockedModes = prefs.getString(KEY_UNLOCKED_MODES, "");
        if (!unlockedModes.contains(mode.name())) {
            unlockedModes += mode.name() + ",";
            prefs.edit().putString(KEY_UNLOCKED_MODES, unlockedModes).apply();
            Log.d(TAG, "Unlocked game mode: " + mode.getDisplayName());
        }
    }
    
    public void checkAndUnlockModes(int totalQuizzesCompleted) {
        for (GameMode mode : GameMode.values()) {
            if (mode != GameMode.CLASSIC && 
                totalQuizzesCompleted >= mode.getRequiredQuizzes() &&
                !isModeUnlocked(mode)) {
                unlockMode(mode);
            }
        }
    }
    
    public float getScoreMultiplierForCurrentMode() {
        return currentMode.getScoreMultiplier();
    }
    
    public String getCurrentModeDisplayName() {
        return currentMode.getDisplayName();
    }
    
    public String getCurrentModeDescription() {
        return currentMode.getDescription();
    }
    
    public GameMode[] getAvailableModes() {
        return GameMode.values();
    }
    
    public Map<String, Object> getModeSettings(GameMode mode) {
        Map<String, Object> settings = new HashMap<>();
        
        switch (mode) {
            case CLASSIC:
                settings.put("timeLimit", 30);
                settings.put("allowLifelines", true);
                settings.put("allowSkip", true);
                settings.put("questionsCount", 10);
                break;
            case TIMED:
                settings.put("timeLimit", 20);
                settings.put("allowLifelines", false);
                settings.put("allowSkip", false);
                settings.put("questionsCount", 15);
                break;
            case SURVIVAL:
                settings.put("timeLimit", 25);
                settings.put("allowLifelines", true);
                settings.put("allowSkip", false);
                settings.put("questionsCount", 20);
                break;
            case EXPERT:
                settings.put("timeLimit", 35);
                settings.put("allowLifelines", false);
                settings.put("allowSkip", false);
                settings.put("questionsCount", 25);
                break;
        }
        
        return settings;
    }
    
    // Metodele noi necesare pentru compatibilitate cu MoldovaGameActivity
    public void initializeGameMode(GameMode gameMode, Object difficulty) {
        this.currentMode = gameMode;
        this.wrongAnswers = 0;
        Log.d(TAG, "Initialized game mode: " + gameMode.getDisplayName());
    }
    
    public List<EnhancedQuestionModel> filterQuestionsForGameMode(List<EnhancedQuestionModel> questions) {
        List<EnhancedQuestionModel> filtered = new ArrayList<>();
        
        // Filtrăm întrebările în funcție de modul de joc
        switch (currentMode) {
            case CLASSIC:
                // Toate întrebările sunt permise
                filtered.addAll(questions);
                break;
            case TIMED:
                // Prioritate pentru întrebări mai simple în modul cronometrat
                for (EnhancedQuestionModel q : questions) {
                    if (q.getDifficulty() == EnhancedQuestionModel.Difficulty.EASY ||
                        q.getDifficulty() == EnhancedQuestionModel.Difficulty.MEDIUM) {
                        filtered.add(q);
                    }
                }
                if (filtered.isEmpty()) filtered.addAll(questions);
                break;
            case SURVIVAL:
                // Mix echilibrat pentru supraviețuire
                filtered.addAll(questions);
                break;
            case EXPERT:
                // Doar întrebări dificile pentru expert
                for (EnhancedQuestionModel q : questions) {
                    if (q.getDifficulty() == EnhancedQuestionModel.Difficulty.HARD ||
                        q.getDifficulty() == EnhancedQuestionModel.Difficulty.EXPERT) {
                        filtered.add(q);
                    }
                }
                if (filtered.isEmpty()) filtered.addAll(questions);
                break;
        }
        
        return filtered;
    }
    
    public int calculateModeBonus(int basePoints, boolean isCorrect, long timeSpent) {
        if (!isCorrect) return 0;
        
        float bonus = basePoints * currentMode.getScoreMultiplier();
        
        // Bonus pentru timpul rămas în modul cronometrat
        if (currentMode == GameMode.TIMED && timeSpent < 15000) {
            bonus *= 1.5f; // 50% bonus pentru răspuns rapid
        }
        
        return Math.round(bonus);
    }
    
    public boolean shouldEndGame(boolean wrongAnswer, int currentQuestionIndex) {
        if (wrongAnswer && currentMode == GameMode.SURVIVAL) {
            wrongAnswers++;
            return wrongAnswers >= 1; // Un singur răspuns greșit termină jocul în survival
        }
        return false;
    }
    
    public boolean isGameComplete(int currentQuestionIndex) {
        return currentQuestionIndex >= currentMode.getQuestionsCount();
    }
    
    public void resetProgress() {
        prefs.edit()
            .remove(KEY_CURRENT_MODE)
            .remove(KEY_UNLOCKED_MODES)
            .apply();
        currentMode = GameMode.CLASSIC;
        wrongAnswers = 0;
        Log.d(TAG, "Game mode progress reset");
    }
} 