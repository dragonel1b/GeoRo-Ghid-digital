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
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;

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
        
        // Set up global exception handler for Google Play Services issues
        setupGlobalExceptionHandler();
        
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
        
        // Verificăm Google Play Services
        checkGooglePlayServices();
    }
    
    
    /**
     * Set up global exception handler to catch Google Play Services SecurityExceptions
     */
    private void setupGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof SecurityException) {
                String message = throwable.getMessage();
                if (message != null && message.contains("com.google.android.gms")) {
                    Log.w(TAG, "Caught Google Play Services SecurityException: " + message);
                    
                    // Save the error status to preferences
                    SharedPreferences prefs = getSharedPreferences("app_config", Context.MODE_PRIVATE);
                    prefs.edit()
                        .putInt("google_play_services_status", ConnectionResult.SERVICE_INVALID)
                        .putLong("google_play_services_check_time", System.currentTimeMillis())
                        .putBoolean("google_play_services_security_error", true)
                        .apply();
                    
                    // Don't crash the app, just log and continue
                    return;
                }
            }
            
            // For other exceptions, use the default handler
            if (Thread.getDefaultUncaughtExceptionHandler() != null) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, throwable);
            }
        });
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
            // Verificăm dacă Firebase este deja inițializat
            if (FirebaseApp.getApps(this).isEmpty()) {
                // Inițializăm Firebase doar dacă nu este deja inițializat
                FirebaseApp.initializeApp(this);
            }
            
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
        } catch (SecurityException e) {
            Log.w(TAG, "SecurityException when initializing Firebase - using fallback mode", e);
            // În caz de eroare de securitate, continuăm fără Firebase
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
    
    /**
     * Verifică și gestionează Google Play Services
     */
    private void checkGooglePlayServices() {
        try {
            // Verificăm dacă Google Play Services sunt disponibile doar dacă este necesar
            // și doar în contextul corect pentru a evita SecurityException
            if (BuildConfig.DEBUG) {
                // În modul debug, încercăm să verificăm Google Play Services
                try {
                    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
                    int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
                    
                    if (resultCode != ConnectionResult.SUCCESS) {
                        Log.w(TAG, "Google Play Services not available: " + resultCode);
                        
                        // Salvăm statusul pentru a-l verifica în activități
                        SharedPreferences prefs = getSharedPreferences("app_config", Context.MODE_PRIVATE);
                        prefs.edit()
                            .putInt("google_play_services_status", resultCode)
                            .putLong("google_play_services_check_time", System.currentTimeMillis())
                            .apply();
                            
                        // Dacă serviciile nu sunt disponibile, dezactivăm funcționalitățile care le necesită
                        if (apiAvailability.isUserResolvableError(resultCode)) {
                            Log.i(TAG, "Google Play Services error is user resolvable");
                        } else {
                            Log.w(TAG, "Google Play Services error is not user resolvable");
                        }
                    } else {
                        Log.i(TAG, "Google Play Services available");
                        
                        // Salvăm statusul pozitiv
                        SharedPreferences prefs = getSharedPreferences("app_config", Context.MODE_PRIVATE);
                        prefs.edit()
                            .putInt("google_play_services_status", ConnectionResult.SUCCESS)
                            .putLong("google_play_services_check_time", System.currentTimeMillis())
                            .apply();
                    }
                } catch (SecurityException e) {
                    Log.w(TAG, "SecurityException when checking Google Play Services - using fallback mode", e);
                    
                    // Salvăm statusul ca fiind indisponibil din cauza erorii de securitate
                    SharedPreferences prefs = getSharedPreferences("app_config", Context.MODE_PRIVATE);
                    prefs.edit()
                        .putInt("google_play_services_status", ConnectionResult.SERVICE_INVALID)
                        .putLong("google_play_services_check_time", System.currentTimeMillis())
                        .apply();
                }
            } else {
                // În modul release, presupunem că Google Play Services sunt disponibile
                // pentru a evita problemele de securitate
                Log.i(TAG, "Release mode - assuming Google Play Services available");
                
                SharedPreferences prefs = getSharedPreferences("app_config", Context.MODE_PRIVATE);
                prefs.edit()
                    .putInt("google_play_services_status", ConnectionResult.SUCCESS)
                    .putLong("google_play_services_check_time", System.currentTimeMillis())
                    .apply();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking Google Play Services", e);
            
            // În caz de eroare, presupunem că serviciile nu sunt disponibile
            SharedPreferences prefs = getSharedPreferences("app_config", Context.MODE_PRIVATE);
            prefs.edit()
                .putInt("google_play_services_status", ConnectionResult.SERVICE_INVALID)
                .putLong("google_play_services_check_time", System.currentTimeMillis())
                .apply();
        }
    }
    
    /**
     * Verifică dacă Google Play Services sunt disponibile
     */
    public static boolean isGooglePlayServicesAvailable() {
        try {
            SharedPreferences prefs = appContext.getSharedPreferences("app_config", Context.MODE_PRIVATE);
            int status = prefs.getInt("google_play_services_status", -1);
            boolean hasSecurityError = prefs.getBoolean("google_play_services_security_error", false);
            
            // If there was a security error, consider Google Play Services unavailable
            if (hasSecurityError) {
                Log.w(TAG, "Google Play Services unavailable due to security error");
                return false;
            }
            
            return status == ConnectionResult.SUCCESS;
        } catch (Exception e) {
            Log.e(TAG, "Error checking Google Play Services availability", e);
            return false;
        }
    }
}
