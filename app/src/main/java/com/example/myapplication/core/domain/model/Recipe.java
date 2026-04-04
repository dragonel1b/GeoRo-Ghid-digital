package com.example.myapplication.core.domain.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity class representing a recipe
 */
@Entity(tableName = "recipes")
public class Recipe {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @NonNull
    private String name;
    
    private String description;
    private String imageUrl;
    private String region;
    private String category;
    private int prepTimeMinutes;
    private int cookTimeMinutes;
    private String difficulty;
    
    @Ignore
    private List<Ingredient> ingredients;
    
    @Ignore
    private List<Ingredient> missingIngredients;
    
    @Ignore
    private int matchScore;
    
    public Recipe() {
        // Empty constructor for Room
        ingredients = new ArrayList<>();
        missingIngredients = new ArrayList<>();
        matchScore = 0;
    }

    public Recipe(int id, @NonNull String name, String description, String imageUrl, String region,
                 String category, int prepTimeMinutes, int cookTimeMinutes, String difficulty) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.region = region;
        this.category = category;
        this.prepTimeMinutes = prepTimeMinutes;
        this.cookTimeMinutes = cookTimeMinutes;
        this.difficulty = difficulty;
        
        ingredients = new ArrayList<>();
        missingIngredients = new ArrayList<>();
        matchScore = 0;
    }

    // Getters and Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPrepTimeMinutes() {
        return prepTimeMinutes;
    }

    public void setPrepTimeMinutes(int prepTimeMinutes) {
        this.prepTimeMinutes = prepTimeMinutes;
    }

    public int getCookTimeMinutes() {
        return cookTimeMinutes;
    }

    public void setCookTimeMinutes(int cookTimeMinutes) {
        this.cookTimeMinutes = cookTimeMinutes;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<Ingredient> getMissingIngredients() {
        return missingIngredients;
    }

    public void setMissingIngredients(List<Ingredient> missingIngredients) {
        this.missingIngredients = missingIngredients;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }

    /**
     * Calculate match score based on available ingredients
     * @param availableIngredients List of ingredients the user has available
     */
    public void calculateMatchScoreAndMissingIngredients(List<Ingredient> availableIngredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            matchScore = 0;
            return;
        }

        // Clear previously missing ingredients
        missingIngredients.clear();
        
        // Count how many ingredients match
        int matchCount = 0;
        
        for (Ingredient recipeIngredient : ingredients) {
            boolean found = false;
            
            for (Ingredient availableIngredient : availableIngredients) {
                if (recipeIngredient.getId() == availableIngredient.getId() ||
                        recipeIngredient.getName().equalsIgnoreCase(availableIngredient.getName())) {
                    found = true;
                    matchCount++;
                    break;
                }
            }
            
            if (!found) {
                missingIngredients.add(recipeIngredient);
            }
        }
        
        // Calculate percentage match
        matchScore = (int) (((double) matchCount / ingredients.size()) * 100);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return id == recipe.id && 
               Objects.equals(name, recipe.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
} 