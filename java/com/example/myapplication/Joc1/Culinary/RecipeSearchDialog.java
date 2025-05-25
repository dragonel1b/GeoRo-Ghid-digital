package com.example.myapplication.Joc1.Culinary;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

/**
 * Material Design 3 dialog for searching and selecting recipes
 */
public class RecipeSearchDialog extends AppCompatDialogFragment {
    
    private RecyclerView recipesRecyclerView;
    private TextInputEditText searchEditText;
    private View noResultsView;
    
    private List<ModernCulinaryActivity.Recipe> allRecipes = new ArrayList<>();
    private List<ModernCulinaryActivity.Recipe> filteredRecipes = new ArrayList<>();
    private RecipeSearchAdapter adapter;
    private FirebaseAnalytics firebaseAnalytics;
    
    private OnRecipeSelectedListener recipeSelectedListener;

    public interface OnRecipeSelectedListener {
        void onRecipeSelected(long recipeId, String recipeName);
    }

    public static RecipeSearchDialog newInstance() {
        return new RecipeSearchDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(AppCompatDialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered);
        
        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());
        
        // Log dialog opened
        firebaseAnalytics.logEvent("recipe_search_dialog_opened", null);
        
        // Fetch all recipes
        allRecipes = getAllRecipes();
        filteredRecipes = new ArrayList<>(allRecipes);
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View dialogView = LayoutInflater.from(getContext()).inflate(
            R.layout.dialog_recipe_search, null);

        // Initialize views
        searchEditText = dialogView.findViewById(R.id.searchEditText);
        recipesRecyclerView = dialogView.findViewById(R.id.recipesRecyclerView);
        noResultsView = dialogView.findViewById(R.id.noResultsView);
        
        // Setup search functionality
        setupSearch();
        
        // Setup RecyclerView
        setupRecyclerView();

