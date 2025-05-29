package com.example.myapplication.recipe.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.recipe.model.Recipe;
import com.example.myapplication.recipe.repository.RecipeRepository;
import com.example.myapplication.recipe.ui.RecipeDetailActivity;
import com.example.myapplication.utils.TransitionHelper;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private final Context context;
    private List<Recipe> recipes;
    private List<Recipe> filteredRecipes;
    private final RecipeRepository recipeRepository;
    private OnRecipeClickListener listener;
    
    // Interface for recipe click events
    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }
    
    public RecipeAdapter(Context context) {
        this.context = context;
        this.recipeRepository = RecipeRepository.getInstance();
        this.recipes = new ArrayList<>();
        this.filteredRecipes = new ArrayList<>();
        
        // If context implements the listener interface, set it automatically
        if (context instanceof OnRecipeClickListener) {
            this.listener = (OnRecipeClickListener) context;
        }
    }
    
    public void setOnRecipeClickListener(OnRecipeClickListener listener) {
        this.listener = listener;
    }
    
    public void setRecipes(List<Recipe> recipes) {
        this.recipes = new ArrayList<>(recipes);
        this.filteredRecipes = new ArrayList<>(recipes);
        notifyDataSetChanged();
    }
    
    public void updateRecipes(List<Recipe> recipes) {
        setRecipes(recipes);
    }
    
    public void filterByCategory(String category) {
        filteredRecipes = new ArrayList<>();
        if (category == null || category.isEmpty()) {
            filteredRecipes = new ArrayList<>(recipes);
        } else {
            for (Recipe recipe : recipes) {
                if (recipe.getCategory().equalsIgnoreCase(category)) {
                    filteredRecipes.add(recipe);
                }
            }
        }
        notifyDataSetChanged();
    }
    
    public void filterByFavorites(boolean showOnlyFavorites) {
        filteredRecipes = new ArrayList<>();
        if (showOnlyFavorites) {
            for (Recipe recipe : recipes) {
                if (recipe.isFavorite()) {
                    filteredRecipes.add(recipe);
                }
            }
        } else {
            filteredRecipes = new ArrayList<>(recipes);
        }
        notifyDataSetChanged();
    }
    
    public void filterByQuery(String query) {
        filteredRecipes = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            filteredRecipes = new ArrayList<>(recipes);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Recipe recipe : recipes) {
                if (recipe.getTitle().toLowerCase().contains(lowerCaseQuery) || 
                    recipe.getDescription().toLowerCase().contains(lowerCaseQuery)) {
                    filteredRecipes.add(recipe);
                } else {
                    // Check ingredients
                    boolean hasMatchingIngredient = recipe.getIngredients().stream()
                            .anyMatch(ingredient -> 
                                    ingredient.getName().toLowerCase().contains(lowerCaseQuery));
                    if (hasMatchingIngredient) {
                        filteredRecipes.add(recipe);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = filteredRecipes.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return filteredRecipes.size();
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final ImageView recipeImageView;
        private final TextView recipeTitleTextView;
        private final TextView recipeDescriptionTextView;
        private final TextView prepTimeTextView;
        private final TextView difficultyTextView;
        private final ShapeableImageView favoriteButton;
        private final TextView categoryTextView;
        private final TextView regionTextView;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImageView = itemView.findViewById(R.id.recipeImageView);
            recipeTitleTextView = itemView.findViewById(R.id.recipeTitleTextView);
            recipeDescriptionTextView = itemView.findViewById(R.id.recipeDescriptionTextView);
            prepTimeTextView = itemView.findViewById(R.id.prepTimeTextView);
            difficultyTextView = itemView.findViewById(R.id.difficultyTextView);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            regionTextView = itemView.findViewById(R.id.regionTextView);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Recipe recipe = filteredRecipes.get(position);
                    if (listener != null) {
                        listener.onRecipeClick(recipe);
                    }
                }
            });
        }

        public void bind(Recipe recipe) {
            recipeTitleTextView.setText(recipe.getTitle());
            recipeDescriptionTextView.setText(recipe.getDescription());
            prepTimeTextView.setText(recipe.getFormattedTime());
            difficultyTextView.setText(recipe.getDifficulty());
            categoryTextView.setText(recipe.getCategory());
            regionTextView.setText(recipe.getRegion());
            
            // Set image resource if available, otherwise use placeholder
            if (recipe.getImageResourceId() != 0) {
                recipeImageView.setImageResource(recipe.getImageResourceId());
            } else {
                recipeImageView.setImageResource(R.drawable.placeholder_recipe);
            }
            
            // Set favorite button state and click listener
            updateFavoriteButton(recipe);
            favoriteButton.setOnClickListener(v -> {
                // Animate the favorite button
                favoriteButton.animate()
                        .scaleX(0.7f)
                        .scaleY(0.7f)
                        .setDuration(150)
                        .withEndAction(() -> {
                            recipe.setFavorite(!recipe.isFavorite());
                            recipeRepository.updateRecipe(recipe);
                            updateFavoriteButton(recipe);
                            
                            // Animate back to normal size
                            favoriteButton.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(150)
                                    .start();
                            
                            // Show toast
                            String message = recipe.isFavorite() ? 
                                    context.getString(R.string.added_to_favorites) : 
                                    context.getString(R.string.removed_from_favorites);
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        })
                        .start();
            });
        }
        
        private void updateFavoriteButton(Recipe recipe) {
            int resourceId = recipe.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_empty;
            favoriteButton.setImageResource(resourceId);
            
            // Update content description
            favoriteButton.setContentDescription(
                recipe.isFavorite() 
                    ? context.getString(R.string.remove_from_favorites_description) 
                    : context.getString(R.string.add_to_favorites_description)
            );
            
            // Update alpha for better visual feedback
            favoriteButton.setAlpha(recipe.isFavorite() ? 1.0f : 0.8f);
            
            // Add rotation effect
            if (recipe.isFavorite()) {
                favoriteButton.animate()
                    .rotation(360f)
                    .setDuration(300)
                    .start();
            } else {
                favoriteButton.animate()
                    .rotation(0f)
                    .setDuration(300)
                    .start();
            }
        }
    }
} 