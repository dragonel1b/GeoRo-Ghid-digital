package com.example.myapplication.moldovausage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracker pentru progresul jucătorului în quiz-ul Moldova
 * Similar cu cel din Transilvania pentru consistență
 */
public class PlayerProgressTracker {
    private static final String TAG = "MoldovaProgressTracker";
    private static final String PREFS_NAME = "MoldovaProgressPrefs";
    
    // Keys pentru SharedPreferences
    private static final String KEY_TOTAL_GAMES = "total_games";
    private static final String KEY_TOTAL_SCORE = "total_score";
    private static final String KEY_BEST_SCORE = "best_score";
    private static final String KEY_TOTAL_CORRECT = "total_correct";
    private static final String KEY_TOTAL_QUESTIONS = "total_questions";
    private static final String KEY_LONGEST_STREAK = "longest_streak";
    private static final String KEY_TOTAL_TIME = "total_time";
    private static final String KEY_DAILY_STREAK = "daily_streak";
    private static final String KEY_LAST_PLAY_DATE = "last_play_date";
    private static final String KEY_CATEGORY_STATS = "category_stats_";
    private static final String KEY_DIFFICULTY_STATS = "difficulty_stats_";
    private static final String KEY_MODE_STATS = "mode_stats_";
    
    private Context context;
    private SharedPreferences prefs;
    
    public static class QuizStats {
        public int totalGamesPlayed;
        public int totalScore;
        public int bestScore;
        public int totalCorrectAnswers;
        public int totalQuestionsAnswered;
        public int longestStreak;
        public long totalTimeSpent;
        public int dailyStreak;
        public float overallAccuracy;
        public long averageTimePerQuestion;
        
        public QuizStats() {
            totalGamesPlayed = 0;
            totalScore = 0;
            bestScore = 0;
            totalCorrectAnswers = 0;
            totalQuestionsAnswered = 0;
            longestStreak = 0;
            totalTimeSpent = 0;
            dailyStreak = 0;
            overallAccuracy = 0.0f;
            averageTimePerQuestion = 0;
        }
    }
    
    public static class CategoryStats {
        public String categoryName;
        public int questionsAnswered;
        public int correctAnswers;
        public float accuracy;
        public boolean isStrong;
        public boolean isWeak;
        
        public CategoryStats(String categoryName) {
            this.categoryName = categoryName;
            this.questionsAnswered = 0;
            this.correctAnswers = 0;
            this.accuracy = 0.0f;
            this.isStrong = false;
            this.isWeak = false;
        }
    }
    
    public PlayerProgressTracker(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public QuizStats getCurrentStats() {
        QuizStats stats = new QuizStats();
        
        stats.totalGamesPlayed = prefs.getInt(KEY_TOTAL_GAMES, 0);
        stats.totalScore = prefs.getInt(KEY_TOTAL_SCORE, 0);
        stats.bestScore = prefs.getInt(KEY_BEST_SCORE, 0);
        stats.totalCorrectAnswers = prefs.getInt(KEY_TOTAL_CORRECT, 0);
        stats.totalQuestionsAnswered = prefs.getInt(KEY_TOTAL_QUESTIONS, 0);
        stats.longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0);
        stats.totalTimeSpent = prefs.getLong(KEY_TOTAL_TIME, 0);
        stats.dailyStreak = prefs.getInt(KEY_DAILY_STREAK, 0);
        
        // Calculăm acuratețea generală
        if (stats.totalQuestionsAnswered > 0) {
            stats.overallAccuracy = (float) stats.totalCorrectAnswers / stats.totalQuestionsAnswered;
        }
        
        // Calculăm timpul mediu per întrebare
        if (stats.totalQuestionsAnswered > 0) {
            stats.averageTimePerQuestion = stats.totalTimeSpent / stats.totalQuestionsAnswered;
        }
        
        return stats;
    }
    
