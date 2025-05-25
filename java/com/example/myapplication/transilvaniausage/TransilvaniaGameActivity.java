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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AlertDialog;

public class TransilvaniaGameActivity extends AppCompatActivity {
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
        initializeQuestions();
        setupLifelines();
        applyButtonStyles();
        setupAccessibility();
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
        
        // Întrebări despre Transilvania
        questions.add(new Question(
            "Care este cea mai veche cetate medievală locuită din Transilvania?",
            new String[]{"Sibiu", "Sighișoara", "Brașov", "Alba Iulia"},
            1,
            R.drawable.sighisoara,
            "Sighișoara este singura cetate medievală locuită din Europa de Est, parte a patrimoniului UNESCO.",
            "Sighișoara este singura cetate medievală locuită din Europa de Est, parte a patrimoniului UNESCO."
        ));
        questions.add(new Question(
            "Ce cetate din Transilvania este adesea asociată cu legenda lui Dracula?",
            new String[]{"Cetatea Făgăraș", "Cetatea Rupea", "Castelul Bran", "Cetatea Râșnov"},
            2,
            R.drawable.bran,
            "Castelul Bran, deși nu are o legătură istorică dovedită cu Vlad Țepeș, este popularizat ca \"Castelul lui Dracula\" datorită romanului lui Bram Stoker.",
            "Castelul Bran, deși nu are o legătură istorică dovedită cu Vlad Țepeș, este popularizat ca \"Castelul lui Dracula\" datorită romanului lui Bram Stoker."
        ));
        
        // Păstrăm celelalte întrebări...
        questions.add(new Question(
            "Care oraș din Transilvania a fost Capitală Culturală Europeană în 2007?",
            new String[]{"Cluj-Napoca", "Sibiu", "Brașov", "Târgu Mureș"},
            1,
            R.drawable.sibiu,
            "Sibiu a fost desemnat Capitală Culturală Europeană în 2007, alături de Luxembourg.",
            "Sibiu a fost desemnat Capitală Culturală Europeană în 2007, alături de Luxembourg."
        ));
        questions.add(new Question(
            "În ce oraș din Transilvania s-a semnat actul Unirii din 1918?",
            new String[]{"Cluj-Napoca", "Sibiu", "Alba Iulia", "Deva"},
            2,
            R.drawable.alba_iulia,
            "Alba Iulia este locul unde s-a semnat actul Unirii din 1 Decembrie 1918, când Transilvania s-a unit cu Regatul României.",
            "Alba Iulia este locul unde s-a semnat actul Unirii din 1 Decembrie 1918, când Transilvania s-a unit cu Regatul României."
        ));
        
        // Adăugăm mai multe întrebări noi
        questions.add(new Question(
            "Ce munte din Transilvania este asociat cu o legendă despre un bătrân care a născocit vinul?",
            new String[]{"Retezat", "Bucegi", "Ceahlău", "Hășmaș"},
            1,
            R.drawable.bucegi,
            "Muntele Bucegi este legat de legenda bătrânului Bucur, care se spune că a inventat vinul și a dat numele orașului București.",
            "Muntele Bucegi este legat de legenda bătrânului Bucur, care se spune că a inventat vinul și a dat numele orașului București."
        ));
        
        questions.add(new Question(
            "Care este cel mai mare oraș din Transilvania?",
            new String[]{"Brașov", "Sibiu", "Cluj-Napoca", "Oradea"},
            2,
            R.drawable.cluj,
            "Cluj-Napoca este cel mai mare oraș din Transilvania, fiind un important centru economic, academic și cultural.",
            "Cluj-Napoca este cel mai mare oraș din Transilvania, fiind un important centru economic, academic și cultural."
        ));
        
        questions.add(new Question(
            "Ce lac glaciar din Munții Făgăraș poartă numele unui conte transilvan?",
            new String[]{"Lacul Bâlea", "Lacul Sfânta Ana", "Lacul Vidraru", "Lacul Roșu"},
            0,
            R.drawable.balea,
            "Lacul Bâlea este un lac glaciar situat la altitudinea de 2034 m în Munții Făgăraș, numit după contele transilvănean Bâlea.",
            "Lacul Bâlea este un lac glaciar situat la altitudinea de 2034 m în Munții Făgăraș, numit după contele transilvănean Bâlea."
        ));
        
