package com.example.myapplication.Joc1;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RomQuizActivity extends AppCompatActivity {
    private RomGameState gameState;
    private TextView questionText, scoreText;
    private RadioGroup answersGroup;
    private MaterialButton submitButton, nextButton;
    private MaterialCardView quizCard;
    private android.widget.ProgressBar progressBar;

    private int currentQuestionIndex = 0;
    private int score = 0;
    private List<Question> currentQuestions;
    private String cityName;

    // Quiz database
    private static final Map<String, List<Question>> QUIZ_DATABASE = new HashMap<String, List<Question>>() {{
        // Sibiu Questions
        put("Sibiu", new ArrayList<Question>() {{
            add(new Question(
                    "În ce an a fost construit Podul Minciunilor?",
                    new String[]{"1859", "1875", "1892", "1902"},
                    0
            ));
            add(new Question(
                    "Care este numele original al Bisericii Evanghelice?",
                    new String[]{"Biserica Sfântul Ioan", "Biserica Sfânta Maria", "Biserica Sfântul Mihail", "Biserica Sfântul Petru"},
                    1
            ));
            add(new Question(
                    "Ce muzeu important se află în Piața Mare?",
                    new String[]{"Muzeul de Istorie", "Muzeul de Artă", "Muzeul Brukenthal", "Muzeul Satului"},
                    2
            ));
        }});

        // Cluj Questions
        put("Cluj", new ArrayList<Question>() {{
            add(new Question(
                    "În ce an a fost înființată Universitatea Babeș-Bolyai?",
                    new String[]{"1872", "1919", "1945", "1959"},
                    0
            ));
            add(new Question(
                    "Care este cel mai mare parc din Cluj-Napoca?",
                    new String[]{"Parcul Central", "Parcul Detunata", "Parcul Iulius", "Parcul Cetățuia"},
                    0
            ));
            add(new Question(
                    "Ce monument important se află în Piața Unirii?",
                    new String[]{"Statuia lui Mihai Viteazul", "Biserica Sf. Mihail", "Monumentul Memorandiștilor", "Statuia lui Matei Corvin"},
                    1
            ));
        }});

        // Brașov Questions
        put("Brașov", new ArrayList<Question>() {{
            add(new Question(
                    "În ce secol a fost construită Biserica Neagră?",
                    new String[]{"Secolul XIV", "Secolul XV", "Secolul XVI", "Secolul XVII"},
                    0
            ));
            add(new Question(
                    "Care este înălțimea Muntelui Tâmpa?",
                    new String[]{"960m", "955m", "995m", "1000m"},
                    1
            ));
            add(new Question(
                    "Ce poartă medievală este cea mai bine conservată din Brașov?",
                    new String[]{"Poarta Șchei", "Poarta Ecaterinei", "Poarta Străzii Lungi", "Poarta Vămii"},
                    1
            ));
        }});

        // București Questions
        put("București", new ArrayList<Question>() {{
            add(new Question(
                    "În ce an a fost inaugurat Palatul Parlamentului?",
                    new String[]{"1984", "1989", "1994", "1997"},
                    2
            ));
            add(new Question(
                    "Care este cel mai vechi parc din București?",
                    new String[]{"Parcul Herăstrău", "Parcul Cișmigiu", "Parcul Carol", "Parcul Tineretului"},
                    1
            ));
            add(new Question(
                    "În ce an a devenit București capitala României?",
                    new String[]{"1859", "1862", "1878", "1881"},
                    1
            ));
        }});

        // Iași Questions
        put("Iași", new ArrayList<Question>() {{
            add(new Question(
                    "În ce an a fost construită Palatul Culturii?",
                    new String[]{"1906", "1925", "1932", "1945"},
                    1
            ));
            add(new Question(
                    "Care este cea mai veche universitate din România?",
                    new String[]{"Universitatea din București", "Universitatea Babeș-Bolyai", "Universitatea Alexandru Ioan Cuza", "Universitatea Politehnica"},
                    2
            ));
            add(new Question(
                    "Ce lungime are Bulevardul Ștefan cel Mare?",
                    new String[]{"2.2 km", "2.5 km", "2.8 km", "3.0 km"},
                    1
            ));
        }});

        // Timișoara Questions
        put("Timișoara", new ArrayList<Question>() {{
            add(new Question(
                    "În ce an a devenit Timișoara primul oraș european cu iluminat stradal electric?",
                    new String[]{"1879", "1884", "1889", "1894"},
                    1
            ));
            add(new Question(
                    "Care este supranumele Timișoarei?",
                    new String[]{"Orașul Grădină", "Mica Vienă", "Orașul Luminii", "Orașul Florilor"},
                    2
            ));
            add(new Question(
                    "În ce an a început Revoluția Română în Timișoara?",
                    new String[]{"1987", "1988", "1989", "1990"},
                    2
            ));
        }});

        // Constanța Questions
        put("Constanța", new ArrayList<Question>() {{
            add(new Question(
                    "Care este lungimea Plajei Modern?",
                    new String[]{"2.5 km", "3 km", "3.5 km", "4 km"},
                    2
            ));
            add(new Question(
                    "În ce an a fost construit Cazinoul din Constanța?",
                    new String[]{"1880", "1900", "1910", "1920"},
                    2
            ));
            add(new Question(
                    "Care este numele antic al orașului Constanța?",
                    new String[]{"Tomis", "Histria", "Callatis", "Axiopolis"},
                    0
            ));
        }});

        // Oradea Questions
        put("Oradea", new ArrayList<Question>() {{
            add(new Question(
                    "În ce stil arhitectural este construită majoritatea clădirilor din centrul istoric?",
                    new String[]{"Baroc", "Art Nouveau", "Gotic", "Renascentist"},
                    1
            ));
            add(new Question(
                    "Care este lungimea totală a pasajelor subterane din Cetatea Oradea?",
                    new String[]{"150m", "250m", "350m", "450m"},
                    2
            ));
            add(new Question(
                    "În ce an a fost construită Biserica cu Lună?",
                    new String[]{"1784", "1792", "1800", "1810"},
                    1
            ));
        }});
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_quiz);

        gameState = RomGameState.getInstance();

        cityName = getIntent().getStringExtra("CITY_NAME");
        if (cityName == null) {
            Toast.makeText(this, "Eroare la încărcarea quiz-ului", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        prepareQuiz();
        showQuestion();
    }

    private void initializeViews() {
        questionText = findViewById(R.id.questionText);
        scoreText = findViewById(R.id.scoreText);
        answersGroup = findViewById(R.id.answersGroup);
        submitButton = findViewById(R.id.submitButton);
        nextButton = findViewById(R.id.nextButton);
        quizCard = findViewById(R.id.quizCard);
        progressBar = findViewById(R.id.progressBar);

        submitButton.setOnClickListener(v -> checkAnswer());
        nextButton.setOnClickListener(v -> showNextQuestion());
    }

    private void prepareQuiz() {
        List<Question> cityQuestions = QUIZ_DATABASE.get(cityName);
        if (cityQuestions == null) {
            Toast.makeText(this, "Nu există întrebări pentru acest oraș", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentQuestions = new ArrayList<>(cityQuestions);
        Collections.shuffle(currentQuestions);
        updateScoreDisplay();

        // Initialize progress bar
        progressBar.setMax(100);
        progressBar.setProgress(0);
    }

    private void showQuestion() {
        Question currentQuestion = currentQuestions.get(currentQuestionIndex);
        questionText.setText(currentQuestion.question);

        // Update progress bar
        int progress = (currentQuestionIndex + 1) * 100 / currentQuestions.size();
        progressBar.setProgress(progress);

        answersGroup.removeAllViews();
        for (int i = 0; i < currentQuestion.answers.length; i++) {
            RadioButton button = new RadioButton(this);
            button.setText(currentQuestion.answers[i]);
            button.setId(i);

            // Style the radio button
            button.setTextSize(18);
            button.setTextColor(getResources().getColor(android.R.color.black));
            button.setPadding(32, 24, 32, 24);

            // Add margin between options
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 8, 0, 8);
            button.setLayoutParams(params);

            answersGroup.addView(button);
        }

        submitButton.setEnabled(true);
        nextButton.setVisibility(View.GONE);

        // Apply animation
        quizCard.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
    }

    private void checkAnswer() {
        int selectedId = answersGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Te rog selectează un răspuns", Toast.LENGTH_SHORT).show();
            return;
        }

        Question currentQuestion = currentQuestions.get(currentQuestionIndex);
        boolean isCorrect = selectedId == currentQuestion.correctAnswerIndex;

        if (isCorrect) {
            score += 10;
            updateScoreDisplay();
            Toast.makeText(this, "Corect! +10 puncte", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Incorect! Răspunsul corect era: " +
                    currentQuestion.answers[currentQuestion.correctAnswerIndex], Toast.LENGTH_LONG).show();
        }

        // Disable answer selection and show next button
        for (int i = 0; i < answersGroup.getChildCount(); i++) {
            answersGroup.getChildAt(i).setEnabled(false);
        }
        submitButton.setEnabled(false);
        nextButton.setVisibility(View.VISIBLE);
    }

    private void showNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < currentQuestions.size()) {
            showQuestion();
        } else {
            finishQuiz();
        }
    }

    private void updateScoreDisplay() {
        scoreText.setText("Scor: " + score);
    }

    private void finishQuiz() {
        // Award wisdom points based on score
        int puncteIntelepte = score / 10;
        gameState.addPuncteIntelepte(puncteIntelepte, this);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Quiz Completat!")
                .setMessage(String.format(
                        "Felicitări!\n\nScor final: %d\nPuncte Înțelepte dobândite: %d",
                        score, puncteIntelepte))
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    public void goBack(View view) {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // Inner class for quiz questions
    private static class Question {
        String question;
        String[] answers;
        int correctAnswerIndex;

        Question(String question, String[] answers, int correctAnswerIndex) {
            this.question = question;
            this.answers = answers;
            this.correctAnswerIndex = correctAnswerIndex;
        }
    }
}
