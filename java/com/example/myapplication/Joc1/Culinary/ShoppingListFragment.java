package com.example.myapplication.Joc1.Culinary;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.myapplication.Joc1.Culinary.ManualShoppingItemDialogFragment;
import com.example.myapplication.Joc1.Culinary.MealPlanDBHelper;
import com.example.myapplication.Joc1.Culinary.MealPlan;
import com.example.myapplication.Joc1.Culinary.ShoppingItem;

/**
 * Fragment for displaying and managing shopping lists
 */
public class ShoppingListFragment extends Fragment implements ManualShoppingItemDialogFragment.OnItemAddedListener {
    
    private TextView descriptionText;
    private ChipGroup filterChipGroup;
    private RecyclerView shoppingListRecyclerView;
    private TextView emptyStateText;
    private Button btnAddManually;
    private Button btnExport;
    
    private MealPlanDBHelper dbHelper;
    private ShoppingListAdapter adapter;
    private MealPlan currentPlan;
    private List<ShoppingItem> allItems = new ArrayList<>();
    private String currentFilter = "Toate";
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize database helper
        dbHelper = new MealPlanDBHelper(requireContext());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        
        // Initialize views
        descriptionText = rootView.findViewById(R.id.descriptionText);
        filterChipGroup = rootView.findViewById(R.id.filterChipGroup);
        shoppingListRecyclerView = rootView.findViewById(R.id.shoppingListRecyclerView);
        emptyStateText = rootView.findViewById(R.id.emptyStateText);
        btnAddManually = rootView.findViewById(R.id.btnAddManually);
        btnExport = rootView.findViewById(R.id.btnExport);
        
        return rootView;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Set up RecyclerView
        shoppingListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ShoppingListAdapter();
        shoppingListRecyclerView.setAdapter(adapter);
        
