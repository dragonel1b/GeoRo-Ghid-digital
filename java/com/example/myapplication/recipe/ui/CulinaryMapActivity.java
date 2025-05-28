package com.example.myapplication.recipe.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;

import com.example.myapplication.Joc1.RomMapActivity;
import com.example.myapplication.R;
import com.example.myapplication.recipe.model.Recipe;
import com.example.myapplication.recipe.repository.RecipeRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CulinaryMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    
    private MapView mapView;
    private GoogleMap googleMap;
    private RecipeRepository recipeRepository;
    private List<Recipe> allRecipes;
    private Map<Marker, Recipe> markerRecipeMap = new HashMap<>();
    private Map<String, List<Recipe>> recipesByRegion = new HashMap<>();

    // UI elements
    private ExtendedFloatingActionButton mapTypeToggleButton;
    private MaterialCardView recipeInfoCard;
    private TextView recipeNameText;
    private TextView recipeRegionText;
    private TextView recipeDescriptionText;
    private MaterialButton viewRecipeButton;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private SearchView searchBar;
    private ChipGroup filterChipGroup;

    // State
    private boolean isCulinaryMapMode = true;
    private String currentFilter = null;
    private Recipe selectedRecipe = null;
    private boolean isNightMode = false;
    private final Handler handler = new Handler();

    // Region coordinates in Romania
    private final Map<String, LatLng> regionCenters = new HashMap<>();

    // Color constants
    private final int COLOR_RECIPE_MARKER = Color.rgb(220, 80, 80);
    private final int COLOR_REGION_OUTLINE = Color.rgb(70, 100, 200);
    private final int COLOR_REGION_FILL = Color.argb(50, 70, 100, 200);
    
    // Custom icons for markers
    private final float MARKER_SIZE_REGULAR = 1.0f;
    private final float MARKER_SIZE_CLUSTER = 1.4f;
    private final float MARKER_SIZE_HIGHLIGHT = 1.2f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_culinary_map);

        // Initialize map
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initialize recipe repository and load recipes
        recipeRepository = RecipeRepository.getInstance();
        allRecipes = recipeRepository.getAllRecipes();
        
        // Setup UI components
        initializeRegionCenters();
        initializeUI();
        organizeRecipesByRegion();
        setupFilterChips();
    }

    private void initializeRegionCenters() {
        // Center coordinates for each Romanian region
        regionCenters.put("Moldova", new LatLng(47.0000, 27.5000));
        regionCenters.put("Muntenia", new LatLng(44.9000, 26.0000));
        regionCenters.put("Oltenia", new LatLng(44.3000, 23.8000));
        regionCenters.put("Transilvania", new LatLng(46.7700, 23.6000));
        regionCenters.put("Banat", new LatLng(45.7500, 21.2300));
        regionCenters.put("Crisana", new LatLng(46.9500, 21.9300));
        regionCenters.put("Maramures", new LatLng(47.6600, 24.7000));
        regionCenters.put("Bucovina", new LatLng(47.7000, 25.7000));
        regionCenters.put("Dobrogea", new LatLng(44.8800, 28.7500));
    }

    private void organizeRecipesByRegion() {
        // Group recipes by region
        for (Recipe recipe : allRecipes) {
            String region = recipe.getRegion();
            if (region != null && !region.isEmpty()) {
                if (!recipesByRegion.containsKey(region)) {
                    recipesByRegion.put(region, new ArrayList<>());
                }
                recipesByRegion.get(region).add(recipe);
            }
        }
    }

    private void initializeUI() {
        // Back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Map type toggle button
        mapTypeToggleButton = findViewById(R.id.mapTypeToggleButton);
        mapTypeToggleButton.setOnClickListener(v -> toggleMapMode());

        // Add night mode button
        FloatingActionButton nightModeButton = findViewById(R.id.nightModeButton);
        if (nightModeButton != null) {
            nightModeButton.setOnClickListener(v -> toggleMapStyle());
        }

        // Recipe info card (bottom sheet)
        recipeInfoCard = findViewById(R.id.recipeInfoCard);
        recipeNameText = findViewById(R.id.recipeNameText);
        recipeRegionText = findViewById(R.id.recipeRegionText);
        recipeDescriptionText = findViewById(R.id.recipeDescriptionText);
        viewRecipeButton = findViewById(R.id.viewRecipeButton);
        
        // Set up bottom sheet behavior
        View bottomSheetView = findViewById(R.id.recipeInfoCard);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // Search bar setup
        searchBar = findViewById(R.id.searchBar);
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchRecipes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    resetMapMarkers();
                }
                return false;
            }
        });

        // View recipe button
        viewRecipeButton.setOnClickListener(v -> {
            if (selectedRecipe != null) {
                openRecipeDetail(selectedRecipe);
            }
        });

        // Filter chip group
        filterChipGroup = findViewById(R.id.filterChipGroup);
    }

    private void setupFilterChips() {
        // Add chips for filtering by region
        filterChipGroup.removeAllViews();
        
        // Add "Toate regiunile" chip
        Chip allRegionsChip = new Chip(this);
        allRegionsChip.setText("Toate regiunile");
        allRegionsChip.setCheckable(true);
        allRegionsChip.setChecked(true);
        allRegionsChip.setChipIconVisible(true);
        allRegionsChip.setChipIcon(ContextCompat.getDrawable(this, R.drawable.ic_map));
        allRegionsChip.setChipBackgroundColorResource(R.color.purple_200);
        allRegionsChip.setTextColor(Color.WHITE);
        filterChipGroup.addView(allRegionsChip);
        
        // Add a chip for each region that has recipes
        int[] regionColors = new int[]{
            Color.parseColor("#4CAF50"),  // Verde - Transilvania
            Color.parseColor("#2196F3"),  // Albastru - Moldova
            Color.parseColor("#FF9800"),  // Portocaliu - Muntenia
            Color.parseColor("#9C27B0"),  // Violet - Oltenia
            Color.parseColor("#FFEB3B"),  // Galben - Dobrogea
            Color.parseColor("#E91E63"),  // Roz - Banat
            Color.parseColor("#00BCD4"),  // Turcoaz - Crișana
            Color.parseColor("#8BC34A"),  // Verde deschis - Maramureș
            Color.parseColor("#795548")   // Maro - Bucovina
        };
        int colorIndex = 0;
        
        for (String region : recipesByRegion.keySet()) {
            Chip chip = new Chip(this);
            chip.setText(region);
            chip.setCheckable(true);
            chip.setChipIconVisible(true);
            chip.setChipIcon(ContextCompat.getDrawable(this, R.drawable.ic_location));
            
            // Set a distinct color for each region
            int color = regionColors[colorIndex % regionColors.length];
            colorIndex++;
            
            chip.setChipBackgroundColor(ColorStateList.valueOf(color));
            chip.setTextColor(Color.WHITE);
            filterChipGroup.addView(chip);
        }
        
        // Add a chip for categories with colors matching the marker colors
        Map<String, Integer> categoryColors = new HashMap<>();
        categoryColors.put("Fel principal", Color.parseColor("#E57373"));    // Roșu
        categoryColors.put("Supă/Ciorbă", Color.parseColor("#64B5F6"));      // Albastru
        categoryColors.put("Desert", Color.parseColor("#F48FB1"));           // Roz
        categoryColors.put("Aperitiv", Color.parseColor("#81C784"));         // Verde
        
        for (String category : categoryColors.keySet()) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChipIconVisible(true);
            chip.setChipIcon(ContextCompat.getDrawable(this, R.drawable.ic_restaurant));
            
            int color = categoryColors.get(category);
            chip.setChipBackgroundColor(ColorStateList.valueOf(color));
            chip.setTextColor(Color.WHITE);
            filterChipGroup.addView(chip);
        }
        
        // Set up listener for filtering
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                currentFilter = null;
                resetMapMarkers();
                return;
            }
            
            Chip selectedChip = findViewById(checkedId);
            if (selectedChip != null) {
                String filter = selectedChip.getText().toString();
                currentFilter = filter.equals("Toate regiunile") ? null : filter;
                filterMarkers(currentFilter);
                
                // If it's a region, animate to that region's center
                if (regionCenters.containsKey(currentFilter)) {
                    LatLng regionCenter = regionCenters.get(currentFilter);
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(regionCenter, 7.5f),
                        500,
                        null
                    );
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        
        // Configure map
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        
        // Apply styling based on system night mode
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            isNightMode = true;
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_night));
            updateNightModeUI();
        }
        
        // Set marker click listener
        googleMap.setOnMarkerClickListener(this);
        
        // Initial camera position - centered on Romania
        LatLng romaniaCenter = new LatLng(45.9443, 25.0094);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(romaniaCenter, 6.5f));
        
        // Draw region boundaries and add recipe markers
        drawRegionBoundaries();
        addRecipeMarkers();
    }

    private void drawRegionBoundaries() {
        // Draw simplified polygon boundaries for each region
        // These are simplified coordinates for illustration
        
        // Simplified coordinates for regions (these would be more accurate in a real app)
        Map<String, List<LatLng>> regionBoundaries = new HashMap<>();
        
        // Add Moldova region (simplified)
        List<LatLng> moldovaCoords = new ArrayList<>();
        moldovaCoords.add(new LatLng(48.1, 26.6));
        moldovaCoords.add(new LatLng(48.0, 28.0));
        moldovaCoords.add(new LatLng(45.8, 28.2));
        moldovaCoords.add(new LatLng(45.7, 27.2));
        moldovaCoords.add(new LatLng(46.5, 26.5));
        moldovaCoords.add(new LatLng(47.5, 26.0));
        regionBoundaries.put("Moldova", moldovaCoords);

        // Add Transilvania region (simplified)
        List<LatLng> transilvaniaCoords = new ArrayList<>();
        transilvaniaCoords.add(new LatLng(47.8, 22.9));
        transilvaniaCoords.add(new LatLng(47.8, 25.5));
        transilvaniaCoords.add(new LatLng(45.8, 25.8));
        transilvaniaCoords.add(new LatLng(45.9, 23.0));
        transilvaniaCoords.add(new LatLng(46.7, 22.5));
        regionBoundaries.put("Transilvania", transilvaniaCoords);
        
        // Draw each region
        for (Map.Entry<String, List<LatLng>> entry : regionBoundaries.entrySet()) {
            googleMap.addPolygon(new PolygonOptions()
                    .addAll(entry.getValue())
                    .strokeColor(COLOR_REGION_OUTLINE)
                    .fillColor(COLOR_REGION_FILL)
                    .strokeWidth(3));
        }
    }

    private void addRecipeMarkers() {
        markerRecipeMap.clear();
        googleMap.clear();
        
        drawRegionBoundaries();
        
        // Ensure we have the latest recipes
        allRecipes = recipeRepository.getAllRecipes();
        
        // Re-organize recipes by region with the latest data
        organizeRecipesByRegion();
        
        // Add individual markers for each recipe
        for (Recipe recipe : allRecipes) {
            String region = recipe.getRegion();
            if (region != null && !region.isEmpty() && regionCenters.containsKey(region)) {
                // Get the base position for the region
                LatLng basePosition = regionCenters.get(region);
                
                // Add some randomness to position so markers don't overlap
                // Create more spread between markers from the same region
                double spreadFactor = 0.4; // Adjust spread distance for better visibility
                double lat = basePosition.latitude + (Math.random() - 0.5) * spreadFactor;
                double lng = basePosition.longitude + (Math.random() - 0.5) * spreadFactor;
                LatLng position = new LatLng(lat, lng);
                
                // Select marker color based on recipe category
                float markerHue = getMarkerHueForCategory(recipe.getCategory());
                
                // Create and add the marker
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(recipe.getTitle())
                        .snippet(recipe.getCategory())
                        .icon(BitmapDescriptorFactory.defaultMarker(markerHue))
                        .zIndex(1.0f); // Higher z-index to appear on top
                        
                Marker marker = googleMap.addMarker(markerOptions);
                if (marker != null) {
                    markerRecipeMap.put(marker, recipe);
                }
            }
        }
    }
    
    private float getMarkerHueForCategory(String category) {
        // Return different hues based on recipe category for visual differentiation
        switch(category.toLowerCase()) {
            case "fel principal": return BitmapDescriptorFactory.HUE_RED;
            case "supă": case "ciorbă": case "supă/ciorbă": return BitmapDescriptorFactory.HUE_BLUE;
            case "desert": return BitmapDescriptorFactory.HUE_ROSE;
            case "aperitiv": return BitmapDescriptorFactory.HUE_GREEN;
            case "băutură": return BitmapDescriptorFactory.HUE_CYAN;
            case "garnitură": return BitmapDescriptorFactory.HUE_YELLOW;
            default: return BitmapDescriptorFactory.HUE_ORANGE;
        }
    }

    private void filterMarkers(String filter) {
        if (googleMap == null) return;
        
        // Ensure we have the latest recipes
        allRecipes = recipeRepository.getAllRecipes();
        
        markerRecipeMap.clear();
        googleMap.clear();
        drawRegionBoundaries();
        
        for (Recipe recipe : allRecipes) {
            // Skip recipes that don't match the filter
            if (filter != null && 
                !recipe.getRegion().equals(filter) && 
                !recipe.getCategory().equals(filter)) {
                continue;
            }
            
            String region = recipe.getRegion();
            if (region != null && !region.isEmpty() && regionCenters.containsKey(region)) {
                // Get the base position for the region
                LatLng basePosition = regionCenters.get(region);
                
                // Add some randomness to avoid markers stacking exactly on top of each other
                double lat = basePosition.latitude + (Math.random() - 0.5) * 0.3;
                double lng = basePosition.longitude + (Math.random() - 0.5) * 0.3;
                LatLng position = new LatLng(lat, lng);
                
                // Create and add the marker with color based on category
                float markerHue = getMarkerHueForCategory(recipe.getCategory());
                
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(recipe.getTitle())
                        .snippet(recipe.getCategory())
                        .icon(BitmapDescriptorFactory.defaultMarker(markerHue));
                        
                Marker marker = googleMap.addMarker(markerOptions);
                if (marker != null) {
                    markerRecipeMap.put(marker, recipe);
                }
            }
        }
    }

    private void searchRecipes(String query) {
        if (query == null || query.isEmpty()) {
            resetMapMarkers();
            return;
        }
        
        List<Recipe> searchResults = recipeRepository.searchRecipes(query);
        
        markerRecipeMap.clear();
        googleMap.clear();
        drawRegionBoundaries();
        
        for (Recipe recipe : searchResults) {
            String region = recipe.getRegion();
            if (region != null && !region.isEmpty() && regionCenters.containsKey(region)) {
                // Get the base position for the region
                LatLng basePosition = regionCenters.get(region);
                
                // Add some randomness to avoid markers stacking exactly on top of each other
                double lat = basePosition.latitude + (Math.random() - 0.5) * 0.3;
                double lng = basePosition.longitude + (Math.random() - 0.5) * 0.3;
                LatLng position = new LatLng(lat, lng);
                
                // Create and add the marker with color based on category
                float markerHue = getMarkerHueForCategory(recipe.getCategory());
                
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(recipe.getTitle())
                        .snippet(recipe.getCategory())
                        .icon(BitmapDescriptorFactory.defaultMarker(markerHue));
                        
                Marker marker = googleMap.addMarker(markerOptions);
                if (marker != null) {
                    markerRecipeMap.put(marker, recipe);
                }
            }
        }
        
        if (searchResults.isEmpty()) {
            Toast.makeText(this, "Nu s-au găsit rețete pentru: " + query, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "S-au găsit " + searchResults.size() + " rețete", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetMapMarkers() {
        if (googleMap != null) {
            addRecipeMarkers();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Get the recipe for this marker
        Recipe recipe = markerRecipeMap.get(marker);
        if (recipe != null) {
            showRecipeInfo(recipe);
            
            // Center the map on the marker with animation
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 8f),
                500,
                null
            );
            
            return true;
        }
        return false;
    }

    private void showRecipeInfo(Recipe recipe) {
        selectedRecipe = recipe;
        
        recipeNameText.setText(recipe.getTitle());
        recipeRegionText.setText(recipe.getRegion());
        recipeDescriptionText.setText(recipe.getDescription());
        
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void openRecipeDetail(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("RECIPE_ID", recipe.getId());
        startActivity(intent);
    }

    private void toggleMapMode() {
        if (isCulinaryMapMode) {
            // Switch to region map
            mapTypeToggleButton.setText("Comută la harta culinară");
            Intent intent = new Intent(this, RomMapActivity.class);
            startActivity(intent);
        } else {
            // Currently in region map mode, code would handle this when returning from RomMapActivity
            mapTypeToggleButton.setText("Comută la harta regiunilor");
            isCulinaryMapMode = true;
            resetMapMarkers();
        }
    }

    private void toggleMapStyle() {
        isNightMode = !isNightMode;
        if (googleMap != null) {
            if (isNightMode) {
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_night));
            } else {
                googleMap.setMapStyle(null);
            }
        }
        updateNightModeUI();
    }
    
    private void updateNightModeUI() {
        // Update night mode button icon
        FloatingActionButton nightModeButton = findViewById(R.id.nightModeButton);
        if (nightModeButton != null) {
            if (isNightMode) {
                nightModeButton.setImageResource(R.drawable.ic_day_mode);
            } else {
                nightModeButton.setImageResource(R.drawable.ic_night_mode);
            }
        }
        
        // Update map legend colors based on night mode
        View mapLegend = findViewById(R.id.mapLegend);
        if (mapLegend != null) {
            int backgroundColor = isNightMode ? Color.parseColor("#263238") : Color.WHITE;
            int textColor = isNightMode ? Color.WHITE : Color.BLACK;
            
            mapLegend.setBackgroundColor(backgroundColor);
            
            // Find and update all TextViews in the legend
            if (mapLegend instanceof ViewGroup) {
                updateTextViewColors((ViewGroup)mapLegend, textColor);
            }
        }
    }
    
    private void updateTextViewColors(ViewGroup viewGroup, int color) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(color);
            } else if (child instanceof ViewGroup) {
                updateTextViewColors((ViewGroup) child, color);
            }
        }
    }

    // Lifecycle methods for MapView
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        
        // Reload recipes when activity resumes to get any changes
        if (googleMap != null) {
            resetMapMarkers();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
} 