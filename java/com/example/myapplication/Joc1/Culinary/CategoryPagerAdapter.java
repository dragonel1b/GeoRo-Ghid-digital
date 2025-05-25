package com.example.myapplication.Joc1.Culinary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.example.myapplication.R;
import java.util.List;
import java.util.ArrayList;

/**
 * Adapter for category-based paging in the modern culinary interface
 */
public class CategoryPagerAdapter extends PagerAdapter {
    private final Context context;
    private final String[] categories;
    private final List<List<ModernCulinaryActivity.Recipe>> categorizedRecipes;
    
    /**
     * Constructor for CategoryPagerAdapter
     * @param context Context for inflating views
     */
    public CategoryPagerAdapter(Context context) {
        this.context = context;
        this.categories = CulinaryUtils.CATEGORIES;
        this.categorizedRecipes = new ArrayList<>();
        
        // Initialize lists for each category
        for (int i = 0; i < categories.length; i++) {
            categorizedRecipes.add(new ArrayList<>());
        }
        
        // Load recipes for each category 
        // This is a simplified implementation
        loadRecipes();
    }
    
    private void loadRecipes() {
        // For now, create sample recipes for each category
        RecipeManager recipeManager = RecipeManager.getInstance(context);
        List<ModernCulinaryActivity.Recipe> allRecipes = recipeManager.getAllRecipes();
        
        // Categorize recipes
        for (ModernCulinaryActivity.Recipe recipe : allRecipes) {
            // Find which category the recipe belongs to
            for (int i = 0; i < categories.length; i++) {
                if (i == 0 || categories[i].equals(recipe.getCategory())) {
                    // Add to "All" category (index 0) or matching category
                    categorizedRecipes.get(i).add(recipe);
                }
            }
        }
    }
    
    @Override
    public int getCount() {
        return categories.length;
    }
    
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
    
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        // Inflate layout for this category page
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.category_page_layout, container, false);
        
        // Set up RecyclerView for this category
        RecyclerView recyclerView = view.findViewById(R.id.categoryRecipesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        
        // Create and set adapter for the recipes in this category
        RecipeCardAdapter adapter = new RecipeCardAdapter(
                categorizedRecipes.get(position),
                recipe -> {
                    // Handle recipe click
                    if (context instanceof ModernCulinaryActivity) {
                        ((ModernCulinaryActivity) context).openRecipeDetail(recipe);
                    }
                });
        recyclerView.setAdapter(adapter);
        
        // Add the view to container
        container.addView(view);
        return view;
    }
    
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
    
    @Override
    public CharSequence getPageTitle(int position) {
        return categories[position];
    }
    
    /**
     * Update recipes for all categories
     * @param allRecipes List of all recipes
     */
    public void updateRecipes(List<ModernCulinaryActivity.Recipe> allRecipes) {
        // Clear existing lists
        for (List<ModernCulinaryActivity.Recipe> categoryList : categorizedRecipes) {
            categoryList.clear();
        }
        
        // Recategorize recipes
        for (ModernCulinaryActivity.Recipe recipe : allRecipes) {
            // Add to "All" category (index 0)
            categorizedRecipes.get(0).add(recipe);
            
            // Find which specific category the recipe belongs to
            for (int i = 1; i < categories.length; i++) {
                if (categories[i].equals(recipe.getCategory())) {
                    categorizedRecipes.get(i).add(recipe);
                    break;
                }
            }
        }
        
        // Notify that data has changed (this requires implementation in child adapters)
        notifyDataSetChanged();
    }
} 