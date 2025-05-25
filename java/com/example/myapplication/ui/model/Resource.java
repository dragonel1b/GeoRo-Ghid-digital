package com.example.myapplication.ui.model;

/**
 * Model class representing a game resource like fuel, money, food, etc.
 */
public class Resource {
    private String id;
    private String name;
    private int amount;
    private int iconResId;
    private int colorTint;
    
    public Resource(String id, String name, int amount, int iconResId, int colorTint) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.iconResId = iconResId;
        this.colorTint = colorTint;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }
    
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    public void addAmount(int add) {
        this.amount += add;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getColorTint() {
        return colorTint;
    }
} 