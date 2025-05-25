package com.example.myapplication.Joc1.Culinary;

import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity.Recipe;

/**
 * Interface for culinary activities with common functionality
 */
public interface CulinaryActivityInterface {
    /**
     * Initialize views and components
     */
    void initializeViews();
    
    /**
     * Setup recipe data
     */
    void setupRecipes();
    
    /**
     * Setup filter UI components
     */
    void setupFilters();
    
    /**
     * Handle recipe selection
     * @param recipeId ID of the selected recipe
     */
    void onRecipeSelected(long recipeId);
    
    /**
     * Show user profile
     */
    void showUserProfile();
    
    /**
     * Apply filters to recipe list
     */
    void applyFilters();
    
    /**
     * Navigate to search screen
     */
    void navigateToSearch();
} 