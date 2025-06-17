package com.example.myapplication.transilvaniausage;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.Transilvania;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.models.QuestionModel;
import com.example.myapplication.repository.FirestoreQuestionRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;

public class TransilvaniaGameActivity extends AppCompatActivity {
    private static final String TAG = "TransilvaniaGameActivity";
    private static final String REGION = "transilvania";
    private static final String GAME_TYPE = "quiz";
    
    private TextView questionTextView;
    private MaterialButton[] answerButtons;
    private TextView scoreTextView;
    private ProgressBar progressBar;
    private TextView timerTextView;
    private TextView streakTextView;
    private ImageView questionImage;
    private MaterialButton fiftyFiftyButton;
    private MaterialButton hintButton;
    private MaterialButton skipQuestionButton;
    private MaterialCardView[] answerCards;
    private MaterialButton finishButton;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int streak = 0;
    private int maxStreak = 0;
    private int totalQuestions = 0;
    private int correctAnswers = 0;
    private long totalTime = 0;
    private List<Question> questions;
    private List<QuestionModel> firestoreQuestions;
    private static final int POINTS_PER_CORRECT_ANSWER = 10;
    private static final int BONUS_POINTS = 50;
    private static final int TIME_PER_QUESTION = 30000; // 30 seconds
    private static final int STREAK_BONUS_THRESHOLD = 3;
    private PointsManager pointsManager;
    private CountDownTimer timer;
    private boolean isFiftyFiftyUsed = false;
    private boolean isHintUsed = false;
    private boolean isSkipUsed = false;
    private Random random = new Random();
    private FirestoreQuestionRepository questionRepository;
    private boolean isDataLoaded = false;
    private boolean useFirestore = true;

    private static class Question {
        String question;
        String[] answers;
        int correctAnswerIndex;
        int imageResourceId;
        String fact;
        String hint;

        Question(String question, String[] answers, int correctAnswerIndex, int imageResourceId, String fact, String hint) {
            this.question = question;
            this.answers = answers;
            this.correctAnswerIndex = correctAnswerIndex;
            this.imageResourceId = imageResourceId;
            this.fact = fact;
            this.hint = hint;
        }

        String getHint() {
            return hint;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply dynamic colors if available
        DynamicColors.applyToActivityIfAvailable(this);
        
        // Set theme before super.onCreate
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transilvania_game);

        initializeViews();
        pointsManager = PointsManager.getInstance(this);
        questionRepository = FirestoreQuestionRepository.getInstance();
        
        // Încărcăm întrebările din Firestore
        loadQuestionsFromFirestore();
        
        setupLifelines();
        applyButtonStyles();
        setupAccessibility();
    }
    
    private void loadQuestionsFromFirestore() {
        // Afișăm un indicator de încărcare
        progressBar.setVisibility(View.VISIBLE);
        
        questionRepository.getQuestionsAsModels(REGION, GAME_TYPE)
            .thenAccept(loadedQuestions -> {
                // Verificăm dacă avem întrebări
                if (loadedQuestions != null && !loadedQuestions.isEmpty()) {
                    firestoreQuestions = loadedQuestions;
                    useFirestore = true;
                    Log.d(TAG, "Întrebări încărcate din Firestore: " + firestoreQuestions.size());
                    
                    // Actualizăm progress bar
                    progressBar.setMax(firestoreQuestions.size());
                    progressBar.setProgress(0);
                    
                    // Amestecăm întrebările
                    Collections.shuffle(firestoreQuestions);
                    
                    // Afișăm prima întrebare
                    isDataLoaded = true;
                    displayQuestion();
                    updateScore();
                    startTimer();
                } else {
                    // Nu avem întrebări în Firestore, încercăm să migrăm
                    Log.d(TAG, "Nu există întrebări în Firestore, folosim întrebările locale");
                    migrateQuestionsToFirestore();
                }
            })
            .exceptionally(e -> {
                // Eroare la încărcarea întrebărilor
                Log.e(TAG, "Eroare la încărcarea întrebărilor din Firestore", e);
                useFirestore = false;
                isDataLoaded = true;
                displayQuestion();
                updateScore();
                startTimer();
                return null;
            });
    }
    
