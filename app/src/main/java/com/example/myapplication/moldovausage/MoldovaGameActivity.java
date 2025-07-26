package com.example.myapplication.moldovausage;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.example.myapplication.utils.SyncManager;

import com.example.myapplication.models.EnhancedQuestionModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.Moldova;
import com.example.myapplication.utils.PointsManager;
import com.example.myapplication.models.QuestionModel;
import com.example.myapplication.model.QuizResult;
import com.example.myapplication.repository.FirestoreQuestionRepository;
import com.example.myapplication.utils.GameOverHelper;
import com.example.myapplication.Joc1.AchievementManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Date;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;
import android.graphics.Paint;
import java.util.Arrays;

public class MoldovaGameActivity extends AppCompatActivity {
    private static final String TAG = "MoldovaGameActivity";
    private static final String REGION = "moldova";
    private static final String GAME_TYPE = "quiz";
    
    // UI Components
    private TextView questionTextView;
    private MaterialButton[] answerButtons;
    private TextView scoreTextView;
    private ProgressBar progressBar;
    private TextView timerTextView;
    private TextView streakTextView;
    private ImageView questionImage;
    private ImageButton fiftyFiftyButton;
    private ImageButton hintButton;
    private ImageButton skipQuestionButton;
    private ImageButton quitButton;
    private MaterialCardView[] answerCards;
    private MaterialButton finishButton;
    
    // Enhanced game state variables
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int streak = 0;
    private int maxStreak = 0;
    private int totalQuestions = 0;
    private int correctAnswers = 0;
    private long totalTime = 0;
    private long questionStartTime = 0;
    private boolean answerSelected = false;
    
    // Enhanced question management
    private List<QuestionModel> firestoreQuestions;
    private List<EnhancedQuestionModel> enhancedQuestions;
    
    // Enhanced game systems - following Transilvania pattern
    private DifficultyManager difficultyManager;
    private GameModeManager gameModeManager;
    private PlayerProgressTracker progressTracker;
    private AchievementManager achievementManager;
    private SyncManager syncManager;
    
    // Dynamic game constants based on difficulty and mode
    private int POINTS_PER_CORRECT_ANSWER = 10;
    private int BONUS_POINTS = 50;
    private int TIME_PER_QUESTION = 30000; // Will be updated based on mode/difficulty
    private static final int STREAK_BONUS_THRESHOLD = 3;
    
