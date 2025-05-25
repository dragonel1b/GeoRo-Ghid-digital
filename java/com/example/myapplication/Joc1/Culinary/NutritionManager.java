package com.example.myapplication.Joc1.Culinary;

import android.content.Context;

import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity.Recipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager class for nutritional calculations and tracking
 */
public class NutritionManager {
    private static NutritionManager instance;
    private final Map<String, Float> nutritionGoals; // daily nutrition goals
    private final Map<String, NutritionalInfo> ingredientDatabase;
    
    /**
     * Get singleton instance of NutritionManager
     * @return The NutritionManager instance
     */
    public static synchronized NutritionManager getInstance() {
        if (instance == null) {
            instance = new NutritionManager();
        }
        return instance;
    }
    
    /**
     * Private constructor
     */
    private NutritionManager() {
        nutritionGoals = new HashMap<>();
        setDefaultNutritionGoals();
        ingredientDatabase = new HashMap<>();
        initializeDatabase();
    }
    
    /**
     * Set default nutrition goals
     */
    private void setDefaultNutritionGoals() {
        // Default daily values based on 2000 calorie diet
        nutritionGoals.put("calories", 2000f);
        nutritionGoals.put("protein", 50f);  // g
        nutritionGoals.put("carbs", 275f);   // g
        nutritionGoals.put("fat", 65f);      // g
        nutritionGoals.put("fiber", 28f);    // g
        nutritionGoals.put("sugar", 50f);    // g
        nutritionGoals.put("sodium", 2300f); // mg
    }
    
    /**
     * Initialize the nutrition database with common ingredients
     */
    private void initializeDatabase() {
        // Sample data - in a real app, this would be loaded from a database or API
        // Initialize with (label, calories, protein, fat, carbs, fiber, sugar, sodium)
        ingredientDatabase.put("cartofi", new NutritionalInfo("Cartofi", 77, 2.0f, 0.1f, 17.0f, 2.2f, 0.8f, 6f));
        ingredientDatabase.put("carne de vita", new NutritionalInfo("Carne de vită", 250, 26.0f, 17.0f, 0.0f, 0f, 0f, 72f));
        ingredientDatabase.put("ceapa", new NutritionalInfo("Ceapă", 40, 1.1f, 0.1f, 9.0f, 1.7f, 4.2f, 4f));
        ingredientDatabase.put("morcovi", new NutritionalInfo("Morcovi", 41, 0.9f, 0.2f, 9.6f, 2.8f, 4.7f, 69f));
        ingredientDatabase.put("smantana", new NutritionalInfo("Smântână", 230, 2.5f, 23.0f, 4.0f, 0f, 3.2f, 40f));
        ingredientDatabase.put("faina", new NutritionalInfo("Făină", 364, 10.0f, 1.0f, 76.0f, 2.7f, 0.3f, 2f));
        ingredientDatabase.put("zahar", new NutritionalInfo("Zahăr", 387, 0.0f, 0.0f, 100.0f, 0f, 100.0f, 0f));
        ingredientDatabase.put("oua", new NutritionalInfo("Ouă", 155, 13.0f, 11.0f, 1.1f, 0f, 0.4f, 124f));
    }
    
    /**
     * Calculate total nutritional information for a list of recipes
     * @param recipes List of recipes
     * @param servings Map of recipe IDs to number of servings
     * @return Combined nutritional information
     */
    public NutritionalInfo calculateTotalNutrition(List<ModernCulinaryActivity.Recipe> recipes, Map<Long, Integer> servings) {
        NutritionalInfo totalInfo = new NutritionalInfo("Total", 0, 0, 0, 0, 0, 0, 0);
        
        for (ModernCulinaryActivity.Recipe recipe : recipes) {
            NutritionalInfo recipeInfo = recipe.getNutritionalInfo();
            if (recipeInfo != null) {
                int servingCount = servings.getOrDefault(recipe.getId(), 1);
                NutritionalInfo scaledInfo = recipeInfo.scale(servingCount);
                totalInfo = totalInfo.add(scaledInfo);
            }
        }
        
        return totalInfo;
    }
    
    /**
     * Calculate percentage of daily nutritional goals
     * @param nutritionalInfo Nutritional information to evaluate
     * @return Map of nutrient names to percentage of daily goals
     */
    public Map<String, Float> calculateDailyPercentages(NutritionalInfo nutritionalInfo) {
        Map<String, Float> percentages = new HashMap<>();
        
        percentages.put("calories", calculatePercentage(nutritionalInfo.getCalories(), nutritionGoals.get("calories")));
        percentages.put("protein", calculatePercentage(nutritionalInfo.getProtein(), nutritionGoals.get("protein")));
        percentages.put("carbs", calculatePercentage(nutritionalInfo.getCarbohydrates(), nutritionGoals.get("carbs")));
        percentages.put("fat", calculatePercentage(nutritionalInfo.getFat(), nutritionGoals.get("fat")));
        percentages.put("fiber", calculatePercentage(nutritionalInfo.getFiber(), nutritionGoals.get("fiber")));
        percentages.put("sugar", calculatePercentage(nutritionalInfo.getSugar(), nutritionGoals.get("sugar")));
        percentages.put("sodium", calculatePercentage(nutritionalInfo.getSodium(), nutritionGoals.get("sodium")));
        
        return percentages;
    }
    
