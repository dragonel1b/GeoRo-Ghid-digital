package com.example.myapplication.viewmodel;

import androidx.lifecycle.ViewModel;
import java.util.HashMap;
import java.util.Map;

public class TransilvaniaViewModel extends ViewModel {
    private Map<Integer, Boolean> locationProgress;
    private int totalLocations = 5; // Total number of locations in Transilvania

    public TransilvaniaViewModel() {
        locationProgress = new HashMap<>();
        // Initialize all locations as not completed
        for (int i = 1; i <= totalLocations; i++) {
            locationProgress.put(i, false);
        }
    }

    public void updateLocationProgress(int locationId, boolean completed) {
        if (locationId >= 1 && locationId <= totalLocations) {
            locationProgress.put(locationId, completed);
        }
    }

    public boolean isLocationCompleted(int locationId) {
        return locationProgress.getOrDefault(locationId, false);
    }

    public int getCompletedLocationsCount() {
        int count = 0;
        for (boolean completed : locationProgress.values()) {
            if (completed) count++;
        }
        return count;
    }

    public int getTotalLocations() {
        return totalLocations;
    }

    public float getProgressPercentage() {
        return (float) getCompletedLocationsCount() / totalLocations * 100;
    }

    public void resetProgress() {
        for (int i = 1; i <= totalLocations; i++) {
            locationProgress.put(i, false);
        }
    }
} 