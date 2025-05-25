package com.example.myapplication.Joc1.Culinary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity.Recipe;

/**
 * Activity for adding a new recipe
 */
public class AddRecipeActivity extends AppCompatActivity {
    
    // Shared Preferences
    private static final String PREFS_NAME = "RecipeDraft";
    
    // UI Components
    private TextInputLayout titleInput;
    private TextInputLayout descriptionInput;
    private TextInputLayout ingredientsInput;
    private TextInputLayout stepsInput;
    private TextInputLayout prepTimeInput;
    
    // Dropdown components
    private TextInputLayout regionDropdown;
    private TextInputLayout categoryDropdown;
    private TextInputLayout difficultyDropdown;
    private AutoCompleteTextView regionAutoCompleteTextView;
    private AutoCompleteTextView categoryAutoCompleteTextView;
    private AutoCompleteTextView difficultyAutoCompleteTextView;
    
    private ImageView recipeImageView;
    private MaterialButton addImageButton;
    private MaterialButton saveButton;

    // Data
    private Uri selectedImageUri;
    private RecipeDBHelper dbHelper;
    private String selectedRegion;
    private String selectedCategory;
    private String selectedDifficulty;
    
    // Firebase
    private FirebaseDatabase database;
    private DatabaseReference recipesRef;
    
    // Image picker launcher
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        // Inițializare Firebase
        initFirebase();
        
        // Initialize database helper
        dbHelper = new RecipeDBHelper(this);

        // Initialize UI components
        initViews();
        setupToolbar();
        setupDropdowns();
        setupListeners();
        setupImagePicker();
        
