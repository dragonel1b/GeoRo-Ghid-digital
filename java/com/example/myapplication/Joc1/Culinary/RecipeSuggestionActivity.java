package com.example.myapplication.Joc1.Culinary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.myapplication.Joc1.Culinary.UserCulinaryProfile;
import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity;

public class RecipeSuggestionActivity extends AppCompatActivity {
    private RecyclerView recipesRecyclerView;
    private MaterialCardView noRecipesFoundCard;
    private TextView noRecipesFoundText;
    private MaterialButton addIngredientsButton;
    private ExtendedFloatingActionButton addIngredientsExtendedFab;
    private ChipGroup sortChipGroup;
    private ChipGroup mealTypeChipGroup;
    private ChipGroup categoryChipGroup;
    private Chip breakfastChip;
    private Chip lunchChip;
    private Chip dinnerChip;
    private Chip currentMealChip;
    
    private List<RecipeMatch> recipeMatches;
    private List<RecipeMatch> filteredRecipeMatches;
    private Set<String> availableIngredients;
    private UserCulinaryProfile userProfile;
    private String currentMealType;
    private Set<String> selectedMealTypes;
    private Set<String> selectedCategories;
    
    private static final String PREF_AVAILABLE_INGREDIENTS = "available_ingredients";
    private static final String PREF_FILTER_SETTINGS = "recipe_filter_settings";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_suggestion);
        
        // Initialize views
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView);
        noRecipesFoundCard = findViewById(R.id.noRecipesFoundCard);
        noRecipesFoundText = findViewById(R.id.noRecipesFoundText);
        addIngredientsButton = findViewById(R.id.addIngredientsButton);
        addIngredientsExtendedFab = findViewById(R.id.addIngredientsExtendedFab);
        sortChipGroup = findViewById(R.id.sortChipGroup);
        mealTypeChipGroup = findViewById(R.id.mealTypeChipGroup);
        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        breakfastChip = findViewById(R.id.breakfastChip);
        lunchChip = findViewById(R.id.lunchChip);
        dinnerChip = findViewById(R.id.dinnerChip);
        currentMealChip = findViewById(R.id.currentMealChip);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Rețete personalizate");
        
        // Get user profile
        userProfile = UserCulinaryProfile.getInstance(this);
        
        // Load available ingredients
        loadAvailableIngredients();
        
        // Initialize selected filters
        selectedMealTypes = new HashSet<>();
        selectedCategories = new HashSet<>();
        
        // Determine current meal type based on time of day
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        currentMealType = getMealTypeForHour(hourOfDay);
        
        // Set current meal chip text based on time
        updateCurrentMealChip();
        
        // Setup category filtering
        setupCategoryFiltering();
        
        // Setup meal type filtering
        setupMealTypeFiltering();
        
        // Find matching recipes
        findMatchingRecipes();
        
        // Set up sorting options
        setupSortingOptions();
        
        // Set up recipes list
        setupRecipesRecyclerView();
        
        // Set up FAB and button actions
        setupButtons();
        
        // Load saved filters if available
        loadSavedFilters();
    }
    
    private void setupButtons() {
        addIngredientsButton.setOnClickListener(v -> openAddIngredientsScreen());
        addIngredientsExtendedFab.setOnClickListener(v -> openAddIngredientsScreen());
        
        // Hide/Show FAB on scroll
        recipesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && addIngredientsExtendedFab.isExtended()) {
                    addIngredientsExtendedFab.shrink();
                } else if (dy < 0 && !addIngredientsExtendedFab.isExtended()) {
                    addIngredientsExtendedFab.extend();
                }
            }
        });
    }
    
    private void openAddIngredientsScreen() {
        Intent intent = new Intent(this, AvailableIngredientsActivity.class);
        startActivity(intent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_filter, menu);
        return true;
    }

    private void loadSavedFilters() {
        SharedPreferences prefs = getSharedPreferences("culinary_preferences", MODE_PRIVATE);
        Set<String> savedMealTypes = prefs.getStringSet(PREF_FILTER_SETTINGS + "_meals", null);
        Set<String> savedCategories = prefs.getStringSet(PREF_FILTER_SETTINGS + "_categories", null);
        
        if (savedMealTypes != null) {
            selectedMealTypes.clear();
            selectedMealTypes.addAll(savedMealTypes);
            
            // Update chip states to match saved filters
            breakfastChip.setChecked(selectedMealTypes.contains(UserCulinaryProfile.MEAL_BREAKFAST));
            lunchChip.setChecked(selectedMealTypes.contains(UserCulinaryProfile.MEAL_LUNCH));
            dinnerChip.setChecked(selectedMealTypes.contains(UserCulinaryProfile.MEAL_DINNER));
            currentMealChip.setChecked(selectedMealTypes.contains(currentMealType));
        }
        
        if (savedCategories != null && categoryChipGroup != null) {
            selectedCategories.clear();
            selectedCategories.addAll(savedCategories);
            
            // Update category chip states
            for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) categoryChipGroup.getChildAt(i);
                String category = chip.getText().toString();
                chip.setChecked(selectedCategories.contains(category) || chip.getId() == R.id.filterChipAll);
            }
        }
        
        // Apply filters
        applyAllFilters();
    }
    
    private void saveFilters() {
        SharedPreferences prefs = getSharedPreferences("culinary_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putStringSet(PREF_FILTER_SETTINGS + "_meals", selectedMealTypes);
        editor.putStringSet(PREF_FILTER_SETTINGS + "_categories", selectedCategories);
        
        editor.apply();
        
        Snackbar.make(recipesRecyclerView, "Filtrele au fost salvate", Snackbar.LENGTH_SHORT).show();
    }
    
    private void clearFilters() {
        // Reset all chip selections except "All" category
        for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) categoryChipGroup.getChildAt(i);
            chip.setChecked(chip.getId() == R.id.filterChipAll);
        }
        
        // Clear meal type selections
        mealTypeChipGroup.clearCheck();
        currentMealChip.setChecked(true);
        
        // Reset collections
        selectedMealTypes.clear();
        selectedMealTypes.add(currentMealType);
        selectedCategories.clear();
        
        // Apply filters
        applyAllFilters();
        
        // Clear saved preferences
        SharedPreferences prefs = getSharedPreferences("culinary_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PREF_FILTER_SETTINGS + "_meals");
        editor.remove(PREF_FILTER_SETTINGS + "_categories");
        editor.apply();
        
        Snackbar.make(recipesRecyclerView, "Filtrele au fost resetate", Snackbar.LENGTH_SHORT).show();
    }
    
    private void updateCurrentMealChip() {
        String chipText = "Ora actuală";
        
        switch (currentMealType) {
            case UserCulinaryProfile.MEAL_BREAKFAST:
                chipText = "Ora actuală (Mic dejun)";
                break;
            case UserCulinaryProfile.MEAL_LUNCH:
                chipText = "Ora actuală (Prânz)";
                break;
            case UserCulinaryProfile.MEAL_DINNER:
                chipText = "Ora actuală (Cină)";
                break;
            case UserCulinaryProfile.MEAL_SNACK:
                chipText = "Ora actuală (Gustare)";
                break;
        }
        
        currentMealChip.setText(chipText);
    }
    
    private void setupCategoryFiltering() {
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            selectedCategories.clear();
            boolean hasAllSelected = false;
            
            for (int id : checkedIds) {
                Chip chip = findViewById(id);
                String category = chip.getText().toString();
                
                if (id == R.id.filterChipAll) {
                    hasAllSelected = true;
                } else {
                    selectedCategories.add(category);
                }
            }
            
            // If "All" is selected, clear other selections
            if (hasAllSelected) {
                for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) categoryChipGroup.getChildAt(i);
                    if (chip.getId() != R.id.filterChipAll) {
                        chip.setChecked(false);
                    }
                }
                selectedCategories.clear();
            }
            
            // Apply all filters
            applyAllFilters();
        });
    }
    
    private void setupMealTypeFiltering() {
        // Set initial state - current meal selected by default
        if (currentMealChip.isChecked()) {
            selectedMealTypes.add(currentMealType);
        }
        
        // Set up listeners for meal type chips
        mealTypeChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            selectedMealTypes.clear();
            
            for (int id : checkedIds) {
                if (id == R.id.breakfastChip) {
                    selectedMealTypes.add(UserCulinaryProfile.MEAL_BREAKFAST);
                } else if (id == R.id.lunchChip) {
                    selectedMealTypes.add(UserCulinaryProfile.MEAL_LUNCH);
                } else if (id == R.id.dinnerChip) {
                    selectedMealTypes.add(UserCulinaryProfile.MEAL_DINNER);
                } else if (id == R.id.currentMealChip) {
                    selectedMealTypes.add(currentMealType);
                }
            }
            
            // Apply all filters together
            applyAllFilters();
        });
    }
    
    private void applyAllFilters() {
        // Start with all recipes
        filteredRecipeMatches = new ArrayList<>(recipeMatches);
        
        // Apply meal type filtering if needed
        if (!selectedMealTypes.isEmpty()) {
            List<RecipeMatch> mealFiltered = new ArrayList<>();
            
            for (RecipeMatch match : filteredRecipeMatches) {
                String category = match.getRecipe().getCategory();
                String mealType = getCategoryMealType(category);
                
                if (mealType != null && selectedMealTypes.contains(mealType)) {
                    mealFiltered.add(match);
                }
            }
            
            filteredRecipeMatches = mealFiltered;
        }
        
        // Apply category filtering if needed
        if (!selectedCategories.isEmpty()) {
            List<RecipeMatch> categoryFiltered = new ArrayList<>();
            
            for (RecipeMatch match : filteredRecipeMatches) {
                String recipeCategory = match.getRecipe().getCategory();
                boolean matches = false;
                
                for (String filterCategory : selectedCategories) {
                    if (categoryMatches(recipeCategory, filterCategory)) {
                        matches = true;
                        break;
                    }
                }
                
                if (matches) {
                    categoryFiltered.add(match);
                }
            }
            
            filteredRecipeMatches = categoryFiltered;
        }
        
        // Update the RecyclerView with filtered recipes
        RecipeMatchAdapter adapter = (RecipeMatchAdapter) recipesRecyclerView.getAdapter();
        if (adapter != null) {
            adapter.updateData(filteredRecipeMatches);
        }
        
        // Show/hide "no recipes" message
        noRecipesFoundCard.setVisibility(filteredRecipeMatches.isEmpty() ? View.VISIBLE : View.GONE);
    }
    
    private boolean categoryMatches(String recipeCategory, String filterCategory) {
        if (recipeCategory == null) return false;
        
        recipeCategory = recipeCategory.toLowerCase();
        filterCategory = filterCategory.toLowerCase();
        
        switch (filterCategory) {
            case "tradiționale":
                return recipeCategory.contains("tradițional") || 
                       recipeCategory.contains("traditional") ||
                       recipeCategory.contains("autentic");
            case "rapid":
                return recipeCategory.contains("rapid") || 
                       recipeCategory.contains("quick") ||
                       recipeCategory.contains("30 minute");
            case "vegetarian":
                return recipeCategory.contains("vegetarian") || 
                       recipeCategory.contains("vegan") ||
                       recipeCategory.contains("legume");
            case "de sezon":
                // Check current season and match seasonal recipes
                int month = Calendar.getInstance().get(Calendar.MONTH);
                String season = getSeasonForMonth(month);
                
                return recipeCategory.contains(season) || 
                       recipeCategory.contains("sezon") ||
                       recipeCategory.contains("seasonal");
        }
        
        return false;
    }
    
    private String getSeasonForMonth(int month) {
        // 0-based month index: 0 = January, 11 = December
        if (month >= 2 && month <= 4) return "primăvară"; // Spring
        if (month >= 5 && month <= 7) return "vară";      // Summer
        if (month >= 8 && month <= 10) return "toamnă";   // Fall
        return "iarnă";                                   // Winter
    }
    
    /**
     * Determine meal type from recipe category
     */
    private String getCategoryMealType(String category) {
        if (category == null) {
            return null;
        }
        
        category = category.toLowerCase();
        
        if (category.contains("mic dejun") || category.contains("breakfast") || 
            category.contains("patiserie") || category.contains("pâine")) {
            return UserCulinaryProfile.MEAL_BREAKFAST;
        } else if (category.contains("felul principal") || category.contains("supe") || 
                 category.contains("ciorbe") || category.contains("aperitive")) {
            // For lunch and dinner, we need to differentiate
            // For simplicity, we'll say soups are more lunch-oriented, while main dishes can be both
            if (category.contains("supe") || category.contains("ciorbe")) {
                return UserCulinaryProfile.MEAL_LUNCH;
            } else {
                // Return the current meal type between lunch and dinner
                return (currentMealType.equals(UserCulinaryProfile.MEAL_LUNCH) || 
                        currentMealType.equals(UserCulinaryProfile.MEAL_DINNER)) ? 
                        currentMealType : UserCulinaryProfile.MEAL_DINNER;
            }
        } else if (category.contains("desert") || category.contains("prăjitură") || 
                 category.contains("dulce")) {
            return UserCulinaryProfile.MEAL_SNACK;
        }
        
        // Default to current meal type if we can't determine
        return currentMealType;
    }
    
    /**
     * Determine the meal type based on hour of day
     */
    private String getMealTypeForHour(int hour) {
        if (hour >= 5 && hour < 11) {
            return UserCulinaryProfile.MEAL_BREAKFAST;
        } else if (hour >= 11 && hour < 16) {
            return UserCulinaryProfile.MEAL_LUNCH;
        } else if (hour >= 16 && hour < 22) {
            return UserCulinaryProfile.MEAL_DINNER;
        } else {
            return UserCulinaryProfile.MEAL_SNACK;
        }
    }
    
    private void loadAvailableIngredients() {
        SharedPreferences prefs = getSharedPreferences("culinary_preferences", MODE_PRIVATE);
        availableIngredients = new HashSet<>(prefs.getStringSet(PREF_AVAILABLE_INGREDIENTS, new HashSet<>()));
    }
    
    private void findMatchingRecipes() {
        // Create a list to hold recipes with their match score
        recipeMatches = new ArrayList<>();
        
        // Get all recipes from ModernCulinaryActivity
        List<ModernCulinaryActivity.Recipe> allRecipes = getAllRecipes();
        
        // Get user dietary preferences and allergies
        Set<String> dietaryPreferences = userProfile.getDietaryPreferences();
        Set<String> allergies = userProfile.getAllergies();
        String userSkillLevel = userProfile.getSkillLevel();
        Set<String> favoriteMealTimes = userProfile.getFavoriteMealTimes();
        
        // Get current time to suggest appropriate meal types
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        String currentMealType = getMealTypeForHour(hourOfDay);
        
        for (ModernCulinaryActivity.Recipe recipe : allRecipes) {
            // Check dietary restrictions first - skip if incompatible
            if (!isDietCompatible(recipe, dietaryPreferences)) {
                continue;
            }
            
            // Check for allergens - skip if contains allergens
            if (containsAllergens(recipe, allergies)) {
                continue;
            }
            
            // Calculate how many ingredients we have from the recipe
            int matchedIngredients = 0;
            Set<String> missingIngredients = new HashSet<>();
            
            for (String recipeIngredient : recipe.getIngredients()) {
                boolean matched = false;
                
                // Check if any available ingredient is contained in the recipe ingredient
                for (String availableIngredient : availableIngredients) {
                    if (recipeIngredient.toLowerCase().contains(availableIngredient.toLowerCase())) {
                        matchedIngredients++;
                        matched = true;
                        break;
                    }
                }
                
                if (!matched) {
                    missingIngredients.add(recipeIngredient);
                }
            }
            
            // Calculate match percentage
            int totalIngredients = recipe.getIngredients().length;
            float matchPercentage = totalIngredients > 0 ? 
                    (float) matchedIngredients / totalIngredients * 100 : 0;
            
            // Calculate skill level compatibility
            int skillCompatibility = calculateSkillCompatibility(recipe.getDifficulty(), userSkillLevel);
            
            // Get meal type from recipe category
            String recipeMealType = getCategoryMealType(recipe.getCategory());
            
            // Calculate meal time relevance
            boolean isRelevantMealTime = isMealTimeRelevant(recipe.getCategory(), currentMealType, favoriteMealTimes);
            
            // Only add recipes with at least 30% match (lower threshold for personalized matches)
            if (matchPercentage >= 30) {
                RecipeMatch match = new RecipeMatch(
                        recipe, 
                        matchPercentage, 
                        matchedIngredients, 
                        totalIngredients,
                        missingIngredients);
                        
                // Set additional personalization attributes
                match.setSkillCompatibility(skillCompatibility);
                match.setRelevantMealTime(isRelevantMealTime);
                match.setMealType(recipeMealType);
                
                recipeMatches.add(match);
            }
        }
        
        // Sort by personalized score (highest first)
        sortByPersonalizedScore();
        
        // Set filtered recipes to all recipes initially
        filteredRecipeMatches = new ArrayList<>(recipeMatches);
        
        // Apply filtering based on currently selected meal types
        if (!selectedMealTypes.isEmpty()) {
            applyAllFilters();
        }
    }
    
    /**
     * Sort recipes by personalized score taking into account match percentage,
     * skill compatibility, and meal time relevance
     */
    private void sortByPersonalizedScore() {
        Collections.sort(recipeMatches, (a, b) -> {
            // Calculate personalized scores
            double scoreA = calculatePersonalizedScore(a);
            double scoreB = calculatePersonalizedScore(b);
            
            // Sort by descending score
            return Double.compare(scoreB, scoreA);
        });
    }
    
    /**
     * Calculate a personalized score for a recipe match
     */
    private double calculatePersonalizedScore(RecipeMatch match) {
        // Base score is the match percentage (0-100)
        double score = match.getMatchPercentage();
        
        // Add bonus for skill compatibility (0-20) - more bonus for optimal skill match
        int skillComp = match.getSkillCompatibility();
        if (skillComp == 4) { // Perfect match
            score += 20; 
        } else if (skillComp == 3) {
            score += 15;
        } else if (skillComp == 2) {
            score += 10;
        } else if (skillComp == 1) {
            score += 5;
        }
        
        // Add significant bonus for meal time relevance (0 or 25)
        if (match.isRelevantMealTime()) {
            score += 25;
        }
        
        // Add bonus for smaller number of missing ingredients
        int missingCount = match.getMissingIngredients().size();
        if (missingCount == 0) {
            score += 15;
        } else if (missingCount <= 2) {
            score += 10;
        } else if (missingCount <= 4) {
            score += 5;
        }
        
        return score;
    }
    
    /**
     * Check if recipe category is relevant for current meal time or user's favorite times
     */
    private boolean isMealTimeRelevant(String category, String currentMealTime, Set<String> favoriteMealTimes) {
        // Map recipe categories to meal times
        String mealType = getCategoryMealType(category);
        
        // If we couldn't determine the meal type, it's not specifically relevant
        if (mealType == null) {
            return false;
        }
        
        // Check if it matches current meal time
        if (mealType.equals(currentMealTime)) {
            return true;
        }
        
        // Check if it matches user's favorite meal times
        return favoriteMealTimes.contains(mealType);
    }
    
    /**
     * Calculate skill compatibility score (0-4) based on recipe difficulty and user skill
     */
    private int calculateSkillCompatibility(String recipeDifficulty, String userSkillLevel) {
        int difficultyScore;
        int userSkillScore;
        
        // Convert recipe difficulty to numeric score
        switch (recipeDifficulty.toLowerCase()) {
            case "foarte ușor":
                difficultyScore = 1;
                break;
            case "ușor":
                difficultyScore = 2;
                break;
            case "mediu":
                difficultyScore = 3;
                break;
            case "dificil":
                difficultyScore = 4;
                break;
            case "foarte dificil":
                difficultyScore = 5;
                break;
            default:
                difficultyScore = 3; // Default to medium
        }
        
        // Convert user skill to numeric score
        switch (userSkillLevel) {
            case UserCulinaryProfile.SKILL_BEGINNER:
                userSkillScore = 1;
                break;
            case UserCulinaryProfile.SKILL_INTERMEDIATE:
                userSkillScore = 3;
                break;
            case UserCulinaryProfile.SKILL_ADVANCED:
                userSkillScore = 5;
                break;
            default:
                userSkillScore = 2; // Default
        }
        
        // Calculate skill match score (0-4)
        // Perfect match: Recipe difficulty matches user skill exactly
        if (difficultyScore == userSkillScore) {
            return 4;
        }
        // Good match: Recipe is 1 level above or below user skill
        else if (Math.abs(difficultyScore - userSkillScore) == 1) {
            return 3;
        }
        // Decent match: Recipe is 2 levels from user skill
        else if (Math.abs(difficultyScore - userSkillScore) == 2) {
            return 2;
        }
        // Poor match: Recipe is much harder or much easier than user skill
        else if (Math.abs(difficultyScore - userSkillScore) == 3) {
            return 1;
        }
        // Very poor match
        else {
            return 0;
        }
    }
    
    /**
     * Check if a recipe is compatible with the user's dietary preferences
     */
    private boolean isDietCompatible(ModernCulinaryActivity.Recipe recipe, Set<String> dietaryPreferences) {
        // If no dietary preferences, all recipes are compatible
        if (dietaryPreferences == null || dietaryPreferences.isEmpty()) {
            return true;
        }
        
        // Track ingredients list for extensive checks
        String[] ingredients = recipe.getIngredients();
        String allIngredientsText = String.join(" ", ingredients).toLowerCase();
        
        // Check each dietary preference
        for (String diet : dietaryPreferences) {
            switch (diet) {
                case UserCulinaryProfile.DIET_VEGETARIAN:
                    // Check for non-vegetarian ingredients
                    for (String ingredient : ingredients) {
                        if (containsNonVegetarianIngredient(ingredient)) {
                            return false;
                        }
                    }
                    break;
                    
                case UserCulinaryProfile.DIET_VEGAN:
                    // Stricter than vegetarian - check for any animal products
                    for (String ingredient : ingredients) {
                        if (containsNonVeganIngredient(ingredient)) {
                            return false;
                        }
                    }
                    break;
                    
                case UserCulinaryProfile.DIET_GLUTEN_FREE:
                    // Check for gluten-containing ingredients
                    if (allIngredientsText.contains("făină") || 
                        allIngredientsText.contains("grâu") || 
                        allIngredientsText.contains("secară") ||
                        allIngredientsText.contains("orz") ||
                        allIngredientsText.contains("ovăz") ||
                        allIngredientsText.contains("aluat") ||
                        allIngredientsText.contains("pâine") ||
                        allIngredientsText.contains("biscuiți") ||
                        allIngredientsText.contains("paste") ||
                        allIngredientsText.contains("cozonac") ||
                        allIngredientsText.contains("prăjitură")) {
                        
                        // Exception: if explicitly mentions gluten-free
                        if (allIngredientsText.contains("fără gluten") || 
                            allIngredientsText.contains("făină de porumb") ||
                            allIngredientsText.contains("făină de orez") ||
                            allIngredientsText.contains("făină de mălai")) {
                            // Might be ok, continue checking
                        } else {
                            return false;
                        }
                    }
                    break;
                    
                case UserCulinaryProfile.DIET_LACTOSE_FREE:
                    // Check for dairy products
                    if (allIngredientsText.contains("lapte") ||
                        allIngredientsText.contains("smântână") ||
                        allIngredientsText.contains("iaurt") ||
                        allIngredientsText.contains("brânză") ||
                        allIngredientsText.contains("cașcaval") ||
                        allIngredientsText.contains("unt") ||
                        allIngredientsText.contains("frișcă")) {
                        
                        // Exception: if explicitly mentions lactose-free
                        if (allIngredientsText.contains("fără lactoză") ||
                            allIngredientsText.contains("lapte vegetal")) {
                            // Might be ok, continue checking
                        } else {
                            return false;
                        }
                    }
                    break;
                    
                case UserCulinaryProfile.DIET_KETO:
                    // Check for high-carb ingredients
                    if (allIngredientsText.contains("zahăr") ||
                        allIngredientsText.contains("făină") ||
                        allIngredientsText.contains("paste") ||
                        allIngredientsText.contains("orez") ||
                        allIngredientsText.contains("porumb") ||
                        allIngredientsText.contains("cartofi") ||
                        allIngredientsText.contains("pâine") ||
                        allIngredientsText.contains("miere") ||
                        allIngredientsText.contains("sirop")) {
                        
                        return false;
                    }
                    break;
                    
                case UserCulinaryProfile.DIET_LOW_CARB:
                    // Check for high-carb ingredients, but less strict than keto
                    int carbCounters = 0;
                    if (allIngredientsText.contains("zahăr")) carbCounters++;
                    if (allIngredientsText.contains("făină")) carbCounters++;
                    if (allIngredientsText.contains("paste")) carbCounters++;
                    if (allIngredientsText.contains("orez")) carbCounters++;
                    if (allIngredientsText.contains("cartofi")) carbCounters++;
                    if (allIngredientsText.contains("pâine")) carbCounters++; 
                    
                    // Allow at most one high-carb ingredient for low-carb (not strict keto)
                    if (carbCounters > 1) {
                        return false;
                    }
                    break;
            }
        }
        
        // If all dietary preferences are satisfied, return true
        return true;
    }
    
    /**
     * Check if an ingredient is non-vegetarian
     */
    private boolean containsNonVegetarianIngredient(String ingredient) {
        String lowerIngredient = ingredient.toLowerCase();
        
        // Check for obvious meat sources
        return lowerIngredient.contains("carne") ||
               lowerIngredient.contains("pui") ||
               lowerIngredient.contains("vită") ||
               lowerIngredient.contains("porc") ||
               lowerIngredient.contains("miel") ||
               lowerIngredient.contains("curcan") ||
               lowerIngredient.contains("rață") ||
               lowerIngredient.contains("gâscă") ||
               lowerIngredient.contains("iepure") ||
               lowerIngredient.contains("vânat") ||
               lowerIngredient.contains("șuncă") ||
               lowerIngredient.contains("bacon") ||
               lowerIngredient.contains("cârnați") ||
               lowerIngredient.contains("slănină") ||
               lowerIngredient.contains("pește") ||
               lowerIngredient.contains("fructe de mare") ||
               lowerIngredient.contains("creveti") ||
               lowerIngredient.contains("midii") ||
               lowerIngredient.contains("caracatiță") ||
               lowerIngredient.contains("calamari") ||
               lowerIngredient.contains("ton") ||
               lowerIngredient.contains("sardine") ||
               lowerIngredient.contains("scoici") ||
               lowerIngredient.contains("file") && 
               (lowerIngredient.contains("de pui") || 
                lowerIngredient.contains("de pește") || 
                lowerIngredient.contains("de vită"));
    }
    
    /**
     * Check if an ingredient is non-vegan
     */
    private boolean containsNonVeganIngredient(String ingredient) {
        String lowerIngredient = ingredient.toLowerCase();
        
        // Non-vegan includes non-vegetarian plus dairy and eggs
        return containsNonVegetarianIngredient(ingredient) ||
               lowerIngredient.contains("lapte") ||
               lowerIngredient.contains("brânză") ||
               lowerIngredient.contains("smântână") ||
               lowerIngredient.contains("iaurt") ||
               lowerIngredient.contains("unt") ||
               lowerIngredient.contains("frișcă") ||
               lowerIngredient.contains("cașcaval") ||
               lowerIngredient.contains("parmezan") ||
               lowerIngredient.contains("ou") ||
               lowerIngredient.contains("ouă") ||
               lowerIngredient.contains("albuș") ||
               lowerIngredient.contains("gălbenuș") ||
               lowerIngredient.contains("miere");
    }
    
    /**
     * Check if recipe contains allergens
     */
    private boolean containsAllergens(ModernCulinaryActivity.Recipe recipe, Set<String> allergies) {
        if (allergies.isEmpty()) {
            return false;
        }
        
        for (String ingredient : recipe.getIngredients()) {
            String lowerIngredient = ingredient.toLowerCase();
            
            for (String allergy : allergies) {
                if (lowerIngredient.contains(allergy.toLowerCase())) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private List<ModernCulinaryActivity.Recipe> getAllRecipes() {
        // This is a simplified approach. In a real app, you would get recipes from a repository.
        // For now, we'll recreate the recipes similar to how they are created in ModernCulinaryActivity.
        List<ModernCulinaryActivity.Recipe> recipes = new ArrayList<>();
        
        // Moldova recipes
        recipes.add(new ModernCulinaryActivity.Recipe(
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
        
        // Oltenia recipes
        recipes.add(new ModernCulinaryActivity.Recipe(
                "Praz cu Măsline",
                "Oltenia",
                "Felul principal",
                "Mâncare oltenească de post cu praz și măsline",
                "Ușor",
                "45 min",
                new String[] {
                        "Praz",
                        "Măsline negre",
                        "Ceapă",
                        "Bulion",
                        "Ulei de măsline",
                        "Pătrunjel"
                },
                new String[] {
                        "Se călește ceapa și prazul",
                        "Se adaugă bulionul și puțină apă",
                        "Se fierbe până când prazul devine moale",
                        "Se adaugă măslinele și se mai fierbe 5-10 minute"
                }
        ));
        
        // Banat recipes
        recipes.add(new ModernCulinaryActivity.Recipe(
                "Papricaș de Pui",
                "Banat",
                "Felul principal",
                "Tocană de pui cu paprika și smântână",
                "Ușor",
                "60 min",
                new String[] {
                        "Carne de pui",
                        "Ceapă",
                        "Paprika",
                        "Smântână",
                        "Ardei gras",
                        "Ulei"
                },
                new String[] {
                        "Se prăjește puiul cu ceapa",
                        "Se adaugă paprika și puțină apă",
                        "Se fierbe până când carnea devine fragedă",
                        "Se adaugă smântâna și se servește"
                }
        ));
        
        // Add a vegetarian/vegan recipe
        recipes.add(new ModernCulinaryActivity.Recipe(
                "Fasole Bătută",
                "Muntenia",
                "Felul principal",
                "Pastă de fasole aromată cu ceapă călită",
                "Ușor",
                "60 min",
                new String[] {
                        "Fasole albă",
                        "Ceapă",
                        "Usturoi",
                        "Ulei",
                        "Pătrunjel"
                },
                new String[] {
                        "Se fierbe fasolea până se înmoaie",
                        "Se călește ceapa în ulei",
                        "Se pasează fasolea și se amestecă cu ceapa",
                        "Se adaugă usturoiul pisat și pătrunjelul"
                }
        ));
        
        return recipes;
    }
    
    private void setupSortingOptions() {
        // Set up sorting options
        sortChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            
            int checkedId = checkedIds.get(0);
            
            if (checkedId == R.id.sortByMatchChip) {
                // Sort by match percentage
                Collections.sort(filteredRecipeMatches, (a, b) -> 
                        Float.compare(b.getMatchPercentage(), a.getMatchPercentage()));
            } else if (checkedId == R.id.sortByMissingChip) {
                // Sort by missing ingredients (ascending)
                Collections.sort(filteredRecipeMatches, (a, b) -> 
                        Integer.compare(a.getMissingIngredients().size(), b.getMissingIngredients().size()));
            } else if (checkedId == R.id.sortByDifficultyChip) {
                // Sort by difficulty (easy to hard)
                Collections.sort(filteredRecipeMatches, (a, b) -> {
                    String diffA = a.getRecipe().getDifficulty();
                    String diffB = b.getRecipe().getDifficulty();
                    
                    int scoreA = getDifficultyScore(diffA);
                    int scoreB = getDifficultyScore(diffB);
                    
                    return Integer.compare(scoreA, scoreB);
                });
            } else if (checkedId == R.id.sortBySkillLevelChip) {
                // Sort by skill level compatibility (best match first)
                Collections.sort(filteredRecipeMatches, (a, b) -> 
                        Integer.compare(b.getSkillCompatibility(), a.getSkillCompatibility()));
            }
            
            // Update RecyclerView
            RecipeMatchAdapter adapter = (RecipeMatchAdapter) recipesRecyclerView.getAdapter();
            adapter.notifyDataSetChanged();
        });
    }
    
    private int getDifficultyScore(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "foarte ușor": return 1;
            case "ușor": return 2;
            case "mediu": return 3;
            case "dificil": return 4;
            case "foarte dificil": return 5;
            default: return 3;
        }
    }
    
    private void setupRecipesRecyclerView() {
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Set up adapter with click listener
        RecipeMatchAdapter adapter = new RecipeMatchAdapter(filteredRecipeMatches, 
            new RecipeMatchAdapter.OnRecipeClickListener() {
                @Override
                public void onRecipeClick(RecipeMatch recipeMatch) {
                    openRecipeDetails(recipeMatch.getRecipe());
                }
                
                @Override
                public void onAddToShoppingList(RecipeMatch recipeMatch) {
                    addMissingIngredientsToShoppingList(recipeMatch);
                }
            });
        
        recipesRecyclerView.setAdapter(adapter);
        
        // Show/hide "no recipes" message
        noRecipesFoundCard.setVisibility(filteredRecipeMatches.isEmpty() ? 
            View.VISIBLE : View.GONE);
    }
    
    private void openRecipeDetails(ModernCulinaryActivity.Recipe recipe) {
        // Navigate to recipe details page
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipe_id", recipe.getId());
        startActivity(intent);
    }
    
    private void addMissingIngredientsToShoppingList(RecipeMatch recipeMatch) {
        // Add missing ingredients to shopping list
        Set<String> missingIngredients = recipeMatch.getMissingIngredients();
        
        if (missingIngredients.isEmpty()) {
            Snackbar.make(recipesRecyclerView, 
                "Ai toate ingredientele necesare!", Snackbar.LENGTH_SHORT).show();
            return;
        }
        
        // Add to shopping list logic here
        // ...
        
        Snackbar.make(recipesRecyclerView, 
            missingIngredients.size() + " ingrediente adăugate la lista de cumpărături", 
            Snackbar.LENGTH_SHORT).show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_save_filters) {
            saveFilters();
            return true;
        } else if (id == R.id.action_clear_filters) {
            clearFilters();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    public static class RecipeMatch {
        private final ModernCulinaryActivity.Recipe recipe;
        private final float matchPercentage;
        private final int matchedIngredients;
        private final int totalIngredients;
        private final Set<String> missingIngredients;
        private int skillCompatibility; // 0-4 score for skill compatibility
        private boolean relevantMealTime; // if recipe matches current meal time
        private String mealType;
        
        public RecipeMatch(ModernCulinaryActivity.Recipe recipe, float matchPercentage, 
                     int matchedIngredients, int totalIngredients, 
                     Set<String> missingIngredients) {
            this.recipe = recipe;
            this.matchPercentage = matchPercentage;
            this.matchedIngredients = matchedIngredients;
            this.totalIngredients = totalIngredients;
            this.missingIngredients = missingIngredients;
            
            this.skillCompatibility = 2; // Default to moderate compatibility
            this.relevantMealTime = false;
            this.mealType = null;
        }
        
        public ModernCulinaryActivity.Recipe getRecipe() {
            return recipe;
        }
        
        public float getMatchPercentage() {
            return matchPercentage;
        }
        
        public int getMatchedIngredients() {
            return matchedIngredients;
        }
        
        public int getTotalIngredients() {
            return totalIngredients;
        }
        
        public Set<String> getMissingIngredients() {
            return missingIngredients;
        }
        
        public int getSkillCompatibility() {
            return skillCompatibility;
        }
        
        public void setSkillCompatibility(int skillCompatibility) {
            this.skillCompatibility = skillCompatibility;
        }
        
        public boolean isRelevantMealTime() {
            return relevantMealTime;
        }
        
        public void setRelevantMealTime(boolean relevantMealTime) {
            this.relevantMealTime = relevantMealTime;
        }
        
        public String getMealType() {
            return mealType;
        }
        
        public void setMealType(String mealType) {
            this.mealType = mealType;
        }
        
        public String getFormattedMatch() {
            return String.format("%.0f%% (%d/%d ingrediente)", 
                   matchPercentage, matchedIngredients, totalIngredients);
        }
        
        public String getSkillCompatibilityLabel() {
            switch (skillCompatibility) {
                case 4: return "Perfect pentru nivelul tău";
                case 3: return "Bun pentru nivelul tău";
                case 2: return "Acceptabil pentru nivelul tău";
                case 1: return "Provocator pentru nivelul tău";
                case 0: return "Foarte dificil pentru nivelul tău";
                default: return "";
            }
        }
        
        public String getMealTypeLabel() {
            if (mealType == null) {
                return "Potrivit pentru orice masă";
            }
            
            switch (mealType) {
                case UserCulinaryProfile.MEAL_BREAKFAST:
                    return "Ideal pentru mic dejun";
                case UserCulinaryProfile.MEAL_LUNCH:
                    return "Ideal pentru prânz";
                case UserCulinaryProfile.MEAL_DINNER:
                    return "Ideal pentru cină";
                case UserCulinaryProfile.MEAL_SNACK:
                    return "Perfect ca gustare";
                default:
                    return "Potrivit pentru orice masă";
            }
        }
    }
    
    private static class RecipeMatchAdapter extends RecyclerView.Adapter<RecipeMatchAdapter.ViewHolder> {
        private List<RecipeMatch> recipeMatches;
        private final OnRecipeClickListener listener;
        
        interface OnRecipeClickListener {
            void onRecipeClick(RecipeMatch recipeMatch);
            void onAddToShoppingList(RecipeMatch recipeMatch);
        }
        
        RecipeMatchAdapter(List<RecipeMatch> recipeMatches, OnRecipeClickListener listener) {
            this.recipeMatches = recipeMatches;
            this.listener = listener;
        }
        
        public void updateData(List<RecipeMatch> newRecipeMatches) {
            this.recipeMatches = newRecipeMatches;
            notifyDataSetChanged();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = View.inflate(parent.getContext(), R.layout.item_recipe_suggestion, null);
            ViewHolder vh = new ViewHolder(view);
            view.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT));
            return vh;
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            RecipeMatch recipeMatch = recipeMatches.get(position);
            ModernCulinaryActivity.Recipe recipe = recipeMatch.getRecipe();
            
            holder.titleText.setText(recipe.getName());
            
            // Set up region chip
            holder.recipeRegionChip.setText(recipe.getRegion());
            
            // Set difficulty chip with appropriate color
            String difficulty = recipe.getDifficulty();
            holder.difficultyChip.setText(difficulty);
            
            // Configure color based on difficulty
            int difficultyColor;
            switch (difficulty.toLowerCase()) {
                case "ușor":
                case "usor":
                case "începător":
                case "incepator":
                    difficultyColor = R.color.md_theme_tertiary;
                    break;
                case "mediu":
                    difficultyColor = R.color.md_theme_primary;
                    break;
                default: // Difficult
                    difficultyColor = R.color.md_theme_error;
                    break;
            }
            
            // Match percentage and progress indicator
            float matchPercentage = recipeMatch.getMatchPercentage();
            holder.matchPercentageText.setText(String.format("%.0f%%", matchPercentage));
            holder.matchProgressIndicator.setProgress((int) matchPercentage);
            
            // Adjust progress color based on match percentage
            if (matchPercentage >= 90) {
                holder.matchProgressIndicator.setIndicatorColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.md_theme_tertiary));
            } else if (matchPercentage >= 75) {
                holder.matchProgressIndicator.setIndicatorColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.md_theme_primary));
            } else if (matchPercentage >= 50) {
                holder.matchProgressIndicator.setIndicatorColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.md_theme_secondary));
            } else {
                holder.matchProgressIndicator.setIndicatorColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.md_theme_error));
            }
            
            // Missing ingredients text
            Set<String> missingIngredients = recipeMatch.getMissingIngredients();
            int missingCount = missingIngredients.size();
            
            if (missingCount == 0) {
                holder.missingIngredientsText.setText("Ai toate ingredientele necesare!");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Îți lipsesc ").append(missingCount).append(" ingrediente: ");
                
                int i = 0;
                for (String ingredient : missingIngredients) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(ingredient);
                    i++;
                    
                    // Only show first 3 ingredients
                    if (i >= 3 && missingCount > 3) {
                        sb.append(" și altele");
                        break;
                    }
                }
                
                holder.missingIngredientsText.setText(sb.toString());
            }
            
            // Skill compatibility text
            holder.skillCompatibilityText.setText(recipeMatch.getSkillCompatibilityLabel());
            
            // Meal type label
            holder.mealTypeLabel.setText(recipeMatch.getMealTypeLabel());
            
            // Meal time badge visibility
            holder.mealTimeBadge.setVisibility(recipeMatch.isRelevantMealTime() ? 
                View.VISIBLE : View.INVISIBLE);
                
            // Set up action buttons
            holder.viewRecipeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(recipeMatch);
                }
            });
            
            holder.addToShoppingListButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToShoppingList(recipeMatch);
                }
            });
            
            // Set up the card click listener
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(recipeMatch);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return recipeMatches.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleText;
            Chip recipeRegionChip;
            Chip difficultyChip;
            TextView matchPercentageText;
            LinearProgressIndicator matchProgressIndicator;
            TextView missingIngredientsText;
            TextView skillCompatibilityText;
            TextView mealTypeLabel;
            ImageView mealTimeBadge;
            MaterialButton viewRecipeButton;
            MaterialButton addToShoppingListButton;
            
            ViewHolder(View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.recipeTitleText);
                recipeRegionChip = itemView.findViewById(R.id.recipeRegionChip);
                difficultyChip = itemView.findViewById(R.id.difficultyChip);
                matchPercentageText = itemView.findViewById(R.id.matchPercentageText);
                matchProgressIndicator = itemView.findViewById(R.id.matchProgressIndicator);
                missingIngredientsText = itemView.findViewById(R.id.missingIngredientsText);
                skillCompatibilityText = itemView.findViewById(R.id.skillCompatibilityText);
                mealTypeLabel = itemView.findViewById(R.id.mealTypeLabel);
                mealTimeBadge = itemView.findViewById(R.id.mealTimeBadge);
                viewRecipeButton = itemView.findViewById(R.id.viewRecipeButton);
                addToShoppingListButton = itemView.findViewById(R.id.addToShoppingListButton);
            }
        }
    }
} 