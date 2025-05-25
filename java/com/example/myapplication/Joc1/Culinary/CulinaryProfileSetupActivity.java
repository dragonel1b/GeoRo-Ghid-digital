package com.example.myapplication.Joc1.Culinary;

import android.content.Intent;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

import com.example.myapplication.Joc1.Culinary.UserCulinaryProfile;

public class CulinaryProfileSetupActivity extends AppCompatActivity {
    private UserCulinaryProfile userProfile;
    
    // UI Components
    private CircleImageView profileImageView;
    private MaterialButton uploadPhotoButton;
    
    private AutoCompleteTextView skillLevelAutoCompleteTextView;
    
    private ChipGroup dietaryPreferencesChipGroup;
    private Chip dietNoneChip, dietVegetarianChip, dietVeganChip, dietGlutenFreeChip, 
                 dietLactoseFreeChip, dietKetoChip, dietLowCarbChip, addDietaryPreferenceChip;
    
    private TextInputEditText allergyInput;
    private TextInputLayout allergyInputLayout;
    private ChipGroup allergiesChipGroup;
    
    private ChipGroup mealTimesChipGroup;
    private Chip mealBreakfastChip, mealLunchChip, mealDinnerChip, mealSnackChip;
    
    private AutoCompleteTextView cuisineAutoCompleteTextView;
    private TextInputLayout cuisineInputLayout;
    private ChipGroup cuisineChipGroup;
    
    private MaterialButton saveProfileButton;

    // Data
    private Uri profileImageUri;
    private List<String> skillLevels = Arrays.asList("Începător", "Intermediar", "Avansat", "Profesionist");
    private List<String> availableCuisines = Arrays.asList(
            "Românească", "Italiană", "Franceză", "Asiatică", "Mediteraneană", "Mexicană", 
            "Indiană", "Japoneză", "Chinezească", "Grecească", "Spaniolă", "Turcească", 
            "Libaneză", "Americană", "Germană", "Ungurească"
    );
    
    // Firebase Analytics
    private FirebaseAnalytics firebaseAnalytics;
    
