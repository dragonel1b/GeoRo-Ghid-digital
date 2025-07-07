package com.example.myapplication.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.myapplication.model.base.BaseGameOverActivity;
import com.example.myapplication.transilvaniausage.GameOverActivity;

import java.util.Map;

/**
 * Helper class pentru lansarea modulară a GameOverActivity pentru toate regiunile
 * Facilitează integrarea cu baza de date și standardizează transmiterea datelor
 */
public class GameOverHelper {
    
    private static final String TAG = "GameOverHelper";
    
    /**
     * Builder pattern pentru construirea unui Intent modular pentru GameOver
     */
    public static class Builder {
        private final Context context;
        private final Intent intent;
        
        public Builder(Context context, String regionName) {
            this.context = context;
            
            // Determinăm clasa de GameOver în funcție de regiune
            Class<?> gameOverClass = getGameOverClassForRegion(regionName);
            this.intent = new Intent(context, gameOverClass);
            
            // Setăm datele implicite
            BaseGameOverActivity.RegionConfig config = getRegionConfig(regionName);
            if (config != null) {
                intent.putExtra("regionName", config.displayName);
                intent.putExtra("quizTitle", config.quizTitle);
            }
            intent.putExtra("gameType", "quiz");
        }
        
        public Builder setScore(int score) {
            intent.putExtra("score", score);
            return this;
        }
        
        public Builder setQuestionStats(int correctAnswers, int totalQuestions) {
            intent.putExtra("correctAnswers", correctAnswers);
            intent.putExtra("totalQuestions", totalQuestions);
            return this;
        }
        
        public Builder setMaxStreak(int maxStreak) {
            intent.putExtra("maxStreak", maxStreak);
            return this;
        }
        
        public Builder setTotalTime(long totalTime) {
            intent.putExtra("totalTime", totalTime);
            return this;
        }
        
        public Builder setAchievements(String achievements) {
            intent.putExtra("ACHIEVEMENTS", achievements);
            return this;
        }
        
        public Builder setGameType(String gameType) {
            intent.putExtra("gameType", gameType);
            return this;
        }
        
        public Builder setCustomQuizTitle(String quizTitle) {
            intent.putExtra("quizTitle", quizTitle);
            return this;
        }
        
        public Builder setDatabaseId(String databaseId) {
            intent.putExtra("databaseId", databaseId);
            return this;
        }
        
        public Builder setUserId(String userId) {
            intent.putExtra("userId", userId);
            return this;
        }
        
        public Builder setSessionData(String sessionId, long sessionStartTime) {
            intent.putExtra("sessionId", sessionId);
            intent.putExtra("sessionStartTime", sessionStartTime);
            return this;
        }
        
        /**
         * Adaugă toate datele necesare pentru salvarea în baza de date
         */
        public Builder withDatabaseIntegration(String userId, String sessionId, long sessionStartTime) {
            return setUserId(userId)
                   .setSessionData(sessionId, sessionStartTime)
                   .setDatabaseId(generateDatabaseId());
        }
        
        /**
         * Construiește și returnează Intent-ul
         */
        public Intent build() {
            return intent;
        }
        
        /**
         * Construiește Intent-ul și lansează activitatea
         */
        public void launch() {
            context.startActivity(intent);
        }
        
        private String generateDatabaseId() {
            String region = intent.getStringExtra("regionName");
            String gameType = intent.getStringExtra("gameType");
            long timestamp = System.currentTimeMillis();
            return String.format("%s_%s_%d", region, gameType, timestamp).toLowerCase().replaceAll("\\s+", "_");
        }
    }
    
    /**
     * Creează un Builder pentru o regiune specifică
     */
    public static Builder forRegion(Context context, String regionName) {
        return new Builder(context, regionName);
    }
    
    /**
     * Metodă de conveniență pentru Transilvania (backward compatibility)
     */
    public static Builder forTransilvania(Context context) {
        return forRegion(context, "transilvania");
    }
    
    /**
     * Metodă de conveniență pentru Banat
     */
    public static Builder forBanat(Context context) {
        return forRegion(context, "banat");
    }
    
    /**
     * Metodă de conveniență pentru Bucovina
     */
    public static Builder forBucovina(Context context) {
        return forRegion(context, "bucovina");
    }
    
