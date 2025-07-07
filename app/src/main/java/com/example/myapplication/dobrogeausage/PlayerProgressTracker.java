package com.example.myapplication.dobrogeausage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.myapplication.models.EnhancedQuestionModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tracker pentru progresul și performanța detaliată a jucătorului în quiz-ul Dobrogea
 * Tematică specifică: Marea Neagră, Delta Dunării, istorie antică
 */
public class PlayerProgressTracker {
    private static final String TAG = "DobrogeaProgressTracker";
    private static final String PREFS_NAME = "DobrogeaPlayerProgressTracking";
    private static final String KEY_QUIZ_STATS = "dobrogea_quiz_stats";
    private static final String KEY_CATEGORY_STATS = "dobrogea_category_stats";
    private static final String KEY_DIFFICULTY_STATS = "dobrogea_difficulty_stats";
    private static final String KEY_LEARNING_PATH = "dobrogea_learning_path";
    private static final String KEY_MARITIME_EXPERTISE = "maritime_expertise";
    
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
        
        // Statistici specifice Dobrogei
        public int maritimeQuestionsAnswered = 0;
        public int deltaQuestionsAnswered = 0;
        public int archaeologyQuestionsAnswered = 0;
        public float maritimeAccuracy = 0;
        public float deltaAccuracy = 0;
        public float archaeologyAccuracy = 0;
        public String favoriteGameMode = "CLASSIC";
        public int expertModeCompletions = 0;
        
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
        
