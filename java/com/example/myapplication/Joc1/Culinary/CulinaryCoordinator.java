package com.example.myapplication.Joc1.Culinary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity;
import com.example.myapplication.Joc1.Culinary.RecipeDetailActivity;
import com.example.myapplication.Joc1.Culinary.MealPlanningActivity;
import com.example.myapplication.Joc1.Culinary.ShoppingListActivity;
import com.example.myapplication.Joc1.Culinary.RecipeSuggestionActivity;

/**
 * Coordinator class to manage navigation between different culinary screens
 */
public class CulinaryCoordinator {
    private static CulinaryCoordinator instance;
    
    /**
     * Get singleton instance
     * @return CulinaryCoordinator instance
     */
    public static CulinaryCoordinator getInstance() {
        if (instance == null) {
            instance = new CulinaryCoordinator();
        }
        return instance;
    }
    
    private CulinaryCoordinator() {
        // Private constructor to enforce singleton pattern
    }
    
    /**
     * Navigate to recipe detail screen
     * @param context Current context
     * @param recipeId ID of the recipe to show
     */
    public void navigateToRecipeDetail(Context context, long recipeId) {
        Intent intent = new Intent(context, RecipeDetailActivity.class);
        intent.putExtra("recipe_id", recipeId);
        context.startActivity(intent);
    }
    
    /**
     * Navigate to recipe detail screen by title and region
     * @param context Current context
     * @param title Recipe title
     * @param region Recipe region
     */
    public void navigateToRecipeDetail(Context context, String title, String region) {
        Intent intent = new Intent(context, RecipeDetailActivity.class);
        intent.putExtra("recipe_title", title);
        intent.putExtra("recipe_region", region);
        context.startActivity(intent);
    }
    
    /**
     * Navigate to meal planning screen
     * @param context Current context
     */
    public void navigateToMealPlanning(Context context) {
        Intent intent = new Intent(context, MealPlanningActivity.class);
        context.startActivity(intent);
    }
    
    /**
     * Navigate to shopping list screen
     * @param context Current context
     */
    public void navigateToShoppingList(Context context) {
        Intent intent = new Intent(context, ShoppingListActivity.class);
        context.startActivity(intent);
    }
    
    /**
     * Navigate to recipe suggestion screen
     * @param context Current context
     */
    public void navigateToRecipeSuggestion(Context context) {
        Intent intent = new Intent(context, RecipeSuggestionActivity.class);
        context.startActivity(intent);
    }
    
    /**
     * Navigate to culinary home screen
     * @param context Current context
     */
    public void navigateToCulinaryHome(Context context) {
        Intent intent = new Intent(context, ModernCulinaryActivity.class);
        context.startActivity(intent);
    }
} 