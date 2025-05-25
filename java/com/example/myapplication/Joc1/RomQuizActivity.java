package com.example.myapplication.Joc1;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myapplication.R;
import com.example.myapplication.RomApplication;
import com.example.myapplication.security.SecurityManager;
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
    private TextView questionTextView;
    private Button[] answerButtons;
    private TextView scoreTextView;
    private ImageView questionImageView;
    private CardView questionCard;
    
    // Game variables
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean answered = false;
    private int consecutiveCorrect = 0;
    
    // Security manager
    private SecurityManager securityManager;
    
    // Animation handler
    private Handler animationHandler = new Handler(Looper.getMainLooper());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_quiz);
        
        try {
            // Get security manager
            securityManager = ((RomApplication) getApplication()).getSecurityManager();
            
            // Initialize UI elements
            initializeViews();
            
            // Load questions
            loadQuestions();
            
            // Display first question
            displayQuestion(currentQuestionIndex);
            
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
    
    private void initializeViews() {
        questionTextView = findViewById(R.id.questionTextView);
        questionImageView = findViewById(R.id.questionImageView);
        questionCard = findViewById(R.id.questionCard);
        scoreTextView = findViewById(R.id.scoreTextView);
        
        // Initialize answer buttons
        answerButtons = new Button[4];
        answerButtons[0] = findViewById(R.id.answerButton1);
        answerButtons[1] = findViewById(R.id.answerButton2);
        answerButtons[2] = findViewById(R.id.answerButton3);
        answerButtons[3] = findViewById(R.id.answerButton4);
        
        // Set click listeners for answer buttons
        for (int i = 0; i < answerButtons.length; i++) {
            final int buttonIndex = i;
            answerButtons[i].setOnClickListener(v -> checkAnswer(buttonIndex));
        }
        
        // Set initial score
        updateScore();
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
            
            // Apply entrance animation
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            questionCard.startAnimation(fadeIn);
            
            // Set question text
            questionTextView.setText(currentQuestion.text);
            
            // Set answer options
            for (int i = 0; i < answerButtons.length; i++) {
                if (i < currentQuestion.options.length) {
                    answerButtons[i].setText(currentQuestion.options[i]);
                    answerButtons[i].setVisibility(View.VISIBLE);
                    answerButtons[i].setEnabled(true);
                    answerButtons[i].setBackgroundResource(R.drawable.rom_button_background);
                } else {
                    answerButtons[i].setVisibility(View.GONE);
                }
            }
            
            // Reset answered state
            answered = false;
            
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
            answerButtons[buttonIndex].setBackgroundResource(R.drawable.correct_answer_background);
            score += 10;
            consecutiveCorrect++;
            
            // Bonus for streak
            if (consecutiveCorrect >= 3) {
                score += 5;
                Snackbar.make(findViewById(android.R.id.content),
                        "Răspunsuri consecutive! +5 bonus!", Snackbar.LENGTH_SHORT).show();
            }
            
            updateScore();
            
            // Show success message
            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.minigame_correct), Snackbar.LENGTH_SHORT).show();
        } else {
            // Wrong answer
            answerButtons[buttonIndex].setBackgroundResource(R.drawable.wrong_answer_background);
            consecutiveCorrect = 0;
            
            // Highlight correct answer
            for (int i = 0; i < answerButtons.length; i++) {
                if (answerButtons[i].getText().toString().equals(correctAnswer)) {
                    answerButtons[i].setBackgroundResource(R.drawable.correct_answer_background);
                    break;
                }
            }
            
            // Show failure message
            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.minigame_wrong), Snackbar.LENGTH_SHORT).show();
        }
        
        // Disable all buttons after answering
        for (Button button : answerButtons) {
            button.setEnabled(false);
        }
        
        // Delay before next question
        animationHandler.postDelayed(() -> moveToNextQuestion(), 1500);
    }
    
    private void moveToNextQuestion() {
        currentQuestionIndex++;
        
        // Apply exit animation
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
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
    }
    
    private void updateScore() {
        scoreTextView.setText(getString(R.string.minigame_score, score));
    }
    
    private void finishQuiz() {
        // Update game state with earned points
        RomGameState gameState = RomGameState.getInstance();
        gameState.addPuncteIntelepte(score / 10, this);
        
        // Show completion message
        Snackbar.make(findViewById(android.R.id.content),
                "Quiz terminat! Punctaj final: " + score, Snackbar.LENGTH_LONG).show();
        
        // Delay before finishing activity
        animationHandler.postDelayed(() -> {
            setResult(RESULT_OK);
            finish();
        }, 2000);
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
    }
    
    @Override
    public void onBackPressed() {
        // Show confirmation dialog if quiz is not completed
        if (currentQuestionIndex < questions.size() - 1) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Ieșire")
                   .setMessage("Ești sigur că vrei să părăsești quiz-ul? Progresul va fi pierdut.")
                   .setPositiveButton("Da", (dialog, which) -> {
                       super.onBackPressed();
                   })
                   .setNegativeButton("Nu", (dialog, which) -> {
                       dialog.dismiss();
                   })
                   .show();
        } else {
            super.onBackPressed();
        }
    }
} 