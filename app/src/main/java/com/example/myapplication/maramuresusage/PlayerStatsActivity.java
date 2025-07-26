package com.example.myapplication.maramuresusage;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import java.util.Map;

public class PlayerStatsActivity extends AppCompatActivity {
    private PlayerProgressTracker progressTracker;
    private TextView textStats, textStreak, textRecommendations;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_stats);
        progressTracker = new PlayerProgressTracker(this);
        textStats = findViewById(R.id.textStats);
        textStreak = findViewById(R.id.textStreak);
        textRecommendations = findViewById(R.id.textRecommendations);
        showStats();
    }
    private void showStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Jocuri totale: ").append(progressTracker.getTotalGames()).append("\n");
        stats.append("Întrebări totale: ").append(progressTracker.getTotalQuestions()).append("\n");
        stats.append("Răspunsuri corecte: ").append(progressTracker.getCorrectAnswers()).append("\n");
        stats.append("Acuratețe globală: ").append(Math.round(progressTracker.getOverallAccuracy() * 100)).append("%\n");
        stats.append("Timp total jucat: ").append(progressTracker.getTotalTimeSpent() / 1000).append(" secunde\n");
        textStats.setText(stats.toString());
        textStreak.setText("Streak curent: " + progressTracker.getCurrentStreak() + " zile\nCel mai bun streak: " + progressTracker.getBestStreak() + " zile");
        StringBuilder recs = new StringBuilder();
        for (String rec : progressTracker.getLearningRecommendations()) {
            recs.append("• ").append(rec).append("\n");
        }
        textRecommendations.setText(recs.toString());
    }
} 