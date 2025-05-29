package com.example.myapplication.recipe.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.recipe.adapter.RecipeAdapter;
import com.example.myapplication.recipe.model.Ingredient;
import com.example.myapplication.recipe.model.Recipe;
import com.example.myapplication.recipe.repository.RecipeRepository;
import com.example.myapplication.utils.TransitionHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class RecipeListActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    private RecyclerView recipesRecyclerView;
    private TextView emptyView;
    private ProgressBar loadingIndicator;
    private RecipeAdapter recipeAdapter;
    private EditText searchEditText;
    private RecipeRepository recipeRepository;
    private List<Recipe> recipes = new ArrayList<>();

    private Chip favoritesChip;
    private Chip aperitiveChip;
    private Chip soupChip;
    private Chip mainChip;
    private Chip dessertChip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        // Initialize repository
        recipeRepository = RecipeRepository.getInstance();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView);
        emptyView = findViewById(R.id.emptyView);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        searchEditText = findViewById(R.id.searchEditText);
        ImageButton filterButton = findViewById(R.id.filterButton);

        // Initialize chips
        favoritesChip = findViewById(R.id.favoritesChip);
        aperitiveChip = findViewById(R.id.aperitiveChip);
        soupChip = findViewById(R.id.soupChip);
        mainChip = findViewById(R.id.mainChip);
        dessertChip = findViewById(R.id.dessertChip);

        // Set up recycler view
        setupRecyclerView();

        // Setup search functionality
        setupSearch();

        // Setup filter button
        filterButton.setOnClickListener(v -> {
            toggleFilterChips();
        });

        // Setup chip click listeners
        setupFilterChips();

        // Load recipes
        loadRecipes();

        // Configurare FAB pentru adăugare rețetă nouă
        FloatingActionButton fab = findViewById(R.id.fab_add_recipe);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(RecipeListActivity.this, AddRecipeActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        recipeAdapter = new RecipeAdapter(this);
        recipesRecyclerView.setAdapter(recipeAdapter);
        
        // Use a single column layout instead of a grid for better readability
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        recipesRecyclerView.setLayoutManager(layoutManager);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                recipeAdapter.filterByQuery(s.toString());
                updateEmptyViewVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
        
        // Add search filter button
        ImageButton filterButton = findViewById(R.id.filterButton);
        filterButton.setOnClickListener(v -> {
            showAdvancedSearchDialog();
        });
    }

    private void toggleFilterChips() {
        int currentVisibility = findViewById(R.id.filterChipGroup).getVisibility();
        int newVisibility = currentVisibility == View.VISIBLE ? View.GONE : View.VISIBLE;
        findViewById(R.id.filterChipGroup).setVisibility(newVisibility);
        
        // Show a brief tooltip to explain how to use filters when they become visible
        if (newVisibility == View.VISIBLE) {
            Toast.makeText(this, "Selectați categoriile dorite pentru filtrare", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupFilterChips() {
        // Favorites filter
        favoritesChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // If favorites is checked, uncheck category filters
            if (isChecked) {
                aperitiveChip.setChecked(false);
                soupChip.setChecked(false);
                mainChip.setChecked(false);
                dessertChip.setChecked(false);
                
                recipeAdapter.filterByFavorites(true);
            } else {
                // If no filters are selected, show all recipes
                if (!isCategoryFilterActive()) {
                    recipeAdapter.filterByFavorites(false);
                }
            }
            updateEmptyViewVisibility();
        });

        // Category filters
        aperitiveChip.setOnCheckedChangeListener((buttonView, isChecked) -> handleCategoryFilter());
        soupChip.setOnCheckedChangeListener((buttonView, isChecked) -> handleCategoryFilter());
        mainChip.setOnCheckedChangeListener((buttonView, isChecked) -> handleCategoryFilter());
        dessertChip.setOnCheckedChangeListener((buttonView, isChecked) -> handleCategoryFilter());
    }

    private void handleCategoryFilter() {
        // Uncheck favorites if any category is selected
        if (isCategoryFilterActive()) {
            favoritesChip.setChecked(false);
            
            // Determine which category to filter by
            if (aperitiveChip.isChecked()) {
                recipeAdapter.filterByCategory(getString(R.string.category_appetizer));
            } else if (soupChip.isChecked()) {
                recipeAdapter.filterByCategory(getString(R.string.category_soup));
            } else if (mainChip.isChecked()) {
                recipeAdapter.filterByCategory(getString(R.string.category_main_course));
            } else if (dessertChip.isChecked()) {
                recipeAdapter.filterByCategory(getString(R.string.category_dessert));
            }
        } else {
            // If no category is selected, show all recipes
            recipeAdapter.filterByCategory(null);
        }
        updateEmptyViewVisibility();
    }

    private boolean isCategoryFilterActive() {
        return aperitiveChip.isChecked() || soupChip.isChecked() || 
               mainChip.isChecked() || dessertChip.isChecked();
    }

    private void loadRecipes() {
        // Show loading indicator
        loadingIndicator.setVisibility(View.VISIBLE);
        
        // Get recipes from repository
        List<Recipe> recipes = recipeRepository.getAllRecipes();
        recipeAdapter.setRecipes(recipes);
        
        // Hide loading indicator
        loadingIndicator.setVisibility(View.GONE);
        
        // Update empty view visibility
        updateEmptyViewVisibility();
    }

    private void updateEmptyViewVisibility() {
        if (recipeAdapter.getItemCount() == 0) {
            findViewById(R.id.emptyStateContainer).setVisibility(View.VISIBLE);
            recipesRecyclerView.setVisibility(View.GONE);
        } else {
            findViewById(R.id.emptyStateContainer).setVisibility(View.GONE);
            recipesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_favorites) {
            // Afișează doar rețetele favorite
            recipes = recipeRepository.getFavoriteRecipes();
            recipeAdapter.updateRecipes(recipes);
            return true;
        } else if (item.getItemId() == R.id.action_all_recipes) {
            // Afișează toate rețetele
            recipes = recipeRepository.getAllRecipes();
            recipeAdapter.updateRecipes(recipes);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void filterRecipes(String query) {
        if (query == null || query.isEmpty()) {
            recipes = recipeRepository.getAllRecipes();
        } else {
            recipes = recipeRepository.searchRecipes(query);
        }
        recipeAdapter.updateRecipes(recipes);
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        // Navigate to recipe detail
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        
        // Use a shared element transition if possible
        View recipeImage = findViewById(R.id.recipeImageView);
        if (recipeImage != null) {
            TransitionHelper.startActivityWithSharedElement(this, intent, recipeImage, "recipeImage");
        } else {
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh recipes in case favorites status changed
        loadRecipes();
    }

    private void showAdvancedSearchDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_advanced_search, null);
        
        // Inițializare controale
        EditText maxTimeEditText = dialogView.findViewById(R.id.edit_max_time);
        CheckBox vegetarianCheckbox = dialogView.findViewById(R.id.checkbox_vegetarian);
        CheckBox veganCheckbox = dialogView.findViewById(R.id.checkbox_vegan);
        CheckBox glutenFreeCheckbox = dialogView.findViewById(R.id.checkbox_gluten_free);
        CheckBox lactoseFreeCheckbox = dialogView.findViewById(R.id.checkbox_lactose_free);
        EditText ingredientsEditText = dialogView.findViewById(R.id.edit_ingredients);
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Căutare avansată")
                .setView(dialogView)
                .setPositiveButton("Caută", (dialog, which) -> {
                    // Colectare parametri de căutare
                    int maxTime = 0;
                    try {
                        String maxTimeStr = maxTimeEditText.getText().toString().trim();
                        if (!maxTimeStr.isEmpty()) {
                            maxTime = Integer.parseInt(maxTimeStr);
                        }
                    } catch (NumberFormatException e) {
                        // Ignoră
                    }
                    
                    boolean isVegetarian = vegetarianCheckbox.isChecked();
                    boolean isVegan = veganCheckbox.isChecked();
                    boolean isGlutenFree = glutenFreeCheckbox.isChecked();
                    boolean isLactoseFree = lactoseFreeCheckbox.isChecked();
                    
                    String ingredientsStr = ingredientsEditText.getText().toString().trim();
                    List<String> requiredIngredients = new ArrayList<>();
                    if (!ingredientsStr.isEmpty()) {
                        String[] ingredients = ingredientsStr.split(",");
                        for (String ingredient : ingredients) {
                            requiredIngredients.add(ingredient.trim().toLowerCase());
                        }
                    }
                    
                    // Aplicare filtre
                    advancedFilterRecipes(maxTime, isVegetarian, isVegan, isGlutenFree, isLactoseFree, requiredIngredients);
                })
                .setNegativeButton("Anulează", null)
                .show();
    }

    private void advancedFilterRecipes(int maxTime, boolean isVegetarian, boolean isVegan, 
                                      boolean isGlutenFree, boolean isLactoseFree, 
                                      List<String> requiredIngredients) {
        // Resetare toate filtrele din interfață
        resetFilters();
        
        // Obținere toate rețetele
        List<Recipe> allRecipes = recipeRepository.getAllRecipes();
        List<Recipe> filteredRecipes = new ArrayList<>();
        
        for (Recipe recipe : allRecipes) {
            boolean matchesFilters = true;
            
            // Verificare timp de preparare
            if (maxTime > 0 && recipe.getTotalTime() > maxTime) {
                matchesFilters = false;
                continue;
            }
            
            // Verificare restricții alimentare
            if (isVegetarian && !recipe.isVegetarian()) {
                matchesFilters = false;
                continue;
            }
            
            if (isVegan && !recipe.isVegan()) {
                matchesFilters = false;
                continue;
            }
            
            if (isGlutenFree && !recipe.isGlutenFree()) {
                matchesFilters = false;
                continue;
            }
            
            if (isLactoseFree && !recipe.isLactoseFree()) {
                matchesFilters = false;
                continue;
            }
            
            // Verificare ingrediente necesare
            if (!requiredIngredients.isEmpty()) {
                List<Ingredient> recipeIngredients = recipe.getIngredients();
                
                for (String requiredIngredient : requiredIngredients) {
                    boolean hasIngredient = false;
                    
                    for (Ingredient ingredient : recipeIngredients) {
                        if (ingredient.getName().toLowerCase().contains(requiredIngredient)) {
                            hasIngredient = true;
                            break;
                        }
                    }
                    
                    if (!hasIngredient) {
                        matchesFilters = false;
                        break;
                    }
                }
            }
            
            if (matchesFilters) {
                filteredRecipes.add(recipe);
            }
        }
        
        // Actualizare adapter cu rezultatele filtrării
        recipeAdapter.setRecipes(filteredRecipes);
        updateEmptyViewVisibility();
    }

    private void resetFilters() {
        // Resetare chipuri de filtrare
        favoritesChip.setChecked(false);
        aperitiveChip.setChecked(false);
        soupChip.setChecked(false);
        mainChip.setChecked(false);
        dessertChip.setChecked(false);
        
        // Resetare text căutare
        searchEditText.setText("");
    }
} 