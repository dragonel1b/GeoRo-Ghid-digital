package com.example.myapplication.security;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

/**
 * Central security manager that coordinates all security-related operations
 * Provides a single entry point for security functions across the application
 */
public class SecurityManager {
    private static final String TAG = "SecurityManager";
    private static SecurityManager instance;
    
    private final Context applicationContext;
    private final SecurityAuditor securityAuditor;
    private final SecureStorageManager secureStorageManager;
    private final ExceptionHandler.GlobalExceptionHandler globalExceptionHandler;
    
    private SecurityManager(Context context) {
        this.applicationContext = context.getApplicationContext();
        this.securityAuditor = new SecurityAuditor(applicationContext);
        this.secureStorageManager = new SecureStorageManager(applicationContext);
        this.globalExceptionHandler = new ExceptionHandler.GlobalExceptionHandler(applicationContext);
        
        // Register global exception handler
        globalExceptionHandler.register();
    }
    
    /**
     * Get the singleton instance of SecurityManager
     *
     * @param context Application context
     * @return Instance of SecurityManager
     */
    public static synchronized SecurityManager getInstance(Context context) {
        if (instance == null) {
            instance = new SecurityManager(context);
        }
        return instance;
    }
    
    /**
     * Initialize security components
     * Should be called during application startup
     */
    public void initializeSecurity() {
        Log.i(TAG, "Initializing security components");
        
        // Perform initial security audit
        if (!securityAuditor.performSecurityAudit()) {
            handleSecurityThreat(securityAuditor.getDetectedThreats());
        }
    }
    
    /**
     * Handle security threats based on severity
     *
     * @param detectedThreats List of detected threats
     */
    private void handleSecurityThreat(List<String> detectedThreats) {
        // Log all threats
        Log.w(TAG, "Security threats detected: " + detectedThreats);
        
        // Determine if any critical threats are present
        boolean hasCriticalThreats = detectedThreats.stream()
                .anyMatch(threat -> threat.equals("SignatureTampering") || 
                                   threat.startsWith("DangerousApp:"));
        
        // Store security incident
        storeSecurityIncident(detectedThreats);
        
        // For critical threats, consider taking stronger actions
        if (hasCriticalThreats) {
            Log.e(TAG, "Critical security threats detected!");
            // Here you could implement actions like:
            // - Logging the user out
            // - Restricting access to sensitive features
            // - Sending a security alert to your backend
            // - In extreme cases, disabling the app
        }
    }
    
    /**
     * Store security incident information securely
     *
     * @param detectedThreats List of detected threats
     */
    private void storeSecurityIncident(List<String> detectedThreats) {
        try {
            // Store timestamp and threat information
            long timestamp = System.currentTimeMillis();
            String threatInfo = String.join(",", detectedThreats);
            
            // Store in secure storage
            secureStorageManager.storeString("security_incident_" + timestamp, threatInfo);
            
            // Update incident count
            int incidentCount = secureStorageManager.getInt("security_incident_count", 0);
            secureStorageManager.storeInt("security_incident_count", incidentCount + 1);
            
        } catch (Exception e) {
            Log.e(TAG, "Error storing security incident", e);
        }
    }
    
    /**
     * Validate input data
     *
     * @param input Input to validate
     * @return true if valid, false otherwise
     */
    public boolean validateInput(String input) {
        return InputValidator.isSafeText(input);
    }
    
    /**
     * Sanitize input data
     *
     * @param input Input to sanitize
     * @return Sanitized input
     */
    public String sanitizeInput(String input) {
        return InputValidator.sanitizeText(input);
    }
    
    /**
     * Handle exceptions in a consistent manner
     *
     * @param context Context where the exception occurred
     * @param exception Exception to handle
     * @param userMessage Message to display to the user
     * @param isCritical Whether this is a critical exception
     */
    public void handleException(Context context, Exception exception, String userMessage, boolean isCritical) {
        ExceptionHandler.handleException(context, exception, userMessage, isCritical);
    }
    
    /**
     * Store data securely
     *
     * @param key Storage key
     * @param value Value to store
     * @return true if successful, false otherwise
     */
    public boolean storeSecureData(String key, String value) {
        return secureStorageManager.storeString(key, value);
    }
    
    /**
     * Retrieve securely stored data
     *
     * @param key Storage key
     * @param defaultValue Default value if key not found
     * @return Stored value or defaultValue
     */
    public String getSecureData(String key, String defaultValue) {
        return secureStorageManager.getString(key, defaultValue);
    }
    
    /**
     * Store integer value securely
     *
     * @param key Storage key
     * @param value Value to store
     * @return true if successful, false otherwise
     */
    public boolean storeSecureInt(String key, int value) {
        return secureStorageManager.storeInt(key, value);
    }
    
    /**
     * Retrieve securely stored integer
     *
     * @param key Storage key
     * @param defaultValue Default value if key not found
     * @return Stored value or defaultValue
     */
    public int getSecureInt(String key, int defaultValue) {
        return secureStorageManager.getInt(key, defaultValue);
    }
    
    /**
     * Perform security validation on an intent
     * Helps prevent intent-based attacks
     *
     * @param intent Intent to validate
     * @return true if intent appears safe, false otherwise
     */
    public boolean validateIntent(Intent intent) {
        if (intent == null) {
            return false;
        }
        
        // Check for null action or component
        if (intent.getAction() == null && intent.getComponent() == null) {
            Log.w(TAG, "Intent has null action and component");
            return false;
        }
        
        // Additional intent validation logic could be added here
        // For example, checking specific extras or flags
        
        return true;
    }
    
    /**
     * Perform a security audit at critical points
     * Should be called at application start and during sensitive operations
     *
     * @return true if environment appears secure, false otherwise
     */
    public boolean performSecurityAudit() {
        boolean isSecure = securityAuditor.performSecurityAudit();
        
        if (!isSecure) {
            handleSecurityThreat(securityAuditor.getDetectedThreats());
        }
        
        return isSecure;
    }
} 