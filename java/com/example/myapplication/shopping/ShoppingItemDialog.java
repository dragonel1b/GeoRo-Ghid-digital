package com.example.myapplication.shopping;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Material Design 3 Bottom Sheet Dialog for adding items to a shopping list
 */
public class ShoppingItemDialog extends BottomSheetDialogFragment {

    // UI Components
    private ShapeableImageView itemImageView;
    private TextInputLayout itemNameLayout;
    private TextInputEditText itemNameInput;
    private RadioGroup categoryRadioGroup;
    private MaterialRadioButton categoryOther;
    private TextInputLayout categoryCustomLayout;
    private MaterialAutoCompleteTextView categoryDropdown;
    private TextInputLayout quantityLayout;
    private TextInputEditText quantityInput;
    private TextInputLayout unitLayout;
    private MaterialAutoCompleteTextView unitDropdown;
    private MaterialButton cancelButton, addButton;

    // Data
    private String selectedCategory = "";
    private Uri currentImageUri = null;
    private File photoFile = null;
    private FirebaseAnalytics firebaseAnalytics;
    private ShoppingListViewModel viewModel;

    // Activity Result Launchers
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Intent> cropImageLauncher;

    // Listener
    private OnItemAddedListener onItemAddedListener;

    // Interface for callbacks
    public interface OnItemAddedListener {
        void onItemAdded(ShoppingItem item);
    }

