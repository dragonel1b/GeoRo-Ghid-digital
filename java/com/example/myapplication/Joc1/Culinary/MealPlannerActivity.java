package com.example.myapplication.Joc1.Culinary;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity for meal planning, shopping list generation, and nutrition tracking
 * Implementează interfața Material Design 3 și oferă funcționalități avansate
 * de planificare a meselor, sugestii personalizate și statistici nutriționale.
 */
public class MealPlannerActivity extends AppCompatActivity {
    
    // UI Components
    private Toolbar toolbar;
    private TabLayout weekDaysTabs;
    private RecyclerView weeklyPlanRecycler;
    private TextView emptyPlanMessage;
    private ViewPager2 suggestionsCarousel;
    private TabLayout carouselIndicator;
    private ChipGroup nutritionStatsChips;
    private LineChart nutritionChart;
    private TextView noStatsMessage;
    private ExtendedFloatingActionButton fabAddMeal;
    private NestedScrollView mainScrollView;
    
    // Data
    private String[] daysOfWeek = {"Luni", "Marți", "Miercuri", "Joi", "Vineri", "Sâmbătă", "Duminică"};
    private int currentDayIndex;
    private List<MealItem> meals = new ArrayList<>();
    private List<RecipeItem> suggestedRecipes = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planner);
        
        initializeViews();
        setupToolbar();
        setupWeekDaysTabs();
        setupMealsList();
        setupSuggestionsCarousel();
        setupNutritionChart();
        setupFab();
        
        // Încarcă datele inițiale
        loadMealsForCurrentDay();
        loadSuggestedRecipes();
        updateNutritionChart();
    }
    
    private void initializeViews() {
        // Main UI elements
        toolbar = findViewById(R.id.toolbar);
        mainScrollView = findViewById(R.id.mainScrollView);
        fabAddMeal = findViewById(R.id.fabAddMeal);
        
        // Weekly planning section
        weekDaysTabs = findViewById(R.id.weekDaysTabs);
        weeklyPlanRecycler = findViewById(R.id.weeklyPlanRecycler);
        emptyPlanMessage = findViewById(R.id.emptyPlanMessage);
        
        // Suggestions carousel
        suggestionsCarousel = findViewById(R.id.suggestionsCarousel);
        carouselIndicator = findViewById(R.id.carouselIndicator);
        
        // Nutrition stats
        nutritionStatsChips = findViewById(R.id.nutritionStatsChips);
        nutritionChart = findViewById(R.id.nutritionChart);
        noStatsMessage = findViewById(R.id.noStatsMessage);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupWeekDaysTabs() {
        // Determină ziua curentă (1 = Luni, 7 = Duminică)
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // Convertește la indexul nostru (0 = Luni, 6 = Duminică)
        currentDayIndex = dayOfWeek == Calendar.SUNDAY ? 6 : dayOfWeek - 2;
        
        // Adaugă taburi pentru fiecare zi a săptămânii
        for (String day : daysOfWeek) {
            weekDaysTabs.addTab(weekDaysTabs.newTab().setText(day));
        }
        
        // Selectează ziua curentă
        if (currentDayIndex >= 0 && currentDayIndex < weekDaysTabs.getTabCount()) {
            weekDaysTabs.selectTab(weekDaysTabs.getTabAt(currentDayIndex));
        }
        
        // Listener pentru schimbarea zilei
        weekDaysTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentDayIndex = tab.getPosition();
                loadMealsForCurrentDay();
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }
    
    private void setupMealsList() {
        // Configurează RecyclerView pentru lista de mese
        weeklyPlanRecycler.setLayoutManager(new LinearLayoutManager(this));
        weeklyPlanRecycler.setNestedScrollingEnabled(false);
        
        // Adapter-ul va fi setat când încărcăm mesele
    }
    
    private void setupSuggestionsCarousel() {
        // Configurează PageTransformer pentru efect de carousel
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        transformer.addTransformer((page, position) -> {
            float absPos = Math.abs(position);
            page.setScaleY(0.85f + (1 - absPos) * 0.15f);
            page.setAlpha(0.5f + (1 - absPos) * 0.5f);
        });
        
        suggestionsCarousel.setPageTransformer(transformer);
        
        // Setăm loop infinit și orientare orizontală
        suggestionsCarousel.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        suggestionsCarousel.setOffscreenPageLimit(3);
        
        // Conectăm cu indicatorul
        suggestionsCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Actualizăm indicatorul
                if (carouselIndicator.getTabCount() > 0 && position < carouselIndicator.getTabCount()) {
                    carouselIndicator.selectTab(carouselIndicator.getTabAt(position));
                }
            }
        });
    }
    
    private void setupNutritionChart() {
        // Configurare ChipGroup pentru selectarea tipului de nutrient
        nutritionStatsChips.setOnCheckedChangeListener((group, checkedId) -> {
            updateNutritionChart();
        });
        
        // Configurare LineChart
        nutritionChart.setDescription(null);
        nutritionChart.getLegend().setEnabled(false);
        nutritionChart.setDrawGridBackground(false);
        nutritionChart.setTouchEnabled(true);
        nutritionChart.setDragEnabled(true);
        nutritionChart.setScaleEnabled(true);
        nutritionChart.setPinchZoom(true);
        nutritionChart.setExtraOffsets(10, 10, 10, 10);
        
        // Configurare axe
        XAxis xAxis = nutritionChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());
            
            @Override
            public String getFormattedValue(float value) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, (int) value);
                return dateFormat.format(calendar.getTime());
            }
        });
        
        YAxis leftAxis = nutritionChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        
        nutritionChart.getAxisRight().setEnabled(false);
        
        // Adăugăm listener pentru interacțiuni
        nutritionChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String nutrientType = "nutrient";
                if (nutritionStatsChips.getCheckedChipId() == R.id.caloriesChip) {
                    nutrientType = "calorii";
                } else if (nutritionStatsChips.getCheckedChipId() == R.id.proteinsChip) {
                    nutrientType = "proteine";
                } else if (nutritionStatsChips.getCheckedChipId() == R.id.carbsChip) {
                    nutrientType = "carbohidrați";
                } else if (nutritionStatsChips.getCheckedChipId() == R.id.fatsChip) {
                    nutrientType = "grăsimi";
                } else if (nutritionStatsChips.getCheckedChipId() == R.id.vitaminsChip) {
                    nutrientType = "vitamine";
                }
                
                Snackbar.make(nutritionChart, 
                        String.format(Locale.getDefault(), "Ziua %.0f: %.1f %s", 
                                e.getX(), e.getY(), nutrientType), 
                        Snackbar.LENGTH_SHORT).show();
            }
            
            @Override
            public void onNothingSelected() {}
        });
    }
    
    private void setupFab() {
        // Comportamentul FAB la scroll
        mainScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY + 20) {
                    // Scroll down - shrink FAB to icon only
                    fabAddMeal.shrink();
                } else if (scrollY < oldScrollY - 20) {
                    // Scroll up - extend FAB to show text
                    fabAddMeal.extend();
                }
            }
        });
        
        // Click listener pentru FAB
        fabAddMeal.setOnClickListener(v -> {
            showAddMealDialog();
        });
    }
    
    private void loadMealsForCurrentDay() {
        // TODO: În implementarea reală, datele ar fi încărcate din baza de date
        // Aici simulăm câteva mese pentru ziua curentă
        
        meals.clear();
        
        // Variază numărul de mese în funcție de zi pentru a demonstra UI-ul
        if (currentDayIndex % 2 == 0) {
            meals.add(new MealItem("Mic dejun", "Ouă ochiuri cu spanac și roșii", "08:00", 350));
            meals.add(new MealItem("Prânz", "Supă de legume cu piept de pui", "13:00", 450));
            meals.add(new MealItem("Cină", "Somon la cuptor cu sparanghel", "19:00", 380));
        } else {
            meals.add(new MealItem("Brunch", "Omletă cu legume și brânză", "10:30", 420));
            meals.add(new MealItem("Cină", "Salată cu quinoa și avocado", "18:30", 350));
        }
        
        // Actualizăm UI-ul
        if (meals.isEmpty()) {
            weeklyPlanRecycler.setVisibility(View.GONE);
            emptyPlanMessage.setVisibility(View.VISIBLE);
        } else {
            MealAdapter adapter = new MealAdapter(meals);
            weeklyPlanRecycler.setAdapter(adapter);
            weeklyPlanRecycler.setVisibility(View.VISIBLE);
            emptyPlanMessage.setVisibility(View.GONE);
        }
    }
    
    private void loadSuggestedRecipes() {
        // TODO: În implementarea reală, sugestiile ar veni din algoritm de recomandare
        // Simulăm câteva rețete sugerate
        suggestedRecipes.clear();
        suggestedRecipes.add(new RecipeItem("Sarmale moldovenești", "Tradițional", 420, R.drawable.ic_menu_camera));
        suggestedRecipes.add(new RecipeItem("Papanași cu smântână", "Desert", 380, R.drawable.ic_menu_camera));
        suggestedRecipes.add(new RecipeItem("Ciorbă de perișoare", "Supă", 280, R.drawable.ic_menu_camera));
        suggestedRecipes.add(new RecipeItem("Mămăligă cu brânză", "Tradițional", 350, R.drawable.ic_menu_camera));
        
        // Actualizăm carouselul
        RecipeCarouselAdapter adapter = new RecipeCarouselAdapter(suggestedRecipes);
        suggestionsCarousel.setAdapter(adapter);
        
        // Actualizăm indicatorul
        if (carouselIndicator.getTabCount() != suggestedRecipes.size()) {
            carouselIndicator.removeAllTabs();
            for (int i = 0; i < suggestedRecipes.size(); i++) {
                carouselIndicator.addTab(carouselIndicator.newTab());
            }
        }
    }
    
    private void updateNutritionChart() {
        // Generează date pentru grafic în funcție de chipul selectat
        List<Entry> entries = new ArrayList<>();
        
        // Determină tipul de nutrient selectat
        String nutrientType;
        int color;
        
        if (nutritionStatsChips.getCheckedChipId() == R.id.caloriesChip) {
            nutrientType = "Calorii";
            color = Color.RED;
            // Simulează date pentru ultimele 7 zile
            entries.add(new Entry(-6, 1840));
            entries.add(new Entry(-5, 2100));
            entries.add(new Entry(-4, 1950));
            entries.add(new Entry(-3, 2210));
            entries.add(new Entry(-2, 1760));
            entries.add(new Entry(-1, 1920));
            entries.add(new Entry(0, 1880));
        } else if (nutritionStatsChips.getCheckedChipId() == R.id.proteinsChip) {
            nutrientType = "Proteine";
            color = Color.BLUE;
            entries.add(new Entry(-6, 75));
            entries.add(new Entry(-5, 82));
            entries.add(new Entry(-4, 68));
            entries.add(new Entry(-3, 90));
            entries.add(new Entry(-2, 72));
            entries.add(new Entry(-1, 78));
            entries.add(new Entry(0, 85));
        } else if (nutritionStatsChips.getCheckedChipId() == R.id.carbsChip) {
            nutrientType = "Carbohidrați";
            color = Color.GREEN;
            entries.add(new Entry(-6, 210));
            entries.add(new Entry(-5, 240));
            entries.add(new Entry(-4, 220));
            entries.add(new Entry(-3, 260));
            entries.add(new Entry(-2, 200));
            entries.add(new Entry(-1, 230));
            entries.add(new Entry(0, 215));
        } else if (nutritionStatsChips.getCheckedChipId() == R.id.fatsChip) {
            nutrientType = "Grăsimi";
            color = Color.YELLOW;
            entries.add(new Entry(-6, 65));
            entries.add(new Entry(-5, 72));
            entries.add(new Entry(-4, 58));
            entries.add(new Entry(-3, 80));
            entries.add(new Entry(-2, 62));
            entries.add(new Entry(-1, 68));
            entries.add(new Entry(0, 70));
        } else {
            nutrientType = "Vitamine";
            color = Color.MAGENTA;
            entries.add(new Entry(-6, 85));
            entries.add(new Entry(-5, 90));
            entries.add(new Entry(-4, 92));
            entries.add(new Entry(-3, 88));
            entries.add(new Entry(-2, 95));
            entries.add(new Entry(-1, 91));
            entries.add(new Entry(0, 93));
        }
        
        // Verifică dacă avem date
        if (entries.isEmpty()) {
            nutritionChart.setVisibility(View.GONE);
            noStatsMessage.setVisibility(View.VISIBLE);
            return;
        }
        
        nutritionChart.setVisibility(View.VISIBLE);
        noStatsMessage.setVisibility(View.GONE);
        
        // Creează setul de date
        LineDataSet dataSet = new LineDataSet(entries, nutrientType);
        dataSet.setColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(color);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(50);
        dataSet.setFillColor(color);
        dataSet.setHighlightEnabled(true);
        dataSet.setDrawHorizontalHighlightIndicator(false);
        
        // Actualizează graficul
        LineData lineData = new LineData(dataSet);
        nutritionChart.setData(lineData);
        
        // Animează graficul
        nutritionChart.animateX(1000);
        nutritionChart.invalidate();
    }
    
    /**
     * Show dialog to add a meal to the planner
     */
    private void showAddMealDialog() {
        // TODO: Implementare dialog pentru adăugarea unei mese
        Snackbar.make(fabAddMeal, "Adaugă o masă nouă pentru " + daysOfWeek[currentDayIndex], 
                Snackbar.LENGTH_SHORT).show();
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
     * Clasa pentru elementele din lista de mese
     */
    private static class MealItem {
        String type;
        String description;
        String time;
        int calories;
        
        MealItem(String type, String description, String time, int calories) {
            this.type = type;
            this.description = description;
            this.time = time;
            this.calories = calories;
        }
    }
    
    /**
     * Clasa pentru elementele din carouselul de sugestii
     */
    private static class RecipeItem {
        String name;
        String category;
        int calories;
        int imageResId;
        
        RecipeItem(String name, String category, int calories, int imageResId) {
            this.name = name;
            this.category = category;
            this.calories = calories;
            this.imageResId = imageResId;
        }
    }
    
    /**
     * Adapter pentru lista de mese
     */
    private class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {
        private List<MealItem> mealItems;
        
        MealAdapter(List<MealItem> mealItems) {
            this.mealItems = mealItems;
        }
        
        @NonNull
        @Override
        public MealViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_meal_plan, parent, false);
            return new MealViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
            // TODO: Implementare reală a binding-ului datelor
            // În implementarea completă, acest cod ar face binding la viewHolder cu datele
            // specifice din mealItems.get(position)
        }
        
        @Override
        public int getItemCount() {
            return mealItems.size();
        }
        
        class MealViewHolder extends RecyclerView.ViewHolder {
            // TODO: Declarare referințe la elementele din layout
            
            MealViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO: Inițializare elemente UI din layout
            }
        }
    }
    
    /**
     * Adapter pentru carouselul de sugestii
     */
    private class RecipeCarouselAdapter extends RecyclerView.Adapter<RecipeCarouselAdapter.RecipeViewHolder> {
        private List<RecipeItem> recipeItems;
        
        RecipeCarouselAdapter(List<RecipeItem> recipeItems) {
            this.recipeItems = recipeItems;
        }
        
        @NonNull
        @Override
        public RecipeViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            // TODO: Implementare reală a creării viewHolder
            View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
            return new RecipeViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
            // TODO: Implementare reală a binding-ului datelor
            // În implementarea completă, acest cod ar face binding la viewHolder cu datele
            // specifice din recipeItems.get(position)
        }
        
        @Override
        public int getItemCount() {
            return recipeItems.size();
        }
        
        class RecipeViewHolder extends RecyclerView.ViewHolder {
            // TODO: Declarare referințe la elementele din layout
            
            RecipeViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO: Inițializare elemente UI din layout
            }
        }
    }
} 