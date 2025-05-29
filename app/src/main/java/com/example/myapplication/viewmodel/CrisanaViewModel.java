package com.example.myapplication.viewmodel;

import androidx.lifecycle.ViewModel;
import java.util.HashMap;
import java.util.Map;

public class CrisanaViewModel extends ViewModel {
    private final Map<Integer, Boolean> locationProgress = new HashMap<>();

    public CrisanaViewModel() {
        // Initialize default progress values
        for (int i = 1; i <= 5; i++) {
            locationProgress.put(i, false);
        }
    }

    public void updateProgress(int locationId, boolean completed) {
        locationProgress.put(locationId, completed);
    }
    
    public void updateLocationProgress(int locationId, boolean completed) {
        locationProgress.put(locationId, completed);
    }

    public boolean isLocationCompleted(int locationId) {
        return locationProgress.getOrDefault(locationId, false);
    }

    public int getCompletedLocationsCount() {
        int count = 0;
        for (Boolean completed : locationProgress.values()) {
            if (completed) {
                count++;
            }
        }
        return count;
    }
} 