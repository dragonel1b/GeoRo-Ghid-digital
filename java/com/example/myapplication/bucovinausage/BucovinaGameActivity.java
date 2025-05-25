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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.Bucovina;
import com.example.myapplication.RomApp.PointsManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.bumptech.glide.Glide;
import java.util.Locale;

public class BucovinaGameActivity extends AppCompatActivity {
    private TextView questionTextView;
    private MaterialButton[] answerButtons;
    private TextView scoreTextView;
    private ProgressBar progressBar;
    private TextView timerTextView;
    private TextView streakTextView;
    private ImageView questionImage;
    private MaterialButton fiftyFiftyButton;
    private MaterialButton skipQuestionButton;
    private MaterialCardView[] answerCards;
    private MaterialButton finishButton;
    private CardView questionImageCard;
    private TextView factTextView;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int streak = 0;
    private int maxStreak = 0;
    private int totalQuestions = 0;
    private int correctAnswers = 0;
    private long totalTime = 0;
    private List<QuestionModel> questions;
    private static final int POINTS_PER_CORRECT_ANSWER = 10;
    private static final int BONUS_POINTS = 50;
    private static final int TIME_PER_QUESTION = 30; // in seconds
    private static final int STREAK_BONUS_THRESHOLD = 3;
    private PointsManager pointsManager;
    private CountDownTimer timer;
    private boolean isFiftyFiftyUsed = false;
    private boolean isSkipUsed = false;
    private Random random = new Random();

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

        initializeViews();
        pointsManager = PointsManager.getInstance(this);
        initializeQuestions();
        setupLifelines();
        applyBucovinaTheme();
        applyButtonStyles();
        displayQuestion();
        updateScore();
        startTimer();
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

    private void startTimer() {
        // Cancel existing timer if running
        if (timer != null) {
            timer.cancel();
        }
        
        // Start a new timer from TIME_PER_QUESTION seconds
        timerTextView.setText(String.valueOf(TIME_PER_QUESTION));
        
        timer = new CountDownTimer(TIME_PER_QUESTION * 1000, 1000) {
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
        questions = new ArrayList<>();
        
        // Bucovina-specific questions
        questions.add(new QuestionModel(
            "Care este capitala județului Suceava?",
            "Suceava", 
            new String[]{"Rădăuți", "Câmpulung Moldovenesc", "Fălticeni"}, 
            R.drawable.suceava));
        
        questions.add(new QuestionModel(
            "În ce an a fost construită Mănăstirea Voroneț?",
            "1488", 
            new String[]{"1476", "1504", "1527"}, 
            R.drawable.manastire_voronet));
        
        questions.add(new QuestionModel(
            "Ce culoare este specifică frescelor exterioare de la Voroneț?",
            "Albastru", 
            new String[]{"Verde", "Roșu", "Galben"}, 
            R.drawable.manastire_voronet));
        
        questions.add(new QuestionModel(
            "Care dintre următoarele mănăstiri NU se află în Bucovina?",
            "Mănăstirea Cozia", 
            new String[]{"Mănăstirea Putna", "Mănăstirea Sucevița", "Mănăstirea Humor"}, 
            R.drawable.cozia));
        
        questions.add(new QuestionModel(
            "Cine a fost domnitorul care a ctitorit Mănăstirea Putna?",
            "Ștefan cel Mare", 
            new String[]{"Alexandru cel Bun", "Petru Rareș", "Mihai Viteazul"}, 
            R.drawable.manastirea_putna));
        
        questions.add(new QuestionModel(
            "Care este cea mai înaltă vârful montan din Bucovina?",
            "Vârful Pietrosul Călimanilor", 
            new String[]{"Vârful Rarău", "Vârful Giumalău", "Vârful Suhard"}, 
            R.drawable.varful_pietros));
        
        questions.add(new QuestionModel(
            "Care este obiceiul tradițional de iarnă specific Bucovinei, în care tineri mascați colindă satele?",
            "Urșii", 
            new String[]{"Capra", "Căiuții", "Malanca"}, 
            R.drawable.bucovina));
        
        questions.add(new QuestionModel(
            "Care dintre următoarele localități este cunoscută ca 'Mica Vienă' a Bucovinei?",
            "Cernăuți", 
            new String[]{"Suceava", "Rădăuți", "Gura Humorului"}, 
            R.drawable.mica_viena));
        
        questions.add(new QuestionModel(
            "Ce tehnica artizanala este foarte cunoscută în zona Bucovinei?",
            "Încondeiat ouă", 
            new String[]{"Țesutul covoarelor", "Sculptat lemn", "Olăritul"}, 
            R.drawable.inc_oua));
        
        questions.add(new QuestionModel(
            "În ce perioadă istorică a fost Bucovina parte din Imperiul Habsburgic?",
            "1775-1918", 
            new String[]{"1812-1918", "1699-1859", "1821-1918"}, 
            R.drawable.imp_has));
        
        totalQuestions = questions.size();
        Collections.shuffle(questions);
    }
    
    private void displayQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            showFinishButton();
            return;
        }

