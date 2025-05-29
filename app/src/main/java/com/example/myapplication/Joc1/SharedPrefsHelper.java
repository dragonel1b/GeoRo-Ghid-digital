package com.example.myapplication.Joc1;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class SharedPrefsHelper {

    private static final String PREFS_NAME = "RomGamePrefs";
    private static final String KEY_BALANCE = "balance";
    private static final String KEY_FUEL = "fuel";
    private static final String KEY_FOOD = "food";
    private static final String KEY_COMPLETED_QUESTS = "completed_quests";
    private static final String KEY_VISITED_CITIES = "visited_cities";
    private static final String KEY_PLAYER_LEVEL = "player_level";
    private static final String KEY_EXPERIENCE = "experience";

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Balance methods
    public static int getBalance(Context context) {
        return getPrefs(context).getInt(KEY_BALANCE, 100);  // Default balance is 100
    }

    public static void setBalance(Context context, int balance) {
        getPrefs(context).edit().putInt(KEY_BALANCE, balance).apply();
    }

    public static void addToBalance(Context context, int amount) {
        int currentBalance = getBalance(context);
        setBalance(context, currentBalance + amount);
    }

    // Fuel methods
    public static int getFuel(Context context) {
        return getPrefs(context).getInt(KEY_FUEL, 50);  // Default fuel is 50
    }

    public static void setFuel(Context context, int fuel) {
        getPrefs(context).edit().putInt(KEY_FUEL, fuel).apply();
    }

    public static void consumeFuel(Context context, int amount) {
        int currentFuel = getFuel(context);
        setFuel(context, Math.max(0, currentFuel - amount));
    }

    // Food methods
    public static int getFood(Context context) {
        return getPrefs(context).getInt(KEY_FOOD, 50);  // Default food is 50
    }

    public static void setFood(Context context, int food) {
        getPrefs(context).edit().putInt(KEY_FOOD, food).apply();
    }

    public static void consumeFood(Context context, int amount) {
        int currentFood = getFood(context);
        setFood(context, Math.max(0, currentFood - amount));
    }

    // Checkbox methods
    public static boolean getCheckboxState(Context context, String checkboxKey) {
        return getPrefs(context).getBoolean(checkboxKey, false);  // Default is unchecked
    }

    public static void setCheckboxState(Context context, String checkboxKey, boolean isChecked) {
        getPrefs(context).edit().putBoolean(checkboxKey, isChecked).apply();
    }

    // Quest tracking
    public static Set<String> getCompletedQuests(Context context) {
        return getPrefs(context).getStringSet(KEY_COMPLETED_QUESTS, new HashSet<>());
    }

    public static void markQuestCompleted(Context context, String questId) {
        Set<String> completedQuests = new HashSet<>(getCompletedQuests(context));
        completedQuests.add(questId);
        getPrefs(context).edit().putStringSet(KEY_COMPLETED_QUESTS, completedQuests).apply();
    }

    public static boolean isQuestCompleted(Context context, String questId) {
        return getCompletedQuests(context).contains(questId);
    }

    // City visits tracking
    public static Set<String> getVisitedCities(Context context) {
        return getPrefs(context).getStringSet(KEY_VISITED_CITIES, new HashSet<>());
    }

    public static void markCityVisited(Context context, String cityName) {
        Set<String> visitedCities = new HashSet<>(getVisitedCities(context));
        visitedCities.add(cityName);
        getPrefs(context).edit().putStringSet(KEY_VISITED_CITIES, visitedCities).apply();
    }

    public static boolean isCityVisited(Context context, String cityName) {
        return getVisitedCities(context).contains(cityName);
    }

    // Player progression
    public static int getPlayerLevel(Context context) {
        return getPrefs(context).getInt(KEY_PLAYER_LEVEL, 1);
    }

    public static void setPlayerLevel(Context context, int level) {
        getPrefs(context).edit().putInt(KEY_PLAYER_LEVEL, level).apply();
    }

    public static int getExperience(Context context) {
        return getPrefs(context).getInt(KEY_EXPERIENCE, 0);
    }

    public static void addExperience(Context context, int amount) {
        int currentExp = getExperience(context);
        int newExp = currentExp + amount;
        getPrefs(context).edit().putInt(KEY_EXPERIENCE, newExp).apply();
        
        // Level up logic
        int currentLevel = getPlayerLevel(context);
        int requiredExp = currentLevel * 100; // Simple level-up formula
        
        if (newExp >= requiredExp) {
            setPlayerLevel(context, currentLevel + 1);
            // Could trigger a level-up notification/callback here
        }
    }

    // Clear all saved data (for game reset)
    public static void resetGameData(Context context) {
        getPrefs(context).edit().clear().apply();
    }
}