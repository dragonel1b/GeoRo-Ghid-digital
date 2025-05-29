package com.example.myapplication.bucovinausage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BucovinaViewModel extends ViewModel {
    
    // List of Bucovina locations
    private final List<String> locations = Arrays.asList(
            "Suceava", 
            "Gura Humorului", 
            "Câmpulung Moldovenesc", 
            "Rădăuți", 
            "Vatra Dornei"
    );
    
    // Map to track visited locations
    private final Map<String, Boolean> visitedLocations = new HashMap<>();
    
    // LiveData for location completion status
    private final MutableLiveData<Map<String, Boolean>> locationStatus = new MutableLiveData<>();
    
    // LiveData for overall completion
    private final MutableLiveData<Integer> completionPercentage = new MutableLiveData<>();
    
    // Currently selected location
    private String currentLocation;
    
    public BucovinaViewModel() {
        // Initialize location status
        for (String location : locations) {
            visitedLocations.put(location, false);
        }
        locationStatus.setValue(visitedLocations);
        updateCompletionPercentage();
    }
    
    // Get all locations
    public List<String> getLocations() {
        return locations;
    }
    
    // Mark a location as visited
    public void markLocationAsVisited(String location) {
        if (locations.contains(location)) {
            visitedLocations.put(location, true);
            locationStatus.setValue(visitedLocations);
            updateCompletionPercentage();
        }
    }
    
    // Check if a location is visited
    public boolean isLocationVisited(String location) {
        Boolean visited = visitedLocations.get(location);
        return visited != null && visited;
    }
    
    // Get visited locations
    public List<String> getVisitedLocations() {
        List<String> visited = new ArrayList<>();
        for (String location : locations) {
            if (Boolean.TRUE.equals(visitedLocations.get(location))) {
                visited.add(location);
            }
        }
        return visited;
    }
    
    // Get unvisited locations
    public List<String> getUnvisitedLocations() {
        List<String> unvisited = new ArrayList<>();
        for (String location : locations) {
            if (!Boolean.TRUE.equals(visitedLocations.get(location))) {
                unvisited.add(location);
            }
        }
        return unvisited;
    }
    
    // Set current location
    public void setCurrentLocation(String location) {
        this.currentLocation = location;
    }
    
    // Get current location
    public String getCurrentLocation() {
        return currentLocation;
    }
    
    // Get location status LiveData
    public LiveData<Map<String, Boolean>> getLocationStatus() {
        return locationStatus;
    }
    
    // Get completion percentage LiveData
    public LiveData<Integer> getCompletionPercentage() {
        return completionPercentage;
    }
    
    // Update completion percentage
    private void updateCompletionPercentage() {
        int visitedCount = 0;
        for (Boolean visited : visitedLocations.values()) {
            if (visited) {
                visitedCount++;
            }
        }
        
        int percentage = (int) (((float) visitedCount / locations.size()) * 100);
        completionPercentage.setValue(percentage);
    }
    
    // Reset all progress
    public void resetProgress() {
        for (String location : locations) {
            visitedLocations.put(location, false);
        }
        locationStatus.setValue(visitedLocations);
        updateCompletionPercentage();
    }
} 