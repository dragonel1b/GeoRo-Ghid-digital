package com.example.myapplication.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * Entity class representing an ingredient
 */
@Entity(tableName = "ingredients")
public class Ingredient {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @NonNull
    private String name;
    
    private String category;
    private String unitOfMeasure;
    private double defaultQuantity;
    
    public Ingredient() {
        // Empty constructor for Room
    }

    public Ingredient(int id, @NonNull String name, String category, String unitOfMeasure, double defaultQuantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.unitOfMeasure = unitOfMeasure;
        this.defaultQuantity = defaultQuantity;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public double getDefaultQuantity() {
        return defaultQuantity;
    }

    public void setDefaultQuantity(double defaultQuantity) {
        this.defaultQuantity = defaultQuantity;
    }
    
    // For dropdown display
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return id == that.id && 
               Objects.equals(name, that.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
} 