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
    private ImageButton fiftyFiftyButton;
    private ImageButton skipQuestionButton;
    private MaterialCardView[] answerCards;
    private MaterialButton finishButton;
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
        fiftyFiftyButton = findViewById(R.id.fiftyFiftyButton);
        skipQuestionButton = findViewById(R.id.skipQuestionButton);
        finishButton = findViewById(R.id.finishButton);
        
        // Improve text visibility and style - with null check
        if (questionTextView != null) {
        questionTextView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        }
        
        // Apply button styles - with null checks
        for (int i = 0; i < answerButtons.length; i++) {
            MaterialButton button = answerButtons[i];
            if (button != null) {
            button.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            button.setElevation(4f);
            button.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            
            // Disable default button behavior to prevent display issues
            button.setClickable(false);
            button.setFocusable(false);
            }
        }
        
        // Initialize finish button - with null check
        if (finishButton != null) {
        finishButton.setOnClickListener(v -> finishGame());
        }
        
        // Setup click for cards - with null checks
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
    }
    
    private void applyBucovinaTheme() {
        // Apply Bucovina theme colors to UI elements
        int primaryColor = ContextCompat.getColor(this, R.color.bucovina_primary);
        int primaryLightColor = ContextCompat.getColor(this, R.color.bucovina_primary_light);
        int secondaryColor = ContextCompat.getColor(this, R.color.bucovina_secondary);
        int accentColor = ContextCompat.getColor(this, R.color.bucovina_accent);
        int textColor = ContextCompat.getColor(this, R.color.bucovina_text);
        int cardBgColor = ContextCompat.getColor(this, R.color.bucovina_card_bg);
        
        // Set status bar color
        getWindow().setStatusBarColor(primaryColor);
        
        // Apply to text elements - with null checks
        if (questionTextView != null) {
        questionTextView.setTextColor(textColor);
        }
        if (scoreTextView != null) {
        scoreTextView.setTextColor(textColor);
        }
        if (streakTextView != null) {
        streakTextView.setTextColor(textColor);
        }
        
        // Apply color to progress bar - with null check
        if (progressBar != null) {
        progressBar.setProgressTintList(ContextCompat.getColorStateList(this, R.color.bucovina_primary));
        progressBar.setProgressBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_background));
        }
        
        // Apply to answer cards and buttons - with null checks
        for (int i = 0; i < answerCards.length; i++) {
            if (answerCards[i] != null) {
            answerCards[i].setCardBackgroundColor(cardBgColor);
            answerCards[i].setStrokeColor(primaryLightColor);
            }
            if (i < answerButtons.length && answerButtons[i] != null) {
            answerButtons[i].setTextColor(textColor);
            }
        }
        
        // Apply to finish button - with null check
        if (finishButton != null) {
        finishButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_primary));
        finishButton.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
        finishButton.setStrokeColor(ContextCompat.getColorStateList(this, R.color.bucovina_primary_light));
        finishButton.setRippleColorResource(R.color.bucovina_accent);
        }
        
        // Apply to lifeline buttons - with null checks
        if (fiftyFiftyButton != null) {
        fiftyFiftyButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_primary_light));
            fiftyFiftyButton.setColorFilter(ContextCompat.getColor(this, R.color.white));
        }
        
        if (skipQuestionButton != null) {
            skipQuestionButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_primary_light));
        skipQuestionButton.setColorFilter(ContextCompat.getColor(this, R.color.white));
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
        if (timer != null) {
            timer.cancel();
        }
        
        // Only start the timer if timerTextView exists
        if (timerTextView != null) {
            timer = new CountDownTimer(TIME_PER_QUESTION * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                    int secondsRemaining = (int) (millisUntilFinished / 1000);
                    timerTextView.setText(String.valueOf(secondsRemaining));
                    
                    // Change color when time is running out
                    if (secondsRemaining <= 5) {
                        timerTextView.setTextColor(getResources().getColor(R.color.bucovina_incorrect_answer));
                } else {
                        timerTextView.setTextColor(getResources().getColor(R.color.transilvania_text));
                    }
            }

            @Override
            public void onFinish() {
                handleTimeout();
            }
        }.start();
        }
    }

    private void handleTimeout() {
        Toast.makeText(this, "Timpul a expirat!", Toast.LENGTH_SHORT).show();
        streak = 0;
        updateStreak();
        moveToNextQuestion();
    }

    private void useFiftyFifty() {
        if (isFiftyFiftyUsed) {
            Toast.makeText(this, "Ai folosit deja acest ajutor!", Toast.LENGTH_SHORT).show();
            return;
        }

        QuestionModel currentQuestion = questions.get(currentQuestionIndex);
        List<String> wrongAnswers = new ArrayList<>();
        for (String answer : currentQuestion.getIncorrectAnswers()) {
            wrongAnswers.add(answer);
        }
        Collections.shuffle(wrongAnswers);
        
        // Disable two wrong answers with visual feedback - with null checks
        for (int i = 0; i < Math.min(2, wrongAnswers.size()); i++) {
            String wrongAnswer = wrongAnswers.get(i);
            for (MaterialButton button : answerButtons) {
                if (button != null && button.getText().toString().equals(wrongAnswer)) {
                    button.setEnabled(false);
                }
            }
            for (MaterialCardView card : answerCards) {
                if (card != null && card.getCardBackgroundColor().getDefaultColor() == ContextCompat.getColor(this, R.color.bucovina_incorrect_answer)) {
                    card.setAlpha(0.5f);
                    card.setClickable(false);
                }
            }
        }

        isFiftyFiftyUsed = true;
        
        // Disable the button - with null check
        if (fiftyFiftyButton != null) {
        fiftyFiftyButton.setEnabled(false);
        fiftyFiftyButton.setAlpha(0.5f);
        }
    }
    
    private void skipQuestion() {
        if (isSkipUsed) {
            Toast.makeText(this, "Ai folosit deja acest ajutor!", Toast.LENGTH_SHORT).show();
            return;
        }

        moveToNextQuestion();
        isSkipUsed = true;
        
        // Disable the button - with null check
        if (skipQuestionButton != null) {
        skipQuestionButton.setEnabled(false);
        skipQuestionButton.setAlpha(0.5f);
        }
    }
    
    private void initializeQuestions() {
        questions = new ArrayList<>();
        
        // Bucovina-specific questions
        questions.add(new QuestionModel(
            "Care este capitala județului Suceava?",
            "Suceava", 
            new String[]{"Rădăuți", "Câmpulung Moldovenesc", "Fălticeni"}, 
            R.drawable.card_gradient_background_bucovina));
        
        questions.add(new QuestionModel(
            "În ce an a fost construită Mănăstirea Voroneț?",
            "1488", 
            new String[]{"1476", "1504", "1527"}, 
            R.drawable.card_gradient_background_bucovina));
        
        questions.add(new QuestionModel(
            "Ce culoare este specifică frescelor exterioare de la Voroneț?",
            "Albastru", 
            new String[]{"Verde", "Roșu", "Galben"}, 
            R.drawable.card_gradient_background_bucovina));
        
        questions.add(new QuestionModel(
            "Care dintre următoarele mănăstiri NU se află în Bucovina?",
            "Mănăstirea Cozia", 
            new String[]{"Mănăstirea Putna", "Mănăstirea Sucevița", "Mănăstirea Humor"}, 
            R.drawable.card_gradient_background_bucovina));
        
        questions.add(new QuestionModel(
            "Cine a fost domnitorul care a ctitorit Mănăstirea Putna?",
            "Ștefan cel Mare", 
            new String[]{"Alexandru cel Bun", "Petru Rareș", "Mihai Viteazul"}, 
            R.drawable.card_gradient_background_bucovina));
        
        questions.add(new QuestionModel(
            "Care este cea mai înaltă vârful montan din Bucovina?",
            "Vârful Pietrosul Călimanilor", 
            new String[]{"Vârful Rarău", "Vârful Giumalău", "Vârful Suhard"}, 
            R.drawable.card_gradient_background_bucovina));
        
        questions.add(new QuestionModel(
            "Care este obiceiul tradițional de iarnă specific Bucovinei, în care tineri mascați colindă satele?",
            "Urșii", 
            new String[]{"Capra", "Căiuții", "Malanca"}, 
            R.drawable.card_gradient_background_bucovina));
        
        questions.add(new QuestionModel(
            "Care dintre următoarele localități este cunoscută ca 'Mica Vienă' a Bucovinei?",
            "Cernăuți", 
            new String[]{"Suceava", "Rădăuți", "Gura Humorului"}, 
            R.drawable.card_gradient_background_bucovina));
        
        questions.add(new QuestionModel(
            "Ce tehnica artizanala este foarte cunoscută în zona Bucovinei?",
            "Încondeiat ouă", 
            new String[]{"Țesutul covoarelor", "Sculptat lemn", "Olăritul"}, 
            R.drawable.card_gradient_background_bucovina));
        
        questions.add(new QuestionModel(
            "În ce perioadă istorică a fost Bucovina parte din Imperiul Habsburgic?",
            "1775-1918", 
            new String[]{"1812-1918", "1699-1859", "1821-1918"}, 
            R.drawable.card_gradient_background_bucovina));
        
        totalQuestions = questions.size();
        Collections.shuffle(questions);
    }
    
    private void displayQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            finishGame();
            return;
        }
        
        resetCardStyles();
        
        QuestionModel currentQuestion = questions.get(currentQuestionIndex);
        
        // Set question text with null check
        if (questionTextView != null) {
            questionTextView.setText(currentQuestion.getQuestion());
        }
        
        // Set answer texts with null checks
        for (int i = 0; i < answerButtons.length; i++) {
            if (i < currentQuestion.getIncorrectAnswers().length) {
                if (answerButtons[i] != null) {
                    answerButtons[i].setText(currentQuestion.getIncorrectAnswers()[i]);
                }
                if (answerCards[i] != null) {
            answerCards[i].setClickable(true);
                }
            }
        }
        
        // Set question image with null check
        if (questionImage != null) {
            if (currentQuestion.getImageResourceId() != 0) {
                questionImage.setImageResource(currentQuestion.getImageResourceId());
            questionImage.setVisibility(View.VISIBLE);
        } else {
            questionImage.setVisibility(View.GONE);
            }
        }
        
        // Update progress bar with null check
        if (progressBar != null) {
        progressBar.setMax(questions.size());
        progressBar.setProgress(currentQuestionIndex + 1);
        }
        
        // Show finish button on last question
        if (currentQuestionIndex == questions.size() - 1) {
            showFinishButton();
        }
    }
    
    private void resetCardStyles() {
        for (int i = 0; i < answerCards.length; i++) {
            MaterialCardView card = answerCards[i];
            if (card != null) {
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bucovina_card_bg));
            card.setStrokeColor(ContextCompat.getColor(this, R.color.bucovina_primary_light));
            card.setAlpha(1.0f);
            card.setClickable(true);
            }
        }
    }
    
    private void checkAnswer(int selectedAnswerIndex, String selectedAnswer) {
        if (selectedAnswerIndex >= questions.size()) {
            return;
        }
        
        QuestionModel currentQuestion = questions.get(currentQuestionIndex);
        boolean isCorrect = selectedAnswer.equals(currentQuestion.getCorrectAnswer());
        
        // Cancel the timer
        if (timer != null) {
            timer.cancel();
        }
        
        // Update streak and score
        if (isCorrect) {
            streak++;
            correctAnswers++;
            if (streak > maxStreak) {
                maxStreak = streak;
            }
            
            // Base points
            int pointsEarned = POINTS_PER_CORRECT_ANSWER;
            
            // Streak bonus
            if (streak >= STREAK_BONUS_THRESHOLD) {
                pointsEarned += BONUS_POINTS;
                Toast.makeText(this, "Bonus pentru serie: +" + BONUS_POINTS + " puncte!", Toast.LENGTH_SHORT).show();
            }
            
            score += pointsEarned;
            updateScore();
            updateStreak();
            
            // Show correct answer with green background
            if (selectedAnswerIndex < answerCards.length && answerCards[selectedAnswerIndex] != null) {
            answerCards[selectedAnswerIndex].setCardBackgroundColor(ContextCompat.getColor(this, R.color.bucovina_correct_answer));
            }
            
            // Show fact toast
            Toast.makeText(this, currentQuestion.getFact(), Toast.LENGTH_LONG).show();
        } else {
            streak = 0;
            updateStreak();
            
            // Show wrong answer with red background
            if (selectedAnswerIndex < answerCards.length && answerCards[selectedAnswerIndex] != null) {
            answerCards[selectedAnswerIndex].setCardBackgroundColor(ContextCompat.getColor(this, R.color.bucovina_incorrect_answer));
            }
            
            // Show correct answer with green background
            highlightCorrectAnswer();
        }
        
        // Disable all cards after answer
        for (MaterialCardView card : answerCards) {
            if (card != null) {
            card.setClickable(false);
            }
        }
        
        // Move to next question after delay
        new Handler().postDelayed(() -> moveToNextQuestion(), 2000);
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
        if (finishButton != null) {
        finishButton.setVisibility(View.VISIBLE);
        finishButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        }
    }
    
    public void goBack(View view) {
        finish();
    }
} 