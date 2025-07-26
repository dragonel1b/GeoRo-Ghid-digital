package com.example.myapplication.moldovausage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager pentru gestionarea dificultăților în quiz-ul Moldova
 * Similar cu cel din Transilvania pentru consistență
 */
public class DifficultyManager {
    private static final String TAG = "MoldovaDifficultyManager";
    private static final String PREFS_NAME = "MoldovaDifficultyPrefs";
    private static final String KEY_CURRENT_DIFFICULTY = "current_difficulty";
    private static final String KEY_DIFFICULTY_PROGRESS = "difficulty_progress_";
    private static final String KEY_UNLOCKED_DIFFICULTIES = "unlocked_difficulties";
    private static final String KEY_PERFORMANCE_STATS = "performance_stats";
    
    public enum DifficultyLevel {
        EASY("Ușor", 30, 1.0f, 0),
        MEDIUM("Mediu", 25, 1.5f, 5),
        HARD("Greu", 20, 2.0f, 10),
        EXPERT("Expert", 15, 3.0f, 20);
        
        public final String displayName;
        public final int timeSeconds;
        public final float scoreMultiplier;
        public final int requiredQuizzes;
        
        DifficultyLevel(String displayName, int timeSeconds, float scoreMultiplier, int requiredQuizzes) {
            this.displayName = displayName;
            this.timeSeconds = timeSeconds;
            this.scoreMultiplier = scoreMultiplier;
            this.requiredQuizzes = requiredQuizzes;
        }
        
        public String getDisplayName() { return displayName; }
        public int getTimeSeconds() { return timeSeconds; }
        public float getScoreMultiplier() { return scoreMultiplier; }
        public int getRequiredQuizzes() { return requiredQuizzes; }
    }
    
    private Context context;
    private SharedPreferences prefs;
    private DifficultyLevel currentDifficulty;
    private int lifelinesUsed = 0;
    
    public DifficultyManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadCurrentDifficulty();
    }
    
    private void loadCurrentDifficulty() {
        String difficultyName = prefs.getString(KEY_CURRENT_DIFFICULTY, DifficultyLevel.EASY.name());
        try {
            currentDifficulty = DifficultyLevel.valueOf(difficultyName);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Invalid difficulty saved, defaulting to EASY");
            currentDifficulty = DifficultyLevel.EASY;
        }
    }
    
    public DifficultyLevel getCurrentDifficulty() {
        return currentDifficulty;
    }
    
    public void setCurrentDifficulty(DifficultyLevel difficulty) {
        if (isDifficultyUnlocked(difficulty)) {
            this.currentDifficulty = difficulty;
            prefs.edit().putString(KEY_CURRENT_DIFFICULTY, difficulty.name()).apply();
            Log.d(TAG, "Difficulty set to: " + difficulty.getDisplayName());
        } else {
            Log.w(TAG, "Attempted to set locked difficulty: " + difficulty.getDisplayName());
        }
    }
    
    public boolean isDifficultyUnlocked(DifficultyLevel difficulty) {
        if (difficulty == DifficultyLevel.EASY) return true;
        
        String unlockedDifficulties = prefs.getString(KEY_UNLOCKED_DIFFICULTIES, "");
        return unlockedDifficulties.contains(difficulty.name());
    }
    
    public void unlockDifficulty(DifficultyLevel difficulty) {
        if (difficulty == DifficultyLevel.EASY) return;
        
        String unlockedDifficulties = prefs.getString(KEY_UNLOCKED_DIFFICULTIES, "");
        if (!unlockedDifficulties.contains(difficulty.name())) {
            unlockedDifficulties += difficulty.name() + ",";
            prefs.edit().putString(KEY_UNLOCKED_DIFFICULTIES, unlockedDifficulties).apply();
            Log.d(TAG, "Unlocked difficulty: " + difficulty.getDisplayName());
        }
    }
    
    public void checkAndUnlockDifficulties(int totalQuizzesCompleted) {
        for (DifficultyLevel difficulty : DifficultyLevel.values()) {
            if (difficulty != DifficultyLevel.EASY && 
                totalQuizzesCompleted >= difficulty.getRequiredQuizzes() &&
                !isDifficultyUnlocked(difficulty)) {
                unlockDifficulty(difficulty);
            }
        }
    }
    
    public int getTimeForCurrentDifficulty() {
        return currentDifficulty.getTimeSeconds();
    }
    
    public float getScoreMultiplierForCurrentDifficulty() {
        return currentDifficulty.getScoreMultiplier();
    }
    
    public String getCurrentDifficultyDisplayName() {
        return currentDifficulty.getDisplayName();
    }
    
    public DifficultyLevel[] getAvailableDifficulties() {
        return DifficultyLevel.values();
    }
    
    // Metodele noi necesare pentru compatibilitate cu MoldovaGameActivity
    public void updateDifficultyAfterGame(int correctAnswers, int totalQuestions, long totalTime) {
        // Calculăm performanța
        float accuracy = (float) correctAnswers / totalQuestions;
        float averageTimePerQuestion = (float) totalTime / totalQuestions / 1000.0f;
        
        // Salvăm statisticile pentru recomandări viitoare
        prefs.edit()
            .putFloat(KEY_PERFORMANCE_STATS + "_accuracy", accuracy)
            .putFloat(KEY_PERFORMANCE_STATS + "_avg_time", averageTimePerQuestion)
            .putInt(KEY_PERFORMANCE_STATS + "_total_questions", totalQuestions)
            .apply();
        
        Log.d(TAG, "Updated performance stats: accuracy=" + accuracy + ", avgTime=" + averageTimePerQuestion);
    }
    
    public String getPerformanceRecommendation() {
        float accuracy = prefs.getFloat(KEY_PERFORMANCE_STATS + "_accuracy", 0.0f);
        float avgTime = prefs.getFloat(KEY_PERFORMANCE_STATS + "_avg_time", 0.0f);
        
        if (accuracy > 0.8f && avgTime < currentDifficulty.getTimeSeconds() * 0.7f) {
            return "Excelent! Poți încerca o dificultate mai mare.";
        } else if (accuracy < 0.5f) {
            return "Încearcă să te concentrezi mai mult pe întrebări.";
        } else if (avgTime > currentDifficulty.getTimeSeconds() * 0.9f) {
            return "Încearcă să răspunzi mai rapid pentru a obține mai multe puncte.";
        } else {
            return "Performanță bună! Continuă să exersezi.";
        }
    }
    
    public boolean canUseLifeline(int lifelinesUsed) {
        switch (currentDifficulty) {
            case EASY:
                return lifelinesUsed < 3;
            case MEDIUM:
                return lifelinesUsed < 2;
            case HARD:
                return lifelinesUsed < 1;
            case EXPERT:
                return false;
            default:
                return true;
        }
    }
    
    public int calculateFinalScore(int basePoints) {
        return Math.round(basePoints * currentDifficulty.getScoreMultiplier());
    }
    
    public void resetProgress() {
        prefs.edit()
            .remove(KEY_CURRENT_DIFFICULTY)
            .remove(KEY_UNLOCKED_DIFFICULTIES)
            .apply();
        currentDifficulty = DifficultyLevel.EASY;
        Log.d(TAG, "Difficulty progress reset");
    }
} 