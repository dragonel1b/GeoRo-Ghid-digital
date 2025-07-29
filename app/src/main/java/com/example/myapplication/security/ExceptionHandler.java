package com.example.myapplication.security;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Centralized exception handling utility
 * Provides consistent error handling, logging, and user feedback
 */
public class ExceptionHandler {
    private static final String TAG = "ExceptionHandler";
    private static final String LOG_FILE_PREFIX = "error_log_";
    private static final String LOG_FILE_EXTENSION = ".txt";
    
    /**
     * Handle exceptions in a consistent manner
     * 
     * @param context The context where the exception occurred
     * @param exception The exception to handle
     * @param userMessage Message to show to the user
     * @param isCritical If true, indicates a critical error that might require app termination
     */
    public static void handleException(Context context, Exception exception, String userMessage, boolean isCritical) {
        // Log the exception
        logException(context, exception);
        
        // Show user feedback on the main thread
        if (context != null) {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (context instanceof Activity && !((Activity) context).isFinishing()) {
                    showErrorMessage(context, userMessage);
                }
            });
        }
        
        // For critical exceptions, consider additional actions
        if (isCritical) {
            // Send error report to server or terminate gracefully
            // This is where you might add crash reporting like Firebase Crashlytics
        }
    }
    
    /**
     * Log exception to both system log and internal file
     * 
     * @param context The context where the exception occurred
     * @param exception The exception to log
     */
    private static void logException(Context context, Exception exception) {
        // Log to system log
        Log.e(TAG, "Exception occurred: ", exception);
        
        // Log to file for future analysis
        if (context != null) {
            try {
                // Convert exception to string
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                exception.printStackTrace(pw);
                String stackTrace = sw.toString();
                
                // Create log entry with timestamp
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String timestamp = sdf.format(new Date());
                String logEntry = timestamp + "\n" + exception.getMessage() + "\n" + stackTrace + "\n\n";
                
                // Write to internal file
                String fileName = LOG_FILE_PREFIX + System.currentTimeMillis() + LOG_FILE_EXTENSION;
                FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                fos.write(logEntry.getBytes());
                fos.close();
                
            } catch (Exception e) {
                Log.e(TAG, "Error writing exception to log file", e);
            }
        }
    }
    
    /**
     * Show error message to user
     * 
     * @param context Context to show message in
     * @param message Message to display
     */
    private static void showErrorMessage(Context context, String message) {
        try {
            if (context instanceof Activity) {
                Snackbar.make(
                        ((Activity) context).findViewById(android.R.id.content),
                        message,
                        Snackbar.LENGTH_LONG
                ).show();
            } else {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing error message", e);
        }
    }
    
    /**
     * Implementation of Thread.UncaughtExceptionHandler to catch unhandled exceptions
     */
    public static class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Context applicationContext;
        private final Thread.UncaughtExceptionHandler defaultHandler;
        
        public GlobalExceptionHandler(Context context) {
            this.applicationContext = context.getApplicationContext();
            this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        }
        
        @Override
        public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
            try {
                // First, let Crashlytics handle the crash
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, throwable);
                }
                
                // Then log the uncaught exception for our own tracking
                if (throwable instanceof Exception) {
                    logException(applicationContext, (Exception) throwable);
                } else {
                    // Convert Throwable to Exception for logging
                    Exception exception = new Exception(throwable);
                    logException(applicationContext, exception);
                }
                
                // You can add custom recovery logic here
                // For example, restart the app or specific components
                
            } catch (Exception e) {
                Log.e(TAG, "Error in uncaught exception handler", e);
            }
        }
        
        /**
         * Register this handler as the global uncaught exception handler
         */
        public void register() {
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }
} 