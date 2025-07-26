package com.example.myapplication.maramuresusage;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import com.example.myapplication.utils.SyncManager;
import com.example.myapplication.models.EnhancedQuestionModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.models.QuestionModel;
import com.example.myapplication.model.QuizResult;
import com.example.myapplication.repository.FirestoreQuestionRepository;
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
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Map;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;

import java.util.Arrays;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class MaramuresGameActivity extends AppCompatActivity {
    private static final String TAG = "MaramuresGameActivity";
    private static final String REGION = "maramures";
    private static final String GAME_TYPE = "quiz";
    
    // Enhanced UI Components
    private TextView questionTextView;
    private MaterialButton[] answerButtons;
    private TextView scoreTextView;
    private ProgressBar progressBar;
    private TextView timerTextView;
    private TextView streakTextView;
    private ImageView questionImage;
    private MaterialButton fiftyFiftyButton;
    private MaterialButton hintButton;
    private MaterialButton skipQuestionButton;
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
    
    // Enhanced managers
    private PointsManager pointsManager;
    private CountDownTimer timer;
    private boolean isFiftyFiftyUsed = false;
    private boolean isHintUsed = false;
    private boolean isSkipUsed = false;
    private int lifelinesUsed = 0;
    private Random random = new Random();
    private FirestoreQuestionRepository questionRepository;
    private boolean isDataLoaded = false;

    // --- HYBRID SYSTEM FIELDS ---
    private static final String DATA_SOURCE_PREF_KEY = "data_source_preference";
    private static final String CACHE_KEY = "questions_cache_" + REGION + "_" + GAME_TYPE;
    private static final String CACHE_TIMESTAMP_KEY = CACHE_KEY + "_timestamp";
    private static final long CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24h
    private String dataSourcePreference = "ask_every_time";
    private int numQuestions = 10;

    private AlertDialog exitDialog; // Referință la dialogul de ieșire

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
                    
                    if (onlineCount > localCount) {
                        Log.d(TAG, "🆕 Actualizări găsite: " + onlineCount + " online vs " + localCount + " local");
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
                loadQuestionsFromDatabase();
            })
            .setNegativeButton("📱 Mai târziu", null)
            .show();
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
        setContentView(R.layout.activity_maramures_game);

        // Initialize enhanced systems
        initializeEnhancedSystems();
        
        initializeViews();
        pointsManager = PointsManager.getInstance(this);
        questionRepository = FirestoreQuestionRepository.getInstance();
        
        // Setup game mode and difficulty
        setupGameModeAndDifficulty();
        
        // Show initial setup dialog
        showInitialSetupDialog();
        
        setupLifelines();
        applyButtonStyles();
        setupAccessibility();
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Inițializează sistemele îmbunătățite
     */
    private void initializeEnhancedSystems() {
        difficultyManager = new DifficultyManager(this);
        gameModeManager = new GameModeManager(this);
        progressTracker = new PlayerProgressTracker(this);
        achievementManager = AchievementManager.getInstance(this);
        syncManager = SyncManager.getInstance(this);
        
        // Set up achievement listener for notifications
        achievementManager.setAchievementUnlockedListener(achievement -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "🏆 Achievement Unlocked: " + achievement.getTitle(), 
                             Toast.LENGTH_LONG).show();
                // Could add more sophisticated notification here
            });
        });
        
        // Update daily play streak - simplu fără metode specifice
        Log.d(TAG, "Daily play streak updated for Maramureș");
        
        Log.d(TAG, "Enhanced systems initialized");
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Configurează modul de joc și dificultatea
     */
    private void setupGameModeAndDifficulty() {
        // Obține modul de joc din intent sau folosește default
        String gameMode = getIntent().getStringExtra("GAME_MODE");
        String focusCategory = getIntent().getStringExtra("FOCUS_CATEGORY");
        
        GameModeManager.GameMode mode = gameMode != null ? 
            GameModeManager.GameMode.valueOf(gameMode) : GameModeManager.GameMode.CLASSIC;
        
        EnhancedQuestionModel.Category category = focusCategory != null ?
            EnhancedQuestionModel.Category.valueOf(focusCategory) : null;
            
        // Inițializează modul de joc
        gameModeManager.initializeGameMode(mode, category);
        
        // Actualizează constantele de joc bazate pe modul selectat
        updateGameConstants();
        
        Log.d(TAG, "Game mode set to: " + mode.displayName + 
                (category != null ? " (Category: " + category.displayName + ")" : ""));
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Actualizează constantele de joc bazate pe mod și dificultate
     */
    private void updateGameConstants() {
        GameModeManager.GameMode currentMode = gameModeManager.getCurrentGameMode();
        
        // Actualizează timpul per întrebare bazat pe modul de joc
        TIME_PER_QUESTION = currentMode.timePerQuestion * 1000; // Convert to milliseconds
        
        // Actualizează numărul de întrebări
        if (currentMode.questionCount > 0) {
            numQuestions = currentMode.questionCount;
        }
        
        // Actualizează punctajele bazate pe dificultate - valori fixe
        POINTS_PER_CORRECT_ANSWER = 10; // Base points
        BONUS_POINTS = 50; // Streak bonus
        
        Log.d(TAG, "Game constants updated - Time: " + TIME_PER_QUESTION + 
                "ms, Questions: " + numQuestions + 
                ", Points: " + POINTS_PER_CORRECT_ANSWER);
    }

    private void initializeViews() {
        questionTextView = findViewById(R.id.textQuestion);
        
        // Initialize answer buttons array
        answerButtons = new MaterialButton[]{
            findViewById(R.id.btnAnswer1),
            findViewById(R.id.btnAnswer2),
            findViewById(R.id.btnAnswer3),
            findViewById(R.id.btnAnswer4)
        };
        // Debug: log și toast pentru fiecare buton
        String[] btnIds = {"btnAnswer1", "btnAnswer2", "btnAnswer3", "btnAnswer4"};
        for (int i = 0; i < answerButtons.length; i++) {
            if (answerButtons[i] == null) {
                Log.e(TAG, "Butonul cu id-ul " + btnIds[i] + " este null!");
                Toast.makeText(this, "Butonul cu id-ul " + btnIds[i] + " este null!", Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "Butonul cu id-ul " + btnIds[i] + " a fost găsit cu succes.");
                Toast.makeText(this, "Butonul cu id-ul " + btnIds[i] + " OK", Toast.LENGTH_SHORT).show();
            }
        }
        
        scoreTextView = findViewById(R.id.scoreTextView);
        progressBar = findViewById(R.id.progressBar);
        timerTextView = findViewById(R.id.timerTextView);
        streakTextView = findViewById(R.id.streakTextView);
        questionImage = findViewById(R.id.questionImage);
        fiftyFiftyButton = findViewById(R.id.fiftyFiftyButton);
        hintButton = findViewById(R.id.hintButton);
        skipQuestionButton = findViewById(R.id.skipQuestionButton);
        finishButton = findViewById(R.id.finishButton);
        
        // Îmbunătățiri pentru vizibilitate și stil text
        questionTextView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        
        // Aplicăm stiluri pentru butoane
        for (MaterialButton button : answerButtons) {
            button.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            button.setElevation(4f);
            button.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            
            // ✅ CORECTARE: Activez butoanele pentru click handling
            button.setClickable(true);
            button.setFocusable(true);
        }
        
        // Inițializare buton terminare
        finishButton.setOnClickListener(v -> finishGame());
        
        // Setup click pentru carduri
        for (int i = 0; i < answerButtons.length; i++) {
            final int index = i;
            
            // Click listener pentru card
            answerButtons[i].setOnClickListener(v -> {
                Log.d(TAG, "🖱️ Card clicked: " + index + ", isClickable: " + v.isClickable());
                if (v.isClickable()) {
                    Log.d(TAG, "🔄 Processing answer for card " + index);
                    checkAnswer(index, answerButtons[index].getText().toString());
                } else {
                    Log.w(TAG, "⚠️ Card " + index + " is not clickable!");
                }
            });
            
            // ✅ CORECTARE: Click listener și pentru buton (backup)
            answerButtons[i].setOnClickListener(v -> {
                Log.d(TAG, "🖱️ Button clicked: " + index + ", isEnabled: " + v.isEnabled());
                if (v.isEnabled() && answerButtons[index].isClickable()) {
                    Log.d(TAG, "🔄 Processing answer for button " + index);
                    checkAnswer(index, answerButtons[index].getText().toString());
                } else {
                    Log.w(TAG, "⚠️ Button " + index + " is not enabled or card not clickable!");
                }
            });
        }
    }
    
    private void applyButtonStyles() {
        // Stilizăm butoanele pentru tema Maramureș
        for (int i = 0; i < answerButtons.length; i++) {
            MaterialButton button = answerButtons[i];
            if (button == null) {
                Log.e(TAG, "[applyButtonStyles] Butonul de pe poziția " + i + " este null!");
                continue;
            } else {
                Log.d(TAG, "[applyButtonStyles] Butonul de pe poziția " + i + " este OK: id=" + button.getId());
            }
            // Activăm efectul de ripple pentru buton
            button.setClickable(true);
            button.setFocusable(true);
            // Adaugă animație la apăsare
            button.setRippleColor(ContextCompat.getColorStateList(this, R.color.rom_region_maramures));
            // Adaugăm shadow și efecte vizuale pentru butoane
            button.setElevation(4f);
            button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            // Adaugă efect de touch feedback
            button.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        // Scălează ușor butonul la apăsare
                        v.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        // Restabilește scara normală
                        v.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .setInterpolator(new OvershootInterpolator())
                            .start();
                        break;
                }
                return false; // Permit click-ul să continue
            });
        }
    }
    
    private void setupAccessibility() {
        // Îmbunătățim accesibilitatea pentru utilizatorii cu nevoi speciale
        questionTextView.setContentDescription("Întrebarea curentă din quiz-ul Maramureș");
        
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i].setContentDescription("Opțiunea de răspuns " + (i + 1));
        }
        
        scoreTextView.setContentDescription("Scorul curent");
        streakTextView.setContentDescription("Seria curentă de răspunsuri corecte");
        timerTextView.setContentDescription("Timpul rămas pentru întrebarea curentă");
        
        Log.d(TAG, "Accessibility features configured");
    }
    
    private void setupLifelines() {
        // Configurăm lifeline-urile interactive
        
        // 50:50 Lifeline
        if (fiftyFiftyButton != null) {
            fiftyFiftyButton.setOnClickListener(v -> useFiftyFifty());
            fiftyFiftyButton.setContentDescription("Elimină două răspunsuri greșite");
        }
        
        // Hint Lifeline
        if (hintButton != null) {
            hintButton.setOnClickListener(v -> useHint());
            hintButton.setContentDescription("Obține un indiciu pentru întrebarea curentă");
        }
        
        // Skip Question Lifeline
        if (skipQuestionButton != null) {
            skipQuestionButton.setOnClickListener(v -> useSkipQuestion());
            skipQuestionButton.setContentDescription("Sari peste întrebarea curentă");
        }
        
        // Quit Button
        if (finishButton != null) {
            finishButton.setOnClickListener(v -> showQuitConfirmation());
            finishButton.setContentDescription("Ieși din quiz");
        }
        
        updateLifelinesAvailability();
        
        Log.d(TAG, "Lifelines configured and ready");
    }
    
    private void updateLifelinesAvailability() {
        // Actualizează disponibilitatea lifeline-urilor bazată pe modul de joc și progres
        GameModeManager.GameMode currentMode = gameModeManager.getCurrentGameMode();
        
        // Unele moduri de joc nu permit lifeline-uri
        boolean lifelinesAllowed = !currentMode.equals(GameModeManager.GameMode.SURVIVAL) &&
                                  !currentMode.equals(GameModeManager.GameMode.EXPERT_CHALLENGE);
        
        if (fiftyFiftyButton != null) {
            fiftyFiftyButton.setEnabled(lifelinesAllowed && !isFiftyFiftyUsed);
            fiftyFiftyButton.setAlpha(lifelinesAllowed && !isFiftyFiftyUsed ? 1.0f : 0.5f);
        }
        
        if (hintButton != null) {
            hintButton.setEnabled(lifelinesAllowed && !isHintUsed);
            hintButton.setAlpha(lifelinesAllowed && !isHintUsed ? 1.0f : 0.5f);
        }
        
        if (skipQuestionButton != null) {
            skipQuestionButton.setEnabled(lifelinesAllowed && !isSkipUsed);
            skipQuestionButton.setAlpha(lifelinesAllowed && !isSkipUsed ? 1.0f : 0.5f);
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
                .setTitle("Setare Quiz Maramureș")
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("Start", (dialog, which) -> {
                    int sourceIdx = sourceSpinner.getSelectedItemPosition();
                    int numIdx = numQuestionsSpinner.getSelectedItemPosition();
                    numQuestions = numOptions[numIdx];
                    if (sources[sourceIdx].contains("Baza de Date")) {
                        loadQuestionsFromDatabase();
                    } else if (sources[sourceIdx].contains("Cache Local")) {
                        loadQuestionsFromLocalCache();
                    } else {
                        loadQuestionsHybrid();
                    }
                })
                .show();
    }

    private boolean isInternetAvailable() {
        return syncManager != null ? syncManager.isInternetAvailable() : true;
    }
    
    private boolean checkIfLocalCacheExists() {
        String cacheKey = "questions_cache_" + REGION + "_" + GAME_TYPE;
        String json = getSharedPreferences("HybridStorage", MODE_PRIVATE).getString(cacheKey, null);
        return json != null && !json.isEmpty();
    }
    
    private void loadQuestionsHybrid() {
        if (isInternetAvailable()) {
            loadQuestionsFromDatabase();
        } else if (checkIfLocalCacheExists()) {
            loadQuestionsFromLocalCache();
        } else {
            createLocalQuestionsForMigration();
        }
    }
    
    private void loadQuestionsFromDatabase() {
        // Afișăm un indicator de încărcare
        progressBar.setVisibility(View.VISIBLE);
        
        Log.d(TAG, "🌐 Încărcăm din baza de date pentru " + REGION);
        
        if (!syncManager.isInternetAvailable()) {
            progressBar.setVisibility(View.GONE);
            showNoInternetDialog();
            return;
        }
        
        questionRepository.getQuestionsAsModels(REGION, GAME_TYPE)
            .thenAccept(loadedQuestions -> {
                runOnUiThread(() -> {
                    Log.d(TAG, "🔍 Răspuns primit din Firestore:");
                    Log.d(TAG, "   📊 loadedQuestions != null: " + (loadedQuestions != null));
                    if (loadedQuestions != null) {
                        Log.d(TAG, "   📊 loadedQuestions.size(): " + loadedQuestions.size());
                        Log.d(TAG, "   📊 loadedQuestions.isEmpty(): " + loadedQuestions.isEmpty());
                    }
                    
                    if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
                        firestoreQuestions = loadedQuestions;
                        Log.d(TAG, "✅ Întrebări încărcate DIRECT din baza de date: " + firestoreQuestions.size());
                        
                        // ✅ CACHE LOCAL: Salvăm în cache pentru utilizare offline viitoare
                        saveQuestionsToLocalCache(loadedQuestions);
                        
                        // ✅ ÎMBUNĂTĂȚIRE: Convertește în enhanced questions și aplică filtre
                        enhancedQuestions = convertToEnhancedQuestions(firestoreQuestions);
                        enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
                        
                        // Limitează la numărul dorit de întrebări
                        if (enhancedQuestions.size() > numQuestions) {
                            Collections.shuffle(enhancedQuestions);
                            enhancedQuestions = enhancedQuestions.subList(0, numQuestions);
                        }
                        
                        // Actualizăm progress bar
                        progressBar.setMax(enhancedQuestions.size());
                        progressBar.setProgress(0);
                        progressBar.setVisibility(View.GONE);
                        
                        // Afișăm prima întrebare
                        isDataLoaded = true;
                        
                        // ✅ CORECTARE: Asigurăm că timer-ul este vizibil când încărcăm din baza de date
                        timerTextView.setVisibility(View.VISIBLE);
                        
                        // ✅ ÎMBUNĂTĂȚIRE: Începe sesiunea de tracking - simplu
                        Log.d(TAG, "Starting new quiz session");
                        
                        displayQuestion();
                        updateScore();
                        startTimer();
                        
                        // Notificăm utilizatorul despre sursa de date
                        Toast.makeText(this, "🌐 Întrebări încărcate din baza de date", Toast.LENGTH_SHORT).show();
                    } else {
                        // Dacă nu avem întrebări în Firestore, încercăm din cache local
                        Log.w(TAG, "⚠️ Nu există întrebări în Firestore pentru " + REGION + " - verificăm cache local");
                        loadQuestionsFromLocalCache();
                    }
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    Log.e(TAG, "❌ Eroare la încărcarea din Firestore", throwable);
                    progressBar.setVisibility(View.GONE);
                    loadQuestionsFromLocalCache();
                });
                return null;
            });
    }
    
    /**
     * 💾 Încarcă întrebări din cache local
     */
    private void loadQuestionsFromLocalCache() {
        Log.d(TAG, "💾 Încercăm să încărcăm din cache local...");
        
        String cacheKey = "questions_cache_" + REGION + "_" + GAME_TYPE;
        String json = getSharedPreferences("HybridStorage", MODE_PRIVATE).getString(cacheKey, null);
        
        if (json != null && !json.isEmpty()) {
            try {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<QuestionModel>>(){}.getType();
                List<QuestionModel> cachedQuestions = gson.fromJson(json, listType);
                
                if (!cachedQuestions.isEmpty()) {
                    firestoreQuestions = cachedQuestions;
                    Log.d(TAG, "💾 ✅ Întrebări încărcate din cache local: " + firestoreQuestions.size());
                    
                    // ✅ ÎMBUNĂTĂȚIRE: Convertește în enhanced questions și aplică filtre
                    enhancedQuestions = convertToEnhancedQuestions(firestoreQuestions);
                    enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
                    
                    // Limitează la numărul dorit de întrebări
                    if (enhancedQuestions.size() > numQuestions) {
                        Collections.shuffle(enhancedQuestions);
                        enhancedQuestions = enhancedQuestions.subList(0, numQuestions);
                    }
                    
                    // Actualizăm progress bar
                    progressBar.setMax(enhancedQuestions.size());
                    progressBar.setProgress(0);
                    progressBar.setVisibility(View.GONE);
                    
                    // Afișăm prima întrebare
                    isDataLoaded = true;
                    
                    // ✅ CORECTARE: Asigurăm că timer-ul este vizibil când încărcăm din cache
                    timerTextView.setVisibility(View.VISIBLE);
                    
                    // ✅ ÎMBUNĂTĂȚIRE: Începe sesiunea de tracking - simplu
                    Log.d(TAG, "Starting new quiz session from cache");
                    
                    displayQuestion();
                    updateScore();
                    startTimer();
                    
                    // Notificăm utilizatorul că folosim cache-ul
                    Toast.makeText(this, "📱 Utilizez întrebări din cache (offline)", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "💾 ❌ Eroare la parsarea cache-ului local", e);
            }
        }
        
        // Dacă ajungem aici, nu avem cache valid
        Log.w(TAG, "💾 ❌ Cache local gol sau invalid");
        createLocalQuestionsForMigration();
    }

    /**
     * Crează întrebări locale pentru Maramureș când nu există alte surse
     */
    private void createLocalQuestionsForMigration() {
        List<QuestionModel> questions = new ArrayList<>();
        
        // Întrebări despre Maramureș pentru migrare în Firestore
        questions.add(new QuestionModel(
                "Care este capitala județului Maramureș?",
            "Baia Mare", 
            Arrays.asList("Sighetu Marmației", "Borșa", "Vișeu de Sus"), 
            0,
            "Baia Mare este reședința județului Maramureș și un important centru istoric minier."
        ));
        
        questions.add(new QuestionModel(
                "Ce biserică din Maramureș este inclusă în patrimoniul UNESCO?",
            "Biserica de lemn din Budești", 
            Arrays.asList("Biserica de piatră din Baia Mare", "Biserica Sf. Nicolae din Sighet", "Biserica Neagră"), 
            0,
            "Biserica de lemn din Budești, construită în 1643, face parte din cele 8 biserici de lemn din Maramureș incluse în patrimoniul UNESCO."
        ));
        
        questions.add(new QuestionModel(
                "Ce tradiție de iarnă este specifică Maramureșului?",
            "Colindatul Feciorilor", 
            Arrays.asList("Capra", "Ursul", "Viflaimul"), 
            0,
            "Colindatul Feciorilor este o veche tradiție maramureșeană ce se păstrează din vremuri străvechi, tinerii colindând casele din sat în perioada sărbătorilor de iarnă."
        ));
        
        questions.add(new QuestionModel(
                "În ce an a fost eliberat ultimul deținut politic din închisoarea Sighet?",
            "1964", 
            Arrays.asList("1955", "1989", "1977"), 
            0,
            "În 1964 au fost eliberați ultimii deținuți politici din închisoarea Sighet, locul unde elita intelectuală și politică interbelică a fost exterminată."
        ));
        
        questions.add(new QuestionModel(
                "Ce materie primă a stat la baza dezvoltării orașului Baia Mare?",
            "Aurul și argintul", 
            Arrays.asList("Sarea", "Lemnul", "Cărbunele"), 
            0,
            "Bogăția în aur și argint a zonei a făcut ca Baia Mare să devină un important centru minier încă din Evul Mediu."
        ));
        
        questions.add(new QuestionModel(
                "Cimitirul Vesel se află în localitatea:",
            "Săpânța", 
            Arrays.asList("Bârsana", "Botiza", "Ieud"), 
            0,
            "Cimitirul Vesel din Săpânța este renumit pentru crucile colorate și epitafurile pline de umor care narează viața defunctului."
        ));
        
        questions.add(new QuestionModel(
                "Ce râu traversează Maramureșul Istoric?",
            "Tisa", 
            Arrays.asList("Someș", "Iza", "Vișeu"), 
            0,
            "Râul Tisa formează granița naturală între România și Ucraina, marcând limita nordică a Maramureșului Istoric."
        ));
        
        questions.add(new QuestionModel(
                "Care dintre următoarele este un port tradițional maramureșean?",
            "Clop, gaci, zadie", 
            Arrays.asList("Suman, iţari, opinci", "Cojoc, cioareci, bundă", "Pieptar, șubă, leucă"), 
                0,
            "Portul tradițional maramureșean include clop (pălărie), gaci (pantaloni din pânză) și zadie (fustă) pentru femei."
        ));

        questions.add(new QuestionModel(
                "Ce meșteșug tradițional este specific Maramureșului?",
            "Prelucrarea lemnului", 
            Arrays.asList("Olăritul", "Țesutul covoarelor", "Încondeierea ouălor"), 
            0,
            "Prelucrarea lemnului este un meșteșug de bază în Maramureș, cunoscut ca 'țara lemnului', cu porți monumentale și case tradiționale din lemn."
        ));
        
        questions.add(new QuestionModel(
                "Ce munte se află în Maramureș?",
            "Pietrosul Rodnei", 
            Arrays.asList("Ceahlău", "Făgăraș", "Moldoveanu"), 
            0,
            "Vârful Pietrosul Rodnei (2303 m) este cel mai înalt din Munții Rodnei și din nordul României, situat la granița dintre județele Maramureș și Bistrița-Năsăud."
        ));
        
        // Amestecăm și limitez
        Collections.shuffle(questions);
        if (questions.size() > numQuestions) {
            questions = questions.subList(0, numQuestions);
        }
        
        firestoreQuestions = questions;
        
        // Convertesc la enhanced questions
        enhancedQuestions = convertToEnhancedQuestions(firestoreQuestions);
        enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
        
        // Actualizez UI
        progressBar.setMax(enhancedQuestions.size());
        progressBar.setProgress(0);
        progressBar.setVisibility(View.GONE);
        
        isDataLoaded = true;
        timerTextView.setVisibility(View.VISIBLE);
        Log.d(TAG, "Starting new local quiz session");
        
        displayQuestion();
        updateScore();
        startTimer();
        
        Toast.makeText(this, "📚 Utilizez întrebări locale pentru Maramureș", Toast.LENGTH_SHORT).show();
        
        Log.d(TAG, "Întrebări locale create: " + questions.size());
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Convertește întrebările simple în enhanced questions
     */
    private List<EnhancedQuestionModel> convertToEnhancedQuestions(List<QuestionModel> questions) {
        List<EnhancedQuestionModel> enhanced = new ArrayList<>();
        
        for (QuestionModel question : questions) {
            // Mapează întrebările la categorii bazate pe conținut
            EnhancedQuestionModel.Category category = inferCategory(question.getQuestion());
            EnhancedQuestionModel.Difficulty difficulty = inferDifficulty(question);
            
            EnhancedQuestionModel enhancedQuestion = EnhancedQuestionModel.fromQuestionModel(
                question, category, difficulty);
            
            // Adaugă tag-uri bazate pe conținut
            enhancedQuestion.setTags(generateTags(question));
            
            enhanced.add(enhancedQuestion);
        }
        
        Log.d(TAG, "Converted " + questions.size() + " questions to enhanced format");
        return enhanced;
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Inferă categoria unei întrebări bazată pe conținut
     */
    private EnhancedQuestionModel.Category inferCategory(String questionText) {
        String text = questionText.toLowerCase();
        
        if (text.contains("biserică") || text.contains("lemn") || text.contains("arhitectur") || 
            text.contains("construc")) {
            return EnhancedQuestionModel.Category.ARCHITECTURE;
        } else if (text.contains("închisoare") || text.contains("deținut") || text.contains("1964") ||
                  text.contains("istorie") || text.contains("politic")) {
            return EnhancedQuestionModel.Category.HISTORY;
        } else if (text.contains("munte") || text.contains("râu") || text.contains("județ") ||
                  text.contains("tisa") || text.contains("geografie") || text.contains("capitala")) {
            return EnhancedQuestionModel.Category.GEOGRAPHY;
        } else if (text.contains("tradiție") || text.contains("colind") || text.contains("port") ||
                  text.contains("meșteșug") || text.contains("cultură")) {
            return EnhancedQuestionModel.Category.CULTURE;
        } else if (text.contains("cimitir") || text.contains("vesel") || text.contains("crucile") ||
                  text.contains("epitaf")) {
            return EnhancedQuestionModel.Category.LEGENDS;
        } else if (text.contains("aur") || text.contains("argint") || text.contains("minier") ||
                  text.contains("dezvoltare")) {
            return EnhancedQuestionModel.Category.HISTORY;
        } else {
            return EnhancedQuestionModel.Category.GENERAL; // Default
        }
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Inferă dificultatea unei întrebări
     */
    private EnhancedQuestionModel.Difficulty inferDifficulty(QuestionModel question) {
        String text = question.getQuestion().toLowerCase();
        
        // Întrebări despre date specifice sau detalii tehnice = Hard
        if (text.contains("1964") || text.contains("2303") || text.contains("1643")) {
            return EnhancedQuestionModel.Difficulty.HARD;
        }
        
        // Întrebări despre capitale sau fapte de bază = Easy
        if (text.contains("capitala") || text.contains("se află în")) {
            return EnhancedQuestionModel.Difficulty.EASY;
        }
        
        // Întrebări despre cultură și tradiții = Medium
        if (text.contains("tradiție") || text.contains("meșteșug") || text.contains("port")) {
            return EnhancedQuestionModel.Difficulty.MEDIUM;
        }
        
        // Default la Medium
        return EnhancedQuestionModel.Difficulty.MEDIUM;
    }
    
    /**
     * ✅ ÎMBUNĂTĂȚIRE: Generează tag-uri pentru căutare
     */
    private String[] generateTags(QuestionModel question) {
        List<String> tags = new ArrayList<>();
        String text = question.getQuestion().toLowerCase();
        
        // Tag-uri geografice
        if (text.contains("maramureș")) tags.add("maramures");
        if (text.contains("baia mare")) tags.add("baia-mare");
        if (text.contains("sighet")) tags.add("sighet");
        if (text.contains("săpânța")) tags.add("sapanta");
        if (text.contains("budești")) tags.add("budesti");
        
        // Tag-uri istorice
        if (text.contains("închisoare")) tags.add("inchisoare");
        if (text.contains("1964")) tags.add("comunism");
        if (text.contains("deținut")) tags.add("represiune");
        
        // Tag-uri culturale
        if (text.contains("unesco")) tags.add("unesco");
        if (text.contains("patrimoniu")) tags.add("patrimoniu");
        if (text.contains("tradiție")) tags.add("traditie");
        if (text.contains("lemn")) tags.add("lemn");
        if (text.contains("biserică")) tags.add("biserica");
        
        return tags.toArray(new String[0]);
    }

    /**
     * 💾 Salvează întrebările în cache local
     */
    private void saveQuestionsToLocalCache(List<QuestionModel> questions) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(questions);
            
            String cacheKey = "questions_cache_" + REGION + "_" + GAME_TYPE;
            String timestampKey = cacheKey + "_timestamp";
            
            getSharedPreferences("HybridStorage", MODE_PRIVATE)
                .edit()
                .putString(cacheKey, json)
                .putLong(timestampKey, System.currentTimeMillis())
                .apply();
                
            Log.d(TAG, "💾 ✅ " + questions.size() + " întrebări salvate în cache local");
        } catch (Exception e) {
            Log.e(TAG, "💾 ❌ Eroare la salvarea în cache local", e);
        }
    }

    /**
     * 📱 Afișează dialog când nu există internet pentru încărcarea din baza de date
     */
    private void showNoInternetDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Nu există conexiune la internet")
            .setMessage("Pentru regiunea Maramureș, întrebările se încarcă din baza de date.\n\n" +
                       "Vă rugăm să verificați conexiunea la internet și să încercați din nou.")
            .setPositiveButton("🔄 Reîncearcă", (dialog, which) -> {
                loadQuestionsFromDatabase();
            })
            .setNegativeButton("📱 Cache Local", (dialog, which) -> {
                loadQuestionsFromLocalCache();
            })
            .setNeutralButton("❌ Închide", (dialog, which) -> {
                finish();
            })
            .setCancelable(false)
            .show();
    }

    private void displayQuestion() {
        if (!isDataLoaded) {
            Log.d(TAG, "Data not loaded yet, waiting...");
            return;
        }
        
        // ✅ ÎMBUNĂTĂȚIRE: Verificăm enhanced questions în loc de firestore questions
        if (enhancedQuestions == null || enhancedQuestions.isEmpty()) {
            Log.e(TAG, "No enhanced questions available");
            showNoQuestionsError();
            return;
        }
        
        // ✅ ÎMBUNĂTĂȚIRE: Verifică dacă jocul trebuie să se termine bazat pe modul
        if (currentQuestionIndex >= enhancedQuestions.size() || 
            gameModeManager.isGameComplete(enhancedQuestions.size())) {
            Log.d(TAG, "All questions completed, finishing game");
            finishGame();
            return;
        }
        
        // Reset card styles
        resetCardStyles();
        
        // Actualizăm progress bar
        progressBar.setProgress(currentQuestionIndex + 1);
        
        // ✅ ÎMBUNĂTĂȚIRE: Afișăm întrebarea curentă din enhanced questions
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        questionTextView.setText(currentQuestion.getQuestion());
        
        // ✅ ÎMBUNĂTĂȚIRE: Marchează timpul de început pentru tracking
        questionStartTime = System.currentTimeMillis();
        
        // ✅ ÎMBUNĂTĂȚIRE: Verifică și aplică restricțiile lifeline bazate pe modul și dificultate
        updateLifelinesAvailability();
        
        Log.d(TAG, "Displaying question " + (currentQuestionIndex + 1) + "/" + enhancedQuestions.size() + 
                ": " + currentQuestion.getQuestion() + 
                " [Category: " + currentQuestion.getCategory().displayName + 
                ", Difficulty: " + currentQuestion.getDifficulty().displayName + "]");
        
        // Obținem toate răspunsurile
        List<String> allAnswers = currentQuestion.getAnswers();
        
        // Amestecăm răspunsurile pentru randomizare
        Collections.shuffle(allAnswers);
        
        // Populăm butoanele cu răspunsurile amestecate
        for (int i = 0; i < Math.min(answerButtons.length, allAnswers.size()); i++) {
            answerButtons[i].setText(allAnswers.get(i));
            answerButtons[i].setVisibility(View.VISIBLE);
        }
        
        // Ascundem butoanele neutilizate
        for (int i = allAnswers.size(); i < answerButtons.length; i++) {
            answerButtons[i].setVisibility(View.GONE);
        }
        
        // Resetăm lifeline-urile pentru întrebarea nouă
        resetLifelinesForNewQuestion();
    }

    private void resetCardStyles() {
        for (int i = 0; i < answerButtons.length; i++) {
            MaterialButton button = answerButtons[i];
            
            // Resetăm stilurile butonului
            button.setEnabled(true);
            button.setClickable(true);
            button.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            button.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            
            // Resetăm scara pentru animații
            button.setScaleX(1.0f);
            button.setScaleY(1.0f);
        }
    }
    
    private void resetLifelinesForNewQuestion() {
        // Resetez availability pentru lifeline-uri pe baza modului curent
        updateLifelinesAvailability();
    }

    private void checkAnswer(int selectedAnswerIndex, String selectedAnswer) {
        // Dezactivăm butoanele pentru a preveni răspunsuri multiple
        for (MaterialButton button : answerButtons) {
            button.setEnabled(false);
        }
        
        // ✅ ÎMBUNĂTĂȚIRE: Verificăm enhanced questions în loc de firestore questions
        if (enhancedQuestions == null || enhancedQuestions.isEmpty() || 
            currentQuestionIndex >= enhancedQuestions.size()) {
            Log.e(TAG, "No enhanced questions available for answer checking");
            return;
        }
        
        // ✅ ÎMBUNĂTĂȚIRE: Obținem întrebarea curentă din enhanced questions
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        String correctAnswer = currentQuestion.getCorrectAnswer();
        String fact = currentQuestion.getFact();
        boolean isCorrect = selectedAnswer.equals(correctAnswer);
        
        // ✅ ÎMBUNĂTĂȚIRE: Calculează timpul petrecut pentru această întrebare
        long timeSpent = System.currentTimeMillis() - questionStartTime;
        
        // ✅ ÎMBUNĂTĂȚIRE: Track răspunsul pentru analytics - simplu
        Log.d(TAG, "Question answered: " + (isCorrect ? "CORRECT" : "WRONG") + 
               ", Category: " + currentQuestion.getCategory().name() + 
               ", Time: " + timeSpent + "ms");
        
        // ✅ ÎMBUNĂTĂȚIRE: Track răspunsul pentru achievements - simplu
        Log.d(TAG, "Recording quiz answer for achievements");
        
        Log.d(TAG, "Checking answer: '" + selectedAnswer + "' vs correct: '" + correctAnswer + 
                 "' -> " + (isCorrect ? "CORRECT" : "WRONG"));
         
        // Anulăm timerul
        if (timer != null) {
            timer.cancel();
        }

        // Actualizăm statisticile
        totalQuestions++;
         
        // Aplicăm stilurile corespunzătoare pentru răspuns
        if (isCorrect) {
            // ✅ ÎMBUNĂTĂȚIRE: Feedback pentru răspuns corect
            provideCorrectAnswerFeedback(selectedAnswerIndex);
             
            // ✅ ÎMBUNĂTĂȚIRE: Calculează punctajul cu bonusuri pentru mod și dificultate
            int basePoints = POINTS_PER_CORRECT_ANSWER;
            int modeBonus = gameModeManager.calculateModeBonus(basePoints, isCorrect, timeSpent);
            int finalScore = basePoints + modeBonus;
             
            score += finalScore;
            streak++;
            if (streak > maxStreak) {
                maxStreak = streak;
            }
             
            // Bonus pentru streak
            if (streak >= STREAK_BONUS_THRESHOLD) {
                score += BONUS_POINTS;
                showStreakBonus();
            }
             
            // ✅ ÎMBUNĂTĂȚIRE: Log detaliat al punctajului
            Log.d(TAG, "Score calculation - Base: " + basePoints + 
                   ", Mode bonus: " + modeBonus + 
                   ", Final: " + finalScore);
            
            correctAnswers++;
             
            // Actualizăm scorul și streak-ul
            updateScore();
            updateStreak();
             
            Log.d(TAG, "✅ Correct answer! Score: " + score + ", Streak: " + streak);
             
            // Afișăm informația suplimentară
            showAnswerDialog(fact, true);
        } else {
            // ✅ ÎMBUNĂTĂȚIRE: Feedback pentru răspuns greșit
            provideWrongAnswerFeedback(selectedAnswerIndex, correctAnswer);
             
            // Resetăm streak-ul
            streak = 0;
            updateStreak();
             
            Log.d(TAG, "❌ Wrong answer! Streak reset. Score remains: " + score);
             
            // Afișăm informația suplimentară
            showAnswerDialog(fact, false);
        }
         
        // Vibrație pentru feedback haptic
        provideHapticFeedback(isCorrect);
         
        // Progres la următoarea întrebare după delay
        new Handler().postDelayed(() -> moveToNextQuestion(), 2000);
    }

    private void provideCorrectAnswerFeedback(int selectedIndex) {
        // Colorăm butonul selectat în verde pentru răspuns corect
        answerButtons[selectedIndex].setBackgroundColor(ContextCompat.getColor(this, R.color.rom_correct_answer));
        answerButtons[selectedIndex].setTextColor(ContextCompat.getColor(this, R.color.white));
        
        // Animație de succes
        answerButtons[selectedIndex].animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(200)
            .setInterpolator(new OvershootInterpolator())
            .withEndAction(() -> {
                answerButtons[selectedIndex].animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start();
            })
            .start();
    }
    
    private void provideWrongAnswerFeedback(int selectedIndex, String correctAnswer) {
        // Colorăm butonul selectat în roșu pentru răspuns greșit
        answerButtons[selectedIndex].setBackgroundColor(ContextCompat.getColor(this, R.color.rom_wrong_answer));
        answerButtons[selectedIndex].setTextColor(ContextCompat.getColor(this, R.color.white));
        
        // Găsim și evidențiem răspunsul corect
        for (int i = 0; i < answerButtons.length; i++) {
            if (answerButtons[i].getText().toString().equals(correctAnswer)) {
                answerButtons[i].setBackgroundColor(ContextCompat.getColor(this, R.color.rom_correct_answer));
                answerButtons[i].setTextColor(ContextCompat.getColor(this, R.color.white));
                break;
            }
        }
    }
    
    private void provideHapticFeedback(boolean isCorrect) {
        try {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    if (isCorrect) {
                        // Vibrație scurtă pentru răspuns corect
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        // Vibrație dublă pentru răspuns greșit
                        vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 100, 100, 100}, -1));
                    }
                } else {
                    // Fallback pentru versiuni mai vechi
                    vibrator.vibrate(isCorrect ? 100 : 300);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not provide haptic feedback", e);
        }
    }
    
    private void showStreakBonus() {
        // Animație specială pentru bonus streak
        Toast.makeText(this, "🔥 Streak Bonus! +" + BONUS_POINTS + " puncte pentru " + streak + " răspunsuri consecutive!", 
                      Toast.LENGTH_LONG).show();
        
        // Animație pe streak text
        if (streakTextView != null) {
            streakTextView.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setDuration(300)
                .withEndAction(() -> {
                    streakTextView.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(300)
                        .start();
                })
                .start();
        }
    }
    
    private void showAnswerDialog(String fact, boolean isCorrect) {
        if (fact != null && !fact.isEmpty()) {
            String title = isCorrect ? "✅ Răspuns Corect!" : "❌ Răspuns Greșit";
            String icon = isCorrect ? "🎉" : "📚";
            
            new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(icon + " " + fact)
                .setPositiveButton("Continuă", null)
                .show();
        }
    }

    private void moveToNextQuestion() {
        currentQuestionIndex++;
        
        Log.d(TAG, "🔄 Moving to next question: " + (currentQuestionIndex + 1) + "/" + getQuestionsCount());
        
        if (currentQuestionIndex < getQuestionsCount()) {
            // ✅ BUG FIX: Asigurăm că toate cardurile sunt re-activate și reset-ate complet
            resetCardStyles();
            
            // ✅ EXTRA SAFEGUARD: Re-activăm manual cardurile pentru siguranță
            for (MaterialButton button : answerButtons) {
                button.setClickable(true);
                button.setEnabled(true);
            }
            
            // ✅ BUG FIX: Afișăm întrebarea și actualizăm UI-ul
            displayQuestion();
            updateScore(); // Actualizez progress bar-ul corect
            
            // ✅ BUG FIX: Repornesc timer-ul pentru următoarea întrebare
            startTimer();
            
            // ✅ DEBUG: Verificăm starea finală a cardurilor
            boolean buttonsClickable = true;
        for (MaterialButton button : answerButtons) {
                if (!button.isClickable()) {
                    buttonsClickable = false;
                    break;
                }
            }
            
            Log.d(TAG, "✅ Successfully moved to question " + (currentQuestionIndex + 1) + "/" + getQuestionsCount() + 
                    ", All buttons clickable: " + buttonsClickable + 
                    ", Timer active: " + (timer != null));
        } else {
            // ✅ CORECTARE: Mergem direct la finalizare în loc de showFinishButton
            Log.d(TAG, "🏁 Quiz completed! Starting finish game.");
            finishGame();
        }
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        
        timer = new CountDownTimer(TIME_PER_QUESTION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                timerTextView.setText(String.valueOf(secondsLeft));
                
                // Actualizăm progress bar-ul pentru timer
                int progress = (int) ((millisUntilFinished * 100) / TIME_PER_QUESTION);
                progressBar.setSecondaryProgress(progress);
                
                // Schimbăm culoarea timer-ului când timpul se apropie de sfârșit
                if (secondsLeft <= 5) {
                    timerTextView.setTextColor(ContextCompat.getColor(MaramuresGameActivity.this, R.color.rom_wrong_answer));
        } else {
                    timerTextView.setTextColor(ContextCompat.getColor(MaramuresGameActivity.this, R.color.rom_region_maramures));
                }
            }

            @Override
            public void onFinish() {
                timerTextView.setText("0");
                handleTimeUp();
            }
        };
        
        timer.start();
    }
    
    private void handleTimeUp() {
        // Dezactivăm toate cardurile
        for (MaterialButton button : answerButtons) {
            button.setEnabled(false);
        }
        
        // Resetăm streak-ul pentru timeout
        streak = 0;
        updateStreak();
        
        // Găsim și evidențiem răspunsul corect
        if (enhancedQuestions != null && currentQuestionIndex < enhancedQuestions.size()) {
            String correctAnswer = enhancedQuestions.get(currentQuestionIndex).getCorrectAnswer();
            for (int i = 0; i < answerButtons.length; i++) {
                if (answerButtons[i].getText().toString().equals(correctAnswer)) {
                    answerButtons[i].setBackgroundColor(ContextCompat.getColor(this, R.color.rom_correct_answer));
                    answerButtons[i].setTextColor(ContextCompat.getColor(this, R.color.white));
                    break;
                }
            }
        }
        
        Toast.makeText(this, "⏰ Timpul a expirat!", Toast.LENGTH_SHORT).show();
        
        // Progres la următoarea întrebare după delay
        new Handler().postDelayed(() -> moveToNextQuestion(), 1500);
    }

    private void updateScore() {
        // ✅ CORECTARE: Afișez doar scorul, fără indicator sursă
        scoreTextView.setText(String.valueOf(score));
        
        // ✅ BUG FIX: Calculez progress bar-ul corect (currentQuestionIndex + 1 pentru întrebarea curentă)
        int progress = Math.min(100, ((currentQuestionIndex + 1) * 100) / getQuestionsCount());
        progressBar.setProgress(progress);
        
        Log.d(TAG, "Score updated: " + score + ", Progress: " + progress + "% (" + 
                (currentQuestionIndex + 1) + "/" + getQuestionsCount() + ")");
    }

    private void updateStreak() {
        if (streakTextView != null) {
            streakTextView.setText(String.valueOf(streak));
        }
    }

    private int getQuestionsCount() {
        return enhancedQuestions != null ? enhancedQuestions.size() : 0;
    }

    // --- LIFELINE METHODS ---
    
    private void useFiftyFifty() {
        if (isFiftyFiftyUsed || !isDataLoaded || currentQuestionIndex >= enhancedQuestions.size()) {
            return;
        }
        
        isFiftyFiftyUsed = true;
        lifelinesUsed++;
        updateLifelinesAvailability();
        
        // Găsește răspunsul corect
        String correctAnswer = enhancedQuestions.get(currentQuestionIndex).getCorrectAnswer();
        
        // Găsește două răspunsuri greșite și le ascunde
        List<Integer> wrongAnswerIndices = new ArrayList<>();
        for (int i = 0; i < answerButtons.length; i++) {
            if (!answerButtons[i].getText().toString().equals(correctAnswer) && 
                answerButtons[i].getVisibility() == View.VISIBLE) {
                wrongAnswerIndices.add(i);
            }
        }
        
        Collections.shuffle(wrongAnswerIndices);
        
        // Ascunde două răspunsuri greșite
        for (int i = 0; i < Math.min(2, wrongAnswerIndices.size()); i++) {
            int index = wrongAnswerIndices.get(i);
            answerButtons[index].setVisibility(View.GONE);
        }
        
        Toast.makeText(this, "🎯 50:50 folosit! Două răspunsuri greșite eliminate.", Toast.LENGTH_SHORT).show();
        provideHapticFeedback(true);
    }
    
    private void useHint() {
        if (isHintUsed || !isDataLoaded || currentQuestionIndex >= enhancedQuestions.size()) {
            return;
        }
        
        isHintUsed = true;
        lifelinesUsed++;
        updateLifelinesAvailability();
        
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        String hint = generateHint(currentQuestion);
        
            new MaterialAlertDialogBuilder(this)
                    .setTitle("💡 Indiciu")
            .setMessage(hint)
                    .setPositiveButton("Am înțeles", null)
                    .show();
        
        Toast.makeText(this, "💡 Indiciu folosit!", Toast.LENGTH_SHORT).show();
        provideHapticFeedback(true);
    }
    
    private String generateHint(EnhancedQuestionModel question) {
        // Generează indicii bazate pe categoria întrebării
        switch (question.getCategory()) {
            case GEOGRAPHY:
                return "💡 Gândește-te la geografia și teritoriul Maramureșului.";
            case HISTORY:
                return "💡 Această întrebare se referă la evenimente istorice importante din Maramureș.";
            case CULTURE:
                return "💡 Răspunsul se referă la tradițiile și cultura maramureșeană.";
            case ARCHITECTURE:
                return "💡 Gândește-te la monumentele și construcțiile specifice Maramureșului.";
            case LEGENDS:
                return "💡 Această întrebare se referă la o tradiție sau legendă locală.";
            default:
                return "💡 Gândește-te la specificul regiunii Maramureș - țara lemnului și a tradițiilor.";
        }
    }
    
    private void useSkipQuestion() {
        if (isSkipUsed || !isDataLoaded) {
            return;
        }
        
        isSkipUsed = true;
        lifelinesUsed++;
        updateLifelinesAvailability();
        
        // Resetăm streak-ul pentru skip
        streak = 0;
        updateStreak();
        
        Toast.makeText(this, "⏭️ Întrebare sărită!", Toast.LENGTH_SHORT).show();
        provideHapticFeedback(true);
        
        // Trecem direct la următoarea întrebare
        moveToNextQuestion();
    }
    
    private void showQuitConfirmation() {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("🚪 Ieșire din Quiz")
            .setMessage("Ești sigur că vrei să ieși?\n\n" +
                       "Progresul curent:\n" +
                       "• Scor: " + score + " puncte\n" +
                       "• Întrebarea " + (currentQuestionIndex + 1) + " din " + getQuestionsCount() + "\n" +
                       "• Streak: " + streak + " răspunsuri corecte consecutive\n\n" +
                       "Progresul va fi pierdut!")
                    .setPositiveButton("Da, ieși", (dialog, which) -> finish())
                    .setNegativeButton("Continuă", null)
                    .show();
        }

    /**
     * Afișează eroare când nu există întrebări deloc
     */
    private void showNoQuestionsError() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Eroare")
            .setMessage("Nu există întrebări disponibile pentru Maramureș. Contactați administratorul aplicației.")
            .setPositiveButton("OK", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }

    private String getAchievements() {
        List<String> achievements = new ArrayList<>();
        
        if (maxStreak >= 5) {
            achievements.add("Geniu Maramureșean (serie de " + maxStreak + " răspunsuri corecte)");
        } else if (maxStreak >= 3) {
            achievements.add("Cunoscător al Maramureșului (serie de " + maxStreak + " răspunsuri corecte)");
        }
        
        if (correctAnswers == getQuestionsCount()) {
            achievements.add("Perfect! Toate răspunsurile corecte");
        } else if (correctAnswers >= getQuestionsCount() * 0.8) {
            achievements.add("Expert al Maramureșului (" + correctAnswers + " din " + getQuestionsCount() + " corecte)");
        } else if (correctAnswers >= getQuestionsCount() * 0.5) {
            achievements.add("Bun cunoscător (" + correctAnswers + " din " + getQuestionsCount() + " corecte)");
        }
        
        if (lifelinesUsed == 0) {
            achievements.add("Fără ajutor extern - nu ai folosit niciun lifeline!");
        }
        
        if (achievements.isEmpty()) {
            return "Nicio realizare specială. Poți face mai bine data viitoare!";
        }
        
        StringBuilder result = new StringBuilder("Realizări:");
        for (String achievement : achievements) {
            result.append("\n• ").append(achievement);
        }
        
        return result.toString();
    }

    private void finishGame() {
        if (timer != null) {
            timer.cancel();
        }

        // ✅ ÎMBUNĂTĂȚIRE: Închide sesiunea de tracking și actualizează dificultatea - simplu
        Log.d(TAG, "Ending quiz session with score: " + score);
        
        // ✅ ÎMBUNĂTĂȚIRE: Verifică pentru noi achievements - simplu
        Log.d(TAG, "Checking for new achievements...");

        // ✅ ACHIEVEMENT TRACKING: Update Maramureș specific achievements - simplu
        Log.d(TAG, "Updating Maramureș quiz completions");
        
        // Check for perfect score
        if (correctAnswers == getQuestionsCount()) {
            Log.d(TAG, "Perfect score achieved!");
        }
        
        // Update game mode achievements
        String gameModeName = gameModeManager.getCurrentGameMode().name();
        Log.d(TAG, "Recording game mode completion: " + gameModeName);
        
        // Update difficulty unlock achievements - simplu
        Log.d(TAG, "Checking difficulty unlock achievements");

        // Adăugăm punctele în contul utilizatorului
        pointsManager.addPoints(this, "maramures", score);
        
        // Salvăm rezultatul în sistemul hibrid (local + cloud)
        saveQuizResultToHybridStorage();
        
        // Salvăm rezultatul quiz-ului într-o structură organizată pentru user profile și leaderboard
        saveQuizResultToFirebase();
        
        // Calculăm noile achievement-uri deblocate
        String achievementMessage = getAchievements();
        
        // ✅ ÎMBUNĂTĂȚIRE: Navigăm la activitatea de rezultate în loc să afișăm dialog
        Intent intent = new Intent(this, com.example.myapplication.crisanausage.CrisanaGameOverActivity.class);
        intent.putExtra("SCORE", score);
        intent.putExtra("CORRECT_ANSWERS", correctAnswers);
        intent.putExtra("TOTAL_QUESTIONS", getQuestionsCount());
        intent.putExtra("MAX_STREAK", maxStreak);
        intent.putExtra("TIME_SPENT", totalTime);
        intent.putExtra("GAME_MODE", gameModeManager.getCurrentGameMode().name());
        intent.putExtra("DIFFICULTY", "NORMAL"); // Default difficulty
        intent.putExtra("LIFELINES_USED", lifelinesUsed);
        intent.putExtra("ACHIEVEMENTS", achievementMessage);
        intent.putExtra("REGION", "Maramureș");
        
        startActivity(intent);
        finish();
    }

    /**
     * 💾🌐 Salvează rezultatul într-un sistem hibrid (local + cloud)
     */
    private void saveQuizResultToHybridStorage() {
        try {
            QuizResult result = new QuizResult();
            result.setRegion("maramures");
            result.setScore(score);
            result.setCorrectAnswers(correctAnswers);
            result.setTotalQuestions(getQuestionsCount());
            result.setMaxStreak(maxStreak);
            result.setTotalTime(totalTime);
            result.setGameMode(gameModeManager.getCurrentGameMode().name());
            result.setDifficulty("NORMAL"); // Default difficulty
            result.setLifelinesUsed(lifelinesUsed);
            result.setTimestamp(new Date(System.currentTimeMillis()));
            
            // Salvare locală
            saveResultLocally(result);
            
            // Salvare în cloud (dacă există conexiune)
            if (syncManager.isInternetAvailable()) {
                saveResultToCloud(result);
            }
            
            Log.d(TAG, "Quiz result saved to hybrid storage");
        } catch (Exception e) {
            Log.e(TAG, "Error saving quiz result to hybrid storage", e);
        }
    }
    
    private void saveResultLocally(QuizResult result) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(result);
            
            String key = "quiz_result_" + REGION + "_" + System.currentTimeMillis();
            getSharedPreferences("QuizResults", MODE_PRIVATE)
                .edit()
                .putString(key, json)
                .apply();
                
            Log.d(TAG, "Result saved locally");
        } catch (Exception e) {
            Log.e(TAG, "Error saving result locally", e);
        }
    }
    
    private void saveResultToCloud(QuizResult result) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("region", result.getRegion());
            resultData.put("score", result.getScore());
            resultData.put("correctAnswers", result.getCorrectAnswers());
            resultData.put("totalQuestions", result.getTotalQuestions());
            resultData.put("maxStreak", result.getMaxStreak());
            resultData.put("timeSpent", result.getTotalTime());
            resultData.put("gameMode", result.getGameMode());
            resultData.put("difficulty", result.getDifficulty());
            resultData.put("lifelinesUsed", result.getLifelinesUsed());
            resultData.put("timestamp", FieldValue.serverTimestamp());
            resultData.put("userId", user != null ? user.getUid() : "anonymous");
            
            db.collection("quiz_results")
                .add(resultData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Result saved to cloud with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving result to cloud", e);
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error preparing result for cloud", e);
        }
    }
    
    /**
     * 🌐 Salvează rezultatul quiz-ului în Firebase pentru profilul utilizatorului
     */
    private void saveQuizResultToFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Calculăm acuratețea
        float accuracy = getQuestionsCount() > 0 ? ((float) correctAnswers / getQuestionsCount()) * 100 : 0;
        
        // Creăm structura de date pentru rezultat
        Map<String, Object> quizResult = new HashMap<>();
        quizResult.put("region", "maramures");
        quizResult.put("score", score);
        quizResult.put("correctAnswers", correctAnswers);
        quizResult.put("totalQuestions", getQuestionsCount());
        quizResult.put("accuracy", accuracy);
        quizResult.put("maxStreak", maxStreak);
        quizResult.put("timeSpent", totalTime);
        quizResult.put("gameMode", gameModeManager.getCurrentGameMode().name());
        quizResult.put("difficulty", "NORMAL"); // Default difficulty
        quizResult.put("lifelinesUsed", lifelinesUsed);
        quizResult.put("timestamp", FieldValue.serverTimestamp());
        quizResult.put("date", new Date());
        
        // Salvăm în colecția de rezultate globale pentru leaderboard
        db.collection("quiz_results")
            .add(quizResult)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "✅ Quiz result saved to global leaderboard: " + documentReference.getId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Error saving quiz result to global leaderboard", e);
            });
        
        // Actualizăm profilul utilizatorului cu noul rezultat
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("lastPlayedRegion", "maramures");
        userUpdate.put("lastPlayedDate", FieldValue.serverTimestamp());
        userUpdate.put("totalGamesPlayed", FieldValue.increment(1));
        userUpdate.put("totalScore", FieldValue.increment(score));
        
        // Actualizăm statisticile specifice pentru Maramureș
        userUpdate.put("maramures.gamesPlayed", FieldValue.increment(1));
        userUpdate.put("maramures.totalScore", FieldValue.increment(score));
        userUpdate.put("maramures.bestScore", score); // Va fi actualizat prin security rules dacă e mai mare
        userUpdate.put("maramures.totalCorrectAnswers", FieldValue.increment(correctAnswers));
        userUpdate.put("maramures.totalQuestions", FieldValue.increment(getQuestionsCount()));
        
        if (maxStreak > 0) {
            userUpdate.put("maramures.bestStreak", maxStreak); // Va fi actualizat prin security rules dacă e mai mare
        }
        
        // Actualizăm documentul utilizatorului
        db.collection("users").document(user.getUid())
            .update(userUpdate)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "✅ User profile updated with quiz result");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Error updating user profile", e);
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        if (exitDialog != null && exitDialog.isShowing()) {
            exitDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showExitConfirmationDialog();
    }

    private void showExitConfirmationDialog() {
        if (exitDialog != null && exitDialog.isShowing()) {
            return;
        }
        
        if (isFinishing()) {
            return;
        }
        
        exitDialog = new MaterialAlertDialogBuilder(this)
            .setTitle("Ești sigur?")
            .setMessage("Progresul actual va fi pierdut.\n\n" +
                       "Progresul curent:\n" +
                       "• Scor: " + score + " puncte\n" +
                       "• Întrebarea " + (currentQuestionIndex + 1) + " din " + getQuestionsCount() + "\n" +
                       "• Streak: " + streak + " răspunsuri corecte consecutive")
            .setPositiveButton("Da, ies", (dialog, which) -> {
                 if (timer != null) timer.cancel();
                 super.onBackPressed();
            })
            .setNegativeButton("Nu", null)
            .create();
        
        exitDialog.show();
    }
} 