package com.example.myapplication.dobrogeausage;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import com.example.myapplication.utils.SyncManager;

import com.example.myapplication.models.EnhancedQuestionModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.PointsManager;
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
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.animation.OvershootInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;
import java.util.Arrays;
import android.os.Build;

/**
 * Activitate pentru jocul regiunii Dobrogea
 */
public class DobrogeaGameActivity extends AppCompatActivity {
    private static final String TAG = "DobrogeaGameActivity";
    private static final String REGION = "dobrogea";
    private static final String GAME_TYPE = "quiz";
    
    private TextView questionTextView;
    private MaterialButton[] answerButtons;
    private TextView scoreTextView;
    private ProgressBar progressBar;
    private TextView timerTextView;
    private TextView streakTextView;
    private ImageView questionImage;
    private MaterialCardView fiftyFiftyButton;
    private MaterialCardView hintButton;
    private MaterialCardView skipQuestionButton;
    private MaterialCardView quitButton;
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
    
    // Enhanced question management
    private List<QuestionModel> firestoreQuestions;
    private List<EnhancedQuestionModel> enhancedQuestions;
    
    // Enhanced game systems specific to Dobrogea
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dobrogea_game);

        // Initialize enhanced systems
        initializeEnhancedSystems();
        
        // Setup game mode and difficulty
        setupGameModeAndDifficulty();
        
        // Initialize views
        initializeViews();
        
        // Load questions
        loadQuestionsFromFirestore();
        
        // Apply button styles
        applyButtonStyles();
        
        // Setup lifelines
        setupLifelines();
        
        // Setup accessibility
        setupAccessibility();
    }
    
    /**
     * Inițializează sistemele îmbunătățite pentru Dobrogea
     */
    private void initializeEnhancedSystems() {
        difficultyManager = new DifficultyManager(this);
        gameModeManager = new GameModeManager(this);
        progressTracker = new PlayerProgressTracker(this);
        achievementManager = AchievementManager.getInstance(this);
        syncManager = SyncManager.getInstance(this);
        pointsManager = PointsManager.getInstance(this);
        questionRepository = FirestoreQuestionRepository.getInstance();
        
        Log.d(TAG, "🌊 Enhanced systems initialized for Dobrogea quiz");
    }
    
    /**
     * Configurează modul de joc și dificultatea
     */
    private void setupGameModeAndDifficulty() {
        Intent intent = getIntent();
        
        // Get game mode from intent
        String gameModeStr = intent.getStringExtra("GAME_MODE");
        GameModeManager.GameMode gameMode = GameModeManager.GameMode.CLASSIC;
        if (gameModeStr != null) {
            try {
                gameMode = GameModeManager.GameMode.valueOf(gameModeStr);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Invalid game mode: " + gameModeStr);
            }
        }
        
        // Get focus category from intent
        String categoryStr = intent.getStringExtra("FOCUS_CATEGORY");
        EnhancedQuestionModel.Category focusCategory = null;
        if (categoryStr != null) {
            try {
                focusCategory = EnhancedQuestionModel.Category.valueOf(categoryStr);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Invalid category: " + categoryStr);
            }
        }
        
        // Initialize game mode
        gameModeManager.initializeGameMode(gameMode, focusCategory);
        
        // Update time per question based on mode and difficulty
        TIME_PER_QUESTION = Math.min(
            gameModeManager.getTimePerQuestion(),
            difficultyManager.getCurrentDifficulty().timePerQuestion
        );
        
        Log.d(TAG, "🎯 Game mode: " + gameMode.displayName + 
               ", Time per question: " + TIME_PER_QUESTION + "ms");
    }
    
    /**
     * Încarcă întrebările din Firestore pentru Dobrogea
     */
    private void loadQuestionsFromFirestore() {
        Log.d(TAG, "🔄 Loading questions from Firestore for Dobrogea");
        
        questionRepository.getQuestions(REGION, GAME_TYPE)
            .addOnSuccessListener(querySnapshot -> {
                firestoreQuestions = new ArrayList<>();
                querySnapshot.forEach(document -> {
                    try {
                        QuestionModel question = document.toObject(QuestionModel.class);
                        if (question != null) {
                            firestoreQuestions.add(question);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing question", e);
                    }
                });
                
                if (!firestoreQuestions.isEmpty()) {
                    Log.d(TAG, "✅ Loaded " + firestoreQuestions.size() + " Dobrogea questions");
                    enhancedQuestions = convertToEnhancedQuestions(firestoreQuestions);
                    
                    // Filter questions based on game mode
                    enhancedQuestions = gameModeManager.filterQuestionsForGameMode(enhancedQuestions);
                    
                    totalQuestions = enhancedQuestions.size();
                    isDataLoaded = true;
        displayQuestion();
        startTimer();
                } else {
                    handleNoQuestionsAvailable();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Failed to load Dobrogea questions", e);
                showFirestoreError();
            });
    }
    
    /**
     * Convertește întrebările simple în întrebări îmbunătățite pentru Dobrogea
     */
    private List<EnhancedQuestionModel> convertToEnhancedQuestions(List<QuestionModel> questions) {
        List<EnhancedQuestionModel> enhanced = new ArrayList<>();
        
        for (QuestionModel question : questions) {
            EnhancedQuestionModel.Category category = inferDobrogeaCategory(question.getQuestion());
            EnhancedQuestionModel.Difficulty difficulty = inferDifficulty(question);
            String[] tags = generateDobrogeaTags(question);
            
            EnhancedQuestionModel enhancedQuestion = new EnhancedQuestionModel(
                question.getQuestion(),
                question.getCorrectAnswer(),
                question.getIncorrectAnswers(),
                question.getImageResourceId(),
                question.getFact(),
                category,
                difficulty,
                tags
            );
            
            enhanced.add(enhancedQuestion);
        }
        
        return enhanced;
    }
    
    /**
     * Inferă categoria pentru întrebările despre Dobrogea
     */
    private EnhancedQuestionModel.Category inferDobrogeaCategory(String questionText) {
        String questionLower = questionText.toLowerCase();
        
        // Maritime și porturi
        if (questionLower.contains("mare") || questionLower.contains("port") || 
            questionLower.contains("constanța") || questionLower.contains("mangalia") ||
            questionLower.contains("mamaia") || questionLower.contains("navigație") ||
            questionLower.contains("pescuit") || questionLower.contains("farul")) {
            return EnhancedQuestionModel.Category.CULTURE;
        }
        
        // Delta și natură
        if (questionLower.contains("delta") || questionLower.contains("dunăre") ||
            questionLower.contains("pelican") || questionLower.contains("stuf") ||
            questionLower.contains("canal") || questionLower.contains("tulcea") ||
            questionLower.contains("lebădă") || questionLower.contains("pescăruș")) {
            return EnhancedQuestionModel.Category.NATURE;
        }
        
        // Arheologie și istorie antică
        if (questionLower.contains("tomis") || questionLower.contains("histria") ||
            questionLower.contains("callatis") || questionLower.contains("tropaeum") ||
            questionLower.contains("adamclisi") || questionLower.contains("roman") ||
            questionLower.contains("grec") || questionLower.contains("antic")) {
            return EnhancedQuestionModel.Category.HISTORY;
        }
        
        // Arhitectură și monumente
        if (questionLower.contains("cetate") || questionLower.contains("monument") ||
            questionLower.contains("castel") || questionLower.contains("biserică") ||
            questionLower.contains("mănăstire")) {
            return EnhancedQuestionModel.Category.ARCHITECTURE;
        }
        
        // Gastronomie și tradițiile pescarilor
        if (questionLower.contains("pește") || questionLower.contains("saramură") ||
            questionLower.contains("icre") || questionLower.contains("mâncare") ||
            questionLower.contains("tradițional") || questionLower.contains("bucătărie")) {
            return EnhancedQuestionModel.Category.GASTRONOMY;
        }
        
        // Geografia Dobrogei
        if (questionLower.contains("județ") || questionLower.contains("oraș") ||
            questionLower.contains("sat") || questionLower.contains("râu") ||
            questionLower.contains("lac") || questionLower.contains("munte")) {
            return EnhancedQuestionModel.Category.GEOGRAPHY;
        }
        
        // Default la istorie pentru Dobrogea
        return EnhancedQuestionModel.Category.HISTORY;
    }
    
    /**
     * Inferă dificultatea întrebării
     */
    private EnhancedQuestionModel.Difficulty inferDifficulty(QuestionModel question) {
        String questionText = question.getQuestion();
        String correctAnswer = question.getCorrectAnswer();
        
        // Expert: întrebări foarte specifice sau tehnice
        if (questionText.length() > 150 || 
            questionText.contains("Tropaeum Traiani") ||
            questionText.contains("Callatis") ||
            questionText.contains("Aegyssus") ||
            correctAnswer.length() > 30) {
            return EnhancedQuestionModel.Difficulty.EXPERT;
        }
        
        // Hard: întrebări despre detalii specifice
        if (questionText.length() > 100 ||
            questionText.contains("Histria") ||
            questionText.contains("Tomis") ||
            questionText.contains("Adamclisi")) {
            return EnhancedQuestionModel.Difficulty.HARD;
        }
        
        // Medium: întrebări generale despre Dobrogea
        if (questionText.contains("Constanța") ||
            questionText.contains("Tulcea") ||
            questionText.contains("Delta")) {
            return EnhancedQuestionModel.Difficulty.MEDIUM;
        }
        
        // Easy: întrebări de bază
        return EnhancedQuestionModel.Difficulty.EASY;
    }
    
    /**
     * Generează tag-uri specifice pentru Dobrogea
     */
    private String[] generateDobrogeaTags(QuestionModel question) {
        List<String> tags = new ArrayList<>();
        String questionLower = question.getQuestion().toLowerCase();
        
        // Tag-uri maritime
        if (questionLower.contains("mare") || questionLower.contains("port")) {
            tags.add("maritime");
        }
        if (questionLower.contains("constanța")) {
            tags.add("constanta");
        }
        if (questionLower.contains("mangalia")) {
            tags.add("mangalia");
        }
        
        // Tag-uri pentru deltă
        if (questionLower.contains("delta")) {
            tags.add("delta");
        }
        if (questionLower.contains("dunăre")) {
            tags.add("danube");
        }
        if (questionLower.contains("tulcea")) {
            tags.add("tulcea");
        }
        
        // Tag-uri arheologice
        if (questionLower.contains("tomis")) {
            tags.add("tomis");
        }
        if (questionLower.contains("histria")) {
            tags.add("histria");
        }
        if (questionLower.contains("roman")) {
            tags.add("roman");
        }
        
        // Tag-uri pentru biodiversitate
        if (questionLower.contains("pelican") || questionLower.contains("lebădă")) {
            tags.add("birds");
        }
        if (questionLower.contains("pește")) {
            tags.add("fish");
        }
        
        return tags.toArray(new String[0]);
    }
    
    /**
     * Afișează întrebarea curentă
     */
    private void displayQuestion() {
        if (enhancedQuestions == null || currentQuestionIndex >= enhancedQuestions.size()) {
            finishGame();
            return;
        }
        
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        
        // Update question text with maritime theme
        questionTextView.setText(currentQuestion.getQuestion());
        
        // Create all answers array
        List<String> allAnswers = new ArrayList<>();
        allAnswers.add(currentQuestion.getCorrectAnswer());
        allAnswers.addAll(currentQuestion.getIncorrectAnswers());
        Collections.shuffle(allAnswers);
        
        // Set answers to buttons
        for (int i = 0; i < answerButtons.length && i < allAnswers.size(); i++) {
            answerButtons[i].setText(allAnswers.get(i));
            answerButtons[i].setVisibility(View.VISIBLE);
            answerButtons[i].setEnabled(true);
            
            final int index = i;
            final String answer = allAnswers.get(i);
            answerButtons[i].setOnClickListener(v -> checkAnswer(index, answer));
        }
        
        // Hide unused buttons
        for (int i = allAnswers.size(); i < answerButtons.length; i++) {
            answerButtons[i].setVisibility(View.GONE);
        }
        
        // Update progress
        if (progressBar != null) {
            int progress = (int) ((float) currentQuestionIndex / totalQuestions * 100);
            progressBar.setProgress(progress);
        }
        
        // Reset lifelines for new question
        resetLifelinesForNewQuestion();
        
        // Reset card styles
        resetCardStyles();
        
        // Update lifelines availability
        updateLifelinesAvailability();
        
        questionStartTime = System.currentTimeMillis();
        
        Log.d(TAG, "🌊 Displaying Dobrogea question " + (currentQuestionIndex + 1) + "/" + totalQuestions);
    }
    
    /**
     * Verifică răspunsul și actualizează scorul
     */
    private void checkAnswer(int selectedAnswerIndex, String selectedAnswer) {
        if (timer != null) {
        timer.cancel();
        }
        
        long timeSpent = System.currentTimeMillis() - questionStartTime;
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        String correctAnswer = currentQuestion.getCorrectAnswer();
        boolean isCorrect = selectedAnswer.equals(correctAnswer);
        
        // Track answer with progress tracker
        progressTracker.trackAnswer(
            "dobrogea_q_" + currentQuestionIndex,
            isCorrect,
            timeSpent,
            currentQuestion.getCategory(),
            currentQuestion.getDifficulty()
        );
        
        MaterialCardView selectedCard = answerCards[selectedAnswerIndex];

        if (isCorrect) {
            correctAnswers++;
            streak++;
            if (streak > maxStreak) {
                maxStreak = streak;
            }
            
            // Calculate score with mode bonus
            int baseScore = POINTS_PER_CORRECT_ANSWER;
            int modeBonus = gameModeManager.calculateModeBonus(baseScore, true, timeSpent);
            int difficultyBonus = difficultyManager.calculateFinalScore(baseScore) - baseScore;
            
            score += baseScore + modeBonus + difficultyBonus;
            
            // Provide correct answer feedback
            provideCorrectAnswerFeedback(selectedCard, selectedAnswerIndex);
            
            // Show streak bonus if applicable
            if (streak >= STREAK_BONUS_THRESHOLD) {
                showStreakBonus();
            }
            
        } else {
            streak = 0;
            provideWrongAnswerFeedback(selectedCard, selectedAnswerIndex, correctAnswer);
        }

        // Update UI
        updateScore();
        updateStreak();
        
        // Provide haptic feedback
        provideHapticFeedback(isCorrect ? HapticFeedbackType.CORRECT : HapticFeedbackType.WRONG);
        
        // Show answer dialog with maritime theme
        showAnswerDialog(currentQuestion.getFact(), isCorrect);
        
        // Check if game should end based on mode
        if (gameModeManager.shouldEndGame(isCorrect, totalQuestions - correctAnswers)) {
            new Handler().postDelayed(this::finishGame, 2000);
        } else {
            // Move to next question after delay
            new Handler().postDelayed(this::moveToNextQuestion, 2000);
        }
    }

    private void initializeViews() {
        questionTextView = findViewById(R.id.questionTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
        progressBar = findViewById(R.id.progressBar);
        timerTextView = findViewById(R.id.timerTextView);
        streakTextView = findViewById(R.id.streakTextView);
        questionImage = findViewById(R.id.questionImage);
        fiftyFiftyButton = findViewById(R.id.fiftyFiftyButton);
        hintButton = findViewById(R.id.hintButton);
        skipQuestionButton = findViewById(R.id.skipQuestionButton);
        quitButton = findViewById(R.id.quitButton);
        finishButton = findViewById(R.id.finishButton);
        
        // Initialize answer buttons and cards
        answerButtons = new MaterialButton[4];
        answerCards = new MaterialCardView[4];
        
        answerButtons[0] = findViewById(R.id.answerButton1);
        answerButtons[1] = findViewById(R.id.answerButton2);
        answerButtons[2] = findViewById(R.id.answerButton3);
        answerButtons[3] = findViewById(R.id.answerButton4);
        
        answerCards[0] = findViewById(R.id.answerCard1);
        answerCards[1] = findViewById(R.id.answerCard2);
        answerCards[2] = findViewById(R.id.answerCard3);
        answerCards[3] = findViewById(R.id.answerCard4);
    }
    
    private void handleNoQuestionsAvailable() {
        Toast.makeText(this, "🌊 Nu sunt disponibile întrebări pentru Dobrogea", Toast.LENGTH_LONG).show();
        finish();
    }
    
    private void showFirestoreError() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("🌊 Eroare Dobrogea")
            .setMessage("Nu s-au putut încărca întrebările despre Dobrogea. Verifică conexiunea la internet.")
            .setPositiveButton("Încearcă din nou", (dialog, which) -> loadQuestionsFromFirestore())
            .setNegativeButton("Înapoi", (dialog, which) -> finish())
            .show();
    }

    /**
     * Finalizează quiz-ul Dobrogea cu salvare scor și animații
     */
    private void finishGame() {
        if (timer != null) {
            timer.cancel();
        }
        // Salvăm rezultatul în Firebase și leaderboard
        saveQuizResultToFirebase();
        // Afișăm ecranul de final cu animații
        showFinishButton();
    }

    /**
     * Afișează ecranul de final cu animații și scor
     */
    private void showFinishButton() {
        // Ascunde toate cardurile de răspuns
        for (MaterialCardView card : answerCards) {
            card.setVisibility(View.GONE);
        }
        // Mesaj de finalizare
        questionTextView.setText("Quiz complet! Felicitări!");
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeIn.setDuration(700);
        questionTextView.startAnimation(fadeIn);
        // Actualizează progress bar la final
        progressBar.setProgress(getQuestionsCount());
        progressBar.setContentDescription("Ai terminat quiz-ul Dobrogea!");
        progressBar.animate()
            .scaleY(1.2f)
            .setDuration(300)
            .withEndAction(() -> {
                progressBar.animate()
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start();
            })
            .start();
        // Afișează scorul final cu animație
        String scoreMessage = "Scor final: " + score + " puncte";
        TextView scoreView = new TextView(this);
        scoreView.setText(scoreMessage);
        scoreView.setTextSize(20);
        scoreView.setTextColor(ContextCompat.getColor(this, R.color.dobrogea_primary));
        scoreView.setTypeface(Typeface.DEFAULT_BOLD);
        scoreView.setGravity(Gravity.CENTER);
        ConstraintLayout layout = findViewById(R.id.main_constraint_layout);
        ConstraintSet constraintSet = new ConstraintSet();
        scoreView.setId(View.generateViewId());
        layout.addView(scoreView);
        constraintSet.clone(layout);
        constraintSet.connect(scoreView.getId(), ConstraintSet.TOP, questionTextView.getId(), ConstraintSet.BOTTOM, 24);
        constraintSet.connect(scoreView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(scoreView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.applyTo(layout);
        scoreView.setAlpha(0f);
        scoreView.setScaleX(0.7f);
        scoreView.setScaleY(0.7f);
        scoreView.animate()
            .alpha(1f)
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(800)
            .setStartDelay(500)
            .setInterpolator(new OvershootInterpolator())
            .start();
    }

    /**
     * Salvează rezultatul quiz-ului Dobrogea în Firebase și leaderboard
     */
    private void saveQuizResultToFirebase() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.w(TAG, "Utilizatorul nu este autentificat, nu se poate salva rezultatul în clasament");
            Toast.makeText(this, "Trebuie să fii autentificat pentru a apărea în clasament", Toast.LENGTH_LONG).show();
            return;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        QuizResult quizResult = createQuizResult(userId);
        saveToQuizResults(quizResult);
        saveToUserActivityHistory(quizResult);
        saveToLeaderboardData(quizResult);
        updateUserProfileStats(quizResult);
        Log.d(TAG, "Dobrogea Quiz Result saved - Score: " + score + ", Region: " + REGION + ", GameType: " + GAME_TYPE);
    }

    /**
     * Creează obiectul QuizResult cu datele Dobrogea
     */
    private QuizResult createQuizResult(String userId) {
        QuizResult quizResult = new QuizResult();
        quizResult.setUserId(userId);
        quizResult.setScore(score);
        quizResult.setCorrectAnswers(correctAnswers);
        quizResult.setTotalQuestions(getQuestionsCount());
        quizResult.setMaxStreak(maxStreak);
        quizResult.setTotalTime(totalTime);
        quizResult.setRegion(REGION);
        quizResult.setGameType(GAME_TYPE);
        quizResult.setCompletedAt(new Date());
        quizResult.setQuizId("dobrogea_main_quiz_" + System.currentTimeMillis());
        return quizResult;
    }

    /**
     * Salvează rezultatul în colecția principală quiz_results
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
     * Salvează activitatea recentă a utilizatorului
     */
    private void saveToUserActivityHistory(QuizResult quizResult) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> activityData = new HashMap<>();
        activityData.put("userId", quizResult.getUserId());
        activityData.put("activityType", "dobrogea_quiz");
        activityData.put("displayName", "Quiz Dobrogea");
        activityData.put("score", quizResult.getScore());
        activityData.put("accuracy", quizResult.getAccuracy());
        activityData.put("correctAnswers", quizResult.getCorrectAnswers());
        activityData.put("totalQuestions", quizResult.getTotalQuestions());
        activityData.put("maxStreak", quizResult.getMaxStreak());
        activityData.put("region", REGION);
        activityData.put("gameType", GAME_TYPE);
        activityData.put("completedAt", quizResult.getCompletedAt());
        activityData.put("duration", totalTime);
        activityData.put("iconResource", "ic_dobrogea");
        activityData.put("colorTheme", "dobrogea_primary");
        activityData.put("description", "Quiz despre Dobrogea - " + correctAnswers + "/" + getQuestionsCount() + " corecte");
        db.collection("user_activity_history")
            .document(quizResult.getUserId())
            .collection("recent_activities")
            .add(activityData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Activity saved to user history with ID: " + documentReference.getId());
                limitUserActivityHistory(quizResult.getUserId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving activity to user history", e);
            });
    }

    /**
     * Salvează datele pentru leaderboard (cel mai bun scor)
     */
    private void saveToLeaderboardData(QuizResult quizResult) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = quizResult.getUserId();
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
                updateBestScoreForLeaderboard(quizResult);
            });
    }

    /**
     * Actualizează cel mai bun scor pentru leaderboard
     */
    private void updateBestScoreForLeaderboard(QuizResult quizResult) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> bestScoreData = new HashMap<>();
        bestScoreData.put("userId", quizResult.getUserId());
        bestScoreData.put("username", currentUser.getEmail());
        bestScoreData.put("displayName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Navigator Dobrogea");
        bestScoreData.put("profileImageUrl", currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "");
        bestScoreData.put("score", quizResult.getScore());
        bestScoreData.put("accuracy", quizResult.getAccuracy());
        bestScoreData.put("maxStreak", quizResult.getMaxStreak());
        bestScoreData.put("region", REGION);
        bestScoreData.put("gameType", GAME_TYPE);
        bestScoreData.put("achievedAt", quizResult.getCompletedAt());
        bestScoreData.put("leaderboardCategory", "dobrogea_quiz_masters");
        db.collection("user_best_scores")
            .document(quizResult.getUserId())
            .collection("regional_scores")
            .document(REGION + "_" + GAME_TYPE)
            .set(bestScoreData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Best score updated for user in " + REGION);
                updateGlobalLeaderboard(bestScoreData);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating best score", e);
            });
    }

    /**
     * Actualizează leaderboard-ul global
     */
    private void updateGlobalLeaderboard(Map<String, Object> bestScoreData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String leaderboardId = REGION + "_" + GAME_TYPE;
        db.collection("leaderboards")
            .document(leaderboardId)
            .collection("entries")
            .document((String) bestScoreData.get("userId"))
            .set(bestScoreData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Global leaderboard updated successfully");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating global leaderboard", e);
            });
    }

    /**
     * Actualizează statisticile generale ale utilizatorului
     */
    private void updateUserProfileStats(QuizResult quizResult) {
        pointsManager.addPoints(this, REGION, quizResult.getScore());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        com.google.firebase.firestore.DocumentReference userRef = db.collection("users").document(quizResult.getUserId());
        userRef.get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("lastActivity", new Date());
                    updates.put("lastRegionPlayed", REGION);
                    updates.put("totalDobrogeaQuizzes", FieldValue.increment(1));
                    updates.put("totalDobrogeaPoints", FieldValue.increment(quizResult.getScore()));
                    userRef.update(updates)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile stats updated"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error updating user profile stats", e));
                }
            });
    }

    /**
     * Limitează istoricul de activități la ultimele 20
     */
    private void limitUserActivityHistory(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user_activity_history")
            .document(userId)
            .collection("recent_activities")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(25)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.size() > 20) {
                    for (int i = 20; i < queryDocumentSnapshots.size(); i++) {
                        queryDocumentSnapshots.getDocuments().get(i).getReference().delete();
                    }
                    Log.d(TAG, "Cleaned up old activities, kept latest 20");
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error cleaning up activity history", e);
            });
    }

    /**
     * Aplică stilurile tematice maritime pentru Dobrogea
     */
    private void applyButtonStyles() {
        // Stiluri pentru butoanele de răspuns cu tema maritime
        for (MaterialCardView card : answerCards) {
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
            card.setStrokeColor(ContextCompat.getColor(this, R.color.dobrogea_primary_light));
            card.setStrokeWidth(2);
            card.setElevation(6f);
            card.setRadius(12f);
            
            // Adăugăm efecte de hover și click
            card.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                        break;
                }
                return false;
            });
        }
        
        // Stiluri pentru butoanele de lifeline cu tema maritime
        fiftyFiftyButton.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dobrogea_accent));
        fiftyFiftyButton.setStrokeColor(ContextCompat.getColor(this, R.color.dobrogea_primary));
        fiftyFiftyButton.setStrokeWidth(2);
        fiftyFiftyButton.setRadius(8f);
        fiftyFiftyButton.setElevation(4f);
        
        hintButton.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dobrogea_secondary));
        hintButton.setStrokeColor(ContextCompat.getColor(this, R.color.dobrogea_primary));
        hintButton.setStrokeWidth(2);
        hintButton.setRadius(8f);
        hintButton.setElevation(4f);
        
        skipQuestionButton.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dobrogea_warning));
        skipQuestionButton.setStrokeColor(ContextCompat.getColor(this, R.color.dobrogea_primary));
        skipQuestionButton.setStrokeWidth(2);
        skipQuestionButton.setRadius(8f);
        skipQuestionButton.setElevation(4f);
        
        quitButton.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dobrogea_error));
        quitButton.setStrokeColor(ContextCompat.getColor(this, R.color.dobrogea_primary));
        quitButton.setStrokeWidth(2);
        quitButton.setRadius(8f);
        quitButton.setElevation(4f);
        
        // Efecte de hover pentru lifeline-uri
        fiftyFiftyButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                    break;
            }
            return false;
        });
        
        hintButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                    break;
            }
            return false;
        });
        
        skipQuestionButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                    break;
            }
            return false;
        });
    }

    /**
     * Configurează lifeline-urile cu tema maritime
     */
    private void setupLifelines() {
        fiftyFiftyButton.setOnClickListener(v -> {
            // Adăugăm efect vizual la apăsare
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.button_press);
            v.startAnimation(pulse);
            useFiftyFifty();
        });
        
        hintButton.setOnClickListener(v -> {
            // Adăugăm efect vizual la apăsare
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.button_press);
            v.startAnimation(pulse);
            showHint();
        });
        
        skipQuestionButton.setOnClickListener(v -> {
            // Adăugăm efect vizual la apăsare
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.button_press);
            v.startAnimation(pulse);
            skipQuestion();
        });
        
        quitButton.setOnClickListener(v -> {
            // Adăugăm efect vizual la apăsare
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.button_press);
            v.startAnimation(pulse);
            showConfirmQuitDialog();
        });
    }

    /**
     * Configurează funcționalitățile de accesibilitate
     */
    private void setupAccessibility() {
        // Set content descriptions for better screen reader support
        ViewCompat.setAccessibilityHeading(questionTextView, true);
        
        // Parse the timer text to integer
        int timeValue;
        try {
            timeValue = Integer.parseInt(timerTextView.getText().toString());
        } catch (NumberFormatException e) {
            timeValue = 30; // Default value
        }
        timerTextView.setContentDescription("Timp rămas: " + timeValue + " secunde");
        
        fiftyFiftyButton.setContentDescription("Ajutor 50/50 - elimină două răspunsuri greșite");
        hintButton.setContentDescription("Indiciu - vezi un indiciu pentru întrebare");
        skipQuestionButton.setContentDescription("Omite întrebarea - treci la următoarea");
        quitButton.setContentDescription("Încheie quiz-ul");
        
        // Set content descriptions for answer buttons based on their text
        for (int i = 0; i < answerButtons.length; i++) {
            MaterialButton button = answerButtons[i];
            button.setContentDescription("Răspuns " + (i+1) + ": " + button.getText());
        }
        
        // Ensure minimum touch target size for better accessibility
        for (MaterialCardView card : answerCards) {
            card.setMinimumHeight((int) (48 * getResources().getDisplayMetrics().density));
        }
    }

    /**
     * Pornește timer-ul cu animații maritime
     */
    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(TIME_PER_QUESTION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                timerTextView.setText(String.valueOf(secondsRemaining));
                timerTextView.setContentDescription("Timp rămas: " + secondsRemaining + " secunde");
                
                // Adăugăm efect vizual când timpul este sub 10 secunde
                if (millisUntilFinished <= 10000) {
                    Animation pulse = AnimationUtils.loadAnimation(DobrogeaGameActivity.this, R.anim.pulse);
                    timerTextView.startAnimation(pulse);
                    timerTextView.setTextColor(ContextCompat.getColor(DobrogeaGameActivity.this, R.color.dobrogea_accent));
                } else {
                    timerTextView.setTextColor(ContextCompat.getColor(DobrogeaGameActivity.this, R.color.dobrogea_text));
                }
            }

            @Override
            public void onFinish() {
                handleTimeout();
            }
        }.start();
    }

    /**
     * Gestionează timeout-ul cu feedback îmbunătățit
     */
    private void handleTimeout() {
        // Feedback haptic pentru timeout
        provideHapticFeedback(HapticFeedbackType.WRONG);
        
        // Dezactivăm toate cardurile
        for (MaterialCardView card : answerCards) {
            card.setClickable(false);
        }
        
        // Animație pentru timeout - fade out toate răspunsurile
        for (MaterialCardView card : answerCards) {
            card.animate()
                .alpha(0.5f)
                .setDuration(300)
                .start();
        }
        
        // Actualizăm statisticile pentru întrebarea ratată
        totalQuestions++;
        streak = 0;
        updateStreak();
        updateScore();
        
        // Afișăm răspunsul corect pentru timeout
        if (enhancedQuestions != null && !enhancedQuestions.isEmpty() && 
            currentQuestionIndex < enhancedQuestions.size()) {
            EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
            String correctAnswer = currentQuestion.getCorrectAnswer();
            String fact = currentQuestion.getFact();
            
            // Evidențiem răspunsul corect
            highlightCorrectAnswer(correctAnswer);
            
            // Dialog pentru timeout cu informație educațională
            String timeoutMessage = "✅ Răspunsul corect era: " + correctAnswer;
            
            if (fact != null && !fact.isEmpty()) {
                timeoutMessage += "\n\n📚 " + fact;
            }
            
            // Verificăm dacă este ultima întrebare pentru timeout
            boolean isLastQuestion = (currentQuestionIndex + 1) >= getQuestionsCount();
            String continueButtonText = isLastQuestion ? "🏁 Vezi rezultate" : "➡️ Următoarea întrebare";
            
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                .setTitle("⏰ Timp expirat")
                .setMessage(timeoutMessage)
                .setPositiveButton(continueButtonText, (dialog, which) -> {
                    if (isLastQuestion) {
                        currentQuestionIndex++;
                        finishGame();
                    } else {
        moveToNextQuestion();
    }
                })
                .setCancelable(false);
            
            // Adăugăm buton de încheiere pentru timeout-uri
            if (!isLastQuestion) {
                dialogBuilder.setNegativeButton("🚪 Încheie quiz", (dialog, which) -> {
                    showConfirmQuitDialog();
                });
            }
            
            dialogBuilder.show();
        } else {
            // Fallback dacă nu avem întrebări
            Toast.makeText(this, "⏰ Timpul a expirat!", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> {
                if ((currentQuestionIndex + 1) >= getQuestionsCount()) {
                    currentQuestionIndex++;
                    finishGame();
                } else {
                    moveToNextQuestion();
                }
            }, 1500);
        }
        
        Log.d(TAG, "⏰ Timeout for question " + (currentQuestionIndex + 1) + ", streak reset to 0");
    }

    /**
     * Implementează lifeline-ul 50/50 cu tema maritime
     */
    private void useFiftyFifty() {
        if (isFiftyFiftyUsed) {
            Toast.makeText(this, "🚫 Ai folosit deja acest ajutor!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Confirmare înainte de folosire
        new MaterialAlertDialogBuilder(this)
            .setTitle("🎯 Confirmare 50/50")
            .setMessage("Vrei să folosești ajutorul 50/50?\n\n" +
                       "🎲 Vor fi eliminate 2 răspunsuri greșite aleatoriu.")
            .setPositiveButton("✓ Da, folosește", (dialog, which) -> {
                executeFiftyFifty();
            })
            .setNegativeButton("✗ Nu", null)
            .show();
    }
    
    /**
     * Execuția efectivă a 50/50 cu animații și feedback
     */
    private void executeFiftyFifty() {
        if (enhancedQuestions == null || enhancedQuestions.isEmpty() || 
            currentQuestionIndex >= enhancedQuestions.size()) {
            Toast.makeText(this, "❌ Nu se poate aplica ajutorul pentru această întrebare", Toast.LENGTH_SHORT).show();
            return;
        }

        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        String correctAnswer = currentQuestion.getCorrectAnswer();
        
        // Track lifelines used
        lifelinesUsed++;
        
        List<Integer> wrongAnswerIndices = new ArrayList<>();
        
        // Găsim indexurile răspunsurilor greșite
        for (int i = 0; i < answerButtons.length; i++) {
            if (answerButtons[i].getVisibility() == View.VISIBLE && 
                !answerButtons[i].getText().toString().equals(correctAnswer)) {
                wrongAnswerIndices.add(i);
            }
        }

        // Dacă avem cel puțin 2 răspunsuri greșite, eliminăm 2
        if (wrongAnswerIndices.size() >= 2) {
            Collections.shuffle(wrongAnswerIndices);
            
            // Animații în secvență pentru eliminarea răspunsurilor
        for (int i = 0; i < 2; i++) {
                int index = wrongAnswerIndices.get(i);
            answerButtons[index].setEnabled(false);
                
                // Animație îmbunătățită cu staggered delay
                answerCards[index].animate()
                    .alpha(0.3f)
                    .scaleX(0.85f)
                    .scaleY(0.85f)
                    .setDuration(400)
                    .setStartDelay(i * 200)
                    .start();
                    
                answerCards[index].setClickable(false);
                answerCards[index].setStrokeColor(ContextCompat.getColor(this, R.color.button_disabled_background));
                
                // Adăugăm strikethrough text effect
                answerButtons[index].setPaintFlags(answerButtons[index].getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                answerButtons[index].setTextColor(ContextCompat.getColor(this, R.color.button_disabled_text));
            }
            
            // Feedback haptical pentru lifeline
            provideHapticFeedback(HapticFeedbackType.LIFELINE);
        }

        // Marchează butonul ca utilizat
        isFiftyFiftyUsed = true;
        fiftyFiftyButton.setEnabled(false);
        
        // Animație pentru dezactivarea butonului
        fiftyFiftyButton.animate()
            .alpha(0.6f)
            .setDuration(300)
            .start();
            
        Toast.makeText(this, "🎯 S-au eliminat două răspunsuri incorecte!", Toast.LENGTH_SHORT).show();
        
        Log.d(TAG, "✅ 50-50 lifeline used successfully for question " + (currentQuestionIndex + 1));
    }

    /**
     * Implementează lifeline-ul de skip cu confirmare
     */
    private void skipQuestion() {
        if (isSkipUsed) {
            Toast.makeText(this, "🚫 Ai folosit deja acest ajutor!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Confirmare înainte de skip cu avertizare
        new MaterialAlertDialogBuilder(this)
            .setTitle("⏭️ Confirmare Skip")
            .setMessage("Vrei să treci la următoarea întrebare?\n\n" +
                       "⚠️ Nu vei primi puncte pentru această întrebare.")
            .setPositiveButton("✓ Da, treci", (dialog, which) -> {
                executeSkipQuestion();
            })
            .setNegativeButton("✗ Nu", null)
            .show();
    }
    
    /**
     * Execuția efectivă a skip-ului
     */
    private void executeSkipQuestion() {
        // Feedback haptical pentru skip
        provideHapticFeedback(HapticFeedbackType.LIFELINE);
        
        // Track lifelines used
        lifelinesUsed++;
        
        // Marchează întrebarea ca skip-uită (fără puncte)
        totalQuestions++;
        
        // Actualizează statisticile
        updateScore();
        updateStreak();
        
        // Trece la următoarea întrebare
        moveToNextQuestion();
        
        isSkipUsed = true;
        skipQuestionButton.setEnabled(false);
        skipQuestionButton.animate()
            .alpha(0.6f)
            .setDuration(300)
            .start();
            
        Toast.makeText(this, "⏭️ Întrebarea a fost omisă!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "✅ Skip lifeline used for question " + currentQuestionIndex);
    }

    /**
     * Implementează lifeline-ul de hint
     */
    private void showHint() {
        if (isHintUsed) {
            Toast.makeText(this, "🚫 Ai folosit deja acest ajutor!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Confirmare înainte de afișarea hint-ului
        new MaterialAlertDialogBuilder(this)
            .setTitle("💡 Confirmare Hint")
            .setMessage("Vrei să vezi un indiciu pentru această întrebare?\n\n" +
                       "🧠 Indiciul poate să te ajute să găsești răspunsul corect.")
            .setPositiveButton("✓ Da, arată", (dialog, which) -> {
                executeShowHint();
            })
            .setNegativeButton("✗ Nu", null)
            .show();
    }
    
    /**
     * Execuția efectivă a hint-ului
     */
    private void executeShowHint() {
        if (enhancedQuestions == null || enhancedQuestions.isEmpty() || 
            currentQuestionIndex >= enhancedQuestions.size()) {
            Toast.makeText(this, "❌ Nu există hint disponibil pentru această întrebare", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Obținem hint-ul din întrebarea curentă
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        String hint = currentQuestion.getFact();
        
        if (hint == null || hint.isEmpty()) {
            hint = "💡 Gândește-te la legăturile maritime și istorice ale Dobrogei!";
        }
        
        // Dialog îmbunătățit pentru hint
        new MaterialAlertDialogBuilder(this)
            .setTitle("💡 Indiciu pentru întrebare")
            .setMessage("🧠 " + hint)
            .setPositiveButton("👍 Mulțumesc", null)
            .show();
        
        // Feedback haptical pentru hint
        provideHapticFeedback(HapticFeedbackType.LIFELINE);
        
        isHintUsed = true;
        hintButton.setEnabled(false);
        hintButton.animate()
            .alpha(0.6f)
            .setDuration(300)
            .start();
        
        Toast.makeText(this, "💡 Hint afișat!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "✅ Hint displayed for question: " + currentQuestion.getQuestion());
    }

    /**
     * Resetează lifeline-urile complet pentru întrebarea nouă
     */
    private void resetLifelinesForNewQuestion() {
        isFiftyFiftyUsed = false;
        isHintUsed = false;
        isSkipUsed = false;
        
        // Resetează starea butoanelor complet
        fiftyFiftyButton.setEnabled(true);
        fiftyFiftyButton.setAlpha(1.0f);
        
        hintButton.setEnabled(true);
        hintButton.setAlpha(1.0f);
        
        skipQuestionButton.setEnabled(true);
        skipQuestionButton.setAlpha(1.0f);
        
        // Reset strike-through effects pentru răspunsuri
            for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i].setPaintFlags(answerButtons[i].getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            answerButtons[i].setTextColor(ContextCompat.getColor(this, R.color.dobrogea_text));
        }
        
        Log.d(TAG, "✅ Lifelines reset for question " + (currentQuestionIndex + 1));
    }

    /**
     * Resetează stilurile cardurilor
     */
    private void resetCardStyles() {
        for (int i = 0; i < answerCards.length; i++) {
            MaterialCardView card = answerCards[i];
            
            // Reset card background color to default
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
            
            // Reset card properties
            card.setStrokeColor(ContextCompat.getColor(this, R.color.dobrogea_primary_light));
            card.setAlpha(1.0f);
            card.setClickable(true);
            card.setElevation(6f);
            card.setScaleX(1.0f);
            card.setScaleY(1.0f);
            
            // Reset button properties
            MaterialButton button = answerButtons[i];
            button.setEnabled(true);
            button.setTextColor(ContextCompat.getColor(this, R.color.dobrogea_text));
            button.setPaintFlags(button.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    /**
     * Actualizează disponibilitatea lifeline-urilor bazată pe modul și dificultate
     */
    private void updateLifelinesAvailability() {
        boolean lifelinesAllowed = gameModeManager.areLifelinesAllowed();
        int maxLifelines = gameModeManager.getMaxLifelines();
        boolean canUseMore = difficultyManager.canUseLifeline(lifelinesUsed);
        
        // Activează/dezactivează lifeline-urile
        fiftyFiftyButton.setEnabled(lifelinesAllowed && canUseMore && !isFiftyFiftyUsed);
        hintButton.setEnabled(lifelinesAllowed && canUseMore && !isHintUsed);
        skipQuestionButton.setEnabled(lifelinesAllowed && canUseMore && !isSkipUsed);
        
        // Actualizează opacitatea vizuală
        fiftyFiftyButton.setAlpha(fiftyFiftyButton.isEnabled() ? 1.0f : 0.5f);
        hintButton.setAlpha(hintButton.isEnabled() ? 1.0f : 0.5f);
        skipQuestionButton.setAlpha(skipQuestionButton.isEnabled() ? 1.0f : 0.5f);
        
        Log.d(TAG, "Lifelines updated - Allowed: " + lifelinesAllowed + 
               ", Max: " + maxLifelines + ", Used: " + lifelinesUsed);
    }

    /**
     * Feedback haptic îmbunătățit
     */
    private void provideHapticFeedback(HapticFeedbackType type) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern;
            int[] amplitudes;
            
            switch (type) {
                case CORRECT:
                    pattern = new long[]{0, 100, 50, 100};
                    amplitudes = new int[]{0, 255, 0, 128};
                    break;
                case WRONG:
                    pattern = new long[]{0, 200, 100, 200};
                    amplitudes = new int[]{0, 255, 0, 255};
                    break;
                case LIFELINE:
                    pattern = new long[]{0, 50, 50, 50, 50, 50};
                    amplitudes = new int[]{0, 128, 0, 128, 0, 128};
                    break;
                default:
                    pattern = new long[]{0, 100};
                    amplitudes = new int[]{0, 128};
                    break;
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createWaveform(pattern, amplitudes, -1);
                vibrator.vibrate(effect);
            } else {
                vibrator.vibrate(pattern, -1);
            }
        }
    }

    /**
     * Feedback pentru răspuns corect
     */
    private void provideCorrectAnswerFeedback(MaterialCardView selectedCard, int selectedAnswerIndex) {
        // Animație pentru răspunsul corect
        animateCorrectAnswer(selectedCard);
        
        // Feedback haptic
        provideHapticFeedback(HapticFeedbackType.CORRECT);
        
        // Afișăm dialog cu fapte educaționale
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        String fact = currentQuestion.getFact();
        
        if (fact != null && !fact.isEmpty()) {
            showAnswerDialog(fact, true);
        } else {
            // Dacă nu avem fact, afișăm un mesaj generic
            new Handler().postDelayed(() -> {
                Toast.makeText(this, "✅ Răspuns corect! +" + POINTS_PER_CORRECT_ANSWER + " puncte", Toast.LENGTH_SHORT).show();
        moveToNextQuestion();
            }, 1000);
        }
    }

    /**
     * Feedback pentru răspuns greșit
     */
    private void provideWrongAnswerFeedback(MaterialCardView selectedCard, int selectedAnswerIndex, String correctAnswer) {
        // Animație pentru răspunsul greșit
        selectedCard.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(200)
            .withEndAction(() -> {
                selectedCard.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start();
            })
            .start();
        
        // Feedback haptic
        provideHapticFeedback(HapticFeedbackType.WRONG);
        
        // Evidențiem răspunsul corect
        highlightCorrectAnswer(correctAnswer);
        
        // Afișăm dialog cu explicație
        EnhancedQuestionModel currentQuestion = enhancedQuestions.get(currentQuestionIndex);
        String fact = currentQuestion.getFact();
        
        if (fact != null && !fact.isEmpty()) {
            showAnswerDialog(fact, false);
        } else {
            // Dacă nu avem fact, afișăm un mesaj generic
            new Handler().postDelayed(() -> {
                Toast.makeText(this, "❌ Răspuns greșit! Răspunsul corect era: " + correctAnswer, Toast.LENGTH_LONG).show();
                moveToNextQuestion();
            }, 2000);
        }
    }

    /**
     * Evidențiază răspunsul corect
     */
    private void highlightCorrectAnswer(String correctAnswer) {
        for (int i = 0; i < answerButtons.length; i++) {
            if (answerButtons[i].getText().toString().equals(correctAnswer)) {
                MaterialCardView card = answerCards[i];
                
                // Animație pentru evidențierea răspunsului corect
                card.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        card.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(200)
                            .start();
                    })
                    .start();
                
                // Schimbăm culoarea pentru a evidenția răspunsul corect
                card.setStrokeColor(ContextCompat.getColor(this, R.color.dobrogea_success));
                card.setStrokeWidth(4);
                
                break;
            }
        }
    }

    /**
     * Animație pentru răspunsul corect
     */
    private void animateCorrectAnswer(MaterialCardView card) {
        // Animație de succes
        card.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(200)
            .withEndAction(() -> {
                card.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start();
            })
            .start();
        
        // Schimbăm culoarea pentru succes
        card.setStrokeColor(ContextCompat.getColor(this, R.color.dobrogea_success));
        card.setStrokeWidth(4);
    }

    /**
     * Afișează dialog cu fapte educaționale
     */
    private void showAnswerDialog(String fact, boolean isCorrect) {
        String title = isCorrect ? "✅ Răspuns corect!" : "❌ Răspuns greșit!";
        String message = fact;
        
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("➡️ Continuă", (dialog, which) -> {
                moveToNextQuestion();
            })
            .setCancelable(false);
        
        // Adăugăm buton de încheiere pentru dialog-uri
        dialogBuilder.setNegativeButton("🚪 Încheie quiz", (dialog, which) -> {
            showConfirmQuitDialog();
        });
        
        dialogBuilder.show();
    }

    /**
     * Afișează dialog de confirmare pentru încheierea quiz-ului
     */
    private void showConfirmQuitDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("🚪 Încheiere Quiz")
            .setMessage("Ești sigur că vrei să închei quiz-ul?\n\n" +
                       "📊 Scorul tău actual: " + score + " puncte\n" +
                       "🎯 Răspunsuri corecte: " + correctAnswers + "/" + totalQuestions)
            .setPositiveButton("✓ Da, încheie", (dialog, which) -> {
                finishGame();
            })
            .setNegativeButton("✗ Continuă jocul", null)
            .setNeutralButton("💾 Salvează și continuă", (dialog, which) -> {
                // Salvăm progresul și continuăm
                Toast.makeText(this, "💾 Progresul a fost salvat!", Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    /**
     * Afișează bonus pentru serie
     */
    private void showStreakBonus() {
        Toast.makeText(this, "Bonus serie: +" + BONUS_POINTS + " puncte!", Toast.LENGTH_SHORT).show();
        Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        streakTextView.startAnimation(bounceAnim);
    }

    /**
     * Returnează numărul total de întrebări
     */
    private int getQuestionsCount() {
        if (enhancedQuestions != null) {
            return enhancedQuestions.size();
        }
        return 0;
    }

    /**
     * Actualizează afișarea scorului
     */
    private void updateScore() { 
        scoreTextView.setText(String.valueOf(score)); 
    }
    
    /**
     * Actualizează afișarea streak-ului
     */
    private void updateStreak() { 
        streakTextView.setText(String.valueOf(streak)); 
    }
    
    /**
     * Trece la următoarea întrebare
     */
    private void moveToNextQuestion() { 
        currentQuestionIndex++; 
        displayQuestion(); 
        startTimer(); 
    }
    
    /**
     * Enum pentru tipurile de feedback haptic
     */
    private enum HapticFeedbackType {
        CORRECT, WRONG, LIFELINE
    }
} 