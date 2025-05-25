package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.myapplication.security.SecurityManager;

/**
 * Main Application class for initializing app-wide components
 */
public class RomApplication extends Application {
    private static final String TAG = "RomApplication";
    
    private SecurityManager securityManager;
    private static Context appContext;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Store application context
        appContext = getApplicationContext();
        
        // Initialize security framework
        initializeSecurity();
        
        // Other application-wide initializations can go here
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
} 