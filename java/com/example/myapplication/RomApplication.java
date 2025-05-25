package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.example.myapplication.security.SecurityManager;
import android.content.SharedPreferences;
import com.example.myapplication.config.FloggerConfig;
import com.google.common.flogger.FluentLogger;

/**
 * Main Application class for initializing app-wide components
 */
public class RomApplication extends Application {
    private static final String TAG = "RomApplication";
    
    private SecurityManager securityManager;
    private static Context appContext;
    private static boolean loggingAlreadyConfigured = false;
    
    /**
     * Static initializer block to configure logging as early as possible
     * This runs before any other code in the application
     */
    static {
        // Use centralized Flogger configuration
        FloggerConfig.configure();
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        // Ensure Flogger is configured before attaching base context
        if (!FloggerConfig.isConfigured()) {
            FloggerConfig.configure();
        }
        super.attachBaseContext(base);
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Ensure Flogger is configured (should already be done in static block)
        if (!FloggerConfig.isConfigured()) {
            FloggerConfig.configure();
        }
        
        // Save the logging configuration
        saveLoggingConfiguration();
        
        // Optional: Enable StrictMode for development builds
        if (BuildConfig.DEBUG) {
            enableStrictMode();
        }
        
        // Initialize other app-wide components here
        Log.i(TAG, "RomApplication initialized");
        
        // Store application context
        appContext = getApplicationContext();
        
        // Initialize security framework
        initializeSecurity();
    }
    
    
    /**
     * Save logging configuration to preferences so activities can check it
     */
    private void saveLoggingConfiguration() {
        try {
            SharedPreferences prefs = getSharedPreferences("app_config", Context.MODE_PRIVATE);
            prefs.edit()
                .putBoolean("logging_disabled", true)
                .putLong("logging_config_time", System.currentTimeMillis())
                .apply();
        } catch (Exception e) {
            // Ignore
        }
    }
    
    
    
    /**
     * Initialize all security-related components
     */
    private void initializeSecurity() {
        try {
            Log.i(TAG, "Initializing security framework");
            
            // Initialize security manager
            securityManager = SecurityManager.getInstance(this);
            securityManager.initializeSecurity();
            
            // Perform initial security audit
            boolean isSecure = securityManager.performSecurityAudit();
            Log.i(TAG, "Initial security audit result: " + (isSecure ? "Secure" : "Insecure"));
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing security framework", e);
        }
    }
    
    /**
     * Get the application's security manager instance
     * 
     * @return SecurityManager instance
     */
    public SecurityManager getSecurityManager() {
        return securityManager;
    }
    
    /**
     * Get the application context
     * @return Application context
     */
    public static Context getAppContext() {
        return appContext;
    }
    
    private void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
                
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build());
    }
}
