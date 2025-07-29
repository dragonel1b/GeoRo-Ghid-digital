package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Bug tracking system for the application
 * Provides centralized error reporting and issue tracking
 */
public class BugTracker {
    private static final String TAG = "BugTracker";
    private static final String PREFS_NAME = "bug_tracker_prefs";
    private static final String KEY_BUG_COUNT = "bug_count";
    private static final String KEY_LAST_REPORT_TIME = "last_report_time";
    
    private static BugTracker instance;
    private final Context context;
    private final SharedPreferences prefs;
    private final FirebaseFirestore firestore;
    private final ExecutorService executorService;
    
    // Bug severity levels
    public enum Severity {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        CRITICAL("Critical");
        
        private final String displayName;
        
        Severity(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Bug categories
    public enum Category {
        UI_UX("UI/UX"),
        PERFORMANCE("Performance"),
        NETWORK("Network"),
        SECURITY("Security"),
        DATA("Data"),
        GAMEPLAY("Gameplay"),
        OTHER("Other");
        
        private final String displayName;
        
        Category(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private BugTracker(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.firestore = FirebaseFirestore.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Get singleton instance of BugTracker
     */
    public static synchronized BugTracker getInstance(Context context) {
        if (instance == null) {
            instance = new BugTracker(context);
        }
        return instance;
    }
    
    /**
     * Report a bug with detailed information
     * 
     * @param title Bug title
     * @param description Detailed description
     * @param severity Bug severity level
     * @param category Bug category
     * @param stackTrace Stack trace if available
     * @param deviceInfo Device information
     * @param appVersion App version
     */
    public void reportBug(String title, String description, Severity severity, 
                         Category category, String stackTrace, String deviceInfo, String appVersion) {
        
        executorService.execute(() -> {
            try {
                // Create bug report
                Map<String, Object> bugReport = new HashMap<>();
                bugReport.put("title", title);
                bugReport.put("description", description);
                bugReport.put("severity", severity.getDisplayName());
                bugReport.put("category", category.getDisplayName());
                bugReport.put("stackTrace", stackTrace);
                bugReport.put("deviceInfo", deviceInfo);
                bugReport.put("appVersion", appVersion);
                bugReport.put("timestamp", new Date());
                bugReport.put("status", "Open");
                bugReport.put("reportedBy", "User");
                
                // Save to Firestore
                firestore.collection("bug_reports")
                    .add(bugReport)
                    .addOnSuccessListener(documentReference -> {
                        Log.i(TAG, "Bug report saved successfully: " + documentReference.getId());
                        updateBugCount();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving bug report", e);
                        saveBugLocally(bugReport);
                    });
                    
            } catch (Exception e) {
                Log.e(TAG, "Error creating bug report", e);
            }
        });
    }
    
    /**
     * Report a bug with minimal information
     * 
     * @param title Bug title
     * @param description Bug description
     * @param severity Bug severity
     */
    public void reportBug(String title, String description, Severity severity) {
        String deviceInfo = getDeviceInfo();
        String appVersion = getAppVersion();
        
        reportBug(title, description, severity, Category.OTHER, null, deviceInfo, appVersion);
    }
    
    /**
     * Report an exception
     * 
     * @param exception The exception to report
     * @param context Additional context
     */
    public void reportException(Exception exception, String context) {
        String title = "Exception: " + exception.getClass().getSimpleName();
        String description = "Exception occurred in: " + context + "\nMessage: " + exception.getMessage();
        String stackTrace = getStackTrace(exception);
        
        reportBug(title, description, Severity.HIGH, Category.OTHER, stackTrace, 
                 getDeviceInfo(), getAppVersion());
    }
    
    /**
     * Report a UI/UX issue
     * 
     * @param title Issue title
     * @param description Issue description
     * @param screenName Screen where issue occurred
     */
    public void reportUIUXIssue(String title, String description, String screenName) {
        String fullDescription = description + "\nScreen: " + screenName;
        reportBug(title, fullDescription, Severity.MEDIUM);
    }
    
    /**
     * Report a performance issue
     * 
     * @param title Issue title
     * @param description Issue description
     * @param performanceMetrics Performance metrics
     */
    public void reportPerformanceIssue(String title, String description, String performanceMetrics) {
        String fullDescription = description + "\nMetrics: " + performanceMetrics;
        reportBug(title, fullDescription, Severity.MEDIUM);
    }
    
    /**
     * Report a network issue
     * 
     * @param title Issue title
     * @param description Issue description
     * @param endpoint Affected endpoint
     */
    public void reportNetworkIssue(String title, String description, String endpoint) {
        String fullDescription = description + "\nEndpoint: " + endpoint;
        reportBug(title, fullDescription, Severity.HIGH);
    }
    
    /**
     * Report a security issue
     * 
     * @param title Issue title
     * @param description Issue description
     * @param securityLevel Security level
     */
    public void reportSecurityIssue(String title, String description, String securityLevel) {
        String fullDescription = description + "\nSecurity Level: " + securityLevel;
        reportBug(title, fullDescription, Severity.CRITICAL);
    }
    
    /**
     * Report a gameplay issue
     * 
     * @param title Issue title
     * @param description Issue description
     * @param region Affected region
     * @param gameType Game type
     */
    public void reportGameplayIssue(String title, String description, String region, String gameType) {
        String fullDescription = description + "\nRegion: " + region + "\nGame Type: " + gameType;
        reportBug(title, fullDescription, Severity.MEDIUM);
    }
    
    /**
     * Update bug count in preferences
     */
    private void updateBugCount() {
        int currentCount = prefs.getInt(KEY_BUG_COUNT, 0);
        prefs.edit()
            .putInt(KEY_BUG_COUNT, currentCount + 1)
            .putLong(KEY_LAST_REPORT_TIME, System.currentTimeMillis())
            .apply();
    }
    
    /**
     * Save bug report locally if Firestore fails
     */
    private void saveBugLocally(Map<String, Object> bugReport) {
        try {
            String bugId = "local_" + System.currentTimeMillis();
            prefs.edit()
                .putString("bug_" + bugId, bugReport.toString())
                .apply();
            Log.i(TAG, "Bug report saved locally: " + bugId);
        } catch (Exception e) {
            Log.e(TAG, "Error saving bug report locally", e);
        }
    }
    
    /**
     * Get device information
     */
    private String getDeviceInfo() {
        return android.os.Build.MANUFACTURER + " " + 
               android.os.Build.MODEL + " " + 
               android.os.Build.VERSION.RELEASE;
    }
    
    /**
     * Get app version
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
     * Get stack trace from exception
     */
    private String getStackTrace(Exception exception) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * Get bug count
     */
    public int getBugCount() {
        return prefs.getInt(KEY_BUG_COUNT, 0);
    }
    
    /**
     * Get last report time
     */
    public long getLastReportTime() {
        return prefs.getLong(KEY_LAST_REPORT_TIME, 0);
    }
    
    /**
     * Clear bug tracking data
     */
    public void clearBugData() {
        prefs.edit().clear().apply();
    }
    
    /**
     * Shutdown the bug tracker
     */
    public void shutdown() {
        executorService.shutdown();
    }
} 