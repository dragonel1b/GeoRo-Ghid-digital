package com.example.myapplication.Joc1.Culinary;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Material Design 3 Modal Bottom Sheet for adding a meal to the meal planner
 */
public class AddMealBottomSheetDialog extends BottomSheetDialogFragment {
    
    private TextInputLayout dayInputLayout;
    private MaterialAutoCompleteTextView dayDropdown;
    private MaterialRadioButton breakfastRadio, lunchRadio, dinnerRadio;
    private TextInputLayout recipeInputLayout;
    private TextInputEditText selectedRecipeText;
    private MaterialButton cancelButton, addButton;

    private MealPlannerViewModel viewModel;
    private FirebaseAnalytics firebaseAnalytics;
    
    private String selectedRecipeId;
    private String selectedDay;
    private String selectedMealType;

    private OnMealAddedListener onMealAddedListener;

    public interface OnMealAddedListener {
        void onMealAdded(String day, String mealType, String recipeId, String recipeName);
    }

    public static AddMealBottomSheetDialog newInstance() {
        return new AddMealBottomSheetDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use Material3 BottomSheet theme
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_Material3_BottomSheetDialog);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(MealPlannerViewModel.class);
        
        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());
        
        // Log sheet opened event
        Bundle params = new Bundle();
        params.putString("screen_name", "add_meal_bottom_sheet");
        firebaseAnalytics.logEvent("bottom_sheet_opened", params);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_meal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);
        
        // Setup day dropdown
        setupDayDropdown();
        
        // Setup meal type radio buttons
        setupMealTypeRadioGroup();
        
        // Setup recipe selection
        setupRecipeSelection();
        
        // Setup buttons
        setupButtons();
        
        // Load previously selected values (persistence)
        loadSavedSelections();
        
        // Initial validation
        validateForm();
    }

    private void initViews(View view) {
        dayInputLayout = view.findViewById(R.id.dayInputLayout);
        dayDropdown = view.findViewById(R.id.dayDropdown);
        
        breakfastRadio = view.findViewById(R.id.breakfastRadio);
        lunchRadio = view.findViewById(R.id.lunchRadio);
        dinnerRadio = view.findViewById(R.id.dinnerRadio);
        
        recipeInputLayout = view.findViewById(R.id.recipeInputLayout);
        selectedRecipeText = view.findViewById(R.id.selectedRecipeText);
        
        cancelButton = view.findViewById(R.id.cancelButton);
        addButton = view.findViewById(R.id.addButton);
    }

    private void setupDayDropdown() {
        // Day options
        List<String> days = Arrays.asList(
            getString(R.string.monday),
            getString(R.string.tuesday),
            getString(R.string.wednesday),
            getString(R.string.thursday), 
            getString(R.string.friday),
            getString(R.string.saturday),
            getString(R.string.sunday)
        );
        
        // Create adapter for dropdown
        ArrayAdapter<String> daysAdapter = new ArrayAdapter<>(
            requireContext(), 
            R.layout.item_dropdown_menu, 
            days
        );
        
        dayDropdown.setAdapter(daysAdapter);
        
        // Set selection listener
        dayDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedDay = (String) parent.getItemAtPosition(position);
            
            // Log day selected event
            Bundle params = new Bundle();
            params.putString("selected_day", selectedDay);
            firebaseAnalytics.logEvent("day_selected", params);
            
            validateForm();
            
            // Save selection for persistence
            saveSelections();
        });
    }

    private void setupMealTypeRadioGroup() {
        // Set default
        selectedMealType = getString(R.string.breakfast);
        
        breakfastRadio.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedMealType = getString(R.string.breakfast);
                logMealTypeSelected(selectedMealType);
                validateForm();
                saveSelections();
            }
        });
        
        lunchRadio.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedMealType = getString(R.string.lunch);
                logMealTypeSelected(selectedMealType);
                validateForm();
                saveSelections();
            }
        });
        
        dinnerRadio.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedMealType = getString(R.string.dinner);
                logMealTypeSelected(selectedMealType);
                validateForm();
                saveSelections();
            }
        });
    }

    private void logMealTypeSelected(String mealType) {
        Bundle params = new Bundle();
        params.putString("meal_type", mealType);
        firebaseAnalytics.logEvent("meal_type_selected", params);
    }

    private void setupRecipeSelection() {
        // Set click listener to open recipe search dialog
        selectedRecipeText.setOnClickListener(v -> {
            openRecipeSearchDialog();
        });
        
        // Also set click listener on the end icon (search icon)
        recipeInputLayout.setEndIconOnClickListener(v -> {
            openRecipeSearchDialog();
        });
    }

    private void openRecipeSearchDialog() {
        // Create and show search dialog for recipes
        RecipeSearchDialog recipeSearchDialog = RecipeSearchDialog.newInstance();
        recipeSearchDialog.setOnRecipeSelectedListener(this::onRecipeSelected);
        recipeSearchDialog.show(getChildFragmentManager(), "RecipeSearch");
        
        // Log event
        firebaseAnalytics.logEvent("recipe_search_opened", null);
    }

    private void onRecipeSelected(long recipeId, String recipeName) {
        this.selectedRecipeId = String.valueOf(recipeId);
        selectedRecipeText.setText(recipeName);
        
        // Log recipe selected event
        Bundle params = new Bundle();
        params.putString("recipe_id", String.valueOf(recipeId));
        params.putString("recipe_name", recipeName);
        firebaseAnalytics.logEvent("recipe_selected", params);
        
        validateForm();
    }

    private void setupButtons() {
        cancelButton.setOnClickListener(v -> {
            // Log cancel event
            firebaseAnalytics.logEvent("add_meal_cancelled", null);
            dismiss();
        });
        
        addButton.setOnClickListener(v -> {
            if (validateForm()) {
                addMeal();
            }
        });
    }

    private void addMeal() {
        // Add meal through view model
        if (selectedDay != null && selectedMealType != null && selectedRecipeId != null && 
            selectedRecipeText != null && !selectedRecipeText.getText().toString().isEmpty()) {
            
            String recipeName = selectedRecipeText.getText().toString();
            
            // Log meal added event
            Bundle params = new Bundle();
            params.putString("day", selectedDay);
            params.putString("meal_type", selectedMealType);
            params.putString("recipe_id", selectedRecipeId);
            firebaseAnalytics.logEvent("meal_added", params);
            
            // Call listener
            if (onMealAddedListener != null) {
                onMealAddedListener.onMealAdded(
                    selectedDay, 
                    selectedMealType, 
                    selectedRecipeId, 
                    recipeName
                );
            }
            
            // Save selections for next time
            saveSelections();
            
            // Show success message and dismiss
            Toast.makeText(requireContext(), R.string.meal_added_success, Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        // Validate day selection
        if (selectedDay == null || selectedDay.isEmpty()) {
            dayInputLayout.setError(getString(R.string.select_day_error));
            isValid = false;
        } else {
            dayInputLayout.setError(null);
        }
        
        // Validate recipe selection
        if (selectedRecipeId == null || selectedRecipeText.getText().toString().isEmpty()) {
            recipeInputLayout.setError(getString(R.string.select_recipe_error));
            isValid = false;
        } else {
            recipeInputLayout.setError(null);
        }
        
        // Update add button state
        addButton.setEnabled(isValid);
        
        return isValid;
    }

    private void loadSavedSelections() {
        SharedPreferences prefs = requireContext().getSharedPreferences(
            "meal_planner_prefs", Context.MODE_PRIVATE);
        
        // Load last selected day
        String lastDay = prefs.getString("last_selected_day", null);
        if (lastDay != null) {
            dayDropdown.setText(lastDay, false);
            selectedDay = lastDay;
        }
        
        // Load last selected meal type
        String lastMealType = prefs.getString("last_selected_meal_type", null);
        if (lastMealType != null) {
            if (lastMealType.equals(getString(R.string.breakfast))) {
                breakfastRadio.setChecked(true);
            } else if (lastMealType.equals(getString(R.string.lunch))) {
                lunchRadio.setChecked(true);
            } else if (lastMealType.equals(getString(R.string.dinner))) {
                dinnerRadio.setChecked(true);
            }
            selectedMealType = lastMealType;
        }
        
        validateForm();
    }

    private void saveSelections() {
        SharedPreferences prefs = requireContext().getSharedPreferences(
            "meal_planner_prefs", Context.MODE_PRIVATE);
        
        SharedPreferences.Editor editor = prefs.edit();
        
        // Save selected day
        if (selectedDay != null) {
            editor.putString("last_selected_day", selectedDay);
        }
        
        // Save selected meal type
        if (selectedMealType != null) {
            editor.putString("last_selected_meal_type", selectedMealType);
        }
        
        editor.apply();
    }

    public void setOnMealAddedListener(OnMealAddedListener listener) {
        this.onMealAddedListener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        firebaseAnalytics.logEvent("add_meal_bottom_sheet_dismissed", null);
    }
} 