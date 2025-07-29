package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Test configuration utility for Android tests
 * Provides common setup and utilities for testing
 */
public class TestConfig {

    private static final String TEST_PREFS_NAME = "test_preferences";
    
    /**
     * Get test context
     */
    public static Context getTestContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }
    
    /**
     * Get test shared preferences
     */
    public static SharedPreferences getTestPreferences() {
        return getTestContext().getSharedPreferences(TEST_PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Clear test data
     */
    public static void clearTestData() {
        SharedPreferences prefs = getTestPreferences();
        prefs.edit().clear().apply();
    }
    
    /**
     * Initialize Firebase for testing
     */
    public static void initializeFirebaseForTesting() {
        try {
            FirebaseApp.initializeApp(getTestContext());
        } catch (Exception e) {
            // Firebase might already be initialized
        }
    }
    
    /**
     * Get test Firestore instance
     */
    public static FirebaseFirestore getTestFirestore() {
        return FirebaseFirestore.getInstance();
    }
    
    /**
     * Wait for a specified time (useful for async operations)
     */
    public static void waitFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Check if running on emulator
     */
    public static boolean isEmulator() {
        return android.os.Build.FINGERPRINT.startsWith("generic") ||
               android.os.Build.FINGERPRINT.startsWith("unknown") ||
               android.os.Build.MODEL.contains("google_sdk") ||
               android.os.Build.MODEL.contains("Emulator") ||
               android.os.Build.MODEL.contains("Android SDK built for x86") ||
               android.os.Build.MANUFACTURER.contains("Genymotion") ||
               (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic")) ||
               "google_sdk".equals(android.os.Build.PRODUCT);
    }
    
    /**
     * Get test device info
     */
    public static String getTestDeviceInfo() {
        return android.os.Build.MANUFACTURER + " " + 
               android.os.Build.MODEL + " " + 
               android.os.Build.VERSION.RELEASE;
    }
} 