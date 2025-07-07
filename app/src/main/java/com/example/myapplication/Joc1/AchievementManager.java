package com.example.myapplication.Joc1;

import android.content.Context;
import com.example.myapplication.utils.SyncManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Enhanced Manages the player's achievements and progress across all games
 * Now includes comprehensive Transilvania quiz achievements
 */
public class AchievementManager {
    private static AchievementManager instance;
    private Context context;
    private Map<String, Achievement> achievements = new HashMap<>();
    private SyncManager syncManager;
    
    // Achievement Categories
    public enum AchievementCategory {
        EXPLORATION, QUIZ, QUEST, CITY, TRANSILVANIA, DIFFICULTY, GAME_MODE, LEARNING, SOCIAL, SPECIAL
    }
    
    public static class Achievement {
        private String id;
        private String title;
        private String description;
        private int iconResourceId;
        private boolean unlocked;
        private int pointsReward;
        private int requiredProgress;
        private int currentProgress;
        private AchievementCategory category;
        private String region; // For regional achievements
        
        public Achievement(String id, String title, String description, int iconResourceId, 
                         int pointsReward, int requiredProgress, AchievementCategory category) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.iconResourceId = iconResourceId;
            this.pointsReward = pointsReward;
            this.requiredProgress = requiredProgress;
            this.currentProgress = 0;
            this.unlocked = false;
            this.category = category;
        }
        
        public Achievement(String id, String title, String description, int iconResourceId, 
                         int pointsReward, int requiredProgress, AchievementCategory category, String region) {
            this(id, title, description, iconResourceId, pointsReward, requiredProgress, category);
            this.region = region;
        }
        
        public String getId() {
            return id;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getIconResourceId() {
            return iconResourceId;
        }
        
        public boolean isUnlocked() {
            return unlocked;
        }
        
        public void setUnlocked(boolean unlocked) {
            this.unlocked = unlocked;
        }
        
        public int getPointsReward() {
            return pointsReward;
        }
        
        public int getRequiredProgress() {
            return requiredProgress;
        }
        
        public int getCurrentProgress() {
            return currentProgress;
        }
        
        public void setCurrentProgress(int progress) {
            this.currentProgress = Math.min(progress, requiredProgress);
            if (this.currentProgress >= requiredProgress) {
                this.unlocked = true;
            }
        }
        
        public float getProgressPercentage() {
            return (float) currentProgress / requiredProgress * 100;
        }
        
        public AchievementCategory getCategory() {
            return category;
        }
        
        public String getRegion() {
            return region;
        }
    }
    
    public interface AchievementUnlockedListener {
        void onAchievementUnlocked(Achievement achievement);
    }
    
    private AchievementUnlockedListener listener;
    
    private AchievementManager(Context context) {
        this.context = context.getApplicationContext();
        this.syncManager = SyncManager.getInstance(context);
        initializeAchievements();
    }
    
    public static synchronized AchievementManager getInstance(Context context) {
        if (instance == null) {
            instance = new AchievementManager(context);
        }
        return instance;
    }
    
