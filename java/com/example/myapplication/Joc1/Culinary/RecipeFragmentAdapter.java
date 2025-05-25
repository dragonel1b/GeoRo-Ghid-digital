package com.example.myapplication.Joc1.Culinary;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter pentru ViewPager2 care gestionează fragmentele pentru detaliile rețetei
 */
public class RecipeFragmentAdapter extends FragmentStateAdapter {
    
    private static final int NUM_TABS = 4;
    private static final int TAB_INGREDIENTS = 0;
    private static final int TAB_STEPS = 1;
    private static final int TAB_REVIEWS = 2;
    private static final int TAB_NUTRITION = 3;
    
    private final long recipeId;
    
    public RecipeFragmentAdapter(FragmentActivity activity, long recipeId) {
        super(activity);
        this.recipeId = recipeId;
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Creează fragmentul corespunzător pentru fiecare tab
        switch (position) {
            case TAB_INGREDIENTS:
                return RecipeIngredientsFragment.newInstance(recipeId);
            case TAB_STEPS:
                return RecipeStepsFragment.newInstance(recipeId);
            case TAB_REVIEWS:
                return RecipeReviewsFragment.newInstance(recipeId);
            case TAB_NUTRITION:
                return RecipeNutritionFragment.newInstance(recipeId);
            default:
                throw new IllegalArgumentException("Poziție invalidă: " + position);
        }
    }
    
    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
} 