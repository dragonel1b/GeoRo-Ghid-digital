package com.example.myapplication.security;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import java.util.regex.Pattern;

/**
 * Utility class for validating and sanitizing user input
 * to prevent injection attacks and ensure data integrity
 */
public class InputValidator {
    private static final String TAG = "InputValidator";
    
    // Pattern for alphanumeric input with basic punctuation
    private static final Pattern SAFE_TEXT_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s.,;:?!()'-]*$");
    
    // Pattern for numeric input only
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]*$");
    
    /**
     * Validates if the provided input is safe (contains only allowed characters)
     *
     * @param input Text to validate
     * @return true if input is safe, false otherwise
     */
    public static boolean isSafeText(String input) {
        if (input == null) {
            return false;
        }
        return SAFE_TEXT_PATTERN.matcher(input).matches();
    }
    
    /**
     * Sanitizes text input by removing potentially dangerous characters
     *
     * @param input Text to sanitize
     * @return Sanitized text
     */
    public static String sanitizeText(String input) {
        if (input == null) {
            return "";
        }
        // Replace potentially dangerous characters with safe alternatives
        return input.replaceAll("[^a-zA-Z0-9\\s.,;:?!()'-]", "");
    }
    
    /**
     * Validates if the provided input is a valid email address
     *
     * @param email Email to validate
     * @return true if email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    /**
     * Validates if the provided input is numeric only
     *
     * @param input Text to validate
     * @return true if input contains only numbers, false otherwise
     */
    public static boolean isNumeric(String input) {
        if (input == null) {
            return false;
        }
        return NUMERIC_PATTERN.matcher(input).matches();
    }
    
    /**
     * Validates if the provided input is within acceptable range
     *
     * @param value Value to check
     * @param min Minimum acceptable value
     * @param max Maximum acceptable value
     * @return true if within range, false otherwise
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
    
    /**
     * Validates if the provided input is within acceptable range
     *
     * @param value Value to check
     * @param min Minimum acceptable value
     * @param max Maximum acceptable value
     * @return true if within range, false otherwise
     */
    public static boolean isInRange(float value, float min, float max) {
        return value >= min && value <= max;
    }
    
    /**
     * Log validation failure for debugging purposes
     *
     * @param validationType Type of validation that failed
     * @param inputValue Value that failed validation
     */
    public static void logValidationFailure(String validationType, String inputValue) {
        Log.w(TAG, "Validation failed for " + validationType + ": " + inputValue);
    }
} 