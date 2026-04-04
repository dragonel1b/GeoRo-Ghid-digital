package com.example.myapplication.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.Joc1.AchievementManager;
import com.example.myapplication.core.domain.model.EnhancedQuestionModel;
import com.example.myapplication.transilvaniausage.DifficultyManager;
import com.example.myapplication.transilvaniausage.GameModeManager;
import com.example.myapplication.transilvaniausage.PlayerProgressTracker;

import com.example.myapplication.utils.HapticFeedbackType;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class to provide standardized game enhancements across all regions
 */
public class RegionGameEnhancer {
    private static final String TAG = "RegionGameEnhancer";
    
    // Enhanced game systems
    private DifficultyManager difficultyManager;
    private GameModeManager gameModeManager;
    private PlayerProgressTracker progressTracker;
    private com.example.myapplication.Joc1.AchievementManager achievementManager;
    private Context context;
    private String regionName;
    
    public RegionGameEnhancer(Context context, String regionName) {
        this.context = context;
        this.regionName = regionName;
        initializeSystems();
    }
    
    /**
     * Initialize all enhanced systems
     */
    private void initializeSystems() {
        try {
            difficultyManager = new DifficultyManager(context);
            gameModeManager = new GameModeManager(context);
            progressTracker = new PlayerProgressTracker(context);
            achievementManager = new com.example.myapplication.Joc1.AchievementManager(context);
            
            Log.d(TAG, "Enhanced systems initialized for " + regionName);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing enhanced systems for " + regionName, e);
        }
    }
    
    /**
     * Initialize game mode from intent
     */
    public void initializeGameMode(Intent intent) {
        GameModeManager.GameMode gameMode = GameModeManager.GameMode.CLASSIC;
        if (intent != null && intent.hasExtra("GAME_MODE")) {
            try {
                gameMode = GameModeManager.GameMode.valueOf(intent.getStringExtra("GAME_MODE"));
            } catch (Exception e) {
                Log.w(TAG, "Invalid game mode in intent for " + regionName + ", using CLASSIC", e);
            }
        }
        
        if (gameModeManager != null) {
            gameModeManager.initializeGameMode(gameMode, null);
        }
    }
    
    /**
     * Update game constants based on current mode and difficulty
     */
    public GameConstants updateGameConstants() {
        if (gameModeManager != null && difficultyManager != null) {
            GameModeManager.GameMode currentMode = gameModeManager.getCurrentGameMode();
            DifficultyManager.DifficultyLevel currentDifficulty = difficultyManager.getCurrentDifficulty();
            
            // Calculate time per question based on difficulty
            int timePerQuestion = currentDifficulty.timePerQuestion;
            
            // Calculate points based on mode
            int pointsPerCorrectAnswer = 10;
            if (currentMode == GameModeManager.GameMode.LIGHTNING) {
                pointsPerCorrectAnswer = 15; // More points for quick mode
            } else if (currentMode == GameModeManager.GameMode.MARATHON) {
                pointsPerCorrectAnswer = 8; // Slightly less for longer mode
            }
            
            Log.d(TAG, regionName + " - Updated game constants - Time: " + timePerQuestion + "ms, Points: " + pointsPerCorrectAnswer);
            
            return new GameConstants(timePerQuestion, pointsPerCorrectAnswer, 50, 3);
        }
        
        // Fallback constants
        return new GameConstants(30000, 10, 50, 3);
    }
    
    /**
     * Provide haptic feedback for user actions
     */
    public void performHapticFeedback(HapticFeedbackType type) {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                long[] pattern;
                switch (type) {
                    case CORRECT:
                        pattern = new long[]{0, 100, 50, 100}; // Success pattern
                        break;
                    case WRONG:
                        pattern = new long[]{0, 200, 100, 200, 100, 200}; // Error pattern
                        break;
                    case LIFELINE:
                        pattern = new long[]{0, 50}; // Light tap
                        break;
                    default:
                        pattern = new long[]{0, 50};
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                } else {
                    vibrator.vibrate(pattern, -1);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not perform haptic feedback for " + regionName, e);
        }
    }
    
