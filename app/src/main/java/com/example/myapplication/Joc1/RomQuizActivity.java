package com.example.myapplication.Joc1;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.RomApplication;
import com.example.myapplication.security.SecurityManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Quiz activity for testing knowledge about Romania
 */
public class RomQuizActivity extends AppCompatActivity {
    private static final String TAG = "RomQuizActivity";
    
    // UI elements
    private TextView questionTextView, scoreTextView, questionCountTextView;
    private MaterialButton[] answerButtons;
    private MaterialButton nextQuestionButton;
    private ImageView questionImageView, feedbackIcon;
    private MaterialCardView questionCard, answersCard, scoreCard;
    private LinearProgressIndicator progressIndicator;
    
    // Game variables
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean answered = false;
    private int consecutiveCorrect = 0;
    private int totalQuestions = 0;
    
    // Security manager
    private SecurityManager securityManager;
    
    // Animation handler
    private Handler animationHandler = new Handler(Looper.getMainLooper());
    
    // Pre-load color state lists for better performance
    private android.content.res.ColorStateList primaryColor;
    private android.content.res.ColorStateList correctColor;
    private android.content.res.ColorStateList wrongColor;
    
    // Pre-load animations
    private Animation fadeIn;
    private Animation fadeOut;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable hardware acceleration for better performance
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        
        setContentView(R.layout.activity_rom_quiz);
        
