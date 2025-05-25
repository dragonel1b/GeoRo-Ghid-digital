package com.example.myapplication.recipe.model;

/**
 * Clasa model pentru un ingredient al unei rețete românești.
 */
public class Ingredient {
    private String name;
    private String quantity;
    private String unit; // ex: grame, linguri, ml, etc.
    private boolean checked;

    public Ingredient() {
    }

    public Ingredient(String name, String quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.checked = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getFullDescription() {
        return quantity + " " + unit + " " + name;
    }

    /**
     * Returnează o reprezentare formatată a ingredientului.
     * De exemplu: "200 g făină (cernut în prealabil)"
     */
    public String getFormattedString() {
        StringBuilder formattedIngredient = new StringBuilder();
        
        formattedIngredient.append(quantity).append(" ").append(unit).append(" ").append(name);
        
        return formattedIngredient.toString();
    }

    @Override
    public String toString() {
        return getFormattedString();
    }
} 