package com.example.myapplication.core.domain.model;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.core.domain.model.Question;
import com.example.myapplication.core.domain.model.QuestionModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Clasă de bază pentru toate activitățile de joc din diferite regiuni
 * Implementează funcționalitatea comună pentru jocuri de tip quiz
 */
public abstract class BaseGameActivity extends AppCompatActivity {
    
    // Constante
    protected static final int POINTS_PER_CORRECT_ANSWER = 10;
    protected static final int TIME_PER_QUESTION = 30000; // 30 secunde
    
    // Variabile pentru UI
    protected TextView questionText;
    protected TextView questionCounterText;
    protected TextView timerText;
    protected ProgressBar progressBar;
    protected CardView[] answerCards;
    protected Button[] answerButtons;
    protected Button skipButton;
    protected ImageView questionImage;
    protected TextView streakTextView;
    protected TextView factTextView;
    protected Button finishButton;
    
    // Variabile pentru logică
    protected int currentQuestionIndex = 0;
    protected int correctAnswers = 0;
    protected int totalQuestions = 0;
    protected int timeRemaining = 0;
    protected CountDownTimer timer;
    protected boolean answerSelected = false;
    protected List<Question> questions;
    protected List<QuestionModel> questionModels;
    protected String region;
    protected int score = 0;
    protected int currentStreak = 0;
    protected int maxStreak = 0;
    protected long timeLeftInMillis = 20000; // 20 seconds per question
    protected PointsManager pointsManager;
    
