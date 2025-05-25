package com.example.myapplication.Joc1.Culinary;

/**
 * Class representing nutritional information for a recipe
 */
public class NutritionalInfo {
    private String label;
    private int calories;
    private float protein;
    private float carbs;
    private float fat;
    private float fiber;
    private float sugar;
    private float sodium;

    public NutritionalInfo() {
        this("", 0, 0, 0, 0, 0, 0, 0);
    }

    public NutritionalInfo(int calories, float protein, float carbs, float fat, float fiber) {
        this("", calories, protein, carbs, fat, fiber, 0, 0);
    }

    public NutritionalInfo(int calories, float protein, float fat, float carbs) {
        this("", calories, protein, carbs, fat, 0, 0, 0);
    }

    public NutritionalInfo(String label, int calories, float protein, float carbs, float fat, float fiber, float sugar, float sodium) {
        this.label = label;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.fiber = fiber;
        this.sugar = sugar;
        this.sodium = sodium;
    }

    // Static factory method for creating from float calories
    public static NutritionalInfo fromFloatCalories(float calories, float protein, float carbs, float fat, float fiber, float sugar, float sodium) {
        return new NutritionalInfo("", (int)calories, protein, carbs, fat, fiber, sugar, sodium);
    }
    
    public String getLabel() { return label; }
    public int getCalories() { return calories; }
    public float getProtein() { return protein; }
    public float getCarbs() { return carbs; }
    public float getFat() { return fat; }
    public float getFiber() { return fiber; }
    public float getSugar() { return sugar; }
    public float getSodium() { return sodium; }
    public float getCarbohydrates() { return carbs; }

    public String getFormattedCalories() {
        return String.format("%d kcal", calories);
    }

    public boolean hasNutritionalInformation() {
        return calories > 0 || protein > 0 || carbs > 0 || fat > 0;
    }

    public boolean isComplete() {
        return calories > 0 && protein >= 0 && carbs >= 0 && fat >= 0;
    }

    public NutritionalInfo add(NutritionalInfo other) {
        return new NutritionalInfo(
            "Total",
            this.calories + other.calories,
            this.protein + other.protein,
            this.carbs + other.carbs,
            this.fat + other.fat,
            this.fiber + other.fiber,
            this.sugar + other.sugar,
            this.sodium + other.sodium
        );
    }

    public NutritionalInfo scale(int factor) {
        return new NutritionalInfo(
            this.label,
            this.calories * factor,
            this.protein * factor,
            this.carbs * factor,
            this.fat * factor,
            this.fiber * factor,
            this.sugar * factor,
            this.sodium * factor
        );
    }
    
    @Override
    public String toString() {
        return String.format(
            "Calorii: %d kcal\nProteine: %.1f g\nGrăsimi: %.1f g\nCarbohidrați: %.1f g",
            calories, protein, fat, carbs
        );
    }
}
