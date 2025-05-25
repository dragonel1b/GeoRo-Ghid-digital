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
        
        // Adăugăm mai multe întrebări despre Oltenia
        questions.add(new Question(
            "Care este cea mai veche mănăstire din Oltenia?",
            new String[]{"Mănăstirea Cozia", "Mănăstirea Tismana", "Mănăstirea Polovragi", "Mănăstirea Horezu"},
            1,
            R.drawable.tismana,
            "Mănăstirea Tismana a fost fondată în secolul al XIV-lea de către Sfântul Nicodim și este cea mai veche mănăstire din Oltenia."
        ));
        
        questions.add(new Question(
            "Ce peșteră importantă se află în Oltenia?",
            new String[]{"Peștera Muierilor", "Peștera Urșilor", "Peștera Scărișoara", "Peștera Polovragi"},
            0,
            R.drawable.pestera_muierilor,
            "Peștera Muierilor din județul Gorj este una dintre cele mai vechi peșteri din România, cu o vechime de aproximativ 1,5 milioane de ani."
        ));
        
        questions.add(new Question(
            "Care este dansul popular specific Olteniei?",
            new String[]{"Călușul", "Hora", "Sârba", "Alunelul"},
            0,
            R.drawable.calusul,
            "Călușul este un dans popular specific Olteniei, inclus în patrimoniul UNESCO ca parte a patrimoniului cultural imaterial al umanității."
        ));
        
        questions.add(new Question(
            "Ce eveniment cultural important se desfășoară anual la Craiova?",
            new String[]{"Festivalul Shakespeare", "Festivalul George Enescu", "Festivalul Internațional de Teatru", "Festivalul Medieval"},
            0,
            R.drawable.craiova,
            "Festivalul Shakespeare este un eveniment cultural important care se desfășoară anual la Craiova și atrage artiști din întreaga lume."
        ));
        
        questions.add(new Question(
            "Care dintre următoarele personalități nu s-a născut în Oltenia?",
            new String[]{"Tudor Vladimirescu", "Constantin Brâncuși", "Mihai Eminescu", "Petrache Poenaru"},
            2,
            R.drawable.oltenia_map,
            "Mihai Eminescu s-a născut la Botoșani, în Moldova, nu în Oltenia."
        ));
        
        questions.add(new Question(
            "Ce monument natural spectaculos se află în Gorj?",
            new String[]{"Sfinxul", "Babele", "Cheile Sohodolului", "Cascada Bigăr"},
            2,
            R.drawable.cheile_sohodolului,
            "Cheile Sohodolului din județul Gorj sunt considerate printre cele mai spectaculoase chei din România."
        ));
        
        questions.add(new Question(
            "Care este cel mai important port dunărean din Oltenia?",
            new String[]{"Orșova", "Calafat", "Drobeta-Turnu Severin", "Corabia"},
            2,
            R.drawable.drobeta,
            "Drobeta-Turnu Severin este cel mai important port dunărean din Oltenia și unul dintre cele mai vechi orașe din România."
        ));
        
        questions.add(new Question(
            "Ce parc național important se află în Oltenia?",
            new String[]{"Parcul Național Domogled-Valea Cernei", "Parcul Național Retezat", "Parcul Național Piatra Craiului", "Parcul Național Ceahlău"},
            0,
            R.drawable.domogled,
            "Parcul Național Domogled-Valea Cernei este situat în sud-vestul României, în Oltenia, și este cel mai mare parc național din țară."
        ));
        
        questions.add(new Question(
            "Care este cea mai importantă stațiune balneară din Oltenia?",
            new String[]{"Băile Herculane", "Băile Felix", "Călimănești-Căciulata", "Sovata"},
            2,
            R.drawable.calimanesti,
            "Călimănești-Căciulata este cea mai importantă stațiune balneară din Oltenia, situată pe Valea Oltului."
        ));
        
        questions.add(new Question(
            "Ce pod celebru traversează Dunărea între România și Bulgaria, în Oltenia?",
            new String[]{"Podul Prieteniei", "Podul Calafat-Vidin", "Podul Giurgiu-Ruse", "Podul Cernavodă"},
            1,
            R.drawable.pod_calafat,
            "Podul Calafat-Vidin (Podul Nova Europa) a fost inaugurat în 2013 și leagă orașul Calafat din Oltenia de orașul Vidin din Bulgaria."
        ));
        
        questions.add(new Question(
            "Care este cel mai vechi oraș din Oltenia?",
            new String[]{"Craiova", "Râmnicu Vâlcea", "Drobeta-Turnu Severin", "Slatina"},
            2,
            R.drawable.drobeta,
            "Drobeta-Turnu Severin este cel mai vechi oraș din Oltenia, fiind fondat de romani în anul 105 d.Hr."
        ));
        
        questions.add(new Question(
            "Care este mâncarea tradițională specifică Olteniei?",
            new String[]{"Sarmale", "Piftie", "Ciorbă de burtă", "Praz cu ciolan afumat"},
            3,
            R.drawable.praz_ciolan,
            "Prazul cu ciolan afumat este o mâncare tradițională specifică Olteniei, foarte apreciată în gastronomia locală."
        ));
        
        questions.add(new Question(
            "Ce rezervație naturală importantă se află în Mehedinți?",
            new String[]{"Rezervația Naturală Ponoarele", "Rezervația Naturală Retezat", "Rezervația Naturală Bucegi", "Rezervația Naturală Apuseni"},
            0,
            R.drawable.ponoarele,
            "Rezervația Naturală Ponoarele din județul Mehedinți este cunoscută pentru fenomenele carstice spectaculoase, inclusiv Podul Natural de la Ponoarele."
        ));
        
        questions.add(new Question(
            "Ce lac de acumulare important se află pe râul Olt, în Oltenia?",
            new String[]{"Lacul Vidraru", "Lacul Vidra", "Lacul Izvorul Muntelui", "Lacul Călimănești"},
            3,
            R.drawable.lac_calimanesti,
            "Lacul Călimănești este un lac de acumulare important pe râul Olt, în Oltenia, utilizat pentru producerea de energie electrică."
        ));
        
        questions.add(new Question(
            "Ce castel important se află în județul Gorj?",
            new String[]{"Castelul Peleș", "Castelul Bran", "Castelul Corvinilor", "Castelul de la Măldărești"},
            3,
            R.drawable.castel_maldaresti,
            "Castelul de la Măldărești (Cula Măldărești) este un monument istoric important din județul Gorj, reprezentativ pentru arhitectura tradițională oltenească."
        ));
        
        Collections.shuffle(questions);
        progressBar.setMax(questions.size());
        totalQuestions = questions.size();
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            finishGame();
            return;
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        questionTextView.setText(currentQuestion.question);
        
        // Resetăm starea butoanelor pentru noua întrebare
        for (Button button : answerButtons) {
            button.setEnabled(true);
            button.setAlpha(1.0f);
            button.setBackgroundColor(getResources().getColor(R.color.oltenia_primary));
            button.setTextColor(getResources().getColor(R.color.oltenia_card_bg));
        }
        
        // Setăm răspunsurile
        for (int i = 0; i < currentQuestion.answers.length; i++) {
            answerButtons[i].setText(currentQuestion.answers[i]);
            final int index = i;
            answerButtons[i].setOnClickListener(v -> checkAnswer(index, currentQuestion.answers[index]));
        }
        
        // Setăm imaginea
        if (currentQuestion.imageResourceId != 0) {
            questionImage.setVisibility(View.VISIBLE);
            questionImage.setImageResource(currentQuestion.imageResourceId);
        } else {
            questionImage.setVisibility(View.GONE);
        }
        
        // Actualizăm progress bar
        progressBar.setProgress(currentQuestionIndex + 1);
    }

    private void checkAnswer(int selectedAnswerIndex, String selectedAnswer) {
        Question currentQuestion = questions.get(currentQuestionIndex);
        int correctIndex = currentQuestion.correctAnswerIndex;
        
        // Dezactivăm toate butoanele pentru a preveni răspunsuri multiple
        for (Button button : answerButtons) {
            button.setEnabled(false);
        }
        
        if (selectedAnswerIndex == correctIndex) {
            // Răspuns corect
            answerButtons[selectedAnswerIndex].setBackgroundColor(getResources().getColor(R.color.correct_answer));
            score += POINTS_PER_CORRECT_ANSWER;
            correctAnswers++;
            updateScore();
            
            // Afișăm informația suplimentară
            TextView factTextView = findViewById(R.id.factTextView);
            factTextView.setText("Corect! " + currentQuestion.fact);
            factTextView.setVisibility(View.VISIBLE);
            
            // Trecem la următoarea întrebare după o scurtă pauză
            new Handler().postDelayed(this::moveToNextQuestion, 2000);
        } else {
            // Răspuns greșit
            answerButtons[selectedAnswerIndex].setBackgroundColor(getResources().getColor(R.color.wrong_answer));
            answerButtons[correctIndex].setBackgroundColor(getResources().getColor(R.color.correct_answer));
            
            // Afișăm informația suplimentară
            TextView factTextView = findViewById(R.id.factTextView);
            factTextView.setText("Greșit! Răspunsul corect este: " + currentQuestion.answers[correctIndex] + ". " + currentQuestion.fact);
            factTextView.setVisibility(View.VISIBLE);
            
            // Trecem la următoarea întrebare după o scurtă pauză
            new Handler().postDelayed(this::moveToNextQuestion, 3000);
        }
    }

    private void moveToNextQuestion() {
        currentQuestionIndex++;
        TextView factTextView = findViewById(R.id.factTextView);
        factTextView.setVisibility(View.GONE);
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