    private void initializeAchievements() {
        // Basic exploration achievements
        addAchievement(new Achievement(
            "explorer_novice",
            "Novice Explorer",
            "Visit 3 different locations in Romania",
            android.R.drawable.ic_menu_compass,
            50,
            3,
            AchievementCategory.EXPLORATION
        ));
        
        addAchievement(new Achievement(
            "explorer_advanced",
            "Advanced Explorer",
            "Visit 10 different locations in Romania",
            android.R.drawable.ic_menu_compass,
            150,
            10,
            AchievementCategory.EXPLORATION
        ));
        
        addAchievement(new Achievement(
            "explorer_master",
            "Master Explorer",
            "Visit all locations in Romania",
            android.R.drawable.ic_menu_compass,
            500,
            18,
            AchievementCategory.EXPLORATION
        ));
        
        // Quest achievements
        addAchievement(new Achievement(
            "quest_beginner",
            "Quest Beginner",
            "Complete 3 quests",
            android.R.drawable.ic_menu_help,
            100,
            3,
            AchievementCategory.QUEST
        ));
        
        addAchievement(new Achievement(
            "quest_expert",
            "Quest Expert",
            "Complete 10 quests",
            android.R.drawable.ic_menu_help,
            300,
            10,
            AchievementCategory.QUEST
        ));
        
        // Basic quiz achievements
        addAchievement(new Achievement(
            "quiz_novice",
            "Quiz Novice",
            "Answer 10 quiz questions correctly",
            android.R.drawable.ic_menu_help,
            100,
            10,
            AchievementCategory.QUIZ
        ));
        
        addAchievement(new Achievement(
            "quiz_master",
            "Quiz Master",
            "Answer 50 quiz questions correctly",
            android.R.drawable.ic_menu_help,
            500,
            50,
            AchievementCategory.QUIZ
        ));
        
        // === TRANSILVANIA QUIZ SPECIFIC ACHIEVEMENTS ===
        
        // Quiz Completion Achievements
        addAchievement(new Achievement(
            "transilvania_first_quiz",
            "Transilvania Novice",
            "Complete your first Transilvania quiz",
            android.R.drawable.ic_menu_gallery,
            50,
            1,
            AchievementCategory.TRANSILVANIA,
            "Transilvania"
        ));
        
        addAchievement(new Achievement(
            "transilvania_quiz_veteran",
            "Transilvania Veteran",
            "Complete 10 Transilvania quizzes",
            android.R.drawable.ic_menu_gallery,
            200,
            10,
            AchievementCategory.TRANSILVANIA,
            "Transilvania"
        ));
        
        addAchievement(new Achievement(
            "transilvania_quiz_master",
            "Transilvania Master",
            "Complete 25 Transilvania quizzes",
            android.R.drawable.ic_menu_gallery,
            500,
            25,
            AchievementCategory.TRANSILVANIA,
            "Transilvania"
        ));
        
        // Perfect Score Achievements
        addAchievement(new Achievement(
            "transilvania_perfect_score",
            "Transilvanian Scholar",
            "Get a perfect score in a Transilvania quiz",
            android.R.drawable.ic_dialog_info,
            100,
            1,
            AchievementCategory.TRANSILVANIA,
            "Transilvania"
        ));
        
        addAchievement(new Achievement(
            "transilvania_perfect_streak",
            "Perfect Perfection",
            "Get 3 perfect scores in a row",
            android.R.drawable.ic_dialog_info,
            300,
            3,
            AchievementCategory.TRANSILVANIA,
            "Transilvania"
        ));
        
        // Category Master Achievements
        String[] categories = {"History", "Geography", "Culture", "Architecture", "Gastronomy", "Legends", "Personalities", "Nature"};
        for (String category : categories) {
            addAchievement(new Achievement(
                "transilvania_" + category.toLowerCase() + "_master",
                category + " Expert",
                "Answer 20 " + category + " questions correctly",
                                 android.R.drawable.ic_dialog_info,
                150,
                20,
                AchievementCategory.LEARNING,
                "Transilvania"
            ));
        }
        
        // === OLTENIA QUIZ SPECIFIC ACHIEVEMENTS ===
        
        // Quiz Completion Achievements
        addAchievement(new Achievement(
            "oltenia_first_quiz",
            "Oltenia Novice",
            "Complete your first Oltenia quiz",
            android.R.drawable.ic_menu_gallery,
            50,
            1,
            AchievementCategory.QUIZ,
            "Oltenia"
        ));
        
        addAchievement(new Achievement(
            "oltenia_quiz_veteran",
            "Oltenia Veteran",
            "Complete 10 Oltenia quizzes",
            android.R.drawable.ic_menu_gallery,
            200,
            10,
            AchievementCategory.QUIZ,
            "Oltenia"
        ));
        
        addAchievement(new Achievement(
            "oltenia_quiz_master",
            "Oltenia Master",
            "Complete 25 Oltenia quizzes",
            android.R.drawable.ic_menu_gallery,
            500,
            25,
            AchievementCategory.QUIZ,
            "Oltenia"
        ));
        
        // Perfect Score Achievements
        addAchievement(new Achievement(
            "oltenia_perfect_score",
            "Oltenia Scholar",
            "Get a perfect score in an Oltenia quiz",
            android.R.drawable.ic_dialog_info,
            100,
            1,
            AchievementCategory.QUIZ,
            "Oltenia"
        ));
        
        addAchievement(new Achievement(
            "oltenia_perfect_streak",
            "Oltenia Perfectionist",
            "Get 3 perfect scores in a row",
            android.R.drawable.ic_dialog_info,
            300,
            3,
            AchievementCategory.QUIZ,
            "Oltenia"
        ));
        
        // Category Master Achievements for Oltenia
        for (String category : categories) {
            addAchievement(new Achievement(
                "oltenia_" + category.toLowerCase() + "_master",
                "Oltenia " + category + " Expert",
                "Answer 20 " + category + " questions correctly in Oltenia",
                android.R.drawable.ic_dialog_info,
                150,
                20,
                AchievementCategory.LEARNING,
                "Oltenia"
            ));
        }
        
        // Difficulty Level Achievements
        addAchievement(new Achievement(
            "oltenia_intermediate_unlock",
            "Oltenia Rising Scholar",
            "Unlock Intermediate difficulty in Oltenia quiz",
            android.R.drawable.ic_menu_preferences,
            75,
            1,
            AchievementCategory.DIFFICULTY,
            "Oltenia"
        ));
        
        addAchievement(new Achievement(
            "oltenia_advanced_unlock",
            "Oltenia Advanced Mind",
            "Unlock Advanced difficulty in Oltenia quiz",
            android.R.drawable.ic_menu_preferences,
            150,
            1,
            AchievementCategory.DIFFICULTY,
            "Oltenia"
        ));
        
        addAchievement(new Achievement(
            "oltenia_expert_unlock",
            "Oltenia Expert Knowledge",
            "Unlock Expert difficulty in Oltenia quiz",
            android.R.drawable.ic_menu_preferences,
            300,
            1,
            AchievementCategory.DIFFICULTY,
            "Oltenia"
        ));
        
        addAchievement(new Achievement(
            "oltenia_master_unlock",
            "Master of Oltenia",
            "Unlock Master difficulty in Oltenia quiz",
            android.R.drawable.ic_menu_preferences,
            500,
            1,
            AchievementCategory.DIFFICULTY,
            "Oltenia"
        ));
        
        // Game Mode Achievements
        addAchievement(new Achievement(
            "oltenia_lightning_champion",
            "Oltenia Lightning Champion",
            "Complete 5 Lightning mode quizzes in Oltenia",
            android.R.drawable.ic_media_next,
            200,
            5,
            AchievementCategory.GAME_MODE,
            "Oltenia"
        ));
        
        addAchievement(new Achievement(
            "oltenia_marathon_runner",
            "Oltenia Marathon Runner",
            "Complete a Marathon mode quiz in Oltenia",
            android.R.drawable.ic_media_play,
            300,
            1,
            AchievementCategory.GAME_MODE,
            "Oltenia"
        ));
        
        addAchievement(new Achievement(
            "oltenia_survival_expert",
            "Oltenia Survival Expert",
            "Survive 15 questions in Oltenia Survival mode",
            android.R.drawable.ic_menu_compass,
            250,
            15,
            AchievementCategory.GAME_MODE,
            "Oltenia"
        ));
        
        addAchievement(new Achievement(
            "oltenia_blitz_master",
            "Oltenia Blitz Master",
            "Complete 10 Blitz mode quizzes in Oltenia",
            android.R.drawable.ic_media_next,
            400,
            10,
            AchievementCategory.GAME_MODE,
            "Oltenia"
        ));
        
        // Speed Achievements
        addAchievement(new Achievement(
            "oltenia_speed_demon",
            "Oltenia Speed Demon",
            "Answer 10 Oltenia questions in under 5 seconds each",
            android.R.drawable.ic_media_next,
            200,
            10,
            AchievementCategory.SPECIAL,
            "Oltenia"
        ));
        
        addAchievement(new Achievement(
            "oltenia_quick_thinker",
            "Oltenia Quick Thinker",
            "Answer 50 Oltenia questions in under 10 seconds each",
            android.R.drawable.ic_media_next,
            300,
            50,
            AchievementCategory.SPECIAL,
            "Oltenia"
        ));
        
        // Streak Achievements
        addAchievement(new Achievement(
            "oltenia_hot_streak",
            "Oltenia Hot Streak",
            "Get a 5-question correct streak in Oltenia",
            android.R.drawable.ic_menu_send,
            100,
            5,
            AchievementCategory.SPECIAL,
            "Oltenia"
        ));
        
        addAchievement(new Achievement(
            "oltenia_unstoppable",
            "Oltenia Unstoppable",
            "Get a 10-question correct streak in Oltenia",
            android.R.drawable.ic_menu_send,
            200,
            10,
            AchievementCategory.SPECIAL,
            "Oltenia"
        ));
        
        addAchievement(new Achievement(
            "oltenia_legendary",
            "Oltenia Legendary",
            "Get a 15-question correct streak in Oltenia",
            android.R.drawable.ic_menu_send,
            400,
            15,
            AchievementCategory.SPECIAL,
            "Oltenia"
        ));
        
        // Accuracy Achievements
        addAchievement(new Achievement(
            "oltenia_sharpshooter",
            "Oltenia Sharpshooter",
            "Maintain 90% accuracy over 50 Oltenia questions",
            android.R.drawable.ic_menu_mylocation,
            250,
            50,
            AchievementCategory.SPECIAL,
            "Oltenia"
        ));
        
        addAchievement(new Achievement(
            "oltenia_perfectionist",
            "Oltenia Perfectionist",
            "Maintain 95% accuracy over 100 Oltenia questions",
            android.R.drawable.ic_menu_mylocation,
            500,
            100,
            AchievementCategory.SPECIAL,
            "Oltenia"
        ));
        
        // Difficulty Level Achievements
        addAchievement(new Achievement(
            "transilvania_intermediate_unlock",
            "Rising Scholar",
            "Unlock Intermediate difficulty in Transilvania quiz",
            android.R.drawable.ic_menu_preferences,
            75,
            1,
            AchievementCategory.DIFFICULTY,
            "Transilvania"
        ));
        
        addAchievement(new Achievement(
            "transilvania_advanced_unlock",
            "Advanced Mind",
            "Unlock Advanced difficulty in Transilvania quiz",
            android.R.drawable.ic_menu_preferences,
            150,
            1,
            AchievementCategory.DIFFICULTY,
            "Transilvania"
        ));
        
        addAchievement(new Achievement(
            "transilvania_expert_unlock",
            "Expert Knowledge",
            "Unlock Expert difficulty in Transilvania quiz",
            android.R.drawable.ic_menu_preferences,
            300,
            1,
            AchievementCategory.DIFFICULTY,
            "Transilvania"
        ));
        
        addAchievement(new Achievement(
            "transilvania_master_unlock",
            "Master of Transilvania",
            "Unlock Master difficulty in Transilvania quiz",
            android.R.drawable.ic_menu_preferences,
            500,
            1,
            AchievementCategory.DIFFICULTY,
            "Transilvania"
        ));
        
        // Game Mode Achievements
        addAchievement(new Achievement(
            "transilvania_lightning_champion",
            "Lightning Champion",
            "Complete 5 Lightning mode quizzes",
            android.R.drawable.ic_media_next,
            200,
            5,
            AchievementCategory.GAME_MODE,
            "Transilvania"
        ));
        
        addAchievement(new Achievement(
            "transilvania_marathon_runner",
            "Marathon Runner",
            "Complete a Marathon mode quiz",
            android.R.drawable.ic_media_play,
            300,
            1,
            AchievementCategory.GAME_MODE,
            "Transilvania"
        ));
        
        addAchievement(new Achievement(
            "transilvania_survival_expert",
            "Survival Expert",
            "Survive 15 questions in Survival mode",
            android.R.drawable.ic_menu_compass,
            250,
            15,
            AchievementCategory.GAME_MODE,
            "Transilvania"
        ));
        
        addAchievement(new Achievement(
            "transilvania_blitz_master",
            "Blitz Master",
            "Complete 10 Blitz mode quizzes",
            android.R.drawable.ic_media_next,
            400,
            10,
            AchievementCategory.GAME_MODE,
            "Transilvania"
        ));
        
        // Speed Achievements
        addAchievement(new Achievement(
            "transilvania_speed_demon",
            "Speed Demon",
            "Answer 10 questions in under 5 seconds each",
            android.R.drawable.ic_media_next,
            200,
            10,
            AchievementCategory.SPECIAL,
            "Transilvania"
        ));
        
        addAchievement(new Achievement(
            "transilvania_quick_thinker",
            "Quick Thinker",
            "Answer 50 questions in under 10 seconds each",
            android.R.drawable.ic_media_next,
            300,
            50,
            AchievementCategory.SPECIAL,
            "Transilvania"
        ));
        
        // Streak Achievements
        addAchievement(new Achievement(
            "transilvania_hot_streak",
            "Hot Streak",
            "Get a 5-question correct streak",
            android.R.drawable.ic_menu_send,
            100,
            5,
            AchievementCategory.SPECIAL,
            "Transilvania"
        ));
        
        addAchievement(new Achievement(
            "transilvania_blazing_streak",
            "Blazing Streak",
            "Get a 10-question correct streak",
            android.R.drawable.ic_menu_send,
            250,
            10,
            AchievementCategory.SPECIAL,
            "Transilvania"
        ));
        
        addAchievement(new Achievement(
            "transilvania_legendary_streak",
            "Legendary Streak",
            "Get a 20-question correct streak",
            android.R.drawable.ic_menu_send,
            500,
            20,
            AchievementCategory.SPECIAL,
            "Transilvania"
        ));
        
        // Learning Progress Achievements
        addAchievement(new Achievement(
            "transilvania_accuracy_master",
            "Accuracy Master",
            "Maintain 90% accuracy over 100 questions",
            android.R.drawable.ic_menu_edit,
            400,
            100,
            AchievementCategory.LEARNING,
            "Transilvania"
        ));
        
        addAchievement(new Achievement(
            "transilvania_consistent_learner",
            "Consistent Learner",
            "Play Transilvania quiz for 7 consecutive days",
            android.R.drawable.ic_menu_today,
            300,
            7,
            AchievementCategory.LEARNING,
            "transilvania"
        ));
        
        addAchievement(new Achievement(
            "transilvania_knowledge_seeker",
            "Knowledge Seeker",
            "Answer questions from all 8 categories",
            android.R.drawable.ic_menu_search,
            200,
            8,
            AchievementCategory.LEARNING,
            "Transilvania"
        ));
        
        // Special Milestone Achievements
        addAchievement(new Achievement(
            "transilvania_centurion",
            "Transilvanian Centurion",
            "Answer 100 questions correctly in Transilvania",
            android.R.drawable.ic_menu_view,
            600,
            100,
            AchievementCategory.SPECIAL,
            "Transilvania"
        ));
        
        addAchievement(new Achievement(
            "transilvania_legend",
            "Legend of Transilvania",
            "Answer 500 questions correctly in Transilvania",
            android.R.drawable.ic_dialog_info,
            1500,
            500,
            AchievementCategory.SPECIAL,
            "Transilvania"
        ));
        
        // City achievements (existing)
        for (String city : new String[]{"Sibiu", "Cluj", "Brașov", "București", "Iași", "Timișoara"}) {
            addAchievement(new Achievement(
                "city_" + city.toLowerCase(),
                city + " Explorer",
                "Explore all attractions in " + city,
                android.R.drawable.ic_menu_mapmode,
                200,
                3,
                AchievementCategory.CITY
            ));
        }
        
        // Load saved progress
        loadProgress();
    }
    
