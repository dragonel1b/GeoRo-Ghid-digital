package com.example.myapplication.bucovinausage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.myapplication.models.EnhancedQuestionModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.*;
import java.lang.reflect.Type;

/**
 * Tracker pentru progresul jucătorului în quiz-ul Bucovina
 */
public class PlayerProgressTracker {
    private static final String TAG = "BucovinaProgressTracker";
    private static final String PREFS_NAME = "BucovinaPlayerProgress";
    
    // Keys pentru SharedPreferences
    private static final String KEY_TOTAL_GAMES = "total_games";
    private static final String KEY_TOTAL_QUESTIONS = "total_questions";
    private static final String KEY_CORRECT_ANSWERS = "correct_answers";
    private static final String KEY_TOTAL_TIME_SPENT = "total_time_spent";
    private static final String KEY_CATEGORY_STATS = "category_stats";
    private static final String KEY_DIFFICULTY_STATS = "difficulty_stats";
    private static final String KEY_GAME_MODE_STATS = "game_mode_stats";
    private static final String KEY_ACHIEVEMENT_PROGRESS = "achievement_progress";
    private static final String KEY_LEARNING_RECOMMENDATIONS = "learning_recommendations";
    private static final String KEY_LAST_GAME_DATE = "last_game_date";
    private static final String KEY_STREAK_DAYS = "streak_days";
    private static final String KEY_BEST_STREAK = "best_streak";
    private static final String KEY_AVERAGE_ACCURACY = "average_accuracy";
    
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;
    
    // Statistici în timp real
    private Map<EnhancedQuestionModel.Category, CategoryStats> categoryStats;
    private Map<EnhancedQuestionModel.Difficulty, DifficultyStats> difficultyStats;
    private Map<String, GameModeStats> gameModeStats;
    private Map<String, Integer> achievementProgress;
    private List<String> learningRecommendations;
    
    public PlayerProgressTracker(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        
        loadAllStats();
    }
    
    /**
     * Încarcă toate statisticile din SharedPreferences
     */
    private void loadAllStats() {
        // Încarcă statistici categorii
        String categoryStatsJson = prefs.getString(KEY_CATEGORY_STATS, "{}");
        Type categoryStatsType = new TypeToken<Map<EnhancedQuestionModel.Category, CategoryStats>>(){}.getType();
        categoryStats = gson.fromJson(categoryStatsJson, categoryStatsType);
        if (categoryStats == null) {
            categoryStats = new HashMap<>();
        }
        
        // Încarcă statistici dificultăți
        String difficultyStatsJson = prefs.getString(KEY_DIFFICULTY_STATS, "{}");
        Type difficultyStatsType = new TypeToken<Map<EnhancedQuestionModel.Difficulty, DifficultyStats>>(){}.getType();
        difficultyStats = gson.fromJson(difficultyStatsJson, difficultyStatsType);
        if (difficultyStats == null) {
            difficultyStats = new HashMap<>();
        }
        
        // Încarcă statistici moduri de joc
        String gameModeStatsJson = prefs.getString(KEY_GAME_MODE_STATS, "{}");
        Type gameModeStatsType = new TypeToken<Map<String, GameModeStats>>(){}.getType();
        gameModeStats = gson.fromJson(gameModeStatsJson, gameModeStatsType);
        if (gameModeStats == null) {
            gameModeStats = new HashMap<>();
        }
        
        // Încarcă progres achievement-uri
        String achievementProgressJson = prefs.getString(KEY_ACHIEVEMENT_PROGRESS, "{}");
        Type achievementProgressType = new TypeToken<Map<String, Integer>>(){}.getType();
        achievementProgress = gson.fromJson(achievementProgressJson, achievementProgressType);
        if (achievementProgress == null) {
            achievementProgress = new HashMap<>();
        }
        
        // Încarcă recomandări
        String recommendationsJson = prefs.getString(KEY_LEARNING_RECOMMENDATIONS, "[]");
        Type recommendationsType = new TypeToken<List<String>>(){}.getType();
        learningRecommendations = gson.fromJson(recommendationsJson, recommendationsType);
        if (learningRecommendations == null) {
            learningRecommendations = new ArrayList<>();
        }
    }
    
