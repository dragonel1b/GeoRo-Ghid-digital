package com.example.myapplication.RomApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

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
        // Standardizăm numele regiunii pentru a evita probleme cu case-sensitivity
        String standardizedRegion = standardizeRegionName(region);
        
        if (isChecked) {
            // Adăugăm exact 20 de puncte
            addPoints(context, standardizedRegion, 20);
            
            // Afișăm un mesaj de confirmare
            Toast.makeText(context, "+" + POINTS_PER_LANDMARK + 
                          " puncte adăugate în " + standardizedRegion + "!", 
                          Toast.LENGTH_SHORT).show();
        } else {
            // Scădem exact 20 de puncte
            removePoints(context, standardizedRegion, 20);
            
            // Afișăm un mesaj de confirmare
            Toast.makeText(context, "-" + POINTS_PER_LANDMARK + 
                          " puncte eliminate din " + standardizedRegion + "!", 
                          Toast.LENGTH_SHORT).show();
        }
    }

    // Metodă pentru standardizarea numelor regiunilor
    public String standardizeRegionName(String region) {
        if (region == null) return "romania";
        
        region = region.toLowerCase().trim();
        
        // Mapăm posibile variante de scriere la numele standard
        if (region.contains("trans") || region.contains("ardeal")) {
            return "transilvania";
        } else if (region.contains("mold")) {
            return "moldova";
        } else if (region.contains("olten")) {
            return "oltenia";
        } else if (region.contains("munte") || region.contains("valah")) {
            return "muntenia";
        } else if (region.contains("dobr")) {
            return "dobrogea";
        } else if (region.contains("bana")) {
            return "banat";
        } else if (region.contains("crisa")) {
            return "crisana";
        } else if (region.contains("mara")) {
            return "maramures";
        } else if (region.contains("buco")) {
            return "bucovina";
        }
        
        return region; // Păstrăm numele original dacă nu se potrivește cu nicio regiune
    }

    public static int getPointsPerLandmark() {
        return POINTS_PER_LANDMARK;
    }
}
