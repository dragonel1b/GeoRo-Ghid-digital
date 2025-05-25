package com.example.myapplication.moldovausage;

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
import com.example.myapplication.RomApp.Moldova;
import com.example.myapplication.RomApp.PointsManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MoldovaGameActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_moldova_game);

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
        // Stilizăm butoanele pentru tema Moldova
        for (int i = 0; i < answerButtons.length; i++) {
            MaterialButton button = answerButtons[i];
            MaterialCardView card = answerCards[i];
            
            // Activăm efectul de ripple pentru card
            card.setClickable(true);
            card.setFocusable(true);
            
            // Adaugă animație la apăsare
            card.setRippleColor(ContextCompat.getColorStateList(this, R.color.moldova_primary_light));
        }
        
        // Adaugă efecte vizuale pentru butonul de finalizare
        finishButton.setRippleColor(ContextCompat.getColorStateList(this, R.color.moldova_accent));
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
                    Animation pulse = AnimationUtils.loadAnimation(MoldovaGameActivity.this, R.anim.pulse);
                    timerTextView.startAnimation(pulse);
                    timerTextView.setTextColor(ContextCompat.getColor(MoldovaGameActivity.this, R.color.moldova_accent));
                } else {
                    timerTextView.setTextColor(ContextCompat.getColor(MoldovaGameActivity.this, R.color.moldova_text));
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
        // Resetăm streak-ul
        streak = 0;
        updateStreak();
        
        // Afișăm răspunsul corect
        highlightCorrectAnswer();
    }

    private void useFiftyFifty() {
        if (!isFiftyFiftyUsed) {
            // Găsim indexul răspunsului corect pentru întrebarea curentă
            int correctIndex = questions.get(currentQuestionIndex).correctAnswerIndex;
            
            // Generăm două indexuri aleatorii pentru răspunsuri incorecte
            List<Integer> incorrectIndexes = new ArrayList<>();
            for (int i = 0; i < answerButtons.length; i++) {
                if (i != correctIndex) {
                    incorrectIndexes.add(i);
                }
            }
            
            // Amestecăm și selectăm două răspunsuri incorecte pentru a le elimina
            Collections.shuffle(incorrectIndexes);
            for (int i = 0; i < 2; i++) {
                int indexToRemove = incorrectIndexes.get(i);
                answerButtons[indexToRemove].setText("");
                answerCards[indexToRemove].setClickable(false);
                answerCards[indexToRemove].setAlpha(0.3f);
            }
            
            // Marcăm varianta de ajutor ca fiind utilizată
            isFiftyFiftyUsed = true;
            fiftyFiftyButton.setAlpha(0.5f);
            fiftyFiftyButton.setClickable(false);
            
            Toast.makeText(this, "50:50 a fost folosit!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ai folosit deja varianta 50:50!", Toast.LENGTH_SHORT).show();
        }
    }

    private void skipQuestion() {
        if (!isSkipUsed) {
            // Marcăm skip ca fiind utilizat
            isSkipUsed = true;
            skipQuestionButton.setAlpha(0.5f);
            skipQuestionButton.setClickable(false);
            
            // Trecem la următoarea întrebare
            moveToNextQuestion();
            Toast.makeText(this, "Întrebare sărita!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ai folosit deja opțiunea de a sări o întrebare!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeQuestions() {
        questions = new ArrayList<>();
        
        // Întrebări despre Moldova
        questions.add(new Question(
            "Care este capitala istorică a Moldovei?",
            new String[]{"Iași", "Bacău", "Suceava", "Chișinău"},
            0,
            R.drawable.moldova_iasi,
            "Iași a fost capitala Principatului Moldovei între 1564 și 1859."
        ));
        
        questions.add(new Question(
            "Care este cel mai important râu care traversează Moldova?",
            new String[]{"Prut", "Siret", "Olt", "Mureș"},
            0,
            R.drawable.moldova_prut,
            "Râul Prut are o lungime de 953 km și formează granița naturală între România și Republica Moldova."
        ));
        
        questions.add(new Question(
            "Ce mănăstire din Moldova este inclusă în patrimoniul UNESCO?",
            new String[]{"Mănăstirea Voroneț", "Mănăstirea Putna", "Mănăstirea Cozia", "Mănăstirea Bistrița"},
            0,
            R.drawable.moldova_voronet,
            "Mănăstirea Voroneț, cunoscută pentru 'albastrul de Voroneț', a fost construită în 1488 de Ștefan cel Mare."
        ));
        
        questions.add(new Question(
            "Cine a fost domnitorul cel mai important al Moldovei?",
            new String[]{"Ștefan cel Mare", "Mihai Viteazul", "Mircea cel Bătrân", "Alexandru Ioan Cuza"},
            0,
            R.drawable.moldova_stefan,
            "Ștefan cel Mare a domnit între 1457 și 1504 și este considerat un erou național în România și Republica Moldova."
        ));
        
        questions.add(new Question(
            "Care este cel mai mare oraș din Moldova românească în prezent?",
            new String[]{"Iași", "Galați", "Bacău", "Suceava"},
            0,
            R.drawable.moldova_iasi_city,
            "Iașiul este al doilea oraș ca mărime din România după București, cu o populație de aproximativ 380.000 locuitori."
        ));
        
        questions.add(new Question(
            "Care personalitate culturală importantă s-a născut la Humulești, Moldova?",
            new String[]{"Ion Creangă", "Mihai Eminescu", "Vasile Alecsandri", "George Enescu"},
            0,
            R.drawable.moldova_creanga,
            "Ion Creangă (1837-1889) este unul dintre cei mai importanți scriitori români, cunoscut pentru 'Amintiri din copilărie'."
        ));
        
        questions.add(new Question(
            "Ce monument istoric important se află la Ruginoasa, în Moldova?",
            new String[]{"Palatul Cuza", "Cetatea Neamț", "Cetatea Sucevei", "Casa Pogor"},
            0,
            R.drawable.moldova_palat_cuza,
            "Palatul de la Ruginoasa a fost reședința lui Alexandru Ioan Cuza, primul domnitor al Principatelor Unite."
        ));
        
        questions.add(new Question(
            "Care este cel mai vechi oraș din Moldova?",
            new String[]{"Suceava", "Roman", "Iași", "Bârlad"},
            0,
            R.drawable.moldova_suceava,
            "Suceava datează din secolul al XIV-lea și a fost capitala Moldovei între 1388 și 1564."
        ));
        
        questions.add(new Question(
            "Care este cea mai importantă universitate din Moldova?",
            new String[]{"Universitatea 'Alexandru Ioan Cuza' din Iași", "Universitatea din Galați", "Universitatea din Bacău", "Universitatea 'Ștefan cel Mare' din Suceava"},
            0,
            R.drawable.moldova_universitate,
            "Universitatea 'Alexandru Ioan Cuza' din Iași, fondată în 1860, este cea mai veche universitate din România."
        ));
        
        questions.add(new Question(
            "Care localitate este cunoscută ca 'Poarta Moldovei'?",
            new String[]{"Târgu Neamț", "Focșani", "Pașcani", "Tescani"},
            1,
            R.drawable.moldova_focsani,
            "Focșani este cunoscut ca 'Poarta Moldovei' datorită poziției sale geografice, la granița dintre Moldova și Muntenia."
        ));
        
        // Amestecăm întrebările
        Collections.shuffle(questions);
        
        // Setăm numărul total de întrebări
        totalQuestions = questions.size();
        
        // Actualizăm ProgressBar
        progressBar.setMax(totalQuestions);
        progressBar.setProgress(0);
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question currentQuestion = questions.get(currentQuestionIndex);
            
            // Afișăm textul întrebării
            questionTextView.setText(currentQuestion.question);
            
            // Afișăm imaginea asociată întrebării
            if (currentQuestion.imageResourceId != 0) {
                questionImage.setVisibility(View.VISIBLE);
                questionImage.setImageResource(currentQuestion.imageResourceId);
            } else {
                questionImage.setVisibility(View.GONE);
            }
            
            // Resetăm stilurile cardurilor
            resetCardStyles();
            
            // Afișăm răspunsurile
            for (int i = 0; i < answerButtons.length; i++) {
                answerButtons[i].setText(currentQuestion.answers[i]);
                answerCards[i].setClickable(true);
                answerCards[i].setAlpha(1.0f);
            }
            
            // Actualizăm progress bar
            progressBar.setProgress(currentQuestionIndex);
            
            // Repornim timer-ul
            startTimer();
        } else {
            // Dacă am terminat toate întrebările, afișăm rezultatul final
            finishGame();
        }
    }

    private void resetCardStyles() {
        for (int i = 0; i < answerCards.length; i++) {
            MaterialCardView card = answerCards[i];
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
            MaterialButton button = answerButtons[i];
            button.setTextColor(ContextCompat.getColor(this, R.color.black));
            card.setStrokeWidth(0);
        }
    }

    private void checkAnswer(int selectedAnswerIndex, String selectedAnswer) {
        // Oprim timer-ul
        if (timer != null) {
            timer.cancel();
        }
        
        // Dezactivăm toate cardurile pentru a preveni multiple clickuri
        for (MaterialCardView card : answerCards) {
            card.setClickable(false);
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        int correctAnswerIndex = currentQuestion.correctAnswerIndex;
        
        if (selectedAnswerIndex == correctAnswerIndex) {
            // Răspuns corect
            answerCards[selectedAnswerIndex].setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct_answer));
            answerButtons[selectedAnswerIndex].setTextColor(ContextCompat.getColor(this, R.color.white));
            
            // Adăugăm puncte și actualizăm streak-ul
            score += POINTS_PER_CORRECT_ANSWER;
            streak++;
            maxStreak = Math.max(maxStreak, streak);
            correctAnswers++;
            
            // Bonus pentru streak
            if (streak >= STREAK_BONUS_THRESHOLD) {
                int bonus = (streak - STREAK_BONUS_THRESHOLD + 1) * 5;
                score += bonus;
                Toast.makeText(this, "Bonus streak: +" + bonus + " puncte!", Toast.LENGTH_SHORT).show();
            }
            
            // Afișăm detalii despre răspuns
            Toast.makeText(this, "Corect! " + currentQuestion.fact, Toast.LENGTH_LONG).show();
        } else {
            // Răspuns greșit
            answerCards[selectedAnswerIndex].setCardBackgroundColor(ContextCompat.getColor(this, R.color.wrong_answer));
            answerButtons[selectedAnswerIndex].setTextColor(ContextCompat.getColor(this, R.color.white));
            
            // Evidențiem răspunsul corect
            highlightCorrectAnswer();
            
            // Reset streak
            streak = 0;
            
            // Afișăm detalii despre răspunsul corect
            Toast.makeText(this, "Incorect! " + currentQuestion.fact, Toast.LENGTH_LONG).show();
        }
        
        // Actualizăm scorurile afișate
        updateScore();
        updateStreak();
        
        // Planificăm trecerea la următoarea întrebare după o scurtă pauză
        new Handler().postDelayed(this::moveToNextQuestion, 3000);
    }

    private void highlightCorrectAnswer() {
        int correctIndex = questions.get(currentQuestionIndex).correctAnswerIndex;
        answerCards[correctIndex].setCardBackgroundColor(ContextCompat.getColor(this, R.color.correct_answer));
        answerButtons[correctIndex].setTextColor(ContextCompat.getColor(this, R.color.white));
        answerCards[correctIndex].setStrokeWidth(5);
        answerCards[correctIndex].setStrokeColor(ContextCompat.getColor(this, R.color.correct_answer_border));
    }

    private void moveToNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            displayQuestion();
        } else {
            showFinishButton();
        }
    }

    private void updateScore() {
        scoreTextView.setText(String.valueOf(score));
    }

    private void updateStreak() {
        streakTextView.setText(String.valueOf(streak));
    }

    private String getAchievements() {
        StringBuilder achievements = new StringBuilder();
        
        // Adăugăm realizările în funcție de performanță
        if (correctAnswers == totalQuestions) {
            achievements.append("🏆 Perfecțiune! Ai răspuns corect la toate întrebările!\n\n");
        } else if ((double) correctAnswers / totalQuestions >= 0.8) {
            achievements.append("🥇 Expert în Moldova! Cunoști foarte bine această regiune!\n\n");
        } else if ((double) correctAnswers / totalQuestions >= 0.6) {
            achievements.append("🥈 Bun cunoscător al Moldovei! Ai făcut față cu brio testului!\n\n");
        } else if ((double) correctAnswers / totalQuestions >= 0.4) {
            achievements.append("🥉 Cunoștințe decente despre Moldova!\n\n");
        } else {
            achievements.append("Mai ai de învățat despre Moldova!\n\n");
        }
        
        // Adăugăm statistici
        achievements.append(String.format("Răspunsuri corecte: %d/%d (%.1f%%)\n", 
            correctAnswers, totalQuestions, (double) correctAnswers / totalQuestions * 100));
        
        if (maxStreak > 1) {
            achievements.append(String.format("Cel mai mare streak: %d răspunsuri consecutive\n", maxStreak));
        }
        
        achievements.append(String.format("Scor final: %d puncte\n", score));
        
        // Adăugăm bonus final
        if (correctAnswers > totalQuestions / 2) {
            achievements.append(String.format("\nBONUS: +%d puncte!", BONUS_POINTS));
            score += BONUS_POINTS;
        }
        
        return achievements.toString();
    }

    private void finishGame() {
        if (timer != null) {
            timer.cancel();
        }
        
        // Afișăm dialog cu rezultatul
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Joc Terminat!")
               .setMessage(getAchievements())
               .setPositiveButton("Continuă", (dialog, which) -> {
                   // Adăugăm scorul în pointsManager
                   pointsManager.addPoints(this, "moldova", score);
                   
                   // Trimitem înapoi rezultatul la activitatea Moldova
                   Intent resultIntent = new Intent();
                   resultIntent.putExtra("GAME_SCORE", score);
                   setResult(RESULT_OK, resultIntent);
                   
                   // Închidem activitatea
                   finish();
               })
               .setCancelable(false)
               .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Anulăm timer-ul pentru a preveni memory leaks
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void showFinishButton() {
        finishButton.setVisibility(View.VISIBLE);
        progressBar.setProgress(progressBar.getMax());
        
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        finishButton.startAnimation(fadeIn);
    }
} 