    /**
     * Urmărește un răspuns dat de jucător
     */
    public void trackAnswer(EnhancedQuestionModel question, boolean isCorrect, long timeSpent) {
        // Actualizează statisticile globale
        updateGlobalStats(isCorrect, timeSpent);
        
        // Actualizează statisticile pe categorii
        updateCategoryStats(question.getCategory(), isCorrect, timeSpent);
        
        // Actualizează statisticile pe dificultăți
        updateDifficultyStats(question.getDifficulty(), isCorrect, timeSpent);
        
        // Actualizează progresul achievement-urilor
        updateAchievementProgress(isCorrect);
        
        Log.d(TAG, "Tracked answer - Category: " + question.getCategory() + 
              ", Difficulty: " + question.getDifficulty() + ", Correct: " + isCorrect);
    }
    
    /**
     * Actualizează statisticile globale
     */
    private void updateGlobalStats(boolean isCorrect, long timeSpent) {
        int totalQuestions = prefs.getInt(KEY_TOTAL_QUESTIONS, 0) + 1;
        int correctAnswers = prefs.getInt(KEY_CORRECT_ANSWERS, 0) + (isCorrect ? 1 : 0);
        long totalTimeSpent = prefs.getLong(KEY_TOTAL_TIME_SPENT, 0) + timeSpent;
        
        float newAccuracy = (float) correctAnswers / totalQuestions;
        
        prefs.edit()
            .putInt(KEY_TOTAL_QUESTIONS, totalQuestions)
            .putInt(KEY_CORRECT_ANSWERS, correctAnswers)
            .putLong(KEY_TOTAL_TIME_SPENT, totalTimeSpent)
            .putFloat(KEY_AVERAGE_ACCURACY, newAccuracy)
            .apply();
    }
    
    /**
     * Actualizează statisticile pe categorii
     */
    private void updateCategoryStats(EnhancedQuestionModel.Category category, boolean isCorrect, long timeSpent) {
        CategoryStats stats = categoryStats.getOrDefault(category, new CategoryStats());
        stats.totalQuestions++;
        if (isCorrect) {
            stats.correctAnswers++;
        }
        stats.totalTimeSpent += timeSpent;
        stats.averageTime = stats.totalTimeSpent / stats.totalQuestions;
        stats.accuracy = (float) stats.correctAnswers / stats.totalQuestions;
        
        categoryStats.put(category, stats);
        
        // Salvează în SharedPreferences
        String categoryStatsJson = gson.toJson(categoryStats);
        prefs.edit().putString(KEY_CATEGORY_STATS, categoryStatsJson).apply();
    }
    
    /**
     * Actualizează statisticile pe dificultăți
     */
    private void updateDifficultyStats(EnhancedQuestionModel.Difficulty difficulty, boolean isCorrect, long timeSpent) {
        DifficultyStats stats = difficultyStats.getOrDefault(difficulty, new DifficultyStats());
        stats.totalQuestions++;
        if (isCorrect) {
            stats.correctAnswers++;
        }
        stats.totalTimeSpent += timeSpent;
        stats.averageTime = stats.totalTimeSpent / stats.totalQuestions;
        stats.accuracy = (float) stats.correctAnswers / stats.totalQuestions;
        
        difficultyStats.put(difficulty, stats);
        
        // Salvează în SharedPreferences
        String difficultyStatsJson = gson.toJson(difficultyStats);
        prefs.edit().putString(KEY_DIFFICULTY_STATS, difficultyStatsJson).apply();
    }
    
    /**
     * Actualizează progresul achievement-urilor
     */
    private void updateAchievementProgress(boolean isCorrect) {
        if (isCorrect) {
            // Actualizează achievement-urile pentru răspunsuri corecte
            achievementProgress.put("correct_answers", 
                achievementProgress.getOrDefault("correct_answers", 0) + 1);
        }
        
        // Actualizează achievement-urile pentru total întrebări
        achievementProgress.put("total_questions", 
            achievementProgress.getOrDefault("total_questions", 0) + 1);
        
        // Salvează în SharedPreferences
        String achievementProgressJson = gson.toJson(achievementProgress);
        prefs.edit().putString(KEY_ACHIEVEMENT_PROGRESS, achievementProgressJson).apply();
    }
    
