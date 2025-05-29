package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.Recipe;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter for displaying suggested recipes in a RecyclerView
 */
public class SuggestedRecipeAdapter extends ListAdapter<Recipe, SuggestedRecipeAdapter.RecipeViewHolder> {

    private final OnRecipeClickListener clickListener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public SuggestedRecipeAdapter(OnRecipeClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    private static final DiffUtil.ItemCallback<Recipe> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Recipe>() {
                @Override
                public boolean areItemsTheSame(@NonNull Recipe oldItem, @NonNull Recipe newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Recipe oldItem, @NonNull Recipe newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggested_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = getItem(position);
        holder.bind(recipe);
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {

        private final ImageView recipeImageView;
        private final TextView recipeNameTextView;
        private final TextView matchScoreTextView;
        private final TextView recipeMissingIngredientsTextView;
        private final MaterialButton viewRecipeButton;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImageView = itemView.findViewById(R.id.recipeImageView);
            recipeNameTextView = itemView.findViewById(R.id.recipeNameTextView);
            matchScoreTextView = itemView.findViewById(R.id.matchScoreTextView);
            recipeMissingIngredientsTextView = itemView.findViewById(R.id.recipeMissingIngredientsTextView);
            viewRecipeButton = itemView.findViewById(R.id.viewRecipeButton);
        }

        void bind(Recipe recipe) {
            // Set recipe name
            recipeNameTextView.setText(recipe.getName());
            
            // Set match score
            matchScoreTextView.setText(String.format("Potrivire: %d%%", recipe.getMatchScore()));
            
            // Load recipe image
            Glide.with(itemView.getContext())
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.image_placeholder_background)
                    .error(R.drawable.image_placeholder_background)
                    .centerCrop()
                    .into(recipeImageView);
            
            // Set missing ingredients text
            if (recipe.getMissingIngredients() != null && !recipe.getMissingIngredients().isEmpty()) {
                String missingText = recipe.getMissingIngredients().stream()
                        .map(ingredient -> ingredient.getName())
                        .collect(Collectors.joining(", "));
                
                recipeMissingIngredientsTextView.setText("Lipsesc: " + missingText);
                recipeMissingIngredientsTextView.setVisibility(View.VISIBLE);
            } else {
                recipeMissingIngredientsTextView.setVisibility(View.GONE);
            }
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onRecipeClick(recipe);
                }
            });
            
            // Set button click listener
            viewRecipeButton.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onRecipeClick(recipe);
                }
            });
        }
    }
} 