package com.example.myapplication.Joc1.Culinary;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing a meal plan for a date range
 */
public class MealPlan {
    private long id;
    private Date startDate;
    private Date endDate;
    private String title;
    private boolean isActive;
    private List<MealItem> mealItems;
    private Map<Integer, List<MealItem>> mealsByDay;  // Day of week -> meals
    
    /**
     * Constructor for a meal plan
     * 
     * @param id Unique identifier for the meal plan
     * @param startDate Start date of the plan
     * @param endDate End date of the plan
     * @param title Title of the plan
     * @param isActive Whether this plan is active
     */
    public MealPlan(long id, Date startDate, Date endDate, String title, boolean isActive) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
        this.isActive = isActive;
        this.mealItems = new ArrayList<>();
        this.mealsByDay = new HashMap<>();
    }
    
    /**
     * Constructor with basic fields
     * 
     * @param id         Unique ID
     * @param startDate  Start date
     * @param endDate    End date
     */
    public MealPlan(long id, Date startDate, Date endDate) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = "Weekly Plan";
        this.isActive = true;
        this.mealItems = new ArrayList<>();
    }
    
    /**
     * Initialize meal days map when meal items are set
     */
    private void initMealsByDay() {
        mealsByDay.clear();
        
        for (MealItem item : mealItems) {
            int day = item.getDay();
            
            if (!mealsByDay.containsKey(day)) {
                mealsByDay.put(day, new ArrayList<>());
            }
            
            mealsByDay.get(day).add(item);
        }
    }
    
    /**
     * Add a meal item to the plan
     * 
     * @param item Meal item to add
     */
    public void addMealItem(MealItem item) {
        mealItems.add(item);
        
        int day = item.getDay();
        if (!mealsByDay.containsKey(day)) {
            mealsByDay.put(day, new ArrayList<>());
        }
        
        mealsByDay.get(day).add(item);
    }
    
    /**
     * Remove a meal item from the plan
     * 
     * @param itemId ID of the meal item to remove
     * @return true if removed, false if not found
     */
    public boolean removeMealItem(long itemId) {
        MealItem itemToRemove = null;
        
        for (MealItem item : mealItems) {
            if (item.getId() == itemId) {
                itemToRemove = item;
                break;
            }
        }
        
        if (itemToRemove != null) {
            mealItems.remove(itemToRemove);
            
            // Update mealsByDay
            int day = itemToRemove.getDay();
            if (mealsByDay.containsKey(day)) {
                mealsByDay.get(day).remove(itemToRemove);
                
                if (mealsByDay.get(day).isEmpty()) {
                    mealsByDay.remove(day);
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Get all meal items for a specific day of the week
     * 
     * @param day Day of the week (0 = Sunday, 1 = Monday, etc.)
     * @return List of meal items for that day
     */
    public List<MealItem> getMealsForDay(int day) {
        if (mealsByDay.containsKey(day)) {
            return new ArrayList<>(mealsByDay.get(day));
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Get all meal items of a specific type for a day
     * 
     * @param day Day of the week
     * @param mealType Type of meal (breakfast, lunch, dinner, snack)
     * @return List of meals matching the type
     */
    public List<MealItem> getMealsForDayAndType(int day, String mealType) {
        List<MealItem> result = new ArrayList<>();
        
        if (mealsByDay.containsKey(day)) {
            for (MealItem item : mealsByDay.get(day)) {
                if (item.getMealType().equals(mealType)) {
                    result.add(item);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Get all meal items by type across all days
     * 
     * @param mealType Type of meal
     * @return Map of day to list of meals of that type
     */
    public Map<Integer, List<MealItem>> getMealsByType(String mealType) {
        Map<Integer, List<MealItem>> result = new HashMap<>();
        
        for (Map.Entry<Integer, List<MealItem>> entry : mealsByDay.entrySet()) {
            List<MealItem> mealsOfType = new ArrayList<>();
            
            for (MealItem item : entry.getValue()) {
                if (item.getMealType().equals(mealType)) {
                    mealsOfType.add(item);
                }
            }
            
            if (!mealsOfType.isEmpty()) {
                result.put(entry.getKey(), mealsOfType);
            }
        }
        
        return result;
    }
    
    /**
     * Get nutritional information for a specific date
     * 
     * @param date Date to get nutrition info for
     * @return Nutritional information for all meals on that date
     */
    public NutritionalInfo getNutritionalInfoForDate(Date date) {
        // Convert date to day of week
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // Convert to 0-based
        
        // Start with base nutritional info
        NutritionalInfo totalNutrition = new NutritionalInfo(
            "Daily Total", 0, 0, 0, 0, 0, 0, 0);
        
        // Add nutrition from all meals that day
        List<MealItem> dailyMeals = getMealsForDay(dayOfWeek);
        for (MealItem meal : dailyMeals) {
            // In a real app, we would fetch recipe info from database
            // and calculate actual nutritional values
            
            // For now, create sample data based on meal type
            NutritionalInfo mealNutrition;
            
            switch (meal.getMealType()) {
                case "breakfast":
                    mealNutrition = new NutritionalInfo(
                        "Breakfast", 300, 10, 9, 40, 2, 5, 150);
                    break;
                case "lunch":
                    mealNutrition = new NutritionalInfo(
                        "Lunch", 500, 25, 15, 60, 4, 3, 400);
                    break;
                case "dinner":
                    mealNutrition = new NutritionalInfo(
                        "Dinner", 700, 35, 20, 80, 6, 4, 500);
                    break;
                default: // snack
                    mealNutrition = new NutritionalInfo(
                        "Snack", 200, 5, 7, 25, 1, 10, 100);
                    break;
            }
            
            totalNutrition = totalNutrition.add(mealNutrition);
        }
        
        return totalNutrition;
    }
    
    /**
     * Get average daily nutritional information for the entire meal plan
     * 
     * @return Average daily nutritional information
     */
    public NutritionalInfo getAverageDailyNutritionalInfo() {
        // Start with base nutritional info
        NutritionalInfo totalNutrition = new NutritionalInfo(
            "Daily Average", 0, 0, 0, 0, 0, 0, 0);
        
        // Count number of days with meals
        int daysWithMeals = mealsByDay.size();
        
        if (daysWithMeals == 0) {
            return totalNutrition; // Return zeros if no meals planned
        }
        
        // Sum up nutrition for all days
        for (int day : mealsByDay.keySet()) {
            // Convert day to a date within our meal plan
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            
            // Adjust calendar to match the day of week
            while (cal.get(Calendar.DAY_OF_WEEK) - 1 != day) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                
                // Don't go past end date
                if (cal.getTime().after(endDate)) {
                    break;
                }
            }
            
            // If we found a valid date for this day within our range
            if (!cal.getTime().after(endDate)) {
                NutritionalInfo dayNutrition = getNutritionalInfoForDate(cal.getTime());
                totalNutrition = totalNutrition.add(dayNutrition);
            }
        }
        
        // Calculate average by dividing by number of days
        return new NutritionalInfo(
            "Daily Average",
            totalNutrition.getCalories() / daysWithMeals,
            totalNutrition.getProtein() / daysWithMeals,
            totalNutrition.getFat() / daysWithMeals,
            totalNutrition.getCarbohydrates() / daysWithMeals,
            totalNutrition.getFiber() / daysWithMeals,
            totalNutrition.getSugar() / daysWithMeals,
            totalNutrition.getSodium() / daysWithMeals
        );
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public List<MealItem> getMealItems() {
        return mealItems;
    }
    
    public void setMealItems(List<MealItem> mealItems) {
        this.mealItems = mealItems;
        initMealsByDay();
    }
    
    /**
     * Generate a shopping list from the meal plan
     * @return List of ShoppingItem objects
     */
    public List<ShoppingItem> generateShoppingList() {
        List<ShoppingItem> shoppingList = new ArrayList<>();
        Map<String, ShoppingItem> itemMap = new HashMap<>();

        // Iterate through all meal items
        for (MealItem mealItem : mealItems) {
            // Get recipe ingredients (this would need to be implemented)
            List<String> ingredients = getRecipeIngredients(mealItem.getRecipeId());
            
            // Add each ingredient to the shopping list
            for (String ingredient : ingredients) {
                if (!itemMap.containsKey(ingredient)) {
                    ShoppingItem item = new ShoppingItem(0, ingredient, "", "", false);
                    itemMap.put(ingredient, item);
                }
            }
        }

        // Convert map to list
        shoppingList.addAll(itemMap.values());
        return shoppingList;
    }

    /**
     * Get ingredients for a recipe (placeholder - needs implementation)
     */
    private List<String> getRecipeIngredients(long recipeId) {
        // This should be implemented to fetch ingredients from your recipe database
        return new ArrayList<>();
    }
    
    /**
     * Inner class representing a meal item within a meal plan
     */
    public static class MealItem {
        private long id;
        private int day;  // Day of week (0 = Sunday, 1 = Monday, etc.)
        private String mealType;  // breakfast, lunch, dinner, snack
        private long recipeId;
        private String notes;
        
        /**
         * Constructor for a meal item
         * 
         * @param id Unique identifier
         * @param day Day of week (0-6)
         * @param mealType Type of meal
         * @param recipeId ID of the recipe
         * @param notes Additional notes
         */
        public MealItem(long id, int day, String mealType, long recipeId, String notes) {
            this.id = id;
            this.day = day;
            this.mealType = mealType;
            this.recipeId = recipeId;
            this.notes = notes;
        }
        
        // Getters and setters
        
        public long getId() {
            return id;
        }
        
        public int getDay() {
            return day;
        }
        
        public void setDay(int day) {
            this.day = day;
        }
        
        public String getMealType() {
            return mealType;
        }
        
        public void setMealType(String mealType) {
            this.mealType = mealType;
        }
        
        public long getRecipeId() {
            return recipeId;
        }
        
        public void setRecipeId(long recipeId) {
            this.recipeId = recipeId;
        }
        
        public String getNotes() {
            return notes;
        }
        
        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
} 