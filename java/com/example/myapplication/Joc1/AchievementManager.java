package com.example.myapplication.Joc1;

import android.content.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages the player's achievements and progress
 */
public class AchievementManager {
    private static AchievementManager instance;
    private Context context;
    private Map<String, Achievement> achievements = new HashMap<>();
    
    public static class Achievement {
        private String id;
        private String title;
        private String description;
        private int iconResourceId;
        private boolean unlocked;
        private int pointsReward;
        private int requiredProgress;
        private int currentProgress;
        
        public Achievement(String id, String title, String description, int iconResourceId, int pointsReward, int requiredProgress) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.iconResourceId = iconResourceId;
            this.pointsReward = pointsReward;
            this.requiredProgress = requiredProgress;
            this.currentProgress = 0;
            this.unlocked = false;
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
    }
    
    public interface AchievementUnlockedListener {
        void onAchievementUnlocked(Achievement achievement);
    }
    
    private AchievementUnlockedListener listener;
    
    private AchievementManager(Context context) {
        this.context = context.getApplicationContext();
        initializeAchievements();
    }
    
    public static synchronized AchievementManager getInstance(Context context) {
        if (instance == null) {
            instance = new AchievementManager(context);
        }
        return instance;
    }
    
    private void initializeAchievements() {
        // Basic achievements for exploration
        addAchievement(new Achievement(
            "explorer_novice",
            "Novice Explorer",
            "Visit 3 different locations in Romania",
            android.R.drawable.ic_menu_compass, // Use appropriate icon
            50,
            3
        ));
        
        addAchievement(new Achievement(
            "explorer_advanced",
            "Advanced Explorer",
            "Visit 10 different locations in Romania",
            android.R.drawable.ic_menu_compass, // Use appropriate icon
            150,
            10
        ));
        
        addAchievement(new Achievement(
            "explorer_master",
            "Master Explorer",
            "Visit all locations in Romania",
            android.R.drawable.ic_menu_compass, // Use appropriate icon
            500,
            18 // Total number of locations
        ));
        
        // Quest achievements
        addAchievement(new Achievement(
            "quest_beginner",
            "Quest Beginner",
            "Complete 3 quests",
            android.R.drawable.ic_menu_help, // Use appropriate icon
            100,
            3
        ));
        
        addAchievement(new Achievement(
            "quest_expert",
            "Quest Expert",
            "Complete 10 quests",
            android.R.drawable.ic_menu_help, // Use appropriate icon
            300,
            10
        ));
        
        // Quiz achievements
        addAchievement(new Achievement(
            "quiz_novice",
            "Quiz Novice",
            "Answer 10 quiz questions correctly",
            android.R.drawable.ic_menu_help, // Use appropriate icon
            100,
            10
        ));
        
        addAchievement(new Achievement(
            "quiz_master",
            "Quiz Master",
            "Answer 50 quiz questions correctly",
            android.R.drawable.ic_menu_help, // Use appropriate icon
            500,
            50
        ));
        
        // City achievements
        for (String city : new String[]{"Sibiu", "Cluj", "Brașov", "București", "Iași", "Timișoara"}) {
            addAchievement(new Achievement(
                "city_" + city.toLowerCase(),
                city + " Explorer",
                "Explore all attractions in " + city,
                android.R.drawable.ic_menu_mapmode, // Use appropriate icon
                200,
                3 // Assuming 3 attractions per city
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
    
    /**
     * Call this method to update all achievements based on current game state
     */
    public void refreshAllAchievements() {
        updateLocationExplorationAchievements();
        updateQuestAchievements();
        
        // Update quiz achievements
        int correctAnswers = SharedPrefsHelper.getPrefs(context).getInt("quiz_correct_answers", 0);
        updateAchievement("quiz_novice", correctAnswers);
        updateAchievement("quiz_master", correctAnswers);
    }
} 