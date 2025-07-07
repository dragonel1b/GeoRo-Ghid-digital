package com.example.myapplication.olteniausage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Manager pentru sistemul de dificultate adaptiv al quiz-ului Oltenia
 */
public class DifficultyManager {
    private static final String TAG = "DifficultyManager";
    private static final String PREFS_NAME = "OlteniaDifficulty";
    private static final String KEY_CURRENT_DIFFICULTY = "current_difficulty";
    private static final String KEY_TOTAL_GAMES = "total_games";
    private static final String KEY_TOTAL_CORRECT = "total_correct";
    private static final String KEY_AVERAGE_TIME = "average_time";
    
    public enum DifficultyLevel {
        BEGINNER(40000, 1.5f, 3, "Începător", "40s per întrebare"),
        NORMAL(30000, 1.0f, 3, "Normal", "30s per întrebare"),
        ADVANCED(20000, 1.2f, 2, "Avansat", "20s per întrebare"),
        EXPERT(15000, 1.5f, 1, "Expert", "15s per întrebare"),
        MASTER(10000, 2.0f, 0, "Maestru", "10s per întrebare");
        
        public final int timePerQuestion;
        public final float pointsMultiplier;
        public final int availableLifelines;
        public final String displayName;
        public final String description;
        
        DifficultyLevel(int timePerQuestion, float pointsMultiplier, int availableLifelines, 
                       String displayName, String description) {
            this.timePerQuestion = timePerQuestion;
            this.pointsMultiplier = pointsMultiplier;
            this.availableLifelines = availableLifelines;
            this.displayName = displayName;
            this.description = description;
        }
    }
    
    private Context context;
    private SharedPreferences prefs;
    private DifficultyLevel currentDifficulty;
    
    public DifficultyManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.currentDifficulty = DifficultyLevel.valueOf(
            prefs.getString(KEY_CURRENT_DIFFICULTY, DifficultyLevel.NORMAL.name())
        );
    }
    
    /**
     * Calculează nivelul de dificultate bazat pe performanța utilizatorului
     */
    public DifficultyLevel calculateAdaptiveDifficulty(int correctAnswers, int totalQuestions, 
                                                      long averageTimePerQuestion) {
        if (totalQuestions < 5) {
            return DifficultyLevel.NORMAL; // Păstrăm normal pentru începători
        }
        
        float accuracy = (float) correctAnswers / totalQuestions;
        float timeRatio = (float) averageTimePerQuestion / 30000; // Raport față de 30s
        
        Log.d(TAG, "Calculating difficulty - Accuracy: " + accuracy + ", Time ratio: " + timeRatio);
        
        // Algoritm de adaptare bazat pe acuratețe și viteză
        if (accuracy >= 0.95f && timeRatio <= 0.5f) {
            return DifficultyLevel.MASTER;
        } else if (accuracy >= 0.85f && timeRatio <= 0.7f) {
            return DifficultyLevel.EXPERT;
        } else if (accuracy >= 0.75f && timeRatio <= 0.8f) {
            return DifficultyLevel.ADVANCED;
        } else if (accuracy >= 0.60f) {
            return DifficultyLevel.NORMAL;
        } else {
            return DifficultyLevel.BEGINNER;
        }
    }
    
    /**
     * Actualizează dificultatea după un quiz completat
     */
    public void updateDifficultyAfterGame(int correctAnswers, int totalQuestions, 
                                         long totalTimeSpent) {
        // Actualizăm statisticile globale
        int totalGames = prefs.getInt(KEY_TOTAL_GAMES, 0) + 1;
        int totalCorrect = prefs.getInt(KEY_TOTAL_CORRECT, 0) + correctAnswers;
        long averageTime = calculateAverageTime(totalTimeSpent, totalQuestions);
        
        // Calculăm noua dificultate
        DifficultyLevel newDifficulty = calculateAdaptiveDifficulty(
            totalCorrect, totalGames * 10, averageTime);
        
        // Salvăm în SharedPreferences
        prefs.edit()
            .putString(KEY_CURRENT_DIFFICULTY, newDifficulty.name())
            .putInt(KEY_TOTAL_GAMES, totalGames)
            .putInt(KEY_TOTAL_CORRECT, totalCorrect)
            .putLong(KEY_AVERAGE_TIME, averageTime)
            .apply();
            
        currentDifficulty = newDifficulty;
        
        Log.d(TAG, "Difficulty updated to: " + newDifficulty.displayName);
    }
    
    /**
     * Calculează timpul mediu per întrebare
     */
    private long calculateAverageTime(long totalTimeSpent, int totalQuestions) {
        if (totalQuestions == 0) return 30000;
        return totalTimeSpent / totalQuestions;
    }
    
    /**
     * Obține dificultatea curentă
     */
    public DifficultyLevel getCurrentDifficulty() {
        return currentDifficulty;
    }
    
    /**
     * Setează manual dificultatea (pentru setări utilizator)
     */
    public void setManualDifficulty(DifficultyLevel difficulty) {
        this.currentDifficulty = difficulty;
        prefs.edit().putString(KEY_CURRENT_DIFFICULTY, difficulty.name()).apply();
    }
    
    /**
     * Verifică dacă utilizatorul poate folosi toate lifeline-urile
     */
    public boolean canUseLifeline(int lifelinesUsed) {
        return lifelinesUsed < currentDifficulty.availableLifelines;
    }
    
    /**
     * Calculează punctajul final cu multiplicatorul de dificultate
     */
    public int calculateFinalScore(int baseScore) {
        return Math.round(baseScore * currentDifficulty.pointsMultiplier);
    }
    
    /**
     * Obține recomandări pentru utilizator bazate pe performanță
     */
    public String getPerformanceRecommendation() {
        int totalGames = prefs.getInt(KEY_TOTAL_GAMES, 0);
        int totalCorrect = prefs.getInt(KEY_TOTAL_CORRECT, 0);
        
        if (totalGames == 0) {
            return "Bine ai venit în Oltenia! Începe cu nivelul Normal și vezi cum te descurci.";
        }
        
        float overallAccuracy = (float) totalCorrect / (totalGames * 10);
        
        if (overallAccuracy >= 0.9f) {
            return "Performanță excelentă în Oltenia! Încearcă nivelul Expert pentru o provocare mai mare.";
        } else if (overallAccuracy >= 0.7f) {
            return "Performanță bună în Oltenia! Continuă să exersezi pentru a ajunge la nivelul următor.";
        } else {
            return "Continuă să exersezi Oltenia! Încearcă să te concentrezi pe categoriile cu care ai dificultăți.";
        }
    }
    
    /**
     * Resetează statisticile de dificultate
     */
    public void resetDifficultyStats() {
        prefs.edit().clear().apply();
        currentDifficulty = DifficultyLevel.NORMAL;
    }
} 