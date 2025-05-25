package com.example.myapplication.Joc1.Culinary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity.Recipe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;

public class RomCulinaryActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeActionListener {

    private static List<Recipe> recipes = new ArrayList<>();
    private List<Recipe> filteredRecipes;
    private RecipeAdapter recipeAdapter;
    private RecipeDBHelper dbHelper;
    private ChipGroup regionChipGroup;
    private ChipGroup categoryChipGroup;
    private TextView emptyStateText;
    private FloatingActionButton addRecipeButton;
    
    // RecyclerViews for categories, popular and recent recipes
    private RecyclerView categoriesRecyclerView;
    private RecyclerView popularRecipesRecyclerView;
    private RecyclerView recentRecipesRecyclerView;
    
    // Adapters
    private RecipeCardAdapter categoriesAdapter;
    private RecipeCardAdapter popularRecipesAdapter;
    private RecipeCardAdapter recentRecipesAdapter;

    public static List<Recipe> getRecipes() {
        return recipes;
    }
    
    public static void setRecipes(List<Recipe> newRecipes) {
        recipes = new ArrayList<>(newRecipes);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_culinary);

        // Initialize views
        initializeViews();
        
        // Initialize database helper
        dbHelper = new RecipeDBHelper(this);
        
        // Load recipes
        loadRecipes();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup filters
        setupFilters();
        
        // Setup categories, popular and recent recipes
        setupCategoriesRecyclerView();
        setupPopularRecipesRecyclerView();
        setupRecentRecipesRecyclerView();
        
        // Update UI state
        updateEmptyState();
        
