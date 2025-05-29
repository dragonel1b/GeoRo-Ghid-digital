package com.example.myapplication.crisanausage;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.PointsManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CrisanaGameActivity extends AppCompatActivity {
    private TextView questionText;
    private TextView questionNumberText;
    private TextView scoreText;
    private TextView timerText;
    private Button option1Button;
    private Button option2Button;
    private Button option3Button;
    private Button option4Button;
    private Button nextButton;
    private CardView feedbackCard;
    private TextView feedbackText;
    private ProgressBar timeProgressBar;

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean answered = false;
    private CountDownTimer timer;
    private static final long QUESTION_TIME_MS = 20000; // 20 seconds per question
    private long timeRemainingMs = QUESTION_TIME_MS;
    private PointsManager pointsManager;
    private int correctAnswers = 0;
    private int currentStreak = 0;
    private int maxStreak = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crisana_game);

        // Initialize views
        questionText = findViewById(R.id.questionText);
        questionNumberText = findViewById(R.id.questionNumber);
        scoreText = findViewById(R.id.scoreText);
        timerText = findViewById(R.id.timerText);
        option1Button = findViewById(R.id.option1Button);
        option2Button = findViewById(R.id.option2Button);
        option3Button = findViewById(R.id.option3Button);
        option4Button = findViewById(R.id.option4Button);
        nextButton = findViewById(R.id.nextButton);
        feedbackCard = findViewById(R.id.feedbackCard);
        feedbackText = findViewById(R.id.feedbackText);
        timeProgressBar = findViewById(R.id.timeProgressBar);

        // Initialize points manager
        pointsManager = PointsManager.getInstance(this);

        // Set up questions
        initQuestions();

        // Set up UI for first question
        displayQuestion();

        // Set up click listeners
        option1Button.setOnClickListener(v -> checkAnswer(0));
        option2Button.setOnClickListener(v -> checkAnswer(1));
        option3Button.setOnClickListener(v -> checkAnswer(2));
        option4Button.setOnClickListener(v -> checkAnswer(3));

        nextButton.setOnClickListener(v -> {
            if (currentQuestionIndex < questions.size() - 1) {
                currentQuestionIndex++;
                displayQuestion();
            } else {
                // Game completed, show final screen
                finishGame();
            }
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> showExitConfirmation());
    }

    private void initQuestions() {
        questions = new ArrayList<>();

        // Add questions about Crisana
        questions.add(new Question(
            "Cum se numește cel mai mare oraș din Crișana?",
            new String[]{"Arad", "Oradea", "Salonta", "Beiuș"},
            1,
            "Oradea este cel mai mare oraș din Crișana și unul dintre cele mai importante centre culturale și economice din vestul României."
        ));

        questions.add(new Question(
            "Care stațiune balneară din Crișana este cunoscută pentru nuferii tropicali care cresc în aer liber și iarna?",
            new String[]{"Băile Felix", "Băile 1 Mai", "Moneasa", "Băile Herculane"},
            0,
            "Băile Felix este renumită pentru nuferii tropicali care cresc în apele termale și supraviețuiesc în aer liber chiar și în timpul iernii."
        ));

        questions.add(new Question(
            "Care stil arhitectural este predominant în centrul istoric al Oradiei?",
            new String[]{"Baroc", "Bizantin", "Art Nouveau/Secession", "Neoclasic"},
            2,
            "Oradea are una dintre cele mai bogate colecții de clădiri Art Nouveau (Secession) din Europa, multe construite la începutul secolului XX."
        ));

        questions.add(new Question(
            "Ce râu important trece prin Crișana și îi dă numele?",
            new String[]{"Mureș", "Someș", "Criș", "Timiș"},
            2,
            "Crișana este traversată de râurile Crișul Alb, Crișul Negru și Crișul Repede, care au dat numele regiunii."
        ));

        questions.add(new Question(
            "Care peșteră din Munții Apuseni este cunoscută pentru fosilele de urs de cavernă?",
            new String[]{"Peștera Urșilor", "Peștera Scărișoara", "Peștera Vântului", "Peștera Meziad"},
            0,
            "Peștera Urșilor, descoperită în 1975, este faimoasă pentru fosilele de urs de cavernă vechi de aproximativ 15.000 de ani."
        ));

        questions.add(new Question(
            "Ce minorități etnice importante trăiesc în Crișana?",
            new String[]{"Sași și secui", "Maghiari și germani", "Ucraineni și ruși", "Bulgari și sârbi"},
            1,
            "Crișana are o compoziție etnică diversă, cu comunități importante de maghiari și germani, ceea ce îi conferă un bogat patrimoniu cultural."
        ));

        questions.add(new Question(
            "Ce specialitate gastronomică este asociată cu regiunea Crișana?",
            new String[]{"Sarmale în foi de viță", "Cozonac secuiesc (kürtőskalács)", "Mămăligă cu brânză", "Ciorbă de burtă"},
            1,
            "Cozonacul secuiesc (kürtőskalács) este un desert tradițional specific regiunii Crișana, datorită influenței maghiare."
        ));

        questions.add(new Question(
            "În ce an a fost construită Cetatea Oradea în forma sa actuală?",
            new String[]{"1692", "1735", "1802", "1872"},
            0,
            "Cetatea Oradea, în forma sa actuală de tip Vauban (stea cu cinci colțuri), a fost construită de habsburgi în 1692."
        ));

        questions.add(new Question(
            "Ce personalitate culturală importantă s-a născut în Oradea și a fondat revista literară „Familia",
            new String[]{"George Coșbuc", "Octavian Goga", "Iosif Vulcan", "Ioan Slavici"},
            2,
            "Iosif Vulcan, născut la Holod, lângă Oradea, a fondat revista „Familia în 1865, publicație care a debutat poeți precum Mihai Eminescu."
        ));

        questions.add(new Question(
            "Care cetate medievală din Crișana este considerată una dintre cele mai bine conservate din Transilvania?",
            new String[]{"Cetatea Șiria", "Cetatea Oradea", "Cetatea Aradului", "Cetatea Șoimoș"},
            1,
            "Cetatea Oradea este una dintre cele mai bine conservate cetăți medievale din Transilvania, având o istorie de peste 900 de ani."
        ));

        // Shuffle questions for variety
        Collections.shuffle(questions);
    }

    private void displayQuestion() {
        // Reset state for new question
        answered = false;
        timeRemainingMs = QUESTION_TIME_MS;
        feedbackCard.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        
        // Enable all option buttons
        option1Button.setEnabled(true);
        option2Button.setEnabled(true);
        option3Button.setEnabled(true);
        option4Button.setEnabled(true);
        
        // Reset button backgrounds
        option1Button.setBackgroundResource(R.drawable.button_default);
        option2Button.setBackgroundResource(R.drawable.button_default);
        option3Button.setBackgroundResource(R.drawable.button_default);
        option4Button.setBackgroundResource(R.drawable.button_default);

        // Set question data
        Question currentQuestion = questions.get(currentQuestionIndex);
        questionText.setText(currentQuestion.getQuestion());
        questionNumberText.setText(String.format(Locale.getDefault(), "Întrebarea %d/%d", 
                currentQuestionIndex + 1, questions.size()));
        scoreText.setText(String.format(Locale.getDefault(), "Scor: %d", score));
        
        // Set options
        option1Button.setText(currentQuestion.getOptions()[0]);
        option2Button.setText(currentQuestion.getOptions()[1]);
        option3Button.setText(currentQuestion.getOptions()[2]);
        option4Button.setText(currentQuestion.getOptions()[3]);
        
        // Start countdown timer
        startTimer();
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        
        timeProgressBar.setMax((int) QUESTION_TIME_MS);
        timeProgressBar.setProgress((int) QUESTION_TIME_MS);
        
        timer = new CountDownTimer(QUESTION_TIME_MS, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemainingMs = millisUntilFinished;
                timeProgressBar.setProgress((int) millisUntilFinished);
                timerText.setText(String.format(Locale.getDefault(), "%d s", millisUntilFinished / 1000 + 1));
            }

            @Override
            public void onFinish() {
                if (!answered) {
                    timeRemainingMs = 0;
                    timeProgressBar.setProgress(0);
                    timerText.setText("0 s");
                    showCorrectAnswer();
                }
            }
        }.start();
    }

    private void checkAnswer(int selectedOptionIndex) {
        if (answered) return;
        
        answered = true;
        if (timer != null) {
            timer.cancel();
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        int correctAnswerIndex = currentQuestion.getCorrectAnswerIndex();
        
        // Disable all buttons
        option1Button.setEnabled(false);
        option2Button.setEnabled(false);
        option3Button.setEnabled(false);
        option4Button.setEnabled(false);
        
        // Calculate points based on remaining time
        int pointsForQuestion = 0;
        
        // Show feedback
        if (selectedOptionIndex == correctAnswerIndex) {
            // Correct answer
            correctAnswers++;
            currentStreak++;
            if (currentStreak > maxStreak) {
                maxStreak = currentStreak;
            }
            
            pointsForQuestion = (int) (10 + (timeRemainingMs / 1000)); // Base 10 points + 1 point per second remaining
            score += pointsForQuestion;
            scoreText.setText(String.format(Locale.getDefault(), "Scor: %d", score));
            
            // Highlight correct answer
            highlightButton(selectedOptionIndex, true);
            
            feedbackText.setText(String.format(Locale.getDefault(), 
                    "Corect! +%d puncte\n\n%s", 
                    pointsForQuestion, 
                    currentQuestion.getExplanation()));
        } else {
            // Wrong answer
            currentStreak = 0;
            
            // Highlight selected answer as wrong
            highlightButton(selectedOptionIndex, false);
            
            // Highlight correct answer
            highlightButton(correctAnswerIndex, true);
            
            feedbackText.setText(String.format(Locale.getDefault(), 
                    "Incorect!\n\n%s", 
                    currentQuestion.getExplanation()));
        }
        
        feedbackCard.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
    }

    private void showCorrectAnswer() {
        if (answered) return;
        
        answered = true;
        
        // Disable all buttons
        option1Button.setEnabled(false);
        option2Button.setEnabled(false);
        option3Button.setEnabled(false);
        option4Button.setEnabled(false);
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        int correctAnswerIndex = currentQuestion.getCorrectAnswerIndex();
        
        // Highlight correct answer
        highlightButton(correctAnswerIndex, true);
        
        feedbackText.setText(String.format(Locale.getDefault(), 
                "Timpul a expirat!\n\n%s", 
                currentQuestion.getExplanation()));
        
        feedbackCard.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
    }

    private void highlightButton(int buttonIndex, boolean correct) {
        Button button;
        switch (buttonIndex) {
            case 0:
                button = option1Button;
                break;
            case 1:
                button = option2Button;
                break;
            case 2:
                button = option3Button;
                break;
            case 3:
                button = option4Button;
                break;
            default:
                return;
        }
        
        if (correct) {
            button.setBackgroundResource(R.drawable.button_correct);
        } else {
            button.setBackgroundResource(R.drawable.button_incorrect);
        }
    }

    private void showGameSummary() {
        // Calculate final score percentage
        int maxScore = questions.size() * 30; // Maximum possible score (assuming 30 points per question)
        int percentage = (int) ((float) score / maxScore * 100);
        
        // Award points to player based on performance
        int pointsAwarded = score / 5; // Convert game score to points
        pointsManager.addPoints(this, "crisana", pointsAwarded);
        
        // Create and show summary dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Joc terminat!");
        builder.setMessage(String.format(Locale.getDefault(),
                "Scorul tău final: %d puncte\nPerformanță: %d%%\n\nAi primit %d puncte în aplicație!",
                score, percentage, pointsAwarded));
        
        builder.setPositiveButton("Înapoi la pagina regiunii", (dialog, which) -> {
            // Return to the Crisana activity with score
            Intent intent = new Intent();
            intent.putExtra("GAME_SCORE", pointsAwarded);
            setResult(RESULT_OK, intent);
            finish();
        });
        
        builder.setCancelable(false);
        builder.show();
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Ieșire")
                .setMessage("Ești sigur că vrei să ieși? Progresul va fi pierdut.")
                .setPositiveButton("Da", (dialog, which) -> finish())
                .setNegativeButton("Nu", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!answered && currentQuestionIndex < questions.size()) {
            startTimer();
        }
    }

    @Override
    public void onBackPressed() {
        showExitConfirmation();
    }

    // Question class to store question data
    private static class Question {
        private final String question;
        private final String[] options;
        private final int correctAnswerIndex;
        private final String explanation;

        Question(String question, String[] options, int correctAnswerIndex, String explanation) {
            this.question = question;
            this.options = options;
            this.correctAnswerIndex = correctAnswerIndex;
            this.explanation = explanation;
        }

        String getQuestion() {
            return question;
        }

        String[] getOptions() {
            return options;
        }

        int getCorrectAnswerIndex() {
            return correctAnswerIndex;
        }

        String getExplanation() {
            return explanation;
        }
    }

    private void finishGame() {
        if (timer != null) {
            timer.cancel();
        }

        // Adăugăm punctele în contul utilizatorului
        pointsManager.addPoints(this, "crisana", score);
        
        Intent intent = new Intent(this, CrisanaGameOverActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("correctAnswers", correctAnswers);
        intent.putExtra("totalQuestions", questions.size());
        intent.putExtra("maxStreak", maxStreak);
        intent.putExtra("ACHIEVEMENTS", getAchievements());
        startActivity(intent);
        finish();
    }

    private String getAchievements() {
        List<String> achievements = new ArrayList<>();
        
        // Calculate accuracy
        int percentage = questions.size() > 0 ? (correctAnswers * 100) / questions.size() : 0;
        
        // Achievement for accuracy
        if (percentage == 100) {
            achievements.add("Maestru al Crișanei (Toate răspunsurile corecte)");
        } else if (percentage >= 80) {
            achievements.add("Expert al Crișanei (" + correctAnswers + " din " + questions.size() + " corecte)");
        } else if (percentage >= 60) {
            achievements.add("Bun cunoscător al Crișanei (" + correctAnswers + " din " + questions.size() + " corecte)");
        }
        
        // Achievement for streak
        if (maxStreak >= 5) {
            achievements.add("Neînvins! Serie de " + maxStreak + " răspunsuri corecte consecutive");
        } else if (maxStreak >= 3) {
            achievements.add("Cărturar! Serie de " + maxStreak + " răspunsuri corecte consecutive");
        }
        
        // Achievement for score
        if (score >= questions.size() * 25) {
            achievements.add("Scor excepțional: " + score + " puncte");
        }
        
        if (achievements.isEmpty()) {
            return "Continuă să explorezi Crișana pentru a obține realizări!";
        }
        
        StringBuilder result = new StringBuilder();
        for (String achievement : achievements) {
            result.append("• ").append(achievement).append("\n");
        }
        
        return result.toString();
    }
} 