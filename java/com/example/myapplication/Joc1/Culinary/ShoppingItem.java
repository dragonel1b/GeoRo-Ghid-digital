package com.example.myapplication.Joc1.Culinary;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for a shopping item in the database
 */
public class ShoppingItem {
    private long id;
    private String name;
    private String quantity;
    private String unit;
    private String recipeSource;
    private boolean checked;
    private String category;
    private List<String> recipeSources;
    private int recipeCount;
    
    /**
     * Constructor for a new shopping item
     * 
     * @param id           Unique ID
     * @param name         Item name/description
     * @param quantity     Quantity value
     * @param unit         Unit of measurement
     * @param recipeSource Source recipe
     * @param checked      Whether item is checked off
     */
    public ShoppingItem(long id, String name, String quantity, String unit, String recipeSource, boolean checked) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.recipeSource = recipeSource;
        this.checked = checked;
        this.category = "";
        this.recipeSources = new ArrayList<>();
        this.recipeCount = 0;
    }
    
    /**
     * Constructor for MealPlanDBHelper compatibility
     * 
     * @param id        Unique ID
     * @param name      Item name/description
     * @param category  Category for grouping items
     * @param quantity  Quantity as a string
     * @param checked   Whether item is checked off
     */
    public ShoppingItem(long id, String name, String category, String quantity, boolean checked) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = "";
        this.recipeSource = "";
        this.checked = checked;
        this.recipeSources = new ArrayList<>();
        this.recipeCount = 0;
    }
    
    /**
     * Simple constructor for items with just name and source
     */
    public ShoppingItem(long id, String name, String recipeSource) {
        this.id = id;
        this.name = name;
        this.quantity = "";
        this.unit = "";
        this.recipeSource = recipeSource;
        this.checked = false;
        this.category = "";
        this.recipeSources = new ArrayList<>();
        this.recipeCount = 0;
    }
    
    /**
     * Constructor with name, source and checked status
     */
    public ShoppingItem(long id, String name, String recipeSource, String unit) {
        this.id = id;
        this.name = name;
        this.quantity = "";
        this.unit = unit;
        this.recipeSource = recipeSource;
        this.checked = false;
        this.category = "";
        this.recipeSources = new ArrayList<>();
        this.recipeCount = 0;
    }
    
    /**
     * Constructor with name, source and checked status
     */
    public ShoppingItem(long id, String name, String recipeSource, boolean checked) {
        this.id = id;
        this.name = name;
        this.quantity = "";
        this.unit = "";
        this.recipeSource = recipeSource;
        this.checked = checked;
        this.category = "";
        this.recipeSources = new ArrayList<>();
        this.recipeCount = 0;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
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
    
    /**
     * Get quantity as a string (for MealPlanDBHelper compatibility)
     */
    public String getQuantityString() {
        if (quantity.isEmpty()) {
            return "";
        }
        return quantity;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public String getRecipeSource() {
        return recipeSource;
    }
    
    public void setRecipeSource(String recipeSource) {
        this.recipeSource = recipeSource;
    }
    
    public boolean isChecked() {
        return checked;
    }
    
    public void setChecked(boolean checked) {
        this.checked = checked;
    }
    
    /**
     * Get category for the item
     * @return The category
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Set category for the item
     * @param category The category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * Get formatted quantity string
     * 
     * @return Formatted quantity with unit
     */
    public String getFormattedQuantity() {
        if (quantity.isEmpty()) {
            return "";
        }
        
        return quantity + " " + unit;
    }
    
    public List<String> getRecipeSourcesList() {
        return recipeSources;
    }
    
    public void addRecipeSource(String source) {
        if (!recipeSources.contains(source)) {
            recipeSources.add(source);
            recipeCount = recipeSources.size();
        }
    }
    
    public int getRecipeCount() {
        return recipeCount;
    }
} 