package com.example.myapplication.olteniausage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.Oltenia;
import com.example.myapplication.RomApp.PointsManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class OlteniaGameActivity extends AppCompatActivity {
    private TextView questionTextView;
    private Button[] answerButtons;
    private TextView scoreTextView;
    private ProgressBar progressBar;
    private ImageView questionImage;
    private ImageButton fiftyFiftyButton;
    private ImageButton skipQuestionButton;
    private Button finishButton;
    
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int totalQuestions = 0;
    private int correctAnswers = 0;
    private List<Question> questions;
    
    private static final int POINTS_PER_CORRECT_ANSWER = 10;
    private PointsManager pointsManager;
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
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oltenia_game);

        initializeViews();
        pointsManager = PointsManager.getInstance(this);
        initializeQuestions();
        setupLifelines();
        displayQuestion();
        updateScore();
    }

    public void goBack(View view) {
        onBackPressed();
    }

    private void initializeViews() {
        questionTextView = findViewById(R.id.questionTextView);
        answerButtons = new Button[4];
        answerButtons[0] = findViewById(R.id.answerButton1);
        answerButtons[1] = findViewById(R.id.answerButton2);
        answerButtons[2] = findViewById(R.id.answerButton3);
        answerButtons[3] = findViewById(R.id.answerButton4);
        scoreTextView = findViewById(R.id.scoreTextView);
        progressBar = findViewById(R.id.progressBar);
        questionImage = findViewById(R.id.questionImage);
        fiftyFiftyButton = findViewById(R.id.fiftyFiftyButton);
        skipQuestionButton = findViewById(R.id.skipQuestionButton);
        finishButton = findViewById(R.id.finishButton);
        
        finishButton.setOnClickListener(v -> finishGame());
        
        pointsManager = PointsManager.getInstance(this);
        
        // Initialize questions and display first question
        initializeQuestions();
        if (questions.size() > 0) {
            progressBar.setMax(questions.size());
            displayQuestion();
        }
    }

    private void setupLifelines() {
        fiftyFiftyButton.setOnClickListener(v -> useFiftyFifty());
        skipQuestionButton.setOnClickListener(v -> skipQuestion());
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
        
        // Întrebări despre Oltenia
        questions.add(new Question(
            "Care este cel mai înalt vârf montan din Oltenia?",
            new String[]{"Vârful Parângul Mare", "Vârful Moldoveanu", "Vârful Omu", "Vârful Nedeia"},
            0,
            R.drawable.parang,
            "Vârful Parângul Mare are o altitudine de 2.519 metri și este situat în Munții Parâng."
        ));
        
        questions.add(new Question(
            "Care este cel mai mare oraș din Oltenia?",
            new String[]{"Târgu Jiu", "Slatina", "Râmnicu Vâlcea", "Craiova"},
            3,
            R.drawable.craiova,
            "Craiova este cel mai mare oraș din Oltenia, cu o populație de aproximativ 300.000 de locuitori."
        ));
        
        questions.add(new Question(
            "Ce râu important traversează Oltenia de la nord la sud?",
            new String[]{"Jiu", "Olt", "Mureș", "Siret"},
            0,
            R.drawable.targujiu,
            "Râul Jiu străbate Oltenia de la nord la sud, având o lungime de 331 km."
        ));
        
        questions.add(new Question(
            "În ce județ se află Mănăstirea Tismana?",
            new String[]{"Dolj", "Gorj", "Mehedinți", "Vâlcea"},
            1,
            R.drawable.tismana,
            "Mănăstirea Tismana se află în județul Gorj și este una dintre cele mai vechi mănăstiri din România."
        ));
        
        questions.add(new Question(
            "Care dintre următoarele nu este un județ din Oltenia?",
            new String[]{"Dolj", "Gorj", "Olt", "Argeș"},
            3,
            R.drawable.oltenia_map,
            "Județul Argeș face parte din regiunea Muntenia, nu din Oltenia."
        ));
        
        questions.add(new Question(
            "Ce sculptor român celebru s-a născut în Hobița, Gorj?",
            new String[]{"Constantin Brâncuși", "Ion Jalea", "Dimitrie Paciurea", "Gheorghe Anghel"},
            0,
            R.drawable.brancusi,
            "Constantin Brâncuși s-a născut la Hobița, în județul Gorj, în 1876."
        ));
        
        questions.add(new Question(
            "Ce obiectiv turistic sculptat în stâncă se află în Oltenia?",
            new String[]{"Sfinxul", "Babele", "Chipul lui Decebal", "Cheile Oltețului"},
            2,
            R.drawable.chipul_decebal,
            "Chipul lui Decebal, sculptat în stâncă, se află pe malul Dunării, în județul Mehedinți."
        ));
        
        questions.add(new Question(
            "Care este principala zonă viticolă din Oltenia?",
            new String[]{"Drăgășani", "Recaș", "Cotnari", "Murfatlar"},
            0,
            R.drawable.dragasani,
            "Zona Drăgășani este cunoscută pentru vinurile sale de calitate, în special soiurile Crâmpoșie și Tămâioasă."
        ));
        
        questions.add(new Question(
            "Ce defileu spectaculos se găsește pe râul Olt?",
            new String[]{"Defileul Jiului", "Defileul Oltului", "Cheile Bicazului", "Cheile Turzii"},
            1,
            R.drawable.defileul_oltului,
            "Defileul Oltului este unul dintre cele mai spectaculoase din România, cu o lungime de aproximativ 47 km."
        ));
        
        questions.add(new Question(
            "Ce obiectiv important realizat de Constantin Brâncuși se află în Târgu Jiu?",
            new String[]{"Coloana Infinitului", "Poarta Sărutului", "Masa Tăcerii", "Toate variantele"},
            3,
            R.drawable.brancusi,
            "Ansamblul Monumental realizat de Constantin Brâncuși la Târgu Jiu cuprinde Coloana Infinitului, Poarta Sărutului și Masa Tăcerii."
        ));
        
        Collections.shuffle(questions);
        progressBar.setMax(questions.size());
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            finishGame();
            return;
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        questionTextView.setText(currentQuestion.question);
        
        // Set question image
        if (currentQuestion.imageResourceId != 0) {
            questionImage.setVisibility(View.VISIBLE);
            questionImage.setImageResource(currentQuestion.imageResourceId);
        } else {
            questionImage.setVisibility(View.GONE);
        }
        
        // Enable all buttons
        for (int i = 0; i < answerButtons.length; i++) {
            final int answerIndex = i;
            final String answer = currentQuestion.answers[i];
            
            answerButtons[i].setText(answer);
            answerButtons[i].setEnabled(true);
            answerButtons[i].setBackgroundTintList(getResources().getColorStateList(R.color.oltenia_primary));
            
            // Set click listeners for answer buttons
            answerButtons[i].setOnClickListener(v -> checkAnswer(answerIndex, answer));
        }
        
        // Update progress
        progressBar.setProgress(currentQuestionIndex + 1);
    }

    private void checkAnswer(int selectedAnswerIndex, String selectedAnswer) {
        Question currentQuestion = questions.get(currentQuestionIndex);
        boolean isCorrect = selectedAnswerIndex == currentQuestion.correctAnswerIndex;

        if (isCorrect) {
            score += POINTS_PER_CORRECT_ANSWER;
            correctAnswers++;
            
            Toast.makeText(this, "Corect! +" + POINTS_PER_CORRECT_ANSWER + " puncte", Toast.LENGTH_SHORT).show();
            
            // Highlight correct answer
            answerButtons[selectedAnswerIndex].setBackgroundColor(getResources().getColor(R.color.rom_correct_answer));
        } else {
            Toast.makeText(this, "Răspuns greșit! Răspunsul corect era: " + 
                    currentQuestion.answers[currentQuestion.correctAnswerIndex], Toast.LENGTH_SHORT).show();
            
            // Highlight wrong answer and show correct one
            answerButtons[selectedAnswerIndex].setBackgroundColor(getResources().getColor(R.color.rom_wrong_answer));
            answerButtons[currentQuestion.correctAnswerIndex].setBackgroundColor(getResources().getColor(R.color.rom_correct_answer));
        }

        totalQuestions++;
        updateScore();

        // Disable all answer buttons
        for (Button button : answerButtons) {
            button.setEnabled(false);
        }

        // Display fact in a toast
        new Handler().postDelayed(() -> {
            Toast.makeText(this, currentQuestion.fact, Toast.LENGTH_LONG).show();
            
            // Reset button colors
            for (Button button : answerButtons) {
                button.setBackgroundTintList(getResources().getColorStateList(R.color.oltenia_primary));
            }
            
            moveToNextQuestion();
        }, 2000);
    }

    private void moveToNextQuestion() {
        currentQuestionIndex++;
        displayQuestion();
    }

    private void updateScore() {
        scoreTextView.setText(getString(R.string.score_format, score));
    }

    private String getAchievements() {
        StringBuilder achievements = new StringBuilder();
        if (correctAnswers >= questions.size() * 0.8) achievements.append("Expert în Oltenia!\n");
        if (score >= 100) achievements.append("Scor impresionant!\n");
        if (isFiftyFiftyUsed && isSkipUsed) achievements.append("Utilizator de lifeline!\n");
        if (!isFiftyFiftyUsed && !isSkipUsed && correctAnswers >= questions.size() * 0.7) achievements.append("Fără ajutor - expert adevărat!\n");
        return achievements.toString();
    }

    private void finishGame() {
        String achievements = getAchievements();
        if (!achievements.isEmpty()) {
            Toast.makeText(this, "Achievements:\n" + achievements, Toast.LENGTH_LONG).show();
        }

        pointsManager.addPoints(this, "Oltenia", score);
        
        Intent intent = new Intent(this, OlteniaGameOverActivity.class);
        intent.putExtra("finalScore", score);
        intent.putExtra("correctAnswers", correctAnswers);
        intent.putExtra("totalQuestions", totalQuestions);
        intent.putExtra("achievements", achievements);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Ask user if they want to exit the game
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.exit_game)
            .setMessage(R.string.exit_game_confirmation)
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                Intent intent = new Intent(this, Oltenia.class);
                startActivity(intent);
                finish();
            })
            .setNegativeButton(R.string.no, null)
            .show();
    }
} 