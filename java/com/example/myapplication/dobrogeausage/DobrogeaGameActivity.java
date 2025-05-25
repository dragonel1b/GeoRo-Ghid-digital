package com.example.myapplication.dobrogeausage;

import android.content.Intent;
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
import com.example.myapplication.R;
import com.example.myapplication.RomApp.Dobrogea;
import com.example.myapplication.RomApp.PointsManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DobrogeaGameActivity extends AppCompatActivity {
    private TextView questionTextView;
    private Button[] answerButtons;
    private TextView scoreTextView;
    private ProgressBar progressBar;
    private TextView timerTextView;
    private TextView streakTextView;
    private ImageView questionImage;
    private ImageButton fiftyFiftyButton;
    private ImageButton skipQuestionButton;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int streak = 0;
    private int maxStreak = 0;
    private int totalQuestions = 0;
    private int correctAnswers = 0;
    private long totalTime = 0;
    private List<Question> questions;
    private static final int POINTS_PER_CORRECT_ANSWER = 10;
    private static final int BONUS_POINTS = 50;
    private static final int TIME_PER_QUESTION = 30000; // 30 seconds
    private static final int STREAK_BONUS_THRESHOLD = 3;
    private PointsManager pointsManager;
    private CountDownTimer timer;
    private boolean isFiftyFiftyUsed = false;
    private boolean isSkipUsed = false;
    private Random random = new Random();

    private static class Question {
        String question;
        String[] answers;
        int correctAnswerIndex;
        int imageResourceId;
        String fact;

        Question(String question, String[] answers, int correctAnswerIndex, int imageResourceId, String fact) {
            this.question = question;
            this.answers = answers;
            this.correctAnswerIndex = correctAnswerIndex;
            this.imageResourceId = imageResourceId;
            this.fact = fact;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set theme before super.onCreate
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dobrogea_game);

        initializeViews();
        pointsManager = PointsManager.getInstance(this);
        initializeQuestions();
        setupLifelines();
        displayQuestion();
        updateScore();
        startTimer();
    }

    private void initializeViews() {
        questionTextView = findViewById(R.id.questionTextView);
        answerButtons = new Button[]{
            findViewById(R.id.answerButton1),
            findViewById(R.id.answerButton2),
            findViewById(R.id.answerButton3),
            findViewById(R.id.answerButton4)
        };
        scoreTextView = findViewById(R.id.scoreTextView);
        progressBar = findViewById(R.id.progressBar);
        timerTextView = findViewById(R.id.timerTextView);
        streakTextView = findViewById(R.id.streakTextView);
        questionImage = findViewById(R.id.questionImage);
        fiftyFiftyButton = findViewById(R.id.fiftyFiftyButton);
        skipQuestionButton = findViewById(R.id.skipQuestionButton);
        
        // Initialize finish button
        Button finishButton = findViewById(R.id.finishButton);
        finishButton.setOnClickListener(v -> finishGame());
    }

    private void setupLifelines() {
        fiftyFiftyButton.setOnClickListener(v -> useFiftyFifty());
        skipQuestionButton.setOnClickListener(v -> skipQuestion());
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(TIME_PER_QUESTION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTextView.setText(String.valueOf(millisUntilFinished / 1000));
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
        
        // Disable two wrong answers
        for (int i = 0; i < 2; i++) {
            int index = wrongAnswers.get(i);
            answerButtons[index].setEnabled(false);
            answerButtons[index].setAlpha(0.5f);
        }

        isFiftyFiftyUsed = true;
        fiftyFiftyButton.setEnabled(false);
        fiftyFiftyButton.setAlpha(0.5f);
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
        
        // Întrebări despre Dobrogea
        questions.add(new Question(
            "Care este cel mai vechi oraș atestat din Dobrogea?",
            new String[]{"Constanța", "Histria", "Mangalia", "Tulcea"},
            1,
            R.drawable.histria,
            "Histria a fost fondată în secolul al VII-lea î.Hr. de către coloniștii greci."
        ));
        questions.add(new Question(
            "Care este cel mai mare port din România?",
            new String[]{"Tulcea", "Mangalia", "Constanța", "Sulina"},
            2,
            R.drawable.mangalia,
            "Mangalia este cel mai mare port din România."
        ));
        questions.add(new Question(
            "Care este cea mai mare stațiune din Dobrogea?",
            new String[]{"Eforie", "Mamaia", "Neptun", "Mangalia"},
            1,
            R.drawable.mamaia,
            "Mamaia este cea mai mare stațiune din Dobrogea."
        ));

        // Întrebări despre Delta Dunării
        questions.add(new Question(
            "Câte brațe principale are Delta Dunării?",
            new String[]{"2", "3", "4", "5"},
            1,
            R.drawable.delta_dunarii,
            "Delta Dunării are 4 brațe principale."
        ));
        questions.add(new Question(
            "Care este cel mai lung braț al Deltei Dunării?",
            new String[]{"Sulina", "Sfântu Gheorghe", "Chilia", "Toate au aceeași lungime"},
            2,
            R.drawable.sulina,
            "Sulina este cel mai lung braț al Deltei Dunării."
        ));
        questions.add(new Question(
            "Ce tip de relief predomină în Delta Dunării?",
            new String[]{"Munți", "Câmpii", "Zone mlăștinoase", "Dealuri"},
            2,
            R.drawable.delta_dunarii_relief,
            "Câmpiile sunt predominantele caracteristici ale Deltei Dunării."
        ));
        questions.add(new Question(
            "Care dintre următoarele nu este o zonă protejată din Delta Dunării?",
            new String[]{"Pădurea Letea", "Pădurea Caraorman", "Muntele Măcin", "Insula Popina"},
            2,
            R.drawable.insula_popina,
            "Insula Popina este o zonă protejată din Delta Dunării."
        ));
        questions.add(new Question(
            "Câte specii de păsări trăiesc în Delta Dunării?",
            new String[]{"100+", "200+", "300+", "400+"},
            2,
            R.drawable.delta_dunarii_pasari,
            "În Delta Dunării trăiesc peste 300 de specii de păsări."
        ));
        questions.add(new Question(
            "Care este cea mai mare specie de pelican din Delta Dunării?",
            new String[]{"Pelicanul creț", "Pelicanul comun", "Pelicanul roz", "Nu există pelicani în Delta"},
            0,
            R.drawable.pelican,
            "Pelicanul creț este cea mai mare specie de pelican din Delta Dunării."
        ));
        questions.add(new Question(
            "Ce tip de vegetație este caracteristică Deltei Dunării?",
            new String[]{"Stepa", "Pădurea de stuf", "Pădurea de stejar", "Pădurea de pin"},
            1,
            R.drawable.delta_dunarii_vegetatie,
            "Pădurea de stuf este predominantă în Delta Dunării."
        ));

        Collections.shuffle(questions);
        progressBar.setMax(questions.size());
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question currentQuestion = questions.get(currentQuestionIndex);
            
            // Animate question card
            Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            
            questionTextView.startAnimation(fadeOut);
            questionImage.startAnimation(fadeOut);
            
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    questionTextView.setText(currentQuestion.question);
                    questionImage.setImageResource(currentQuestion.imageResourceId);
                    questionTextView.startAnimation(fadeIn);
                    questionImage.startAnimation(fadeIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            List<String> answers = new ArrayList<>();
            for (String answer : currentQuestion.answers) {
                answers.add(answer);
            }
            Collections.shuffle(answers);

            for (int i = 0; i < answerButtons.length; i++) {
                answerButtons[i].setText(answers.get(i));
                answerButtons[i].setEnabled(true);
                answerButtons[i].setAlpha(1.0f);
                final int buttonIndex = i;
                answerButtons[i].setOnClickListener(v -> checkAnswer(buttonIndex, answers.get(buttonIndex)));
            }

            progressBar.setProgress(currentQuestionIndex + 1);
            startTimer();
        } else {
            finishGame();
        }
    }

    private void checkAnswer(int selectedAnswerIndex, String selectedAnswer) {
        timer.cancel();
        Question currentQuestion = questions.get(currentQuestionIndex);
        boolean isCorrect = selectedAnswer.equals(currentQuestion.answers[currentQuestion.correctAnswerIndex]);
        
        // Update statistics
        totalQuestions++;
        long timeSpent = TIME_PER_QUESTION - Long.parseLong(timerTextView.getText().toString()) * 1000;
        totalTime += timeSpent;

        if (isCorrect) {
            correctAnswers++;
            score += POINTS_PER_CORRECT_ANSWER;
            streak++;
            maxStreak = Math.max(maxStreak, streak);
            pointsManager.addPoints(this, "dobrogea", POINTS_PER_CORRECT_ANSWER);
            
            if (streak >= STREAK_BONUS_THRESHOLD) {
                int bonusPoints = streak * 5;
                score += bonusPoints;
                pointsManager.addPoints(this, "dobrogea", bonusPoints);
                Toast.makeText(this, "Streak bonus! +" + bonusPoints + " puncte", Toast.LENGTH_SHORT).show();
            }
        } else {
            streak = 0;
        }

        updateScore();
        updateStreak();
        
        // Show fact about the answer
        Toast.makeText(this, currentQuestion.fact, Toast.LENGTH_LONG).show();
        
        moveToNextQuestion();
    }

    private void moveToNextQuestion() {
        currentQuestionIndex++;
        displayQuestion();
    }

    private void updateScore() {
        scoreTextView.setText("Scor: " + score);
    }

    private void updateStreak() {
        streakTextView.setText("🔥 Streak: " + streak);
    }

    private String getAchievements() {
        StringBuilder achievements = new StringBuilder();
        float accuracy = totalQuestions > 0 ? (correctAnswers * 100.0f) / totalQuestions : 0;
        
        if (maxStreak >= 5) achievements.append("Streak Master! ");
        if (accuracy >= 80) achievements.append("Accuracy Expert! ");
        if (score >= 1000) achievements.append("High Scorer! ");
        if (achievements.length() == 0) achievements.append("Keep practicing!");
        return achievements.toString();
    }

    private void finishGame() {
        if (timer != null) {
            timer.cancel();
        }

        float accuracy = (float) correctAnswers / totalQuestions * 100;
        float averageTime = (float) totalTime / totalQuestions;

        Intent intent = new Intent(this, com.example.myapplication.dobrogeausage.GameOverActivity.class);
        intent.putExtra("finalScore", score);
        intent.putExtra("longestStreak", maxStreak);
        intent.putExtra("averageTime", averageTime);
        intent.putExtra("accuracy", accuracy);
        intent.putExtra("achievements", getAchievements());
        intent.putExtra("GAME_SCORE", score);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
} 