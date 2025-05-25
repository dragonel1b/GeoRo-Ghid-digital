package com.example.myapplication.Joc1.Culinary;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel for handling meal planning data
 */
public class MealPlannerViewModel extends ViewModel {

    // Map of day -> meal type -> meal info
    private final Map<String, Map<String, MealInfo>> mealPlan = new HashMap<>();
    
    private final MutableLiveData<Map<String, Map<String, MealInfo>>> mealPlanLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> savingInProgress = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> savedSuccessfully = new MutableLiveData<>(false);

    /**
     * Initialize the ViewModel with any stored meal plan data
     */
    public void init() {
        // In a real app, load saved meal plan from Room database or preferences
        // For now, using empty data
        mealPlanLiveData.setValue(mealPlan);
    }

    /**
     * Add a meal to the plan
     */
    public void addMeal(String day, String mealType, String recipeId, String recipeName) {
        // Get or create the map for this day
        Map<String, MealInfo> dayMeals = mealPlan.getOrDefault(day, new HashMap<>());
        
        // Add the meal for this type
        dayMeals.put(mealType, new MealInfo(recipeId, recipeName));
        
        // Put back in the main map
        mealPlan.put(day, dayMeals);
        
        // Notify observers
        mealPlanLiveData.setValue(mealPlan);
        
        // Save the updated meal plan
        saveMealPlan();
    }

    /**
     * Remove a meal from the plan
     */
    public void removeMeal(String day, String mealType) {
        Map<String, MealInfo> dayMeals = mealPlan.get(day);
        if (dayMeals != null) {
            dayMeals.remove(mealType);
            mealPlanLiveData.setValue(mealPlan);
            
            // Save the updated meal plan
            saveMealPlan();
        }
    }

    /**
     * Get all meals for a specific day
     */
    public Map<String, MealInfo> getMealsForDay(String day) {
        return mealPlan.getOrDefault(day, new HashMap<>());
    }

    /**
     * Get the meal plan live data for observing
     */
    public LiveData<Map<String, Map<String, MealInfo>>> getMealPlan() {
        return mealPlanLiveData;
    }

    /**
     * Get saving in progress status for observing
     */
    public LiveData<Boolean> getSavingInProgress() {
        return savingInProgress;
    }

    /**
     * Get error messages for observing
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get save success status for observing
     */
    public LiveData<Boolean> getSavedSuccessfully() {
        return savedSuccessfully;
    }

    /**
     * Save the meal plan (simulated async operation)
     */
    private void saveMealPlan() {
        // Set saving in progress
        savingInProgress.setValue(true);
        
        // Simulate network or database operation
        new Thread(() -> {
            try {
                // Simulate delay
                Thread.sleep(500);
                
                // In a real app, save to Room database or remote API
                
                // Set saved successfully
                savingInProgress.postValue(false);
                savedSuccessfully.postValue(true);
                
                // Reset success after a delay
                Thread.sleep(1000);
                savedSuccessfully.postValue(false);
            } catch (Exception e) {
                // Handle error
                savingInProgress.postValue(false);
                errorMessage.postValue("Nu s-a putut salva planul de mese: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Retry saving if it failed
     */
    public void retrySave() {
        saveMealPlan();
    }

    /**
     * Information about a meal
     */
    public static class MealInfo {
        private final String recipeId;
        private final String recipeName;

        public MealInfo(String recipeId, String recipeName) {
            this.recipeId = recipeId;
            this.recipeName = recipeName;
        }

        public String getRecipeId() {
            return recipeId;
        }

        public String getRecipeName() {
            return recipeName;
        }
    }
} 