    // Helpers
    protected boolean isFiftyFiftyUsed = false;
    protected boolean isSkipUsed = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Aplicăm tema în funcție de modul de afișare
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }
        
        super.onCreate(savedInstanceState);
        
        // Inițializăm managerul de puncte
        pointsManager = PointsManager.getInstance(this);
    }
    
    /**
     * Inițializează elementele comune de UI
     */
    protected void initializeCommonViews() {
        questionText = findViewById(R.id.questionText);
        questionCounterText = findViewById(R.id.questionCounter);
        timerText = findViewById(R.id.timerText);
        progressBar = findViewById(R.id.progressBar);
        skipButton = findViewById(R.id.skipQuestionButton);
        questionImage = findViewById(R.id.questionImage);
        streakTextView = findViewById(R.id.streakTextView);
        factTextView = findViewById(R.id.factTextView);
        finishButton = findViewById(R.id.finishButton);
        
        if (finishButton != null) {
            finishButton.setOnClickListener(v -> finishGame());
            finishButton.setVisibility(View.GONE);
        }
        
        // Inițializarea cardurilor și butoanelor pentru răspunsuri trebuie făcută în subclase
        // deoarece ID-urile pot varia
    }
    
    /**
     * Inițializează întrebările pentru quiz
     * Trebuie suprascrisă în subclase pentru a adăuga întrebările specifice regiunii
     */
    protected abstract void initializeQuestions();
    
    /**
     * Afișează întrebarea curentă
     */
    protected void displayQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question question = questions.get(currentQuestionIndex);
            questionText.setText(question.getQuestion());
            questionCounterText.setText(getString(R.string.question_counter, currentQuestionIndex + 1, questions.size()));
            
            // Afișarea imaginii dacă există
            if (question.getImageResourceId() != 0) {
                questionImage.setVisibility(View.VISIBLE);
                questionImage.setImageResource(question.getImageResourceId());
            } else {
                questionImage.setVisibility(View.GONE);
            }
            
            // Afișarea opțiunilor
            List<String> options = question.getOptions();
            for (int i = 0; i < answerButtons.length && i < options.size(); i++) {
                answerButtons[i].setText(options.get(i));
                answerCards[i].setVisibility(View.VISIBLE);
            }
            
            // Ascunderea opțiunilor neutilizate
            for (int i = options.size(); i < answerButtons.length; i++) {
                answerCards[i].setVisibility(View.GONE);
            }
            
            // Actualizarea progress bar-ului
            progressBar.setMax(questions.size());
            progressBar.setProgress(currentQuestionIndex + 1);
            
            // Resetarea stării
            answerSelected = false;
            
            // Pornirea timerului
            startTimer();
        } else {
            // Toate întrebările au fost parcurse, terminăm jocul
            finishGame();
        }
    }
    
    /**
     * Pornește timerul pentru întrebarea curentă
     */
    protected void startTimer() {
        // Anulăm timerul anterior dacă există
        if (timer != null) {
            timer.cancel();
        }
        
        // Setăm timpul pentru întrebare (30 secunde)
        timeRemaining = 30;
        
        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = (int) (millisUntilFinished / 1000);
                timerText.setText(String.valueOf(timeRemaining));
            }
            
            @Override
            public void onFinish() {
                if (!answerSelected) {
                    // Timpul a expirat, trecem la următoarea întrebare
                    handleTimeOut();
                }
            }
        }.start();
    }
    
    /**
     * Amestecă întrebările pentru a le afișa în ordine aleatorie
     */
    protected void shuffleQuestions() {
        if (questions != null && !questions.isEmpty()) {
            Collections.shuffle(questions);
            totalQuestions = questions.size();
        }
    }
    
    /**
     * Gestionează evenimentul când timpul expiră
     */
    protected void handleTimeOut() {
        // Marcăm răspunsul corect
        Question currentQuestion = questions.get(currentQuestionIndex);
        int correctIndex = currentQuestion.getCorrectAnswerIndex();
        
        // Evidențiem răspunsul corect
        highlightCorrectAnswer(correctIndex);
        
        // Trecem la următoarea întrebare după o scurtă pauză
        new CountDownTimer(2000, 2000) {
            @Override
            public void onTick(long millisUntilFinished) {}
            
            @Override
            public void onFinish() {
                moveToNextQuestion();
            }
        }.start();
    }
    
    /**
     * Verifică răspunsul selectat
     */
    protected void checkAnswer(int selectedIndex) {
        if (answerSelected) return;
        
        answerSelected = true;
        timer.cancel();
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        boolean isCorrect = selectedIndex == currentQuestion.getCorrectAnswerIndex();
        
        if (isCorrect) {
            correctAnswers++;
            // Animație pentru răspuns corect
            animateCorrectAnswer(selectedIndex);
        } else {
            // Animație pentru răspuns greșit
            animateWrongAnswer(selectedIndex, currentQuestion.getCorrectAnswerIndex());
        }
        
        // Trecem la următoarea întrebare după o scurtă pauză
        new CountDownTimer(2000, 2000) {
            @Override
            public void onTick(long millisUntilFinished) {}
            
            @Override
            public void onFinish() {
                moveToNextQuestion();
            }
        }.start();
    }
    
    /**
     * Animație pentru răspuns corect
     */
    protected void animateCorrectAnswer(int index) {
        // Animație de pulsație pentru cardul cu răspunsul corect
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(answerCards[index], "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(answerCards[index], "scaleY", 1f, 1.1f, 1f);
        scaleX.setDuration(500);
        scaleY.setDuration(500);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleX.start();
        scaleY.start();
        
        // Schimbăm culoarea cardului
        answerCards[index].setCardBackgroundColor(getResources().getColor(R.color.correct_answer));
    }
    
    /**
     * Animație pentru răspuns greșit
     */
    protected void animateWrongAnswer(int selectedIndex, int correctIndex) {
        // Animație de tremur pentru cardul cu răspunsul greșit
        ObjectAnimator shake = ObjectAnimator.ofFloat(answerCards[selectedIndex], "translationX", 0, 10, -10, 10, -10, 5, -5, 0);
        shake.setDuration(700);
        shake.start();
        
        // Schimbăm culoarea cardurilor
        answerCards[selectedIndex].setCardBackgroundColor(getResources().getColor(R.color.wrong_answer));
        highlightCorrectAnswer(correctIndex);
    }
    
    /**
     * Evidențiază răspunsul corect
     */
    protected void highlightCorrectAnswer(int correctIndex) {
        answerCards[correctIndex].setCardBackgroundColor(getResources().getColor(R.color.correct_answer));
    }
    
    /**
     * Trece la următoarea întrebare
     */
    protected void moveToNextQuestion() {
        // Resetăm culorile cardurilor
        for (CardView card : answerCards) {
            card.setCardBackgroundColor(getResources().getColor(R.color.card_background));
        }
        
        currentQuestionIndex++;
        displayQuestion();
    }
    
    /**
     * Actualizează scorul în funcție de răspunsurile corecte și streak
     */
    protected void updateScore() {
        // Adăugăm puncte pentru răspunsul corect
        score += POINTS_PER_CORRECT_ANSWER;
        
        // Bonus pentru streak
        if (currentStreak >= 3) {
            score += (currentStreak - 2) * 5;
        }
    }
    
    /**
     * Afișează butonul de finalizare a jocului
     */
    protected void showFinishButton() {
        if (finishButton != null) {
            finishButton.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Finalizează jocul și afișează rezultatele
     * Trebuie suprascrisă în subclase pentru a implementa comportamentul specific
     */
    protected abstract void finishGame();
    
    /**
     * Salvează punctele acumulate în managerul de puncte
     */
    protected void savePoints() {
        if (pointsManager != null) {
            pointsManager.addPoints(this, region.toLowerCase(), score);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (!answerSelected && currentQuestionIndex < questions.size()) {
            startTimer();
        }
    }
    
    /**
     * Actualizează afișarea streak-ului
     */
    protected void updateStreak() {
        if (streakTextView != null) {
            streakTextView.setText(String.format("Streak: %d", currentStreak));
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
} 