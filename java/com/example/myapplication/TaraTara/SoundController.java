package com.example.myapplication.TaraTara;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import com.example.myapplication.R;

public class SoundController {
    private final Context context;
    private SoundPool soundPool;
    private int soundShout;
    private int soundAnswer;
    private int soundSuccess;
    private int soundFailure;
    private int soundVictory;
    private int soundDefeat;

    public enum SoundType {
        SHOUT,
        ANSWER,
        SUCCESS,
        FAILURE,
        VICTORY,
        DEFEAT
    }

    public enum VibrationPattern {
        SUCCESS,
        FAILURE,
        VICTORY,
        DEFEAT,
        BUTTON_PRESS
    }

    public SoundController(Context context) {
        this.context = context;
        initializeSoundEffects();
    }

    private void initializeSoundEffects() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();

                soundPool = new SoundPool.Builder()
                        .setMaxStreams(6)
                        .setAudioAttributes(audioAttributes)
                        .build();
            } else {
                soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
            }

            // Load sound resources
            soundShout = soundPool.load(context, R.raw.shout, 1);
            soundAnswer = soundPool.load(context, R.raw.shout, 1); // Using shout sound as fallback
            soundSuccess = soundPool.load(context, R.raw.shout, 1); // Using shout sound as fallback
            soundFailure = soundPool.load(context, R.raw.shout, 1); // Using shout sound as fallback
            soundVictory = soundPool.load(context, R.raw.shout, 1); // Using shout sound as fallback
            soundDefeat = soundPool.load(context, R.raw.shout, 1); // Using shout sound as fallback

            Log.d("SoundController", "Sound effects initialized successfully");
        } catch (Exception e) {
            Log.e("SoundController", "Error initializing sound effects", e);
            soundPool = null;
        }
    }

    public void playSound(SoundType soundType) {
        if (soundPool == null) return;

        try {
            int soundId;
            switch (soundType) {
                case SHOUT:
                    soundId = soundShout;
                    break;
                case ANSWER:
                    soundId = soundAnswer;
                    break;
                case SUCCESS:
                    soundId = soundSuccess;
                    break;
                case FAILURE:
                    soundId = soundFailure;
                    break;
                case VICTORY:
                    soundId = soundVictory;
                    break;
                case DEFEAT:
                    soundId = soundDefeat;
                    break;
                default:
                    return;
            }
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
        } catch (Exception e) {
            Log.e("SoundController", "Error playing sound", e);
        }
    }

    public void vibrate(VibrationPattern pattern) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    switch (pattern) {
                        case SUCCESS:
                            vibrator.vibrate(VibrationEffect.createWaveform(
                                    new long[]{0, 50, 50, 50}, -1));
                            break;
                        case FAILURE:
                            vibrator.vibrate(VibrationEffect.createWaveform(
                                    new long[]{0, 200}, -1));
                            break;
                        case VICTORY:
                            vibrator.vibrate(VibrationEffect.createWaveform(
                                    new long[]{0, 100, 100, 100}, -1));
                            break;
                        case DEFEAT:
                            vibrator.vibrate(VibrationEffect.createWaveform(
                                    new long[]{0, 500}, -1));
                            break;
                        case BUTTON_PRESS:
                            vibrator.vibrate(VibrationEffect.createOneShot(50,
                                    VibrationEffect.DEFAULT_AMPLITUDE));
                            break;
                    }
                }
            } catch (Exception e) {
                Log.e("SoundController", "Error during vibration", e);
            }
        }
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
