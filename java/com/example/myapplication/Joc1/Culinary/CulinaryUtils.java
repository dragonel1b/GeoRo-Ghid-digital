package com.example.myapplication.Joc1.Culinary;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.example.myapplication.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class with constants and helper methods for culinary module
 */
public class CulinaryUtils {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
    
    // Regions
    public static final String[] REGIONS = {
            "Moldova", 
            "Muntenia", 
            "Transilvania", 
            "Banat", 
            "Oltenia", 
            "Dobrogea", 
            "Maramureș", 
            "Bucovina", 
            "Crișana"
    };
    
    // Categories
    public static final String[] CATEGORIES = {
            "Feluri principale", 
            "Supe și ciorbe", 
            "Deserturi", 
            "Pâine și produse de patiserie", 
            "Gustări", 
            "Salate",
            "Sosuri și garnituri"
    };
    
    // Difficulty levels
    public static final String[] DIFFICULTY_LEVELS = {
            "Ușor", 
            "Mediu", 
            "Dificil"
    };
    
    private CulinaryUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Check if a region is valid
     * @param region Region name to check
     * @return true if region is valid
     */
    public static boolean isValidRegion(String region) {
        return Arrays.asList(REGIONS).contains(region);
    }
    
    /**
     * Check if a category is valid
     * @param category Category name to check
     * @return true if category is valid
     */
    public static boolean isValidCategory(String category) {
        return Arrays.asList(CATEGORIES).contains(category);
    }
    
    /**
     * Check if a difficulty level is valid
     * @param difficulty Difficulty level to check
     * @return true if difficulty level is valid
     */
    public static boolean isValidDifficulty(String difficulty) {
        return Arrays.asList(DIFFICULTY_LEVELS).contains(difficulty);
    }
    
    /**
     * Format time string for display
     * @param hours Hours
     * @param minutes Minutes
     * @return Formatted time string
     */
    public static String formatPrepTime(int hours, int minutes) {
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || hours == 0) {
            sb.append(minutes).append("min");
        }
        return sb.toString().trim();
    }
    
    /**
     * Parse time string to minutes
     * @param timeString Time string (e.g. "1h 30min")
     * @return Total minutes
     */
    public static int parseTimeToMinutes(String timeString) {
        int totalMinutes = 0;
        
        if (timeString == null || timeString.isEmpty()) {
            return totalMinutes;
        }
        
        // Parse hours
        if (timeString.contains("h")) {
            String[] parts = timeString.split("h");
            try {
                int hours = Integer.parseInt(parts[0].trim());
                totalMinutes += hours * 60;
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
            
            // If there's content after "h", update timeString
            if (parts.length > 1) {
                timeString = parts[1].trim();
            } else {
                timeString = "";
            }
        }
        
        // Parse minutes
        if (timeString.contains("min")) {
            String minutesStr = timeString.replace("min", "").trim();
            try {
                int minutes = Integer.parseInt(minutesStr);
                totalMinutes += minutes;
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }
        
        return totalMinutes;
    }
    
    /**
     * Format minutes as a readable time string
     * 
     * @param minutes Total minutes
     * @return Formatted string (e.g., "1h 30min")
     */
    public static String formatMinutesToTime(int minutes) {
        if (minutes <= 0) {
            return "0min";
        }
        
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        
        if (hours > 0 && remainingMinutes > 0) {
            return hours + "h " + remainingMinutes + "min";
        } else if (hours > 0) {
            return hours + "h";
        } else {
            return remainingMinutes + "min";
        }
    }
    
    /**
     * Format a date string for display
     * 
     * @param dateString Date in format "yyyy-MM-dd"
     * @return Formatted date string
     */
    public static String formatDate(String dateString) {
        if (TextUtils.isEmpty(dateString)) {
            return "";
        }
        
        try {
            Date date = DATE_FORMAT.parse(dateString);
            if (date != null) {
                return DISPLAY_DATE_FORMAT.format(date);
            }
        } catch (ParseException e) {
            return dateString;
        }
        
        return dateString;
    }
    
    /**
     * Get the number of days between two date strings
     * 
     * @param startDateStr Start date in format "yyyy-MM-dd"
     * @param endDateStr End date in format "yyyy-MM-dd"
     * @return Number of days between dates, or -1 if parsing fails
     */
    public static long getDaysBetween(String startDateStr, String endDateStr) {
        try {
            Date startDate = DATE_FORMAT.parse(startDateStr);
            Date endDate = DATE_FORMAT.parse(endDateStr);
            
            if (startDate != null && endDate != null) {
                long diffInMillis = Math.abs(endDate.getTime() - startDate.getTime());
                return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
            }
        } catch (ParseException e) {
            return -1;
        }
        
        return -1;
    }
    
    /**
     * Get a drawable resource for a difficulty level
     * 
     * @param context Application context
     * @param difficulty Difficulty level string
     * @return Corresponding color resource ID
     */
    public static int getDifficultyColor(Context context, String difficulty) {
        if ("Ușor".equals(difficulty)) {
            return ContextCompat.getColor(context, R.color.difficulty_easy);
        } else if ("Mediu".equals(difficulty)) {
            return ContextCompat.getColor(context, R.color.difficulty_medium);
        } else if ("Dificil".equals(difficulty)) {
            return ContextCompat.getColor(context, R.color.difficulty_hard);
        } else {
            return ContextCompat.getColor(context, R.color.difficulty_medium);
        }
    }
    
    /**
     * Determine if a recipe is quick to make (under 45 minutes)
     * 
     * @param timeString Cooking time string
     * @return True if the recipe is quick to make
     */
    public static boolean isQuickRecipe(String timeString) {
        int minutes = parseTimeToMinutes(timeString);
        return minutes > 0 && minutes <= 45;
    }
    
    /**
     * Converts a category string to a meal type
     * @param category Recipe category
     * @return Appropriate meal type or null if not specific
     */
    public static String categoryToMealType(String category) {
        if (category == null) return null;
        
        category = category.toLowerCase();
        if (category.contains("mic dejun") || category.contains("breakfast")) {
            return "breakfast";
        } else if (category.contains("aperitiv") || category.contains("starter")) {
            return "lunch";
        } else if (category.contains("fel principal") || category.contains("main")) {
            return "dinner";
        } else if (category.contains("desert") || category.contains("dessert")) {
            return "snack";
        }
        
        return null;
    }
    
    /**
     * Gets the appropriate meal type based on the current hour
     * @param hour Current hour (24-hour format)
     * @return Suggested meal type
     */
    public static String getMealTypeForHour(int hour) {
        if (hour >= 5 && hour < 11) {
            return "breakfast";
        } else if (hour >= 11 && hour < 15) {
            return "lunch";
        } else if (hour >= 17 && hour < 22) {
            return "dinner";
        } else {
            return "snack";
        }
    }
} 