    public void recordQuizResult(int score, int correctAnswers, int totalQuestions, 
                                int maxStreak, long totalTime, String category) {
        SharedPreferences.Editor editor = prefs.edit();
        
        // Actualizăm statisticile generale
        int currentTotalGames = prefs.getInt(KEY_TOTAL_GAMES, 0) + 1;
        int currentTotalScore = prefs.getInt(KEY_TOTAL_SCORE, 0) + score;
        int currentBestScore = Math.max(prefs.getInt(KEY_BEST_SCORE, 0), score);
        int currentTotalCorrect = prefs.getInt(KEY_TOTAL_CORRECT, 0) + correctAnswers;
        int currentTotalQuestions = prefs.getInt(KEY_TOTAL_QUESTIONS, 0) + totalQuestions;
        int currentLongestStreak = Math.max(prefs.getInt(KEY_LONGEST_STREAK, 0), maxStreak);
        long currentTotalTime = prefs.getLong(KEY_TOTAL_TIME, 0) + totalTime;
        
        editor.putInt(KEY_TOTAL_GAMES, currentTotalGames);
        editor.putInt(KEY_TOTAL_SCORE, currentTotalScore);
        editor.putInt(KEY_BEST_SCORE, currentBestScore);
        editor.putInt(KEY_TOTAL_CORRECT, currentTotalCorrect);
        editor.putInt(KEY_TOTAL_QUESTIONS, currentTotalQuestions);
        editor.putInt(KEY_LONGEST_STREAK, currentLongestStreak);
        editor.putLong(KEY_TOTAL_TIME, currentTotalTime);
        
        // Actualizăm daily streak
        updateDailyStreak(editor);
        
        // Actualizăm statisticile pe categorii
        updateCategoryStats(editor, category, correctAnswers, totalQuestions);
        
        editor.apply();
        
        Log.d(TAG, "Quiz result recorded - Score: " + score + ", Accuracy: " + 
              String.format("%.1f%%", (float)correctAnswers/totalQuestions*100));
    }
    
    private void updateDailyStreak(SharedPreferences.Editor editor) {
        long currentTime = System.currentTimeMillis();
        long lastPlayDate = prefs.getLong(KEY_LAST_PLAY_DATE, 0);
        int currentStreak = prefs.getInt(KEY_DAILY_STREAK, 0);
        
        // Verificăm dacă jucăm astăzi
        if (isSameDay(currentTime, lastPlayDate)) {
            // Jucăm în aceeași zi, nu modificăm streak-ul
        } else if (isConsecutiveDay(currentTime, lastPlayDate)) {
            // Jucăm în ziua consecutivă, incrementăm streak-ul
            currentStreak++;
        } else {
            // Nu jucăm în ziua consecutivă, resetăm streak-ul
            currentStreak = 1;
        }
        
        editor.putInt(KEY_DAILY_STREAK, currentStreak);
        editor.putLong(KEY_LAST_PLAY_DATE, currentTime);
    }
    
    private boolean isSameDay(long time1, long time2) {
        long day1 = time1 / (24 * 60 * 60 * 1000);
        long day2 = time2 / (24 * 60 * 60 * 1000);
        return day1 == day2;
    }
    
    private boolean isConsecutiveDay(long currentTime, long lastTime) {
        long currentDay = currentTime / (24 * 60 * 60 * 1000);
        long lastDay = lastTime / (24 * 60 * 60 * 1000);
        return currentDay == lastDay + 1;
    }
    
    private void updateCategoryStats(SharedPreferences.Editor editor, String category, 
                                   int correctAnswers, int totalQuestions) {
        String key = KEY_CATEGORY_STATS + category;
        String statsString = prefs.getString(key, "0,0"); // format: "answered,correct"
        
        String[] parts = statsString.split(",");
        int answered = Integer.parseInt(parts[0]);
        int correct = Integer.parseInt(parts[1]);
        
        answered += totalQuestions;
        correct += correctAnswers;
        
        String newStats = answered + "," + correct;
        editor.putString(key, newStats);
    }
    
