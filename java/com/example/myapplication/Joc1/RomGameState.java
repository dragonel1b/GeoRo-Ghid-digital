package com.example.myapplication.Joc1;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.myapplication.R;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RomGameState {
    private static RomGameState instance;
    private SharedPreferences preferences;
    private static final String PREF_NAME = "RomGamePreferences";

    // Resource keys
    private static final String KEY_ESENTA_CALATORIEI = "esenta_calatoriei"; // Fuel renamed to "Esența Călătoriei"
    private static final String KEY_MONEDE_DACICE = "monede_dacice"; // Money renamed to "Monede Dacice"
    private static final String KEY_MERINDE = "merinde"; // Food renamed to "Merinde"
    private static final String KEY_PUNCTE_INTELEPTE = "puncte_intelepte"; // Culture points renamed to "Puncte Înțelepte"
    private static final String KEY_ACHIEVEMENTS = "achievements";

    // Achievement IDs
    public static final String ACHIEVEMENT_PRIMUL_PAS = "primul_pas"; // "First Step"
    public static final String ACHIEVEMENT_MAESTRU_ISTORIC = "maestru_istoric"; // "History Master"
    public static final String ACHIEVEMENT_CALATOR_LEGENDAR = "calator_legendar"; // "Legendary Traveler"
    public static final String ACHIEVEMENT_BUCATAR_REGAL = "bucatar_regal"; // "Royal Chef"
    public static final String ACHIEVEMENT_SPIRIT_DACIC = "spirit_dacic"; // "Dacian Spirit"

    // Default values with Romanian-themed descriptions
    private static final float DEFAULT_ESENTA = 50.0f; // Starting essence for your journey
    private static final float DEFAULT_MONEDE = 1000.0f; // Initial Dacian coins
    private static final float DEFAULT_MERINDE = 5.0f; // Starting provisions
    private static final int DEFAULT_PUNCTE = 0; // Initial wisdom points

    // Current values with Romanian-themed names
    private float esentaCalatoriei; // Travel essence
    private float monedeDacice; // Dacian coins
    private float merinde; // Provisions
    private int puncteIntelepte; // Wisdom points
    private Set<String> unlockedAchievements;

    private RomGameState() {
        // Private constructor for singleton
        unlockedAchievements = new HashSet<>();
    }

    public static RomGameState getInstance() {
        if (instance == null) {
            instance = new RomGameState();
        }
        return instance;
    }

    public void initialize(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            loadState();
        }
    }

    private void loadState() {
        try {
            esentaCalatoriei = preferences.getFloat(KEY_ESENTA_CALATORIEI, DEFAULT_ESENTA);
            monedeDacice = preferences.getFloat(KEY_MONEDE_DACICE, DEFAULT_MONEDE);
            merinde = preferences.getFloat(KEY_MERINDE, DEFAULT_MERINDE);
            puncteIntelepte = preferences.getInt(KEY_PUNCTE_INTELEPTE, DEFAULT_PUNCTE);

            // Load achievements
            Set<String> defaultSet = new HashSet<>();
            unlockedAchievements = new HashSet<>(
                    preferences.getStringSet(KEY_ACHIEVEMENTS, defaultSet)
            );
        } catch (Exception e) {
            e.printStackTrace();
            resetToDefaults();
        }
    }

    public void saveState() {
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat(KEY_ESENTA_CALATORIEI, esentaCalatoriei);
            editor.putFloat(KEY_MONEDE_DACICE, monedeDacice);
            editor.putFloat(KEY_MERINDE, merinde);
            editor.putInt(KEY_PUNCTE_INTELEPTE, puncteIntelepte);
            editor.putStringSet(KEY_ACHIEVEMENTS, unlockedAchievements);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetToDefaults() {
        esentaCalatoriei = DEFAULT_ESENTA;
        monedeDacice = DEFAULT_MONEDE;
        merinde = DEFAULT_MERINDE;
        puncteIntelepte = DEFAULT_PUNCTE;
        saveState();
    }

    // Resource getters with Romanian-themed names
    public float getEsentaCalatoriei() { return esentaCalatoriei; }
    public float getMonedeDacice() { return monedeDacice; }
    public float getMerinde() { return merinde; }
    public int getPuncteIntelepte() { return puncteIntelepte; }

    // Resource update methods with Romanian-themed names
    public boolean updateEsentaCalatoriei(float delta) {
        if (esentaCalatoriei + delta < 0) return false;
        esentaCalatoriei += delta;
        saveState();
        return true;
    }

    // Alias method for updateEsentaCalatoriei to maintain API compatibility
    public boolean updateFuel(float delta) {
        return updateEsentaCalatoriei(delta);
    }

    public boolean updateMonedeDacice(float delta) {
        if (monedeDacice + delta < 0) return false;
        monedeDacice += delta;
        saveState();
        return true;
    }

    // Alias method for updateMonedeDacice to maintain API compatibility
    public boolean updateMoney(float delta) {
        return updateMonedeDacice(delta);
    }

    public boolean updateMerinde(float delta) {
        if (merinde + delta < 0) return false;
        merinde += delta;
        saveState();
        return true;
    }

    // Alias method for updateMerinde to maintain API compatibility
    public boolean updateFood(float delta) {
        return updateMerinde(delta);
    }

    public boolean addPuncteIntelepte(int points, Context context) {
        if (points > 0) {
            puncteIntelepte += points;

            // Check for Dacian Spirit achievement
            if (puncteIntelepte >= 500 &&
                    !isAchievementUnlocked(ACHIEVEMENT_SPIRIT_DACIC)) {
                unlockAchievement(ACHIEVEMENT_SPIRIT_DACIC, context);
            }

            saveState();
            return true;
        }
        return false;
    }

    // Resource cost calculations
    public float calculateFuelCost(float distance) {
        // Average consumption: 7L/100km
        return (distance * 7.0f) / 100.0f;
    }

    public float calculateFoodCost(int days) {
        // Average food consumption: 1.5kg per day
        return days * 1.5f;
    }

    // Game progress checks
    public boolean canTravelDistance(float distance) {
        float fuelNeeded = calculateFuelCost(distance);
        return esentaCalatoriei >= fuelNeeded;
    }

    public boolean canAffordPurchase(float cost) {
        return monedeDacice >= cost;
    }

    public boolean hasEnoughFood(int days) {
        return merinde >= calculateFoodCost(days);
    }

    // Achievement methods
    public boolean unlockAchievement(String achievementId, Context context) {
        if (!unlockedAchievements.contains(achievementId)) {
            unlockedAchievements.add(achievementId);
            saveState();

            // Show achievement notification
            Achievement achievement = ACHIEVEMENTS.get(achievementId);
            if (achievement != null && context != null) {
                Toast.makeText(context,
                        context.getString(R.string.achievement_unlocked, achievement.title),
                        Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return false;
    }

    public boolean unlockAchievement(String achievementId) {
        return unlockAchievement(achievementId, null);
    }

    public boolean isAchievementUnlocked(String achievementId) {
        return unlockedAchievements.contains(achievementId);
    }

    public Set<String> getUnlockedAchievements() {
        return new HashSet<>(unlockedAchievements);
    }

    public static class Achievement {
        public final String id;
        public final String title;
        public final String description;
        public final int pointsRequired;

        public Achievement(String id, String title, String description, int pointsRequired) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.pointsRequired = pointsRequired;
        }
    }

    public static final Map<String, Achievement> ACHIEVEMENTS = new HashMap<String, Achievement>() {{
        put(ACHIEVEMENT_PRIMUL_PAS, new Achievement(
                ACHIEVEMENT_PRIMUL_PAS,
                "Prima Explorare",
                "Completează primul tău quiz despre un oraș",
                0
        ));
        put(ACHIEVEMENT_MAESTRU_ISTORIC, new Achievement(
                ACHIEVEMENT_MAESTRU_ISTORIC,
                "Maestru al Cunoașterii",
                "Obține scorul maxim la 3 quiz-uri diferite",
                100
        ));
        put(ACHIEVEMENT_CALATOR_LEGENDAR, new Achievement(
                ACHIEVEMENT_CALATOR_LEGENDAR,
                "Explorator Cultural",
                "Vizitează 5 orașe diferite",
                50
        ));
        put(ACHIEVEMENT_BUCATAR_REGAL, new Achievement(
                ACHIEVEMENT_BUCATAR_REGAL,
                "Expert Culinar",
                "Descoperă 10 rețete tradiționale",
                75
        ));
        put(ACHIEVEMENT_SPIRIT_DACIC, new Achievement(
                ACHIEVEMENT_SPIRIT_DACIC,
                "Spirit Dacic",
                "Acumulează 500 de puncte înțelepte",
                500
        ));
    }};
}
