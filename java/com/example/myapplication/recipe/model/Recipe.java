package com.example.myapplication.recipe.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Clasa model pentru o rețetă românească.
 */
public class Recipe {
    private int id;
    private String title;
    private String description;
    private String category;
    private String region;
    private String difficulty;
    private int preparationTime; // in minutes
    private int cookingTime; // in minutes
    private int servings;
    private int imageResourceId;
    private boolean favorite;
    private double rating; // rating între 0-5
    private int ratingCount; // numărul de evaluări
    private String history; // istoria sau contextul cultural al rețetei
    private NutritionalInfo nutritionalInfo;
    private List<Ingredient> ingredients;
    private List<String> preparationSteps;

    public Recipe(int id, String title, String description, String category, String region, 
                 String difficulty, int preparationTime, int cookingTime, 
                 int servings, int imageResourceId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.region = region;
        this.difficulty = difficulty;
        this.preparationTime = preparationTime;
        this.cookingTime = cookingTime;
        this.servings = servings;
        this.imageResourceId = imageResourceId;
        this.favorite = false;
        this.ingredients = new ArrayList<>();
        this.preparationSteps = new ArrayList<>();
        this.rating = 0.0;
        this.ratingCount = 0;
    }

    // Getteri și setteri
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
    }

    public int getTotalTime() {
        return preparationTime + cookingTime;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    public String getImageResourceName() {
        return "recipe_" + id; // Return a default image resource name based on id
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public void toggleFavorite() {
        this.favorite = !this.favorite;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public String getFormattedRating() {
        return String.format("%.1f (%d)", rating, ratingCount);
    }

    public void addRating(float newRating) {
        double totalRating = (rating * ratingCount) + newRating;
        ratingCount++;
        rating = totalRating / ratingCount;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public NutritionalInfo getNutritionalInfo() {
        return nutritionalInfo;
    }

    public void setNutritionalInfo(NutritionalInfo nutritionalInfo) {
        this.nutritionalInfo = nutritionalInfo;
    }

    public String getFormattedTime() {
        int hours = getTotalTime() / 60;
        int minutes = getTotalTime() % 60;
        
        if (hours > 0) {
            return String.format("%d ore %02d min", hours, minutes);
        } else {
            return String.format("%d min", minutes);
        }
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
    }

    public List<String> getPreparationSteps() {
        return preparationSteps;
    }

    public void setPreparationSteps(List<String> preparationSteps) {
        this.preparationSteps = preparationSteps;
    }

    public void addPreparationStep(String step) {
        this.preparationSteps.add(step);
    }
} 