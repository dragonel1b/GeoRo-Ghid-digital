package com.example.myapplication.Joc1.Culinary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class AchievementsFragment extends Fragment {

    private CulinaryViewModel viewModel;
    private TextView pointsTextView;
    private TextView motivationTextView;
    private RecyclerView badgesRecyclerView;
    private BadgeAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_achievements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(CulinaryViewModel.class);

        // Initialize views
        pointsTextView = view.findViewById(R.id.pointsTextView);
        motivationTextView = view.findViewById(R.id.motivationTextView);
        badgesRecyclerView = view.findViewById(R.id.badgesRecyclerView);

        // Setup RecyclerView
        badgesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new BadgeAdapter();
        badgesRecyclerView.setAdapter(adapter);

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getUserPoints().observe(getViewLifecycleOwner(), points -> {
            if (points != null) {
                pointsTextView.setText(getString(R.string.total_points, points));
            }
        });

        viewModel.getUserBadges().observe(getViewLifecycleOwner(), badges -> {
            if (badges != null) {
                adapter.setBadges(badges);
                updateMotivationText(badges.size());
            }
        });
    }

    private void updateMotivationText(int badgeCount) {
        if (badgeCount == 0) {
            motivationTextView.setText(R.string.keep_cooking);
        } else {
            motivationTextView.setVisibility(View.GONE);
        }
    }

    private class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {
        private List<String> badges = new ArrayList<>();

        @NonNull
        @Override
        public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_badge, parent, false);
            return new BadgeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
            String badge = badges.get(position);
            holder.bind(badge);
        }

        @Override
        public int getItemCount() {
            return badges.size();
        }

        public void setBadges(List<String> badges) {
            this.badges = badges;
            notifyDataSetChanged();
        }

        class BadgeViewHolder extends RecyclerView.ViewHolder {
            private TextView badgeNameView;
            private TextView badgeDescriptionView;
            private MaterialCardView cardView;

            BadgeViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = (MaterialCardView) itemView;
                badgeNameView = itemView.findViewById(R.id.badgeName);
                badgeDescriptionView = itemView.findViewById(R.id.badgeDescription);
            }

            void bind(String badge) {
                // In a real app, badge would be a proper object with these fields
                badgeNameView.setText(getBadgeName(badge));
                badgeDescriptionView.setText(getBadgeDescription(badge));

                cardView.setOnClickListener(v -> {
                    // Show badge details or animation
                });
            }

            private String getBadgeName(String badge) {
                switch (badge) {
                    case "master_chef_badge":
                        return "Master Chef";
                    case "healthy_eating_badge":
                        return "Health Guru";
                    case "quick_meals_badge":
                        return "Speed Cooker";
                    default:
                        return "Mystery Badge";
                }
            }

            private String getBadgeDescription(String badge) {
                switch (badge) {
                    case "master_chef_badge":
                        return "Complete 50 recipes";
                    case "healthy_eating_badge":
                        return "Track nutrition for 30 days";
                    case "quick_meals_badge":
                        return "Prepare 20 meals under 30 minutes";
                    default:
                        return "Keep cooking to unlock";
                }
            }
        }
    }
}
