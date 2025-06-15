package com.example.myapplication.munteniausage;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.PointsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import android.annotation.SuppressLint;

/**
 * Activitate pentru jocul regiunii Muntenia
 */
public class MunteniaGameActivity extends AppCompatActivity {

    private static final int TOTAL_QUESTIONS = 10;
    private static final int TIME_PER_QUESTION = 20000; // 20 seconds in milliseconds
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
    
    private List<Question> questions;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_placeholder);
        
        // Setăm titlul jocului
        TextView titleText = findViewById(R.id.placeholderTitle);
        if (titleText != null) {
            titleText.setText("Joc Muntenia");
        }
        
        // Setăm mesajul
        TextView messageText = findViewById(R.id.placeholderMessage);
        if (messageText != null) {
            messageText.setText("Jocul pentru regiunea Muntenia va fi implementat în curând!");
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

        // Set click listeners for lifelines
        fiftyFiftyButton.setOnClickListener(v -> useFiftyFifty());
        skipQuestionButton.setOnClickListener(v -> skipQuestion());
    }

    private void setupSounds() {
        correctSound = MediaPlayer.create(this, R.raw.win_sound);
        wrongSound = MediaPlayer.create(this, R.raw.lose_sound);
        // Replace missing sound with existing ones
        clockSound = MediaPlayer.create(this, R.raw.clock_thinking);
        winSound = MediaPlayer.create(this, R.raw.win_sound);
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
    }

    @SuppressLint("SetTextI18n")
    private void displayQuestion(int index) {
        if (index >= selectedQuestions.size()) {
            showFinalScore();
            return;
        }

        timeLeftInMillis = TIME_PER_QUESTION;
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
        if (timer != null) {
            timer.cancel();
        }
        
        timeLeft = 30;
        timerTextView.setText(String.valueOf(timeLeft));
        
        timer = new CountDownTimer(30000, 1000) {
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
        if (answered) return;
        
        // Mark question as answered
        answered = true;
        
        // Stop timer
        if (timer != null) {
            timer.cancel();
        }
        
        QuizQuestion currentQuestion = selectedQuestions.get(currentQuestionIndex);
        int correctAnswerIndex = currentQuestion.getCorrectAnswerIndex();
        MaterialCardView selectedCard = getCardByIndex(selectedAnswerIndex);
        MaterialCardView correctCard = getCardByIndex(correctAnswerIndex);
        
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
            // Calculate score based on time left
            int timeBonus = (int) (timeLeftInMillis / 1000) * TIME_BONUS_POINTS;
            int questionScore = CORRECT_ANSWER_POINTS + timeBonus;
            score += questionScore;
            
            // Increase streak
            streak++;
            
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
        
        // Move to next question after delay
        new Handler().postDelayed(() -> {
            currentQuestionIndex++;
            
            // Check if we're at the end
            if (currentQuestionIndex >= selectedQuestions.size()) {
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
        
        // Save high score if current score is higher
        saveHighScore();
        
        // Play win sound
        if (winSound != null) {
            winSound.start();
        }
        
        // Generate achievements string
        StringBuilder achievements = new StringBuilder();
        
        // Add achievements based on score
        if (score >= 80) {
            achievements.append("Expert în Muntenia\n");
        }
        if (score >= 50) {
            achievements.append("Bun cunoscător al regiunii\n");
        }
        if (streak >= 3) {
            achievements.append("Streak master: " + streak + " răspunsuri consecutive\n");
        }
        
        // Get statistics
        int correctAnswers = 0;
        for (int i = 0; i < Math.min(currentQuestionIndex + 1, selectedQuestions.size()); i++) {
            if (answeredQuestions.contains(i)) {
                correctAnswers++;
            }
        }
        
        int totalQuestions = Math.min(currentQuestionIndex + 1, selectedQuestions.size());
        
        // Start GameOver activity with stats
        Intent intent = new Intent(this, MunteniaGameOverActivity.class);
        intent.putExtra("finalScore", score);
        intent.putExtra("longestStreak", streak);
        intent.putExtra("correctAnswers", correctAnswers);
        intent.putExtra("totalQuestions", totalQuestions);
        intent.putExtra("achievements", achievements.toString());
        startActivity(intent);
        finish();
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
        showExitConfirmation();
    }

    private void showExitConfirmation() {
        if (timer != null) {
            timer.cancel();
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Ieșire")
                .setMessage("Ești sigur că vrei să părăsești jocul? Progresul va fi pierdut.")
                .setPositiveButton("Da", (dialog, which) -> {
                    saveHighScore();
                    finish();
                })
                .setNegativeButton("Nu", (dialog, which) -> {
                    if (timer != null) {
                        startTimer();
                    }
                })
                .setCancelable(false)
                .show();
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
        if (!answered && timer == null) {
            startTimer();
        }
        
        // Reinitialize sounds if they were released
        if (correctSound == null) {
            setupSounds();
        }
    }
} 