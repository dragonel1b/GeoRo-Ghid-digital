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
import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.ArrayList;
import java.util.List;

public class NutritionTrackingFragment extends Fragment {

    private CulinaryViewModel viewModel;
    private TextView caloriesTextView;
    private RecyclerView goalsRecyclerView;
    private NutritionGoalAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nutrition_tracking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(CulinaryViewModel.class);

        // Initialize views
        caloriesTextView = view.findViewById(R.id.caloriesTextView);
        goalsRecyclerView = view.findViewById(R.id.goalsRecyclerView);

        // Setup RecyclerView
        goalsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NutritionGoalAdapter();
        goalsRecyclerView.setAdapter(adapter);

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getDailyCalories().observe(getViewLifecycleOwner(), calories -> {
            if (calories != null) {
                caloriesTextView.setText(getString(R.string.calories_format, calories));
            }
        });

        viewModel.getNutritionGoals().observe(getViewLifecycleOwner(), goals -> {
            if (goals != null) {
                adapter.setGoals(goals);
            }
        });
    }

    private class NutritionGoalAdapter extends RecyclerView.Adapter<NutritionGoalAdapter.GoalViewHolder> {
        private List<String> goals = new ArrayList<>();

        @NonNull
        @Override
        public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_nutrition_goal, parent, false);
            return new GoalViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
            String goal = goals.get(position);
            holder.bind(goal);
        }

        @Override
        public int getItemCount() {
            return goals.size();
        }

        public void setGoals(List<String> goals) {
            this.goals = goals;
            notifyDataSetChanged();
        }

        class GoalViewHolder extends RecyclerView.ViewHolder {
            private TextView titleView;
            private TextView descriptionView;
            private TextView statusView;
            private LinearProgressIndicator progressIndicator;
            private MaterialCardView cardView;

            GoalViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = (MaterialCardView) itemView;
                titleView = itemView.findViewById(R.id.goalTitle);
                descriptionView = itemView.findViewById(R.id.goalDescription);
                statusView = itemView.findViewById(R.id.goalStatus);
                progressIndicator = itemView.findViewById(R.id.goalProgress);
            }

            void bind(String goal) {
                // In a real app, goal would be a proper object with these fields
                switch (goal) {
                    case "calories":
                        titleView.setText("Daily Calories");
                        descriptionView.setText("Stay within your daily calorie target");
                        statusView.setText("1500/2000 cal");
                        progressIndicator.setProgress(75);
                        break;
                    case "protein":
                        titleView.setText("Protein Intake");
                        descriptionView.setText("Meet your daily protein requirements");
                        statusView.setText("45/60 g");
                        progressIndicator.setProgress(75);
                        break;
                    case "carbs":
                        titleView.setText("Carbohydrates");
                        descriptionView.setText("Balance your carb intake");
                        statusView.setText("200/250 g");
                        progressIndicator.setProgress(80);
                        break;
                    case "fats":
                        titleView.setText("Healthy Fats");
                        descriptionView.setText("Include essential fatty acids");
                        statusView.setText("40/55 g");
                        progressIndicator.setProgress(73);
                    break;
                }

                cardView.setOnClickListener(v -> {
                    // Show detailed nutrition tracking
                });
            }
        }
    }
}
