package com.example.myapplication.banatusage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.myapplication.core.domain.model.EnhancedQuestionModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tracker pentru progresul și performanța detaliată a jucătorului în Banat
 */
public class PlayerProgressTracker {
    private static final String TAG = "BanatProgressTracker";
    private static final String PREFS_NAME = "BanatPlayerProgressTracking";
    private static final String KEY_QUIZ_STATS = "quiz_stats";
    private static final String KEY_CATEGORY_STATS = "category_stats";
    private static final String KEY_DIFFICULTY_STATS = "difficulty_stats";
    private static final String KEY_LEARNING_PATH = "learning_path";
    
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;
    private QuizStats currentSessionStats;
    
    public static class QuizStats {
        public int totalGamesPlayed = 0;
        public int totalQuestionsAnswered = 0;
        public int totalCorrectAnswers = 0;
        public long totalTimeSpent = 0; // în milisecunde
        public int longestStreak = 0;
        public int currentStreak = 0;
        public Map<String, CategoryStats> categoryStats = new HashMap<>();
        public Map<String, DifficultyStats> difficultyStats = new HashMap<>();
        public List<String> weakTopics = new ArrayList<>();
        public List<String> strongTopics = new ArrayList<>();
        public Map<String, Integer> questionHistory = new HashMap<>(); // questionId -> times answered
        public List<LearningRecommendation> recommendations = new ArrayList<>();
        public Date lastPlayed = new Date();
        public float averageTimePerQuestion = 0;
        public float overallAccuracy = 0;
        
        public void updateAccuracy() {
            if (totalQuestionsAnswered > 0) {
                overallAccuracy = (float) totalCorrectAnswers / totalQuestionsAnswered;
            }
        }
        
        public void updateAverageTime() {
            if (totalQuestionsAnswered > 0) {
                averageTimePerQuestion = (float) totalTimeSpent / totalQuestionsAnswered;
            }
        }
    }
    
    public static class CategoryStats {
        public String categoryName;
        public int questionsAnswered = 0;
        public int correctAnswers = 0;
        public long totalTime = 0;
        public int streak = 0;
        public int maxStreak = 0;
        public float accuracy = 0;
        public float averageTime = 0;
        public Date lastPlayed = new Date();
        public boolean isWeak = false;
        public boolean isStrong = false;
        
        public CategoryStats(String categoryName) {
            this.categoryName = categoryName;
        }
        
        public void updateStats() {
            if (questionsAnswered > 0) {
                accuracy = (float) correctAnswers / questionsAnswered;
                averageTime = (float) totalTime / questionsAnswered;
                isWeak = accuracy < 0.6f;
                isStrong = accuracy > 0.85f && questionsAnswered >= 5;
            }
        }
    }
    
    public static class DifficultyStats {
        public String difficultyName;
        public int questionsAnswered = 0;
        public int correctAnswers = 0;
        public long totalTime = 0;
        public float accuracy = 0;
        public float averageTime = 0;
        public boolean isComfortable = false; // >75% accuracy with 10+ questions
        
        public DifficultyStats(String difficultyName) {
            this.difficultyName = difficultyName;
        }
        
        public void updateStats() {
            if (questionsAnswered > 0) {
                accuracy = (float) correctAnswers / questionsAnswered;
                averageTime = (float) totalTime / questionsAnswered;
                isComfortable = accuracy > 0.75f && questionsAnswered >= 10;
            }
        }
    }
    
    public static class LearningRecommendation {
        public String title;
        public String description;
        public String category;
        public String difficulty;
        public int priority; // 1-5, 5 being highest
        public Date created;
        
        public LearningRecommendation(String title, String description, String category, 
                                    String difficulty, int priority) {
            this.title = title;
            this.description = description;
            this.category = category;
            this.difficulty = difficulty;
            this.priority = priority;
            this.created = new Date();
        }
    }
    
    public PlayerProgressTracker(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.currentSessionStats = new QuizStats();
        loadStats();
    }
    
    /**
     * Înregistrează un răspuns și actualizează statisticile
     */
    public void trackAnswer(String questionId, boolean isCorrect, long timeSpent,
                           EnhancedQuestionModel.Category category,
                           EnhancedQuestionModel.Difficulty difficulty) {
        
        QuizStats stats = getCurrentStats();
        
        // Actualizează statisticile generale
        stats.totalQuestionsAnswered++;
        stats.totalTimeSpent += timeSpent;
        if (isCorrect) {
            stats.totalCorrectAnswers++;
            stats.currentStreak++;
            if (stats.currentStreak > stats.longestStreak) {
                stats.longestStreak = stats.currentStreak;
            }
        } else {
            stats.currentStreak = 0;
        }
        
        // Actualizează statisticile pe categorii
        updateCategoryStats(stats, category.name(), isCorrect, timeSpent);
        
        // Actualizează statisticile pe dificultăți
        updateDifficultyStats(stats, difficulty.name(), isCorrect, timeSpent);
        
        // Înregistrează întrebarea în istoric
        stats.questionHistory.put(questionId, 
            stats.questionHistory.getOrDefault(questionId, 0) + 1);
        
        // Actualizează acuratețea și timpul mediu
        stats.updateAccuracy();
        stats.updateAverageTime();
        stats.lastPlayed = new Date();
        
        // Generează recomandări actualizate
        generateLearningRecommendations(stats);
        
        // Salvează statisticile
        saveStats(stats);
        
        Log.d(TAG, "Answer tracked - Correct: " + isCorrect + 
               ", Category: " + category.name() + 
               ", Difficulty: " + difficulty.name() +
               ", Overall accuracy: " + stats.overallAccuracy);
    }
    
