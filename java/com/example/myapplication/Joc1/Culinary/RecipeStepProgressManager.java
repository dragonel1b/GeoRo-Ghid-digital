package com.example.myapplication.Joc1.Culinary;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Manager pentru progresul rețetei, calculul și afișarea timpului
 */
public class RecipeStepProgressManager {

    private final Context context;
    private List<RecipeStepByStepActivity.RecipeStep> steps;
    private int completedSteps = 0;
    private int currentStepPosition = 0;
    private long overallTimerStartTime;
    private boolean isTimerRunning = false;
    private CountDownTimer currentTimer;
    private long timerTimeRemaining = 0;
    private MediaPlayer timerAlarmSound;
    
    // Progress UI components
    private LinearProgressIndicator progressBar;
    private TextView progressText;
    private TextView overallTimer;

    // Timer UI components
    private TextView timerText;
    private ProgressCallback progressCallback;

    public interface ProgressCallback {
        void onProgressUpdated(int progress, int completed, int total);
        void onStepTimerFinished();
        void onStepTimerTick(long millisUntilFinished);
    }
    
    public RecipeStepProgressManager(Context context, List<RecipeStepByStepActivity.RecipeStep> steps) {
        this.context = context;
        this.steps = steps;
    }
    
    public void setProgressUiComponents(LinearProgressIndicator progressBar, TextView progressText, 
                                     TextView overallTimer, TextView timerText) {
        this.progressBar = progressBar;
        this.progressText = progressText;
        this.overallTimer = overallTimer;
        this.timerText = timerText;
    }
    
    public void setProgressCallback(ProgressCallback callback) {
        this.progressCallback = callback;
    }
    
    /**
     * Inițializează și pornește cronometrul general
     */
    public void startOverallTimer() {
        overallTimerStartTime = System.currentTimeMillis();
        
        // Actualizare la fiecare secundă
        android.os.Handler handler = new android.os.Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateOverallTimerDisplay();
                handler.postDelayed(this, 1000);
            }
        });
    }
    
    /**
     * Actualizează afișarea timpului total
     */
    private void updateOverallTimerDisplay() {
        if (overallTimer != null) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - overallTimerStartTime;
            
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String timeDisplay = sdf.format(TimeUnit.MILLISECONDS.toHours(elapsedTime));
            overallTimer.setText(timeDisplay);
        }
    }
    
    /**
     * Marchează un pas ca finalizat și actualizează progresul
     */
    public void markStepAsCompleted(int position) {
        if (position >= 0 && position < steps.size()) {
            RecipeStepByStepActivity.RecipeStep step = steps.get(position);
            if (!step.isCompleted()) {
                step.setCompleted(true);
                completedSteps++;
                updateProgress();
                
                // Log analytics event
                logAnalyticsEvent("step_completed", position);
            }
        }
    }
    
    /**
     * Setează pasul curent și actualizează progresul
     */
    public void setCurrentStep(int position) {
        if (position >= 0 && position < steps.size()) {
            currentStepPosition = position;
            updateProgress();
        }
    }
    
    /**
     * Calculează și actualizează progresul general
     */
    private void updateProgress() {
        if (progressBar != null && progressText != null) {
            int totalSteps = steps.size();
            int progress = totalSteps > 0 ? (completedSteps * 100) / totalSteps : 0;
            
            progressBar.setProgress(progress);
            progressText.setText(completedSteps + "/" + totalSteps + " pași completați");
            
            if (progressCallback != null) {
                progressCallback.onProgressUpdated(progress, completedSteps, totalSteps);
            }
        }
    }
    
    /**
     * Verifică dacă toți pașii au fost completați
     */
    public boolean areAllStepsCompleted() {
        return completedSteps >= steps.size();
    }
    
    /**
     * Pornește cronometrul pentru pasul curent
     */
    public void startStepTimer(long durationMillis) {
        // Oprește timer-ul curent dacă rulează
        stopStepTimer();
        
        // Inițializează și pornește noul timer
        isTimerRunning = true;
        timerTimeRemaining = durationMillis;
        
        currentTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTimeRemaining = millisUntilFinished;
                updateTimerDisplay(millisUntilFinished);
                
                if (progressCallback != null) {
                    progressCallback.onStepTimerTick(millisUntilFinished);
                }
            }
            
            @Override
            public void onFinish() {
                timerTimeRemaining = 0;
                isTimerRunning = false;
                updateTimerDisplay(0);
                
                // Notificare sonoră și vibrație
                playTimerFinishedSound();
                vibrateDevice();
                
                if (progressCallback != null) {
                    progressCallback.onStepTimerFinished();
                }
                
                // Log analytics event
                logAnalyticsEvent("timer_finished", currentStepPosition);
            }
        }.start();
        
        // Log analytics event
        logAnalyticsEvent("timer_started", currentStepPosition);
    }
    
    /**
     * Oprește cronometrul pentru pasul curent
     */
    public void stopStepTimer() {
        if (currentTimer != null) {
            currentTimer.cancel();
            isTimerRunning = false;
        }
    }
    
    /**
     * Pune pe pauză sau reia cronometrul
     */
    public void toggleStepTimer() {
        if (isTimerRunning) {
            // Pune pe pauză
            stopStepTimer();
            
            // Log analytics event
            logAnalyticsEvent("timer_paused", currentStepPosition);
        } else if (timerTimeRemaining > 0) {
            // Reia cu timpul rămas
            startStepTimer(timerTimeRemaining);
            
            // Log analytics event
            logAnalyticsEvent("timer_resumed", currentStepPosition);
        }
    }
    
    /**
     * Resetează cronometrul
     */
    public void resetStepTimer(long durationMillis) {
        stopStepTimer();
        timerTimeRemaining = durationMillis;
        updateTimerDisplay(durationMillis);
        
        // Log analytics event
        logAnalyticsEvent("timer_reset", currentStepPosition);
    }
    
    /**
     * Actualizează afișarea timpului rămas
     */
    private void updateTimerDisplay(long millisRemaining) {
        if (timerText != null) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millisRemaining);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millisRemaining) % 60;
            
            String timeDisplay = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            timerText.setText(timeDisplay);
        }
    }
    
    /**
     * Redă un sunet la terminarea timer-ului
     */
    private void playTimerFinishedSound() {
        if (timerAlarmSound == null) {
            // timerAlarmSound = MediaPlayer.create(context, R.raw.timer_alarm);
        }
        
        if (timerAlarmSound != null) {
            timerAlarmSound.start();
        }
    }
    
    /**
     * Vibrează dispozitivul la terminarea timer-ului
     */
    private void vibrateDevice() {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            VibrationEffect effect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(effect);
        }
    }
    
    /**
     * Salvează starea curentă pentru reluare ulterioară
     */
    public void saveCurrentState() {
        // TODO: Salvează în ViewModel și Room
        // Ar trebui implementat într-o metodă reală
    }
    
    /**
     * Înregistrează evenimente analytics pentru monitorizare și îmbunătățire
     */
    private void logAnalyticsEvent(String eventName, int stepPosition) {
        // Implementare reală ar folosi Firebase Analytics sau alt serviciu
        // FirebaseAnalytics.getInstance(context).logEvent(eventName, bundle);
    }
    
    /**
     * Eliberează resursele și închide timer-ele
     */
    public void cleanup() {
        stopStepTimer();
        if (timerAlarmSound != null) {
            timerAlarmSound.release();
            timerAlarmSound = null;
        }
    }
} 