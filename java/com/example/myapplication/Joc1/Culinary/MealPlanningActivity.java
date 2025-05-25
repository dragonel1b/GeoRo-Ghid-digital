package com.example.myapplication.Joc1.Culinary;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.Joc1.Culinary.RecipeSelectionActivity;
import com.example.myapplication.Joc1.Culinary.ShoppingListActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.myapplication.Joc1.Culinary.MealPlanDBHelper;
import com.example.myapplication.Joc1.Culinary.RecipeDBHelper;
import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity;

public class MealPlanningActivity extends AppCompatActivity {

    private RecyclerView weekRecyclerView;
    private TextView currentMonthText;
    private MaterialButton previousWeekButton;
    private MaterialButton nextWeekButton;
    private FloatingActionButton generateShoppingListButton;
    
    private WeeklyPlanAdapter weeklyAdapter;
    private Calendar currentCalendar;
    private List<DayPlan> weekPlan;
    private MealPlanDBHelper dbHelper;
    private RecipeDBHelper recipeDBHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planning);
        
        // Initialize database helpers
        dbHelper = new MealPlanDBHelper(this);
        recipeDBHelper = new RecipeDBHelper(this);
        
        // Initialize views
        setupViews();
        setupToolbar();
        
        // Initialize calendar to current week
        currentCalendar = Calendar.getInstance();
        currentCalendar.set(Calendar.DAY_OF_WEEK, currentCalendar.getFirstDayOfWeek());
        
        // Load current week's meal plan
        loadWeekPlan();
        
        // Setup listeners
        setupNavButtons();
        setupShoppingListGeneration();
    }
    
    private void setupViews() {
        weekRecyclerView = findViewById(R.id.weekRecyclerView);
        currentMonthText = findViewById(R.id.currentMonthText);
        previousWeekButton = findViewById(R.id.previousWeekButton);
        nextWeekButton = findViewById(R.id.nextWeekButton);
        generateShoppingListButton = findViewById(R.id.generateShoppingListButton);
        
        // Setup RecyclerView
        weekRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.meal_planning_title);
        }
    }
    
    private void loadWeekPlan() {
        weekPlan = new ArrayList<>();
        
        // Format for display
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        SimpleDateFormat dayMonthFormat = new SimpleDateFormat("EEE, d MMM", Locale.getDefault());
        
        // Set month/year text
        currentMonthText.setText(monthYearFormat.format(currentCalendar.getTime()));
        
        // Create 7 days of the week starting from current week's start
        Calendar dayCal = (Calendar) currentCalendar.clone();
        
        for (int i = 0; i < 7; i++) {
            Date date = dayCal.getTime();
            String dateStr = dayMonthFormat.format(date);
            
            // Format date as yyyy-MM-dd for database lookup
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dbDateStr = dbFormat.format(date);
            
            // Create a day plan and load meals from database
            DayPlan dayPlan = new DayPlan(dateStr, dbDateStr);
            
            // Load breakfast, lunch, dinner from database
            dayPlan.setBreakfast(dbHelper.getMealForDateAndType(dbDateStr, MealType.BREAKFAST));
            dayPlan.setLunch(dbHelper.getMealForDateAndType(dbDateStr, MealType.LUNCH));
            dayPlan.setDinner(dbHelper.getMealForDateAndType(dbDateStr, MealType.DINNER));
            
            weekPlan.add(dayPlan);
            
            // Move to next day
            dayCal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // Create adapter if needed or update existing
        if (weeklyAdapter == null) {
            weeklyAdapter = new WeeklyPlanAdapter(weekPlan, this::handleMealSelection);
            weekRecyclerView.setAdapter(weeklyAdapter);
        } else {
            weeklyAdapter.updateData(weekPlan);
        }
    }
    
    private void setupNavButtons() {
        previousWeekButton.setOnClickListener(v -> {
            // Move to previous week
            currentCalendar.add(Calendar.WEEK_OF_YEAR, -1);
            loadWeekPlan();
        });
        
        nextWeekButton.setOnClickListener(v -> {
            // Move to next week
            currentCalendar.add(Calendar.WEEK_OF_YEAR, 1);
            loadWeekPlan();
        });
    }
    
    private void setupShoppingListGeneration() {
        generateShoppingListButton.setOnClickListener(v -> {
            // Count planned meals
            int plannedMeals = 0;
            for (DayPlan day : weekPlan) {
                if (day.getBreakfast() != null) plannedMeals++;
                if (day.getLunch() != null) plannedMeals++;
                if (day.getDinner() != null) plannedMeals++;
            }
            
            if (plannedMeals == 0) {
                Toast.makeText(this, R.string.no_meals_planned, Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Show confirmation dialog
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.generate_shopping_list)
                    .setMessage(getString(R.string.generate_shopping_list_confirmation, plannedMeals))
                    .setPositiveButton(R.string.generate, (dialog, which) -> generateShoppingList())
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
    }
    
    private void generateShoppingList() {
        // Collect all ingredients from planned meals
        Map<String, String> allIngredients = new HashMap<>();
        List<String> recipeNames = new ArrayList<>();
        
        for (DayPlan day : weekPlan) {
            collectIngredientsFromMeal(day.getBreakfast(), allIngredients, recipeNames);
            collectIngredientsFromMeal(day.getLunch(), allIngredients, recipeNames);
            collectIngredientsFromMeal(day.getDinner(), allIngredients, recipeNames);
        }
        
        if (allIngredients.isEmpty()) {
            Toast.makeText(this, R.string.no_ingredients_found, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Convert to array for intent
        String[] ingredientArray = allIngredients.keySet().toArray(new String[0]);
        String[] recipeNamesArray = recipeNames.toArray(new String[0]);
        
        // Launch ShoppingListActivity with ingredients
        Intent intent = new Intent(this, ShoppingListActivity.class);
        intent.putExtra("recipe_ingredients", ingredientArray);
        intent.putExtra("recipe_sources", recipeNamesArray);
        intent.putExtra("from_meal_plan", true);
        startActivity(intent);
        
        Toast.makeText(this, R.string.shopping_list_generated, Toast.LENGTH_SHORT).show();
    }
    
    private void collectIngredientsFromMeal(PlannedMeal meal, Map<String, String> ingredients, List<String> recipeNames) {
        if (meal == null || meal.getRecipeId() <= 0) return;
        
        // Get recipe from database
        ModernCulinaryActivity.Recipe recipe = recipeDBHelper.getModernRecipeById(meal.getRecipeId());
        if (recipe == null) return;
        
        // Add recipe name to list
        recipeNames.add(recipe.getTitle());
        
        // Add all ingredients with recipe name as source
        for (String ingredient : recipe.getIngredients()) {
            ingredients.put(ingredient, recipe.getTitle());
        }
    }
    
    private void handleMealSelection(String dateStr, MealType mealType) {
        // Show recipe selection dialog
        Intent intent = new Intent(this, RecipeSelectionActivity.class);
        intent.putExtra("date", dateStr);
        intent.putExtra("meal_type", mealType.toString());
        startActivityForResult(intent, REQUEST_SELECT_RECIPE);
    }
    
    private static final int REQUEST_SELECT_RECIPE = 101;
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_SELECT_RECIPE && resultCode == RESULT_OK && data != null) {
            String date = data.getStringExtra("date");
            String mealTypeStr = data.getStringExtra("meal_type");
            long recipeId = data.getLongExtra("recipe_id", -1);
            String recipeTitle = data.getStringExtra("recipe_title");
            
            if (date != null && mealTypeStr != null && recipeId > 0) {
                // Save to database
                MealType mealType = MealType.valueOf(mealTypeStr);
                PlannedMeal meal = new PlannedMeal(date, mealType, recipeId, recipeTitle);
                dbHelper.savePlannedMeal(meal);
                
                // Refresh the view
                loadWeekPlan();
                
                Toast.makeText(this, getString(R.string.meal_planned_success, recipeTitle), Toast.LENGTH_SHORT).show();
            }
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
    
    /**
     * Model class for a meal item in the meal planner
     */
    public static class MealItem {
        private long id;
        private ModernCulinaryActivity.Recipe recipe;
        private String mealType;
        private Date date;
        private int portions;
        private String notes;
        
        public MealItem(long id, ModernCulinaryActivity.Recipe recipe, String mealType, 
                        Date date, int portions) {
            this.id = id;
            this.recipe = recipe;
            this.mealType = mealType;
            this.date = date;
            this.portions = portions;
        }
        
        public MealItem(long id, ModernCulinaryActivity.Recipe recipe, String mealType, 
                        Date date, int portions, String notes) {
            this.id = id;
            this.recipe = recipe;
            this.mealType = mealType;
            this.date = date;
            this.portions = portions;
            this.notes = notes;
        }
        
        public long getId() {
            return id;
        }
        
        public void setId(long id) {
            this.id = id;
        }
        
        public ModernCulinaryActivity.Recipe getRecipe() {
            return recipe;
        }
        
        public void setRecipe(ModernCulinaryActivity.Recipe recipe) {
            this.recipe = recipe;
        }
        
        public String getMealType() {
            return mealType;
        }
        
        public void setMealType(String mealType) {
            this.mealType = mealType;
        }
        
        public Date getDate() {
            return date;
        }
        
        public void setDate(Date date) {
            this.date = date;
        }
        
        public int getPortions() {
            return portions;
        }
        
        public void setPortions(int portions) {
            this.portions = portions;
        }
        
        public String getNotes() {
            return notes;
        }
        
        public void setNotes(String notes) {
            this.notes = notes;
        }
        
        public long getRecipeId() {
            return recipe != null ? recipe.getId() : 0;
        }
        
        public String getFormattedDate() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return dateFormat.format(date);
        }
    }
    
    /**
     * Enum for meal types
     */
    public enum MealType {
        BREAKFAST("Mic dejun"),
        LUNCH("Prânz"),
        DINNER("Cină"),
        SNACK("Gustare");
        
        private final String displayName;
        
        MealType(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    /**
     * Class for a planned meal
     */
    public static class PlannedMeal {
        private String date;
        private MealType mealType;
        private long recipeId;
        private String recipeTitle;
        
        public PlannedMeal(String date, MealType mealType, long recipeId, String recipeTitle) {
            this.date = date;
            this.mealType = mealType;
            this.recipeId = recipeId;
            this.recipeTitle = recipeTitle;
        }
        
        public String getDate() {
            return date;
        }
        
        public MealType getMealType() {
            return mealType;
        }
        
        public long getRecipeId() {
            return recipeId;
        }
        
        public String getRecipeTitle() {
            return recipeTitle;
        }
    }
    
    public static class DayPlan {
        private final String displayDate;
        private final String dbDate;
        private PlannedMeal breakfast;
        private PlannedMeal lunch;
        private PlannedMeal dinner;
        
        public DayPlan(String displayDate, String dbDate) {
            this.displayDate = displayDate;
            this.dbDate = dbDate;
        }
        
        public String getDisplayDate() { return displayDate; }
        public String getDbDate() { return dbDate; }
        
        public PlannedMeal getBreakfast() { return breakfast; }
        public void setBreakfast(PlannedMeal breakfast) { this.breakfast = breakfast; }
        
        public PlannedMeal getLunch() { return lunch; }
        public void setLunch(PlannedMeal lunch) { this.lunch = lunch; }
        
        public PlannedMeal getDinner() { return dinner; }
        public void setDinner(PlannedMeal dinner) { this.dinner = dinner; }
    }
    
    public class WeeklyPlanAdapter extends RecyclerView.Adapter<WeeklyPlanAdapter.DayViewHolder> {
        
        private List<DayPlan> weekPlan;
        private final MealSelectionListener mealSelectionListener;
        
        public WeeklyPlanAdapter(List<DayPlan> weekPlan, MealSelectionListener listener) {
            this.weekPlan = weekPlan;
            this.mealSelectionListener = listener;
        }
        
        public void updateData(List<DayPlan> newWeekPlan) {
            this.weekPlan = newWeekPlan;
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public DayViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_meal_plan_day, parent, false);
            return new DayViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
            DayPlan dayPlan = weekPlan.get(position);
            holder.bind(dayPlan);
        }
        
        @Override
        public int getItemCount() {
            return weekPlan.size();
        }
        
        class DayViewHolder extends RecyclerView.ViewHolder {
            private final TextView dayDateText;
            private final TextView breakfastText;
            private final TextView lunchText;
            private final TextView dinnerText;
            private final MaterialButton addBreakfastButton;
            private final MaterialButton addLunchButton;
            private final MaterialButton addDinnerButton;
            
            public DayViewHolder(@NonNull View itemView) {
                super(itemView);
                dayDateText = itemView.findViewById(R.id.dayDateText);
                breakfastText = itemView.findViewById(R.id.breakfastText);
                lunchText = itemView.findViewById(R.id.lunchText);
                dinnerText = itemView.findViewById(R.id.dinnerText);
                addBreakfastButton = itemView.findViewById(R.id.addBreakfastButton);
                addLunchButton = itemView.findViewById(R.id.addLunchButton);
                addDinnerButton = itemView.findViewById(R.id.addDinnerButton);
            }
            
            public void bind(DayPlan dayPlan) {
                dayDateText.setText(dayPlan.getDisplayDate());
                
                // Setup breakfast
                if (dayPlan.getBreakfast() != null) {
                    breakfastText.setText(dayPlan.getBreakfast().getRecipeTitle());
                    breakfastText.setVisibility(View.VISIBLE);
                    addBreakfastButton.setText(R.string.change);
                } else {
                    breakfastText.setVisibility(View.GONE);
                    addBreakfastButton.setText(R.string.add);
                }
                
                // Setup lunch
                if (dayPlan.getLunch() != null) {
                    lunchText.setText(dayPlan.getLunch().getRecipeTitle());
                    lunchText.setVisibility(View.VISIBLE);
                    addLunchButton.setText(R.string.change);
                } else {
                    lunchText.setVisibility(View.GONE);
                    addLunchButton.setText(R.string.add);
                }
                
                // Setup dinner
                if (dayPlan.getDinner() != null) {
                    dinnerText.setText(dayPlan.getDinner().getRecipeTitle());
                    dinnerText.setVisibility(View.VISIBLE);
                    addDinnerButton.setText(R.string.change);
                } else {
                    dinnerText.setVisibility(View.GONE);
                    addDinnerButton.setText(R.string.add);
                }
                
                // Set click listeners
                addBreakfastButton.setOnClickListener(v -> 
                        mealSelectionListener.onMealSelected(dayPlan.getDbDate(), MealType.BREAKFAST));
                
                addLunchButton.setOnClickListener(v -> 
                        mealSelectionListener.onMealSelected(dayPlan.getDbDate(), MealType.LUNCH));
                
                addDinnerButton.setOnClickListener(v -> 
                        mealSelectionListener.onMealSelected(dayPlan.getDbDate(), MealType.DINNER));
                
                // Allow clicking on text to change meal too
                breakfastText.setOnClickListener(v -> 
                        mealSelectionListener.onMealSelected(dayPlan.getDbDate(), MealType.BREAKFAST));
                
                lunchText.setOnClickListener(v -> 
                        mealSelectionListener.onMealSelected(dayPlan.getDbDate(), MealType.LUNCH));
                
                dinnerText.setOnClickListener(v -> 
                        mealSelectionListener.onMealSelected(dayPlan.getDbDate(), MealType.DINNER));
            }
        }
    }
    
    public interface MealSelectionListener {
        void onMealSelected(String date, MealType mealType);
    }
}
