package com.example.myapplication.Joc1;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class RomAchievementsActivity extends AppCompatActivity {
    private RomGameState gameState;
    private AchievementManager achievementManager;
    private RecyclerView achievementsRecyclerView;
    private TabLayout categoryTabs;
    private ProgressBar achievementsProgress;
    private TextView achievementsProgressText;
    private TextView pointsIntelepteText;
    private TextView regionsExploredText;
    private TextView recipesDiscoveredText;
    private TextView quizCompletedText;
    private TextView totalPointsText;
    private MaterialCardView emptyStateCard;
    
    private List<AchievementManager.Achievement> allAchievements;
    private List<AchievementManager.Achievement> displayedAchievements;
    private ModernAchievementAdapter adapter;
    private AchievementManager.AchievementCategory currentCategory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_achievements);

        // Initialize managers
        gameState = RomGameState.getInstance();
        gameState.initialize(this);
        achievementManager = AchievementManager.getInstance(this);

        // Initialize views
        initializeViews();
        setupToolbar();
        setupTabs();
        setupAchievements();
        setupRecyclerView();
        updateProgress();
        updateStats();
        
        // Set up achievement unlock listener
        achievementManager.setAchievementUnlockedListener(achievement -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "🏆 Achievement Unlocked: " + achievement.getTitle(), Toast.LENGTH_LONG).show();
                refreshAchievements();
            });
        });
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
        
        // Add TabLayout for category filtering (if it exists in layout)
        categoryTabs = findViewById(R.id.categoryTabs);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("🏆 Achievement-uri");
        }
    }

    private void setupTabs() {
        if (categoryTabs != null) {
            categoryTabs.addTab(categoryTabs.newTab().setText("Toate"));
            categoryTabs.addTab(categoryTabs.newTab().setText("Transilvania"));
            categoryTabs.addTab(categoryTabs.newTab().setText("Dificultate"));
            categoryTabs.addTab(categoryTabs.newTab().setText("Moduri"));
            categoryTabs.addTab(categoryTabs.newTab().setText("Învățare"));
            categoryTabs.addTab(categoryTabs.newTab().setText("Speciale"));
            
            categoryTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    filterAchievementsByTab(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }
    }
    
    private void filterAchievementsByTab(int position) {
        switch (position) {
            case 0: // Toate
                displayedAchievements = new ArrayList<>(allAchievements);
                currentCategory = null;
                break;
            case 1: // Transilvania
                displayedAchievements = achievementManager.getAchievementsByCategory(
                    AchievementManager.AchievementCategory.TRANSILVANIA);
                currentCategory = AchievementManager.AchievementCategory.TRANSILVANIA;
                break;
            case 2: // Dificultate
                displayedAchievements = achievementManager.getAchievementsByCategory(
                    AchievementManager.AchievementCategory.DIFFICULTY);
                currentCategory = AchievementManager.AchievementCategory.DIFFICULTY;
                break;
            case 3: // Mod Joc
                displayedAchievements = achievementManager.getAchievementsByCategory(
                    AchievementManager.AchievementCategory.GAME_MODE);
                currentCategory = AchievementManager.AchievementCategory.GAME_MODE;
                break;
            case 4: // Învățare
                displayedAchievements = achievementManager.getAchievementsByCategory(
                    AchievementManager.AchievementCategory.LEARNING);
                currentCategory = AchievementManager.AchievementCategory.LEARNING;
                break;
            case 5: // Speciale
                displayedAchievements = achievementManager.getAchievementsByCategory(
                    AchievementManager.AchievementCategory.SPECIAL);
                currentCategory = AchievementManager.AchievementCategory.SPECIAL;
                break;
            default:
                displayedAchievements = new ArrayList<>(allAchievements);
                currentCategory = null;
                break;
        }
        
        adapter.updateAchievements(displayedAchievements);
        updateProgress();
    }

    private void setupAchievements() {
        // Get all achievements from AchievementManager
        allAchievements = achievementManager.getAllAchievements();
        displayedAchievements = new ArrayList<>(allAchievements);
    }
    
    private void refreshAchievements() {
        allAchievements = achievementManager.getAllAchievements();
        if (currentCategory == null) {
            displayedAchievements = new ArrayList<>(allAchievements);
        } else {
            displayedAchievements = achievementManager.getAchievementsByCategory(currentCategory);
        }
        adapter.updateAchievements(displayedAchievements);
        updateProgress();
    }

    private void setupRecyclerView() {
        achievementsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ModernAchievementAdapter(displayedAchievements);
        achievementsRecyclerView.setAdapter(adapter);
        
        updateEmptyState();
    }
    
    private void updateEmptyState() {
        boolean hasAchievements = displayedAchievements != null && !displayedAchievements.isEmpty();
        emptyStateCard.setVisibility(hasAchievements ? View.GONE : View.VISIBLE);
        achievementsRecyclerView.setVisibility(hasAchievements ? View.VISIBLE : View.GONE);
    }

    private void updateProgress() {
        int totalAchievements = displayedAchievements.size();
        int unlockedAchievements = 0;
        int totalPoints = 0;
        
        for (AchievementManager.Achievement achievement : displayedAchievements) {
            if (achievement.isUnlocked()) {
                unlockedAchievements++;
                totalPoints += achievement.getPointsReward();
            }
        }
        
        int progress = totalAchievements > 0 ? (unlockedAchievements * 100) / totalAchievements : 0;
        achievementsProgress.setProgress(progress);
        
        String categoryText = currentCategory != null ? getCategoryDisplayName(currentCategory) : "Toate";
        achievementsProgressText.setText(String.format(
                "%s: %d/%d deblocate (%d%%)", 
                categoryText, unlockedAchievements, totalAchievements, progress));
    }
    
    private void updateStats() {
        // Update stats based on game state
        pointsIntelepteText.setText(String.valueOf(gameState.getPuncteIntelepte()));
        regionsExploredText.setText(String.valueOf(gameState.getRegionsVisited()));
        recipesDiscoveredText.setText(String.valueOf(gameState.getRecipesDiscovered()));
        quizCompletedText.setText(String.valueOf(gameState.getQuizCompleted()));
        
        // Calculate total achievement points
        int totalAchievementPoints = 0;
        for (AchievementManager.Achievement achievement : achievementManager.getUnlockedAchievements()) {
            totalAchievementPoints += achievement.getPointsReward();
        }
        
        totalPointsText.setText(String.valueOf(totalAchievementPoints));
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
     * Modern adapter for displaying AchievementManager achievements
     */
    private class ModernAchievementAdapter extends RecyclerView.Adapter<ModernAchievementAdapter.AchievementViewHolder> {
        private List<AchievementManager.Achievement> achievements;

        public ModernAchievementAdapter(List<AchievementManager.Achievement> achievements) {
            this.achievements = new ArrayList<>(achievements);
        }
        
        public void updateAchievements(List<AchievementManager.Achievement> newAchievements) {
            this.achievements = new ArrayList<>(newAchievements);
            notifyDataSetChanged();
            updateEmptyState();
        }

        @NonNull
        @Override
        public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_achievement, parent, false);
            return new AchievementViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
            AchievementManager.Achievement achievement = achievements.get(position);
            
            holder.titleText.setText(achievement.getTitle());
            holder.descriptionText.setText(achievement.getDescription());
            
            // Show progress for achievements
            String progressText = String.format("%d/%d (%d%%)", 
                achievement.getCurrentProgress(), 
                achievement.getRequiredProgress(),
                (int) achievement.getProgressPercentage());
            holder.progressText.setText(progressText);
            holder.progressBar.setProgress((int) achievement.getProgressPercentage());
            
            // Set achievement icon
            holder.iconView.setImageResource(achievement.getIconResourceId());
            
            // Set card style based on achievement state
            if (achievement.isUnlocked()) {
                holder.card.setCardBackgroundColor(getResources().getColor(R.color.correct_answer, null));
                holder.unlockedBadge.setVisibility(View.VISIBLE);
                holder.titleText.setTextColor(getResources().getColor(android.R.color.white, null));
                holder.descriptionText.setTextColor(getResources().getColor(android.R.color.white, null));
                holder.progressText.setTextColor(getResources().getColor(android.R.color.white, null));
                
                // Add points indicator
                holder.progressText.setText(progressText + " • +" + achievement.getPointsReward() + " puncte");
            } else {
                holder.card.setCardBackgroundColor(getResources().getColor(R.color.white, null));
                holder.unlockedBadge.setVisibility(View.GONE);
                holder.titleText.setTextColor(getResources().getColor(android.R.color.black, null));
                holder.descriptionText.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
                holder.progressText.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
            }
            
            // Add category badge
            String categoryEmoji = getCategoryEmoji(achievement.getCategory());
            holder.titleText.setText(categoryEmoji + " " + achievement.getTitle());
        }

        @Override
        public int getItemCount() {
            return achievements.size();
        }
        
        private String getCategoryEmoji(AchievementManager.AchievementCategory category) {
            switch (category) {
                case TRANSILVANIA: return "🏰";
                case DIFFICULTY: return "⚡";
                case GAME_MODE: return "🎮";
                case LEARNING: return "📚";
                case SPECIAL: return "⭐";
                case QUIZ: return "❓";
                case EXPLORATION: return "🗺️";
                case QUEST: return "⚔️";
                case CITY: return "🏙️";
                default: return "🏆";
            }
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
    
    /**
     * Traduce numele categoriei în română pentru afișare
     */
    private String getCategoryDisplayName(AchievementManager.AchievementCategory category) {
        switch (category) {
            case TRANSILVANIA: return "Transilvania";
            case DIFFICULTY: return "Dificultate";
            case GAME_MODE: return "Moduri de Joc";
            case LEARNING: return "Învățare";
            case SPECIAL: return "Speciale";
            case QUIZ: return "Quiz-uri";
            case EXPLORATION: return "Explorare";
            case QUEST: return "Misiuni";
            case CITY: return "Orașe";
            default: return "General";
        }
    }
}
