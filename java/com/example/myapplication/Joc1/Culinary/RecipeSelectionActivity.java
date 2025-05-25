package com.example.myapplication.Joc1.Culinary;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Add import for RecipeDBHelper
import com.example.myapplication.Joc1.Culinary.RecipeDBHelper;

// Import ModernCulinaryActivity
import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity;


/**
 * Activity for selecting recipes to add to meal plan
 */
public class RecipeSelectionActivity extends AppCompatActivity {
    
    private RecyclerView recipesRecyclerView;
    private ChipGroup categoryChipGroup;
    private ChipGroup regionChipGroup;
    private SearchView searchView;
    private TextView mealTypeHeaderText;
    private TextView dateHeaderText;
    
    private RecipeDBHelper dbHelper;
    private List<ModernCulinaryActivity.Recipe> allRecipes;
    private List<ModernCulinaryActivity.Recipe> filteredRecipes;
    
    private String date;
    private String mealType;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_selection);
        
        // Get intent data
        date = getIntent().getStringExtra("date");
        mealType = getIntent().getStringExtra("meal_type");
        
        if (date == null || mealType == null) {
            finish();
            return;
        }
        
        // Initialize database helper
        dbHelper = new RecipeDBHelper(this);
        
        // Initialize views
        initializeViews();
        setupToolbar();
        
        // Load recipes and setup filters
        loadRecipes();
        setupFilterChips();
        
        // Configure RecyclerView
        setupRecyclerView();
    }
    
    private void initializeViews() {
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView);
        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        regionChipGroup = findViewById(R.id.regionChipGroup);
        searchView = findViewById(R.id.searchView);
        mealTypeHeaderText = findViewById(R.id.mealTypeHeaderText);
        dateHeaderText = findViewById(R.id.dateHeaderText);
        
        // Set header texts
        mealTypeHeaderText.setText(mealType);
        dateHeaderText.setText(date);
        
        // Setup search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterRecipes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRecipes(newText);
                return true;
            }
        });
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.select_recipe);
        }
    }
    
    private void loadRecipes() {
        // Load all recipes from database
        allRecipes = dbHelper.getAllRecipes();
        
        // Filter recipes appropriate for meal type
        allRecipes = filterRecipesForMealType(allRecipes, mealType);
        
        // Initialize filtered recipes
        filteredRecipes = new ArrayList<>(allRecipes);
    }
    
    private List<ModernCulinaryActivity.Recipe> filterRecipesForMealType(
            List<ModernCulinaryActivity.Recipe> recipes, String mealType) {
        
        // Determine appropriate categories for this meal type
        List<String> appropriateCategories = new ArrayList<>();
        
        switch (mealType) {
            case "Mic dejun":
                appropriateCategories.add("Mic dejun");
                appropriateCategories.add("Deserturi");
                appropriateCategories.add("Pâine și produse de patiserie");
                break;
                
            case "Prânz":
                appropriateCategories.add("Feluri principale");
                appropriateCategories.add("Supe și ciorbe");
                appropriateCategories.add("Aperitive");
                break;
                
            case "Cină":
                appropriateCategories.add("Feluri principale");
                appropriateCategories.add("Aperitive");
                appropriateCategories.add("Sosuri și garnituri");
                break;
        }
        
        // Filter if we have categories, otherwise return all
        if (!appropriateCategories.isEmpty()) {
            return recipes.stream()
                    .filter(recipe -> appropriateCategories.contains(recipe.getCategory()))
                    .collect(Collectors.toList());
        }
        
        return recipes;
    }
    
    private void setupFilterChips() {
        // Create region chips dynamically based on available recipes
        List<String> regions = allRecipes.stream()
                .map(ModernCulinaryActivity.Recipe::getRegion)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
                
        for (String region : regions) {
            Chip chip = new Chip(this);
            chip.setText(region);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.rom_chip_background);
            regionChipGroup.addView(chip);
        }
        
        // Create category chips dynamically based on available recipes
        List<String> categories = allRecipes.stream()
                .map(ModernCulinaryActivity.Recipe::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
                
        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.rom_chip_background);
            categoryChipGroup.addView(chip);
        }
        
        // Add listeners
        categoryChipGroup.setOnCheckedChangeListener((group, checkedId) -> applyFilters());
        regionChipGroup.setOnCheckedChangeListener((group, checkedId) -> applyFilters());
    }
    
    private void setupRecyclerView() {
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        RecipeSelectionAdapter adapter = new RecipeSelectionAdapter(filteredRecipes, recipe -> {
            // Return selected recipe to MealPlanningActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("date", date);
            resultIntent.putExtra("meal_type", mealType);
            resultIntent.putExtra("recipe_id", recipe.getId());
            resultIntent.putExtra("recipe_title", recipe.getTitle());
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        
        recipesRecyclerView.setAdapter(adapter);
    }
    
    private void filterRecipes(String query) {
        if (query == null || query.isEmpty()) {
            filteredRecipes = new ArrayList<>(allRecipes);
        } else {
            String lowerQuery = query.toLowerCase();
            filteredRecipes = allRecipes.stream()
                    .filter(recipe -> recipe.getTitle().toLowerCase().contains(lowerQuery) ||
                                      recipe.getDescription().toLowerCase().contains(lowerQuery))
                    .collect(Collectors.toList());
        }
        
        // Apply any active filters
        applyFilters();
        
        // Update RecyclerView
        recipesRecyclerView.getAdapter().notifyDataSetChanged();
    }
    
    private void applyFilters() {
        // Start with current filtered recipes (from search)
        List<ModernCulinaryActivity.Recipe> result = new ArrayList<>(filteredRecipes);
        
        // Apply region filter if selected
        int regionCheckedId = regionChipGroup.getCheckedChipId();
        if (regionCheckedId != View.NO_ID) {
            Chip regionChip = findViewById(regionCheckedId);
            if (regionChip != null) {
                String region = regionChip.getText().toString();
                result = result.stream()
                        .filter(recipe -> recipe.getRegion().equals(region))
                        .collect(Collectors.toList());
            }
        }
        
        // Apply category filter if selected
        int categoryCheckedId = categoryChipGroup.getCheckedChipId();
        if (categoryCheckedId != View.NO_ID) {
            Chip categoryChip = findViewById(categoryCheckedId);
            if (categoryChip != null) {
                String category = categoryChip.getText().toString();
                result = result.stream()
                        .filter(recipe -> recipe.getCategory().equals(category))
                        .collect(Collectors.toList());
            }
        }
        
        // Update filtered recipes
        filteredRecipes = result;
        
        // Update RecyclerView
        recipesRecyclerView.getAdapter().notifyDataSetChanged();
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Adapter for recipe selection
     */
    private class RecipeSelectionAdapter extends RecyclerView.Adapter<RecipeSelectionAdapter.ViewHolder> {
        
        private final List<ModernCulinaryActivity.Recipe> recipes;
        private final OnRecipeSelectedListener listener;
        
        public RecipeSelectionAdapter(List<ModernCulinaryActivity.Recipe> recipes, OnRecipeSelectedListener listener) {
            this.recipes = recipes;
            this.listener = listener;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_recipe_selection, parent, false);
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
            private final TextView recipeTitle;
            private final TextView recipeRegion;
            private final TextView recipeCategory;
            private final TextView recipeDescription;
            
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                recipeTitle = itemView.findViewById(R.id.recipeTitle);
                recipeRegion = itemView.findViewById(R.id.recipeRegion);
                recipeCategory = itemView.findViewById(R.id.recipeCategory);
                recipeDescription = itemView.findViewById(R.id.recipeDescription);
            }
            
            public void bind(ModernCulinaryActivity.Recipe recipe) {
                recipeTitle.setText(recipe.getTitle());
                recipeRegion.setText(recipe.getRegion());
                recipeCategory.setText(recipe.getCategory());
                recipeDescription.setText(recipe.getDescription());
                
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRecipeSelected(recipe);
                    }
                });
            }
        }
    }
    
    public interface OnRecipeSelectedListener {
        void onRecipeSelected(ModernCulinaryActivity.Recipe recipe);
    }
} 