package com.example.myapplication.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RomaniaMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener {

    private static final String TAG = "RomaniaMapActivity";
    private static final LatLng ROMANIA_CENTER = new LatLng(45.9443, 25.0094);
    private static final float DEFAULT_ZOOM = 6.5f;
    
    private GoogleMap mMap;
    private TextView regionNameText;
    private TextView regionSubtitleText;
    private TextView regionDescriptionText;
    private TextView citiesLabel;
    private TextView citiesList;
    private Button learnMoreButton;
    private ChipGroup regionChipGroup;
    private FloatingActionButton themeToggleButton;
    
    private boolean isNightMode = false;
    private Map<String, Polygon> regionPolygons = new HashMap<>();
    private Map<String, List<Marker>> regionMarkers = new HashMap<>();
    private Map<String, JSONObject> citiesData = new HashMap<>();
    private String currentRegionId = null;
    
    /**
     * Static method to launch this activity from anywhere in the app
     * @param context The context to use for the intent
     */
    public static void launch(Context context) {
        Intent intent = new Intent(context, RomaniaMapActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_romania_map);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize views
        regionNameText = findViewById(R.id.regionNameText);
        regionSubtitleText = findViewById(R.id.regionSubtitleText);
        regionDescriptionText = findViewById(R.id.regionDescriptionText);
        citiesLabel = findViewById(R.id.citiesLabel);
        citiesList = findViewById(R.id.citiesList);
        learnMoreButton = findViewById(R.id.learnMoreButton);
        regionChipGroup = findViewById(R.id.regionChipGroup);
        themeToggleButton = findViewById(R.id.themeToggleButton);

        // Hide the learn more button initially
        learnMoreButton.setVisibility(View.GONE);

        // Set up the map view
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up theme toggle button
        themeToggleButton.setOnClickListener(v -> {
            isNightMode = !isNightMode;
            applyMapStyle();
        });

        // Set up learn more button
        learnMoreButton.setOnClickListener(v -> {
            if (currentRegionId != null) {
                // In a real app, you would launch a detailed activity about the region
                Toast.makeText(this, "Mai multe detalii despre " + 
                        getRegionName(currentRegionId), Toast.LENGTH_SHORT).show();
            }
        });

        // Load cities data
        loadCitiesData();
        
        // Set up chip group for regions filter
        setupRegionChips();
    }

    private void setupRegionChips() {
        // Add a chip for "All Regions"
        Chip allRegionsChip = new Chip(this);
        allRegionsChip.setText(R.string.all_regions);
        allRegionsChip.setCheckable(true);
        allRegionsChip.setChecked(true);
        allRegionsChip.setTag("all");
        regionChipGroup.addView(allRegionsChip);
        
        // Add chips for each region
        String[] regionIds = {"transilvania", "moldova", "muntenia", "dobrogea", 
                "oltenia", "banat", "crisana", "maramures", "bucovina"};
        
        for (String regionId : regionIds) {
            Chip chip = new Chip(this);
            chip.setText(getRegionName(regionId));
            chip.setCheckable(true);
            chip.setTag(regionId);
            regionChipGroup.addView(chip);
        }
        
        // Set up listener to filter regions
        regionChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                // If no chip is selected, select "All Regions" chip
                allRegionsChip.setChecked(true);
                return;
            }
            
            Chip selectedChip = findViewById(checkedId);
            String selectedRegion = (String) selectedChip.getTag();
            filterRegions(selectedRegion);
        });
    }

    private void filterRegions(String regionId) {
        if (mMap == null) return;
        
        if ("all".equals(regionId)) {
            // Show all regions
            for (Polygon polygon : regionPolygons.values()) {
                polygon.setVisible(true);
            }
            for (List<Marker> markers : regionMarkers.values()) {
                for (Marker marker : markers) {
                    marker.setVisible(true);
                }
            }
        } else {
            // Show only selected region
            for (Map.Entry<String, Polygon> entry : regionPolygons.entrySet()) {
                entry.getValue().setVisible(entry.getKey().equals(regionId));
            }
            for (Map.Entry<String, List<Marker>> entry : regionMarkers.entrySet()) {
                boolean isVisible = entry.getKey().equals(regionId);
                for (Marker marker : entry.getValue()) {
                    marker.setVisible(isVisible);
                }
            }
            
            // Zoom to the selected region
            if (regionPolygons.containsKey(regionId)) {
                Polygon polygon = regionPolygons.get(regionId);
                zoomToPolygon(polygon);
                
                // Show region info
                displayRegionInfo(regionId);
            }
        }
    }

    private void zoomToPolygon(Polygon polygon) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : polygon.getPoints()) {
            builder.include(point);
        }
        
        LatLngBounds bounds = builder.build();
        
        // Use animation for smoother transitions
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        mMap.animateCamera(cameraUpdate, 700, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                // Apply a subtle bounce effect after zooming is complete
                final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
                if (mapView != null) {
                    Animation bounceAnimation = AnimationUtils.loadAnimation(RomaniaMapActivity.this, R.anim.region_zoom);
                    mapView.startAnimation(bounceAnimation);
                }
            }

            @Override
            public void onCancel() {
                // Do nothing on cancel
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Initial map setup
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        
        // Set polygon click listener
        mMap.setOnPolygonClickListener(this);
        
        // Set marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            String regionId = (String) marker.getTag();
            if (regionId != null) {
                // Filter to this region
                for (int i = 0; i < regionChipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) regionChipGroup.getChildAt(i);
                    if (regionId.equals(chip.getTag())) {
                        chip.setChecked(true);
                        break;
                    }
                }
                return true;
            }
            return false;
        });
        
        // Apply initial style
        applyMapStyle();
        
        // Load and display all regions
        loadAndDisplayRegions();
        
        // Add city markers
        addCityMarkers();
        
        // Move camera to Romania center
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ROMANIA_CENTER, DEFAULT_ZOOM));
    }

    private void applyMapStyle() {
        if (mMap == null) return;
        
        try {
            int styleResId = isNightMode ? R.raw.map_style_night : R.raw.map_style_day;
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, styleResId));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    private void loadAndDisplayRegions() {
        String[] regionIds = {"transilvania", "moldova", "muntenia", "dobrogea", 
                "oltenia", "banat", "crisana", "maramures", "bucovina"};
        
        for (String regionId : regionIds) {
            int resourceId = getResources().getIdentifier("region_" + regionId, "raw", getPackageName());
            if (resourceId != 0) {
                try {
                    // Load GeoJSON for region
                    String geoJson = readResourceAsString(resourceId);
                    JSONObject jsonObject = new JSONObject(geoJson);
                    
                    // Parse features
                    JSONArray features = jsonObject.getJSONArray("features");
                    if (features.length() > 0) {
                        JSONObject feature = features.getJSONObject(0);
                        JSONObject properties = feature.getJSONObject("properties");
                        String color = properties.getString("color");
                        
                        // Get geometry coordinates
                        JSONObject geometry = feature.getJSONObject("geometry");
                        JSONArray coordinates = geometry.getJSONArray("coordinates")
                                .getJSONArray(0);
                        
                        // Create polygon with customized styling
                        PolygonOptions polygonOptions = new PolygonOptions()
                                .clickable(true)
                                .strokeColor(Color.parseColor(color))
                                .strokeWidth(3)
                                .fillColor(Color.parseColor(color + "66")); // 40% opacity
                        
                        for (int i = 0; i < coordinates.length(); i++) {
                            JSONArray point = coordinates.getJSONArray(i);
                            double lng = point.getDouble(0);
                            double lat = point.getDouble(1);
                            polygonOptions.add(new LatLng(lat, lng));
                        }
                        
                        Polygon polygon = mMap.addPolygon(polygonOptions);
                        polygon.setTag(regionId);
                        
                        // Add hover effect for regions
                        addHoverEffect(polygon);
                        
                        regionPolygons.put(regionId, polygon);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading region " + regionId, e);
                }
            }
        }
    }

    private void addCityMarkers() {
        try {
            for (Map.Entry<String, JSONObject> entry : citiesData.entrySet()) {
                JSONObject cityData = entry.getValue();
                String regionId = cityData.getString("region");
                
                double lat = cityData.getDouble("lat");
                double lng = cityData.getDouble("lng");
                String name = cityData.getString("name");
                
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title(name)
                        .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(regionId)));
                
                Marker marker = mMap.addMarker(markerOptions);
                marker.setTag(regionId);
                
                if (!regionMarkers.containsKey(regionId)) {
                    regionMarkers.put(regionId, new ArrayList<>());
                }
                regionMarkers.get(regionId).add(marker);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding city markers", e);
        }
    }

    private float getMarkerColor(String regionId) {
        switch (regionId) {
            case "transilvania": return BitmapDescriptorFactory.HUE_BLUE;
            case "moldova": return BitmapDescriptorFactory.HUE_GREEN;
            case "muntenia": return BitmapDescriptorFactory.HUE_RED;
            case "dobrogea": return BitmapDescriptorFactory.HUE_AZURE;
            case "oltenia": return BitmapDescriptorFactory.HUE_YELLOW;
            case "banat": return BitmapDescriptorFactory.HUE_VIOLET;
            case "crisana": return BitmapDescriptorFactory.HUE_ORANGE;
            case "maramures": return BitmapDescriptorFactory.HUE_CYAN;
            case "bucovina": return BitmapDescriptorFactory.HUE_MAGENTA;
            default: return BitmapDescriptorFactory.HUE_RED;
        }
    }

    private void loadCitiesData() {
        try {
            String citiesJson = readResourceAsString(R.raw.cities_data);
            JSONObject jsonObject = new JSONObject(citiesJson);
            JSONArray cities = jsonObject.getJSONArray("cities");
            
            for (int i = 0; i < cities.length(); i++) {
                JSONObject city = cities.getJSONObject(i);
                String name = city.getString("name");
                citiesData.put(name, city);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading cities data", e);
        }
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        String regionId = (String) polygon.getTag();
        if (regionId != null) {
            displayRegionInfo(regionId);
            
            // Update selected chip
            for (int i = 0; i < regionChipGroup.getChildCount(); i++) {
                Chip chip = (Chip) regionChipGroup.getChildAt(i);
                if (regionId.equals(chip.getTag())) {
                    chip.setChecked(true);
                    break;
                }
            }
        }
    }
    
    private void displayRegionInfo(String regionId) {
        currentRegionId = regionId;
        
        // Update region name and description
        regionNameText.setText(getRegionName(regionId));
        
        // Set subtitle if available
        int subtitleResId = getResources().getIdentifier("subtitle_" + regionId, "string", getPackageName());
        if (subtitleResId != 0) {
            regionSubtitleText.setText(subtitleResId);
            regionSubtitleText.setVisibility(View.VISIBLE);
        } else {
            regionSubtitleText.setVisibility(View.GONE);
        }
        
        // Set description
        int descResId = getResources().getIdentifier("description_" + regionId, "string", getPackageName());
        if (descResId != 0) {
            regionDescriptionText.setText(descResId);
        } else {
            int fallbackResId = getResources().getIdentifier("rom_region_" + regionId + "_desc", "string", getPackageName());
            if (fallbackResId != 0) {
                regionDescriptionText.setText(fallbackResId);
            } else {
                regionDescriptionText.setText(R.string.select_region_hint);
            }
        }
        
        // Show cities
        int citiesResId = getResources().getIdentifier("cities_" + regionId, "string", getPackageName());
        if (citiesResId != 0) {
            citiesList.setText(citiesResId);
            citiesLabel.setVisibility(View.VISIBLE);
            citiesList.setVisibility(View.VISIBLE);
        } else {
            citiesLabel.setVisibility(View.GONE);
            citiesList.setVisibility(View.GONE);
        }
        
        // Show learn more button
        learnMoreButton.setVisibility(View.VISIBLE);
    }
    
    private String getRegionName(String regionId) {
        int nameResId = getResources().getIdentifier("rom_region_" + regionId, "string", getPackageName());
        return nameResId != 0 ? getString(nameResId) : capitalize(regionId);
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    private String readResourceAsString(int resourceId) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = getResources().openRawResource(resourceId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        
        reader.close();
        return stringBuilder.toString();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Add hover effect to polygon
    private void addHoverEffect(final Polygon polygon) {
        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon clickedPolygon) {
                // Reset all polygon styles first
                for (Polygon p : regionPolygons.values()) {
                    String regionId = (String) p.getTag();
                    JSONObject feature = getRegionFeature(regionId);
                    if (feature != null) {
                        try {
                            JSONObject properties = feature.getJSONObject("properties");
                            String color = properties.getString("color");
                            
                            p.setStrokeWidth(3);
                            p.setStrokeColor(Color.parseColor(color));
                            p.setFillColor(Color.parseColor(color + "66")); // 40% opacity
                        } catch (JSONException e) {
                            Log.e(TAG, "Error resetting polygon style", e);
                        }
                    }
                }
                
                // Apply highlight style to the clicked polygon
                if (clickedPolygon.equals(polygon)) {
                    String regionId = (String) polygon.getTag();
                    JSONObject feature = getRegionFeature(regionId);
                    if (feature != null) {
                        try {
                            JSONObject properties = feature.getJSONObject("properties");
                            String color = properties.getString("color");
                            
                            // Make the stroke wider and brighter for the selected region
                            polygon.setStrokeWidth(5);
                            polygon.setStrokeColor(Color.WHITE);
                            polygon.setFillColor(Color.parseColor(color + "99")); // 60% opacity
                            
                            // Show region info in the panel
                            displayRegionInfo(regionId);
                            
                            // Update selected chip
                            for (int i = 0; i < regionChipGroup.getChildCount(); i++) {
                                Chip chip = (Chip) regionChipGroup.getChildAt(i);
                                if (regionId.equals(chip.getTag())) {
                                    chip.setChecked(true);
                                    break;
                                }
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error applying highlight style", e);
                        }
                    }
                }
            }
        });
    }
    
    private JSONObject getRegionFeature(String regionId) {
        int resourceId = getResources().getIdentifier("region_" + regionId, "raw", getPackageName());
        if (resourceId != 0) {
            try {
                String geoJson = readResourceAsString(resourceId);
                JSONObject jsonObject = new JSONObject(geoJson);
                JSONArray features = jsonObject.getJSONArray("features");
                if (features.length() > 0) {
                    return features.getJSONObject(0);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting region feature", e);
            }
        }
        return null;
    }
} 