    /**
     * Set nutrition goal for a specific nutrient
     * @param nutrient Nutrient name
     * @param value Goal value
     */
    public void setNutritionGoal(String nutrient, float value) {
        nutritionGoals.put(nutrient, value);
    }
    
    /**
     * Get nutrition goal for a specific nutrient
     * @param nutrient Nutrient name
     * @return Goal value
     */
    public float getNutritionGoal(String nutrient) {
        return nutritionGoals.getOrDefault(nutrient, 0f);
    }
    
    /**
     * Calculate percentage of a value against a goal
     * @param value Current value
     * @param goal Goal value
     * @return Percentage (0-100)
     */
    private float calculatePercentage(float value, float goal) {
        if (goal <= 0) return 0;
        return (value / goal) * 100;
    }
    
    /**
     * Analyze a nutritional profile and provide recommendations
     * @param nutritionalInfo Nutritional information to analyze
     * @return List of recommendation strings
     */
    public String[] generateNutritionRecommendations(NutritionalInfo nutritionalInfo) {
        Map<String, Float> percentages = calculateDailyPercentages(nutritionalInfo);
        
        String[] recommendations = new String[3];
        int index = 0;
        
        // Check protein
        if (percentages.get("protein") < 80) {
            recommendations[index++] = "Increase protein intake for better muscle maintenance and satiety.";
        }
        
        // Check fiber
        if (percentages.get("fiber") < 70) {
            recommendations[index++] = "Add more fiber-rich foods like vegetables, fruits, and whole grains for digestive health.";
        }
        
        // Check sodium
        if (percentages.get("sodium") > 100) {
            recommendations[index++] = "Reduce sodium intake by limiting processed foods and adding less salt to meals.";
        }
        
        // Check sugar
        if (percentages.get("sugar") > 100) {
            recommendations[index++] = "Reduce added sugar consumption by choosing natural sweeteners and whole foods.";
        }
        
        // Check fat
        if (percentages.get("fat") > 120) {
            recommendations[index++] = "Consider reducing fat intake, especially from saturated and trans fats.";
        }
        
        // Check carbs
        if (percentages.get("carbs") > 120) {
            recommendations[index++] = "Consider reducing carbohydrate intake, focusing on complex carbs rather than simple sugars.";
        }
        
        // Check calories
        if (percentages.get("calories") > 110) {
            recommendations[index++] = "Your caloric intake is higher than recommended. Consider portion control.";
        } else if (percentages.get("calories") < 80) {
            recommendations[index++] = "Your caloric intake may be too low. Consider increasing portion sizes or meal frequency.";
        }
        
        // Fill remaining slots with general advice
        String[] generalAdvice = {
            "Include a variety of colorful foods for a wide range of nutrients.",
            "Stay hydrated by drinking water throughout the day.",
            "Include healthy fats from sources like olive oil, avocados, and nuts.",
            "Try to include lean proteins in every meal."
        };
        
        int generalIndex = 0;
        while (index < recommendations.length) {
            recommendations[index++] = generalAdvice[generalIndex++ % generalAdvice.length];
        }
        
        return recommendations;
    }
    
    /**
     * Calculate the nutritional value of a recipe based on its ingredients
     * 
     * @param recipe The recipe to analyze
     * @return NutritionalInfo object containing the calculated values
     */
    public NutritionalInfo calculateRecipeNutrition(Recipe recipe) {
        // In a real implementation, this would parse the recipe ingredients
        // and calculate the total nutritional value
        
        // For demonstration, return a sample nutritional info
        return new NutritionalInfo(
            recipe.getTitle(),
            350,  // calories per serving
            15.0f,  // protein
            35.0f,  // carbs
            12.0f,  // fat
            2.0f,   // fiber
            5.0f,   // sugar
            500.0f  // sodium
        );
    }
    
    /**
     * Get nutritional information for a specific ingredient
     * 
     * @param ingredientName Name of the ingredient
     * @return NutritionalInfo for the ingredient, or null if not found
     */
    public NutritionalInfo getIngredientInfo(String ingredientName) {
        String normalizedName = ingredientName.toLowerCase();
        return ingredientDatabase.getOrDefault(normalizedName, null);
    }
    
    /**
     * Save user's daily nutritional intake
     * 
     * @param context Application context
     * @param calories Total calories consumed
     * @param protein Protein in grams
     * @param fat Fat in grams
     * @param carbs Carbohydrates in grams
     */
    public void saveDailyIntake(Context context, float calories, float protein, float fat, float carbs) {
        // In a real implementation, this would save to a persistent storage like SharedPreferences
        // or a local database
    }
} 