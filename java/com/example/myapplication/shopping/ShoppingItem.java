package com.example.myapplication.shopping;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity class for shopping items
 */
@Entity(tableName = "shopping_items")
public class ShoppingItem {

    @PrimaryKey
    private final long id;
    
    @NonNull
    private String name;
    
    private String category;
    
    private float quantity;
    
    @NonNull
    private String unit;
    
    private boolean checked;
    
    private String imageUri;

    public ShoppingItem(long id, @NonNull String name, String category, float quantity, @NonNull String unit) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.checked = false;
    }

    public long getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @Nullable
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    @NonNull
    public String getUnit() {
        return unit;
    }

    public void setUnit(@NonNull String unit) {
        this.unit = unit;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Nullable
    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
    
    @NonNull
    public String getDisplayText() {
        return String.format("%s (%.1f %s)", name, quantity, unit);
    }
    
    @Override
    @NonNull
    public String toString() {
        return "ShoppingItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", checked=" + checked +
                ", imageUri='" + imageUri + '\'' +
                '}';
    }
} 