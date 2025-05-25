package com.example.myapplication.Joc1.Culinary;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Class representing user's culinary profile including preferences and achievements
 * Uses a singleton pattern for global access
 */
public class UserCulinaryProfile {
    // Skill level constants
    public static final String SKILL_BEGINNER = "beginner";
    public static final String SKILL_INTERMEDIATE = "intermediate";
    public static final String SKILL_ADVANCED = "advanced";
    
    // Dietary preference constants
    public static final String DIET_NONE = "none";
    public static final String DIET_VEGETARIAN = "vegetarian";
    public static final String DIET_VEGAN = "vegan";
    public static final String DIET_GLUTEN_FREE = "gluten_free";
    public static final String DIET_LACTOSE_FREE = "lactose_free";
    public static final String DIET_KETO = "keto";
    public static final String DIET_LOW_CARB = "low_carb";
    
    // Meal type constants
    public static final String MEAL_BREAKFAST = "breakfast";
    public static final String MEAL_LUNCH = "lunch";
    public static final String MEAL_DINNER = "dinner";
    public static final String MEAL_SNACK = "snack";
    
    // Preference keys
    private static final String PREFS_NAME = "culinary_profile";
    private static final String KEY_FIRST_TIME = "first_time";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_SKILL_LEVEL = "skill_level";
    private static final String KEY_DIETARY_RESTRICTIONS = "dietary_restrictions";
    private static final String KEY_PREFERRED_CUISINES = "preferred_cuisines";
    private static final String KEY_COMPLETED_RECIPES = "completed_recipes";
    private static final String KEY_COOKING_ACHIEVEMENTS = "cooking_achievements";
    private static final String KEY_DIETARY_PREFERENCES = "dietary_preferences";
    private static final String KEY_ALLERGIES = "allergies";
    private static final String KEY_FAVORITE_MEAL_TIMES = "favorite_meal_times";
    private static final String KEY_FAVORITE_CUISINES = "favorite_cuisines";
    
    private static UserCulinaryProfile instance;
    private final SharedPreferences prefs;
    private String userName;
    
    // Private constructor for singleton pattern
    private UserCulinaryProfile(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Initialize default values if first time
        if (prefs.getBoolean(KEY_FIRST_TIME, true)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_FIRST_TIME, false);
            editor.putString(KEY_USER_NAME, "Chef");
            editor.putString(KEY_SKILL_LEVEL, SKILL_BEGINNER);
            editor.putStringSet(KEY_DIETARY_RESTRICTIONS, new HashSet<>());
            editor.putStringSet(KEY_PREFERRED_CUISINES, new HashSet<>());
            editor.putStringSet(KEY_COMPLETED_RECIPES, new HashSet<>());
            editor.putStringSet(KEY_COOKING_ACHIEVEMENTS, new HashSet<>());
            editor.putStringSet(KEY_DIETARY_PREFERENCES, new HashSet<>());
            editor.putStringSet(KEY_ALLERGIES, new HashSet<>());
            editor.putStringSet(KEY_FAVORITE_MEAL_TIMES, new HashSet<>());
            editor.putStringSet(KEY_FAVORITE_CUISINES, new HashSet<>());
            editor.apply();
        }
        
