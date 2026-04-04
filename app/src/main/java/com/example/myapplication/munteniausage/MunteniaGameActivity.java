package com.example.myapplication.munteniausage;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.core.domain.model.EnhancedQuestionModel;
import com.example.myapplication.utils.RegionGameEnhancer;
import com.example.myapplication.utils.HapticFeedbackType;
import com.example.myapplication.Joc1.AchievementManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activitate pentru jocul regiunii Muntenia - Enhanced Version
 */
public class MunteniaGameActivity extends AppCompatActivity {
    private static final String TAG = "MunteniaGameActivity";
    private static final String REGION = "muntenia";
    private static final String GAME_TYPE = "quiz";

    // Modern game systems
    private GameModeManager gameModeManager;
    private DifficultyManager difficultyManager;
    private PlayerProgressTracker progressTracker;
    private RegionGameEnhancer gameEnhancer;
    private RegionGameEnhancer.GameConstants gameConstants;
    
    // Enhanced question management
    private List<EnhancedQuestionModel> enhancedQuestions;
    
    // Enhanced game state variables
    private long totalTime = 0;
    private long questionStartTime = 0;
    private boolean answerSelected = false;
    private int correctAnswers = 0;
    private int maxStreak = 0;
    
    // Lifeline states
    private boolean hintUsed = false;
    
    // Enhanced timer and UI
    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();

    private static final int TOTAL_QUESTIONS = 10;
    private static final int TIME_PER_QUESTION = 20000; // Will be updated by game constants
    private static final int CORRECT_ANSWER_POINTS = 50;
    private static final int TIME_BONUS_POINTS = 10;

    // View variables
    private TextView timerTextView;
    private TextView scoreTextView;
    private TextView questionTextView;
    private TextView factTextView;
    private TextView streakTextView;
    private MaterialButton answerButton1;
    private MaterialButton answerButton2;
    private MaterialButton answerButton3;
    private MaterialButton answerButton4;
    private MaterialButton finishButton;
    private ImageView questionImage;
    private MaterialButton fiftyFiftyButton;
    private MaterialButton skipQuestionButton;
    private ImageButton hintButton;
    private ImageButton quitButton;
    private MaterialCardView answerCard1;
    private MaterialCardView answerCard2;
    private MaterialCardView answerCard3;
    private MaterialCardView answerCard4;
    private CardView questionImageCard;
    
    // Additional fields
    private TextView tvQuestion;
    private TextView tvTimer;
    private TextView tvScore;
    private ImageView ivQuestionImage;
    private MaterialButton btnAnswer1;
    private MaterialButton btnAnswer2;
    private MaterialButton btnAnswer3;
    private MaterialButton btnAnswer4;
    private ProgressBar progressBar;
    private ImageButton btnFiftyFifty;
    private ImageButton btnSkip;
    private MaterialCardView questionCard;
    private Button btnBack;

    // Game state variables
    private boolean answered = false;
    private int timeLeft = 30;
    private int streak = 0;
    private boolean skipUsed = false;
    private Set<Integer> answeredQuestions = new HashSet<>();
    
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean fiftyFiftyUsed = false;
    private boolean skipQuestionUsed = false;
    private CountDownTimer timer;
    private long timeLeftInMillis = TIME_PER_QUESTION;
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MunteniaGamePrefs";

    // Sound effects
    private MediaPlayer correctSound;
    private MediaPlayer wrongSound;
    private MediaPlayer clockSound;
    private MediaPlayer winSound;

    private static final int QUESTION_COUNT = 10; // Number of questions to show
    private static final int CORRECT_POINTS = 10; // Base points for a correct answer
    private static final int TIME_BONUS_FACTOR = 1; // Points per second left
    private static final int STREAK_BONUS = 5; // Additional points for streak

    private List<QuizQuestion> quizQuestions;
    private List<QuizQuestion> selectedQuestions;