    private void addAchievement(Achievement achievement) {
        achievements.put(achievement.getId(), achievement);
    }
    
    private void loadProgress() {
        for (Achievement achievement : achievements.values()) {
            String progressKey = "achievement_progress_" + achievement.getId();
            int savedProgress = SharedPrefsHelper.getPrefs(context).getInt(progressKey, 0);
            achievement.setCurrentProgress(savedProgress);
            
            String unlockedKey = "achievement_unlocked_" + achievement.getId();
            boolean unlocked = SharedPrefsHelper.getPrefs(context).getBoolean(unlockedKey, false);
            achievement.setUnlocked(unlocked);
        }
    }
    
    private void saveProgress(Achievement achievement) {
        // Creăm datele pentru salvare
        Map<String, Object> achievementData = new HashMap<>();
        achievementData.put("id", achievement.getId());
        achievementData.put("currentProgress", achievement.getCurrentProgress());
        achievementData.put("unlocked", achievement.isUnlocked());
        achievementData.put("timestamp", System.currentTimeMillis());
        
        // Salvăm prin SyncManager - va salva local și sync cu cloud când e posibil
        syncManager.saveData("user_achievements", achievement.getId(), achievementData, new SyncManager.SyncCallback() {
            @Override
            public void onSyncComplete(boolean success, String message) {
                // Optional: log pentru debugging
                if (success) {
                    android.util.Log.d("AchievementManager", "✅ Achievement saved: " + achievement.getId());
                } else {
                    android.util.Log.w("AchievementManager", "⚠️ Achievement save failed: " + message);
                }
            }
        });
        
        // BACKUP: Salvăm și în SharedPreferences pentru compatibilitate
        String progressKey = "achievement_progress_" + achievement.getId();
        String unlockedKey = "achievement_unlocked_" + achievement.getId();
        
        SharedPrefsHelper.getPrefs(context).edit()
            .putInt(progressKey, achievement.getCurrentProgress())
            .putBoolean(unlockedKey, achievement.isUnlocked())
            .apply();
    }
    
