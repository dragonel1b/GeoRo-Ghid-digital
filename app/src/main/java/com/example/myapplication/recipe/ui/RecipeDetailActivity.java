package com.example.myapplication.recipe.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.adapter.IngredientAdapter;
import com.example.myapplication.adapter.CommentsAdapter;
import com.example.myapplication.core.domain.model.UserProfile;
import com.example.myapplication.recipe.model.Ingredient;
import com.example.myapplication.recipe.model.NutritionalInfo;
import com.example.myapplication.recipe.model.Recipe;
import com.example.myapplication.recipe.repository.RecipeRepository;
import com.example.myapplication.utils.PreferenceManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {
    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";
    
    private RecipeRepository recipeRepository;
    private Recipe recipe;
    private int currentServings;
    
    // UI components
    private ImageView recipeImage;
    private TextView descriptionTextView;
    private TextView timeTextView;
    private TextView servingsTextView;
    private TextView difficultyTextView;
    private FloatingActionButton fabCooking;
    private TextView regionChip;
    private TextView categoryChip;
    private RecyclerView ingredientsRecyclerView;
    private RecyclerView stepsRecyclerView;
    private RecyclerView commentsRecyclerView;
    private TextView nutritionalInfoTextView;
    private TextView authorInfoTextView;
    private View dietaryRestrictionsContainer;
    private Chip vegetarianChip;
    private Chip veganChip;
    private Chip glutenFreeChip;
    private Chip lactoseFreeChip;
    private IngredientAdapter ingredientAdapter;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private View scrollContainer;
    
    // Cards
    private View descriptionCard;
    private View ingredientsCard;
    private View stepsCard;
    private View nutritionalCard;
    private View commentsCard;
    private TextView noCommentsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        
        // Get recipe ID from intent
        int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        if (recipeId == -1) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize repository and get recipe
        recipeRepository = RecipeRepository.getInstance();
        recipe = recipeRepository.getRecipeById(recipeId);
        
        if (recipe == null) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize currentServings
        currentServings = recipe.getServings();
        
        // Initialize views
        initializeViews();
        
        // Set up toolbar
        setupToolbar();
        
        // Set up tabs
        setupTabs();
        
        // Populate UI with recipe data
        populateRecipeDetails();
        
        // Set up cooking button
        setupCookingButton();
        
        // Set up ingredients list
        setupIngredientsList();
        
        // Set up preparation steps
        setupPreparationSteps();
        
        // Set up dietary restrictions chips
        setupDietaryRestrictions();
        
        // Set up author info if user contributed
        setupAuthorInfo();
        
        // Set up comments section
        setupComments();
        
        // Set up servings adjustment
        setupServingsAdjustment();
    }

    private void initializeViews() {
        // Basic views
        recipeImage = findViewById(R.id.recipe_image);
        descriptionTextView = findViewById(R.id.recipe_description);
        timeTextView = findViewById(R.id.recipe_time);
        servingsTextView = findViewById(R.id.recipe_servings);
        difficultyTextView = findViewById(R.id.recipe_difficulty);
        regionChip = findViewById(R.id.recipe_region);
        categoryChip = findViewById(R.id.recipe_category);
        fabCooking = findViewById(R.id.fab_cooking);
        ingredientsRecyclerView = findViewById(R.id.ingredients_recycler_view);
        stepsRecyclerView = findViewById(R.id.steps_recycler_view);
        nutritionalInfoTextView = findViewById(R.id.nutritional_info_text);
        
        // Tabs
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.view_pager);
        scrollContainer = findViewById(R.id.scroll_container);
        
        // Cards
        descriptionCard = findViewById(R.id.description_card);
        ingredientsCard = findViewById(R.id.ingredients_card);
        stepsCard = findViewById(R.id.steps_card);
        nutritionalCard = findViewById(R.id.nutritional_card);
        commentsCard = findViewById(R.id.comments_card);
        
        // Comments views
        commentsRecyclerView = findViewById(R.id.comments_recycler_view);
        noCommentsText = findViewById(R.id.no_comments_text);
        authorInfoTextView = findViewById(R.id.author_info);
        dietaryRestrictionsContainer = findViewById(R.id.dietary_restrictions_container);
        vegetarianChip = findViewById(R.id.vegetarian_chip);
        veganChip = findViewById(R.id.vegan_chip);
        glutenFreeChip = findViewById(R.id.gluten_free_chip);
        lactoseFreeChip = findViewById(R.id.lactose_free_chip);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setTitle(recipe.getTitle());
        }
    }
    
    private void setupTabs() {
        // Configurez tab-urile pentru a afișa doar una dintre secțiuni odată
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Ascundem toate cardurile
                descriptionCard.setVisibility(View.GONE);
                ingredientsCard.setVisibility(View.GONE);
                stepsCard.setVisibility(View.GONE);
                nutritionalCard.setVisibility(View.GONE);
                
                // Arătăm doar cardul selectat
                switch (tab.getPosition()) {
                    case 0:
                        descriptionCard.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        ingredientsCard.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        stepsCard.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        nutritionalCard.setVisibility(View.VISIBLE);
                        break;
                }
                
                // Comentariile sunt întotdeauna vizibile
                commentsCard.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        
        // Selectează prima filă inițial
        tabLayout.selectTab(tabLayout.getTabAt(0));
    }

    private void populateRecipeDetails() {
        // Set recipe image if available
        if (recipe.getImageResourceId() != 0) {
            recipeImage.setImageResource(recipe.getImageResourceId());
        } else {
            recipeImage.setImageResource(R.drawable.placeholder_recipe);
        }
        
        // Set text views
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(recipe.getTitle());
        }
        descriptionTextView.setText(recipe.getDescription());
        timeTextView.setText(recipe.getFormattedTime());
        servingsTextView.setText(String.valueOf(recipe.getServings()));
        
        // Set chips
        regionChip.setText(recipe.getRegion());
        categoryChip.setText(recipe.getCategory());
        difficultyTextView.setText(recipe.getDifficulty());
        
        // Set nutritional info if available
        if (recipe.getNutritionalInfo() != null) {
            NutritionalInfo info = recipe.getNutritionalInfo();
            String nutritionalText = String.format(
                "Calorii: %d kcal\nProteine: %.1fg\nCarbohidrați: %.1fg\nGrăsimi: %.1fg\nFibre: %.1fg\nZahăr: %.1fg\nSodiu: %.1fmg",
                info.getCalories(),
                info.getProtein(),
                info.getCarbs(),
                info.getFat(),
                info.getFiber(),
                info.getSugar(),
                info.getSodium()
            );
            nutritionalInfoTextView.setText(nutritionalText);
        } else {
            nutritionalInfoTextView.setText("Informații nutriționale indisponibile");
        }
    }

    private void setupCookingButton() {
        fabCooking.setOnClickListener(v -> {
            Toast.makeText(this, "Începi să gătești: " + recipe.getTitle(), Toast.LENGTH_SHORT).show();
            // Aici se poate implementa modul de gătire pas cu pas
        });
    }
    
    private void setupIngredientsList() {
        // Set layout manager
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Create and set adapter with initial ingredients
        ingredientAdapter = new IngredientAdapter(this, recipe.getIngredients());
        ingredientsRecyclerView.setAdapter(ingredientAdapter);
    }
    
    private void setupPreparationSteps() {
        // Set layout manager
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Create and set adapter
        PreparationStepsAdapter stepsAdapter = new PreparationStepsAdapter(recipe.getPreparationSteps());
        stepsRecyclerView.setAdapter(stepsAdapter);
    }
    
    private void setupDietaryRestrictions() {
        boolean hasRestrictions = false;
        
        if (recipe.isVegetarian()) {
            vegetarianChip.setVisibility(View.VISIBLE);
            hasRestrictions = true;
        } else {
            vegetarianChip.setVisibility(View.GONE);
        }
        
        if (recipe.isVegan()) {
            veganChip.setVisibility(View.VISIBLE);
            hasRestrictions = true;
        } else {
            veganChip.setVisibility(View.GONE);
        }
        
        if (recipe.isGlutenFree()) {
            glutenFreeChip.setVisibility(View.VISIBLE);
            hasRestrictions = true;
        } else {
            glutenFreeChip.setVisibility(View.GONE);
        }
        
        if (recipe.isLactoseFree()) {
            lactoseFreeChip.setVisibility(View.VISIBLE);
            hasRestrictions = true;
        } else {
            lactoseFreeChip.setVisibility(View.GONE);
        }
        
        dietaryRestrictionsContainer.setVisibility(hasRestrictions ? View.VISIBLE : View.GONE);
    }
    
    private void setupAuthorInfo() {
        if (recipe.isUserContributed() && recipe.getAuthorName() != null) {
            authorInfoTextView.setVisibility(View.VISIBLE);
            authorInfoTextView.setText(String.format("Adăugată de: %s", recipe.getAuthorName()));
        } else {
            authorInfoTextView.setVisibility(View.GONE);
        }
    }
    
    private void setupComments() {
        if (recipe.getComments() != null && !recipe.getComments().isEmpty()) {
            commentsRecyclerView.setVisibility(View.VISIBLE);
            commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            CommentsAdapter commentsAdapter = new CommentsAdapter(this, recipe.getComments());
            commentsRecyclerView.setAdapter(commentsAdapter);
            noCommentsText.setVisibility(View.GONE);
        } else {
            commentsRecyclerView.setVisibility(View.GONE);
            noCommentsText.setVisibility(View.VISIBLE);
        }
    }
    
    private void setupServingsAdjustment() {
        // Set click listener for servings text to adjust number of servings
        View servingsContainer = findViewById(R.id.recipe_servings_container);
        servingsContainer.setOnClickListener(v -> showServingsDialog());
    }
    
    private void showServingsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_adjust_servings, null);
        EditText servingsEditText = dialogView.findViewById(R.id.edit_servings);
        servingsEditText.setText(String.valueOf(currentServings));
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Ajustare porții")
                .setView(dialogView)
                .setPositiveButton("Aplică", (dialog, which) -> {
                    try {
                        int newServings = Integer.parseInt(servingsEditText.getText().toString().trim());
                        if (newServings > 0) {
                            updateServings(newServings);
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Valoare invalidă", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Anulează", null)
                .show();
    }
    
    private void updateServings(int newServings) {
        currentServings = newServings;
        servingsTextView.setText(String.valueOf(newServings));
        
        // Update ingredients with scaled quantities
        List<Ingredient> scaledIngredients = recipe.getScaledIngredients(newServings);
        ingredientAdapter.updateIngredients(scaledIngredients);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe_detail, menu);
        // Adaugă butonul de favorite în toolbar
        MenuItem favoriteItem = menu.findItem(R.id.action_favorite);
        if (favoriteItem != null) {
            favoriteItem.setIcon(recipe.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_share) {
            shareRecipe();
            return true;
        } else if (id == R.id.action_add_comment) {
            showAddCommentDialog();
            return true;
        } else if (id == R.id.action_favorite) {
            toggleFavorite(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void toggleFavorite(MenuItem item) {
        recipe.setFavorite(!recipe.isFavorite());
        recipeRepository.updateRecipe(recipe);
        
        // Actualizare icon
        item.setIcon(recipe.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        
        // Show toast message
        String message = recipe.isFavorite() ? 
                getString(R.string.added_to_favorites) : 
                getString(R.string.removed_from_favorites);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    private void shareRecipe() {
        String shareText = String.format(
                "Încearcă această rețetă: %s\n\n%s\n\nTrebuie să ai:\n",
                recipe.getTitle(),
                recipe.getDescription()
        );
        
        // Add ingredients
        for (Ingredient ingredient : recipe.getIngredients()) {
            String unit = ingredient.getUnit() != null && !ingredient.getUnit().isEmpty() ? 
                    ingredient.getUnit() : "";
            
            // Verificăm tipul cantității și o formatăm corect
            Object quantity = ingredient.getQuantity();
            String formattedQuantity;
            
            if (quantity instanceof Number) {
                // Dacă este un număr, îl formatăm ca atare
                formattedQuantity = String.format("%.1f", ((Number)quantity).doubleValue());
            } else {
                // Altfel, îl tratăm ca pe un String
                formattedQuantity = quantity.toString();
            }
            
            shareText += String.format("- %s %s %s\n", 
                    formattedQuantity, unit, ingredient.getName());
        }
        
        // Increment share count
        recipe.incrementShareCount();
        recipeRepository.updateRecipe(recipe);
        
        // Create share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Rețetă: " + recipe.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Partajează rețeta"));
    }
    
    private void showAddCommentDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_comment, null);
        EditText commentEditText = dialogView.findViewById(R.id.edit_comment);
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Adaugă comentariu")
                .setView(dialogView)
                .setPositiveButton("Adaugă", (dialog, which) -> {
                    String comment = commentEditText.getText().toString().trim();
                    if (!comment.isEmpty()) {
                        addComment(comment);
                    }
                })
                .setNegativeButton("Anulează", null)
                .show();
    }
    
    private void addComment(String commentText) {
        // Get current user info
        PreferenceManager prefManager = new PreferenceManager(this);
        UserProfile userProfile = prefManager.getUserProfile();
        
        String userName = "Anonim";
        if (userProfile != null) {
            userName = userProfile.getDisplayName();
        }
        
        // Format comment with username
        String formattedComment = String.format("%s: %s", userName, commentText);
        
        // Add to recipe
        recipe.addComment(formattedComment);
        recipeRepository.updateRecipe(recipe);
        
        // Refresh comments
        setupComments();
    }
    
    // Inner class for preparation steps adapter
    private class PreparationStepsAdapter extends RecyclerView.Adapter<PreparationStepsAdapter.StepViewHolder> {
        
        private final List<String> steps;
        
        public PreparationStepsAdapter(List<String> steps) {
            this.steps = steps;
        }
        
        @NonNull
        @Override
        public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recipe_step, parent, false);
            return new StepViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
            String step = steps.get(position);
            holder.stepNumberTextView.setText(String.format("%d", position + 1));
            holder.stepDescriptionTextView.setText(step);
        }
        
        @Override
        public int getItemCount() {
            return steps.size();
        }
        
        class StepViewHolder extends RecyclerView.ViewHolder {
            final TextView stepNumberTextView;
            final TextView stepDescriptionTextView;
            
            StepViewHolder(@NonNull View itemView) {
                super(itemView);
                stepNumberTextView = itemView.findViewById(R.id.stepNumberText);
                stepDescriptionTextView = itemView.findViewById(R.id.stepInstructionText);
            }
        }
    }
} 