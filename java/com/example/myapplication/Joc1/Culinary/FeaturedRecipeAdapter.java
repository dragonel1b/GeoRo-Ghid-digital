package com.example.myapplication.Joc1.Culinary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adapter for displaying featured recipes in a horizontal RecyclerView
 */
public class FeaturedRecipeAdapter extends RecyclerView.Adapter<FeaturedRecipeAdapter.ViewHolder> {
    private final List<ModernCulinaryActivity.Recipe> recipes;
    private final OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(ModernCulinaryActivity.Recipe recipe);
    }

    public FeaturedRecipeAdapter(List<ModernCulinaryActivity.Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_featured_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModernCulinaryActivity.Recipe recipe = recipes.get(position);
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
        private final TextView recipeRegion;
        private final TextView recipeDifficulty;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.recipeCard);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeTitle = itemView.findViewById(R.id.recipeTitle);
            recipeRegion = itemView.findViewById(R.id.recipeRegion);
            recipeDifficulty = itemView.findViewById(R.id.recipeDifficulty);
        }

        void bind(ModernCulinaryActivity.Recipe recipe) {
            recipeTitle.setText(recipe.getTitle());
            recipeRegion.setText(recipe.getRegion());
            recipeDifficulty.setText(recipe.getDifficulty());
            
            // Set image resource if available, otherwise use placeholder
            if (recipe.getImageResourceId() != 0) {
                recipeImage.setImageResource(recipe.getImageResourceId());
            } else {
                recipeImage.setImageResource(R.drawable.placeholder_recipe);
            }
            
            // Set click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(recipe);
                }
            });
        }
    }
} 