        questions.add(new Question(
            "Ce minoritate etnică a avut o contribuție semnificativă la dezvoltarea Transilvaniei?",
            new String[]{"Maghiarii", "Sașii", "Secuii", "Toate variantele sunt corecte"},
            3,
            R.drawable.etnii_transilvania,
            "Transilvania are o istorie bogată a diversității etnice, maghiarii, sașii și secuii contribuind semnificativ la dezvoltarea regiunii.",
            "Transilvania are o istorie bogată a diversității etnice, maghiarii, sașii și secuii contribuind semnificativ la dezvoltarea regiunii."
        ));
        
        questions.add(new Question(
            "Care este numele teatrului național din Cluj-Napoca?",
            new String[]{"Teatrul Mihai Eminescu", "Teatrul Lucian Blaga", "Teatrul Național Maghiar", "Teatrul Radu Stanca"},
            1,
            R.drawable.teatru_cluj,
            "Teatrul Național \"Lucian Blaga\" din Cluj-Napoca este una dintre cele mai importante instituții culturale din Transilvania.",
            "Teatrul Național \"Lucian Blaga\" din Cluj-Napoca este una dintre cele mai importante instituții culturale din Transilvania."
        ));
        
        questions.add(new Question(
            "Ce pasaj montan face legătura între Transilvania și Țara Românească?",
            new String[]{"Pasul Tihuța", "Pasul Oituz", "Pasul Predeal", "Pasul Turnu Roșu"},
            2,
            R.drawable.predeal,
            "Pasul Predeal, situat la o altitudine de 1033 m, este unul dintre cele mai importante pasaje montane care leagă Transilvania de Țara Românească.",
            "Pasul Predeal, situat la o altitudine de 1033 m, este unul dintre cele mai importante pasaje montane care leagă Transilvania de Țara Românească."
        ));
        
        questions.add(new Question(
            "Ce universitate din Transilvania este una dintre cele mai vechi din România?",
            new String[]{"Universitatea din Alba Iulia", "Universitatea Babeș-Bolyai", "Universitatea din Oradea", "Universitatea de Medicină din Târgu Mureș"},
            1,
            R.drawable.babes_bolyai,
            "Universitatea Babeș-Bolyai din Cluj-Napoca, înființată în 1581, este una dintre cele mai vechi și prestigioase universități din România.",
            "Universitatea Babeș-Bolyai din Cluj-Napoca, înființată în 1581, este una dintre cele mai vechi și prestigioase universități din România."
        ));
        
        questions.add(new Question(
            "Ce fruct este specific regiunii Bistrița din Transilvania?",
            new String[]{"Cirese", "Mere", "Prune", "Struguri"},
            1,
            R.drawable.mere_bistrita,
            "Merele de Bistrița sunt renumite în întreaga Românie pentru gustul și calitatea lor, fiind o emblemă a regiunii.",
            "Merele de Bistrița sunt renumite în întreaga Românie pentru gustul și calitatea lor, fiind o emblemă a regiunii."
        ));
        
        questions.add(new Question(
            "Ce sat din Transilvania a fost declarat sat UNESCO?",
            new String[]{"Viscri", "Biertan", "Saschiz", "Toate variantele sunt corecte"},
            3,
            R.drawable.viscri,
            "Satele cu biserici fortificate din Transilvania: Viscri, Biertan, Saschiz și altele, sunt toate parte din patrimoniul mondial UNESCO.",
            "Satele cu biserici fortificate din Transilvania: Viscri, Biertan, Saschiz și altele, sunt toate parte din patrimoniul mondial UNESCO."
        ));
        
