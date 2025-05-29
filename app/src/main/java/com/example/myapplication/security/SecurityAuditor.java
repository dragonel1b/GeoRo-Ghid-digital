package com.example.myapplication.security;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Debug;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Security auditor to detect potential security threats
 * Checks for signs of tampering, debugging, and known malicious scenarios
 */
public class SecurityAuditor {
    private static final String TAG = "SecurityAuditor";
    
    private final Context context;
    private final List<String> detectedThreats = new ArrayList<>();
    
    // List of potentially dangerous tools or apps
    private static final String[] DANGEROUS_APPS = {
            "com.noshufou.android.su",
            "com.thirdparty.superuser",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.zachspong.temprootremovejb",
            "com.ramdroid.appquarantine",
            "com.topjohnwu.magisk"
    };
    
    public SecurityAuditor(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Perform a comprehensive security audit
     * 
     * @return True if the environment appears to be secure
     */
    public boolean performSecurityAudit() {
        detectedThreats.clear();
        
        // Run security checks
        checkDebuggerAttached();
        checkEmulator();
        checkDeveloperMode();
        checkRootAccess();
        checkDangerousApps();
        checkTamperingSignatures();
        
        // Log any detected threats
        if (!detectedThreats.isEmpty()) {
            Log.w(TAG, "Security threats detected: " + detectedThreats.toString());
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if a debugger is attached
     */
    private void checkDebuggerAttached() {
        if (Debug.isDebuggerConnected()) {
            detectedThreats.add("DebuggerAttached");
        }
    }
    
    /**
     * Check if app is running in an emulator
     */
    private void checkEmulator() {
        if (isEmulator()) {
            detectedThreats.add("Emulator");
        }
    }
    
    /**
     * Check if developer mode is enabled
     */
    private void checkDeveloperMode() {
        int devOptions = Settings.Secure.getInt(context.getContentResolver(), 
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
        
        if (devOptions == 1) {
            detectedThreats.add("DeveloperMode");
        }
    }
    
    /**
     * Check for signs of root access
     */
    private void checkRootAccess() {
        if (checkRootMethod1() || checkRootMethod2() || checkRootMethod3()) {
            detectedThreats.add("RootAccess");
        }
    }
    
    /**
     * Check for root method 1: Check common root paths
     */
    private boolean checkRootMethod1() {
        String[] rootPaths = {
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"
        };
        
        for (String path : rootPaths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check for root method 2: Try to execute su command
     */
    private boolean checkRootMethod2() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return in.readLine() != null;
        } catch (Exception e) {
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
    
    /**
     * Check for root method 3: Check for Magisk Hide
     */
    private boolean checkRootMethod3() {
        return new File("/sbin/.magisk").exists() || 
               new File("/sbin/.core").exists() ||
               new File("/sbin/.su").exists();
    }
    
    /**
     * Check for known dangerous apps
     */
    private void checkDangerousApps() {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        
        for (ApplicationInfo app : installedApps) {
            if (Arrays.asList(DANGEROUS_APPS).contains(app.packageName)) {
                detectedThreats.add("DangerousApp:" + app.packageName);
                break;
            }
        }
    }
    
    /**
     * Check if app has been tampered with
     */
    private void checkTamperingSignatures() {
        try {
            if (context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 
                    PackageManager.GET_SIGNATURES).signatures.length != 1) {
                detectedThreats.add("SignatureTampering");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking signatures", e);
        }
    }
    
    /**
     * Helper to detect if running on an emulator
     */
    private boolean isEmulator() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator");
    }
    
    /**
     * Get a list of detected security threats
     */
    public List<String> getDetectedThreats() {
        return new ArrayList<>(detectedThreats);
    }
} 