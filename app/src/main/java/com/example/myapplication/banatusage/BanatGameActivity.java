package com.example.myapplication.banatusage;

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
import com.example.myapplication.RomApp.Banat;
import com.example.myapplication.RomApp.PointsManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BanatGameActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_banat_game);

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
        // Stilizăm butoanele pentru tema Banat
        for (int i = 0; i < answerButtons.length; i++) {
            MaterialButton button = answerButtons[i];
            MaterialCardView card = answerCards[i];
            
            // Activăm efectul de ripple pentru card
            card.setClickable(true);
            card.setFocusable(true);
            
            // Adaugă animație la apăsare
            card.setRippleColor(ContextCompat.getColorStateList(this, R.color.banat_primary_light));
        }
        
        // Adaugă efecte vizuale pentru butonul de finalizare
        finishButton.setRippleColor(ContextCompat.getColorStateList(this, R.color.banat_accent));
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
                    Animation pulse = AnimationUtils.loadAnimation(BanatGameActivity.this, R.anim.pulse);
                    timerTextView.startAnimation(pulse);
                    timerTextView.setTextColor(ContextCompat.getColor(BanatGameActivity.this, R.color.banat_accent));
                } else {
                    timerTextView.setTextColor(ContextCompat.getColor(BanatGameActivity.this, R.color.banat_text));
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

        isSkipUsed = true;
        skipQuestionButton.setEnabled(false);
        skipQuestionButton.setAlpha(0.5f);
        
        // Opțional, putem da un mic bonus pentru săritura
        score += 5;
        updateScore();
        
        moveToNextQuestion();
    }
    
    private void initializeQuestions() {
        questions = new ArrayList<>();
        
        // Întrebări despre Banat - istorie, geografie, cultură
        questions.add(new Question(
            "Care dintre următoarele orașe este considerat capitala Banatului?",
            new String[]{"Timișoara", "Reșița", "Lugoj", "Caransebeș"},
            0, // Timișoara
            R.drawable.timisoara,
            "Timișoara este considerat capitala istorică și culturală a Banatului."
        ));
        
        questions.add(new Question(
            "În ce an a început Revoluția Română din 1989 la Timișoara?",
            new String[]{"15 decembrie", "16 decembrie", "17 decembrie", "18 decembrie"},
            1, // 16 decembrie
            R.drawable.revolution,
            "Revoluția Română a început pe 16 decembrie 1989 la Timișoara, cu protestele împotriva evacuării pastorului reformat László Tőkés."
        ));
        
        questions.add(new Question(
            "Care dintre următoarele parcuri naționale se află în Banat?",
            new String[]{"Parcul Național Retezat", "Parcul Național Apuseni", "Parcul Național Cheile Nerei-Beușnița", "Parcul Național Piatra Craiului"},
            2, // Cheile Nerei-Beușnița
            R.drawable.cheile_nerei,
            "Parcul Național Cheile Nerei-Beușnița se află în sudul Banatului și este cunoscut pentru peisajele spectaculoase, cascade și lacuri."
        ));
        
        questions.add(new Question(
            "Care din următoarele minorități etnice NU este tradițional asociată cu regiunea Banatului?",
            new String[]{"Germanii (șvabii)", "Sârbii", "Secuii", "Maghiarii"},
            2, // Secuii
            R.drawable.ethnic_groups,
            "Secuii sunt o minoritate maghiară care locuiește tradițional în estul Transilvaniei, nu în Banat."
        ));
        
        questions.add(new Question(
            "Cum se numește stațiunea balneară din Banat cu o istorie de peste 2000 de ani?",
            new String[]{"Băile Felix", "Sovata", "Băile Herculane", "Vatra Dornei"},
            2, // Băile Herculane
            R.drawable.herculane,
            "Băile Herculane, numită în perioada romană Ad Aquas Herculi Sacras, are o istorie de peste 2000 de ani, fiind una dintre cele mai vechi stațiuni din Europa."
        ));
        
        questions.add(new Question(
            "Care dintre următoarele personalități s-a născut în Timișoara?",
            new String[]{"George Enescu", "Johnny Weissmuller", "Nicolae Grigorescu", "Gheorghe Hagi"},
            1, // Johnny Weissmuller
            R.drawable.johnny_weissmuller,
            "Johnny Weissmuller, cunoscut pentru rolul Tarzan în filmele clasice hollywoodiene și campion olimpic la înot, s-a născut în Timișoara în 1904."
        ));
        
        questions.add(new Question(
            "Cu ce țară se învecinează Banatul la vest?",
            new String[]{"Ungaria", "Serbia", "Bulgaria", "Ucraina"},
            1, // Serbia
            R.drawable.banat_map,
            "Banatul se învecinează cu Serbia la vest, unde o parte din regiunea istorică a Banatului (Banatul sârbesc) aparține astăzi Serbiei."
        ));
        
        questions.add(new Question(
            "Pentru ce eveniment cultural european a fost desemnată Timișoara în 2023?",
            new String[]{"Capitala Europeană a Filmului", "Capitala Europeană a Tineretului", "Capitala Europeană a Culturii", "Capitala Europeană a Sportului"},
            2, // Capitala Europeană a Culturii
            R.drawable.timisoara_culture,
            "Timișoara a fost desemnată Capitala Europeană a Culturii pentru anul 2023, fiind al doilea oraș din România care primește acest titlu, după Sibiu în 2007."
        ));
        
        questions.add(new Question(
            "Care este cea mai importantă activitate industrială tradițională a orașului Reșița?",
            new String[]{"Industria textilă", "Siderurgia (metalurgia)", "Industria lemnului", "Industria chimică"},
            1, // Siderurgia
            R.drawable.resita,
            "Reșița are o tradiție siderurgică de peste 250 de ani, fiind unul dintre cele mai vechi centre metalurgice din România."
        ));
        
        questions.add(new Question(
            "Ce râu important traversează orașul Timișoara?",
            new String[]{"Mureș", "Timiș", "Bega", "Someș"},
            2, // Bega
            R.drawable.bega_river,
            "Râul Bega traversează orașul Timișoara și este primul canal navigabil din România, construit în secolul al XVIII-lea."
        ));
        
        // Amestecăm întrebările pentru a fi prezentate în ordine aleatorie
        Collections.shuffle(questions);
        totalQuestions = questions.size();
        progressBar.setMax(totalQuestions);
    }
    
    private void displayQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            showFinishButton();
            return;
        }
        
        // Resetăm stilurile cardurilor
        resetCardStyles();
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        questionTextView.setText(currentQuestion.question);
        
        // Setăm imaginea întrebării
        if (currentQuestion.imageResourceId != 0) {
            questionImage.setImageResource(currentQuestion.imageResourceId);
            questionImage.setVisibility(View.VISIBLE);
        } else {
            questionImage.setVisibility(View.GONE);
        }
        
        // Setăm textul butoanelor
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i].setText(currentQuestion.answers[i]);
        }
        
        // Actualizăm progress bar
        progressBar.setProgress(currentQuestionIndex + 1);
        
        // Repornim cronometrul
        startTimer();
    }
    
    private void resetCardStyles() {
        for (MaterialCardView card : answerCards) {
            card.setStrokeColor(ContextCompat.getColor(this, R.color.banat_border));
            card.setStrokeWidth(2);
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.banat_card_background));
            card.setClickable(true);
            card.setAlpha(1.0f);
        }
        
        for (MaterialButton button : answerButtons) {
            button.setEnabled(true);
        }
    }
    
    private void checkAnswer(int selectedAnswerIndex, String selectedAnswer) {
        if (timer != null) {
            timer.cancel();
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        boolean isCorrect = selectedAnswerIndex == currentQuestion.correctAnswerIndex;
        
        // Arătăm feedback vizual pentru răspuns
        if (isCorrect) {
            // Răspuns corect - highlight verde
            answerCards[selectedAnswerIndex].setStrokeColor(ContextCompat.getColor(this, R.color.correct_answer));
            answerCards[selectedAnswerIndex].setStrokeWidth(4);
            
            // Adăugăm puncte și actualizăm streak
            score += POINTS_PER_CORRECT_ANSWER;
            streak++;
            correctAnswers++;
            
            if (streak > maxStreak) {
                maxStreak = streak;
            }
            
            // Bonus pentru streak
            if (streak >= STREAK_BONUS_THRESHOLD) {
                int streakBonus = 5 * (streak - STREAK_BONUS_THRESHOLD + 1);
                score += streakBonus;
                Toast.makeText(this, "Bonus serie: +" + streakBonus + " puncte!", Toast.LENGTH_SHORT).show();
            }
            
            // Arătăm faptul interesant
            Toast.makeText(this, "Corect! " + currentQuestion.fact, Toast.LENGTH_LONG).show();
            
        } else {
            // Răspuns greșit - highlight roșu și arătăm răspunsul corect
            answerCards[selectedAnswerIndex].setStrokeColor(ContextCompat.getColor(this, R.color.wrong_answer));
            answerCards[selectedAnswerIndex].setStrokeWidth(4);
            
            answerCards[currentQuestion.correctAnswerIndex].setStrokeColor(ContextCompat.getColor(this, R.color.correct_answer));
            answerCards[currentQuestion.correctAnswerIndex].setStrokeWidth(4);
            
            // Resetăm streak-ul
            streak = 0;
            
            // Arătăm faptul interesant
            Toast.makeText(this, "Incorect! " + currentQuestion.fact, Toast.LENGTH_LONG).show();
        }
        
        // Dezactivăm toate cardurile pentru a preveni răspunsuri multiple
        for (MaterialCardView card : answerCards) {
            card.setClickable(false);
        }
        
        updateScore();
        updateStreak();
        
        // Trecem la următoarea întrebare după o scurtă pauză
        new Handler().postDelayed(() -> moveToNextQuestion(), 2500);
    }
    
    private void moveToNextQuestion() {
        currentQuestionIndex++;
        displayQuestion();
    }
    
    private void updateScore() {
        scoreTextView.setText("Scor: " + score);
    }
    
    private void updateStreak() {
        streakTextView.setText("Serie: " + streak);
    }
    
    private String getAchievements() {
        StringBuilder achievements = new StringBuilder();
        
        // Adăugăm diferite realizări bazate pe performanță
        if (correctAnswers == totalQuestions) {
            achievements.append("🏆 Perfect! Ai răspuns corect la toate întrebările!\n");
        } else if (correctAnswers >= totalQuestions * 0.8) {
            achievements.append("🥇 Excelent! Ai un nivel ridicat de cunoștințe despre Banat!\n");
        } else if (correctAnswers >= totalQuestions * 0.6) {
            achievements.append("🥈 Bine! Ai cunoștințe solide despre Banat!\n");
        } else if (correctAnswers >= totalQuestions * 0.4) {
            achievements.append("🥉 Acceptabil! Ai câteva cunoștințe despre Banat!\n");
        } else {
            achievements.append("Încearcă din nou! Mai ai de învățat despre Banat!\n");
        }
        
        if (maxStreak >= 5) {
            achievements.append("🔥 Serie impresionantă: " + maxStreak + " răspunsuri corecte consecutive!\n");
        }
        
        if (!isFiftyFiftyUsed || !isSkipUsed) {
            achievements.append("💪 Ai terminat jocul fără să folosești toate ajutoarele!\n");
        }
        
        achievements.append("\nScor final: ").append(score).append(" puncte");
        return achievements.toString();
    }
    
    private void finishGame() {
        if (timer != null) {
            timer.cancel();
        }
        
        // Adăugăm punctele la total
        pointsManager.addPoints(this, "banat", score);
        
        // Lansăm BanatGameOverActivity cu rezultatele
        Intent intent = new Intent(this, BanatGameOverActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("totalQuestions", totalQuestions);
        intent.putExtra("correctAnswers", correctAnswers);
        intent.putExtra("maxStreak", maxStreak);
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
    
    private void showFinishButton() {
        questionTextView.setText("Ai terminat toate întrebările!");
        questionImage.setVisibility(View.GONE);
        
        for (MaterialCardView card : answerCards) {
            card.setVisibility(View.GONE);
        }
        
        finishButton.setVisibility(View.VISIBLE);
        timerTextView.setVisibility(View.GONE);
        
        if (timer != null) {
            timer.cancel();
        }
    }
} 