        public void updateSpecializedStats() {
            // Actualizează statisticile pentru teme specifice Dobrogei
            if (maritimeQuestionsAnswered > 0) {
                // Calculat din categoriile relevante
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
        public String specialization = ""; // maritime, delta, archaeology
        
        public CategoryStats(String categoryName) {
            this.categoryName = categoryName;
            this.specialization = determineSpecialization(categoryName);
        }
        
        private String determineSpecialization(String category) {
            switch (category.toUpperCase()) {
                case "GEOGRAPHY":
                case "NATURE":
                    return "delta";
                case "HISTORY":
                case "ARCHITECTURE":
                    return "archaeology";
                case "CULTURE":
                case "GASTRONOMY":
                    return "maritime";
                default:
                    return "general";
            }
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
        public String maritimeRank = "Marinar"; // Specific pentru Dobrogea
        
        public DifficultyStats(String difficultyName) {
            this.difficultyName = difficultyName;
        }
        
        public void updateStats() {
            if (questionsAnswered > 0) {
                accuracy = (float) correctAnswers / questionsAnswered;
                averageTime = (float) totalTime / questionsAnswered;
                isComfortable = accuracy > 0.75f && questionsAnswered >= 10;
                updateMaritimeRank();
            }
        }
        
        private void updateMaritimeRank() {
            if (accuracy >= 0.95f) maritimeRank = "Amiral";
            else if (accuracy >= 0.85f) maritimeRank = "Căpitan";
            else if (accuracy >= 0.75f) maritimeRank = "Navigator";
            else if (accuracy >= 0.60f) maritimeRank = "Pescar";
            else maritimeRank = "Marinar";
        }
    }
    
    public static class LearningRecommendation {
        public String title;
        public String description;
        public String category;
        public String difficulty;
        public int priority; // 1-5, 5 being highest
        public Date created;
        public String theme; // maritime, delta, archaeology
        
        public LearningRecommendation(String title, String description, String category, 
                                    String difficulty, int priority, String theme) {
            this.title = title;
            this.description = description;
            this.category = category;
            this.difficulty = difficulty;
            this.priority = priority;
            this.theme = theme;
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
        
        // Actualizează statisticile specifice Dobrogei
        updateDobrogeaSpecificStats(stats, category, isCorrect);
        
        // Înregistrează întrebarea în istoric
        stats.questionHistory.put(questionId, 
            stats.questionHistory.getOrDefault(questionId, 0) + 1);
        
        // Actualizează acuratețea și timpul mediu
        stats.updateAccuracy();
        stats.updateAverageTime();
        stats.updateSpecializedStats();
        stats.lastPlayed = new Date();
        
        // Generează recomandări actualizate
        generateMaritimeLearningRecommendations(stats);
        
        // Salvează statisticile
        saveStats(stats);
        
        Log.d(TAG, "Dobrogea answer tracked - Correct: " + isCorrect + 
               ", Category: " + category.name() + 
               ", Difficulty: " + difficulty.name() +
               ", Overall accuracy: " + stats.overallAccuracy);
    }
    
    /**
     * Actualizează statisticile specifice Dobrogei
     */
    private void updateDobrogeaSpecificStats(QuizStats stats, EnhancedQuestionModel.Category category, boolean isCorrect) {
        switch (category) {
            case GEOGRAPHY:
            case NATURE:
                stats.deltaQuestionsAnswered++;
                if (isCorrect) {
                    stats.deltaAccuracy = (stats.deltaAccuracy * (stats.deltaQuestionsAnswered - 1) + 1) / stats.deltaQuestionsAnswered;
                } else {
                    stats.deltaAccuracy = (stats.deltaAccuracy * (stats.deltaQuestionsAnswered - 1)) / stats.deltaQuestionsAnswered;
                }
                break;
            case HISTORY:
            case ARCHITECTURE:
                stats.archaeologyQuestionsAnswered++;
                if (isCorrect) {
                    stats.archaeologyAccuracy = (stats.archaeologyAccuracy * (stats.archaeologyQuestionsAnswered - 1) + 1) / stats.archaeologyQuestionsAnswered;
                } else {
                    stats.archaeologyAccuracy = (stats.archaeologyAccuracy * (stats.archaeologyQuestionsAnswered - 1)) / stats.archaeologyQuestionsAnswered;
                }
                break;
            case CULTURE:
            case GASTRONOMY:
                stats.maritimeQuestionsAnswered++;
                if (isCorrect) {
                    stats.maritimeAccuracy = (stats.maritimeAccuracy * (stats.maritimeQuestionsAnswered - 1) + 1) / stats.maritimeQuestionsAnswered;
                } else {
                    stats.maritimeAccuracy = (stats.maritimeAccuracy * (stats.maritimeQuestionsAnswered - 1)) / stats.maritimeQuestionsAnswered;
                }
                break;
        }
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
     * Generează recomandări de învățare specifice pentru Dobrogea
     */
    private void generateMaritimeLearningRecommendations(QuizStats stats) {
        stats.recommendations.clear();
        
        // Recomandări bazate pe performanța pe teme specifice
        if (stats.deltaAccuracy < 0.7f && stats.deltaQuestionsAnswered >= 3) {
            stats.recommendations.add(new LearningRecommendation(
                "🦢 Explorează Delta Dunării",
                "Îmbunătățește cunoștințele despre biodiversitatea și geografia deltei. Focus pe păsări, canale și ecosistem.",
                "NATURE",
                "MEDIUM",
                5,
                "delta"
            ));
        }
        
        if (stats.maritimeAccuracy < 0.7f && stats.maritimeQuestionsAnswered >= 3) {
            stats.recommendations.add(new LearningRecommendation(
                "⚓ Navigație și Tradițiile Maritime",
                "Studiază tradițiile pescarilor, porturile Mării Negre și navigația. Focus pe Constanța și Mangalia.",
                "CULTURE",
                "MEDIUM",
                4,
                "maritime"
            ));
        }
        
        if (stats.archaeologyAccuracy < 0.7f && stats.archaeologyQuestionsAnswered >= 3) {
            stats.recommendations.add(new LearningRecommendation(
                "🏛️ Civilizațiile Antice",
                "Aprofundează cunoștințele despre Tomis, Histria, Callatis și alte situri arheologice dobrogene.",
                "HISTORY",
                "HARD",
                4,
                "archaeology"
            ));
        }
        
        // Recomandări bazate pe acuratețe generală
        if (stats.overallAccuracy > 0.85f) {
            stats.recommendations.add(new LearningRecommendation(
                "👨‍✈️ Provocarea Căpitanului",
                "Performanță excelentă! Încearcă modurile expert pentru a-ți testa cunoștințele avansate.",
                "ALL",
                "EXPERT",
                3,
                "general"
            ));
        }
        
        // Recomandări pentru moduri de joc specifice
        if (stats.totalGamesPlayed >= 5) {
            String weakestTheme = getWeakestTheme(stats);
            switch (weakestTheme) {
                case "delta":
                    stats.recommendations.add(new LearningRecommendation(
                        "🦢 Modul Delta Explorer",
                        "Încearcă modul Delta Explorer pentru a-ți îmbunătăți cunoștințele despre biodiversitate.",
                        "NATURE",
                        "MEDIUM",
                        3,
                        "delta"
                    ));
                    break;
                case "maritime":
                    stats.recommendations.add(new LearningRecommendation(
                        "⚓ Modul Aventura Marină",
                        "Explorează modul Aventura Marină pentru a învăța despre navigație și porturi.",
                        "CULTURE",
                        "MEDIUM",
                        3,
                        "maritime"
                    ));
                    break;
                case "archaeology":
                    stats.recommendations.add(new LearningRecommendation(
                        "🏛️ Modul Săpături Arheologice",
                        "Descoperă tezaurele antice în modul Săpături Arheologice.",
                        "HISTORY",
                        "HARD",
                        3,
                        "archaeology"
                    ));
                    break;
            }
        }
        
        // Sortează recomandările după prioritate
        stats.recommendations.sort((r1, r2) -> Integer.compare(r2.priority, r1.priority));
        
        // Păstrează doar top 5 recomandări
        if (stats.recommendations.size() > 5) {
            stats.recommendations = stats.recommendations.subList(0, 5);
        }
    }
    
    /**
     * Determină tema cea mai slabă a utilizatorului
     */
    private String getWeakestTheme(QuizStats stats) {
        float deltaScore = stats.deltaQuestionsAnswered > 0 ? stats.deltaAccuracy : 1.0f;
        float maritimeScore = stats.maritimeQuestionsAnswered > 0 ? stats.maritimeAccuracy : 1.0f;
        float archaeologyScore = stats.archaeologyQuestionsAnswered > 0 ? stats.archaeologyAccuracy : 1.0f;
        
        if (deltaScore <= maritimeScore && deltaScore <= archaeologyScore) {
            return "delta";
        } else if (maritimeScore <= archaeologyScore) {
            return "maritime";
        } else {
            return "archaeology";
        }
    }
    
    /**
     * Începe o nouă sesiune de joc
     */
    public void startNewSession() {
        currentSessionStats = new QuizStats();
        Log.d(TAG, "New Dobrogea quiz session started");
    }
    
    /**
     * Termină sesiunea curentă și salvează progresul
     */
    public void endSession(int finalScore, GameModeManager.GameMode gameMode) {
        QuizStats stats = getCurrentStats();
        stats.totalGamesPlayed++;
        
        // Actualizează modul de joc favorit
        updateFavoriteGameMode(stats, gameMode);
        
        // Verifică achievement-uri specifice Dobrogei
        checkDobrogeaAchievements(stats, finalScore, gameMode);
        
        saveStats(stats);
        Log.d(TAG, "Dobrogea quiz session ended with score: " + finalScore + 
               ", mode: " + gameMode.displayName);
    }
    
    /**
     * Actualizează modul de joc favorit
     */
    private void updateFavoriteGameMode(QuizStats stats, GameModeManager.GameMode gameMode) {
        // Logică simplă pentru a determina modul favorit
        // În implementare completă, ar trebui să țină cont de frecvența de utilizare
        stats.favoriteGameMode = gameMode.name();
        
        if (gameMode == GameModeManager.GameMode.CAPTAIN_EXPERT) {
            stats.expertModeCompletions++;
        }
    }
    
    /**
     * Verifică achievement-uri specifice Dobrogei
     */
    private void checkDobrogeaAchievements(QuizStats stats, int finalScore, GameModeManager.GameMode gameMode) {
        // Achievement-uri specifice pentru Dobrogea
        // În implementare completă, ar integra cu AchievementManager
        
        if (stats.deltaAccuracy >= 0.9f && stats.deltaQuestionsAnswered >= 10) {
            Log.d(TAG, "Achievement unlocked: Delta Expert");
        }
        
        if (stats.maritimeAccuracy >= 0.9f && stats.maritimeQuestionsAnswered >= 10) {
            Log.d(TAG, "Achievement unlocked: Maritime Master");
        }
        
        if (stats.archaeologyAccuracy >= 0.9f && stats.archaeologyQuestionsAnswered >= 10) {
            Log.d(TAG, "Achievement unlocked: Archaeology Scholar");
        }
        
        if (gameMode == GameModeManager.GameMode.STORM_SURVIVAL && finalScore > 500) {
            Log.d(TAG, "Achievement unlocked: Storm Survivor");
        }
    }
    
    /**
     * Obține categoriile recomandate pentru următorul quiz
     */
    public List<String> getRecommendedQuestionCategories() {
        QuizStats stats = getCurrentStats();
        List<String> recommended = new ArrayList<>();
        
        // Prioritizează categoriile cu performanțe slabe
        for (CategoryStats categoryStats : stats.categoryStats.values()) {
            if (categoryStats.isWeak && categoryStats.questionsAnswered >= 3) {
                recommended.add(categoryStats.categoryName);
            }
        }
        
        // Dacă nu există categorii slabe, recomandă categoriile cu puține întrebări
        if (recommended.isEmpty()) {
            String[] allCategories = {"HISTORY", "GEOGRAPHY", "CULTURE", "NATURE", "ARCHITECTURE", "GASTRONOMY"};
            for (String category : allCategories) {
                CategoryStats stats_cat = stats.categoryStats.get(category);
                if (stats_cat == null || stats_cat.questionsAnswered < 5) {
                    recommended.add(category);
                }
            }
        }
        
        return recommended.isEmpty() ? Arrays.asList("GEOGRAPHY", "NATURE") : recommended;
    }
    
    /**
     * Obține dificultatea recomandată
     */
    public EnhancedQuestionModel.Difficulty getRecommendedDifficulty() {
        QuizStats stats = getCurrentStats();
        
        if (stats.overallAccuracy >= 0.9f) {
            return EnhancedQuestionModel.Difficulty.EXPERT;
        } else if (stats.overallAccuracy >= 0.8f) {
            return EnhancedQuestionModel.Difficulty.HARD;
        } else if (stats.overallAccuracy >= 0.7f) {
            return EnhancedQuestionModel.Difficulty.MEDIUM;
        } else {
            return EnhancedQuestionModel.Difficulty.EASY;
        }
    }
    
    /**
     * Verifică pentru achievement-uri noi
     */
    public List<String> checkForNewAchievements() {
        List<String> newAchievements = new ArrayList<>();
        QuizStats stats = getCurrentStats();
        
        // Maritime achievements
        if (stats.maritimeAccuracy >= 0.95f && stats.maritimeQuestionsAnswered >= 15) {
            newAchievements.add("🏆 Amiral al Mării Negre - Maestria în cunoștințe maritime!");
        }
        
        // Delta achievements
        if (stats.deltaAccuracy >= 0.95f && stats.deltaQuestionsAnswered >= 15) {
            newAchievements.add("🦢 Gardianul Deltei - Expert în biodiversitatea Deltei Dunării!");
        }
        
        // Archaeology achievements
        if (stats.archaeologyAccuracy >= 0.95f && stats.archaeologyQuestionsAnswered >= 15) {
            newAchievements.add("🏛️ Arheolog Dobrogean - Cunoștințe excepționale despre civilizațiile antice!");
        }
        
        // Streak achievements
        if (stats.longestStreak >= 20) {
            newAchievements.add("🔥 Navigatorul Perfect - 20 de răspunsuri consecutive corecte!");
        }
        
        // Speed achievements
        if (stats.averageTimePerQuestion < 15000 && stats.overallAccuracy > 0.8f) {
            newAchievements.add("⚡ Fulgerul Dobrogei - Viteză și acuratețe excepțională!");
        }
        
        return newAchievements;
    }
    
    // Metode pentru încărcare și salvare
    private void loadStats() {
        String statsJson = prefs.getString(KEY_QUIZ_STATS, "");
        if (!statsJson.isEmpty()) {
            try {
                Type type = new TypeToken<QuizStats>(){}.getType();
                currentSessionStats = gson.fromJson(statsJson, type);
                if (currentSessionStats == null) {
                    currentSessionStats = new QuizStats();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading Dobrogea stats", e);
                currentSessionStats = new QuizStats();
            }
        }
    }
    
    private void saveStats(QuizStats stats) {
        try {
            String statsJson = gson.toJson(stats);
            prefs.edit().putString(KEY_QUIZ_STATS, statsJson).apply();
            Log.d(TAG, "Dobrogea stats saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving Dobrogea stats", e);
        }
    }
    
    public QuizStats getCurrentStats() {
        if (currentSessionStats == null) {
            loadStats();
        }
        return currentSessionStats;
    }
    
    /**
     * Obține statistici specializate pentru Dobrogea
     */
    public String getDobrogeaSpecializedStats() {
        QuizStats stats = getCurrentStats();
        StringBuilder specialStats = new StringBuilder();
        
        specialStats.append("🌊 Statistici Specializate Dobrogea:\n\n");
        
        specialStats.append("🦢 Delta Dunării:\n");
        specialStats.append("  Întrebări: ").append(stats.deltaQuestionsAnswered).append("\n");
        specialStats.append("  Acuratețe: ").append(String.format("%.1f%%", stats.deltaAccuracy * 100)).append("\n\n");
        
        specialStats.append("⚓ Tema Marină:\n");
        specialStats.append("  Întrebări: ").append(stats.maritimeQuestionsAnswered).append("\n");
        specialStats.append("  Acuratețe: ").append(String.format("%.1f%%", stats.maritimeAccuracy * 100)).append("\n\n");
        
        specialStats.append("🏛️ Arheologie:\n");
        specialStats.append("  Întrebări: ").append(stats.archaeologyQuestionsAnswered).append("\n");
        specialStats.append("  Acuratețe: ").append(String.format("%.1f%%", stats.archaeologyAccuracy * 100)).append("\n\n");
        
        specialStats.append("🎮 Mod favorit: ").append(stats.favoriteGameMode).append("\n");
        specialStats.append("🏆 Expert completions: ").append(stats.expertModeCompletions);
        
        return specialStats.toString();
    }
    
    /**
     * Resetează toate statisticile
     */
    public void resetAllStats() {
        prefs.edit().clear().apply();
        currentSessionStats = new QuizStats();
        Log.d(TAG, "All Dobrogea stats reset");
    }
    
    /**
     * Exportă statisticile ca JSON
     */
    public String exportStatsAsJson() {
        return gson.toJson(getCurrentStats());
    }
} 