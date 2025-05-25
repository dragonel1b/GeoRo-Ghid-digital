package com.example.myapplication.Joc1.Culinary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Database helper for shopping list feature
 */
public class ShoppingListDBHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "ShoppingList.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table name
    private static final String TABLE_SHOPPING_LIST = "shopping_list";
    
    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_INGREDIENT = "ingredient";
    private static final String COLUMN_RECIPE_SOURCE = "recipe_source";
    private static final String COLUMN_CHECKED = "checked";
    
    // Create table statement
    private static final String CREATE_TABLE_SHOPPING_LIST = 
            "CREATE TABLE " + TABLE_SHOPPING_LIST + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_INGREDIENT + " TEXT NOT NULL, " +
                    COLUMN_RECIPE_SOURCE + " TEXT, " +
                    COLUMN_CHECKED + " INTEGER DEFAULT 0)";
    
    public ShoppingListDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SHOPPING_LIST);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if it exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOPPING_LIST);
        
        // Create tables again
        onCreate(db);
    }
    
    /**
     * Add a new item to the shopping list
     * @param item The shopping list item to add
     * @return The row ID of the newly inserted item, or -1 if an error occurred
     */
    public long addItem(ShoppingItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_INGREDIENT, item.getName());
        values.put(COLUMN_RECIPE_SOURCE, item.getRecipeSource());
        values.put(COLUMN_CHECKED, item.isChecked() ? 1 : 0);
        
        // Insert row
        long id = db.insert(TABLE_SHOPPING_LIST, null, values);
        db.close();
        
        return id;
    }
    
    /**
     * Get all shopping list items
     * @return List of shopping list items
     */
    public List<ShoppingItem> getAllItems() {
        List<ShoppingItem> items = new ArrayList<>();
        
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_SHOPPING_LIST + " ORDER BY " + COLUMN_CHECKED + " ASC";
        
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        // Looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                String ingredient = cursor.getString(cursor.getColumnIndex(COLUMN_INGREDIENT));
                String recipeSource = cursor.getString(cursor.getColumnIndex(COLUMN_RECIPE_SOURCE));
                boolean checked = cursor.getInt(cursor.getColumnIndex(COLUMN_CHECKED)) == 1;
                
                // Create ShoppingItem with empty string for quantity
                ShoppingItem item = new ShoppingItem(id, ingredient, "", "", recipeSource, checked);
                items.add(item);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return items;
    }
    
    /**
     * Update a shopping list item
     * @param item The shopping list item to update
     * @return The number of rows affected
     */
    public int updateItem(ShoppingItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_INGREDIENT, item.getName());
        values.put(COLUMN_RECIPE_SOURCE, item.getRecipeSource());
        values.put(COLUMN_CHECKED, item.isChecked() ? 1 : 0);
        
        // Updating row
        int rowsAffected = db.update(TABLE_SHOPPING_LIST, values, 
                COLUMN_ID + " = ?", new String[]{String.valueOf(item.getId())});
        
        db.close();
        
        return rowsAffected;
    }
    
    /**
     * Delete a shopping list item
     * @param id The ID of the item to delete
     */
    public void deleteItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SHOPPING_LIST, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
    
    /**
     * Clear all items from the shopping list
     */
    public void clearAllItems() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SHOPPING_LIST, null, null);
        db.close();
    }
} 