    /**
     * Actualizează statisticile pentru o categorie specifică
     */
    private void updateCategoryStats(QuizStats stats, String categoryName, 
                                   boolean isCorrect, long timeSpent) {
        CategoryStats categoryStats = stats.categoryStats.get(categoryName);
        if (categoryStats == null) {
            categoryStats = new CategoryStats(categoryName);
            stats.categoryStats.put(categoryName, categoryStats);
        }
        
        categoryStats.questionsAnswered++;
        categoryStats.totalTime += timeSpent;
        categoryStats.lastPlayed = new Date();
        
        if (isCorrect) {
            categoryStats.correctAnswers++;
            categoryStats.streak++;
            if (categoryStats.streak > categoryStats.maxStreak) {
                categoryStats.maxStreak = categoryStats.streak;
            }
        } else {
            categoryStats.streak = 0;
        }
        
        categoryStats.updateStats();
    }
    
    /**
     * Actualizează statisticile pentru o dificultate specifică
     */
    private void updateDifficultyStats(QuizStats stats, String difficultyName,
                                     boolean isCorrect, long timeSpent) {
        DifficultyStats difficultyStats = stats.difficultyStats.get(difficultyName);
        if (difficultyStats == null) {
            difficultyStats = new DifficultyStats(difficultyName);
            stats.difficultyStats.put(difficultyName, difficultyStats);
        }
        
        difficultyStats.questionsAnswered++;
        difficultyStats.totalTime += timeSpent;
        
        if (isCorrect) {
            difficultyStats.correctAnswers++;
        }
        
        difficultyStats.updateStats();
    }
    
    /**
     * Generează recomandări de învățare personalizate
     */
    private void generateLearningRecommendations(QuizStats stats) {
        List<LearningRecommendation> recommendations = new ArrayList<>();
        
        // Analizează categoriile slabe
        for (CategoryStats categoryStats : stats.categoryStats.values()) {
            if (categoryStats.isWeak && categoryStats.questionsAnswered >= 3) {
                recommendations.add(new LearningRecommendation(
                    "Îmbunătățește " + categoryStats.categoryName,
                    "Acuratețe: " + String.format("%.1f%%", categoryStats.accuracy * 100) + 
                    ". Încearcă să te concentrezi mai mult pe această categorie.",
                    categoryStats.categoryName,
                    "MEDIUM",
                    5
                ));
            }
        }
        
        // Analizează dificultățile
        for (DifficultyStats difficultyStats : stats.difficultyStats.values()) {
            if (difficultyStats.isComfortable) {
                String nextLevel = getNextDifficultyLevel(difficultyStats.difficultyName);
                if (nextLevel != null) {
                    recommendations.add(new LearningRecommendation(
                        "Încearcă " + nextLevel,
                        "Performanță excelentă la " + difficultyStats.difficultyName + 
                        ". Ești pregătit pentru următorul nivel!",
                        "ALL",
                        nextLevel,
                        4
                    ));
                }
            }
        }
        
        // Recomandări generale
        if (stats.overallAccuracy > 0.85f && stats.totalGamesPlayed >= 5) {
            recommendations.add(new LearningRecommendation(
                "Moduri de joc avansate",
                "Încearcă modurile Lightning sau Expert Challenge pentru o provocare mai mare!",
                "ALL",
                "HARD",
                3
            ));
        }
        
        // Sortează după prioritate
        recommendations.sort((a, b) -> Integer.compare(b.priority, a.priority));
        
        // Păstrează doar primele 5 recomandări
        if (recommendations.size() > 5) {
            recommendations = recommendations.subList(0, 5);
        }
        
        stats.recommendations = recommendations;
    }
    
    /**
     * Obține următorul nivel de dificultate
     */
    private String getNextDifficultyLevel(String currentLevel) {
        switch (currentLevel) {
            case "EASY": return "MEDIUM";
            case "MEDIUM": return "HARD";
            case "HARD": return "EXPERT";
            default: return null;
        }
    }
    
    /**
     * Începe o nouă sesiune de joc
     */
    public void startNewSession() {
        currentSessionStats = new QuizStats();
        Log.d(TAG, "New session started");
    }
    
    /**
     * Termină sesiunea curentă
     */
    public void endSession(int finalScore, GameModeManager.GameMode gameMode) {
        QuizStats stats = getCurrentStats();
        stats.totalGamesPlayed++;
        
        // Actualizează topicurile slabe și puternice
        updateWeakAndStrongTopics(stats);
        
        // Salvează statisticile
        saveStats(stats);
        
        Log.d(TAG, "Session ended - Final score: " + finalScore + 
               ", Game mode: " + gameMode.displayName +
               ", Total games: " + stats.totalGamesPlayed);
    }
    
