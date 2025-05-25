package com.example.myapplication.Joc1;

import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.view.MotionEvent;
import java.util.Map;
import java.util.HashMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap;
import androidx.annotation.NonNull;
import android.content.res.Configuration;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.material.search.SearchBar;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.Dobrogea;
import com.example.myapplication.RomApp.Transilvania;
import com.example.myapplication.RomApp.Moldova;
import com.example.myapplication.RomApp.Oltenia;
import com.example.myapplication.RomApp.Muntenia;
import com.example.myapplication.RomApp.Banat;
import com.example.myapplication.RomApp.Crisana;
import com.example.myapplication.RomApp.Maramures;
import com.example.myapplication.RomApp.Bucovina;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import java.util.List;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import android.widget.LinearLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class RomMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final Map<String, Rect> regionBounds = new HashMap<>();
    private MapView mapView;
    private GoogleMap googleMap;
    private RomGameState gameState;
    private MaterialCardView regionInfoCard;
    private TextView regionNameText;
    private TextView regionDescriptionText;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private String currentRegion = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_map_google);

        // Initialize Google Map
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initialize region bounds
        regionBounds.put("banat", new Rect(52, 220, 87, 258));
        regionBounds.put("crisana", new Rect(76, 120, 108, 153));
        regionBounds.put("maramures", new Rect(104, 84, 130, 111));
        regionBounds.put("bucovina", new Rect(200, 112, 223, 138));
        regionBounds.put("transilvania", new Rect(148, 120, 183, 157));
        regionBounds.put("moldova", new Rect(236, 124, 265, 155));
        regionBounds.put("oltenia", new Rect(132, 212, 159, 247));
        regionBounds.put("muntenia", new Rect(200, 256, 236, 290));
        regionBounds.put("dobrogea", new Rect(284, 244, 316, 267));

        // Initialize views and game state
        gameState = RomGameState.getInstance();
        initializeViews();
        setupRegionButtons();
        setupCityMarkers();
        applyEntryAnimations();
        setupSearchBar();

        // Map interactions are handled through Google Maps API
    }

    private void initializeViews() {
        FloatingActionButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        View regionInfoCard = findViewById(R.id.regionInfoCard);
        if (regionInfoCard != null) {
            this.regionInfoCard = (MaterialCardView) regionInfoCard;
            this.regionNameText = findViewById(R.id.regionNameText);
            this.regionDescriptionText = findViewById(R.id.regionDescriptionText);
            this.bottomSheetBehavior = BottomSheetBehavior.from(regionInfoCard);
            this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private void setupSearchBar() {
        SearchBar searchBar = findViewById(R.id.searchBar);
        if (searchBar != null) {
            searchBar.setOnClickListener(v -> {
                Toast.makeText(this, "Search functionality coming soon", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupRegionButtons() {
        // Region buttons are handled via map clicks instead
    }

    private void setupCityMarkers() {
        if (googleMap != null) {
            googleMap.clear();
            
            // Center map on Romania
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(45.9432, 24.9668), 6f));
            
            // Add markers for major cities with tags
            googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(46.77, 23.59)) // Cluj-Napoca
                .title("Cluj-Napoca"))
                .setTag("cluj");

            googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(47.16, 27.58)) // Iași
                .title("Iași"))
                .setTag("iasi");

            googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(44.16, 28.63)) // Constanța
                .title("Constanța"))
                .setTag("constanta");
            
            // Add markers for user activity locations
            addActivityLocationMarkers();
        }
    }

    private void addActivityLocationMarkers() {
        List<LatLng> activityLocations = gameState.getActivityLocations();
        for (LatLng location : activityLocations) {
            googleMap.addMarker(new MarkerOptions()
                .position(location)
                .title("Activitate ta")
                .icon(BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_AZURE)));
        }
    }

    private void applyEntryAnimations() {
        FrameLayout mapContainer = findViewById(R.id.mapContainer);
        if (mapContainer != null) {
            mapContainer.setAlpha(0f);
            mapContainer.animate().alpha(1f).setDuration(500).start();
        }
    }

    private void handleMapClick(float x, float y) {
        // Convert screen coordinates to LatLng for Google Maps
        if (googleMap != null) {
            LatLng location = googleMap.getProjection().fromScreenLocation(
                new android.graphics.Point((int)x, (int)y));
            
            // Check if click is within any region bounds
            for (Map.Entry<String, Rect> entry : regionBounds.entrySet()) {
                if (entry.getValue().contains((int)x, (int)y)) {
                    navigateToRegion(entry.getKey());
                    return;
                }
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);

        // Set map style based on theme
        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) 
            == Configuration.UI_MODE_NIGHT_YES) {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this, R.raw.map_style_night));
        } else {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this, R.raw.map_style_day));
        }

        // Set up map click listeners
        googleMap.setOnMapClickListener(latLng -> {
            android.graphics.Point screenPoint = googleMap.getProjection().toScreenLocation(latLng);
            handleMapClick(screenPoint.x, screenPoint.y);
        });

        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag != null) {
                navigateToRegion(tag.toString());
                return true;
            }
            return false;
        });
        
        setupCityMarkers();
    }

    private void navigateToRegion(String regionId) {
        Class<?> regionActivity = getRegionActivityClass(regionId);
        if (regionActivity != null) {
            Intent intent = new Intent(this, regionActivity);
            startActivity(intent);
        }
    }

    private Class<?> getRegionActivityClass(String regionId) {
        switch (regionId) {
            case "banat": return Banat.class;
            case "crisana": return Crisana.class;
            case "maramures": return Maramures.class;
            case "bucovina": return Bucovina.class;
            case "transilvania": return Transilvania.class;
            case "moldova": return Moldova.class;
            case "oltenia": return Oltenia.class;
            case "muntenia": return Muntenia.class;
            case "dobrogea": return Dobrogea.class;
            case "cluj": return com.example.myapplication.RomApp.ClujNapoca.class;
            case "iasi": return com.example.myapplication.RomApp.Iasi.class;
            case "constanta": return com.example.myapplication.RomApp.Constanta.class;
            default: return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