    public void setAchievementUnlockedListener(AchievementUnlockedListener listener) {
        this.listener = listener;
    }
    
    public void updateAchievement(String achievementId, int progress) {
        Achievement achievement = achievements.get(achievementId);
        if (achievement != null) {
            boolean wasUnlocked = achievement.isUnlocked();
            achievement.setCurrentProgress(progress);
            saveProgress(achievement);
            
            if (!wasUnlocked && achievement.isUnlocked()) {
                // Achievement just unlocked
                if (listener != null) {
                    listener.onAchievementUnlocked(achievement);
                }
                
                // Give player reward
                SharedPrefsHelper.addToBalance(context, achievement.getPointsReward());
            }
        }
    }
    
    public void incrementAchievement(String achievementId) {
        Achievement achievement = achievements.get(achievementId);
        if (achievement != null) {
            updateAchievement(achievementId, achievement.getCurrentProgress() + 1);
        }
    }
    
    public List<Achievement> getAllAchievements() {
        return new ArrayList<>(achievements.values());
    }
    
    public List<Achievement> getUnlockedAchievements() {
        List<Achievement> unlockedList = new ArrayList<>();
        for (Achievement achievement : achievements.values()) {
            if (achievement.isUnlocked()) {
                unlockedList.add(achievement);
            }
        }
        return unlockedList;
    }
    
    public List<Achievement> getAchievementsByCategory(AchievementCategory category) {
        List<Achievement> categoryList = new ArrayList<>();
        for (Achievement achievement : achievements.values()) {
            if (achievement.getCategory() == category) {
                categoryList.add(achievement);
            }
        }
        return categoryList;
    }
    
