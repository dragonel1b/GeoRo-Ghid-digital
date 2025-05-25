package com.example.myapplication.Joc1;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.myapplication.R;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.android.gms.maps.model.LatLng;

public class RomGameState {
    private static RomGameState instance;
    private SharedPreferences preferences;
    private static final String PREF_NAME = "RomGamePreferences";

    // Resource keys
    private static final String KEY_ESENTA_CALATORIEI = "esenta_calatoriei"; // Fuel renamed to "Esența Călătoriei"
    private static final String KEY_MONEDE_DACICE = "monede_dacice"; // Money renamed to "Monede Dacice"
    private static final String KEY_MERINDE = "merinde"; // Food renamed to "Merinde"
    private static final String KEY_PUNCTE_INTELEPTE = "puncte_intelepte"; // Culture points renamed to "Puncte Înțelepte"
    private static final String KEY_ACHIEVEMENTS = "achievements";
    private static final String KEY_VISITED_LOCATIONS = "visited_locations";
    private static final String KEY_CITY_PHOTOS = "city_photos";
    private static final String KEY_BUCKET_LIST = "bucket_list";
    private static final String KEY_STORY_CHAPTER = "story_chapter";
    private static final String KEY_STORY_STEP = "story_step";
    private static final String KEY_COMPLETED_MISSIONS = "completed_missions";
    private static final String KEY_ACTIVE_MISSIONS = "active_missions";

    // Achievement IDs
    public static final String ACHIEVEMENT_PRIMUL_PAS = "primul_pas"; // "First Step"
    public static final String ACHIEVEMENT_MAESTRU_ISTORIC = "maestru_istoric"; // "History Master"
    public static final String ACHIEVEMENT_CALATOR_LEGENDAR = "calator_legendar"; // "Legendary Traveler"
    public static final String ACHIEVEMENT_BUCATAR_REGAL = "bucatar_regal"; // "Royal Chef"
    public static final String ACHIEVEMENT_SPIRIT_DACIC = "spirit_dacic"; // "Dacian Spirit"
    public static final String ACHIEVEMENT_MEDALIE_BRONZ = "medalie_bronz"; // "Bronze Medal"
    public static final String ACHIEVEMENT_MEDALIE_ARGINT = "medalie_argint"; // "Silver Medal"
    public static final String ACHIEVEMENT_MEDALIE_AUR = "medalie_aur"; // "Gold Medal"
    // New achievement IDs for the achievements activity
    public static final String ACHIEVEMENT_EXPLORATOR_REGIONAL = "explorator_regional"; // "Regional Explorer"
    public static final String ACHIEVEMENT_ETNOGRAF_AMATOR = "etnograf_amator"; // "Amateur Ethnographer"
    public static final String ACHIEVEMENT_ISTORIC_CUNOSCATOR = "istoric_cunoscator"; // "History Expert"
    public static final String ACHIEVEMENT_CALATOR_PASIONAT = "calator_pasionat"; // "Passionate Traveler"
    public static final String ACHIEVEMENT_FOLCLORIST_EXPERIMENTAT = "folclorist_experimentat"; // "Experienced Folklorist"
    public static final String ACHIEVEMENT_MASTER_CULTURAL = "master_cultural"; // "Cultural Master"
    // New badges for cooking and streaks
    public static final String ACHIEVEMENT_BUCATAR_DEDICAT = "bucatar_dedicat"; // "Dedicated Chef" - streak of 3 days
    public static final String ACHIEVEMENT_BUCATAR_PERSEVERENT = "bucatar_perseverent"; // "Persevering Chef" - streak of 7 days
    public static final String ACHIEVEMENT_BUCATAR_MAESTRU = "bucatar_maestru"; // "Master Chef" - streak of 14 days
    public static final String ACHIEVEMENT_COLECTIONAR_RETETE = "colectionar_retete"; // "Recipe Collector" - discover 10 recipes
    public static final String ACHIEVEMENT_AMBASADOR_CULINAR = "ambasador_culinar"; // "Culinary Ambassador" - share 5 recipes
    public static final String ACHIEVEMENT_CRITIC_GASTRONOMIC = "critic_gastronomic"; // "Food Critic" - write 10 reviews

