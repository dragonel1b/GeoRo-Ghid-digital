package com.example.myapplication.Joc1.Culinary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity.Recipe;
import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adapter for displaying recipe cards in a RecyclerView
 */
public class RecipeCardAdapter extends RecyclerView.Adapter<RecipeCardAdapter.ViewHolder> {
    private List<Recipe> recipes;
    private final OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeCardAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    /**
     * Update the recipe list and refresh the display
     * @param newRecipes New list of recipes
     */
    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView recipeImage;
        private final TextView recipeTitle;
        private final TextView recipeDescription;
        private final TextView recipeRegion;
        private final TextView recipeDifficulty;
        private final TextView ratingText;
        private final TextView timeText;
        private final MaterialButton favoriteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.recipeCard);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeTitle = itemView.findViewById(R.id.recipeTitle);
            recipeDescription = itemView.findViewById(R.id.recipeDescription);
            recipeRegion = itemView.findViewById(R.id.recipeRegion);
            recipeDifficulty = itemView.findViewById(R.id.recipeDifficulty);
            ratingText = itemView.findViewById(R.id.ratingText);
            timeText = itemView.findViewById(R.id.timeText);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);

            // Setăm listener pentru click pe card
            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRecipeClick(recipes.get(position));
                }
            });

            // Setăm listener pentru butonul favorit
            favoriteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Recipe recipe = recipes.get(position);
                    recipe.setFavorite(!recipe.isFavorite());
                    updateFavoriteIcon(recipe.isFavorite());
                }
            });
        }

        void bind(Recipe recipe) {
            recipeTitle.setText(recipe.getTitle());
            
            if (recipeDescription != null) {
                recipeDescription.setText(recipe.getDescription());
            }
            
            recipeRegion.setText(recipe.getRegion());
            recipeDifficulty.setText(recipe.getDifficulty());
            
            // Set image resource if available, otherwise use placeholder
            if (recipe.getImageResourceId() != 0) {
                recipeImage.setImageResource(recipe.getImageResourceId());
            } else {
                recipeImage.setImageResource(R.drawable.placeholder_recipe);
            }
            
            ratingText.setText(recipe.getFormattedRating());
            timeText.setText(recipe.getTime());

            // Actualizăm iconița de favorit
            updateFavoriteIcon(recipe.isFavorite());
        }

        private void updateFavoriteIcon(boolean isFavorite) {
            if (isFavorite) {
                favoriteButton.setIconResource(R.drawable.ic_favorite);
            } else {
                favoriteButton.setIconResource(R.drawable.ic_favorite_border);
            }
        }
    }
}
