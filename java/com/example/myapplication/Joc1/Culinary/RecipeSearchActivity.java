package com.example.myapplication.Joc1.Culinary;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for searching recipes
 */
public class RecipeSearchActivity extends AppCompatActivity {

    private EditText searchEditText;
    private RecyclerView resultsRecyclerView;
    private ChipGroup filtersChipGroup;
    private TextView noResultsText;
    private CulinaryViewModel viewModel;
    private RecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_search);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(CulinaryViewModel.class);
        viewModel.setDbHelper(new RecipeDBHelper(this));
        
        // Initialize views
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();
        
        // Load initial data
        viewModel.loadRecipes();
        observeViewModel();
        
        // Setup search functionality
        setupSearch();
    }
    
    private void initViews() {
        searchEditText = findViewById(R.id.searchEditText);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        filtersChipGroup = findViewById(R.id.filtersChipGroup);
        noResultsText = findViewById(R.id.noResultsText);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Search Recipes");
            getSupportActionBar().setElevation(8f);
            toolbar.setTitleTextColor(getResources().getColor(R.color.white, null));
        }

        // Set status bar color
        getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark, null));
    }
    
    private void setupRecyclerView() {
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(new ArrayList<>(), recipe -> {
            // Launch RecipeDetailActivity with required identifiers
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra("recipe_title", recipe.getTitle());
            intent.putExtra("recipe_region", recipe.getRegion());
            startActivity(intent);
        });
        resultsRecyclerView.setAdapter(adapter);
    }
    
    private void setupFilters() {
        filtersChipGroup.setSingleSelection(false);
        filtersChipGroup.setSelectionRequired(false);
        
        // Add category filters
        for (String category : CulinaryUtils.CATEGORIES) {
            Chip chip = new Chip(this, null, R.style.Widget_App_Chip);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            chip.setClickable(true);
            
            // Set checked state colors
            int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] { -android.R.attr.state_checked }
            };
            
            int[] chipColors = new int[] {
                getResources().getColor(R.color.primary, null),
                getResources().getColor(R.color.surface, null)
            };
            
            int[] textColors = new int[] {
                getResources().getColor(R.color.white, null),
                getResources().getColor(R.color.text_primary, null)
            };
            
            chip.setChipBackgroundColor(new ColorStateList(states, chipColors));
            chip.setTextColor(new ColorStateList(states, textColors));
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilters());
            
            filtersChipGroup.addView(chip);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Apply elevation to search card
        View searchCard = findViewById(R.id.searchCard);
        if (searchCard != null) {
            searchCard.setElevation(4f);
        }
    }
    
    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Search as user types
                searchRecipes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }
    
    private void observeViewModel() {
        viewModel.getRecipes().observe(this, recipes -> {
            updateSearchResults(recipes);
        });
    }
    
    private void searchRecipes(String query) {
        if (query == null || query.isEmpty()) {
            viewModel.loadRecipes();
        } else {
            viewModel.searchRecipes(query.toLowerCase());
        }
    }
    
    private void applyFilters() {
        // Get selected categories
        List<String> selectedCategories = new ArrayList<>();
        for (int i = 0; i < filtersChipGroup.getChildCount(); i++) {
            View view = filtersChipGroup.getChildAt(i);
            if (view instanceof Chip) {
                Chip chip = (Chip) view;
                if (chip.isChecked()) {
                    selectedCategories.add(chip.getText().toString());
                }
            }
        }
        
        // Get current search query
        String query = searchEditText.getText().toString();
        
        // Get all recipes
        List<ModernCulinaryActivity.Recipe> allRecipes = viewModel.getRecipes().getValue();
        if (allRecipes == null) return;
        
        List<ModernCulinaryActivity.Recipe> filteredRecipes = new ArrayList<>();
        
        // Apply both search query and category filters
        for (ModernCulinaryActivity.Recipe recipe : allRecipes) {
            boolean matchesQuery = query.isEmpty() ||
                    recipe.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    recipe.getDescription().toLowerCase().contains(query.toLowerCase());
            
            boolean matchesCategory = selectedCategories.isEmpty() ||
                    selectedCategories.contains(recipe.getCategory());
            
            if (matchesQuery && matchesCategory) {
                filteredRecipes.add(recipe);
            }
        }
        
        updateSearchResults(filteredRecipes);
    }
    
    private void updateSearchResults(List<ModernCulinaryActivity.Recipe> recipes) {
        adapter.updateRecipes(recipes);
        
        // Show/hide no results message
        if (recipes.isEmpty()) {
            noResultsText.setVisibility(View.VISIBLE);
            resultsRecyclerView.setVisibility(View.GONE);
        } else {
            noResultsText.setVisibility(View.GONE);
            resultsRecyclerView.setVisibility(View.VISIBLE);
        }
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
     * Adapter for recipe search results
     */
    private static class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
        
        interface OnRecipeClickListener {
            void onRecipeClick(ModernCulinaryActivity.Recipe recipe);
        }
        
        private List<ModernCulinaryActivity.Recipe> recipes;
        private final OnRecipeClickListener listener;
        
        RecipeAdapter(List<ModernCulinaryActivity.Recipe> recipes, OnRecipeClickListener listener) {
            this.recipes = recipes;
            this.listener = listener;
        }
        
        void updateRecipes(List<ModernCulinaryActivity.Recipe> newRecipes) {
            this.recipes = newRecipes;
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recipe, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ModernCulinaryActivity.Recipe recipe = recipes.get(position);
            holder.titleText.setText(recipe.getTitle());
            holder.descriptionText.setText(recipe.getDescription());
            holder.regionText.setText(recipe.getRegion());
            holder.timeText.setText(recipe.getPrepTime());
            
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(recipe);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return recipes.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleText;
            TextView descriptionText;
            TextView regionText;
            TextView timeText;
            
            ViewHolder(View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.recipeTitleText);
                descriptionText = itemView.findViewById(R.id.recipeDescriptionText);
                regionText = itemView.findViewById(R.id.recipeRegionText);
                timeText = itemView.findViewById(R.id.recipeTimeText);
            }
        }
    }
} 