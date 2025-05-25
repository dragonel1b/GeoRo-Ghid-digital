package com.example.myapplication.recipe.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
        this.recipes = recipes;
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
        private final ImageButton favoriteButton;
        private final TextView categoryTextView;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImageView = itemView.findViewById(R.id.recipeImageView);
            recipeTitleTextView = itemView.findViewById(R.id.recipeTitleTextView);
            recipeDescriptionTextView = itemView.findViewById(R.id.recipeDescriptionTextView);
            prepTimeTextView = itemView.findViewById(R.id.prepTimeTextView);
            difficultyTextView = itemView.findViewById(R.id.difficultyTextView);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            
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
            prepTimeTextView.setText(recipe.getPreparationTime() + " min");
            difficultyTextView.setText(recipe.getDifficulty());
            categoryTextView.setText(recipe.getCategory());
            
            // Set image resource if available, otherwise use placeholder
            if (recipe.getImageResourceId() != 0) {
                recipeImageView.setImageResource(recipe.getImageResourceId());
            } else {
                recipeImageView.setImageResource(R.drawable.placeholder_recipe);
            }
            
            // Set favorite button state and click listener
            updateFavoriteButton(recipe);
            favoriteButton.setOnClickListener(v -> {
                recipe.setFavorite(!recipe.isFavorite());
                recipeRepository.updateRecipe(recipe);
                updateFavoriteButton(recipe);
                
                // Show toast
                String message = recipe.isFavorite() ? 
                        context.getString(R.string.added_to_favorites) : 
                        context.getString(R.string.removed_from_favorites);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            });
        }
        
        private void updateFavoriteButton(Recipe recipe) {
            if (recipe.isFavorite()) {
                favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
                favoriteButton.setContentDescription(context.getString(R.string.remove_from_favorites_description));
            } else {
                favoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
                favoriteButton.setContentDescription(context.getString(R.string.add_to_favorites_description));
            }
        }
    }
} 