    // --- HYBRID SYSTEM FIELDS ---
    private static final String DATA_SOURCE_PREF_KEY = "data_source_preference";
    private static final String CACHE_KEY = "questions_cache_" + REGION + "_" + GAME_TYPE;
    private static final String CACHE_TIMESTAMP_KEY = CACHE_KEY + "_timestamp";
    private static final long CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24h
    private String dataSourcePreference = "ask_every_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set theme before super.onCreate
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muntenia_game);
        
        // Initialize enhanced systems first
        initializeEnhancedSystems();
        
        // Initialize views and game
        initViews();
        setupSounds();
        // Dialog setup pentru sursă și număr întrebări
        showInitialSetupDialog();
        
        // Convert to enhanced questions and filter by game mode
        // convertToEnhancedQuestions(); // This will be called after selectedQuestions is populated
        // filterQuestionsByGameMode(); // This will be called after selectedQuestions is populated
        
        // Setup lifelines
        setupLifelines();
        
        // Start with first question
        // displayQuestion(currentQuestionIndex); // This will be called after selectedQuestions is populated
        
        // Update UI
        updateScoreDisplay();
        
        // Start timer
        // startTimer(); // This will be called after selectedQuestions is populated
    }
    
    /**
     * Initialize modern game systems
     */
    private void initializeEnhancedSystems() {
        try {
            // Initialize the modern managers
            gameModeManager = new GameModeManager(this);
            difficultyManager = new DifficultyManager(this);
            progressTracker = new PlayerProgressTracker(this);
            
            // Initialize game enhancer
            gameEnhancer = new RegionGameEnhancer(this, "Muntenia");
            gameEnhancer.initializeGameMode(getIntent());
            
            // Get updated game constants
            gameConstants = gameEnhancer.updateGameConstants();
            
            Log.d(TAG, "Enhanced systems initialized successfully for Muntenia");
            Log.d(TAG, "Game constants - Time: " + (gameConstants != null ? gameConstants.timePerQuestion : TIME_PER_QUESTION) + "ms, Points: " + (gameConstants != null ? gameConstants.pointsPerCorrectAnswer : CORRECT_ANSWER_POINTS));
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing enhanced systems for Muntenia", e);
            // Fallback to basic values
        }
    }

    private void initViews() {
        timerTextView = findViewById(R.id.timerTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
        questionTextView = findViewById(R.id.questionTextView);
        factTextView = findViewById(R.id.factTextView);
        streakTextView = findViewById(R.id.streakTextView);
        answerButton1 = findViewById(R.id.answerButton1);
        answerButton2 = findViewById(R.id.answerButton2);
        answerButton3 = findViewById(R.id.answerButton3);
        answerButton4 = findViewById(R.id.answerButton4);
        finishButton = findViewById(R.id.finishButton);
        progressBar = findViewById(R.id.progressBar);
        questionImage = findViewById(R.id.questionImage);
        questionImageCard = findViewById(R.id.questionImageCard);
        fiftyFiftyButton = findViewById(R.id.fiftyFiftyButton);
        skipQuestionButton = findViewById(R.id.skipQuestionButton);
        answerCard1 = findViewById(R.id.answerCard1);
        answerCard2 = findViewById(R.id.answerCard2);
        answerCard3 = findViewById(R.id.answerCard3);
        answerCard4 = findViewById(R.id.answerCard4);
        
        // Initialize enhanced lifeline buttons (if available in layout)
        try {
            // Note: These buttons are not in the layout, so we'll use null checks
            hintButton = null;
            quitButton = null;
        } catch (Exception e) {
            Log.d(TAG, "Some enhanced lifeline buttons not available in layout");
        }

        // Set progress bar max and progress
        progressBar.setMax(QUESTION_COUNT);
        progressBar.setProgress(currentQuestionIndex);

        // Set click listeners for answer cards
        answerCard1.setOnClickListener(v -> checkAnswer(0));
        answerCard2.setOnClickListener(v -> checkAnswer(1));
        answerCard3.setOnClickListener(v -> checkAnswer(2));
        answerCard4.setOnClickListener(v -> checkAnswer(3));

        // Set click listener for finish button
        finishButton.setOnClickListener(v -> showFinalScore());
        finishButton.setVisibility(View.GONE);

        // Lifeline click listeners will be set in setupLifelines()
    }
    
    /**
     * Setup lifelines with enhanced functionality
     */
    private void setupLifelines() {
        // Check if lifelines are allowed in current game mode
        boolean lifelinesAllowed = gameModeManager != null ? gameModeManager.areLifelinesAllowed() : true;
        
        // 50:50 lifeline
        if (fiftyFiftyButton != null) {
            boolean canUseFiftyFifty = lifelinesAllowed && !fiftyFiftyUsed && 
                (difficultyManager == null || difficultyManager.canUseLifeline(fiftyFiftyUsed ? 1 : 0));
            fiftyFiftyButton.setEnabled(canUseFiftyFifty);
            fiftyFiftyButton.setAlpha(canUseFiftyFifty ? 1.0f : 0.5f);
            fiftyFiftyButton.setOnClickListener(v -> useFiftyFifty());
        }

        // Hint lifeline
        if (hintButton != null) {
            boolean canUseHint = lifelinesAllowed && !hintUsed && 
                (difficultyManager == null || difficultyManager.canUseLifeline(hintUsed ? 1 : 0));
            hintButton.setEnabled(canUseHint);
            hintButton.setAlpha(canUseHint ? 1.0f : 0.5f);
            hintButton.setOnClickListener(v -> useHint());
        }
        
        // Skip question lifeline
        if (skipQuestionButton != null) {
            boolean canUseSkip = lifelinesAllowed && !skipQuestionUsed && 
                (difficultyManager == null || difficultyManager.canUseLifeline(skipQuestionUsed ? 1 : 0));
            skipQuestionButton.setEnabled(canUseSkip);
            skipQuestionButton.setAlpha(canUseSkip ? 1.0f : 0.5f);
            skipQuestionButton.setOnClickListener(v -> skipQuestion());
        }
        
        // Quit button
        if (quitButton != null) {
            quitButton.setOnClickListener(v -> showQuitConfirmation());
        }
    }

    private void setupSounds() {
        correctSound = MediaPlayer.create(this, R.raw.win_sound);
        wrongSound = MediaPlayer.create(this, R.raw.lose_sound);
        // Replace missing sound with existing ones
        clockSound = MediaPlayer.create(this, R.raw.clock_thinking);
        winSound = MediaPlayer.create(this, R.raw.win_sound);
    }
    
    /**
     * Use hint lifeline - provides intelligent hints based on question category
     */
    private void useHint() {
        if (hintUsed) {
            Toast.makeText(this, "Ai folosit deja indiciul!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Mark hint as used
        hintUsed = true;
        if (hintButton != null) {
            hintButton.setEnabled(false);
            hintButton.setAlpha(0.5f);
        }
        
        // Get current enhanced question for category-based hint
        if (enhancedQuestions != null && currentQuestionIndex < enhancedQuestions.size()) {
            EnhancedQuestionModel enhancedQuestion = enhancedQuestions.get(currentQuestionIndex);
            showCategoryBasedHint(enhancedQuestion.getCategory());
        } else {
            // Fallback hint for Muntenia
            new MaterialAlertDialogBuilder(this)
                    .setTitle("💡 Indiciu")
                    .setMessage("Indiciu: Gândește-te la istoria, geografia și tradițiile specifice Munteniei - regiunea centrală a României cu București ca centru.")
                    .setPositiveButton("Am înțeles", null)
                    .show();
        }
        
        Log.d(TAG, "Hint used for question " + currentQuestionIndex);
    }
    
    /**
     * Show category-based hint
     */
    private void showCategoryBasedHint(EnhancedQuestionModel.Category category) {
        String hintMessage = "";
        String hintTitle = "💡 Indiciu - " + category.displayName;
        
        switch (category) {
            case HISTORY:
                hintMessage = "Concentrează-te pe evenimentele istorice, domnitori și perioade importante din istoria Munteniei.";
                break;
            case GEOGRAPHY:
                hintMessage = "Gândește-te la relief, râuri, orașe și caracteristicile geografice specifice Munteniei.";
                break;
            case ARCHITECTURE:
                hintMessage = "Focusează-te pe monumente, palate, mănăstiri și arhitectura tradițională din Muntenia.";
                break;
            case CULTURE:
                hintMessage = "Consideră tradițiile, obiceiurile, festivalurile și aspectele culturale specifice Munteniei.";
                break;
            default:
                hintMessage = "Gândește-te la aspectele generale ale Munteniei - istoria, geografia și cultura regiunii.";
                break;
        }
        
        new MaterialAlertDialogBuilder(this)
                .setTitle(hintTitle)
                .setMessage(hintMessage)
                .setPositiveButton("Am înțeles", null)
                .show();
    }
    
    /**
     * Show quit confirmation dialog with current statistics
     */
    private void showQuitConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("🚪 Ieșire din Quiz")
                .setMessage("Ești sigur că vrei să ieși? Progresul va fi pierdut!")
                .setPositiveButton("Da, ieși", (dialog, which) -> finish())
                .setNegativeButton("Continuă", null)
                .show();
    }

    private void initQuestions() {
        quizQuestions = new ArrayList<>();

        // Muntenia Quiz Questions
        quizQuestions.add(new QuizQuestion(
                "Care este capitala regiunii istorice Muntenia?",
                Arrays.asList("București", "Ploiești", "Pitești", "Târgoviște"),
                0,
                "București este capitala României și a fost istoric capitala Munteniei.",
                R.drawable.bucuresti_parlament));

        quizQuestions.add(new QuizQuestion(
                "Muntenia este străbătută de la nord la sud de râul:",
                Arrays.asList("Olt", "Siret", "Ialomița", "Argeș"),
                3,
                "Râul Argeș este unul dintre cele mai importante cursuri de apă din Muntenia.",
                R.drawable.muntenia_bg_simple));

        quizQuestions.add(new QuizQuestion(
                "Ce monument UNESCO se află în Muntenia?",
                Arrays.asList("Mănăstirea Voroneț", "Cetatea Sighișoara", "Mănăstirea Horezu", "Delta Dunării"),
                2,
                "Mănăstirea Horezu a fost construită între 1690 și 1697 și este inclusă în patrimoniul UNESCO.",
                R.drawable.muntenia_bg_simple));

        quizQuestions.add(new QuizQuestion(
                "Care dintre următoarele orașe NU este în Muntenia?",
                Arrays.asList("Giurgiu", "Călărași", "Slatina", "Focșani"),
                3,
                "Focșani se află în Moldova, nu în Muntenia.",
                R.drawable.muntenia_bg_simple));

        quizQuestions.add(new QuizQuestion(
                "Palatul Cantacuzino se află în ce localitate din Muntenia?",
                Arrays.asList("Sinaia", "Bușteni", "Breaza", "Azuga"),
                0,
                "Palatul Cantacuzino din Sinaia este un exemplu remarcabil de arhitectură în stil neoclasic.",
                R.drawable.bucuresti_ateneu));

        quizQuestions.add(new QuizQuestion(
                "Ce relief predomină în Muntenia?",
                Arrays.asList("Munți", "Dealuri", "Câmpie", "Podiș"),
                2,
                "Câmpia Română ocupă cea mai mare parte din suprafața Munteniei.",
                R.drawable.buzau_munti));

        quizQuestions.add(new QuizQuestion(
                "Care dintre următorii domnitori a fost asociat cu Muntenia?",
                Arrays.asList("Ștefan cel Mare", "Mircea cel Bătrân", "Decebal", "Petru Rareș"),
                1,
                "Mircea cel Bătrân a fost domnitor al Țării Românești (Munteniei) între 1386 și 1418.",
                R.drawable.muntenia_bg_simple));

        quizQuestions.add(new QuizQuestion(
                "Curtea de Argeș este faimoasă pentru:",
                Arrays.asList("Castelul Peleș", "Mănăstirea și Catedrala de la Curtea de Argeș", "Delta Dunării", "Podul de la Cernavodă"),
                1,
                "Mănăstirea Curtea de Argeș este una dintre cele mai valoroase monumente de arhitectură din România.",
                R.drawable.targoviste_curtea));

        quizQuestions.add(new QuizQuestion(
                "Ce stațiune montană se află în Muntenia?",
                Arrays.asList("Vatra Dornei", "Sinaia", "Sovata", "Băile Felix"),
                1,
                "Sinaia, cunoscută și ca 'Perla Carpaților', este una dintre cele mai populare stațiuni montane din Muntenia.",
                R.drawable.buzau_munti));

        quizQuestions.add(new QuizQuestion(
                "Ce industrie a fost tradițional dezvoltată în Muntenia?",
                Arrays.asList("Extracția de sare", "Extracția petrolului", "Industria textilă", "Construcții navale"),
                1,
                "Muntenia a fost una dintre primele regiuni din lume unde s-a exploatat petrolul la scară industrială.",
                R.drawable.ploiesti_muzeu));

        quizQuestions.add(new QuizQuestion(
                "Câmpia Bărăganului face parte din:",
                Arrays.asList("Câmpia Transilvaniei", "Câmpia Română", "Podișul Moldovei", "Câmpia de Vest"),
                1,
                "Câmpia Bărăganului este o subdiviziune a Câmpiei Române și se află în partea de est a Munteniei.",
                R.drawable.muntenia_bg_simple));

        quizQuestions.add(new QuizQuestion(
                "Ce lac glaciar se află în Munții Făgăraș, în partea muntenească?",
                Arrays.asList("Lacul Roșu", "Lacul Sfânta Ana", "Lacul Bâlea", "Lacul Vidraru"),
                2,
                "Lacul Bâlea este un lac glaciar situat la altitudinea de 2.034 m în Munții Făgăraș.",
                R.drawable.buzau_munti));

        quizQuestions.add(new QuizQuestion(
                "Care a fost capitala Țării Românești înainte de București?",
                Arrays.asList("Pitești", "Târgoviște", "Craiova", "Ploiești"),
                1,
                "Târgoviște a fost capitala Țării Românești între secolele XV-XVII.",
                R.drawable.targoviste_curtea));

        quizQuestions.add(new QuizQuestion(
                "Ce tip de cultură agricolă predomină în Câmpia Română?",
                Arrays.asList("Viță de vie", "Pomi fructiferi", "Cereale", "Cartofi"),
                2,
                "Datorită solului fertil, în Câmpia Română predomină culturile de cereale, în special grâu și porumb.",
                R.drawable.muntenia_bg_simple));

        quizQuestions.add(new QuizQuestion(
                "Transfăgărășanul traversează:",
                Arrays.asList("Munții Retezat", "Munții Bucegi", "Munții Făgăraș", "Munții Apuseni"),
                2,
                "Transfăgărășanul este un drum montan spectaculos care traversează Munții Făgăraș.",
                R.drawable.buzau_munti));
    }

    private void selectRandomQuestions() {
        // Shuffle the questions
        Collections.shuffle(quizQuestions);
        
        // Select the first QUESTION_COUNT questions
        selectedQuestions = new ArrayList<>(quizQuestions.subList(0, Math.min(QUESTION_COUNT, quizQuestions.size())));
        
        // Convert to enhanced questions for advanced tracking
        convertToEnhancedQuestions();
    }
    
    /**
     * Convert regular questions to enhanced questions for advanced tracking
     */
    private void convertToEnhancedQuestions() {
        if (selectedQuestions != null) {
            enhancedQuestions = convertToEnhancedQuestions(selectedQuestions);
            Log.d(TAG, "Converted " + selectedQuestions.size() + " questions to enhanced format for Muntenia");
        } else {
            Log.w(TAG, "Selected questions not available, skipping enhanced question conversion");
        }
    }
    
    /**
     * Filter questions based on current game mode
     */
    private void filterQuestionsByGameMode() {
        if (enhancedQuestions != null && !enhancedQuestions.isEmpty()) {
            enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
            Log.d(TAG, "Filtered questions for game mode: " + gameModeManager.getCurrentGameMode().displayName);
        }
    }
    
    /**
     * Convert QuizQuestion objects to EnhancedQuestionModel objects
     */
    private List<EnhancedQuestionModel> convertToEnhancedQuestions(List<QuizQuestion> questions) {
        List<EnhancedQuestionModel> enhanced = new ArrayList<>();
        
        for (QuizQuestion question : questions) {
            // Map questions to categories based on content
            EnhancedQuestionModel.Category category = inferCategory(question.getQuestion());
            EnhancedQuestionModel.Difficulty difficulty = inferDifficulty(question);
            
            // Get the correct answer and incorrect answers
            List<String> answers = question.getAnswers();
            String correctAnswer = answers.get(question.getCorrectAnswerIndex());
            List<String> incorrectAnswers = new ArrayList<>(answers);
            incorrectAnswers.remove(question.getCorrectAnswerIndex());
            
            EnhancedQuestionModel enhancedQuestion = new EnhancedQuestionModel(
                question.getQuestion(),
                correctAnswer,
                incorrectAnswers,
                question.getImageResource(),
                question.getFact(),
                category,
                difficulty,
                new String[]{}
            );
            
            enhanced.add(enhancedQuestion);
        }
        
        Log.d(TAG, "Converted " + questions.size() + " questions to enhanced format");
        return enhanced;
    }
    
    /**
     * Infer category based on question content
     */
    private EnhancedQuestionModel.Category inferCategory(String questionText) {
        String text = questionText.toLowerCase();
        
        if (text.contains("capital") || text.contains("oraș") || text.contains("localitate")) {
            return EnhancedQuestionModel.Category.GEOGRAPHY;
        } else if (text.contains("monument") || text.contains("mănăstire") || text.contains("palat")) {
            return EnhancedQuestionModel.Category.ARCHITECTURE;
        } else if (text.contains("domnitor") || text.contains("istorie") || text.contains("secol")) {
            return EnhancedQuestionModel.Category.HISTORY;
        } else if (text.contains("relief") || text.contains("câmpie") || text.contains("râu")) {
            return EnhancedQuestionModel.Category.GEOGRAPHY;
        } else if (text.contains("stațiune") || text.contains("turism")) {
            return EnhancedQuestionModel.Category.CULTURE;
        } else {
            return EnhancedQuestionModel.Category.CULTURE; // Default
        }
    }
    
    /**
     * Infer difficulty based on question content
     */
    private EnhancedQuestionModel.Difficulty inferDifficulty(QuizQuestion question) {
        String text = question.getQuestion().toLowerCase();
        int questionLength = text.length();
        
        // Short and direct questions are easier
        if (questionLength < 50 && 
            (text.contains("care") || text.contains("unde") || text.contains("ce"))) {
            return EnhancedQuestionModel.Difficulty.EASY;
        }
        
        // Questions with specific details or exact dates are harder
        if (text.contains("anul") || text.contains("secolul") || text.contains("exacte") ||
            text.contains("precisez") || questionLength > 120) {
            return EnhancedQuestionModel.Difficulty.HARD;
        }
        
        // Very specific questions or with multiple elements
        if (text.contains("dintre următoarele") && text.contains("nu") ||
            text.contains("toate") || text.contains("exclusiv")) {
            return EnhancedQuestionModel.Difficulty.EXPERT;
        }
        
        return EnhancedQuestionModel.Difficulty.MEDIUM; // Default
    }

    @SuppressLint("SetTextI18n")
    private void displayQuestion(int index) {
        if (index >= selectedQuestions.size()) {
            showFinalScore();
            return;
        }

        // Set question start time for tracking
        questionStartTime = System.currentTimeMillis();
        answerSelected = false;

        timeLeftInMillis = gameModeManager != null ? gameModeManager.getTimePerQuestion() : TIME_PER_QUESTION;
        QuizQuestion currentQuestion = selectedQuestions.get(index);
        questionTextView.setText(currentQuestion.getQuestion());

        // Hide fact text initially
        factTextView.setVisibility(View.GONE);

        // Reset button styles
        resetButtonStyles();

        // Set answers
        List<String> answers = currentQuestion.getAnswers();
        answerButton1.setText(answers.get(0));
        answerButton2.setText(answers.get(1));
        answerButton3.setText(answers.get(2));
        answerButton4.setText(answers.get(3));

        // Reset button click states
        enableAnswerButtons(true);

        // Setup image if available
        int imageResource = currentQuestion.getImageResource();
        if (imageResource != 0) {
            questionImage.setImageResource(imageResource);
            questionImageCard.setVisibility(View.VISIBLE);
        } else {
            questionImageCard.setVisibility(View.GONE);
        }

        // Update progress
        progressBar.setProgress(index + 1);
        
        // Cancel previous timer if running
        if (timer != null) {
            timer.cancel();
        }
        
        // Start new timer
        startTimer();
        
        // Continue with the next question
        answered = false;
    }

    private void resetButtonStyles() {
        // Reset button styles to default
        answerButton1.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.rom_card_background));
        answerButton2.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.rom_card_background));
        answerButton3.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.rom_card_background));
        answerButton4.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.rom_card_background));
        
        answerButton1.setTextColor(ContextCompat.getColor(this, R.color.rom_text));
        answerButton2.setTextColor(ContextCompat.getColor(this, R.color.rom_text));
        answerButton3.setTextColor(ContextCompat.getColor(this, R.color.rom_text));
        answerButton4.setTextColor(ContextCompat.getColor(this, R.color.rom_text));
        
        // Reset card styles
        answerCard1.setStrokeColor(ContextCompat.getColor(this, R.color.muntenia_primary));
        answerCard2.setStrokeColor(ContextCompat.getColor(this, R.color.muntenia_primary));
        answerCard3.setStrokeColor(ContextCompat.getColor(this, R.color.muntenia_primary));
        answerCard4.setStrokeColor(ContextCompat.getColor(this, R.color.muntenia_primary));
        
        answerCard1.setStrokeWidth(2);
        answerCard2.setStrokeWidth(2);
        answerCard3.setStrokeWidth(2);
        answerCard4.setStrokeWidth(2);
    }

    private void startTimer() {
        // Check if timerTextView is initialized
        if (timerTextView == null) {
            return; // Skip timer if view is not initialized
        }
        
        if (timer != null) {
            timer.cancel();
        }
        
        // Get time per question from game mode manager
        int timePerQuestion = gameModeManager != null ? gameModeManager.getTimePerQuestion() / 1000 : 30;
        timeLeft = timePerQuestion;
        timerTextView.setText(String.valueOf(timeLeft));
        
        timer = new CountDownTimer(timePerQuestion * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = (int) (millisUntilFinished / 1000);
                timerTextView.setText(String.valueOf(timeLeft));
                
                // Play clock sound on last 5 seconds
                if (timeLeft <= 5 && !answered) {
                    if (clockSound != null && !clockSound.isPlaying()) {
                        clockSound.start();
                    }
                }
            }
            
            @Override
            public void onFinish() {
                if (!answered) {
                    timeLeft = 0;
                    timerTextView.setText("0");
                    handleTimeOut();
                }
            }
        }.start();
    }

    private void checkAnswer(int selectedAnswerIndex) {
        if (currentQuestionIndex >= selectedQuestions.size()) {
            showFinalScore();
            return;
        }
        // Prevent multiple answers
        if (answered || answerSelected) return;
        
        // Mark question as answered
        answered = true;
        answerSelected = true;
        
        // Calculate time taken for this question
        long questionTime = System.currentTimeMillis() - questionStartTime;
        totalTime += questionTime;
        
        // Stop timer
        if (timer != null) {
            timer.cancel();
        }
        
        QuizQuestion currentQuestion = selectedQuestions.get(currentQuestionIndex);
        int correctAnswerIndex = currentQuestion.getCorrectAnswerIndex();
        boolean isCorrect = selectedAnswerIndex == correctAnswerIndex;
        MaterialCardView selectedCard = getCardByIndex(selectedAnswerIndex);
        MaterialCardView correctCard = getCardByIndex(correctAnswerIndex);
        
        // Haptic feedback based on answer (simplified)
        // Note: Haptic feedback can be implemented here if needed
        
        // Disable all buttons
        enableAnswerButtons(false);
        
        // Highlight correct answer
        MaterialButton correctButton = getButtonByIndex(correctAnswerIndex);
        correctButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.correct_answer));
        correctButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        correctCard.setStrokeColor(ContextCompat.getColor(this, R.color.correct_answer));
        correctCard.setStrokeWidth(4);
        
        // If selected answer is wrong, highlight it in red
        if (selectedAnswerIndex != correctAnswerIndex) {
            // Wrong answer
            MaterialButton selectedButton = getButtonByIndex(selectedAnswerIndex);
            selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.wrong_answer));
            selectedButton.setTextColor(ContextCompat.getColor(this, R.color.white));
            selectedCard.setStrokeColor(ContextCompat.getColor(this, R.color.wrong_answer));
            selectedCard.setStrokeWidth(4);
            
            // Play wrong sound
            if (wrongSound != null) {
                wrongSound.start();
            }
            
            // Reset streak
            streak = 0;
        } else {
            // Correct answer
            correctAnswers++;
            
            // Calculate score with game mode bonus
            int basePoints = CORRECT_ANSWER_POINTS;
            int timeBonus = (int) (timeLeftInMillis / 1000) * TIME_BONUS_POINTS;
            int modeBonus = gameModeManager != null ? gameModeManager.calculateModeBonus(basePoints, isCorrect, questionTime) : 0;
            int questionScore = basePoints + timeBonus + modeBonus;
            
            // Apply difficulty multiplier
            if (difficultyManager != null) {
                questionScore = difficultyManager.calculateFinalScore(questionScore);
            }
            
            score += questionScore;
            
            // Increase streak
            streak++;
            
            // Update max streak
            if (streak > maxStreak) {
                maxStreak = streak;
            }
            
            // Play correct sound
            if (correctSound != null) {
                correctSound.start();
            }
            
            // Add streak bonus if applicable
            if (streak >= 3) {
                score += (streak - 2) * 10; // 10 bonus points per streak above 2
                Toast.makeText(this, "Bonus pentru " + streak + " răspunsuri consecutive!", Toast.LENGTH_SHORT).show();
            }
        }
        
        // Show fact if available
        String fact = currentQuestion.getFact();
        if (fact != null && !fact.isEmpty()) {
            factTextView.setText(fact);
            factTextView.setVisibility(View.VISIBLE);
        }
        
        // Update score display
        updateScoreDisplay();
        
        // Track answer with progress tracker
        if (enhancedQuestions != null && currentQuestionIndex < enhancedQuestions.size()) {
            EnhancedQuestionModel enhancedQuestion = enhancedQuestions.get(currentQuestionIndex);
            progressTracker.trackAnswer(enhancedQuestion, isCorrect, questionTime);
        }
        
        // Move to next question after delay
        new Handler().postDelayed(() -> {
            currentQuestionIndex++;
            
            // Check if we're at the end
            if (currentQuestionIndex >= selectedQuestions.size()) {
                hideQuizElements();
                finishButton.setVisibility(View.VISIBLE);
            } else {
                displayQuestion(currentQuestionIndex);
            }
        }, 2000);
    }
    
    private MaterialCardView getCardByIndex(int index) {
        switch (index) {
            case 0: return answerCard1;
            case 1: return answerCard2;
            case 2: return answerCard3;
            case 3: return answerCard4;
            default: return answerCard1;
        }
    }

    private MaterialButton getButtonByIndex(int index) {
        switch (index) {
            case 0: return answerButton1;
            case 1: return answerButton2;
            case 2: return answerButton3;
            case 3: return answerButton4;
            default: return answerButton1;
        }
    }

    private void handleTimeOut() {
        answered = true;
        
        QuizQuestion currentQuestion = selectedQuestions.get(currentQuestionIndex);
        int correctAnswerIndex = currentQuestion.getCorrectAnswerIndex();
        
        // Highlight the correct answer
        MaterialButton correctButton = getButtonByIndex(correctAnswerIndex);
        correctButton.setBackgroundTintList(getColorStateList(R.color.rom_correct_answer));
        
        // Play wrong sound
        if (wrongSound != null) {
            wrongSound.start();
        }
        
        // Reset streak
        streak = 0;
        streakTextView.setText(String.format(Locale.getDefault(), "🔥 Reușite consecutive: %d", streak));
        
        // Show fact
        factTextView.setText(currentQuestion.getFact());
        factTextView.setVisibility(View.VISIBLE);
        
        // Disable all answer buttons
        enableAnswerButtons(false);
        
        // Show toast
        Toast.makeText(this, "Timpul a expirat!", Toast.LENGTH_SHORT).show();
        
        // Show next question after delay or show finish button if this is the last question
        new Handler().postDelayed(() -> {
            if (currentQuestionIndex < selectedQuestions.size() - 1) {
                currentQuestionIndex++;
                displayQuestion(currentQuestionIndex);
                startTimer();
            } else {
                // This was the last question
                hideQuizElements();
                finishButton.setVisibility(View.VISIBLE);
            }
        }, 3000);
    }

    private void enableAnswerButtons(boolean enable) {
        answerCard1.setClickable(enable);
        answerCard2.setClickable(enable);
        answerCard3.setClickable(enable);
        answerCard4.setClickable(enable);
        
        answerCard1.setEnabled(enable);
        answerCard2.setEnabled(enable);
        answerCard3.setEnabled(enable);
        answerCard4.setEnabled(enable);
        
        answerButton1.setClickable(false); // Buttons are always not clickable, we use the cards
        answerButton2.setClickable(false);
        answerButton3.setClickable(false);
        answerButton4.setClickable(false);
    }

    private void updateScoreDisplay() {
        scoreTextView.setText(String.format(Locale.getDefault(), "Scor: %d", score));
    }

    private void useFiftyFifty() {
        if (fiftyFiftyUsed) {
            Toast.makeText(this, "Ai folosit deja opțiunea 50:50!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        fiftyFiftyUsed = true;
        fiftyFiftyButton.setEnabled(false);
        fiftyFiftyButton.setAlpha(0.5f);
        
        // Haptic feedback
        if (gameEnhancer != null) {
            gameEnhancer.performHapticFeedback(HapticFeedbackType.LIFELINE);
        }
        
        // Get current question and correct answer
        QuizQuestion currentQuestion = selectedQuestions.get(currentQuestionIndex);
        int correctAnswerIndex = currentQuestion.getCorrectAnswerIndex();
        
        // Create list of wrong answer indices
        List<Integer> wrongAnswerIndices = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (i != correctAnswerIndex) {
                wrongAnswerIndices.add(i);
            }
        }
        
        // Randomly select 2 wrong answers to hide
        Collections.shuffle(wrongAnswerIndices);
        List<Integer> answersToHide = wrongAnswerIndices.subList(0, 2);
        
        // Hide selected wrong answers
        for (int index : answersToHide) {
            MaterialCardView card = getCardByIndex(index);
            card.setVisibility(View.INVISIBLE);
            card.setClickable(false);
        }
        
        Toast.makeText(this, "Două răspunsuri greșite au fost eliminate!", Toast.LENGTH_SHORT).show();
    }

    private void skipQuestion() {
        if (skipQuestionUsed) {
            Toast.makeText(this, "Ai folosit deja opțiunea de a sări peste o întrebare!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        skipQuestionUsed = true;
        skipQuestionButton.setEnabled(false);
        skipQuestionButton.setAlpha(0.5f);
        
        // Haptic feedback
        if (gameEnhancer != null) {
            gameEnhancer.performHapticFeedback(HapticFeedbackType.LIFELINE);
        }
        
        // Cancel the current timer
        if (timer != null) {
            timer.cancel();
        }
        
        // Show toast message
        Toast.makeText(this, "Ai sărit peste această întrebare!", Toast.LENGTH_SHORT).show();
        
        // Move to the next question
        currentQuestionIndex++;
        
        // Check if we're at the end
        if (currentQuestionIndex >= selectedQuestions.size()) {
            hideQuizElements();
            finishButton.setVisibility(View.VISIBLE);
        } else {
            displayQuestion(currentQuestionIndex);
        }
    }

    private void showFinalScore() {
        // Cancel timer if it's running
        if (timer != null) {
            timer.cancel();
        }
        
        // Update difficulty based on performance
        if (difficultyManager != null) {
            difficultyManager.updateDifficultyAfterGame(correctAnswers, selectedQuestions.size(), totalTime);
        }
        
        // Finish game with progress tracker
        if (progressTracker != null) {
            String gameModeName = gameModeManager != null ? gameModeManager.getCurrentGameMode().displayName : "Quiz Clasic";
            progressTracker.finishGame(gameModeName, correctAnswers, selectedQuestions.size(), totalTime);
        }
        
        // Save high score if current score is higher
        saveHighScore();
        
        // Play win sound
        if (winSound != null) {
            winSound.start();
        }
        
        // Save game result to leaderboard
        saveGameResultToLeaderboard();
        
        // Enhanced statistics
        float accuracy = correctAnswers > 0 ? (float) correctAnswers / selectedQuestions.size() * 100 : 0;
        float avgTimePerQuestion = totalTime > 0 ? (float) totalTime / selectedQuestions.size() / 1000.0f : 0;
        
        int totalQuestions = Math.min(currentQuestionIndex + 1, selectedQuestions.size());
        
        // Start modernized GameOver activity with stats
        Intent intent = new Intent(this, GameOverActivity.class);
        intent.putExtra("final_score", score);
        intent.putExtra("total_questions", totalQuestions);
        intent.putExtra("total_time_spent", totalTime);
        intent.putExtra("game_mode", gameModeManager != null ? gameModeManager.getCurrentGameMode().displayName : "Quiz Clasic");
        startActivity(intent);
        finish();
    }
    
    /**
     * Save game result to Firestore leaderboard
     */
    private void saveGameResultToLeaderboard() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "User not logged in, skipping leaderboard save");
            return;
        }

        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            
            // Calculate final statistics
            float accuracy = correctAnswers > 0 ? (float) correctAnswers / selectedQuestions.size() * 100 : 0;
            float avgTimePerQuestion = totalTime > 0 ? (float) totalTime / selectedQuestions.size() / 1000.0f : 0;
            
            // Create game result data
            Map<String, Object> gameResult = new HashMap<>();
            gameResult.put("userId", currentUser.getUid());
            gameResult.put("userName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonim");
            gameResult.put("region", "Muntenia");
            gameResult.put("gameType", "quiz");
            gameResult.put("score", score);
            gameResult.put("correctAnswers", correctAnswers);
            gameResult.put("totalQuestions", selectedQuestions.size());
            gameResult.put("accuracy", accuracy);
            gameResult.put("maxStreak", maxStreak);
            gameResult.put("totalTime", totalTime);
            gameResult.put("averageTimePerQuestion", avgTimePerQuestion);
            gameResult.put("timestamp", FieldValue.serverTimestamp());
            gameResult.put("date", new Date());
            
            // Add lifeline usage
            gameResult.put("fiftyFiftyUsed", fiftyFiftyUsed);
            gameResult.put("skipUsed", skipQuestionUsed);
            gameResult.put("hintUsed", hintUsed);
            
            // Save to main quiz_results collection
            db.collection("quiz_results")
                    .add(gameResult)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Game result saved to leaderboard: " + documentReference.getId());
                        
                        // Update user's best score for Muntenia
                        updateUserBestScore(currentUser.getUid(), score);
                        
                        // Add to user's activity history
                        addToUserActivityHistory(currentUser.getUid(), gameResult);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving game result to leaderboard", e);
                    });
                    
        } catch (Exception e) {
            Log.e(TAG, "Error in saveGameResultToLeaderboard", e);
        }
    }
    
    /**
     * Update user's best score for Muntenia region
     */
    private void updateUserBestScore(String userId, int newScore) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long currentBestScore = documentSnapshot.getLong("bestScoreMuntenia");
                        if (currentBestScore == null || newScore > currentBestScore) {
                            // Update best score
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("bestScoreMuntenia", newScore);
                            updates.put("lastPlayedMuntenia", FieldValue.serverTimestamp());
                            
                            db.collection("users").document(userId)
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User best score updated for Muntenia"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error updating user best score", e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error getting user document", e));
    }
    
    /**
     * Add game result to user's activity history
     */
    private void addToUserActivityHistory(String userId, Map<String, Object> gameResult) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Create activity entry
        Map<String, Object> activity = new HashMap<>();
        activity.put("type", "quiz_completed");
        activity.put("region", "Muntenia");
        activity.put("score", gameResult.get("score"));
        activity.put("accuracy", gameResult.get("accuracy"));
        activity.put("timestamp", FieldValue.serverTimestamp());
        
        db.collection("users").document(userId)
                .collection("recent_activities")
                .add(activity)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Activity added to user history");
                    
                    // Keep only the last 20 activities
                    cleanupUserActivityHistory(userId);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error adding activity to user history", e));
    }
    
    /**
     * Keep only the last 20 activities for the user
     */
    private void cleanupUserActivityHistory(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("users").document(userId)
                .collection("recent_activities")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() == 20) {
                        // Get the timestamp of the 20th document
                        Date cutoffDate = queryDocumentSnapshots.getDocuments().get(19).getDate("timestamp");
                        
                        if (cutoffDate != null) {
                            // Delete all activities older than the cutoff
                            db.collection("users").document(userId)
                                    .collection("recent_activities")
                                    .whereLessThan("timestamp", cutoffDate)
                                    .get()
                                    .addOnSuccessListener(oldActivities -> {
                                        for (com.google.firebase.firestore.DocumentSnapshot doc : oldActivities) {
                                            doc.getReference().delete();
                                        }
                                        Log.d(TAG, "Cleaned up old activities, deleted " + oldActivities.size() + " entries");
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error cleaning up user activity history", e));
    }

    private void resetGame() {
        // Reset game state
        currentQuestionIndex = 0;
        score = 0;
        streak = 0;
        answered = false;
        fiftyFiftyUsed = false;
        skipUsed = false;
        
        // Reset UI
        updateScoreDisplay();
        fiftyFiftyButton.setAlpha(1.0f);
        fiftyFiftyButton.setEnabled(true);
        skipQuestionButton.setAlpha(1.0f);
        skipQuestionButton.setEnabled(true);
        
        // Select new random questions
        selectRandomQuestions();
        
        // Start the game
        displayQuestion(currentQuestionIndex);
        startTimer();
    }

    private void loadHighScore() {
        SharedPreferences prefs = getSharedPreferences("MunteniaGamePrefs", MODE_PRIVATE);
        int highScore = prefs.getInt("highScore", 0);
        
        // Display high score somewhere if needed
    }

    private void saveHighScore() {
        SharedPreferences prefs = getSharedPreferences("MunteniaGamePrefs", MODE_PRIVATE);
        int highScore = prefs.getInt("highScore", 0);
        
        if (score > highScore) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highScore", score);
            editor.apply();
        }
    }

    @Override
    public void onBackPressed() {
        showQuitConfirmation();
    }

    // Quiz Question class
    private static class QuizQuestion {
        private final String question;
        private final List<String> answers;
        private final int correctAnswerIndex;
        private final String fact;
        private final int imageResource;

        public QuizQuestion(String question, List<String> answers, int correctAnswerIndex, String fact, int imageResource) {
            this.question = question;
            this.answers = answers;
            this.correctAnswerIndex = correctAnswerIndex;
            this.fact = fact;
            this.imageResource = imageResource;
        }

        public String getQuestion() {
            return question;
        }

        public List<String> getAnswers() {
            return answers;
        }

        public int getCorrectAnswerIndex() {
            return correctAnswerIndex;
        }

        public String getFact() {
            return fact;
        }

        public int getImageResource() {
            return imageResource;
        }
    }

    // --- DIALOG INIȚIAL PENTRU SURSA ȘI NUMĂRUL DE ÎNTREBĂRI ---
    private void showInitialSetupDialog() {
        boolean hasInternet = isInternetAvailable();
        boolean hasLocalCache = checkIfLocalCacheExistsHybrid();
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
        int[] numQuestionsOptions = {5, 10, 15, 20, 30, 50};
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quiz_setup, null);
        android.widget.Spinner sourceSpinner = dialogView.findViewById(R.id.sourceSpinner);
        android.widget.Spinner numQuestionsSpinner = dialogView.findViewById(R.id.numQuestionsSpinner);
        android.widget.ArrayAdapter<String> sourceAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sources);
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sourceSpinner.setAdapter(sourceAdapter);
        android.widget.ArrayAdapter<Integer> numAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, toIntegerList(numQuestionsOptions));
        numAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numQuestionsSpinner.setAdapter(numAdapter);
        new MaterialAlertDialogBuilder(this)
            .setTitle("Setări quiz")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Start", (dialog, which) -> {
                int sourceIndex = sourceSpinner.getSelectedItemPosition();
                String selectedSource = sources[sourceIndex];
                int numQuestions = (Integer) numQuestionsSpinner.getSelectedItem();
                if (selectedSource.contains("Baza de Date")) {
                    saveDataSourcePreferenceHybrid("always_database");
                } else if (selectedSource.contains("Cache Local")) {
                    saveDataSourcePreferenceHybrid("always_cache");
                } else if (selectedSource.contains("Automat")) {
                    saveDataSourcePreferenceHybrid("auto");
                }
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putInt("quiz_num_questions", numQuestions).apply();
                continueHybridLoadWithNumQuestions(numQuestions);
            })
            .setNegativeButton("Anulează", (dialog, which) -> finish())
            .show();
    }
    private java.util.List<Integer> toIntegerList(int[] arr) {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (int v : arr) list.add(v);
        return list;
    }
    private void continueHybridLoadWithNumQuestions(int numQuestions) {
        boolean hasInternet = isInternetAvailable();
        boolean hasLocalCache = checkIfLocalCacheExistsHybrid();
        switch (dataSourcePreference) {
            case "always_database":
                if (hasInternet) {
                    loadQuestionsFromDatabaseHybrid(numQuestions);
                } else {
                    showNoInternetForPreferredDatabaseDialogHybrid(numQuestions);
                }
                break;
            case "always_cache":
                if (hasLocalCache) {
                    loadQuestionsFromLocalCacheHybrid(numQuestions);
                } else {
                    showNoCacheForPreferredLocalDialogHybrid(numQuestions);
                }
                break;
            case "auto":
                if (hasInternet) {
                    loadQuestionsFromDatabaseHybrid(numQuestions);
                } else if (hasLocalCache) {
                    loadQuestionsFromLocalCacheHybrid(numQuestions);
                } else {
                    showDataSourceSelectionDialogHybrid(numQuestions);
                }
                break;
            case "ask_every_time":
            default:
                showDataSourceSelectionDialogHybrid(numQuestions);
                break;
        }
    }
    // --- METODE HIBRID ---
    private boolean isInternetAvailable() {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm != null) {
            android.net.NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        }
        return false;
    }
    private boolean checkIfLocalCacheExistsHybrid() {
        String cachedJson = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(CACHE_KEY, null);
        long timestamp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getLong(CACHE_TIMESTAMP_KEY, 0);
        boolean notExpired = (System.currentTimeMillis() - timestamp) < CACHE_EXPIRY_MS;
        if (cachedJson != null && !cachedJson.isEmpty() && notExpired) {
            try {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                java.lang.reflect.Type mapType = new com.google.gson.reflect.TypeToken<java.util.Map<String, Object>>(){}.getType();
                java.util.Map<String, Object> cacheData = gson.fromJson(cachedJson, mapType);
                if (cacheData != null && cacheData.containsKey("questions")) {
                    String questionsJson = gson.toJson(cacheData.get("questions"));
                    java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<java.util.List<QuizQuestion>>(){}.getType();
                    java.util.List<QuizQuestion> cachedQuestions = gson.fromJson(questionsJson, listType);
                    return cachedQuestions != null && !cachedQuestions.isEmpty();
                }
            } catch (Exception e) {
                Log.e(TAG, "Eroare la parsing cache local", e);
            }
        }
        return false;
    }
    private void saveDataSourcePreferenceHybrid(String pref) {
        dataSourcePreference = pref;
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(DATA_SOURCE_PREF_KEY, pref).apply();
    }
    private void showNoInternetForPreferredDatabaseDialogHybrid(int numQuestions) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Nu există conexiune la internet")
            .setMessage("Preferința este baza de date, dar nu există conexiune. Încercați cache local?")
            .setPositiveButton("Cache Local", (dialog, which) -> loadQuestionsFromLocalCacheHybrid(numQuestions))
            .setNegativeButton("Închide", (dialog, which) -> finish())
            .show();
    }
    private void showNoCacheForPreferredLocalDialogHybrid(int numQuestions) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("❌ Nu există cache local")
            .setMessage("Preferința este cache local, dar nu există date salvate. Încercați baza de date?")
            .setPositiveButton("Baza de date", (dialog, which) -> loadQuestionsFromDatabaseHybrid(numQuestions))
            .setNegativeButton("Închide", (dialog, which) -> finish())
            .show();
    }
    private void showDataSourceSelectionDialogHybrid(int numQuestions) {
        boolean hasInternet = isInternetAvailable();
        boolean hasLocalCache = checkIfLocalCacheExistsHybrid();
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("📚 Alegeți sursa întrebărilor");
        dialogBuilder.setCancelable(false);
        if (hasInternet && hasLocalCache) {
            dialogBuilder.setMessage("📊 Ambele surse sunt disponibile!\n\n🌐 Baza de Date: Întrebări actualizate\n📱 Cache Local: Încărcare rapidă\n🎯 Automat: Alege cel mai bun\n\nCe preferați?");
            dialogBuilder.setPositiveButton("🌐 Baza de Date", (dialog, which) -> {
                saveDataSourcePreferenceHybrid("always_database");
                loadQuestionsFromDatabaseHybrid(numQuestions);
            });
            dialogBuilder.setNegativeButton("📱 Cache Local", (dialog, which) -> {
                saveDataSourcePreferenceHybrid("always_cache");
                loadQuestionsFromLocalCacheHybrid(numQuestions);
            });
            dialogBuilder.setNeutralButton("🎯 Automat", (dialog, which) -> {
                saveDataSourcePreferenceHybrid("auto");
                continueHybridLoadWithNumQuestions(numQuestions);
            });
        } else if (hasInternet) {
            dialogBuilder.setMessage("🌐 Doar conexiune la internet disponibilă. Încărcăm din baza de date?");
            dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                saveDataSourcePreferenceHybrid("always_database");
                loadQuestionsFromDatabaseHybrid(numQuestions);
            });
        } else if (hasLocalCache) {
            dialogBuilder.setMessage("📱 Doar cache local disponibil. Încărcăm din cache?");
            dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                saveDataSourcePreferenceHybrid("always_cache");
                loadQuestionsFromLocalCacheHybrid(numQuestions);
            });
        } else {
            dialogBuilder.setMessage("❌ Nicio sursă disponibilă. Conectați-vă la internet sau jucați după ce ați descărcat întrebările.");
            dialogBuilder.setPositiveButton("Închide", (dialog, which) -> finish());
        }
        dialogBuilder.show();
    }
    private void loadQuestionsFromDatabaseHybrid(int numQuestions) {
        // Pentru demo: folosește întrebările hardcodate, shuffle și limitează la numQuestions
        if (quizQuestions == null || quizQuestions.isEmpty()) {
            initQuestions();
        }
        List<QuizQuestion> limitedQuestions = new ArrayList<>(quizQuestions);
        Collections.shuffle(limitedQuestions);
        limitedQuestions = limitedQuestions.subList(0, Math.min(numQuestions, limitedQuestions.size()));
        selectedQuestions = limitedQuestions;
        convertToEnhancedQuestions();
        filterQuestionsByGameMode();
        displayQuestion(0);
        updateScoreDisplay();
        startTimer();
        saveQuestionsToLocalCacheHybrid(limitedQuestions);
        Toast.makeText(this, "🌐 Întrebări încărcate din baza de date! (demo)", Toast.LENGTH_SHORT).show();
    }
    private void loadQuestionsFromLocalCacheHybrid(int numQuestions) {
        String cachedJson = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(CACHE_KEY, null);
        if (cachedJson != null && !cachedJson.isEmpty()) {
            try {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                java.lang.reflect.Type mapType = new com.google.gson.reflect.TypeToken<java.util.Map<String, Object>>(){}.getType();
                java.util.Map<String, Object> cacheData = gson.fromJson(cachedJson, mapType);
                if (cacheData != null && cacheData.containsKey("questions")) {
                    String questionsJson = gson.toJson(cacheData.get("questions"));
                    java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<java.util.List<QuizQuestion>>(){}.getType();
                    java.util.List<QuizQuestion> cachedQuestions = gson.fromJson(questionsJson, listType);
                    if (cachedQuestions != null && !cachedQuestions.isEmpty()) {
                        List<QuizQuestion> limitedQuestions = cachedQuestions;
                        if (cachedQuestions.size() > numQuestions) {
                            limitedQuestions = new ArrayList<>(cachedQuestions);
                            Collections.shuffle(limitedQuestions);
                            limitedQuestions = limitedQuestions.subList(0, numQuestions);
                        }
                        selectedQuestions = limitedQuestions;
                        convertToEnhancedQuestions();
                        filterQuestionsByGameMode();
                        displayQuestion(0);
                        updateScoreDisplay();
                        startTimer();
                        Toast.makeText(this, "📱 Întrebări încărcate din cache local!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Eroare la parsing cache local", e);
            }
        }
        Toast.makeText(this, "❌ Nu există întrebări valide în cache local!", Toast.LENGTH_LONG).show();
        // Fallback la întrebări hardcodate
        selectRandomQuestions();
        convertToEnhancedQuestions();
        filterQuestionsByGameMode();
        displayQuestion(0);
        updateScoreDisplay();
        startTimer();
    }
    private void saveQuestionsToLocalCacheHybrid(List<QuizQuestion> questions) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            String questionsJson = gson.toJson(questions);
            java.util.Map<String, Object> cacheData = new java.util.HashMap<>();
            cacheData.put("questions", questions);
            cacheData.put("timestamp", System.currentTimeMillis());
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(CACHE_KEY, gson.toJson(cacheData))
                .putLong(CACHE_TIMESTAMP_KEY, System.currentTimeMillis())
                .apply();
        } catch (Exception e) {
            Log.e(TAG, "Eroare la salvarea în cache local (hybrid)", e);
        }
    }

    /**
     * Ascunde toate elementele de quiz la final, pentru un UI curat
     */
    private void hideQuizElements() {
        if (answerButton1 != null) answerButton1.setVisibility(View.GONE);
        if (answerButton2 != null) answerButton2.setVisibility(View.GONE);
        if (answerButton3 != null) answerButton3.setVisibility(View.GONE);
        if (answerButton4 != null) answerButton4.setVisibility(View.GONE);
        if (answerCard1 != null) answerCard1.setVisibility(View.GONE);
        if (answerCard2 != null) answerCard2.setVisibility(View.GONE);
        if (answerCard3 != null) answerCard3.setVisibility(View.GONE);
        if (answerCard4 != null) answerCard4.setVisibility(View.GONE);
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
        if (hintButton != null) hintButton.setVisibility(View.GONE);
        if (quitButton != null) quitButton.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
        }
        
        // Release media resources
        if (correctSound != null) {
            correctSound.release();
            correctSound = null;
        }
        if (wrongSound != null) {
            wrongSound.release();
            wrongSound = null;
        }
        if (clockSound != null) {
            clockSound.release();
            clockSound = null;
        }
        if (winSound != null) {
            winSound.release();
            winSound = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Only start timer if timerTextView is initialized
        if (timerTextView != null && !answered && timer == null) {
            startTimer();
        }
        
        // Reinitialize sounds if they were released
        if (correctSound == null) {
            setupSounds();
        }
    }
} 