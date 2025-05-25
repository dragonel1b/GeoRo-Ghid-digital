package com.example.myapplication.Joc1;

import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private final List<RomCulinaryActivity.Recipe> recipes;
    private final OnRecipeActionListener viewListener;
    private final OnRecipeActionListener prepareListener;

    public interface OnRecipeActionListener {
        void onRecipeAction(RomCulinaryActivity.Recipe recipe);
    }

    public RecipeAdapter(List<RomCulinaryActivity.Recipe> recipes,
                         OnRecipeActionListener viewListener,
                         OnRecipeActionListener prepareListener) {
        this.recipes = recipes;
        this.viewListener = viewListener;
        this.prepareListener = prepareListener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rom_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        try {
            if (recipes == null || position < 0 || position >= recipes.size()) {
                return;
            }

            RomCulinaryActivity.Recipe recipe = recipes.get(position);
            if (recipe == null) {
                return;
            }

            // Set text fields with null checks
            holder.recipeTitle.setText(recipe.getTitle() != null ? recipe.getTitle() : "");
            holder.recipeRegion.setText(recipe.getRegion() != null ? recipe.getRegion() : "");
            holder.recipeDescription.setText(recipe.getDescription() != null ? recipe.getDescription() : "");
            holder.recipeDifficulty.setText(String.format("Dificultate: %s",
                    recipe.getDifficulty() != null ? recipe.getDifficulty() : "N/A"));
            holder.recipeTime.setText(String.format("Timp: %s",
                    recipe.getTime() != null ? recipe.getTime() : "N/A"));

            // Set recipe image based on region with error handling
            try {
                if (recipe.getRegion() != null) {
                    int imageResource = holder.itemView.getContext().getResources().getIdentifier(
                            "food_" + recipe.getRegion().toLowerCase().replace(" ", "_"),
                            "drawable",
                            holder.itemView.getContext().getPackageName()
                    );
                    if (imageResource != 0) {
                        holder.recipeImage.setImageResource(imageResource);
                    } else {
                        // Set a default image if resource not found
                        holder.recipeImage.setImageResource(R.drawable.ic_food);
                    }
                }
            } catch (Resources.NotFoundException e) {
                // Set default image in case of resource not found
                holder.recipeImage.setImageResource(R.drawable.ic_food);
            }

            // Update recipe status with animation
            if (recipe.isDiscovered()) {
                holder.recipeStatus.setVisibility(View.VISIBLE);
                holder.recipeStatus.setAlpha(0f);
                holder.recipeStatus.animate().alpha(1f).setDuration(300).start();
                holder.prepareRecipeButton.setText("Rețetă Descoperită");
                holder.prepareRecipeButton.setEnabled(false);
            } else {
                holder.recipeStatus.setVisibility(View.GONE);
                holder.prepareRecipeButton.setText("Prepară");
                holder.prepareRecipeButton.setEnabled(true);
            }

            // Set click listeners with null checks and error handling
            holder.viewRecipeButton.setOnClickListener(v -> {
                try {
                    if (viewListener != null && recipe != null) {
                        viewListener.onRecipeAction(recipe);
                    }
                } catch (Exception e) {
                    Toast.makeText(holder.itemView.getContext(),
                            "Nu se poate afișa rețeta momentan",
                            Toast.LENGTH_SHORT).show();
                }
            });

            holder.prepareRecipeButton.setOnClickListener(v -> {
                try {
                    if (!recipe.isDiscovered() && prepareListener != null && recipe != null) {
                        prepareListener.onRecipeAction(recipe);
                    }
                } catch (Exception e) {
                    Toast.makeText(holder.itemView.getContext(),
                            "Nu se poate prepara rețeta momentan",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            // Handle any unexpected errors
            Log.e("RecipeAdapter", "Error binding view holder", e);
        }
    }

    @Override
    public int getItemCount() {
        return recipes != null ? recipes.size() : 0;
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final ImageView recipeImage;
        final TextView recipeTitle;
        final TextView recipeRegion;
        final TextView recipeDescription;
        final TextView recipeDifficulty;
        final TextView recipeTime;
        final ImageView recipeStatus;
        final MaterialButton viewRecipeButton;
        final MaterialButton prepareRecipeButton;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeTitle = itemView.findViewById(R.id.recipeTitle);
            recipeRegion = itemView.findViewById(R.id.recipeRegion);
            recipeDescription = itemView.findViewById(R.id.recipeDescription);
            recipeDifficulty = itemView.findViewById(R.id.recipeDifficulty);
            recipeTime = itemView.findViewById(R.id.recipeTime);
            recipeStatus = itemView.findViewById(R.id.recipeStatus);
            viewRecipeButton = itemView.findViewById(R.id.viewRecipeButton);
            prepareRecipeButton = itemView.findViewById(R.id.prepareRecipeButton);
        }
    }
}