    public List<Achievement> getAchievementsByRegion(String region) {
        List<Achievement> regionList = new ArrayList<>();
        for (Achievement achievement : achievements.values()) {
            if (region.equals(achievement.getRegion())) {
                regionList.add(achievement);
            }
        }
        return regionList;
    }
    
    public Achievement getAchievement(String achievementId) {
        return achievements.get(achievementId);
    }
    
    public void updateLocationExplorationAchievements() {
        LocationManager locationManager = LocationManager.getInstance(context);
        int visitedLocations = locationManager.getTotalVisitedAttractionsCount(context);
        
        updateAchievement("explorer_novice", visitedLocations);
        updateAchievement("explorer_advanced", visitedLocations);
        updateAchievement("explorer_master", visitedLocations);
        
        // Update city-specific achievements
        for (String city : new String[]{"Sibiu", "Cluj", "Brașov", "București", "Iași", "Timișoara"}) {
            int visitedInCity = locationManager.getVisitedAttractionsCount(context, city);
            updateAchievement("city_" + city.toLowerCase(), visitedInCity);
        }
    }
    
    public void updateQuestAchievements() {
        Set<String> completedQuests = SharedPrefsHelper.getCompletedQuests(context);
        int questCount = completedQuests.size();
        
        updateAchievement("quest_beginner", questCount);
        updateAchievement("quest_expert", questCount);
    }
    
    public void incrementQuizCorrectAnswers() {
        String key = "quiz_correct_answers";
        int correctAnswers = SharedPrefsHelper.getPrefs(context).getInt(key, 0) + 1;
        
        SharedPrefsHelper.getPrefs(context).edit()
            .putInt(key, correctAnswers)
            .apply();
            
        updateAchievement("quiz_novice", correctAnswers);
        updateAchievement("quiz_master", correctAnswers);
    }
    
    // === NEW TRANSILVANIA SPECIFIC METHODS ===
    
    /**
     * Update Transilvania quiz completion achievements
     */
    public void updateTransilvaniaQuizCompletions() {
        String key = "transilvania_quiz_completions";
        int completions = SharedPrefsHelper.getPrefs(context).getInt(key, 0);
        
        updateAchievement("transilvania_first_quiz", completions);
        updateAchievement("transilvania_quiz_veteran", completions);
        updateAchievement("transilvania_quiz_master", completions);
    }
    
    /**
     * Increment Transilvania quiz completion count
     */
    public void incrementTransilvaniaQuizCompletions() {
        String key = "transilvania_quiz_completions";
        int completions = SharedPrefsHelper.getPrefs(context).getInt(key, 0) + 1;
        
        SharedPrefsHelper.getPrefs(context).edit()
            .putInt(key, completions)
            .apply();
            
        updateTransilvaniaQuizCompletions();
    }
    
    /**
     * Update perfect score achievements
     */
    public void updateTransilvaniaPerfectScores() {
        String key = "transilvania_perfect_scores";
        int perfectScores = SharedPrefsHelper.getPrefs(context).getInt(key, 0);
        
        updateAchievement("transilvania_perfect_score", perfectScores);
        
        // Check for consecutive perfect scores
        String streakKey = "transilvania_perfect_streak_current";
        int currentStreak = SharedPrefsHelper.getPrefs(context).getInt(streakKey, 0);
        updateAchievement("transilvania_perfect_streak", currentStreak);
    }
    
    /**
     * Record a perfect score
     */
    public void recordTransilvaniaPerfectScore() {
        // Increment perfect scores
        String key = "transilvania_perfect_scores";
        int perfectScores = SharedPrefsHelper.getPrefs(context).getInt(key, 0) + 1;
        
        // Update perfect streak
        String streakKey = "transilvania_perfect_streak_current";
        int currentStreak = SharedPrefsHelper.getPrefs(context).getInt(streakKey, 0) + 1;
        
        // Update best streak if current is better
        String bestStreakKey = "transilvania_perfect_streak_best";
        int bestStreak = SharedPrefsHelper.getPrefs(context).getInt(bestStreakKey, 0);
        if (currentStreak > bestStreak) {
            bestStreak = currentStreak;
        }
        
        SharedPrefsHelper.getPrefs(context).edit()
            .putInt(key, perfectScores)
            .putInt(streakKey, currentStreak)
            .putInt(bestStreakKey, bestStreak)
            .apply();
            
        updateTransilvaniaPerfectScores();
    }
    
    /**
     * Break perfect score streak
     */
    public void breakTransilvaniaPerfectStreak() {
        String streakKey = "transilvania_perfect_streak_current";
        SharedPrefsHelper.getPrefs(context).edit()
            .putInt(streakKey, 0)
            .apply();
    }
    
    /**
     * Update category mastery achievements
     */
    public void updateTransilvaniaCategoryMastery(String category) {
        String key = "transilvania_" + category.toLowerCase() + "_correct";
        int correct = SharedPrefsHelper.getPrefs(context).getInt(key, 0);
        
        updateAchievement("transilvania_" + category.toLowerCase() + "_master", correct);
    }
    
    /**
     * Increment category correct answers
     */
    public void incrementTransilvaniaCategoryCorrect(String category) {
        String key = "transilvania_" + category.toLowerCase() + "_correct";
        int correct = SharedPrefsHelper.getPrefs(context).getInt(key, 0) + 1;
        
        SharedPrefsHelper.getPrefs(context).edit()
            .putInt(key, correct)
            .apply();
            
        updateTransilvaniaCategoryMastery(category);
        
        // Check if all categories have been answered
        String[] categories = {"history", "geography", "culture", "architecture", "gastronomy", "legends", "personalities", "nature"};
        int categoriesWithAnswers = 0;
        for (String cat : categories) {
            String catKey = "transilvania_" + cat + "_correct";
            if (SharedPrefsHelper.getPrefs(context).getInt(catKey, 0) > 0) {
                categoriesWithAnswers++;
            }
        }
        updateAchievement("transilvania_knowledge_seeker", categoriesWithAnswers);
    }
    
