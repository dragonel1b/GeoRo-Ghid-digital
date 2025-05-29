package com.example.myapplication.Joc1;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class RomAchievementsActivity extends AppCompatActivity {
    private RomGameState gameState;
    private RecyclerView achievementsRecyclerView;
    private ProgressBar achievementsProgress;
    private TextView achievementsProgressText;
    private TextView pointsIntelepteText;
    private TextView regionsExploredText;
    private TextView recipesDiscoveredText;
    private TextView quizCompletedText;
    private TextView totalPointsText;
    private MaterialCardView emptyStateCard;
    
    private List<Achievement> achievements;
    private AchievementAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_achievements);

        // Initialize game state
        gameState = RomGameState.getInstance();
        gameState.initialize(this);

        // Initialize views
        initializeViews();
        setupToolbar();
        setupAchievements();
        setupRecyclerView();
        updateProgress();
        updateStats();
    }

    private void initializeViews() {
        achievementsRecyclerView = findViewById(R.id.achievementsRecyclerView);
        achievementsProgress = findViewById(R.id.achievementsProgress);
        achievementsProgressText = findViewById(R.id.achievementsProgressText);
        pointsIntelepteText = findViewById(R.id.pointsIntelepteText);
        regionsExploredText = findViewById(R.id.regionsExploredText);
        recipesDiscoveredText = findViewById(R.id.recipesDiscoveredText);
        quizCompletedText = findViewById(R.id.quizCompletedText);
        totalPointsText = findViewById(R.id.totalPointsText);
        emptyStateCard = findViewById(R.id.emptyStateCard);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.rom_achievements_title);
        }
    }

    private void setupAchievements() {
        achievements = new ArrayList<>();
        
        // Add achievements based on the game state constants
        achievements.add(new Achievement(
                RomGameState.ACHIEVEMENT_EXPLORATOR_REGIONAL,
                "Explorator Regional",
                "Vizitează cel puțin 3 regiuni diferite ale României",
                "Regiuni vizitate: %d/3",
                gameState.getRegionsVisited(),
                3,
                R.drawable.ic_explore
        ));
        
        achievements.add(new Achievement(
                RomGameState.ACHIEVEMENT_BUCATAR_REGAL,
                "Bucătar Regal",
                "Descoperă cel puțin 5 rețete tradiționale românești",
                "Rețete descoperite: %d/5",
                gameState.getRecipesDiscovered(),
                5,
                R.drawable.ic_food
        ));
        
        achievements.add(new Achievement(
                RomGameState.ACHIEVEMENT_ETNOGRAF_AMATOR,
                "Etnograf Amator",
                "Colectează 10 obiecte tradiționale în mini-jocuri",
                "Obiecte colectate: %d/10",
                gameState.getCollectedItems(),
                10,
                R.drawable.ic_collection
        ));
        
        achievements.add(new Achievement(
                RomGameState.ACHIEVEMENT_ISTORIC_CUNOSCATOR,
                "Istoric Cunoscător",
                "Răspunde corect la 15 întrebări despre istoria României",
                "Răspunsuri corecte: %d/15",
                gameState.getCorrectQuizAnswers(),
                15,
                R.drawable.ic_history
        ));
        
        achievements.add(new Achievement(
                RomGameState.ACHIEVEMENT_CALATOR_PASIONAT,
                "Călător Pasionat",
                "Călătorește cel puțin 500 km pe harta României",
                "Kilometri parcurși: %d/500",
                gameState.getTravelDistance(),
                500,
                R.drawable.ic_travel
        ));
        
        achievements.add(new Achievement(
                RomGameState.ACHIEVEMENT_FOLCLORIST_EXPERIMENTAT,
                "Folclorist Experimentat",
                "Participă la 3 jocuri tradiționale românești",
                "Jocuri încercate: %d/3",
                gameState.getPlayedGames(),
                3,
                R.drawable.ic_game
        ));
        
        achievements.add(new Achievement(
                RomGameState.ACHIEVEMENT_MASTER_CULTURAL,
                "Maestru Cultural",
                "Acumulează 1000 de Puncte Înțelepte",
                "Puncte acumulate: %d/1000",
                gameState.getPuncteIntelepte(),
                1000,
                R.drawable.ic_culture
        ));
        
        // New cooking streak achievements
        achievements.add(new Achievement(
                RomGameState.ACHIEVEMENT_BUCATAR_DEDICAT,
                "Bucătar Dedicat",
                "Gătește 3 zile consecutiv",
                "Zile consecutive: %d/3",
                gameState.getCookingStreak(),
                3,
                R.drawable.ic_food
        ));
        
        achievements.add(new Achievement(
                RomGameState.ACHIEVEMENT_BUCATAR_PERSEVERENT,
                "Bucătar Perseverent",
                "Gătește 7 zile consecutiv",
                "Zile consecutive: %d/7",
                gameState.getCookingStreak(),
                7,
                R.drawable.ic_food
        ));
        
        achievements.add(new Achievement(
                RomGameState.ACHIEVEMENT_BUCATAR_MAESTRU,
                "Bucătar Maestru",
                "Gătește 14 zile consecutiv",
                "Zile consecutive: %d/14",
                gameState.getCookingStreak(),
                14,
                R.drawable.ic_food
        ));
        
        // New community achievements
        achievements.add(new Achievement(
                RomGameState.ACHIEVEMENT_COLECTIONAR_RETETE,
                "Colecționar de Rețete",
                "Descoperă 10 rețete tradiționale",
                "Rețete descoperite: %d/10",
                gameState.getRecipesDiscovered(),
                10,
                R.drawable.ic_food
        ));
        
        achievements.add(new Achievement(
                RomGameState.ACHIEVEMENT_AMBASADOR_CULINAR,
                "Ambasador Culinar",
                "Împărtășește 5 rețete cu comunitatea",
                "Rețete împărtășite: %d/5",
                gameState.getRecipesShared(),
                5,
                R.drawable.ic_share
        ));
        
        achievements.add(new Achievement(
                RomGameState.ACHIEVEMENT_CRITIC_GASTRONOMIC,
                "Critic Gastronomic",
                "Scrie 10 recenzii pentru rețete",
                "Recenzii scrise: %d/10",
                gameState.getReviewsWritten(),
                10,
                R.drawable.ic_rate
        ));
    }

    private void setupRecyclerView() {
        achievementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AchievementAdapter(achievements);
        achievementsRecyclerView.setAdapter(adapter);
        
        // Show empty state if no achievements are unlocked
        boolean hasUnlockedAchievements = false;
        for (Achievement achievement : achievements) {
            if (achievement.isUnlocked()) {
                hasUnlockedAchievements = true;
                break;
            }
        }
        
        emptyStateCard.setVisibility(hasUnlockedAchievements ? View.GONE : View.VISIBLE);
        achievementsRecyclerView.setVisibility(hasUnlockedAchievements ? View.VISIBLE : View.GONE);
    }

    private void updateProgress() {
        int totalAchievements = achievements.size();
        int unlockedAchievements = 0;
        
        for (Achievement achievement : achievements) {
            if (achievement.isUnlocked()) {
                unlockedAchievements++;
            }
        }
        
        int progress = totalAchievements > 0 ? (unlockedAchievements * 100) / totalAchievements : 0;
        achievementsProgress.setProgress(progress);
        
        achievementsProgressText.setText(getString(
                R.string.rom_achievements_progress, 
                unlockedAchievements, 
                totalAchievements));
    }
    
    private void updateStats() {
        // Update stats based on game state
        pointsIntelepteText.setText(String.valueOf(gameState.getPuncteIntelepte()));
        regionsExploredText.setText(String.valueOf(gameState.getRegionsVisited()));
        recipesDiscoveredText.setText(String.valueOf(gameState.getRecipesDiscovered()));
        quizCompletedText.setText(String.valueOf(gameState.getQuizCompleted()));
        
        // Calculate total score based on all metrics
        int totalScore = gameState.getPuncteIntelepte() 
                + (gameState.getRegionsVisited() * 50)
                + (gameState.getRecipesDiscovered() * 25)
                + (gameState.getCorrectQuizAnswers() * 10)
                + (gameState.getCollectedItems() * 5);
        
        totalPointsText.setText(String.valueOf(totalScore));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Inner class representing an Achievement
     */
    public static class Achievement {
        private final String id;
        private final String title;
        private final String description;
        private final String progressFormat;
        private final int currentProgress;
        private final int targetProgress;
        private final int iconResource;

        public Achievement(String id, String title, String description, String progressFormat,
                          int currentProgress, int targetProgress, int iconResource) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.progressFormat = progressFormat;
            this.currentProgress = currentProgress;
            this.targetProgress = targetProgress;
            this.iconResource = iconResource;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getProgressText() {
            return String.format(progressFormat, currentProgress);
        }

        public int getCurrentProgress() {
            return currentProgress;
        }

        public int getTargetProgress() {
            return targetProgress;
        }

        public int getProgressPercentage() {
            return (currentProgress * 100) / targetProgress;
        }

        public boolean isUnlocked() {
            return currentProgress >= targetProgress;
        }

        public int getIconResource() {
            return iconResource;
        }
    }

    /**
     * Adapter for displaying achievements in RecyclerView
     */
    private class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {
        private final List<Achievement> achievements;

        public AchievementAdapter(List<Achievement> achievements) {
            this.achievements = achievements;
        }

        @NonNull
        @Override
        public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_achievement, parent, false);
            return new AchievementViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
            Achievement achievement = achievements.get(position);
            
            holder.titleText.setText(achievement.getTitle());
            holder.descriptionText.setText(achievement.getDescription());
            holder.progressText.setText(achievement.getProgressText());
            holder.progressBar.setProgress(achievement.getProgressPercentage());
            
            // Set achievement icon
            holder.iconView.setImageResource(achievement.getIconResource());
            
            // Set card style based on achievement state
            if (achievement.isUnlocked()) {
                holder.card.setCardBackgroundColor(getResources().getColor(R.color.rom_success_light));
                holder.unlockedBadge.setVisibility(View.VISIBLE);
            } else {
                holder.card.setCardBackgroundColor(getResources().getColor(R.color.rom_card_background));
                holder.unlockedBadge.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return achievements.size();
        }

        class AchievementViewHolder extends RecyclerView.ViewHolder {
            final MaterialCardView card;
            final ImageView iconView;
            final TextView titleText;
            final TextView descriptionText;
            final TextView progressText;
            final ProgressBar progressBar;
            final ImageView unlockedBadge;

            AchievementViewHolder(View itemView) {
                super(itemView);
                card = (MaterialCardView) itemView;
                iconView = itemView.findViewById(R.id.achievementIcon);
                titleText = itemView.findViewById(R.id.achievementTitle);
                descriptionText = itemView.findViewById(R.id.achievementDescription);
                progressText = itemView.findViewById(R.id.achievementProgressText);
                progressBar = itemView.findViewById(R.id.achievementProgressBar);
                unlockedBadge = itemView.findViewById(R.id.achievementUnlockedBadge);
            }
        }
    }
}