        // Set featured recipe
        setupFeaturedRecipe();
    }

    private void initializeViews() {
        regionChipGroup = findViewById(R.id.regionChipGroup);
        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        emptyStateText = findViewById(R.id.emptyStateText);
        addRecipeButton = findViewById(R.id.addRecipeButton);
        
        // Initialize new RecyclerViews
        categoriesRecyclerView = findViewById(R.id.categories_recycler);
        popularRecipesRecyclerView = findViewById(R.id.popular_recipes_recycler);
        recentRecipesRecyclerView = findViewById(R.id.recent_recipes_recycler);
        
        addRecipeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddRecipeActivity.class);
            startActivity(intent);
        });
    }

    private void loadRecipes() {
        // Încarcă rețetele din baza de date locală
        recipes = dbHelper.getAllRecipes();
        
        // Încarcă rețete din Firebase
        loadRecipesFromFirebase();
        
        // Dacă nu există rețete în baza de date, folosește rețetele din exemplu
        if (recipes == null || recipes.isEmpty()) {
            recipes = ModernCulinaryActivity.getRecipes();
        }
        
        filteredRecipes = new ArrayList<>(recipes);
    }
    
    private void loadRecipesFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference recipesRef = database.getReference("recipes");
        
        recipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Recipe> recipeList = new ArrayList<>();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        recipeList.add(recipe);
                    }
                }
                
                if (!recipeList.isEmpty()) {
                    // Actualizează lista de rețete
                    recipes = recipeList;
                    filteredRecipes = new ArrayList<>(recipes);
                    
                    // Actualizează UI-ul
                    if (recipeAdapter != null) {
                        recipeAdapter.updateRecipes(filteredRecipes);
                    }
                    
                    setupCategoriesRecyclerView();
                    setupPopularRecipesRecyclerView();
                    setupRecentRecipesRecyclerView();
                    setupFeaturedRecipe();
                    updateEmptyState();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RomCulinaryActivity.this, 
                               "Eroare la încărcarea rețetelor: " + databaseError.getMessage(), 
                               Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        RecyclerView recipesRecyclerView = findViewById(R.id.recipesRecyclerView);
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        recipeAdapter = new RecipeAdapter(filteredRecipes, this, this);
        recipesRecyclerView.setAdapter(recipeAdapter);
    }
    
    private void setupCategoriesRecyclerView() {
        // Obține categoriile unice
        List<String> categories = recipes.stream()
                .map(Recipe::getCategory)
                .distinct()
                .collect(Collectors.toList());
        
        // Pentru fiecare categorie, găsește o rețetă reprezentativă
        List<Recipe> categoryRecipes = new ArrayList<>();
        for (String category : categories) {
            // Găsește prima rețetă din categorie
            Recipe recipe = recipes.stream()
                    .filter(r -> r.getCategory().equals(category))
                    .findFirst()
                    .orElse(null);
            
            if (recipe != null) {
                categoryRecipes.add(recipe);
            }
        }
        
        // Setează layout manager pentru afișare orizontală
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);
        categoriesRecyclerView.setLayoutManager(layoutManager);
        
        // Creează și setează adapter
        categoriesAdapter = new RecipeCardAdapter(categoryRecipes, recipe -> {
            // Filtrează rețetele după categorie când se face click
            Chip chip = null;
            
            // Găsește chip-ul corespunzător categoriei
            for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                View view = categoryChipGroup.getChildAt(i);
                if (view instanceof Chip) {
                    Chip c = (Chip) view;
                    if (c.getText().toString().equals(recipe.getCategory())) {
                        chip = c;
                        break;
                    }
                }
            }
            
            // Aplică filtrul selectând chip-ul
            if (chip != null) {
                chip.setChecked(true);
            }
        });
        
        categoriesRecyclerView.setAdapter(categoriesAdapter);
    }
    
    private void setupPopularRecipesRecyclerView() {
        // Obține rețetele populare (cele cu rating mare)
        List<Recipe> popularRecipes = recipes.stream()
                .sorted(Comparator.comparing(Recipe::getRating).reversed())
                .limit(5)  // Limitează la 5 rețete
                .collect(Collectors.toList());
        
        // Setează layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);
        popularRecipesRecyclerView.setLayoutManager(layoutManager);
        
        // Creează și setează adapter
        popularRecipesAdapter = new RecipeCardAdapter(popularRecipes, recipe -> {
            // Deschide detaliile rețetei când se face click
            openRecipeDetail(recipe);
        });
        
        popularRecipesRecyclerView.setAdapter(popularRecipesAdapter);
    }
    
    private void setupRecentRecipesRecyclerView() {
        // În implementarea reală, ar trebui să sortăm după data adăugării
        // Deoarece nu avem această informație, vom folosi ID-urile (presupunând că ID-urile mai mari sunt mai recente)
        List<Recipe> recentRecipes = recipes.stream()
                .sorted(Comparator.comparing(Recipe::getId).reversed())
                .limit(5)  // Limitează la 5 rețete
                .collect(Collectors.toList());
        
        // Setează layout manager
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);
        recentRecipesRecyclerView.setLayoutManager(horizontalLayoutManager);
        
        // Creează și setează adapter
        recentRecipesAdapter = new RecipeCardAdapter(recentRecipes, recipe -> {
            // Deschide detaliile rețetei când se face click
            openRecipeDetail(recipe);
        });
        
        recentRecipesRecyclerView.setAdapter(recentRecipesAdapter);
    }
    
    private void setupFeaturedRecipe() {
        if (recipes.isEmpty()) {
            return;
        }
        
        // Găsește rețeta cu cel mai mare rating
        Recipe featuredRecipe = recipes.stream()
                .max(Comparator.comparing(Recipe::getRating))
                .orElse(recipes.get(0));
        
        // Setează informațiile pentru rețeta prezentată
        ImageView featuredRecipeImage = findViewById(R.id.featured_recipe_image);
        TextView featuredRecipeTitle = findViewById(R.id.featured_recipe_title);
        
        // Setează imaginea (în implementarea reală, ar trebui să încarce imaginea din resurse sau URL)
        featuredRecipeImage.setImageResource(R.drawable.placeholder_recipe);
        
        // Setează titlul
        featuredRecipeTitle.setText(featuredRecipe.getTitle());
        
        // Setează click listener pentru a deschide detaliile rețetei
        findViewById(R.id.featured_recipe_card).setOnClickListener(v -> {
            openRecipeDetail(featuredRecipe);
        });
    }

    private void setupFilters() {
        // Get unique regions and categories
        List<String> regions = recipes.stream()
                .map(Recipe::getRegion)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
                
        List<String> categories = recipes.stream()
                .map(Recipe::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        // Add chips for regions
        for (String region : regions) {
            Chip chip = new Chip(this);
            chip.setText(region);
            chip.setCheckable(true);
            regionChipGroup.addView(chip);
        }
        
        // Add chips for categories
        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            categoryChipGroup.addView(chip);
        }
        
        // Add filter listeners
        regionChipGroup.setOnCheckedChangeListener((group, checkedId) -> applyFilters());
        categoryChipGroup.setOnCheckedChangeListener((group, checkedId) -> applyFilters());
    }

    private void applyFilters() {
        filteredRecipes = new ArrayList<>(recipes);
        
        // Apply region filter
        int selectedRegionId = regionChipGroup.getCheckedChipId();
        if (selectedRegionId != View.NO_ID) {
            Chip selectedRegion = findViewById(selectedRegionId);
            String region = selectedRegion.getText().toString();
            filteredRecipes = filteredRecipes.stream()
                    .filter(recipe -> recipe.getRegion().equals(region))
                    .collect(Collectors.toList());
        }
        
        // Apply category filter
        int selectedCategoryId = categoryChipGroup.getCheckedChipId();
        if (selectedCategoryId != View.NO_ID) {
            Chip selectedCategory = findViewById(selectedCategoryId);
            String category = selectedCategory.getText().toString();
            filteredRecipes = filteredRecipes.stream()
                    .filter(recipe -> recipe.getCategory().equals(category))
                    .collect(Collectors.toList());
        }
        
        // Update adapter
        recipeAdapter.updateRecipes(filteredRecipes);
        
        // Update empty state
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredRecipes.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            emptyStateText.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipes();
        applyFilters();
        setupCategoriesRecyclerView();
        setupPopularRecipesRecyclerView();
        setupRecentRecipesRecyclerView();
        setupFeaturedRecipe();
    }

    /**
     * Open recipe detail screen
     * @param recipe Recipe to display
     */
    private void openRecipeDetail(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipe_id", recipe.getId());
        startActivity(intent);
    }

    // OnRecipeActionListener implementation
    @Override
    public void onRecipeAction(Recipe recipe) {
        openRecipeDetail(recipe);
    }

    public void onFavoriteClick(Recipe recipe) {
        recipe.toggleFavorite();
        if (dbHelper != null) {
            dbHelper.updateFavoriteStatus(recipe.getId(), recipe.isFavorite());
        }
        recipeAdapter.notifyDataSetChanged();
    }
}
