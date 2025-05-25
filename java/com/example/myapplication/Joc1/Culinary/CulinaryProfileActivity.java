package com.example.myapplication.Joc1.Culinary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import com.example.myapplication.R;
import android.content.ContentResolver;
import android.util.Log;


/**
 * Activity for setting up and editing user culinary profile
 */
public class CulinaryProfileActivity extends AppCompatActivity {

    // UI Components
    private CircleImageView profileImageView;
    private TextInputLayout nameInputLayout, emailInputLayout, phoneInputLayout;
    private TextInputEditText nameEditText, emailEditText, phoneEditText, aboutMeEditText;
    private TextInputEditText yearsOfExperienceEditText;
    private AutoCompleteTextView skillLevelAutoCompleteTextView, favoriteCuisineAutoCompleteTextView;
    private ChipGroup specializariChipGroup, dietaryPreferencesChipGroup, favoriteCuisinesChipGroup;
    private MaterialButton uploadPhotoButton, saveProfileButton, exportProfileButton;
    private Chip addSpecializareChip, addDietaryPreferenceChip;

    // Data
    private Uri profileImageUri;
    private List<String> skillLevels = Arrays.asList("Începător", "Intermediar", "Avansat", "Profesionist");
    private List<String> availableCuisines = Arrays.asList(
            "Românească", "Italiană", "Franceză", "Asiatică", "Mediteraneană", "Mexicană", 
            "Indiană", "Japoneză", "Chinezească", "Grecească", "Spaniolă", "Turcească", 
            "Libaneză", "Americană", "Germană", "Ungurească"
    );
    private Set<String> selectedSpecializari = new HashSet<>();
    private Set<String> selectedDietaryPreferences = new HashSet<>();
    private Set<String> selectedCuisines = new HashSet<>();

    // Utility
    private SharedPreferences preferences;
    private FirebaseAnalytics firebaseAnalytics;
    private static final String PREFS_NAME = "CulinaryProfilePrefs";
    private static final int DRAFT_SAVE_INTERVAL = 30000; // 30 seconds
    private boolean formModified = false;