    // Existing managers
    private PointsManager pointsManager;
    private CountDownTimer timer;
    private boolean isFiftyFiftyUsed = false;
    private boolean isHintUsed = false;
    private boolean isSkipUsed = false;
    private int lifelinesUsed = 0;
    private Random random = new Random();
    private FirestoreQuestionRepository questionRepository;
    private boolean isDataLoaded = false;
    private int timeRemaining = 0;
    
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_moldova_game);
        
        // Initialize enhanced systems following Transilvania pattern
        initializeEnhancedSystems();
        setupGameModeAndDifficulty();
        initializeViews();
        setupAccessibility();
        
        // Load questions using advanced data source selection
        checkUserPreferenceAndLoad();
    }
    
    /**
     * Initialize enhanced game systems following Transilvania pattern
     */
    private void initializeEnhancedSystems() {
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Initialize repository and sync manager
        questionRepository = FirestoreQuestionRepository.getInstance();
        syncManager = SyncManager.getInstance(this);
        
        // Initialize game managers
        difficultyManager = new DifficultyManager(this);
        gameModeManager = new GameModeManager(this);
        progressTracker = new PlayerProgressTracker(this);
        
        // Initialize other managers
        pointsManager = new PointsManager(this);
        achievementManager = new AchievementManager(this);
        
        Log.d(TAG, "✅ Enhanced systems initialized successfully");
    }
    
    /**
     * Setup game mode and difficulty from intent or defaults
     */
    private void setupGameModeAndDifficulty() {
        Intent intent = getIntent();
        
        // Get game mode from intent
        String gameModeName = intent.getStringExtra("GAME_MODE");
        if (gameModeName != null) {
            try {
                GameModeManager.GameMode gameMode = GameModeManager.GameMode.valueOf(gameModeName);
                gameModeManager.initializeGameMode(gameMode, null);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Invalid game mode: " + gameModeName + ", using default");
            }
        }
        
        // Get difficulty from intent
        String difficultyName = intent.getStringExtra("DIFFICULTY");
        if (difficultyName != null) {
            try {
                DifficultyManager.DifficultyLevel difficulty = DifficultyManager.DifficultyLevel.valueOf(difficultyName);
                difficultyManager.setCurrentDifficulty(difficulty);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Invalid difficulty: " + difficultyName + ", using current");
            }
        }
        
        // Update game constants based on current settings
        updateGameConstants();
        
        Log.d(TAG, "🎮 Game mode: " + gameModeManager.getCurrentGameMode().displayName + 
              ", Difficulty: " + difficultyManager.getCurrentDifficulty().displayName);
    }
    
    /**
     * Update game constants based on current mode and difficulty
     */
    private void updateGameConstants() {
        DifficultyManager.DifficultyLevel difficulty = difficultyManager.getCurrentDifficulty();
        GameModeManager.GameMode gameMode = gameModeManager.getCurrentGameMode();
        
        // Update time per question from difficulty
        TIME_PER_QUESTION = difficulty.getTimeSeconds() * 1000; // Convert to milliseconds
        
        // Update points multiplier
        POINTS_PER_CORRECT_ANSWER = Math.round(10 * difficulty.getScoreMultiplier());
        
        Log.d(TAG, "📊 Updated constants - Time: " + TIME_PER_QUESTION + 
              "ms, Points: " + POINTS_PER_CORRECT_ANSWER);
    }

    protected void initializeQuestions() {
        // This will be called by the base class
        // We'll handle question loading in loadQuestions() method
    }
    
    protected void finishGame() {
        if (timer != null) {
            timer.cancel();
        }
        
        Log.d(TAG, "🏁 Finishing game with enhanced statistics");
        
        // Calculate final statistics
        float accuracy = totalQuestions > 0 ? (float) correctAnswers / totalQuestions : 0.0f;
        
        // Update difficulty based on performance
        difficultyManager.updateDifficultyAfterGame(correctAnswers, totalQuestions, totalTime);
        
        // End progress tracking session
        progressTracker.endSession(score, gameModeManager.getCurrentGameMode());
        
        // Update achievements
        List<String> newAchievements = progressTracker.checkForNewAchievements();
        for (String achievement : newAchievements) {
            Log.d(TAG, "🏆 New achievement unlocked: " + achievement);
        }
        
        // Save final score with enhanced systems
        pointsManager.addPoints(this, REGION, score);
        
        // Create and save quiz result
        QuizResult quizResult = createQuizResult();
        saveQuizResult(quizResult);
        
        // Show results
        showGameResults(accuracy, newAchievements);
    }
    
    /**
     * Show game results with enhanced statistics
     */
    private void showGameResults(float accuracy, List<String> newAchievements) {
        StringBuilder results = new StringBuilder();
        results.append("🎯 Quiz Moldova Completat!\n\n");
        
        // Basic stats
        results.append("📊 Statistici:\n");
        results.append("• Scor final: ").append(score).append(" puncte\n");
        results.append("• Răspunsuri corecte: ").append(correctAnswers).append("/").append(totalQuestions).append("\n");
        results.append("• Acuratețe: ").append(String.format("%.1f%%", accuracy * 100)).append("\n");
        results.append("• Cel mai lung streak: ").append(maxStreak).append("\n");
        results.append("• Timpul total: ").append(String.format("%.1fs", totalTime / 1000.0f)).append("\n\n");
        
        // Game mode and difficulty info
        results.append("🎮 Mod joc: ").append(gameModeManager.getCurrentGameMode().displayName).append("\n");
        results.append("⚡ Dificultate: ").append(difficultyManager.getCurrentDifficulty().displayName).append("\n\n");
        
        // Performance feedback
        if (accuracy >= 0.9f) {
            results.append("🏆 Performanță excepțională! Ești un adevărat expert în Moldova!\n");
        } else if (accuracy >= 0.7f) {
            results.append("👏 Performanță foarte bună! Cunoști bine istoria Moldovei!\n");
        } else if (accuracy >= 0.5f) {
            results.append("👍 Performanță bună! Mai exersează pentru a deveni expert!\n");
        } else {
            results.append("📚 Continuă să înveți despre Moldova! Fiecare quiz te face mai bun!\n");
        }
        
        // New achievements
        if (!newAchievements.isEmpty()) {
            results.append("\n🏆 Realizări noi deblocate:\n");
            for (String achievement : newAchievements) {
                results.append("• ").append(achievement).append("\n");
            }
        }
        
        // Recommendations
        String recommendation = difficultyManager.getPerformanceRecommendation();
        results.append("\n💡 Recomandare: ").append(recommendation);
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("🎉 Rezultate Finale")
            .setMessage(results.toString())
            .setPositiveButton("🏠 Înapoi la meniu", (dialog, which) -> finish())
            .setNegativeButton("🔄 Joacă din nou", (dialog, which) -> restartGame())
            .setCancelable(false)
            .show();
    }
    
    /**
     * Restart the game with same settings
     */
    private void restartGame() {
        // Reset all game state
        currentQuestionIndex = 0;
        score = 0;
        streak = 0;
        maxStreak = 0;
        correctAnswers = 0;
        totalTime = 0;
        answerSelected = false;
        
        // Reset lifelines
        isFiftyFiftyUsed = false;
        isHintUsed = false;
        isSkipUsed = false;
        lifelinesUsed = 0;
        
        // Reload and restart
        loadQuestionsFromFirestore();
    }
    
    /**
     * Create quiz result for saving
     */
    private QuizResult createQuizResult() {
        QuizResult quizResult = new QuizResult();
        quizResult.setUserId(mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous");
        quizResult.setScore(score);
        quizResult.setCorrectAnswers(correctAnswers);
        quizResult.setTotalQuestions(totalQuestions);
        quizResult.setMaxStreak(maxStreak);
        quizResult.setTotalTime(totalTime);
        quizResult.setRegion(REGION);
        quizResult.setGameType(GAME_TYPE);
        quizResult.setCompletedAt(new Date());
        quizResult.setQuizId("moldova_enhanced_quiz_" + System.currentTimeMillis());
        return quizResult;
    }
    
    /**
     * Save quiz result to Firestore
     */
    private void saveQuizResult(QuizResult quizResult) {
        // Save to both hybrid storage and Firebase
        saveQuizResultToHybridStorage();
        saveQuizResultToFirebase();
    }
    
    /**
     * 💾 Salvează rezultatul în sistemul hibrid de stocare
     */
    private void saveQuizResultToHybridStorage() {
        // Creăm datele pentru salvare hibridă
        Map<String, Object> quizResultData = new HashMap<>();
        quizResultData.put("score", score);
        quizResultData.put("correctAnswers", correctAnswers);
        quizResultData.put("totalQuestions", totalQuestions);
        quizResultData.put("maxStreak", maxStreak);
        quizResultData.put("totalTime", totalTime);
        quizResultData.put("accuracy", ((float) correctAnswers / totalQuestions) * 100);
        quizResultData.put("region", REGION);
        quizResultData.put("gameType", GAME_TYPE);
        quizResultData.put("completedAt", System.currentTimeMillis());
        quizResultData.put("lifelinesUsed", lifelinesUsed);
        
        // Adăugăm date despre dificultate și mod de joc
        if (difficultyManager != null) {
            quizResultData.put("difficulty", difficultyManager.getCurrentDifficulty().name());
        }
        if (gameModeManager != null && gameModeManager.getCurrentGameMode() != null) {
            quizResultData.put("gameMode", gameModeManager.getCurrentGameMode().name());
        }
        
        // Generăm un ID unic pentru acest quiz
        String quizId = "moldova_quiz_" + System.currentTimeMillis();
        
        // Salvăm în sistemul hibrid
        syncManager.saveData("quiz_results", quizId, quizResultData, new SyncManager.SyncCallback() {
            @Override
            public void onSyncComplete(boolean success, String message) {
                if (success) {
                    Log.d(TAG, "✅ Quiz result saved to hybrid storage: " + message);
                } else {
                    Log.w(TAG, "⚠️ Quiz result hybrid storage failed: " + message);
                }
            }
        });
        
        Log.d(TAG, "🔄 Quiz result submitted to hybrid storage system");
    }
    
    /**
     * 🏆 Salvează rezultatul quiz-ului în Firebase pentru leaderboard
     */
    private void saveQuizResultToFirebase() {
        // Verificăm dacă utilizatorul este autentificat
        if (mAuth.getCurrentUser() == null) {
            Log.w(TAG, "Utilizatorul nu este autentificat, nu se poate salva rezultatul în clasament");
            Toast.makeText(this, "Trebuie să fii autentificat pentru a apărea în clasament", Toast.LENGTH_LONG).show();
            return;
        }
        
        String userId = mAuth.getCurrentUser().getUid();
        
        // Creăm obiectul QuizResult principal
        QuizResult quizResult = createQuizResult(userId);
        
        // Salvăm rezultatul în Firebase cu metode separate pentru organizare
        saveToQuizResults(quizResult);
        saveToUserActivityHistory(quizResult);
        saveToLeaderboardData(quizResult);
        
        // Actualizăm profilul utilizatorului
        updateUserProfileStats(quizResult);
        
        Log.d(TAG, "Moldova Quiz Result saved - Score: " + score + ", Region: " + REGION + ", GameType: " + GAME_TYPE);
    }
    
    /**
     * 📋 Creează obiectul QuizResult cu toate datele necesare
     */
    private QuizResult createQuizResult(String userId) {
        QuizResult quizResult = new QuizResult();
        quizResult.setUserId(userId);
        quizResult.setScore(score);
        quizResult.setCorrectAnswers(correctAnswers);
        quizResult.setTotalQuestions(totalQuestions);
        quizResult.setMaxStreak(maxStreak);
        quizResult.setTotalTime(totalTime);
        quizResult.setRegion(REGION);
        quizResult.setGameType(GAME_TYPE);
        quizResult.setCompletedAt(new Date());
        
        // Adăugăm metadate specifice pentru Moldova
        quizResult.setQuizId("moldova_main_quiz_" + System.currentTimeMillis());
        
        return quizResult;
    }
    
    /**
     * 💾 Salvează în colecția principală quiz_results
     */
    private void saveToQuizResults(QuizResult quizResult) {
        FirestoreQuestionRepository.getInstance().saveQuizResult(quizResult)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Quiz result saved to main collection with ID: " + documentReference.getId());
                Toast.makeText(this, "Rezultatul a fost adăugat în clasament!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving quiz result to main collection", e);
                Toast.makeText(this, "Eroare la salvarea rezultatului în clasament", Toast.LENGTH_SHORT).show();
            });
    }
    
    /**
     * 📱 Salvează în colecția pentru activitatea recentă a utilizatorului
     */
    private void saveToUserActivityHistory(QuizResult quizResult) {
        // Creăm un document pentru activitatea recentă
        Map<String, Object> activityData = new HashMap<>();
        activityData.put("userId", quizResult.getUserId());
        activityData.put("activityType", "moldova_quiz");
        activityData.put("displayName", "Quiz Moldova");
        activityData.put("score", quizResult.getScore());
        activityData.put("accuracy", quizResult.getAccuracy());
        activityData.put("correctAnswers", quizResult.getCorrectAnswers());
        activityData.put("totalQuestions", quizResult.getTotalQuestions());
        activityData.put("maxStreak", quizResult.getMaxStreak());
        activityData.put("region", REGION);
        activityData.put("gameType", GAME_TYPE);
        activityData.put("completedAt", quizResult.getCompletedAt());
        activityData.put("duration", totalTime);
        
        // Adăugăm detalii specifice pentru afișare în profil
        activityData.put("iconResource", "ic_moldova");
        activityData.put("colorTheme", "moldova_primary");
        activityData.put("description", "Quiz despre Moldova - " + correctAnswers + "/" + totalQuestions + " corecte");
        
        db.collection("user_activity_history")
            .document(quizResult.getUserId())
            .collection("recent_activities")
            .add(activityData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Activity saved to user history with ID: " + documentReference.getId());
                
                // Păstrăm doar ultimele 20 de activități pentru fiecare utilizator
                limitUserActivityHistory(quizResult.getUserId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving activity to user history", e);
            });
    }
    
    /**
     * 🏆 Salvează datele pentru leaderboard (cel mai bun scor)
     */
    private void saveToLeaderboardData(QuizResult quizResult) {
        String userId = quizResult.getUserId();
        
        // Verificăm mai întâi dacă e cel mai bun scor pentru această regiune
        db.collection("user_best_scores")
            .document(userId)
            .collection("regional_scores")
            .document(REGION + "_" + GAME_TYPE)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                boolean shouldUpdate = false;
                
                if (!documentSnapshot.exists()) {
                    shouldUpdate = true;
                    Log.d(TAG, "No existing score found for " + REGION + ", saving new best score");
                } else {
                    Long existingScore = documentSnapshot.getLong("score");
                    if (existingScore == null || quizResult.getScore() > existingScore) {
                        shouldUpdate = true;
                        Log.d(TAG, "New score (" + quizResult.getScore() + ") is better than existing (" + existingScore + ")");
                    }
                }
                
                if (shouldUpdate) {
                    updateBestScoreForLeaderboard(quizResult);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error checking existing best score", e);
                // În caz de eroare, salvăm oricum
                updateBestScoreForLeaderboard(quizResult);
            });
    }
    
    /**
     * 🏆 Actualizează cel mai bun scor pentru leaderboard
     */
    private void updateBestScoreForLeaderboard(QuizResult quizResult) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        Map<String, Object> bestScoreData = new HashMap<>();
        bestScoreData.put("userId", quizResult.getUserId());
        bestScoreData.put("username", currentUser.getEmail());
        bestScoreData.put("displayName", currentUser.getDisplayName() != null ? 
                currentUser.getDisplayName() : "Explorator Moldova");
        bestScoreData.put("profileImageUrl", currentUser.getPhotoUrl() != null ? 
                currentUser.getPhotoUrl().toString() : "");
        bestScoreData.put("score", quizResult.getScore());
        bestScoreData.put("accuracy", quizResult.getAccuracy());
        bestScoreData.put("maxStreak", quizResult.getMaxStreak());
        bestScoreData.put("region", REGION);
        bestScoreData.put("gameType", GAME_TYPE);
        bestScoreData.put("achievedAt", quizResult.getCompletedAt());
        bestScoreData.put("totalQuestions", quizResult.getTotalQuestions());
        bestScoreData.put("correctAnswers", quizResult.getCorrectAnswers());
        
        // Salvăm cel mai bun scor regional
        db.collection("user_best_scores")
            .document(quizResult.getUserId())
            .collection("regional_scores")
            .document(REGION + "_" + GAME_TYPE)
            .set(bestScoreData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Best score updated for Moldova");
                
                // Actualizăm și leaderboard-ul global
                updateGlobalLeaderboard(bestScoreData);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating best score for Moldova", e);
            });
    }
    
    /**
     * 🌍 Actualizează leaderboard-ul global
     */
    private void updateGlobalLeaderboard(Map<String, Object> bestScoreData) {
        db.collection("global_leaderboard")
            .document(REGION + "_" + GAME_TYPE)
            .collection("top_scores")
            .document(bestScoreData.get("userId").toString())
            .set(bestScoreData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Global leaderboard updated for Moldova");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating global leaderboard for Moldova", e);
            });
    }
    
    /**
     * 👤 Actualizează statisticile din profilul utilizatorului
     */
    private void updateUserProfileStats(QuizResult quizResult) {
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("lastPlayedRegion", REGION);
        userStats.put("lastPlayedAt", quizResult.getCompletedAt());
        userStats.put("totalGamesPlayed", FieldValue.increment(1));
        userStats.put("totalScore", FieldValue.increment(quizResult.getScore()));
        userStats.put("totalCorrectAnswers", FieldValue.increment(quizResult.getCorrectAnswers()));
        userStats.put("totalQuestionsAnswered", FieldValue.increment(quizResult.getTotalQuestions()));
        
        db.collection("user_profiles")
            .document(quizResult.getUserId())
            .update(userStats)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "User profile stats updated");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating user profile stats", e);
            });
    }
    
    /**
     * 🗂️ Limitează istoricul de activități la ultimele 20
     */
    private void limitUserActivityHistory(String userId) {
        db.collection("user_activity_history")
            .document(userId)
            .collection("recent_activities")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(25)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.size() > 20) {
                    // Șterge documentele în plus
                    for (int i = 20; i < querySnapshot.size(); i++) {
                        querySnapshot.getDocuments().get(i).getReference().delete();
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error limiting user activity history", e);
            });
    }
    
    /**
     * Create local questions for fallback
     */
    private List<QuestionModel> createLocalQuestionsForMigration() {
        List<QuestionModel> questions = new ArrayList<>();
        
        // Enhanced Moldova questions with better categorization
        questions.add(new QuestionModel(
            "Care este capitala istorică a Moldovei?",
            "Iași", 
            Arrays.asList("Bacău", "Suceava", "Chișinău"), 
            0,
            "Iași a fost capitala Principatului Moldovei între 1564 și 1859 și rămâne centrul cultural al regiunii."
        ));
        
        questions.add(new QuestionModel(
            "Care este cel mai important râu care traversează Moldova?",
            "Prut", 
            Arrays.asList("Siret", "Olt", "Mureș"), 
            0,
            "Râul Prut are o lungime de 953 km și formează granița naturală între România și Republica Moldova."
        ));
        
        questions.add(new QuestionModel(
            "Ce mănăstire din Moldova este inclusă în patrimoniul UNESCO?",
            "Mănăstirea Voroneț", 
            Arrays.asList("Mănăstirea Putna", "Mănăstirea Cozia", "Mănăstirea Bistrița"), 
            0,
            "Mănăstirea Voroneț, cunoscută pentru 'albastrul de Voroneț', a fost construită în 1488 de Ștefan cel Mare."
        ));
        
        questions.add(new QuestionModel(
            "Cine a fost domnitorul cel mai important al Moldovei?",
            "Ștefan cel Mare", 
            Arrays.asList("Mihai Viteazul", "Mircea cel Bătrân", "Alexandru Ioan Cuza"), 
            0,
            "Ștefan cel Mare a domnit între 1457 și 1504 și este considerat un erou național în România și Republica Moldova."
        ));
        
        questions.add(new QuestionModel(
            "Care este cel mai mare oraș din Moldova românească în prezent?",
            "Iași", 
            Arrays.asList("Galați", "Bacău", "Suceava"), 
            0,
            "Iașiul este al doilea oraș ca mărime din România după București, cu o populație de aproximativ 380.000 locuitori."
        ));
        
        questions.add(new QuestionModel(
            "Care personalitate culturală importantă s-a născut la Humulești, Moldova?",
            "Ion Creangă", 
            Arrays.asList("Mihai Eminescu", "Vasile Alecsandri", "George Enescu"), 
            0,
            "Ion Creangă (1837-1889) este unul dintre cei mai importanți scriitori români, cunoscut pentru 'Amintiri din copilărie'."
        ));
        
        questions.add(new QuestionModel(
            "Ce monument istoric important se află la Ruginoasa, în Moldova?",
            "Palatul Cuza", 
            Arrays.asList("Cetatea Neamț", "Cetatea Sucevei", "Casa Pogor"), 
            0,
            "Palatul de la Ruginoasa a fost reședința lui Alexandru Ioan Cuza, primul domnitor al Principatelor Unite."
        ));
        
        questions.add(new QuestionModel(
            "Care este cel mai vechi oraș din Moldova?",
            "Suceava", 
            Arrays.asList("Roman", "Iași", "Bârlad"), 
            0,
            "Suceava datează din secolul al XIV-lea și a fost capitala Moldovei între 1388 și 1564."
        ));
        
        questions.add(new QuestionModel(
            "Care este cea mai importantă universitate din Moldova?",
            "Universitatea 'Alexandru Ioan Cuza' din Iași", 
            Arrays.asList("Universitatea din Galați", "Universitatea din Bacău", "Universitatea 'Ștefan cel Mare' din Suceava"), 
            0,
            "Universitatea 'Alexandru Ioan Cuza' din Iași, fondată în 1860, este cea mai veche universitate din România."
        ));
        
        questions.add(new QuestionModel(
            "Care localitate este cunoscută ca 'Poarta Moldovei'?",
            "Focșani", 
            Arrays.asList("Târgu Neamț", "Pașcani", "Tescani"), 
            0,
            "Focșani este cunoscut ca 'Poarta Moldovei' datorită poziției sale geografice, la granița dintre Moldova și Muntenia."
        ));
        
        // Shuffle for variety
        Collections.shuffle(questions);
        return questions;
    }

    /**
     * Setup UI accessibility following Transilvania pattern
     */
    private void setupAccessibility() {
        // Set content descriptions for better screen reader support
        ViewCompat.setAccessibilityHeading(questionTextView, true);
        
        questionTextView.setContentDescription("Întrebarea curentă din quiz-ul Moldova");
        timerTextView.setContentDescription("Timpul rămas pentru răspuns");
        scoreTextView.setContentDescription("Scorul curent");
        streakTextView.setContentDescription("Seria de răspunsuri corecte consecutive");
        
        // Set content descriptions for answer buttons
        for (int i = 0; i < answerButtons.length; i++) {
            MaterialButton button = answerButtons[i];
            MaterialCardView card = answerCards[i];
            
            button.setContentDescription("Opțiunea de răspuns " + (i + 1));
            card.setContentDescription("Apasă pentru a selecta răspunsul " + (i + 1));
            
            // Ensure minimum touch target size
            card.setMinimumHeight((int) (48 * getResources().getDisplayMetrics().density));
            
            // Setup click listeners
            final int index = i;
            card.setOnClickListener(v -> {
                if (v.isClickable() && !answerSelected) {
                    checkAnswer(index, button.getText().toString());
                }
            });
        }
        
        // Setup lifeline accessibility
        if (fiftyFiftyButton != null) {
            fiftyFiftyButton.setContentDescription("Folosește lifeline 50:50 pentru a elimina două răspunsuri greșite");
        }
        if (hintButton != null) {
            hintButton.setContentDescription("Folosește lifeline hint pentru a vedea un indiciu");
        }
        if (skipQuestionButton != null) {
            skipQuestionButton.setContentDescription("Sari peste această întrebare");
        }
    }
    
    /**
     * Initialize UI views following Transilvania pattern
     */
    private void initializeViews() {
        Log.d(TAG, "🎨 Initializing UI views");
        
        // Initialize Moldova-specific UI components
        questionTextView = findViewById(R.id.questionTextView);
        answerButtons = new MaterialButton[]{
            findViewById(R.id.answerButton1),
            findViewById(R.id.answerButton2),
            findViewById(R.id.answerButton3),
            findViewById(R.id.answerButton4)
        };
        
        answerCards = new MaterialCardView[]{
            findViewById(R.id.answerCard1),
            findViewById(R.id.answerCard2),
            findViewById(R.id.answerCard3),
            findViewById(R.id.answerCard4)
        };
        
        scoreTextView = findViewById(R.id.scoreTextView);
        timerTextView = findViewById(R.id.timerTextView);
        streakTextView = findViewById(R.id.streakTextView);
        questionImage = findViewById(R.id.questionImage);
        progressBar = findViewById(R.id.progressBar);
        
        // Lifeline buttons (may be null if not in layout)
        fiftyFiftyButton = findViewById(R.id.fiftyFiftyButton);
        hintButton = findViewById(R.id.hintButton);
        skipQuestionButton = findViewById(R.id.skipQuestionButton);
        quitButton = findViewById(R.id.quitButton);
        finishButton = findViewById(R.id.finishButton);
        
        // Setup typography and styling
        setupUIStyling();
        
        // Setup lifeline buttons
        setupLifelines();
        
        // Apply Moldova theme
        applyMoldovaTheme();
        
        Log.d(TAG, "✅ UI views initialized successfully");
    }
    
    /**
     * Setup UI styling following Transilvania pattern
     */
    private void setupUIStyling() {
        // Setup typography
        if (questionTextView != null) {
            questionTextView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        }
        
        // Setup answer buttons
        for (MaterialButton button : answerButtons) {
            if (button != null) {
                button.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                button.setElevation(4f);
                button.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                button.setClickable(false);
                button.setFocusable(false);
            }
        }
        
        // Setup finish button
        if (finishButton != null) {
            finishButton.setOnClickListener(v -> finishGame());
            finishButton.setVisibility(View.GONE);
        }
        
        // Setup progress bar
        if (progressBar != null) {
            progressBar.setMax(10); // Default, will be updated when questions load
            progressBar.setProgress(0);
        }
    }
    
    /**
     * Setup lifeline buttons
     */
    private void setupLifelines() {
        if (fiftyFiftyButton != null) {
            fiftyFiftyButton.setOnClickListener(v -> {
                if (difficultyManager.canUseLifeline(lifelinesUsed)) {
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
                    useFiftyFifty();
                } else {
                    Toast.makeText(this, "Nu mai poți folosi lifeline-uri la această dificultate!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        if (hintButton != null) {
            hintButton.setOnClickListener(v -> {
                if (difficultyManager.canUseLifeline(lifelinesUsed)) {
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
                    useHint();
                } else {
                    Toast.makeText(this, "Nu mai poți folosi lifeline-uri la această dificultate!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        if (skipQuestionButton != null) {
            skipQuestionButton.setOnClickListener(v -> {
                if (gameModeManager.getCurrentGameMode().areLifelinesAllowed() && !isSkipUsed) {
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
                    skipQuestion();
                } else {
                    Toast.makeText(this, "Nu poți sări peste întrebări în acest mod de joc!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        if (quitButton != null) {
            quitButton.setOnClickListener(v -> showConfirmQuitDialog());
        }
    }
    
    /**
     * Apply Moldova-specific theme styling
     */
    private void applyMoldovaTheme() {
        // Style answer cards with Moldova colors
        for (int i = 0; i < answerCards.length; i++) {
            MaterialCardView card = answerCards[i];
            if (card != null) {
                card.setClickable(true);
                card.setFocusable(true);
                card.setRippleColor(ContextCompat.getColorStateList(this, R.color.moldova_primary_light));
                
                // Add elevation and corner radius
                card.setCardElevation(4f);
                card.setRadius(12f);
            }
        }
        
        // Style finish button
        if (finishButton != null) {
            finishButton.setRippleColor(ContextCompat.getColorStateList(this, R.color.moldova_accent));
        }
        
        // Style lifeline buttons (ImageButtons don't have setRippleColor method)
        if (fiftyFiftyButton != null) {
            fiftyFiftyButton.setAlpha(1.0f);
        }
        if (hintButton != null) {
            hintButton.setAlpha(1.0f);
        }
        if (skipQuestionButton != null) {
            skipQuestionButton.setAlpha(1.0f);
        }
    }
    
    /**
     * Use fifty-fifty lifeline
     */
    private void useFiftyFifty() {
        if (isFiftyFiftyUsed || enhancedQuestions == null || currentQuestionIndex >= enhancedQuestions.size()) {
            Toast.makeText(this, "Ai folosit deja varianta 50:50!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        
        // Find correct answer index
            int correctIndex = -1;
            for (int i = 0; i < answerButtons.length; i++) {
                if (answerButtons[i].getText().equals(currentQuestion.getCorrectAnswer())) {
                    correctIndex = i;
                    break;
                }
            }
            
        // Generate two random incorrect indexes to remove
            List<Integer> incorrectIndexes = new ArrayList<>();
            for (int i = 0; i < answerButtons.length; i++) {
                if (i != correctIndex) {
                    incorrectIndexes.add(i);
                }
            }
            
        // Shuffle and remove first two incorrect answers
            Collections.shuffle(incorrectIndexes);
        for (int i = 0; i < 2 && i < incorrectIndexes.size(); i++) {
                int indexToRemove = incorrectIndexes.get(i);
                answerButtons[indexToRemove].setText("");
                answerCards[indexToRemove].setClickable(false);
                answerCards[indexToRemove].setAlpha(0.3f);
            }
            
        // Provide haptic feedback for lifeline usage
        provideHapticFeedback(HapticFeedbackType.LIFELINE);
        
        // Mark as used
            isFiftyFiftyUsed = true;
        lifelinesUsed++;
        if (fiftyFiftyButton != null) {
            fiftyFiftyButton.setAlpha(0.5f);
            fiftyFiftyButton.setClickable(false);
        }
            
        Toast.makeText(this, "✂️ 50:50 folosit! Două răspunsuri greșite eliminate.", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Use hint lifeline
     */
    private void useHint() {
        if (isHintUsed || enhancedQuestions == null || currentQuestionIndex >= enhancedQuestions.size()) {
            Toast.makeText(this, "Ai folosit deja hint-ul!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        
        // Show hint about the correct answer
            String hint = "💡 Hint: " + currentQuestion.getFact();
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("💡 Indiciu")
            .setMessage(hint)
            .setPositiveButton("OK", null)
            .show();
        
        // Provide haptic feedback for lifeline usage
        provideHapticFeedback(HapticFeedbackType.LIFELINE);
        
        // Mark as used
            isHintUsed = true;
        lifelinesUsed++;
        if (hintButton != null) {
            hintButton.setAlpha(0.5f);
            hintButton.setClickable(false);
        }
        
        Toast.makeText(this, "💡 Hint folosit!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Skip current question
     */
    private void skipQuestion() {
        if (isSkipUsed) {
            Toast.makeText(this, "Ai folosit deja opțiunea de a sări o întrebare!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Provide haptic feedback for lifeline usage
        provideHapticFeedback(HapticFeedbackType.LIFELINE);
        
        // Mark as used
            isSkipUsed = true;
        if (skipQuestionButton != null) {
            skipQuestionButton.setAlpha(0.5f);
            skipQuestionButton.setClickable(false);
        }
            
        // Move to next question
            moveToNextQuestion();
        Toast.makeText(this, "⏭️ Întrebare sărita!", Toast.LENGTH_SHORT).show();
    }
    

    
    /**
     * Load questions from Firestore with hybrid storage approach like Transilvania
     */
    private void loadQuestionsFromFirestore() {
        Log.d(TAG, "🔄 Loading questions from Firestore...");
        
        questionRepository.getQuestions(REGION, "general")
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    List<QuestionModel> questions = new ArrayList<>();
                    for (var doc : querySnapshot) {
                        QuestionModel question = doc.toObject(QuestionModel.class);
                        if (question != null) {
                            questions.add(question);
                        }
                    }
                    Log.d(TAG, "✅ Loaded " + questions.size() + " questions from Firestore");
                    
                    // Convert to enhanced questions
                    enhancedQuestions = convertToEnhancedQuestions(questions);
                    
                    // Filter questions based on game mode
                    enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
                    
                    firestoreQuestions = questions;
                    isDataLoaded = true;
                    startGame();
        } else {
                    Log.w(TAG, "⚠️ No questions found in Firestore - using local questions");
                    loadLocalQuestions();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Error loading questions from Firestore", e);
                loadLocalQuestions();
            });
    }
    
    /**
     * Load local questions as fallback
     */
    private void loadLocalQuestions() {
        Log.d(TAG, "📱 Loading local questions as fallback");
        firestoreQuestions = createLocalQuestionsForMigration();
        enhancedQuestions = convertToEnhancedQuestions(firestoreQuestions);
        enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
        isDataLoaded = true;
        startGame();
    }
    
    /**
     * Convert QuestionModel to EnhancedQuestionModel
     */
    private List<EnhancedQuestionModel> convertToEnhancedQuestions(List<QuestionModel> questions) {
        List<EnhancedQuestionModel> enhanced = new ArrayList<>();
        for (QuestionModel q : questions) {
            EnhancedQuestionModel enhanced1 = new EnhancedQuestionModel(
                q.getQuestion(),
                q.getCorrectAnswer(),
                q.getIncorrectAnswers(),
                q.getImageResourceId(),
                q.getFact(),
                inferCategory(q.getQuestion()),
                inferDifficulty(q),
                generateTags(q)
            );
            enhanced.add(enhanced1);
        }
        return enhanced;
    }
    
    /**
     * Infer category from question text
     */
    private EnhancedQuestionModel.Category inferCategory(String questionText) {
        String text = questionText.toLowerCase();
        if (text.contains("domnitor") || text.contains("ștefan") || text.contains("cuza")) {
            return EnhancedQuestionModel.Category.HISTORY;
        } else if (text.contains("râu") || text.contains("munte") || text.contains("oraș")) {
            return EnhancedQuestionModel.Category.GEOGRAPHY;
        } else if (text.contains("mănăstire") || text.contains("biserică") || text.contains("creangă")) {
            return EnhancedQuestionModel.Category.CULTURE;
        } else {
            return EnhancedQuestionModel.Category.GENERAL;
        }
    }
    
    /**
     * Infer difficulty from question
     */
    private EnhancedQuestionModel.Difficulty inferDifficulty(QuestionModel question) {
        String text = question.getQuestion().toLowerCase();
        if (text.length() > 100 || text.contains("secolul")) {
            return EnhancedQuestionModel.Difficulty.HARD;
        } else if (text.contains("care") && text.contains("cel mai")) {
            return EnhancedQuestionModel.Difficulty.MEDIUM;
        } else {
            return EnhancedQuestionModel.Difficulty.EASY;
        }
    }
    
    /**
     * Generate tags for question
     */
    private String[] generateTags(QuestionModel question) {
        List<String> tags = new ArrayList<>();
        String text = question.getQuestion().toLowerCase();
        
        if (text.contains("iași")) tags.add("iași");
        if (text.contains("moldova")) tags.add("moldova");
        if (text.contains("ștefan")) tags.add("ștefan cel mare");
        if (text.contains("mănăstire")) tags.add("mănăstiri");
        
        return tags.toArray(new String[0]);
    }
    
    /**
     * Start the game with enhanced systems
     */
    private void startGame() {
        Log.d(TAG, "🎮 Starting game with enhanced systems");
        
        // Initialize game state
        currentQuestionIndex = 0;
        score = 0;
        streak = 0;
        maxStreak = 0;
        correctAnswers = 0;
        totalTime = 0;
        answerSelected = false;
        
        // Reset lifelines
        isFiftyFiftyUsed = false;
        isHintUsed = false;
        isSkipUsed = false;
        lifelinesUsed = 0;
        
        // Start progress tracking
        progressTracker.startNewSession();
        
        // Set total questions
        totalQuestions = enhancedQuestions != null ? enhancedQuestions.size() : 0;
        
        // Display first question
        displayQuestion();
        
        Log.d(TAG, "🎯 Game started with " + totalQuestions + " questions");
    }
    
    /**
     * Display current question with enhanced features
     */
    private void displayQuestion() {
        if (enhancedQuestions == null || currentQuestionIndex >= enhancedQuestions.size()) {
            finishGame();
            return;
        }
        
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        answerSelected = false;
        questionStartTime = System.currentTimeMillis();
        
        // Display question text
        questionTextView.setText(currentQuestion.getQuestion());
        
        // Display image if available
            if (currentQuestion.getImageResourceId() != 0) {
                questionImage.setVisibility(View.VISIBLE);
                questionImage.setImageResource(currentQuestion.getImageResourceId());
            } else {
                questionImage.setVisibility(View.GONE);
            }
            
        // Reset card styles
            resetCardStyles();
            
        // Display answers
            List<String> allAnswers = new ArrayList<>();
            allAnswers.add(currentQuestion.getCorrectAnswer());
            allAnswers.addAll(currentQuestion.getIncorrectAnswers());
            Collections.shuffle(allAnswers);
            
        for (int i = 0; i < answerButtons.length && i < allAnswers.size(); i++) {
                answerButtons[i].setText(allAnswers.get(i));
                answerCards[i].setClickable(true);
                answerCards[i].setAlpha(1.0f);
            }
            
        // Update progress bar
        progressBar.setMax(totalQuestions);
            progressBar.setProgress(currentQuestionIndex);
            
        // Update UI
        updateScoreDisplay();
        updateStreakDisplay();
        
        // Start timer
            startTimer();
        
        Log.d(TAG, "❓ Displaying question " + (currentQuestionIndex + 1) + "/" + totalQuestions);
    }
    
    /**
     * Start timer for current question
     */
    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        
        timeRemaining = TIME_PER_QUESTION / 1000; // Convert to seconds
        
        timer = new CountDownTimer(TIME_PER_QUESTION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = (int) (millisUntilFinished / 1000);
                if (timerTextView != null) {
                    timerTextView.setText(String.valueOf(timeRemaining));
                }
            }
            
            @Override
            public void onFinish() {
                if (!answerSelected) {
                    handleTimeout();
                }
            }
        }.start();
    }
    
    /**
     * Handle timeout situation
     */
    private void handleTimeout() {
        answerSelected = true;
        
        // Track the timeout as wrong answer
        if (enhancedQuestions != null && currentQuestionIndex < enhancedQuestions.size()) {
            EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
            long timeSpent = System.currentTimeMillis() - questionStartTime;
            
            progressTracker.trackAnswer(
                currentQuestion.getId(),
                false,
                timeSpent,
                currentQuestion.getCategory(),
                currentQuestion.getDifficulty()
            );
        }
        
        // Reset streak
        streak = 0;
        updateStreakDisplay();
        
        // Highlight correct answer
        highlightCorrectAnswer();
        
        // Move to next question after delay
        new Handler().postDelayed(() -> moveToNextQuestion(), 2000);
        
        Toast.makeText(this, "⏰ Timpul a expirat!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Check answer and provide feedback
     */
    private void checkAnswer(int selectedAnswerIndex, String selectedAnswer) {
        if (answerSelected) return;
        
        answerSelected = true;
        timer.cancel();
        
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        boolean isCorrect = selectedAnswer.equals(currentQuestion.getCorrectAnswer());
        long timeSpent = System.currentTimeMillis() - questionStartTime;
        
        // Track answer
        progressTracker.trackAnswer(
            currentQuestion.getId(),
            isCorrect,
            timeSpent,
            currentQuestion.getCategory(),
            currentQuestion.getDifficulty()
        );
        
        if (isCorrect) {
            correctAnswers++;
            streak++;
            if (streak > maxStreak) {
                maxStreak = streak;
            }
            
            // Calculate score with difficulty bonus
            int basePoints = gameModeManager.calculateModeBonus(POINTS_PER_CORRECT_ANSWER, true, timeSpent);
            score += difficultyManager.calculateFinalScore(basePoints);
            
            // Provide haptic feedback for correct answer
            provideHapticFeedback(HapticFeedbackType.CORRECT);
            
            // Animate correct answer
            animateCorrectAnswer(answerCards[selectedAnswerIndex]);
            
            Toast.makeText(this, "✅ Corect! +" + basePoints + " puncte", Toast.LENGTH_SHORT).show();
        } else {
            streak = 0;
            
            // Provide haptic feedback for wrong answer
            provideHapticFeedback(HapticFeedbackType.WRONG);
            
            // Animate wrong answer
            animateWrongAnswer(answerCards[selectedAnswerIndex]);
            highlightCorrectAnswer();
            
            Toast.makeText(this, "❌ Greșit! Răspunsul corect: " + currentQuestion.getCorrectAnswer(), Toast.LENGTH_LONG).show();
        }
        
        // Update displays
        updateScoreDisplay();
        updateStreakDisplay();
        totalTime += timeSpent;
        
        // Show answer dialog with fact
        String fact = currentQuestion.getFact();
        if (fact != null && !fact.isEmpty()) {
            // Delay to show animation before dialog
            new Handler().postDelayed(() -> showAnswerDialog(fact, isCorrect), 1500);
        } else {
            // Check if game should end (for elimination modes)
            if (gameModeManager.shouldEndGame(!isCorrect, 0)) {
                new Handler().postDelayed(() -> finishGame(), 2000);
            } else {
                // Move to next question after delay
                new Handler().postDelayed(() -> moveToNextQuestion(), 2000);
            }
        }
    }
    
    /**
     * Move to next question
     */
    private void moveToNextQuestion() {
        currentQuestionIndex++;
        
        if (currentQuestionIndex >= totalQuestions || 
            gameModeManager.isGameComplete(currentQuestionIndex)) {
            finishGame();
        } else {
            displayQuestion();
        }
    }
    
    /**
     * Update score display
     */
    private void updateScoreDisplay() {
        if (scoreTextView != null) {
            scoreTextView.setText("Scor: " + score);
        }
    }
    
    /**
     * Update streak display
     */
    private void updateStreakDisplay() {
        if (streakTextView != null) {
            streakTextView.setText("Streak: " + streak);
        }
    }
    
    /**
     * Animate correct answer
     */
    private void animateCorrectAnswer(MaterialCardView card) {
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.moldova_primary));
        
        // Scale animation
        card.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(300)
            .withEndAction(() -> {
                card.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200);
            });
    }
    
    /**
     * Animate wrong answer
     */
    private void animateWrongAnswer(MaterialCardView card) {
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.wrong_answer));
        
        // Shake animation
        card.animate()
            .translationX(10f)
            .setDuration(100)
            .withEndAction(() -> {
                card.animate()
                    .translationX(-10f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        card.animate()
                            .translationX(0f)
                            .setDuration(100);
                    });
            });
    }
    
    /**
     * Highlight correct answer
     */
    private void highlightCorrectAnswer() {
        if (enhancedQuestions == null || currentQuestionIndex >= enhancedQuestions.size()) return;
        
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        
        for (int i = 0; i < answerButtons.length; i++) {
            if (answerButtons[i].getText().equals(currentQuestion.getCorrectAnswer())) {
                answerCards[i].setCardBackgroundColor(ContextCompat.getColor(this, R.color.moldova_primary));
                answerButtons[i].setTextColor(ContextCompat.getColor(this, R.color.white));
                break;
            }
        }
    }

    /**
     * Reset card styles for new question
     */
    private void resetCardStyles() {
        for (int i = 0; i < answerCards.length; i++) {
            MaterialCardView card = answerCards[i];
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
            MaterialButton button = answerButtons[i];
            button.setTextColor(ContextCompat.getColor(this, R.color.black));
            card.setStrokeWidth(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // 🔄 CHECK FOR UPDATES: Verificăm dacă sunt actualizări în baza de date
        if (syncManager != null && syncManager.isInternetAvailable() && isDataLoaded) {
            checkForQuestionUpdates();
        }
        
        // Resume timer if needed
        if (!answerSelected && enhancedQuestions != null && currentQuestionIndex < enhancedQuestions.size()) {
            startTimer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Anulăm timer-ul pentru a preveni memory leaks
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    protected void showFinishButton() {
        finishButton.setVisibility(View.VISIBLE);
        progressBar.setProgress(progressBar.getMax());
        
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        finishButton.startAnimation(fadeIn);
    }

    private String getAchievements() {
        StringBuilder achievements = new StringBuilder();
        
        // Adăugăm realizările în funcție de performanță
        if (correctAnswers == totalQuestions) {
            achievements.append("🏆 Perfecțiune! Ai răspuns corect la toate întrebările!\n\n");
        } else if ((double) correctAnswers / totalQuestions >= 0.8) {
            achievements.append("🥇 Expert în Moldova! Cunoști foarte bine această regiune!\n\n");
        } else if ((double) correctAnswers / totalQuestions >= 0.6) {
            achievements.append("🥈 Bun cunoscător al Moldovei! Ai făcut față cu brio testului!\n\n");
        } else if ((double) correctAnswers / totalQuestions >= 0.4) {
            achievements.append("🥉 Cunoștințe decente despre Moldova!\n\n");
        } else {
            achievements.append("Mai ai de învățat despre Moldova!\n\n");
        }
        
        // Adăugăm statistici
        achievements.append(String.format("Răspunsuri corecte: %d/%d (%.1f%%)\n", 
            correctAnswers, totalQuestions, (double) correctAnswers / totalQuestions * 100));
        
        if (maxStreak > 1) {
            achievements.append(String.format("Cel mai mare streak: %d răspunsuri consecutive\n", maxStreak));
        }
        
        achievements.append(String.format("Scor final: %d puncte\n", score));
        
        // Adăugăm bonus final
        if (correctAnswers > totalQuestions / 2) {
            achievements.append(String.format("\nBONUS: +%d puncte!", BONUS_POINTS));
            score += BONUS_POINTS;
        }
        
        return achievements.toString();
    }

    /**
     * Check for question updates following Transilvania pattern
     */
    private void checkForQuestionUpdates() {
        String cacheKey = "questions_cache_" + REGION + "_" + GAME_TYPE + "_timestamp";
        long lastCacheTime = getSharedPreferences("HybridStorage", MODE_PRIVATE).getLong(cacheKey, 0);
        long currentTime = System.currentTimeMillis();
        
        // Check for updates only if more than 30 minutes have passed
        if (currentTime - lastCacheTime > 30 * 60 * 1000) {
            questionRepository.getQuestions(REGION, GAME_TYPE)
                .addOnSuccessListener(querySnapshot -> {
                    int onlineCount = querySnapshot.size();
                    int localCount = enhancedQuestions != null ? enhancedQuestions.size() : 0;
                    
                    if (onlineCount != localCount) {
                        Log.d(TAG, "🔄 Updates detected: online=" + onlineCount + ", local=" + localCount);
                        showUpdateAvailableDialog();
                    } else {
                        // Update cache timestamp
                        getSharedPreferences("HybridStorage", MODE_PRIVATE)
                            .edit()
                            .putLong(cacheKey, currentTime)
                            .apply();
                    }
            })
            .addOnFailureListener(e -> {
                    Log.w(TAG, "🔄 Could not check for updates", e);
                });
        }
    }
    
    /**
     * Show update available dialog
     */
    private void showUpdateAvailableDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("🆕 Actualizări Disponibile")
            .setMessage("Sunt disponibile întrebări noi pentru Moldova!\n\n" +
                       "Doriți să reîncărcați pentru a avea cele mai recente întrebări?")
            .setPositiveButton("🔄 Actualizează", (dialog, which) -> {
                Toast.makeText(this, "🔄 Reîncărcăm întrebările...", Toast.LENGTH_SHORT).show();
                loadQuestionsFromFirestore();
            })
            .setNegativeButton("📱 Mai târziu", null)
            .show();
    }
    
    /**
     * 💾 Verifică preferința utilizatorului și încarcă în consecință
     */
    private void checkUserPreferenceAndLoad() {
        SharedPreferences prefs = getSharedPreferences("MoldovaGamePrefs", MODE_PRIVATE);
        String savedPreference = prefs.getString("data_source_preference", "ask_every_time");
        boolean hasInternet = syncManager.isInternetAvailable();
        boolean hasLocalCache = checkIfLocalCacheExists();
        
        Log.d(TAG, "🔍 Verificăm preferința utilizatorului: " + savedPreference + 
              ", Internet: " + hasInternet + ", Cache: " + hasLocalCache);
        
        switch (savedPreference) {
            case "always_database":
                if (hasInternet) {
                    Toast.makeText(this, "🌐 Încărcăm din baza de date (preferință salvată)...", Toast.LENGTH_SHORT).show();
                    loadQuestionsFromDatabase();
                } else {
                    showNoInternetForPreferredDatabaseDialog();
                }
                break;
                
            case "always_local":
                if (hasLocalCache) {
                    Toast.makeText(this, "📱 Încărcăm din cache local (preferință salvată)...", Toast.LENGTH_SHORT).show();
                    loadQuestionsFromLocalCache();
                } else {
                    showNoCacheForPreferredLocalDialog();
                }
                break;
                
            case "ask_every_time":
            default:
                showDataSourceSelectionDialog();
                break;
        }
    }
    
    /**
     * 🚫 Dialog când nu există internet pentru baza de date preferată
     */
    private void showNoInternetForPreferredDatabaseDialog() {
        boolean hasLocalCache = checkIfLocalCacheExists();
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
            .setTitle("🌐 Fără Internet")
            .setMessage("Preferința ta este să încarci din baza de date, dar nu ai conexiune la internet.\n\n" +
                       (hasLocalCache ? "Putem încărca din cache-ul local disponibil." : "Nu există nici cache local disponibil."));
        
        if (hasLocalCache) {
            builder.setPositiveButton("📱 Folosește cache local", (dialog, which) -> {
                loadQuestionsFromLocalCache();
            });
        }
        
        builder.setNegativeButton("⚙️ Schimbă preferințele", (dialog, which) -> {
                showDataSourceSelectionDialogWithPreferences();
            })
            .setNeutralButton("🔄 Încearcă din nou", (dialog, which) -> {
                checkUserPreferenceAndLoad();
            })
            .show();
    }
    
    /**
     * 📱 Dialog când nu există cache local pentru preferința locală
     */
    private void showNoCacheForPreferredLocalDialog() {
        boolean hasInternet = syncManager.isInternetAvailable();
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
            .setTitle("📱 Fără Cache Local")
            .setMessage("Preferința ta este să încarci din cache local, dar nu există întrebări salvate local.\n\n" +
                       (hasInternet ? "Putem încărca din baza de date online." : "Nu există nici conexiune la internet."));
        
        if (hasInternet) {
            builder.setPositiveButton("🌐 Folosește baza de date", (dialog, which) -> {
                loadQuestionsFromDatabase();
            });
        }
        
        builder.setNegativeButton("⚙️ Schimbă preferințele", (dialog, which) -> {
                showDataSourceSelectionDialogWithPreferences();
            })
            .setNeutralButton("🔄 Încearcă din nou", (dialog, which) -> {
                checkUserPreferenceAndLoad();
            })
            .show();
    }
    
    /**
     * 🎯 Afișează dialogul de selecție sursă date simplu
     */
    private void showDataSourceSelectionDialog() {
        boolean hasInternet = syncManager.isInternetAvailable();
        boolean hasLocalCache = checkIfLocalCacheExists();
        
        if (!hasInternet && !hasLocalCache) {
            handleNoQuestionsAvailable();
            return;
        }
        
        showDataSourceSelectionDialogWithPreferences();
    }
    
    /**
     * 🎯 Afișează dialogul complex de selecție sursă date cu preferințe
     */
    private void showDataSourceSelectionDialogWithPreferences() {
        boolean hasInternet = syncManager.isInternetAvailable();
        boolean hasLocalCache = checkIfLocalCacheExists();
        
        // Construim lista de opțiuni disponibile
        List<String> options = new ArrayList<>();
        List<String> optionKeys = new ArrayList<>();
        
        if (hasInternet) {
            options.add("🌐 Baza de date online (cea mai recentă)");
            optionKeys.add("database");
        }
        
        if (hasLocalCache) {
            options.add("📱 Cache local (mai rapid)");
            optionKeys.add("cache");
        }
        
        if (options.isEmpty()) {
            handleNoQuestionsAvailable();
            return;
        }
        
        String[] optionsArray = options.toArray(new String[0]);
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("📊 Selectează Sursa de Date")
            .setMessage("De unde vrei să încarci întrebările pentru acest quiz?\n\n" +
                       "💡 Baza de date: Întrebări actualizate, necesită internet\n" +
                       "💡 Cache local: Mai rapid, funcționează offline")
            .setItems(optionsArray, (dialog, which) -> {
                String selectedKey = optionKeys.get(which);
                
                if ("database".equals(selectedKey)) {
                    loadQuestionsFromDatabase();
                } else if ("cache".equals(selectedKey)) {
                    loadQuestionsFromLocalCache();
                }
            })
            .setNeutralButton("⚙️ Setează ca preferință", (dialog, which) -> {
                showPreferenceSettingDialog();
            })
            .setNegativeButton("❌ Anulează", (dialog, which) -> finish())
            .show();
    }
    
    /**
     * ⚙️ Dialog pentru setarea preferințelor
     */
    private void showPreferenceSettingDialog() {
        boolean hasInternet = syncManager.isInternetAvailable();
        boolean hasLocalCache = checkIfLocalCacheExists();
        
        List<String> options = new ArrayList<>();
        List<String> preferenceKeys = new ArrayList<>();
        
        options.add("❓ Întreabă de fiecare dată");
        preferenceKeys.add("ask_every_time");
        
        if (hasInternet) {
            options.add("🌐 Întotdeauna baza de date");
            preferenceKeys.add("always_database");
        }
        
        if (hasLocalCache) {
            options.add("📱 Întotdeauna cache local");
            preferenceKeys.add("always_local");
        }
        
        String[] optionsArray = options.toArray(new String[0]);
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("⚙️ Setează Preferința")
            .setMessage("Cum vrei să se încarce întrebările în viitor?\n\n" +
                       "Poți schimba această preferință oricând din setări.")
            .setItems(optionsArray, (dialog, which) -> {
                String selectedPreference = preferenceKeys.get(which);
                saveUserPreference(selectedPreference);
                
                Toast.makeText(this, "✅ Preferința a fost salvată!", Toast.LENGTH_SHORT).show();
                
                // Reîncărcăm folosind noua preferință
                checkUserPreferenceAndLoad();
            })
            .setNegativeButton("❌ Anulează", (dialog, which) -> {
                showDataSourceSelectionDialog();
            })
            .show();
    }
    
    /**
     * 🔍 Verifică dacă există cache local
     */
    private boolean checkIfLocalCacheExists() {
        String cacheKey = "questions_cache_" + REGION + "_" + GAME_TYPE;
        SharedPreferences hybridPrefs = getSharedPreferences("HybridStorage", MODE_PRIVATE);
        
        // Verificăm dacă există date în cache
        String cachedData = hybridPrefs.getString(cacheKey, null);
        if (cachedData == null || cachedData.isEmpty()) {
            return false;
        }
        
        // Verificăm dacă cache-ul nu e prea vechi (maxim 7 zile)
        long cacheTimestamp = hybridPrefs.getLong(cacheKey + "_timestamp", 0);
        long currentTime = System.currentTimeMillis();
        long maxCacheAge = 7 * 24 * 60 * 60 * 1000L; // 7 zile în milisecunde
        
        boolean isCacheValid = (currentTime - cacheTimestamp) < maxCacheAge;
        
        Log.d(TAG, "🔍 Cache check: exists=" + true + ", valid=" + isCacheValid + 
              ", age=" + ((currentTime - cacheTimestamp) / (1000 * 60 * 60)) + "h");
        
        return isCacheValid;
    }
    
    /**
     * 💾 Salvează preferința utilizatorului
     */
    private void saveUserPreference(String preference) {
        SharedPreferences prefs = getSharedPreferences("MoldovaGamePrefs", MODE_PRIVATE);
        prefs.edit()
             .putString("data_source_preference", preference)
             .apply();
        
        Log.d(TAG, "💾 Preferința salvată: " + preference);
    }
    
    /**
     * 🔄 Resetează preferințele utilizatorului
     */
    private void resetUserPreferences() {
        SharedPreferences prefs = getSharedPreferences("MoldovaGamePrefs", MODE_PRIVATE);
        prefs.edit()
             .remove("data_source_preference")
             .apply();
        
        Log.d(TAG, "🔄 Preferințele au fost resetate");
    }
    
    /**
     * 🌐 Încarcă întrebările din baza de date online
     */
    /**
     * 💾 CACHE LOCAL: Salvează întrebările în cache pentru utilizare offline
     */
    private void saveQuestionsToLocalCache(List<QuestionModel> questions) {
        // Convertim întrebările într-un format compatibil cu JSON/Firestore
        List<Map<String, Object>> questionMaps = new ArrayList<>();
        for (QuestionModel question : questions) {
            Map<String, Object> questionMap = new HashMap<>();
            questionMap.put("question", question.getQuestion());
            questionMap.put("correctAnswer", question.getCorrectAnswer());
            questionMap.put("incorrectAnswers", question.getIncorrectAnswers());
            questionMap.put("fact", question.getFact());
            questionMap.put("imageResourceId", question.getImageResourceId());
            questionMaps.add(questionMap);
        }
        
        Map<String, Object> cacheData = new HashMap<>();
        cacheData.put("questions", questionMaps);
        cacheData.put("region", REGION);
        cacheData.put("gameType", GAME_TYPE);
        cacheData.put("timestamp", System.currentTimeMillis());
        cacheData.put("count", questions.size());
        
        // Salvăm în sistemul hibrid pentru cache local
        syncManager.saveData("questions_cache", REGION + "_" + GAME_TYPE, cacheData, new SyncManager.SyncCallback() {
    @Override
            public void onSyncComplete(boolean success, String message) {
                if (success) {
                    Log.d(TAG, "💾 ✅ Questions cached locally: " + questions.size() + " questions");
                } else {
                    Log.w(TAG, "💾 ⚠️ Failed to cache questions locally: " + message);
                }
            }
        });
    }
    
    /**
     * 💾 CACHE LOCAL: Încarcă întrebările din cache local
     */
    private void loadQuestionsFromLocalCache() {
        Log.d(TAG, "💾 Încercăm să încărcăm din cache local pentru " + REGION + "_" + GAME_TYPE);
        
        // Încercăm să încărcăm din SharedPreferences (cache local)
        String cacheKey = "questions_cache_" + REGION + "_" + GAME_TYPE;
        String cachedJson = getSharedPreferences("HybridStorage", MODE_PRIVATE).getString(cacheKey, null);
        
        if (cachedJson != null && !cachedJson.isEmpty()) {
            try {
                // Parsăm datele din cache
                com.google.gson.Gson gson = new com.google.gson.Gson();
                com.google.gson.reflect.TypeToken<Map<String, Object>> typeToken = 
                    new com.google.gson.reflect.TypeToken<Map<String, Object>>(){};
                Map<String, Object> cacheData = gson.fromJson(cachedJson, typeToken.getType());
                
                if (cacheData != null && cacheData.containsKey("questions")) {
                    // Extragem lista de întrebări din cache
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> questionMaps = (List<Map<String, Object>>) cacheData.get("questions");
                    List<QuestionModel> cachedQuestions = new ArrayList<>();
                    
                    for (Map<String, Object> questionMap : questionMaps) {
                        // Reconstituim QuestionModel din Map
                        String question = (String) questionMap.get("question");
                        String correctAnswer = (String) questionMap.get("correctAnswer");
                        @SuppressWarnings("unchecked")
                        List<String> incorrectAnswersList = (List<String>) questionMap.get("incorrectAnswers");
                        String fact = (String) questionMap.get("fact");
                        
                        // Folosim noul constructor cu List<String> pentru compatibilitate Firebase
                        QuestionModel questionModel = new QuestionModel(question, correctAnswer, incorrectAnswersList, 0, fact);
                        cachedQuestions.add(questionModel);
                    }
                    
                    if (!cachedQuestions.isEmpty()) {
                        firestoreQuestions = cachedQuestions;
                        Log.d(TAG, "💾 ✅ Întrebări încărcate din cache local: " + firestoreQuestions.size());
                        
                        // Convertim în enhanced questions și aplicăm filtre
                        enhancedQuestions = convertToEnhancedQuestions(firestoreQuestions);
                        enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
                        
                        isDataLoaded = true;
                        startGame();
                        
                        // Notificăm utilizatorul că folosim cache-ul
                        Toast.makeText(this, "📱 Utilizez întrebări din cache (offline)", Toast.LENGTH_SHORT).show();
                        
                        // Adăugăm indicator vizual pentru sursă
                        updateDataSourceIndicator("📱 Cache Local");
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "💾 ❌ Eroare la parsarea cache-ului local", e);
            }
        }
        
        // Dacă nu avem cache local, încercăm migrarea ca ultimă soluție
        Log.w(TAG, "💾 ❌ Nu există cache local - încercăm migrarea ca ultimă soluție");
        handleNoQuestionsAvailable();
    }
    
    /**
     * 🚨 Gestionează cazul când nu sunt disponibile întrebări nicăieri
     */
    private void handleNoQuestionsAvailable() {
        if (syncManager.isInternetAvailable()) {
            // Avem internet dar nu avem întrebări în Firestore - încercăm migrarea
            Log.d(TAG, "🔄 Internet disponibil - încercăm migrarea întrebărilor în Firestore");
            migrateQuestionsToFirestore();
        } else {
            // Nu avem internet și nici cache local - afișăm eroare
            showOfflineNoQuestionsError();
        }
    }
    
    /**
     * 📱 Afișează eroare pentru lipsa întrebărilor offline
     */
    private void showOfflineNoQuestionsError() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("📱 Mod Offline")
            .setMessage("Nu sunt disponibile întrebări pentru joc offline.\n\n" +
                       "💡 Pentru a juca:\n" +
                       "• Conectați-vă la internet\n" +
                       "• Jucați o dată pentru a descărca întrebările\n" +
                       "• Apoi veți putea juca și offline")
            .setPositiveButton("🔄 Încearcă din nou", (dialog, which) -> {
                checkUserPreferenceAndLoad();
            })
            .setNegativeButton("🚪 Înapoi", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }
    
    /**
     * 🔄 Migrează întrebările locale în Firestore pentru consistența bazei de date
     */
    private void migrateQuestionsToFirestore() {
        Log.d(TAG, "🔄 Începem migrarea întrebărilor locale în Firestore pentru " + REGION);
        
        // Creăm întrebările locale temporar doar pentru migrare
        List<QuestionModel> localQuestions = createLocalQuestionsForMigration();
        
        if (localQuestions.isEmpty()) {
            Log.e(TAG, "❌ Nu avem întrebări locale pentru migrare");
            showNoQuestionsError();
            return;
        }
        
        Log.d(TAG, "📝 Pregătim " + localQuestions.size() + " întrebări pentru migrare");
        
        // Salvăm întrebările în Firestore
        questionRepository.addQuestions(localQuestions, REGION, GAME_TYPE)
            .thenAccept(voidResult -> {
                runOnUiThread(() -> {
                    Log.d(TAG, "✅ Migrare completă! Reîncărcăm din Firestore...");
                    Toast.makeText(this, "✅ Întrebări create! Reîncărcăm...", Toast.LENGTH_SHORT).show();
                    
                    // Reîncărcăm din Firestore acum că avem datele
                    loadQuestionsFromDatabase();
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    Log.e(TAG, "❌ Eroare la migrarea în Firestore", throwable);
                    showMigrationErrorWithAlternative(throwable);
                });
                return null;
            });
    }
    
    /**
     * 🚫 Arată eroare de migrare cu alternativă locală
     */
    private void showMigrationErrorWithAlternative(Throwable error) {
        String errorMessage = error.getMessage() != null ? error.getMessage() : "Eroare necunoscută";
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("🔄 Eroare Migrare")
            .setMessage("Nu s-au putut sincroniza întrebările cu serverul:\n\n" + errorMessage + 
                       "\n\nVrei să folosești întrebările locale direct?")
            .setPositiveButton("📝 Da, folosește local", (dialog, which) -> {
                useLocalQuestionsDirectly();
            })
            .setNegativeButton("❌ Înapoi", (dialog, which) -> finish())
            .show();
    }
    
    /**
     * 📝 Folosește întrebările locale direct (fallback)
     */
    private void useLocalQuestionsDirectly() {
        Log.d(TAG, "📝 Folosim întrebările locale direct ca fallback");
        
        firestoreQuestions = createLocalQuestionsForMigration();
        
        if (firestoreQuestions.isEmpty()) {
            showNoQuestionsError();
            return;
        }
        
        enhancedQuestions = convertToEnhancedQuestions(firestoreQuestions);
        enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
        
        isDataLoaded = true;
        startGame();
        
        Toast.makeText(this, "📝 Utilizez întrebări locale", Toast.LENGTH_SHORT).show();
        updateDataSourceIndicator("📝 Întrebări Locale");
    }
    
    /**
     * 🚫 Arată eroare când nu sunt întrebări disponibile
     */
    private void showNoQuestionsError() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Eroare")
            .setMessage("Nu sunt disponibile întrebări pentru acest quiz.\n\nVă rugăm să încercați mai târziu.")
            .setPositiveButton("❌ Înapoi", (dialog, which) -> finish())
            .show();
    }
    
    private void loadQuestionsFromDatabase() {
        if (!syncManager.isInternetAvailable()) {
            Log.w(TAG, "🌐 Nu există conexiune la internet pentru baza de date");
            
            if (checkIfLocalCacheExists()) {
                new MaterialAlertDialogBuilder(this)
                    .setTitle("🌐 Fără Internet")
                    .setMessage("Nu se poate conecta la baza de date.\nÎncărcăm din cache-ul local disponibil.")
                    .setPositiveButton("📱 OK", (dialog, which) -> {
                        loadQuestionsFromLocalCache();
                    })
                    .show();
            } else {
                showOfflineNoQuestionsError();
            }
            return;
        }
        
        Log.d(TAG, "🌐 Încărcăm întrebări din baza de date pentru " + REGION);
        updateDataSourceIndicator("🌐 Baza de date");
        
        questionRepository.getQuestions(REGION, GAME_TYPE)
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    Log.w(TAG, "🌐 Nu s-au găsit întrebări în baza de date");
                    showNoDatabaseQuestionsDialog();
                    return;
                }
                
                firestoreQuestions = querySnapshot.toObjects(QuestionModel.class);
                
                Log.d(TAG, "✅ Încărcate " + firestoreQuestions.size() + " întrebări din baza de date");
                
                // Salvăm în cache hibrid pentru folosire ulterioară
                saveQuestionsToLocalCache(firestoreQuestions);
                
                // Convertim și pornim jocul
                enhancedQuestions = convertToEnhancedQuestions(firestoreQuestions);
                
                // Filtrăm pe baza modului de joc
                enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
                
                startGame();
                isDataLoaded = true;
                
                Toast.makeText(this, "✅ Întrebări încărcate din baza de date!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Eroare la încărcarea din baza de date", e);
                showEnhancedDatabaseErrorDialog(e);
            });
    }
    
    /**
     * 🚫 Dialog când nu există întrebări în baza de date
     */
    private void showNoDatabaseQuestionsDialog() {
        boolean hasLocalCache = checkIfLocalCacheExists();
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
            .setTitle("🌐 Baza de Date Goală")
            .setMessage("Nu s-au găsit întrebări pentru Moldova în baza de date online.\n\n" +
                       (hasLocalCache ? "Vrei să folosești cache-ul local?" : "Nu există nici cache local disponibil."));
        
        if (hasLocalCache) {
            builder.setPositiveButton("📱 Folosește cache local", (dialog, which) -> {
                loadQuestionsFromLocalCache();
            });
        } else {
            builder.setPositiveButton("📝 Folosește întrebările implicite", (dialog, which) -> {
                useLocalQuestionsDirectly();
            });
        }
        
        builder.setNegativeButton("❌ Înapoi", (dialog, which) -> finish())
               .show();
    }
    
    /**
     * 🌐 Dialog pentru eroarea de bază de date
     */
    private void showDatabaseErrorDialog() {
        boolean hasLocalCache = checkIfLocalCacheExists();
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("🌐 Eroare Bază de Date")
            .setMessage("A apărut o eroare la încărcarea din baza de date.\n\n" +
                       (hasLocalCache ? "Vrei să folosești cache-ul local?" : "Vrei să folosești întrebările implicite?"))
            .setPositiveButton(hasLocalCache ? "📱 Cache local" : "📝 Întrebări implicite", (dialog, which) -> {
                if (hasLocalCache) {
                    loadQuestionsFromLocalCache();
                } else {
                    useLocalQuestionsDirectly();
                }
            })
            .setNegativeButton("❌ Înapoi", (dialog, which) -> finish())
            .show();
    }
    
    /**
     * 🌐 Dialog îmbunătățit pentru eroarea de bază de date
     */
    private void showEnhancedDatabaseErrorDialog(Throwable error) {
        boolean hasLocalCache = checkIfLocalCacheExists();
        
        String errorMessage = "A apărut o eroare la încărcarea din baza de date:\n\n";
        
        // Analizăm tipul de eroare pentru mesaje mai clare
        if (error.getMessage() != null) {
            if (error.getMessage().contains("network")) {
                errorMessage += "🌐 Problemă de rețea - verifică conexiunea la internet.";
            } else if (error.getMessage().contains("permission")) {
                errorMessage += "🔒 Problemă de autorizare - verifică permisiunile.";
            } else if (error.getMessage().contains("timeout")) {
                errorMessage += "⏱️ Timeout - serverul nu răspunde.";
            } else {
                errorMessage += "⚠️ " + error.getMessage();
            }
        } else {
            errorMessage += "⚠️ Eroare necunoscută.";
        }
        
        errorMessage += "\n\n" + (hasLocalCache ? "Vrei să folosești cache-ul local?" : "Vrei să folosești întrebările implicite?");
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
            .setTitle("🌐 Eroare Bază de Date")
            .setMessage(errorMessage);
        
        if (hasLocalCache) {
            builder.setPositiveButton("📱 Cache local", (dialog, which) -> {
                loadQuestionsFromLocalCache();
            });
        }
        
        builder.setNeutralButton("📝 Întrebări implicite", (dialog, which) -> {
                useLocalQuestionsDirectly();
            })
            .setNegativeButton("❌ Înapoi", (dialog, which) -> finish())
            .show();
    }
    
    /**
     * 📊 Actualizează indicatorul sursei de date
     */
    private void updateDataSourceIndicator(String source) {
        Log.d(TAG, "📊 Sursă date: " + source);
        // Aici putem adăuga un indicator vizual în viitor
    }
    
    /**
     * 🎯 Tipuri de feedback haptic
     */
    private enum HapticFeedbackType {
        CORRECT, WRONG, LIFELINE
    }
    
    /**
     * 📳 Furnizează feedback haptic pentru diferite tipuri de evenimente
     */
    private void provideHapticFeedback(HapticFeedbackType type) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // API 26+ - VibrationEffect
            VibrationEffect effect;
            
            switch (type) {
                case CORRECT:
                    // Pattern pentru răspuns corect: scurt, pauză, scurt
                    long[] correctPattern = {0, 50, 30, 50};
                    int[] correctAmplitudes = {0, 120, 0, 120};
                    effect = VibrationEffect.createWaveform(correctPattern, correctAmplitudes, -1);
                    break;
                    
                case WRONG:
                    // Pattern pentru răspuns greșit: lung, pauză scurtă, lung
                    long[] wrongPattern = {0, 200, 50, 200};
                    int[] wrongAmplitudes = {0, 200, 0, 200};
                    effect = VibrationEffect.createWaveform(wrongPattern, wrongAmplitudes, -1);
                    break;
                    
                case LIFELINE:
                    // Pattern pentru lifeline: trei impulsuri scurte
                    long[] lifelinePattern = {0, 30, 30, 30, 30, 30};
                    int[] lifelineAmplitudes = {0, 100, 0, 100, 0, 100};
                    effect = VibrationEffect.createWaveform(lifelinePattern, lifelineAmplitudes, -1);
                    break;
                    
                default:
                    effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE);
                    break;
            }
            
            vibrator.vibrate(effect);
        } else {
            // API 25 și mai mic - vibrate clasic
            switch (type) {
                case CORRECT:
                    long[] correctPattern = {0, 50, 30, 50};
                    vibrator.vibrate(correctPattern, -1);
                    break;
                    
                case WRONG:
                    long[] wrongPattern = {0, 200, 50, 200};
                    vibrator.vibrate(wrongPattern, -1);
                    break;
                    
                case LIFELINE:
                    long[] lifelinePattern = {0, 30, 30, 30, 30, 30};
                    vibrator.vibrate(lifelinePattern, -1);
                    break;
                    
                default:
                    vibrator.vibrate(50);
                    break;
            }
        }
        
        Log.d(TAG, "📳 Feedback haptic: " + type);
    }
    
    /**
     * 💬 Afișează dialog cu informații detaliate despre răspuns
     */
    private void showAnswerDialog(String fact, boolean isCorrect) {
        if (fact != null && !fact.isEmpty()) {
            String title = isCorrect ? "✅ Răspuns corect!" : "❌ Răspuns greșit";
            String emoji = isCorrect ? "🎉" : "📚";
            
            // Verificăm dacă este ultima întrebare
            boolean isLastQuestion = (currentQuestionIndex + 1) >= totalQuestions;
            String continueButtonText = isLastQuestion ? "🏁 Vezi rezultate" : "➡️ Următoarea întrebare";
            
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(emoji + " " + fact)
                .setPositiveButton(continueButtonText, (dialog, which) -> {
                    dialog.dismiss();
                    if (isLastQuestion) {
                        currentQuestionIndex++;
                        finishGame();
                    } else {
                        // Check if game should end (for elimination modes)
                        if (gameModeManager.shouldEndGame(!isCorrect, 0)) {
                            finishGame();
                        } else {
                            moveToNextQuestion();
                        }
                    }
                })
                .setCancelable(false);
            
            // Adăugăm buton de încheiere pentru toate întrebările
            if (!isLastQuestion) {
                dialogBuilder.setNegativeButton("🚪 Încheie quiz", (dialog, which) -> {
                    dialog.dismiss();
                    showConfirmQuitDialog();
                });
            }
            
            dialogBuilder.show();
        } else {
            // Pentru cazurile fără fact, afișăm un dialog simplu cu opțiunile
            showQuickActionDialog(isCorrect);
        }
    }
    
    /**
     * 🚪 Dialog rapid pentru acțiuni când nu avem fact
     */
    private void showQuickActionDialog(boolean isCorrect) {
        boolean isLastQuestion = (currentQuestionIndex + 1) >= totalQuestions;
        String continueButtonText = isLastQuestion ? "🏁 Vezi rezultate" : "➡️ Următoarea întrebare";
        
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
            .setTitle("Continuă quiz-ul?")
            .setMessage("Vrei să continui la următoarea întrebare?")
            .setPositiveButton(continueButtonText, (dialog, which) -> {
                dialog.dismiss();
                if (isLastQuestion) {
                    currentQuestionIndex++;
                    finishGame();
                } else {
                    // Check if game should end (for elimination modes)
                    if (gameModeManager.shouldEndGame(!isCorrect, 0)) {
                        finishGame();
                    } else {
                        moveToNextQuestion();
                    }
                }
            })
            .setCancelable(false);
        
        // Adăugăm buton de încheiere dacă nu e ultima întrebare
        if (!isLastQuestion) {
            dialogBuilder.setNegativeButton("🚪 Încheie quiz", (dialog, which) -> {
                dialog.dismiss();
                showConfirmQuitDialog();
            });
        }
        
        dialogBuilder.show();
    }
    
    /**
     * 🚪 Dialog îmbunătățit de confirmare pentru ieșirea din quiz
     */
    private void showConfirmQuitDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("🚪 Confirmare ieșire")
            .setMessage("Ești sigur că vrei să închei quiz-ul?\n\n" +
                       "📊 Progresul tău:\n" +
                       "• Întrebări răspunse: " + currentQuestionIndex + " din " + totalQuestions + "\n" +
                       "• Scor curent: " + score + " puncte\n" +
                       "• Răspunsuri corecte: " + correctAnswers + "\n\n" +
                       "⚠️ Dacă ieși acum, progresul va fi salvat parțial.")
            .setPositiveButton("✓ Da, încheie", (dialog, which) -> {
                dialog.dismiss();
                finishGame();
            })
            .setNegativeButton("✗ Nu, continuă", (dialog, which) -> {
                dialog.dismiss();
                Log.d(TAG, "🔄 User chose to continue quiz from quit dialog");
                
                // Re-activăm cardurile pentru ca utilizatorul să poată răspunde
                resetCardStyles();
                for (MaterialCardView card : answerCards) {
                    card.setClickable(true);
                    card.setEnabled(true);
                }
                for (MaterialButton button : answerButtons) {
                    button.setEnabled(true);
                    button.setClickable(true);
                }
                
                // Repornește timer-ul dacă nu este activ
                if (timer == null) {
                    answerSelected = false;
                    startTimer();
                }
            })
            .show();
    }
} 