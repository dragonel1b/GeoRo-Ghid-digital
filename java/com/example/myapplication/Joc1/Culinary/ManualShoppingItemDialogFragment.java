package com.example.myapplication.Joc1.Culinary;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.R;

/**
 * Dialog fragment for manually adding shopping list items
 */
public class ManualShoppingItemDialogFragment extends DialogFragment {

    private static final String[] CATEGORIES = {
            "Legume și fructe",
            "Lactate",
            "Carne și pește",
            "Panificație",
            "Conserve",
            "Condimente",
            "Dulciuri",
            "Băuturi",
            "Alte produse"
    };

    private OnItemAddedListener listener;
    private EditText nameInput;
    private Spinner categorySpinner;
    private EditText quantityInput;

    /**
     * Interface for item added callback
     */
    public interface OnItemAddedListener {
        /**
         * Called when an item is added
         * @param name Name of the item
         * @param category Category of the item
         * @param quantity Quantity of the item
         */
        void onItemAdded(String name, String category, String quantity);
    }

    /**
     * Set the listener for item added events
     * @param listener Listener to set
     */
    public void setListener(OnItemAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_manual_shopping_item, null);

        // Initialize views
        nameInput = view.findViewById(R.id.nameInput);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        quantityInput = view.findViewById(R.id.quantityInput);
        Button btnAdd = view.findViewById(R.id.btnAdd);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // Set up category spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                CATEGORIES
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Set up click listeners
        btnAdd.setOnClickListener(v -> addItem());
        btnCancel.setOnClickListener(v -> dismiss());

        // Set custom view, title, and buttons
        builder.setView(view);
        builder.setTitle("Adaugă un produs");

        return builder.create();
    }

    /**
     * Add the item to the shopping list
     */
    private void addItem() {
        // Validate input
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            nameInput.setError("Introduceți numele produsului");
            return;
        }

        // Get category and quantity
        String category = CATEGORIES[categorySpinner.getSelectedItemPosition()];
        String quantity = quantityInput.getText().toString().trim();

        // Notify listener
        if (listener != null) {
            listener.onItemAdded(name, category, quantity);
        }

        // Close dialog
        dismiss();
    }
} 