    public Map<String, CategoryStats> getCategoryStats() {
        Map<String, CategoryStats> categoryStats = new HashMap<>();
        
        // Categorii predefinite pentru Moldova
        String[] categories = {"ISTORIE", "GEOGRAFIE", "CULTURĂ", "PERSONALITĂȚI", "MONUMENTE"};
        
        for (String category : categories) {
            CategoryStats stats = new CategoryStats(category);
            String key = KEY_CATEGORY_STATS + category;
            String statsString = prefs.getString(key, "0,0");
            
            String[] parts = statsString.split(",");
            stats.questionsAnswered = Integer.parseInt(parts[0]);
            stats.correctAnswers = Integer.parseInt(parts[1]);
            
            if (stats.questionsAnswered > 0) {
                stats.accuracy = (float) stats.correctAnswers / stats.questionsAnswered;
                stats.isStrong = stats.accuracy >= 0.8f;
                stats.isWeak = stats.accuracy <= 0.4f;
            }
            
            categoryStats.put(category, stats);
        }
        
        return categoryStats;
    }
    
    public void resetProgress() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        Log.d(TAG, "Player progress reset");
    }
    
    public int getTotalQuizzesCompleted() {
        return prefs.getInt(KEY_TOTAL_GAMES, 0);
    }
    
    public int getBestScore() {
        return prefs.getInt(KEY_BEST_SCORE, 0);
    }
    
    public int getDailyStreak() {
        return prefs.getInt(KEY_DAILY_STREAK, 0);
    }
    
    // Metodele noi necesare pentru compatibilitate cu MoldovaGameActivity
    public void startNewSession() {
        Log.d(TAG, "Started new quiz session");
        // Session start logic can be added here if needed
    }
    
    public void endSession(int score, GameModeManager.GameMode gameMode) {
        Log.d(TAG, "Ended session with score: " + score + " in mode: " + gameMode.getDisplayName());
        // Additional session end logic can be added here
    }
    
    public java.util.List<String> checkForNewAchievements() {
        java.util.List<String> newAchievements = new java.util.ArrayList<>();
        QuizStats stats = getCurrentStats();
        
        // Verificăm pentru realizări noi
        if (stats.totalGamesPlayed == 1) {
            newAchievements.add("Prima încercare!");
        }
        if (stats.totalGamesPlayed == 10) {
            newAchievements.add("Jucător dedicat!");
        }
        if (stats.bestScore >= 1000) {
            newAchievements.add("Scor înalt!");
        }
        if (stats.overallAccuracy >= 0.9f && stats.totalQuestionsAnswered >= 50) {
            newAchievements.add("Maestru quiz!");
        }
        if (stats.dailyStreak >= 7) {
            newAchievements.add("Săptămână perfectă!");
        }
        
        return newAchievements;
    }
    
    public void trackAnswer(String questionId, boolean isCorrect, long timeSpent, 
                           com.example.myapplication.core.domain.model.EnhancedQuestionModel.Category category,
                           com.example.myapplication.core.domain.model.EnhancedQuestionModel.Difficulty difficulty) {
        Log.d(TAG, "Tracking answer: " + questionId + ", correct: " + isCorrect + 
              ", time: " + timeSpent + "ms, category: " + category + ", difficulty: " + difficulty);
        
        // Aici putem adăuga logica pentru urmărirea detaliată a răspunsurilor
        // Pentru moment, doar înregistrăm informația
        
        // Salvăm categoria răspunsului pentru analize viitoare
        SharedPreferences.Editor editor = prefs.edit();
        String categoryKey = KEY_CATEGORY_STATS + category.name();
        String statsString = prefs.getString(categoryKey, "0,0");
        String[] parts = statsString.split(",");
        int answered = Integer.parseInt(parts[0]) + 1;
        int correct = Integer.parseInt(parts[1]) + (isCorrect ? 1 : 0);
        
        editor.putString(categoryKey, answered + "," + correct);
        editor.apply();
    }
} 