    // Activity result launcher for image picking
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    profileImageUri = uri;
                    profileImageView.setImageURI(uri);
                    formModified = true;
                    logAnalyticsEvent("profile_image_updated");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_culinary_profile);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize SharedPreferences
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize UI components
        initializeViews();
        setupListeners();
        setupAdapters();

        // Load saved profile data
        loadProfileData();

        // Set up auto-save timer
        setupAutoSaveDraft();
    }

    private void initializeViews() {
        // Profile Image
        profileImageView = findViewById(R.id.profileImageView);
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);

        // Personal Info
        nameInputLayout = findViewById(R.id.nameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);

        // Cooking Skills
        skillLevelAutoCompleteTextView = findViewById(R.id.skillLevelAutoCompleteTextView);
        yearsOfExperienceEditText = findViewById(R.id.yearsOfExperienceEditText);
        specializariChipGroup = findViewById(R.id.specializariChipGroup);
        addSpecializareChip = findViewById(R.id.addSpecializareChip);

        // Dietary Preferences
        dietaryPreferencesChipGroup = findViewById(R.id.dietaryPreferencesChipGroup);
        addDietaryPreferenceChip = findViewById(R.id.addDietaryPreferenceChip);

        // Favorite Cuisines
        favoriteCuisineAutoCompleteTextView = findViewById(R.id.favoriteCuisineAutoCompleteTextView);
        favoriteCuisinesChipGroup = findViewById(R.id.favoriteCuisinesChipGroup);

        // About Me
        aboutMeEditText = findViewById(R.id.aboutMeEditText);

        // Action Buttons
        saveProfileButton = findViewById(R.id.saveProfileButton);
        exportProfileButton = findViewById(R.id.exportProfileButton);
        
        // Improve scrolling by properly handling EditTexts focus changes
        setupImprovedScrolling();
    }

    private void setupImprovedScrolling() {
        // Get the NestedScrollView
        androidx.core.widget.NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);
        
        // Setup touch listener to hide keyboard when tapping outside of text fields
        nestedScrollView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            nestedScrollView.requestFocus();
            return false;
        });
        
        // Set consistent input modes for all EditTexts to improve scroll behavior
        nameEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
        emailEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
        phoneEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
        yearsOfExperienceEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
        aboutMeEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);
        
        // Add keyboard action listeners
        aboutMeEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                return true;
            }
            return false;
        });
    }
    
    private void hideKeyboard() {
        android.view.View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = 
                (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setupListeners() {
        // Profile Image
        uploadPhotoButton.setOnClickListener(v -> pickImage());

        // Form change tracking - for auto-save
        setupFormChangeListeners();

        // Add Specializare Chip
        addSpecializareChip.setOnClickListener(v -> showAddSpecializareDialog());

        // Add Dietary Preference Chip
        addDietaryPreferenceChip.setOnClickListener(v -> showAddDietaryPreferenceDialog());

        // Favorite Cuisine
        TextInputLayout favoriteCuisineLayout = findViewById(R.id.favoriteCuisineLayout);
        favoriteCuisineLayout.setEndIconOnClickListener(v -> addSelectedCuisine());

        // Chip click listeners for favorites
        setupChipListeners();

        // Save Button
        saveProfileButton.setOnClickListener(v -> saveProfile());

        // Export Button
        exportProfileButton.setOnClickListener(v -> exportProfile());
    }

    private void setupAdapters() {
        // Skill Level Dropdown
        ArrayAdapter<String> skillLevelAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, skillLevels);
        skillLevelAutoCompleteTextView.setAdapter(skillLevelAdapter);

        // Favorite Cuisine Dropdown
        ArrayAdapter<String> cuisineAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, availableCuisines);
        favoriteCuisineAutoCompleteTextView.setAdapter(cuisineAdapter);
    }

    private void setupFormChangeListeners() {
        // Personal Info change listeners
        nameEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) validateName();
            formModified = true;
        });
        
        emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) validateEmail();
            formModified = true;
        });
        
        phoneEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) validatePhone();
            formModified = true;
        });

        // Skill level change listener
        skillLevelAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            formModified = true;
            logAnalyticsEvent("skill_level_selected");
        });

        // Years of experience change listener
        yearsOfExperienceEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) validateYearsOfExperience();
            formModified = true;
        });

        // About me change listener
        aboutMeEditText.setOnFocusChangeListener((v, hasFocus) -> formModified = true);
    }

    private void setupChipListeners() {
        // Specializări chip listener
        for (int i = 0; i < specializariChipGroup.getChildCount(); i++) {
            View view = specializariChipGroup.getChildAt(i);
            if (view instanceof Chip && view.getId() != R.id.addSpecializareChip) {
                ((Chip) view).setOnCheckedChangeListener((chip, isChecked) -> {
                    if (isChecked) {
                        selectedSpecializari.add(chip.getText().toString());
                    } else {
                        selectedSpecializari.remove(chip.getText().toString());
                    }
                    formModified = true;
                    logAnalyticsEvent("specialization_toggled");
                });
            }
        }

        // Dietary preferences chip listener
        for (int i = 0; i < dietaryPreferencesChipGroup.getChildCount(); i++) {
            View view = dietaryPreferencesChipGroup.getChildAt(i);
            if (view instanceof Chip && view.getId() != R.id.addDietaryPreferenceChip) {
                ((Chip) view).setOnCheckedChangeListener((chip, isChecked) -> {
                    if (isChecked) {
                        selectedDietaryPreferences.add(chip.getText().toString());
                    } else {
                        selectedDietaryPreferences.remove(chip.getText().toString());
                    }
                    formModified = true;
                    logAnalyticsEvent("dietary_preference_toggled");
                });
            }
        }

        // Favorite cuisines chip listener - using close icon for removal
        for (int i = 0; i < favoriteCuisinesChipGroup.getChildCount(); i++) {
            View view = favoriteCuisinesChipGroup.getChildAt(i);
            if (view instanceof Chip) {
                Chip chip = (Chip) view;
                chip.setOnCloseIconClickListener(v -> {
                    favoriteCuisinesChipGroup.removeView(v);
                    selectedCuisines.remove(chip.getText().toString());
                    formModified = true;
                    logAnalyticsEvent("cuisine_removed");
                });
                selectedCuisines.add(chip.getText().toString());
            }
        }
    }

    private void setupAutoSaveDraft() {
        // Save draft every 30 seconds if modified
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (formModified) {
                    saveDraft();
                    formModified = false;
                }
                new android.os.Handler().postDelayed(this, DRAFT_SAVE_INTERVAL);
            }
        }, DRAFT_SAVE_INTERVAL);
    }

    private void pickImage() {
        pickImageLauncher.launch("image/*");
    }

    private void showAddSpecializareDialog() {
        final EditText input = new EditText(this);
        input.setHint("Introduceți specializarea");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Adăugare specializare nouă")
                .setView(input)
                .setPositiveButton("Adaugă", (dialog, which) -> {
                    String newSpecializare = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(newSpecializare)) {
                        addSpecializareChip(newSpecializare);
                        logAnalyticsEvent("specialization_added");
                    }
                })
                .setNegativeButton("Anulare", null)
                .show();
    }

    private void addSpecializareChip(String specializare) {
        Chip chip = new Chip(this);
        chip.setText(specializare);
        chip.setCheckable(true);
        chip.setChecked(true);
        chip.setChipBackgroundColorResource(R.color.culinary_secondary_light);
        chip.setChipStrokeColorResource(R.color.culinary_secondary);
        chip.setChipStrokeWidth(1);
        chip.setCloseIconVisible(false);
        chip.setTextColor(getResources().getColor(R.color.culinary_text));
        
        // Add listener
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedSpecializari.add(specializare);
            } else {
                selectedSpecializari.remove(specializare);
            }
            formModified = true;
        });

        // Add to selected set and group
        selectedSpecializari.add(specializare);
        specializariChipGroup.addView(chip, specializariChipGroup.getChildCount() - 1); // Add before the "Add" chip
    }

    private void showAddDietaryPreferenceDialog() {
        final EditText input = new EditText(this);
        input.setHint("Introduceți preferința alimentară");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Adăugare preferință alimentară")
                .setView(input)
                .setPositiveButton("Adaugă", (dialog, which) -> {
                    String newPreference = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(newPreference)) {
                        addDietaryPreferenceChip(newPreference);
                        logAnalyticsEvent("dietary_preference_added");
                    }
                })
                .setNegativeButton("Anulare", null)
                .show();
    }

    private void addDietaryPreferenceChip(String preference) {
            Chip chip = new Chip(this);
            chip.setText(preference);
            chip.setCheckable(true);
        chip.setChecked(true);
        chip.setChipBackgroundColorResource(R.color.culinary_difficulty_easy);
        chip.setChipStrokeColorResource(R.color.culinary_accent);
        chip.setChipStrokeWidth(1);
        chip.setCloseIconVisible(false);
        chip.setTextColor(getResources().getColor(R.color.culinary_text));
        
        // Add listener
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedDietaryPreferences.add(preference);
            } else {
                selectedDietaryPreferences.remove(preference);
            }
            formModified = true;
        });

        // Add to selected set and group
        selectedDietaryPreferences.add(preference);
        dietaryPreferencesChipGroup.addView(chip, dietaryPreferencesChipGroup.getChildCount() - 1); // Add before the "Add" chip
    }

    private void addSelectedCuisine() {
        String cuisine = favoriteCuisineAutoCompleteTextView.getText().toString().trim();
        if (TextUtils.isEmpty(cuisine)) {
            return;
        }

        // Check if already added
        if (selectedCuisines.contains(cuisine)) {
            favoriteCuisineAutoCompleteTextView.setText("");
            return;
        }

        // Create a chip for the cuisine
        Chip chip = new Chip(this);
        chip.setText(cuisine);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColorResource(R.color.culinary_primary_light);
        chip.setCloseIconTintResource(R.color.white);
        chip.setTextColor(getResources().getColor(R.color.white));
        
        // Add close listener
        chip.setOnCloseIconClickListener(v -> {
            favoriteCuisinesChipGroup.removeView(chip);
            selectedCuisines.remove(cuisine);
            formModified = true;
        });

        // Add to selected set and group
        selectedCuisines.add(cuisine);
        favoriteCuisinesChipGroup.addView(chip);
        favoriteCuisineAutoCompleteTextView.setText("");
        formModified = true;
        logAnalyticsEvent("cuisine_added");
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate name
        if (!validateName()) {
            isValid = false;
        }

        // Validate email
        if (!validateEmail()) {
            isValid = false;
        }

        // Validate phone
        if (!validatePhone()) {
            isValid = false;
        }

        // Validate years of experience
        if (!validateYearsOfExperience()) {
            isValid = false;
        }

        // Validate skill level
        String skillLevel = skillLevelAutoCompleteTextView.getText().toString();
        if (TextUtils.isEmpty(skillLevel)) {
            skillLevelAutoCompleteTextView.setError("Selectați nivelul de abilitate");
            isValid = false;
        }

        return isValid;
    }

    private boolean validateName() {
        String name = nameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            nameInputLayout.setError("Numele este obligatoriu");
            return false;
        } else {
            nameInputLayout.setError(null);
            return true;
        }
    }

    private boolean validateEmail() {
        String email = emailEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Email-ul este obligatoriu");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Email invalid");
            return false;
        } else {
            emailInputLayout.setError(null);
            return true;
        }
    }

    private boolean validatePhone() {
        String phone = phoneEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(phone) && !Patterns.PHONE.matcher(phone).matches()) {
            phoneInputLayout.setError("Număr de telefon invalid");
            return false;
        } else {
            phoneInputLayout.setError(null);
            return true;
        }
    }

    private boolean validateYearsOfExperience() {
        String yearsStr = yearsOfExperienceEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(yearsStr)) {
            try {
                int years = Integer.parseInt(yearsStr);
                if (years < 0 || years > 99) {
                    yearsOfExperienceEditText.setError("Valoare între 0 și 99");
                    return false;
                }
            } catch (NumberFormatException e) {
                yearsOfExperienceEditText.setError("Valoare numerică invalidă");
                return false;
            }
        }
        return true;
    }

    private void saveProfile() {
        if (!validateForm()) {
            Snackbar.make(saveProfileButton, "Corectați erorile din formular", Snackbar.LENGTH_LONG).show();
            return;
        }

        // Save all profile data to SharedPreferences
        SharedPreferences.Editor editor = preferences.edit();
        
        // Personal info
        editor.putString("name", nameEditText.getText().toString().trim());
        editor.putString("email", emailEditText.getText().toString().trim());
        editor.putString("phone", phoneEditText.getText().toString().trim());
        
        // Cooking skills
        editor.putString("skillLevel", skillLevelAutoCompleteTextView.getText().toString());
        editor.putString("yearsOfExperience", yearsOfExperienceEditText.getText().toString().trim());
        
        // About me
        editor.putString("aboutMe", aboutMeEditText.getText().toString().trim());
        
        // Profile image
        if (profileImageUri != null) {
            editor.putString("profileImageUri", profileImageUri.toString());
        }
        
        // Collection data (using JSON)
        try {
            // Specializări
            JSONArray specializariArray = new JSONArray();
            for (String item : selectedSpecializari) {
                specializariArray.put(item);
            }
            editor.putString("specializari", specializariArray.toString());
            
            // Dietary preferences
            JSONArray dietaryArray = new JSONArray();
            for (String item : selectedDietaryPreferences) {
                dietaryArray.put(item);
            }
            editor.putString("dietaryPreferences", dietaryArray.toString());
            
            // Favorite cuisines
            JSONArray cuisinesArray = new JSONArray();
            for (String item : selectedCuisines) {
                cuisinesArray.put(item);
            }
            editor.putString("favoriteCuisines", cuisinesArray.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        editor.apply();
        
        Snackbar.make(saveProfileButton, "Profil salvat cu succes", Snackbar.LENGTH_SHORT).show();
        formModified = false;
        logAnalyticsEvent("profile_saved");
    }

    private void saveDraft() {
        // Save as draft without validation
        SharedPreferences.Editor editor = preferences.edit();
        
        // Personal info
        editor.putString("draft_name", nameEditText.getText().toString().trim());
        editor.putString("draft_email", emailEditText.getText().toString().trim());
        editor.putString("draft_phone", phoneEditText.getText().toString().trim());
        
        // Cooking skills
        editor.putString("draft_skillLevel", skillLevelAutoCompleteTextView.getText().toString());
        editor.putString("draft_yearsOfExperience", yearsOfExperienceEditText.getText().toString().trim());
        
        // About me
        editor.putString("draft_aboutMe", aboutMeEditText.getText().toString().trim());
        
        // Profile image
        if (profileImageUri != null) {
            editor.putString("draft_profileImageUri", profileImageUri.toString());
        }
        
        // Collection data (using JSON)
        try {
            // Specializări
            JSONArray specializariArray = new JSONArray();
            for (String item : selectedSpecializari) {
                specializariArray.put(item);
            }
            editor.putString("draft_specializari", specializariArray.toString());
            
            // Dietary preferences
            JSONArray dietaryArray = new JSONArray();
            for (String item : selectedDietaryPreferences) {
                dietaryArray.put(item);
            }
            editor.putString("draft_dietaryPreferences", dietaryArray.toString());
            
            // Favorite cuisines
            JSONArray cuisinesArray = new JSONArray();
            for (String item : selectedCuisines) {
                cuisinesArray.put(item);
            }
            editor.putString("draft_favoriteCuisines", cuisinesArray.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        editor.apply();
    }

    private void loadProfileData() {
        // Try to load saved profile, if not found try draft
        String name = preferences.getString("name", preferences.getString("draft_name", ""));
        String email = preferences.getString("email", preferences.getString("draft_email", ""));
        String phone = preferences.getString("phone", preferences.getString("draft_phone", ""));
        String skillLevel = preferences.getString("skillLevel", preferences.getString("draft_skillLevel", ""));
        String yearsOfExperience = preferences.getString("yearsOfExperience", preferences.getString("draft_yearsOfExperience", ""));
        String aboutMe = preferences.getString("aboutMe", preferences.getString("draft_aboutMe", ""));
        String profileImageUriStr = preferences.getString("profileImageUri", preferences.getString("draft_profileImageUri", ""));
        
        // Set values
        nameEditText.setText(name);
        emailEditText.setText(email);
        phoneEditText.setText(phone);
        skillLevelAutoCompleteTextView.setText(skillLevel);
        yearsOfExperienceEditText.setText(yearsOfExperience);
        aboutMeEditText.setText(aboutMe);
        
        // Load profile image
        if (!TextUtils.isEmpty(profileImageUriStr)) {
            try {
                profileImageUri = Uri.parse(profileImageUriStr);
                
                // Check if we can access this URI before setting it
                if (ContentResolver.SCHEME_CONTENT.equals(profileImageUri.getScheme())) {
                    try {
                        getContentResolver().getType(profileImageUri);
                    } catch (SecurityException e) {
                        Log.e("CulinaryProfile", "Security exception with image URI: " + e.getMessage());
                        // Clear the invalid URI
                        profileImageUri = null;
                        preferences.edit().remove("profileImageUri").remove("draft_profileImageUri").apply();
                        return;
                    }
                }
                
                profileImageView.setImageURI(profileImageUri);
            } catch (Exception e) {
                Log.e("CulinaryProfile", "Error loading profile image: " + e.getMessage());
                // Clear the invalid URI
                preferences.edit().remove("profileImageUri").remove("draft_profileImageUri").apply();
            }
        }
        
        // Load collections from JSON
        loadCollectionData();
    }

    private void loadCollectionData() {
        try {
            // Load specializări
            String specializariJson = preferences.getString("specializari", preferences.getString("draft_specializari", "[]"));
            JSONArray specializariArray = new JSONArray(specializariJson);
            specializariChipGroup.removeAllViews();
            specializariChipGroup.addView(addSpecializareChip); // Keep the "Add" chip
            selectedSpecializari.clear();
            
            for (int i = 0; i < specializariArray.length(); i++) {
                String item = specializariArray.getString(i);
                addSpecializareChip(item);
            }
            
            // Load dietary preferences
            String dietaryJson = preferences.getString("dietaryPreferences", preferences.getString("draft_dietaryPreferences", "[]"));
            JSONArray dietaryArray = new JSONArray(dietaryJson);
            selectedDietaryPreferences.clear();
            
            // First reset all chips
            for (int i = 0; i < dietaryPreferencesChipGroup.getChildCount(); i++) {
                View view = dietaryPreferencesChipGroup.getChildAt(i);
                if (view instanceof Chip && view.getId() != R.id.addDietaryPreferenceChip) {
                    ((Chip) view).setChecked(false);
                }
            }
            
            // Then check the saved ones and add any custom ones
            for (int i = 0; i < dietaryArray.length(); i++) {
                String item = dietaryArray.getString(i);
                boolean found = false;
                
                // Check if this is a predefined chip
                for (int j = 0; j < dietaryPreferencesChipGroup.getChildCount(); j++) {
                    View view = dietaryPreferencesChipGroup.getChildAt(j);
                    if (view instanceof Chip && ((Chip) view).getText().toString().equals(item)) {
                        ((Chip) view).setChecked(true);
                        found = true;
                        break;
                    }
                }
                
                // If not found, add as a custom chip
                if (!found) {
                    addDietaryPreferenceChip(item);
                }
            }
            
            // Load favorite cuisines
            String cuisinesJson = preferences.getString("favoriteCuisines", preferences.getString("draft_favoriteCuisines", "[]"));
            JSONArray cuisinesArray = new JSONArray(cuisinesJson);
            favoriteCuisinesChipGroup.removeAllViews();
            selectedCuisines.clear();
            
            for (int i = 0; i < cuisinesArray.length(); i++) {
                String cuisine = cuisinesArray.getString(i);
                
                // Create a chip for the cuisine
                Chip chip = new Chip(this);
                chip.setText(cuisine);
                chip.setCloseIconVisible(true);
                chip.setChipBackgroundColorResource(R.color.culinary_primary_light);
                chip.setCloseIconTintResource(R.color.white);
                chip.setTextColor(getResources().getColor(R.color.white));
                
                // Add close listener
                chip.setOnCloseIconClickListener(v -> {
                    favoriteCuisinesChipGroup.removeView(chip);
                    selectedCuisines.remove(cuisine);
                    formModified = true;
                });
                
                // Add to selected set and group
                selectedCuisines.add(cuisine);
                favoriteCuisinesChipGroup.addView(chip);
            }
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void exportProfile() {
        try {
            // Create a JSON object with all profile data
            JSONObject profileJson = new JSONObject();
            
            // Personal info
            profileJson.put("name", nameEditText.getText().toString().trim());
            profileJson.put("email", emailEditText.getText().toString().trim());
            profileJson.put("phone", phoneEditText.getText().toString().trim());
            profileJson.put("skillLevel", skillLevelAutoCompleteTextView.getText().toString());
            profileJson.put("yearsOfExperience", yearsOfExperienceEditText.getText().toString().trim());
            profileJson.put("aboutMe", aboutMeEditText.getText().toString().trim());
            
            // Collections
            JSONArray specializariArray = new JSONArray();
            for (String item : selectedSpecializari) {
                specializariArray.put(item);
            }
            profileJson.put("specializari", specializariArray);
            
            JSONArray dietaryArray = new JSONArray();
            for (String item : selectedDietaryPreferences) {
                dietaryArray.put(item);
            }
            profileJson.put("dietaryPreferences", dietaryArray);
            
            JSONArray cuisinesArray = new JSONArray();
            for (String item : selectedCuisines) {
                cuisinesArray.put(item);
            }
            profileJson.put("favoriteCuisines", cuisinesArray);
            
            // Create an intent to share the JSON
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Profilul meu culinar");
            shareIntent.putExtra(Intent.EXTRA_TEXT, prettyFormatJson(profileJson));
            
            startActivity(Intent.createChooser(shareIntent, "Partajează profilul"));
            logAnalyticsEvent("profile_exported");
            
        } catch (JSONException e) {
            e.printStackTrace();
            Snackbar.make(exportProfileButton, "Eroare la exportarea profilului", Snackbar.LENGTH_SHORT).show();
        }
    }

    private String prettyFormatJson(JSONObject jsonObject) throws JSONException {
        // For user-friendly sharing, create a readable format
        StringBuilder sb = new StringBuilder();
        sb.append("PROFIL CULINAR\n\n");
        
        // Personal info
        sb.append("Nume: ").append(jsonObject.optString("name")).append("\n");
        sb.append("Email: ").append(jsonObject.optString("email")).append("\n");
        sb.append("Telefon: ").append(jsonObject.optString("phone")).append("\n\n");
        
        // Cooking skills
        sb.append("Nivel de abilitate: ").append(jsonObject.optString("skillLevel")).append("\n");
        sb.append("Ani de experiență: ").append(jsonObject.optString("yearsOfExperience")).append("\n\n");
        
        // Specializări
        sb.append("SPECIALIZĂRI:\n");
        JSONArray specializariArray = jsonObject.getJSONArray("specializari");
        for (int i = 0; i < specializariArray.length(); i++) {
            sb.append("• ").append(specializariArray.getString(i)).append("\n");
        }
        sb.append("\n");
        
        // Dietary preferences
        sb.append("PREFERINȚE ALIMENTARE:\n");
        JSONArray dietaryArray = jsonObject.getJSONArray("dietaryPreferences");
        for (int i = 0; i < dietaryArray.length(); i++) {
            sb.append("• ").append(dietaryArray.getString(i)).append("\n");
        }
        sb.append("\n");
        
        // Favorite cuisines
        sb.append("BUCĂTĂRII PREFERATE:\n");
        JSONArray cuisinesArray = jsonObject.getJSONArray("favoriteCuisines");
        for (int i = 0; i < cuisinesArray.length(); i++) {
            sb.append("• ").append(cuisinesArray.getString(i)).append("\n");
        }
        sb.append("\n");
        
        // About me
        sb.append("DESPRE MINE:\n").append(jsonObject.optString("aboutMe"));
        
        return sb.toString();
    }

    private void logAnalyticsEvent(String eventName) {
        try {
            Bundle bundle = new Bundle();
            firebaseAnalytics.logEvent(eventName, bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_culinary_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // Handle back button in toolbar
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (formModified) {
            new AlertDialog.Builder(this)
                    .setTitle("Salvare modificări")
                    .setMessage("Doriți să salvați modificările înainte de a ieși?")
                    .setPositiveButton("Salvează", (dialog, which) -> {
                        saveProfile();
                        super.onBackPressed();
                    })
                    .setNegativeButton("Renunță", (dialog, which) -> super.onBackPressed())
                    .setNeutralButton("Anulare", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (formModified) {
            saveDraft();
        }
    }
} 