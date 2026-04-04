package com.example.myapplication.transilvaniausage;

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
 * Tracker pentru progresul și performanța detaliată a jucătorului
 */
public class PlayerProgressTracker {
    private static final String TAG = "PlayerProgressTracker";
    private static final String PREFS_NAME = "PlayerProgressTracking";
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
     * Actualizează statisticile pentru un nivel de dificultate
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
     * Generează recomandări de învățare bazate pe performanță
     */
    private void generateLearningRecommendations(QuizStats stats) {
        stats.recommendations.clear();
        stats.weakTopics.clear();
        stats.strongTopics.clear();
        
        // Analizează categoriile slabe și puternice
        for (CategoryStats categoryStats : stats.categoryStats.values()) {
            if (categoryStats.isWeak && categoryStats.questionsAnswered >= 3) {
                stats.weakTopics.add(categoryStats.categoryName);
                
                // Recomandare pentru îmbunătățirea categoriei slabe
                stats.recommendations.add(new LearningRecommendation(
                    "Îmbunătățește " + categoryStats.categoryName,
                    "Acuratețea ta la " + categoryStats.categoryName + 
                    " este de " + String.format("%.1f%%", categoryStats.accuracy * 100) + 
                    ". Încearcă să exersezi mai mult în această categorie.",
                    categoryStats.categoryName,
                    "EASY",
                    5 // prioritate mare pentru categoriile slabe
                ));
            } else if (categoryStats.isStrong) {
                stats.strongTopics.add(categoryStats.categoryName);
            }
        }
        
        // Recomandări bazate pe dificultate
        for (DifficultyStats difficultyStats : stats.difficultyStats.values()) {
            if (difficultyStats.isComfortable && 
                !difficultyStats.difficultyName.equals("EXPERT")) {
                
                // Recomandă trecerea la următorul nivel
                String nextLevel = getNextDifficultyLevel(difficultyStats.difficultyName);
                if (nextLevel != null) {
                    stats.recommendations.add(new LearningRecommendation(
                        "Provocare: " + nextLevel,
                        "Ești confortabil cu " + difficultyStats.difficultyName + 
                        ". Încearcă întrebări de nivel " + nextLevel + "!",
                        "",
                        nextLevel,
                        3
                    ));
                }
            }
        }
        
        // Recomandare pentru streak-uri
        if (stats.longestStreak >= 10) {
            stats.recommendations.add(new LearningRecommendation(
                "Maestru al Consistenței",
                "Cel mai lung streak: " + stats.longestStreak + 
                ". Încearcă modul Survival pentru o provocare extremă!",
                "",
                "",
                2
            ));
        }
        
        // Recomandare pentru viteză
        if (stats.averageTimePerQuestion < 15000) { // sub 15 secunde
            stats.recommendations.add(new LearningRecommendation(
                "Viteza Fulgerului",
                "Răspunsuri rapide! Încearcă modul Lightning sau Blitz.",
                "",
                "",
                2
            ));
        }
        
        // Sortează recomandările după prioritate
        stats.recommendations.sort((a, b) -> Integer.compare(b.priority, a.priority));
        
        // Păstrează doar top 5 recomandări
        if (stats.recommendations.size() > 5) {
            stats.recommendations = stats.recommendations.subList(0, 5);
        }
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
     * Marchează începutul unei noi sesiuni de joc
     */
    public void startNewSession() {
        currentSessionStats = new QuizStats();
    }
    
    /**
     * Marchează sfârșitul unei sesiuni și actualizează statisticile globale
     */
    public void endSession(int finalScore, GameModeManager.GameMode gameMode) {
        QuizStats globalStats = getCurrentStats();
        globalStats.totalGamesPlayed++;
        
        // Adaugă statisticile sesiunii la cele globale
        globalStats.totalQuestionsAnswered += currentSessionStats.totalQuestionsAnswered;
        globalStats.totalCorrectAnswers += currentSessionStats.totalCorrectAnswers;
        globalStats.totalTimeSpent += currentSessionStats.totalTimeSpent;
        
        if (currentSessionStats.longestStreak > globalStats.longestStreak) {
            globalStats.longestStreak = currentSessionStats.longestStreak;
        }
        
        // Actualizează statisticile
        globalStats.updateAccuracy();
        globalStats.updateAverageTime();
        globalStats.lastPlayed = new Date();
        
        // Salvează
        saveStats(globalStats);
        
        Log.d(TAG, "Session ended - Score: " + finalScore + 
               ", Mode: " + gameMode.displayName + 
               ", Questions: " + currentSessionStats.totalQuestionsAnswered);
    }
    
    /**
     * Obține întrebări recomandate pentru utilizator
     */
    public List<String> getRecommendedQuestionCategories() {
        QuizStats stats = getCurrentStats();
        List<String> recommended = new ArrayList<>();
        
        // Prioritizează categoriile slabe
        recommended.addAll(stats.weakTopics);
        
        // Adaugă categorii cu care utilizatorul nu a interacționat recent
        Set<String> allCategories = Arrays.stream(EnhancedQuestionModel.Category.values())
            .map(Enum::name)
            .collect(Collectors.toSet());
        
        for (String category : allCategories) {
            if (!stats.categoryStats.containsKey(category) || 
                stats.categoryStats.get(category).questionsAnswered < 3) {
                if (!recommended.contains(category)) {
                    recommended.add(category);
                }
            }
        }
        
        return recommended;
    }
    
    /**
     * Obține nivelul de dificultate recomandat pentru utilizator
     */
    public EnhancedQuestionModel.Difficulty getRecommendedDifficulty() {
        QuizStats stats = getCurrentStats();
        
        if (stats.overallAccuracy >= 0.9f && stats.totalQuestionsAnswered >= 20) {
            return EnhancedQuestionModel.Difficulty.EXPERT;
        } else if (stats.overallAccuracy >= 0.8f && stats.totalQuestionsAnswered >= 15) {
            return EnhancedQuestionModel.Difficulty.HARD;
        } else if (stats.overallAccuracy >= 0.6f && stats.totalQuestionsAnswered >= 10) {
            return EnhancedQuestionModel.Difficulty.MEDIUM;
        } else {
            return EnhancedQuestionModel.Difficulty.EASY;
        }
    }
    
    /**
     * Verifică dacă utilizatorul ar trebui să primească un achievement
     */
    public List<String> checkForNewAchievements() {
        QuizStats stats = getCurrentStats();
        List<String> newAchievements = new ArrayList<>();
        
        // Achievement pentru acuratețe
        if (stats.overallAccuracy >= 0.95f && stats.totalQuestionsAnswered >= 50) {
            newAchievements.add("perfectionist");
        }
        
        // Achievement pentru streak
        if (stats.longestStreak >= 20) {
            newAchievements.add("streak_master");
        }
        
        // Achievement pentru viteză
        if (stats.averageTimePerQuestion < 10000 && stats.totalQuestionsAnswered >= 30) {
            newAchievements.add("speed_demon");
        }
        
        // Achievement pentru categorii
        long masteredCategories = stats.categoryStats.values().stream()
            .filter(cat -> cat.isStrong)
            .count();
        
        if (masteredCategories >= 5) {
            newAchievements.add("category_master");
        }
        
        return newAchievements;
    }
    
    /**
     * Încarcă statisticile salvate
     */
    private void loadStats() {
        String statsJson = prefs.getString(KEY_QUIZ_STATS, "");
        if (!statsJson.isEmpty()) {
            try {
                Type type = new TypeToken<QuizStats>(){}.getType();
                QuizStats loadedStats = gson.fromJson(statsJson, type);
                if (loadedStats != null) {
                    // Nu suprascriem currentSessionStats aici
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading stats", e);
            }
        }
    }
    
    /**
     * Salvează statisticile
     */
    private void saveStats(QuizStats stats) {
        try {
            String statsJson = gson.toJson(stats);
            prefs.edit().putString(KEY_QUIZ_STATS, statsJson).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving stats", e);
        }
    }
    
    /**
     * Obține statisticile curente (combină globale + sesiune)
     */
    public QuizStats getCurrentStats() {
        String statsJson = prefs.getString(KEY_QUIZ_STATS, "");
        if (statsJson.isEmpty()) {
            return new QuizStats();
        }
        
        try {
            Type type = new TypeToken<QuizStats>(){}.getType();
            QuizStats stats = gson.fromJson(statsJson, type);
            return stats != null ? stats : new QuizStats();
        } catch (Exception e) {
            Log.e(TAG, "Error loading current stats", e);
            return new QuizStats();
        }
    }
    
    /**
     * Resetează toate statisticile
     */
    public void resetAllStats() {
        prefs.edit().clear().apply();
        currentSessionStats = new QuizStats();
        Log.d(TAG, "All stats reset");
    }
    
    /**
     * Exportă statisticile pentru analiză
     */
    public String exportStatsAsJson() {
        QuizStats stats = getCurrentStats();
        return gson.toJson(stats);
    }
} 