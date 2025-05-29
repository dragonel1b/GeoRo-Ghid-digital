package com.example.myapplication.recipe.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.example.myapplication.R;
import com.example.myapplication.recipe.model.Recipe;
import com.example.myapplication.recipe.repository.RecipeRepository;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptor pentru afișarea rețetelor într-un RecyclerView.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private static final int ANIMATION_DELAY = 30;

    private final Context context;
    private List<Recipe> recipes;
    private List<Recipe> filteredRecipes;
    private final OnRecipeClickListener listener;
    private final RecipeRepository recipeRepository;
    private int lastAnimatedPosition = -1;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(Context context) {
        this.context = context;
        this.recipes = new ArrayList<>();
        this.filteredRecipes = new ArrayList<>();
        this.listener = (OnRecipeClickListener) context;
        this.recipeRepository = RecipeRepository.getInstance();
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        this.filteredRecipes = new ArrayList<>(recipes);
        notifyDataSetChanged();
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        this.filteredRecipes = new ArrayList<>(newRecipes);
        notifyDataSetChanged();
    }

    public void filterByQuery(String query) {
        filteredRecipes = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            filteredRecipes.addAll(recipes);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Recipe recipe : recipes) {
                if (recipe.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                    recipe.getDescription().toLowerCase().contains(lowerCaseQuery)) {
                    filteredRecipes.add(recipe);
                }
            }
        }
        resetAnimationIndex();
        notifyDataSetChanged();
    }

    public void filterByCategory(String category) {
        filteredRecipes = new ArrayList<>();
        if (category == null || category.isEmpty()) {
            filteredRecipes.addAll(recipes);
        } else {
            for (Recipe recipe : recipes) {
                if (recipe.getCategory().equals(category)) {
                    filteredRecipes.add(recipe);
                }
            }
        }
        resetAnimationIndex();
        notifyDataSetChanged();
    }

    public void filterByFavorites(boolean showOnlyFavorites) {
        filteredRecipes = new ArrayList<>();
        if (!showOnlyFavorites) {
            filteredRecipes.addAll(recipes);
        } else {
            for (Recipe recipe : recipes) {
                if (recipe.isFavorite()) {
                    filteredRecipes.add(recipe);
                }
            }
        }
        resetAnimationIndex();
        notifyDataSetChanged();
    }
    
    private void resetAnimationIndex() {
        lastAnimatedPosition = -1;
    }
    
    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastAnimatedPosition) {
            viewToAnimate.setAlpha(0.0f);
            viewToAnimate.animate()
                    .alpha(1.0f)
                    .setDuration(300)
                    .setStartDelay(position * ANIMATION_DELAY)
                    .start();
            lastAnimatedPosition = position;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = filteredRecipes.get(position);
        
        // Aplică animație la rulare
        setAnimation(holder.itemView, position);
        
        // Set image
        if (recipe.getImageResourceId() != 0) {
            holder.imageView.setImageResource(recipe.getImageResourceId());
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_recipe);
        }
        
        // Set texts
        holder.titleTextView.setText(recipe.getTitle());
        holder.descriptionTextView.setText(recipe.getDescription());
        holder.regionTextView.setText(recipe.getRegion());
        holder.categoryTextView.setText(recipe.getCategory());
        
        // Set time and difficulty
        holder.timeTextView.setText(recipe.getFormattedTime());
        holder.difficultyTextView.setText(recipe.getDifficulty());
        
        // Set rating
        double rating = recipe.getRating();
        if (rating > 0) {
            holder.ratingTextView.setText(String.format("%.1f", rating));
            holder.ratingTextView.setVisibility(View.VISIBLE);
        } else {
            holder.ratingTextView.setVisibility(View.GONE);
        }
        
        // Set favorite status
        updateFavoriteButton(holder, recipe.isFavorite());
        
        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });
        
        // Set favorite button click listener
        holder.favoriteButton.setOnClickListener(v -> {
            boolean newFavoriteStatus = !recipe.isFavorite();
            recipe.setFavorite(newFavoriteStatus);
            
            // Animation
            animateFavoriteButton(holder.favoriteButton, newFavoriteStatus);
            
            // Update in repository
            recipeRepository.updateRecipe(recipe);
        });
        
        // Tag click listeners
        holder.regionTextView.setOnClickListener(v -> {
            filterByRegion(recipe.getRegion());
        });
        
        holder.categoryTextView.setOnClickListener(v -> {
            filterByCategory(recipe.getCategory());
        });
    }
    
    private void filterByRegion(String region) {
        filteredRecipes = new ArrayList<>();
        if (region == null || region.isEmpty()) {
            filteredRecipes.addAll(recipes);
        } else {
            for (Recipe recipe : recipes) {
                if (recipe.getRegion().equals(region)) {
                    filteredRecipes.add(recipe);
                }
            }
        }
        resetAnimationIndex();
        notifyDataSetChanged();
    }
    
    private void animateFavoriteButton(ImageView favoriteButton, boolean isFavorite) {
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.avd_star_empty_to_filled);
        } else {
            favoriteButton.setImageResource(R.drawable.avd_star_filled_to_empty);
        }
        
        Drawable drawable = favoriteButton.getDrawable();
        if (drawable instanceof AnimatedVectorDrawable) {
            ((AnimatedVectorDrawable) drawable).start();
        } else if (drawable instanceof AnimatedVectorDrawableCompat) {
            ((AnimatedVectorDrawableCompat) drawable).start();
        }
    }
    
    private void updateFavoriteButton(ViewHolder holder, boolean isFavorite) {
        if (isFavorite) {
            holder.favoriteButton.setImageResource(R.drawable.ic_star_filled);
        } else {
            holder.favoriteButton.setImageResource(R.drawable.ic_star_empty);
        }
    }

    @Override
    public int getItemCount() {
        return filteredRecipes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView imageView;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView regionTextView;
        TextView categoryTextView;
        TextView timeTextView;
        TextView difficultyTextView;
        TextView ratingTextView;
        ImageView favoriteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            imageView = itemView.findViewById(R.id.recipeImageView);
            titleTextView = itemView.findViewById(R.id.recipeTitleTextView);
            descriptionTextView = itemView.findViewById(R.id.recipeDescriptionTextView);
            timeTextView = itemView.findViewById(R.id.prepTimeTextView);
            difficultyTextView = itemView.findViewById(R.id.difficultyTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            regionTextView = itemView.findViewById(R.id.regionTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }
} 