    /**
     * Actualizează listele de topicuri slabe și puternice
     */
    private void updateWeakAndStrongTopics(QuizStats stats) {
        stats.weakTopics.clear();
        stats.strongTopics.clear();
        
        for (CategoryStats categoryStats : stats.categoryStats.values()) {
            if (categoryStats.isWeak) {
                stats.weakTopics.add(categoryStats.categoryName);
            } else if (categoryStats.isStrong) {
                stats.strongTopics.add(categoryStats.categoryName);
            }
        }
    }
    
    /**
     * Obține categoriile recomandate pentru următorul quiz
     */
    public List<String> getRecommendedQuestionCategories() {
        QuizStats stats = getCurrentStats();
        List<String> recommended = new ArrayList<>();
        
        // Prioritizează categoriile slabe
        for (CategoryStats categoryStats : stats.categoryStats.values()) {
            if (categoryStats.isWeak) {
                recommended.add(categoryStats.categoryName);
            }
        }
        
        // Dacă nu sunt categorii slabe, returnează categoriile cu cea mai mică acuratețe
        if (recommended.isEmpty()) {
            stats.categoryStats.values().stream()
                .filter(cs -> cs.questionsAnswered >= 3)
                .sorted((a, b) -> Float.compare(a.accuracy, b.accuracy))
                .limit(3)
                .forEach(cs -> recommended.add(cs.categoryName));
        }
        
        return recommended;
    }
    
    /**
     * Obține dificultatea recomandată
     */
    public EnhancedQuestionModel.Difficulty getRecommendedDifficulty() {
        QuizStats stats = getCurrentStats();
        
        if (stats.overallAccuracy >= 0.9f) {
            return EnhancedQuestionModel.Difficulty.EXPERT;
        } else if (stats.overallAccuracy >= 0.75f) {
            return EnhancedQuestionModel.Difficulty.HARD;
        } else if (stats.overallAccuracy >= 0.6f) {
            return EnhancedQuestionModel.Difficulty.MEDIUM;
        } else {
            return EnhancedQuestionModel.Difficulty.EASY;
        }
    }
    
    /**
     * Verifică realizările noi
     */
    public List<String> checkForNewAchievements() {
        QuizStats stats = getCurrentStats();
        List<String> achievements = new ArrayList<>();
        
        // Achievement pentru streak
        if (stats.longestStreak >= 10) {
            achievements.add("🔥 Streak Master - 10 răspunsuri consecutive corecte!");
        }
        
        // Achievement pentru acuratețe
        if (stats.overallAccuracy >= 0.95f && stats.totalQuestionsAnswered >= 50) {
            achievements.add("🎯 Perfectionist - 95% acuratețe cu 50+ întrebări!");
        }
        
        // Achievement pentru experiență
        if (stats.totalGamesPlayed >= 100) {
            achievements.add("🏆 Veteran - 100 de jocuri completate!");
        }
        
        // Achievement pentru viteză
        if (stats.averageTimePerQuestion <= 10000) { // 10 secunde
            achievements.add("⚡ Speed Demon - Timpul mediu sub 10 secunde!");
        }
        
        // Achievement pentru categorii
        long masteredCategories = stats.categoryStats.values().stream()
            .filter(cs -> cs.isStrong)
            .count();
        if (masteredCategories >= 5) {
            achievements.add("📚 Category Master - Stăpânești 5+ categorii!");
        }
        
        return achievements;
    }
    
    /**
     * Încarcă statisticile din SharedPreferences
     */
    private void loadStats() {
        try {
            String statsJson = prefs.getString(KEY_QUIZ_STATS, "");
            if (!statsJson.isEmpty()) {
                Type type = new TypeToken<QuizStats>(){}.getType();
                currentSessionStats = gson.fromJson(statsJson, type);
                if (currentSessionStats == null) {
                    currentSessionStats = new QuizStats();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading stats", e);
            currentSessionStats = new QuizStats();
        }
    }
    
    /**
     * Salvează statisticile în SharedPreferences
     */
    private void saveStats(QuizStats stats) {
        try {
            String statsJson = gson.toJson(stats);
            prefs.edit()
                .putString(KEY_QUIZ_STATS, statsJson)
                .apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving stats", e);
        }
    }
    
    /**
     * Obține statisticile curente
     */
    public QuizStats getCurrentStats() {
        if (currentSessionStats == null) {
            loadStats();
        }
        return currentSessionStats;
    }
    
    /**
     * Resetează toate statisticile
     */
    public void resetAllStats() {
        currentSessionStats = new QuizStats();
        prefs.edit().clear().apply();
        Log.d(TAG, "All stats reset");
    }
    
    /**
     * Exportă statisticile ca JSON
     */
    public String exportStatsAsJson() {
        return gson.toJson(getCurrentStats());
    }
} 