package com.example.myapplication.recipe.model;

/**
 * Clasa model pentru informațiile nutriționale ale unei rețete.
 */
public class NutritionalInfo {
    private int calories;        // calorii per porție
    private float protein;       // proteine în grame
    private float carbs;         // carbohidrați în grame
    private float fat;           // grăsimi în grame
    private float fiber;         // fibre în grame
    private float sugar;         // zahăr în grame
    private float sodium;        // sodiu în mg

    public NutritionalInfo() {
    }

    public NutritionalInfo(int calories, float protein, float carbs, float fat) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    public NutritionalInfo(int calories, float protein, float carbs, float fat, float fiber, float sugar, float sodium) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.fiber = fiber;
        this.sugar = sugar;
        this.sodium = sodium;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public float getProtein() {
        return protein;
    }

    public void setProtein(float protein) {
        this.protein = protein;
    }

    public float getCarbs() {
        return carbs;
    }

    public void setCarbs(float carbs) {
        this.carbs = carbs;
    }

    public float getFat() {
        return fat;
    }

    public void setFat(float fat) {
        this.fat = fat;
    }

    public float getFiber() {
        return fiber;
    }

    public void setFiber(float fiber) {
        this.fiber = fiber;
    }

    public float getSugar() {
        return sugar;
    }

    public void setSugar(float sugar) {
        this.sugar = sugar;
    }

    public float getSodium() {
        return sodium;
    }

    public void setSodium(float sodium) {
        this.sodium = sodium;
    }
} 