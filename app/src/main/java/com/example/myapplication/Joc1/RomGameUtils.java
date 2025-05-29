package com.example.myapplication.Joc1;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class RomGameUtils {
    private static final float FUEL_CONSUMPTION_PER_100KM = 7.0f;
    private static final float FOOD_CONSUMPTION_PER_DAY = 1.5f;
    private static final int BASE_EXPLORATION_COST = 50;
    private static final Random random = new Random();

    // Resource Calculations
    public static float calculateFuelNeeded(float distance) {
        return (distance * FUEL_CONSUMPTION_PER_100KM) / 100.0f;
    }

    public static float calculateFoodNeeded(int days) {
        return days * FOOD_CONSUMPTION_PER_DAY;
    }

    public static int calculateExplorationCost(String cityName) {
        // Base cost plus random variation based on city
        return BASE_EXPLORATION_COST + random.nextInt(30);
    }

    public static int calculateCulturePoints(int basePoints, float explorationTime) {
        return (int)(basePoints * (1.0f + explorationTime / 60.0f));
    }

    // Distance Calculations
    public static float calculateDistance(float lat1, float lon1, float lat2, float lon2) {
        final int R = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (float)(R * c);
    }

    // UI Helpers
    public static void showToast(@NonNull Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(@NonNull Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void applyFadeInAnimation(@NonNull Context context, @NonNull View view) {
        Animation fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        view.startAnimation(fadeIn);
    }

    public static void applySlideInAnimation(@NonNull Context context, @NonNull View view) {
        Animation slideIn = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        view.startAnimation(slideIn);
    }

    // Time and Date Formatting
    public static String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("ro"));
        return sdf.format(new Date(timestamp));
    }

    // Game Progress
    public static int calculateGameProgress(RomGameState gameState) {
        // Calculate overall game progress based on visited cities and completed activities
        return Math.min(100, (int)((gameState.getPuncteIntelepte() / 1000.0f) * 100));
    }

    // Achievement System
    public static boolean checkAchievement(String achievementId, RomGameState gameState) {
        switch (achievementId) {
            case "FIRST_CITY":
                return gameState.getPuncteIntelepte() >= 10;
            case "FUEL_MASTER":
                return gameState.getEsentaCalatoriei() >= 100;
            case "CULTURAL_EXPERT":
                return gameState.getPuncteIntelepte() >= 500;
            case "RICH_TRAVELER":
                return gameState.getMonedeDacice() >= 5000;
            default:
                return false;
        }
    }

    // Resource Formatting
    public static String formatMoney(float amount) {
        return String.format(Locale.getDefault(), "%.2f RON", amount);
    }

    public static String formatFuel(float amount) {
        return String.format(Locale.getDefault(), "%.1f L", amount);
    }

    public static String formatFood(float amount) {
        return String.format(Locale.getDefault(), "%.1f kg", amount);
    }

    // Error Handling
    public static void handleError(@NonNull Context context, Exception e) {
        e.printStackTrace();
        showToast(context, "A apărut o eroare: " + e.getMessage());
    }

    // Validation
    public static boolean isValidResourceAmount(float amount) {
        return amount >= 0 && !Float.isInfinite(amount) && !Float.isNaN(amount);
    }

    public static boolean canAffordPurchase(RomGameState gameState, float cost) {
        return gameState.getMonedeDacice() >= cost;
    }

    public static boolean hasEnoughFuel(RomGameState gameState, float distance) {
        float fuelNeeded = calculateFuelNeeded(distance);
        return gameState.getEsentaCalatoriei() >= fuelNeeded;
    }

    public static boolean hasEnoughFood(RomGameState gameState, int days) {
        float foodNeeded = calculateFoodNeeded(days);
        return gameState.getMerinde() >= foodNeeded;
    }

    // Game Difficulty Adjustments
    public static float getResourceMultiplier(int difficultyLevel) {
        switch (difficultyLevel) {
            case 1: // Easy
                return 1.5f;
            case 2: // Normal
                return 1.0f;
            case 3: // Hard
                return 0.7f;
            default:
                return 1.0f;
        }
    }
}
