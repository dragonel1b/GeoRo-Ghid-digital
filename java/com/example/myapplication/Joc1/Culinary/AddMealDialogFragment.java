package com.example.myapplication.Joc1.Culinary;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.Joc1.Culinary.RecipeSelectionActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Dialog fragment for adding meals to the weekly meal planner
 */
public class AddMealDialogFragment extends DialogFragment {

    private OnMealAddedListener mealAddedListener;
    private Spinner daySpinner;
    private RadioGroup mealTypeGroup;
    private Button selectRecipeButton;
    private Button cancelButton;
    private Button addButton;
    
    private String selectedRecipeTitle;
    private String selectedRecipeRegion;
    private static final int REQUEST_SELECT_RECIPE = 100;

    public interface OnMealAddedListener {
        void onMealAdded(String day, String mealType, String recipeTitle, String recipeRegion);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Try to find the WeeklyPlanningFragment to set as listener
        try {
            // First check if parent fragment implements the interface
            if (getParentFragment() instanceof OnMealAddedListener) {
                mealAddedListener = (OnMealAddedListener) getParentFragment();
            } 
            // Then check if the activity implements it
            else if (context instanceof OnMealAddedListener) {
                mealAddedListener = (OnMealAddedListener) context;
            } 
            // Finally, try to find the WeeklyPlanningFragment from the activity's fragments
            else if (getActivity() != null) {
                for (Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
                    if (fragment instanceof WeeklyPlanningFragment) {
                        mealAddedListener = (OnMealAddedListener) fragment;
                        break;
                    }
                }
            }
        } catch (ClassCastException e) {
            throw new ClassCastException("Host must implement OnMealAddedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.add_meal);
        
        // Inflate the custom view
        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_add_meal, null);
        builder.setView(view);
        
        // Initialize views
        initializeViews(view);
        
        return builder.create();
    }

    private void initializeViews(View view) {
        daySpinner = view.findViewById(R.id.daySpinner);
        mealTypeGroup = view.findViewById(R.id.mealTypeGroup);
        selectRecipeButton = view.findViewById(R.id.selectRecipeButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        addButton = view.findViewById(R.id.addButton);
        
        // Setup day spinner
        setupDaySpinner();
        
        // Setup select recipe button
        selectRecipeButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RecipeSelectionActivity.class);
            startActivityForResult(intent, REQUEST_SELECT_RECIPE);
        });
        
        // Setup cancel button
        cancelButton.setOnClickListener(v -> dismiss());
        
        // Setup add button (disabled until recipe is selected)
        addButton.setEnabled(false);
        addButton.setOnClickListener(v -> addMealToPlanner());
    }
    
    private void setupDaySpinner() {
        // Create day options
        String[] days = {"Luni", "Marți", "Miercuri", "Joi", "Vineri", "Sâmbătă", "Duminică"};
        
        // Set default selection to current day
        int currentDayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
        if (currentDayIndex < 0) currentDayIndex = 6; // Sunday becomes index 6
        
        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, days);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        // Set adapter and default selection
        daySpinner.setAdapter(adapter);
        daySpinner.setSelection(currentDayIndex);
    }
    
    private void addMealToPlanner() {
        if (selectedRecipeTitle == null || selectedRecipeRegion == null) {
            return;
        }
        
        // Get selected day
        String day = daySpinner.getSelectedItem().toString();
        
        // Get selected meal type
        int selectedId = mealTypeGroup.getCheckedRadioButtonId();
        RadioButton radioButton = mealTypeGroup.findViewById(selectedId);
        String mealType = radioButton.getText().toString();
        
        // Notify listener
        if (mealAddedListener != null) {
            mealAddedListener.onMealAdded(day, mealType, selectedRecipeTitle, selectedRecipeRegion);
        }
        
        // Dismiss dialog
        dismiss();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_SELECT_RECIPE && resultCode == getActivity().RESULT_OK && data != null) {
            // Get selected recipe information
            selectedRecipeTitle = data.getStringExtra("recipe_title");
            selectedRecipeRegion = data.getStringExtra("recipe_region");
            
            // Update UI
            if (selectedRecipeTitle != null) {
                selectRecipeButton.setText(selectedRecipeTitle);
                addButton.setEnabled(true);
            }
        }
    }
} 