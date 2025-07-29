package com.example.myapplication.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

/**
 * Manager pentru Firebase Crashlytics
 * Gestionează raportarea erorilor și crash-urilor
 */
public class FirebaseCrashlyticsManager {
    private static final String TAG = "FirebaseCrashlytics";
    private static FirebaseCrashlyticsManager instance;
    private final FirebaseCrashlytics crashlytics;
    private final Context context;

    private FirebaseCrashlyticsManager(Context context) {
        this.context = context.getApplicationContext();
        this.crashlytics = FirebaseCrashlytics.getInstance();
    }

    /**
     * Obține instanța singleton
     */
    public static synchronized FirebaseCrashlyticsManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseCrashlyticsManager(context);
        }
        return instance;
    }

    /**
     * Inițializează Crashlytics
     */
    public void initialize() {
        try {
            Log.d(TAG, "Initializing Firebase Crashlytics...");
            
            // Activează Crashlytics
            crashlytics.setCrashlyticsCollectionEnabled(true);
            Log.d(TAG, "Crashlytics collection enabled: " + crashlytics.isCrashlyticsCollectionEnabled());
            
            // Setează informații despre utilizator
            setUserInfo();
            
            // Setează informații despre aplicație
            setAppInfo();
            
            // Testează conexiunea cu Firebase
            crashlytics.log("Firebase Crashlytics initialized successfully");
            crashlytics.setCustomKey("initialization_time", System.currentTimeMillis());
            crashlytics.setCustomKey("app_package", context.getPackageName());
            
            Log.i(TAG, "Firebase Crashlytics initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Crashlytics", e);
        }
    }

    /**
     * Setează informații despre utilizator
     */
    public void setUserInfo() {
        try {
            // Setează ID-ul utilizatorului
            crashlytics.setUserId("user_" + System.currentTimeMillis());
            
            // Setează informații despre utilizator
            crashlytics.setCustomKey("user_level", "5");
            crashlytics.setCustomKey("user_region", "Transilvania");
            crashlytics.setCustomKey("app_version", getAppVersion());
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting user info", e);
        }
    }

    /**
     * Setează informații despre aplicație
     */
    public void setAppInfo() {
        try {
            crashlytics.setCustomKey("device_model", android.os.Build.MODEL);
            crashlytics.setCustomKey("android_version", android.os.Build.VERSION.RELEASE);
            crashlytics.setCustomKey("app_version", getAppVersion());
            crashlytics.setCustomKey("build_type", "debug"); // sau "release"
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting app info", e);
        }
    }

    /**
     * Raportează o excepție
     */
    public void reportException(Exception exception) {
        try {
            crashlytics.recordException(exception);
            Log.i(TAG, "Exception reported to Crashlytics: " + exception.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error reporting exception", e);
        }
    }

    /**
     * Raportează o excepție cu context
     */
    public void reportException(Exception exception, String context) {
        try {
            crashlytics.setCustomKey("error_context", context);
            crashlytics.recordException(exception);
            Log.i(TAG, "Exception reported to Crashlytics with context: " + context);
        } catch (Exception e) {
            Log.e(TAG, "Error reporting exception with context", e);
        }
    }

    /**
     * Raportează o eroare custom
     */
    public void reportError(String errorType, String errorMessage) {
        try {
            crashlytics.setCustomKey("error_type", errorType);
            crashlytics.setCustomKey("error_message", errorMessage);
            crashlytics.recordException(new RuntimeException(errorMessage));
            Log.i(TAG, "Custom error reported to Crashlytics: " + errorType);
        } catch (Exception e) {
            Log.e(TAG, "Error reporting custom error", e);
        }
    }

    /**
     * Raportează o eroare de rețea
     */
    public void reportNetworkError(String endpoint, String error) {
        try {
            crashlytics.setCustomKey("network_endpoint", endpoint);
            crashlytics.setCustomKey("network_error", error);
            crashlytics.recordException(new RuntimeException("Network error: " + error));
            Log.i(TAG, "Network error reported to Crashlytics: " + endpoint);
        } catch (Exception e) {
            Log.e(TAG, "Error reporting network error", e);
        }
    }

    /**
     * Raportează o eroare de Firebase
     */
    public void reportFirebaseError(String operation, String error) {
        try {
            crashlytics.setCustomKey("firebase_operation", operation);
            crashlytics.setCustomKey("firebase_error", error);
            crashlytics.recordException(new RuntimeException("Firebase error: " + error));
            Log.i(TAG, "Firebase error reported to Crashlytics: " + operation);
        } catch (Exception e) {
            Log.e(TAG, "Error reporting Firebase error", e);
        }
    }

    /**
     * Raportează o eroare de UI
     */
    public void reportUIError(String screen, String action, String error) {
        try {
            crashlytics.setCustomKey("ui_screen", screen);
            crashlytics.setCustomKey("ui_action", action);
            crashlytics.setCustomKey("ui_error", error);
            crashlytics.recordException(new RuntimeException("UI error: " + error));
            Log.i(TAG, "UI error reported to Crashlytics: " + screen);
        } catch (Exception e) {
            Log.e(TAG, "Error reporting UI error", e);
        }
    }

    /**
     * Raportează o eroare de joc
     */
    public void reportGameError(String region, String gameType, String error) {
        try {
            crashlytics.setCustomKey("game_region", region);
            crashlytics.setCustomKey("game_type", gameType);
            crashlytics.setCustomKey("game_error", error);
            crashlytics.recordException(new RuntimeException("Game error: " + error));
            Log.i(TAG, "Game error reported to Crashlytics: " + region);
        } catch (Exception e) {
            Log.e(TAG, "Error reporting game error", e);
        }
    }

    /**
     * Raportează o eroare de securitate
     */
    public void reportSecurityError(String threatType, String details) {
        try {
            crashlytics.setCustomKey("security_threat", threatType);
            crashlytics.setCustomKey("security_details", details);
            crashlytics.recordException(new RuntimeException("Security threat: " + threatType));
            Log.i(TAG, "Security error reported to Crashlytics: " + threatType);
        } catch (Exception e) {
            Log.e(TAG, "Error reporting security error", e);
        }
    }

    /**
     * Setează o cheie custom
     */
    public void setCustomKey(String key, String value) {
        try {
            crashlytics.setCustomKey(key, value);
        } catch (Exception e) {
            Log.e(TAG, "Error setting custom key", e);
        }
    }

    /**
     * Setează o cheie custom cu valoare numerică
     */
    public void setCustomKey(String key, int value) {
        try {
            crashlytics.setCustomKey(key, value);
        } catch (Exception e) {
            Log.e(TAG, "Error setting custom key", e);
        }
    }

    /**
     * Setează o cheie custom cu valoare booleană
     */
    public void setCustomKey(String key, boolean value) {
        try {
            crashlytics.setCustomKey(key, value);
        } catch (Exception e) {
            Log.e(TAG, "Error setting custom key", e);
        }
    }

    /**
     * Logează un mesaj
     */
    public void log(String message) {
        try {
            crashlytics.log(message);
            Log.d(TAG, "Logged to Crashlytics: " + message);
        } catch (Exception e) {
            Log.e(TAG, "Error logging to Crashlytics", e);
        }
    }

    /**
     * Logează un breadcrumb pentru debugging
     */
    public void logBreadcrumb(String category, String message) {
        try {
            String breadcrumb = String.format("[%s] %s", category, message);
            crashlytics.log(breadcrumb);
            Log.d(TAG, "Breadcrumb logged: " + breadcrumb);
        } catch (Exception e) {
            Log.e(TAG, "Error logging breadcrumb", e);
        }
    }

    /**
     * Logează un eveniment de utilizator
     */
    public void logUserEvent(String eventName, String details) {
        try {
            crashlytics.setCustomKey("last_user_event", eventName);
            crashlytics.setCustomKey("last_user_event_details", details);
            crashlytics.log("User Event: " + eventName + " - " + details);
            Log.d(TAG, "User event logged: " + eventName);
        } catch (Exception e) {
            Log.e(TAG, "Error logging user event", e);
        }
    }

    /**
     * Logează o acțiune de joc
     */
    public void logGameAction(String region, String action, String details) {
        try {
            crashlytics.setCustomKey("game_region", region);
            crashlytics.setCustomKey("game_action", action);
            crashlytics.setCustomKey("game_action_details", details);
            crashlytics.log("Game Action: " + region + " - " + action + " - " + details);
            Log.d(TAG, "Game action logged: " + action);
        } catch (Exception e) {
            Log.e(TAG, "Error logging game action", e);
        }
    }

    /**
     * Logează o acțiune de UI
     */
    public void logUIAction(String screen, String action, String details) {
        try {
            crashlytics.setCustomKey("ui_screen", screen);
            crashlytics.setCustomKey("ui_action", action);
            crashlytics.setCustomKey("ui_action_details", details);
            crashlytics.log("UI Action: " + screen + " - " + action + " - " + details);
            Log.d(TAG, "UI action logged: " + action);
        } catch (Exception e) {
            Log.e(TAG, "Error logging UI action", e);
        }
    }

    /**
     * Obține versiunea aplicației
     */
    private String getAppVersion() {
        try {
            return context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0)
                .versionName;
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * Activează/dezactivează Crashlytics
     */
    public void setCrashlyticsEnabled(boolean enabled) {
        try {
            crashlytics.setCrashlyticsCollectionEnabled(enabled);
            Log.i(TAG, "Crashlytics enabled: " + enabled);
        } catch (Exception e) {
            Log.e(TAG, "Error setting Crashlytics enabled", e);
        }
    }
} 