    // Default values with Romanian-themed descriptions
    private static final float DEFAULT_ESENTA = 50.0f; // Starting essence for your journey
    private static final float DEFAULT_MONEDE = 1000.0f; // Initial Dacian coins
    private static final float DEFAULT_MERINDE = 5.0f; // Starting provisions
    private static final int DEFAULT_PUNCTE = 0; // Initial wisdom points
    private static final int DEFAULT_STORY_CHAPTER = 1;
    private static final int DEFAULT_STORY_STEP = 1;

    // Current values with Romanian-themed names
    private float esentaCalatoriei; // Travel essence
    private float monedeDacice; // Dacian coins
    private float merinde; // Provisions
    private int puncteIntelepte; // Wisdom points
    private Set<String> unlockedAchievements;
    private List<LatLng> visitedLocations;
    private Map<String, String> cityPhotos; // Maps city names to photo URIs
    private Set<String> bucketListCities;
    private int storyChapter;
    private int storyStep;
    private Set<String> completedMissionIds;
    private Set<String> activeMissionIds;

    // Additional tracking variables
    private int regionsVisited = 0;
    private int recipesDiscovered = 0;
    private int collectedItems = 0;
    private int correctQuizAnswers = 0;
    private int travelDistance = 0;
    private int playedGames = 0;
    private int quizCompleted = 0;
    private int cookingStreak = 0;
    private long lastCookingDate = 0;
    private int recipesShared = 0;
    private int reviewsWritten = 0;
    
    // Keys for SharedPreferences
    private static final String KEY_REGIONS_VISITED = "regions_visited";
    private static final String KEY_RECIPES_DISCOVERED = "recipes_discovered";
    private static final String KEY_COLLECTED_ITEMS = "collected_items";
    private static final String KEY_CORRECT_QUIZ_ANSWERS = "correct_quiz_answers";
    private static final String KEY_TRAVEL_DISTANCE = "travel_distance";
    private static final String KEY_PLAYED_GAMES = "played_games";
    private static final String KEY_QUIZ_COMPLETED = "quiz_completed";
    private static final String KEY_COOKING_STREAK = "cooking_streak";
    private static final String KEY_LAST_COOKING_DATE = "last_cooking_date";
    private static final String KEY_RECIPES_SHARED = "recipes_shared";
    private static final String KEY_REVIEWS_WRITTEN = "reviews_written";

    private RomGameState() {
        // Private constructor for singleton
        unlockedAchievements = new HashSet<>();
        visitedLocations = new ArrayList<>();
        cityPhotos = new HashMap<>();
        bucketListCities = new HashSet<>();
        storyChapter = DEFAULT_STORY_CHAPTER;
        storyStep = DEFAULT_STORY_STEP;
        completedMissionIds = new HashSet<>();
        activeMissionIds = new HashSet<>();
    }

    public static RomGameState getInstance() {
        if (instance == null) {
            instance = new RomGameState();
        }
        return instance;
    }

