package com.example.myapplication.Joc1.Culinary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.adapter.ImageCarouselAdapter;
import com.example.myapplication.adapter.IngredientAdapter;
import com.example.myapplication.adapter.StepAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Add imports for RecipeDBHelper, NutritionalInfo, RomCulinaryActivity, and RomGameState
import com.example.myapplication.Joc1.Culinary.RecipeDBHelper;
import com.example.myapplication.Joc1.Culinary.NutritionalInfo;
import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity;
import com.example.myapplication.Joc1.RomGameState;

public class RomRecipeDetailActivity extends AppCompatActivity {
    private RomGameState gameState;
    private ModernCulinaryActivity.Recipe recipe;
    
    // UI Elements
    private TextView recipeDetailTitle;
    private TextView recipeDetailRegion;
    private TextView recipeDetailDescription;
    private TextView recipeTimePrep;
    private TextView recipeTimeCooking;
    private TextView culturalSignificanceText;
    private TextView recipeRatingText;
    private Chip recipeDifficultyChip;
    private ViewPager2 recipeImageCarousel;
    private TabLayout recipeImageIndicator;
    private RecyclerView ingredientsRecyclerView;
    private RecyclerView stepsRecyclerView;
    private MaterialButton saveRecipeButton;
    private MaterialButton startCookingButton;
    private MaterialButton favoriteButton;
    private MaterialButton cookTodayButton;
    private MaterialCardView ratingCardView;
    private RatingBar recipeRatingBar;
    private MaterialButton submitRatingButton;
    private boolean hasUserRated = false;
    private float userRating = 0;
    private boolean isFavorite = false;
    
    // Recipe images mapped by region
    private final Map<String, ArrayList<String>> regionImages = new HashMap<>();
    
    // Cultural significance texts for each region
    private final Map<String, String> culturalSignificanceMap = new HashMap<>();
    
    private static final int MAX_PREP_TIME = 30; // in minutes
    
    // UI Elements for Nutritional Information
    private TextView caloriesValue;
    private TextView proteinValue;
    private TextView carbsValue;
    private TextView fatValue;
    private TextView fiberValue;
    private TextView sugarValue;
    private TextView sodiumValue;
    private TextView noNutritionalInfoText;
    private LinearLayout additionalNutrientsSection;
    private CardView nutritionalInfoCard;
    
    // Database helper
    private RecipeDBHelper dbHelper;
    
