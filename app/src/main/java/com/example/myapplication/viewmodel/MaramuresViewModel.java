package com.example.myapplication.viewmodel;

import android.content.Context;
import android.content.SharedPreferences;

public class MaramuresViewModel {
    private static final String PREFS_NAME = "MaramuresPrefs";
    private static final String COMPLETED_LOCATIONS_COUNT = "completedLocationsCount";
    private static final String LOCATION_PREFIX = "location_";

    public MaramuresViewModel() {
        // Default constructor
    }

    public int getUserProgress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("userProgress", 0);
    }

    public void setLocationCompleted(Context context, int locationId, boolean completed) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(LOCATION_PREFIX + locationId, completed);
        editor.apply();
    }

    public boolean isLocationCompleted(Context context, int locationId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(LOCATION_PREFIX + locationId, false);
    }

    public int getCompletedLocationsCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int count = 0;
        for (int i = 1; i <= 8; i++) { // Assuming there are 8 locations in Maramures
            if (prefs.getBoolean(LOCATION_PREFIX + i, false)) {
                count++;
            }
        }
        return count;
    }

    public void incrementUserProgress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int currentProgress = prefs.getInt("userProgress", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("userProgress", currentProgress + 1);
        editor.apply();
    }
} 