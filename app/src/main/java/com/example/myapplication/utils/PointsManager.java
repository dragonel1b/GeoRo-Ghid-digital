package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;

/**
 * Manager pentru gestionarea punctelor și locațiilor vizitate
 */
public class PointsManager {
    private static final String POINTS_PREFS = "PointsPrefs";
    private static final String TOTAL_POINTS = "totalPoints_";  // Will be appended with userId
    private static final String REGION_POINTS_PREFIX = "points_";  // Will be appended with userId_region
    private static final String VISITED_LOCATIONS_PREFIX = "visited_locations_";
    private static final String COMPLETED_STORIES_PREFIX = "completed_stories_";
    private static final int POINTS_PER_LANDMARK = 20;

    private SharedPreferences sharedPreferences;
    private static PointsManager instance;

    /**
     * Constructor privat pentru Singleton
     * @param context Contextul aplicației
     */
    private PointsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(POINTS_PREFS, Context.MODE_PRIVATE);
    }
    
    /**
     * Obține instanța singleton a managerului
     * @param context Contextul aplicației
     * @return Instanța PointsManager
     */
    public static synchronized PointsManager getInstance(Context context) {
        if (instance == null) {
            instance = new PointsManager(context.getApplicationContext());
        }
        return instance;
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

    /**
     * Obține numărul total de puncte
     * @return Numărul total de puncte
     */
    public int getPoints() {
        return sharedPreferences.getInt(TOTAL_POINTS + "default", 0);
    }
    
    // Alias pentru getTotalPoints pentru compatibilitate
    public int getPoints(Context context) {
        return getTotalPoints(context);
    }

    public String getPointsWithEmoji(Context context) {
        return "" + getPoints(context);
    }

    /**
     * Adaugă puncte
     * @param points Numărul de puncte de adăugat
     */
    public void addPoints(int points) {
        int currentPoints = getPoints();
        sharedPreferences.edit().putInt(TOTAL_POINTS + "default", currentPoints + points).apply();
    }
    
    public void addPoints(Context context, String region, int points) {
        String userId = getCurrentUserId(context);
        int currentTotal = getTotalPoints(context);
        int regionPoints = getRegionPoints(context, region);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(TOTAL_POINTS + userId, currentTotal + points);
        editor.putInt(REGION_POINTS_PREFIX + userId + "_" + region, regionPoints + points);
        editor.apply();
        
        // Actualizăm și în noul sistem
        addPoints(points);
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
    
    /**
     * Marchează o poveste ca fiind completată
     * @param activity Activitatea curentă
     * @param region Regiunea poveștii
     * @param completed Starea de completare
     */
    public void setStoryCompleted(AppCompatActivity activity, String region, boolean completed) {
        String userId = getCurrentUserId(activity);
        String key = COMPLETED_STORIES_PREFIX + userId + "_" + region.toLowerCase();
        
        sharedPreferences.edit().putBoolean(key, completed).apply();
        
        if (completed) {
            // Adăugăm puncte bonus pentru completarea poveștii
            addPoints(activity, region, 50);
            Toast.makeText(activity, "+50 puncte pentru completarea poveștii din " + region + "!", 
                          Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Verifică dacă o poveste a fost completată
     * @param context Contextul aplicației
     * @param region Regiunea poveștii
     * @return true dacă povestea a fost completată, false în caz contrar
     */
    public boolean isStoryCompleted(Context context, String region) {
        String userId = getCurrentUserId(context);
        String key = COMPLETED_STORIES_PREFIX + userId + "_" + region.toLowerCase();
        return sharedPreferences.getBoolean(key, false);
    }

    /**
     * Marchează o locație ca vizitată
     * @param regionName Numele regiunii
     * @param locationId ID-ul locației
     */
    public void markLocationAsVisited(String regionName, int locationId) {
        String key = VISITED_LOCATIONS_PREFIX + regionName.toLowerCase();
        Set<String> visitedLocations = getVisitedLocationsSet(regionName);
        visitedLocations.add(String.valueOf(locationId));
        sharedPreferences.edit().putStringSet(key, visitedLocations).apply();
    }
    
    /**
     * Marchează o locație ca vizitată
     * @param context Contextul aplicației
     * @param regionName Numele regiunii
     * @param locationId ID-ul locației
     */
    public void markLocationAsVisited(Context context, String regionName, int locationId) {
        String userId = getCurrentUserId(context);
        String key = VISITED_LOCATIONS_PREFIX + userId + "_" + regionName.toLowerCase();
        
        Set<String> visitedLocations = sharedPreferences.getStringSet(key, new HashSet<>());
        Set<String> updatedLocations = new HashSet<>(visitedLocations); // Create a copy to modify
        updatedLocations.add(String.valueOf(locationId));
        
        sharedPreferences.edit().putStringSet(key, updatedLocations).apply();
        
        // Also update the old method for backward compatibility
        markLocationAsVisited(regionName, locationId);
    }
    
    /**
     * Verifică dacă o locație a fost vizitată
     * @param regionName Numele regiunii
     * @param locationId ID-ul locației
     * @return true dacă locația a fost vizitată, false în caz contrar
     */
    public boolean isLocationVisited(String regionName, int locationId) {
        Set<String> visitedLocations = getVisitedLocationsSet(regionName);
        return visitedLocations.contains(String.valueOf(locationId));
    }
    
    /**
     * Verifică dacă o locație a fost vizitată
     * @param context Contextul aplicației
     * @param regionName Numele regiunii
     * @param locationId ID-ul locației
     * @return true dacă locația a fost vizitată, false în caz contrar
     */
    public boolean isLocationVisited(Context context, String regionName, int locationId) {
        String userId = getCurrentUserId(context);
        String key = VISITED_LOCATIONS_PREFIX + userId + "_" + regionName.toLowerCase();
        
        Set<String> visitedLocations = sharedPreferences.getStringSet(key, new HashSet<>());
        return visitedLocations.contains(String.valueOf(locationId));
    }
    
    /**
     * Obține numărul de locații vizitate pentru o regiune
     * @param regionName Numele regiunii
     * @return Numărul de locații vizitate
     */
    public int getVisitedLocationsCount(String regionName) {
        return getVisitedLocationsSet(regionName).size();
    }
    
    /**
     * Obține numărul de locații vizitate pentru o regiune
     * @param context Contextul aplicației
     * @param regionName Numele regiunii
     * @return Numărul de locații vizitate
     */
    public int getVisitedLocationsCount(Context context, String regionName) {
        String userId = getCurrentUserId(context);
        String key = VISITED_LOCATIONS_PREFIX + userId + "_" + regionName.toLowerCase();
        
        Set<String> visitedLocations = sharedPreferences.getStringSet(key, new HashSet<>());
        return visitedLocations.size();
    }
    
    /**
     * Obține setul de locații vizitate pentru o regiune
     * @param regionName Numele regiunii
     * @return Setul de ID-uri ale locațiilor vizitate
     */
    private Set<String> getVisitedLocationsSet(String regionName) {
        String key = VISITED_LOCATIONS_PREFIX + regionName.toLowerCase();
        return sharedPreferences.getStringSet(key, new HashSet<>());
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

    /**
     * Resetează toate punctele și locațiile vizitate
     */
    public void resetAll() {
        sharedPreferences.edit().clear().apply();
    }
    
    /**
     * Resetează locațiile vizitate pentru o regiune
     * @param regionName Numele regiunii
     */
    public void resetRegion(String regionName) {
        String key = VISITED_LOCATIONS_PREFIX + regionName.toLowerCase();
        sharedPreferences.edit().remove(key).apply();
    }
    
    public static int getPointsPerLandmark() {
        return POINTS_PER_LANDMARK;
    }
} 