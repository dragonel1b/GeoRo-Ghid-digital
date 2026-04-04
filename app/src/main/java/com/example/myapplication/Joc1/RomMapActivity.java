package com.example.myapplication.Joc1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.content.res.Configuration;
import android.os.Handler;
import android.widget.FrameLayout;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activitate pentru harta interactivă a României
 * Afișează regiuni, orașe importante și permite interacțiunea cu acestea
 */
public class RomMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Componente UI principale
    private MapView mapView;
    private GoogleMap googleMap;
    private FloatingActionButton styleToggleButton;
    private ExtendedFloatingActionButton culinaryMapButton;
    private SearchView searchView;
    private MaterialCardView regionInfoCard;
    private TextView regionNameText;
    private TextView regionDescriptionText;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private ExtendedFloatingActionButton discoverFab;
    private AlertDialog dialog; // Dialog for city info
    // Region filters
    private com.google.android.material.chip.ChipGroup regionFilterChipGroup;
    
    // State
    private boolean isNightMode = false;
    private final Handler handler = new Handler();
    private String currentRegion = null;
    private boolean isDiscoveryMode = false;
    private RomGameState gameState;
    // Add flag to prevent recursion
    private boolean isUpdatingChipSelection = false;
    
    // Missions system
    private List<Mission> availableMissions = new ArrayList<>();
    private List<Mission> activeMissions = new ArrayList<>();
    private List<Mission> completedMissions = new ArrayList<>();
    private Map<String, Marker> missionMarkers = new HashMap<>();
    
    // Stocare date pentru regiuni
    private final Map<String, LatLng> regionCenters = new HashMap<>();
    private final Map<String, com.google.android.gms.maps.model.Polygon> regionPolygons = new HashMap<>();
    
    // Constante pentru culori
    private static final int COLOR_TRANSILVANIA = Color.rgb(76, 175, 80);    // Verde
    private static final int COLOR_MOLDOVA = Color.rgb(33, 150, 243);        // Albastru
    private static final int COLOR_MUNTENIA = Color.rgb(255, 152, 0);        // Portocaliu  
    private static final int COLOR_DOBROGEA = Color.rgb(255, 235, 59);       // Galben
    private static final int COLOR_OLTENIA = Color.rgb(156, 39, 176);        // Violet
    private static final int COLOR_BANAT = Color.rgb(233, 30, 99);           // Roz
    private static final int COLOR_CRISANA = Color.rgb(0, 188, 212);         // Cyan
    private static final int COLOR_MARAMURES = Color.rgb(139, 195, 74);      // Verde deschis
    private static final int COLOR_BUCOVINA = Color.rgb(121, 85, 72);        // Maro

    private static final String TAG = "RomMapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_map);

        // Initialize game state
        gameState = RomGameState.getInstance();
        gameState.initialize(this);

        // Inițializare MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Inițializare UI și datele regiunilor
        initializeUI();
        initializeRegionCenters();
        
        // Initialize missions
        loadMissions();
        
        // Set up search functionality
        setupSearchBar();
    }

    private void initializeUI() {
        // Buton Back
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Buton Toggle Mod Zi/Noapte
        styleToggleButton = findViewById(R.id.styleToggleButton);
        styleToggleButton.setOnClickListener(v -> toggleMapStyle());

        // Buton pentru Harta Culinară
        culinaryMapButton = new ExtendedFloatingActionButton(this);
        culinaryMapButton.setText("Hartă culinară");
        culinaryMapButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_restaurant));
        
        // Poziționare buton în partea stânga jos a ecranului
        FrameLayout mapContainer = findViewById(R.id.mapContainer);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.START;
        params.setMargins(16, 0, 0, 16);
        culinaryMapButton.setLayoutParams(params);
        
        culinaryMapButton.setOnClickListener(v -> openCulinaryMap());
        
        mapContainer.addView(culinaryMapButton);
        
        // Setup Region Info Card with Bottom Sheet Behavior
        regionInfoCard = findViewById(R.id.regionInfoCard);
        if (regionInfoCard != null) {
            regionNameText = findViewById(R.id.regionNameText);
            regionDescriptionText = findViewById(R.id.regionDescriptionText);
            bottomSheetBehavior = BottomSheetBehavior.from(regionInfoCard);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
        
        // Setup Discovery Button
        discoverFab = findViewById(R.id.discoverFab);
        if (discoverFab != null) {
            discoverFab.setOnClickListener(v -> toggleDiscoveryMode());
        }
        
        // Setup search view
        searchView = findViewById(R.id.searchBar);
        
        // Setup region filter chips
        setupRegionFilters();
    }

    private void initializeRegionCenters() {
        // Coordonate centru pentru fiecare regiune a României
        regionCenters.put("Transilvania", new LatLng(46.7700, 23.6000));
        regionCenters.put("Moldova", new LatLng(47.0000, 27.5000));
        regionCenters.put("Muntenia", new LatLng(44.9000, 26.0000));
        regionCenters.put("Oltenia", new LatLng(44.3000, 23.8000));
        regionCenters.put("Banat", new LatLng(45.7500, 21.2300));
        regionCenters.put("Crisana", new LatLng(46.9500, 21.9300));
        regionCenters.put("Maramures", new LatLng(47.6600, 24.7000));
        regionCenters.put("Bucovina", new LatLng(47.7000, 25.7000));
        regionCenters.put("Dobrogea", new LatLng(44.8800, 28.7500));
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
        }
        
        // Set marker click listener
        googleMap.setOnMarkerClickListener(marker -> {
            String title = marker.getTitle();
            Object tag = marker.getTag();
            
            // Animează markerul selectat
            animateMarker(marker);
            
            if (tag != null) {
                String tagStr = tag.toString();
                
                // Verifică tipul de marker
                if (tagStr.startsWith("mission:")) {
                    String missionId = tagStr.substring(8);
                    for (Mission mission : availableMissions) {
                        if (mission.getId().equals(missionId)) {
                            Toast.makeText(this, "Misiune: " + mission.getTitle(), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    }
                    return false;
                } else if (tagStr.startsWith("attraction:")) {
                    // Extract attraction info
                    String[] parts = tagStr.split("\\|");
                    String attractionName = parts[0].substring(11);
                    String description = parts.length > 1 ? parts[1] : "";
                    showAttractionInfo(attractionName, marker.getSnippet(), description);
                    return true;
                } else if (tagStr.startsWith("city:")) {
                    // City marker
                    showCityInfo(title, marker.getSnippet());
                    return true;
                }
            }
            
            // Standard marker
            showCityInfo(title, marker.getSnippet());
            return true;
        });
        
        // Configure polygon (region) click listener
        googleMap.setOnPolygonClickListener(polygon -> {
            String regionId = (String) polygon.getTag();
            if (regionId != null) {
                selectRegion(regionId);
            }
        });
        
        googleMap.setOnMapClickListener(latLng -> {
            // Hide bottom sheet on map click
            if (bottomSheetBehavior != null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
        
        // Initial camera position - centered on Romania
        LatLng romaniaCenter = new LatLng(45.9443, 25.0094);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(romaniaCenter, 6.5f));
        
        // Draw region boundaries
        drawRegionBoundaries();
        
        // Add city markers
        addCityMarkers();
        
        // Add mission markers if in discovery mode
        if (isDiscoveryMode) {
            addMissionMarkers();
            pulseMissionMarkers(true);
        }
        
        // Check if we need to select a specific region based on filter
        if (regionFilterChipGroup != null) {
            int checkedId = regionFilterChipGroup.getCheckedChipId();
            if (checkedId != View.NO_ID && checkedId != R.id.allRegionsChip) {
                // Get the selected region from the chip
                com.google.android.material.chip.Chip selectedChip = findViewById(checkedId);
                if (selectedChip != null) {
                    String regionName = selectedChip.getText().toString();
                    // Apply the filter after a short delay to ensure map is ready
                    new Handler().postDelayed(() -> filterByRegion(regionName), 500);
                }
            }
        }
    }

    private void drawRegionBoundaries() {
        // Șterge poligoanele existente dacă există
        for (com.google.android.gms.maps.model.Polygon polygon : regionPolygons.values()) {
            polygon.remove();
        }
        regionPolygons.clear();
        
        // Desenează poligoane pentru fiecare regiune
        drawRegionPolygon("Transilvania", getRegionCoordinates("Transilvania"), COLOR_TRANSILVANIA);
        drawRegionPolygon("Moldova", getRegionCoordinates("Moldova"), COLOR_MOLDOVA);
        drawRegionPolygon("Muntenia", getRegionCoordinates("Muntenia"), COLOR_MUNTENIA);
        drawRegionPolygon("Dobrogea", getRegionCoordinates("Dobrogea"), COLOR_DOBROGEA);
        drawRegionPolygon("Oltenia", getRegionCoordinates("Oltenia"), COLOR_OLTENIA);
        drawRegionPolygon("Banat", getRegionCoordinates("Banat"), COLOR_BANAT);
        drawRegionPolygon("Crisana", getRegionCoordinates("Crisana"), COLOR_CRISANA);
        drawRegionPolygon("Maramures", getRegionCoordinates("Maramures"), COLOR_MARAMURES);
        drawRegionPolygon("Bucovina", getRegionCoordinates("Bucovina"), COLOR_BUCOVINA);
    }
    
    private void drawRegionPolygon(String regionId, LatLng[] coordinates, int color) {
        if (googleMap == null || coordinates == null || coordinates.length < 3) return;
        
        // Creare poligon cu stil îmbunătățit
        PolygonOptions polygonOptions = new PolygonOptions()
                .strokeColor(Color.WHITE)
                .strokeWidth(2.5f)
                .fillColor(color & 0x4FFFFFFF); // Culoare semi-transparentă (31% opacitate)
        
        // Adăugare coordonate la poligon
        for (LatLng coordinate : coordinates) {
            polygonOptions.add(coordinate);
        }
        
        // Adăugare poligon pe hartă și stocare referință
        com.google.android.gms.maps.model.Polygon polygon = googleMap.addPolygon(polygonOptions);
        
        // Setare ID regiune ca tag pentru identificare în evenimente de click
        polygon.setTag(regionId);
        polygon.setClickable(true);
        
        // Stocare poligon pentru referință ulterioară
        regionPolygons.put(regionId, polygon);
    }
    
    private LatLng[] getRegionCoordinates(String regionId) {
        switch (regionId.toLowerCase()) {
            case "transilvania":
                return new LatLng[]{
                    new LatLng(47.15, 23.0), new LatLng(46.8, 22.8), new LatLng(46.3, 23.2),
                    new LatLng(45.7, 23.0), new LatLng(45.4, 23.7), new LatLng(45.5, 24.8),
                    new LatLng(45.9, 25.8), new LatLng(46.3, 26.0), new LatLng(46.9, 25.5),
                    new LatLng(47.4, 25.2), new LatLng(47.5, 24.8), new LatLng(47.4, 23.5)
                };
            case "moldova":
                return new LatLng[]{
                    new LatLng(48.2, 26.6), new LatLng(47.9, 27.7), new LatLng(46.7, 28.2),
                    new LatLng(46.4, 28.1), new LatLng(46.0, 27.9), new LatLng(45.6, 27.5),
                    new LatLng(45.9, 26.8), new LatLng(46.2, 26.5), new LatLng(46.4, 26.2),
                    new LatLng(46.8, 26.1), new LatLng(47.3, 26.2), new LatLng(47.8, 26.2)
                };
            case "muntenia":
                return new LatLng[]{
                    new LatLng(45.9, 25.7), new LatLng(45.8, 26.8), new LatLng(45.5, 27.5),
                    new LatLng(45.2, 27.8), new LatLng(44.8, 28.0), new LatLng(44.0, 27.9),
                    new LatLng(43.7, 27.2), new LatLng(43.6, 26.2), new LatLng(43.7, 25.5),
                    new LatLng(43.8, 25.0), new LatLng(44.0, 24.5), new LatLng(44.4, 24.3),
                    new LatLng(44.8, 24.8), new LatLng(45.3, 25.2)
                };
            case "dobrogea":
                return new LatLng[]{
                    new LatLng(45.2, 27.8), new LatLng(45.0, 28.0), new LatLng(44.8, 28.7),
                    new LatLng(44.6, 28.9), new LatLng(44.2, 29.0), new LatLng(43.8, 28.6),
                    new LatLng(43.7, 28.1), new LatLng(43.8, 27.7), new LatLng(44.0, 27.9)
                };
            case "oltenia":
                return new LatLng[]{
                    new LatLng(45.0, 23.3), new LatLng(44.8, 24.8), new LatLng(44.4, 24.3),
                    new LatLng(44.0, 24.5), new LatLng(43.8, 25.0), new LatLng(43.7, 24.5),
                    new LatLng(43.9, 23.0), new LatLng(44.1, 22.7), new LatLng(44.4, 22.7),
                    new LatLng(44.7, 22.5), new LatLng(44.9, 22.4), new LatLng(45.1, 22.6)
                };
            case "banat":
                return new LatLng[]{
                    new LatLng(45.8, 21.2), new LatLng(45.1, 22.6), new LatLng(44.9, 22.4),
                    new LatLng(44.7, 22.5), new LatLng(44.4, 22.7), new LatLng(44.2, 22.4),
                    new LatLng(44.2, 21.6), new LatLng(44.8, 21.0), new LatLng(45.2, 21.2)
                };
            case "crisana":
                return new LatLng[]{
                    new LatLng(47.5, 21.5), new LatLng(47.0, 22.2), new LatLng(46.5, 22.6),
                    new LatLng(46.1, 22.1), new LatLng(45.8, 21.8), new LatLng(45.8, 21.2),
                    new LatLng(46.1, 20.8), new LatLng(46.2, 20.4), new LatLng(46.9, 20.3),
                    new LatLng(47.2, 21.0)
                };
            case "maramures":
                return new LatLng[]{
                    new LatLng(47.9, 23.6), new LatLng(47.9, 24.5), new LatLng(47.7, 25.0),
                    new LatLng(47.6, 25.1), new LatLng(47.3, 25.2), new LatLng(47.4, 23.5),
                    new LatLng(47.5, 23.1), new LatLng(47.7, 23.0)
                };
            case "bucovina":
                return new LatLng[]{
                    new LatLng(47.9, 24.5), new LatLng(47.7, 25.0), new LatLng(47.6, 25.1),
                    new LatLng(47.7, 25.5), new LatLng(47.6, 26.1), new LatLng(47.8, 26.2),
                    new LatLng(48.2, 26.6), new LatLng(48.3, 26.4), new LatLng(48.1, 25.5),
                    new LatLng(48.0, 25.2)
                };
            default:
                return null;
        }
    }

    private void addCityMarkers() {
        if (googleMap == null) return;
        
        // Transilvania
        addCustomCityMarker(new LatLng(46.7712, 23.6236), "Cluj-Napoca", "Transilvania", R.drawable.ic_city_marker, COLOR_TRANSILVANIA);
        addCustomCityMarker(new LatLng(45.6427, 25.5887), "Brașov", "Transilvania", R.drawable.ic_city_marker, COLOR_TRANSILVANIA);
        addCustomCityMarker(new LatLng(46.2195, 24.7964), "Sighișoara", "Transilvania", R.drawable.ic_city_marker, COLOR_TRANSILVANIA);
        addCustomCityMarker(new LatLng(45.7983, 24.1256), "Sibiu", "Transilvania", R.drawable.ic_city_marker, COLOR_TRANSILVANIA);
        addCustomCityMarker(new LatLng(46.0470, 23.5858), "Alba Iulia", "Transilvania", R.drawable.ic_city_marker, COLOR_TRANSILVANIA);
        
        // Moldova
        addCustomCityMarker(new LatLng(47.1585, 27.6014), "Iași", "Moldova", R.drawable.ic_city_marker, COLOR_MOLDOVA);
        addCustomCityMarker(new LatLng(46.5667, 26.9145), "Bacău", "Moldova", R.drawable.ic_city_marker, COLOR_MOLDOVA);
        addCustomCityMarker(new LatLng(47.6426, 26.2499), "Suceava", "Moldova", R.drawable.ic_city_marker, COLOR_MOLDOVA);
        addCustomCityMarker(new LatLng(46.8273, 26.3706), "Piatra Neamț", "Moldova", R.drawable.ic_city_marker, COLOR_MOLDOVA);
        
        // Muntenia
        addCustomCityMarker(new LatLng(44.4268, 26.1025), "București", "Muntenia", R.drawable.ic_capital_marker, COLOR_MUNTENIA); // Capitala cu marker special
        addCustomCityMarker(new LatLng(44.9475, 25.6358), "Ploiești", "Muntenia", R.drawable.ic_city_marker, COLOR_MUNTENIA);
        addCustomCityMarker(new LatLng(44.4323, 24.3619), "Slatina", "Muntenia", R.drawable.ic_city_marker, COLOR_MUNTENIA);
        
        // Dobrogea
        addCustomCityMarker(new LatLng(44.1598, 28.6348), "Constanța", "Dobrogea", R.drawable.ic_beach_marker, COLOR_DOBROGEA); // Oraș de coastă
        addCustomCityMarker(new LatLng(44.8998, 28.8041), "Tulcea", "Dobrogea", R.drawable.ic_city_marker, COLOR_DOBROGEA);
        
        // Oltenia
        addCustomCityMarker(new LatLng(44.3302, 23.7949), "Craiova", "Oltenia", R.drawable.ic_city_marker, COLOR_OLTENIA);
        addCustomCityMarker(new LatLng(44.6994, 22.5456), "Drobeta-Turnu Severin", "Oltenia", R.drawable.ic_city_marker, COLOR_OLTENIA);
        
        // Banat
        addCustomCityMarker(new LatLng(45.7489, 21.2087), "Timișoara", "Banat", R.drawable.ic_city_marker, COLOR_BANAT);
        addCustomCityMarker(new LatLng(45.3088, 21.8900), "Reșița", "Banat", R.drawable.ic_city_marker, COLOR_BANAT);
        
        // Crișana
        addCustomCityMarker(new LatLng(47.0465, 21.9189), "Oradea", "Crișana", R.drawable.ic_city_marker, COLOR_CRISANA);
        addCustomCityMarker(new LatLng(46.1865, 21.3123), "Arad", "Crișana", R.drawable.ic_city_marker, COLOR_CRISANA);
        
        // Maramureș
        addCustomCityMarker(new LatLng(47.6635, 23.5823), "Baia Mare", "Maramureș", R.drawable.ic_city_marker, COLOR_MARAMURES);
        
        // Bucovina
        addCustomCityMarker(new LatLng(47.9304, 25.9355), "Rădăuți", "Bucovina", R.drawable.ic_city_marker, COLOR_BUCOVINA);
        
        // Adaugă markere pentru atracții culturale și turistice
        addAttractionsMarkers();
    }
    
    /**
     * Adaugă markere pentru atracții turistice și culturale
     */
    private void addAttractionsMarkers() {
        // Castele și cetăți
        addCustomAttractionMarker(new LatLng(45.5149, 25.3672), "Castelul Bran", "Transilvania", R.drawable.ic_castle_marker, "Castel medieval faimos pentru legătura cu mitul lui Dracula");
        addCustomAttractionMarker(new LatLng(45.5135, 25.4200), "Castelul Peleș", "Transilvania", R.drawable.ic_castle_marker, "Castel regal impresionant din secolul XIX");
        addCustomAttractionMarker(new LatLng(45.9408, 23.5517), "Cetatea Alba Carolina", "Transilvania", R.drawable.ic_fortress_marker, "Cea mai mare cetate din România");
        
        // Mănăstiri și biserici
        addCustomAttractionMarker(new LatLng(47.7647, 26.0973), "Mănăstirea Voroneț", "Bucovina", R.drawable.ic_church_marker, "Mănăstire pictată faimoasă pentru 'albastrul de Voroneț'");
        addCustomAttractionMarker(new LatLng(47.6284, 26.2045), "Mănăstirea Moldovița", "Bucovina", R.drawable.ic_church_marker, "Mănăstire pictată inclusă în patrimoniul UNESCO");
        
        // Atracții naturale
        addCustomAttractionMarker(new LatLng(45.0079, 29.2143), "Delta Dunării", "Dobrogea", R.drawable.ic_nature_marker, "Rezervație a Biosferei, inclusă în patrimoniul UNESCO");
        addCustomAttractionMarker(new LatLng(45.4081, 25.5625), "Muntele Bucegi", "Transilvania", R.drawable.ic_mountain_marker, "Masiv montan cu formațiuni spectaculoase");
        
        // Orașe turistice principale
        addCustomAttractionMarker(new LatLng(45.7879, 24.1429), "Sibiu - Centrul Vechi", "Transilvania", R.drawable.ic_historic_marker, "Piața Mare și Piața Mică, bijuterii arhitecturale");
        addCustomAttractionMarker(new LatLng(46.2243, 24.7936), "Sighișoara - Centrul Medieval", "Transilvania", R.drawable.ic_historic_marker, "Unul dintre cele mai bine conservate orașe medievale din Europa");
    }
    
    /**
     * Adaugă un marker personalizat pentru orașe
     */
    private void addCustomCityMarker(LatLng position, String title, String region, int iconResource, int regionColor) {
        if (googleMap == null) return;
        
        try {
            // Creez un bitmap colorat pentru iconița markerului
            com.google.android.gms.maps.model.BitmapDescriptor icon;
            
            if (iconResource != 0) {
                // Încerc să folosesc o iconiță personalizată
                android.graphics.Bitmap originalBitmap = android.graphics.BitmapFactory.decodeResource(getResources(), iconResource);
                
                if (originalBitmap != null) {
                    // Creez un bitmap colorat cu culoarea regiunii
                    android.graphics.Bitmap coloredBitmap = changeBitmapColor(originalBitmap, regionColor);
                    icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(coloredBitmap);
                } else {
                    // Fallback la marker normal
                    float hue = getHueFromColor(regionColor);
                    icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(hue);
                }
            } else {
                // Fallback la marker normal
                float hue = getHueFromColor(regionColor);
                icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(hue);
            }
            
            // Creare marker cu stil personalizat
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(region)
                    .icon(icon)
                    .alpha(0.9f);
            
            // Adăugare marker pe hartă
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag("city:" + title);
        } catch (Exception e) {
            Log.e(TAG, "Error adding custom marker: " + e.getMessage());
            // Fallback la marker standard
            addCityMarker(position, title, region, getHueFromColor(regionColor));
        }
    }
    
    /**
     * Adaugă un marker personalizat pentru atracții turistice
     */
    private void addCustomAttractionMarker(LatLng position, String title, String region, int iconResource, String description) {
        if (googleMap == null) return;
        
        try {
            com.google.android.gms.maps.model.BitmapDescriptor icon;
            
            if (iconResource != 0) {
                android.graphics.Bitmap originalBitmap = android.graphics.BitmapFactory.decodeResource(getResources(), iconResource);
                
                if (originalBitmap != null) {
                    icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(originalBitmap);
                } else {
                    icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
                }
            } else {
                icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
            }
            
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(region)
                    .icon(icon)
                    .zIndex(1.0f); // Pune atracțiile deasupra orașelor
            
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag("attraction:" + title + "|" + description); // Include descrierea în tag
            
        } catch (Exception e) {
            Log.e(TAG, "Error adding attraction marker: " + e.getMessage());
        }
    }
    
    /**
     * Schimbă culoarea unui bitmap pentru marker personalizat
     */
    private android.graphics.Bitmap changeBitmapColor(android.graphics.Bitmap sourceBitmap, int color) {
        android.graphics.Bitmap resultBitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);
        android.graphics.Paint paint = new android.graphics.Paint();
        android.graphics.ColorFilter filter = new android.graphics.PorterDuffColorFilter(color, android.graphics.PorterDuff.Mode.SRC_ATOP);
        paint.setColorFilter(filter);
        
        android.graphics.Canvas canvas = new android.graphics.Canvas(resultBitmap);
        canvas.drawBitmap(resultBitmap, 0, 0, paint);
        
        return resultBitmap;
    }
    
    /**
     * Convertește o culoare RGB în valoarea HUE pentru marker
     */
    private float getHueFromColor(int color) {
        float[] hsv = new float[3];
        android.graphics.Color.colorToHSV(color, hsv);
        return hsv[0];
    }
    
    private void showCityInfo(String cityName, String regionName) {
        // Create dialog to show city info
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_city_info, null);
        
        // Set up dialog views
        TextView cityNameText = dialogView.findViewById(R.id.cityNameText);
        TextView regionNameText = dialogView.findViewById(R.id.regionNameText);
        TextView cityDescriptionText = dialogView.findViewById(R.id.cityDescriptionText);
        
        cityNameText.setText(cityName);
        regionNameText.setText(regionName);
        
        // Set description based on city
        String description = getCityDescription(cityName);
        cityDescriptionText.setText(description);
        
        // Set up visit button
        dialogView.findViewById(R.id.visitCityButton).setOnClickListener(v -> openCityActivity(cityName));
        
        // Set up close button
        dialogView.findViewById(R.id.closeButton).setOnClickListener(v -> {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        });
        
        // Show dialog
        builder.setView(dialogView);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = builder.create();
        dialog.show();
    }
    
    /**
     * Get description for a city
     */
    private String getCityDescription(String cityName) {
        switch(cityName.toLowerCase()) {
            case "bucuresti":
                return "București este capitala României și cel mai important centru politic, economic și cultural al țării. Orașul este faimos pentru Palatul Parlamentului, al doilea cel mai mare edificiu administrativ din lume.";
            case "cluj-napoca":
            case "cluj":
                return "Cluj-Napoca este considerat capitala neoficială a Transilvaniei. Oraș universitar, cu o viață culturală intensă și numeroase clădiri istorice în centrul vechi.";
            case "iasi":
                return "Iași este un important centru universitar și cultural din nordul Moldovei. A fost capitala istorică a Moldovei și găzduiește impresionantul Palat al Culturii.";
            case "timisoara":
                return "Timișoara este un important centru economic și cultural din vestul României. Este primul oraș european iluminat electric și locul unde a început Revoluția din 1989.";
            case "constanta":
                return "Constanța este cel mai important port la Marea Neagră și un popular centru turistic cu o istorie ce datează din antichitate, sub numele de Tomis.";
            case "brasov":
                return "Brașov este un oraș istoric din Transilvania, înconjurat de munții Carpați. Este cunoscut pentru Biserica Neagră, zidurile medievale și Poarta Ecaterinei.";
            case "sibiu":
                return "Sibiu este un fermecător oraș medieval din centrul României. A fost Capitală Culturală Europeană în 2007 și este cunoscut pentru arhitectura sa gotică și piețele sale istorice.";
            case "oradea":
                return "Oradea este un important centru economic și cultural din vestul României, cunoscut pentru numeroasele clădiri în stil Art Nouveau și apele termale.";
            case "craiova":
                return "Craiova este un important centru urban din Oltenia, cu o istorie bogată. Este un centru universitar important și oraș cu multe spații verzi.";
            case "alba iulia":
                return "Alba Iulia este un oraș cu o semnificație istorică deosebită, locul unde s-a înfăptuit Marea Unire din 1918. Cetatea Alba Carolina este principalul punct de atracție.";
            default:
                return "Un oraș important din România, cu multe atracții turistice și culturale de descoperit.";
        }
    }
    
    /**
     * Open the appropriate activity for a city
     */
    private void openCityActivity(String cityName) {
        Intent intent = new Intent(this, com.example.myapplication.RomApp.CityDetailActivity.class);
        
        // Deducem id-ul curatant și scriind tot cu litere mici
        String cityId = cityName.toLowerCase().replace(" ", "").replace("-", "");
        
        // Adjustari pentru diacritice cunoscute sau prescurtări speciale dacă este cazul
        cityId = java.text.Normalizer.normalize(cityId, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        
        intent.putExtra(com.example.myapplication.RomApp.CityDetailActivity.EXTRA_CITY_ID, cityId);
        startActivity(intent);
        Toast.makeText(this, "Vizitare " + cityName, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Open the culinary map activity
     */
    private void openCulinaryMap() {
        Intent intent = new Intent(this, com.example.myapplication.recipe.ui.CulinaryMapActivity.class);
        startActivity(intent);
        Toast.makeText(this, "Se deschide harta culinară...", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Open recipe detail activity for a specific recipe
     * @param recipeId ID of the recipe to display
     */
    private void openRecipeDetail(int recipeId) {
        Intent intent = new Intent(this, com.example.myapplication.recipe.ui.RecipeDetailActivity.class);
        intent.putExtra("RECIPE_ID", recipeId);
        startActivity(intent);
    }

    private void selectRegion(String regionId) {
        // Cache the current region ID
        currentRegion = regionId;

        // Then highlight it in the UI
        showRegionInfo(regionId);
        highlightSelectedRegion(regionId);
        centerMapOnRegion(regionId);
        
        // Update the filter chip selection to match the selected region
        updateRegionFilterSelection(regionId);
    }
    
    /**
     * Updates the region filter chips to match the selected region
     * @param regionId The region ID to select in the filter
     */
    private void updateRegionFilterSelection(String regionId) {
        if (regionFilterChipGroup == null) return;
        
        // Prevent recursion
        if (isUpdatingChipSelection) return;
        
        // Set flag to indicate we're programmatically changing selection
        isUpdatingChipSelection = true;
        
        // Clear current selection
        regionFilterChipGroup.clearCheck();
        
        // Find the chip corresponding to the selected region
        int chipId;
        switch (regionId) {
            case "Transilvania":
                chipId = R.id.transylvaniaChip;
                break;
            case "Moldova":
                chipId = R.id.moldovaChip;
                break;
            case "Muntenia":
                chipId = R.id.munteniaChip;
                break;
            case "Oltenia":
                chipId = R.id.olteniaChip;
                break;
            case "Dobrogea":
                chipId = R.id.dobrogeaChip;
                break;
            case "Banat":
                chipId = R.id.banatChip;
                break;
            case "Crisana":
                chipId = R.id.crisanaChip;
                break;
            case "Maramures":
                chipId = R.id.maramuresChip;
                break;
            case "Bucovina":
                chipId = R.id.bucovinaChip;
                break;
            default:
                chipId = R.id.allRegionsChip;
                break;
        }
        
        // Check the appropriate chip
        regionFilterChipGroup.check(chipId);
        
        // Reset flag
        isUpdatingChipSelection = false;
    }
    
    private void showRegionInfo(String regionId) {
        if (regionInfoCard == null || bottomSheetBehavior == null) return;
        
        // Set region name
        if (regionNameText != null) {
            regionNameText.setText(regionId);
        }
        
        // Set region description
        if (regionDescriptionText != null) {
            regionDescriptionText.setText(getRegionDescription(regionId));
        }
        
        // Expand the bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        
        // Add animation
        regionInfoCard.setAlpha(0f);
        regionInfoCard.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
    }
    
    /**
     * Get description for a region
     */
    private String getRegionDescription(String regionId) {
        switch(regionId.toLowerCase()) {
            case "transilvania":
                return "Transilvania este o regiune istorică în centrul României, renumită pentru peisajele sale pitorești, castele medievale și legende. Regiunea este înconjurată de Carpați și păstrează o bogată moștenire culturală săsească și maghiară.";
            case "moldova":
                return "Moldova este situată în nord-estul țării și este cunoscută pentru mănăstirile sale pictate, incluse în patrimoniul UNESCO. Regiunea are o bogată istorie și tradiții folclorice unice.";
            case "muntenia":
                return "Muntenia, cunoscută și sub numele de Țara Românească, este situată în sud-estul României. Include capitala București și câmpia fertilă din sudul țării, precum și traseele montane din nordul regiunii.";
            case "dobrogea":
                return "Dobrogea este situată între Dunăre și Marea Neagră și oferă o diversitate unică în România, cu influențe turcești și tătare. Este cunoscută pentru Delta Dunării, cetăți antice și plaje la Marea Neagră.";
            case "oltenia":
                return "Oltenia este situată în sud-vestul țării, formată din câmpii fertile și dealuri subcarpatice. Este cunoscută pentru tradițiile folclorice vibrante, arhitectura tradițională și mănăstiri istorice.";
            case "banat":
                return "Banatul este o regiune multiculturală din vestul României, cu influențe germane, maghiare și sârbești. Este cunoscută pentru orașele sale frumoase, în special Timișoara, și pentru arhitectura sa variată.";
            case "crisana":
                return "Crișana este situată în vestul României și este caracterizată de peisaje variate, de la câmpii fertile la zone montane. Este cunoscută pentru apele termale și arhitectura Art Nouveau din Oradea.";
            case "maramures":
                return "Maramureșul, situat în nordul României, este faimos pentru porțile sale sculptate în lemn, bisericile din lemn incluse în patrimoniul UNESCO și tradițiile bine păstrate în satele izolate.";
            case "bucovina":
                return "Bucovina, în nord-estul României, este cunoscută pentru mănăstirile pictate pe exterior, incluse în patrimoniul UNESCO. Peisajele sale montane și tradițiile folclorice atrag vizitatori din întreaga lume.";
            default:
                return "O regiune fascinantă a României cu peisaje deosebite și o istorie bogată ce merită explorată.";
        }
    }
    
    private void highlightSelectedRegion(String regionId) {
        // Resetare toate poligoanele la stilul normal
        for (Map.Entry<String, com.google.android.gms.maps.model.Polygon> entry : regionPolygons.entrySet()) {
            String polygonRegionId = entry.getKey();
            com.google.android.gms.maps.model.Polygon polygon = entry.getValue();
            
            if (polygonRegionId.equalsIgnoreCase(regionId)) {
                // Evidențiere regiune selectată
                polygon.setStrokeColor(Color.WHITE);
                polygon.setStrokeWidth(4);
            } else {
                // Resetare alte poligoane
                polygon.setStrokeColor(Color.WHITE);
                polygon.setStrokeWidth(2.5f);
            }
        }
    }
    
    private void centerMapOnRegion(String regionId) {
        LatLng center = regionCenters.get(regionId);
        if (center != null && googleMap != null) {
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(center, 7f),
                1000, // durată animație (ms)
                null
            );
        }
    }
    
    private void toggleMapStyle() {
        isNightMode = !isNightMode;
        if (googleMap != null) {
            if (isNightMode) {
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_night));
                styleToggleButton.setImageResource(R.drawable.ic_day_mode); // Trebuie creat acest drawable
            } else {
                googleMap.setMapStyle(null); // Stil implicit zi
                styleToggleButton.setImageResource(R.drawable.ic_night_mode);
            }
        }
    }
    
    // Metodele de ciclu de viață pentru MapView
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
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

    /**
     * Load all available missions
     */
    private void loadMissions() {
        // Initialize mission lists if not already done
        if (availableMissions == null) {
            availableMissions = new ArrayList<>();
        } else {
            availableMissions.clear();
        }
        
        if (activeMissions == null) {
            activeMissions = new ArrayList<>();
        }
        
        if (completedMissions == null) {
            completedMissions = new ArrayList<>();
        }
        
        // Add sample missions for each region
        // Transilvania
        Mission mission1 = new Mission(
                "explore_transilvania",
                "Descoperă inima Transilvaniei",
                "Vizitează centrele istorice și culturale din Transilvania pentru a afla despre istoria bogată a regiunii.",
                "Transilvania",
                150,
                Mission.TYPE_EXPLORATION);
        mission1.addObjective("Vizitează Cetatea Alba Carolina din Alba Iulia");
        mission1.addObjective("Explorează centrul vechi din Cluj-Napoca");
        mission1.addObjective("Descoperă Biserica Neagră din Brașov");
        availableMissions.add(mission1);
        
        // Moldova
        Mission mission2 = new Mission(
                "moldova_heritage",
                "Moștenirea culturală din Moldova",
                "Descoperă tradițiile și monumentele istorice din Moldova.",
                "Moldova",
                120,
                Mission.TYPE_CULTURAL);
        mission2.addObjective("Vizitează mănăstirile pictate din Moldova");
        mission2.addObjective("Explorează Palatul Culturii din Iași");
        availableMissions.add(mission2);
        
        // Muntenia
        Mission mission3 = new Mission(
                "bucharest_adventure",
                "Aventură în București",
                "Explorează capitala României și descoperă atracțiile sale principale.",
                "Muntenia",
                100,
                Mission.TYPE_EXPLORATION);
        mission3.addObjective("Vizitează Palatul Parlamentului");
        mission3.addObjective("Explorează Centrul Vechi");
        mission3.addObjective("Descoperă Muzeul Satului");
        mission3.setCityName("Bucuresti");
        availableMissions.add(mission3);
    }
    
    /**
     * Toggle discovery mode for finding missions and points of interest
     */
    private void toggleDiscoveryMode() {
        isDiscoveryMode = !isDiscoveryMode;
        
        if (isDiscoveryMode) {
            Toast.makeText(this, "Mod de descoperire activat! Caută locuri noi și misiuni ascunse.", 
                Toast.LENGTH_SHORT).show();
            
            discoverFab.setIconTint(ContextCompat.getColorStateList(this, R.color.purple_500));
            discoverFab.setText("Mod activ");
            
            // Animate the button
            AnimatorSet animatorSet = new AnimatorSet();
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(discoverFab, "scaleX", 1f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(discoverFab, "scaleY", 1f, 1.1f, 1f);
            scaleX.setDuration(500);
            scaleY.setDuration(500);
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.start();
            
            // Pulse mission markers
            if (googleMap != null) {
                pulseMissionMarkers(true);
            }
        } else {
            discoverFab.setIconTint(ContextCompat.getColorStateList(this, R.color.black));
            discoverFab.setText("Descoperă");
            
            // Stop pulsing markers
            if (googleMap != null) {
                pulseMissionMarkers(false);
            }
        }
    }
    
    /**
     * Make mission markers pulse to highlight them
     */
    private void pulseMissionMarkers(boolean enabled) {
        if (missionMarkers.isEmpty()) {
            addMissionMarkers();
        }
        
        for (Marker marker : missionMarkers.values()) {
            if (enabled) {
                // Create a repeating animation on a background thread
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    float hue = 0;
                    
                    @Override
                    public void run() {
                        if (!isDiscoveryMode) return;
                        
                        // Change marker color
                        hue = (hue + 15) % 360;
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(hue));
                        
                        // Schedule next animation frame
                        handler.postDelayed(this, 300);
                    }
                };
                
                handler.post(runnable);
            } else {
                // Reset to default color
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
        }
    }
    
    /**
     * Add markers for available missions
     */
    private void addMissionMarkers() {
        if (googleMap == null) return;
        
        // Clear existing mission markers
        for (Marker marker : missionMarkers.values()) {
            marker.remove();
        }
        missionMarkers.clear();
        
        // Add new markers for available missions
        for (Mission mission : availableMissions) {
            // Get appropriate position for the mission marker
            LatLng position = getMissionPosition(mission);
            
            if (position != null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(mission.getTitle())
                        .snippet("Misiune: " + mission.getDescription())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                
                Marker marker = googleMap.addMarker(markerOptions);
                marker.setTag(mission.getId()); // Store mission ID in marker tag
                missionMarkers.put(mission.getId(), marker);
            }
        }
    }
    
    /**
     * Get position for a mission marker
     */
    private LatLng getMissionPosition(Mission mission) {
        String cityName = mission.getCityName();
        
        // Use the region center if no specific city
        if (cityName.equalsIgnoreCase(mission.getRegionId())) {
            return regionCenters.get(mission.getRegionId());
        }
        
        // Otherwise, use specific city coordinates
        switch (cityName.toLowerCase()) {
            case "bucuresti": return new LatLng(44.4268, 26.1025);
            case "cluj-napoca": case "cluj": return new LatLng(46.7712, 23.6236);
            case "brasov": return new LatLng(45.6427, 25.5887);
            case "sibiu": return new LatLng(45.7983, 24.1256);
            case "iasi": return new LatLng(47.1585, 27.6014);
            case "constanta": return new LatLng(44.1598, 28.6348);
            case "timisoara": return new LatLng(45.7489, 21.2087);
            case "alba iulia": return new LatLng(46.0470, 23.5858);
            case "oradea": return new LatLng(47.0465, 21.9189);
            default: return regionCenters.get(mission.getRegionId());
        }
    }
    
    /**
     * Setup search functionality
     */
    private void setupSearchBar() {
        if (searchView != null) {
            // Configure SearchView
            searchView.setQueryHint("Caută regiuni, orașe...");
            searchView.setIconifiedByDefault(false);
            
            // Make search bar more visible
            View searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_plate);
            if (searchPlate != null) {
                searchPlate.setBackgroundColor(Color.WHITE);
            }
            
            // Set search icon color for better visibility
            ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
            if (searchIcon != null) {
                searchIcon.setColorFilter(Color.BLACK);
            }
            
            // Set text color for better visibility
            EditText searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            if (searchText != null) {
                searchText.setTextColor(Color.BLACK);
                searchText.setHintTextColor(Color.GRAY);
            }
            
            // Set up search listener
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    performSearch(query);
                    searchView.clearFocus(); // Hide keyboard
                    return true;
                }
                
                @Override
                public boolean onQueryTextChange(String newText) {
                    // Could implement suggestions here
                    return false;
                }
            });
        }
    }
    
    /**
     * Perform search based on user query
     */
    private void performSearch(String query) {
        // Check for regions first
        for (String regionName : regionCenters.keySet()) {
            if (regionName.toLowerCase().contains(query.toLowerCase())) {
                // Found a region match
                selectRegion(regionName);
                return;
            }
        }
        
        // Check for cities
        if (searchForCity(query)) {
            return;
        }
        
        // No matches found
        Toast.makeText(this, "Nu am găsit rezultate pentru: " + query, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Search for a city by name
     * @return true if found and centered the map on it
     */
    private boolean searchForCity(String cityName) {
        // Map of city names to their coordinates and region
        Map<String, Object[]> cityData = new HashMap<>();
        
        // Add major cities with their coordinates and region
        cityData.put("bucuresti", new Object[]{new LatLng(44.4268, 26.1025), "Muntenia"});
        cityData.put("cluj-napoca", new Object[]{new LatLng(46.7712, 23.6236), "Transilvania"});
        cityData.put("cluj", new Object[]{new LatLng(46.7712, 23.6236), "Transilvania"});
        cityData.put("timisoara", new Object[]{new LatLng(45.7489, 21.2087), "Banat"});
        cityData.put("iasi", new Object[]{new LatLng(47.1585, 27.6014), "Moldova"});
        cityData.put("constanta", new Object[]{new LatLng(44.1598, 28.6348), "Dobrogea"});
        cityData.put("brasov", new Object[]{new LatLng(45.6427, 25.5887), "Transilvania"});
        cityData.put("craiova", new Object[]{new LatLng(44.3302, 23.7949), "Oltenia"});
        cityData.put("sibiu", new Object[]{new LatLng(45.7983, 24.1256), "Transilvania"});
        cityData.put("oradea", new Object[]{new LatLng(47.0465, 21.9189), "Crisana"});
        
        // Check if the search matches any city
        for (Map.Entry<String, Object[]> entry : cityData.entrySet()) {
            if (entry.getKey().toLowerCase().contains(cityName.toLowerCase())) {
                // Found a match, center map and show info
                LatLng position = (LatLng) entry.getValue()[0];
                String region = (String) entry.getValue()[1];
                
                // Center map on city with higher zoom
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(position, 12f),
                    1000,
                    null
                );
                
                // Show city info
                showCityInfo(entry.getKey(), region);
                return true;
            }
        }
        
        return false;
    }

    /**
     * Setup region filter chips to filter by regions
     */
    private void setupRegionFilters() {
        regionFilterChipGroup = findViewById(R.id.regionFilterChipGroup);
        if (regionFilterChipGroup == null) return;
        
        // Set up listener for filtering by region
        regionFilterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Prevent responding to programmatic changes
            if (isUpdatingChipSelection) return;
            
            if (checkedId == View.NO_ID) {
                // Revert to showing all regions if no chip is selected
                showAllRegions();
                return;
            }
            
            // Handle region selection based on the selected chip
            if (checkedId == R.id.allRegionsChip) {
                showAllRegions();
            } else if (checkedId == R.id.transylvaniaChip) {
                filterByRegion("Transilvania");
            } else if (checkedId == R.id.moldovaChip) {
                filterByRegion("Moldova");
            } else if (checkedId == R.id.munteniaChip) {
                filterByRegion("Muntenia");
            } else if (checkedId == R.id.olteniaChip) {
                filterByRegion("Oltenia");
            } else if (checkedId == R.id.dobrogeaChip) {
                filterByRegion("Dobrogea");
            } else if (checkedId == R.id.banatChip) {
                filterByRegion("Banat");
            } else if (checkedId == R.id.crisanaChip) {
                filterByRegion("Crisana");
            } else if (checkedId == R.id.maramuresChip) {
                filterByRegion("Maramures");
            } else if (checkedId == R.id.bucovinaChip) {
                filterByRegion("Bucovina");
            }
        });
    }
    
    /**
     * Show all regions on the map
     */
    private void showAllRegions() {
        // Clear any filtering
        if (googleMap != null) {
            // Reset the map view to show all of Romania
            LatLng romaniaCenter = new LatLng(45.9443, 25.0094);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(romaniaCenter, 6.5f));
            
            // Reset any highlighted regions
            for (Map.Entry<String, com.google.android.gms.maps.model.Polygon> entry : regionPolygons.entrySet()) {
                entry.getValue().setStrokeWidth(2);
            }
            
            // Hide any opened info windows
            if (bottomSheetBehavior != null) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
            
            currentRegion = null;
        }
    }
    
    /**
     * Filter the map view to show only the selected region
     * @param regionId The ID of the region to focus on
     */
    private void filterByRegion(String regionId) {
        if (googleMap != null && regionId != null) {
            // Select and highlight the region
            selectRegion(regionId);
            
            // Close any open dialogs
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    /**
     * Animă un marker când este selectat
     */
    private void animateMarker(Marker marker) {
        // Scale animation
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    // Scale original marker
                    LatLng startPosition = marker.getPosition();
                    LatLng endPosition = new LatLng(
                            startPosition.latitude,
                            startPosition.longitude
                    );
                    
                    // Start with marker at current position
                    marker.setAnchor(0.5f, 0.5f);
                    
                    // Pulse animation
                    android.animation.ValueAnimator pulseAnim = android.animation.ValueAnimator.ofFloat(1f, 1.2f, 1f);
                    pulseAnim.setDuration(400);
                    pulseAnim.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
                    
                    pulseAnim.addUpdateListener(new android.animation.ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(android.animation.ValueAnimator animation) {
                            try {
                                float scale = (float)animation.getAnimatedValue();
                                marker.setAlpha(1.0f); // Make sure it's fully visible
                            } catch (Exception e) {
                                Log.e(TAG, "Error in marker animation: " + e.getMessage());
                            }
                        }
                    });
                    
                    pulseAnim.start();
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error animating marker: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Show attraction info dialog
     */
    private void showAttractionInfo(String attractionName, String regionName, String description) {
        // Create dialog to show attraction info
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_attraction_info, null);
        
        // If layout doesn't exist, fall back to city info layout
        if (dialogView == null) {
            showCityInfo(attractionName, regionName);
            return;
        }
        
        // Set up dialog views
        TextView nameText = dialogView.findViewById(R.id.attractionNameText);
        TextView regionText = dialogView.findViewById(R.id.attractionRegionText);
        TextView descriptionText = dialogView.findViewById(R.id.attractionDescriptionText);
        ImageView imageView = dialogView.findViewById(R.id.attractionImageView);
        
        if (nameText != null) nameText.setText(attractionName);
        if (regionText != null) regionText.setText(regionName);
        if (descriptionText != null) descriptionText.setText(description);
        
        // Set image if available (based on name)
        if (imageView != null) {
            int imageResId = getAttractionImageResource(attractionName);
            if (imageResId != 0) {
                imageView.setImageResource(imageResId);
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.GONE);
            }
        }
        
        // Set up visit button
        View visitButton = dialogView.findViewById(R.id.visitAttractionButton);
        if (visitButton != null) {
            visitButton.setOnClickListener(v -> {
                // TODO: Implement opening attraction details
                Toast.makeText(this, "Vizitare " + attractionName, Toast.LENGTH_SHORT).show();
                if (dialog != null) dialog.dismiss();
            });
        }
        
        // Set up close button
        View closeButton = dialogView.findViewById(R.id.closeButton);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            });
        }
        
        // Show dialog
        builder.setView(dialogView);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = builder.create();
        dialog.show();
    }
    
    /**
     * Get image resource for an attraction (placeholder implementation)
     */
    private int getAttractionImageResource(String attractionName) {
        // This would be replaced with actual image resources in a real app
        if (attractionName.toLowerCase().contains("castel")) {
            return R.drawable.placeholder_castle; // Placeholder, create this drawable
        } else if (attractionName.toLowerCase().contains("mănăstire") || 
                   attractionName.toLowerCase().contains("manastire") ||
                   attractionName.toLowerCase().contains("biserica")) {
            return R.drawable.placeholder_monastery; // Placeholder, create this drawable
        } else if (attractionName.toLowerCase().contains("delta")) {
            return R.drawable.placeholder_delta; // Placeholder, create this drawable
        }
        
        return 0; // No image available
    }

    /**
     * Adaugă un marker standard pentru un oraș
     */
    private void addCityMarker(LatLng position, String title, String region, float hue) {
        if (googleMap == null) return;
        
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title(title)
                .snippet(region)
                .icon(BitmapDescriptorFactory.defaultMarker(hue));
                
        Marker marker = googleMap.addMarker(markerOptions);
        marker.setTag("city:" + title);
    }
} 