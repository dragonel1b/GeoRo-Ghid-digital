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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.ArrayList;
import java.util.List;

public class WeeklyChallengesFragment extends Fragment {

    private CulinaryViewModel viewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialCardView featuredChallengeCard;
    private TextView featuredTitleView;
    private TextView featuredDescriptionView;
    private TextView featuredRewardView;
    private LinearProgressIndicator featuredProgressIndicator;
    private RecyclerView challengesRecyclerView;
    private ChallengeAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weekly_challenges, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(CulinaryViewModel.class);

        // Initialize views
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        featuredChallengeCard = view.findViewById(R.id.featuredChallengeCard);
        featuredTitleView = view.findViewById(R.id.featuredTitle);
        featuredDescriptionView = view.findViewById(R.id.featuredDescription);
        featuredRewardView = view.findViewById(R.id.featuredReward);
        featuredProgressIndicator = view.findViewById(R.id.featuredProgress);
        challengesRecyclerView = view.findViewById(R.id.challengesRecyclerView);

        // Setup RecyclerView
        challengesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChallengeAdapter();
        challengesRecyclerView.setAdapter(adapter);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshChallenges();
            swipeRefreshLayout.setRefreshing(false);
        });

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getFeaturedChallenge().observe(getViewLifecycleOwner(), challenge -> {
            if (challenge != null) {
                featuredTitleView.setText(challenge.getTitle());
                featuredDescriptionView.setText(challenge.getDescription());
                featuredRewardView.setText(challenge.getReward());
                int progress = (int) ((float) challenge.getCompletedSteps() / challenge.getRequiredSteps() * 100);
                featuredProgressIndicator.setProgress(progress);
            }
        });

        viewModel.getActiveChallenges().observe(getViewLifecycleOwner(), challenges -> {
            if (challenges != null) {
                adapter.setChallenges(challenges);
            }
        });
    }

    private class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {
        private List<String> challenges = new ArrayList<>();

        @NonNull
        @Override
        public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_challenge, parent, false);
            return new ChallengeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
            String challenge = challenges.get(position);
            holder.bind(challenge);
        }

        @Override
        public int getItemCount() {
            return challenges.size();
        }

        public void setChallenges(List<String> challenges) {
            this.challenges = challenges;
            notifyDataSetChanged();
        }

        class ChallengeViewHolder extends RecyclerView.ViewHolder {
            private TextView titleView;
            private MaterialCardView cardView;

            ChallengeViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = (MaterialCardView) itemView;
                titleView = itemView.findViewById(R.id.challengeTitle);
            }

            void bind(String challenge) {
                titleView.setText(challenge);
                cardView.setOnClickListener(v -> {
                    // Handle challenge selection
                });
            }
        }
    }
}
