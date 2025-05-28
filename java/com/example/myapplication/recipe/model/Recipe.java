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
    // Noi câmpuri pentru contribuții utilizatori și funcții sociale
    private String authorId; // ID-ul utilizatorului care a creat rețeta
    private String authorName; // Numele utilizatorului care a creat rețeta
    private List<String> comments; // Lista de comentarii
    private boolean isUserContributed; // Indicator dacă rețeta este contribuită de utilizator
    private int shareCount; // Numărul de partajări
    // Restricții alimentare
    private boolean isVegetarian;
    private boolean isVegan;
    private boolean isGlutenFree;
    private boolean isLactoseFree;
    private List<String> allergens; // Lista de alergeni

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
        this.comments = new ArrayList<>();
        this.allergens = new ArrayList<>();
        this.isUserContributed = false;
        this.shareCount = 0;
        this.isVegetarian = false;
        this.isVegan = false;
        this.isGlutenFree = false;
        this.isLactoseFree = false;
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

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public void addComment(String comment) {
        this.comments.add(comment);
    }

    public boolean isUserContributed() {
        return isUserContributed;
    }

    public void setUserContributed(boolean userContributed) {
        isUserContributed = userContributed;
    }

    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public void incrementShareCount() {
        this.shareCount++;
    }

    public boolean isVegetarian() {
        return isVegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        isVegetarian = vegetarian;
    }

    public boolean isVegan() {
        return isVegan;
    }

    public void setVegan(boolean vegan) {
        isVegan = vegan;
    }

    public boolean isGlutenFree() {
        return isGlutenFree;
    }

    public void setGlutenFree(boolean glutenFree) {
        isGlutenFree = glutenFree;
    }

    public boolean isLactoseFree() {
        return isLactoseFree;
    }

    public void setLactoseFree(boolean lactoseFree) {
        isLactoseFree = lactoseFree;
    }

    public List<String> getAllergens() {
        return allergens;
    }

    public void setAllergens(List<String> allergens) {
        this.allergens = allergens;
    }

    public void addAllergen(String allergen) {
        this.allergens.add(allergen);
    }

    // Metodă pentru a scala ingredientele în funcție de numărul de porții
    public List<Ingredient> getScaledIngredients(int targetServings) {
        if (targetServings <= 0 || targetServings == this.servings) {
            return this.ingredients;
        }

        List<Ingredient> scaledIngredients = new ArrayList<>();
        double scaleFactor = (double) targetServings / this.servings;

        for (Ingredient ingredient : this.ingredients) {
            try {
                // Convert string quantity to double for scaling
                double originalQuantity = Double.parseDouble(ingredient.getQuantity());
                double scaledQuantity = originalQuantity * scaleFactor;
                
                // Format back to string with proper decimal places
                String formattedQuantity;
                if (scaledQuantity == (int) scaledQuantity) {
                    formattedQuantity = String.valueOf((int) scaledQuantity);
                } else {
                    formattedQuantity = String.format("%.1f", scaledQuantity).replace(",", ".");
                }
                
                Ingredient scaledIngredient = new Ingredient(
                    ingredient.getName(),
                    formattedQuantity,
                    ingredient.getUnit()
                );
                scaledIngredients.add(scaledIngredient);
            } catch (NumberFormatException e) {
                // For non-numeric quantities (like "1/2" or "un praf"), just keep original
                scaledIngredients.add(new Ingredient(
                    ingredient.getName(),
                    ingredient.getQuantity(),
                    ingredient.getUnit()
                ));
            }
        }

        return scaledIngredients;
    }
} 