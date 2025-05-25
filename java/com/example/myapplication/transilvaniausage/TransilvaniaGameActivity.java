package com.example.myapplication.transilvaniausage;

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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.Transilvania;
import com.example.myapplication.RomApp.PointsManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TransilvaniaGameActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_transilvania_game);

        initializeViews();
        pointsManager = PointsManager.getInstance(this);
        initializeQuestions();
        setupLifelines();
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
        }
        
        // Adaugă efecte vizuale pentru butonul de finalizare
        finishButton.setRippleColor(ContextCompat.getColorStateList(this, R.color.transilvania_accent));
    }

    private void setupLifelines() {
        fiftyFiftyButton.setOnClickListener(v -> {
            // Adăugăm efect vizual la apăsare
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            useFiftyFifty();
        });
        
        skipQuestionButton.setOnClickListener(v -> {
            // Adăugăm efect vizual la apăsare
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
            skipQuestion();
        });
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(TIME_PER_QUESTION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTextView.setText(String.valueOf(millisUntilFinished / 1000));
                
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
        
        // Dezactivăm două răspunsuri greșite cu feedback vizual
        for (int i = 0; i < 2; i++) {
            int index = wrongAnswers.get(i);
            answerButtons[index].setEnabled(false);
            answerCards[index].setAlpha(0.5f);
            answerCards[index].setClickable(false);
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
        
        // Întrebări despre Transilvania
        questions.add(new Question(
            "Care este cea mai veche cetate medievală locuită din Transilvania?",
            new String[]{"Sibiu", "Sighișoara", "Brașov", "Alba Iulia"},
            1,
            R.drawable.sighisoara,
            "Sighișoara este singura cetate medievală locuită din Europa de Est, parte a patrimoniului UNESCO."
        ));
        questions.add(new Question(
            "Ce cetate din Transilvania este adesea asociată cu legenda lui Dracula?",
            new String[]{"Cetatea Făgăraș", "Cetatea Rupea", "Castelul Bran", "Cetatea Râșnov"},
            2,
            R.drawable.bran,
            "Castelul Bran, deși nu are o legătură istorică dovedită cu Vlad Țepeș, este popularizat ca \"Castelul lui Dracula\" datorită romanului lui Bram Stoker."
        ));
        
        // Păstrăm celelalte întrebări...
        questions.add(new Question(
            "Care oraș din Transilvania a fost Capitală Culturală Europeană în 2007?",
            new String[]{"Cluj-Napoca", "Sibiu", "Brașov", "Târgu Mureș"},
            1,
            R.drawable.sibiu,
            "Sibiu a fost desemnat Capitală Culturală Europeană în 2007, alături de Luxembourg."
        ));
        questions.add(new Question(
            "În ce oraș din Transilvania s-a semnat actul Unirii din 1918?",
            new String[]{"Cluj-Napoca", "Sibiu", "Alba Iulia", "Deva"},
            2,
            R.drawable.alba_iulia,
            "Alba Iulia este locul unde s-a semnat actul Unirii din 1 Decembrie 1918, când Transilvania s-a unit cu Regatul României."
        ));
        
        // Adăugăm mai multe întrebări noi
        questions.add(new Question(
            "Ce munte din Transilvania este asociat cu o legendă despre un bătrân care a născocit vinul?",
            new String[]{"Retezat", "Bucegi", "Ceahlău", "Hășmaș"},
            1,
            R.drawable.bucegi,
            "Muntele Bucegi este legat de legenda bătrânului Bucur, care se spune că a inventat vinul și a dat numele orașului București."
        ));
        
        questions.add(new Question(
            "Care este cel mai mare oraș din Transilvania?",
            new String[]{"Brașov", "Sibiu", "Cluj-Napoca", "Oradea"},
            2,
            R.drawable.cluj_napoca,
            "Cluj-Napoca este cel mai mare oraș din Transilvania, fiind un important centru economic, academic și cultural."
        ));
        
        questions.add(new Question(
            "Ce lac glaciar din Munții Făgăraș poartă numele unui conte transilvan?",
            new String[]{"Lacul Bâlea", "Lacul Sfânta Ana", "Lacul Vidraru", "Lacul Roșu"},
            0,
            R.drawable.balea,
            "Lacul Bâlea este un lac glaciar situat la altitudinea de 2034 m în Munții Făgăraș, numit după contele transilvănean Bâlea."
        ));
        
        questions.add(new Question(
            "Ce minoritate etnică a avut o contribuție semnificativă la dezvoltarea Transilvaniei?",
            new String[]{"Maghiarii", "Sașii", "Secuii", "Toate variantele sunt corecte"},
            3,
            R.drawable.etnii_transilvania,
            "Transilvania are o istorie bogată a diversității etnice, maghiarii, sașii și secuii contribuind semnificativ la dezvoltarea regiunii."
        ));
        
        questions.add(new Question(
            "Care este numele teatrului național din Cluj-Napoca?",
            new String[]{"Teatrul Mihai Eminescu", "Teatrul Lucian Blaga", "Teatrul Național Maghiar", "Teatrul Radu Stanca"},
            1,
            R.drawable.teatru_cluj,
            "Teatrul Național \"Lucian Blaga\" din Cluj-Napoca este una dintre cele mai importante instituții culturale din Transilvania."
        ));
        
        questions.add(new Question(
            "Ce pasaj montan face legătura între Transilvania și Țara Românească?",
            new String[]{"Pasul Tihuța", "Pasul Oituz", "Pasul Predeal", "Pasul Turnu Roșu"},
            2,
            R.drawable.predeal,
            "Pasul Predeal, situat la o altitudine de 1033 m, este unul dintre cele mai importante pasaje montane care leagă Transilvania de Țara Românească."
        ));
        
        questions.add(new Question(
            "Ce universitate din Transilvania este una dintre cele mai vechi din România?",
            new String[]{"Universitatea din Alba Iulia", "Universitatea Babeș-Bolyai", "Universitatea din Oradea", "Universitatea de Medicină din Târgu Mureș"},
            1,
            R.drawable.babes_bolyai,
            "Universitatea Babeș-Bolyai din Cluj-Napoca, înființată în 1581, este una dintre cele mai vechi și prestigioase universități din România."
        ));
        
        questions.add(new Question(
            "Ce fruct este specific regiunii Bistrița din Transilvania?",
            new String[]{"Cirese", "Mere", "Prune", "Struguri"},
            1,
            R.drawable.mere_bistrita,
            "Merele de Bistrița sunt renumite în întreaga Românie pentru gustul și calitatea lor, fiind o emblemă a regiunii."
        ));
        
        questions.add(new Question(
            "Ce sat din Transilvania a fost declarat sat UNESCO?",
            new String[]{"Viscri", "Biertan", "Saschiz", "Toate variantele sunt corecte"},
            3,
            R.drawable.viscri,
            "Satele cu biserici fortificate din Transilvania: Viscri, Biertan, Saschiz și altele, sunt toate parte din patrimoniul mondial UNESCO."
        ));
        
        questions.add(new Question(
            "Ce fenomen natural unic poate fi observat în Salina Turda?",
            new String[]{"Stalactite de sare", "Un lac subteran", "Corali fosili", "Un ecou care se repetă de 7 ori"},
            1,
            R.drawable.salina_turda,
            "Salina Turda adăpostește un lac subteran cu apă foarte sărată, format natural și unul dintre cele mai spectaculoase obiective turistice din Transilvania."
        ));
        
        questions.add(new Question(
            "Ce obiect din castelul Corvinilor se spune că ar avea puteri magice?",
            new String[]{"O armură medievală", "O fântână", "Un inel", "Un scut"},
            1,
            R.drawable.castelul_corvinilor,
            "Legenda spune că fântâna din castelul Corvinilor a fost săpată de trei prizonieri turci cărora li s-a promis libertatea după finalizare, dar promisiunea nu a fost respectată."
        ));
        
        questions.add(new Question(
            "Care este numele tradițional dat colindătorilor din Transilvania?",
            new String[]{"Urători", "Colindători", "Dițaladă", "Pitărași"},
            2,
            R.drawable.colindatori,
            "În unele zone din Transilvania, colindătorii sunt cunoscuți sub numele de \"dițaladă\", termen ce provine din tradiția locală."
        ));
        
        questions.add(new Question(
            "Ce fortificație din Transilvania a fost construită în secolul al XIII-lea de Cavalerii Teutoni?",
            new String[]{"Cetatea Neamțului", "Cetatea Râșnov", "Cetatea Feldioara", "Cetatea Făgăraș"},
            2,
            R.drawable.feldioara,
            "Cetatea Feldioara a fost construită de Cavalerii Teutoni în secolul al XIII-lea pentru a proteja granițele sud-estice ale Transilvaniei de invaziile cumanilor."
        ));
        
        // Amestecăm întrebările pentru experiență diferită de fiecare dată
        Collections.shuffle(questions);
        progressBar.setMax(questions.size());
        progressBar.setProgress(0);
    }

    private void displayQuestion() {
        resetCardStyles();
        
        if (currentQuestionIndex < questions.size()) {
            // Hide finish button during questions
            finishButton.setVisibility(View.GONE);
            
            Question question = questions.get(currentQuestionIndex);
            questionTextView.setText(question.question);
            
            for (int i = 0; i < answerButtons.length; i++) {
                answerButtons[i].setText(question.answers[i]);
                answerButtons[i].setEnabled(true);
                answerCards[i].setAlpha(1.0f);
                
                final int answerIndex = i;
                answerCards[i].setOnClickListener(v -> {
                    // Adaugă animație la apăsare
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press));
                    checkAnswer(answerIndex, question.answers[answerIndex]);
                });
            }
            
            if (question.imageResourceId != 0) {
                questionImage.setImageResource(question.imageResourceId);
                questionImage.setVisibility(View.VISIBLE);
            } else {
                questionImage.setVisibility(View.GONE);
            }
            
            // Adăugăm un efect de fade in pentru întrebare
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            fadeIn.setDuration(500);
            questionTextView.startAnimation(fadeIn);
            
            for (MaterialCardView card : answerCards) {
                card.startAnimation(fadeIn);
            }
            
            progressBar.setProgress(currentQuestionIndex + 1);
        } else {
            showFinishButton();
        }
    }
    
    private void resetCardStyles() {
        for (int i = 0; i < answerCards.length; i++) {
            answerCards[i].setStrokeColor(ContextCompat.getColor(this, R.color.transilvania_primary_light));
            answerCards[i].setAlpha(1.0f);
            answerCards[i].setClickable(true);
            answerButtons[i].setEnabled(true);
            answerButtons[i].setTextColor(ContextCompat.getColor(this, R.color.transilvania_text));
        }
    }

    private void checkAnswer(int selectedAnswerIndex, String selectedAnswer) {
        if (timer != null) {
            timer.cancel();
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        boolean isCorrect = selectedAnswerIndex == currentQuestion.correctAnswerIndex;
        
        // Dezactivează toate cardurile pentru a preveni clicuri multiple
        for (MaterialCardView card : answerCards) {
            card.setClickable(false);
        }
        
        // Marchează răspunsul selectat
        if (isCorrect) {
            // Răspuns corect - verde
            answerCards[selectedAnswerIndex].setStrokeColor(ContextCompat.getColor(this, R.color.rom_correct_answer));
            answerCards[selectedAnswerIndex].setStrokeWidth(4);
            
            // Animație de succes
            Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
            answerCards[selectedAnswerIndex].startAnimation(scaleUp);
            
            // Actualizează scorul și streak-ul
            score += POINTS_PER_CORRECT_ANSWER;
            streak++;
            if (streak > maxStreak) {
                maxStreak = streak;
            }
            correctAnswers++;
            
            // Bonus pentru streak
            if (streak >= STREAK_BONUS_THRESHOLD) {
                int bonus = streak * 5;
                score += bonus;
                Toast.makeText(this, "Bonus serie: +" + bonus + " puncte!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Răspuns greșit - roșu
            answerCards[selectedAnswerIndex].setStrokeColor(ContextCompat.getColor(this, R.color.rom_wrong_answer));
            answerCards[selectedAnswerIndex].setStrokeWidth(4);
            
            // Afișează răspunsul corect - verde
            answerCards[currentQuestion.correctAnswerIndex].setStrokeColor(ContextCompat.getColor(this, R.color.rom_correct_answer));
            answerCards[currentQuestion.correctAnswerIndex].setStrokeWidth(4);
            
            // Reset streak
            streak = 0;
        }
        
        // Actualizează UI
        updateScore();
        updateStreak();
        
        // Așteaptă puțin înainte de a trece la următoarea întrebare
        new Handler().postDelayed(() -> moveToNextQuestion(), 800);
    }

    private void moveToNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            resetCardStyles();
            displayQuestion();
            startTimer();
        } else {
            showFinishButton();
        }
    }

    private void updateScore() {
        scoreTextView.setText(String.valueOf(score));
        progressBar.setProgress(Math.min(100, (currentQuestionIndex * 100) / questions.size()));
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
        
        if (correctAnswers == questions.size()) {
            achievements.add("Perfect! Toate răspunsurile corecte");
        } else if (correctAnswers >= questions.size() * 0.8) {
            achievements.add("Expert al Transilvaniei (" + correctAnswers + " din " + questions.size() + " corecte)");
        } else if (correctAnswers >= questions.size() * 0.5) {
            achievements.add("Bun cunoscător (" + correctAnswers + " din " + questions.size() + " corecte)");
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
        
        Intent intent = new Intent(this, GameOverActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("correctAnswers", correctAnswers);
        intent.putExtra("totalQuestions", questions.size());
        intent.putExtra("maxStreak", maxStreak);
        intent.putExtra("ACHIEVEMENTS", getAchievements());
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

    // New method to show finish button at the end
    private void showFinishButton() {
        // Hide all answer cards to avoid overlap
        for (MaterialCardView card : answerCards) {
            card.setVisibility(View.GONE);
        }
        
        // Show finish button with animation
        finishButton.setVisibility(View.VISIBLE);
        Animation scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        finishButton.startAnimation(scaleIn);
        
        // Optional: Show a message that the quiz is complete
        questionTextView.setText("Quiz complet! Felicitări!");
        if (timer != null) {
            timer.cancel();
        }
    }
} 