    private void migrateQuestionsToFirestore() {
        // Convertim întrebările locale în array pentru migrare
        Object[] questionsArray = questions.toArray();
        
        // Migrăm întrebările
        questionRepository.migrateQuestionsFromSource(questionsArray, REGION, GAME_TYPE)
            .thenRun(() -> {
                // Reîncărcăm întrebările după migrare
                loadQuestionsFromFirestore();
            })
            .exceptionally(e -> {
                // Eroare la migrare
                Log.e(TAG, "Eroare la migrarea întrebărilor", e);
                useFirestore = false;
                isDataLoaded = true;
                displayQuestion();
                updateScore();
                startTimer();
                return null;
            });
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
        hintButton = findViewById(R.id.hintButton);
        skipQuestionButton = findViewById(R.id.skipQuestionButton);
        finishButton = findViewById(R.id.finishButton);
        
        // Îmbunătățiri pentru vizibilitate și stil text
        questionTextView.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        
        // Aplicăm stiluri pentru butoane
        for (MaterialButton button : answerButtons) {
            button.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
            button.setElevation(4f);
            button.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            
            // Disable default button behavior to prevent display issues
            button.setClickable(false);
            button.setFocusable(false);
        }
        
        // Inițializare buton terminare
        finishButton.setOnClickListener(v -> finishGame());
        
        // Setup click pentru carduri
        for (int i = 0; i < answerCards.length; i++) {
            final int index = i;
            answerCards[i].setOnClickListener(v -> {
                if (v.isClickable()) {
                    checkAnswer(index, answerButtons[index].getText().toString());
                }
            });
        }
    }
    