    /**
     * Update difficulty unlock achievements
     */
    public void updateTransilvaniaDifficultyUnlock(String difficulty) {
        String achievementId = "transilvania_" + difficulty.toLowerCase() + "_unlock";
        updateAchievement(achievementId, 1);
    }
    
    /**
     * Update game mode achievements
     */
    public void updateTransilvaniaGameModeAchievements(String gameMode) {
        String key = "transilvania_" + gameMode.toLowerCase() + "_completions";
        int completions = SharedPrefsHelper.getPrefs(context).getInt(key, 0);
        
        switch (gameMode.toLowerCase()) {
            case "lightning":
                updateAchievement("transilvania_lightning_champion", completions);
                break;
            case "marathon":
                updateAchievement("transilvania_marathon_runner", completions);
                break;
            case "blitz":
                updateAchievement("transilvania_blitz_master", completions);
                break;
        }
    }
    
    /**
     * Increment game mode completions
     */
    public void incrementTransilvaniaGameModeCompletion(String gameMode) {
        String key = "transilvania_" + gameMode.toLowerCase() + "_completions";
        int completions = SharedPrefsHelper.getPrefs(context).getInt(key, 0) + 1;
        
        SharedPrefsHelper.getPrefs(context).edit()
            .putInt(key, completions)
            .apply();
            
        updateTransilvaniaGameModeAchievements(gameMode);
    }
    
    /**
     * Update survival mode achievements
     */
    public void updateTransilvaniaSurvivalProgress(int questionsAnswered) {
        updateAchievement("transilvania_survival_expert", questionsAnswered);
    }
    
    /**
     * Update speed achievements
     */
    public void updateTransilvaniaSpeedAchievements(float answerTime) {
        if (answerTime <= 5.0f) {
            String key = "transilvania_fast_answers_5s";
            int fastAnswers = SharedPrefsHelper.getPrefs(context).getInt(key, 0) + 1;
            SharedPrefsHelper.getPrefs(context).edit().putInt(key, fastAnswers).apply();
            updateAchievement("transilvania_speed_demon", fastAnswers);
        }
        
        if (answerTime <= 10.0f) {
            String key = "transilvania_fast_answers_10s";
            int fastAnswers = SharedPrefsHelper.getPrefs(context).getInt(key, 0) + 1;
            SharedPrefsHelper.getPrefs(context).edit().putInt(key, fastAnswers).apply();
            updateAchievement("transilvania_quick_thinker", fastAnswers);
        }
    }
    
    /**
     * Update streak achievements
     */
    public void updateTransilvaniaStreakAchievements(int currentStreak) {
        updateAchievement("transilvania_hot_streak", Math.min(currentStreak, 5));
        updateAchievement("transilvania_blazing_streak", Math.min(currentStreak, 10));
        updateAchievement("transilvania_legendary_streak", Math.min(currentStreak, 20));
    }
    
    /**
     * Update accuracy achievements
     */
    public void updateTransilvaniaAccuracyAchievements() {
        String correctKey = "transilvania_total_correct";
        String totalKey = "transilvania_total_questions";
        
        int correct = SharedPrefsHelper.getPrefs(context).getInt(correctKey, 0);
        int total = SharedPrefsHelper.getPrefs(context).getInt(totalKey, 0);
        
        if (total >= 100) {
            float accuracy = (float) correct / total;
            if (accuracy >= 0.9f) {
                updateAchievement("transilvania_accuracy_master", total);
            }
        }
        
        // Update milestone achievements
        updateAchievement("transilvania_centurion", correct);
        updateAchievement("transilvania_legend", correct);
    }
    
    /**
     * Record a Transilvania quiz answer
     */
    public void recordTransilvaniaQuizAnswer(boolean correct, String category, float answerTime, int currentStreak) {
        // Update totals
        String correctKey = "transilvania_total_correct";
        String totalKey = "transilvania_total_questions";
        
        int totalCorrect = SharedPrefsHelper.getPrefs(context).getInt(correctKey, 0);
        int totalQuestions = SharedPrefsHelper.getPrefs(context).getInt(totalKey, 0) + 1;
        
        if (correct) {
            totalCorrect++;
            incrementTransilvaniaCategoryCorrect(category);
            incrementQuizCorrectAnswers(); // Also update general quiz achievements
        }
        
        SharedPrefsHelper.getPrefs(context).edit()
            .putInt(correctKey, totalCorrect)
            .putInt(totalKey, totalQuestions)
            .apply();
        
        // Update various achievements
        updateTransilvaniaSpeedAchievements(answerTime);
        updateTransilvaniaStreakAchievements(currentStreak);
        updateTransilvaniaAccuracyAchievements();
    }
    
    /**
     * Update daily play streak
     */
    public void updateTransilvaniaDailyPlayStreak() {
        String key = "transilvania_daily_streak";
        String lastPlayKey = "transilvania_last_play_date";
        
        long today = System.currentTimeMillis() / (1000 * 60 * 60 * 24); // Days since epoch
        long lastPlay = SharedPrefsHelper.getPrefs(context).getLong(lastPlayKey, 0);
        
        int currentStreak = SharedPrefsHelper.getPrefs(context).getInt(key, 0);
        
        if (lastPlay == today - 1) {
            // Consecutive day
            currentStreak++;
        } else if (lastPlay < today - 1) {
            // Streak broken
            currentStreak = 1;
        }
        // If lastPlay == today, do nothing (already played today)
        
        SharedPrefsHelper.getPrefs(context).edit()
            .putInt(key, currentStreak)
            .putLong(lastPlayKey, today)
            .apply();
            
        updateAchievement("transilvania_consistent_learner", currentStreak);
    }
    
