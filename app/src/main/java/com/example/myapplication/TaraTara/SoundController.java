package com.example.myapplication.TaraTara;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;

import com.example.myapplication.R;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages all audio and haptic feedback for the game
 */
public class SoundController {
    private static final String TAG = "SoundController";
    
    // Context reference
    private final Context context;
    
    // Audio components
    private SoundPool soundPool;
    private MediaPlayer backgroundMusic;
    private final Vibrator vibrator;
    
    // Sound resource mappings
    private final Map<SoundType, Integer> soundMap = new HashMap<>();
    private final SparseArray<String> loadedSounds = new SparseArray<>();
    
    // Volume controls
    private float soundVolume = 1.0f;
    private float musicVolume = 0.5f;
    private boolean soundEnabled = true;
    private boolean vibrationEnabled = true;
    
    // Background thread for loading sounds
    private final ExecutorService soundLoadExecutor = Executors.newSingleThreadExecutor();
    
    // Sound types
    public enum SoundType {
        SHOUT,
        ANSWER,
        SELECT,
        CHASE,
        SUCCESS,
        FAILURE,
        BUTTON_CLICK,
        ROUND_START,
        GAME_OVER
    }
    
    // Vibration patterns
    public enum VibrationPattern {
        BUTTON_PRESS(new long[]{0, 30}, -1),
        SELECT(new long[]{0, 20, 50, 20}, -1),
        SUCCESS(new long[]{0, 100, 50, 100}, -1),
        FAILURE(new long[]{0, 250}, -1),
        WARNING(new long[]{0, 100, 100, 100, 100, 100}, -1);
        
        final long[] timings;
        final int repeat;
        
        VibrationPattern(long[] timings, int repeat) {
            this.timings = timings;
            this.repeat = repeat;
        }
    }

    public SoundController(Context context) {
        this.context = context;
        
        // Initialize vibrator
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        
        // Initialize sound system
        initSoundPool();
        
        // Preload all sounds
        preloadSounds();
    }

