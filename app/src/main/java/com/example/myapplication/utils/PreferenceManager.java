package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.core.domain.model.UserProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for managing SharedPreferences
 */
public class PreferenceManager {
    private static final String PREF_NAME = "RomanianCulinaryApp";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_DISPLAY_NAME = "user_display_name";
    private static final String KEY_FAVORITE_RECIPES = "favorite_recipes";
    private static final String KEY_RECENT_RECIPES = "recent_recipes";
    private static final String KEY_CONTRIBUTED_RECIPES = "contributed_recipes";
    private static final String KEY_FIRST_TIME_USER = "first_time_user";
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_DIETARY_PREFERENCES = "dietary_preferences";
    
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    
    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    // User related preferences
    public void saveUserId(String userId) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply();
    }
    
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, "");
    }
    
    public void saveUserName(String userName) {
        sharedPreferences.edit().putString(KEY_USER_NAME, userName).apply();
    }
    
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }
    
    public void saveUserEmail(String email) {
        sharedPreferences.edit().putString(KEY_USER_EMAIL, email).apply();
    }
    
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }
    
    public void saveUserDisplayName(String displayName) {
        sharedPreferences.edit().putString(KEY_USER_DISPLAY_NAME, displayName).apply();
    }
    
    public String getUserDisplayName() {
        return sharedPreferences.getString(KEY_USER_DISPLAY_NAME, "");
    }
    
    public void clearUserData() {
        sharedPreferences.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_DISPLAY_NAME)
                .apply();
    }
    
    // Recipe related preferences
    public void saveFavoriteRecipes(Set<Integer> recipeIds) {
        sharedPreferences.edit().putStringSet(KEY_FAVORITE_RECIPES, 
                convertIntSetToStringSet(recipeIds)).apply();
    }
    
    public Set<Integer> getFavoriteRecipes() {
        Set<String> stringSet = sharedPreferences.getStringSet(KEY_FAVORITE_RECIPES, new HashSet<>());
        return convertStringSetToIntSet(stringSet);
    }
    
    public void addFavoriteRecipe(int recipeId) {
        Set<Integer> favorites = getFavoriteRecipes();
        favorites.add(recipeId);
        saveFavoriteRecipes(favorites);
    }
    
    public void removeFavoriteRecipe(int recipeId) {
        Set<Integer> favorites = getFavoriteRecipes();
        favorites.remove(recipeId);
        saveFavoriteRecipes(favorites);
    }
    
    public boolean isRecipeFavorite(int recipeId) {
        return getFavoriteRecipes().contains(recipeId);
    }
    
    public void saveRecentRecipes(List<Integer> recipeIds) {
        String json = gson.toJson(recipeIds);
        sharedPreferences.edit().putString(KEY_RECENT_RECIPES, json).apply();
    }
    
    public List<Integer> getRecentRecipes() {
        String json = sharedPreferences.getString(KEY_RECENT_RECIPES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Integer>>() {}.getType();
        return gson.fromJson(json, type);
    }
    
    public void addRecentRecipe(int recipeId) {
        List<Integer> recents = getRecentRecipes();
        // Remove if already exists to avoid duplicates
        recents.remove(Integer.valueOf(recipeId));
        // Add to the beginning of the list
        recents.add(0, recipeId);
        // Keep only the most recent 10
        if (recents.size() > 10) {
            recents = recents.subList(0, 10);
        }
        saveRecentRecipes(recents);
    }
    
    public void saveContributedRecipes(List<Integer> recipeIds) {
        String json = gson.toJson(recipeIds);
        sharedPreferences.edit().putString(KEY_CONTRIBUTED_RECIPES, json).apply();
    }
    
    public List<Integer> getContributedRecipes() {
        String json = sharedPreferences.getString(KEY_CONTRIBUTED_RECIPES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Integer>>() {}.getType();
        return gson.fromJson(json, type);
    }
    
    public void addContributedRecipe(int recipeId) {
        List<Integer> contributed = getContributedRecipes();
        if (!contributed.contains(recipeId)) {
            contributed.add(recipeId);
            saveContributedRecipes(contributed);
        }
    }
    
    // App settings preferences
    public void setFirstTimeUser(boolean isFirstTime) {
        sharedPreferences.edit().putBoolean(KEY_FIRST_TIME_USER, isFirstTime).apply();
    }
    
    public boolean isFirstTimeUser() {
        return sharedPreferences.getBoolean(KEY_FIRST_TIME_USER, true);
    }
    
    public void setNotificationEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply();
    }
    
    public boolean isNotificationEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }
    
    public void setDarkMode(boolean darkMode) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, darkMode).apply();
    }
    
    public boolean isDarkMode() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false);
    }
    
    public void setLanguage(String language) {
        sharedPreferences.edit().putString(KEY_LANGUAGE, language).apply();
    }
    
    public String getLanguage() {
        return sharedPreferences.getString(KEY_LANGUAGE, "ro");
    }
    
    public void saveDietaryPreferences(Set<String> preferences) {
        sharedPreferences.edit().putStringSet(KEY_DIETARY_PREFERENCES, preferences).apply();
    }
    
    public Set<String> getDietaryPreferences() {
        return sharedPreferences.getStringSet(KEY_DIETARY_PREFERENCES, new HashSet<>());
    }
    
    // Utility methods
    private Set<String> convertIntSetToStringSet(Set<Integer> intSet) {
        Set<String> stringSet = new HashSet<>();
        for (Integer value : intSet) {
            stringSet.add(value.toString());
        }
        return stringSet;
    }
    
    private Set<Integer> convertStringSetToIntSet(Set<String> stringSet) {
        Set<Integer> intSet = new HashSet<>();
        for (String value : stringSet) {
            try {
                intSet.add(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                // Skip invalid values
            }
        }
        return intSet;
    }

    /**
     * Returns a UserProfile object with the current user's data
     */
    public UserProfile getUserProfile() {
        String userId = getUserId();
        String username = getUserName();
        String displayName = getUserDisplayName();
        String email = getUserEmail();
        
        if (userId.isEmpty() && username.isEmpty() && email.isEmpty()) {
            return null; // No user data available
        }
        
        UserProfile profile = new UserProfile(userId, username, displayName, email);
        return profile;
    }
} 