    /**
     * Finalizează un joc și actualizează statisticile
     */
    public void finishGame(String gameMode, int correctAnswers, int totalQuestions, long totalTimeSpent) {
        // Actualizează statisticile globale
        int totalGames = prefs.getInt(KEY_TOTAL_GAMES, 0) + 1;
        prefs.edit().putInt(KEY_TOTAL_GAMES, totalGames).apply();
        
        // Actualizează statisticile modului de joc
        updateGameModeStats(gameMode, correctAnswers, totalQuestions, totalTimeSpent);
        
        // Actualizează streak-ul zilnic
        updateDailyStreak();
        
        // Generează recomandări de învățare
        generateLearningRecommendations();
        
        Log.d(TAG, "Game finished - Mode: " + gameMode + ", Score: " + correctAnswers + "/" + totalQuestions);
    }
    
    /**
     * Actualizează statisticile modului de joc
     */
    private void updateGameModeStats(String gameMode, int correctAnswers, int totalQuestions, long totalTimeSpent) {
        GameModeStats stats = gameModeStats.getOrDefault(gameMode, new GameModeStats());
        stats.gamesPlayed++;
        stats.totalQuestions += totalQuestions;
        stats.totalCorrect += correctAnswers;
        stats.totalTimeSpent += totalTimeSpent;
        
        if (correctAnswers > stats.bestScore) {
            stats.bestScore = correctAnswers;
        }
        
        stats.averageScore = (float) stats.totalCorrect / stats.totalQuestions;
        stats.averageTime = stats.totalTimeSpent / stats.totalQuestions;
        
        gameModeStats.put(gameMode, stats);
        
        // Salvează în SharedPreferences
        String gameModeStatsJson = gson.toJson(gameModeStats);
        prefs.edit().putString(KEY_GAME_MODE_STATS, gameModeStatsJson).apply();
    }
    
    /**
     * Actualizează streak-ul zilnic
     */
    private void updateDailyStreak() {
        long currentDate = System.currentTimeMillis() / (1000 * 60 * 60 * 24); // Zile de la epoch
        long lastGameDate = prefs.getLong(KEY_LAST_GAME_DATE, 0);
        
        int currentStreak = prefs.getInt(KEY_STREAK_DAYS, 0);
        int bestStreak = prefs.getInt(KEY_BEST_STREAK, 0);
        
        if (lastGameDate == 0) {
            // Prima zi
            currentStreak = 1;
        } else if (currentDate - lastGameDate == 1) {
            // Zi consecutivă
            currentStreak++;
        } else if (currentDate - lastGameDate > 1) {
            // Streak întrerupt
            currentStreak = 1;
        }
        // Dacă currentDate == lastGameDate, nu schimbăm streak-ul (același zi)
        
        if (currentStreak > bestStreak) {
            bestStreak = currentStreak;
        }
        
        prefs.edit()
            .putLong(KEY_LAST_GAME_DATE, currentDate)
            .putInt(KEY_STREAK_DAYS, currentStreak)
            .putInt(KEY_BEST_STREAK, bestStreak)
            .apply();
    }
    
