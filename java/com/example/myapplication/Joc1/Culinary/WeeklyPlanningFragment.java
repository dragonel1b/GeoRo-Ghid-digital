package com.example.myapplication.Joc1.Culinary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment for displaying and managing weekly meal plans
 */
public class WeeklyPlanningFragment extends Fragment {

    private static final String[] DAYS_OF_WEEK = {"Luni", "Marți", "Miercuri", "Joi", "Vineri", "Sâmbătă", "Duminică"};
    private static final String[] MEAL_TYPES = {"Mic dejun", "Prânz", "Cină"};
    
    private GridLayout mealGrid;
    private MealPlanDBHelper dbHelper;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new MealPlanDBHelper(requireContext());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Create a ScrollView as the root view
        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        
        // Create a LinearLayout to hold the title and grid
        LinearLayout rootLayout = new LinearLayout(requireContext());
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        rootLayout.setPadding(16, 16, 16, 16);
        
        // Add title
        TextView titleView = new TextView(requireContext());
        titleView.setText("Planificare săptămânală");
        titleView.setTextSize(20);
        titleView.setPadding(0, 0, 0, 16);
        rootLayout.addView(titleView);
        
        // Create grid for meal planning
        mealGrid = new GridLayout(requireContext());
        mealGrid.setColumnCount(4); // Day + 3 meal types
        mealGrid.setUseDefaultMargins(true);
        
        // Add header row
        addHeaderRow(mealGrid);
        
        // Add day rows
        for (String day : DAYS_OF_WEEK) {
            addDayRow(mealGrid, day);
        }
        
        // Add a button to clear the plan
        MaterialButton clearButton = new MaterialButton(requireContext());
        clearButton.setText("Resetează planul");
        clearButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Planul a fost resetat", Toast.LENGTH_SHORT).show();
            // In a real implementation, clear the meal plan from database
        });
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonParams.topMargin = 32;
        clearButton.setLayoutParams(buttonParams);
        
        // Add views to hierarchy
        rootLayout.addView(mealGrid);
        rootLayout.addView(clearButton);
        scrollView.addView(rootLayout);
        
        return scrollView;
    }
    
    private void addHeaderRow(GridLayout grid) {
        // Add empty cell for top-left corner
        TextView emptyCell = new TextView(requireContext());
        emptyCell.setText("");
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.rowSpec = GridLayout.spec(0, 1, 1f);
        params.columnSpec = GridLayout.spec(0, 1, 1f);
        emptyCell.setLayoutParams(params);
        grid.addView(emptyCell);
        
        // Add meal type headers
        for (int i = 0; i < MEAL_TYPES.length; i++) {
            TextView header = new TextView(requireContext());
            header.setText(MEAL_TYPES[i]);
            header.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            header.setPadding(8, 8, 8, 8);
            
            params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(0, 1, 1f);
            params.columnSpec = GridLayout.spec(i + 1, 1, 1f);
            header.setLayoutParams(params);
            
            grid.addView(header);
        }
    }
    
    private void addDayRow(GridLayout grid, String day) {
        int rowIndex = grid.getRowCount();
        
        // Add day label
        TextView dayLabel = new TextView(requireContext());
        dayLabel.setText(day);
        dayLabel.setPadding(8, 8, 8, 8);
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.rowSpec = GridLayout.spec(rowIndex, 1, 1f);
        params.columnSpec = GridLayout.spec(0, 1, 1f);
        dayLabel.setLayoutParams(params);
        
        grid.addView(dayLabel);
        
        // Add meal cells
        for (int i = 0; i < MEAL_TYPES.length; i++) {
            CardView mealCard = createMealCell(day, MEAL_TYPES[i]);
            
            params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(rowIndex, 1, 1f);
            params.columnSpec = GridLayout.spec(i + 1, 1, 1f);
            params.setMargins(4, 4, 4, 4);
            mealCard.setLayoutParams(params);
            
            grid.addView(mealCard);
        }
    }
    
    private CardView createMealCell(String day, String mealType) {
        CardView card = new CardView(requireContext());
        card.setCardElevation(2);
        card.setRadius(8);
        
        TextView mealText = new TextView(requireContext());
        mealText.setText("Adaugă");
        mealText.setPadding(16, 16, 16, 16);
        
        card.addView(mealText);
        
        // Add click listener
        card.setOnClickListener(v -> {
            Toast.makeText(requireContext(), 
                    "Adaugă rețetă pentru " + mealType + " în " + day, 
                    Toast.LENGTH_SHORT).show();
            // In a real implementation, open recipe selection
        });
        
        return card;
        }
        
        @Override
    public void onResume() {
        super.onResume();
        // Refresh data from database
        // In a real implementation, load meal plan data
    }
} 