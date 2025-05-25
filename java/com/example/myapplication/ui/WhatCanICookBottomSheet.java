package com.example.myapplication.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.example.myapplication.R;
import com.example.myapplication.adapter.SuggestedRecipeAdapter;
import com.example.myapplication.model.Ingredient;
import com.example.myapplication.model.Recipe;
import com.example.myapplication.viewmodel.RecipeViewModel;
import com.example.myapplication.viewmodel.ShoppingListViewModel;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.color.DynamicColors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bottom sheet dialog that allows users to select available ingredients
 * and view suggested recipes based on those ingredients.
 */
public class WhatCanICookBottomSheet extends BottomSheetDialogFragment {
    
    private RecipeViewModel recipeViewModel;
    private ShoppingListViewModel shoppingListViewModel;
    private FirebaseAnalytics firebaseAnalytics;
    
    // Views
    private MaterialAutoCompleteTextView ingredientsAutoCompleteTextView;
    private ChipGroup ingredientChipGroup;
    private RecyclerView suggestedRecipesRecyclerView;
    private View loadingProgressIndicator;
    private View noRecipesFoundTextView;
    private MaterialButton addMissingIngredientsButton;
    private MaterialButton clearIngredientsButton;
    private View ingredientsScrollView;
    private TextView ingredientsAvailableTitle;
    private TextView suggestedRecipesTitle;
    
    // Data
    private final Set<Ingredient> selectedIngredients = new HashSet<>();
    private List<Recipe> suggestedRecipes = new ArrayList<>();
    private SuggestedRecipeAdapter recipeAdapter;
    private Recipe selectedRecipe;
    
    public static WhatCanICookBottomSheet newInstance() {
        return new WhatCanICookBottomSheet();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply dynamic colors if available (Android 12+)
        if (getContext() != null) {
            DynamicColors.applyToActivityIfAvailable(requireActivity());
        }
        
        // Set up ViewModels
        recipeViewModel = new ViewModelProvider(requireActivity()).get(RecipeViewModel.class);
        shoppingListViewModel = new ViewModelProvider(requireActivity()).get(ShoppingListViewModel.class);
        
        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_what_can_i_cook, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Set accessibility heading for the title
        TextView titleTextView = view.findViewById(R.id.titleTextView);
        ViewCompat.setAccessibilityHeading(titleTextView, true);
        
        // Initialize views
        initializeViews(view);
        
        // Set up ingredient search dropdown
        setupIngredientDropdown();
        
        // Set up suggested recipes
        setupRecyclerView();
        
        // Set up buttons
        setupButtons();
        
        // Observe data changes
        observeViewModel();
    }
    
    private void initializeViews(View view) {
        ingredientsAutoCompleteTextView = view.findViewById(R.id.ingredientsAutoCompleteTextView);
        ingredientChipGroup = view.findViewById(R.id.ingredientChipGroup);
        suggestedRecipesRecyclerView = view.findViewById(R.id.suggestedRecipesRecyclerView);
        loadingProgressIndicator = view.findViewById(R.id.loadingProgressIndicator);
        noRecipesFoundTextView = view.findViewById(R.id.noRecipesFoundTextView);
        addMissingIngredientsButton = view.findViewById(R.id.addMissingIngredientsButton);
        clearIngredientsButton = view.findViewById(R.id.clearIngredientsButton);
        ingredientsScrollView = view.findViewById(R.id.ingredientsScrollView);
        ingredientsAvailableTitle = view.findViewById(R.id.ingredientsAvailableTitle);
        suggestedRecipesTitle = view.findViewById(R.id.suggestedRecipesTitle);
        
        // Set up bottom sheet behavior
        View bottomSheet = view.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        
        // Clear the ChipGroup of sample items
        ingredientChipGroup.removeAllViews();
    }
    
