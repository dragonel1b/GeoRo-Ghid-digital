package com.example.myapplication.Joc1.Culinary;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Activity for meal planning, shopping list generation, and nutrition tracking
 * Implementează interfața Material Design 3 și oferă funcționalități avansate
 * de planificare a meselor, sugestii personalizate și statistici nutriționale.
 */
public class ModernMealPlannerActivity extends AppCompatActivity {

    // UI Components
    private MaterialToolbar toolbar;
    private TabLayout weekDaysTabs;
    private ViewPager2 daysPager;
    private TextView emptyStateText;
    private TextView emptyPlanMessage;
    private ViewPager2 suggestionsCarousel;
    private TabLayout carouselIndicator;
    private ChipGroup nutritionStatsChips;
    private LineChart nutritionChart;
    private TextView noStatsMessage;
    private ExtendedFloatingActionButton fabAddMeal;
    private NestedScrollView mainScrollView;
    private RecyclerView shoppingListRecycler;
    
    // Data
    private final String[] daysOfWeek = {"Luni", "Marți", "Miercuri", "Joi", "Vineri", "Sâmbătă", "Duminică"};
    private int currentDayIndex;
    private List<MealItem> meals = new ArrayList<>();
    private List<RecipeItem> suggestedRecipes = new ArrayList<>();
    private List<GroceryItem> groceryItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modern_meal_planner);
        
        initializeViews();
        setupToolbar();
        setupDayPager();
        setupSuggestionsCarousel();
        setupNutritionChart();
        setupShoppingList();
        setupFab();
        
        // Încarcă datele inițiale
        loadSuggestedRecipes();
        updateNutritionChart();
        checkEmptyState();
    }
    
    private void initializeViews() {
        // Main UI elements
        toolbar = findViewById(R.id.toolbar);
        mainScrollView = findViewById(R.id.mainScrollView);
        fabAddMeal = findViewById(R.id.fabAddMeal);
        emptyStateText = findViewById(R.id.emptyStateText);
        
        // Weekly planning section
        weekDaysTabs = findViewById(R.id.weekDaysTabs);
        daysPager = findViewById(R.id.daysPager);
        emptyPlanMessage = findViewById(R.id.emptyPlanMessage);
        
        // Suggestions carousel
        suggestionsCarousel = findViewById(R.id.suggestionsCarousel);
        carouselIndicator = findViewById(R.id.carouselIndicator);
        
        // Nutrition stats
        nutritionStatsChips = findViewById(R.id.nutritionStatsChips);
        nutritionChart = findViewById(R.id.nutritionChart);
        noStatsMessage = findViewById(R.id.noStatsMessage);
        
        // Shopping list
        shoppingListRecycler = findViewById(R.id.shoppingListRecycler);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }
    
    private void setupDayPager() {
        // Determină ziua curentă (1 = Luni, 7 = Duminică)
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // Convertește la indexul nostru (0 = Luni, 6 = Duminică)
        currentDayIndex = dayOfWeek == Calendar.SUNDAY ? 6 : dayOfWeek - 2;
        
        // Setup ViewPager2 with FragmentStateAdapter
        DayPagerAdapter pagerAdapter = new DayPagerAdapter(this);
        daysPager.setAdapter(pagerAdapter);
        
        // Conectăm ViewPager2 cu TabLayout folosind TabLayoutMediator
        new TabLayoutMediator(weekDaysTabs, daysPager,
                (tab, position) -> tab.setText(daysOfWeek[position]))
                .attach();
        
        // Selectează ziua curentă
        if (currentDayIndex >= 0 && currentDayIndex < daysOfWeek.length) {
            daysPager.setCurrentItem(currentDayIndex, false);
        }
        
        // Listener pentru schimbarea paginii
        daysPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentDayIndex = position;
                loadMealsForCurrentDay();
            }
        });
    }
    
    private void setupSuggestionsCarousel() {
        // Configurează PageTransformer pentru efect de carousel
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        transformer.addTransformer((page, position) -> {
            float absPos = Math.abs(position);
            // Scalarea și opacitatea variază bazat pe poziția relativă
            page.setScaleY(0.85f + (1 - absPos) * 0.15f);
            page.setScaleX(0.85f + (1 - absPos) * 0.15f);
            page.setAlpha(0.5f + (1 - absPos) * 0.5f);
            
            // Efect de suprapunere pentru a simula un carousel 3D
            if (position < 0) { 
                page.setTranslationX(position * page.getWidth() * 0.3f); 
            } else {
                page.setTranslationX(position * page.getWidth() * 0.3f);
            }
        });
        
        suggestionsCarousel.setPageTransformer(transformer);
        
        // Configurează carousel
        suggestionsCarousel.setClipToPadding(false);
        suggestionsCarousel.setClipChildren(false);
        suggestionsCarousel.setOffscreenPageLimit(3);
        suggestionsCarousel.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
        
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
        
        // Configurăm legenda
        Legend legend = nutritionChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(12f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        
        // Adăugăm listener pentru interacțiuni (pentru accesibilitate)
        nutritionChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String nutrientType = "nutrient";
                int chipId = nutritionStatsChips.getCheckedChipId();
                
                if (chipId == R.id.caloriesChip) {
                    nutrientType = "calorii";
                } else if (chipId == R.id.proteinsChip) {
                    nutrientType = "proteine";
                } else if (chipId == R.id.carbsChip) {
                    nutrientType = "carbohidrați";
                } else if (chipId == R.id.fatsChip) {
                    nutrientType = "grăsimi";
                } else if (chipId == R.id.vitaminsChip) {
                    nutrientType = "vitamine";
                }
                
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, (int) e.getX());
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
                
                String message = sdf.format(cal.getTime()) + ": " + 
                        String.format(Locale.getDefault(), "%.1f %s", e.getY(), nutrientType);
                
                Snackbar snackbar = Snackbar.make(nutritionChart, message, Snackbar.LENGTH_SHORT);
                snackbar.getView().setContentDescription("Informație grafic: " + message);
                snackbar.show();
            }
            
            @Override
            public void onNothingSelected() {}
        });
    }
    
    private void setupShoppingList() {
        shoppingListRecycler.setLayoutManager(new LinearLayoutManager(this));
        shoppingListRecycler.setNestedScrollingEnabled(false);
        loadGroceryItems();
        
        // Configurăm generarea listei de cumpărături
        findViewById(R.id.generateShoppingListButton).setOnClickListener(v -> {
            generateShoppingList();
        });
    }
    
    private void setupFab() {
        // Comportamentul FAB la scroll
        mainScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY + 20) {
                // Scroll down - shrink FAB to icon only
                fabAddMeal.shrink();
            } else if (scrollY < oldScrollY - 20) {
                // Scroll up - extend FAB to show text
                fabAddMeal.extend();
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
        } else if (currentDayIndex % 3 == 0) {
            meals.add(new MealItem("Brunch", "Omletă cu legume și brânză", "10:30", 420));
            meals.add(new MealItem("Cină", "Salată cu quinoa și avocado", "18:30", 350));
        } else if (currentDayIndex == 5 || currentDayIndex == 6) {
            // Weekend - mai multe mese
            meals.add(new MealItem("Mic dejun", "Clătite cu fructe de pădure", "09:00", 380));
            meals.add(new MealItem("Gustare", "Iaurt cu nuci și miere", "11:00", 180));
            meals.add(new MealItem("Prânz", "Ciorbă de perișoare", "14:00", 320));
            meals.add(new MealItem("Desert", "Înghețată de casă", "15:30", 250));
            meals.add(new MealItem("Cină", "Paste carbonara", "19:30", 520));
        }
        
        // Actualizăm starea ecranului gol
        checkEmptyState();
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
    
    private void loadGroceryItems() {
        // Simulăm articole de cumpărat bazate pe planurile de masă
        groceryItems.clear();
        groceryItems.add(new GroceryItem("Ouă", "12 buc", "Lactate"));
        groceryItems.add(new GroceryItem("Spanac", "200g", "Legume"));
        groceryItems.add(new GroceryItem("Roșii", "4 buc", "Legume"));
        groceryItems.add(new GroceryItem("Piept de pui", "500g", "Carne"));
        groceryItems.add(new GroceryItem("Somon", "400g", "Pește"));
        groceryItems.add(new GroceryItem("Sparanghel", "1 legătură", "Legume"));
        
        // Actualizăm lista
        GroceryAdapter adapter = new GroceryAdapter(groceryItems);
        shoppingListRecycler.setAdapter(adapter);
    }
    
    private void generateShoppingList() {
        // Simulăm regenerarea listei de cumpărături
        Snackbar.make(shoppingListRecycler, 
                "Lista de cumpărături actualizată pentru săptămâna curentă", 
                Snackbar.LENGTH_LONG).show();
        
        // În implementarea reală, aici s-ar extrage toate ingredientele 
        // necesare din planurile de masă ale săptămânii
        
        loadGroceryItems(); // Reîncarcă lista (sau o actualizează)
    }
    
    private void updateNutritionChart() {
        // Generează date pentru grafic în funcție de chipul selectat
        List<Entry> entries = new ArrayList<>();
        
        // Determină tipul de nutrient selectat
        String nutrientType;
        int color;
        
        int chipId = nutritionStatsChips.getCheckedChipId();
        
        if (chipId == R.id.caloriesChip) {
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
        } else if (chipId == R.id.proteinsChip) {
            nutrientType = "Proteine";
            color = Color.BLUE;
            entries.add(new Entry(-6, 75));
            entries.add(new Entry(-5, 82));
            entries.add(new Entry(-4, 68));
            entries.add(new Entry(-3, 90));
            entries.add(new Entry(-2, 72));
            entries.add(new Entry(-1, 78));
            entries.add(new Entry(0, 85));
        } else if (chipId == R.id.carbsChip) {
            nutrientType = "Carbohidrați";
            color = Color.GREEN;
            entries.add(new Entry(-6, 210));
            entries.add(new Entry(-5, 240));
            entries.add(new Entry(-4, 220));
            entries.add(new Entry(-3, 260));
            entries.add(new Entry(-2, 200));
            entries.add(new Entry(-1, 230));
            entries.add(new Entry(0, 215));
        } else if (chipId == R.id.fatsChip) {
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
    
    private void checkEmptyState() {
        boolean hasAnyMeals = false;
        
        // Verificăm dacă există mese planificate pentru oricare din zile
        for (int i = 0; i < daysOfWeek.length; i++) {
            // Aici ar trebui să verificăm datele reale din baza de date
            // Pentru simplificare, folosim meals pentru ziua curentă
            if (i == currentDayIndex && !meals.isEmpty()) {
                hasAnyMeals = true;
                break;
            }
        }
        
        // Afișăm/ascundem mesajul de stare goală
        emptyStateText.setVisibility(hasAnyMeals ? View.GONE : View.VISIBLE);
        
        // Verificăm dacă avem mese pentru ziua curentă
        if (meals.isEmpty()) {
            emptyPlanMessage.setVisibility(View.VISIBLE);
        } else {
            emptyPlanMessage.setVisibility(View.GONE);
        }
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
     * ViewPager2 adapter pentru zilele săptămânii
     */
    private class DayPagerAdapter extends FragmentStateAdapter {
        
        public DayPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }
        
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return DayPlanFragment.newInstance(position, daysOfWeek[position]);
        }
        
        @Override
        public int getItemCount() {
            return daysOfWeek.length;
        }
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
     * Clasa pentru elementele din lista de cumpărături
     */
    private static class GroceryItem {
        String name;
        String amount;
        String category;
        boolean checked;
        
        GroceryItem(String name, String amount, String category) {
            this.name = name;
            this.amount = amount;
            this.category = category;
            this.checked = false;
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
        public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_recipe_card, parent, false);
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
    
    /**
     * Adapter pentru lista de cumpărături
     */
    private class GroceryAdapter extends RecyclerView.Adapter<GroceryAdapter.GroceryViewHolder> {
        private List<GroceryItem> groceryItems;
        
        GroceryAdapter(List<GroceryItem> groceryItems) {
            this.groceryItems = groceryItems;
        }
        
        @NonNull
        @Override
        public GroceryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_grocery, parent, false);
            return new GroceryViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull GroceryViewHolder holder, int position) {
            // TODO: Implementare reală a binding-ului datelor
            // În implementarea completă, acest cod ar face binding la viewHolder cu datele
            // specifice din groceryItems.get(position)
        }
        
        @Override
        public int getItemCount() {
            return groceryItems.size();
        }
        
        class GroceryViewHolder extends RecyclerView.ViewHolder {
            // TODO: Declarare referințe la elementele din layout
            
            GroceryViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO: Inițializare elemente UI din layout
            }
        }
    }
} 