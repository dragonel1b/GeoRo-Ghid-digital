package com.example.myapplication.maramuresusage;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
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
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.Maramures;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.databinding.ActivityMaramuresGameBinding;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MaramuresGameActivity extends AppCompatActivity {

    private static final int TOTAL_QUESTIONS = 10;
    private static final int TIME_PER_QUESTION = 20000; // 20 seconds in milliseconds
    private static final int CORRECT_ANSWER_POINTS = 50;
    private static final int TIME_BONUS_POINTS_PER_SECOND = 2; // Changed bonus logic
    private static final long ANSWER_FEEDBACK_DELAY_MS = 1000;
    private static final String PREFS_NAME = "MaramuresGamePrefs";
    private static final String HIGH_SCORE_KEY = "highScoreMaramures";

    private ActivityMaramuresGameBinding binding;

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int correctAnswers = 0;
    private int consecutiveCorrect = 0;
    private boolean fiftyFiftyUsed;
    private boolean skipQuestionUsed;
    private CountDownTimer timer;
    private long timeLeftInMillis = TIME_PER_QUESTION;
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private Random random = new Random();
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<MaterialButton> answerButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }

        super.onCreate(savedInstanceState);
        binding = ActivityMaramuresGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeViews();
        
        pointsManager = PointsManager.getInstance(this);
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadLifelineState();

        initQuestions();

        binding.progressBar.setMax(questions.size());
        binding.progressBar.setProgress(1);

        updateScoreText();

        setupButtonListeners();
        
        setupLifelines();

        displayQuestion();
    }

    private void initializeViews() {
        answerButtons = Arrays.asList(binding.btnAnswer1, binding.btnAnswer2, binding.btnAnswer3, binding.btnAnswer4);
        binding.btnBack.setOnClickListener(v -> showExitConfirmationDialog());
    }
    
    private void setupButtonListeners() {
        for (int i = 0; i < answerButtons.size(); i++) {
            final int index = i;
            answerButtons.get(i).setOnClickListener(v -> checkAnswer(index));
        }
        binding.btnFinish.setOnClickListener(v -> finishQuiz());
    }
    
    private void setupLifelines() {
        binding.btnFiftyFifty.setEnabled(!fiftyFiftyUsed);
        binding.btnFiftyFifty.setAlpha(fiftyFiftyUsed ? 0.5f : 1.0f);
        binding.btnFiftyFifty.setOnClickListener(v -> useFiftyFifty());

        binding.btnSkip.setEnabled(!skipQuestionUsed);
        binding.btnSkip.setAlpha(skipQuestionUsed ? 0.5f : 1.0f);
        binding.btnSkip.setOnClickListener(v -> useSkipQuestion());
    }

    private void initQuestions() {
        questions = new ArrayList<>();

        questions.add(new Question(
                "Care este capitala județului Maramureș?",
                R.drawable.baia_mare,
                new String[]{"Sighetu Marmației", "Baia Mare", "Borșa", "Vișeu de Sus"},
                1,
                "Baia Mare este reședința județului Maramureș și un important centru istoric minier."));

        questions.add(new Question(
                "Ce biserică din Maramureș este inclusă în patrimoniul UNESCO?",
                R.drawable.maramures_wooden_church,
                new String[]{"Biserica de piatră din Baia Mare", "Biserica Sf. Nicolae din Sighet", "Biserica de lemn din Budești", "Biserica Neagră"},
                2,
                "Biserica de lemn din Budești, construită în 1643, face parte din cele 8 biserici de lemn din Maramureș incluse în patrimoniul UNESCO."));

        questions.add(new Question(
                "Ce tradiție de iarnă este specifică Maramureșului?",
                R.drawable.maramures_traditions,
                new String[]{"Capra", "Ursul", "Colindatul Feciorilor", "Viflaimul"},
                2,
                "Colindatul Feciorilor este o veche tradiție maramureșeană ce se păstrează din vremuri străvechi, tinerii colindând casele din sat în perioada sărbătorilor de iarnă."));

        questions.add(new Question(
                "În ce an a fost eliberat ultimul deținut politic din închisoarea Sighet?",
                R.drawable.maramures_sighet_prison,
                new String[]{"1955", "1964", "1989", "1977"},
                1,
                "În 1964 au fost eliberați ultimii deținuți politici din închisoarea Sighet, locul unde elita intelectuală și politică interbelică a fost exterminată."));

        questions.add(new Question(
                "Ce materie primă a stat la baza dezvoltării orașului Baia Mare?",
                R.drawable.maramures_baia_mare_mining,
                new String[]{"Sarea", "Aurul și argintul", "Lemnul", "Cărbunele"},
                1,
                "Bogăția în aur și argint a zonei a făcut ca Baia Mare să devină un important centru minier încă din Evul Mediu."));

        questions.add(new Question(
                "Cimitirul Vesel se află în localitatea:",
                R.drawable.cimitir_vesel,
                new String[]{"Bârsana", "Săpânța", "Botiza", "Ieud"},
                1,
                "Cimitirul Vesel din Săpânța este renumit pentru crucile colorate și epitafurile pline de umor care narează viața defunctului."));

        questions.add(new Question(
                "Ce râu traversează Maramureșul Istoric?",
                R.drawable.maramures_river,
                new String[]{"Someș", "Iza", "Tisa", "Vișeu"},
                2,
                "Râul Tisa formează granița naturală între România și Ucraina, marcând limita nordică a Maramureșului Istoric."));

        questions.add(new Question(
                "Care dintre următoarele este un port tradițional maramureșean?",
                R.drawable.maramures_traditional_costume,
                new String[]{"Clop, gaci, zadie", "Suman, iţari, opinci", "Cojoc, cioareci, bundă", "Pieptar, șubă, leucă"},
                0,
                "Portul tradițional maramureșean include clop (pălărie), gaci (pantaloni din pânză) și zadie (fustă) pentru femei."));

        questions.add(new Question(
                "Ce meșteșug tradițional este specific Maramureșului?",
                R.drawable.maramures_crafts,
                new String[]{"Olăritul", "Țesutul covoarelor", "Prelucrarea lemnului", "Încondeierea ouălor"},
                2,
                "Prelucrarea lemnului este un meșteșug de bază în Maramureș, cunoscut ca 'țara lemnului', cu porți monumentale și case tradiționale din lemn."));

        questions.add(new Question(
                "Ce munte se află în Maramureș?",
                R.drawable.munte_tampa,
                new String[]{"Ceahlău", "Pietrosul Rodnei", "Făgăraș", "Moldoveanu"},
                1,
                "Vârful Pietrosul Rodnei (2303 m) este cel mai înalt din Munții Rodnei și din nordul României, situat la granița dintre județele Maramureș și Bistrița-Năsăud."));

        Collections.shuffle(questions);
        
        if (questions.size() > TOTAL_QUESTIONS) {
            questions = questions.subList(0, TOTAL_QUESTIONS);
        }
    }

    private void displayQuestion() {
        if (timer != null) {
            timer.cancel();
        }

        resetButtonStates();
        binding.tvFact.setVisibility(View.GONE);
        binding.tvStreak.setVisibility(View.GONE);

        if (currentQuestionIndex >= questions.size()) {
            finishQuiz();
            return;
        }

        Question question = questions.get(currentQuestionIndex);

        binding.tvQuestion.setText(question.getQuestionText());
        binding.ivQuestionImage.setImageResource(question.getImageResId());

        for (int i = 0; i < answerButtons.size(); i++) {
            answerButtons.get(i).setText(question.getAnswers()[i]);
            answerButtons.get(i).setVisibility(View.VISIBLE);
        }

        if (consecutiveCorrect > 0) {
            binding.tvStreak.setVisibility(View.VISIBLE);
            binding.tvStreak.setText(getString(R.string.streak_text, consecutiveCorrect));
        }

        timeLeftInMillis = TIME_PER_QUESTION;
        startTimer();

        binding.progressBar.setProgress(currentQuestionIndex + 1);
    }

    private void resetButtonStates() {
        for (MaterialButton button : answerButtons) {
            button.setEnabled(true);
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.answer_button_background_tint));
            button.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
        binding.tvQuestion.setTextColor(ContextCompat.getColor(this, R.color.default_text_color));
    }

    private void startTimer() {
        timer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateTimerText(0);
                handleTimeout();
            }
        }.start();
    }

    private void updateTimerText(long millisUntilFinished) {
        int seconds = (int) (millisUntilFinished / 1000);
        binding.tvTimer.setText(String.format(Locale.getDefault(), "%02d", seconds));
        if (seconds <= 5) {
            binding.tvTimer.setTextColor(Color.RED);
        } else {
            binding.tvTimer.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void handleTimeout() {
        disableAnswerButtons();

        binding.tvQuestion.setText(R.string.time_expired);
        binding.tvQuestion.setTextColor(Color.RED);

        MaterialButton[] buttons = {answerButtons.get(0), answerButtons.get(1), answerButtons.get(2), answerButtons.get(3)};
        int correctAnswerIndex = questions.get(currentQuestionIndex).getCorrectAnswerIndex();
        
        buttons[correctAnswerIndex].setBackgroundTintList(
                ContextCompat.getColorStateList(this, R.color.correct_answer_green));
        
        consecutiveCorrect = 0;
        
        String fact = questions.get(currentQuestionIndex).getFact();
        if (fact != null && !fact.isEmpty()) {
            binding.tvFact.setText(fact);
            binding.tvFact.setVisibility(View.VISIBLE);
        }
        
        handler.postDelayed(this::moveToNextQuestion, ANSWER_FEEDBACK_DELAY_MS * 2);
    }

    private void checkAnswer(int selectedAnswerIndex) {
        if (timer != null) {
            timer.cancel();
        }

        disableAnswerButtons();

        MaterialButton[] buttons = {answerButtons.get(0), answerButtons.get(1), answerButtons.get(2), answerButtons.get(3)};
        int correctAnswerIndex = questions.get(currentQuestionIndex).getCorrectAnswerIndex();

        if (selectedAnswerIndex == correctAnswerIndex) {
            buttons[selectedAnswerIndex].setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.correct_answer_green));

            int timeBonus = (int) (timeLeftInMillis / 1000) * TIME_BONUS_POINTS_PER_SECOND;
            score += CORRECT_ANSWER_POINTS + timeBonus;
            updateScoreText();
            
            correctAnswers++;
            consecutiveCorrect++;

            Toast.makeText(this, "+" + (CORRECT_ANSWER_POINTS + timeBonus) + " puncte", Toast.LENGTH_SHORT).show();

            if (consecutiveCorrect >= 2) {
                binding.tvStreak.setText(getString(R.string.streak_text, consecutiveCorrect));
                binding.tvStreak.setVisibility(View.VISIBLE);
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(binding.tvStreak, "alpha", 0f, 1f);
                fadeIn.setDuration(500).start();
            }
        } else {
            buttons[selectedAnswerIndex].setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.wrong_answer_red));
            buttons[correctAnswerIndex].setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.correct_answer_green));
                    
            consecutiveCorrect = 0;
        }
        
        String fact = questions.get(currentQuestionIndex).getFact();
        if (fact != null && !fact.isEmpty()) {
            binding.tvFact.setText(fact);
            binding.tvFact.setVisibility(View.VISIBLE);
        }

        handler.postDelayed(this::moveToNextQuestion, ANSWER_FEEDBACK_DELAY_MS);
    }

    private void disableAnswerButtons() {
        for (MaterialButton button : answerButtons) {
            button.setEnabled(false);
        }
    }

    private void updateScoreText() {
        binding.tvScore.setText(getString(R.string.score_text, score));
    }

    private void moveToNextQuestion() {
        currentQuestionIndex++;
        
        if (currentQuestionIndex < questions.size()) {
            displayQuestion();
        } else {
            finishQuiz();
        }
    }

    private void useFiftyFifty() {
        if (fiftyFiftyUsed || currentQuestionIndex >= questions.size()) return;

        fiftyFiftyUsed = true;
        saveLifelineState();
        binding.btnFiftyFifty.setEnabled(false);
        binding.btnFiftyFifty.setAlpha(0.5f);

        Question currentQuestion = questions.get(currentQuestionIndex);
        int correctAnswerIndex = currentQuestion.getCorrectAnswerIndex();
        List<Integer> incorrectIndices = new ArrayList<>();
        for (int i = 0; i < answerButtons.size(); i++) {
            if (i != correctAnswerIndex) {
                incorrectIndices.add(i);
            }
        }
        Collections.shuffle(incorrectIndices);

        if (incorrectIndices.size() >= 2) {
            answerButtons.get(incorrectIndices.get(0)).setVisibility(View.INVISIBLE);
            answerButtons.get(incorrectIndices.get(1)).setVisibility(View.INVISIBLE);
        }
    }

    private void useSkipQuestion() {
        if (skipQuestionUsed || currentQuestionIndex >= questions.size() - 1) {
            if(skipQuestionUsed) {
                Toast.makeText(this, R.string.skip_used_toast, Toast.LENGTH_SHORT).show();
            } else {
                skipQuestionUsed = true;
                saveLifelineState();
                binding.btnSkip.setEnabled(false);
                binding.btnSkip.setAlpha(0.5f);
            }
            moveToNextQuestion();
            Toast.makeText(this, R.string.question_skipped_toast, Toast.LENGTH_SHORT).show();
        }
    }

    private void finishQuiz() {
        if (timer != null) {
            timer.cancel();
        }
        int finalScore = score;
        int highScore = sharedPreferences.getInt(HIGH_SCORE_KEY, 0);
        pointsManager.addPoints(this, "maramures", finalScore);

        boolean newHighScore = finalScore > highScore;
        if (newHighScore) {
            sharedPreferences.edit().putInt(HIGH_SCORE_KEY, finalScore).apply();
        }

        showResultDialog(finalScore, correctAnswers, questions.size(), newHighScore, highScore);
    }

    private void showResultDialog(int finalScore, int correctCount, int totalCount, boolean newHighScore, int oldHighScore) {
        String title = newHighScore ? "🎉 Nou Record! 🎉" : "Quiz Terminat!";
        String message = String.format(Locale.getDefault(),
                "Scorul tău: %d\nRăspunsuri corecte: %d/%d",
                finalScore, correctCount, totalCount);
        if (newHighScore) {
            message += String.format(Locale.getDefault(), "\nFelicitări! Ai depășit recordul anterior de %d puncte!", oldHighScore);
        } else {
            message += String.format(Locale.getDefault(), "\nRecordul actual este: %d puncte", oldHighScore);
        }

        new AlertDialog.Builder(this, R.style.AlertDialogCustom)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Înapoi la Maramureș", (dialog, which) -> navigateBackToMaramures())
                .setNegativeButton("Meniul Principal", (dialog, which) -> navigateToMainMenu())
                .setNeutralButton("Reîncearcă", (dialog, which) -> restartQuiz())
                .setCancelable(false)
                .show();
    }

    private void navigateBackToMaramures() {
        Intent intent = new Intent(MaramuresGameActivity.this, Maramures.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToMainMenu() {
        finishAffinity();
    }

    private void restartQuiz() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showExitConfirmationDialog();
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this, R.style.AlertDialogCustom)
            .setTitle("Ești sigur?")
            .setMessage("Progresul actual va fi pierdut.")
            .setPositiveButton("Da, ies", (dialog, which) -> {
                 if (timer != null) timer.cancel();
                 super.onBackPressed();
            })
            .setNegativeButton("Nu", null)
            .show();
    }

    private void saveLifelineState() {
        sharedPreferences.edit()
                .putBoolean("fiftyFiftyUsed", fiftyFiftyUsed)
                .putBoolean("skipQuestionUsed", skipQuestionUsed)
                .apply();
    }

    private void loadLifelineState() {
        fiftyFiftyUsed = sharedPreferences.getBoolean("fiftyFiftyUsed", false);
        skipQuestionUsed = sharedPreferences.getBoolean("skipQuestionUsed", false);
    }

    private static class Question {
        private final String questionText;
        private final int imageResId;
        private final String[] answers;
        private final int correctAnswerIndex;
        private final String fact;

        public Question(String questionText, int imageResId, String[] answers, int correctAnswerIndex, String fact) {
            this.questionText = questionText;
            this.imageResId = imageResId;
            this.answers = answers;
            this.correctAnswerIndex = correctAnswerIndex;
            this.fact = fact;
        }

        public String getQuestionText() {
            return questionText;
        }

        public int getImageResId() {
            return imageResId;
        }

        public String[] getAnswers() {
            return answers;
        }

        public int getCorrectAnswerIndex() {
            return correctAnswerIndex;
        }
        
        public String getFact() {
            return fact;
        }
    }
} 