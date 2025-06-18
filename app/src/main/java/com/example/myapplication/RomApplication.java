package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.example.myapplication.security.SecurityManager;
import android.content.SharedPreferences;
import com.example.myapplication.config.FloggerConfig;
import com.google.common.flogger.FluentLogger;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.auth.FirebaseAuth;

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
        
        // Inițializăm Firebase
        initializeFirebase();
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
        // Configurăm StrictMode doar pentru mediul de dezvoltare
        if (BuildConfig.DEBUG) {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
                    .detectNetwork()
                    .detectCustomSlowCalls()
                    .detectResourceMismatches();
            
            // Permite anumite operații de disc pentru UI fluent și previne false positives
            // Poate fi activat complet pentru depanare detaliată
            // .detectDiskReads()
            // .detectDiskWrites()
            
            // Setăm doar penalitatea de log, nu penalități de crash
            threadPolicyBuilder.penaltyLog();
            
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            
            StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectActivityLeaks()
                    .detectLeakedRegistrationObjects();
            
            // Setăm doar penalitatea de log, nu penalități de crash
            vmPolicyBuilder.penaltyLog();
            
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
            
            Log.d(TAG, "StrictMode enabled with customized policies for development");
        }
    }
    
    /**
     * Inițializează Firebase și configurează Firestore
     */
    private void initializeFirebase() {
        try {
            // Inițializăm Firebase
            FirebaseApp.initializeApp(this);
            
            // Configurăm Firestore pentru performanță optimă
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)  // Activăm cache-ul offline
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)  // Cache nelimitat
                .build();
            db.setFirestoreSettings(settings);
            
            // Sincronizăm punctele cu Firebase
            syncPointsWithFirebase();
            
            Log.d(TAG, "Firebase inițializat cu succes");
        } catch (Exception e) {
            Log.e(TAG, "Eroare la inițializarea Firebase", e);
        }
    }
    
    /**
     * Sincronizează punctele locale cu Firebase
     */
    private void syncPointsWithFirebase() {
        // Verificăm dacă utilizatorul este autentificat
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // Încărcăm punctele din Firebase în stocarea locală
            com.example.myapplication.RomApp.PointsManager.getInstance(this)
                .loadPointsFromFirebase(this);
        }
    }
}
