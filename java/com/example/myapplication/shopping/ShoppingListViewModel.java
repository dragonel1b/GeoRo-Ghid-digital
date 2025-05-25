package com.example.myapplication.shopping;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for handling shopping list operations
 */
public class ShoppingListViewModel extends AndroidViewModel {

    private final MutableLiveData<List<ShoppingItem>> itemsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSaving = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final List<ShoppingItem> items = new ArrayList<>();
    
    private ShoppingItem lastAddedItem;

    public ShoppingListViewModel(@NonNull Application application) {
        super(application);
        itemsLiveData.setValue(items);
    }

    public LiveData<List<ShoppingItem>> getItems() {
        return itemsLiveData;
    }

    public LiveData<Boolean> getIsSaving() {
        return isSaving;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void addShoppingItem(ShoppingItem item) {
        // Add to in-memory list
        items.add(item);
        itemsLiveData.setValue(items);
        
        // Save reference to last added item (for undo)
        lastAddedItem = item;
        
        // Simulate saving to database (in a real app, use Room)
        saveToDatabase(item);
    }
    
    public void removeLastAddedItem() {
        if (lastAddedItem != null && items.contains(lastAddedItem)) {
            items.remove(lastAddedItem);
            itemsLiveData.setValue(items);
            
            // In a real app, delete from database
            deleteFromDatabase(lastAddedItem);
            
            lastAddedItem = null;
        }
    }
    
    public void toggleItemChecked(long itemId) {
        for (ShoppingItem item : items) {
            if (item.getId() == itemId) {
                item.setChecked(!item.isChecked());
                itemsLiveData.setValue(items);
                
                // In a real app, update in database
                updateInDatabase(item);
                break;
            }
        }
    }
    
    public void deleteItem(ShoppingItem item) {
        if (items.contains(item)) {
            items.remove(item);
            itemsLiveData.setValue(items);
            
            // In a real app, delete from database
            deleteFromDatabase(item);
        }
    }
    
    private void saveToDatabase(ShoppingItem item) {
        isSaving.setValue(true);
        
        // Simulate network/database delay
        new Thread(() -> {
            try {
                // Simulate saving
                Thread.sleep(500);
                
                // Simulate success
                isSaving.postValue(false);
            } catch (Exception e) {
                // Handle error
                isSaving.postValue(false);
                errorMessage.postValue("Eroare la salvare: " + e.getMessage());
            }
        }).start();
    }
    
    private void updateInDatabase(ShoppingItem item) {
        // In a real app, this would use Room DAO
        // Similar to saveToDatabase but for updates
    }
    
    private void deleteFromDatabase(ShoppingItem item) {
        // In a real app, this would use Room DAO
        // Similar to saveToDatabase but for deletes
    }
    
    public void retryLastOperation() {
        // Retry the last failed operation
        if (lastAddedItem != null) {
            saveToDatabase(lastAddedItem);
        }
    }
} 