    /**
     * Metodă de conveniență pentru Crișana
     */
    public static Builder forCrisana(Context context) {
        return forRegion(context, "crisana");
    }
    
    /**
     * Metodă de conveniență pentru Dobrogea
     */
    public static Builder forDobrogea(Context context) {
        return forRegion(context, "dobrogea");
    }
    
    /**
     * Metodă de conveniență pentru Maramureș
     */
    public static Builder forMaramures(Context context) {
        return forRegion(context, "maramures");
    }
    
    /**
     * Metodă de conveniență pentru Moldova
     */
    public static Builder forMoldova(Context context) {
        return forRegion(context, "moldova");
    }
    
    /**
     * Metodă de conveniență pentru Muntenia
     */
    public static Builder forMuntenia(Context context) {
        return forRegion(context, "muntenia");
    }
    
    /**
     * Metodă de conveniență pentru Oltenia
     */
    public static Builder forOltenia(Context context) {
        return forRegion(context, "oltenia");
    }
    
    /**
     * Determină clasa GameOverActivity pentru o regiune
     */
    private static Class<?> getGameOverClassForRegion(String regionName) {
        switch (regionName.toLowerCase()) {
            case "transilvania":
                return GameOverActivity.class;
            // Pentru celelalte regiuni, pentru moment returnăm GameOverActivity generic
            // Acestea pot fi înlocuite cu clase specifice când vor fi implementate
            case "banat":
            case "bucovina":
            case "crisana":
            case "dobrogea":
            case "maramures":
            case "moldova":
            case "muntenia":
            case "oltenia":
            default:
                Log.d(TAG, "Using generic GameOverActivity for region: " + regionName);
                return GameOverActivity.class;
        }
    }
    
    /**
     * Obține configurația pentru o regiune
     */
    private static BaseGameOverActivity.RegionConfig getRegionConfig(String regionName) {
        Map<String, BaseGameOverActivity.RegionConfig> configs = BaseGameOverActivity.getRegionConfigs();
        return configs.get(regionName.toLowerCase());
    }
    
    /**
     * Validează datele înainte de lansare
     */
    public static boolean validateGameData(int correctAnswers, int totalQuestions, int score) {
        if (totalQuestions <= 0) {
            Log.e(TAG, "Invalid totalQuestions: " + totalQuestions);
            return false;
        }
        
        if (correctAnswers < 0 || correctAnswers > totalQuestions) {
            Log.e(TAG, "Invalid correctAnswers: " + correctAnswers + " (total: " + totalQuestions + ")");
            return false;
        }
        
        if (score < 0) {
            Log.e(TAG, "Invalid score: " + score);
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculează statistici suplimentare pentru baza de date
     */
    public static GameStats calculateStats(int correctAnswers, int totalQuestions, int score, 
                                         int maxStreak, long totalTime) {
        int percentage = totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;
        long averageTimePerQuestion = totalQuestions > 0 ? totalTime / totalQuestions : 0;
        
        return new GameStats(percentage, averageTimePerQuestion, 
                           calculateAccuracyRating(percentage), 
                           calculatePerformanceLevel(percentage));
    }
    
    private static String calculateAccuracyRating(int percentage) {
        if (percentage == 100) return "perfect";
        if (percentage >= 90) return "excellent";
        if (percentage >= 80) return "very_good";
        if (percentage >= 70) return "good";
        if (percentage >= 60) return "decent";
        return "needs_improvement";
    }
    
    private static int calculatePerformanceLevel(int percentage) {
        if (percentage >= 90) return 5;
        if (percentage >= 80) return 4;
        if (percentage >= 70) return 3;
        if (percentage >= 60) return 2;
        return 1;
    }
    
    /**
     * Clasa pentru statistici calculate
     */
    public static class GameStats {
        public final int percentage;
        public final long averageTimePerQuestion;
        public final String accuracyRating;
        public final int performanceLevel;
        
        public GameStats(int percentage, long averageTimePerQuestion, 
                        String accuracyRating, int performanceLevel) {
            this.percentage = percentage;
            this.averageTimePerQuestion = averageTimePerQuestion;
            this.accuracyRating = accuracyRating;
            this.performanceLevel = performanceLevel;
        }
    }
} 