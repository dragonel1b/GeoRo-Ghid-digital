package com.example.myapplication.Joc1.Culinary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity.Recipe;
import android.view.Gravity;

/**
 * Activity pentru detalii rețetă cu suport Material Design 3
 */
public class RecipeDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "RecipeDetailActivity";
    private static final String KEY_RECIPE_ID = "recipe_id";
    
    // UI Components
    private MaterialToolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private ExtendedFloatingActionButton favoriteFab;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ImageView recipeImageView;
    private TextView timeTextView;
    private TextView difficultyTextView;
    private Chip regionChip;
    private Chip categoryChip;
    private TextView recipeDescription;
    
    // Data
    private long recipeId;
    private boolean isFavorite = false;
    private Recipe recipe;
    private RecipeDBHelper dbHelper;
    private String recipeTitle;
    private String recipeRegion;

    public static Intent newIntent(Context context, long recipeId) {        Intent intent = new Intent(context, RecipeDetailActivity.class);        intent.putExtra(KEY_RECIPE_ID, recipeId);        return intent;    }        public static void start(Context context, String recipeId) {        Intent intent = new Intent(context, RecipeDetailActivity.class);        intent.putExtra(KEY_RECIPE_ID, Long.parseLong(recipeId));        context.startActivity(intent);    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Activează Dynamic Colors pentru Material You (Android 12+)
        // com.google.android.material.color.DynamicColors.applyToActivityIfAvailable(this);
        
        setContentView(R.layout.activity_recipe_detail);
        
        // Extrage ID-ul rețetei
        recipeId = getIntent().getLongExtra(KEY_RECIPE_ID, -1);
        if (recipeId == -1) {
            Toast.makeText(this, "Eroare la încărcarea rețetei", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize database helper
        dbHelper = new RecipeDBHelper(this);
        
        // Get recipe data from intent
        recipeTitle = getIntent().getStringExtra("recipe_title");
        recipeRegion = getIntent().getStringExtra("recipe_region");
        
        // Load recipe data
        loadRecipeData();
        
        initializeViews();
        setupToolbar();
        setupTabLayout();
        setupFab();
        loadRecipeDetails();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        favoriteFab = findViewById(R.id.favoriteFab);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        recipeImageView = findViewById(R.id.recipeImageView);
        timeTextView = findViewById(R.id.timeTextView);
        difficultyTextView = findViewById(R.id.difficultyTextView);
        regionChip = findViewById(R.id.regionChip);
        categoryChip = findViewById(R.id.categoryChip);
        recipeDescription = findViewById(R.id.recipeDescription);
        
        // Configurează accesibilitatea
        ViewCompat.setAccessibilityHeading(collapsingToolbar, true);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Setează navigarea înapoi
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupTabLayout() {
        // Configurează ViewPager2 cu adapter pentru fragmente
        setupViewPager();
        
        // Conectează TabLayout cu ViewPager2 folosind TabLayoutMediator
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Detalii");
                    break;
                case 1:
                    tab.setText("Ingrediente");
                    break;
                case 2:
                    tab.setText("Preparare");
                    break;
            }
        }).attach();
    }
    
    private void setupFab() {
        // Configurare FloatingActionButton
        favoriteFab.setOnClickListener(v -> {
            // Inversează starea de favorit
            isFavorite = !isFavorite;
            updateFavoriteState();
            
            // Afișează un mesaj de confirmare
            String message = isFavorite ? "Adăugat la favorite" : "Eliminat din favorite";
            Snackbar.make(favoriteFab, message, Snackbar.LENGTH_SHORT).show();
            
            // TODO: Salvare în baza de date
        });
    }
    
    private void updateFavoriteState() {
        // Actualizează iconița în funcție de stare
        favoriteFab.setIcon(getDrawable(isFavorite ? 
                android.R.drawable.btn_star_big_on : 
                android.R.drawable.btn_star_big_off));
        
        // Animație de tranziție
        if (isFavorite) {
            favoriteFab.extend();
        } else {
            favoriteFab.shrink();
            favoriteFab.postDelayed(() -> favoriteFab.extend(), 1000);
        }
    }
    
    private void loadRecipeData() {
        if (recipeId != -1) {
            // Load by ID
            recipe = dbHelper.getRecipeById(recipeId);
        } else if (recipeTitle != null && recipeRegion != null) {
            // Load by title and region
            recipe = dbHelper.getRecipeByTitleAndRegion(recipeTitle, recipeRegion);
        }
        
        if (recipe == null) {
            // Recipe not found, show error and finish
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void loadRecipeDetails() {
        // TODO: În implementarea reală, aici ar trebui încărcat din baza de date sau API
        
        // Setează titlul
        collapsingToolbar.setTitle(recipe.getTitle());
        
        // Setează informații despre rețetă în card
        if (timeTextView != null) {
            timeTextView.setText(recipe.getTime());
        }
        
        if (difficultyTextView != null) {
            difficultyTextView.setText(recipe.getDifficulty());
        }
        
        if (regionChip != null) {
            regionChip.setText(recipe.getRegion());
        }
        
        if (categoryChip != null) {
            categoryChip.setText(recipe.getCategory());
        }
        
        if (recipeDescription != null) {
            recipeDescription.setText(recipe.getDescription());
        }
        
        // Set recipe image
        if (recipeImageView != null) {
            if (recipe.getImageResourceId() > 0) {
                recipeImageView.setImageResource(recipe.getImageResourceId());
            } else {
                recipeImageView.setImageResource(R.drawable.placeholder_recipe);
            }
        }
        
        // The following code is now moved to appropriate fragments:
        // - Nutritional info display
        // - Ingredients list
        // - Cooking steps
        // - Reviews
    }
    
    /**
     * Partajează rețeta cu alte aplicații
     */
    private void shareRecipe() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        
        // Construiește textul pentru sharing
        String shareText = "Sarmale tradiționale - O rețetă delicioasă din bucătăria românească\n\n" +
                "Ingrediente principale:\n" +
                "- Varză murată\n" +
                "- Carne tocată (porc și vită)\n" +
                "- Orez\n" +
                "- Ceapă\n\n" +
                "Descarcă aplicația noastră pentru a vedea rețeta completă și multe altele!\n" +
                "https://play.google.com/store/apps/details?id=com.example.myapplication";
        
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, recipe.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        
        // Deschide dialog de sharing
        startActivity(Intent.createChooser(shareIntent, "Distribuie rețeta"));
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe_detail, menu);
        return true;
    }
    
    /**
     * Configurează butoanele de acțiune
     */
    public void onStartCookingClicked(View view) {
        // Eliminăm apelul către funcționalitatea indisponibilă
    }
    
    public void onShareButtonClicked(View view) {
        shareRecipe();
    }
    
    public void onAddToMealPlanClicked(View view) {
        // Eliminăm apelul către funcționalitatea indisponibilă
    }
    
    /**
     * Adapter for ingredients list
     */
    private class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {
        private final List<String> ingredients;
        
        IngredientAdapter(List<String> ingredients) {
            this.ingredients = ingredients;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_ingredient, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.ingredientText.setText(ingredients.get(position));
        }
        
        @Override
        public int getItemCount() {
            return ingredients.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView ingredientText;
            
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ingredientText = itemView.findViewById(R.id.ingredientText);
            }
        }
    }
    
    /**
     * Adapter for cooking steps
     */
    private class StepAdapter extends RecyclerView.Adapter<StepAdapter.ViewHolder> {
        private final List<String> steps;
        
        StepAdapter(List<String> steps) {
            this.steps = steps;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_step, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.stepNumberText.setText(String.valueOf(position + 1));
            holder.stepText.setText(steps.get(position));
        }
        
        @Override
        public int getItemCount() {
            return steps.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView stepNumberText;
            TextView stepText;
            
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                stepNumberText = itemView.findViewById(R.id.stepNumberText);
                stepText = itemView.findViewById(R.id.stepText);
            }
        }
    }
    
    /**
     * Adapter for recipe reviews
     */
    private class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
        private final List<RecipeDBHelper.Review> reviews;
        
        ReviewAdapter(List<RecipeDBHelper.Review> reviews) {
            this.reviews = reviews;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_review, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RecipeDBHelper.Review review = reviews.get(position);
            holder.usernameText.setText(review.getUserName());
            holder.ratingBar.setRating(review.getRating());
            holder.reviewText.setText(review.getReviewText());
            // Format date
            java.text.DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(getApplicationContext());
            String dateString = dateFormat.format(new java.util.Date(review.getDate()));
            holder.dateText.setText(dateString);
        }
        
        @Override
        public int getItemCount() {
            return reviews.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView usernameText;
            android.widget.RatingBar ratingBar;
            TextView reviewText;
            TextView dateText;
            
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                usernameText = itemView.findViewById(R.id.usernameText);
                ratingBar = itemView.findViewById(R.id.rating_review);
                reviewText = itemView.findViewById(R.id.text_review_content);
                dateText = itemView.findViewById(R.id.dateText);
            }
        }
    }

    private String formatNutritionalInfo(NutritionalInfo info) {
        StringBuilder builder = new StringBuilder();
        builder.append(getString(R.string.calories)).append(": ").append(info.getCalories()).append(" kcal\n");
        builder.append(getString(R.string.protein)).append(": ").append(info.getProtein()).append("g\n");
        builder.append(getString(R.string.fat)).append(": ").append(info.getFat()).append("g\n");
        builder.append(getString(R.string.carbs)).append(": ").append(info.getCarbohydrates()).append("g\n");
        builder.append(getString(R.string.fiber)).append(": ").append(info.getFiber()).append("g\n");
        builder.append(getString(R.string.sugar)).append(": ").append(info.getSugar()).append("g\n");
        builder.append(getString(R.string.sodium)).append(": ").append(info.getSodium()).append("mg");
        return builder.toString();
    }

    private void setupViewPager() {
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        
        // Simplificăm implementarea pentru a evita referințele la clase care nu există
        // Vom folosi un adapter simplu fără fragmente
        
        // Creăm un adapter de bază ce nu depinde de fragmente
        viewPager.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Creăm un ViewHolder simplu
                TextView textView = new TextView(parent.getContext());
                textView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                textView.setGravity(Gravity.CENTER);
                textView.setPadding(16, 16, 16, 16);
                
                return new RecyclerView.ViewHolder(textView) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                TextView textView = (TextView) holder.itemView;
                
                switch (position) {
                    case 0:
                        textView.setText("Detalii rețetă " + recipe.getTitle());
                        break;
                    case 1:
                        textView.setText("Ingrediente:\n" + getIngredientsAsString());
                        break;
                    case 2:
                        textView.setText("Pași de preparare:\n" + getPreparationStepsAsString());
                        break;
                }
            }

            @Override
            public int getItemCount() {
                return 3; // Detalii, Ingrediente, Pași
            }
        });
        
        // Configurăm TabLayout fără fragment-uri
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Detalii");
                    break;
                case 1:
                    tab.setText("Ingrediente");
                    break;
                case 2:
                    tab.setText("Preparare");
                    break;
            }
        }).attach();
    }

    /**
     * Converts recipe ingredients to a formatted string
     * @return Formatted ingredients string
     */
    private String getIngredientsAsString() {
        if (recipe == null || recipe.getIngredients() == null) {
            return "Nu există ingrediente disponibile";
        }
        
        StringBuilder sb = new StringBuilder();
        for (String ingredient : recipe.getIngredients()) {
            sb.append("• ").append(ingredient).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Converts preparation steps to a formatted string
     * @return Formatted preparation steps string
     */
    private String getPreparationStepsAsString() {
        if (recipe == null || recipe.getSteps() == null) {
            return "Nu există pași de preparare disponibili";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recipe.getSteps().length; i++) {
            sb.append(i + 1).append(". ").append(recipe.getSteps()[i]).append("\n\n");
        }
        return sb.toString();
    }
}