    /**
     * Generează recomandări de învățare bazate pe performanță
     */
    private void generateLearningRecommendations() {
        learningRecommendations.clear();
        
        // Analizează performanța pe categorii
        EnhancedQuestionModel.Category weakestCategory = null;
        float lowestAccuracy = 1.0f;
        
        for (Map.Entry<EnhancedQuestionModel.Category, CategoryStats> entry : categoryStats.entrySet()) {
            CategoryStats stats = entry.getValue();
            if (stats.totalQuestions >= 5 && stats.accuracy < lowestAccuracy) {
                lowestAccuracy = stats.accuracy;
                weakestCategory = entry.getKey();
            }
        }
        
        if (weakestCategory != null) {
            learningRecommendations.add("Exersează mai mult categoria " + 
                weakestCategory.displayName + " (acuratețe: " + 
                Math.round(lowestAccuracy * 100) + "%)");
        }
        
        // Analizează performanța pe dificultăți
        EnhancedQuestionModel.Difficulty weakestDifficulty = null;
        float lowestDifficultyAccuracy = 1.0f;
        
        for (Map.Entry<EnhancedQuestionModel.Difficulty, DifficultyStats> entry : difficultyStats.entrySet()) {
            DifficultyStats stats = entry.getValue();
            if (stats.totalQuestions >= 3 && stats.accuracy < lowestDifficultyAccuracy) {
                lowestDifficultyAccuracy = stats.accuracy;
                weakestDifficulty = entry.getKey();
            }
        }
        
        if (weakestDifficulty != null) {
            learningRecommendations.add("Încearcă să exersezi mai mult întrebările de nivel " + 
                weakestDifficulty.displayName + " (acuratețe: " + 
                Math.round(lowestDifficultyAccuracy * 100) + "%)");
        }
        
        // Recomandări generale
        float overallAccuracy = prefs.getFloat(KEY_AVERAGE_ACCURACY, 0);
        if (overallAccuracy < 0.6f) {
            learningRecommendations.add("Încearcă să te concentrezi mai mult pe întrebări și să citești cu atenție toate opțiunile");
        } else if (overallAccuracy > 0.9f) {
            learningRecommendations.add("Performanță excelentă! Încearcă modurile de joc mai provocatoare");
        }
        
        // Salvează recomandările
        String recommendationsJson = gson.toJson(learningRecommendations);
        prefs.edit().putString(KEY_LEARNING_RECOMMENDATIONS, recommendationsJson).apply();
    }
    
    // Getters pentru statistici
    public int getTotalGames() {
        return prefs.getInt(KEY_TOTAL_GAMES, 0);
    }
    
    public int getTotalQuestions() {
        return prefs.getInt(KEY_TOTAL_QUESTIONS, 0);
    }
    
    public int getCorrectAnswers() {
        return prefs.getInt(KEY_CORRECT_ANSWERS, 0);
    }
    
    public float getOverallAccuracy() {
        return prefs.getFloat(KEY_AVERAGE_ACCURACY, 0);
    }
    
    public long getTotalTimeSpent() {
        return prefs.getLong(KEY_TOTAL_TIME_SPENT, 0);
    }
    
    public int getCurrentStreak() {
        return prefs.getInt(KEY_STREAK_DAYS, 0);
    }
    
    public int getBestStreak() {
        return prefs.getInt(KEY_BEST_STREAK, 0);
    }
    
    public Map<EnhancedQuestionModel.Category, CategoryStats> getCategoryStats() {
        return new HashMap<>(categoryStats);
    }
    
    public Map<EnhancedQuestionModel.Difficulty, DifficultyStats> getDifficultyStats() {
        return new HashMap<>(difficultyStats);
    }
    
    public Map<String, GameModeStats> getGameModeStats() {
        return new HashMap<>(gameModeStats);
    }
    
    public List<String> getLearningRecommendations() {
        return new ArrayList<>(learningRecommendations);
    }
    
    public Map<String, Integer> getAchievementProgress() {
        return new HashMap<>(achievementProgress);
    }
    
    /**
     * Resetează toate statisticile
     */
    public void resetAllStats() {
        prefs.edit().clear().apply();
        categoryStats.clear();
        difficultyStats.clear();
        gameModeStats.clear();
        achievementProgress.clear();
        learningRecommendations.clear();
    }
    
    // Clase pentru statistici
    public static class CategoryStats {
        public int totalQuestions = 0;
        public int correctAnswers = 0;
        public long totalTimeSpent = 0;
        public float accuracy = 0;
        public long averageTime = 0;
    }
    
    public static class DifficultyStats {
        public int totalQuestions = 0;
        public int correctAnswers = 0;
        public long totalTimeSpent = 0;
        public float accuracy = 0;
        public long averageTime = 0;
    }
    
    public static class GameModeStats {
        public int gamesPlayed = 0;
        public int totalQuestions = 0;
        public int totalCorrect = 0;
        public long totalTimeSpent = 0;
        public int bestScore = 0;
        public float averageScore = 0;
        public long averageTime = 0;
    }
} 