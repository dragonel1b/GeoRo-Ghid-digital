package com.example.myapplication.Joc1;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import java.util.Random;

public class MinigameRomaniaActivity extends AppCompatActivity {
    private TextView questionText;
    private TextView scoreText;
    private MaterialCardView[] optionCards;
    private TextView[] optionTexts;
    private int currentScore = 0;
    private int currentQuestionIndex = 0;
    private Random random = new Random();
    private RomGameState gameState;
    private Animation shakeAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minigame_romania);

        gameState = RomGameState.getInstance();
        gameState.initialize(this);

        initializeViews();
        setupToolbar();
        loadQuestion();
    }

    private void initializeViews() {
        questionText = findViewById(R.id.questionText);
        scoreText = findViewById(R.id.scoreText);
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);

        // Initialize option cards and texts
        optionCards = new MaterialCardView[4];
        optionTexts = new TextView[4];

        optionCards[0] = findViewById(R.id.option1Card);
        optionCards[1] = findViewById(R.id.option2Card);
        optionCards[2] = findViewById(R.id.option3Card);
        optionCards[3] = findViewById(R.id.option4Card);

        optionTexts[0] = findViewById(R.id.option1Text);
        optionTexts[1] = findViewById(R.id.option2Text);
        optionTexts[2] = findViewById(R.id.option3Text);
        optionTexts[3] = findViewById(R.id.option4Text);

        // Set click listeners for options
        for (int i = 0; i < optionCards.length; i++) {
            final int optionIndex = i;
            optionCards[i].setOnClickListener(v -> checkAnswer(optionIndex));
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void loadQuestion() {
        // Get questions and options from resources
        String[] questions = getResources().getStringArray(R.array.minigame_questions);
        String[][] allOptions = new String[][] {
                getResources().getStringArray(R.array.minigame_options_1),
                getResources().getStringArray(R.array.minigame_options_2),
                getResources().getStringArray(R.array.minigame_options_3),
                getResources().getStringArray(R.array.minigame_options_4),
                getResources().getStringArray(R.array.minigame_options_5)
        };

        // Set question text
        questionText.setText(questions[currentQuestionIndex]);

        // Set options
        String[] currentOptions = allOptions[currentQuestionIndex];
        for (int i = 0; i < optionTexts.length; i++) {
            optionTexts[i].setText(currentOptions[i]);
        }
    }

    private void checkAnswer(int selectedOption) {
        String[] correctAnswers = getResources().getStringArray(R.array.minigame_correct_answers);
        String selectedAnswer = optionTexts[selectedOption].getText().toString();
        String correctAnswer = correctAnswers[currentQuestionIndex];

        if (selectedAnswer.equals(correctAnswer)) {
            // Correct answer
            currentScore += 10;
            scoreText.setText(String.valueOf(currentScore));

            // Show success message
            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.minigame_correct),
                    Snackbar.LENGTH_SHORT).show();

            // Move to next question or finish game
            currentQuestionIndex++;
            if (currentQuestionIndex < correctAnswers.length) {
                loadQuestion();
            } else {
                finishGame();
            }
        } else {
            // Wrong answer
            View wrongOptionCard = optionCards[selectedOption];
            wrongOptionCard.startAnimation(shakeAnimation);

            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.minigame_wrong),
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private void finishGame() {
        // Award wisdom points based on score
        int wisdomPoints = currentScore / 2;
        gameState.addPuncteIntelepte(wisdomPoints, this);

        // Return result and finish
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
