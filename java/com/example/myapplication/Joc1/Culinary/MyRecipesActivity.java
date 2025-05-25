package com.example.myapplication.Joc1.Culinary;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.myapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity.Recipe;

/**
 * Activity for displaying user's recipes (created, favorited, etc.)
 */
public class MyRecipesActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TextView emptyStateText;
    private RecyclerView recipesRecyclerView;
    private FloatingActionButton addRecipeFab;

    private RecipeDBHelper dbHelper;
    private List<Recipe> favoriteRecipes;
    private List<Recipe> myRecipes;
    private List<Recipe> recentlyViewedRecipes;
    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);
        
        // Initialize database helper
        dbHelper = new RecipeDBHelper(this);
        
        // Initialize UI components
        initViews();
        setupToolbar();
        
        // Set up tabs
        setupTabs();
        
        // Load recipes
        loadRecipes();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload recipes in case something has changed
        loadRecipes();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        emptyStateText = findViewById(R.id.emptyStateText);
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView);
        addRecipeFab = findViewById(R.id.addRecipeFab);
        
        // Set up recycler view
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Set up add recipe button
        addRecipeFab.setOnClickListener(v -> navigateToAddRecipe());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.my_recipes);
        }
    }

    private void setupTabs() {
        // Add tabs
        tabLayout.addTab(tabLayout.newTab().setText(R.string.favorites));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.my_recipes));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.recently_viewed));
        
        // Set up tab selection listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                updateRecipeList();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });
    }

    private void loadRecipes() {
        // Load favorite recipes
        favoriteRecipes = dbHelper.getFavoriteRecipes();
        
        // Load my recipes (created by user - this is a simplified implementation)
        myRecipes = new ArrayList<>(); // In a real app, this would use the database
        
        // Load recently viewed recipes (simplified implementation)
        recentlyViewedRecipes = new ArrayList<>(); // In a real app, this would use the database
        
        // Update the UI
        updateRecipeList();
    }

    private void updateRecipeList() {
        List<Recipe> recipesToShow;
        
        switch (currentTab) {
            case 0: // Favorites
                recipesToShow = favoriteRecipes;
                emptyStateText.setText(R.string.no_favorite_recipes);
                break;
            case 1: // My Recipes
                recipesToShow = myRecipes;
                emptyStateText.setText(R.string.no_created_recipes);
                break;
            case 2: // Recently viewed
                recipesToShow = recentlyViewedRecipes;
                emptyStateText.setText(R.string.no_recent_recipes);
                break;
            default:
                recipesToShow = new ArrayList<>();
                break;
        }
        
        // Show empty state if no recipes
        if (recipesToShow.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recipesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recipesRecyclerView.setVisibility(View.VISIBLE);
            
            // Set up adapter
            RecipeCardAdapter adapter = new RecipeCardAdapter(recipesToShow, recipe -> {
                // Open recipe detail on click
                CulinaryCoordinator.getInstance().navigateToRecipeDetail(
                        this, recipe.getTitle(), recipe.getRegion());
            });
            
            recipesRecyclerView.setAdapter(adapter);
        }
    }

    private void navigateToAddRecipe() {
        Intent intent = new Intent(this, AddRecipeActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
