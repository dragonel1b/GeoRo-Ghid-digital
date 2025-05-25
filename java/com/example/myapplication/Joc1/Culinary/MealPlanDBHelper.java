package com.example.myapplication.Joc1.Culinary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity.Recipe;
import com.example.myapplication.Joc1.Culinary.MealItem;
import com.example.myapplication.Joc1.Culinary.ShoppingItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * SQLite Database helper for storing meal plans
 */
public class MealPlanDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "MealPlanDBHelper";
    
    // Database version and name
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "meal_planner.db";
    
    // Store context reference
    private final Context mContext;
    
    // Table names
    private static final String TABLE_MEAL_PLANS = "meal_plans";
    private static final String TABLE_MEALS = "meals";
    private static final String TABLE_SHOPPING_ITEMS = "shopping_items";
    
    // Common column names
    private static final String COLUMN_ID = "id";
    
    // Meal plan table columns
    private static final String COLUMN_START_DATE = "start_date";
    private static final String COLUMN_END_DATE = "end_date";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_IS_ACTIVE = "is_active";
    
    // Meal items table columns
    private static final String COLUMN_PLAN_ID = "plan_id";
    private static final String COLUMN_DAY = "day";
    private static final String COLUMN_MEAL_TYPE = "meal_type";
    private static final String COLUMN_RECIPE_ID = "recipe_id";
    private static final String COLUMN_NOTES = "notes";
    
    // Meal columns
    private static final String COLUMN_MEAL_DATE = "meal_date";
    
    // Shopping item columns
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_CHECKED = "checked";
    
    // Date format for storage
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    // Create table statements
    private static final String CREATE_TABLE_MEAL_PLANS = "CREATE TABLE " + TABLE_MEAL_PLANS + "("
        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + COLUMN_START_DATE + " TEXT,"
        + COLUMN_END_DATE + " TEXT,"
        + COLUMN_TITLE + " TEXT,"
        + COLUMN_IS_ACTIVE + " INTEGER"
        + ")";
    
    private static final String CREATE_TABLE_MEALS = "CREATE TABLE " + TABLE_MEALS + "("
        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + COLUMN_PLAN_ID + " INTEGER,"
        + COLUMN_RECIPE_ID + " INTEGER,"
        + COLUMN_MEAL_TYPE + " TEXT,"
        + COLUMN_MEAL_DATE + " TEXT,"
        + "FOREIGN KEY(" + COLUMN_PLAN_ID + ") REFERENCES " + TABLE_MEAL_PLANS + "(" + COLUMN_ID + ")"
        + ")";
    
    private static final String CREATE_TABLE_SHOPPING_ITEMS = "CREATE TABLE " + TABLE_SHOPPING_ITEMS + "("
        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
        + COLUMN_PLAN_ID + " INTEGER,"
        + COLUMN_NAME + " TEXT,"
        + COLUMN_CATEGORY + " TEXT,"
        + COLUMN_QUANTITY + " TEXT,"
        + COLUMN_CHECKED + " INTEGER DEFAULT 0,"
        + "FOREIGN KEY(" + COLUMN_PLAN_ID + ") REFERENCES " + TABLE_MEAL_PLANS + "(" + COLUMN_ID + ")"
        + ")";
    
    /**
     * Constructor
     * 
     * @param context Application context
     */
    public MealPlanDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MEAL_PLANS);
        db.execSQL(CREATE_TABLE_MEALS);
        db.execSQL(CREATE_TABLE_SHOPPING_ITEMS);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOPPING_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEAL_PLANS);
        
        // Create fresh tables
        onCreate(db);
    }
    
    /**
     * Add a new meal plan
     * 
     * @param startDate Start date of the plan
     * @param endDate End date of the plan
     * @param title Title of the plan
     * @param isActive Whether this plan is currently active
     * @return ID of the new plan
     */
    public long addMealPlan(Date startDate, Date endDate, String title, boolean isActive) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_START_DATE, DATE_FORMAT.format(startDate));
        values.put(COLUMN_END_DATE, DATE_FORMAT.format(endDate));
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_IS_ACTIVE, isActive ? 1 : 0);
        
        // If this plan is active, deactivate other plans
        if (isActive) {
            ContentValues deactivateValues = new ContentValues();
            deactivateValues.put(COLUMN_IS_ACTIVE, 0);
            db.update(TABLE_MEAL_PLANS, deactivateValues, COLUMN_IS_ACTIVE + "=?", new String[]{"1"});
        }
        
        // Insert row
        long id = db.insert(TABLE_MEAL_PLANS, null, values);
        db.close();
        
        return id;
    }
    
    /**
     * Add a meal item to a plan
     * 
     * @param planId ID of the parent meal plan
     * @param day Day of the week (0 = Sunday, 1 = Monday, etc.)
     * @param mealType Type of meal (breakfast, lunch, dinner, snack)
     * @param recipeId ID of the recipe
     * @param notes Additional notes
     * @return ID of the new meal item
     */
    public long addMealItem(long planId, int day, String mealType, long recipeId, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_PLAN_ID, planId);
        values.put(COLUMN_DAY, day);
        values.put(COLUMN_MEAL_TYPE, mealType);
        values.put(COLUMN_RECIPE_ID, recipeId);
        values.put(COLUMN_NOTES, notes);
        
        // Insert row
        long id = db.insert(TABLE_MEALS, null, values);
        db.close();
        
        return id;
    }
    
    /**
     * Get a meal plan by ID
     * 
     * @param planId ID of the meal plan
     * @return MealPlan object
     */
    public MealPlan getMealPlan(long planId) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(
            TABLE_MEAL_PLANS,
            new String[] {
                COLUMN_ID, COLUMN_START_DATE, COLUMN_END_DATE, COLUMN_TITLE, COLUMN_IS_ACTIVE
            },
            COLUMN_ID + "=?",
            new String[] { String.valueOf(planId) },
            null, null, null, null
        );
        
        MealPlan mealPlan = null;
        
        if (cursor != null && cursor.moveToFirst()) {
            try {
                Date startDate = DATE_FORMAT.parse(cursor.getString(cursor.getColumnIndex(COLUMN_START_DATE)));
                Date endDate = DATE_FORMAT.parse(cursor.getString(cursor.getColumnIndex(COLUMN_END_DATE)));
                
                mealPlan = new MealPlan(
                    cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                    startDate,
                    endDate,
                    cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_IS_ACTIVE)) == 1
                );
                
                // Populate meal items
                List<MealPlan.MealItem> mealItems = getMealItemsForPlan(planId);
                mealPlan.setMealItems(mealItems);
                
            } catch (ParseException e) {
                e.printStackTrace();
            }
            
            cursor.close();
        }
        
        db.close();
        return mealPlan;
    }
    
    /**
     * Get all meal items for a specific plan
     * 
     * @param planId ID of the meal plan
     * @return List of meal items
     */
    private List<MealPlan.MealItem> getMealItemsForPlan(long planId) {
        List<MealPlan.MealItem> mealItems = new ArrayList<>();
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
            TABLE_MEALS,
            null,
            COLUMN_PLAN_ID + "=?",
            new String[] { String.valueOf(planId) },
            null, null, COLUMN_DAY + " ASC, " + COLUMN_MEAL_TYPE + " ASC"
        );
        
        if (cursor.moveToFirst()) {
            do {
                MealPlan.MealItem item = new MealPlan.MealItem(
                    cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_DAY)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_MEAL_TYPE)),
                    cursor.getLong(cursor.getColumnIndex(COLUMN_RECIPE_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NOTES))
                );
                mealItems.add(item);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        
        return mealItems;
    }
    
    /**
     * Get the currently active meal plan
     * 
     * @return Active MealPlan object or null if none is active
     */
    public MealPlan getActiveMealPlan() {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(
            TABLE_MEAL_PLANS,
            new String[] {
                COLUMN_ID, COLUMN_START_DATE, COLUMN_END_DATE, COLUMN_TITLE, COLUMN_IS_ACTIVE
            },
            COLUMN_IS_ACTIVE + "=?",
            new String[] { "1" },
            null, null, null, null
        );
        
        MealPlan mealPlan = null;
        
        if (cursor != null && cursor.moveToFirst()) {
            try {
                Date startDate = DATE_FORMAT.parse(cursor.getString(cursor.getColumnIndex(COLUMN_START_DATE)));
                Date endDate = DATE_FORMAT.parse(cursor.getString(cursor.getColumnIndex(COLUMN_END_DATE)));
                
                mealPlan = new MealPlan(
                    cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                    startDate,
                    endDate,
                    cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)),
                    true
                );
                
                // Populate meal items
                long planId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                List<MealPlan.MealItem> mealItems = getMealItemsForPlan(planId);
                mealPlan.setMealItems(mealItems);
                
            } catch (ParseException e) {
                e.printStackTrace();
            }
            
            cursor.close();
        }
        
        db.close();
        return mealPlan;
    }
    
    /**
     * Get all meal plans
     * 
     * @return List of all meal plans
     */
    public List<MealPlan> getAllMealPlans() {
        List<MealPlan> mealPlans = new ArrayList<>();
        
        String selectQuery = "SELECT * FROM " + TABLE_MEAL_PLANS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                try {
                    Date startDate = DATE_FORMAT.parse(cursor.getString(cursor.getColumnIndex(COLUMN_START_DATE)));
                    Date endDate = DATE_FORMAT.parse(cursor.getString(cursor.getColumnIndex(COLUMN_END_DATE)));
                    
                    MealPlan mealPlan = new MealPlan(
                        cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                        startDate,
                        endDate,
                        cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_IS_ACTIVE)) == 1
                    );
                    
                    // Populate meal items
                    long planId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                    List<MealPlan.MealItem> mealItems = getMealItemsForPlan(planId);
                    mealPlan.setMealItems(mealItems);
                    
                    mealPlans.add(mealPlan);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return mealPlans;
    }
    
    /**
     * Update a meal plan
     * 
     * @param mealPlan Meal plan to update
     * @return Number of rows affected
     */
    public int updateMealPlan(MealPlan mealPlan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_START_DATE, DATE_FORMAT.format(mealPlan.getStartDate()));
        values.put(COLUMN_END_DATE, DATE_FORMAT.format(mealPlan.getEndDate()));
        values.put(COLUMN_TITLE, mealPlan.getTitle());
        values.put(COLUMN_IS_ACTIVE, mealPlan.isActive() ? 1 : 0);
        
        // If this plan is active, deactivate other plans
        if (mealPlan.isActive()) {
            ContentValues deactivateValues = new ContentValues();
            deactivateValues.put(COLUMN_IS_ACTIVE, 0);
            db.update(TABLE_MEAL_PLANS, deactivateValues, 
                   COLUMN_IS_ACTIVE + "=? AND " + COLUMN_ID + "!=?", 
                   new String[]{"1", String.valueOf(mealPlan.getId())});
        }
        
        int rowsAffected = db.update(
            TABLE_MEAL_PLANS,
            values,
            COLUMN_ID + " = ?",
            new String[] { String.valueOf(mealPlan.getId()) }
        );
        
        db.close();
        return rowsAffected;
    }
    
    /**
     * Delete a meal plan and all its items
     * 
     * @param planId ID of the meal plan to delete
     */
    public void deleteMealPlan(long planId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Delete meal items
        db.delete(
            TABLE_MEALS,
            COLUMN_PLAN_ID + " = ?",
            new String[] { String.valueOf(planId) }
        );
        
        // Delete the plan
        db.delete(
            TABLE_MEAL_PLANS,
            COLUMN_ID + " = ?",
            new String[] { String.valueOf(planId) }
        );
        
        db.close();
    }
    
    /**
     * Delete a meal item
     * 
     * @param itemId ID of the meal item to delete
     */
    public void deleteMealItem(long itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        db.delete(
            TABLE_MEALS,
            COLUMN_ID + " = ?",
            new String[] { String.valueOf(itemId) }
        );
        
        db.close();
    }
    
    /**
     * Update a meal item
     * 
     * @param itemId ID of the item to update
     * @param recipeId New recipe ID
     * @param notes New notes
     * @return Number of rows affected
     */
    public int updateMealItem(long itemId, long recipeId, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_RECIPE_ID, recipeId);
        values.put(COLUMN_NOTES, notes);
        
        int rowsAffected = db.update(
            TABLE_MEALS,
            values,
            COLUMN_ID + " = ?",
            new String[] { String.valueOf(itemId) }
        );
        
        db.close();
        return rowsAffected;
    }
    
    /**
     * Inner class to represent a Review
     */
    public static class Review {
        private long id;
        private long recipeId;
        private String userName;
        private float rating;
        private String comment;
        private String date;
        
        public Review(long id, long recipeId, String userName, float rating, String comment, String date) {
            this.id = id;
            this.recipeId = recipeId;
            this.userName = userName;
            this.rating = rating;
            this.comment = comment;
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
        
        public float getRating() {
            return rating;
        }
        
        public String getComment() {
            return comment;
        }
        
        public String getDate() {
            return date;
        }
    }
    
    /**
     * Create a new meal plan
     * @param startDate Start date
     * @param endDate End date
     * @return MealPlan object
     */
    public MealPlan createMealPlan(Date startDate, Date endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_START_DATE, DATE_FORMAT.format(startDate));
        values.put(COLUMN_END_DATE, DATE_FORMAT.format(endDate));
        
        long id = db.insert(TABLE_MEAL_PLANS, null, values);
        db.close();
        
        return new MealPlan(id, startDate, endDate);
    }
    
    /**
     * Get a meal plan for a date range
     * @param startDate Start date
     * @param endDate End date
     * @return MealPlan object or null if not found
     */
    public MealPlan getMealPlanForDateRange(Date startDate, Date endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String startDateStr = DATE_FORMAT.format(startDate);
        String endDateStr = DATE_FORMAT.format(endDate);
        
        Cursor cursor = db.query(
                TABLE_MEAL_PLANS,
                null,
                COLUMN_START_DATE + " = ? AND " + COLUMN_END_DATE + " = ?",
                new String[]{startDateStr, endDateStr},
                null,
                null,
                null
        );
        
        MealPlan mealPlan = null;
        if (cursor != null && cursor.moveToFirst()) {
            try {
                Date start = DATE_FORMAT.parse(cursor.getString(cursor.getColumnIndex(COLUMN_START_DATE)));
                Date end = DATE_FORMAT.parse(cursor.getString(cursor.getColumnIndex(COLUMN_END_DATE)));
                mealPlan = new MealPlan(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)), start, end);
                loadMealsForPlan(mealPlan);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cursor.close();
        }
        
        db.close();
        return mealPlan;
    }
    
    /**
     * Load meals for a meal plan
     * @param mealPlan MealPlan object
     */
    private void loadMealsForPlan(MealPlan mealPlan) {
        // In a real implementation, this would query the meals table
        // For simplicity, we'll just use empty lists
    }
    
    /**
     * Get shopping items for a meal plan
     * @param planId Plan ID
     * @return List of ShoppingItem objects
     */
    public List<ShoppingItem> getShoppingItems(long planId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<ShoppingItem> items = new ArrayList<>();
                        
                        Cursor cursor = db.query(
                TABLE_SHOPPING_ITEMS,
                                null,
                COLUMN_PLAN_ID + " = ?",
                new String[]{String.valueOf(planId)},
                                null,
                                null,
                COLUMN_CATEGORY + " ASC, " + COLUMN_NAME + " ASC"
        );
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                String category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY));
                String quantity = cursor.getString(cursor.getColumnIndex(COLUMN_QUANTITY));
                boolean checked = cursor.getInt(cursor.getColumnIndex(COLUMN_CHECKED)) == 1;
                
                ShoppingItem item = new ShoppingItem(id, name, category, quantity, checked);
                                        items.add(item);
            } while (cursor.moveToNext());
                        cursor.close();
        }
        
        db.close();
        return items;
    }
    
    /**
     * Add a shopping item
     * @param planId Plan ID
     * @param item ShoppingItem object
     * @return ID of the new item
     */
    public long addShoppingItem(long planId, ShoppingItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_PLAN_ID, planId);
        values.put(COLUMN_NAME, item.getName());
        values.put(COLUMN_CATEGORY, item.getCategory());
        values.put(COLUMN_QUANTITY, item.getQuantityString());
        values.put(COLUMN_CHECKED, item.isChecked() ? 1 : 0);
        
        long id = db.insert(TABLE_SHOPPING_ITEMS, null, values);
        db.close();
        
        item.setId(id);
        return id;
    }
    
    /**
     * Update a shopping item
     * @param planId Plan ID
     * @param item ShoppingItem object
     * @return true if updated successfully
     */
    public boolean updateShoppingItem(long planId, ShoppingItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_NAME, item.getName());
        values.put(COLUMN_CATEGORY, item.getCategory());
        values.put(COLUMN_QUANTITY, item.getQuantityString());
        values.put(COLUMN_CHECKED, item.isChecked() ? 1 : 0);
        
        int rows = db.update(
                TABLE_SHOPPING_ITEMS,
                values,
                COLUMN_ID + " = ? AND " + COLUMN_PLAN_ID + " = ?",
                new String[]{String.valueOf(item.getId()), String.valueOf(planId)}
        );
        
        db.close();
        return rows > 0;
    }
    
    /**
     * Delete a shopping item
     * @param itemId Item ID
     * @return true if deleted successfully
     */
    public boolean deleteShoppingItem(long itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(
                TABLE_SHOPPING_ITEMS,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(itemId)}
        );
        
            db.close();
        return rows > 0;
    }
    
    /**
     * Clear all shopping items for a meal plan
     * @param planId Plan ID
     * @return true if cleared successfully
     */
    public boolean clearShoppingItems(long planId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(
                TABLE_SHOPPING_ITEMS,
                COLUMN_PLAN_ID + " = ?",
                new String[]{String.valueOf(planId)}
        );
        
            db.close();
        return rows > 0;
    }
    
    /**
     * Get planned meal for a specific date and meal type
     * 
     * @param dateStr Date string in yyyy-MM-dd format
     * @param mealType Type of meal (breakfast, lunch, dinner)
     * @return PlannedMeal object or null if not found
     */
    public MealPlanningActivity.PlannedMeal getMealForDateAndType(String dateStr, MealPlanningActivity.MealType mealType) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(
                TABLE_MEALS,
                null,
                COLUMN_MEAL_DATE + "=? AND " + COLUMN_MEAL_TYPE + "=?",
                new String[]{dateStr, mealType.toString()},
                null, null, null
        );
        
        MealPlanningActivity.PlannedMeal meal = null;
        
        if (cursor != null && cursor.moveToFirst()) {
            long recipeId = cursor.getLong(cursor.getColumnIndex(COLUMN_RECIPE_ID));
            String recipeTitle = ""; // We'll need to get this from the recipe db
            
            // Try to get the recipe title from the recipe database
            RecipeDBHelper recipeDBHelper = new RecipeDBHelper(mContext);
            Recipe recipe = recipeDBHelper.getModernRecipeById(recipeId);
            if (recipe != null) {
                recipeTitle = recipe.getTitle();
            }
            
            meal = new MealPlanningActivity.PlannedMeal(dateStr, mealType, recipeId, recipeTitle);
            cursor.close();
        }
        
        db.close();
        return meal;
    }
    
    /**
     * Save a planned meal to the database
     * 
     * @param meal PlannedMeal to save
     * @return ID of the new meal record
     */
    public long savePlannedMeal(MealPlanningActivity.PlannedMeal meal) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // First, delete any existing meal for the same date and type
        db.delete(
                TABLE_MEALS,
                COLUMN_MEAL_DATE + "=? AND " + COLUMN_MEAL_TYPE + "=?",
                new String[]{meal.getDate(), meal.getMealType().toString()}
        );
        
        // Now insert the new meal
        ContentValues values = new ContentValues();
        values.put(COLUMN_MEAL_DATE, meal.getDate());
        values.put(COLUMN_MEAL_TYPE, meal.getMealType().toString());
        values.put(COLUMN_RECIPE_ID, meal.getRecipeId());
        
        long id = db.insert(TABLE_MEALS, null, values);
        db.close();
        
        return id;
    }
} 