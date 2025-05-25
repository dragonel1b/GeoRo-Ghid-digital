package com.example.myapplication.Joc1.Culinary;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity;
import com.example.myapplication.Joc1.RomGameState;
import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.example.myapplication.Joc1.Culinary.RecipeDBHelper;

/**
 * Activity for users to share their own recipes with photos
 */
public class ShareMyRecipeActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int PERMISSION_REQUEST_CAMERA = 3;
    private static final int PERMISSION_REQUEST_STORAGE = 4;
    
    private EditText recipeTitle;
    private Spinner regionSpinner;
    private Spinner categorySpinner;
    private EditText recipeDescription;
    private Spinner difficultySpinner;
    private EditText prepTimeEditText;
    private EditText ingredientsEditText;
    private EditText stepsEditText;
    private EditText culturalNotesEditText;
    private ImageView recipeImageView;
    private MaterialButton addPhotoButton;
    private MaterialButton takePhotoButton;
    private MaterialButton submitButton;
    private ChipGroup tagChipGroup;
    
    private RecipeDBHelper dbHelper;
    private String currentPhotoPath;
    private Uri photoUri;
    private RomGameState gameState;
    
    /**
     * Static method to start this activity
     * 
     * @param context The context to start the activity from
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, ShareMyRecipeActivity.class);
        context.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_my_recipe);
        
        dbHelper = new RecipeDBHelper(this);
        
        // Initialize game state
        gameState = RomGameState.getInstance();
        gameState.initialize(this);
        
        initializeViews();
        setupToolbar();
        setupSpinners();
        setupButtons();
        setupTagChips();
    }
    
    private void initializeViews() {
        recipeTitle = findViewById(R.id.recipeTitle);
        regionSpinner = findViewById(R.id.regionSpinner);
        categorySpinner = findViewById(R.id.categorySpinner);
        recipeDescription = findViewById(R.id.recipeDescription);
        difficultySpinner = findViewById(R.id.difficultySpinner);
        prepTimeEditText = findViewById(R.id.prepTimeEditText);
        ingredientsEditText = findViewById(R.id.ingredientsEditText);
        stepsEditText = findViewById(R.id.stepsEditText);
        culturalNotesEditText = findViewById(R.id.culturalNotesEditText);
        recipeImageView = findViewById(R.id.recipeImageView);
        addPhotoButton = findViewById(R.id.addPhotoButton);
        takePhotoButton = findViewById(R.id.takePhotoButton);
        submitButton = findViewById(R.id.submitButton);
        tagChipGroup = findViewById(R.id.tagChipGroup);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Împărtășește Rețeta Ta");
        }
    }
    
    private void setupSpinners() {
        // Setup region spinner
        String[] regions = {"Transilvania", "Moldova", "Muntenia", "Oltenia", "Dobrogea", "Banat", "Maramureș", "Bucovina"};
        ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, regions);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(regionAdapter);
        
        // Setup category spinner
        String[] categories = {"Aperitive", "Supe și ciorbe", "Feluri principale", 
                "Deserturi", "Pâine și produse de patiserie", "Sosuri și garnituri"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        
        // Setup difficulty spinner
        String[] difficulties = {"Ușor", "Mediu", "Dificil"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, difficulties);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);
    }
    
    private void setupButtons() {
        addPhotoButton.setOnClickListener(v -> {
            if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) {
                openGallery();
            }
        });
        
        takePhotoButton.setOnClickListener(v -> {
            if (checkPermission(Manifest.permission.CAMERA, PERMISSION_REQUEST_CAMERA)) {
                takePhoto();
            }
        });
        
        submitButton.setOnClickListener(v -> submitRecipe());
    }
    
    private void setupTagChips() {
        String[] commonTags = {
                "Tradițional", "Vegetarian", "Vegan", "Fără gluten", 
                "Post", "Rapid", "Sărbători", "Familie"
        };
        
        for (String tag : commonTags) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            tagChipGroup.addView(chip);
        }
    }
    
    private boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }
    
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selectează o fotografie"), REQUEST_IMAGE_PICK);
    }
    
    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Eroare la crearea fișierului foto", Toast.LENGTH_SHORT).show();
            }
            
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        "com.example.myapplication.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
                photoUri = data.getData();
                recipeImageView.setImageURI(photoUri);
                recipeImageView.setVisibility(View.VISIBLE);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (photoUri != null) {
                    recipeImageView.setImageURI(photoUri);
                    recipeImageView.setVisibility(View.VISIBLE);
                } else if (currentPhotoPath != null) {
                    File file = new File(currentPhotoPath);
                    photoUri = Uri.fromFile(file);
                    recipeImageView.setImageURI(photoUri);
                    recipeImageView.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PERMISSION_REQUEST_CAMERA) {
                takePhoto();
            } else if (requestCode == PERMISSION_REQUEST_STORAGE) {
                openGallery();
            }
        } else {
            Toast.makeText(this, "Permisiune necesară pentru această funcționalitate", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void submitRecipe() {
        // Validate input fields
        if (!validateInputs()) {
            return;
        }
        
        // Get values from fields
        String title = recipeTitle.getText().toString().trim();
        String region = regionSpinner.getSelectedItem().toString();
        String category = categorySpinner.getSelectedItem().toString();
        String description = recipeDescription.getText().toString().trim();
        String difficulty = difficultySpinner.getSelectedItem().toString();
        String time = prepTimeEditText.getText().toString().trim() + " min";
        
        // Parse ingredients (one per line)
        String[] ingredients = ingredientsEditText.getText().toString().trim().split("\n");
        
        // Parse steps (one per line)
        String[] steps = stepsEditText.getText().toString().trim().split("\n");
        
        // Parse cultural notes (if any)
        String culturalNotesText = culturalNotesEditText.getText().toString().trim();
        String[] culturalNotes = culturalNotesText.isEmpty() ? new String[0] : new String[]{culturalNotesText};
        
        // Get selected tags
        List<String> selectedTags = new ArrayList<>();
        for (int i = 0; i < tagChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) tagChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedTags.add(chip.getText().toString());
            }
        }
        
        // Create Recipe object
        ModernCulinaryActivity.Recipe recipe = new ModernCulinaryActivity.Recipe(
                title, region, category, description, difficulty, time,
                ingredients, steps);
        recipe.setImageResourceId(R.drawable.placeholder_food);
        
        // Mark as user-submitted and set discovered status
        recipe.setUserSubmitted(true);
        recipe.setDiscovered(true);
        
        // Get user ID and name (use device ID if not available)
        String userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("user_id", UUID.randomUUID().toString());
        String userName = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("user_name", "Utilizator");
        
        // Save user ID if it was generated
        if (!getSharedPreferences("user_prefs", MODE_PRIVATE).contains("user_id")) {
            SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
            editor.putString("user_id", userId);
            editor.apply();
        }
        
        recipe.setSubmittedBy(userName);
        
        // Process photo (copy to app's storage for persistence)
        String photoPath = null;
        if (photoUri != null) {
            try {
                // Create a directory for user recipes if it doesn't exist
                File recipesDir = new File(getFilesDir(), "user_recipes");
                if (!recipesDir.exists()) {
                    recipesDir.mkdirs();
                }
                
                // Create a unique filename for the photo
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String filename = "RECIPE_" + timeStamp + ".jpg";
                File destFile = new File(recipesDir, filename);
                
                // Copy the image to our app's files directory
                java.io.InputStream input = getContentResolver().openInputStream(photoUri);
                java.io.FileOutputStream output = new java.io.FileOutputStream(destFile);
                
                byte[] buffer = new byte[4 * 1024]; // 4KB buffer
                int read;
                
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                
                output.flush();
                output.close();
                input.close();
                
                photoPath = destFile.getAbsolutePath();
                recipe.setUserPhotoPath(photoPath);
            } catch (Exception e) {
                Toast.makeText(this, "Eroare la salvarea fotografiei: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        }
        
        // Save to database
        long recipeId = dbHelper.addUserRecipe(recipe, userId, userName, photoPath);
        
        if (recipeId != -1) {
            // Track the recipe sharing for achievement progress
            gameState.shareRecipe(this);
            
            Toast.makeText(this, "Rețeta ta a fost împărtășită cu succes!", Toast.LENGTH_LONG).show();
            
            // Return to recipe list
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "A apărut o eroare la salvarea rețetei. Te rugăm să încerci din nou.", 
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean validateInputs() {
        boolean valid = true;
        
        // Validate title
        if (TextUtils.isEmpty(recipeTitle.getText())) {
            ((TextInputLayout) findViewById(R.id.titleInputLayout)).setError("Titlul este obligatoriu");
            valid = false;
        } else {
            ((TextInputLayout) findViewById(R.id.titleInputLayout)).setError(null);
        }
        
        // Validate description
        if (TextUtils.isEmpty(recipeDescription.getText())) {
            ((TextInputLayout) findViewById(R.id.descriptionInputLayout)).setError("Descrierea este obligatorie");
            valid = false;
        } else {
            ((TextInputLayout) findViewById(R.id.descriptionInputLayout)).setError(null);
        }
        
        // Validate prep time
        if (TextUtils.isEmpty(prepTimeEditText.getText())) {
            ((TextInputLayout) findViewById(R.id.prepTimeInputLayout)).setError("Timpul de preparare este obligatoriu");
            valid = false;
        } else {
            try {
                Integer.parseInt(prepTimeEditText.getText().toString().trim());
                ((TextInputLayout) findViewById(R.id.prepTimeInputLayout)).setError(null);
            } catch (NumberFormatException e) {
                ((TextInputLayout) findViewById(R.id.prepTimeInputLayout)).setError("Introduceți un număr valid");
                valid = false;
            }
        }
        
        // Validate ingredients
        if (TextUtils.isEmpty(ingredientsEditText.getText())) {
            ((TextInputLayout) findViewById(R.id.ingredientsInputLayout)).setError("Ingredientele sunt obligatorii");
            valid = false;
        } else {
            ((TextInputLayout) findViewById(R.id.ingredientsInputLayout)).setError(null);
        }
        
        // Validate steps
        if (TextUtils.isEmpty(stepsEditText.getText())) {
            ((TextInputLayout) findViewById(R.id.stepsInputLayout)).setError("Pașii de preparare sunt obligatorii");
            valid = false;
        } else {
            ((TextInputLayout) findViewById(R.id.stepsInputLayout)).setError(null);
        }
        
        return valid;
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