        // Get the current question
        QuestionModel currentQuestion = questions.get(currentQuestionIndex);
        
        // Reset UI elements
        resetCardStyles();
        factTextView.setVisibility(View.GONE);
        
        // Set question text
        questionTextView.setText(currentQuestion.getQuestion());
        
        // Set answer options
        String[] answers = currentQuestion.getAnswers();
        for (int i = 0; i < Math.min(answers.length, answerButtons.length); i++) {
            answerButtons[i].setText(answers[i]);
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
        if (currentQuestionIndex >= questions.size() || !answerCards[selectedAnswerIndex].isEnabled()) {
            return;
        }

        // Get current question
        QuestionModel currentQuestion = questions.get(currentQuestionIndex);
        int correctAnswerIndex = currentQuestion.getCorrectAnswerIndex();

        // Disable all answer cards to prevent multiple answers
        for (MaterialCardView card : answerCards) {
            card.setEnabled(false);
            card.setClickable(false);
        }

        // Cancel timer
        if (timer != null) {
            timer.cancel();
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
            
            // Reset streak
            streak = 0;
        } else {
            // Correct answer - update score and streak
            correctAnswers++;
            
            // Calculate score with time bonus
            int timeRemaining = Math.max(0, Integer.parseInt(timerTextView.getText().toString()));
            int questionScore = POINTS_PER_CORRECT_ANSWER + timeRemaining;
            
            // Add streak bonus
            streak++;
            if (streak >= STREAK_BONUS_THRESHOLD) {
                questionScore += BONUS_POINTS;
                Toast.makeText(this, "Bonus pentru " + streak + " răspunsuri consecutive!", Toast.LENGTH_SHORT).show();
            }
            
            score += questionScore;
            updateScore();
            Toast.makeText(this, "+" + questionScore + " puncte!", Toast.LENGTH_SHORT).show();
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
        
        // Send result back to calling activity and to GameOverActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("GAME_SCORE", score);
        setResult(RESULT_OK, resultIntent);
        
        // Launch game over screen
        Intent intent = new Intent(this, BucovinaGameOverActivity.class);
        intent.putExtra("SCORE", score);
        intent.putExtra("CORRECT_ANSWERS", correctAnswers);
        intent.putExtra("TOTAL_QUESTIONS", totalQuestions);
        intent.putExtra("MAX_STREAK", maxStreak);
        intent.putExtra("ACHIEVEMENTS", getAchievements());
        intent.putExtra("REGION", "Bucovina");
        startActivity(intent);
        
        // Also add points directly to PointsManager
        pointsManager.addPoints(this, "bucovina", score);
        
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
    
    private void showFinishButton() {
        // Hide all answer cards
        for (MaterialCardView card : answerCards) {
            card.setVisibility(View.GONE);
        }
        
        // Hide question area
        questionTextView.setText("Felicitări! Ai terminat quiz-ul.");
        questionImageCard.setVisibility(View.GONE);
        
        // Show finish button with animation
        finishButton.setVisibility(View.VISIBLE);
        finishButton.animate()
            .alpha(1.0f)
            .setDuration(500)
            .start();
            
        // Update final score
        updateScore();
    }
    
    public void goBack(View view) {
        finish();
    }
} 