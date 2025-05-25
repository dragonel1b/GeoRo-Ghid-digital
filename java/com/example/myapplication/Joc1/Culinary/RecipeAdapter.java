package com.example.myapplication.Joc1.Culinary;

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
import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<ModernCulinaryActivity.Recipe> recipes;
    private final OnRecipeActionListener viewListener;
    private final OnRecipeActionListener prepareListener;
    private OnFavoriteChangedListener favoriteListener;

    public interface OnRecipeActionListener {
        void onRecipeAction(ModernCulinaryActivity.Recipe recipe);
    }
    
    public interface OnFavoriteChangedListener {
        void onFavoriteChanged(ModernCulinaryActivity.Recipe recipe, boolean isFavorite);
    }

    public RecipeAdapter(List<ModernCulinaryActivity.Recipe> recipes,
                         OnRecipeActionListener viewListener,
                         OnRecipeActionListener prepareListener) {
        this.recipes = recipes;
        this.viewListener = viewListener;
        this.prepareListener = prepareListener;
    }
    
    public void setOnFavoriteChangedListener(OnFavoriteChangedListener listener) {
        this.favoriteListener = listener;
    }
    
    /**
     * Updates the recipe list with a new filtered list
     * @param newRecipes The new list of recipes to display
     */
    public void updateRecipes(List<ModernCulinaryActivity.Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
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

            ModernCulinaryActivity.Recipe recipe = recipes.get(position);
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
                    
            // Set recipe rating if available
            if (recipe.getRating() > 0) {
                holder.recipeRating.setVisibility(View.VISIBLE);
                holder.recipeRating.setText(recipe.getFormattedRating());
            } else {
                holder.recipeRating.setVisibility(View.GONE);
            }

            // Set recipe image using the imageResourceId property
            try {
                // Încercare folosind proprietatea imageResourceId
                if (recipe.getImageResourceId() != 0) {
                    holder.recipeImage.setImageResource(recipe.getImageResourceId());
                } else {
                    // Dacă nu găsim imaginea, încercăm o variantă de backup bazată pe regiune
                    int imageResource = holder.itemView.getContext().getResources().getIdentifier(
                            "food_" + recipe.getRegion().toLowerCase().replace(" ", "_"),
                            "drawable",
                            holder.itemView.getContext().getPackageName()
                    );
                    if (imageResource != 0) {
                        holder.recipeImage.setImageResource(imageResource);
                    } else {
                        // Set a default image if resource not found
                        holder.recipeImage.setImageResource(R.drawable.placeholder_food);
                    }
                }
            } catch (Exception e) {
                // Set default image in case of any error
                holder.recipeImage.setImageResource(R.drawable.placeholder_food);
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
            
            // Update favorite button state
            if (recipe.isFavorite()) {
                holder.recipeFavoriteButton.setImageResource(R.drawable.ic_favorite);
            } else {
                holder.recipeFavoriteButton.setImageResource(R.drawable.ic_favorite_border);
            }
            
            // Setup favorite button click listener
            holder.recipeFavoriteButton.setOnClickListener(v -> {
                try {
                    recipe.toggleFavorite();
                    
                    // Update icon based on new state
                    if (recipe.isFavorite()) {
                        holder.recipeFavoriteButton.setImageResource(R.drawable.ic_favorite);
                        Toast.makeText(holder.itemView.getContext(), 
                                "Adăugat la favorite", Toast.LENGTH_SHORT).show();
                    } else {
                        holder.recipeFavoriteButton.setImageResource(R.drawable.ic_favorite_border);
                        Toast.makeText(holder.itemView.getContext(), 
                                "Eliminat din favorite", Toast.LENGTH_SHORT).show();
                    }
                    
                    // Notify listener
                    if (favoriteListener != null) {
                        favoriteListener.onFavoriteChanged(recipe, recipe.isFavorite());
                    }
                } catch (Exception e) {
                    Toast.makeText(holder.itemView.getContext(),
                            "Nu se poate actualiza starea favorit",
                            Toast.LENGTH_SHORT).show();
                }
            });

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
        final TextView recipeRating;
        final ImageView recipeStatus;
        final ImageView recipeFavoriteButton;
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
            recipeRating = itemView.findViewById(R.id.recipeRating);
            recipeStatus = itemView.findViewById(R.id.recipeStatus);
            recipeFavoriteButton = itemView.findViewById(R.id.recipeFavoriteButton);
            viewRecipeButton = itemView.findViewById(R.id.viewRecipeButton);
            prepareRecipeButton = itemView.findViewById(R.id.prepareRecipeButton);
        }
    }
}
