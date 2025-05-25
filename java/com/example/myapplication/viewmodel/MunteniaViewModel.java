package com.example.myapplication.viewmodel;

import android.content.Context;
import android.content.SharedPreferences;

public class MunteniaViewModel {
    private final SharedPreferences sharedPreferences;
    private final String regionKey;
    private static final int TOTAL_LOCATIONS = 5; // București, Ploiești, Târgoviște, Sinaia, Curtea de Argeș

    public MunteniaViewModel(Context context, String region) {
        this.sharedPreferences = context.getSharedPreferences("MunteniaPrefs", Context.MODE_PRIVATE);
        this.regionKey = region + "_location_";
    }

    /**
     * Updates the completion status of a location in Muntenia
     *
     * @param locationId ID of the location (1-5)
     * @param completed  Whether the location is completed
     */
    public void updateLocationProgress(int locationId, boolean completed) {
        if (locationId < 1 || locationId > TOTAL_LOCATIONS) return;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(regionKey + locationId, completed);
        editor.apply();
    }

    /**
     * Checks if a specific location is completed
     *
     * @param locationId ID of the location (1-5)
     * @return true if completed, false otherwise
     */
    public boolean isLocationCompleted(int locationId) {
        if (locationId < 1 || locationId > TOTAL_LOCATIONS) return false;
        return sharedPreferences.getBoolean(regionKey + locationId, false);
    }

    /**
     * Gets the count of completed locations in Muntenia
     *
     * @return number of completed locations
     */
    public int getCompletedLocationsCount() {
        int count = 0;
        for (int i = 1; i <= TOTAL_LOCATIONS; i++) {
            if (isLocationCompleted(i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Checks if all locations in Muntenia have been completed
     *
     * @return true if all locations completed, false otherwise
     */
    public boolean isRegionCompleted() {
        return getCompletedLocationsCount() == TOTAL_LOCATIONS;
    }

    /**
     * Clears progress for all locations in Muntenia
     */
    public void resetRegionProgress() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (int i = 1; i <= TOTAL_LOCATIONS; i++) {
            editor.remove(regionKey + i);
        }
        editor.apply();
    }
} 