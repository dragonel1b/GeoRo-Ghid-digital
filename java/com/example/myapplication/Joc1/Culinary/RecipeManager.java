package com.example.myapplication.Joc1.Culinary;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Manager class for recipe operations and recommendations
 */
public class RecipeManager {
    private static RecipeManager instance;
    private final RecipeDBHelper dbHelper;
    private List<ModernCulinaryActivity.Recipe> cachedRecipes;
    
    /**
     * Get singleton instance
     * @param context Application context
     * @return RecipeManager instance
     */
    public static synchronized RecipeManager getInstance(Context context) {
        if (instance == null) {
            instance = new RecipeManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Private constructor
     * @param context Application context
     */
    private RecipeManager(Context context) {
        this.dbHelper = new RecipeDBHelper(context);
        this.cachedRecipes = null;
    }
    
    /**
     * Get all recipes
     * @return List of all recipes
     */
    public List<ModernCulinaryActivity.Recipe> getAllRecipes() {
        if (cachedRecipes == null) {
            cachedRecipes = dbHelper.getAllRecipes();
        }
        return new ArrayList<>(cachedRecipes);
    }
    
    /**
     * Get recipes by category
     * @param category Category to filter by
     * @return List of recipes in the specified category
     */
    public List<ModernCulinaryActivity.Recipe> getRecipesByCategory(String category) {
        List<ModernCulinaryActivity.Recipe> recipes = getAllRecipes();
        List<ModernCulinaryActivity.Recipe> filtered = new ArrayList<>();
        
        for (ModernCulinaryActivity.Recipe recipe : recipes) {
            if (recipe.getCategory().equals(category)) {
                filtered.add(recipe);
            }
        }
        
        return filtered;
    }
    
    /**
     * Get recipes by region
     * @param region Region to filter by
     * @return List of recipes from the specified region
     */
    public List<ModernCulinaryActivity.Recipe> getRecipesByRegion(String region) {
        List<ModernCulinaryActivity.Recipe> recipes = getAllRecipes();
        List<ModernCulinaryActivity.Recipe> filtered = new ArrayList<>();
        
        for (ModernCulinaryActivity.Recipe recipe : recipes) {
            if (recipe.getRegion().equals(region)) {
                filtered.add(recipe);
            }
        }
        
        return filtered;
    }
    
    /**
     * Search recipes by query
     * @param query Search query
     * @return List of matching recipes
     */
    public List<ModernCulinaryActivity.Recipe> searchRecipes(String query) {
        if (query == null || query.isEmpty()) {
            return getAllRecipes();
        }
        
        String lowercaseQuery = query.toLowerCase();
        List<ModernCulinaryActivity.Recipe> recipes = getAllRecipes();
        List<ModernCulinaryActivity.Recipe> results = new ArrayList<>();
        
        for (ModernCulinaryActivity.Recipe recipe : recipes) {
            if (recipe.getTitle().toLowerCase().contains(lowercaseQuery) ||
                    recipe.getDescription().toLowerCase().contains(lowercaseQuery) ||
                    recipe.getRegion().toLowerCase().contains(lowercaseQuery) ||
                    recipe.getCategory().toLowerCase().contains(lowercaseQuery)) {
                results.add(recipe);
            }
        }
        
        return results;
    }
    
    /**
     * Get favorite recipes
     * @return List of favorite recipes
     */
    public List<ModernCulinaryActivity.Recipe> getFavoriteRecipes() {
        List<ModernCulinaryActivity.Recipe> recipes = getAllRecipes();
        List<ModernCulinaryActivity.Recipe> favorites = new ArrayList<>();
        
        for (ModernCulinaryActivity.Recipe recipe : recipes) {
            if (recipe.isFavorite()) {
                favorites.add(recipe);
            }
        }
        
        return favorites;
    }
    
    /**
     * Toggle favorite status of a recipe
     * @param recipe Recipe to toggle
     * @return Updated favorite status
     */
    public boolean toggleFavorite(ModernCulinaryActivity.Recipe recipe) {
        boolean newStatus = !recipe.isFavorite();
        recipe.setFavorite(newStatus);
        dbHelper.updateFavoriteStatus(recipe.getId(), newStatus);
        return newStatus;
    }
    
    /**
     * Get recipe by ID
     * @param recipeId Recipe ID
     * @return Recipe or null if not found
     */
    public ModernCulinaryActivity.Recipe getRecipeById(long recipeId) {
        List<ModernCulinaryActivity.Recipe> recipes = getAllRecipes();
        
        for (ModernCulinaryActivity.Recipe recipe : recipes) {
            if (recipe.getId() == recipeId) {
                return recipe;
            }
        }
        
        return null;
    }
    
    /**
     * Get recipe by title and region
     * @param title Recipe title
     * @param region Recipe region
     * @return Recipe or null if not found
     */
    public ModernCulinaryActivity.Recipe getRecipeByTitleAndRegion(String title, String region) {
        List<ModernCulinaryActivity.Recipe> recipes = getAllRecipes();
        
        for (ModernCulinaryActivity.Recipe recipe : recipes) {
            if (recipe.getTitle().equals(title) && recipe.getRegion().equals(region)) {
                return recipe;
            }
        }
        
        return null;
    }
    
    /**
     * Get recommended recipes
     * @param limit Maximum number of recommendations
     * @return List of recommended recipes
     */
    public List<ModernCulinaryActivity.Recipe> getRecommendedRecipes(int limit) {
        List<ModernCulinaryActivity.Recipe> allRecipes = getAllRecipes();
        Map<ModernCulinaryActivity.Recipe, Integer> scores = new HashMap<>();
        
        // UserCulinaryProfile removed
        Set<String> dietaryPreferences = new HashSet<>();
        Set<String> allergies = new HashSet<>();
        Set<String> favoriteCuisines = new HashSet<>();
        String skillLevel = "BEGINNER";
        
        for (ModernCulinaryActivity.Recipe recipe : allRecipes) {
            // Skip recipes that don't match dietary preferences or contain allergens
            if (!isCompatibleWithDiet(recipe, dietaryPreferences) || containsAllergens(recipe, allergies)) {
                continue;
            }
            
            int score = 0;
            
            // Higher score for matching cuisine preferences
            if (favoriteCuisines.contains(recipe.getRegion())) {
                score += 5;
            }
            
            // Consider skill level
            score += getSkillCompatibilityScore(recipe.getDifficulty(), skillLevel);
            
            // Favorited recipes get bonus points
            if (recipe.isFavorite()) {
                score += 10;
            }
            
            // Add to scores map if relevant
            if (score > 0) {
                scores.put(recipe, score);
            }
        }
        
        // Sort by score and return top recommendations
        return getTopScoringRecipes(scores, limit);
    }
    
    /**
     * Check if recipe is compatible with dietary preferences
     * @param recipe Recipe to check
     * @param dietaryPreferences Set of dietary preferences
     * @return true if compatible, false otherwise
     */
    private boolean isCompatibleWithDiet(ModernCulinaryActivity.Recipe recipe, Set<String> dietaryPreferences) {
        // If no preferences, all recipes are compatible
        if (dietaryPreferences == null || dietaryPreferences.isEmpty()) {
            return true;
        }
        
        // For now, simplified compatibility checking
        // In a real implementation, would check ingredients against dietary requirements
        return true;
    }
    
    /**
     * Check if recipe contains allergens
     * @param recipe Recipe to check
     * @param allergies Set of allergies
     * @return true if recipe contains allergens, false otherwise
     */
    private boolean containsAllergens(ModernCulinaryActivity.Recipe recipe, Set<String> allergies) {
        // If no allergies, recipe is safe
        if (allergies == null || allergies.isEmpty()) {
            return false;
        }
        
        // For now, simplified allergen checking
        // In a real implementation, would check ingredients against allergens
        return false;
    }
    
    /**
     * Calculate skill compatibility score
     * @param difficulty Recipe difficulty
     * @param skillLevel User skill level
     * @return Compatibility score
     */
    private int getSkillCompatibilityScore(String difficulty, String skillLevel) {
        int difficultyScore;
        switch (difficulty) {
            case "Ușor":
                difficultyScore = 1;
                break;
            case "Mediu":
                difficultyScore = 2;
                break;
            case "Dificil":
                difficultyScore = 3;
                break;
            default:
                difficultyScore = 2; // Default to medium
        }
        
        int userScore;
        switch (skillLevel) {
            case "BEGINNER":
                userScore = 1;
                break;
            case "INTERMEDIATE":
                userScore = 2;
                break;
            case "ADVANCED":
                userScore = 3;
                break;
            default:
                userScore = 1; // Default to beginner
        }
        
        // Perfect match gets 5 points
        if (difficultyScore == userScore) {
            return 5;
        }
        // Recipe slightly easier than skill level gets 3 points
        else if (difficultyScore == userScore - 1) {
            return 3;
        }
        // Recipe slightly harder than skill level gets 2 points
        else if (difficultyScore == userScore + 1) {
            return 2;
        }
        // Recipe much easier or harder gets 0 points
        else {
            return 0;
        }
    }
    
    /**
     * Get top scoring recipes
     * @param scores Map of recipes to scores
     * @param limit Maximum number of recipes to return
     * @return List of top recipes
     */
    private List<ModernCulinaryActivity.Recipe> getTopScoringRecipes(Map<ModernCulinaryActivity.Recipe, Integer> scores, int limit) {
        List<Map.Entry<ModernCulinaryActivity.Recipe, Integer>> sortedEntries = new ArrayList<>(scores.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        
        List<ModernCulinaryActivity.Recipe> result = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, sortedEntries.size()); i++) {
            result.add(sortedEntries.get(i).getKey());
        }
        
        return result;
    }
    
    /**
     * Invalidate cached recipes, forcing reload on next access
     */
    public void invalidateCache() {
        cachedRecipes = null;
    }
} 