    private void applyButtonStyles() {
        // Stilizăm butoanele pentru tema Transilvania
        for (int i = 0; i < answerButtons.length; i++) {
            MaterialButton button = answerButtons[i];
            MaterialCardView card = answerCards[i];
            
            // Activăm efectul de ripple pentru card
            card.setClickable(true);
            card.setFocusable(true);
            
            // Adaugă animație la apăsare
            card.setRippleColor(ContextCompat.getColorStateList(this, R.color.transilvania_primary_light));
            
            // Adaugăm shadow și efecte vizuale pentru butoane
            button.setElevation(4f);
            button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            
            // Adaugă efect de touch feedback
            card.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).start();
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                        break;
                }
                return false;
            });
        }
        
        // Adaugă efecte vizuale pentru butonul de finalizare
        finishButton.setRippleColor(ContextCompat.getColorStateList(this, R.color.transilvania_accent));
        finishButton.setElevation(8f);
        
        // Îmbunătățim aspectul vizual pentru butoanele de ajutor
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
    }

    /**
     * Sets up accessibility features for UI components
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
        timerTextView.setContentDescription(getString(R.string.timer_desc, timeValue));
        
        fiftyFiftyButton.setContentDescription(getString(R.string.fifty_fifty_desc));
        hintButton.setContentDescription(getString(R.string.hint_desc));
        skipQuestionButton.setContentDescription(getString(R.string.skip_question_desc));
        
        // Set content descriptions for answer buttons based on their text
        for (int i = 0; i < answerButtons.length; i++) {
            MaterialButton button = answerButtons[i];
            button.setContentDescription(getString(R.string.answer_option_desc, (i+1), button.getText()));
        }
        
        // Ensure minimum touch target size for better accessibility
        for (MaterialCardView card : answerCards) {
            card.setMinimumHeight((int) (48 * getResources().getDisplayMetrics().density));
        }
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(TIME_PER_QUESTION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                timerTextView.setText(String.valueOf(secondsRemaining));
                timerTextView.setContentDescription(getString(R.string.timer_desc, secondsRemaining));
                
                // Adăugăm efect vizual când timpul este sub 10 secunde
                if (millisUntilFinished <= 10000) {
                    Animation pulse = AnimationUtils.loadAnimation(TransilvaniaGameActivity.this, R.anim.pulse);
                    timerTextView.startAnimation(pulse);
                    timerTextView.setTextColor(ContextCompat.getColor(TransilvaniaGameActivity.this, R.color.transilvania_accent));
                } else {
                    timerTextView.setTextColor(ContextCompat.getColor(TransilvaniaGameActivity.this, R.color.transilvania_text));
                }
            }

            @Override
            public void onFinish() {
                handleTimeout();
            }
        }.start();
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

        Question currentQuestion = questions.get(currentQuestionIndex);
        List<Integer> wrongAnswers = new ArrayList<>();
        for (int i = 0; i < currentQuestion.answers.length; i++) {
            if (i != currentQuestion.correctAnswerIndex) {
                wrongAnswers.add(i);
            }
        }
        Collections.shuffle(wrongAnswers);
        
        // Dezactivăm două răspunsuri greșite cu feedback vizual îmbunătățit
        for (int i = 0; i < 2; i++) {
            int index = wrongAnswers.get(i);
            answerButtons[index].setEnabled(false);
            
            // Animație de fade-out pentru răspunsurile eliminate
            answerCards[index].animate()
                .alpha(0.5f)
                .scaleX(0.98f)
                .scaleY(0.98f)
                .setDuration(300)
                .start();
                
            answerCards[index].setClickable(false);
            answerCards[index].setStrokeColor(ContextCompat.getColor(this, R.color.transilvania_secondary));
        }

        // Marchează butonul ca utilizat
        isFiftyFiftyUsed = true;
        fiftyFiftyButton.setEnabled(false);
        
        // Animație pentru dezactivarea butonului
        fiftyFiftyButton.animate()
            .alpha(0.5f)
            .setDuration(300)
            .start();
            
        // Feedback pozitiv pentru utilizator
        Toast.makeText(this, "S-au eliminat două răspunsuri incorecte!", Toast.LENGTH_SHORT).show();
    }

    private void skipQuestion() {
        if (isSkipUsed) {
            Toast.makeText(this, "Ai folosit deja acest ajutor!", Toast.LENGTH_SHORT).show();
            return;
        }

        moveToNextQuestion();
        isSkipUsed = true;
        skipQuestionButton.setEnabled(false);
        skipQuestionButton.setAlpha(0.5f);
    }

    private void initializeQuestions() {
        questions = new ArrayList<>();
        
        // Întrebările au fost mutate în Firebase Firestore
        // Această metodă este păstrată doar pentru compatibilitate
        // și pentru cazuri în care Firebase nu este disponibil
        
        progressBar.setMax(questions.size());
        progressBar.setProgress(0);
    }
    
    /**
     * Amestecă întrebările și opțiunile de răspuns pentru fiecare întrebare
     * pentru o experiență de joc diferită de fiecare dată
     */
    private void shuffleQuestionsAndAnswers() {
        // Folosim un seed bazat pe timpul curent pentru randomizare
        Random rnd = new Random(System.currentTimeMillis());
        Collections.shuffle(questions, rnd);
        
        // Opțional: Amestecăm și variantele de răspuns pentru fiecare întrebare
        // manținând indexul răspunsului corect
        for (Question q : questions) {
            List<String> answerList = new ArrayList<>();
            for (String answer : q.answers) {
                answerList.add(answer);
            }
            
            // Salvăm răspunsul corect
            String correctAnswer = answerList.get(q.correctAnswerIndex);
            
            // Amestecăm răspunsurile
            Collections.shuffle(answerList, rnd);
            
            // Actualizăm array-ul de răspunsuri și indexul celui corect
            for (int i = 0; i < answerList.size(); i++) {
                q.answers[i] = answerList.get(i);
                if (answerList.get(i).equals(correctAnswer)) {
                    q.correctAnswerIndex = i;
                }
            }
        }
        
        // Logăm întrebarea curentă (doar pentru debugging)
        if (!questions.isEmpty()) {
            Question firstQuestion = questions.get(0);
            System.out.println("Prima întrebare: " + firstQuestion.question);
            System.out.println("Răspunsul corect: " + firstQuestion.answers[firstQuestion.correctAnswerIndex]);
        }
    }

    private void displayQuestion() {
        if (!isDataLoaded) {
            return;
        }
        
        if (currentQuestionIndex >= getQuestionsCount()) {
            finishGame();
            return;
        }
        
        // Reset card styles
        resetCardStyles();
        
        // Actualizăm progress bar
        progressBar.setProgress(currentQuestionIndex + 1);
        
        // Afișăm întrebarea curentă
        if (useFirestore && firestoreQuestions != null && !firestoreQuestions.isEmpty()) {
            // Folosim întrebările din Firestore
            QuestionModel currentQuestion = firestoreQuestions.get(currentQuestionIndex);
            questionTextView.setText(currentQuestion.getQuestion());
            
            // Obținem toate răspunsurile
            String[] allAnswers = currentQuestion.getAnswers();
            
            // Amestecăm răspunsurile
            List<String> shuffledAnswers = new ArrayList<>();
            for (String answer : allAnswers) {
                shuffledAnswers.add(answer);
            }
            Collections.shuffle(shuffledAnswers);
            
            // Setăm textul butoanelor
            for (int i = 0; i < answerButtons.length; i++) {
                if (i < shuffledAnswers.size()) {
                    answerButtons[i].setText(shuffledAnswers.get(i));
                    answerCards[i].setVisibility(View.VISIBLE);
                } else {
                    answerCards[i].setVisibility(View.GONE);
                }
            }
            
            // Setăm imaginea dacă există
            if (currentQuestion.getImageResourceId() != 0) {
                questionImage.setImageResource(currentQuestion.getImageResourceId());
                questionImage.setVisibility(View.VISIBLE);
            } else {
                questionImage.setVisibility(View.GONE);
            }
        } else {
            // Folosim întrebările locale
            Question currentQuestion = questions.get(currentQuestionIndex);
            questionTextView.setText(currentQuestion.question);
            
            // Setăm textul butoanelor
            for (int i = 0; i < answerButtons.length; i++) {
                if (i < currentQuestion.answers.length) {
                    answerButtons[i].setText(currentQuestion.answers[i]);
                    answerCards[i].setVisibility(View.VISIBLE);
                } else {
                    answerCards[i].setVisibility(View.GONE);
                }
            }
            
            // Setăm imaginea dacă există
            if (currentQuestion.imageResourceId != 0) {
                questionImage.setImageResource(currentQuestion.imageResourceId);
                questionImage.setVisibility(View.VISIBLE);
            } else {
                questionImage.setVisibility(View.GONE);
            }
        }
        
        // Activăm toate cardurile pentru răspuns
        for (MaterialCardView card : answerCards) {
            card.setClickable(true);
        }
        
        // Resetăm timerul
        if (timer != null) {
            timer.cancel();
        }
        startTimer();
    }
    
    private void resetCardStyles() {
        for (int i = 0; i < answerCards.length; i++) {
            // Reset card properties
            answerCards[i].setStrokeColor(ContextCompat.getColor(this, R.color.transilvania_primary_light));
            answerCards[i].setAlpha(1.0f);
            answerCards[i].setClickable(true);
            answerCards[i].setElevation(6f);
            answerCards[i].setTranslationZ(0f);
            answerCards[i].setScaleX(1.0f);
            answerCards[i].setScaleY(1.0f);
            
            // Reset all button properties
            answerButtons[i].setEnabled(true);
            answerButtons[i].setTextColor(ContextCompat.getColor(this, R.color.transilvania_text));
            
            // Apply consistent corner radius for Material Design 3 feel
            MaterialCardView card = answerCards[i];
            card.setRadius(getResources().getDimension(R.dimen.card_corner_radius));
            
            // Set state list animator for touch feedback
            StateListAnimator stateListAnimator = AnimatorInflater.loadStateListAnimator(
                this, android.R.animator.fade_in);
            card.setStateListAnimator(stateListAnimator);
            
            // Add subtle entrance animation with staggered delay
            card.setAlpha(0.4f);
            card.setScaleX(0.95f);
            card.setScaleY(0.95f);
            card.animate()
                .alpha(1.0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(300)
                .setStartDelay(i * 50) // staggered animation
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
        }
    }

    private void checkAnswer(int selectedAnswerIndex, String selectedAnswer) {
        // Dezactivăm cardurile pentru a preveni răspunsuri multiple
        for (MaterialCardView card : answerCards) {
            card.setClickable(false);
        }
        
        // Verificăm dacă răspunsul este corect
        boolean isCorrect = false;
        String correctAnswer = "";
        String fact = "";
        
        if (useFirestore && firestoreQuestions != null && !firestoreQuestions.isEmpty()) {
            // Folosim întrebările din Firestore
            QuestionModel currentQuestion = firestoreQuestions.get(currentQuestionIndex);
            correctAnswer = currentQuestion.getCorrectAnswer();
            fact = currentQuestion.getFact();
            isCorrect = selectedAnswer.equals(correctAnswer);
        } else {
            // Folosim întrebările locale
            Question currentQuestion = questions.get(currentQuestionIndex);
            correctAnswer = currentQuestion.answers[currentQuestion.correctAnswerIndex];
            fact = currentQuestion.fact;
            isCorrect = selectedAnswer.equals(correctAnswer);
        }
        
        // Anulăm timerul
        if (timer != null) {
            timer.cancel();
        }
        
        // Actualizăm statisticile
        totalQuestions++;
        
        // Aplicăm stilurile corespunzătoare pentru răspuns
        MaterialCardView selectedCard = answerCards[selectedAnswerIndex];
        
        if (isCorrect) {
            // Răspuns corect
            selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct_answer));
            answerButtons[selectedAnswerIndex].setTextColor(ContextCompat.getColor(this, R.color.white));
            
            // Adăugăm puncte și actualizăm streak
            score += POINTS_PER_CORRECT_ANSWER;
            streak++;
            if (streak > maxStreak) {
                maxStreak = streak;
            }
            
            // Bonus pentru streak
            if (streak >= STREAK_BONUS_THRESHOLD) {
                score += BONUS_POINTS;
                showStreakBonus();
            }
            
            correctAnswers++;
            
            // Actualizăm scorul și streak-ul
            updateScore();
            updateStreak();
            
            // Animație pentru răspuns corect
            Animation pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse);
            selectedCard.startAnimation(pulseAnimation);
            
            // Afișăm informația suplimentară
            if (fact != null && !fact.isEmpty()) {
                new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.correct_answer)
                    .setMessage(fact)
                    .setPositiveButton(R.string.next_question, (dialog, which) -> {
                        moveToNextQuestion();
                    })
                    .setCancelable(false)
                    .show();
            } else {
                // Trecem la următoarea întrebare după o scurtă pauză
                new Handler().postDelayed(() -> moveToNextQuestion(), 1500);
            }
        } else {
            // Răspuns greșit
            selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.wrong_answer));
            answerButtons[selectedAnswerIndex].setTextColor(ContextCompat.getColor(this, R.color.white));
            
            // Resetăm streak-ul
            streak = 0;
            updateStreak();
            
            // Găsim și evidențiem răspunsul corect
            for (int i = 0; i < answerButtons.length; i++) {
                if (answerButtons[i].getText().toString().equals(correctAnswer)) {
                    answerCards[i].setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct_answer));
                    answerButtons[i].setTextColor(ContextCompat.getColor(this, R.color.white));
                    break;
                }
            }
            
            // Animație pentru răspuns greșit
            Animation shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
            selectedCard.startAnimation(shakeAnimation);
            
            // Afișăm informația suplimentară
            if (fact != null && !fact.isEmpty()) {
                new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.wrong_answer)
                    .setMessage(fact)
                    .setPositiveButton(R.string.next_question, (dialog, which) -> {
                        moveToNextQuestion();
                    })
                    .setCancelable(false)
                    .show();
            } else {
                // Trecem la următoarea întrebare după o scurtă pauză
                new Handler().postDelayed(() -> moveToNextQuestion(), 1500);
            }
        }
    }

    private void moveToNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < getQuestionsCount()) {
            resetCardStyles();
            displayQuestion();
        } else {
            showFinishButton();
        }
    }

    private void updateScore() {
        scoreTextView.setText(String.valueOf(score));
        progressBar.setProgress(Math.min(100, (currentQuestionIndex * 100) / getQuestionsCount()));
    }

    private void updateStreak() {
        streakTextView.setText(String.valueOf(streak));
    }

    private String getAchievements() {
        List<String> achievements = new ArrayList<>();
        
        if (maxStreak >= 5) {
            achievements.add("Geniu Transilvănean (serie de " + maxStreak + " răspunsuri corecte)");
        } else if (maxStreak >= 3) {
            achievements.add("Cunoscător al Transilvaniei (serie de " + maxStreak + " răspunsuri corecte)");
        }
        
        if (correctAnswers == getQuestionsCount()) {
            achievements.add("Perfect! Toate răspunsurile corecte");
        } else if (correctAnswers >= getQuestionsCount() * 0.8) {
            achievements.add("Expert al Transilvaniei (" + correctAnswers + " din " + getQuestionsCount() + " corecte)");
        } else if (correctAnswers >= getQuestionsCount() * 0.5) {
            achievements.add("Bun cunoscător (" + correctAnswers + " din " + getQuestionsCount() + " corecte)");
        }
        
        if (achievements.isEmpty()) {
            return "Nicio realizare specială. Poți face mai bine data viitoare!";
        }
        
        StringBuilder result = new StringBuilder("Realizări:");
        for (String achievement : achievements) {
            result.append("\n• ").append(achievement);
        }
        
        return result.toString();
    }

    private void finishGame() {
        if (timer != null) {
            timer.cancel();
        }

        // Adăugăm punctele în contul utilizatorului
        pointsManager.addPoints(this, "transilvania", score);
        
        // Construim un dialog pentru a afișa rezultatele și opțiunile
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("Joc terminat!");
        dialogBuilder.setMessage(
            "Scor final: " + score + " puncte\n" +
            "Răspunsuri corecte: " + correctAnswers + " din " + getQuestionsCount() + "\n" +
            "Serie maximă: " + maxStreak + "\n\n" +
            getAchievements()
        );
        
        // Opțiune pentru a juca din nou
        dialogBuilder.setPositiveButton("Joacă din nou", (dialog, which) -> {
            // Resetăm toate valorile și începem un joc nou
            currentQuestionIndex = 0;
            score = 0;
            streak = 0;
            maxStreak = 0;
            correctAnswers = 0;
            
            // Reinițializăm întrebările pentru o nouă ordine aleatorie
            initializeQuestions();
            
            // Resetăm UI-ul
            displayQuestion();
            updateScore();
            updateStreak();
            startTimer();
            
            dialog.dismiss();
        });
        
        // Opțiune pentru a vedea rezultatele detaliate
        dialogBuilder.setNeutralButton("Vezi rezultate", (dialog, which) -> {
            Intent intent = new Intent(this, GameOverActivity.class);
            intent.putExtra("score", score);
            intent.putExtra("correctAnswers", correctAnswers);
            intent.putExtra("totalQuestions", getQuestionsCount());
            intent.putExtra("maxStreak", maxStreak);
            intent.putExtra("ACHIEVEMENTS", getAchievements());
            startActivity(intent);
            finish();
        });
        
        // Opțiune pentru a ieși
        dialogBuilder.setNegativeButton("Ieșire", (dialog, which) -> {
            finish();
        });
        
        dialogBuilder.setCancelable(false);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    // New method to show finish button at the end
    private void showFinishButton() {
        // Hide all answer cards to avoid overlap
        for (MaterialCardView card : answerCards) {
            card.setVisibility(View.GONE);
        }
        
        // Set content description for accessibility
        finishButton.setContentDescription(getString(R.string.finish_game_desc));
        
        // Show a stylized completion message
        questionTextView.setText("Quiz complet! Felicitări!");
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeIn.setDuration(700);
        questionTextView.startAnimation(fadeIn);
        
        // Update the progress bar to show completion
        progressBar.setProgress(getQuestionsCount());
        progressBar.setContentDescription(getString(R.string.progress_desc, getQuestionsCount(), getQuestionsCount()));
        
        // Animate the progress bar
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
        
        // Show finish button with enhanced animations
        finishButton.setVisibility(View.VISIBLE);
        Animation springOvershoot = AnimationUtils.loadAnimation(this, R.anim.spring_overshoot);
        finishButton.startAnimation(springOvershoot);
        
        // Add pulse animation after scale in
        new Handler().postDelayed(() -> {
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            pulse.setRepeatCount(Animation.INFINITE);
            pulse.setRepeatMode(Animation.REVERSE);
            finishButton.startAnimation(pulse);
            
            // Add subtle elevation animation for Material Design depth effect
            float originalElevation = finishButton.getElevation();
            ValueAnimator elevationAnimator = ValueAnimator.ofFloat(originalElevation, originalElevation + 6f, originalElevation);
            elevationAnimator.setDuration(1500);
            elevationAnimator.setRepeatCount(ValueAnimator.INFINITE);
            elevationAnimator.setRepeatMode(ValueAnimator.REVERSE);
            elevationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            elevationAnimator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                finishButton.setElevation(value);
            });
            elevationAnimator.start();
            
        }, 700);
        
        // Display the score with a celebratory animation
        String scoreMessage = "Scor final: " + score + " puncte";
        TextView scoreView = new TextView(this);
        scoreView.setText(scoreMessage);
        scoreView.setTextSize(20);
        scoreView.setTextColor(ContextCompat.getColor(this, R.color.transilvania_primary));
        scoreView.setTypeface(Typeface.DEFAULT_BOLD);
        scoreView.setGravity(Gravity.CENTER);
        
        // Add the score view below the question text
        ConstraintLayout layout = findViewById(R.id.main_constraint_layout);
        ConstraintSet constraintSet = new ConstraintSet();
        scoreView.setId(View.generateViewId());
        layout.addView(scoreView);
        
        constraintSet.clone(layout);
        constraintSet.connect(scoreView.getId(), ConstraintSet.TOP, questionTextView.getId(), ConstraintSet.BOTTOM, 24);
        constraintSet.connect(scoreView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(scoreView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.applyTo(layout);
        
        // Animate the score view
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
        
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * Shows a hint for the current question
     */
    private void showHint() {
        if (isHintUsed) {
            Toast.makeText(this, R.string.hint_already_used, Toast.LENGTH_SHORT).show();
            return;
        }
        
        String hint = "";
        if (useFirestore && firestoreQuestions != null && !firestoreQuestions.isEmpty()) {
            // Pentru întrebările din Firestore
            QuestionModel currentQuestion = firestoreQuestions.get(currentQuestionIndex);
            hint = currentQuestion.getFact(); // Folosim fact ca hint pentru întrebările din Firestore
        } else {
            // Pentru întrebările locale
            Question currentQuestion = questions.get(currentQuestionIndex);
            hint = currentQuestion.getHint();
        }
        
        if (hint == null || hint.isEmpty()) {
            hint = getString(R.string.no_hint_available);
        }
        
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.hint)
            .setMessage(hint)
            .setPositiveButton(R.string.ok, null)
            .show();
        
        isHintUsed = true;
        hintButton.setEnabled(false);
        hintButton.setAlpha(0.5f);
    }

    /**
     * Loads the next question in the quiz
     */
    private void loadNextQuestion() {
        // Reset state for new question
        isFiftyFiftyUsed = false;
        isHintUsed = false;
        isSkipUsed = false;
        
        // Re-enable lifeline buttons
        fiftyFiftyButton.setEnabled(true);
        fiftyFiftyButton.setAlpha(1.0f);
        hintButton.setEnabled(true);
        hintButton.setAlpha(1.0f);
        skipQuestionButton.setEnabled(true);
        skipQuestionButton.setAlpha(1.0f);
        
        // Enable all answer buttons
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i].setEnabled(true);
            answerCards[i].setEnabled(true);
            answerCards[i].setClickable(true);
            answerCards[i].setAlpha(1.0f);
            answerCards[i].setScaleX(1.0f);
            answerCards[i].setScaleY(1.0f);
            answerCards[i].setStrokeColor(ContextCompat.getColor(this, R.color.transilvania_primary_light));
            answerCards[i].setStrokeWidth(2);
            answerButtons[i].setTextColor(ContextCompat.getColor(this, R.color.transilvania_text));
        }
        
        // Display the next question
        displayQuestion();
        
        // Start the timer for the new question
        startTimer();
    }

    private int getQuestionsCount() {
        if (useFirestore && firestoreQuestions != null) {
            return firestoreQuestions.size();
        } else {
            return questions.size();
        }
    }

    private void showStreakBonus() {
        Toast.makeText(this, "Bonus serie: +" + BONUS_POINTS + " puncte!", Toast.LENGTH_SHORT).show();
        Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        streakTextView.startAnimation(bounceAnim);
    }
} 