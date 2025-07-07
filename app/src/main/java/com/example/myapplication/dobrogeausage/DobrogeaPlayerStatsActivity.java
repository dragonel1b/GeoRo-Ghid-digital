package com.example.myapplication.dobrogeausage;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import com.example.myapplication.R;
import com.example.myapplication.Joc1.AchievementManager;
import com.example.myapplication.utils.SyncManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.Date;

/**
 * Activitate pentru afișarea statisticilor jucătorului în quiz-ul Dobrogea
 */
public class DobrogeaPlayerStatsActivity extends AppCompatActivity {
    private static final String TAG = "DobrogeaPlayerStats";
    private static final String REGION = "dobrogea";
    
    private TextView totalQuizzesTextView;
    private TextView totalScoreTextView;
    private TextView averageScoreTextView;
    private TextView bestScoreTextView;
    private TextView totalCorrectAnswersTextView;
    private TextView totalQuestionsTextView;
    private TextView accuracyTextView;
    private TextView totalTimeTextView;
    private TextView maxStreakTextView;
    private TextView achievementsTextView;
    private TextView lastPlayedTextView;
    private CardView statsCard;
    private ConstraintLayout mainLayout;
    
    private AchievementManager achievementManager;
    private SyncManager syncManager;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dobrogea_player_stats);
        
        initializeViews();
        initializeManagers();
        loadPlayerStats();
    }
    
    /**
     * Inițializează view-urile
     */
    private void initializeViews() {
        totalQuizzesTextView = findViewById(R.id.total_quizzes_text);
        totalScoreTextView = findViewById(R.id.total_score_text);
        averageScoreTextView = findViewById(R.id.average_score_text);
        bestScoreTextView = findViewById(R.id.best_score_text);
        totalCorrectAnswersTextView = findViewById(R.id.total_correct_answers_text);
        totalQuestionsTextView = findViewById(R.id.total_questions_text);
        accuracyTextView = findViewById(R.id.accuracy_text);
        totalTimeTextView = findViewById(R.id.total_time_text);
        maxStreakTextView = findViewById(R.id.max_streak_text);
        achievementsTextView = findViewById(R.id.achievements_text);
        lastPlayedTextView = findViewById(R.id.last_played_text);
        statsCard = findViewById(R.id.stats_card);
        mainLayout = findViewById(R.id.main_constraint_layout);
        
        // Aplică tema maritimă Dobrogea
        applyDobrogeaTheme();
    }
    
    /**
     * Aplică tema maritimă Dobrogea
     */
    private void applyDobrogeaTheme() {
        mainLayout.setBackgroundResource(R.drawable.dobrogea_bg_maritime);
        statsCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dobrogea_card_bg));
        
        // Setează culorile textului
        int textColor = ContextCompat.getColor(this, R.color.dobrogea_text);
        totalQuizzesTextView.setTextColor(textColor);
        totalScoreTextView.setTextColor(textColor);
        averageScoreTextView.setTextColor(textColor);
        bestScoreTextView.setTextColor(textColor);
        totalCorrectAnswersTextView.setTextColor(textColor);
        totalQuestionsTextView.setTextColor(textColor);
        accuracyTextView.setTextColor(textColor);
        totalTimeTextView.setTextColor(textColor);
        maxStreakTextView.setTextColor(textColor);
        achievementsTextView.setTextColor(textColor);
        lastPlayedTextView.setTextColor(textColor);
    }
    
    /**
     * Inițializează managerii
     */
    private void initializeManagers() {
        achievementManager = AchievementManager.getInstance(this);
        syncManager = SyncManager.getInstance(this);
        db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Încarcă statisticile jucătorului
     */
    private void loadPlayerStats() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            showNoUserMessage();
            return;
        }
        
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // Încarcă statisticile din Firestore
        db.collection("quiz_results")
            .whereEqualTo("userId", userId)
            .whereEqualTo("region", REGION)
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                calculateAndDisplayStats(querySnapshot);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading player stats", e);
                showErrorLoadingStats();
            });
    }
    
    /**
     * Calculează și afișează statisticile
     */
    private void calculateAndDisplayStats(com.google.firebase.firestore.QuerySnapshot querySnapshot) {
        int totalQuizzes = querySnapshot.size();
        int totalScore = 0;
        int totalCorrectAnswers = 0;
        int totalQuestions = 0;
        long totalTime = 0;
        int maxStreak = 0;
        int bestScore = 0;
        Date lastPlayed = null;
        
        for (com.google.firebase.firestore.QueryDocumentSnapshot document : querySnapshot) {
            Long score = document.getLong("score");
            Long correctAnswers = document.getLong("correctAnswers");
            Long questions = document.getLong("totalQuestions");
            Long time = document.getLong("totalTime");
            Long streak = document.getLong("maxStreak");
            Date completedAt = document.getDate("completedAt");
            
            if (score != null) {
                totalScore += score;
                if (score > bestScore) {
                    bestScore = score.intValue();
                }
            }
            
            if (correctAnswers != null) {
                totalCorrectAnswers += correctAnswers;
            }
            
            if (questions != null) {
                totalQuestions += questions;
            }
            
            if (time != null) {
                totalTime += time;
            }
            
            if (streak != null && streak > maxStreak) {
                maxStreak = streak.intValue();
            }
            
            if (completedAt != null && (lastPlayed == null || completedAt.after(lastPlayed))) {
                lastPlayed = completedAt;
            }
        }
        
        // Calculează media
        double averageScore = totalQuizzes > 0 ? (double) totalScore / totalQuizzes : 0;
        double accuracy = totalQuestions > 0 ? (double) totalCorrectAnswers / totalQuestions * 100 : 0;
        
        // Afișează statisticile
        displayStats(totalQuizzes, totalScore, averageScore, bestScore, 
                    totalCorrectAnswers, totalQuestions, accuracy, 
                    totalTime, maxStreak, lastPlayed);
    }
    
    /**
     * Afișează statisticile în UI
     */
    private void displayStats(int totalQuizzes, int totalScore, double averageScore, int bestScore,
                            int totalCorrectAnswers, int totalQuestions, double accuracy,
                            long totalTime, int maxStreak, Date lastPlayed) {
        
        totalQuizzesTextView.setText("🌊 " + totalQuizzes + " quiz-uri jucate");
        totalScoreTextView.setText("📊 " + totalScore + " puncte totale");
        averageScoreTextView.setText("📈 " + String.format("%.1f", averageScore) + " puncte în medie");
        bestScoreTextView.setText("🏆 " + bestScore + " cel mai bun scor");
        totalCorrectAnswersTextView.setText("✅ " + totalCorrectAnswers + " răspunsuri corecte");
        totalQuestionsTextView.setText("❓ " + totalQuestions + " întrebări răspunse");
        accuracyTextView.setText("🎯 " + String.format("%.1f", accuracy) + "% acuratețe");
        totalTimeTextView.setText("⏱️ " + formatTime(totalTime));
        maxStreakTextView.setText("🔥 " + maxStreak + " serie maximă");
        
        if (lastPlayed != null) {
            lastPlayedTextView.setText("📅 Ultima dată: " + formatDate(lastPlayed));
        } else {
            lastPlayedTextView.setText("📅 Nu ai jucat încă");
        }
        
        // Afișează realizările
        displayAchievements();
    }
    
    /**
     * Formatează timpul în format citibil
     */
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
    
    /**
     * Formatează data în format citibil
     */
    private String formatDate(Date date) {
        return new java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(date);
    }
    
    /**
     * Afișează realizările jucătorului
     */
    private void displayAchievements() {
        // Aici poți adăuga logica pentru afișarea realizărilor specifice Dobrogea
        String achievements = "🏅 Navigator Dobrogea\n" +
                            "⚓ Cunoștințe Maritime\n" +
                            "🌊 Explorator Delta";
        
        achievementsTextView.setText(achievements);
    }
    
    /**
     * Afișează mesaj pentru utilizator neautentificat
     */
    private void showNoUserMessage() {
        totalQuizzesTextView.setText("🌊 Trebuie să fii autentificat pentru a vedea statisticile");
        totalScoreTextView.setVisibility(View.GONE);
        averageScoreTextView.setVisibility(View.GONE);
        bestScoreTextView.setVisibility(View.GONE);
        totalCorrectAnswersTextView.setVisibility(View.GONE);
        totalQuestionsTextView.setVisibility(View.GONE);
        accuracyTextView.setVisibility(View.GONE);
        totalTimeTextView.setVisibility(View.GONE);
        maxStreakTextView.setVisibility(View.GONE);
        achievementsTextView.setVisibility(View.GONE);
        lastPlayedTextView.setVisibility(View.GONE);
    }
    
    /**
     * Afișează mesaj de eroare la încărcarea statisticilor
     */
    private void showErrorLoadingStats() {
        totalQuizzesTextView.setText("❌ Eroare la încărcarea statisticilor");
        totalScoreTextView.setVisibility(View.GONE);
        averageScoreTextView.setVisibility(View.GONE);
        bestScoreTextView.setVisibility(View.GONE);
        totalCorrectAnswersTextView.setVisibility(View.GONE);
        totalQuestionsTextView.setVisibility(View.GONE);
        accuracyTextView.setVisibility(View.GONE);
        totalTimeTextView.setVisibility(View.GONE);
        maxStreakTextView.setVisibility(View.GONE);
        achievementsTextView.setVisibility(View.GONE);
        lastPlayedTextView.setVisibility(View.GONE);
    }
} 