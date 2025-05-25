package com.example.myapplication.Joc1.Culinary;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to manage a shopping list of ingredients from recipes
 */
public class ShoppingListActivity extends AppCompatActivity {
    
    private RecyclerView shoppingListRecyclerView;
    private CardView emptyStateCard;
    private TextInputEditText addItemEditText;
    private MaterialButton addItemButton;
    private FloatingActionButton clearAllFab;
    
    private ShoppingListDBHelper dbHelper;
    private ShoppingListAdapter adapter;
    private List<ShoppingListItem> shoppingList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        
        // Initialize database helper
        dbHelper = new ShoppingListDBHelper(this);
        
        // Initialize UI elements
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        
        // Load shopping list items
        loadShoppingListItems();
        
        // Check for incoming ingredients from recipes or meal planning
        if (getIntent().hasExtra("recipe_ingredients")) {
            String[] ingredients = getIntent().getStringArrayExtra("recipe_ingredients");
            String recipeTitle = getIntent().getStringExtra("recipe_title");
            boolean fromMealPlan = getIntent().getBooleanExtra("from_meal_plan", false);
            
            if (ingredients != null && ingredients.length > 0) {
                if (fromMealPlan) {
                    // If from meal plan, we have multiple recipe sources
                    String[] recipeSources = getIntent().getStringArrayExtra("recipe_sources");
                    addIngredientsFromMealPlan(ingredients, recipeSources);
                } else {
                    // Single recipe
                    addIngredientsFromRecipe(ingredients, recipeTitle);
                }
            }
        }
    }
    
    private void initializeViews() {
        shoppingListRecyclerView = findViewById(R.id.shoppingListRecyclerView);
        emptyStateCard = findViewById(R.id.emptyStateCard);
        addItemEditText = findViewById(R.id.addItemEditText);
        addItemButton = findViewById(R.id.addItemButton);
        clearAllFab = findViewById(R.id.clearAllFab);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.shopping_list_title);
        }
    }
    
    private void setupRecyclerView() {
        shoppingList = new ArrayList<>();
        adapter = new ShoppingListAdapter(shoppingList);
        shoppingListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        shoppingListRecyclerView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        // Add button click listener
        addItemButton.setOnClickListener(v -> addNewItem());
        
        // Edit text "Done" action listener
        addItemEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || 
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                addNewItem();
                return true;
            }
            return false;
        });
        
        // Clear all button listener
        clearAllFab.setOnClickListener(v -> showClearConfirmationDialog());
    }
    
    private void loadShoppingListItems() {
        shoppingList.clear();
        List<ShoppingItem> items = dbHelper.getAllItems();
        
        for (ShoppingItem item : items) {
            ShoppingListItem listItem = new ShoppingListItem(
                item.getId(),
                item.getName(),
                item.getRecipeSource(),
                item.isChecked()
            );
            shoppingList.add(listItem);
        }
        
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }
    
    private void addNewItem() {
        String ingredient = addItemEditText.getText().toString().trim();
        
        if (!TextUtils.isEmpty(ingredient)) {
            ShoppingListItem listItem = new ShoppingListItem(ingredient, null, false);
            
            // Convert to ShoppingItem for database
            ShoppingItem item = new ShoppingItem(
                0,
                ingredient,
                "",  // quantity as empty string
                "",
                null, 
                false
            );
            
            long id = dbHelper.addItem(item);
            
            if (id != -1) {
                listItem.setId(id);
                shoppingList.add(listItem);
                adapter.notifyItemInserted(shoppingList.size() - 1);
                addItemEditText.setText("");
                updateEmptyState();
            } else {
                Toast.makeText(this, "Eroare la adăugarea ingredientului", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void addIngredientsFromRecipe(String[] ingredients, String recipeTitle) {
        int addedCount = 0;
        
        for (String ingredient : ingredients) {
            // Clean up the ingredient text
            String cleanedIngredient = ingredient.trim();
            
            // Check if the ingredient is not empty and not already in the list
            if (!TextUtils.isEmpty(cleanedIngredient) && !ingredientAlreadyExists(cleanedIngredient)) {
                // Create ShoppingListItem
                ShoppingListItem listItem = new ShoppingListItem(cleanedIngredient, recipeTitle, false);
                
                // Convert to ShoppingItem for database
                ShoppingItem item = new ShoppingItem(
                    0,
                    cleanedIngredient,
                    "",  // quantity as empty string
                    "",
                    recipeTitle, 
                    false
                );
                
                long id = dbHelper.addItem(item);
                
                if (id != -1) {
                    listItem.setId(id);
                    shoppingList.add(listItem);
                    addedCount++;
                }
            }
        }
        
        if (addedCount > 0) {
            adapter.notifyDataSetChanged();
            updateEmptyState();
            
            Toast.makeText(this, 
                    "Au fost adăugate " + addedCount + " ingrediente din " + recipeTitle, 
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, 
                    "Toate ingredientele pentru această rețetă sunt deja în listă", 
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    private void addIngredientsFromMealPlan(String[] ingredients, String[] recipeSources) {
        int addedCount = 0;
        
        for (int i = 0; i < ingredients.length; i++) {
            // Clean up the ingredient text
            String cleanedIngredient = ingredients[i].trim();
            
            // Determine the recipe source if available
            String recipeSource = (recipeSources != null && i < recipeSources.length) ? 
                    recipeSources[i] : "Plan de mese";
            
            // Check if the ingredient is not empty and not already in the list
            if (!TextUtils.isEmpty(cleanedIngredient) && !ingredientAlreadyExists(cleanedIngredient)) {
                // Create ShoppingListItem
                ShoppingListItem listItem = new ShoppingListItem(cleanedIngredient, recipeSource, false);
                
                // Convert to ShoppingItem for database
                ShoppingItem item = new ShoppingItem(
                    0,
                    cleanedIngredient,
                    "",  // quantity as empty string
                    "",
                    recipeSource, 
                    false
                );
                
                long id = dbHelper.addItem(item);
                
                if (id != -1) {
                    listItem.setId(id);
                    shoppingList.add(listItem);
                    addedCount++;
                }
            }
        }
        
        if (addedCount > 0) {
            adapter.notifyDataSetChanged();
            updateEmptyState();
            
            Toast.makeText(this, 
                    "Au fost adăugate " + addedCount + " ingrediente din planul de mese", 
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, 
                    "Toate ingredientele sunt deja în listă", 
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean ingredientAlreadyExists(String ingredient) {
        for (ShoppingListItem item : shoppingList) {
            if (item.getIngredient().equalsIgnoreCase(ingredient)) {
                return true;
            }
        }
        return false;
    }
    
    private void updateEmptyState() {
        if (shoppingList.isEmpty()) {
            emptyStateCard.setVisibility(View.VISIBLE);
            shoppingListRecyclerView.setVisibility(View.GONE);
            clearAllFab.setVisibility(View.GONE);
        } else {
            emptyStateCard.setVisibility(View.GONE);
            shoppingListRecyclerView.setVisibility(View.VISIBLE);
            clearAllFab.setVisibility(View.VISIBLE);
        }
    }
    
    private void showClearConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Golește lista")
                .setMessage("Ești sigur că vrei să ștergi toate elementele din lista de cumpărături?")
                .setPositiveButton("Da", (dialog, which) -> {
                    clearAllItems();
                })
                .setNegativeButton("Nu", null)
                .show();
    }
    
    private void clearAllItems() {
        if (!shoppingList.isEmpty()) {
            dbHelper.clearAllItems();
            shoppingList.clear();
            adapter.notifyDataSetChanged();
            updateEmptyState();
            
            Snackbar.make(
                    findViewById(android.R.id.content),
                    "Lista de cumpărături a fost golită",
                    Snackbar.LENGTH_LONG
            ).setAction("Anulează", v -> {
                // Future feature: restore deleted items
                Toast.makeText(this, "Funcționalitatea de restaurare va fi disponibilă în versiuni viitoare", Toast.LENGTH_SHORT).show();
            }).show();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {
        
        private final List<ShoppingListItem> items;
        
        public ShoppingListAdapter(List<ShoppingListItem> items) {
            this.items = items;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_shopping_list, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ShoppingListItem item = items.get(position);
            holder.bind(item);
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            private final CheckBox checkBox;
            private final TextView tvIngredientName;
            private final TextView tvCategory;
            private final TextView tvQuantity;
            private final TextView tvSource;
            private final TextView tvRecipeCount;
            
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                checkBox = itemView.findViewById(R.id.checkBox);
                tvIngredientName = itemView.findViewById(R.id.tvIngredientName);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                tvSource = itemView.findViewById(R.id.tvSource);
                tvRecipeCount = itemView.findViewById(R.id.tvRecipeCount);
            }
            
            public void bind(ShoppingListItem item) {
                checkBox.setChecked(item.isChecked());
                tvIngredientName.setText(item.getIngredient());
                tvSource.setText(item.getRecipeSource());
                
                // Set up checkbox change listener
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (buttonView.isPressed()) {
                        item.setChecked(isChecked);
                        // Update in database
                        ShoppingItem dbItem = new ShoppingItem(
                            item.getId(),
                            item.getIngredient(),
                            "",  // quantity as empty string
                            "",
                            item.getRecipeSource(),
                            isChecked
                        );
                        dbHelper.updateItem(dbItem);
                    }
                });
            }
        }
    }
    
    /**
     * Model class for a shopping list item
     */
    public static class ShoppingListItem {
        private long id;
        private String ingredient;
        private String recipeSource;
        private boolean checked;
        
        public ShoppingListItem(String ingredient, String recipeSource, boolean checked) {
            this.ingredient = ingredient;
            this.recipeSource = recipeSource;
            this.checked = checked;
        }
        
        public ShoppingListItem(long id, String ingredient, String recipeSource, boolean checked) {
            this.id = id;
            this.ingredient = ingredient;
            this.recipeSource = recipeSource;
            this.checked = checked;
        }
        
        public long getId() {
            return id;
        }
        
        public void setId(long id) {
            this.id = id;
        }
        
        public String getIngredient() {
            return ingredient;
        }
        
        public String getRecipeSource() {
            return recipeSource;
        }
        
        public boolean isChecked() {
            return checked;
        }
        
        public void setChecked(boolean checked) {
            this.checked = checked;
        }
    }
} 