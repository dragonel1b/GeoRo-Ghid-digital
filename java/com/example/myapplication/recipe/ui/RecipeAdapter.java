package com.example.myapplication.recipe.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.recipe.model.Recipe;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adaptor pentru afișarea rețetelor într-un RecyclerView.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private final Context context;
    private List<Recipe> recipes;
    private final OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(Context context, List<Recipe> recipes, OnRecipeClickListener listener) {
        this.context = context;
        this.recipes = recipes;
        this.listener = listener;
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        
        // Set image
        String imageResourceName = recipe.getImageResourceName();
        if (imageResourceName != null && !imageResourceName.isEmpty()) {
            int resourceId = context.getResources().getIdentifier(
                    imageResourceName, "drawable", context.getPackageName());
            if (resourceId != 0) {
                holder.imageView.setImageResource(resourceId);
            }
        }
        
        // Set texts
        holder.titleTextView.setText(recipe.getTitle());
        holder.descriptionTextView.setText(recipe.getDescription());
        holder.regionTextView.setText(recipe.getRegion());
        holder.categoryTextView.setText(recipe.getCategory());
        
        // Set time and difficulty
        holder.timeTextView.setText(recipe.getPreparationTime() + " min");
        holder.difficultyTextView.setText(recipe.getDifficulty());
        
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
            updateFavoriteButton(holder, newFavoriteStatus);
            notifyDataSetChanged();
        });
    }
    
    private void updateFavoriteButton(ViewHolder holder, boolean isFavorite) {
        if (isFavorite) {
            holder.favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.favoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    @Override
    public int getItemCount() {
        return recipes.size();
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
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }
} 