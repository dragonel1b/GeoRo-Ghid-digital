package com.example.myapplication.Joc1.Culinary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity.Recipe;

/**
 * SQLite Database helper for storing recipes
 */
public class RecipeDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "RecipeDBHelper";
    
    // Database version and name
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "culinary_recipes.db";
    
    // Table names
    private static final String TABLE_RECIPES = "recipes";
    private static final String TABLE_FAVORITES = "favorites";
    private static final String TABLE_COMPLETED = "completed_recipes";
    private static final String TABLE_REVIEWS = "reviews";
    private static final String TABLE_NUTRITIONAL_INFO = "nutritional_info";
    
    // Common column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_RECIPE_ID = "recipe_id";
    private static final String COLUMN_DATE = "date";
    
    // Recipe table columns
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_REGION = "region";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DIFFICULTY = "difficulty";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_IMAGE_URL = "image_url";
    private static final String COLUMN_IMAGE_RESOURCE_ID = "image_resource_id";
    private static final String COLUMN_IS_FAVORITE = "is_favorite";
    private static final String COLUMN_RATING = "rating";
    private static final String COLUMN_INGREDIENTS = "ingredients";
    private static final String COLUMN_STEPS = "steps";
    
    // Review table columns
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_REVIEW_TEXT = "review_text";
    private static final String COLUMN_REVIEW_RATING = "rating";
    
    // NutritionalInfo table columns
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CALORIES = "calories";
    private static final String COLUMN_PROTEIN = "protein";
    private static final String COLUMN_FAT = "fat";
    private static final String COLUMN_CARBS = "carbohydrates";
    private static final String COLUMN_FIBER = "fiber";
    private static final String COLUMN_SUGAR = "sugar";
    private static final String COLUMN_SODIUM = "sodium";
    
    // Create table statements
    private static final String CREATE_TABLE_RECIPES = "CREATE TABLE " + TABLE_RECIPES + "("
        + COLUMN_ID + " INTEGER PRIMARY KEY,"
        + COLUMN_TITLE + " TEXT,"
        + COLUMN_REGION + " TEXT,"
        + COLUMN_CATEGORY + " TEXT,"
        + COLUMN_DESCRIPTION + " TEXT,"
        + COLUMN_DIFFICULTY + " TEXT,"
        + COLUMN_TIME + " TEXT,"
        + COLUMN_IMAGE_URL + " TEXT,"
        + COLUMN_IMAGE_RESOURCE_ID + " INTEGER DEFAULT 0,"
        + COLUMN_IS_FAVORITE + " INTEGER DEFAULT 0,"
        + COLUMN_RATING + " REAL DEFAULT 0,"
        + COLUMN_INGREDIENTS + " TEXT,"
        + COLUMN_STEPS + " TEXT"
            + ")";
    
    private static final String CREATE_TABLE_FAVORITES = "CREATE TABLE " + TABLE_FAVORITES + "("
        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + COLUMN_RECIPE_ID + " INTEGER,"
        + "FOREIGN KEY(" + COLUMN_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COLUMN_ID + ")"
            + ")";
    
    private static final String CREATE_TABLE_COMPLETED = "CREATE TABLE " + TABLE_COMPLETED + "("
        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + COLUMN_RECIPE_ID + " INTEGER,"
        + COLUMN_DATE + " TEXT,"
        + "FOREIGN KEY(" + COLUMN_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COLUMN_ID + ")"
            + ")";
    
    private static final String CREATE_TABLE_REVIEWS = "CREATE TABLE " + TABLE_REVIEWS + "("
        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + COLUMN_RECIPE_ID + " INTEGER,"
        + COLUMN_USER_NAME + " TEXT,"
        + COLUMN_REVIEW_TEXT + " TEXT,"
        + COLUMN_REVIEW_RATING + " REAL,"
        + COLUMN_DATE + " TEXT,"
        + "FOREIGN KEY(" + COLUMN_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COLUMN_ID + ")"
            + ")";
    
    private static final String CREATE_TABLE_NUTRITIONAL_INFO = "CREATE TABLE " + TABLE_NUTRITIONAL_INFO + "("
        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + COLUMN_RECIPE_ID + " INTEGER,"
        + COLUMN_NAME + " TEXT,"
        + COLUMN_CALORIES + " REAL,"
        + COLUMN_PROTEIN + " REAL,"
        + COLUMN_FAT + " REAL,"
        + COLUMN_CARBS + " REAL,"
        + COLUMN_FIBER + " REAL,"
        + COLUMN_SUGAR + " REAL,"
        + COLUMN_SODIUM + " REAL,"
        + "FOREIGN KEY(" + COLUMN_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COLUMN_ID + ")"
            + ")";
    
    /**
     * Constructor
     * 
     * @param context Application context
     */
    public RecipeDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_RECIPES);
        db.execSQL(CREATE_TABLE_FAVORITES);
        db.execSQL(CREATE_TABLE_COMPLETED);
        db.execSQL(CREATE_TABLE_REVIEWS);
        db.execSQL(CREATE_TABLE_NUTRITIONAL_INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NUTRITIONAL_INFO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPLETED);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        
        // Create fresh tables
        onCreate(db);
    }
    
    /**
     * Add a new recipe to the database
     * 
     * @param recipe Recipe to add
     * @return ID of the new recipe
     */
    public long addRecipe(Recipe recipe) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_TITLE, recipe.getTitle());
        values.put(COLUMN_REGION, recipe.getRegion());
        values.put(COLUMN_CATEGORY, recipe.getCategory());
        values.put(COLUMN_DESCRIPTION, recipe.getDescription());
        values.put(COLUMN_DIFFICULTY, recipe.getDifficulty());
        values.put(COLUMN_TIME, recipe.getTime());
        values.put(COLUMN_IMAGE_RESOURCE_ID, recipe.getImageResourceId());
        values.put(COLUMN_IS_FAVORITE, recipe.isFavorite() ? 1 : 0);
        values.put(COLUMN_RATING, recipe.getRating());
        
        // Insert row
        long id = db.insert(TABLE_RECIPES, null, values);
        
        // If insertion was successful and nutritional info is available, save it
        if (id > 0 && recipe.getNutritionalInfo() != null) {
            saveNutritionalInfo(id, recipe.getNutritionalInfo());
        }
        
            db.close();
        return id;
    }
    
    /**
     * Add a user-submitted recipe to the database
     * 
     * @param recipe Recipe to add
     * @param userId User ID of the creator
     * @param userName User name of the creator
     * @param photoPath Path to the recipe photo
     * @return ID of the new recipe
     */
    public long addUserRecipe(Recipe recipe, String userId, String userName, String photoPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_TITLE, recipe.getTitle());
        values.put(COLUMN_REGION, recipe.getRegion());
        values.put(COLUMN_CATEGORY, recipe.getCategory());
        values.put(COLUMN_DESCRIPTION, recipe.getDescription());
        values.put(COLUMN_DIFFICULTY, recipe.getDifficulty());
        values.put(COLUMN_TIME, recipe.getTime());
        values.put(COLUMN_IMAGE_URL, photoPath);
        values.put(COLUMN_IS_FAVORITE, 0); // New recipes start not favorited
        values.put(COLUMN_RATING, 0); // New recipes start with 0 rating
        
        // Join arrays into strings for storage
        if (recipe.getIngredients() != null) {
            values.put(COLUMN_INGREDIENTS, String.join("|", recipe.getIngredients()));
        }
        
        if (recipe.getSteps() != null) {
            values.put(COLUMN_STEPS, String.join("|", recipe.getSteps()));
        }
        
        // Insert row
        long id = db.insert(TABLE_RECIPES, null, values);
        
        // If insertion was successful and nutritional info is available, save it
        if (id > 0 && recipe.getNutritionalInfo() != null) {
            saveNutritionalInfo(id, recipe.getNutritionalInfo());
        }
        
            db.close();
        return id;
    }
    
    /**
     * Get recipe by ID
     * @param recipeId Recipe ID
     * @return Recipe object or null if not found
     */
    public Recipe getRecipeById(long recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_RECIPES,
                null,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(recipeId)},
                null,
                null,
                null
        );

        Recipe recipe = null;
        if (cursor != null && cursor.moveToFirst()) {
            recipe = cursorToRecipe(cursor);
            cursor.close();
        }

        return recipe;
    }

    /**
     * Convert database cursor to Recipe object
     * @param cursor Database cursor
     * @return Recipe object
     */
    private Recipe cursorToRecipe(Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
        String region = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REGION));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
        String difficulty = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIFFICULTY));
        String prepTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME));
        
        // Get ingredients and steps arrays
        String ingredientsStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS));
        String stepsStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STEPS));
        String[] ingredients = ingredientsStr != null ? ingredientsStr.split("\\|") : new String[0];
        String[] steps = stepsStr != null ? stepsStr.split("\\|") : new String[0];

        // Create recipe with required constructor parameters
        Recipe recipe = new Recipe(
            title, region, category, description, difficulty, prepTime, ingredients, steps
        );

        // Set additional properties
        recipe.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        recipe.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_FAVORITE)) == 1);
        recipe.setRating(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_RATING)), 1);
        recipe.setImageResourceId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_RESOURCE_ID)));

        // Load and set nutritional info if available
        NutritionalInfo nutritionalInfo = getNutritionalInfoForRecipe(recipe.getId());
        if (nutritionalInfo != null) {
            recipe.setNutritionalInfo(nutritionalInfo);
        }

        return recipe;
    }

    /**
     * Get all recipes
     * @return List of Recipe objects
     */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_RECIPES,
                null,
                null,
                null,
                null,
                null,
                COLUMN_TITLE + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                recipes.add(cursorToRecipe(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return recipes;
    }

    /**
     * Get recipe by title and region
     * @param title Recipe title
     * @param region Recipe region
     * @return Recipe object or null if not found
     */
    public Recipe getRecipeByTitleAndRegion(String title, String region) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_RECIPES,
                null,
                COLUMN_TITLE + " = ? AND " + COLUMN_REGION + " = ?",
                new String[]{title, region},
                null,
                null,
                null
        );

        Recipe recipe = null;
        if (cursor != null && cursor.moveToFirst()) {
            recipe = cursorToRecipe(cursor);
            cursor.close();
        }

        return recipe;
    }

    /**
     * Get favorite recipes
     * @return List of favorite Recipe objects
     */
    public List<Recipe> getFavoriteRecipes() {
        List<Recipe> favorites = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_RECIPES,
                null,
                COLUMN_IS_FAVORITE + " = 1",
                null,
                null,
                null,
                COLUMN_TITLE + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                favorites.add(cursorToRecipe(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return favorites;
    }

    /**
     * Update recipe favorite status
     * @param recipeId Recipe ID
     * @param isFavorite New favorite status
     * @return true if updated successfully
     */
    public boolean updateFavoriteStatus(long recipeId, boolean isFavorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_FAVORITE, isFavorite ? 1 : 0);

        return db.update(
                TABLE_RECIPES,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(recipeId)}
        ) > 0;
    }

    /**
     * Update recipe
     * @param recipe Recipe to update
     * @return true if updated successfully
     */
    public boolean updateRecipe(Recipe recipe) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, recipe.getTitle());
        values.put(COLUMN_REGION, recipe.getRegion());
        values.put(COLUMN_CATEGORY, recipe.getCategory());
        values.put(COLUMN_DESCRIPTION, recipe.getDescription());
        values.put(COLUMN_DIFFICULTY, recipe.getDifficulty());
        values.put(COLUMN_TIME, recipe.getTime());
        values.put(COLUMN_IMAGE_RESOURCE_ID, recipe.getImageResourceId());
        values.put(COLUMN_IS_FAVORITE, recipe.isFavorite() ? 1 : 0);
        values.put(COLUMN_RATING, recipe.getRating());

        return db.update(
                TABLE_RECIPES,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(recipe.getId())}
        ) > 0;
    }

    
    /**
     * Get recipe ID by title and region
     * 
     * @param title Recipe title
     * @param region Recipe region
     * @return Recipe ID or -1 if not found
     */
    public long getRecipeId(String title, String region) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(
            TABLE_RECIPES,
            new String[] { COLUMN_ID },
            COLUMN_TITLE + "=? AND " + COLUMN_REGION + "=?",
            new String[] { title, region },
            null, null, null, null
        );
        
        long id = -1;
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
        cursor.close();
        }
        
        db.close();
        return id;
    }
    
    /**
     * Update an existing recipe
     * 
     * @param recipe Recipe to update
     * @param nutritionalInfo Nutritional information for the recipe
     * @return Number of rows affected
     */
    public int updateRecipe(Recipe recipe, NutritionalInfo nutritionalInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
        
        values.put(COLUMN_TITLE, recipe.getTitle());
        values.put(COLUMN_REGION, recipe.getRegion());
        values.put(COLUMN_CATEGORY, recipe.getCategory());
        values.put(COLUMN_DESCRIPTION, recipe.getDescription());
        values.put(COLUMN_DIFFICULTY, recipe.getDifficulty());
        values.put(COLUMN_TIME, recipe.getTime());
        values.put(COLUMN_IMAGE_RESOURCE_ID, recipe.getImageResourceId());
        values.put(COLUMN_IS_FAVORITE, recipe.isFavorite() ? 1 : 0);
        values.put(COLUMN_RATING, recipe.getRating());
        
        int rowsAffected = db.update(
            TABLE_RECIPES,
            values,
            COLUMN_ID + " = ?",
            new String[] { String.valueOf(recipe.getId()) }
        );
        
        // Update nutritional info if available
        if (nutritionalInfo != null) {
            updateNutritionalInfo(recipe.getId(), nutritionalInfo);
        }
        
        db.close();
        return rowsAffected;
    }
    
    /**
     * Delete a recipe
     * 
     * @param recipeId ID of recipe to delete
     */
    public void deleteRecipe(long recipeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Delete from nutritional info
        db.delete(
            TABLE_NUTRITIONAL_INFO,
            COLUMN_RECIPE_ID + " = ?",
            new String[] { String.valueOf(recipeId) }
        );
        
        // Delete from reviews
        db.delete(
            TABLE_REVIEWS,
            COLUMN_RECIPE_ID + " = ?",
            new String[] { String.valueOf(recipeId) }
        );
        
        // Delete from favorites
        db.delete(
            TABLE_FAVORITES,
            COLUMN_RECIPE_ID + " = ?",
            new String[] { String.valueOf(recipeId) }
        );
        
        // Delete from completed
        db.delete(
            TABLE_COMPLETED,
            COLUMN_RECIPE_ID + " = ?",
            new String[] { String.valueOf(recipeId) }
        );
        
        // Delete recipe
        db.delete(
            TABLE_RECIPES,
            COLUMN_ID + " = ?",
            new String[] { String.valueOf(recipeId) }
        );
        
        db.close();
    }
    
    /**
     * Update favorite status for a recipe by title and region
     * 
     * @param title Recipe title
     * @param region Recipe region
     * @param isFavorite New favorite status
     * @return Number of rows affected
     */
    public int updateFavoriteStatus(String title, String region, boolean isFavorite) {
        long recipeId = getRecipeId(title, region);
        if (recipeId == -1) {
            return 0;
        }
        return updateFavoriteStatus(recipeId, isFavorite) ? 1 : 0;
    }
    
    /**
     * Add a review for a recipe
     * 
     * @param recipeId ID of the recipe
     * @param userName Name of the reviewer
     * @param reviewText Review text
     * @param rating Rating (1-5)
     * @return ID of the new review
     */
    public long addReview(long recipeId, String userName, String reviewText, float rating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_RECIPE_ID, recipeId);
        values.put(COLUMN_USER_NAME, userName);
        values.put(COLUMN_REVIEW_TEXT, reviewText);
        values.put(COLUMN_REVIEW_RATING, rating);
        values.put(COLUMN_DATE, System.currentTimeMillis());
        
        // Insert review
        long reviewId = db.insert(TABLE_REVIEWS, null, values);
        
        // Update recipe rating
        updateRecipeRating(recipeId);
        
        db.close();
        return reviewId;
    }
    
    /**
     * Get all reviews for a recipe
     * 
     * @param recipeId Recipe ID
     * @return List of reviews
     */
    public List<Review> getReviewsForRecipe(long recipeId) {
        List<Review> reviews = new ArrayList<>();
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
            TABLE_REVIEWS,
            null,
            COLUMN_RECIPE_ID + "=?",
            new String[] { String.valueOf(recipeId) },
            null, null, COLUMN_DATE + " DESC"
        );
        
        if (cursor.moveToFirst()) {
            do {
                Review review = new Review(
                    cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getLong(cursor.getColumnIndex(COLUMN_RECIPE_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_REVIEW_TEXT)),
                    cursor.getFloat(cursor.getColumnIndex(COLUMN_REVIEW_RATING)),
                    cursor.getLong(cursor.getColumnIndex(COLUMN_DATE))
                );
                reviews.add(review);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return reviews;
    }
    
    /**
     * Update the recipe rating based on all reviews
     * 
     * @param recipeId Recipe ID
     */
    private void updateRecipeRating(long recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT AVG(" + COLUMN_REVIEW_RATING + ") as avg_rating, COUNT(*) as count FROM " +
            TABLE_REVIEWS + " WHERE " + COLUMN_RECIPE_ID + "=?",
            new String[] { String.valueOf(recipeId) }
        );
        
        if (cursor.moveToFirst()) {
            float avgRating = cursor.getFloat(cursor.getColumnIndex("avg_rating"));
            
            // Update recipe rating
            ContentValues values = new ContentValues();
            values.put(COLUMN_RATING, avgRating);
            
            db.update(
                TABLE_RECIPES,
                values,
                COLUMN_ID + "=?",
                new String[] { String.valueOf(recipeId) }
            );
        }
        
        cursor.close();
        db.close();
    }
    
    /**
     * Save nutritional information for a recipe
     * 
     * @param recipeId Recipe ID
     * @param info Nutritional information
     * @return ID of the nutritional info record
     */
    public long saveNutritionalInfo(long recipeId, NutritionalInfo info) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_RECIPE_ID, recipeId);
        values.put(COLUMN_NAME, info.getLabel());
        values.put(COLUMN_CALORIES, info.getCalories());
        values.put(COLUMN_PROTEIN, info.getProtein());
        values.put(COLUMN_FAT, info.getFat());
        values.put(COLUMN_CARBS, info.getCarbs());
        values.put(COLUMN_FIBER, info.getFiber());
        values.put(COLUMN_SUGAR, info.getSugar());
        values.put(COLUMN_SODIUM, info.getSodium());
        
        // Check if info already exists
        Cursor cursor = db.query(
            TABLE_NUTRITIONAL_INFO,
            new String[] { COLUMN_ID },
            COLUMN_RECIPE_ID + "=?",
            new String[] { String.valueOf(recipeId) },
            null, null, null
        );
        
        long infoId;
        
        if (cursor.moveToFirst()) {
            // Update existing record
            infoId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
            
            db.update(
                TABLE_NUTRITIONAL_INFO,
                values,
                COLUMN_ID + "=?",
                new String[] { String.valueOf(infoId) }
            );
        } else {
            // Insert new record
            infoId = db.insert(TABLE_NUTRITIONAL_INFO, null, values);
        }
        
        cursor.close();
        db.close();
        
        return infoId;
    }
    
    /**
     * Update nutritional information for a recipe
     * 
     * @param recipeId Recipe ID
     * @param info Nutritional information
     * @return Number of rows affected
     */
    public int updateNutritionalInfo(long recipeId, NutritionalInfo info) {
        return (int) saveNutritionalInfo(recipeId, info);
    }
    
    /**
     * Update nutritional information for a recipe by title and region
     * 
     * @param title Recipe title
     * @param region Recipe region
     * @param info Nutritional information
     * @return Number of rows affected
     */
    public int updateNutritionalInfo(String title, String region, NutritionalInfo info) {
        long recipeId = getRecipeId(title, region);
        if (recipeId == -1) {
            return 0;
        }
        return updateNutritionalInfo(recipeId, info);
    }
    
    /**
     * Get nutritional information for a recipe
     * 
     * @param recipeId Recipe ID
     * @return Nutritional information or null if not found
     */
    public NutritionalInfo getNutritionalInfoForRecipe(long recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
            TABLE_NUTRITIONAL_INFO,
                null,
            COLUMN_RECIPE_ID + "=?",
            new String[] { String.valueOf(recipeId) },
            null, null, null
        );
        
        NutritionalInfo info = null;
        
        if (cursor.moveToFirst()) {
            info = new NutritionalInfo(
                cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                (int)cursor.getFloat(cursor.getColumnIndex(COLUMN_CALORIES)),
                cursor.getFloat(cursor.getColumnIndex(COLUMN_PROTEIN)),
                cursor.getFloat(cursor.getColumnIndex(COLUMN_FAT)),
                cursor.getFloat(cursor.getColumnIndex(COLUMN_CARBS)),
                cursor.getFloat(cursor.getColumnIndex(COLUMN_FIBER)),
                cursor.getFloat(cursor.getColumnIndex(COLUMN_SUGAR)),
                cursor.getFloat(cursor.getColumnIndex(COLUMN_SODIUM))
            );
        }
        
        cursor.close();
        db.close();
        
        return info;
    }
    
    /**
     * Review class to represent recipe reviews
     */
    public static class Review {
        private final long id;
        private final long recipeId;
        private final String userName;
        private final String reviewText;
        private final float rating;
        private final long date;
        
        public Review(long id, long recipeId, String userName, String reviewText, float rating, long date) {
            this.id = id;
            this.recipeId = recipeId;
            this.userName = userName;
            this.reviewText = reviewText;
            this.rating = rating;
            this.date = date;
        }
        
        public long getId() {
            return id;
        }
        
        public long getRecipeId() {
            return recipeId;
        }
        
        public String getUserName() {
            return userName;
        }
        
        public String getReviewText() {
            return reviewText;
        }
        
        public float getRating() {
            return rating;
        }
        
        public long getDate() {
            return date;
        }
    }
    
    /**
     * Get recipe by ID as a ModernCulinaryActivity.Recipe object
     * 
     * @param recipeId Recipe ID
     * @return ModernCulinaryActivity.Recipe object or null if not found
     */
    public Recipe getModernRecipeById(long recipeId) {
        return getRecipeById(recipeId); // Since Recipe is now from ModernCulinaryActivity
    }
}
