package com.example.myapplication.bucovinausage;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;

import com.example.myapplication.models.QuestionModel;
import com.example.myapplication.models.EnhancedQuestionModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.Bucovina;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.bucovinausage.DifficultyManager;
import com.example.myapplication.bucovinausage.GameModeManager;
import com.example.myapplication.bucovinausage.PlayerProgressTracker;
import com.example.myapplication.utils.HapticFeedbackType;
import com.example.myapplication.Joc1.AchievementManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.bumptech.glide.Glide;
import java.util.Locale;
import java.util.Arrays;
import android.content.Context;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.os.Build;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.Date;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class BucovinaGameActivity extends AppCompatActivity {
    private static final String TAG = "BucovinaGameActivity";
    private static final String REGION = "bucovina";
    private static final String GAME_TYPE = "quiz";
    
    // UI Components
    private TextView questionTextView;
    private MaterialButton[] answerButtons;
    private TextView scoreTextView;
    private ProgressBar progressBar;
    private TextView timerTextView;
    private TextView streakTextView;
    private ImageView questionImage;
    private MaterialButton fiftyFiftyButton;
    private MaterialButton skipQuestionButton;
    private MaterialButton finishButton;
    private MaterialCardView[] answerCards;
    private CardView questionImageCard;
    private TextView factTextView;
    
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
    private List<QuestionModel> questions;
    private List<QuestionModel> firestoreQuestions;
    private List<EnhancedQuestionModel> enhancedQuestions;
    
    // Enhanced game systems - following Transilvania pattern
    private DifficultyManager difficultyManager;
    private GameModeManager gameModeManager;
    private PlayerProgressTracker progressTracker;
    private AchievementManager achievementManager;
    
    // Dynamic game constants based on difficulty and mode
    private int POINTS_PER_CORRECT_ANSWER = 2;
    private int BONUS_POINTS = 5;
    private int TIME_PER_QUESTION = 30000; // Will be updated based on mode/difficulty
    private static final int STREAK_BONUS_THRESHOLD = 3;
    
    // Existing managers
    private PointsManager pointsManager;
    private CountDownTimer timer;
    private boolean isFiftyFiftyUsed = false;
    private boolean isHintUsed = false;
    private boolean isSkipUsed = false;
    private Random random = new Random();

    // --- HYBRID SYSTEM FIELDS ---
    private static final String DATA_SOURCE_PREF_KEY = "data_source_preference";
    private static final String CACHE_KEY = "questions_cache_" + REGION + "_" + GAME_TYPE;
    private static final String CACHE_TIMESTAMP_KEY = CACHE_KEY + "_timestamp";
    private static final long CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24h
    private String dataSourcePreference = "ask_every_time";
    private int numQuestions = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set theme before super.onCreate
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucovina_game);

        // Initialize enhanced systems first
        initializeBucovinaManagers();
        
        initializeViews();
        pointsManager = PointsManager.getInstance(this);
        showInitialSetupDialog();
        // Note: displayQuestion() and startTimer() are now called from finalizeQuestionsLoading()
    }
    
    /**
     * Initialize enhanced game systems following Transilvania pattern
     */
    private void initializeBucovinaManagers() {
        try {
            difficultyManager = new DifficultyManager(this);
            gameModeManager = new GameModeManager(this);
            progressTracker = new PlayerProgressTracker(this);
            achievementManager = new AchievementManager(this);
            
            // Extract game mode from intent (if available)
            Intent intent = getIntent();
            GameModeManager.GameMode gameMode = GameModeManager.GameMode.CLASSIC;
            EnhancedQuestionModel.Category focusCategory = null;
            
            if (intent.hasExtra("game_mode")) {
                try {
                    gameMode = GameModeManager.GameMode.valueOf(intent.getStringExtra("game_mode"));
                } catch (Exception e) {
                    Log.w(TAG, "Invalid game mode in intent, using CLASSIC", e);
                }
            }
            
            if (intent.hasExtra("focus_category")) {
                try {
                    focusCategory = EnhancedQuestionModel.Category.valueOf(intent.getStringExtra("focus_category"));
                } catch (Exception e) {
                    Log.w(TAG, "Invalid focus category in intent", e);
                }
            }
            
            // Initialize game mode with proper parameters
            gameModeManager.initializeGameMode(gameMode, focusCategory);
            
            // Update game constants based on mode and difficulty
            updateGameConstants();
            
            Log.d(TAG, "Enhanced systems initialized successfully for Bucovina");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing enhanced systems for Bucovina", e);
            // Fallback to basic functionality
            difficultyManager = new DifficultyManager(this);
            gameModeManager = new GameModeManager(this);
            progressTracker = new PlayerProgressTracker(this);
        }
    }
    
    private void updateGameConstants() {
        if (gameModeManager != null && difficultyManager != null) {
            GameModeManager.GameMode currentMode = gameModeManager.getCurrentGameMode();
            DifficultyManager.DifficultyLevel currentDifficulty = difficultyManager.getCurrentDifficulty();
            
            // Update time per question based on difficulty
            TIME_PER_QUESTION = currentDifficulty.timePerQuestion;
            
            // Update points based on mode
            POINTS_PER_CORRECT_ANSWER = 2;
            if (currentMode == GameModeManager.GameMode.LIGHTNING) {
                POINTS_PER_CORRECT_ANSWER = 15; // More points for quick mode
            } else if (currentMode == GameModeManager.GameMode.MARATHON) {
                POINTS_PER_CORRECT_ANSWER = 8; // Slightly less for longer mode
            }
            
            Log.d(TAG, "Updated game constants - Time: " + TIME_PER_QUESTION + "ms, Points: " + POINTS_PER_CORRECT_ANSWER);
        }
    }

    private void initializeViews() {
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
        progressBar = findViewById(R.id.progressBar);
        timerTextView = findViewById(R.id.timerTextView);
        streakTextView = findViewById(R.id.streakTextView);
        questionImage = findViewById(R.id.questionImage);
        questionImageCard = findViewById(R.id.questionImageCard);
        factTextView = findViewById(R.id.factTextView);
        fiftyFiftyButton = findViewById(R.id.fiftyFiftyButton);
        skipQuestionButton = findViewById(R.id.skipQuestionButton);
        finishButton = findViewById(R.id.finishButton);
        
        // Apply button styles
        for (MaterialButton button : answerButtons) {
            if (button != null) {
                button.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                button.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                button.setClickable(false);
                button.setFocusable(false);
            }
        }
        
        // Initialize finish button
        if (finishButton != null) {
            finishButton.setOnClickListener(v -> finishGame());
            finishButton.setVisibility(View.GONE);
        }
        
        // Setup click for answer cards
        for (int i = 0; i < answerCards.length; i++) {
            final int index = i;
            if (answerCards[i] != null) {
                answerCards[i].setOnClickListener(v -> {
                    if (v.isClickable() && index < answerButtons.length && answerButtons[index] != null) {
                        checkAnswer(index, answerButtons[index].getText().toString());
                    }
                });
            }
        }
        
        // Setup lifeline buttons
        if (fiftyFiftyButton != null) {
        fiftyFiftyButton.setOnClickListener(v -> useFiftyFifty());
        }
        
        if (skipQuestionButton != null) {
            skipQuestionButton.setOnClickListener(v -> skipQuestion());
        }
        
        if (finishButton != null) {
            finishButton.setOnClickListener(v -> finishGame());
        }
    }
    
    private void applyBucovinaTheme() {
        // Apply Bucovina theme colors to UI elements
        int primaryColor = ContextCompat.getColor(this, R.color.bucovina_primary);
        int primaryLightColor = ContextCompat.getColor(this, R.color.bucovina_primary_light);
        int textColor = ContextCompat.getColor(this, R.color.bucovina_text);
        int cardBgColor = ContextCompat.getColor(this, R.color.bucovina_card_bg);
        
        // Set status bar color
        getWindow().setStatusBarColor(primaryColor);
        
        // Apply to text elements
        if (questionTextView != null) {
            questionTextView.setTextColor(textColor);
        }
        if (scoreTextView != null) {
            scoreTextView.setTextColor(textColor);
        }
        if (streakTextView != null) {
            streakTextView.setTextColor(textColor);
        }
        if (factTextView != null) {
            factTextView.setTextColor(textColor);
        }
        
        // Apply color to progress bar
        if (progressBar != null) {
            progressBar.setProgressTintList(ContextCompat.getColorStateList(this, R.color.bucovina_primary));
            progressBar.setProgressBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_background));
        }
        
        // Apply to answer cards and buttons
        for (int i = 0; i < answerCards.length; i++) {
            if (answerCards[i] != null) {
                answerCards[i].setCardBackgroundColor(cardBgColor);
                answerCards[i].setStrokeColor(primaryLightColor);
            }
            if (i < answerButtons.length && answerButtons[i] != null) {
                answerButtons[i].setTextColor(textColor);
                answerButtons[i].setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_card_bg));
            }
        }
        
        // Apply to finish button
        if (finishButton != null) {
            finishButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_primary));
            finishButton.setTextColor(ContextCompat.getColor(this, R.color.white));
            finishButton.setStrokeColor(ContextCompat.getColorStateList(this, R.color.bucovina_primary_light));
        }
    }

    private void applyButtonStyles() {
        // Style buttons for Bucovina theme - with null checks
        for (int i = 0; i < answerButtons.length; i++) {
            if (i < answerButtons.length && answerButtons[i] != null && i < answerCards.length && answerCards[i] != null) {
            MaterialButton button = answerButtons[i];
            MaterialCardView card = answerCards[i];
            
            // Enable ripple effect for card
            card.setClickable(true);
            card.setFocusable(true);
            
            // Add press animation with Bucovina colors
            card.setRippleColor(ContextCompat.getColorStateList(this, R.color.bucovina_primary_light));
            }
        }
        
        // Add visual effects for finish button - with null check
        if (finishButton != null) {
        finishButton.setRippleColor(ContextCompat.getColorStateList(this, R.color.bucovina_accent));
        }
    }

    private void setupLifelines() {
        // Add lifeline functionality - with null checks
        if (fiftyFiftyButton != null) {
        fiftyFiftyButton.setOnClickListener(v -> {
            // Add visual effect on press
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            useFiftyFifty();
        });
        }
        
        if (skipQuestionButton != null) {
        skipQuestionButton.setOnClickListener(v -> {
            // Add visual effect on press
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            skipQuestion();
        });
        }
    }
    
    /**
     * Provides haptic feedback for user actions
     */
    private void performHapticFeedback(HapticFeedbackType type) {
        try {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
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
            Log.w(TAG, "Could not perform haptic feedback", e);
        }
    }
    
    /**
     * Use hint lifeline - provides a clue about the correct answer
     */
    private void useHint() {
        if (isHintUsed || enhancedQuestions == null || currentQuestionIndex >= enhancedQuestions.size()) {
            return;
        }
        
        performHapticFeedback(HapticFeedbackType.LIFELINE);
        
        // Mark hint as used
        isHintUsed = true;
        // hintButton is not initialized in initializeViews, so this line is removed.
        // if (hintButton != null) {
        //     hintButton.setEnabled(false);
        //     hintButton.setAlpha(0.5f);
        // }
        
        // Get current question
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        
        // Create hint based on question category or eliminate one wrong answer
        String hintText = "Indiciu: ";
        switch (currentQuestion.getCategory()) {
            case HISTORY:
                hintText += "Gândește-te la evenimentele istorice importante din Bucovina.";
                break;
            case GEOGRAPHY:
                hintText += "Consideră locația geografică și caracteristicile naturale ale Bucovinei.";
                break;
            case CULTURE:
                hintText += "Reflectă asupra tradițiilor și obiceiurilor specifice Bucovinei.";
                break;
            default:
                hintText += "Analizează cu atenție toate opțiunile și elimină pe cele care nu par corecte.";
        }
        
        // Show hint dialog
        new MaterialAlertDialogBuilder(this)
                .setTitle("💡 Indiciu")
                .setMessage(hintText)
                .setPositiveButton("Am înțeles", null)
                .show();
                
        Log.d(TAG, "Hint used for question " + currentQuestionIndex);
    }
    
    /**
     * Show quit confirmation dialog
     */
    private void showQuitConfirmation() {
        int questionsAnswered = currentQuestionIndex;
        
        String message = String.format(
            "Ești sigur că vrei să ieși?\n\n" +
            "📊 Progres actual:\n" +
            "• Întrebări răspunse: %d/%d\n" +
            "• Scor actual: %d puncte\n" +
            "• Răspunsuri corecte: %d\n\n" +
            "Progresul va fi pierdut!",
            questionsAnswered, totalQuestions, score, correctAnswers
        );
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("🚪 Ieșire din Quiz")
                .setMessage(message)
                .setPositiveButton("Da, ieși", (dialog, which) -> {
                    performHapticFeedback(HapticFeedbackType.LIFELINE);
                    finish();
                })
                .setNegativeButton("Continuă", (dialog, which) -> {
                    // Continue playing
                })
                .show();
    }

    private void startTimer() {
        // Cancel existing timer if running
        if (timer != null) {
            timer.cancel();
        }
        
        questionStartTime = System.currentTimeMillis();
        
        // Start a new timer from TIME_PER_QUESTION milliseconds
        timerTextView.setText(String.valueOf(TIME_PER_QUESTION / 1000));
        
        timer = new CountDownTimer(TIME_PER_QUESTION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                timerTextView.setText(String.valueOf(seconds));
                
                // Apply animation when time is running low
                if (seconds <= 5) {
                    timerTextView.setTextColor(ContextCompat.getColor(BucovinaGameActivity.this, R.color.bucovina_incorrect_answer));
                } else {
                    timerTextView.setTextColor(ContextCompat.getColor(BucovinaGameActivity.this, R.color.bucovina_text));
                }
            }
            
            @Override
            public void onFinish() {
                timerTextView.setText("0");
                handleTimeout();
            }
        }.start();
    }

    private void handleTimeout() {
        // Disable answer cards
        for (MaterialCardView card : answerCards) {
            card.setClickable(false);
            card.setEnabled(false);
        }
        
        // Highlight the correct answer
        QuestionModel currentQuestion = questions.get(currentQuestionIndex);
        int correctAnswerIndex = currentQuestion.getCorrectAnswerIndex();
        
        // Style correct answer
        MaterialCardView correctCard = answerCards[correctAnswerIndex];
        MaterialButton correctButton = answerButtons[correctAnswerIndex];
        
        correctCard.setStrokeColor(ContextCompat.getColor(this, R.color.bucovina_correct_answer));
        correctCard.setStrokeWidth(4);
        correctButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_correct_answer));
        correctButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        
        Toast.makeText(this, "Timpul a expirat!", Toast.LENGTH_SHORT).show();
        
        // Show fact if available
        if (currentQuestion.getFact() != null && !currentQuestion.getFact().isEmpty()) {
            factTextView.setText(currentQuestion.getFact());
            factTextView.setVisibility(View.VISIBLE);
        }
        
        // Reset streak
        streak = 0;
        
        // Move to next question after delay
        new Handler().postDelayed(() -> {
            currentQuestionIndex++;
            if (currentQuestionIndex < questions.size()) {
                displayQuestion();
            } else {
                showFinishButton();
            }
        }, 2000);
    }

    private void useFiftyFifty() {
        if (isFiftyFiftyUsed || currentQuestionIndex >= questions.size()) {
            Toast.makeText(this, "Ai folosit deja opțiunea 50:50!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Mark lifeline as used
        isFiftyFiftyUsed = true;
        fiftyFiftyButton.setEnabled(false);
        fiftyFiftyButton.setAlpha(0.5f);
        
        // Get current question
        QuestionModel currentQuestion = questions.get(currentQuestionIndex);
        int correctAnswerIndex = currentQuestion.getCorrectAnswerIndex();
        
        // Find wrong answers to hide
        List<Integer> wrongAnswerIndices = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (i != correctAnswerIndex) {
                wrongAnswerIndices.add(i);
            }
        }
        
        // Randomly select two wrong answers to hide
        Collections.shuffle(wrongAnswerIndices);
        List<Integer> answersToHide = wrongAnswerIndices.subList(0, 2);
        
        // Hide selected wrong answers
        for (int index : answersToHide) {
            if (index < answerCards.length) {
                answerCards[index].setVisibility(View.INVISIBLE);
                answerCards[index].setClickable(false);
                answerCards[index].setEnabled(false);
            }
        }
        
        Toast.makeText(this, "Două răspunsuri greșite au fost eliminate!", Toast.LENGTH_SHORT).show();
    }
    
    private void skipQuestion() {
        if (isSkipUsed || currentQuestionIndex >= questions.size()) {
            Toast.makeText(this, "Ai folosit deja opțiunea de a sări peste o întrebare!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Mark lifeline as used
        isSkipUsed = true;
        skipQuestionButton.setEnabled(false);
        skipQuestionButton.setAlpha(0.5f);
        
        // Cancel timer
        if (timer != null) {
            timer.cancel();
        }
        
        Toast.makeText(this, "Întrebare omisă!", Toast.LENGTH_SHORT).show();
        
        // Move to next question
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            displayQuestion();
        } else {
            showFinishButton();
        }
    }
    
    private void initializeQuestions() {
        // Smart data source selection with user preference dialog
        showDataSourceSelectionDialog();
    }
    
    /**
     * Show dialog for data source selection with intelligent caching
     */
    private void showDataSourceSelectionDialog() {
        android.content.SharedPreferences prefs = getSharedPreferences("BucovinaGamePrefs", MODE_PRIVATE);
        String savedDataSource = prefs.getString("preferredDataSource", null);
        
        // If user has saved preference, use it directly
        if (savedDataSource != null) {
            if ("online".equals(savedDataSource)) {
                loadQuestionsFromOnlineSource();
            } else {
                loadQuestionsFromLocalSource();
            }
            return;
        }
        
        // Show selection dialog for first-time users
        new MaterialAlertDialogBuilder(this)
                .setTitle("🌐 Selectează sursa întrebărilor")
                .setMessage("Alege sursa de întrebări pentru Quiz-ul Bucovina:\n\n" +
                           "📱 Local: Întrebări integrate în aplicație (rapid)\n" +
                           "☁️ Online: Întrebări actualizate din cloud (mai multe opțiuni)")
                .setPositiveButton("☁️ Online (Recomandat)", (dialog, which) -> {
                    // Save preference
                    prefs.edit().putString("preferredDataSource", "online").apply();
                    loadQuestionsFromOnlineSource();
                })
                .setNegativeButton("📱 Local", (dialog, which) -> {
                    prefs.edit().putString("preferredDataSource", "local").apply();
                    loadQuestionsFromLocalSource();
                })
                .setNeutralButton("📋 Întreabă mereu", (dialog, which) -> {
                    // Don't save preference - ask every time
                    loadQuestionsFromOnlineSource(); // Default to online
                })
                .setCancelable(false)
                .show();
    }
    
    /**
     * Load questions from Firestore with local fallback
     */
    private void loadQuestionsFromOnlineSource() {
        // Fallback to local questions if online source is not available
        Log.d(TAG, "Online source not available, using local questions");
        loadQuestionsFromLocalSource();
    }
    
    /**
     * Load questions from local cache/static data
     */
    private void loadQuestionsFromLocalSource() {
        Log.d(TAG, "Loading questions from local source for Bucovina");
        
        questions = new ArrayList<>();
        
        // Bucovina-specific local questions with enhanced data
        questions.add(new QuestionModel(
            "Care este capitala județului Suceava?",
            "Suceava", 
            Arrays.asList("Rădăuți", "Câmpulung Moldovenesc", "Fălticeni"), 
            R.drawable.suceava, 
            "Suceava este capitala județului Suceava și fost o importantă cetate medievală în timpul lui Ștefan cel Mare."));
        
        questions.add(new QuestionModel(
            "În ce an a fost construită Mănăstirea Voroneț?",
            "1488", 
            Arrays.asList("1476", "1504", "1527"), 
            R.drawable.manastire_voronet, 
            "Mănăstirea Voroneț, cunoscută ca 'Capela Sixtină a Orientului', a fost construită în 1488 de Ștefan cel Mare."));
        
        questions.add(new QuestionModel(
            "Ce culoare este specifică frescelor exterioare de la Voroneț?",
            "Albastru de Voroneț", 
            Arrays.asList("Verde", "Roșu", "Galben"), 
            R.drawable.manastire_voronet, 
            "Albastru de Voroneț este o nuanță unică de albastru, devenită celebră în întreaga lume."));
        
        questions.add(new QuestionModel(
            "Care dintre următoarele mănăstiri NU se află în Bucovina?",
            "Mănăstirea Cozia", 
            Arrays.asList("Mănăstirea Putna", "Mănăstirea Sucevița", "Mănăstirea Humor"), 
            R.drawable.cozia, 
            "Mănăstirea Cozia se află în Oltenia, nu în Bucovina. Celelalte sunt celebre mănăstiri bucovinene."));
        
        questions.add(new QuestionModel(
            "Cine a fost domnitorul care a ctitorit Mănăstirea Putna?",
            "Ștefan cel Mare", 
            Arrays.asList("Alexandru cel Bun", "Petru Rareș", "Mihai Viteazul"), 
            R.drawable.manastirea_putna, 
            "Ștefan cel Mare a ctitorit Mănăstirea Putna în 1466, unde se află și mormântul său."));
        
        questions.add(new QuestionModel(
            "Care este cea mai înaltă vârful montan din Bucovina?",
            "Vârful Pietrosul Călimanilor", 
            Arrays.asList("Vârful Rarău", "Vârful Giumalău", "Vârful Suhard"), 
            R.drawable.varful_pietros, 
            "Vârful Pietrosul din Munții Călimanului, cu 2022m, este cel mai înalt vârf din Bucovina."));
        
        questions.add(new QuestionModel(
            "Care este obiceiul tradițional de iarnă specific Bucovinei?",
            "Urșii", 
            Arrays.asList("Capra", "Căiuții", "Malanca"), 
            R.drawable.bucovina, 
            "Jocul Urșilor este un obicei specific Bucovinei, în care tinerii colindă mascați ca urși."));
        
        questions.add(new QuestionModel(
            "Care dintre următoarele localități era cunoscută ca 'Mica Vienă'?",
            "Cernăuți", 
            Arrays.asList("Suceava", "Rădăuți", "Gura Humorului"), 
            R.drawable.mica_viena, 
            "Cernăuți, în timpul Austro-Ungariei, era cunoscută ca 'Mica Vienă' pentru arhitectura sa."));
        
        questions.add(new QuestionModel(
            "Ce tehnică artizanală este renumită în Bucovina?",
            "Încondeierea ouălor", 
            Arrays.asList("Țesutul covoarelor", "Sculptatul în lemn", "Olăritul"), 
            R.drawable.inc_oua, 
            "Încondeierea ouălor este o artă tradițională bucovinească cu modele geometrice complexe."));
        
        questions.add(new QuestionModel(
            "În ce perioadă a fost Bucovina parte din Imperiul Austro-Ungar?",
            "1775-1918", 
            Arrays.asList("1812-1918", "1699-1859", "1821-1918"), 
            R.drawable.imp_has, 
            "Bucovina a fost anexată de Austro-Ungaria în 1775 și a rămas sub administrația lor până în 1918."));
        
        finalizeQuestionsLoading();
    }
    
    /**
     * Finalize questions loading process
     */
    private void finalizeQuestionsLoading() {
        if (questions != null && !questions.isEmpty()) {
            Collections.shuffle(questions);
            if (questions.size() > numQuestions) {
                questions = new ArrayList<>(questions.subList(0, numQuestions));
            }
        }
        totalQuestions = questions != null ? questions.size() : 0;
        convertToEnhancedQuestions();
        Log.d(TAG, "Questions loading finalized - " + totalQuestions + " total questions for Bucovina");
        runOnUiThread(() -> {
            displayQuestion();
        });
    }
    
    /**
     * Convert regular questions to enhanced questions for advanced features
     */
    private void convertToEnhancedQuestions() {
        enhancedQuestions = new ArrayList<>();
        
        for (QuestionModel question : questions) {
            // Infer category from question content for Bucovina
            EnhancedQuestionModel.Category category = inferCategoryFromQuestion(question.getQuestion());
            
            // Infer difficulty from question complexity
            EnhancedQuestionModel.Difficulty difficulty = inferDifficultyFromQuestion(question.getQuestion());
            
            // Create enhanced question
            List<String> allAnswers = new ArrayList<>();
            allAnswers.add(question.getCorrectAnswer());
            allAnswers.addAll(question.getIncorrectAnswers());
            
            EnhancedQuestionModel enhanced = new EnhancedQuestionModel(
                question.getQuestion(),
                question.getCorrectAnswer(),
                question.getIncorrectAnswers(),
                0, // imageResourceId
                question.getFact(),
                category,
                difficulty,
                null // tags
            );
            
            enhancedQuestions.add(enhanced);
        }
        
        // Apply game mode filtering if we have a game mode manager
        if (gameModeManager != null) {
            enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
        }
        // Limitez la numQuestions după filtrare
        if (enhancedQuestions.size() > numQuestions) {
            enhancedQuestions = new ArrayList<>(enhancedQuestions.subList(0, numQuestions));
        }
        
        Log.d(TAG, "Converted " + enhancedQuestions.size() + " questions to enhanced format for Bucovina");
    }
    
    private EnhancedQuestionModel.Category inferCategoryFromQuestion(String questionText) {
        String text = questionText.toLowerCase();
        
        if (text.contains("an") || text.contains("perioad") || text.contains("constru") || 
            text.contains("dominitor") || text.contains("domn") || text.contains("stefan") ||
            text.contains("habsburgic") || text.contains("istoric")) {
            return EnhancedQuestionModel.Category.HISTORY;
        } else if (text.contains("munte") || text.contains("varful") || text.contains("inaltime") || 
                   text.contains("localitat") || text.contains("capita") || text.contains("oras")) {
            return EnhancedQuestionModel.Category.GEOGRAPHY;
        } else if (text.contains("manastir") || text.contains("culoare") || text.contains("fresc") || 
                   text.contains("traditi") || text.contains("obicei") || text.contains("tehnic") ||
                   text.contains("artizanal") || text.contains("incondei") || text.contains("ursi")) {
            return EnhancedQuestionModel.Category.CULTURE;
        } else {
            return EnhancedQuestionModel.Category.GENERAL;
        }
    }
    
    private EnhancedQuestionModel.Difficulty inferDifficultyFromQuestion(String questionText) {
        String text = questionText.toLowerCase();
        
        // Hard questions - require specific historical knowledge
        if (text.contains("imperiul habsburgic") || text.contains("1775-1918") || 
            text.contains("petru rares") || text.contains("pietro") || text.contains("vaslui")) {
            return EnhancedQuestionModel.Difficulty.HARD;
        }
        // Medium questions - require good knowledge
        else if (text.contains("stefan cel mare") || text.contains("voronet") || 
                 text.contains("putna") || text.contains("1488") || text.contains("cernauti")) {
            return EnhancedQuestionModel.Difficulty.MEDIUM;
        }
        // Easy questions - basic knowledge
        else {
            return EnhancedQuestionModel.Difficulty.EASY;
        }
    }
    
    private void displayQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            showFinishButton();
            return;
        }
        
        // Reset answer selected flag
        answerSelected = false;

        // Get the current question
        QuestionModel currentQuestion = questions.get(currentQuestionIndex);
        
        // Reset UI elements
        resetCardStyles();
        factTextView.setVisibility(View.GONE);
        
        // Set question text
        questionTextView.setText(currentQuestion.getQuestion());
        
        // Set answer options
        List<String> answers = currentQuestion.getAnswers();
        for (int i = 0; i < Math.min(answers.size(), answerButtons.length); i++) {
            answerButtons[i].setText(answers.get(i));
        }
        
        // Set question image if available
        if (currentQuestion.getImageResourceId() != 0) {
            Glide.with(this)
                .load(currentQuestion.getImageResourceId())
                .centerCrop()
                .into(questionImage);
            questionImageCard.setVisibility(View.VISIBLE);
        } else {
            questionImageCard.setVisibility(View.GONE);
        }
        
        // Enable answer cards for interaction
        for (MaterialCardView card : answerCards) {
            card.setEnabled(true);
            card.setClickable(true);
            card.setVisibility(View.VISIBLE);
        }
        
        // Update progress
        if (progressBar != null) {
            progressBar.setMax(questions.size());
            progressBar.setProgress(currentQuestionIndex + 1);
        }
        
        // Set question start time for tracking
        questionStartTime = System.currentTimeMillis();
        
        // Start or restart the timer
        startTimer();
        
        // Update score display
        updateScore();
    }
    
    private void resetCardStyles() {
        // Reset card background colors
        for (int i = 0; i < answerCards.length; i++) {
            if (answerCards[i] != null) {
                answerCards[i].setCardBackgroundColor(ContextCompat.getColor(this, R.color.bucovina_card_bg));
                answerCards[i].setStrokeColor(ContextCompat.getColor(this, R.color.bucovina_primary));
                answerCards[i].setStrokeWidth(2);
                answerCards[i].setEnabled(true);
                answerCards[i].setClickable(true);
            }
            
            if (i < answerButtons.length && answerButtons[i] != null) {
                answerButtons[i].setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_card_bg));
                answerButtons[i].setTextColor(ContextCompat.getColor(this, R.color.bucovina_text));
            }
        }
    }
    
    private void checkAnswer(int selectedAnswerIndex, String selectedAnswer) {
        if (answerSelected || currentQuestionIndex >= questions.size() || !answerCards[selectedAnswerIndex].isEnabled()) {
            return;
        }
        answerSelected = true;

        // Get current question
        QuestionModel currentQuestion = questions.get(currentQuestionIndex);
        int correctAnswerIndex = currentQuestion.getCorrectAnswerIndex();

        // Stop timer and calculate response time
        if (timer != null) {
            timer.cancel();
        }
        long questionTime = System.currentTimeMillis() - questionStartTime;
        totalTime += questionTime;

        // Disable all answer cards to prevent multiple answers
        for (MaterialCardView card : answerCards) {
            card.setEnabled(false);
            card.setClickable(false);
        }

        // Highlight the selected answer
        MaterialCardView selectedCard = answerCards[selectedAnswerIndex];
        MaterialButton selectedButton = answerButtons[selectedAnswerIndex];

        // Highlight the correct answer
        MaterialCardView correctCard = answerCards[correctAnswerIndex];
        MaterialButton correctButton = answerButtons[correctAnswerIndex];

        // Style correct answer
        correctCard.setStrokeColor(ContextCompat.getColor(this, R.color.bucovina_correct_answer));
        correctCard.setStrokeWidth(4);
        correctButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_correct_answer));
        correctButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        // Check if selected answer is correct
        boolean isCorrect = selectedAnswerIndex == correctAnswerIndex;

        if (!isCorrect) {
            // Style wrong answer
            selectedCard.setStrokeColor(ContextCompat.getColor(this, R.color.bucovina_incorrect_answer));
            selectedCard.setStrokeWidth(4);
            selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_incorrect_answer));
            selectedButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            performHapticFeedback(HapticFeedbackType.WRONG);
            streak = 0;
            if (progressTracker != null && enhancedQuestions != null && currentQuestionIndex < enhancedQuestions.size()) {
                EnhancedQuestionModel enhancedQuestion = enhancedQuestions.get(currentQuestionIndex);
                progressTracker.trackAnswer(enhancedQuestion, isCorrect, questionTime);
            }
        } else {
            correctAnswers++;
            performHapticFeedback(HapticFeedbackType.CORRECT);
            // --- SCOR PROGRESIV AJUSTAT MAI MIC ---
            int basePoints = POINTS_PER_CORRECT_ANSWER;
            int timeBonus = (int) ((TIME_PER_QUESTION - questionTime) / 4000); // 0.25 puncte/sec rămas
            if (timeBonus < 0) timeBonus = 0;
            int modeBonus = gameModeManager != null ? gameModeManager.calculateModeBonus(basePoints, isCorrect, questionTime) : 0;
            int questionScore = basePoints + timeBonus + modeBonus;
            if (difficultyManager != null) {
                questionScore = difficultyManager.calculateFinalScore(questionScore);
            }
            streak++;
            if (streak > maxStreak) maxStreak = streak;
            if (streak >= STREAK_BONUS_THRESHOLD) {
                questionScore += BONUS_POINTS;
                Toast.makeText(this, "Bonus pentru " + streak + " răspunsuri consecutive!", Toast.LENGTH_SHORT).show();
            }
            score += questionScore;
            updateScore();
            updateStreak();
            Toast.makeText(this, "+" + questionScore + " puncte!", Toast.LENGTH_SHORT).show();
            if (progressTracker != null && enhancedQuestions != null && currentQuestionIndex < enhancedQuestions.size()) {
                EnhancedQuestionModel enhancedQuestion = enhancedQuestions.get(currentQuestionIndex);
                progressTracker.trackAnswer(enhancedQuestion, isCorrect, questionTime);
            }
        }

        // Show fact if available
        if (currentQuestion.getFact() != null && !currentQuestion.getFact().isEmpty()) {
            factTextView.setText(currentQuestion.getFact());
            factTextView.setVisibility(View.VISIBLE);
        }

        // Move to next question after delay
        new Handler().postDelayed(() -> {
            currentQuestionIndex++;
            if (currentQuestionIndex < questions.size()) {
                displayQuestion();
            } else {
                showFinishButton();
            }
        }, 2000);
    }
    
    private void highlightCorrectAnswer() {
        QuestionModel currentQuestion = questions.get(currentQuestionIndex);
        String correctAnswer = currentQuestion.getCorrectAnswer();
        for (MaterialCardView card : answerCards) {
            if (card != null && card.getCardBackgroundColor().getDefaultColor() == ContextCompat.getColor(this, R.color.bucovina_correct_answer)) {
                card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bucovina_correct_answer));
            }
        }
        for (MaterialButton button : answerButtons) {
            if (button != null && button.getText().toString().equals(correctAnswer)) {
                button.setTextColor(ContextCompat.getColor(this, R.color.bucovina_correct_answer));
            }
        }
    }
    
    private void moveToNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            displayQuestion();
            startTimer();
        } else {
            finishGame();
        }
    }
    
    private void updateScore() {
        if (scoreTextView != null) {
        scoreTextView.setText(String.format("%d", score));
        }
    }
    
    private void updateStreak() {
        if (streakTextView != null) {
        streakTextView.setText(String.format("%d", streak));
        }
    }
    
    private String getAchievements() {
        StringBuilder achievements = new StringBuilder();
        
        // Calculate accuracy
        float accuracy = 0;
        if (totalQuestions > 0) {
            accuracy = ((float) correctAnswers / totalQuestions) * 100;
        }
        
        // Award achievements based on performance
        if (accuracy >= 90) {
            achievements.append("⭐ Expert în Bucovina!\n");
        } else if (accuracy >= 70) {
            achievements.append("⭐ Cunoscător al Bucovinei\n");
        }
        
        if (maxStreak >= 5) {
            achievements.append("🔥 Serie impresionantă: ").append(maxStreak).append(" răspunsuri corecte consecutive\n");
        }
        
        if (correctAnswers == totalQuestions) {
            achievements.append("🏆 Perfect! Toate răspunsurile corecte!\n");
        }
        
        if (achievements.length() == 0) {
            achievements.append("Continuă să înveți despre Bucovina!");
        }
        
        return achievements.toString();
    }
    
    private void finishGame() {
        if (timer != null) {
            timer.cancel();
        }
        
        // Calculate final statistics
        long averageTimePerQuestion = totalQuestions > 0 ? totalTime / totalQuestions : 0;
        float accuracy = totalQuestions > 0 ? ((float) correctAnswers / totalQuestions) * 100 : 0;
        
        // Update enhanced systems with final results
        if (progressTracker != null) {
            String gameModeName = gameModeManager != null ? 
                gameModeManager.getCurrentGameMode().displayName : "Quiz Clasic";
            progressTracker.finishGame(gameModeName, correctAnswers, totalQuestions, totalTime);
        }
        
        if (difficultyManager != null) {
            difficultyManager.updateDifficultyAfterGame(correctAnswers, totalQuestions, totalTime);
        }
        
        // Save result to leaderboard with enhanced data
        saveGameResultToLeaderboard(score, correctAnswers, totalQuestions, averageTimePerQuestion, accuracy);
        
        // Send result back to calling activity and to GameOverActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("GAME_SCORE", score);
        setResult(RESULT_OK, resultIntent);
        
        // Lansare CrisanaGameOverActivity ca la Crișana
        Intent intent = new Intent(this, com.example.myapplication.crisanausage.CrisanaGameOverActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("totalQuestions", totalQuestions);
        intent.putExtra("correctAnswers", correctAnswers);
        intent.putExtra("maxStreak", maxStreak);
        intent.putExtra("accuracy", accuracy);
        intent.putExtra("averageTime", averageTimePerQuestion);
        startActivity(intent);
        
        // Also add points directly to PointsManager
        pointsManager.addPoints(this, "bucovina", score);
        
        finish();
    }
    
    /**
     * Save game result to Firestore leaderboard with enhanced statistics
     */
    private void saveGameResultToLeaderboard(int finalScore, int correctAnswers, int totalQuestions, 
                                           long averageTime, float accuracy) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        
        if (currentUser == null) {
            Log.w(TAG, "User not authenticated, skipping leaderboard save");
            return;
        }
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Prepare result data
        Map<String, Object> result = new HashMap<>();
        result.put("userId", currentUser.getUid());
        result.put("userName", currentUser.getDisplayName() != null ? 
                   currentUser.getDisplayName() : "Utilizator");
        result.put("score", finalScore);
        result.put("correctAnswers", correctAnswers);
        result.put("totalQuestions", totalQuestions);
        result.put("accuracy", accuracy);
        result.put("averageTimePerQuestion", averageTime);
        result.put("maxStreak", maxStreak);
        result.put("region", "bucovina");
        result.put("gameMode", gameModeManager != null ? 
                   gameModeManager.getCurrentGameMode().name() : "CLASSIC");
        result.put("difficulty", difficultyManager != null ? 
                   difficultyManager.getCurrentDifficulty().name() : "MEDIUM");
        result.put("timestamp", FieldValue.serverTimestamp());
        result.put("gameType", GAME_TYPE);
        
        // Save to main quiz_results collection
        db.collection("quiz_results")
                .add(result)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Game result saved to leaderboard: " + documentReference.getId());
                    updateUserProfile(currentUser.getUid(), finalScore, accuracy);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving game result to leaderboard", e);
                });
    }
    
    /**
     * Update user profile with latest game statistics
     */
    private void updateUserProfile(String userId, int score, float accuracy) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> profileUpdate = new HashMap<>();
        profileUpdate.put("lastGameScore", score);
        profileUpdate.put("lastGameAccuracy", accuracy);
        profileUpdate.put("lastGameRegion", "bucovina");
        profileUpdate.put("lastGameTimestamp", FieldValue.serverTimestamp());
        profileUpdate.put("gamesPlayed", FieldValue.increment(1));
        
        // Update best score if this is better
        db.collection("user_profiles").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long currentBestScore = documentSnapshot.getLong("bestScoreBucovina");
                        if (currentBestScore == null || score > currentBestScore) {
                            profileUpdate.put("bestScoreBucovina", score);
                        }
                    } else {
                        profileUpdate.put("bestScoreBucovina", score);
                    }
                    
                    // Apply the update
                    db.collection("user_profiles").document(userId)
                            .set(profileUpdate, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "User profile updated successfully");
                                updateUserActivityHistory(userId, score, accuracy);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating user profile", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking current best score", e);
                });
    }
    
    /**
     * Update user activity history with recent game
     */
    private void updateUserActivityHistory(String userId, int score, float accuracy) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> activity = new HashMap<>();
        activity.put("type", "quiz_completed");
        activity.put("region", "bucovina");
        activity.put("score", score);
        activity.put("accuracy", accuracy);
        activity.put("timestamp", System.currentTimeMillis()); // Folosesc timestamp local, nu FieldValue.serverTimestamp()
        activity.put("correctAnswers", correctAnswers);
        activity.put("totalQuestions", totalQuestions);
        
        db.collection("user_profiles").document(userId)
                .update("recentActivities", FieldValue.arrayUnion(activity))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User activity history updated");
                    // Limit recent activities to last 20 entries
                    limitUserActivityHistory(userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user activity history", e);
                });
    }
    
    /**
     * Limit user activity history to prevent unlimited growth
     */
    private void limitUserActivityHistory(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("user_profiles").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> activities = 
                            (List<Map<String, Object>>) documentSnapshot.get("recentActivities");
                        
                        if (activities != null && activities.size() > 20) {
                            // Keep only the last 20 activities
                            List<Map<String, Object>> limitedActivities = 
                                activities.subList(Math.max(0, activities.size() - 20), activities.size());
                            
                            db.collection("user_profiles").document(userId)
                                    .update("recentActivities", limitedActivities)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "User activity history limited to 20 entries");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error limiting user activity history", e);
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking user activity history size", e);
                });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Check for question updates when resuming activity
        checkForQuestionUpdates();
    }
    
    /**
     * Check for question updates and refresh if needed
     */
    private void checkForQuestionUpdates() {
        // Simplified version without syncManager
        Log.d(TAG, "Question updates check simplified - using local questions");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
    
    // --- DIALOG INIȚIAL PENTRU SURSA ȘI NUMĂRUL DE ÎNTREBĂRI ---
    private void showInitialSetupDialog() {
        boolean hasInternet = isInternetAvailable();
        boolean hasLocalCache = checkIfLocalCacheExists();
        String[] sources;
        if (hasInternet && hasLocalCache) {
            sources = new String[]{"🌐 Baza de Date", "📱 Cache Local", "🎯 Automat"};
        } else if (hasInternet) {
            sources = new String[]{"🌐 Baza de Date"};
        } else if (hasLocalCache) {
            sources = new String[]{"📱 Cache Local"};
        } else {
            sources = new String[]{"❌ Nicio sursă disponibilă"};
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quiz_setup, null);
        Spinner sourceSpinner = dialogView.findViewById(R.id.sourceSpinner);
        Spinner numQuestionsSpinner = dialogView.findViewById(R.id.numQuestionsSpinner);

        ArrayAdapter<String> sourceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sources);
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sourceSpinner.setAdapter(sourceAdapter);

        Integer[] numOptions = new Integer[]{5, 10, 15, 20, 30, 50};
        ArrayAdapter<Integer> numAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, numOptions);
        numAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numQuestionsSpinner.setAdapter(numAdapter);
        numQuestionsSpinner.setSelection(1); // default 10

        new MaterialAlertDialogBuilder(this)
                .setTitle("Setare Quiz Bucovina")
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("Start", (dialog, which) -> {
                    int sourceIdx = sourceSpinner.getSelectedItemPosition();
                    int numIdx = numQuestionsSpinner.getSelectedItemPosition();
                    numQuestions = numOptions[numIdx];
                    if (sources[sourceIdx].contains("Baza de Date")) {
                        saveDataSourcePreference("always_database");
                        loadQuestionsFromDatabaseHybrid(numQuestions);
                    } else if (sources[sourceIdx].contains("Cache Local")) {
                        saveDataSourcePreference("always_cache");
                        loadQuestionsFromLocalCacheHybrid(numQuestions);
                    } else {
                        saveDataSourcePreference("auto");
                        loadQuestionsHybrid(numQuestions);
                    }
                })
                .show();
    }

    // --- METODE HIBRID ---
    private boolean isInternetAvailable() {
        // Implement your internet check logic here
        // For example, using a library like Volley or OkHttp
        // This is a placeholder for actual implementation
        return true; // Assume internet is available for now
    }

    private boolean checkIfLocalCacheExists() {
        android.content.SharedPreferences prefs = getSharedPreferences("BucovinaGamePrefs", MODE_PRIVATE);
        String cachedJson = prefs.getString(CACHE_KEY, null);
        long timestamp = prefs.getLong(CACHE_TIMESTAMP_KEY, 0);
        boolean notExpired = (System.currentTimeMillis() - timestamp) < CACHE_EXPIRY_MS;
        return cachedJson != null && !cachedJson.isEmpty() && notExpired;
    }

    private void saveDataSourcePreference(String pref) {
        android.content.SharedPreferences prefs = getSharedPreferences("BucovinaGamePrefs", MODE_PRIVATE);
        prefs.edit().putString(DATA_SOURCE_PREF_KEY, pref).apply();
    }

    private void saveQuestionsToLocalCacheHybrid(List<QuestionModel> questions) {
        android.content.SharedPreferences prefs = getSharedPreferences("BucovinaGamePrefs", MODE_PRIVATE);
        com.google.gson.Gson gson = new com.google.gson.Gson();
        String json = gson.toJson(questions);
        prefs.edit().putString(CACHE_KEY, json).putLong(CACHE_TIMESTAMP_KEY, System.currentTimeMillis()).apply();
    }

    private void loadQuestionsHybrid(int numQuestions) {
        String pref = getSharedPreferences("BucovinaGamePrefs", MODE_PRIVATE).getString(DATA_SOURCE_PREF_KEY, "auto");
        if (pref.equals("always_database")) {
            loadQuestionsFromDatabaseHybrid(numQuestions);
        } else if (pref.equals("always_cache")) {
            loadQuestionsFromLocalCacheHybrid(numQuestions);
        } else {
            // Automat: dacă există internet, încearcă baza de date, altfel cache
            if (isInternetAvailable()) {
                loadQuestionsFromDatabaseHybrid(numQuestions);
            } else if (checkIfLocalCacheExists()) {
                loadQuestionsFromLocalCacheHybrid(numQuestions);
            } else {
                showNoInternetForPreferredDatabaseDialogHybrid(numQuestions);
            }
        }
    }

    private void loadQuestionsFromDatabaseHybrid(int numQuestions) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("regions").document(REGION)
                .collection("games").document(GAME_TYPE)
                .collection("questions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<QuestionModel> loadedQuestions = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            QuestionModel q = doc.toObject(QuestionModel.class);
                            if (q != null) loadedQuestions.add(q);
                        } catch (Exception e) {
                            // skip invalid
                        }
                    }
                    if (!loadedQuestions.isEmpty()) {
                        Collections.shuffle(loadedQuestions);
                        List<QuestionModel> limitedQuestions = loadedQuestions.subList(0, Math.min(numQuestions, loadedQuestions.size()));
                        questions = new ArrayList<>(limitedQuestions);
                        saveQuestionsToLocalCacheHybrid(questions);
                        finalizeQuestionsLoading();
                    } else {
                        loadQuestionsFromLocalCacheHybrid(numQuestions);
                    }
                })
                .addOnFailureListener(e -> {
                    loadQuestionsFromLocalCacheHybrid(numQuestions);
                });
    }

    private void loadQuestionsFromLocalCacheHybrid(int numQuestions) {
        android.content.SharedPreferences prefs = getSharedPreferences("BucovinaGamePrefs", MODE_PRIVATE);
        String cachedJson = prefs.getString(CACHE_KEY, null);
        if (cachedJson != null && !cachedJson.isEmpty()) {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<QuestionModel>>(){}.getType();
            List<QuestionModel> cachedQuestions = gson.fromJson(cachedJson, type);
            if (cachedQuestions != null && !cachedQuestions.isEmpty()) {
                Collections.shuffle(cachedQuestions);
                questions = cachedQuestions.subList(0, Math.min(numQuestions, cachedQuestions.size()));
                finalizeQuestionsLoading();
                return;
            }
        }
        showNoCacheForPreferredLocalDialogHybrid(numQuestions);
    }

    private void showNoInternetForPreferredDatabaseDialogHybrid(int numQuestions) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Fără conexiune la internet")
                .setMessage("Nu există conexiune la internet și nu se poate accesa baza de date. Încearcă din cache local sau reîncearcă mai târziu.")
                .setPositiveButton("Cache Local", (dialog, which) -> loadQuestionsFromLocalCacheHybrid(numQuestions))
                .setNegativeButton("Renunță", (dialog, which) -> finish())
                .show();
    }

    private void showNoCacheForPreferredLocalDialogHybrid(int numQuestions) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Fără cache local disponibil")
                .setMessage("Nu există întrebări salvate local. Încearcă să te conectezi la internet sau reîncearcă mai târziu.")
                .setPositiveButton("Reîncearcă", (dialog, which) -> showInitialSetupDialog())
                .setNegativeButton("Renunță", (dialog, which) -> finish())
                .show();
    }

    private void showDataSourceSelectionDialogHybrid(int numQuestions) {
        showInitialSetupDialog();
    }

    // --- ASCUNDERE UI LA FINAL ---
    private void hideQuizElements() {
        for (MaterialButton btn : answerButtons) if (btn != null) btn.setVisibility(View.GONE);
        for (MaterialCardView card : answerCards) if (card != null) card.setVisibility(View.GONE);
        if (timerTextView != null) timerTextView.setVisibility(View.GONE);
        if (scoreTextView != null) scoreTextView.setVisibility(View.GONE);
        if (questionTextView != null) questionTextView.setVisibility(View.GONE);
        if (factTextView != null) factTextView.setVisibility(View.GONE);
        if (streakTextView != null) streakTextView.setVisibility(View.GONE);
        if (questionImage != null) questionImage.setVisibility(View.GONE);
        if (questionImageCard != null) questionImageCard.setVisibility(View.GONE);
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (fiftyFiftyButton != null) fiftyFiftyButton.setVisibility(View.GONE);
        if (skipQuestionButton != null) skipQuestionButton.setVisibility(View.GONE);
        if (finishButton != null) finishButton.setVisibility(View.GONE);
    }

    private void showFinishButton() {
        hideQuizElements();
        if (questionTextView != null) questionTextView.setText("Felicitări! Ai terminat quiz-ul.");
        if (questionImageCard != null) questionImageCard.setVisibility(View.GONE);
        if (finishButton != null) {
            finishButton.setVisibility(View.VISIBLE);
            finishButton.animate().alpha(1.0f).setDuration(500).start();
        }
        updateScore();
    }
} 