    public void initialize(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            loadState();
        }
    }

    private void loadState() {
        try {
            esentaCalatoriei = preferences.getFloat(KEY_ESENTA_CALATORIEI, DEFAULT_ESENTA);
            monedeDacice = preferences.getFloat(KEY_MONEDE_DACICE, DEFAULT_MONEDE);
            merinde = preferences.getFloat(KEY_MERINDE, DEFAULT_MERINDE);
            puncteIntelepte = preferences.getInt(KEY_PUNCTE_INTELEPTE, DEFAULT_PUNCTE);

            // Load achievements
            Set<String> defaultSet = new HashSet<>();
            unlockedAchievements = new HashSet<>(
                    preferences.getStringSet(KEY_ACHIEVEMENTS, defaultSet)
            );
            
            // Load visited locations
            Set<String> locationStrings = preferences.getStringSet(KEY_VISITED_LOCATIONS, defaultSet);
            visitedLocations = new ArrayList<>();
            for (String locStr : locationStrings) {
                String[] parts = locStr.split(",");
                if (parts.length == 2) {
                    visitedLocations.add(new LatLng(
                        Double.parseDouble(parts[0]),
                        Double.parseDouble(parts[1])
                    ));
                }
            }
            
            // Load city photos
            String photosJson = preferences.getString(KEY_CITY_PHOTOS, "{}");
            cityPhotos = new Gson().fromJson(photosJson, new TypeToken<Map<String, String>>(){}.getType());
            
            // Load bucket list
            bucketListCities = new HashSet<>(preferences.getStringSet(KEY_BUCKET_LIST, defaultSet));

            storyChapter = preferences.getInt(KEY_STORY_CHAPTER, DEFAULT_STORY_CHAPTER);
            storyStep = preferences.getInt(KEY_STORY_STEP, DEFAULT_STORY_STEP);
            completedMissionIds = new HashSet<>(preferences.getStringSet(KEY_COMPLETED_MISSIONS, new HashSet<>()));
            activeMissionIds = new HashSet<>(preferences.getStringSet(KEY_ACTIVE_MISSIONS, new HashSet<>()));
            
            // Load achievement tracking variables
            regionsVisited = preferences.getInt(KEY_REGIONS_VISITED, 0);
            recipesDiscovered = preferences.getInt(KEY_RECIPES_DISCOVERED, 0);
            collectedItems = preferences.getInt(KEY_COLLECTED_ITEMS, 0);
            correctQuizAnswers = preferences.getInt(KEY_CORRECT_QUIZ_ANSWERS, 0);
            travelDistance = preferences.getInt(KEY_TRAVEL_DISTANCE, 0);
            playedGames = preferences.getInt(KEY_PLAYED_GAMES, 0);
            quizCompleted = preferences.getInt(KEY_QUIZ_COMPLETED, 0);
            cookingStreak = preferences.getInt(KEY_COOKING_STREAK, 0);
            lastCookingDate = preferences.getLong(KEY_LAST_COOKING_DATE, 0);
            recipesShared = preferences.getInt(KEY_RECIPES_SHARED, 0);
            reviewsWritten = preferences.getInt(KEY_REVIEWS_WRITTEN, 0);
        } catch (Exception e) {
            e.printStackTrace();
            resetToDefaults();
        }
    }

    public void saveState() {
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat(KEY_ESENTA_CALATORIEI, esentaCalatoriei);
            editor.putFloat(KEY_MONEDE_DACICE, monedeDacice);
            editor.putFloat(KEY_MERINDE, merinde);
            editor.putInt(KEY_PUNCTE_INTELEPTE, puncteIntelepte);
            editor.putStringSet(KEY_ACHIEVEMENTS, unlockedAchievements);
            
            // Save visited locations
            Set<String> locationStrings = new HashSet<>();
            for (LatLng loc : visitedLocations) {
                locationStrings.add(loc.latitude + "," + loc.longitude);
            }
            editor.putStringSet(KEY_VISITED_LOCATIONS, locationStrings);
            
            // Save city photos
            editor.putString(KEY_CITY_PHOTOS, new Gson().toJson(cityPhotos));
            
            // Save bucket list
            editor.putStringSet(KEY_BUCKET_LIST, bucketListCities);
            editor.putInt(KEY_STORY_CHAPTER, storyChapter);
            editor.putInt(KEY_STORY_STEP, storyStep);
            editor.putStringSet(KEY_COMPLETED_MISSIONS, completedMissionIds);
            editor.putStringSet(KEY_ACTIVE_MISSIONS, activeMissionIds);
            
            // Save achievement tracking variables
            editor.putInt(KEY_REGIONS_VISITED, regionsVisited);
            editor.putInt(KEY_RECIPES_DISCOVERED, recipesDiscovered);
            editor.putInt(KEY_COLLECTED_ITEMS, collectedItems);
            editor.putInt(KEY_CORRECT_QUIZ_ANSWERS, correctQuizAnswers);
            editor.putInt(KEY_TRAVEL_DISTANCE, travelDistance);
            editor.putInt(KEY_PLAYED_GAMES, playedGames);
            editor.putInt(KEY_QUIZ_COMPLETED, quizCompleted);
            editor.putInt(KEY_COOKING_STREAK, cookingStreak);
            editor.putLong(KEY_LAST_COOKING_DATE, lastCookingDate);
            editor.putInt(KEY_RECIPES_SHARED, recipesShared);
            editor.putInt(KEY_REVIEWS_WRITTEN, reviewsWritten);
            
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetToDefaults() {
        esentaCalatoriei = DEFAULT_ESENTA;
        monedeDacice = DEFAULT_MONEDE;
        merinde = DEFAULT_MERINDE;
        puncteIntelepte = DEFAULT_PUNCTE;
        storyChapter = DEFAULT_STORY_CHAPTER;
        storyStep = DEFAULT_STORY_STEP;
        completedMissionIds = new HashSet<>();
        activeMissionIds = new HashSet<>();
        
        // Reset achievement tracking variables
        regionsVisited = 0;
        recipesDiscovered = 0;
        collectedItems = 0;
        correctQuizAnswers = 0;
        travelDistance = 0;
        playedGames = 0;
        quizCompleted = 0;
        cookingStreak = 0;
        lastCookingDate = 0;
        recipesShared = 0;
        reviewsWritten = 0;
        
        saveState();
    }

    // Resource getters with Romanian-themed names
    public float getEsentaCalatoriei() { return esentaCalatoriei; }
    public float getMonedeDacice() { return monedeDacice; }
    public float getMerinde() { return merinde; }
    public int getPuncteIntelepte() { return puncteIntelepte; }
    public int getStoryChapter() {
        return storyChapter;
    }
    public int getStoryStep() {
        return storyStep;
    }

    // Resource update methods with Romanian-themed names
    public boolean updateEsentaCalatoriei(float delta) {
        if (esentaCalatoriei + delta < 0) return false;
        esentaCalatoriei += delta;
        saveState();
        return true;
    }

    // Alias method for updateEsentaCalatoriei to maintain API compatibility
    public boolean updateFuel(float delta) {
        return updateEsentaCalatoriei(delta);
    }

    public boolean updateMonedeDacice(float delta) {
        if (monedeDacice + delta < 0) return false;
        monedeDacice += delta;
        saveState();
        return true;
    }

    // Alias method for updateMonedeDacice to maintain API compatibility
    public boolean updateMoney(float delta) {
        return updateMonedeDacice(delta);
    }

    public boolean updateMerinde(float delta) {
        if (merinde + delta < 0) return false;
        merinde += delta;
        saveState();
        return true;
    }

    // Alias method for updateMerinde to maintain API compatibility
    public boolean updateFood(float delta) {
        return updateMerinde(delta);
    }

    public boolean addPuncteIntelepte(int points, Context context) {
        if (points > 0) {
            puncteIntelepte += points;
            
            // Check for Dacian Spirit achievement
            if (puncteIntelepte >= 500 && !isAchievementUnlocked(ACHIEVEMENT_SPIRIT_DACIC)) {
                unlockAchievement(ACHIEVEMENT_SPIRIT_DACIC, context);
            }
            
            // Check for Cultural Master achievement
            if (puncteIntelepte >= 1000 && !isAchievementUnlocked(ACHIEVEMENT_MASTER_CULTURAL)) {
                unlockAchievement(ACHIEVEMENT_MASTER_CULTURAL, context);
            }
            
            saveState();
            return true;
        }
        return false;
    }

    // Resource cost calculations
    public float calculateFuelCost(float distance) {
        // Average consumption: 7L/100km
        return (distance * 7.0f) / 100.0f;
    }

    public float calculateFoodCost(int days) {
        // Average food consumption: 1.5kg per day
        return days * 1.5f;
    }

    // Game progress checks
    public boolean canTravelDistance(float distance) {
        float fuelNeeded = calculateFuelCost(distance);
        return esentaCalatoriei >= fuelNeeded;
    }

    public boolean canAffordPurchase(float cost) {
        return monedeDacice >= cost;
    }

    public boolean hasEnoughFood(int days) {
        return merinde >= calculateFoodCost(days);
    }

    // Achievement methods
    public boolean unlockAchievement(String achievementId, Context context) {
        if (!unlockedAchievements.contains(achievementId)) {
            unlockedAchievements.add(achievementId);
            saveState();

            // Show achievement notification
            Achievement achievement = ACHIEVEMENTS.get(achievementId);
            if (achievement != null && context != null) {
                Toast.makeText(context,
                        context.getString(R.string.achievement_unlocked, achievement.title),
                        Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return false;
    }

    public boolean unlockAchievement(String achievementId) {
        return unlockAchievement(achievementId, null);
    }

    public boolean isAchievementUnlocked(String achievementId) {
        return unlockedAchievements.contains(achievementId);
    }

    public Set<String> getUnlockedAchievements() {
        return new HashSet<>(unlockedAchievements);
    }

    public static class Achievement {
        public final String id;
        public final String title;
        public final String description;
        public final int pointsRequired;

        public Achievement(String id, String title, String description, int pointsRequired) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.pointsRequired = pointsRequired;
        }
    }

    // Location tracking methods
    public void addVisitedLocation(LatLng location, Context context) {
        if (!visitedLocations.contains(location)) {
            visitedLocations.add(location);
            saveState();
            
            // Check for Legendary Traveler achievement
            if (visitedLocations.size() >= 5 && 
                !isAchievementUnlocked(ACHIEVEMENT_CALATOR_LEGENDAR)) {
                unlockAchievement(ACHIEVEMENT_CALATOR_LEGENDAR, context);
            }
        }
    }
    
    public List<LatLng> getActivityLocations() {
        return new ArrayList<>(visitedLocations);
    }
    
    public boolean hasVisitedLocation(LatLng location) {
        return visitedLocations.contains(location);
    }

    public static final Map<String, Achievement> ACHIEVEMENTS = new HashMap<String, Achievement>() {{
        put(ACHIEVEMENT_PRIMUL_PAS, new Achievement(
                ACHIEVEMENT_PRIMUL_PAS,
                "Primul Pas",
                "Prima ta explorare în călătoria culturală prin România",
                10
        ));
        put(ACHIEVEMENT_MAESTRU_ISTORIC, new Achievement(
                ACHIEVEMENT_MAESTRU_ISTORIC,
                "Maestru Istoric",
                "Ai răspuns corect la 10 întrebări despre istoria României",
                50
        ));
        put(ACHIEVEMENT_CALATOR_LEGENDAR, new Achievement(
                ACHIEVEMENT_CALATOR_LEGENDAR,
                "Călător Legendar",
                "Ai vizitat 5 orașe din regiuni diferite ale României",
                100
        ));
        put(ACHIEVEMENT_BUCATAR_REGAL, new Achievement(
                ACHIEVEMENT_BUCATAR_REGAL,
                "Bucătar Regal",
                "Ai descoperit 5 rețete tradiționale românești",
                75
        ));
        put(ACHIEVEMENT_SPIRIT_DACIC, new Achievement(
                ACHIEVEMENT_SPIRIT_DACIC,
                "Spirit Dacic",
                "Ai acumulat 500 de Puncte Înțelepte în călătoria ta",
                200
        ));
        put(ACHIEVEMENT_MEDALIE_BRONZ, new Achievement(
                ACHIEVEMENT_MEDALIE_BRONZ,
                "Medalie de Bronz",
                "Ai completat prima ta provocare culturală",
                25
        ));
        put(ACHIEVEMENT_MEDALIE_ARGINT, new Achievement(
                ACHIEVEMENT_MEDALIE_ARGINT,
                "Medalie de Argint",
                "Ai completat 3 provocări culturale",
                50
        ));
        put(ACHIEVEMENT_MEDALIE_AUR, new Achievement(
                ACHIEVEMENT_MEDALIE_AUR,
                "Medalie de Aur",
                "Ai completat toate provocările culturale",
                100
        ));
        // New achievements
        put(ACHIEVEMENT_EXPLORATOR_REGIONAL, new Achievement(
                ACHIEVEMENT_EXPLORATOR_REGIONAL,
                "Explorator Regional",
                "Ai vizitat cel puțin 3 regiuni diferite ale României",
                50
        ));
        put(ACHIEVEMENT_ETNOGRAF_AMATOR, new Achievement(
                ACHIEVEMENT_ETNOGRAF_AMATOR,
                "Etnograf Amator",
                "Ai colectat 10 obiecte tradiționale în mini-jocuri",
                75
        ));
        put(ACHIEVEMENT_ISTORIC_CUNOSCATOR, new Achievement(
                ACHIEVEMENT_ISTORIC_CUNOSCATOR,
                "Istoric Cunoscător",
                "Ai răspuns corect la 15 întrebări despre istoria României",
                100
        ));
        put(ACHIEVEMENT_CALATOR_PASIONAT, new Achievement(
                ACHIEVEMENT_CALATOR_PASIONAT,
                "Călător Pasionat",
                "Ai călătorit cel puțin 500 km pe harta României",
                125
        ));
        put(ACHIEVEMENT_FOLCLORIST_EXPERIMENTAT, new Achievement(
                ACHIEVEMENT_FOLCLORIST_EXPERIMENTAT,
                "Folclorist Experimentat",
                "Ai participat la 3 jocuri tradiționale românești",
                75
        ));
        put(ACHIEVEMENT_MASTER_CULTURAL, new Achievement(
                ACHIEVEMENT_MASTER_CULTURAL,
                "Maestru Cultural",
                "Ai acumulat 1000 de Puncte Înțelepte",
                200
        ));
    }};

    // New methods for city photos
    public void addCityPhoto(String cityName, String photoUri) {
        cityPhotos.put(cityName, photoUri);
        saveState();
    }

    public String getCityPhoto(String cityName) {
        return cityPhotos.get(cityName);
    }

    // New methods for bucket list
    public void addToBucketList(String cityName) {
        bucketListCities.add(cityName);
        saveState();
    }

    public void removeFromBucketList(String cityName) {
        bucketListCities.remove(cityName);
        saveState();
    }

    public boolean isInBucketList(String cityName) {
        return bucketListCities.contains(cityName);
    }

    // New methods for progress tracking
    public int getVisitedCityCount() {
        return visitedLocations.size();
    }

    public int getTotalCityCount() {
        // This should be updated with actual total city count
        return 20; // Example value - should match your actual city count
    }

    public float getProgressPercentage() {
        return (float)getVisitedCityCount() / getTotalCityCount() * 100;
    }

    public void setStoryProgress(int chapter, int step) {
        storyChapter = chapter;
        storyStep = step;
        saveState();
    }

    public boolean isMissionCompleted(String missionId) {
        return completedMissionIds.contains(missionId);
    }

    public boolean isMissionActive(String missionId) {
        return activeMissionIds.contains(missionId);
    }

    public void completeMission(String missionId) {
        if (activeMissionIds.contains(missionId)) {
            activeMissionIds.remove(missionId);
        }
        completedMissionIds.add(missionId);
        saveState();
    }

    public void activateMission(String missionId) {
        activeMissionIds.add(missionId);
        saveState();
    }

    public void deactivateMission(String missionId) {
        if (activeMissionIds.contains(missionId)) {
            activeMissionIds.remove(missionId);
            saveState();
        }
    }

    // Getter methods for achievement tracking
    public int getRegionsVisited() { return regionsVisited; }
    public int getRecipesDiscovered() { return recipesDiscovered; }
    public int getCollectedItems() { return collectedItems; }
    public int getCorrectQuizAnswers() { return correctQuizAnswers; }
    public int getTravelDistance() { return travelDistance; }
    public int getPlayedGames() { return playedGames; }
    public int getQuizCompleted() { return quizCompleted; }
    public int getCookingStreak() { return cookingStreak; }
    public long getLastCookingDate() { return lastCookingDate; }
    public int getRecipesShared() { return recipesShared; }
    public int getReviewsWritten() { return reviewsWritten; }
    
    // Update methods for achievement tracking
    public void visitRegion(String regionName, Context context) {
        regionsVisited++;
        saveState();
        
        // Check for achievement unlock
        if (regionsVisited >= 3 && !isAchievementUnlocked(ACHIEVEMENT_EXPLORATOR_REGIONAL)) {
            unlockAchievement(ACHIEVEMENT_EXPLORATOR_REGIONAL, context);
        }
    }
    
    public void discoverRecipe(Context context) {
        recipesDiscovered++;
        saveState();
        
        if (recipesDiscovered >= 5 && !isAchievementUnlocked(ACHIEVEMENT_BUCATAR_REGAL)) {
            unlockAchievement(ACHIEVEMENT_BUCATAR_REGAL, context);
        }
        
        if (recipesDiscovered >= 10 && !isAchievementUnlocked(ACHIEVEMENT_COLECTIONAR_RETETE)) {
            unlockAchievement(ACHIEVEMENT_COLECTIONAR_RETETE, context);
        }
    }
    
    public void collectItem(Context context) {
        collectedItems++;
        saveState();
        
        // Check for achievement unlock
        if (collectedItems >= 10 && !isAchievementUnlocked(ACHIEVEMENT_ETNOGRAF_AMATOR)) {
            unlockAchievement(ACHIEVEMENT_ETNOGRAF_AMATOR, context);
        }
    }
    
    public void answerQuizCorrectly(Context context) {
        correctQuizAnswers++;
        saveState();
        
        // Check for achievement unlock
        if (correctQuizAnswers >= 15 && !isAchievementUnlocked(ACHIEVEMENT_ISTORIC_CUNOSCATOR)) {
            unlockAchievement(ACHIEVEMENT_ISTORIC_CUNOSCATOR, context);
        }
    }
    
    public void addTravelDistance(int distance, Context context) {
        travelDistance += distance;
        saveState();
        
        // Check for achievement unlock
        if (travelDistance >= 500 && !isAchievementUnlocked(ACHIEVEMENT_CALATOR_PASIONAT)) {
            unlockAchievement(ACHIEVEMENT_CALATOR_PASIONAT, context);
        }
    }
    
    public void playTraditionalGame(Context context) {
        playedGames++;
        saveState();
        
        // Check for achievement unlock
        if (playedGames >= 3 && !isAchievementUnlocked(ACHIEVEMENT_FOLCLORIST_EXPERIMENTAT)) {
            unlockAchievement(ACHIEVEMENT_FOLCLORIST_EXPERIMENTAT, context);
        }
    }
    
    public void completeQuiz(Context context) {
        quizCompleted++;
        saveState();
    }

    /**
     * Record a cooking activity for today and update the streak
     * @param context Context to show notifications
     * @return true if the streak was incremented
     */
    public boolean recordCookingActivity(Context context) {
        long currentTime = System.currentTimeMillis();
        long oneDayInMillis = 24 * 60 * 60 * 1000;
        
        // Check if this is the first cooking activity
        if (lastCookingDate == 0) {
            cookingStreak = 1;
            lastCookingDate = currentTime;
            saveState();
            checkStreakAchievements(context);
            return true;
        }
        
        // Get days since last cooking
        long daysSinceLastCooking = (currentTime - lastCookingDate) / oneDayInMillis;
        
        // If already cooked today
        if (daysSinceLastCooking < 1) {
            return false;
        }
        
        // If cooked yesterday, increment streak
        if (daysSinceLastCooking <= 1) {
            cookingStreak++;
        } else {
            // Streak broken, start new streak
            cookingStreak = 1;
        }
        
        lastCookingDate = currentTime;
        saveState();
        checkStreakAchievements(context);
        return true;
    }

    /**
     * Check and unlock achievements related to cooking streaks
     */
    private void checkStreakAchievements(Context context) {
        if (cookingStreak >= 3 && !isAchievementUnlocked(ACHIEVEMENT_BUCATAR_DEDICAT)) {
            unlockAchievement(ACHIEVEMENT_BUCATAR_DEDICAT, context);
        }
        
        if (cookingStreak >= 7 && !isAchievementUnlocked(ACHIEVEMENT_BUCATAR_PERSEVERENT)) {
            unlockAchievement(ACHIEVEMENT_BUCATAR_PERSEVERENT, context);
        }
        
        if (cookingStreak >= 14 && !isAchievementUnlocked(ACHIEVEMENT_BUCATAR_MAESTRU)) {
            unlockAchievement(ACHIEVEMENT_BUCATAR_MAESTRU, context);
        }
    }

    /**
     * Record when user shares a recipe
     */
    public void shareRecipe(Context context) {
        recipesShared++;
        saveState();
        
        if (recipesShared >= 5 && !isAchievementUnlocked(ACHIEVEMENT_AMBASADOR_CULINAR)) {
            unlockAchievement(ACHIEVEMENT_AMBASADOR_CULINAR, context);
        }
    }

    /**
     * Record when user writes a review
     */
    public void writeReview(Context context) {
        reviewsWritten++;
        saveState();
        
        if (reviewsWritten >= 10 && !isAchievementUnlocked(ACHIEVEMENT_CRITIC_GASTRONOMIC)) {
            unlockAchievement(ACHIEVEMENT_CRITIC_GASTRONOMIC, context);
        }
    }

    /**
     * Get the culinary level of the player
     * @return Current culinary level
     */
    public int getCulinaryLevel() {
        // Default to level 1 if not set
        return preferences.getInt("culinary_level", 1);
    }

    /**
     * Set the culinary level of the player
     * @param level New culinary level
     */
    public void setCulinaryLevel(int level) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("culinary_level", level);
        editor.apply();
    }
}
