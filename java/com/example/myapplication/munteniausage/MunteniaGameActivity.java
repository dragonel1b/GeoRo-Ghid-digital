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
    private Button finishButton;
    private ImageView questionImage;
    private Button backButton;
    private ImageButton fiftyFiftyButton;
    private ImageButton skipQuestionButton;
    
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
        setContentView(R.layout.activity_muntenia_game);

        initViews();
        setupSounds();
        loadHighScore();
        initQuestions();
        selectRandomQuestions();
        displayQuestion(currentQuestionIndex);
        startTimer();
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
        questionCard = findViewById(R.id.questionCard);
        backButton = findViewById(R.id.backButton);
        fiftyFiftyButton = findViewById(R.id.fiftyFiftyButton);
        skipQuestionButton = findViewById(R.id.skipQuestionButton);

        // Set progress bar max and progress
        progressBar.setMax(QUESTION_COUNT);
        progressBar.setProgress(currentQuestionIndex);

        // Set click listeners for answer buttons
        answerButton1.setOnClickListener(v -> checkAnswer(0));
        answerButton2.setOnClickListener(v -> checkAnswer(1));
        answerButton3.setOnClickListener(v -> checkAnswer(2));
        answerButton4.setOnClickListener(v -> checkAnswer(3));

        // Set click listener for finish button
        finishButton.setOnClickListener(v -> showFinalScore());
        finishButton.setVisibility(View.GONE);

        // Set click listeners for lifelines
        fiftyFiftyButton.setOnClickListener(v -> useFiftyFifty());
        skipQuestionButton.setOnClickListener(v -> skipQuestion());

        // Back button
        backButton.setOnClickListener(v -> showExitConfirmation());
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
        if (index < selectedQuestions.size()) {
            QuizQuestion currentQuestion = selectedQuestions.get(index);
            
            // Update UI with question
            questionTextView.setText(currentQuestion.getQuestion());
            
            // Set image if available
            if (currentQuestion.getImageResource() != 0) {
                questionImage.setVisibility(View.VISIBLE);
                questionImage.setImageResource(currentQuestion.getImageResource());
            } else {
                questionImage.setVisibility(View.GONE);
            }
            
            // Set up answers (shuffled)
            List<String> answers = new ArrayList<>(currentQuestion.getAnswers());
            
            // Set answer text
            answerButton1.setText(answers.get(0));
            answerButton2.setText(answers.get(1));
            answerButton3.setText(answers.get(2));
            answerButton4.setText(answers.get(3));
            
            // Reset button styles
            resetButtonStyles();
            
            // Update progress
            progressBar.setProgress(index + 1);
            
            // Hide fact text
            factTextView.setVisibility(View.GONE);
            
            // Reset answered state
            answered = false;
            
            // Show answer buttons
            enableAnswerButtons(true);
            
            // Hide finish button if not at the end
            if (index < selectedQuestions.size() - 1) {
                finishButton.setVisibility(View.GONE);
            }
            
            // Update streak text
            streakTextView.setText(String.format(Locale.getDefault(), "🔥 Reușite consecutive: %d", streak));
        } else {
            // All questions answered, show final score
            showFinalScore();
        }
    }

    private void resetButtonStyles() {
        answerButton1.setBackgroundTintList(getColorStateList(R.color.rom_primary));
        answerButton2.setBackgroundTintList(getColorStateList(R.color.rom_primary));
        answerButton3.setBackgroundTintList(getColorStateList(R.color.rom_primary));
        answerButton4.setBackgroundTintList(getColorStateList(R.color.rom_primary));
        
        answerButton1.setTextColor(Color.WHITE);
        answerButton2.setTextColor(Color.WHITE);
        answerButton3.setTextColor(Color.WHITE);
        answerButton4.setTextColor(Color.WHITE);
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
        
        answered = true;
        
        // Stop the timer
        if (timer != null) {
            timer.cancel();
        }
        
        // Stop the clock sound if playing
        if (clockSound != null && clockSound.isPlaying()) {
            clockSound.stop();
            try {
                clockSound.prepare();
            } catch (Exception e) {
                Log.e("MunteniaGame", "Error preparing clock sound: " + e.getMessage());
            }
        }
        
        QuizQuestion currentQuestion = selectedQuestions.get(currentQuestionIndex);
        int correctAnswerIndex = currentQuestion.getCorrectAnswerIndex();
        
        // Get the button that was clicked
        MaterialButton selectedButton = getButtonByIndex(selectedAnswerIndex);
        MaterialButton correctButton = getButtonByIndex(correctAnswerIndex);
        
        // Check if the answer is correct
        if (selectedAnswerIndex == correctAnswerIndex) {
            // Correct answer
            selectedButton.setBackgroundTintList(getColorStateList(R.color.rom_correct_answer));
            
            // Play correct sound
            if (correctSound != null) {
                correctSound.start();
            }
            
            // Calculate score (base + time bonus + streak bonus)
            int questionScore = CORRECT_POINTS + (timeLeft * TIME_BONUS_FACTOR);
            if (streak > 0) {
                questionScore += streak * STREAK_BONUS;
            }
            
            // Update streak
            streak++;
            streakTextView.setText(String.format(Locale.getDefault(), "🔥 Reușite consecutive: %d", streak));
            
            // Update score
            score += questionScore;
            updateScoreDisplay();
            
            // Show a toast with the score for this question
            Toast.makeText(this, 
                    String.format(Locale.getDefault(), "+%d puncte! (%d bază, %d timp, %d streak)", 
                            questionScore, CORRECT_POINTS, timeLeft * TIME_BONUS_FACTOR, (streak - 1) * STREAK_BONUS), 
                    Toast.LENGTH_SHORT).show();
            
        } else {
            // Wrong answer
            selectedButton.setBackgroundTintList(getColorStateList(R.color.rom_wrong_answer));
            correctButton.setBackgroundTintList(getColorStateList(R.color.rom_correct_answer));
            
            // Play wrong sound
            if (wrongSound != null) {
                wrongSound.start();
            }
            
            // Reset streak
            streak = 0;
            streakTextView.setText(String.format(Locale.getDefault(), "🔥 Reușite consecutive: %d", streak));
        }
        
        // Show fact about the answer
        factTextView.setText(currentQuestion.getFact());
        factTextView.setVisibility(View.VISIBLE);
        
        // Disable all answer buttons
        enableAnswerButtons(false);
        
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
        answerButton1.setEnabled(enable);
        answerButton2.setEnabled(enable);
        answerButton3.setEnabled(enable);
        answerButton4.setEnabled(enable);
    }

    private void updateScoreDisplay() {
        scoreTextView.setText(String.format(Locale.getDefault(), "Scor: %d", score));
    }

    private void useFiftyFifty() {
        if (fiftyFiftyUsed || answered) return;
        
        fiftyFiftyUsed = true;
        fiftyFiftyButton.setAlpha(0.5f);
        fiftyFiftyButton.setEnabled(false);
        
        QuizQuestion currentQuestion = selectedQuestions.get(currentQuestionIndex);
        int correctIndex = currentQuestion.getCorrectAnswerIndex();
        
        // Get two wrong answers to eliminate
        List<Integer> wrongIndices = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (i != correctIndex) {
                wrongIndices.add(i);
            }
        }
        
        // Shuffle and pick two
        Collections.shuffle(wrongIndices);
        List<Integer> toRemove = wrongIndices.subList(0, 2);
        
        // Disable these buttons
        for (int index : toRemove) {
            MaterialButton button = getButtonByIndex(index);
            button.setEnabled(false);
            button.setAlpha(0.3f);
        }
        
        // Show toast
        Toast.makeText(this, "Două răspunsuri greșite au fost eliminate!", Toast.LENGTH_SHORT).show();
    }

    private void skipQuestion() {
        if (skipUsed || answered) return;
        
        skipUsed = true;
        skipQuestionButton.setAlpha(0.5f);
        skipQuestionButton.setEnabled(false);
        
        // Skip to next question
        if (timer != null) {
            timer.cancel();
        }
        
        if (currentQuestionIndex < selectedQuestions.size() - 1) {
            currentQuestionIndex++;
            displayQuestion(currentQuestionIndex);
            startTimer();
            
            // Show toast
            Toast.makeText(this, "Întrebare omisă!", Toast.LENGTH_SHORT).show();
        } else {
            // This was the last question
            finishButton.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Nu mai sunt întrebări disponibile!", Toast.LENGTH_SHORT).show();
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
        super.onBackPressed();
        showExitConfirmation();
    }

    private void showExitConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ieșire");
        builder.setMessage("Ești sigur că vrei să părăsești jocul? Progresul va fi pierdut.");
        builder.setPositiveButton("Da", (dialog, which) -> {
            if (timer != null) {
                timer.cancel();
            }
            finish();
        });
        builder.setNegativeButton("Nu", null);
        builder.show();
    }

    public void goBack(View view) {
        showExitConfirmation();
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