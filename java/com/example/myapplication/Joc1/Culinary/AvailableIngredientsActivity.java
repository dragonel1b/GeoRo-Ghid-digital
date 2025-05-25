package com.example.myapplication.Joc1.Culinary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Joc1.Culinary.RecipeSuggestionActivity;
import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AvailableIngredientsActivity extends AppCompatActivity {
    private RecyclerView ingredientsRecyclerView;
    private MaterialButton findRecipesButton;
    private MaterialButton addToShoppingListButton;
    private ChipGroup categoryChipGroup;
    
    private List<String> allIngredients;
    private Set<String> selectedIngredients;
    private Map<String, List<String>> ingredientsByCategory;
    
    private static final String PREF_AVAILABLE_INGREDIENTS = "available_ingredients";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_ingredients);
        
        // Initialize views
        ingredientsRecyclerView = findViewById(R.id.ingredientsRecyclerView);
        findRecipesButton = findViewById(R.id.findRecipesButton);
        addToShoppingListButton = findViewById(R.id.addToShoppingListButton);
        categoryChipGroup = findViewById(R.id.ingredientCategoryChipGroup);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Ce pot găti cu ce am?");
        
        // Initialize data
        initializeIngredientCategories();
        loadSavedIngredients();
        
        // Set up category filter
        setupCategoryFilter();
        
        // Set up ingredients list
        setupIngredientsRecyclerView();
        
        // Set up buttons
        setupButtons();
    }
    
    private void initializeIngredientCategories() {
        ingredientsByCategory = new HashMap<>();
        
        // Common meat ingredients
        ingredientsByCategory.put("Carne", Arrays.asList(
            "Carne de pui", "Carne de porc", "Carne de vită", 
            "Carne de miel", "Carne tocată", "Cârnați", "Slănină", "Bacon"
        ));
        
        // Dairy ingredients
        ingredientsByCategory.put("Lactate", Arrays.asList(
            "Lapte", "Smântână", "Unt", "Brânză de vaci", 
            "Brânză telemea", "Brânză de burduf", "Cașcaval", "Caș"
        ));
        
        // Vegetables
        ingredientsByCategory.put("Legume", Arrays.asList(
            "Cartofi", "Ceapă", "Usturoi", "Morcovi", "Ardei", 
            "Roșii", "Varză", "Praz", "Fasole", "Mazăre", "Ciuperci"
        ));
        
        // Cereals and flour
        ingredientsByCategory.put("Cereale și făină", Arrays.asList(
            "Făină albă", "Făină integrală", "Mălai", "Orez", 
            "Griș", "Paste", "Pâine"
        ));
        
        // Spices and herbs
        ingredientsByCategory.put("Condimente", Arrays.asList(
            "Sare", "Piper", "Boia", "Cimbru", "Mărar", 
            "Pătrunjel", "Leuștean", "Tarhon", "Paprika"
        ));
        
        // Other ingredients
        ingredientsByCategory.put("Altele", Arrays.asList(
            "Ouă", "Ulei", "Bulion", "Măsline", "Stafide", 
            "Nucă", "Mac", "Lămâie", "Miere"
        ));
        
        // Create a master list of all ingredients
        allIngredients = new ArrayList<>();
        for (List<String> ingredients : ingredientsByCategory.values()) {
            allIngredients.addAll(ingredients);
        }
        
        // Initialize selected ingredients set
        selectedIngredients = new HashSet<>();
    }
    
    private void loadSavedIngredients() {
        SharedPreferences prefs = getSharedPreferences("culinary_preferences", MODE_PRIVATE);
        Set<String> saved = prefs.getStringSet(PREF_AVAILABLE_INGREDIENTS, new HashSet<>());
        selectedIngredients = new HashSet<>(saved);
    }
    
    private void saveSelectedIngredients() {
        SharedPreferences prefs = getSharedPreferences("culinary_preferences", MODE_PRIVATE);
        prefs.edit().putStringSet(PREF_AVAILABLE_INGREDIENTS, selectedIngredients).apply();
    }
    
    private void setupCategoryFilter() {
        // Add a chip for "All ingredients"
        Chip allChip = new Chip(this);
        allChip.setText("Toate");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setChipBackgroundColorResource(R.color.rom_chip_background);
        categoryChipGroup.addView(allChip);
        
        // Add chips for each ingredient category
        for (String category : ingredientsByCategory.keySet()) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.rom_chip_background);
            categoryChipGroup.addView(chip);
        }
        
        // Set up listener to filter ingredients by category
        categoryChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                // If no chip is selected, select "All" by default
                allChip.setChecked(true);
                return;
            }
            
            Chip selectedChip = findViewById(checkedId);
            String selectedCategory = selectedChip.getText().toString();
            
            // Filter ingredients based on selected category
            List<String> filteredIngredients;
            if (selectedCategory.equals("Toate")) {
                filteredIngredients = allIngredients;
            } else {
                filteredIngredients = ingredientsByCategory.get(selectedCategory);
            }
            
            // Update the adapter
            IngredientsAdapter adapter = (IngredientsAdapter) ingredientsRecyclerView.getAdapter();
            if (adapter != null) {
                adapter.updateIngredients(filteredIngredients);
            }
        });
    }
    
    private void setupIngredientsRecyclerView() {
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        IngredientsAdapter adapter = new IngredientsAdapter(allIngredients, selectedIngredients, 
            ingredient -> {
                // Toggle ingredient selection
                if (selectedIngredients.contains(ingredient)) {
                    selectedIngredients.remove(ingredient);
                } else {
                    selectedIngredients.add(ingredient);
                }
                
                // Update button state based on selection
                updateButtonsState();
            });
            
        ingredientsRecyclerView.setAdapter(adapter);
        
        // Update buttons initially
        updateButtonsState();
    }
    
    private void updateButtonsState() {
        boolean hasSelection = !selectedIngredients.isEmpty();
        findRecipesButton.setEnabled(hasSelection);
        addToShoppingListButton.setEnabled(hasSelection);
    }
    
    private void setupButtons() {
        findRecipesButton.setOnClickListener(v -> {
            // Save the current selection of ingredients
            saveSelectedIngredients();
            
            // Start the RecipeSuggestionActivity
            Intent intent = new Intent(this, RecipeSuggestionActivity.class);
            startActivity(intent);
        });
        
        addToShoppingListButton.setOnClickListener(v -> {
            // Logic to add selected ingredients to shopping list
            Toast.makeText(this, "Ingrediente adăugate în lista de cumpărături", Toast.LENGTH_SHORT).show();
            
            // Here you would implement integration with a ShoppingListActivity
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    // Adapter for ingredients list with checkboxes
    private static class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {
        private List<String> ingredients;
        private final Set<String> selectedIngredients;
        private final OnIngredientClickListener listener;
        
        interface OnIngredientClickListener {
            void onIngredientClick(String ingredient);
        }
        
        IngredientsAdapter(List<String> ingredients, Set<String> selectedIngredients, 
                          OnIngredientClickListener listener) {
            this.ingredients = ingredients;
            this.selectedIngredients = selectedIngredients;
            this.listener = listener;
        }
        
        void updateIngredients(List<String> newIngredients) {
            this.ingredients = newIngredients;
            notifyDataSetChanged();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient_checkbox, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String ingredient = ingredients.get(position);
            holder.checkBox.setText(ingredient);
            holder.checkBox.setChecked(selectedIngredients.contains(ingredient));
            
            holder.itemView.setOnClickListener(v -> {
                listener.onIngredientClick(ingredient);
                holder.checkBox.setChecked(selectedIngredients.contains(ingredient));
            });
            
            holder.checkBox.setOnClickListener(v -> {
                listener.onIngredientClick(ingredient);
            });
        }
        
        @Override
        public int getItemCount() {
            return ingredients.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;
            
            ViewHolder(View itemView) {
                super(itemView);
                checkBox = itemView.findViewById(R.id.ingredientCheckbox);
            }
        }
    }
} 