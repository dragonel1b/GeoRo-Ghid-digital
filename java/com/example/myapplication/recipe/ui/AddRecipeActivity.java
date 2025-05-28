package com.example.myapplication.recipe.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.EditableIngredientAdapter;
import com.example.myapplication.adapter.EditableStepAdapter;
import com.example.myapplication.model.UserProfile;
import com.example.myapplication.recipe.model.Ingredient;
import com.example.myapplication.recipe.model.Recipe;
import com.example.myapplication.recipe.repository.RecipeRepository;
import com.example.myapplication.utils.BitmapUtils;
import com.example.myapplication.utils.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class AddRecipeActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 1001;
    
    // UI components
    private TextInputEditText titleEditText;
    private TextInputEditText descriptionEditText;
    private TextInputEditText prepTimeEditText;
    private TextInputEditText cookTimeEditText;
    private TextInputEditText servingsEditText;
    private AutoCompleteTextView difficultyDropdown;
    private AutoCompleteTextView categoryDropdown;
    private AutoCompleteTextView regionDropdown;
    private CheckBox vegetarianCheckbox;
    private CheckBox veganCheckbox;
    private CheckBox glutenFreeCheckbox;
    private CheckBox lactoseFreeCheckbox;
    private RecyclerView ingredientsRecyclerView;
    private RecyclerView stepsRecyclerView;
    private MaterialButton addIngredientButton;
    private MaterialButton addStepButton;
    private MaterialButton addImageButton;
    private MaterialButton saveRecipeButton;
    private ImageView imagePreview;
    private TextView imagePlaceholderText;
    private FrameLayout imageContainer;
    
    // Adapters
    private EditableIngredientAdapter ingredientAdapter;
    private EditableStepAdapter stepAdapter;
    
    // Data
    private RecipeRepository recipeRepository;
    private Uri selectedImageUri;
    private List<Ingredient> ingredients = new ArrayList<>();
    private List<String> steps = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);
        
        // Initialize repository
        recipeRepository = RecipeRepository.getInstance();
        
        // Initialize UI components
        initializeViews();
        
        // Setup toolbar
        setupToolbar();
        
        // Setup dropdowns
        setupDropdowns();
        
        // Setup ingredient recycler view
        setupIngredientsRecyclerView();
        
        // Setup steps recycler view
        setupStepsRecyclerView();
        
        // Setup buttons
        setupButtons();
    }
    
    private void initializeViews() {
        titleEditText = findViewById(R.id.edit_recipe_title);
        descriptionEditText = findViewById(R.id.edit_recipe_description);
        prepTimeEditText = findViewById(R.id.edit_prep_time);
        cookTimeEditText = findViewById(R.id.edit_cook_time);
        servingsEditText = findViewById(R.id.edit_servings);
        difficultyDropdown = findViewById(R.id.dropdown_difficulty);
        categoryDropdown = findViewById(R.id.dropdown_category);
        regionDropdown = findViewById(R.id.dropdown_region);
        vegetarianCheckbox = findViewById(R.id.checkbox_vegetarian);
        veganCheckbox = findViewById(R.id.checkbox_vegan);
        glutenFreeCheckbox = findViewById(R.id.checkbox_gluten_free);
        lactoseFreeCheckbox = findViewById(R.id.checkbox_lactose_free);
        ingredientsRecyclerView = findViewById(R.id.recycler_ingredients);
        stepsRecyclerView = findViewById(R.id.recycler_steps);
        addIngredientButton = findViewById(R.id.button_add_ingredient);
        addStepButton = findViewById(R.id.button_add_step);
        addImageButton = findViewById(R.id.button_add_image);
        imagePreview = findViewById(R.id.image_preview);
        saveRecipeButton = findViewById(R.id.button_save_recipe);
        imagePlaceholderText = findViewById(R.id.image_placeholder_text);
        imageContainer = findViewById(R.id.image_container);

        // Setăm input layouts pentru validare
        configureInputLayouts();
    }

    private void configureInputLayouts() {
        TextInputLayout titleInputLayout = findViewById(R.id.title_input_layout);
        TextInputLayout descriptionInputLayout = findViewById(R.id.description_input_layout);
        TextInputLayout prepTimeInputLayout = findViewById(R.id.prep_time_input_layout);
        TextInputLayout cookTimeInputLayout = findViewById(R.id.cook_time_input_layout);
        TextInputLayout servingsInputLayout = findViewById(R.id.servings_input_layout);

        // Configurăm animații și stiluri pentru TextInputLayout-uri
        titleInputLayout.setBoxStrokeColorStateList(getResources().getColorStateList(R.color.text_input_box_stroke));
        descriptionInputLayout.setBoxStrokeColorStateList(getResources().getColorStateList(R.color.text_input_box_stroke));
        prepTimeInputLayout.setBoxStrokeColorStateList(getResources().getColorStateList(R.color.text_input_box_stroke));
        cookTimeInputLayout.setBoxStrokeColorStateList(getResources().getColorStateList(R.color.text_input_box_stroke));
        servingsInputLayout.setBoxStrokeColorStateList(getResources().getColorStateList(R.color.text_input_box_stroke));
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupDropdowns() {
        // Difficulty dropdown
        String[] difficulties = getResources().getStringArray(R.array.recipe_difficulties);
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, difficulties);
        difficultyDropdown.setAdapter(difficultyAdapter);
        
        // Category dropdown
        String[] categories = getResources().getStringArray(R.array.recipe_categories);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, categories);
        categoryDropdown.setAdapter(categoryAdapter);
        
        // Region dropdown
        String[] regions = getResources().getStringArray(R.array.romanian_regions);
        ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, regions);
        regionDropdown.setAdapter(regionAdapter);
    }
    
    private void setupIngredientsRecyclerView() {
        ingredientAdapter = new EditableIngredientAdapter(this, ingredients);
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ingredientsRecyclerView.setAdapter(ingredientAdapter);
        
        // Adăugăm ItemDecoration pentru spațiere uniformă
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        ingredientsRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view, 
                                      @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = spacingInPixels;
            }
        });
    }
    
    private void setupStepsRecyclerView() {
        stepAdapter = new EditableStepAdapter(this, steps);
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stepsRecyclerView.setAdapter(stepAdapter);
        
        // Adăugăm ItemDecoration pentru spațiere uniformă
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        stepsRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view, 
                                      @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = spacingInPixels;
            }
        });
    }
    
    private void setupButtons() {
        // Add ingredient button
        addIngredientButton.setOnClickListener(v -> showAddIngredientDialog());
        
        // Add step button
        addStepButton.setOnClickListener(v -> showAddStepDialog());
        
        // Add image button and container
        addImageButton.setOnClickListener(v -> pickImage());
        imageContainer.setOnClickListener(v -> pickImage());
        
        // Save recipe button
        saveRecipeButton.setOnClickListener(v -> saveRecipe());
    }
    
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }
    
    private void showAddIngredientDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_ingredient, null);
        EditText nameEditText = dialogView.findViewById(R.id.edit_ingredient_name);
        EditText quantityEditText = dialogView.findViewById(R.id.edit_ingredient_quantity);
        EditText unitEditText = dialogView.findViewById(R.id.edit_ingredient_unit);
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Adăugare ingredient")
                .setView(dialogView)
                .setPositiveButton("Adaugă", (dialog, which) -> {
                    String name = nameEditText.getText().toString().trim();
                    String quantityStr = quantityEditText.getText().toString().trim();
                    String unit = unitEditText.getText().toString().trim();
                    
                    if (name.isEmpty() || quantityStr.isEmpty()) {
                        Toast.makeText(this, "Numele și cantitatea sunt obligatorii", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    double quantity;
                    try {
                        quantity = Double.parseDouble(quantityStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Cantitate invalidă", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    Ingredient ingredient = new Ingredient(name, quantityStr, unit);
                    ingredients.add(ingredient);
                    ingredientAdapter.notifyItemInserted(ingredients.size() - 1);
                })
                .setNegativeButton("Anulează", null)
                .show();
    }
    
    private void showAddStepDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_step, null);
        EditText stepEditText = dialogView.findViewById(R.id.edit_step_description);
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Adăugare pas")
                .setView(dialogView)
                .setPositiveButton("Adaugă", (dialog, which) -> {
                    String stepDescription = stepEditText.getText().toString().trim();
                    
                    if (stepDescription.isEmpty()) {
                        Toast.makeText(this, "Descrierea pasului este obligatorie", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    steps.add(stepDescription);
                    stepAdapter.notifyItemInserted(steps.size() - 1);
                })
                .setNegativeButton("Anulează", null)
                .show();
    }
    
    private void saveRecipe() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }
        
        // Create new recipe
        Recipe recipe = createRecipeFromInputs();
        
        // Save recipe to repository
        recipeRepository.addRecipe(recipe);
        
        // Show success message
        Toast.makeText(this, "Rețeta a fost salvată cu succes!", Toast.LENGTH_SHORT).show();
        
        // Return to recipe list
        finish();
    }
    
    private boolean validateInputs() {
        boolean isValid = true;
        
        TextInputLayout titleInputLayout = findViewById(R.id.title_input_layout);
        TextInputLayout descriptionInputLayout = findViewById(R.id.description_input_layout);
        TextInputLayout prepTimeInputLayout = findViewById(R.id.prep_time_input_layout);
        TextInputLayout cookTimeInputLayout = findViewById(R.id.cook_time_input_layout);
        TextInputLayout servingsInputLayout = findViewById(R.id.servings_input_layout);
        TextInputLayout difficultyInputLayout = findViewById(R.id.difficulty_input_layout);
        TextInputLayout categoryInputLayout = findViewById(R.id.category_input_layout);
        TextInputLayout regionInputLayout = findViewById(R.id.region_input_layout);
        
        // Reset all errors and activation states
        titleInputLayout.setError(null);
        titleInputLayout.setActivated(false);
        descriptionInputLayout.setError(null);
        descriptionInputLayout.setActivated(false);
        prepTimeInputLayout.setError(null);
        prepTimeInputLayout.setActivated(false);
        cookTimeInputLayout.setError(null);
        cookTimeInputLayout.setActivated(false);
        servingsInputLayout.setError(null);
        servingsInputLayout.setActivated(false);
        difficultyInputLayout.setError(null);
        difficultyInputLayout.setActivated(false);
        categoryInputLayout.setError(null);
        categoryInputLayout.setActivated(false);
        regionInputLayout.setError(null);
        regionInputLayout.setActivated(false);
        
        if (titleEditText.getText().toString().trim().isEmpty()) {
            titleInputLayout.setError("Titlul este obligatoriu");
            titleInputLayout.setActivated(true);
            isValid = false;
        }
        
        if (descriptionEditText.getText().toString().trim().isEmpty()) {
            descriptionInputLayout.setError("Descrierea este obligatorie");
            descriptionInputLayout.setActivated(true);
            isValid = false;
        }
        
        if (prepTimeEditText.getText().toString().trim().isEmpty()) {
            prepTimeInputLayout.setError("Timpul de preparare este obligatoriu");
            prepTimeInputLayout.setActivated(true);
            isValid = false;
        } else {
            try {
                int prepTime = Integer.parseInt(prepTimeEditText.getText().toString().trim());
                if (prepTime <= 0) {
                    prepTimeInputLayout.setError("Timpul trebuie să fie pozitiv");
                    prepTimeInputLayout.setActivated(true);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                prepTimeInputLayout.setError("Valoare invalidă");
                prepTimeInputLayout.setActivated(true);
                isValid = false;
            }
        }
        
        if (cookTimeEditText.getText().toString().trim().isEmpty()) {
            cookTimeInputLayout.setError("Timpul de gătire este obligatoriu");
            cookTimeInputLayout.setActivated(true);
            isValid = false;
        } else {
            try {
                int cookTime = Integer.parseInt(cookTimeEditText.getText().toString().trim());
                if (cookTime <= 0) {
                    cookTimeInputLayout.setError("Timpul trebuie să fie pozitiv");
                    cookTimeInputLayout.setActivated(true);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                cookTimeInputLayout.setError("Valoare invalidă");
                cookTimeInputLayout.setActivated(true);
                isValid = false;
            }
        }
        
        if (servingsEditText.getText().toString().trim().isEmpty()) {
            servingsInputLayout.setError("Numărul de porții este obligatoriu");
            servingsInputLayout.setActivated(true);
            isValid = false;
        } else {
            try {
                int servings = Integer.parseInt(servingsEditText.getText().toString().trim());
                if (servings <= 0) {
                    servingsInputLayout.setError("Numărul de porții trebuie să fie pozitiv");
                    servingsInputLayout.setActivated(true);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                servingsInputLayout.setError("Valoare invalidă");
                servingsInputLayout.setActivated(true);
                isValid = false;
            }
        }
        
        if (difficultyDropdown.getText().toString().trim().isEmpty()) {
            difficultyInputLayout.setError("Dificultatea este obligatorie");
            difficultyInputLayout.setActivated(true);
            isValid = false;
        }
        
        if (categoryDropdown.getText().toString().trim().isEmpty()) {
            categoryInputLayout.setError("Categoria este obligatorie");
            categoryInputLayout.setActivated(true);
            isValid = false;
        }
        
        if (regionDropdown.getText().toString().trim().isEmpty()) {
            regionInputLayout.setError("Regiunea este obligatorie");
            regionInputLayout.setActivated(true);
            isValid = false;
        }
        
        if (ingredients.isEmpty()) {
            Toast.makeText(this, "Adăugați cel puțin un ingredient", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        if (steps.isEmpty()) {
            Toast.makeText(this, "Adăugați cel puțin un pas de preparare", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        return isValid;
    }
    
    private Recipe createRecipeFromInputs() {
        int prepTime = Integer.parseInt(prepTimeEditText.getText().toString().trim());
        int cookTime = Integer.parseInt(cookTimeEditText.getText().toString().trim());
        int servings = Integer.parseInt(servingsEditText.getText().toString().trim());
        
        // Generate a unique ID for the recipe
        int newId = recipeRepository.getNextAvailableId();
        
        // Create recipe
        Recipe recipe = new Recipe(
                newId,
                titleEditText.getText().toString().trim(),
                descriptionEditText.getText().toString().trim(),
                categoryDropdown.getText().toString().trim(),
                regionDropdown.getText().toString().trim(),
                difficultyDropdown.getText().toString().trim(),
                prepTime,
                cookTime,
                servings,
                R.drawable.placeholder_recipe // Placeholder image until we save the user's image
        );
        
        // Set dietary restrictions
        recipe.setVegetarian(vegetarianCheckbox.isChecked());
        recipe.setVegan(veganCheckbox.isChecked());
        recipe.setGlutenFree(glutenFreeCheckbox.isChecked());
        recipe.setLactoseFree(lactoseFreeCheckbox.isChecked());
        
        // Set ingredients
        recipe.setIngredients(new ArrayList<>(ingredients));
        
        // Set preparation steps
        recipe.setPreparationSteps(new ArrayList<>(steps));
        
        // Set user contribution info
        recipe.setUserContributed(true);
        
        // Get current user info from preferences
        PreferenceManager prefManager = new PreferenceManager(this);
        UserProfile userProfile = prefManager.getUserProfile();
        
        if (userProfile != null) {
            recipe.setAuthorId(userProfile.getUserId());
            recipe.setAuthorName(userProfile.getDisplayName().isEmpty() ? 
                    userProfile.getUsername() : userProfile.getDisplayName());
            
            // Add to user's contributed recipes
            prefManager.addContributedRecipe(newId);
        }
        
        return recipe;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imagePreview.setImageURI(selectedImageUri);
            imagePreview.setVisibility(View.VISIBLE);
            imagePlaceholderText.setVisibility(View.GONE);
        }
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