        questions.add(new Question(
            "Ce fenomen natural unic poate fi observat în Salina Turda?",
            new String[]{"Stalactite de sare", "Un lac subteran", "Corali fosili", "Un ecou care se repetă de 7 ori"},
            1,
            R.drawable.salina_turda,
            "Salina Turda adăpostește un lac subteran cu apă foarte sărată, format natural și unul dintre cele mai spectaculoase obiective turistice din Transilvania.",
            "Salina Turda adăpostește un lac subteran cu apă foarte sărată, format natural și unul dintre cele mai spectaculoase obiective turistice din Transilvania."
        ));
        
        questions.add(new Question(
            "Ce obiect din castelul Corvinilor se spune că ar avea puteri magice?",
            new String[]{"O armură medievală", "O fântână", "Un inel", "Un scut"},
            1,
            R.drawable.castelul_corvinilor,
            "Legenda spune că fântâna din castelul Corvinilor a fost săpată de trei prizonieri turci cărora li s-a promis libertatea după finalizare, dar promisiunea nu a fost respectată.",
            "Legenda spune că fântâna din castelul Corvinilor a fost săpată de trei prizonieri turci cărora li s-a promis libertatea după finalizare, dar promisiunea nu a fost respectată."
        ));
        
        questions.add(new Question(
            "Care este numele tradițional dat colindătorilor din Transilvania?",
            new String[]{"Urători", "Colindători", "Dițaladă", "Pitărași"},
            2,
            R.drawable.colindatori,
            "În unele zone din Transilvania, colindătorii sunt cunoscuți sub numele de \"dițaladă\", termen ce provine din tradiția locală.",
            "În unele zone din Transilvania, colindătorii sunt cunoscuți sub numele de \"dițaladă\", termen ce provine din tradiția locală."
        ));
        
        questions.add(new Question(
            "Ce fortificație din Transilvania a fost construită în secolul al XIII-lea de Cavalerii Teutoni?",
            new String[]{"Cetatea Neamțului", "Cetatea Râșnov", "Cetatea Feldioara", "Cetatea Făgăraș"},
            2,
            R.drawable.feldioara,
            "Cetatea Feldioara a fost construită de Cavalerii Teutoni în secolul al XIII-lea pentru a proteja granițele sud-estice ale Transilvaniei de invaziile cumanilor.",
            "Cetatea Feldioara a fost construită de Cavalerii Teutoni în secolul al XIII-lea pentru a proteja granițele sud-estice ale Transilvaniei de invaziile cumanilor."
        ));
        