        // Load user name
        userName = prefs.getString(KEY_USER_NAME, "Chef");
    }
    
    /**
     * Constructor for creating a new profile with name and skill level
     * 
     * @param name User's name
     * @param skillLevel User's cooking skill level
     */
    public UserCulinaryProfile(String name, String skillLevel) {
        prefs = null; // This constructor is for temporary profile objects
        userName = name;
        // Note: Other methods that use prefs will fail, so this object
        // should only be used to pass data to saveToPreferences()
    }
    
    /**
     * Get singleton instance of the profile
     * 
     * @param context Application context
     * @return UserCulinaryProfile instance
     */
    public static synchronized UserCulinaryProfile getInstance(Context context) {
        if (instance == null) {
            instance = new UserCulinaryProfile(context);
        }
        return instance;
    }
    
    /**
     * Load user profile from shared preferences
     * 
     * @param context Application context
     * @return UserCulinaryProfile object
     */
    public static UserCulinaryProfile loadFromPreferences(Context context) {
        return getInstance(context);
    }
    
    /**
     * Save this profile to shared preferences
     * 
     * @param context Application context
     */
    public void saveToPreferences(Context context) {
        UserCulinaryProfile instance = getInstance(context);
        
        // If this is not the singleton instance, copy values to it
        if (this != instance) {
            instance.setUserName(this.userName);
            // Other fields would be copied here if needed
        }
    }
    
    /**
     * Get user's name
     * 
     * @return User name
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * Set user's name
     * 
     * @param name New user name
     */
    public void setUserName(String name) {
        this.userName = name;
        
        // Only save if this is the singleton instance with valid prefs
        if (prefs != null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_NAME, name);
            editor.apply();
        }
    }
    
    /**
     * Check if this is the first time using the app
     * 
     * @return True if first time, false otherwise
     */
    public boolean isFirstTime() {
        return prefs.getBoolean(KEY_FIRST_TIME, true);
    }
    
    /**
     * Complete the first-time setup
     */
    public void completeFirstTimeSetup() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_FIRST_TIME, false);
        editor.apply();
    }
    
    /**
     * Get user's cooking skill level
     * 
     * @return Skill level string
     */
    public String getSkillLevel() {
        return prefs.getString(KEY_SKILL_LEVEL, SKILL_BEGINNER);
    }
    
    /**
     * Set user's cooking skill level
     * 
     * @param skillLevel New skill level
     */
    public void setSkillLevel(String skillLevel) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_SKILL_LEVEL, skillLevel);
        editor.apply();
    }
    
    /**
     * Get user's dietary restrictions
     * 
     * @return Set of dietary restrictions
     */
    public Set<String> getDietaryRestrictions() {
        return new HashSet<>(prefs.getStringSet(KEY_DIETARY_RESTRICTIONS, new HashSet<>()));
    }
    
    /**
     * Set user's dietary restrictions
     * 
     * @param restrictions Set of dietary restrictions
     */
    public void setDietaryRestrictions(Set<String> restrictions) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_DIETARY_RESTRICTIONS, restrictions);
        editor.apply();
    }
    
    /**
     * Get user's dietary preferences
     * 
     * @return Set of dietary preferences
     */
    public Set<String> getDietaryPreferences() {
        return new HashSet<>(prefs.getStringSet(KEY_DIETARY_PREFERENCES, new HashSet<>()));
    }
    
    /**
     * Set user's dietary preferences
     * 
     * @param preferences Set of dietary preferences
     */
    public void setDietaryPreferences(Set<String> preferences) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_DIETARY_PREFERENCES, preferences);
        editor.apply();
    }
    
    /**
     * Get user's allergies
     * 
     * @return Set of allergies
     */
    public Set<String> getAllergies() {
        return new HashSet<>(prefs.getStringSet(KEY_ALLERGIES, new HashSet<>()));
    }
    
    /**
     * Set user's allergies
     * 
     * @param allergies Set of allergies
     */
    public void setAllergies(Set<String> allergies) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_ALLERGIES, allergies);
        editor.apply();
    }
    
    /**
     * Get user's favorite meal times
     * 
     * @return Set of preferred meal times
     */
    public Set<String> getFavoriteMealTimes() {
        return new HashSet<>(prefs.getStringSet(KEY_FAVORITE_MEAL_TIMES, new HashSet<>()));
    }
    
    /**
     * Set user's favorite meal times
     * 
     * @param mealTimes Set of preferred meal times
     */
    public void setFavoriteMealTimes(Set<String> mealTimes) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_FAVORITE_MEAL_TIMES, mealTimes);
        editor.apply();
    }
    
    /**
     * Get user's preferred cuisines
     * 
     * @return Set of preferred cuisines
     */
    public Set<String> getPreferredCuisines() {
        return new HashSet<>(prefs.getStringSet(KEY_PREFERRED_CUISINES, new HashSet<>()));
    }
    
    /**
     * Set user's preferred cuisines
     * 
     * @param cuisines Set of preferred cuisines
     */
    public void setPreferredCuisines(Set<String> cuisines) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_PREFERRED_CUISINES, cuisines);
        editor.apply();
    }
    
    /**
     * Get user's favorite cuisines
     * 
     * @return Set of favorite cuisines
     */
    public Set<String> getFavoriteCuisines() {
        return new HashSet<>(prefs.getStringSet(KEY_FAVORITE_CUISINES, new HashSet<>()));
    }
    
    /**
     * Set user's favorite cuisines
     * 
     * @param cuisines Set of favorite cuisines
     */
    public void setFavoriteCuisines(Set<String> cuisines) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_FAVORITE_CUISINES, cuisines);
        editor.apply();
    }
    
    /**
     * Get user's completed recipes
     * 
     * @return Set of completed recipe IDs
     */
    public Set<String> getCompletedRecipes() {
        return new HashSet<>(prefs.getStringSet(KEY_COMPLETED_RECIPES, new HashSet<>()));
    }
    
    /**
     * Add a completed recipe
     * 
     * @param recipeId ID of the completed recipe
     */
    public void addCompletedRecipe(String recipeId) {
        Set<String> completedRecipes = getCompletedRecipes();
        completedRecipes.add(recipeId);
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_COMPLETED_RECIPES, completedRecipes);
        editor.apply();
    }
    
    /**
     * Get user's cooking achievements
     * 
     * @return Set of earned achievement IDs
     */
    public Set<String> getCookingAchievements() {
        return new HashSet<>(prefs.getStringSet(KEY_COOKING_ACHIEVEMENTS, new HashSet<>()));
    }
    
    /**
     * Add a cooking achievement
     * 
     * @param achievementId ID of the earned achievement
     */
    public void addCookingAchievement(String achievementId) {
        Set<String> achievements = getCookingAchievements();
        achievements.add(achievementId);
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_COOKING_ACHIEVEMENTS, achievements);
        editor.apply();
    }
    
    /**
     * Reset user profile to default values
     */
    public void resetProfile() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.putBoolean(KEY_FIRST_TIME, false);
        editor.putString(KEY_SKILL_LEVEL, SKILL_BEGINNER);
        editor.putStringSet(KEY_DIETARY_RESTRICTIONS, new HashSet<>());
        editor.putStringSet(KEY_PREFERRED_CUISINES, new HashSet<>());
        editor.putStringSet(KEY_COMPLETED_RECIPES, new HashSet<>());
        editor.putStringSet(KEY_COOKING_ACHIEVEMENTS, new HashSet<>());
        editor.putStringSet(KEY_DIETARY_PREFERENCES, new HashSet<>());
        editor.putStringSet(KEY_ALLERGIES, new HashSet<>());
        editor.putStringSet(KEY_FAVORITE_MEAL_TIMES, new HashSet<>());
        editor.putStringSet(KEY_FAVORITE_CUISINES, new HashSet<>());
        editor.apply();
    }
} 