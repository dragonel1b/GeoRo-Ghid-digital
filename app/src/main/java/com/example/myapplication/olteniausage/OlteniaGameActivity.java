package com.example.myapplication.olteniausage;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.myapplication.utils.SyncManager;

import com.example.myapplication.models.EnhancedQuestionModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.Oltenia;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.models.QuestionModel;
import com.example.myapplication.model.QuizResult;
import com.example.myapplication.repository.FirestoreQuestionRepository;
import com.example.myapplication.Joc1.AchievementManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Date;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.auth.FirebaseUser;

public class OlteniaGameActivity extends AppCompatActivity {
    private static final String TAG = "OlteniaGameActivity";
    private static final String REGION = "oltenia";
    private static final String GAME_TYPE = "quiz";
    
    private TextView questionTextView;
    private MaterialButton[] answerButtons;
    private TextView scoreTextView;
    private ProgressBar progressBar;
    private TextView timerTextView;
    private TextView streakTextView;
    private ImageView questionImage;
    private MaterialCardView fiftyFiftyCard;
    private MaterialCardView hintCard;
    private MaterialCardView skipQuestionCard;
    private MaterialCardView audienceCard;
    private MaterialCardView phoneCard;
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
    
    // Enhanced question management
    private List<QuestionModel> firestoreQuestions;
    private List<EnhancedQuestionModel> enhancedQuestions;
    
    // Enhanced game systems
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
    private boolean isAudienceUsed = false;
    private boolean isPhoneUsed = false;
    private int lifelinesUsed = 0;
    private int totalLifelines = 5; // 50:50, Hint, Skip, Audience, Phone
    private Random random = new Random();
    private FirestoreQuestionRepository questionRepository;
    private boolean isDataLoaded = false;
    
    // Haptic feedback system
    private Vibrator vibrator;
    private boolean hapticFeedbackEnabled = true;
    
    // Local questions variable for backward compatibility
    private List<Question> questions;

    @Override
    protected void onResume() {
        super.onResume();
        
        // 🔄 CHECK FOR UPDATES: Verificăm dacă sunt actualizări în baza de date
        if (syncManager.isInternetAvailable() && isDataLoaded) {
            checkForQuestionUpdates();
        }
    }
    
    /**
     * 🔄 Verifică dacă există actualizări pentru întrebări în baza de date
     */
    private void checkForQuestionUpdates() {
        Log.d(TAG, "🔄 Verificăm actualizări pentru întrebări în baza de date");
        
        // Verificăm timestamp-ul ultimei actualizări din cache
        String cacheKey = "questions_cache_" + REGION + "_" + GAME_TYPE + "_timestamp";
        long lastCacheTime = getSharedPreferences("HybridStorage", MODE_PRIVATE).getLong(cacheKey, 0);
        long currentTime = System.currentTimeMillis();
        
        // Verificăm actualizări doar dacă au trecut mai mult de 30 de minute
        if (currentTime - lastCacheTime > 30 * 60 * 1000) { // 30 minute
            questionRepository.getQuestions(REGION, GAME_TYPE)
                .addOnSuccessListener(querySnapshot -> {
                    int onlineCount = querySnapshot.size();
                    int localCount = firestoreQuestions != null ? firestoreQuestions.size() : 0;
                    
                    if (onlineCount != localCount) {
                        Log.d(TAG, "🔄 Actualizări detectate: online=" + onlineCount + ", local=" + localCount);
                        showUpdateAvailableDialog();
                    } else {
                        // Actualizăm timestamp-ul pentru cache
                        getSharedPreferences("HybridStorage", MODE_PRIVATE)
                            .edit()
                            .putLong(cacheKey, currentTime)
                            .apply();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "🔄 Nu s-au putut verifica actualizările", e);
                });
        }
    }
    