    public static ShoppingItemDialog newInstance() {
        return new ShoppingItemDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set bottom sheet style
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_Material3_BottomSheetDialog);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(ShoppingListViewModel.class);
        
        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());
        logEvent("shopping_item_dialog_opened", null);
        
        // Initialize permission launcher
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean cameraGranted = result.get(Manifest.permission.CAMERA);
                Boolean storageGranted = result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                
                if (Boolean.TRUE.equals(cameraGranted) && Boolean.TRUE.equals(storageGranted)) {
                    showImageSourceDialog();
                } else {
                    Toast.makeText(requireContext(), R.string.item_image_error, Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        // Initialize camera launcher
        takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                if (Boolean.TRUE.equals(result) && currentImageUri != null) {
                    startImageCropping(currentImageUri);
                }
            }
        );
        
        // Initialize gallery launcher
        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    startImageCropping(uri);
                }
            }
        );
        
        // Initialize crop launcher
        cropImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                    Uri resultUri = UCrop.getOutput(result.getData());
                    if (resultUri != null) {
                        currentImageUri = resultUri;
                        itemImageView.setImageURI(resultUri);
                        logEvent("image_selected", null);
                    }
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_shopping_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);
        
        // Setup accessibility
        setupAccessibility(view);
        
        // Setup image picker
        setupImagePicker();
        
        // Setup category selection
        setupCategorySelection();
        
        // Setup unit dropdown
        setupUnitDropdown();
        
        // Setup autocomplete for item names
        setupItemNameAutocomplete();
        
        // Setup buttons
        setupButtons();
        
        // Load previously used values
        loadSavedValues();
        
        // Initial validation
        validateForm();
    }

    private void initViews(View view) {
        itemImageView = view.findViewById(R.id.itemImageView);
        itemNameLayout = view.findViewById(R.id.itemNameLayout);
        itemNameInput = view.findViewById(R.id.itemNameInput);
        categoryRadioGroup = view.findViewById(R.id.categoryRadioGroup);
        categoryOther = view.findViewById(R.id.categoryOther);
        categoryCustomLayout = view.findViewById(R.id.categoryCustomLayout);
        categoryDropdown = view.findViewById(R.id.categoryDropdown);
        quantityLayout = view.findViewById(R.id.quantityLayout);
        quantityInput = view.findViewById(R.id.quantityInput);
        unitLayout = view.findViewById(R.id.unitLayout);
        unitDropdown = view.findViewById(R.id.unitDropdown);
        cancelButton = view.findViewById(R.id.cancelButton);
        addButton = view.findViewById(R.id.addButton);
    }
    
    private void setupAccessibility(View view) {
        // Set accessibility headings
        ViewCompat.setAccessibilityHeading(view.findViewById(R.id.dialogTitle), true);
        
        // Text watcher for live validation
        itemNameInput.addTextChangedListener(new SimpleTextWatcher(text -> validateForm()));
        quantityInput.addTextChangedListener(new SimpleTextWatcher(text -> validateForm()));
    }
    
    private void setupImagePicker() {
        View selectImageButton = requireView().findViewById(R.id.selectImageButton);
        selectImageButton.setOnClickListener(v -> {
            checkCameraPermission();
        });
    }
    
    private void setupCategorySelection() {
        // Setup radio button behavior
        categoryRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.categoryFruits) {
                selectedCategory = getString(R.string.category_fruits);
                categoryCustomLayout.setVisibility(View.GONE);
                logCategorySelected(selectedCategory);
            } else if (checkedId == R.id.categoryVegetables) {
                selectedCategory = getString(R.string.category_vegetables);
                categoryCustomLayout.setVisibility(View.GONE);
                logCategorySelected(selectedCategory);
            } else if (checkedId == R.id.categoryDairy) {
                selectedCategory = getString(R.string.category_dairy);
                categoryCustomLayout.setVisibility(View.GONE);
                logCategorySelected(selectedCategory);
            } else if (checkedId == R.id.categoryOther) {
                selectedCategory = "";
                categoryCustomLayout.setVisibility(View.VISIBLE);
                categoryDropdown.requestFocus();
                logEvent("custom_category_selected", null);
            }
            validateForm();
        });
        
        // Set up custom category dropdown
        List<String> categories = getCustomCategories();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
            requireContext(),
            R.layout.item_dropdown_menu,
            categories
        );
        categoryDropdown.setAdapter(categoryAdapter);
        
        categoryDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = (String) parent.getItemAtPosition(position);
            logCategorySelected(selectedCategory);
            validateForm();
            saveCustomCategory(selectedCategory);
        });
    }
    
    private List<String> getCustomCategories() {
        // This would typically come from a repository or Room
        List<String> categories = new ArrayList<>();
        categories.add("Băuturi");
        categories.add("Patiserie");
        categories.add("Conserve");
        categories.add("Produse congelate");
        categories.add("Produse de curățenie");
        categories.add("Articole de toaletă");
        
        // Add saved categories from preferences
        SharedPreferences prefs = requireContext().getSharedPreferences(
            "shopping_prefs", Context.MODE_PRIVATE);
        String savedCategories = prefs.getString("custom_categories", "");
        if (!savedCategories.isEmpty()) {
            String[] savedCats = savedCategories.split(",");
            for (String cat : savedCats) {
                if (!categories.contains(cat) && !cat.isEmpty()) {
                    categories.add(cat);
                }
            }
        }
        
        return categories;
    }
    
    private void saveCustomCategory(String category) {
        SharedPreferences prefs = requireContext().getSharedPreferences(
            "shopping_prefs", Context.MODE_PRIVATE);
        
        String savedCategories = prefs.getString("custom_categories", "");
        if (!savedCategories.contains(category)) {
            if (savedCategories.isEmpty()) {
                savedCategories = category;
            } else {
                savedCategories += "," + category;
            }
            prefs.edit().putString("custom_categories", savedCategories).apply();
        }
    }
    
    private void setupUnitDropdown() {
        List<String> units = getUnitsList();
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
            requireContext(),
            R.layout.item_dropdown_menu,
            units
        );
        unitDropdown.setAdapter(unitAdapter);
        
        unitDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String unit = (String) parent.getItemAtPosition(position);
            Bundle params = new Bundle();
            params.putString("unit", unit);
            logEvent("unit_selected", params);
            validateForm();
        });
    }
    
    private List<String> getUnitsList() {
        // This would typically come from resources or a repository
        List<String> units = new ArrayList<>();
        units.add("kg");
        units.add("g");
        units.add("l");
        units.add("ml");
        units.add("buc");
        units.add("pachet");
        
        // Add custom units from preferences
        SharedPreferences prefs = requireContext().getSharedPreferences(
            "shopping_prefs", Context.MODE_PRIVATE);
        String savedUnits = prefs.getString("custom_units", "");
        if (!savedUnits.isEmpty()) {
            String[] unitArray = savedUnits.split(",");
            for (String unit : unitArray) {
                if (!units.contains(unit) && !unit.isEmpty()) {
                    units.add(unit);
                }
            }
        }
        
        return units;
    }
    
    private void setupItemNameAutocomplete() {
        // Get frequently used items
        List<String> frequentItems = getFrequentItemNames();
        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            frequentItems
        );
        // TextInputEditText doesn't have setAdapter method directly
        // Either replace the view in the layout with AutoCompleteTextView
        // or use a different approach for autocomplete
        // For now, we'll skip setting the adapter
    }
    
    private List<String> getFrequentItemNames() {
        // This would typically come from a repository
        List<String> items = new ArrayList<>();
        items.add("Pâine");
        items.add("Lapte");
        items.add("Ouă");
        items.add("Roșii");
        items.add("Mere");
        items.add("Apă");
        
        // Add items from preferences
        SharedPreferences prefs = requireContext().getSharedPreferences(
            "shopping_prefs", Context.MODE_PRIVATE);
        String savedItems = prefs.getString("frequent_items", "");
        if (!savedItems.isEmpty()) {
            String[] itemArray = savedItems.split(",");
            for (String item : itemArray) {
                if (!items.contains(item) && !item.isEmpty()) {
                    items.add(item);
                }
            }
        }
        
        return items;
    }
    
    private void setupButtons() {
        cancelButton.setOnClickListener(v -> {
            logEvent("cancel_clicked", null);
            dismiss();
        });
        
        addButton.setOnClickListener(v -> {
            if (validateForm()) {
                addShoppingItem();
            }
        });
    }
    
    private void loadSavedValues() {
        SharedPreferences prefs = requireContext().getSharedPreferences(
            "shopping_prefs", Context.MODE_PRIVATE);
        
        // Load last unit
        String lastUnit = prefs.getString("last_unit", null);
        if (lastUnit != null) {
            unitDropdown.setText(lastUnit, false);
        }
    }
    
    private boolean validateForm() {
        boolean isValid = true;
        
        // Validate item name
        String itemName = itemNameInput.getText().toString().trim();
        if (itemName.isEmpty()) {
            itemNameLayout.setError(getString(R.string.item_error_validation));
            isValid = false;
        } else {
            itemNameLayout.setError(null);
        }
        
        // Validate category
        if (selectedCategory.isEmpty() && categoryOther.isChecked()) {
            String customCategory = categoryDropdown.getText().toString().trim();
            if (customCategory.isEmpty()) {
                categoryCustomLayout.setError(getString(R.string.item_error_validation));
                isValid = false;
            } else {
                categoryCustomLayout.setError(null);
                selectedCategory = customCategory;
            }
        }
        
        // Validate quantity
        String quantity = quantityInput.getText().toString().trim();
        if (quantity.isEmpty()) {
            quantityLayout.setError(getString(R.string.item_error_validation));
            isValid = false;
        } else {
            quantityLayout.setError(null);
        }
        
        // Validate unit
        String unit = unitDropdown.getText().toString().trim();
        if (unit.isEmpty()) {
            unitLayout.setError(getString(R.string.item_error_validation));
            isValid = false;
        } else {
            unitLayout.setError(null);
        }
        
        // Enable or disable add button
        addButton.setEnabled(isValid);
        
        return isValid;
    }
    
    private void addShoppingItem() {
        String name = itemNameInput.getText().toString().trim();
        String quantityStr = quantityInput.getText().toString().trim();
        String unit = unitDropdown.getText().toString().trim();
        float quantity = 0;
        
        try {
            quantity = Float.parseFloat(quantityStr);
        } catch (NumberFormatException e) {
            quantityLayout.setError(getString(R.string.item_error_validation));
            return;
        }
        
        // Create shopping item
        ShoppingItem item = new ShoppingItem(
            System.currentTimeMillis(), // ID
            name,
            selectedCategory,
            quantity,
            unit
        );
        
        // Set image URI if available
        if (currentImageUri != null) {
            item.setImageUri(currentImageUri.toString());
        }
        
        // Add to database
        viewModel.addShoppingItem(item);
        
        // Log event
        Bundle params = new Bundle();
        params.putString("item_name", name);
        params.putString("item_category", selectedCategory);
        logEvent("item_added", params);
        
        // Save unit for next time
        SharedPreferences prefs = requireContext().getSharedPreferences(
            "shopping_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("last_unit", unit).apply();
        
        // Add item to frequent items
        saveFrequentItem(name);
        
        // Notify callback
        if (onItemAddedListener != null) {
            onItemAddedListener.onItemAdded(item);
        }
        
        // Show success message
        showAddedSnackbar();
        
        // Dismiss dialog
        dismiss();
    }
    
    private void saveFrequentItem(String itemName) {
        SharedPreferences prefs = requireContext().getSharedPreferences(
            "shopping_prefs", Context.MODE_PRIVATE);
        
        String frequentItems = prefs.getString("frequent_items", "");
        if (!frequentItems.contains(itemName)) {
            if (frequentItems.isEmpty()) {
                frequentItems = itemName;
            } else {
                frequentItems += "," + itemName;
            }
            prefs.edit().putString("frequent_items", frequentItems).apply();
        }
    }
    
    private void showAddedSnackbar() {
        View rootView = requireActivity().findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(
            rootView,
            R.string.item_added_success,
            Snackbar.LENGTH_LONG
        );
        
        snackbar.setAction(R.string.item_undo_remove, v -> {
            // Undo add operation
            viewModel.removeLastAddedItem();
            logEvent("undo_add", null);
        });
        
        snackbar.show();
    }
    
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            
            requestPermissionLauncher.launch(new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            });
        } else {
            showImageSourceDialog();
        }
    }
    
    private void showImageSourceDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.item_image_select_title)
            .setItems(new CharSequence[] {
                getString(R.string.item_image_select_camera),
                getString(R.string.item_image_select_gallery)
            }, (dialog, which) -> {
                if (which == 0) {
                    // Camera
                    openCamera();
                } else {
                    // Gallery
                    pickImageLauncher.launch("image/*");
                }
            })
            .setNegativeButton(R.string.item_image_select_cancel, null)
            .show();
    }
    
    private void openCamera() {
        try {
            photoFile = createImageFile();
            currentImageUri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.myapplication.fileprovider",
                photoFile
            );
            takePictureLauncher.launch(currentImageUri);
        } catch (IOException e) {
            Toast.makeText(requireContext(), R.string.item_image_error, Toast.LENGTH_SHORT).show();
        }
    }
    
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir("Images");
        
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );
    }
    
    private void startImageCropping(Uri sourceUri) {
        try {
            // Create destination URI
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File destinationFile = new File(
                requireContext().getExternalFilesDir("Images"),
                "CROPPED_" + timeStamp + ".jpg"
            );
            Uri destinationUri = Uri.fromFile(destinationFile);
            
            // Configure crop options
            UCrop.Options options = new UCrop.Options();
            options.setCompressionQuality(80);
            options.setCircleDimmedLayer(false);
            options.setHideBottomControls(false);
            options.setFreeStyleCropEnabled(false);
            options.setToolbarColor(ContextCompat.getColor(requireContext(), R.color.rom_primary));
            options.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.rom_primary_dark));
            options.setToolbarWidgetColor(ContextCompat.getColor(requireContext(), R.color.white));
            
            // Start cropping
            UCrop.of(sourceUri, destinationUri)
                 .withOptions(options)
                 .withAspectRatio(1, 1)
                 .start(requireContext(), this, UCrop.REQUEST_CROP);
                 
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.item_image_error, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void logEvent(String eventName, Bundle params) {
        if (firebaseAnalytics != null) {
            firebaseAnalytics.logEvent(eventName, params);
        }
    }
    
    private void logCategorySelected(String category) {
        Bundle params = new Bundle();
        params.putString("category", category);
        logEvent("category_selected", params);
    }
    
    public void setOnItemAddedListener(OnItemAddedListener listener) {
        this.onItemAddedListener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        logEvent("dialog_dismissed", null);
    }

    /**
     * Simple TextWatcher for input validation
     */
    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final TextChangedCallback callback;
        
        SimpleTextWatcher(TextChangedCallback callback) {
            this.callback = callback;
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            callback.onTextChanged(s.toString());
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {}
        
        interface TextChangedCallback {
            void onTextChanged(String text);
        }
    }
} 