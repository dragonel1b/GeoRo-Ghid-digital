package com.example.myapplication.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Utility class for secure storage of sensitive information
 * Uses Android's EncryptedSharedPreferences for data encryption
 */
public class SecureStorageManager {
    private static final String TAG = "SecureStorageManager";
    private static final String ENCRYPTED_PREFS_FILENAME = "secure_game_prefs";
    
    private final Context context;
    private SharedPreferences encryptedPreferences;
    
    public SecureStorageManager(Context context) {
        this.context = context.getApplicationContext();
        initEncryptedPreferences();
    }
    
    /**
     * Initialize encrypted shared preferences
     */
    private void initEncryptedPreferences() {
        try {
            // Create or retrieve the Master Key for encryption/decryption
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build();
            
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyGenParameterSpec(keyGenParameterSpec)
                    .build();
            
            // Create the EncryptedSharedPreferences
            encryptedPreferences = EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_PREFS_FILENAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error initializing encrypted preferences", e);
            // Fallback to regular shared preferences if encryption fails
            encryptedPreferences = context.getSharedPreferences(
                    "fallback_" + ENCRYPTED_PREFS_FILENAME, Context.MODE_PRIVATE);
        }
    }
    
    /**
     * Store a string value securely
     * 
     * @param key The key to store the value under
     * @param value The value to store
     * @return true if storage was successful, false otherwise
     */
    public boolean storeString(String key, String value) {
        try {
            encryptedPreferences.edit().putString(key, value).apply();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error storing secure string for key: " + key, e);
            return false;
        }
    }
    
    /**
     * Retrieve a securely stored string value
     * 
     * @param key The key to retrieve the value for
     * @param defaultValue The default value if the key is not found
     * @return The retrieved value or defaultValue if not found
     */
    public String getString(String key, String defaultValue) {
        try {
            return encryptedPreferences.getString(key, defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving secure string for key: " + key, e);
            return defaultValue;
        }
    }
    
    /**
     * Store an integer value securely
     * 
     * @param key The key to store the value under
     * @param value The value to store
     * @return true if storage was successful, false otherwise
     */
    public boolean storeInt(String key, int value) {
        try {
            encryptedPreferences.edit().putInt(key, value).apply();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error storing secure int for key: " + key, e);
            return false;
        }
    }
    
    /**
     * Retrieve a securely stored integer value
     * 
     * @param key The key to retrieve the value for
     * @param defaultValue The default value if the key is not found
     * @return The retrieved value or defaultValue if not found
     */
    public int getInt(String key, int defaultValue) {
        try {
            return encryptedPreferences.getInt(key, defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving secure int for key: " + key, e);
            return defaultValue;
        }
    }
    
    /**
     * Store a float value securely
     * 
     * @param key The key to store the value under
     * @param value The value to store
     * @return true if storage was successful, false otherwise
     */
    public boolean storeFloat(String key, float value) {
        try {
            encryptedPreferences.edit().putFloat(key, value).apply();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error storing secure float for key: " + key, e);
            return false;
        }
    }
    
    /**
     * Retrieve a securely stored float value
     * 
     * @param key The key to retrieve the value for
     * @param defaultValue The default value if the key is not found
     * @return The retrieved value or defaultValue if not found
     */
    public float getFloat(String key, float defaultValue) {
        try {
            return encryptedPreferences.getFloat(key, defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving secure float for key: " + key, e);
            return defaultValue;
        }
    }
    
    /**
     * Check if the secure storage contains a specific key
     * 
     * @param key The key to check
     * @return true if the key exists, false otherwise
     */
    public boolean contains(String key) {
        return encryptedPreferences.contains(key);
    }
    
    /**
     * Remove a value from secure storage
     * 
     * @param key The key to remove
     */
    public void remove(String key) {
        encryptedPreferences.edit().remove(key).apply();
    }
    
    /**
     * Clear all values from secure storage
     */
    public void clearAll() {
        encryptedPreferences.edit().clear().apply();
    }
} 