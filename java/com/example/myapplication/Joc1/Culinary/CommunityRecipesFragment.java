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
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class CommunityRecipesFragment extends Fragment {

    private CulinaryViewModel viewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recipesRecyclerView;
    private CommunityRecipeAdapter adapter;
    private TextView emptyView;
    private FloatingActionButton addRecipeFab;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(CulinaryViewModel.class);

        // Initialize views
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recipesRecyclerView = view.findViewById(R.id.recipesRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        addRecipeFab = view.findViewById(R.id.addRecipeFab);

        // Setup RecyclerView
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommunityRecipeAdapter();
        recipesRecyclerView.setAdapter(adapter);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshCommunityRecipes();
            swipeRefreshLayout.setRefreshing(false);
        });

        // Setup FAB
        addRecipeFab.setOnClickListener(v -> {
            // Launch ShareMyRecipeActivity
            ShareMyRecipeActivity.start(requireContext());
        });

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getCommunityRecipes().observe(getViewLifecycleOwner(), recipes -> {
            if (recipes != null && !recipes.isEmpty()) {
                adapter.setRecipes(recipes);
                emptyView.setVisibility(View.GONE);
                recipesRecyclerView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.VISIBLE);
                recipesRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    private class CommunityRecipeAdapter extends RecyclerView.Adapter<CommunityRecipeAdapter.RecipeViewHolder> {
        private List<String> recipes = new ArrayList<>();

        @NonNull
        @Override
        public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_community_recipe, parent, false);
            return new RecipeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
            String recipe = recipes.get(position);
            holder.bind(recipe);
        }

        @Override
        public int getItemCount() {
            return recipes.size();
        }

        public void setRecipes(List<String> recipes) {
            this.recipes = recipes;
            notifyDataSetChanged();
        }

        class RecipeViewHolder extends RecyclerView.ViewHolder {
            private TextView titleView;
            private TextView descriptionView;
            private TextView authorView;
            private Chip likesChip;
            private Chip commentsChip;
            private MaterialCardView cardView;

            RecipeViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = (MaterialCardView) itemView;
                titleView = itemView.findViewById(R.id.recipeTitle);
                descriptionView = itemView.findViewById(R.id.recipeDescription);
                authorView = itemView.findViewById(R.id.recipeAuthor);
                likesChip = itemView.findViewById(R.id.recipeLikes);
                commentsChip = itemView.findViewById(R.id.recipeComments);
            }

            void bind(String recipe) {
                // In a real app, recipe would be a proper object with these fields
                titleView.setText(recipe);
                descriptionView.setText("A delicious traditional recipe");
                authorView.setText("By Community Member");
                likesChip.setText(getString(R.string.likes_count, 0));
                commentsChip.setText(getString(R.string.comments_count, 0));

                cardView.setOnClickListener(v -> {
                    // Open recipe detail view
                    RecipeDetailActivity.start(requireContext(), recipe);
                });
            }
        }
    }
}