        // Amestecăm întrebările pentru experiență diferită de fiecare dată
        shuffleQuestionsAndAnswers();
        
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
                answerButtons[i].setContentDescription(getString(R.string.answer_option_desc, (i+1), question.answers[i]));
                
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
                questionImage.setContentDescription(getString(R.string.question_image_desc));
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
            progressBar.setContentDescription(getString(R.string.progress_desc, currentQuestionIndex + 1, questions.size()));
        } else {
            showFinishButton();
        }
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
            answerButtons[selectedAnswerIndex].setContentDescription(getString(R.string.correct_answer_desc));
            
            // Animație de succes îmbunătățită cu spring effect
            float originalElevation = answerCards[selectedAnswerIndex].getElevation();
            
            // Use spring animation for more fluid movement
            answerCards[selectedAnswerIndex].animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .translationZ(12f)
                .setDuration(300)
                .withEndAction(() -> {
                    answerCards[selectedAnswerIndex].animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .translationZ(originalElevation)
                        .setDuration(200)
                        .start();
                })
                .start();
            
            // Efect de lumină pentru răspunsul corect
            answerCards[selectedAnswerIndex].setCardElevation(12f);
            answerCards[selectedAnswerIndex].setCardBackgroundColor(ContextCompat.getColorStateList(
                this, 
                R.color.transilvania_card_bg
            ).withAlpha(240));
            
            // Pulsare pentru efect vizual
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            pulse.setRepeatCount(1);
            answerCards[selectedAnswerIndex].startAnimation(pulse);
            
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
                
                // Afișăm un toast mai frumos pentru bonus
                Toast.makeText(this, "Bonus serie: +" + bonus + " puncte!", Toast.LENGTH_SHORT).show();
                
                // Animate streak text for visual feedback
                Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
                streakTextView.startAnimation(bounceAnim);
            }
            
            // Update score with animation
            Animation scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
            scoreTextView.startAnimation(scaleIn);
            
        } else {
            // Răspuns greșit - roșu
            answerCards[selectedAnswerIndex].setStrokeColor(ContextCompat.getColor(this, R.color.rom_wrong_answer));
            answerCards[selectedAnswerIndex].setStrokeWidth(4);
            answerButtons[selectedAnswerIndex].setContentDescription(getString(R.string.incorrect_answer_desc));
            
            // Efect de shake pentru răspunsul greșit
            Animation shakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake);
            answerCards[selectedAnswerIndex].startAnimation(shakeAnim);
            
            // Efect de fadeout pentru răspunsul greșit
            answerCards[selectedAnswerIndex].animate()
                .alpha(0.7f)
                .translationZ(-2f)
                .setDuration(300)
                .start();
            
            // Afișează răspunsul corect - verde
            answerCards[currentQuestion.correctAnswerIndex].setStrokeColor(ContextCompat.getColor(this, R.color.rom_correct_answer));
            answerCards[currentQuestion.correctAnswerIndex].setStrokeWidth(4);
            answerButtons[currentQuestion.correctAnswerIndex].setContentDescription(getString(R.string.correct_answer_desc));
            
            // Efect de highlight pentru răspunsul corect
            answerCards[currentQuestion.correctAnswerIndex].setCardElevation(12f);
            
            // Animate correct answer card for emphasis
            answerCards[currentQuestion.correctAnswerIndex].animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .translationZ(12f)
                .setDuration(300)
                .start();
            
            // Animație pentru a arăta răspunsul corect
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            answerCards[currentQuestion.correctAnswerIndex].startAnimation(pulse);
            
            // Reset streak
            streak = 0;
        }
        
        // Actualizează UI
        updateScore();
        updateStreak();
        
        // Așteaptă puțin înainte de a trece la următoarea întrebare
        new Handler().postDelayed(() -> moveToNextQuestion(), 1200);
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
        
        // Construim un dialog pentru a afișa rezultatele și opțiunile
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("Joc terminat!");
        dialogBuilder.setMessage(
            "Scor final: " + score + " puncte\n" +
            "Răspunsuri corecte: " + correctAnswers + " din " + questions.size() + "\n" +
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
            intent.putExtra("totalQuestions", questions.size());
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
        progressBar.setProgress(questions.size());
        progressBar.setContentDescription(getString(R.string.progress_desc, questions.size(), questions.size()));
        
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
            // Dacă a fost deja folosit, arătăm un mesaj
            Toast.makeText(this, "Ai folosit deja indiciul!", Toast.LENGTH_SHORT).show();
            return;
        }

        Question currentQuestion = questions.get(currentQuestionIndex);
        String hint = currentQuestion.getHint();

        if (hint != null && !hint.isEmpty()) {
            // Arătăm indiciul într-un dialog
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle("Indiciu");
            builder.setMessage(hint);
            builder.setIcon(R.drawable.ic_hint);
            builder.setPositiveButton("OK", null);
            builder.setBackground(getResources().getDrawable(R.drawable.dialog_rounded_bg));
            
            AlertDialog dialog = builder.create();
            dialog.show();

            // Dezactivăm butonul după folosire
            hintButton.setEnabled(false);
            hintButton.setAlpha(0.5f);
            isHintUsed = true;

            // Costuri pentru folosirea indiciului (opțional)
            // Poți scădea puncte sau adăuga o penalizare de timp
            if (score >= 5) {
                score -= 5; // Scădem 5 puncte pentru folosirea indiciului
                scoreTextView.setText(String.valueOf(score));
            }
        } else {
            // Dacă întrebarea nu are un indiciu definit
            Toast.makeText(this, "Nu există indiciu pentru această întrebare", Toast.LENGTH_SHORT).show();
        }
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
} 