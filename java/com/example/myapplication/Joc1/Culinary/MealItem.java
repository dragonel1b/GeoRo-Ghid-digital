package com.example.myapplication.Joc1.Culinary;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class representing a meal item in a meal plan
 */
public class MealItem implements Serializable {
    private final long id;
    private final ModernCulinaryActivity.Recipe recipe;
    private final String mealType; // "breakfast", "lunch", "dinner"
    private final Date date;
    private final int servings;
    private String notes;
    
    // Meal types
    public static final String BREAKFAST = "Mic dejun";
    public static final String LUNCH = "Prânz";
    public static final String DINNER = "Cină";
    public static final String SNACK = "Gustare";
    
    /**
     * Construct a meal item
     * 
     * @param id Unique ID for this meal item
     * @param recipe The recipe for this meal
     * @param mealType Type of meal (breakfast, lunch, dinner)
     * @param date The date this meal is scheduled for
     * @param servings Number of servings
     */
    public MealItem(long id, ModernCulinaryActivity.Recipe recipe, String mealType, Date date, int servings) {
        this.id = id;
        this.recipe = recipe;
        this.mealType = mealType;
        this.date = date;
        this.servings = servings;
        this.notes = "";
    }
    
    /**
     * Construct a meal item with notes
     * 
     * @param id Unique ID for this meal item
     * @param recipe The recipe for this meal
     * @param mealType Type of meal (breakfast, lunch, dinner)
     * @param date The date this meal is scheduled for
     * @param servings Number of servings
     * @param notes Additional notes
     */
    public MealItem(long id, ModernCulinaryActivity.Recipe recipe, String mealType, Date date, int servings, String notes) {
        this.id = id;
        this.recipe = recipe;
        this.mealType = mealType;
        this.date = date;
        this.servings = servings;
        this.notes = notes;
    }
    
    /**
     * Get the total calorie count for this meal item (recipe calories * servings)
     */
    public float getTotalCalories() {
        if (recipe != null && recipe.hasNutritionalInfo()) {
            return recipe.getNutritionalInfo().getCalories() * servings;
        }
        return 0;
    }
    
    /**
     * Get the total protein count for this meal item (recipe protein * servings)
     */
    public float getTotalProtein() {
        if (recipe != null && recipe.hasNutritionalInfo()) {
            return recipe.getNutritionalInfo().getProtein() * servings;
        }
        return 0;
    }
    
    /**
     * Get the total carbs count for this meal item (recipe carbs * servings)
     */
    public float getTotalCarbs() {
        if (recipe != null && recipe.hasNutritionalInfo()) {
            return recipe.getNutritionalInfo().getCarbs() * servings;
        }
        return 0;
    }
    
    /**
     * Get the total fat count for this meal item (recipe fat * servings)
     */
    public float getTotalFat() {
        if (recipe != null && recipe.hasNutritionalInfo()) {
            return recipe.getNutritionalInfo().getFat() * servings;
        }
        return 0;
    }
    
    /**
     * Get a formatted string with the meal's nutritional information
     */
    public String getFormattedNutrition() {
        if (recipe != null && recipe.hasNutritionalInfo()) {
            NutritionalInfo info = recipe.getNutritionalInfo();
            return String.format("%.0f kcal | P: %.1fg | C: %.1fg | F: %.1fg", 
                    info.getCalories() * servings, 
                    info.getProtein() * servings, 
                    info.getCarbs() * servings, 
                    info.getFat() * servings);
        }
        return "Informații nutriționale indisponibile";
    }
    
    // Getters and setters
    public long getId() {
        return id;
    }
    
    public ModernCulinaryActivity.Recipe getRecipe() {
        return recipe;
    }
    
    public String getMealType() {
        return mealType;
    }
    
    public Date getDate() {
        return date;
    }
    
    public int getServings() {
        return servings;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    /**
     * Get the formatted meal type name in Romanian
     */
    public String getFormattedMealType() {
        switch (mealType) {
            case "breakfast":
                return "Mic dejun";
            case "lunch":
                return "Prânz";
            case "dinner":
                return "Cină";
            default:
                return mealType;
        }
    }
    
    /**
     * Get the first letter of the meal type
     */
    public String getMealTypeLetter() {
        switch (mealType) {
            case "breakfast":
                return "M";
            case "lunch":
                return "P";
            case "dinner":
                return "C";
            default:
                return mealType.substring(0, 1).toUpperCase();
        }
    }
} 