        // Create Material dialog
        return new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.search_recipes)
            .setView(dialogView)
            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                firebaseAnalytics.logEvent("recipe_search_canceled", null);
                dialog.dismiss();
            })
            .create();
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecipes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterRecipes(String query) {
        filteredRecipes.clear();
        
        if (query.isEmpty()) {
            filteredRecipes.addAll(allRecipes);
        } else {
            String lowerQuery = query.toLowerCase();
            for (ModernCulinaryActivity.Recipe recipe : allRecipes) {
                if (recipe.getName().toLowerCase().contains(lowerQuery) || 
                    recipe.getDescription().toLowerCase().contains(lowerQuery) ||
                    recipe.getRegion().toLowerCase().contains(lowerQuery) ||
                    recipe.getCategory().toLowerCase().contains(lowerQuery)) {
                    
                    filteredRecipes.add(recipe);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        
        // Show/hide no results message
        noResultsView.setVisibility(filteredRecipes.isEmpty() ? View.VISIBLE : View.GONE);
        
        // Log search query
        Bundle params = new Bundle();
        params.putString("search_query", query);
        params.putInt("results_count", filteredRecipes.size());
        firebaseAnalytics.logEvent("recipe_search_query", params);
    }

    private void setupRecyclerView() {
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        adapter = new RecipeSearchAdapter(filteredRecipes, (recipeId, recipeName) -> {
            if (recipeSelectedListener != null) {
                recipeSelectedListener.onRecipeSelected(recipeId, recipeName);
            }
            dismiss();
        });
        
        recipesRecyclerView.setAdapter(adapter);
    }

    private List<ModernCulinaryActivity.Recipe> getAllRecipes() {
        // This should ideally come from a repository or ViewModel
        // For now, using the same sample recipe data
        List<ModernCulinaryActivity.Recipe> recipes = new ArrayList<>();
        
        // Moldova recipes
        recipes.add(new ModernCulinaryActivity.Recipe(
            "1",
            "Sarmale Moldovenești",
            "Moldova",
            "Felul principal",
            "Sarmale tradiționale moldovenești cu carne de porc, orez și verdeață",
            "Mediu",
            "120 min",
            new String[] {
                "Carne tocată de porc",
                "Orez",
                "Ceapă",
                "Morcov",
                "Varză murată",
                "Mărar și pătrunjel"
            },
            new String[] {
                "Se călește ceapa și morcovul",
                "Se amestecă carnea cu orezul și legumele",
                "Se înfășoară în foi de varză",
                "Se fierb timp de 2 ore"
            }
        ));
        
        recipes.add(new ModernCulinaryActivity.Recipe(
            "2",
            "Zeamă Moldovenească",
            "Moldova",
            "Supe și ciorbe",
            "Supă tradițională cu pui, tăiței de casă și legume",
            "Ușor",
            "60 min",
            new String[] {
                "Carne de pui",
                "Morcovi",
                "Ceapă",
                "Cartofi",
                "Pătrunjel",
                "Tăiței de casă",
                "Lămâie"
            },
            new String[] {
                "Se fierbe puiul pentru a obține supă",
                "Se adaugă legumele tăiate cubulețe",
                "Se adaugă tăițeii de casă",
                "Se servește cu lămâie și pătrunjel proaspăt"
            }
        ));
        
        // Transilvania recipes
        recipes.add(new ModernCulinaryActivity.Recipe(
            "3",
            "Gulaș Ardelenesc",
            "Transilvania",
            "Felul principal",
            "Gulaș tradițional cu carne de vită și cartofi",
            "Mediu",
            "150 min",
            new String[] {
                "Carne de vită",
                "Cartofi",
                "Ceapă",
                "Ardei",
                "Boia de ardei",
                "Chimen",
                "Pastă de roșii"
            },
            new String[] {
                "Se taie carnea cuburi și se prăjește cu ceapa",
                "Se adaugă boia, chimenul și pasta de roșii",
                "Se adaugă apă și se fierbe carnea până devine fragedă",
                "Se adaugă cartofii și se mai fierbe până se pătrund"
            }
        ));
        
        // Add more recipes here
        
        return recipes;
    }

    public void setOnRecipeSelectedListener(OnRecipeSelectedListener listener) {
        this.recipeSelectedListener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        firebaseAnalytics.logEvent("recipe_search_dialog_dismissed", null);
    }

    /**
     * Adapter for recipe search items
     */
    private static class RecipeSearchAdapter extends RecyclerView.Adapter<RecipeSearchAdapter.ViewHolder> {
        private final List<ModernCulinaryActivity.Recipe> recipes;
        private final OnRecipeItemClickListener listener;

        interface OnRecipeItemClickListener {
            void onRecipeClick(long recipeId, String recipeName);
        }

        RecipeSearchAdapter(List<ModernCulinaryActivity.Recipe> recipes, OnRecipeItemClickListener listener) {
            this.recipes = recipes;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_search, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ModernCulinaryActivity.Recipe recipe = recipes.get(position);
            
            holder.nameTextView.setText(recipe.getName());
            holder.regionTextView.setText(recipe.getRegion());
            holder.categoryTextView.setText(recipe.getCategory());
            holder.timeTextView.setText(recipe.getTime());
            
            // Set difficulty color indicator
            int difficultyColor;
            switch (recipe.getDifficulty().toLowerCase()) {
                case "ușor":
                    difficultyColor = R.color.difficulty_easy;
                    break;
                case "mediu":
                    difficultyColor = R.color.difficulty_medium;
                    break;
                default: // Difficult
                    difficultyColor = R.color.difficulty_hard;
                    break;
            }
            holder.difficultyIndicator.setBackgroundResource(difficultyColor);
            
            // Set click listener
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(recipe.getId(), recipe.getName());
                }
            });
        }

        @Override
        public int getItemCount() {
            return recipes.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            View difficultyIndicator;
            android.widget.TextView nameTextView;
            android.widget.TextView regionTextView;
            android.widget.TextView categoryTextView;
            android.widget.TextView timeTextView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                difficultyIndicator = itemView.findViewById(R.id.difficultyIndicator);
                nameTextView = itemView.findViewById(R.id.nameTextView);
                regionTextView = itemView.findViewById(R.id.regionTextView);
                categoryTextView = itemView.findViewById(R.id.categoryTextView);
                timeTextView = itemView.findViewById(R.id.timeTextView);
            }
        }
    }
} 