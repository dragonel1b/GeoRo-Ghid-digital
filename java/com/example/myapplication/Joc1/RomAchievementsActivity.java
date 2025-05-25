package com.example.myapplication.Joc1;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RomAchievementsActivity extends AppCompatActivity {
    private RomGameState gameState;
    private RecyclerView achievementsRecyclerView;
    private AchievementsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_achievements);

        gameState = RomGameState.getInstance();
        gameState.initialize(this);

        initializeViews();
        loadAchievements();
    }

    private void initializeViews() {
        achievementsRecyclerView = findViewById(R.id.achievementsRecyclerView);
        achievementsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new AchievementsAdapter();
        achievementsRecyclerView.setAdapter(adapter);

        TextView unlockedCount = findViewById(R.id.unlockedCount);
        TextView totalPoints = findViewById(R.id.totalPoints);

        Set<String> unlockedAchievements = gameState.getUnlockedAchievements();
        unlockedCount.setText(getString(R.string.rom_achievements_progress,
                unlockedAchievements.size(), RomGameState.ACHIEVEMENTS.size()));
        totalPoints.setText(getString(R.string.rom_culture_points_label,
                gameState.getPuncteIntelepte()));

        achievementsRecyclerView.startAnimation(
                AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
    }

    private void loadAchievements() {
        List<AchievementItem> items = new ArrayList<>();
        Set<String> unlockedAchievements = gameState.getUnlockedAchievements();

        for (Map.Entry<String, RomGameState.Achievement> entry : RomGameState.ACHIEVEMENTS.entrySet()) {
            RomGameState.Achievement achievement = entry.getValue();
            boolean isUnlocked = unlockedAchievements.contains(entry.getKey());
            items.add(new AchievementItem(achievement, isUnlocked));
        }

        adapter.setItems(items);
    }

    public void goBack(View view) {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private static class AchievementItem {
        final RomGameState.Achievement achievement;
        final boolean isUnlocked;

        AchievementItem(RomGameState.Achievement achievement, boolean isUnlocked) {
            this.achievement = achievement;
            this.isUnlocked = isUnlocked;
        }
    }

    private class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.ViewHolder> {
        private List<AchievementItem> items = new ArrayList<>();

        void setItems(List<AchievementItem> newItems) {
            items = newItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater()
                    .inflate(R.layout.item_rom_achievement, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView titleText;
            private final TextView descriptionText;
            private final TextView pointsText;
            private final ImageView statusIcon;
            private final MaterialCardView cardView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = (MaterialCardView) itemView;
                titleText = cardView.findViewById(R.id.achievementTitle);
                descriptionText = cardView.findViewById(R.id.achievementDescription);
                pointsText = cardView.findViewById(R.id.achievementPoints);
                statusIcon = cardView.findViewById(R.id.achievementStatus);
            }

            void bind(AchievementItem item) {
                titleText.setText(item.achievement.title);
                descriptionText.setText(item.achievement.description);
                pointsText.setText(getString(R.string.rom_points_required,
                        item.achievement.pointsRequired));

                int strokeColor = item.isUnlocked ?
                        R.color.rom_achievement_unlocked :
                        R.color.rom_achievement_locked;
                int iconRes = item.isUnlocked ?
                        R.drawable.ic_check :
                        R.drawable.ic_lock;

                cardView.setStrokeColor(getResources().getColor(strokeColor, getTheme()));
                statusIcon.setImageResource(iconRes);
                statusIcon.setColorFilter(getResources().getColor(strokeColor, getTheme()));

                cardView.setContentDescription(getString(
                        item.isUnlocked ?
                                R.string.rom_achievement_unlocked :
                                R.string.rom_points_required,
                        item.isUnlocked ?
                                item.achievement.title :
                                item.achievement.pointsRequired
                ));

                cardView.startAnimation(
                        AnimationUtils.loadAnimation(cardView.getContext(),
                                android.R.anim.slide_in_left));
            }
        }
    }
}
