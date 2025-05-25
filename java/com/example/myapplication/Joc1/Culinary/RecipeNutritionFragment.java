package com.example.myapplication.Joc1.Culinary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity.Recipe;

/**
 * Fragment to display recipe nutritional information
 */
public class RecipeNutritionFragment extends Fragment {

    private static final String ARG_RECIPE_ID = "recipe_id";
    
    private long recipeId;
    private TextView caloriesTextView;
    private TextView proteinTextView;
    private TextView fatTextView;
    private TextView carbsTextView;
    private TextView noNutritionInfoTextView;
    private RecipeDBHelper dbHelper;
    
    public static RecipeNutritionFragment newInstance(long recipeId) {
        RecipeNutritionFragment fragment = new RecipeNutritionFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RECIPE_ID, recipeId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recipeId = getArguments().getLong(ARG_RECIPE_ID);
        }
        dbHelper = new RecipeDBHelper(requireContext());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_nutrition, container, false);
        
        caloriesTextView = view.findViewById(R.id.caloriesValueTextView);
        proteinTextView = view.findViewById(R.id.proteinValueTextView);
        fatTextView = view.findViewById(R.id.fatValueTextView);
        carbsTextView = view.findViewById(R.id.carbsValueTextView);
        noNutritionInfoTextView = view.findViewById(R.id.noNutritionInfoTextView);
        
        loadNutritionInfo();
        
        return view;
    }
    
    private void loadNutritionInfo() {
        NutritionalInfo info = dbHelper.getNutritionalInfoForRecipe(recipeId);
        
        if (info != null && info.hasNutritionalInformation()) {
            caloriesTextView.setText(String.format("%d kcal", info.getCalories()));
            proteinTextView.setText(String.format("%.1f g", info.getProtein()));
            fatTextView.setText(String.format("%.1f g", info.getFat()));
            carbsTextView.setText(String.format("%.1f g", info.getCarbs()));
            
            noNutritionInfoTextView.setVisibility(View.GONE);
        } else {
            noNutritionInfoTextView.setVisibility(View.VISIBLE);
        }
    }
} 