        // Load draft if available
        loadDraft();
    }
    
    private void initFirebase() {
        // Inițializează Firebase
        database = FirebaseDatabase.getInstance();
        recipesRef = database.getReference("recipes");
    }

    private void initViews() {
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        ingredientsInput = findViewById(R.id.ingredientsInput);
        stepsInput = findViewById(R.id.stepsInput);
        prepTimeInput = findViewById(R.id.prepTimeInput);
        
        // Initialize dropdowns
        regionDropdown = findViewById(R.id.regionDropdown);
        categoryDropdown = findViewById(R.id.categoryDropdown);
        difficultyDropdown = findViewById(R.id.difficultyDropdown);
        regionAutoCompleteTextView = findViewById(R.id.regionAutoCompleteTextView);
        categoryAutoCompleteTextView = findViewById(R.id.categoryAutoCompleteTextView);
        difficultyAutoCompleteTextView = findViewById(R.id.difficultyAutoCompleteTextView);
        
        recipeImageView = findViewById(R.id.recipeImageView);
        addImageButton = findViewById(R.id.addImageButton);
        saveButton = findViewById(R.id.saveButton);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.add_recipe);
        }
    }

    private void setupDropdowns() {
        // Setup region dropdown
        ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, CulinaryUtils.REGIONS);
        regionAutoCompleteTextView.setAdapter(regionAdapter);
        regionAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            selectedRegion = CulinaryUtils.REGIONS[position];
        });

        // Setup category dropdown
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, CulinaryUtils.CATEGORIES);
        categoryAutoCompleteTextView.setAdapter(categoryAdapter);
        categoryAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = CulinaryUtils.CATEGORIES[position];
        });

        // Setup difficulty dropdown
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, CulinaryUtils.DIFFICULTY_LEVELS);
        difficultyAutoCompleteTextView.setAdapter(difficultyAdapter);
        difficultyAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            selectedDifficulty = CulinaryUtils.DIFFICULTY_LEVELS[position];
        });
    }

    private void setupListeners() {
        // Setup image picker button
        addImageButton.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        // Setup save button
        saveButton.setOnClickListener(v -> {
            saveDraft(); // Save draft before validating and submitting
            if (validateInputs()) {
                saveRecipe();
            }
        });
        
        // Setup text change listeners for auto-saving draft
        setupTextChangeListeners();
    }
    
    private void setupImagePicker() {
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
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
                            Log.w("AddRecipeActivity", "Couldn't take persistable permission for URI: " + e.getMessage());
                        }
                    }
                    
                    selectedImageUri = uri;
                    recipeImageView.setImageURI(selectedImageUri);
                    recipeImageView.setVisibility(View.VISIBLE);
                    addImageButton.setText("Schimbă Imaginea");
                } catch (Exception e) {
                    Log.e("AddRecipeActivity", "Error setting image URI: " + e.getMessage());
                    Toast.makeText(AddRecipeActivity.this, "Eroare la încărcarea imaginii", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void setupTextChangeListeners() {
        // Salvează automat schița când se schimbă textul
        titleInput.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveDraft();
        });
        
        descriptionInput.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveDraft();
        });
        
        ingredientsInput.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveDraft();
        });
        
        stepsInput.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveDraft();
        });
        
        prepTimeInput.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveDraft();
        });
    }

    private void saveRecipe() {
        // Get values from inputs
        String title = titleInput.getEditText().getText().toString().trim();
        String description = descriptionInput.getEditText().getText().toString().trim();
        String ingredients = ingredientsInput.getEditText().getText().toString().trim();
        String steps = stepsInput.getEditText().getText().toString().trim();
        String prepTime = prepTimeInput.getEditText().getText().toString().trim();

        // Split ingredients and steps into arrays
        String[] ingredientsArray = ingredients.split("\n");
        String[] stepsArray = steps.split("\n");

        // Create recipe object
        Recipe recipe = new Recipe(
                title,
                selectedRegion,
                selectedCategory,
                description,
                selectedDifficulty,
                prepTime,
                ingredientsArray,
                stepsArray
        );

        // Încearcă să salvezi în Firebase
        saveRecipeToFirebase(recipe);
        
        // Salvează și în baza de date locală ca backup
        long recipeId = dbHelper.addRecipe(recipe);
        
        if (recipeId > 0) {
            // Succes salvare locală
            // Notă: Nu afișăm mesaj aici deoarece vom afișa din Firebase callback
        } else {
            // Eroare salvare locală - afișează doar dacă și Firebase eșuează
            Snackbar.make(saveButton, R.string.recipe_save_error, Snackbar.LENGTH_SHORT).show();
        }
    }
    
    private void saveRecipeToFirebase(Recipe recipe) {
        // Generează un ID unic
        String recipeId = recipesRef.push().getKey();
        
        // Setează ID-ul ca string în Firebase
        // Asigură-te că este convertit la long acolo unde e necesar
        if (recipeId != null) {
            try {
                // Încercăm să folosim un ID numeric pentru compatibilitate cu codul existent
                long numericId = System.currentTimeMillis(); // Folosim timestamp ca ID numeric
                recipe.setId(numericId);
                
                // Salvează rețeta
                recipesRef.child(recipeId).setValue(recipe)
                        .addOnSuccessListener(aVoid -> {
                            // Succes
                            // Clear draft
                            clearDraft();
                            
                            // Show success message
                            Snackbar.make(saveButton, R.string.recipe_saved, Snackbar.LENGTH_SHORT).show();
                            
                            // Offer to start timer if prep time is specified
                            if (!recipe.getPrepTime().isEmpty()) {
                                try {
                                    int minutes = Integer.parseInt(recipe.getPrepTime());
                                    offerToStartTimer(minutes);
                                } catch (NumberFormatException e) {
                                    // Ignore parsing errors
                                }
                            }
                            
                            // Offer to share recipe
                            offerToShareRecipe(recipe);
                            
                            // Finish activity after short delay
                            new CountDownTimer(1500, 1500) {
                                @Override
                                public void onTick(long millisUntilFinished) {}
                                
                                @Override
                                public void onFinish() {
                                    finish();
                                }
                            }.start();
                        })
                        .addOnFailureListener(e -> {
                            // Eroare
                            Snackbar.make(saveButton, R.string.recipe_save_error, Snackbar.LENGTH_SHORT).show();
                        });
            } catch (Exception e) {
                Snackbar.make(saveButton, R.string.recipe_save_error, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(saveButton, R.string.recipe_save_error, Snackbar.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate title
        if (titleInput.getEditText().getText().toString().trim().isEmpty()) {
            titleInput.setError(getString(R.string.field_required));
            isValid = false;
        } else {
            titleInput.setError(null);
        }

        // Validate description
        if (descriptionInput.getEditText().getText().toString().trim().isEmpty()) {
            descriptionInput.setError(getString(R.string.field_required));
            isValid = false;
        } else {
            descriptionInput.setError(null);
        }

        // Validate ingredients
        if (ingredientsInput.getEditText().getText().toString().trim().isEmpty()) {
            ingredientsInput.setError(getString(R.string.field_required));
            isValid = false;
        } else {
            ingredientsInput.setError(null);
        }

        // Validate steps
        if (stepsInput.getEditText().getText().toString().trim().isEmpty()) {
            stepsInput.setError(getString(R.string.field_required));
            isValid = false;
        } else {
            stepsInput.setError(null);
        }

        // Validate prep time
        if (prepTimeInput.getEditText().getText().toString().trim().isEmpty()) {
            prepTimeInput.setError(getString(R.string.field_required));
            isValid = false;
        } else {
            prepTimeInput.setError(null);
        }

        // Validate region
        if (selectedRegion == null || selectedRegion.isEmpty()) {
            regionDropdown.setError(getString(R.string.field_required));
            isValid = false;
        } else {
            regionDropdown.setError(null);
        }

        // Validate category
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            categoryDropdown.setError(getString(R.string.field_required));
            isValid = false;
        } else {
            categoryDropdown.setError(null);
        }

        // Validate difficulty
        if (selectedDifficulty == null || selectedDifficulty.isEmpty()) {
            difficultyDropdown.setError(getString(R.string.field_required));
            isValid = false;
        } else {
            difficultyDropdown.setError(null);
        }

        return isValid;
    }
    
    private void saveDraft() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Save text fields
        if (titleInput.getEditText() != null) {
            editor.putString("title", titleInput.getEditText().getText().toString());
        }
        
        if (descriptionInput.getEditText() != null) {
            editor.putString("description", descriptionInput.getEditText().getText().toString());
        }
        
        if (ingredientsInput.getEditText() != null) {
            editor.putString("ingredients", ingredientsInput.getEditText().getText().toString());
        }
        
        if (stepsInput.getEditText() != null) {
            editor.putString("steps", stepsInput.getEditText().getText().toString());
        }
        
        if (prepTimeInput.getEditText() != null) {
            editor.putString("prepTime", prepTimeInput.getEditText().getText().toString());
        }
        
        // Save dropdown selections
        editor.putString("region", selectedRegion);
        editor.putString("category", selectedCategory);
        editor.putString("difficulty", selectedDifficulty);
        
        // Save image path if available
        if (selectedImageUri != null) {
            editor.putString("imageUri", selectedImageUri.toString());
        }
        
        editor.apply();
    }
    
    private void loadDraft() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Load text fields
        if (titleInput.getEditText() != null) {
            titleInput.getEditText().setText(prefs.getString("title", ""));
        }
        
        if (descriptionInput.getEditText() != null) {
            descriptionInput.getEditText().setText(prefs.getString("description", ""));
        }
        
        if (ingredientsInput.getEditText() != null) {
            ingredientsInput.getEditText().setText(prefs.getString("ingredients", ""));
        }
        
        if (stepsInput.getEditText() != null) {
            stepsInput.getEditText().setText(prefs.getString("steps", ""));
        }
        
        if (prepTimeInput.getEditText() != null) {
            prepTimeInput.getEditText().setText(prefs.getString("prepTime", ""));
        }
        
        // Load dropdown selections
        selectedRegion = prefs.getString("region", null);
        selectedCategory = prefs.getString("category", null);
        selectedDifficulty = prefs.getString("difficulty", null);
        
        if (selectedRegion != null) {
            regionAutoCompleteTextView.setText(selectedRegion);
        }
        
        if (selectedCategory != null) {
            categoryAutoCompleteTextView.setText(selectedCategory);
        }
        
        if (selectedDifficulty != null) {
            difficultyAutoCompleteTextView.setText(selectedDifficulty);
        }
        
        // Load image if available
        String imageUriString = prefs.getString("imageUri", null);
        if (imageUriString != null) {
            try {
                selectedImageUri = Uri.parse(imageUriString);
                recipeImageView.setImageURI(selectedImageUri);
                recipeImageView.setVisibility(View.VISIBLE);
                addImageButton.setText("Schimbă Imaginea");
            } catch (SecurityException | IllegalArgumentException e) {
                // Can't access the URI anymore, clear it
                selectedImageUri = null;
                // Remove invalid URI from SharedPreferences
                prefs.edit().remove("imageUri").apply();
                Log.e("AddRecipeActivity", "Error loading saved image URI: " + e.getMessage());
            }
        }
    }
    
    private void clearDraft() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
    
    private void offerToStartTimer(int minutes) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.start_timer_title)
            .setMessage(getString(R.string.start_timer_message, minutes))
            .setPositiveButton(R.string.start_timer, (dialog, which) -> {
                startTimer(minutes);
            })
            .setNegativeButton(R.string.no_thanks, null)
            .show();
    }
    
    private void startTimer(int minutes) {
        long millis = minutes * 60 * 1000;
        
        // Create countdown notification
        // Note: In a real app, you would create a foreground service for the timer
        
        new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update notification
            }
            
            @Override
            public void onFinish() {
                // Show completion notification
                Toast.makeText(AddRecipeActivity.this, 
                        R.string.timer_finished, Toast.LENGTH_LONG).show();
            }
        }.start();
    }
    
    private void offerToShareRecipe(Recipe recipe) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.share_recipe_title)
            .setMessage(R.string.share_recipe_message)
            .setPositiveButton(R.string.share, (dialog, which) -> {
                shareRecipe(recipe);
            })
            .setNegativeButton(R.string.no_thanks, null)
            .show();
    }
    
    private void shareRecipe(Recipe recipe) {
        // Create formatted recipe text
        StringBuilder sb = new StringBuilder();
        sb.append(recipe.getTitle()).append("\n\n");
        sb.append("Category: ").append(recipe.getCategory()).append("\n");
        sb.append("Region: ").append(recipe.getRegion()).append("\n");
        sb.append("Difficulty: ").append(recipe.getDifficulty()).append("\n");
        sb.append("Prep Time: ").append(recipe.getPrepTime()).append(" minutes\n\n");
        
        sb.append("Description:\n").append(recipe.getDescription()).append("\n\n");
        
        sb.append("Ingredients:\n");
        for (String ingredient : recipe.getIngredients()) {
            sb.append("• ").append(ingredient).append("\n");
        }
        sb.append("\n");
        
        sb.append("Steps:\n");
        for (int i = 0; i < recipe.getSteps().length; i++) {
            sb.append(i + 1).append(". ").append(recipe.getSteps()[i]).append("\n");
        }
        
        // Create and start share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, recipe.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_recipe_via)));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            saveDraft(); // Save draft before closing
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveDraft(); // Save draft when the activity is paused
    }
}