    /**
     * Show hint based on question category
     */
    public void showHint(EnhancedQuestionModel.Category category) {
        String hintText = "Indiciu: ";
        switch (category) {
            case HISTORY:
                hintText += "Gândește-te la evenimentele istorice importante din " + regionName + ".";
                break;
            case GEOGRAPHY:
                hintText += "Consideră locația geografică și caracteristicile naturale ale regiunii " + regionName + ".";
                break;
            case CULTURE:
                hintText += "Reflectă asupra tradițiilor și obiceiurilor specifice regiunii " + regionName + ".";
                break;
            default:
                hintText += "Analizează cu atenție toate opțiunile și elimină pe cele care nu par corecte.";
        }
        
        new MaterialAlertDialogBuilder(context)
                .setTitle("💡 Indiciu")
                .setMessage(hintText)
                .setPositiveButton("Am înțeles", null)
                .show();
    }
    
    /**
     * Show quit confirmation dialog
     */
    public void showQuitConfirmation(int questionsAnswered, int totalQuestions, int score, int correctAnswers, Runnable onQuit) {
        String message = String.format(
            "Ești sigur că vrei să ieși din Quiz-ul %s?\n\n" +
            "📊 Progres actual:\n" +
            "• Întrebări răspunse: %d/%d\n" +
            "• Scor actual: %d puncte\n" +
            "• Răspunsuri corecte: %d\n\n" +
            "Progresul va fi pierdut!",
            regionName, questionsAnswered, totalQuestions, score, correctAnswers
        );
        
        new MaterialAlertDialogBuilder(context)
                .setTitle("🚪 Ieșire din Quiz")
                .setMessage(message)
                .setPositiveButton("Da, ieși", (dialog, which) -> {
                    performHapticFeedback(HapticFeedbackType.LIFELINE);
                    if (onQuit != null) {
                        onQuit.run();
                    }
                })
                .setNegativeButton("Continuă", null)
                .show();
    }
    
    /**
     * Track answer progress
     */
    public void trackAnswer(EnhancedQuestionModel.Category category, 
                           EnhancedQuestionModel.Difficulty difficulty, 
                           boolean isCorrect, long responseTime) {
        if (progressTracker != null) {
            progressTracker.trackAnswer(regionName, isCorrect, responseTime, category, difficulty);
        }
        
        if (difficultyManager != null && isCorrect) {
            // Update difficulty would need game stats, for now just log
            Log.d(TAG, regionName + " - Answer tracked: " + category + ", " + difficulty + ", " + isCorrect);
        }
    }
    
    /**
     * Finish game and update all systems with final results
     */
    public void finishGame(int score, int correctAnswers, int totalQuestions, long totalTime) {
        Log.d(TAG, regionName + " - Game finished - Score: " + score + ", Correct: " + correctAnswers + 
              "/" + totalQuestions + ", Time: " + totalTime + "ms");
        
        // Update difficulty manager with final results
        if (difficultyManager != null) {
            difficultyManager.updateDifficultyAfterGame(correctAnswers, totalQuestions, totalTime);
        }
        
        // Update progress tracker
        if (progressTracker != null) {
            if (gameModeManager != null) {
                progressTracker.endSession(score, gameModeManager.getCurrentGameMode());
            } else {
                // Use a default game mode if none is available
                // We'll need to get the appropriate GameMode enum from the region's GameModeManager
                // For now, we'll skip the progress tracking if no game mode manager is available
                Log.w(TAG, "No game mode manager available for progress tracking");
            }
        }
        
        // Update achievement manager
        if (achievementManager != null) {
            float accuracy = totalQuestions > 0 ? (float) correctAnswers / totalQuestions * 100 : 0;
            // Update quiz-related achievements
            achievementManager.incrementQuizCorrectAnswers();
            achievementManager.updateAchievement("quiz_novice", correctAnswers);
            achievementManager.updateAchievement("quiz_expert", correctAnswers);
            achievementManager.updateAchievement("quiz_master", correctAnswers);
            
            // Update accuracy achievements
            if (accuracy >= 100) {
                achievementManager.updateAchievement("perfect_score", 1);
            } else if (accuracy >= 80) {
                achievementManager.updateAchievement("high_accuracy", 1);
            }
            
            // Update streak achievements if applicable
            if (correctAnswers >= 5) {
                achievementManager.updateAchievement("streak_master", 1);
            }
        }
        
        // Perform haptic feedback for game completion
        performHapticFeedback(HapticFeedbackType.CORRECT);
    }
    
