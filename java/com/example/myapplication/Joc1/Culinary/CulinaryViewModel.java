package com.example.myapplication.Joc1.Culinary;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class CulinaryViewModel extends ViewModel {
    // Weekly Challenges
    private final MutableLiveData<List<String>> activeChallenges = new MutableLiveData<>();
    private final MutableLiveData<Challenge> featuredChallenge = new MutableLiveData<>();
    
    // Community Recipes
    private final MutableLiveData<List<String>> communityRecipes = new MutableLiveData<>();
    
    // Achievements
    private final MutableLiveData<Integer> userPoints = new MutableLiveData<>();
    private final MutableLiveData<List<String>> userBadges = new MutableLiveData<>();
    
    // Nutrition
    private final MutableLiveData<Integer> dailyCalories = new MutableLiveData<>();
    private final MutableLiveData<List<String>> nutritionGoals = new MutableLiveData<>();
    
    // Recipes
    private final MutableLiveData<List<ModernCulinaryActivity.Recipe>> recipes = new MutableLiveData<>();
    private List<ModernCulinaryActivity.Recipe> allRecipes = new ArrayList<>();
    private List<String> activeFilters = new ArrayList<>();
    
    // Database helper
    private RecipeDBHelper dbHelper;

    public CulinaryViewModel() {
        // Initialize with sample data
        initializeSampleData();
    }
    
    /**
     * Sets the database helper for this view model
     * @param helper RecipeDBHelper instance
     */
    public void setDbHelper(RecipeDBHelper helper) {
        this.dbHelper = helper;
    }

    private void initializeSampleData() {
        // Weekly Challenges
        List<String> challenges = new ArrayList<>();
        challenges.add("Cook a Traditional Romanian Dish");
        challenges.add("Try a New Recipe");
        challenges.add("Share Your Recipe");
        activeChallenges.setValue(challenges);

        Challenge featured = new Challenge(
            "Master Chef Challenge",
            "Cook 3 different traditional Romanian dishes this week",
            "Earn 500 points and Master Chef badge",
            3
        );
        featuredChallenge.setValue(featured);

        // Community Recipes
        List<String> recipes = new ArrayList<>();
        recipes.add("Sarmale");
        recipes.add("Cozonac");
        recipes.add("Ciorba de Perisoare");
        communityRecipes.setValue(recipes);

        // Achievements
        userPoints.setValue(750);
        List<String> badges = new ArrayList<>();
        badges.add("master_chef_badge");
        badges.add("healthy_eating_badge");
        badges.add("quick_meals_badge");
        userBadges.setValue(badges);

        // Nutrition
        dailyCalories.setValue(2100);
        List<String> goals = new ArrayList<>();
        goals.add("calories");
        goals.add("protein");
        goals.add("carbs");
        goals.add("fats");
        nutritionGoals.setValue(goals);
    }
    
    /**
     * Loads all recipes from the data source
     */
    public void loadRecipes() {
        // If dbHelper is available, load from database
        if (dbHelper != null) {
            List<ModernCulinaryActivity.Recipe> dbRecipes = dbHelper.getAllRecipes();
            if (dbRecipes != null && !dbRecipes.isEmpty()) {
                allRecipes = dbRecipes;
                recipes.setValue(allRecipes);
                return;
            }
        }
        
        // Fallback to sample recipes
        allRecipes = ModernCulinaryActivity.getRecipes();
        recipes.setValue(allRecipes);
    }
    
    /**
     * Searches recipes by query string
     * @param query Search query string
     */
    public void searchRecipes(String query) {
        if (query == null || query.isEmpty()) {
            recipes.setValue(allRecipes);
            return;
        }
        
        List<ModernCulinaryActivity.Recipe> searchResults = new ArrayList<>();
        for (ModernCulinaryActivity.Recipe recipe : allRecipes) {
            if (recipe.getTitle().toLowerCase().contains(query) ||
                recipe.getDescription().toLowerCase().contains(query) ||
                recipe.getRegion().toLowerCase().contains(query) ||
                recipe.getCategory().toLowerCase().contains(query)) {
                searchResults.add(recipe);
            }
        }
        
        recipes.setValue(searchResults);
    }
    
    /**
     * Returns the LiveData containing recipes
     */
    public LiveData<List<ModernCulinaryActivity.Recipe>> getRecipes() {
        return recipes;
    }
    
    /**
     * Apply filters to the recipe list
     * @param regionFilters List of region filters to apply
     */
    public void applyFilters(List<String> regionFilters) {
        // Store active filters
        this.activeFilters = regionFilters;
        
        // If no filters, show all recipes
        if (regionFilters == null || regionFilters.isEmpty()) {
            recipes.setValue(allRecipes);
            return;
        }
        
        // Apply filters
        List<ModernCulinaryActivity.Recipe> filteredRecipes = new ArrayList<>();
        for (ModernCulinaryActivity.Recipe recipe : allRecipes) {
            if (regionFilters.contains(recipe.getRegion())) {
                filteredRecipes.add(recipe);
            }
        }
        
        // Update recipes
        recipes.setValue(filteredRecipes);
    }

    // Weekly Challenges
    public LiveData<List<String>> getActiveChallenges() {
        return activeChallenges;
    }

    public LiveData<Challenge> getFeaturedChallenge() {
        return featuredChallenge;
    }

    public void refreshChallenges() {
        // In a real app, this would fetch new data from a backend
        initializeSampleData();
    }

    // Community Recipes
    public LiveData<List<String>> getCommunityRecipes() {
        return communityRecipes;
    }

    public void refreshCommunityRecipes() {
        // In a real app, this would fetch new data from a backend
        initializeSampleData();
    }

    // Achievements
    public LiveData<Integer> getUserPoints() {
        return userPoints;
    }

    public LiveData<List<String>> getUserBadges() {
        return userBadges;
    }

    // Nutrition
    public LiveData<Integer> getDailyCalories() {
        return dailyCalories;
    }

    public LiveData<List<String>> getNutritionGoals() {
        return nutritionGoals;
    }

    public static class Challenge {
        private String title;
        private String description;
        private String reward;
        private int requiredSteps;
        private int completedSteps;

        public Challenge(String title, String description, String reward, int requiredSteps) {
            this.title = title;
            this.description = description;
            this.reward = reward;
            this.requiredSteps = requiredSteps;
            this.completedSteps = 0;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getReward() { return reward; }
        public int getRequiredSteps() { return requiredSteps; }
        public int getCompletedSteps() { return completedSteps; }
        
        public void incrementProgress() {
            if (completedSteps < requiredSteps) {
                completedSteps++;
            }
        }

        public boolean isCompleted() {
            return completedSteps >= requiredSteps;
        }
    }
}