        // Set up filter chip group
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                currentFilter = chip.getText().toString();
                updateFilteredList(currentFilter);
            }
        });
        
        // Set button click listeners
        btnAddManually.setOnClickListener(v -> showAddItemDialog());
        btnExport.setOnClickListener(v -> exportShoppingList());
        
        // Load current shopping list
        loadShoppingList();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload data when resuming
        loadShoppingList();
    }
    
    /**
     * Load shopping list from the current week's meal plan
     */
    private void loadShoppingList() {
        // Get current week range
        Calendar weekStart = Calendar.getInstance();
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        weekStart.set(Calendar.HOUR_OF_DAY, 0);
        weekStart.set(Calendar.MINUTE, 0);
        weekStart.set(Calendar.SECOND, 0);
        weekStart.set(Calendar.MILLISECOND, 0);
        
        Calendar weekEnd = (Calendar) weekStart.clone();
        weekEnd.add(Calendar.DAY_OF_WEEK, 6);
        
        // Update description text with date range
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
        String weekRange = dateFormat.format(weekStart.getTime()) + " - " + 
                           dateFormat.format(weekEnd.getTime());
        descriptionText.setText(getString(R.string.generated_from_meal_plan, weekRange));
        
        // Get or create meal plan for this week
        currentPlan = dbHelper.getMealPlanForDateRange(
                weekStart.getTime(), weekEnd.getTime());
        
        if (currentPlan == null) {
            currentPlan = dbHelper.createMealPlan(weekStart.getTime(), weekEnd.getTime());
        }
        
        // Generate shopping list from meal plan
        allItems = currentPlan.generateShoppingList();
        
        // Add saved items from database
        List<ShoppingItem> savedItems = dbHelper.getShoppingItems(currentPlan.getId());
        
        // Convert saved items to proper ShoppingItem objects
        for (ShoppingItem item : savedItems) {
            if (!containsIngredient(allItems, item.getName())) {
                allItems.add(item);
            }
        }
        
        // Update checked status from database
        for (ShoppingItem item : allItems) {
            ShoppingItem savedItem = getSavedItem(savedItems, item.getName());
            if (savedItem != null) {
                item.setChecked(savedItem.isChecked());
                item.setQuantity(savedItem.getQuantity());
            }
        }
        
        // Filter and display items
        updateFilteredList(currentFilter);
    }
    
    /**
     * Filter shopping list items based on the selected category
     */
    private void filterItems() {
        List<ShoppingItem> filteredItems;
        
        if (currentFilter.equals("Toate")) {
            filteredItems = new ArrayList<>(allItems);
        } else {
            filteredItems = allItems.stream()
                    .filter(item -> item.getCategory().equals(currentFilter))
                    .collect(Collectors.toList());
        }
        
        // Sort items: unchecked first, then by category, then by name
        filteredItems.sort((item1, item2) -> {
            if (item1.isChecked() != item2.isChecked()) {
                return item1.isChecked() ? 1 : -1;
            }
            if (!item1.getCategory().equals(item2.getCategory())) {
                return item1.getCategory().compareTo(item2.getCategory());
            }
            return item1.getName().compareTo(item2.getName());
        });
        
        // Update adapter
        adapter.setItems(filteredItems);
        
        // Show empty state if needed
        if (filteredItems.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            shoppingListRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            shoppingListRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Check if the list contains an ingredient with the given name
     */
    private boolean containsIngredient(List<ShoppingItem> items, String name) {
        for (ShoppingItem item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get a saved item with the given name, or null if not found
     */
    private ShoppingItem getSavedItem(List<ShoppingItem> items, String name) {
        for (ShoppingItem item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Show dialog to add a manual shopping list item
     */
    private void showAddItemDialog() {
        ManualShoppingItemDialogFragment dialog = new ManualShoppingItemDialogFragment();
        dialog.show(getParentFragmentManager(), "AddShoppingItem");
        dialog.setListener(this);
    }
    
    /**
     * Add a manually entered shopping item
     */
    @Override
    public void onItemAdded(String name, String category, String quantity) {
        if (containsIngredient(allItems, name)) {
            // Update existing item
            ShoppingItem item = getSavedItem(allItems, name);
            if (item != null) {
                if (!TextUtils.isEmpty(quantity)) {
                    item.setQuantity(quantity);
                }
                // Update UI
                updateFilteredList(category);
            }
        } else {
            // Create new item
            ShoppingItem item = new ShoppingItem(
                    System.currentTimeMillis(),  // Temporary ID
                    name,
                    category,
                    quantity
            );
            
            // Add to list
            allItems.add(item);
            
            // Update UI
            updateFilteredList(category);
            
            // Save to database (implementation will depend on your db structure)
            saveItemToDatabase(item);
        }
    }
    
    /**
     * Export shopping list to share with other apps
     */
    private void exportShoppingList() {
        if (allItems.isEmpty()) {
            Toast.makeText(requireContext(), "Lista de cumpărături este goală", Toast.LENGTH_SHORT).show();
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("LISTA DE CUMPĂRĂTURI\n\n");
        
        // Group by category
        List<String> categories = new ArrayList<>();
        for (ShoppingItem item : allItems) {
            if (!categories.contains(item.getCategory())) {
                categories.add(item.getCategory());
            }
        }
        
        categories.sort(String::compareTo);
        
        for (String category : categories) {
            sb.append("--- ").append(category).append(" ---\n");
            
            for (ShoppingItem item : allItems) {
                if (item.getCategory().equals(category)) {
                    sb.append("• ").append(item.getName());
                    if (!item.getQuantity().isEmpty()) {
                        sb.append(" (").append(item.getQuantity()).append(")");
                    }
                    if (item.isChecked()) {
                        sb.append(" ✓");
                    }
                    sb.append("\n");
                }
            }
            sb.append("\n");
        }
        
        // Share the text
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Lista de cumpărături");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "Trimite lista de cumpărături"));
    }
    
    /**
     * Update a shopping item's checked status
     */
    private void updateItemCheckedStatus(ShoppingItem item, boolean isChecked) {
        item.setChecked(isChecked);
        dbHelper.updateShoppingItem(currentPlan.getId(), item);
        adapter.notifyDataSetChanged();
    }
    
    /**
     * Show dialog to edit item quantity
     */
    private void showEditQuantityDialog(ShoppingItem item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Editează cantitatea")
                .setView(R.layout.dialog_edit_quantity)
                .setPositiveButton("Salvează", (dialog, which) -> {
                    TextView quantityInput = ((androidx.appcompat.app.AlertDialog) dialog)
                            .findViewById(R.id.quantityInput);
                    if (quantityInput != null) {
                        String newQuantity = quantityInput.getText().toString();
                        item.setQuantity(newQuantity);
                        dbHelper.updateShoppingItem(currentPlan.getId(), item);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Anulează", null)
                .show();
    }
    
    /**
     * Adapter for shopping list items
     */
    private class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingItemViewHolder> {
        
        private List<ShoppingItem> items = new ArrayList<>();
        
        public void setItems(List<ShoppingItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public ShoppingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_shopping_list, parent, false);
            return new ShoppingItemViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ShoppingItemViewHolder holder, int position) {
            holder.bind(items.get(position));
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        /**
         * ViewHolder for shopping items
         */
        class ShoppingItemViewHolder extends RecyclerView.ViewHolder {
            
            private final CheckBox checkBox;
            private final TextView tvIngredientName;
            private final TextView tvCategory;
            private final TextView tvQuantity;
            private final TextView tvSource;
            private final TextView tvRecipeCount;
            
            ShoppingItemViewHolder(@NonNull View itemView) {
                super(itemView);
                
                checkBox = itemView.findViewById(R.id.checkBox);
                tvIngredientName = itemView.findViewById(R.id.tvIngredientName);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                tvSource = itemView.findViewById(R.id.tvSource);
                tvRecipeCount = itemView.findViewById(R.id.tvRecipeCount);
                
                // Set up click listener for quantity
                tvQuantity.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        showEditQuantityDialog(items.get(position));
                    }
                });
                
                // Set up checkbox change listener
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && buttonView.isPressed()) {
                        updateItemCheckedStatus(items.get(position), isChecked);
                    }
                });
            }
            
            void bind(ShoppingItem item) {
                // Prevent triggering checkbox change listener during binding
                checkBox.setOnCheckedChangeListener(null);
                checkBox.setChecked(item.isChecked());
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && buttonView.isPressed()) {
                        updateItemCheckedStatus(items.get(position), isChecked);
                    }
                });
                
                tvIngredientName.setText(item.getName());
                tvCategory.setText(item.getCategory());
                
                // Set quantity if available
                if (item.getQuantity() != null && !item.getQuantity().isEmpty()) {
                    tvQuantity.setText(item.getQuantity());
                    tvQuantity.setVisibility(View.VISIBLE);
                } else {
                    tvQuantity.setText("");
                    tvQuantity.setVisibility(View.GONE);
                }
                
                // Set recipe sources if available
                String sources = item.getRecipeSourcesList().toString();
                if (!sources.isEmpty()) {
                    tvSource.setText(sources);
                    tvSource.setVisibility(View.VISIBLE);
                    
                    int recipeCount = item.getRecipeCount();
                    if (recipeCount > 0) {
                        tvRecipeCount.setText(recipeCount + " " + 
                                (recipeCount == 1 ? "rețetă" : "rețete"));
                        tvRecipeCount.setVisibility(View.VISIBLE);
                    } else {
                        tvRecipeCount.setVisibility(View.GONE);
                    }
                } else {
                    tvSource.setVisibility(View.GONE);
                    tvRecipeCount.setVisibility(View.GONE);
                }
                
                // Apply stroke-through effect on text if checked
                if (item.isChecked()) {
                    tvIngredientName.setPaintFlags(tvIngredientName.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    tvIngredientName.setPaintFlags(tvIngredientName.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                }
            }
        }
    }
    
    private void updateFilteredList(String category) {
        if (category == null || category.isEmpty() || "Toate".equals(category)) {
            // Show all items
            adapter.setItems(new ArrayList<>(allItems));
        } else {
            // Filter by category
            List<ShoppingItem> filteredItems = allItems.stream()
                    .filter(item -> item.getCategory().equals(category))
                    .collect(Collectors.toList());
            adapter.setItems(filteredItems);
        }
        
        // Update empty state visibility
        if (adapter.getItemCount() == 0) {
            emptyStateText.setVisibility(View.VISIBLE);
            shoppingListRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            shoppingListRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Save shopping item to database
     */
    private void saveItemToDatabase(ShoppingItem item) {
        if (currentPlan != null) {
            dbHelper.addShoppingItem(currentPlan.getId(), item);
        }
    }
} 