    /**
     * Convert standard question text to enhanced question with inferred category and difficulty
     */
    public static EnhancedQuestionModel convertToEnhanced(String questionText, List<String> answers, 
                                                         int correctIndex, String fact, String regionName) {
        EnhancedQuestionModel.Category category = inferCategory(questionText, regionName);
        EnhancedQuestionModel.Difficulty difficulty = inferDifficulty(questionText, regionName);
        
        List<String> incorrectAnswers = new ArrayList<>();
        for (int i = 0; i < answers.size(); i++) {
            if (i != correctIndex) {
                incorrectAnswers.add(answers.get(i));
            }
        }
        return new EnhancedQuestionModel(
            questionText,
            answers.get(correctIndex),
            incorrectAnswers,
            0, // imageResourceId
            fact,
            category,
            difficulty,
            null // tags
        );
    }
    
    private static EnhancedQuestionModel.Category inferCategory(String questionText, String regionName) {
        String text = questionText.toLowerCase();
        
        if (text.contains("an") || text.contains("perioad") || text.contains("constru") || 
            text.contains("domn") || text.contains("istoric") || text.contains("stefan") ||
            text.contains("imperiul") || text.contains("timpul")) {
            return EnhancedQuestionModel.Category.HISTORY;
        } else if (text.contains("munte") || text.contains("varful") || text.contains("oras") || 
                   text.contains("capita") || text.contains("localitat") || text.contains("rau") ||
                   text.contains("inaltime") || text.contains("zone")) {
            return EnhancedQuestionModel.Category.GEOGRAPHY;
        } else if (text.contains("traditi") || text.contains("obicei") || text.contains("kultur") || 
                   text.contains("manastir") || text.contains("tehnic") || text.contains("artizanal") ||
                   text.contains("port") || text.contains("mesteri") || text.contains("dans")) {
            return EnhancedQuestionModel.Category.CULTURE;
        } else {
            return EnhancedQuestionModel.Category.GENERAL;
        }
    }
    
    private static EnhancedQuestionModel.Difficulty inferDifficulty(String questionText, String regionName) {
        String text = questionText.toLowerCase();
        
        // Hard questions - require specific detailed knowledge
        if (text.contains("imperiul") || text.contains("habsburgic") || 
            text.contains("specific") || text.contains("detaliat") ||
            (text.contains("an") && (text.contains("15") || text.contains("16") || text.contains("17") || text.contains("18") || text.contains("19")))) {
            return EnhancedQuestionModel.Difficulty.HARD;
        }
        // Medium questions - require good regional knowledge
        else if (text.contains("cel mare") || text.contains("important") || 
                 text.contains("cunoscut") || text.contains("famos") ||
                 text.contains("principal")) {
            return EnhancedQuestionModel.Difficulty.MEDIUM;
        }
        // Easy questions - basic knowledge
        else {
            return EnhancedQuestionModel.Difficulty.EASY;
        }
    }
    
    // Getters for the enhanced systems
    public DifficultyManager getDifficultyManager() { return difficultyManager; }
    public GameModeManager getGameModeManager() { return gameModeManager; }
    public PlayerProgressTracker getProgressTracker() { return progressTracker; }
    public AchievementManager getAchievementManager() { return achievementManager; }
    
    /**
     * Inner class to hold game constants
     */
    public static class GameConstants {
        public final int timePerQuestion;
        public final int pointsPerCorrectAnswer;
        public final int bonusPoints;
        public final int streakBonusThreshold;
        
        public GameConstants(int timePerQuestion, int pointsPerCorrectAnswer, int bonusPoints, int streakBonusThreshold) {
            this.timePerQuestion = timePerQuestion;
            this.pointsPerCorrectAnswer = pointsPerCorrectAnswer;
            this.bonusPoints = bonusPoints;
            this.streakBonusThreshold = streakBonusThreshold;
        }
    }
} 