    /**
     * 📢 Afișează dialog pentru actualizări disponibile
     */
    private void showUpdateAvailableDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("🆕 Actualizări Disponibile")
            .setMessage("Sunt disponibile întrebări noi în baza de date!\n\n" +
                       "Doriți să reîncărcați pentru a avea cele mai recente întrebări?")
            .setPositiveButton("🔄 Actualizează", (dialog, which) -> {
                Toast.makeText(this, "🔄 Reîncărcăm întrebările...", Toast.LENGTH_SHORT).show();
                loadQuestionsFromFirestore();
            })
            .setNegativeButton("📱 Mai târziu", null)
            .show();
    }

    private static class Question {
        String question;
        String[] answers;
        int correctAnswerIndex;
        int imageResourceId;
        String fact;

        Question(String question, String[] answers, int correctAnswerIndex, int imageResourceId, String fact) {
            this.question = question;
            this.answers = answers;
            this.correctAnswerIndex = correctAnswerIndex;
            this.imageResourceId = imageResourceId;
            this.fact = fact;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply dynamic colors if available
        DynamicColors.applyToActivityIfAvailable(this);
        
        // Set theme before super.onCreate
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oltenia_game);

        // Initialize enhanced systems
        initializeEnhancedSystems();

        initializeViews();
        pointsManager = PointsManager.getInstance(this);
        questionRepository = FirestoreQuestionRepository.getInstance();
        
        // Initialize game start time
        questionStartTime = System.currentTimeMillis();
        
        // Setup game mode and difficulty
        setupGameModeAndDifficulty();
        
        // Check user preference and load questions
        checkUserPreferenceAndLoad();
        
        setupLifelines();
    }

    /**
     * Initialize enhanced systems for Oltenia quiz
     */
    private void initializeEnhancedSystems() {
        difficultyManager = new DifficultyManager(this);
        gameModeManager = new GameModeManager(this);
        progressTracker = new PlayerProgressTracker(this);
        achievementManager = AchievementManager.getInstance(this);
        syncManager = SyncManager.getInstance(this);
        
        // Initialize haptic feedback system
        initializeHapticFeedback();
        
        // Set up achievement listener for notifications
        achievementManager.setAchievementUnlockedListener(achievement -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "🏆 Achievement Unlocked: " + achievement.getTitle(), 
                             Toast.LENGTH_LONG).show();
                // Provide haptic feedback for achievement
                provideHapticFeedback(HapticFeedbackType.ACHIEVEMENT);
            });
        });
        
        // Update daily play streak for Oltenia
        achievementManager.updateOlteniaDailyPlayStreak();
        
        // Sync local results with Firebase if possible
        syncLocalResultsWithFirebase();
        
        Log.d(TAG, "Enhanced systems initialized for Oltenia");
    }
    
    /**
     * Setup game mode and difficulty for Oltenia
     */
    private void setupGameModeAndDifficulty() {
        // Get game mode from intent or use default
        String gameMode = getIntent().getStringExtra("GAME_MODE");
        String focusCategory = getIntent().getStringExtra("FOCUS_CATEGORY");
        
        GameModeManager.GameMode mode = gameMode != null ? 
            GameModeManager.GameMode.valueOf(gameMode) : GameModeManager.GameMode.CLASSIC;
        
        EnhancedQuestionModel.Category category = focusCategory != null ?
            EnhancedQuestionModel.Category.valueOf(focusCategory) : null;
            
        // Initialize game mode
        gameModeManager.initializeGameMode(mode, category);
        
        // Update constants based on mode and difficulty
        DifficultyManager.DifficultyLevel difficulty = difficultyManager.getCurrentDifficulty();
        TIME_PER_QUESTION = Math.max(gameModeManager.getTimePerQuestion(), 
                                   difficulty.timePerQuestion);
        
        // Update score based on difficulty
        POINTS_PER_CORRECT_ANSWER = (int)(10 * difficulty.pointsMultiplier);
        
        Log.d(TAG, "Game mode: " + mode.displayName + 
               ", Difficulty: " + difficulty.displayName + 
               ", Time per question: " + TIME_PER_QUESTION + "ms");
    }
    
    /**
     * Check user preference and load questions accordingly
     */
    private void checkUserPreferenceAndLoad() {
        SharedPreferences prefs = getSharedPreferences("OlteniaGamePrefs", MODE_PRIVATE);
        String savedPreference = prefs.getString("data_source_preference", "ask_every_time");
        boolean hasInternet = syncManager.isInternetAvailable();
        boolean hasLocalCache = checkIfLocalCacheExists();
        
        Log.d(TAG, "Checking user preference: " + savedPreference + 
              ", Internet: " + hasInternet + ", Cache: " + hasLocalCache);
        
        switch (savedPreference) {
            case "always_database":
                if (hasInternet) {
                    Toast.makeText(this, "🌐 Loading from database (saved preference)...", Toast.LENGTH_SHORT).show();
                    loadQuestionsFromFirestore();
                } else {
                    // Nu există internet, întrebăm ce să facă
                    showNoInternetForPreferredDatabaseDialog();
                }
                break;
                
            case "always_cache":
                if (hasLocalCache) {
                    Toast.makeText(this, "📱 Loading from local cache (saved preference)...", Toast.LENGTH_SHORT).show();
                    loadQuestionsFromLocalCache();
                } else {
                    // Nu există cache, întrebăm ce să facă
                    showNoCacheForPreferredLocalDialog();
                }
                break;
                
            case "auto":
                Toast.makeText(this, "🎯 Auto selection (saved preference)...", Toast.LENGTH_SHORT).show();
                loadQuestionsFromFirestore();
                break;
                
            case "ask_every_time":
            default:
                showDataSourceSelectionDialog();
                break;
        }
    }
    
    /**
     * Check if local cache exists for Oltenia questions
     */
    private boolean checkIfLocalCacheExists() {
        String cacheKey = "questions_cache_" + REGION + "_" + GAME_TYPE;
        String cachedJson = getSharedPreferences("HybridStorage", MODE_PRIVATE).getString(cacheKey, null);
        
        if (cachedJson != null && !cachedJson.isEmpty()) {
            try {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                com.google.gson.reflect.TypeToken<Map<String, Object>> typeToken = 
                    new com.google.gson.reflect.TypeToken<Map<String, Object>>(){};
                Map<String, Object> cacheData = gson.fromJson(cachedJson, typeToken.getType());
                
                if (cacheData != null && cacheData.containsKey("questions")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> questionMaps = (List<Map<String, Object>>) cacheData.get("questions");
                    boolean hasQuestions = questionMaps != null && !questionMaps.isEmpty();
                    
                    if (hasQuestions) {
                        long timestamp = cacheData.containsKey("timestamp") ? 
                            ((Number) cacheData.get("timestamp")).longValue() : 0;
                        long ageInHours = (System.currentTimeMillis() - timestamp) / (1000 * 60 * 60);
                        
                        Log.d(TAG, "Local cache found: " + questionMaps.size() + 
                              " questions, age: " + ageInHours + " hours");
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking local cache", e);
            }
        }
        
        Log.d(TAG, "No valid local cache found");
        return false;
    }

    public void goBack(View view) {
        onBackPressed();
    }

    private void initializeViews() {
        // Initialize main UI components
        questionTextView = findViewById(R.id.questionTextView);
        answerButtons = new MaterialButton[4];
        answerButtons[0] = findViewById(R.id.answerButton1);
        answerButtons[1] = findViewById(R.id.answerButton2);
        answerButtons[2] = findViewById(R.id.answerButton3);
        answerButtons[3] = findViewById(R.id.answerButton4);
        scoreTextView = findViewById(R.id.scoreTextView);
        progressBar = findViewById(R.id.progressBar);
        timerTextView = findViewById(R.id.timerTextView);
        streakTextView = findViewById(R.id.streakTextView);
        questionImage = findViewById(R.id.questionImage);
        
        // Initialize new MaterialCardView lifelines
        fiftyFiftyCard = findViewById(R.id.fiftyFiftyCard);
        hintCard = findViewById(R.id.hintCard);
        skipQuestionCard = findViewById(R.id.skipQuestionCard);
        audienceCard = findViewById(R.id.audienceCard);
        phoneCard = findViewById(R.id.phoneCard);
        
        finishButton = findViewById(R.id.finishButton);
        
        // Apply modern styling to answer buttons
        for (int i = 0; i < answerButtons.length; i++) {
            final int index = i;
            MaterialButton button = answerButtons[i];
            
            // Set modern styling
            button.setCornerRadius(16);
            button.setElevation(4);
            button.setStrokeWidth(2);
            button.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.oltenia_accent)));
            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.oltenia_primary)));
            button.setTextColor(getResources().getColor(R.color.oltenia_card_bg));
            
            // Set click listener
            button.setOnClickListener(v -> {
                if (timer != null) {
                    provideHapticFeedback(HapticFeedbackType.BUTTON_CLICK);
                    checkAnswer(index, button.getText().toString());
                }
            });
        }
        
        // Set up finish button with modern styling
        finishButton.setCornerRadius(24);
        finishButton.setElevation(8);
        finishButton.setStrokeWidth(2);
        finishButton.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.oltenia_primary)));
        finishButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.oltenia_accent)));
        finishButton.setTextColor(getResources().getColor(R.color.oltenia_primary));
        finishButton.setOnClickListener(v -> {
            provideHapticFeedback(HapticFeedbackType.BUTTON_CLICK);
            finishGame();
        });
        
        // Apply modern styling to progress bar
        progressBar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.oltenia_accent)));
        progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.oltenia_secondary)));
        
        // Initialize haptic feedback
        initializeHapticFeedback();
        
        // Apply modern styling to lifeline cards
        applyLifelineCardStyling();
        
        pointsManager = PointsManager.getInstance(this);
        
        // Initialize questions and display first question
        initializeQuestions();
        if (questions.size() > 0) {
            progressBar.setMax(questions.size());
            displayQuestion();
        }
    }

    /**
     * 🎨 Aplică stilizare modernă pentru cardurile de ajutoare
     */
    private void applyLifelineCardStyling() {
        MaterialCardView[] lifelineCards = {fiftyFiftyCard, hintCard, skipQuestionCard, audienceCard, phoneCard};
        
        for (MaterialCardView card : lifelineCards) {
            card.setRadius(12);
            card.setCardElevation(2);
            card.setStrokeWidth(1);
            card.setStrokeColor(getResources().getColor(R.color.oltenia_accent));
            card.setCardBackgroundColor(getResources().getColor(R.color.oltenia_primary));
        }
    }

    /**
     * 🎯 Setup sistemul avansat de lifeline-uri cu MaterialCardView
     */
    private void setupLifelines() {
        // 50:50 Lifeline
        fiftyFiftyCard.setOnClickListener(v -> {
            provideHapticFeedback(HapticFeedbackType.LIFELINE);
            useFiftyFifty();
        });
        
        // Hint Lifeline
        hintCard.setOnClickListener(v -> {
            provideHapticFeedback(HapticFeedbackType.LIFELINE);
            useHint();
        });
        
        // Skip Question Lifeline
        skipQuestionCard.setOnClickListener(v -> {
            provideHapticFeedback(HapticFeedbackType.LIFELINE);
            skipQuestion();
        });
        
        // Audience Help Lifeline
        audienceCard.setOnClickListener(v -> {
            provideHapticFeedback(HapticFeedbackType.LIFELINE);
            useAudienceHelp();
        });
        
        // Phone a Friend Lifeline
        phoneCard.setOnClickListener(v -> {
            provideHapticFeedback(HapticFeedbackType.LIFELINE);
            usePhoneAFriend();
        });
        
        // Inițializăm stilurile lifeline-urilor
        updateLifelinesAvailability();
    }

    /**
     * 🎯 Lifeline 50:50 - elimină două răspunsuri greșite
     */
    private void useFiftyFifty() {
        if (isFiftyFiftyUsed) {
            showLifelineAlreadyUsedDialog("50:50");
            return;
        }

        // Feedback haptic
        provideHapticFeedback(HapticFeedbackType.LIFELINE);

        Question currentQuestion = questions.get(currentQuestionIndex);
        List<Integer> wrongAnswers = new ArrayList<>();
        for (int i = 0; i < currentQuestion.answers.length; i++) {
            if (i != currentQuestion.correctAnswerIndex) {
                wrongAnswers.add(i);
            }
        }
        Collections.shuffle(wrongAnswers);
        
        // Animația de eliminare a răspunsurilor greșite
        for (int i = 0; i < 2; i++) {
            int index = wrongAnswers.get(i);
            animateLifelineEffect(answerButtons[index], false);
        }

        isFiftyFiftyUsed = true;
        lifelinesUsed++;
        updateLifelinesAvailability();
        
        // Feedback vizual
        Toast.makeText(this, "🎯 50:50 folosit! Două răspunsuri eliminate.", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 💡 Lifeline Hint - oferă un indiciu despre răspuns
     */
    private void useHint() {
        if (isHintUsed) {
            showLifelineAlreadyUsedDialog("Hint");
            return;
        }

        // Feedback haptic
        provideHapticFeedback(HapticFeedbackType.LIFELINE);

        Question currentQuestion = questions.get(currentQuestionIndex);
        
        // Generăm un indiciu bazat pe întrebare
        String hint = generateHint(currentQuestion);
        
        // Afișăm indiciul într-un dialog elegant
        showHintDialog(hint);

        isHintUsed = true;
        lifelinesUsed++;
        updateLifelinesAvailability();
    }
    
    /**
     * ⏭️ Lifeline Skip - sare peste întrebarea curentă
     */
    private void skipQuestion() {
        if (isSkipUsed) {
            showLifelineAlreadyUsedDialog("Skip");
            return;
        }

        // Feedback haptic
        provideHapticFeedback(HapticFeedbackType.LIFELINE);

        // Animația de skip
        animateSkipQuestion();
        
        // Trecem la următoarea întrebare
        moveToNextQuestion();
        
        isSkipUsed = true;
        lifelinesUsed++;
        updateLifelinesAvailability();
        
        // Feedback vizual
        Toast.makeText(this, "⏭️ Întrebare sărită!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 👥 Lifeline Audience Help - simulează ajutorul publicului
     */
    private void useAudienceHelp() {
        if (isAudienceUsed) {
            showLifelineAlreadyUsedDialog("Ajutorul publicului");
            return;
        }

        // Feedback haptic
        provideHapticFeedback(HapticFeedbackType.LIFELINE);

        Question currentQuestion = questions.get(currentQuestionIndex);
        
        // Simulăm voturile publicului
        int[] audienceVotes = simulateAudienceVotes(currentQuestion);
        
        // Afișăm rezultatele într-un dialog interactiv
        showAudienceResultsDialog(audienceVotes);

        isAudienceUsed = true;
        lifelinesUsed++;
        updateLifelinesAvailability();
    }
    
    /**
     * 📞 Lifeline Phone a Friend - simulează apelul către prieten
     */
    private void usePhoneAFriend() {
        if (isPhoneUsed) {
            showLifelineAlreadyUsedDialog("Apel către prieten");
            return;
        }

        // Feedback haptic
        provideHapticFeedback(HapticFeedbackType.LIFELINE);

        Question currentQuestion = questions.get(currentQuestionIndex);
        
        // Simulăm răspunsul prietenului
        String friendAnswer = simulateFriendAnswer(currentQuestion);
        
        // Afișăm răspunsul într-un dialog dramatic
        showPhoneCallDialog(friendAnswer);

        isPhoneUsed = true;
        lifelinesUsed++;
        updateLifelinesAvailability();
    }

    /**
     * 🎯 Actualizează disponibilitatea lifeline-urilor
     */
    private void updateLifelinesAvailability() {
        // 50:50
        fiftyFiftyCard.setEnabled(!isFiftyFiftyUsed);
        fiftyFiftyCard.setAlpha(isFiftyFiftyUsed ? 0.5f : 1.0f);
        
        // Hint
        hintCard.setEnabled(!isHintUsed);
        hintCard.setAlpha(isHintUsed ? 0.5f : 1.0f);
        
        // Skip
        skipQuestionCard.setEnabled(!isSkipUsed);
        skipQuestionCard.setAlpha(isSkipUsed ? 0.5f : 1.0f);
        
        // Audience
        audienceCard.setEnabled(!isAudienceUsed);
        audienceCard.setAlpha(isAudienceUsed ? 0.5f : 1.0f);
        
        // Phone
        phoneCard.setEnabled(!isPhoneUsed);
        phoneCard.setAlpha(isPhoneUsed ? 0.5f : 1.0f);
        
        // Actualizăm contorul de lifeline-uri
        updateLifelineCounter();
    }
    
    /**
     * 📊 Actualizează contorul de lifeline-uri
     */
    private void updateLifelineCounter() {
        int remainingLifelines = totalLifelines - lifelinesUsed;
        // Aici putem afișa un indicator vizual pentru lifeline-urile rămase
        Log.d(TAG, "📊 Lifeline-uri rămase: " + remainingLifelines + "/" + totalLifelines);
    }
    
    /**
     * 🔥 Sistem comprehensiv de salvare a rezultatelor în Firebase
     */
    private void saveQuizResultToFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "🔥 Utilizatorul nu este autentificat - salvăm local");
            saveQuizResultToHybridStorage();
            return;
        }
        
        try {
            // Creăm obiectul de rezultat
            QuizResult quizResult = createQuizResult(currentUser.getUid());
            
            // Salvăm în Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            
            // Salvăm în colecția quiz_results
            db.collection("quiz_results")
                .add(quizResult)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "🔥 ✅ Rezultat salvat în Firebase cu ID: " + documentReference.getId());
                    
                    // Salvăm și în istoricul utilizatorului
                    saveToUserActivityHistory(quizResult);
                    
                    // Actualizăm statisticile globale
                    updateGlobalStatistics(quizResult);
                    
                    // Actualizăm leaderboard-ul
                    updateLeaderboard(quizResult);
                    
                    // Salvăm și local pentru backup
                    saveQuizResultToHybridStorage();
                    
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "🔥 ❌ Eroare la salvarea în Firebase", e);
                    // Fallback la salvarea locală
                    saveQuizResultToHybridStorage();
                });
                
        } catch (Exception e) {
            Log.e(TAG, "🔥 ❌ Eroare la crearea rezultatului", e);
            saveQuizResultToHybridStorage();
        }
    }
    
    /**
     * 📊 Creează obiectul QuizResult pentru Firebase
     */
    private QuizResult createQuizResult(String userId) {
        QuizResult result = new QuizResult();
        result.setUserId(userId);
        result.setRegion(REGION);
        result.setGameType(GAME_TYPE);
        result.setScore(score);
        result.setCorrectAnswers(correctAnswers);
        result.setTotalQuestions(totalQuestions);
        result.setMaxStreak(maxStreak);
        result.setTotalTime(totalTime);
        result.setLifelinesUsed(lifelinesUsed);
        result.setTimestamp(new Date());
        result.setDifficulty(difficultyManager.getCurrentDifficulty().name());
        result.setGameMode(gameModeManager.getCurrentGameMode().name());
        
        // Adăugăm statistici detaliate
        Map<String, Object> detailedStats = new HashMap<>();
        detailedStats.put("averageTimePerQuestion", totalTime / (double) totalQuestions);
        detailedStats.put("accuracy", (double) correctAnswers / totalQuestions * 100);
        detailedStats.put("efficiency", (double) score / totalTime * 1000); // puncte per secundă
        detailedStats.put("lifelineEfficiency", (double) (totalQuestions - lifelinesUsed) / totalQuestions * 100);
        
        result.setDetailedStats(detailedStats);
        
        return result;
    }
    
    /**
     * 📚 Salvează în istoricul activității utilizatorului
     */
    private void saveToUserActivityHistory(QuizResult quizResult) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> activityData = new HashMap<>();
        activityData.put("quizResultId", quizResult.getId());
        activityData.put("region", quizResult.getRegion());
        activityData.put("score", quizResult.getScore());
        activityData.put("timestamp", quizResult.getTimestamp());
        activityData.put("type", "quiz_completed");
        
        db.collection("users")
            .document(quizResult.getUserId())
            .collection("activity_history")
            .add(activityData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "📚 ✅ Activitate salvată în istoric");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "📚 ❌ Eroare la salvarea în istoric", e);
            });
    }
    
    /**
     * 📊 Actualizează statisticile globale
     */
    private void updateGlobalStatistics(QuizResult quizResult) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Actualizăm statisticile pentru regiune
        db.collection("statistics")
            .document("regions")
            .collection(quizResult.getRegion())
            .document("quiz_stats")
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                Map<String, Object> stats = new HashMap<>();
                
                if (documentSnapshot.exists()) {
                    stats = documentSnapshot.getData();
                }
                
                // Actualizăm contoarele
                long totalGames = documentSnapshot.getLong("totalGames") != null ? 
                    documentSnapshot.getLong("totalGames") : 0;
                long totalScore = documentSnapshot.getLong("totalScore") != null ? 
                    documentSnapshot.getLong("totalScore") : 0;
                long totalCorrectAnswers = documentSnapshot.getLong("totalCorrectAnswers") != null ? 
                    documentSnapshot.getLong("totalCorrectAnswers") : 0;
                
                stats.put("totalGames", totalGames + 1);
                stats.put("totalScore", totalScore + quizResult.getScore());
                stats.put("totalCorrectAnswers", totalCorrectAnswers + quizResult.getCorrectAnswers());
                stats.put("averageScore", (totalScore + quizResult.getScore()) / (double) (totalGames + 1));
                stats.put("averageAccuracy", (totalCorrectAnswers + quizResult.getCorrectAnswers()) / 
                    (double) (totalGames * quizResult.getTotalQuestions() + quizResult.getTotalQuestions()) * 100);
                stats.put("lastUpdated", new Date());
                
                // Salvăm statisticile actualizate
                db.collection("statistics")
                    .document("regions")
                    .collection(quizResult.getRegion())
                    .document("quiz_stats")
                    .set(stats)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "📊 ✅ Statistici globale actualizate");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "📊 ❌ Eroare la actualizarea statisticilor", e);
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "📊 ❌ Eroare la citirea statisticilor", e);
            });
    }
    
    /**
     * 💾 Sistem hibrid de stocare pentru rezultate
     */
    private void saveQuizResultToHybridStorage() {
        try {
            // Creăm datele pentru stocare hibridă
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("region", REGION);
            resultData.put("gameType", GAME_TYPE);
            resultData.put("score", score);
            resultData.put("correctAnswers", correctAnswers);
            resultData.put("totalQuestions", totalQuestions);
            resultData.put("maxStreak", maxStreak);
            resultData.put("totalTime", totalTime);
            resultData.put("lifelinesUsed", lifelinesUsed);
            resultData.put("timestamp", System.currentTimeMillis());
            resultData.put("difficulty", difficultyManager.getCurrentDifficulty().name());
            resultData.put("gameMode", gameModeManager.getCurrentGameMode().name());
            
            // Salvăm în sistemul hibrid
            String resultKey = "quiz_result_" + REGION + "_" + System.currentTimeMillis();
            syncManager.saveData("quiz_results", resultKey, resultData, new SyncManager.SyncCallback() {
                @Override
                public void onSyncComplete(boolean success, String message) {
                    if (success) {
                        Log.d(TAG, "💾 ✅ Rezultat salvat în stocarea hibridă");
                        
                        // Actualizăm statisticile locale
                        updateLocalStatistics(resultData);
                        
                        // Sincronizăm cu Firebase dacă este disponibil
                        if (syncManager.isInternetAvailable()) {
                            saveQuizResultToFirebase();
                        }
                    } else {
                        Log.w(TAG, "💾 ⚠️ Eroare la salvarea în stocarea hibridă: " + message);
                        
                        // Fallback la SharedPreferences
                        saveToSharedPreferences(resultData);
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "💾 ❌ Eroare la salvarea în stocarea hibridă", e);
            saveToSharedPreferences(createBasicResultData());
        }
    }
    
    /**
     * 📊 Actualizează statisticile locale
     */
    private void updateLocalStatistics(Map<String, Object> resultData) {
        SharedPreferences prefs = getSharedPreferences("LocalStats", MODE_PRIVATE);
        
        // Statistici generale
        int totalGames = prefs.getInt("total_games", 0) + 1;
        int totalScore = prefs.getInt("total_score", 0) + (int) resultData.get("score");
        int totalCorrectAnswers = prefs.getInt("total_correct_answers", 0) + (int) resultData.get("correctAnswers");
        int totalQuestions = prefs.getInt("total_questions", 0) + (int) resultData.get("totalQuestions");
        
        // Statistici pentru regiune
        String regionKey = "region_" + REGION;
        int regionGames = prefs.getInt(regionKey + "_games", 0) + 1;
        int regionScore = prefs.getInt(regionKey + "_score", 0) + (int) resultData.get("score");
        int regionBestScore = prefs.getInt(regionKey + "_best_score", 0);
        
        if ((int) resultData.get("score") > regionBestScore) {
            regionBestScore = (int) resultData.get("score");
        }
        
        // Salvăm statisticile
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("total_games", totalGames);
        editor.putInt("total_score", totalScore);
        editor.putInt("total_correct_answers", totalCorrectAnswers);
        editor.putInt("total_questions", totalQuestions);
        editor.putFloat("average_score", (float) totalScore / totalGames);
        editor.putFloat("average_accuracy", (float) totalCorrectAnswers / totalQuestions * 100);
        
        // Statistici pentru regiune
        editor.putInt(regionKey + "_games", regionGames);
        editor.putInt(regionKey + "_score", regionScore);
        editor.putInt(regionKey + "_best_score", regionBestScore);
        editor.putFloat(regionKey + "_average_score", (float) regionScore / regionGames);
        
        // Ultima actualizare
        editor.putLong("last_stats_update", System.currentTimeMillis());
        
        editor.apply();
        
        Log.d(TAG, "📊 ✅ Statistici locale actualizate");
    }
    
    /**
     * 💾 Salvează în SharedPreferences ca fallback
     */
    private void saveToSharedPreferences(Map<String, Object> resultData) {
        SharedPreferences prefs = getSharedPreferences("QuizResults", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        String key = "result_" + System.currentTimeMillis();
        editor.putString(key + "_region", (String) resultData.get("region"));
        editor.putString(key + "_gameType", (String) resultData.get("gameType"));
        editor.putInt(key + "_score", (int) resultData.get("score"));
        editor.putInt(key + "_correctAnswers", (int) resultData.get("correctAnswers"));
        editor.putInt(key + "_totalQuestions", (int) resultData.get("totalQuestions"));
        editor.putInt(key + "_maxStreak", (int) resultData.get("maxStreak"));
        editor.putLong(key + "_totalTime", (long) resultData.get("totalTime"));
        editor.putInt(key + "_lifelinesUsed", (int) resultData.get("lifelinesUsed"));
        editor.putLong(key + "_timestamp", (long) resultData.get("timestamp"));
        editor.putString(key + "_difficulty", (String) resultData.get("difficulty"));
        editor.putString(key + "_gameMode", (String) resultData.get("gameMode"));
        
        editor.apply();
        
        Log.d(TAG, "💾 ✅ Rezultat salvat în SharedPreferences");
    }
    
    /**
     * 📊 Creează datele de bază pentru rezultat
     */
    private Map<String, Object> createBasicResultData() {
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("region", REGION);
        resultData.put("gameType", GAME_TYPE);
        resultData.put("score", score);
        resultData.put("correctAnswers", correctAnswers);
        resultData.put("totalQuestions", totalQuestions);
        resultData.put("maxStreak", maxStreak);
        resultData.put("totalTime", totalTime);
        resultData.put("lifelinesUsed", lifelinesUsed);
        resultData.put("timestamp", System.currentTimeMillis());
        resultData.put("difficulty", "UNKNOWN");
        resultData.put("gameMode", "UNKNOWN");
        return resultData;
    }
    
    /**
     * 🔄 Sincronizează rezultatele locale cu Firebase
     */
    private void syncLocalResultsWithFirebase() {
        if (!syncManager.isInternetAvailable()) {
            Log.w(TAG, "🔄 Nu există conexiune la internet pentru sincronizare");
            return;
        }
        
        SharedPreferences prefs = getSharedPreferences("QuizResults", MODE_PRIVATE);
        Map<String, ?> allResults = prefs.getAll();
        
        for (String key : allResults.keySet()) {
            if (key.startsWith("result_") && key.endsWith("_region")) {
                String resultId = key.replace("_region", "");
                String region = prefs.getString(key, "");
                
                if (REGION.equals(region)) {
                    // Creăm obiectul QuizResult din datele locale
                    QuizResult localResult = createQuizResultFromPreferences(prefs, resultId);
                    
                    // Încercăm să-l salvăm în Firebase
                    saveSingleResultToFirebase(localResult);
                    
                    // Ștergem din SharedPreferences după sincronizare
                    removeResultFromPreferences(prefs, resultId);
                }
            }
        }
    }
    
    /**
     * 📊 Creează QuizResult din SharedPreferences
     */
    private QuizResult createQuizResultFromPreferences(SharedPreferences prefs, String resultId) {
        QuizResult result = new QuizResult();
        result.setUserId("local_user"); // Pentru rezultatele locale
        result.setRegion(prefs.getString(resultId + "_region", ""));
        result.setGameType(prefs.getString(resultId + "_gameType", ""));
        result.setScore(prefs.getInt(resultId + "_score", 0));
        result.setCorrectAnswers(prefs.getInt(resultId + "_correctAnswers", 0));
        result.setTotalQuestions(prefs.getInt(resultId + "_totalQuestions", 0));
        result.setMaxStreak(prefs.getInt(resultId + "_maxStreak", 0));
        result.setTotalTime(prefs.getLong(resultId + "_totalTime", 0));
        result.setLifelinesUsed(prefs.getInt(resultId + "_lifelinesUsed", 0));
        result.setTimestamp(new Date(prefs.getLong(resultId + "_timestamp", 0)));
        result.setDifficulty(prefs.getString(resultId + "_difficulty", "UNKNOWN"));
        result.setGameMode(prefs.getString(resultId + "_gameMode", "UNKNOWN"));
        
        return result;
    }
    
    /**
     * 🔥 Salvează un singur rezultat în Firebase
     */
    private void saveSingleResultToFirebase(QuizResult result) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("userId", result.getUserId());
        resultData.put("region", result.getRegion());
        resultData.put("gameType", result.getGameType());
        resultData.put("score", result.getScore());
        resultData.put("correctAnswers", result.getCorrectAnswers());
        resultData.put("totalQuestions", result.getTotalQuestions());
        resultData.put("maxStreak", result.getMaxStreak());
        resultData.put("totalTime", result.getTotalTime());
        resultData.put("lifelinesUsed", result.getLifelinesUsed());
        resultData.put("timestamp", result.getTimestamp());
        resultData.put("difficulty", result.getDifficulty());
        resultData.put("gameMode", result.getGameMode());
        resultData.put("syncedFromLocal", true);
        
        db.collection("quiz_results")
            .add(resultData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "🔄 ✅ Rezultat local sincronizat cu Firebase");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "🔄 ❌ Eroare la sincronizarea rezultatului local", e);
            });
    }
    
    /**
     * 🗑️ Șterge rezultatul din SharedPreferences după sincronizare
     */
    private void removeResultFromPreferences(SharedPreferences prefs, String resultId) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(resultId + "_region");
        editor.remove(resultId + "_gameType");
        editor.remove(resultId + "_score");
        editor.remove(resultId + "_correctAnswers");
        editor.remove(resultId + "_totalQuestions");
        editor.remove(resultId + "_maxStreak");
        editor.remove(resultId + "_totalTime");
        editor.remove(resultId + "_lifelinesUsed");
        editor.remove(resultId + "_timestamp");
        editor.remove(resultId + "_difficulty");
        editor.remove(resultId + "_gameMode");
        editor.apply();
    }
    
    /**
     * 🏆 Actualizează leaderboard-ul
     */
    private void updateLeaderboard(QuizResult quizResult) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Verificăm dacă scorul este suficient de bun pentru leaderboard
        db.collection("leaderboards")
            .document(quizResult.getRegion())
            .collection("top_scores")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                boolean shouldAddToLeaderboard = true;
                
                if (!querySnapshot.isEmpty()) {
                    // Verificăm dacă scorul este în top 100
                    long lowestScore = querySnapshot.getDocuments()
                        .get(querySnapshot.size() - 1)
                        .getLong("score");
                    
                    if (quizResult.getScore() < lowestScore && querySnapshot.size() >= 100) {
                        shouldAddToLeaderboard = false;
                    }
                }
                
                if (shouldAddToLeaderboard) {
                    // Adăugăm în leaderboard
                    Map<String, Object> leaderboardEntry = new HashMap<>();
                    leaderboardEntry.put("userId", quizResult.getUserId());
                    leaderboardEntry.put("score", quizResult.getScore());
                    leaderboardEntry.put("correctAnswers", quizResult.getCorrectAnswers());
                    leaderboardEntry.put("totalQuestions", quizResult.getTotalQuestions());
                    leaderboardEntry.put("timestamp", quizResult.getTimestamp());
                    leaderboardEntry.put("difficulty", quizResult.getDifficulty());
                    leaderboardEntry.put("gameMode", quizResult.getGameMode());
                    
                    db.collection("leaderboards")
                        .document(quizResult.getRegion())
                        .collection("top_scores")
                        .add(leaderboardEntry)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "🏆 ✅ Scor adăugat în leaderboard");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "🏆 ❌ Eroare la adăugarea în leaderboard", e);
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "🏆 ❌ Eroare la verificarea leaderboard-ului", e);
            });
    }
    
    /**
     * 🎯 Resetează lifeline-urile pentru întrebarea următoare
     */
    private void resetLifelinesForNewQuestion() {
        // Resetăm doar lifeline-urile care se pot folosi din nou
        // 50:50 și Skip se resetează, Hint, Audience și Phone rămân folosite
        isFiftyFiftyUsed = false;
        isSkipUsed = false;
        
        updateLifelinesAvailability();
    }
    
    /**
     * 🎯 Animația pentru efectul de lifeline
     */
    private void animateLifelineEffect(MaterialButton button, boolean isCorrect) {
        // Dezactivăm butonul
        button.setEnabled(false);
        
        // Animația de fade out
        ValueAnimator fadeAnimator = ValueAnimator.ofFloat(1.0f, 0.3f);
        fadeAnimator.setDuration(500);
        fadeAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            button.setAlpha(value);
        });
        
        // Animația de scalare
        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(1.0f, 0.8f);
        scaleAnimator.setDuration(300);
        scaleAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            button.setScaleX(value);
            button.setScaleY(value);
        });
        
        fadeAnimator.start();
        scaleAnimator.start();
    }
    
    /**
     * ⏭️ Animația pentru skip question
     */
    private void animateSkipQuestion() {
        // Animația de fade out pentru întrebarea curentă
        ValueAnimator fadeOut = ValueAnimator.ofFloat(1.0f, 0.0f);
        fadeOut.setDuration(300);
        fadeOut.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            questionTextView.setAlpha(value);
            for (MaterialButton button : answerButtons) {
                button.setAlpha(value);
            }
        });
        
        fadeOut.start();
    }
    
    /**
     * 💡 Generează un indiciu pentru întrebare
     */
    private String generateHint(Question question) {
        String questionText = question.question.toLowerCase();
        
        if (questionText.contains("oraș") || questionText.contains("craiova")) {
            return "💡 Gândește-te la cel mai mare oraș din regiune...";
        } else if (questionText.contains("munte") || questionText.contains("vârf")) {
            return "💡 Caută cel mai înalt vârf din Munții Parâng...";
        } else if (questionText.contains("râu") || questionText.contains("jiu")) {
            return "💡 Acest râu străbate regiunea de la nord la sud...";
        } else if (questionText.contains("mănăstire") || questionText.contains("tismana")) {
            return "💡 Una dintre cele mai vechi mănăstiri din România...";
        } else if (questionText.contains("sculptor") || questionText.contains("brâncuși")) {
            return "💡 Cel mai cunoscut sculptor român din secolul XX...";
        } else {
            return "💡 Gândește-te la caracteristicile specifice Olteniei...";
        }
    }
    
    /**
     * 💡 Afișează dialogul cu indiciul
     */
    private void showHintDialog(String hint) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("💡 Indiciu")
            .setMessage(hint)
            .setPositiveButton("✅ Am înțeles", null)
            .setIcon(R.drawable.ic_hint)
            .show();
    }
    
    /**
     * 👥 Simulează voturile publicului
     */
    private int[] simulateAudienceVotes(Question question) {
        int[] votes = new int[4];
        int correctIndex = question.correctAnswerIndex;
        
        // Publicul votează mai mult pentru răspunsul corect
        votes[correctIndex] = random.nextInt(30) + 40; // 40-70% pentru răspunsul corect
        
        // Distribuim restul voturilor
        int remainingVotes = 100 - votes[correctIndex];
        for (int i = 0; i < 4; i++) {
            if (i != correctIndex) {
                votes[i] = remainingVotes / 3 + random.nextInt(10);
                remainingVotes -= votes[i];
            }
        }
        
        return votes;
    }
    
    /**
     * 👥 Afișează rezultatele publicului
     */
    private void showAudienceResultsDialog(int[] votes) {
        String message = "👥 Rezultatele votului publicului:\n\n";
        String[] options = {"A", "B", "C", "D"};
        
        for (int i = 0; i < 4; i++) {
            message += options[i] + ": " + votes[i] + "%\n";
        }
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("👥 Ajutorul publicului")
            .setMessage(message)
            .setPositiveButton("✅ Am înțeles", null)
            .setIcon(R.drawable.ic_audience)
            .show();
    }
    
    /**
     * 📞 Simulează răspunsul prietenului
     */
    private String simulateFriendAnswer(Question question) {
        String[] responses = {
            "Sunt destul de sigur că răspunsul este corect!",
            "Cred că știu răspunsul, dar nu sunt 100% sigur...",
            "Hmm, este o întrebare dificilă...",
            "Sunt confuz, nu sunt sigur de răspuns...",
            "Cred că știu răspunsul!"
        };
        
        return responses[random.nextInt(responses.length)];
    }
    
    /**
     * 📞 Afișează dialogul cu apelul către prieten
     */
    private void showPhoneCallDialog(String friendAnswer) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("📞 Apel către prieten")
            .setMessage("Prietenul tău spune:\n\n\"" + friendAnswer + "\"")
            .setPositiveButton("✅ Mulțumesc", null)
            .setIcon(R.drawable.ic_phone)
            .show();
    }
    
    /**
     * 📳 Sistem de feedback haptic pentru interacțiunile utilizatorului
     */
    private enum HapticFeedbackType {
        CORRECT, WRONG, LIFELINE, BUTTON_CLICK, TIMER_WARNING, ACHIEVEMENT
    }
    
    /**
     * 📳 Inițializează sistemul de feedback haptic
     */
    private void initializeHapticFeedback() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        // Verificăm preferințele utilizatorului pentru feedback haptic
        SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);
        hapticFeedbackEnabled = prefs.getBoolean("haptic_feedback_enabled", true);
    }
    
    /**
     * 📳 Oferă feedback haptic pentru diferite tipuri de interacțiuni
     */
    private void provideHapticFeedback(HapticFeedbackType type) {
        if (!hapticFeedbackEnabled || vibrator == null) {
            return;
        }
        
        try {
            switch (type) {
                case CORRECT:
                    // Vibrație scurtă și plăcută pentru răspuns corect
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(100);
                    }
                    break;
                    
                case WRONG:
                    // Vibrație mai lungă și mai intensă pentru răspuns greșit
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(200);
                    }
                    break;
                    
                case LIFELINE:
                    // Vibrație specială pentru folosirea lifeline-urilor
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(
                            new long[]{0, 50, 100, 50}, 
                            new int[]{0, 128, 255, 128}, 
                            -1));
                    } else {
                        vibrator.vibrate(new long[]{0, 50, 100, 50}, -1);
                    }
                    break;
                    
                case BUTTON_CLICK:
                    // Vibrație foarte scurtă pentru click-uri pe butoane
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(50);
                    }
                    break;
                    
                case TIMER_WARNING:
                    // Vibrație de avertizare pentru timer
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(
                            new long[]{0, 100, 200, 100}, 
                            new int[]{0, 255, 0, 255}, 
                            -1));
                    } else {
                        vibrator.vibrate(new long[]{0, 100, 200, 100}, -1);
                    }
                    break;
                    
                case ACHIEVEMENT:
                    // Vibrație specială pentru achievement-uri
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(
                            new long[]{0, 100, 200, 100, 200, 100}, 
                            new int[]{0, 255, 0, 255, 0, 255}, 
                            -1));
                    } else {
                        vibrator.vibrate(new long[]{0, 100, 200, 100, 200, 100}, -1);
                    }
                    break;
            }
        } catch (Exception e) {
            Log.w(TAG, "📳 Eroare la feedback haptic: " + e.getMessage());
        }
    }
    
    /**
     * 🎨 Sistem de animații pentru răspunsuri corecte/greșite
     */
    private void animateCorrectAnswer(MaterialButton button) {
        // Feedback haptic
        provideHapticFeedback(HapticFeedbackType.CORRECT);
        
        // Animația de scalare pentru răspuns corect
        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(1.0f, 1.2f, 1.0f);
        scaleAnimator.setDuration(600);
        scaleAnimator.setInterpolator(new OvershootInterpolator());
        scaleAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            button.setScaleX(value);
            button.setScaleY(value);
        });
        
        // Animația de culoare pentru răspuns corect
        ValueAnimator colorAnimator = ValueAnimator.ofFloat(0f, 1f);
        colorAnimator.setDuration(300);
        colorAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            int color = interpolateColor(
                getResources().getColor(R.color.oltenia_primary),
                getResources().getColor(R.color.correct_answer),
                progress
            );
            button.setBackgroundTintList(ColorStateList.valueOf(color));
        });
        
        // Animația de rotație subtilă
        ValueAnimator rotationAnimator = ValueAnimator.ofFloat(0f, 5f, -5f, 0f);
        rotationAnimator.setDuration(600);
        rotationAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            button.setRotation(value);
        });
        
        // Pornim animațiile
        scaleAnimator.start();
        colorAnimator.start();
        rotationAnimator.start();
        
        // Efect de particule pentru răspuns corect
        showCorrectAnswerParticles(button);
    }
    
    /**
     * ❌ Animația pentru răspuns greșit
     */
    private void animateWrongAnswer(MaterialButton button) {
        // Feedback haptic
        provideHapticFeedback(HapticFeedbackType.WRONG);
        
        // Animația de shake pentru răspuns greșit
        ValueAnimator shakeAnimator = ValueAnimator.ofFloat(0f, -10f, 10f, -10f, 10f, -5f, 5f, 0f);
        shakeAnimator.setDuration(500);
        shakeAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            button.setTranslationX(value);
        });
        
        // Animația de culoare pentru răspuns greșit
        ValueAnimator colorAnimator = ValueAnimator.ofFloat(0f, 1f);
        colorAnimator.setDuration(300);
        colorAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            int color = interpolateColor(
                getResources().getColor(R.color.oltenia_primary),
                getResources().getColor(R.color.wrong_answer),
                progress
            );
            button.setBackgroundTintList(ColorStateList.valueOf(color));
        });
        
        // Animația de scalare pentru răspuns greșit
        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(1.0f, 0.95f, 1.0f);
        scaleAnimator.setDuration(500);
        scaleAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            button.setScaleX(value);
            button.setScaleY(value);
        });
        
        // Pornim animațiile
        shakeAnimator.start();
        colorAnimator.start();
        scaleAnimator.start();
    }
    
    /**
     * 🎨 Interpolează între două culori
     */
    private int interpolateColor(int color1, int color2, float ratio) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        
        return (r << 16) | (g << 8) | b;
    }
    
    /**
     * ✨ Afișează efecte de particule pentru răspuns corect
     */
    private void showCorrectAnswerParticles(MaterialButton button) {
        // Simulăm efecte de particule cu animații simple
        for (int i = 0; i < 5; i++) {
            final int particleIndex = i; // Make it effectively final
            View particle = new View(this);
            particle.setBackgroundColor(getResources().getColor(R.color.correct_answer));
            particle.setLayoutParams(new ConstraintLayout.LayoutParams(8, 8));
            
            // Adăugăm particula la layout
            ConstraintLayout parent = (ConstraintLayout) button.getParent();
            parent.addView(particle);
            
            // Poziționăm particula în jurul butonului
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) particle.getLayoutParams();
            params.leftMargin = (int) (button.getX() + button.getWidth() / 2);
            params.topMargin = (int) (button.getY() + button.getHeight() / 2);
            particle.setLayoutParams(params);
            
            // Animația particulei
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(1000);
            animator.setStartDelay(particleIndex * 100);
            animator.addUpdateListener(animation -> {
                float progress = (float) animation.getAnimatedValue();
                float angle = (float) (particleIndex * 72 * Math.PI / 180); // 72 grade între particule
                float distance = 100 * progress;
                
                float x = (float) (Math.cos(angle) * distance);
                float y = (float) (Math.sin(angle) * distance);
                
                particle.setTranslationX(x);
                particle.setTranslationY(y);
                particle.setAlpha(1f - progress);
                particle.setScaleX(1f - progress * 0.5f);
                particle.setScaleY(1f - progress * 0.5f);
            });
            
            animator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    parent.removeView(particle);
                }
            });
            
            animator.start();
        }
    }
    
    /**
     * ⚙️ Activează/dezactivează feedback-ul haptic
     */
    private void toggleHapticFeedback() {
        hapticFeedbackEnabled = !hapticFeedbackEnabled;
        
        // Salvăm preferința
        SharedPreferences prefs = getSharedPreferences("GameSettings", MODE_PRIVATE);
        prefs.edit().putBoolean("haptic_feedback_enabled", hapticFeedbackEnabled).apply();
        
        // Feedback vizual
        String message = hapticFeedbackEnabled ? "📳 Feedback haptic activat" : "📳 Feedback haptic dezactivat";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        
        // Feedback haptic pentru confirmare
        if (hapticFeedbackEnabled) {
            provideHapticFeedback(HapticFeedbackType.BUTTON_CLICK);
        }
    }
    
    /**
     * ⚠️ Afișează dialog când lifeline-ul a fost deja folosit
     */
    private void showLifelineAlreadyUsedDialog(String lifelineName) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("⚠️ Lifeline folosit")
            .setMessage("Ai folosit deja " + lifelineName + " în această sesiune!")
            .setPositiveButton("✅ Am înțeles", null)
            .show();
    }

    private void initializeQuestions() {
        questions = new ArrayList<>();
        
        // Întrebări despre Oltenia
        questions.add(new Question(
            "Care este cel mai înalt vârf montan din Oltenia?",
            new String[]{"Vârful Parângul Mare", "Vârful Moldoveanu", "Vârful Omu", "Vârful Nedeia"},
            0,
            R.drawable.parang,
            "Vârful Parângul Mare are o altitudine de 2.519 metri și este situat în Munții Parâng."
        ));
        
        questions.add(new Question(
            "Care este cel mai mare oraș din Oltenia?",
            new String[]{"Târgu Jiu", "Slatina", "Râmnicu Vâlcea", "Craiova"},
            3,
            R.drawable.craiova,
            "Craiova este cel mai mare oraș din Oltenia, cu o populație de aproximativ 300.000 de locuitori."
        ));
        
        questions.add(new Question(
            "Ce râu important traversează Oltenia de la nord la sud?",
            new String[]{"Jiu", "Olt", "Mureș", "Siret"},
            0,
            R.drawable.targujiu,
            "Râul Jiu străbate Oltenia de la nord la sud, având o lungime de 331 km."
        ));
        
        questions.add(new Question(
            "În ce județ se află Mănăstirea Tismana?",
            new String[]{"Dolj", "Gorj", "Mehedinți", "Vâlcea"},
            1,
            R.drawable.tismana,
            "Mănăstirea Tismana se află în județul Gorj și este una dintre cele mai vechi mănăstiri din România."
        ));
        
        questions.add(new Question(
            "Care dintre următoarele nu este un județ din Oltenia?",
            new String[]{"Dolj", "Gorj", "Olt", "Argeș"},
            3,
            R.drawable.oltenia_map,
            "Județul Argeș face parte din regiunea Muntenia, nu din Oltenia."
        ));
        
        questions.add(new Question(
            "Ce sculptor român celebru s-a născut în Hobița, Gorj?",
            new String[]{"Constantin Brâncuși", "Ion Jalea", "Dimitrie Paciurea", "Gheorghe Anghel"},
            0,
            R.drawable.brancusi,
            "Constantin Brâncuși s-a născut la Hobița, în județul Gorj, în 1876."
        ));
        
        questions.add(new Question(
            "Ce obiectiv turistic sculptat în stâncă se află în Oltenia?",
            new String[]{"Sfinxul", "Babele", "Chipul lui Decebal", "Cheile Oltețului"},
            2,
            R.drawable.chipul_decebal,
            "Chipul lui Decebal, sculptat în stâncă, se află pe malul Dunării, în județul Mehedinți."
        ));
        
        questions.add(new Question(
            "Care este principala zonă viticolă din Oltenia?",
            new String[]{"Drăgășani", "Recaș", "Cotnari", "Murfatlar"},
            0,
            R.drawable.dragasani,
            "Zona Drăgășani este cunoscută pentru vinurile sale de calitate, în special soiurile Crâmpoșie și Tămâioasă."
        ));
        
        questions.add(new Question(
            "Ce defileu spectaculos se găsește pe râul Olt?",
            new String[]{"Defileul Jiului", "Defileul Oltului", "Cheile Bicazului", "Cheile Turzii"},
            1,
            R.drawable.defileul_oltului,
            "Defileul Oltului este unul dintre cele mai spectaculoase din România, cu o lungime de aproximativ 47 km."
        ));
        
        questions.add(new Question(
            "Ce obiectiv important realizat de Constantin Brâncuși se află în Târgu Jiu?",
            new String[]{"Coloana Infinitului", "Poarta Sărutului", "Masa Tăcerii", "Toate variantele"},
            3,
            R.drawable.brancusi,
            "Ansamblul Monumental realizat de Constantin Brâncuși la Târgu Jiu cuprinde Coloana Infinitului, Poarta Sărutului și Masa Tăcerii."
        ));
        
        // Adăugăm mai multe întrebări despre Oltenia
        questions.add(new Question(
            "Care este cea mai veche mănăstire din Oltenia?",
            new String[]{"Mănăstirea Cozia", "Mănăstirea Tismana", "Mănăstirea Polovragi", "Mănăstirea Horezu"},
            1,
            R.drawable.tismana,
            "Mănăstirea Tismana a fost fondată în secolul al XIV-lea de către Sfântul Nicodim și este cea mai veche mănăstire din Oltenia."
        ));
        
        questions.add(new Question(
            "Ce peșteră importantă se află în Oltenia?",
            new String[]{"Peștera Muierilor", "Peștera Urșilor", "Peștera Scărișoara", "Peștera Polovragi"},
            0,
            R.drawable.pestera_muierilor,
            "Peștera Muierilor din județul Gorj este una dintre cele mai vechi peșteri din România, cu o vechime de aproximativ 1,5 milioane de ani."
        ));
        
        questions.add(new Question(
            "Care este dansul popular specific Olteniei?",
            new String[]{"Călușul", "Hora", "Sârba", "Alunelul"},
            0,
            R.drawable.calusul,
            "Călușul este un dans popular specific Olteniei, inclus în patrimoniul UNESCO ca parte a patrimoniului cultural imaterial al umanității."
        ));
        
        questions.add(new Question(
            "Ce eveniment cultural important se desfășoară anual la Craiova?",
            new String[]{"Festivalul Shakespeare", "Festivalul George Enescu", "Festivalul Internațional de Teatru", "Festivalul Medieval"},
            0,
            R.drawable.craiova,
            "Festivalul Shakespeare este un eveniment cultural important care se desfășoară anual la Craiova și atrage artiști din întreaga lume."
        ));
        
        questions.add(new Question(
            "Care dintre următoarele personalități nu s-a născut în Oltenia?",
            new String[]{"Tudor Vladimirescu", "Constantin Brâncuși", "Mihai Eminescu", "Petrache Poenaru"},
            2,
            R.drawable.oltenia_map,
            "Mihai Eminescu s-a născut la Botoșani, în Moldova, nu în Oltenia."
        ));
        
        questions.add(new Question(
            "Ce monument natural spectaculos se află în Gorj?",
            new String[]{"Sfinxul", "Babele", "Cheile Sohodolului", "Cascada Bigăr"},
            2,
            R.drawable.cheile_sohodolului,
            "Cheile Sohodolului din județul Gorj sunt considerate printre cele mai spectaculoase chei din România."
        ));
        
        questions.add(new Question(
            "Care este cel mai important port dunărean din Oltenia?",
            new String[]{"Orșova", "Calafat", "Drobeta-Turnu Severin", "Corabia"},
            2,
            R.drawable.drobeta,
            "Drobeta-Turnu Severin este cel mai important port dunărean din Oltenia și unul dintre cele mai vechi orașe din România."
        ));
        
        questions.add(new Question(
            "Ce parc național important se află în Oltenia?",
            new String[]{"Parcul Național Domogled-Valea Cernei", "Parcul Național Retezat", "Parcul Național Piatra Craiului", "Parcul Național Ceahlău"},
            0,
            R.drawable.domogled,
            "Parcul Național Domogled-Valea Cernei este situat în sud-vestul României, în Oltenia, și este cel mai mare parc național din țară."
        ));
        
        questions.add(new Question(
            "Care este cea mai importantă stațiune balneară din Oltenia?",
            new String[]{"Băile Herculane", "Băile Felix", "Călimănești-Căciulata", "Sovata"},
            2,
            R.drawable.calimanesti,
            "Călimănești-Căciulata este cea mai importantă stațiune balneară din Oltenia, situată pe Valea Oltului."
        ));
        
        questions.add(new Question(
            "Ce pod celebru traversează Dunărea între România și Bulgaria, în Oltenia?",
            new String[]{"Podul Prieteniei", "Podul Calafat-Vidin", "Podul Giurgiu-Ruse", "Podul Cernavodă"},
            1,
            R.drawable.pod_calafat,
            "Podul Calafat-Vidin (Podul Nova Europa) a fost inaugurat în 2013 și leagă orașul Calafat din Oltenia de orașul Vidin din Bulgaria."
        ));
        
        questions.add(new Question(
            "Care este cel mai vechi oraș din Oltenia?",
            new String[]{"Craiova", "Râmnicu Vâlcea", "Drobeta-Turnu Severin", "Slatina"},
            2,
            R.drawable.drobeta,
            "Drobeta-Turnu Severin este cel mai vechi oraș din Oltenia, fiind fondat de romani în anul 105 d.Hr."
        ));
        
        questions.add(new Question(
            "Care este mâncarea tradițională specifică Olteniei?",
            new String[]{"Sarmale", "Piftie", "Ciorbă de burtă", "Praz cu ciolan afumat"},
            3,
            R.drawable.praz_ciolan,
            "Prazul cu ciolan afumat este o mâncare tradițională specifică Olteniei, foarte apreciată în gastronomia locală."
        ));
        
        questions.add(new Question(
            "Ce rezervație naturală importantă se află în Mehedinți?",
            new String[]{"Rezervația Naturală Ponoarele", "Rezervația Naturală Retezat", "Rezervația Naturală Bucegi", "Rezervația Naturală Apuseni"},
            0,
            R.drawable.ponoarele,
            "Rezervația Naturală Ponoarele din județul Mehedinți este cunoscută pentru fenomenele carstice spectaculoase, inclusiv Podul Natural de la Ponoarele."
        ));
        
        questions.add(new Question(
            "Ce lac de acumulare important se află pe râul Olt, în Oltenia?",
            new String[]{"Lacul Vidraru", "Lacul Vidra", "Lacul Izvorul Muntelui", "Lacul Călimănești"},
            3,
            R.drawable.lac_calimanesti,
            "Lacul Călimănești este un lac de acumulare important pe râul Olt, în Oltenia, utilizat pentru producerea de energie electrică."
        ));
        
        questions.add(new Question(
            "Ce castel important se află în județul Gorj?",
            new String[]{"Castelul Peleș", "Castelul Bran", "Castelul Corvinilor", "Castelul de la Măldărești"},
            3,
            R.drawable.castel_maldaresti,
            "Castelul de la Măldărești (Cula Măldărești) este un monument istoric important din județul Gorj, reprezentativ pentru arhitectura tradițională oltenească."
        ));
        
        Collections.shuffle(questions);
        progressBar.setMax(questions.size());
        totalQuestions = questions.size();
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            return; // Acest caz este gestionat în moveToNextQuestion()
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        questionTextView.setText(currentQuestion.question);
        
        // Resetăm starea butoanelor pentru noua întrebare cu stilizare modernă
        for (MaterialButton button : answerButtons) {
            button.setEnabled(true);
            button.setAlpha(1.0f);
            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.oltenia_primary)));
            button.setTextColor(getResources().getColor(R.color.oltenia_card_bg));
            button.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.oltenia_accent)));
        }
        
        // Setăm răspunsurile
        for (int i = 0; i < currentQuestion.answers.length; i++) {
            answerButtons[i].setText(currentQuestion.answers[i]);
        }
        
        // Setăm imaginea cu stilizare modernă
        if (currentQuestion.imageResourceId != 0) {
            questionImage.setVisibility(View.VISIBLE);
            questionImage.setImageResource(currentQuestion.imageResourceId);
            questionImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            questionImage.setVisibility(View.GONE);
        }
        
        // Actualizăm progress bar și text
        progressBar.setProgress(currentQuestionIndex + 1);
        TextView progressText = findViewById(R.id.progressText);
        if (progressText != null) {
            progressText.setText((currentQuestionIndex + 1) + "/" + questions.size());
        }
        
        // Actualizăm streak display
        updateStreakDisplay();
        
        // Resetăm lifeline-urile pentru noua întrebare
        resetLifelinesForNewQuestion();
        
        // Pornim timer-ul pentru întrebarea curentă
        startTimer();
    }

    private void checkAnswer(int selectedAnswerIndex, String selectedAnswer) {
        // Oprește timer-ul
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        int correctIndex = currentQuestion.correctAnswerIndex;
        
        // Calculează timpul de răspuns
        long answerTime = (TIME_PER_QUESTION - (timer != null ? 0 : 0)) / 1000;
        
        // Dezactivăm toate butoanele pentru a prevenir răspunsuri multiple
        for (MaterialButton button : answerButtons) {
            button.setEnabled(false);
        }
        
        if (selectedAnswerIndex == correctIndex) {
            // Răspuns corect - animație modernă
            provideHapticFeedback(HapticFeedbackType.CORRECT);
            animateCorrectAnswer(answerButtons[selectedAnswerIndex]);
            
            score += POINTS_PER_CORRECT_ANSWER;
            correctAnswers++;
            
            // Actualizează streak-ul
            streak++;
            if (streak > maxStreak) {
                maxStreak = streak;
            }
            
            // Bonus pentru streak
            if (streak >= STREAK_BONUS_THRESHOLD && streak % STREAK_BONUS_THRESHOLD == 0) {
                score += BONUS_POINTS;
                showStreakBonus();
            }
            
            updateScore();
            updateStreakDisplay();
            
            // Actualizează achievement-urile pentru răspuns corect
            if (achievementManager != null) {
                achievementManager.recordOlteniaQuizAnswer(true, 
                    inferCategory(currentQuestion.question).name(), 
                    answerTime, 
                    streak);
            }
            
            // Afișăm informația suplimentară cu stilizare modernă
            TextView factTextView = findViewById(R.id.factTextView);
            factTextView.setText("✅ Corect! " + currentQuestion.fact);
            factTextView.setVisibility(View.VISIBLE);
            
            // Trecem la următoarea întrebare după o scurtă pauză
            new Handler().postDelayed(this::moveToNextQuestion, 2500);
        } else {
            // Răspuns greșit - animație modernă
            provideHapticFeedback(HapticFeedbackType.WRONG);
            animateWrongAnswer(answerButtons[selectedAnswerIndex]);
            answerButtons[correctIndex].setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.oltenia_success)));
            
            // Resetează streak-ul
            streak = 0;
            updateStreakDisplay();
            
            // Actualizează achievement-urile pentru răspuns greșit
            if (achievementManager != null) {
                achievementManager.recordOlteniaQuizAnswer(false, 
                    inferCategory(currentQuestion.question).name(), 
                    answerTime, 
                    streak);
            }
            
            // Afișăm informația suplimentară cu stilizare modernă
            TextView factTextView = findViewById(R.id.factTextView);
            factTextView.setText("❌ Greșit! Răspunsul corect este: " + currentQuestion.answers[correctIndex] + ". " + currentQuestion.fact);
            factTextView.setVisibility(View.VISIBLE);
            
            // Trecem la următoarea întrebare după o scurtă pauză
            new Handler().postDelayed(this::moveToNextQuestion, 3500);
        }
    }

    private void moveToNextQuestion() {
        currentQuestionIndex++;
        TextView factTextView = findViewById(R.id.factTextView);
        factTextView.setVisibility(View.GONE);
        
        if (currentQuestionIndex < questions.size()) {
        displayQuestion();
            startTimer(); // Restart timer pentru următoarea întrebare
        } else {
            // Jocul s-a terminat
            finishGame();
        }
    }

    private void updateScore() {
        scoreTextView.setText(String.valueOf(score));
    }

    private String getAchievements() {
        StringBuilder achievements = new StringBuilder();
        if (correctAnswers >= questions.size() * 0.8) achievements.append("Expert în Oltenia!\n");
        if (score >= 100) achievements.append("Scor impresionant!\n");
        if (isFiftyFiftyUsed && isSkipUsed) achievements.append("Utilizator de lifeline!\n");
        if (!isFiftyFiftyUsed && !isSkipUsed && correctAnswers >= questions.size() * 0.7) achievements.append("Fără ajutor - expert adevărat!\n");
        return achievements.toString();
    }

    private void finishGame() {
        // Calculăm timpul total
        totalTime = System.currentTimeMillis() - questionStartTime;
        
        // Actualizăm achievement-urile
        if (achievementManager != null) {
            achievementManager.checkOlteniaAchievements(score, (float) correctAnswers / totalQuestions, correctAnswers);
        }
        
        // Salvăm punctele
        pointsManager.addPoints(this, "Oltenia", score);
        
        // Navigăm la activitatea de rezultate modulară
        Intent resultIntent = new Intent(this, OlteniaGameResultActivity.class);
        resultIntent.putExtra("score", score);
        resultIntent.putExtra("correctAnswers", correctAnswers);
        resultIntent.putExtra("totalQuestions", totalQuestions);
        resultIntent.putExtra("maxStreak", maxStreak);
        resultIntent.putExtra("totalTime", totalTime);
        resultIntent.putExtra("lifelinesUsed", lifelinesUsed);
        
        startActivity(resultIntent);
        finish();
    }

    /**
     * ❌ Dialog când utilizatorul preferă baza de date dar nu există internet
     */
    private void showNoInternetForPreferredDatabaseDialog() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("❌ Fără internet");
        dialogBuilder.setCancelable(false);
        
        String baseMessage = "Preferați baza de date, dar nu există conexiune la internet.\n\n💡 Opțiuni disponibile:";
        
        if (checkIfLocalCacheExists()) {
            dialogBuilder.setMessage(baseMessage + 
                    "\n\n📱 Cache Local disponibil\n🔄 Așteptați internetul\n⚙️ Schimbați preferința");
            
            dialogBuilder.setPositiveButton("📱 Cache Local", (dialog, which) -> {
                loadQuestionsFromLocalCache();
            });
            
            dialogBuilder.setNeutralButton("⚙️ Schimbă preferința", (dialog, which) -> {
                showDataSourceSelectionDialogWithPreferences();
            });
        } else {
            dialogBuilder.setMessage(baseMessage + 
                    "\n\n❌ Nu există cache local\n🔄 Așteptați internetul\n⚙️ Schimbați preferința");
            
            dialogBuilder.setNeutralButton("⚙️ Schimbă preferința", (dialog, which) -> {
                showDataSourceSelectionDialogWithPreferences();
            });
        }
        
        dialogBuilder.setNegativeButton("🚪 Înapoi", (dialog, which) -> finish());
        dialogBuilder.show();
    }
    
    /**
     * ❌ Dialog când utilizatorul preferă cache local dar nu există
     */
    private void showNoCacheForPreferredLocalDialog() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("📱 Cache indisponibil");
        dialogBuilder.setCancelable(false);
        
        String baseMessage = "Preferați cache local, dar nu există întrebări salvate.\n\n💡 Opțiuni disponibile:";
        
        if (syncManager.isInternetAvailable()) {
            dialogBuilder.setMessage(baseMessage + 
                    "\n\n🌐 Baza de Date disponibilă\n⚙️ Schimbați preferința");
            
            dialogBuilder.setPositiveButton("🌐 Baza de Date", (dialog, which) -> {
                loadQuestionsFromFirestore();
            });
        } else {
            dialogBuilder.setMessage(baseMessage + 
                    "\n\n❌ Nu există internet\n⚙️ Schimbați preferința");
        }
        
        dialogBuilder.setNeutralButton("⚙️ Schimbă preferința", (dialog, which) -> {
            showDataSourceSelectionDialogWithPreferences();
        });
        
        dialogBuilder.setNegativeButton("🚪 Înapoi", (dialog, which) -> finish());
        dialogBuilder.show();
    }
    
    /**
     * Show data source selection dialog
     */
    private void showDataSourceSelectionDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Alege sursa de date")
            .setMessage("De unde dorești să încarci întrebările?")
            .setPositiveButton("Internet", (dialog, which) -> loadQuestionsFromFirestore())
            .setNegativeButton("Local", (dialog, which) -> initializeQuestions())
            .setNeutralButton("Anulează", (dialog, which) -> finish())
            .show();
    }
    
    /**
     * 🤔 Dialog pentru alegerea sursei de date cu opțiuni de preferințe
     */
    private void showDataSourceSelectionDialogWithPreferences() {
        boolean hasInternet = syncManager.isInternetAvailable();
        boolean hasLocalCache = checkIfLocalCacheExists();
        
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("🎯 Alege sursa de date");
        dialogBuilder.setCancelable(false);
        
        String message = "De unde dorești să încarci întrebările?\n\n" +
                        "📊 Status disponibilitate:\n" +
                        "🌐 Internet: " + (hasInternet ? "✅ Disponibil" : "❌ Indisponibil") + "\n" +
                        "📱 Cache Local: " + (hasLocalCache ? "✅ Disponibil" : "❌ Indisponibil") + "\n\n" +
                        "💡 Poți seta o preferință pentru viitor:";
        
        dialogBuilder.setMessage(message);
        
        // Opțiuni disponibile bazate pe status
        if (hasInternet && hasLocalCache) {
            dialogBuilder.setPositiveButton("🌐 Internet", (dialog, which) -> {
                loadQuestionsFromFirestore();
            });
            dialogBuilder.setNegativeButton("📱 Cache Local", (dialog, which) -> {
                loadQuestionsFromLocalCache();
            });
        } else if (hasInternet) {
            dialogBuilder.setPositiveButton("🌐 Internet", (dialog, which) -> {
                loadQuestionsFromFirestore();
            });
        } else if (hasLocalCache) {
            dialogBuilder.setPositiveButton("📱 Cache Local", (dialog, which) -> {
                loadQuestionsFromLocalCache();
            });
        }
        
        // Opțiuni de preferințe
        dialogBuilder.setNeutralButton("⚙️ Setează preferință", (dialog, which) -> {
            showPreferenceSelectionDialog();
        });
        
        dialogBuilder.show();
    }
    
    /**
     * ⚙️ Dialog pentru setarea preferințelor
     */
    private void showPreferenceSelectionDialog() {
        String[] options = {
            "🌐 Întotdeauna baza de date",
            "📱 Întotdeauna cache local", 
            "🎯 Alegere automată",
            "🤔 Întreabă de fiecare dată"
        };
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("⚙️ Setează preferința")
            .setItems(options, (dialog, which) -> {
                String preference;
                String message;
                
                switch (which) {
                    case 0:
                        preference = "always_database";
                        message = "🌐 Preferința setată: Întotdeauna baza de date";
                        break;
                    case 1:
                        preference = "always_cache";
                        message = "📱 Preferința setată: Întotdeauna cache local";
                        break;
                    case 2:
                        preference = "auto";
                        message = "🎯 Preferința setată: Alegere automată";
                        break;
                    default:
                        preference = "ask_every_time";
                        message = "🤔 Preferința setată: Întreabă de fiecare dată";
                        break;
                }
                
                saveUserPreference(preference);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                
                // Reîncarcă cu noua preferință
                checkUserPreferenceAndLoad();
            })
            .setNegativeButton("🚪 Înapoi", (dialog, which) -> {
                showDataSourceSelectionDialogWithPreferences();
            })
            .show();
    }
    
    /**
     * 💾 Salvează preferința utilizatorului
     */
    private void saveUserPreference(String preference) {
        getSharedPreferences("OlteniaGamePrefs", MODE_PRIVATE)
            .edit()
            .putString("data_source_preference", preference)
            .apply();
        
        Log.d(TAG, "💾 Preferința utilizatorului salvată: " + preference);
    }
    
    /**
     * 🔄 Resetează preferințele utilizatorului
     */
    private void resetUserPreferences() {
        getSharedPreferences("OlteniaGamePrefs", MODE_PRIVATE)
            .edit()
            .clear()
            .apply();
        
        Log.d(TAG, "🔄 Preferințele utilizatorului resetate");
    }
    
    /**
     * Load questions from Firestore
     */
    private void loadQuestionsFromFirestore() {
        Log.d(TAG, "🔍 Loading questions from Firestore...");
        
        questionRepository.getQuestionsAsModels(REGION, GAME_TYPE)
            .thenAccept(loadedQuestions -> {
                runOnUiThread(() -> {
                    if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
                        firestoreQuestions = loadedQuestions;
                        Log.d(TAG, "✅ Questions loaded from Firestore: " + firestoreQuestions.size());
                        
                        // ✅ CACHE LOCAL: Salvăm în cache pentru utilizare offline
                        saveQuestionsToLocalCache(loadedQuestions);
                        
                        convertAndDisplayQuestions();
                    } else {
                        Log.d(TAG, "❌ No questions found in Firestore, falling back to local cache");
                        loadQuestionsFromLocalCache();
                    }
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    Log.e(TAG, "❌ Error loading from Firestore", throwable);
                    Toast.makeText(this, "Eroare la încărcarea din baza de date. Încărcare din cache local...", 
                                 Toast.LENGTH_SHORT).show();
                    loadQuestionsFromLocalCache();
                });
                return null;
            });
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Convertește QuestionModel în EnhancedQuestionModel
     */
    private List<EnhancedQuestionModel> convertToEnhancedQuestions(List<QuestionModel> questions) {
        List<EnhancedQuestionModel> enhancedQuestions = new ArrayList<>();
        
        for (QuestionModel question : questions) {
            EnhancedQuestionModel enhanced = new EnhancedQuestionModel(
                question.getQuestion(),
                question.getCorrectAnswer(),
                question.getIncorrectAnswers(), // Pass List<String> directly
                question.getImageResourceId(),
                question.getFact(),
                inferCategory(question.getQuestion()),
                inferDifficulty(question),
                generateTags(question)
            );
            enhancedQuestions.add(enhanced);
        }
        
        return enhancedQuestions;
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Inferă categoria pe baza textului întrebării
     */
    private EnhancedQuestionModel.Category inferCategory(String questionText) {
        String lowerText = questionText.toLowerCase();
        
        if (lowerText.contains("istoric") || lowerText.contains("război") || lowerText.contains("rege") || 
            lowerText.contains("domn") || lowerText.contains("veac") || lowerText.contains("anul")) {
            return EnhancedQuestionModel.Category.HISTORY;
        } else if (lowerText.contains("munte") || lowerText.contains("râu") || lowerText.contains("oraș") ||
                  lowerText.contains("județ") || lowerText.contains("regiune")) {
            return EnhancedQuestionModel.Category.GEOGRAPHY;
        } else if (lowerText.contains("arhitectur") || lowerText.contains("biserică") || lowerText.contains("castel") ||
                  lowerText.contains("monument") || lowerText.contains("clădir")) {
            return EnhancedQuestionModel.Category.ARCHITECTURE;
        } else if (lowerText.contains("mâncare") || lowerText.contains("bucătar") || lowerText.contains("rețet") ||
                  lowerText.contains("tradițional") || lowerText.contains("specific")) {
            return EnhancedQuestionModel.Category.GASTRONOMY;
        } else if (lowerText.contains("legendă") || lowerText.contains("mit") || lowerText.contains("poveste") ||
                  lowerText.contains("basm") || lowerText.contains("credință")) {
            return EnhancedQuestionModel.Category.LEGENDS;
        } else if (lowerText.contains("personalitate") || lowerText.contains("scriitor") || lowerText.contains("poet") ||
                  lowerText.contains("artist") || lowerText.contains("născut")) {
            return EnhancedQuestionModel.Category.PERSONALITIES;
        } else if (lowerText.contains("natură") || lowerText.contains("parc") || lowerText.contains("rezervație") ||
                  lowerText.contains("animal") || lowerText.contains("plantă")) {
            return EnhancedQuestionModel.Category.NATURE;
        } else {
            return EnhancedQuestionModel.Category.CULTURE;
        }
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Inferă dificultatea pe baza complexității întrebării
     */
    private EnhancedQuestionModel.Difficulty inferDifficulty(QuestionModel question) {
        String questionText = question.getQuestion();
        String fact = question.getFact();
        
        // Calculăm complexitatea pe baza lungimii și cuvintelor cheie
        int complexity = 0;
        
        if (questionText.length() > 100) complexity++;
        if (fact.length() > 150) complexity++;
        if (questionText.split("\\s+").length > 15) complexity++;
        
        // Cuvinte care indică dificultate mare
        String[] hardWords = {"secol", "perioada", "domnie", "arhitectural", "stilistic", "influență"};
        for (String word : hardWords) {
            if (questionText.toLowerCase().contains(word)) complexity++;
        }
        
        if (complexity >= 3) return EnhancedQuestionModel.Difficulty.HARD;
        else if (complexity >= 1) return EnhancedQuestionModel.Difficulty.MEDIUM;
        else return EnhancedQuestionModel.Difficulty.EASY;
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Generează tag-uri pentru întrebare
     */
    private String[] generateTags(QuestionModel question) {
        List<String> tags = new ArrayList<>();
        String text = (question.getQuestion() + " " + question.getFact()).toLowerCase();
        
        // Tag-uri geografice
        if (text.contains("craiova")) tags.add("craiova");
        if (text.contains("slatina")) tags.add("slatina");
        if (text.contains("caracal")) tags.add("caracal");
        if (text.contains("târgu jiu")) tags.add("targu-jiu");
        
        // Tag-uri istorice
        if (text.contains("brâncoveanu")) tags.add("brancoveanu");
        if (text.contains("mihai")) tags.add("mihai-viteazul");
        if (text.contains("tudor")) tags.add("tudor");
        
        // Tag-uri culturale
        if (text.contains("brâncuși")) tags.add("brancusi");
        if (text.contains("eminescu")) tags.add("eminescu");
        
        // Tag-uri generale
        tags.add("oltenia");
        tags.add("romania");
        
        return tags.toArray(new String[0]);
    }
    
    /**
     * Convert enhanced questions to local Question format and display
     */
    private void convertAndDisplayQuestions() {
        // ✅ ÎMBUNĂTĂȚIRE: Convertește în enhanced questions și aplică filtre
        enhancedQuestions = convertToEnhancedQuestions(firestoreQuestions);
        enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
        
        // Convertește enhanced questions în format local pentru compatibilitate
        questions = new ArrayList<>();
        
        for (EnhancedQuestionModel enhanced : enhancedQuestions) {
            // Use the List<String> version for easier manipulation
            List<String> incorrectAnswers = enhanced.getIncorrectAnswers();
            String[] allAnswers = new String[incorrectAnswers.size() + 1];
            allAnswers[0] = enhanced.getCorrectAnswer();
            
            for (int i = 0; i < incorrectAnswers.size(); i++) {
                allAnswers[i + 1] = incorrectAnswers.get(i);
            }
            
            // Shuffle answers and find correct index
            List<String> answerList = new ArrayList<>();
            for (String answer : allAnswers) {
                answerList.add(answer);
            }
            Collections.shuffle(answerList);
            
            int correctIndex = answerList.indexOf(enhanced.getCorrectAnswer());
            
            questions.add(new Question(
                enhanced.getQuestion(),
                answerList.toArray(new String[0]),
                correctIndex,
                enhanced.getImageResourceId(),
                enhanced.getFact()
            ));
        }
        
        Collections.shuffle(questions);
        progressBar.setMax(questions.size());
        totalQuestions = questions.size();
        isDataLoaded = true;
        
        // ✅ ÎMBUNĂTĂȚIRE: Începe sesiunea de tracking
        progressTracker.startNewSession();
        
        displayQuestion();
        startTimer();
        
        // ✅ ÎMBUNĂTĂȚIRE: Setup modern back button handling
        setupBackButtonHandling();
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Setup modern pentru gestionarea butonului back
     */
    private void setupBackButtonHandling() {
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
    @Override
            public void handleOnBackPressed() {
        // Ask user if they want to exit the game
                new androidx.appcompat.app.AlertDialog.Builder(OlteniaGameActivity.this)
            .setTitle(R.string.exit_game)
            .setMessage(R.string.exit_game_confirmation)
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                        Intent intent = new Intent(OlteniaGameActivity.this, Oltenia.class);
                startActivity(intent);
                finish();
            })
                    .setNegativeButton(R.string.no, (dialog, which) -> {
                        // Do nothing, just dismiss dialog
                    })
                    .setOnCancelListener(dialog -> {
                        // Do nothing, just dismiss dialog
                    })
            .show();
    }
        });
    }
    

    
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
            questionMap.put("incorrectAnswers", question.getIncorrectAnswers()); // Folosim String[]
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
     * 🌐 Încarcă întrebările din baza de date (separat de Firestore)
     */
    private void loadQuestionsFromDatabase() {
        Log.d(TAG, "🌐 Încărcăm întrebările din baza de date pentru " + REGION + "_" + GAME_TYPE);
        
        // Verificăm dacă avem conexiune la internet
        if (!syncManager.isInternetAvailable()) {
            Log.w(TAG, "🌐 ❌ Nu există conexiune la internet - folosim cache local");
            loadQuestionsFromLocalCache();
            return;
        }
        
        // Încărcăm din Firestore (baza de date)
        questionRepository.getQuestions(REGION, GAME_TYPE)
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    List<QuestionModel> questions = new ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
                        QuestionModel question = document.toObject(QuestionModel.class);
                        if (question != null) {
                            questions.add(question);
                        }
                    }
                    
                    if (!questions.isEmpty()) {
                        firestoreQuestions = questions;
                        Log.d(TAG, "🌐 ✅ Întrebări încărcate din baza de date: " + firestoreQuestions.size());
                        
                        // Salvăm în cache local pentru utilizare offline
                        saveQuestionsToLocalCache(questions);
                        
                        // Convertim și afișăm întrebările
                        convertAndDisplayQuestions();
                        
                        // Actualizăm indicatorul sursei de date
                        updateDataSourceIndicator("🌐 Baza de Date");
                    } else {
                        Log.w(TAG, "🌐 ⚠️ Nu s-au găsit întrebări în baza de date");
                        showNoDatabaseQuestionsDialog();
                    }
                } else {
                    Log.w(TAG, "🌐 ⚠️ Baza de date este goală");
                    showNoDatabaseQuestionsDialog();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "🌐 ❌ Eroare la încărcarea din baza de date", e);
                showDatabaseErrorDialog();
            });
    }
    
    /**
     * ❌ Dialog când nu există întrebări în baza de date
     */
    private void showNoDatabaseQuestionsDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Baza de date goală")
            .setMessage("Nu există întrebări în baza de date pentru această regiune.\n\n" +
                       "Opțiuni disponibile:\n" +
                       "📱 Cache Local\n" +
                       "🔄 Reîncearcă\n" +
                       "🚪 Înapoi")
            .setPositiveButton("📱 Cache Local", (dialog, which) -> {
                loadQuestionsFromLocalCache();
            })
            .setNegativeButton("🔄 Reîncearcă", (dialog, which) -> {
                loadQuestionsFromDatabase();
            })
            .setNeutralButton("🚪 Înapoi", (dialog, which) -> {
                finish();
            })
            .show();
    }
    
    /**
     * ❌ Dialog pentru erori de baza de date
     */
    private void showDatabaseErrorDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Eroare baza de date")
            .setMessage("Nu s-au putut încărca întrebările din baza de date.\n\n" +
                       "Cauze posibile:\n" +
                       "• Probleme de conexiune\n" +
                       "• Baza de date indisponibilă\n" +
                       "• Erori de configurare\n\n" +
                       "Opțiuni disponibile:\n" +
                       "📱 Cache Local\n" +
                       "🔄 Reîncearcă\n" +
                       "🚪 Înapoi")
            .setPositiveButton("📱 Cache Local", (dialog, which) -> {
                loadQuestionsFromLocalCache();
            })
            .setNegativeButton("🔄 Reîncearcă", (dialog, which) -> {
                loadQuestionsFromDatabase();
            })
            .setNeutralButton("🚪 Înapoi", (dialog, which) -> {
                finish();
            })
            .show();
    }
    
    /**
     * 📊 Actualizează indicatorul sursei de date
     */
    private void updateDataSourceIndicator(String source) {
        // Aici putem afișa un indicator vizual pentru sursa de date
        Log.d(TAG, "📊 Sursa de date: " + source);
        
        // Opțional: afișăm un Toast pentru a informa utilizatorul
        Toast.makeText(this, "📊 " + source, Toast.LENGTH_SHORT).show();
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
                        
                        convertAndDisplayQuestions();
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "💾 ❌ Eroare la încărcarea din cache local", e);
            }
        }
        
        // Dacă nu avem cache local valid, folosim întrebările locale
        Log.w(TAG, "💾 ⚠️ Nu există cache local valid - folosim întrebările locale");
        initializeQuestions();
    }
    
    /**
     * ⏱️ Începe timer-ul pentru întrebare
     */
    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        
        // Folosim timpul configurat pe baza modului și dificultății
        timer = new CountDownTimer(TIME_PER_QUESTION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                if (timerTextView != null) {
                    timerTextView.setText(String.format("⏱️ %d", seconds));
                    
                    // Schimbă culoarea când timpul se termină
                    if (seconds <= 5) {
                        timerTextView.setTextColor(getResources().getColor(R.color.timer_warning));
                    } else {
                        timerTextView.setTextColor(getResources().getColor(R.color.oltenia_text));
                    }
                }
            }
            
            @Override
            public void onFinish() {
                handleTimeout();
            }
        };
        
        timer.start();
    }
    
    /**
     * ⏰ Gestionează timeout-ul întrebării
     */
    private void handleTimeout() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        
        // Dezactivează toate butoanele
        for (MaterialButton button : answerButtons) {
            button.setEnabled(false);
        }
        
        // Afișează răspunsul corect
        Question currentQuestion = questions.get(currentQuestionIndex);
        int correctIndex = currentQuestion.correctAnswerIndex;
        
        // Evidențiază răspunsul corect
        answerButtons[correctIndex].setBackgroundColor(getResources().getColor(R.color.correct_answer));
        
        // Resetează streak-ul
        streak = 0;
        updateStreakDisplay();
        
        // Afișează informația suplimentară
        showTimeoutFeedback(currentQuestion);
        
        // Actualizează achievement-urile pentru timeout
        if (achievementManager != null) {
            achievementManager.recordOlteniaQuizAnswer(false, 
                inferCategory(currentQuestion.question).name(), 
                TIME_PER_QUESTION / 1000.0f, 
                streak);
        }
        
        // Trece la următoarea întrebare după o pauză
        new Handler().postDelayed(this::moveToNextQuestion, 3000);
    }
    
    /**
     * 📢 Afișează feedback pentru timeout
     */
    private void showTimeoutFeedback(Question question) {
        String timeoutMessage = "⏰ Timpul a expirat!\n\n" +
                               "Răspunsul corect era: " + question.answers[question.correctAnswerIndex] + "\n\n" +
                               question.fact;
        
        // Poți afișa într-un TextView sau dialog
        Toast.makeText(this, "⏰ Timpul a expirat!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 🔥 Actualizează afișajul streak-ului
     */
    private void updateStreakDisplay() {
        if (streakTextView != null) {
            streakTextView.setText("🔥 Reușite consecutive: " + streak);
            
            // Animație pentru streak mare
            if (streak >= STREAK_BONUS_THRESHOLD) {
                streakTextView.setTextColor(getResources().getColor(R.color.oltenia_accent));
                // Animație de pulsare pentru streak mare
                streakTextView.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(200)
                    .withEndAction(() -> 
                        streakTextView.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(200)
                            .start()
                    ).start();
            } else {
                streakTextView.setTextColor(getResources().getColor(R.color.oltenia_primary));
            }
        }
    }
    
    /**
     * 🎉 Afișează bonus pentru streak
     */
    private void showStreakBonus() {
        Toast.makeText(this, 
            String.format("🔥 Streak Bonus! +%d puncte pentru %d răspunsuri consecutive!", 
                         BONUS_POINTS, streak), 
            Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Cleanup timer
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


} 