    // Activity result launcher for image picking
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        // Try to get persistable URI permission if possible
                        final int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        
                        // Check if the URI scheme allows taking persistable permissions
                        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
                            try {
                                getContentResolver().takePersistableUriPermission(uri, flags);
                            } catch (SecurityException e) {
                                // It's okay if we can't get persistable permission,
                                // we'll still try to use the URI
                                Log.w("CulinaryProfileSetup", "Couldn't take persistable permission for URI: " + e.getMessage());
                            }
                        }
                        
                        profileImageUri = uri;
                        profileImageView.setImageURI(uri);
                        logAnalyticsEvent("profile_image_uploaded");
                    } catch (Exception e) {
                        Log.e("CulinaryProfileSetup", "Error setting profile image: " + e.getMessage());
                        Toast.makeText(CulinaryProfileSetupActivity.this, "Eroare la încărcarea imaginii", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_culinary_profile_setup);
        
        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        
        // Initialize the user profile singleton
        userProfile = UserCulinaryProfile.getInstance(this);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Initialize views
        initializeViews();
        
        // Setup adapters
        setupAdapters();
        
        // Load existing profile data (if any)
        loadProfileData();
        
        // Set up listeners
        setupListeners();
    }
    
    private void initializeViews() {
        // Profile image
        profileImageView = findViewById(R.id.profileImageView);
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        
        // Skill level dropdown
        skillLevelAutoCompleteTextView = findViewById(R.id.skillLevelAutoCompleteTextView);
        
        // Dietary preferences
        dietaryPreferencesChipGroup = findViewById(R.id.dietaryPreferencesChipGroup);
        dietNoneChip = findViewById(R.id.dietNoneChip);
        dietVegetarianChip = findViewById(R.id.dietVegetarianChip);
        dietVeganChip = findViewById(R.id.dietVeganChip);
        dietGlutenFreeChip = findViewById(R.id.dietGlutenFreeChip);
        dietLactoseFreeChip = findViewById(R.id.dietLactoseFreeChip);
        dietKetoChip = findViewById(R.id.dietKetoChip);
        dietLowCarbChip = findViewById(R.id.dietLowCarbChip);
        addDietaryPreferenceChip = findViewById(R.id.addDietaryPreferenceChip);
        
        // Allergies section
        allergyInput = findViewById(R.id.allergyInput);
        allergyInputLayout = findViewById(R.id.allergyInputLayout);
        allergiesChipGroup = findViewById(R.id.allergiesChipGroup);
        
        // Meal times
        mealTimesChipGroup = findViewById(R.id.mealTimesChipGroup);
        mealBreakfastChip = findViewById(R.id.mealBreakfastChip);
        mealLunchChip = findViewById(R.id.mealLunchChip);
        mealDinnerChip = findViewById(R.id.mealDinnerChip);
        mealSnackChip = findViewById(R.id.mealSnackChip);
        
        // Cuisine preferences
        cuisineAutoCompleteTextView = findViewById(R.id.cuisineAutoCompleteTextView);
        cuisineInputLayout = findViewById(R.id.cuisineInputLayout);
        cuisineChipGroup = findViewById(R.id.cuisineChipGroup);
        
        // Save button
        saveProfileButton = findViewById(R.id.saveProfileButton);
    }
    
    private void setupAdapters() {
        // Skill Level adapter
        ArrayAdapter<String> skillLevelAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, skillLevels);
        skillLevelAutoCompleteTextView.setAdapter(skillLevelAdapter);
        
        // Cuisine adapter
        ArrayAdapter<String> cuisineAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, availableCuisines);
        cuisineAutoCompleteTextView.setAdapter(cuisineAdapter);
    }
    
    private void setupListeners() {
        // Profile Image
        uploadPhotoButton.setOnClickListener(v -> pickImage());
        
        // Add dietary preference
        addDietaryPreferenceChip.setOnClickListener(v -> showAddDietaryPreferenceDialog());
        
        // Handle allergy input
        allergyInputLayout.setEndIconOnClickListener(v -> addAllergy());
        allergyInput.setOnEditorActionListener((v, actionId, event) -> {
            addAllergy();
            return true;
        });
        
        // Handle cuisine input
        cuisineInputLayout.setEndIconOnClickListener(v -> addCuisine());
        
        // Handle diet none chip selection to deselect others
        dietNoneChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck all other diet chips
                deselectOtherDietChips();
            }
        });
        
        // Handle other diet chips to deselect 'none' chip
        setupDietChipsListeners();
        
        // Save button
        saveProfileButton.setOnClickListener(v -> saveProfile());
    }
    
    private void setupDietChipsListeners() {
        List<Chip> dietChips = Arrays.asList(
                dietVegetarianChip, dietVeganChip, dietGlutenFreeChip, 
                dietLactoseFreeChip, dietKetoChip, dietLowCarbChip);
        
        for (Chip chip : dietChips) {
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Uncheck the 'none' chip
                    dietNoneChip.setChecked(false);
                }
                
                // Check if all diet chips are unchecked
                boolean anyDietSelected = false;
                for (Chip c : dietChips) {
                    if (c.isChecked()) {
                        anyDietSelected = true;
                        break;
                    }
                }
                
                // If no other diet is selected, check the 'none' chip
                if (!anyDietSelected && !dietNoneChip.isChecked()) {
                    dietNoneChip.setChecked(true);
                }
            });
        }
    }
    
    private void deselectOtherDietChips() {
        dietVegetarianChip.setChecked(false);
        dietVeganChip.setChecked(false);
        dietGlutenFreeChip.setChecked(false);
        dietLactoseFreeChip.setChecked(false);
        dietKetoChip.setChecked(false);
        dietLowCarbChip.setChecked(false);
    }
    
    private void loadProfileData() {
        // Load profile image
        // Note: In a real app, we'd load this from storage or a URL
        
        // Load skill level
        String skillLevel = userProfile.getSkillLevel();
        if (skillLevel.equals(UserCulinaryProfile.SKILL_BEGINNER)) {
            skillLevelAutoCompleteTextView.setText(skillLevels.get(0));
        } else if (skillLevel.equals(UserCulinaryProfile.SKILL_INTERMEDIATE)) {
            skillLevelAutoCompleteTextView.setText(skillLevels.get(1));
        } else if (skillLevel.equals(UserCulinaryProfile.SKILL_ADVANCED)) {
            skillLevelAutoCompleteTextView.setText(skillLevels.get(2));
        }
        
        // Load dietary preferences
        Set<String> dietaryPreferences = userProfile.getDietaryPreferences();
        if (dietaryPreferences.isEmpty() || dietaryPreferences.contains(UserCulinaryProfile.DIET_NONE)) {
            dietNoneChip.setChecked(true);
        } else {
            dietNoneChip.setChecked(false);
            dietVegetarianChip.setChecked(dietaryPreferences.contains(UserCulinaryProfile.DIET_VEGETARIAN));
            dietVeganChip.setChecked(dietaryPreferences.contains(UserCulinaryProfile.DIET_VEGAN));
            dietGlutenFreeChip.setChecked(dietaryPreferences.contains(UserCulinaryProfile.DIET_GLUTEN_FREE));
            dietLactoseFreeChip.setChecked(dietaryPreferences.contains(UserCulinaryProfile.DIET_LACTOSE_FREE));
            dietKetoChip.setChecked(dietaryPreferences.contains(UserCulinaryProfile.DIET_KETO));
            dietLowCarbChip.setChecked(dietaryPreferences.contains(UserCulinaryProfile.DIET_LOW_CARB));
        }
        
        // Load allergies
        allergiesChipGroup.removeAllViews();
        for (String allergy : userProfile.getAllergies()) {
            addAllergyChip(allergy);
        }
        
        // Load meal times
        Set<String> mealTimes = userProfile.getFavoriteMealTimes();
        mealBreakfastChip.setChecked(mealTimes.contains(UserCulinaryProfile.MEAL_BREAKFAST));
        mealLunchChip.setChecked(mealTimes.contains(UserCulinaryProfile.MEAL_LUNCH));
        mealDinnerChip.setChecked(mealTimes.contains(UserCulinaryProfile.MEAL_DINNER));
        mealSnackChip.setChecked(mealTimes.contains(UserCulinaryProfile.MEAL_SNACK));
        
        // Load cuisine preferences
        cuisineChipGroup.removeAllViews();
        for (String cuisine : userProfile.getFavoriteCuisines()) {
            addCuisineChip(cuisine);
        }
    }
    
    private void pickImage() {
        pickImageLauncher.launch("image/*");
    }
    
    private void showAddDietaryPreferenceDialog() {
        TextInputEditText input = new TextInputEditText(this);
        input.setHint("Introduceți preferința alimentară");
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Adăugare preferință alimentară")
                .setView(input)
                .setPositiveButton("Adaugă", (dialog, which) -> {
                    String preference = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(preference)) {
                        addCustomDietaryPreference(preference);
                        logAnalyticsEvent("custom_diet_added");
        }
                })
                .setNegativeButton("Anulare", null)
                .show();
    }
    
    private void addCustomDietaryPreference(String preference) {
        // Create and add a custom dietary preference chip
        Chip chip = new Chip(this);
        chip.setText(preference);
        chip.setCheckable(true);
        chip.setChecked(true);
        chip.setChipBackgroundColorResource(R.color.culinary_chip_background);
        chip.setCloseIconVisible(true);
        
        // Close icon removes the chip
        chip.setOnCloseIconClickListener(v -> {
            dietaryPreferencesChipGroup.removeView(chip);
            // If no other diet is selected, check the 'none' chip
            if (dietaryPreferencesChipGroup.getChildCount() <= 1) {
                dietNoneChip.setChecked(true);
            }
        });
        
        // Uncheck 'none' when adding custom preference
        dietNoneChip.setChecked(false);
        
        // Add to group before the "Add" chip
        dietaryPreferencesChipGroup.addView(chip, dietaryPreferencesChipGroup.getChildCount() - 1);
    }
    
    private void addAllergy() {
            String allergy = allergyInput.getText().toString().trim();
            if (!TextUtils.isEmpty(allergy)) {
                addAllergyChip(allergy);
                allergyInput.setText("");
            logAnalyticsEvent("allergy_added");
            }
    }
    
    private void addAllergyChip(String allergy) {
        Chip chip = new Chip(this);
        chip.setText(allergy);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.culinary_chip_background);
        
        // Close icon removes the chip
        chip.setOnCloseIconClickListener(v -> allergiesChipGroup.removeView(chip));
        
        allergiesChipGroup.addView(chip);
    }
    
    private void addCuisine() {
        String cuisine = cuisineAutoCompleteTextView.getText().toString().trim();
        if (!TextUtils.isEmpty(cuisine)) {
            addCuisineChip(cuisine);
            cuisineAutoCompleteTextView.setText("");
            logAnalyticsEvent("cuisine_added");
        }
    }
    
    private void addCuisineChip(String cuisine) {
        // Check if this cuisine is already added
        for (int i = 0; i < cuisineChipGroup.getChildCount(); i++) {
            View view = cuisineChipGroup.getChildAt(i);
            if (view instanceof Chip && ((Chip) view).getText().toString().equals(cuisine)) {
                return; // Already exists
            }
        }
        
        Chip chip = new Chip(this);
        chip.setText(cuisine);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.culinary_chip_background);
        
        // Close icon removes the chip
        chip.setOnCloseIconClickListener(v -> cuisineChipGroup.removeView(chip));
        
        cuisineChipGroup.addView(chip);
    }
    
    private void saveProfile() {
        // Validate input
        if (!validateForm()) {
            return;
        }
        
        // Save skill level
        String skillLevel;
        String selectedSkill = skillLevelAutoCompleteTextView.getText().toString();
        if (selectedSkill.equals(skillLevels.get(0))) {
            skillLevel = UserCulinaryProfile.SKILL_BEGINNER;
        } else if (selectedSkill.equals(skillLevels.get(1))) {
            skillLevel = UserCulinaryProfile.SKILL_INTERMEDIATE;
        } else {
            skillLevel = UserCulinaryProfile.SKILL_ADVANCED;
        }
        userProfile.setSkillLevel(skillLevel);
        
        // Save dietary preferences
        Set<String> dietaryPreferences = new HashSet<>();
        if (dietNoneChip.isChecked()) {
            dietaryPreferences.add(UserCulinaryProfile.DIET_NONE);
        } else {
            if (dietVegetarianChip.isChecked()) dietaryPreferences.add(UserCulinaryProfile.DIET_VEGETARIAN);
            if (dietVeganChip.isChecked()) dietaryPreferences.add(UserCulinaryProfile.DIET_VEGAN);
            if (dietGlutenFreeChip.isChecked()) dietaryPreferences.add(UserCulinaryProfile.DIET_GLUTEN_FREE);
            if (dietLactoseFreeChip.isChecked()) dietaryPreferences.add(UserCulinaryProfile.DIET_LACTOSE_FREE);
            if (dietKetoChip.isChecked()) dietaryPreferences.add(UserCulinaryProfile.DIET_KETO);
            if (dietLowCarbChip.isChecked()) dietaryPreferences.add(UserCulinaryProfile.DIET_LOW_CARB);
            
            // Add custom dietary preferences too
            for (int i = 0; i < dietaryPreferencesChipGroup.getChildCount(); i++) {
                View view = dietaryPreferencesChipGroup.getChildAt(i);
                if (view instanceof Chip && 
                    view.getId() != R.id.dietNoneChip && 
                    view.getId() != R.id.dietVegetarianChip &&
                    view.getId() != R.id.dietVeganChip &&
                    view.getId() != R.id.dietGlutenFreeChip &&
                    view.getId() != R.id.dietLactoseFreeChip &&
                    view.getId() != R.id.dietKetoChip &&
                    view.getId() != R.id.dietLowCarbChip &&
                    view.getId() != R.id.addDietaryPreferenceChip) {
                    
                    Chip chip = (Chip) view;
                    if (chip.isChecked()) {
                        dietaryPreferences.add(chip.getText().toString());
                    }
                }
            }
        }
        userProfile.setDietaryPreferences(dietaryPreferences);
        
        // Save allergies
        Set<String> allergies = new HashSet<>();
        for (int i = 0; i < allergiesChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) allergiesChipGroup.getChildAt(i);
            allergies.add(chip.getText().toString());
        }
        userProfile.setAllergies(allergies);
        
        // Save meal times
        Set<String> mealTimes = new HashSet<>();
        if (mealBreakfastChip.isChecked()) mealTimes.add(UserCulinaryProfile.MEAL_BREAKFAST);
        if (mealLunchChip.isChecked()) mealTimes.add(UserCulinaryProfile.MEAL_LUNCH);
        if (mealDinnerChip.isChecked()) mealTimes.add(UserCulinaryProfile.MEAL_DINNER);
        if (mealSnackChip.isChecked()) mealTimes.add(UserCulinaryProfile.MEAL_SNACK);
        userProfile.setFavoriteMealTimes(mealTimes);
        
        // Save cuisine preferences
        Set<String> cuisines = new HashSet<>();
        for (int i = 0; i < cuisineChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) cuisineChipGroup.getChildAt(i);
                cuisines.add(chip.getText().toString());
        }
        userProfile.setFavoriteCuisines(cuisines);
        
        // Mark first time setup as complete
        userProfile.completeFirstTimeSetup();
        
        // Mark that user has created a profile
        CulinaryWelcomeActivity.CulinaryPreferences.setHasUserProfile(this, true);
        
        // Log the profile creation event
        logAnalyticsEvent("profile_created");
        
        // Show success message
        Snackbar.make(
            saveProfileButton, 
            "Profil culinar salvat cu succes!", 
            Snackbar.LENGTH_SHORT
        ).show();
        
        // Open modern culinary activity when profile setup is complete
        Intent intent = new Intent(this, ModernCulinaryActivity.class);
        startActivity(intent);
        
        // Finish activity
        finish();
    }
    
    private boolean validateForm() {
        boolean isValid = true;
        
        // Validate skill level
        if (TextUtils.isEmpty(skillLevelAutoCompleteTextView.getText())) {
            skillLevelAutoCompleteTextView.setError("Selectați nivelul de abilitate");
            isValid = false;
        }
        
        // At least one meal time should be selected
        if (!mealBreakfastChip.isChecked() && !mealLunchChip.isChecked() && 
            !mealDinnerChip.isChecked() && !mealSnackChip.isChecked()) {
            Snackbar.make(
                saveProfileButton, 
                "Selectați cel puțin o masă preferată", 
                Snackbar.LENGTH_SHORT
            ).show();
            isValid = false;
        }
        
        return isValid;
    }
    
    private void logAnalyticsEvent(String eventName) {
        try {
            firebaseAnalytics.logEvent(eventName, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 