    private TabLayout recipeDetailTabs;
    private FrameLayout recipeContentContainer;
    private LinearLayout ingredientsContainer;
    private LinearLayout stepsContainer;
    private LinearLayout infoContainer;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_recipe_details);
        
        // Initialize database helper
        dbHelper = new RecipeDBHelper(this);
        
        // Get recipe data from intent
        if (getIntent().hasExtra("recipe_title") && getIntent().hasExtra("recipe_region")) {
            String title = getIntent().getStringExtra("recipe_title");
            String region = getIntent().getStringExtra("recipe_region");
            
            // Get favorite status if provided
            if (getIntent().hasExtra("recipe_favorite")) {
                isFavorite = getIntent().getBooleanExtra("recipe_favorite", false);
            }
            
            // Find the recipe in RomCulinaryActivity
            recipe = findRecipeByTitleAndRegion(title, region);
            
            if (recipe != null) {
                // Update recipe's favorite status
                recipe.setFavorite(isFavorite);
            }
        }
        
        if (recipe == null) {
            Toast.makeText(this, "Nu s-au putut încărca detaliile rețetei", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        gameState = RomGameState.getInstance();
        gameState.initialize(this);
        
        initializeViews();
        setupToolbar();
        initializeRegionImages();
        initializeCulturalSignificance();
        populateRecipeDetails();
        setupCarousel();
        setupIngredients();
        setupSteps();
        setupButtons();
        setupRating();
        setupNutritionalInfo();
        setupDetailTabs();
        updateFavoriteButton();
    }
    
    private void initializeViews() {
        recipeDetailTitle = findViewById(R.id.recipeDetailTitle);
        recipeDetailRegion = findViewById(R.id.recipeDetailRegion);
        recipeDetailDescription = findViewById(R.id.recipeDetailDescription);
        recipeTimePrep = findViewById(R.id.recipeTimePrep);
        recipeTimeCooking = findViewById(R.id.recipeTimeCooking);
        culturalSignificanceText = findViewById(R.id.culturalSignificanceText);
        recipeDifficultyChip = findViewById(R.id.recipeDifficultyChip);
        recipeImageCarousel = findViewById(R.id.recipeImageCarousel);
        recipeImageIndicator = findViewById(R.id.recipeImageIndicator);
        ingredientsRecyclerView = findViewById(R.id.ingredientsRecyclerView);
        stepsRecyclerView = findViewById(R.id.stepsRecyclerView);
        saveRecipeButton = findViewById(R.id.saveRecipeButton);
        startCookingButton = findViewById(R.id.startCookingButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        cookTodayButton = findViewById(R.id.cookTodayButton);
        
        // Rating related views
        ratingCardView = findViewById(R.id.ratingCardView);
        recipeRatingBar = findViewById(R.id.recipeRatingBar);
        submitRatingButton = findViewById(R.id.submitRatingButton);
        recipeRatingText = findViewById(R.id.recipeRatingText);
        
        // Initialize nutritional information views
        caloriesValue = findViewById(R.id.caloriesValue);
        proteinValue = findViewById(R.id.proteinValue);
        carbsValue = findViewById(R.id.carbsValue);
        fatValue = findViewById(R.id.fatValue);
        fiberValue = findViewById(R.id.fiberValue);
        sugarValue = findViewById(R.id.sugarValue);
        sodiumValue = findViewById(R.id.sodiumValue);
        noNutritionalInfoText = findViewById(R.id.noNutritionalInfoText);
        additionalNutrientsSection = findViewById(R.id.additionalNutrientsSection);
        nutritionalInfoCard = findViewById(R.id.nutritionalInfoCard);
        
        // Initialize tab views
        recipeDetailTabs = findViewById(R.id.recipeDetailTabs);
        recipeContentContainer = findViewById(R.id.recipeContentContainer);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        stepsContainer = findViewById(R.id.stepsContainer);
        infoContainer = findViewById(R.id.infoContainer);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        // Set up collapsing toolbar title
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
        if (collapsingToolbar != null) {
            collapsingToolbar.setTitle(recipe.getTitle());
        }
    }
    
    private void initializeRegionImages() {
        // Initialize region-specific images for carousel
        regionImages.put("Moldova", new ArrayList<String>() {{
            add("food_moldova");
            add("moldova_cultural");
            add("traditional_moldova");
        }});
        
        regionImages.put("Transilvania", new ArrayList<String>() {{
            add("food_transilvania");
            add("transilvania_cultural");
            add("traditional_transilvania");
        }});
        
        regionImages.put("Muntenia", new ArrayList<String>() {{
            add("food_muntenia");
            add("muntenia_cultural");
            add("traditional_muntenia");
        }});
        
        regionImages.put("Dobrogea", new ArrayList<String>() {{
            add("food_dobrogea");
            add("dobrogea_cultural");
            add("traditional_dobrogea");
        }});
        
        regionImages.put("Oltenia", new ArrayList<String>() {{
            add("food_oltenia");
            add("oltenia_cultural");
            add("traditional_oltenia");
        }});
        
        regionImages.put("Banat", new ArrayList<String>() {{
            add("food_banat");
            add("banat_cultural");
            add("traditional_banat");
        }});
        
        regionImages.put("Maramureș", new ArrayList<String>() {{
            add("food_maramures");
            add("maramures_cultural");
            add("traditional_maramures");
        }});
        
        // Fallback for missing images
        regionImages.put("Default", new ArrayList<String>() {{
            add("ic_food");
            add("ic_region");
        }});
    }
    
    private void initializeCulturalSignificance() {
        culturalSignificanceMap.put("Moldova", "Bucătăria moldovenească este renumită pentru preparatele sale consistente și gustoase, influențate de bucătăria ucraineană și rusească. Sarmalele moldovenești sunt mai mari decât în alte regiuni și conțin adesea carne de porc amestecată cu orez și verdeață. Zeama este un simbol al ospitalității moldovenești și se servește adesea la evenimente importante și sărbători.");
        
        culturalSignificanceMap.put("Transilvania", "Bucătăria transilvăneană reflectă diversitatea etnică a regiunii, cu influențe maghiare, săsești și secuiești. Gulașul ardelenesc este un fel de mâncare tradițional care evidențiază stilul de viață al păstorilor. Kürtőskalács are origini secuiești și este adesea pregătit la evenimente în aer liber și târguri tradiționale.");
        
        culturalSignificanceMap.put("Muntenia", "Bucătăria din Muntenia reflectă influențele balcanice și orientale, fiind mai condimentată decât în alte regiuni. Ciorba de burtă este considerată un remediu tradițional după petreceri și este prezentă la mese festive. Mititei sunt emblematici pentru bucătăria urbană a Bucureștiului, având origini în perioada otomană.");
        
        culturalSignificanceMap.put("Dobrogea", "Bucătăria dobrogeană este puternic influențată de diversitatea etnică a regiunii, incluzând elemente turcești, tătărești și grecești. Plăcinta dobrogeană este renumită pentru foile sale foarte subțiri, tehnică moștenită din bucătăria otomană. Sarailia este un desert specific zonei, având origini în bucătăria orientală.");
        
        culturalSignificanceMap.put("Oltenia", "Bucătăria oltenească se remarcă prin utilizarea intensă a legumelor de sezon și prin preparate simple dar gustoase. Prazul cu măsline este un preparat de post des întâlnit în mănăstirile oltenești. Vărzările sunt preparate tradiționale servite la sărbătorile câmpenești și la hramuri.");
        
        culturalSignificanceMap.put("Banat", "Bucătăria bănățeană reflectă influențele austro-ungare, sârbești și germane. Iofca este un fel de mâncare tradițional al șvabilor bănățeni, păstrat și în bucătăria românească. Papricașul de pui arată influența maghiară și este adesea servit la evenimente familiale importante.");
        
        culturalSignificanceMap.put("Maramureș", "Bucătăria maramureșeană este caracterizată prin utilizarea ingredientelor naturale locale și prin tehnici de preparare tradiționale. Balmoșul este un preparat păstoresc, simbolizând legătura puternică cu tradiția creșterii animalelor. Plăcinta creață este pregătită la sărbători și reuniuni familiale importante.");
        
        culturalSignificanceMap.put("Default", "Bucătăria românească este diversă și bogată, reflectând istoria, geografia și influențele culturale din fiecare regiune. Mâncarea tradițională ocupă un loc important în cultura românească, fiind esențială la sărbători, evenimente de familie și în viața de zi cu zi.");
    }
    
    private void populateRecipeDetails() {
        // Populate basic recipe information
        recipeDetailTitle.setText(recipe.getTitle());
        recipeDetailRegion.setText(recipe.getRegion());
        recipeDetailDescription.setText(recipe.getDescription());
        
        // Set difficulty chip
        Map<String, String> difficultyIcons = new HashMap<>();
        difficultyIcons.put("Ușor", "⭐");
        difficultyIcons.put("Mediu", "⭐⭐");
        difficultyIcons.put("Dificil", "⭐⭐⭐");
        
        recipeDifficultyChip.setText(difficultyIcons.getOrDefault(recipe.getDifficulty(), "") + " " + recipe.getDifficulty());
        
        // Parse and set cooking times
        String timeString = recipe.getTime();
        int totalMinutes = parseCookingTime(timeString);
        
        // Estimate prep vs cooking time
        int prepTime = Math.min(totalMinutes / 3, MAX_PREP_TIME);
        int cookingTime = totalMinutes - prepTime;
        
        recipeTimePrep.setText("Preparare: " + prepTime + " min");
        recipeTimeCooking.setText("Gătire: " + cookingTime + " min");
        
        // Set cultural significance
        String significance = culturalSignificanceMap.getOrDefault(recipe.getRegion(), culturalSignificanceMap.get("Default"));
        culturalSignificanceText.setText(significance);
    }
    
    private void setupCarousel() {
        // Get images for this recipe's region
        ArrayList<String> images = regionImages.getOrDefault(recipe.getRegion(), regionImages.get("Default"));
        
        // Add recipe title-specific image if exists
        String specificImageName = "recipe_" + recipe.getTitle().toLowerCase().replace(" ", "_").replace("ă", "a").replace("â", "a").replace("î", "i").replace("ș", "s").replace("ț", "t");
        
        // Check if specific image exists and add it first
        int resId = getResources().getIdentifier(specificImageName, "drawable", getPackageName());
        if (resId != 0) {
            images.add(0, specificImageName); // Add at beginning
        }
        
        // Set up the image carousel
        ImageCarouselAdapter carouselAdapter = new ImageCarouselAdapter(this, images);
        recipeImageCarousel.setAdapter(carouselAdapter);
        
        // Set up the tab indicator
        new TabLayoutMediator(recipeImageIndicator, recipeImageCarousel, (tab, position) -> {
            // Just create empty tabs
        }).attach();
    }
    
    private void setupIngredients() {
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        IngredientAdapter ingredientAdapter = new IngredientAdapter(recipe.getIngredients());
        ingredientsRecyclerView.setAdapter(ingredientAdapter);
    }
    
    private void setupSteps() {
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        StepAdapter stepAdapter = new StepAdapter(recipe.getSteps());
        stepsRecyclerView.setAdapter(stepAdapter);
    }
    
    private void setupButtons() {
        saveRecipeButton.setOnClickListener(v -> {
            // Mark recipe as discovered
            recipe.setDiscovered(true);
            
            // Update SharedPreferences to persist the discovered state
            SharedPreferences prefs = getSharedPreferences("rom_recipe_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("recipe_" + recipe.getTitle() + "_" + recipe.getRegion(), true);
            editor.apply();
            
            // Notify the user
            Toast.makeText(this, "Rețetă salvată cu succes", Toast.LENGTH_SHORT).show();
            
            // Set result and finish
            prepareResultAndFinish();
        });
        
        startCookingButton.setOnClickListener(v -> {
            // Launch step-by-step cooking activity
            Intent intent = new Intent(this, RecipeStepByStepActivity.class);
            intent.putExtra("recipe_title", recipe.getTitle());
            intent.putExtra("recipe_region", recipe.getRegion());
            startActivity(intent);
        });
        
        favoriteButton.setOnClickListener(v -> {
            // Toggle favorite status
            recipe.toggleFavorite();
            
            // Update UI
            updateFavoriteButton();
            
            // Save to database
            dbHelper.updateFavoriteStatus(recipe.getTitle(), recipe.getRegion(), recipe.isFavorite());
            
            // Notify user
            String message = recipe.isFavorite() ? 
                    "Adăugat la favorite" : "Eliminat din favorite";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
        
        // Setup Cook Today button for streak tracking
        cookTodayButton.setOnClickListener(v -> {
            recordCookingActivity();
        });
        
        // Update button state based on whether user already cooked today
        updateCookTodayButton();
    }
    
    private void updateFavoriteButton() {
        if (favoriteButton != null) {
            if (recipe.isFavorite()) {
                favoriteButton.setIcon(getResources().getDrawable(R.drawable.ic_favorite, getTheme()));
                favoriteButton.setText("Eliminați de la favorite");
            } else {
                favoriteButton.setIcon(getResources().getDrawable(R.drawable.ic_favorite_border, getTheme()));
                favoriteButton.setText("Adaugă la favorite");
            }
        }
    }
    
    /**
     * Record that user cooked today and update streak
     */
    private void recordCookingActivity() {
        boolean streakUpdated = gameState.recordCookingActivity(this);
        if (streakUpdated) {
            int currentStreak = gameState.getCookingStreak();
            String message = String.format(
                    "Activitate de gătit înregistrată! Streak actual: %d %s", 
                    currentStreak,
                    currentStreak == 1 ? "zi" : "zile"
            );
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            
            // Mark recipe as discovered
            recipe.setDiscovered(true);
            
            // Update the game state's recipes discovered counter
            gameState.discoverRecipe(this);
            
            // Update the button state
            updateCookTodayButton();
        } else {
            Toast.makeText(this, "Ai gătit deja astăzi. Revino mâine pentru a-ți continua streak-ul!", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Update the Cook Today button state based on whether user already cooked today
     */
    private void updateCookTodayButton() {
        long currentTime = System.currentTimeMillis();
        long lastCookingDate = gameState.getLastCookingDate();
        long oneDayInMillis = 24 * 60 * 60 * 1000;
        
        // Check if already cooked today
        if (lastCookingDate > 0 && (currentTime - lastCookingDate) < oneDayInMillis) {
            cookTodayButton.setEnabled(false);
            cookTodayButton.setText("Gătit Astăzi ✓");
        } else {
            cookTodayButton.setEnabled(true);
            cookTodayButton.setText("Am gătit azi");
        }
        
        // Show streak count in button if there's an active streak
        int currentStreak = gameState.getCookingStreak();
        if (currentStreak > 1) {
            cookTodayButton.setText(cookTodayButton.getText() + " (" + currentStreak + ")");
        }
    }
    
    private void setupNutritionalInfo() {
        NutritionalInfo nutritionalInfo = recipe.getNutritionalInfo();
        
        if (!recipe.hasNutritionalInfo()) {
            // Generate sample nutritional info based on ingredients
            nutritionalInfo = generateSampleNutritionalInfo();
            recipe.setNutritionalInfo(nutritionalInfo);
            
            // Save to database
            dbHelper.updateNutritionalInfo(recipe.getTitle(), recipe.getRegion(), nutritionalInfo);
        }
        
        // Check if we have nutritional info
        if (nutritionalInfo != null && nutritionalInfo.hasNutritionalInformation()) {
            // Hide no info message
            noNutritionalInfoText.setVisibility(View.GONE);
            
            // Set values
            caloriesValue.setText(nutritionalInfo.getFormattedCalories());
            proteinValue.setText(String.format("%.1fg", nutritionalInfo.getProtein()));
            carbsValue.setText(String.format("%.1fg", nutritionalInfo.getCarbs()));
            fatValue.setText(String.format("%.1fg", nutritionalInfo.getFat()));
            
            // Check if additional nutrients are available
            if (nutritionalInfo.getFiber() > 0 || nutritionalInfo.getSugar() > 0 || nutritionalInfo.getSodium() > 0) {
                additionalNutrientsSection.setVisibility(View.VISIBLE);
                fiberValue.setText(String.format("%.1fg", nutritionalInfo.getFiber()));
                sugarValue.setText(String.format("%.1fg", nutritionalInfo.getSugar()));
                sodiumValue.setText(String.format("%.0fmg", nutritionalInfo.getSodium()));
            } else {
                additionalNutrientsSection.setVisibility(View.GONE);
            }
        } else {
            // Show no info message
            noNutritionalInfoText.setVisibility(View.VISIBLE);
            additionalNutrientsSection.setVisibility(View.GONE);
        }
    }
    
    /**
     * Generate sample nutritional information based on ingredients
     */
    private NutritionalInfo generateSampleNutritionalInfo() {
        // This is a simplified approach to generate sample data
        // In a real app, you would use a database of food nutritional values
        
        float totalCalories = 0;
        float totalProtein = 0;
        float totalCarbs = 0;
        float totalFat = 0;
        float totalFiber = 0;
        float totalSugar = 0;
        float totalSodium = 0;
        
        String[] ingredients = recipe.getIngredients();
        if (ingredients != null) {
            for (String ingredient : ingredients) {
                String lowerIngredient = ingredient.toLowerCase();
                
                // Proteins
                if (lowerIngredient.contains("carne") || lowerIngredient.contains("pui") || 
                    lowerIngredient.contains("peste") || lowerIngredient.contains("ou")) {
                    totalProtein += 15 + (float) (Math.random() * 10);
                    totalFat += 7 + (float) (Math.random() * 8);
                    totalCalories += 200 + (float) (Math.random() * 100);
                    totalSodium += 200 + (float) (Math.random() * 100);
                }
                
                // Carbs
                if (lowerIngredient.contains("făină") || lowerIngredient.contains("pâine") || 
                    lowerIngredient.contains("orez") || lowerIngredient.contains("paste")) {
                    totalCarbs += 30 + (float) (Math.random() * 20);
                    totalCalories += 150 + (float) (Math.random() * 50);
                    totalFiber += 2 + (float) (Math.random() * 3);
                }
                
                // Fats
                if (lowerIngredient.contains("ulei") || lowerIngredient.contains("unt") || 
                    lowerIngredient.contains("smântână") || lowerIngredient.contains("frișcă")) {
                    totalFat += 15 + (float) (Math.random() * 10);
                    totalCalories += 120 + (float) (Math.random() * 80);
                }
                
                // Vegetables
                if (lowerIngredient.contains("roșii") || lowerIngredient.contains("ardei") || 
                    lowerIngredient.contains("ceapă") || lowerIngredient.contains("morcov") || 
                    lowerIngredient.contains("varză")) {
                    totalCarbs += 5 + (float) (Math.random() * 5);
                    totalFiber += 2 + (float) (Math.random() * 2);
                    totalCalories += 30 + (float) (Math.random() * 20);
                    totalSugar += 2 + (float) (Math.random() * 2);
                }
                
                // Dairy
                if (lowerIngredient.contains("lapte") || lowerIngredient.contains("iaurt") || 
                    lowerIngredient.contains("brânză") || lowerIngredient.contains("cașcaval")) {
                    totalProtein += 7 + (float) (Math.random() * 5);
                    totalFat += 8 + (float) (Math.random() * 7);
                    totalCalories += 100 + (float) (Math.random() * 50);
                    totalSodium += 100 + (float) (Math.random() * 50);
                }
                
                // Fruits and sugars
                if (lowerIngredient.contains("zahăr") || lowerIngredient.contains("miere") || 
                    lowerIngredient.contains("fructe") || lowerIngredient.contains("stafide")) {
                    totalCarbs += 20 + (float) (Math.random() * 10);
                    totalSugar += 15 + (float) (Math.random() * 10);
                    totalCalories += 80 + (float) (Math.random() * 40);
                }
            }
        }
        
        // Ensure minimum values and round to reasonable numbers
        totalCalories = Math.max(150, totalCalories);
        totalProtein = Math.max(3, totalProtein);
        totalCarbs = Math.max(5, totalCarbs);
        totalFat = Math.max(2, totalFat);
        
        // Normalize per serving (assuming 4 servings as default)
        float servings = 4f;
        totalCalories /= servings;
        totalProtein /= servings;
        totalCarbs /= servings;
        totalFat /= servings;
        totalFiber /= servings;
        totalSugar /= servings;
        totalSodium /= servings;
        
        // Round to 1 decimal place
        totalCalories = Math.round(totalCalories * 10) / 10f;
        totalProtein = Math.round(totalProtein * 10) / 10f;
        totalCarbs = Math.round(totalCarbs * 10) / 10f;
        totalFat = Math.round(totalFat * 10) / 10f;
        totalFiber = Math.round(totalFiber * 10) / 10f;
        totalSugar = Math.round(totalSugar * 10) / 10f;
        totalSodium = Math.round(totalSodium);
        
        return new NutritionalInfo(recipe.getTitle(), (int)totalCalories, totalProtein, totalFat, totalCarbs, 
                                   totalFiber, totalSugar, totalSodium);
    }
    
    private void setupRating() {
        // Show current rating if available
        if (recipe.getRating() > 0) {
            recipeRatingText.setText(recipe.getFormattedRating());
        } else {
            recipeRatingText.setText(R.string.recipe_not_rated);
        }
        
        // Only enable rating if recipe is discovered
        ratingCardView.setVisibility(recipe.isDiscovered() ? View.VISIBLE : View.GONE);
        
        submitRatingButton.setOnClickListener(v -> {
            if (recipeRatingBar.getRating() < 0.5f) {
                Toast.makeText(this, "Te rugăm să alegi un rating", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Add rating to recipe
            recipe.addRating(recipeRatingBar.getRating());
            
            // Update the displayed rating
            recipeRatingText.setText(recipe.getFormattedRating());
            
            // Update in database
            dbHelper.updateRecipe(recipe, recipe.getNutritionalInfo());
            
            // Hide rating card and update the UI
            ratingCardView.setVisibility(View.GONE);
            hasUserRated = true;
            
            // Thank the user
            Toast.makeText(this, "Mulțumim pentru evaluare!", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void completeRecipe() {
        // Mark the recipe as discovered
            recipe.setDiscovered(true);
            
        // Update the recipe in the database
        if (recipe.hasNutritionalInfo()) {
            dbHelper.updateRecipe(recipe, recipe.getNutritionalInfo());
        } else {
            NutritionalInfo nutritionalInfo = generateSampleNutritionalInfo();
            recipe.setNutritionalInfo(nutritionalInfo);
            dbHelper.updateRecipe(recipe, nutritionalInfo);
        }
        
        // Save to SharedPreferences for backward compatibility
        SharedPreferences prefs = getSharedPreferences("rom_recipe_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("recipe_" + recipe.getTitle() + "_" + recipe.getRegion(), true);
        editor.apply();
        
        // Set result and finish
        prepareResultAndFinish();
    }
    
    private int parseCookingTime(String timeString) {
        // Extract total minutes from time string like "60 min" or "2 ore"
        int minutes = 0;
        
        try {
            if (timeString.contains("min")) {
                String numStr = timeString.replaceAll("[^0-9]", "");
                minutes = Integer.parseInt(numStr);
            } else if (timeString.contains("ore") || timeString.contains("oră")) {
                String numStr = timeString.replaceAll("[^0-9]", "");
                minutes = Integer.parseInt(numStr) * 60;
            }
        } catch (NumberFormatException e) {
            minutes = 60; // Default to 60 minutes if parsing fails
        }
        
        return Math.max(minutes, 30); // Ensure minimum 30 minutes
    }
    
    private ModernCulinaryActivity.Recipe findRecipeByTitleAndRegion(String title, String region) {
        // This is a placeholder. In a real implementation, you would need to access
        // the recipes from ModernCulinaryActivity or a shared repository.
        // For demonstration purposes, we'll create a dummy recipe.
        
        return new ModernCulinaryActivity.Recipe(
                title,
                region,
                "Felul principal", // Default category
                "Descriere detaliată a rețetei tradiționale.",
                "Mediu", // Default difficulty
                "60 min", // Default time
                new String[] {
                        "Ingredient 1",
                        "Ingredient 2",
                        "Ingredient 3"
                },
                new String[] {
                        "Pas 1 de preparare",
                        "Pas 2 de preparare",
                        "Pas 3 de preparare"
                }
        );
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            prepareResultAndFinish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void prepareResultAndFinish() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("recipe_title", recipe.getTitle());
        resultIntent.putExtra("recipe_region", recipe.getRegion());
        
        // Add rating if user rated
        if (hasUserRated) {
            resultIntent.putExtra("recipe_rating", userRating);
        }
        
        // Add completion status if recipe was prepared
        if (recipe.isDiscovered()) {
            resultIntent.putExtra("recipe_completed", true);
        }
        
        // Add favorite status
        resultIntent.putExtra("recipe_favorite", isFavorite);
        
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        prepareResultAndFinish();
    }
    
    private void setupDetailTabs() {
        // Set up tab selection listener
        recipeDetailTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateContentForTab(tab.getPosition());
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Nothing to do
            }
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Nothing to do
            }
        });
        
        // Show ingredients tab by default
        updateContentForTab(0);
    }
    
    private void updateContentForTab(int position) {
        // Hide all containers first
        ingredientsContainer.setVisibility(View.GONE);
        stepsContainer.setVisibility(View.GONE);
        infoContainer.setVisibility(View.GONE);
        
        // Show the selected container
        switch (position) {
            case 0:
                ingredientsContainer.setVisibility(View.VISIBLE);
                break;
            case 1:
                stepsContainer.setVisibility(View.VISIBLE);
                break;
            case 2:
                infoContainer.setVisibility(View.VISIBLE);
                break;
        }
    }
} 