    /**
     * Call this method to update all achievements based on current game state
     */
    public void refreshAllAchievements() {
        updateLocationExplorationAchievements();
        updateQuestAchievements();
        
        // Update general quiz achievements
        int correctAnswers = SharedPrefsHelper.getPrefs(context).getInt("quiz_correct_answers", 0);
        updateAchievement("quiz_novice", correctAnswers);
        updateAchievement("quiz_master", correctAnswers);
        
        // Update Transilvania specific achievements
        updateTransilvaniaQuizCompletions();
        updateTransilvaniaPerfectScores();
        updateTransilvaniaAccuracyAchievements();
        updateTransilvaniaDailyPlayStreak();
        
        // Update Oltenia specific achievements
        updateOlteniaQuizCompletions();
        updateOlteniaPerfectScores();
        updateOlteniaAccuracyAchievements();
        
        // Update category achievements
        String[] categories = {"History", "Geography", "Culture", "Architecture", "Gastronomy", "Legends", "Personalities", "Nature"};
        for (String category : categories) {
            updateTransilvaniaCategoryMastery(category);
            updateOlteniaCategoryMastery(category);
        }
        
        // Update game mode achievements
        String[] gameModes = {"Lightning", "Marathon", "Blitz"};
        for (String gameMode : gameModes) {
            updateTransilvaniaGameModeAchievements(gameMode);
            updateOlteniaGameModeAchievements(gameMode);
        }
    }
    
    // === NEW OLTENIA SPECIFIC METHODS ===
    
    /**
     * Update Oltenia quiz completion achievements
     */
    public void updateOlteniaQuizCompletions() {
        String key = "oltenia_quiz_completions";
        int completions = SharedPrefsHelper.getPrefs(context).getInt(key, 0);
        
        updateAchievement("oltenia_first_quiz", completions);
        updateAchievement("oltenia_quiz_veteran", completions);
        updateAchievement("oltenia_quiz_master", completions);
    }
    
    /**
     * Increment Oltenia quiz completion count
     */
    public void incrementOlteniaQuizCompletions() {
        String key = "oltenia_quiz_completions";
        int completions = SharedPrefsHelper.getPrefs(context).getInt(key, 0) + 1;
        
        SharedPrefsHelper.getPrefs(context).edit()
            .putInt(key, completions)
            .apply();
            
        updateOlteniaQuizCompletions();
    }
    
    /**
     * Update Oltenia perfect score achievements
     */
    public void updateOlteniaPerfectScores() {
        String key = "oltenia_perfect_scores";
        int perfectScores = SharedPrefsHelper.getPrefs(context).getInt(key, 0);
        
        updateAchievement("oltenia_perfect_score", perfectScores);
        
        String streakKey = "oltenia_perfect_streak_current";
        int currentStreak = SharedPrefsHelper.getPrefs(context).getInt(streakKey, 0);
        updateAchievement("oltenia_perfect_streak", currentStreak);
    }
    
    /**
     * Record perfect score for Oltenia
     */
    public void recordOlteniaPerfectScore() {
        String key = "oltenia_perfect_scores";
        int perfectScores = SharedPrefsHelper.getPrefs(context).getInt(key, 0) + 1;
        
        String streakKey = "oltenia_perfect_streak_current";
        int currentStreak = SharedPrefsHelper.getPrefs(context).getInt(streakKey, 0) + 1;
        
        String maxStreakKey = "oltenia_perfect_streak_max";
        int maxStreak = SharedPrefsHelper.getPrefs(context).getInt(maxStreakKey, 0);
        
        SharedPrefsHelper.getPrefs(context).edit()
            .putInt(key, perfectScores)
            .putInt(streakKey, currentStreak)
            .putInt(maxStreakKey, Math.max(maxStreak, currentStreak))
            .apply();
            
        updateOlteniaPerfectScores();
    }
    
    /**
     * Break perfect score streak for Oltenia
     */
    public void breakOlteniaPerfectStreak() {
        String streakKey = "oltenia_perfect_streak_current";
        SharedPrefsHelper.getPrefs(context).edit()
            .putInt(streakKey, 0)
            .apply();
    }
    
    /**
     * Update Oltenia category mastery achievements
     */
    public void updateOlteniaCategoryMastery(String category) {
        String key = "oltenia_" + category.toLowerCase() + "_correct";
        int correctAnswers = SharedPrefsHelper.getPrefs(context).getInt(key, 0);
        updateAchievement("oltenia_" + category.toLowerCase() + "_master", correctAnswers);
    }
    
    /**
     * Increment category correct answers for Oltenia
     */
    public void incrementOlteniaCategoryCorrect(String category) {
        String key = "oltenia_" + category.toLowerCase() + "_correct";
        int correctAnswers = SharedPrefsHelper.getPrefs(context).getInt(key, 0) + 1;
        
        SharedPrefsHelper.getPrefs(context).edit()
            .putInt(key, correctAnswers)
            .apply();
            
        updateOlteniaCategoryMastery(category);
    }
    
    /**
     * Update Oltenia difficulty unlock achievements
     */
    public void updateOlteniaDifficultyUnlock(String difficulty) {
        String achievementId = "oltenia_" + difficulty + "_unlock";
        updateAchievement(achievementId, 1);
    }
    
    /**
     * Update Oltenia game mode achievements
     */
    public void updateOlteniaGameModeAchievements(String gameMode) {
        String key = "oltenia_" + gameMode.toLowerCase() + "_completions";
        int completions = SharedPrefsHelper.getPrefs(context).getInt(key, 0);
        
        String achievementId = "oltenia_" + gameMode.toLowerCase() + "_champion";
        if (gameMode.equals("Marathon")) {
            achievementId = "oltenia_marathon_runner";
        } else if (gameMode.equals("Blitz")) {
            achievementId = "oltenia_blitz_master";
        }
        
        updateAchievement(achievementId, completions);
    }
    
    /**
     * Increment Oltenia game mode completion
     */
    public void incrementOlteniaGameModeCompletion(String gameMode) {
        String key = "oltenia_" + gameMode.toLowerCase() + "_completions";
        int completions = SharedPrefsHelper.getPrefs(context).getInt(key, 0) + 1;
        
        SharedPrefsHelper.getPrefs(context).edit()
            .putInt(key, completions)
            .apply();
            
        updateOlteniaGameModeAchievements(gameMode);
    }
    