    /**
     * Initialize the sound pool with appropriate settings based on API level
     */
    private void initSoundPool() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                
                soundPool = new SoundPool.Builder()
                        .setMaxStreams(10)
                        .setAudioAttributes(audioAttributes)
                        .build();
            } else {
                // For older devices
                soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            }
            
            // Set up load completion listener
            soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
                if (status == 0) {
                    Log.d(TAG, "Sound loaded successfully: " + loadedSounds.get(sampleId));
                } else {
                    Log.e(TAG, "Failed to load sound: " + loadedSounds.get(sampleId));
                }
            });
            
            Log.d(TAG, "SoundPool initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing SoundPool", e);
        }
    }

    /**
     * Preload all game sounds for faster playback
     */
    private void preloadSounds() {
        soundLoadExecutor.execute(() -> {
            try {
                // Map sound types to resource IDs
                // We only have shout.mp3 available, so we'll use that for all sounds for now
                // This will prevent the compile errors while we work on adding the proper sounds
                int shoutSoundId = R.raw.shout;
                int clickSoundId = R.raw.click_sound;
                
                // Load available sounds
                loadSound(SoundType.SHOUT, shoutSoundId);
                
                // Use click_sound.mp3 for button click
                loadSound(SoundType.BUTTON_CLICK, clickSoundId);
                
                // Use existing sounds as fallbacks for missing ones
                loadSound(SoundType.ANSWER, shoutSoundId);
                loadSound(SoundType.SELECT, clickSoundId);
                loadSound(SoundType.CHASE, shoutSoundId);
                loadSound(SoundType.SUCCESS, clickSoundId);
                loadSound(SoundType.FAILURE, shoutSoundId);
                loadSound(SoundType.ROUND_START, clickSoundId);
                loadSound(SoundType.GAME_OVER, shoutSoundId);
                
                Log.d(TAG, "All sounds preloaded successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error preloading sounds", e);
            }
        });
    }

    /**
     * Load a sound into the sound pool and map it to a sound type
     */
    private void loadSound(SoundType type, int resId) {
        try {
            int soundId = soundPool.load(context, resId, 1);
            soundMap.put(type, soundId);
            loadedSounds.put(soundId, type.name());
            Log.d(TAG, "Loaded sound " + type.name() + " with ID " + soundId);
        } catch (Exception e) {
            Log.e(TAG, "Error loading sound: " + type.name(), e);
        }
    }

    /**
     * Play a sound with the specified type
     */
    public void playSound(SoundType type) {
        if (!soundEnabled) return;
        
        try {
            Integer soundId = soundMap.get(type);
            if (soundId != null) {
                float volume = soundVolume;
                soundPool.play(soundId, volume, volume, 1, 0, 1.0f);
                Log.d(TAG, "Playing sound: " + type.name());
            } else {
                Log.w(TAG, "Sound not loaded: " + type.name());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing sound: " + type.name(), e);
        }
    }

    /**
     * Play a sound with the specified type, looping if needed
     */
    public void playSound(SoundType type, boolean loop) {
        if (!soundEnabled) return;
        
        try {
            Integer soundId = soundMap.get(type);
            if (soundId != null) {
                float volume = soundVolume;
                soundPool.play(soundId, volume, volume, 1, loop ? -1 : 0, 1.0f);
                Log.d(TAG, "Playing sound: " + type.name() + (loop ? " (looping)" : ""));
            } else {
                Log.w(TAG, "Sound not loaded: " + type.name());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing sound: " + type.name(), e);
        }
    }

    /**
     * Stop a currently playing sound
     */
    public void stopSound(SoundType type) {
        try {
            Integer soundId = soundMap.get(type);
            if (soundId != null) {
                soundPool.stop(soundId);
                Log.d(TAG, "Stopped sound: " + type.name());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping sound: " + type.name(), e);
        }
    }

    /**
     * Play background music
     */
    public void playBackgroundMusic(int resId) {
        if (!soundEnabled) return;
        
        try {
            // Release any existing music
            stopBackgroundMusic();
            
            // Create and prepare new MediaPlayer
            backgroundMusic = MediaPlayer.create(context, resId);
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(musicVolume, musicVolume);
            backgroundMusic.start();
            
            Log.d(TAG, "Background music started");
        } catch (Exception e) {
            Log.e(TAG, "Error playing background music", e);
        }
    }

    /**
     * Stop the background music if playing
     */
    public void stopBackgroundMusic() {
        try {
            if (backgroundMusic != null) {
                if (backgroundMusic.isPlaying()) {
                    backgroundMusic.stop();
                }
                backgroundMusic.release();
                backgroundMusic = null;
                Log.d(TAG, "Background music stopped and released");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping background music", e);
        }
    }

    /**
     * Pause all sounds (for app pause/resume lifecycle)
     */
    public void pauseSounds() {
        try {
            if (soundPool != null) {
                soundPool.autoPause();
            }
            
            if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                backgroundMusic.pause();
            }
            
            Log.d(TAG, "All sounds paused");
        } catch (Exception e) {
            Log.e(TAG, "Error pausing sounds", e);
        }
    }

    /**
     * Resume all sounds (for app pause/resume lifecycle)
     */
    public void resumeSounds() {
        try {
            if (soundPool != null) {
                soundPool.autoResume();
            }
            
            if (backgroundMusic != null && !backgroundMusic.isPlaying() && soundEnabled) {
                backgroundMusic.start();
            }
            
            Log.d(TAG, "All sounds resumed");
        } catch (Exception e) {
            Log.e(TAG, "Error resuming sounds", e);
        }
    }

    /**
     * Release all audio resources
     */
    public void release() {
        try {
            stopBackgroundMusic();
            
            if (soundPool != null) {
                soundPool.release();
                soundPool = null;
            }
            
            soundLoadExecutor.shutdownNow();
            
            Log.d(TAG, "All sound resources released");
        } catch (Exception e) {
            Log.e(TAG, "Error releasing sound resources", e);
        }
    }

    /**
     * Vibrate with a specified pattern
     */
    public void vibrate(VibrationPattern pattern) {
        if (!vibrationEnabled || vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern.timings, pattern.repeat));
            } else {
                vibrator.vibrate(pattern.timings, pattern.repeat);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during vibration", e);
        }
    }

    /**
     * Stop any ongoing vibration
     */
    public void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    /**
     * Toggle sound on/off
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        
        if (!enabled) {
            // Stop all playing sounds
            if (soundPool != null) {
                for (int i = 0; i < loadedSounds.size(); i++) {
                    soundPool.stop(loadedSounds.keyAt(i));
                }
            }
            
            stopBackgroundMusic();
        } else if (backgroundMusic != null) {
            // Resume background music if it was previously created
            backgroundMusic.start();
        }
        
        Log.d(TAG, "Sound " + (enabled ? "enabled" : "disabled"));
    }

    /**
     * Toggle vibration on/off
     */
    public void setVibrationEnabled(boolean enabled) {
        this.vibrationEnabled = enabled;
        
        if (!enabled && vibrator != null) {
            vibrator.cancel();
        }
        
        Log.d(TAG, "Vibration " + (enabled ? "enabled" : "disabled"));
    }

    /**
     * Set the sound effects volume
     */
    public void setSoundVolume(float volume) {
        this.soundVolume = Math.max(0.0f, Math.min(1.0f, volume));
        Log.d(TAG, "Sound volume set to " + this.soundVolume);
    }

    /**
     * Set the background music volume
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(musicVolume, musicVolume);
        }
        
        Log.d(TAG, "Music volume set to " + this.musicVolume);
    }

    /**
     * Check if sound is enabled
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Check if vibration is enabled
     */
    public boolean isVibrationEnabled() {
        return vibrationEnabled;
    }
}
