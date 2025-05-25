package com.example.myapplication.RomApp;

import android.content.Context;
import android.content.SharedPreferences;

public class PointsManager {
    private static final String POINTS_PREFS = "PointsPrefs";
    private static final String TOTAL_POINTS = "totalPoints_";  // Will be appended with userId
    private static final String REGION_POINTS_PREFIX = "points_";  // Will be appended with userId_region
    private static final int POINTS_PER_LANDMARK = 20;

    private SharedPreferences sharedPreferences;
    private static PointsManager instance;

    // Singleton pattern to ensure same points across activities
    public static synchronized PointsManager getInstance(Context context) {
        if (instance == null) {
            instance = new PointsManager(context.getApplicationContext());
        }
        return instance;
    }

    private PointsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(POINTS_PREFS, Context.MODE_PRIVATE);
    }

    private String getCurrentUserId(Context context) {
        SharedPreferences userPrefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return userPrefs.getString("current_user_id", "default");
    }

    public int getTotalPoints(Context context) {
        String userId = getCurrentUserId(context);
        return sharedPreferences.getInt(TOTAL_POINTS + userId, 0);
    }

    public String getTotalPointsWithEmoji(Context context) {
        return "" + getTotalPoints(context);
    }

    // Alias for getTotalPoints to maintain backward compatibility
    public int getPoints(Context context) {
        return getTotalPoints(context);
    }

    public String getPointsWithEmoji(Context context) {
        return "" + getPoints(context);
    }

    public void addPoints(Context context, String region, int points) {
        String userId = getCurrentUserId(context);
        int currentTotal = getTotalPoints(context);
        int regionPoints = getRegionPoints(context, region);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(TOTAL_POINTS + userId, currentTotal + points);
        editor.putInt(REGION_POINTS_PREFIX + userId + "_" + region, regionPoints + points);
        editor.apply();
    }

    public void removePoints(Context context, String region, int points) {
        String userId = getCurrentUserId(context);
        int currentTotal = getTotalPoints(context);
        int regionPoints = getRegionPoints(context, region);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(TOTAL_POINTS + userId, Math.max(0, currentTotal - points));
        editor.putInt(REGION_POINTS_PREFIX + userId + "_" + region, Math.max(0, regionPoints - points));
        editor.apply();
    }

    public int getRegionPoints(Context context, String region) {
        String userId = getCurrentUserId(context);
        return sharedPreferences.getInt(REGION_POINTS_PREFIX + userId + "_" + region, 0);
    }

    public void updateLandmarkStatus(Context context, String region, boolean isChecked) {
        if (isChecked) {
            addPoints(context, region, POINTS_PER_LANDMARK);
        } else {
            removePoints(context, region, POINTS_PER_LANDMARK);
        }
    }

    public static int getPointsPerLandmark() {
        return POINTS_PER_LANDMARK;
    }
}