    /**
     * Update Oltenia daily play streak
     */
    public void updateOlteniaDailyPlayStreak() {
        String key = "oltenia_daily_streak";
        String lastPlayKey = "oltenia_last_play_date";
        
        long today = System.currentTimeMillis() / (1000 * 60 * 60 * 24); // Days since epoch
        long lastPlay = SharedPrefsHelper.getPrefs(context).getLong(lastPlayKey, 0);
        
        int currentStreak = SharedPrefsHelper.getPrefs(context).getInt(key, 0);
        
        if (lastPlay == today - 1) {
            // Consecutive day
            currentStreak++;
        } else if (lastPlay < today - 1) {
            // Streak broken
            currentStreak = 1;
        }
        // If lastPlay == today, do nothing (already played today)
        
        SharedPrefsHelper.getPrefs(context).edit()
            .putInt(key, currentStreak)
            .putLong(lastPlayKey, today)
            .apply();
            
        updateAchievement("oltenia_consistent_learner", currentStreak);
    }
    
    /**
     * Update Oltenia survival mode progress
     */
    public void updateOlteniaSurvivalProgress(int questionsAnswered) {
        updateAchievement("oltenia_survival_expert", questionsAnswered);
    }
    
    /**
     * Update Oltenia speed achievements
     */
    public void updateOlteniaSpeedAchievements(float answerTime) {
        // Track fast answers (under 5 seconds)
        if (answerTime < 5.0f) {
            String key = "oltenia_fast_answers_5s";
            int fastAnswers = SharedPrefsHelper.getPrefs(context).getInt(key, 0) + 1;
            SharedPrefsHelper.getPrefs(context).edit().putInt(key, fastAnswers).apply();
            updateAchievement("oltenia_speed_demon", fastAnswers);
        }
        
        // Track very fast answers (under 10 seconds)
        if (answerTime < 10.0f) {
            String key = "oltenia_fast_answers_10s";
            int fastAnswers = SharedPrefsHelper.getPrefs(context).getInt(key, 0) + 1;
            SharedPrefsHelper.getPrefs(context).edit().putInt(key, fastAnswers).apply();
            updateAchievement("oltenia_quick_thinker", fastAnswers);
        }
    }
    
    /**
     * Update Oltenia streak achievements
     */
    public void updateOlteniaStreakAchievements(int currentStreak) {
        updateAchievement("oltenia_hot_streak", currentStreak >= 5 ? 1 : 0);
        updateAchievement("oltenia_unstoppable", currentStreak >= 10 ? 1 : 0);
        updateAchievement("oltenia_legendary", currentStreak >= 15 ? 1 : 0);
    }
    
    /**
     * Update Oltenia accuracy achievements
     */
    public void updateOlteniaAccuracyAchievements() {
        String questionsKey = "oltenia_total_questions";
        String correctKey = "oltenia_total_correct";
        
        int totalQuestions = SharedPrefsHelper.getPrefs(context).getInt(questionsKey, 0);
        int totalCorrect = SharedPrefsHelper.getPrefs(context).getInt(correctKey, 0);
        
        if (totalQuestions >= 50) {
            float accuracy = (float) totalCorrect / totalQuestions;
            if (accuracy >= 0.90f) {
                updateAchievement("oltenia_sharpshooter", 1);
            }
        }
        
        if (totalQuestions >= 100) {
            float accuracy = (float) totalCorrect / totalQuestions;
            if (accuracy >= 0.95f) {
                updateAchievement("oltenia_perfectionist", 1);
            }
        }
    }
    
    /**
     * Record quiz answer for Oltenia analytics
     */
    public void recordOlteniaQuizAnswer(boolean correct, String category, float answerTime, int currentStreak) {
        // Update total stats
        String questionsKey = "oltenia_total_questions";
        String correctKey = "oltenia_total_correct";
        
        int totalQuestions = SharedPrefsHelper.getPrefs(context).getInt(questionsKey, 0) + 1;
        int totalCorrect = SharedPrefsHelper.getPrefs(context).getInt(correctKey, 0) + (correct ? 1 : 0);
        
        SharedPrefsHelper.getPrefs(context).edit()
            .putInt(questionsKey, totalQuestions)
            .putInt(correctKey, totalCorrect)
            .apply();
        
        // Update category stats
        if (correct && category != null) {
            incrementOlteniaCategoryCorrect(category);
        }
        
        // Update speed achievements
        updateOlteniaSpeedAchievements(answerTime);
        
        // Update streak achievements
        updateOlteniaStreakAchievements(currentStreak);
        
        // Update accuracy achievements
        updateOlteniaAccuracyAchievements();
    }
    
    /**
     * Check and update Oltenia-specific achievements based on game results
     */
    public void checkOlteniaAchievements(int score, float accuracy, int correctAnswers) {
        // Perfect score achievement
        if (accuracy >= 1.0f && correctAnswers >= 5) {
            recordOlteniaPerfectScore();
        }
        
        // High accuracy achievements
        if (accuracy >= 0.9f && correctAnswers >= 10) {
            updateAchievement("oltenia_accuracy_expert", 1);
        }
        
        if (accuracy >= 0.8f && correctAnswers >= 8) {
            updateAchievement("oltenia_accuracy_advanced", 1);
        }
        
        // Score-based achievements
        if (score >= 1000) {
            updateAchievement("oltenia_score_master", 1);
        }
        
        if (score >= 500) {
            updateAchievement("oltenia_score_expert", 1);
        }
        
        // Completion achievements
        incrementOlteniaQuizCompletions();
        
        // Check for new achievements and notify
        List<Achievement> newAchievements = getUnlockedAchievements().stream()
            .filter(a -> a.getRegion() != null && a.getRegion().equals("Oltenia"))
            .toList();
            
        for (Achievement achievement : newAchievements) {
            if (listener != null) {
                listener.onAchievementUnlocked(achievement);
            }
        }
    }
} 