        try {
            // Get security manager
            securityManager = ((RomApplication) getApplication()).getSecurityManager();
            
            // Pre-load resources for better performance
            preloadResources();
            
            // Initialize UI elements
            initializeViews();
            
            // Load questions
            loadQuestions();
            
            // Display first question
            displayQuestion(currentQuestionIndex);
            
            // Handle back button press
            setupBackButtonHandling();
            
        } catch (Exception e) {
            if (securityManager != null) {
                securityManager.handleException(this, e, 
                        getString(R.string.error_initializing_activity), true);
            } else {
                Log.e(TAG, "Error initializing activity", e);
                Toast.makeText(this, R.string.error_initializing_activity, Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void preloadResources() {
        // Pre-load color state lists
        primaryColor = ContextCompat.getColorStateList(this, R.color.rom_primary);
        correctColor = ContextCompat.getColorStateList(this, R.color.correct_answer_background);
        wrongColor = ContextCompat.getColorStateList(this, R.color.wrong_answer_background);
        
        // Pre-load animations
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
    }
    
    private void initializeViews() {
        // Find basic views
        questionTextView = findViewById(R.id.questionTextView);
        questionImageView = findViewById(R.id.questionImageView);
        scoreTextView = findViewById(R.id.scoreTextView);
        questionCountTextView = findViewById(R.id.questionCountTextView);
        feedbackIcon = findViewById(R.id.feedbackIcon);
        
        // Find container cards
        questionCard = findViewById(R.id.questionCard);
        answersCard = findViewById(R.id.answersCard);
        scoreCard = findViewById(R.id.scoreCard);
        
        // Progress indicator
        progressIndicator = findViewById(R.id.quizProgressIndicator);
        
        // Initialize answer buttons
        answerButtons = new MaterialButton[4];
        answerButtons[0] = findViewById(R.id.answerButton1);
        answerButtons[1] = findViewById(R.id.answerButton2);
        answerButtons[2] = findViewById(R.id.answerButton3);
        answerButtons[3] = findViewById(R.id.answerButton4);
        
        // Set click listeners for answer buttons
        for (int i = 0; i < answerButtons.length; i++) {
            final int buttonIndex = i;
            answerButtons[i].setOnClickListener(v -> checkAnswer(buttonIndex));
            
            // Set initial background tint
            answerButtons[i].setBackgroundTintList(primaryColor);
        }
        
        // Next question button
        nextQuestionButton = findViewById(R.id.nextQuestionButton);
        nextQuestionButton.setOnClickListener(v -> {
            nextQuestionButton.setVisibility(View.INVISIBLE);
            moveToNextQuestion();
        });
        
        // Set toolbar navigation (back button)
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Set initial score
        updateScore();
    }
    
    private void setupBackButtonHandling() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentQuestionIndex < questions.size() - 1) {
                    showExitConfirmationDialog();
                } else {
                    finish();
                }
            }
        });
    }
    
    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.minigame_exit_title)
                .setMessage(R.string.minigame_exit_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> finish())
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
    
    private void loadQuestions() {
        // Initialize questions list
        questions = new ArrayList<>();
        
        // Load questions from string arrays
        String[] questionTexts = getResources().getStringArray(R.array.minigame_questions);
        String[] correctAnswers = getResources().getStringArray(R.array.minigame_correct_answers);
        
        // For each question, set up answer options
        for (int i = 0; i < questionTexts.length; i++) {
            Question question = new Question();
            question.text = questionTexts[i];
            question.correctAnswer = correctAnswers[i];
            
            // Get answer options for this question
            int optionsArrayId = getOptionArrayForIndex(i);
            String[] options = getResources().getStringArray(optionsArrayId);
            
            question.options = options;
            questions.add(question);
        }
        
        // Shuffle questions
        Collections.shuffle(questions, new Random(System.currentTimeMillis()));
        
        // Set total questions
        totalQuestions = questions.size();
        
        // Configure progress indicator
        progressIndicator.setMax(totalQuestions);
        progressIndicator.setProgress(1);
    }
    
    private int getOptionArrayForIndex(int questionIndex) {
        switch (questionIndex) {
            case 0: return R.array.minigame_options_1;
            case 1: return R.array.minigame_options_2;
            case 2: return R.array.minigame_options_3;
            case 3: return R.array.minigame_options_4;
            case 4: return R.array.minigame_options_5;
            default: return R.array.minigame_options_1;
        }
    }
    
    private void displayQuestion(int index) {
        try {
            if (index < 0 || index >= questions.size()) {
                finishQuiz();
                return;
            }
            
            Question currentQuestion = questions.get(index);
            
            // Update progress
            progressIndicator.setProgress(index + 1);
            questionCountTextView.setText(getString(R.string.minigame_question_count, index + 1, totalQuestions));
            
            // Apply entrance animation for question card
            questionCard.startAnimation(fadeIn);
            answersCard.startAnimation(fadeIn);
            
            // Set question text
            questionTextView.setText(currentQuestion.text);
            
            // Handle question image if needed
            // For example: questionImageView.setImageResource(getImageForQuestion(index));
            // questionImageView.setVisibility(hasImage ? View.VISIBLE : View.GONE);
            
            // Set answer options - optimize by setting all properties at once
            for (int i = 0; i < answerButtons.length; i++) {
                if (i < currentQuestion.options.length) {
                    answerButtons[i].setText(currentQuestion.options[i]);
                    answerButtons[i].setVisibility(View.VISIBLE);
                    answerButtons[i].setEnabled(true);
                    answerButtons[i].setBackgroundTintList(primaryColor);
                    answerButtons[i].setAlpha(1f); // Reset alpha
                } else {
                    answerButtons[i].setVisibility(View.GONE);
                }
            }
            
            // Reset answered state
            answered = false;
            nextQuestionButton.setVisibility(View.INVISIBLE);
            
        } catch (Exception e) {
            securityManager.handleException(this, e, 
                    "Error displaying question", false);
        }
    }
    
    private void checkAnswer(int buttonIndex) {
        if (answered) return;
        
        answered = true;
        String selectedAnswer = answerButtons[buttonIndex].getText().toString();
        String correctAnswer = questions.get(currentQuestionIndex).correctAnswer;
        
        if (selectedAnswer.equals(correctAnswer)) {
            // Correct answer
            answerButtons[buttonIndex].setBackgroundTintList(correctColor);
            score += 10;
            consecutiveCorrect++;
            
            // Show correct feedback
            showFeedbackAnimation(true);
            
            // Bonus for streak
            if (consecutiveCorrect >= 3) {
                score += 5;
                Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.minigame_consecutive_bonus), Snackbar.LENGTH_SHORT).show();
            }
            
            updateScore();
        } else {
            // Wrong answer
            answerButtons[buttonIndex].setBackgroundTintList(wrongColor);
            consecutiveCorrect = 0;
            
            // Show incorrect feedback
            showFeedbackAnimation(false);
            
            // Highlight correct answer
            for (int i = 0; i < answerButtons.length; i++) {
                if (answerButtons[i].getText().toString().equals(correctAnswer)) {
                    answerButtons[i].setBackgroundTintList(correctColor);
                    break;
                }
            }
        }
        
        // Disable all buttons after answering - more efficient batch operation
        for (MaterialButton button : answerButtons) {
            button.setEnabled(false);
        }
        
        // Show next button after a short delay
        animationHandler.removeCallbacksAndMessages(null); // Clear any pending callbacks
        animationHandler.postDelayed(() -> {
            // Only show next button if we're not on the last question
            if (currentQuestionIndex < questions.size() - 1) {
                nextQuestionButton.setVisibility(View.VISIBLE);
            } else {
                // Auto-navigate to results on last question
                finishQuiz();
            }
        }, 1000);
    }
    
    private void showFeedbackAnimation(boolean isCorrect) {
        // Set feedback icon based on correctness
        feedbackIcon.setImageResource(isCorrect ? 
                R.drawable.ic_check_circle : R.drawable.ic_wrong_answer);
        
        // Simplified animation sequence
        feedbackIcon.setAlpha(0f);
        feedbackIcon.setScaleX(0.5f);
        feedbackIcon.setScaleY(0.5f);
        feedbackIcon.setVisibility(View.VISIBLE);
        
        // Create animator set - use fewer animators
        AnimatorSet animatorSet = new AnimatorSet();
        
        // First fade in and scale up
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(feedbackIcon, View.ALPHA, 0f, 1f);
        fadeIn.setDuration(200);
        
        ObjectAnimator scale = ObjectAnimator.ofFloat(feedbackIcon, View.SCALE_X, 0.5f, 1.0f);
        scale.setDuration(200);
        
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(feedbackIcon, View.SCALE_Y, 0.5f, 1.0f);
        scaleY.setDuration(200);
        
        // Play animations together
        animatorSet.playTogether(fadeIn, scale, scaleY);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        
        // Hide after animation completes
        animationHandler.removeCallbacksAndMessages(null); // Clear any pending callbacks
        animationHandler.postDelayed(() -> {
            feedbackIcon.setVisibility(View.INVISIBLE);
        }, 1000);
        
        animatorSet.start();
    }
    
    private void moveToNextQuestion() {
        currentQuestionIndex++;
        
        // Apply exit animation
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                displayQuestion(currentQuestionIndex);
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        questionCard.startAnimation(fadeOut);
        answersCard.startAnimation(fadeOut);
    }
    
    private void updateScore() {
        scoreTextView.setText(getString(R.string.minigame_score, score));
    }
    
    private void finishQuiz() {
        // Update game state with earned points
        RomGameState gameState = RomGameState.getInstance();
        gameState.addPuncteIntelepte(score / 10, this);
        
        // Show completion message with score
        String completionMessage = getString(R.string.minigame_complete, score);
        
        // Create a special dialog for quiz completion
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.minigame_complete_title)
               .setMessage(completionMessage)
               .setCancelable(false)
               .setPositiveButton(R.string.continue_text, (dialog, which) -> {
                   setResult(RESULT_OK);
                   finish();
               })
               .show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks to prevent leaks
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }
    }
    
    // Question class to hold question data
    private static class Question {
        String text;
        String[] options;
        String correctAnswer;
        int imageResourceId; // Optional image for the question
    }
} 