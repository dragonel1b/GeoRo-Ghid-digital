package com.example.myapplication.Joc1.Culinary;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.R;
import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity.Recipe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Dialog fragment for selecting meals in the meal planner
 */
public class MealSelectionDialogFragment extends DialogFragment {
    
    private static final String ARG_DATE = "date";
    private static final String ARG_MEAL_TYPE = "meal_type";
    
    private Date date;
    private String mealType;
    private OnMealAddedListener listener;
    
    public interface OnMealAddedListener {
        void onMealAdded(String recipeTitle, String recipeRegion, Date date, String mealType);
    }
    
    public static MealSelectionDialogFragment newInstance(Date date, String mealType) {
        MealSelectionDialogFragment fragment = new MealSelectionDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        args.putString(ARG_MEAL_TYPE, mealType);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            date = (Date) getArguments().getSerializable(ARG_DATE);
            mealType = getArguments().getString(ARG_MEAL_TYPE);
        }
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        
        // Verify that the host implements the callback interface
        try {
            // First try to cast the parent fragment
            if (getParentFragment() instanceof OnMealAddedListener) {
                listener = (OnMealAddedListener) getParentFragment();
            } else {
                // If that fails, try the activity
                listener = (OnMealAddedListener) context;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException("Host must implement OnMealAddedListener");
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_select_meal, null);
        
        // Get the list of recipes
        List<Recipe> recipes = ModernCulinaryActivity.getRecipes();
        List<String> recipeNames = new ArrayList<>();
        
        for (Recipe recipe : recipes) {
            recipeNames.add(recipe.getTitle());
        }
        
        // Set up the ListView
        ListView listView = view.findViewById(R.id.recipesListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                recipeNames);
        
        listView.setAdapter(adapter);
        
        // Set up the title
        String title = "Selectează o rețetă pentru " + getMealTypeDisplay(mealType);
        TextView titleView = view.findViewById(R.id.dialogTitle);
        titleView.setText(title);
        
        // Set up the ListView click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Recipe selectedRecipe = recipes.get(position);
                
                // Notify the listener
                if (listener != null) {
                    listener.onMealAdded(
                            selectedRecipe.getTitle(),
                            selectedRecipe.getRegion(),
                            date,
                            mealType
                    );
                }
                
                // Dismiss the dialog
                dismiss();
            }
        });
        
        // Build the dialog
        builder.setView(view)
               .setNegativeButton("Anulează", null);
        
        return builder.create();
    }
    
    private String getMealTypeDisplay(String mealType) {
        switch (mealType) {
            case "breakfast":
                return "Mic dejun";
            case "lunch":
                return "Prânz";
            case "dinner":
                return "Cină";
            case "snack":
                return "Gustare";
            default:
                return mealType;
        }
    }
} 