    private void setupIngredientDropdown() {
        // Get all available ingredients from the view model
        List<Ingredient> allIngredients = recipeViewModel.getAllIngredients();
        
        // Create and set the adapter
        IngredientsAdapter adapter = new IngredientsAdapter(requireContext(), 
                android.R.layout.simple_dropdown_item_1line, allIngredients);
        ingredientsAutoCompleteTextView.setAdapter(adapter);
        
        // Set item click listener
        ingredientsAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            Ingredient selectedIngredient = adapter.getItem(position);
            if (selectedIngredient != null && !selectedIngredients.contains(selectedIngredient)) {
                addIngredientChip(selectedIngredient);
                ingredientsAutoCompleteTextView.setText("");
                updateSuggestedRecipes();
                
                // Log analytics event
                Bundle params = new Bundle();
                params.putString("ingredient_name", selectedIngredient.getName());
                firebaseAnalytics.logEvent("ingredient_selected", params);
            }
        });
        
        // Add text change listener to show dropdown again after selection
        ingredientsAutoCompleteTextView.setOnClickListener(v -> 
                ingredientsAutoCompleteTextView.showDropDown());
    }
    
    private void setupRecyclerView() {
        // Create horizontal layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false);
        suggestedRecipesRecyclerView.setLayoutManager(layoutManager);
        
        // Create and set adapter
        recipeAdapter = new SuggestedRecipeAdapter(recipe -> {
            // Handle recipe click
            selectedRecipe = recipe;
            
            // Enable the "Add Missing Ingredients" button if recipe has missing ingredients
            addMissingIngredientsButton.setEnabled(recipe.getMissingIngredients() != null && 
                    !recipe.getMissingIngredients().isEmpty());
            
            // Log analytics event
            Bundle params = new Bundle();
            params.putString("recipe_name", recipe.getName());
            params.putInt("match_score", recipe.getMatchScore());
            firebaseAnalytics.logEvent("recipe_suggested_clicked", params);
            
            // Open recipe detail
            if (getActivity() instanceof RecipeDetailListener) {
                ((RecipeDetailListener) getActivity()).onOpenRecipeDetail(recipe);
                dismiss();
            }
        });
        
        suggestedRecipesRecyclerView.setAdapter(recipeAdapter);
    }
    
    private void setupButtons() {
        // Close button
        View closeButton = requireView().findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());
        
        // Add missing ingredients button
        addMissingIngredientsButton.setOnClickListener(v -> {
            if (selectedRecipe != null && selectedRecipe.getMissingIngredients() != null) {
                // Add missing ingredients to shopping list
                List<Ingredient> missingIngredients = selectedRecipe.getMissingIngredients();
                shoppingListViewModel.addIngredientsToShoppingList(missingIngredients);
                
                // Show success snackbar with undo option
                Snackbar snackbar = Snackbar.make(
                        requireView(),
                        getString(R.string.ingredients_added_count, missingIngredients.size()),
                        Snackbar.LENGTH_LONG);
                
                snackbar.setAction(R.string.undo, view -> {
                    // Undo the addition
                    shoppingListViewModel.removeIngredientsFromShoppingList(missingIngredients);
                });
                
                snackbar.show();
                
                // Log analytics event
                Bundle params = new Bundle();
                params.putString("recipe_name", selectedRecipe.getName());
                params.putInt("ingredients_count", missingIngredients.size());
                firebaseAnalytics.logEvent("missing_ingredients_added", params);
                
                // Close bottom sheet after action
                dismiss();
            }
        });
        
        // Clear ingredients button
        clearIngredientsButton.setOnClickListener(v -> {
            selectedIngredients.clear();
            ingredientChipGroup.removeAllViews();
            updateVisibility();
            recipeAdapter.submitList(Collections.emptyList());
            addMissingIngredientsButton.setEnabled(false);
            selectedRecipe = null;
            
            // Log analytics event
            firebaseAnalytics.logEvent("ingredients_cleared", null);
        });
    }
    
    private void addIngredientChip(Ingredient ingredient) {
        // Add to selected ingredients set
        selectedIngredients.add(ingredient);
        
        // Create a new chip
        Chip chip = new Chip(requireContext());
        chip.setText(ingredient.getName());
        chip.setCheckable(false);
        chip.setCloseIconVisible(true);
        chip.setContentDescription(getString(R.string.ingredient_chip_description, ingredient.getName()));
        
        // Apply Material3 style
        chip.setChipBackgroundColorResource(com.google.android.material.R.color.m3_chip_background_color);
        chip.setCloseIconTint(ContextCompat.getColorStateList(requireContext(), 
                R.color.black));
        
        // Add ripple effect
        chip.setRippleColorResource(com.google.android.material.R.color.m3_chip_ripple_color);
        
        // Set close icon click listener
        chip.setOnCloseIconClickListener(v -> {
            selectedIngredients.remove(ingredient);
            ingredientChipGroup.removeView(chip);
            updateSuggestedRecipes();
            updateVisibility();
            
            // Animate chip removal
            chip.setAlpha(1f);
            chip.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .start();
        });
        
        // Add chip to the group with animation
        chip.setAlpha(0f);
        ingredientChipGroup.addView(chip);
        chip.animate()
                .alpha(1f)
                .setDuration(200)
                .start();
        
        // Update visibility
        updateVisibility();
    }
    
    private void updateVisibility() {
        boolean hasIngredients = !selectedIngredients.isEmpty();
        
        ingredientsScrollView.setVisibility(hasIngredients ? View.VISIBLE : View.GONE);
        ingredientsAvailableTitle.setVisibility(hasIngredients ? View.VISIBLE : View.GONE);
        suggestedRecipesTitle.setVisibility(hasIngredients ? View.VISIBLE : View.GONE);
        clearIngredientsButton.setVisibility(hasIngredients ? View.VISIBLE : View.GONE);
        
        // Suggested recipes section might still be hidden if loading
        if (!hasIngredients) {
            suggestedRecipesRecyclerView.setVisibility(View.GONE);
            noRecipesFoundTextView.setVisibility(View.GONE);
        }
    }
    
    private void updateSuggestedRecipes() {
        if (selectedIngredients.isEmpty()) {
            recipeAdapter.submitList(Collections.emptyList());
            return;
        }
        
        // Show loading indicator
        loadingProgressIndicator.setVisibility(View.VISIBLE);
        suggestedRecipesRecyclerView.setVisibility(View.GONE);
        noRecipesFoundTextView.setVisibility(View.GONE);
        
        // Get suggested recipes based on selected ingredients
        List<Ingredient> ingredients = new ArrayList<>(selectedIngredients);
        
        // Simulate network delay (remove in production)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Get suggested recipes from view model
            suggestedRecipes = recipeViewModel.getSuggestedRecipesForIngredients(ingredients);
            
            // Hide loading indicator
            loadingProgressIndicator.setVisibility(View.GONE);
            
            // Check if we found any recipes
            if (suggestedRecipes.isEmpty()) {
                noRecipesFoundTextView.setVisibility(View.VISIBLE);
                suggestedRecipesRecyclerView.setVisibility(View.GONE);
            } else {
                noRecipesFoundTextView.setVisibility(View.GONE);
                suggestedRecipesRecyclerView.setVisibility(View.VISIBLE);
                recipeAdapter.submitList(suggestedRecipes);
                
                // Log analytics event
                Bundle params = new Bundle();
                params.putInt("recipes_count", suggestedRecipes.size());
                params.putInt("ingredients_count", selectedIngredients.size());
                firebaseAnalytics.logEvent("recipes_suggested", params);
            }
        }, 800); // Simulated delay
    }
    
    private void observeViewModel() {
        // No live data to observe in this simplified example
        // In a real app, you would observe LiveData from the ViewModel
    }
    
    /**
     * Adapter for the ingredients dropdown with filtering capability
     */
    private static class IngredientsAdapter extends ArrayAdapter<Ingredient> implements Filterable {
        private final List<Ingredient> originalList;
        private List<Ingredient> filteredList;
        
        public IngredientsAdapter(Context context, int resource, List<Ingredient> objects) {
            super(context, resource, objects);
            this.originalList = new ArrayList<>(objects);
            this.filteredList = new ArrayList<>(objects);
        }
        
        @Override
        public int getCount() {
            return filteredList.size();
        }
        
        @Override
        public Ingredient getItem(int position) {
            return filteredList.get(position);
        }
        
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    
                    if (TextUtils.isEmpty(constraint)) {
                        // No filter, return all items
                        results.values = originalList;
                        results.count = originalList.size();
                    } else {
                        // Filter by constraint
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        List<Ingredient> filteredItems = originalList.stream()
                                .filter(ingredient -> 
                                        ingredient.getName().toLowerCase().contains(filterPattern))
                                .collect(Collectors.toList());
                        
                        results.values = filteredItems;
                        results.count = filteredItems.size();
                    }
                    
                    return results;
                }
                
                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredList = (List<Ingredient>) results.values;
                    notifyDataSetChanged();
                }
            };
        }
        
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView textView = (TextView) super.getView(position, convertView, parent);
            textView.setText(getItem(position).getName());
            return textView;
        }
    }
    
    /**
     * Interface for communication with hosting activity
     */
    public interface RecipeDetailListener {
        void onOpenRecipeDetail(Recipe recipe);
    }
} 