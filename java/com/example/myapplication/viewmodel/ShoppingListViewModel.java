package com.example.myapplication.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.model.Ingredient;
import com.example.myapplication.repository.ShoppingListRepository;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for shopping list functionality
 */
public class ShoppingListViewModel extends ViewModel {

    private final ShoppingListRepository repository;
    private final MutableLiveData<List<Ingredient>> shoppingListItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> operationSuccessful = new MutableLiveData<>();
    
    public ShoppingListViewModel() {
        // In a real app, this would be injected
        repository = new ShoppingListRepository();
    }
    
    /**
     * Get all shopping list items
     * @return LiveData list of shopping list items
     */
    public LiveData<List<Ingredient>> getShoppingListItems() {
        return shoppingListItems;
    }
    
    /**
     * Add an ingredient to the shopping list
     * @param ingredient Ingredient to add
     */
    public void addIngredientToShoppingList(Ingredient ingredient) {
        repository.addIngredientToShoppingList(ingredient)
                .thenAccept(success -> {
                    operationSuccessful.postValue(success);
                    
                    if (success) {
                        List<Ingredient> currentList = shoppingListItems.getValue();
                        if (currentList == null) {
                            currentList = new ArrayList<>();
                        }
                        
                        // Add if not already in list
                        if (!currentList.contains(ingredient)) {
                            currentList.add(ingredient);
                            shoppingListItems.postValue(currentList);
                        }
                    }
                });
    }
    
    /**
     * Add multiple ingredients to the shopping list
     * @param ingredients List of ingredients to add
     */
    public void addIngredientsToShoppingList(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }
        
        repository.addIngredientsToShoppingList(ingredients)
                .thenAccept(success -> {
                    operationSuccessful.postValue(success);
                    
                    if (success) {
                        List<Ingredient> currentList = shoppingListItems.getValue();
                        if (currentList == null) {
                            currentList = new ArrayList<>();
                        }
                        
                        // Add new ingredients
                        boolean listChanged = false;
                        for (Ingredient ingredient : ingredients) {
                            if (!currentList.contains(ingredient)) {
                                currentList.add(ingredient);
                                listChanged = true;
                            }
                        }
                        
                        if (listChanged) {
                            shoppingListItems.postValue(currentList);
                        }
                    }
                });
    }
    
    /**
     * Remove an ingredient from the shopping list
     * @param ingredient Ingredient to remove
     */
    public void removeIngredientFromShoppingList(Ingredient ingredient) {
        repository.removeIngredientFromShoppingList(ingredient)
                .thenAccept(success -> {
                    operationSuccessful.postValue(success);
                    
                    if (success) {
                        List<Ingredient> currentList = shoppingListItems.getValue();
                        if (currentList != null && currentList.remove(ingredient)) {
                            shoppingListItems.postValue(new ArrayList<>(currentList));
                        }
                    }
                });
    }
    
    /**
     * Remove multiple ingredients from the shopping list
     * @param ingredients List of ingredients to remove
     */
    public void removeIngredientsFromShoppingList(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }
        
        repository.removeIngredientsFromShoppingList(ingredients)
                .thenAccept(success -> {
                    operationSuccessful.postValue(success);
                    
                    if (success) {
                        List<Ingredient> currentList = shoppingListItems.getValue();
                        if (currentList != null) {
                            boolean listChanged = currentList.removeAll(ingredients);
                            if (listChanged) {
                                shoppingListItems.postValue(new ArrayList<>(currentList));
                            }
                        }
                    }
                });
    }
    
    /**
     * Clear all items from the shopping list
     */
    public void clearShoppingList() {
        repository.clearShoppingList()
                .thenAccept(success -> {
                    operationSuccessful.postValue(success);
                    
                    if (success) {
                        shoppingListItems.postValue(new ArrayList<>());
                    }
                });
    }
    
    /**
     * Get operation success status
     * @return LiveData boolean indicating if the last operation was successful
     */
    public LiveData<Boolean> getOperationSuccessful() {
        return operationSuccessful;
    }
} 