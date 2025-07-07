package com.example.myapplication.model.base;

import android.content.Intent;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.model.RegionMapData;
import com.example.myapplication.utils.PointsManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clasă de bază pentru activitățile de hartă din aplicație.
 * Oferă funcționalități comune pentru toate hărțile din diferite regiuni.
 */
public class BaseMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    // UI Components
    protected MapView mapView;
    protected GoogleMap googleMap;
    protected ImageView backButton;
    protected MaterialButton storyButton;
    protected MaterialButton gameButton;
    protected EditText searchEditText;
    protected ImageButton searchFilterButton;
    protected FloatingActionButton layerToggleButton;
    protected FloatingActionButton toggle3dButton;
    protected FloatingActionButton recenterButton;
    protected MaterialCardView layerSelectionCard;
    protected RadioGroup layerRadioGroup;
    protected MaterialCardView searchFilterCard;
    protected MaterialButton applyFiltersButton;
    protected TextView scalebarText;
    protected TextView distanceTimeText;
    protected View scalebarLine;
    
    // Map state
    protected RegionMapData regionData;
    protected PointsManager pointsManager;
    protected Map<Marker, Integer> markerIdMap = new HashMap<>();
    protected boolean is3dMode = false;
    protected Marker selectedMarker = null;
    protected List<Marker> routeMarkers = new ArrayList<>();
    protected List<LatLng> routePoints = new ArrayList<>();
    
    // Performance optimization flags
    protected boolean enableMarkerAnimations = false;  // Disable animations by default
    protected boolean enableScalebarUpdates = true;
    protected boolean isScalebarUpdatePending = false;
    protected long lastCameraMoveTime = 0;
    protected static final long CAMERA_THROTTLE_MS = 250;  // Throttle camera events to 4 per second
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm layout-ul unitar pentru toate activitățile de hartă
        setContentView(R.layout.activity_base_region_map);
        
        // Inițializăm elementele UI comune
        initializeCommonViews();
    }
    
    /**
     * Inițializează elementele UI comune pentru toate activitățile de hartă
     */
    protected void initializeCommonViews() {
        // Componente de bază
        mapView = findViewById(R.id.mapView);
        backButton = findViewById(R.id.backButton);
        storyButton = findViewById(R.id.storyButton);
        gameButton = findViewById(R.id.gameButton);
        
        // Componente noi
        searchEditText = findViewById(R.id.searchEditText);
        searchFilterButton = findViewById(R.id.searchFilterButton);
        layerToggleButton = findViewById(R.id.layerToggleButton);
        toggle3dButton = findViewById(R.id.toggle3dButton);
        recenterButton = findViewById(R.id.recenterButton);
        layerSelectionCard = findViewById(R.id.layerSelectionCard);
        layerRadioGroup = findViewById(R.id.layerRadioGroup);
        searchFilterCard = findViewById(R.id.searchFilterCard);
        applyFiltersButton = findViewById(R.id.applyFiltersButton);
        scalebarText = findViewById(R.id.scalebarText);
        distanceTimeText = findViewById(R.id.distanceTimeText);
        scalebarLine = findViewById(R.id.scalebarLine);
        
        // Setăm opțiunile de performanță
        setPerformanceOptions();
        
        // Inițializăm butonul de înapoi
        if (backButton != null) {
            backButton.setOnClickListener(v -> onBackPressed());
        }
        
        // Inițializăm butonul de poveste
        if (storyButton != null) {
            storyButton.setOnClickListener(v -> startStoryActivity());
        }
        
        // Inițializăm butonul de joc
        if (gameButton != null) {
            gameButton.setOnClickListener(v -> startGameActivity());
        }
        
        // Inițializăm butonul de toggle pentru layere
        if (layerToggleButton != null) {
            layerToggleButton.setOnClickListener(v -> toggleLayerSelection());
        }
        
        // Inițializăm butonul de toggle pentru 3D
        if (toggle3dButton != null) {
            toggle3dButton.setOnClickListener(v -> toggle3dMode());
        }
        
        // Inițializăm butonul de recentrare
        if (recenterButton != null) {
            recenterButton.setOnClickListener(v -> recenterMap());
        }
        
        // Inițializăm butonul de filtrare pentru căutare
        if (searchFilterButton != null) {
            searchFilterButton.setOnClickListener(v -> toggleSearchFilter());
        }
        
        // Inițializăm butonul de aplicare a filtrelor
        if (applyFiltersButton != null) {
            applyFiltersButton.setOnClickListener(v -> applyFilters());
        }
        
        // Inițializăm radio group pentru selectarea layerelor
        if (layerRadioGroup != null) {
            layerRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (googleMap != null) {
                    if (checkedId == R.id.radioStandard) {
                        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    } else if (checkedId == R.id.radioSatellite) {
                        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    } else if (checkedId == R.id.radioTerrain) {
                        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    }
                    layerSelectionCard.setVisibility(View.GONE);
                }
            });
        }
        
        // Inițializăm chip-urile pentru legendă
        initializeLegendChips();
        
        // Inițializăm managerul de puncte
        pointsManager = PointsManager.getInstance(this);
        
        // Actualizăm textul de progres
        updateProgressText();
    }
    
    /**
     * Setează opțiunile de performanță pentru hartă
     */
    protected void setPerformanceOptions() {
        // Dezactivăm animațiile pentru dispozitive cu performanță redusă
        enableMarkerAnimations = false;
        
        // Activăm actualizarea scalebar-ului doar când camera se oprește din mișcare
        enableScalebarUpdates = true;
        
        // Ascundem elementele UI care nu sunt esențiale pentru a reduce lag-ul
        if (scalebarText != null) scalebarText.setVisibility(View.GONE);
        if (scalebarLine != null) scalebarLine.setVisibility(View.GONE);
        if (distanceTimeText != null) distanceTimeText.setVisibility(View.GONE);
    }
    
    /**
     * Aplică tema specifică regiunii
     * @param regionName Numele regiunii (ex: "oltenia", "moldova", etc.)
     */
    protected void applyRegionTheme(String regionName) {
        // Obținem referințe la elementele UI care trebuie personalizate
        androidx.cardview.widget.CardView headerCard = findViewById(R.id.headerCard);
        TextView regionTitle = findViewById(R.id.regionTitle);
        TextView progressText = findViewById(R.id.progressText);
        androidx.constraintlayout.widget.ConstraintLayout rootLayout = findViewById(R.id.rootLayout);
        
        // Setăm titlul regiunii
        if (regionTitle != null && regionData != null) {
            regionTitle.setText("Harta " + regionData.getRegionName());
        }
        
        // Aplicăm culorile specifice regiunii
        int primaryColor = getResources().getColor(getRegionPrimaryColorId(regionName));
        int accentColor = getResources().getColor(getRegionAccentColorId(regionName));
        
        // Aplicăm culoarea primară pentru header
        if (headerCard != null) {
            headerCard.setCardBackgroundColor(primaryColor);
        }
        
        // Setăm culoarea de accent pentru butoane
        if (storyButton != null) {
            storyButton.setBackgroundTintList(ColorStateList.valueOf(accentColor));
        }
        
        if (gameButton != null) {
            gameButton.setBackgroundTintList(ColorStateList.valueOf(accentColor));
        }
        
        // Setăm fundalul specific regiunii dacă există
        int backgroundResId = getRegionBackgroundResourceId(regionName);
        if (backgroundResId != 0 && rootLayout != null) {
            rootLayout.setBackgroundResource(backgroundResId);
        }
    }
    
    /**
     * Obține ID-ul resursei de culoare primară pentru regiune
     * @param regionName Numele regiunii
     * @return ID-ul resursei de culoare
     */
    private int getRegionPrimaryColorId(String regionName) {
        switch (regionName.toLowerCase()) {
            case "oltenia":
                return R.color.oltenia_primary;
            case "moldova":
                return R.color.moldova_primary;
            case "bucovina":
                return R.color.bucovina_primary;
            case "transilvania":
                return R.color.transilvania_primary;
            case "dobrogea":
                return R.color.dobrogea_primary;
            case "banat":
                return R.color.banat_primary;
            case "crisana":
                return R.color.crisana_primary;
            case "muntenia":
                return R.color.muntenia_primary;
            case "maramures":
                return R.color.maramures_primary;
            default:
                return R.color.region_header_background;
        }
    }
    
    /**
     * Obține ID-ul resursei de culoare de accent pentru regiune
     * @param regionName Numele regiunii
     * @return ID-ul resursei de culoare
     */
    private int getRegionAccentColorId(String regionName) {
        switch (regionName.toLowerCase()) {
            case "oltenia":
                return R.color.oltenia_accent;
            case "moldova":
                return R.color.moldova_accent;
            case "bucovina":
                return R.color.bucovina_accent;
            case "transilvania":
                return R.color.transilvania_accent;
            case "dobrogea":
                return R.color.dobrogea_accent;
            case "banat":
                return R.color.banat_accent;
            case "crisana":
                return R.color.crisana_accent;
            case "muntenia":
                return R.color.muntenia_accent;
            case "maramures":
                return R.color.maramures_accent;
            default:
                return R.color.region_accent;
        }
    }
    
    /**
     * Obține ID-ul resursei de fundal pentru regiune
     * @param regionName Numele regiunii
     * @return ID-ul resursei de fundal sau 0 dacă nu există
     */
    private int getRegionBackgroundResourceId(String regionName) {
        switch (regionName.toLowerCase()) {
            case "oltenia":
                return R.drawable.oltenia_bg_simple;
            case "transilvania":
                return R.drawable.transilvania_bg_simple;
            // Pentru celelalte regiuni, putem adăuga fundaluri specifice când vor fi disponibile
            default:
                return 0;
        }
    }
    
    /**
     * Setează datele regiunii
     * @param regionData Datele regiunii
     */
    protected void setRegionData(RegionMapData regionData) {
        this.regionData = regionData;
        
        // Aplicăm tema specifică regiunii
        if (regionData != null) {
            applyRegionTheme(regionData.getRegionName());
            
            // Actualizăm textul de progres dacă există
            updateProgressText();
        }
    }
    
    /**
     * Actualizează textul de progres
     */
    protected void updateProgressText() {
        TextView progressText = findViewById(R.id.progressText);
        if (progressText != null && regionData != null && regionData.getLocations() != null) {
            int totalLocations = regionData.getLocations().size();
            int visitedLocations = pointsManager.getVisitedLocationsCount(regionData.getRegionName());
            progressText.setText("Progres: " + visitedLocations + "/" + totalLocations);
        }
    }
    
    /**
     * Inițializează harta Google Maps
     */
    protected void initializeMap() {
        if (mapView != null) {
            try {
                // Folosim savedInstanceState null doar dacă este sigur
                mapView.onCreate(null);
                mapView.getMapAsync(this);
            } catch (SecurityException e) {
                Log.w("BaseMapActivity", "SecurityException when initializing map - using fallback mode", e);
                // În caz de eroare de securitate, încercăm să continuăm fără hartă
                Toast.makeText(this, "Harta nu poate fi încărcată în acest moment. Continuăm fără hartă.", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e("BaseMapActivity", "Error initializing map", e);
                Toast.makeText(this, "Eroare la încărcarea hărții. Vă rugăm încercați din nou.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Eroare la încărcarea hărții. Vă rugăm încercați din nou.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        
        if (googleMap == null) {
            Toast.makeText(this, "Eroare la încărcarea hărții Google Maps. Vă rugăm încercați din nou.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Configurăm harta cu setări optimizate pentru performanță
        googleMap.setOnMarkerClickListener(this);
        
        // Dezactivăm funcționalități care pot cauza lag
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false); // Dezactivăm rotația pentru a îmbunătăți performanța
        googleMap.getUiSettings().setTiltGesturesEnabled(false);   // Dezactivăm înclinarea pentru a îmbunătăți performanța
        
        // Reducem calitatea hărții pentru performanță mai bună
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        
        // Configurăm clusterizarea pentru markere (dacă este implementată)
        if (enableMarkerAnimations) {
            setupMarkerClustering();
        }
        
        // Configurăm gesturile pentru trasee doar dacă este necesar
        setupRouteGestures();
        
        // Configurăm scalebar-ul doar dacă este activat
        if (enableScalebarUpdates) {
            setupScalebar();
        }
        
        try {
            // Centrăm harta pe regiunea specificată înainte de a adăuga markere
            // pentru a evita redarea markerelor în afara ecranului
            if (regionData != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    regionData.getCenter(), 
                    regionData.getDefaultZoom()
                ));
                
                // Adăugăm markere pentru locațiile importante
                addMapMarkersFromData();
            } else {
                // Dacă nu avem date pentru regiune, apelăm metoda abstractă
                addMapMarkers();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Eroare la adăugarea markerelor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Configurează clusterizarea pentru markere
     */
    private void setupMarkerClustering() {
        // Aici ar trebui să implementăm clusterizarea markerelor
        // Acest lucru necesită biblioteca Maps Utils
        // Pentru moment, lăsăm această metodă goală
        // În implementarea completă, am folosi MarkerClusterer
    }
    
    /**
     * Configurează gesturile pentru trasee
     */
    private void setupRouteGestures() {
        if (googleMap != null) {
            // Adăugăm listener pentru apăsare lungă pentru a adăuga puncte de traseu
            googleMap.setOnMapLongClickListener(latLng -> {
                addRoutePoint(latLng);
            });
            
            // Adăugăm listener pentru drag pentru a muta punctele de traseu
            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                    // Nu facem nimic la începerea drag-ului
                }
                
                @Override
                public void onMarkerDrag(Marker marker) {
                    // Nu actualizăm în timpul drag-ului pentru a evita lag-ul
                }
                
                @Override
                public void onMarkerDragEnd(Marker marker) {
                    // Actualizăm traseul și distanța doar la finalul drag-ului
                    updateRoute();
                    updateRouteDistance();
                }
            });
        }
    }
    
    /**
     * Adaugă un punct de traseu la poziția specificată
     * @param latLng Poziția punctului de traseu
     */
    private void addRoutePoint(LatLng latLng) {
        if (googleMap != null) {
            // Adăugăm un marker pentru punctul de traseu
            Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            
            if (marker != null) {
                // Adăugăm markerul în lista de puncte de traseu
                routeMarkers.add(marker);
                routePoints.add(latLng);
                
                // Actualizăm traseul
                updateRoute();
                
                // Actualizăm distanța
                updateRouteDistance();
                
                // Afișăm textul cu distanța
                distanceTimeText.setVisibility(View.VISIBLE);
            }
        }
    }
    
    /**
     * Actualizează traseul pe hartă
     */
    private void updateRoute() {
        if (googleMap != null && routePoints.size() >= 2) {
            // Ștergem traseul existent
            googleMap.clear();
            
            // Recreăm markerele
            routePoints.clear();
            for (Marker marker : routeMarkers) {
                routePoints.add(marker.getPosition());
            }
            
            // Desenăm noul traseu
            googleMap.addPolyline(new PolylineOptions()
                .addAll(routePoints)
                .width(5)
                .color(Color.BLUE));
        }
    }
    
    /**
     * Actualizează distanța traseului
     */
    private void updateRouteDistance() {
        if (routePoints.size() >= 2) {
            // Calculăm distanța totală
            float distance = 0;
            for (int i = 0; i < routePoints.size() - 1; i++) {
                float[] results = new float[1];
                android.location.Location.distanceBetween(
                    routePoints.get(i).latitude, routePoints.get(i).longitude,
                    routePoints.get(i + 1).latitude, routePoints.get(i + 1).longitude,
                    results);
                distance += results[0];
            }
            
            // Convertim distanța în format adecvat
            String distanceText;
            if (distance > 1000) {
                distanceText = String.format("%.1f km", distance / 1000);
            } else {
                distanceText = String.format("%.0f m", distance);
            }
            
            // Estimăm timpul (presupunem o viteză medie de mers pe jos de 5 km/h)
            float timeHours = distance / 1000 / 5;
            int timeMinutes = (int) (timeHours * 60);
            String timeText;
            if (timeMinutes > 60) {
                timeText = String.format("%d h %d min", timeMinutes / 60, timeMinutes % 60);
            } else {
                timeText = String.format("%d min", timeMinutes);
            }
            
            // Actualizăm textul
            distanceTimeText.setText("Distanță: " + distanceText + " | Timp: " + timeText);
        }
    }
    
    /**
     * Configurează scalebar-ul
     */
    private void setupScalebar() {
        if (googleMap != null && enableScalebarUpdates) {
            // Adăugăm listener pentru schimbarea camerei pentru a actualiza scalebar-ul
            googleMap.setOnCameraIdleListener(() -> {
                // Update scalebar only when camera stops moving
                updateScalebar();
            });
            
            // Inițializăm scalebar-ul
            updateScalebar();
        }
    }
    
    /**
     * Actualizează scalebar-ul în funcție de zoom-ul curent
     */
    private void updateScalebar() {
        if (googleMap != null && scalebarText != null && scalebarLine != null) {
            // Obținem zoom-ul curent
            float zoom = googleMap.getCameraPosition().zoom;
            
            // Calculăm scara în funcție de zoom
            // La zoom 20, 100 pixeli reprezintă aproximativ 10 metri
            // La fiecare nivel de zoom mai mic, distanța se dublează
            double metersPerPixel = 10 * Math.pow(2, 20 - zoom);
            double scaleWidth = metersPerPixel * 100; // pentru 100 pixeli
            
            // Formatăm textul pentru scalebar
            String scaleText;
            if (scaleWidth >= 1000) {
                scaleText = String.format("%.1f km", scaleWidth / 1000);
            } else {
                scaleText = String.format("%.0f m", scaleWidth);
            }
            
            // Actualizăm textul
            scalebarText.setText(scaleText);
        }
    }
    
    /**
     * Adaugă markeri pe hartă din datele regiunii
     */
    private void addMapMarkersFromData() {
        if (googleMap != null && regionData != null && regionData.getLocations() != null) {
            // Adăugăm markeri pentru fiecare locație din regiune
            for (RegionMapData.MapLocation location : regionData.getLocations()) {
                addMarker(
                    location.getTitle(),
                    location.getDescription(),
                    location.getPosition(),
                    BitmapDescriptorFactory.HUE_RED,
                    location.getId()
                );
            }
        }
    }
    
    /**
     * Adaugă markeri pe hartă
     * Această metodă poate fi suprascrisă în clasele derivate pentru a adăuga markeri specifici
     */
    protected void addMapMarkers() {
        // Metodă abstractă care trebuie implementată în clasele derivate
        // Dacă nu este suprascrisă, nu face nimic
    }
    
    /**
     * Adaugă un marker pe hartă
     * @param title Titlul markerului
     * @param snippet Descrierea markerului
     * @param position Poziția markerului
     * @param hue Culoarea markerului
     * @param id ID-ul markerului
     * @return Referință către markerul adăugat
     */
    protected Marker addMarker(String title, String snippet, LatLng position, float hue, int id) {
        Marker marker = null;
        
        if (googleMap != null) {
            // Creăm opțiunile pentru marker
            MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(hue));
            
            // Adăugăm markerul pe hartă
            marker = googleMap.addMarker(markerOptions);
            
            // Adăugăm markerul în maparea ID-uri
            if (marker != null) {
                markerIdMap.put(marker, id);
                
                // Animăm markerul doar dacă animațiile sunt activate
                if (enableMarkerAnimations) {
                    animateMarkerAppearance(marker);
                }
                
                // Verificăm dacă locația a fost vizitată
                boolean isVisited = false;
                if (regionData != null) {
                    isVisited = pointsManager.isLocationVisited(regionData.getRegionName().toLowerCase(), id);
                }
                
                // Schimbăm culoarea markerului dacă a fost vizitat
                if (isVisited) {
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }
            }
        }
        
        return marker;
    }
    
    /**
     * Animează apariția unui marker
     * @param marker Markerul care trebuie animat
     */
    private void animateMarkerAppearance(final Marker marker) {
        if (marker == null || !enableMarkerAnimations) return;
        
        // Creăm un handler pentru a executa animația
        final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        final long start = android.os.SystemClock.uptimeMillis();
        final long duration = 300; // Reduced from 500ms to 300ms
        
        // Executăm animația
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = android.os.SystemClock.uptimeMillis() - start;
                float t = Math.max(1 - ((float) elapsed / duration), 0);
                
                // Calculăm alpha-ul curent
                float alpha = 1 - t;
                marker.setAlpha(alpha);
                
                // Continuăm animația dacă nu s-a terminat
                if (t > 0.0) {
                    handler.postDelayed(this, 32); // Reduced from 16ms (60fps) to 32ms (30fps)
                }
            }
        });
    }
    
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker != null) {
            // Salvăm referința la markerul selectat
            selectedMarker = marker;
            
            // Afișăm informațiile despre marker
            marker.showInfoWindow();
            
            // Aplicăm un efect de pulsație pentru marker doar dacă animațiile sunt activate
            if (enableMarkerAnimations) {
                animateMarkerPulse(marker);
            }
            
            // Obținem ID-ul markerului
            Integer markerId = markerIdMap.get(marker);
            if (markerId != null) {
                // Gestionăm click-ul pe marker în funcție de ID
                handleMarkerClick(markerId);
            }
            
            return true;
        }
        return false;
    }
    
    /**
     * Animează un marker cu un efect de pulsație
     * @param marker Markerul care trebuie animat
     */
    private void animateMarkerPulse(final Marker marker) {
        if (marker == null || !enableMarkerAnimations) return;
        
        // Creăm un handler pentru a executa animația
        final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        final long start = android.os.SystemClock.uptimeMillis();
        final long duration = 500; // Reduced from 1000ms to 500ms
        
        // Executăm animația
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = android.os.SystemClock.uptimeMillis() - start;
                float t = elapsed / (float) duration;
                
                // Calculăm hue-ul curent (doar 2 valori în loc de animație continuă)
                float hue = (t < 0.5f) ? BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_ORANGE;
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(hue));
                
                // Continuăm animația dacă nu s-a terminat
                if (t < 1.0) {
                    handler.postDelayed(this, 100); // Reduced from 16ms to 100ms (10fps)
                } else {
                    // Resetăm culoarea la final
                    Integer markerId = markerIdMap.get(marker);
                    if (markerId != null && regionData != null) {
                        boolean isVisited = pointsManager.isLocationVisited(regionData.getRegionName().toLowerCase(), markerId);
                        if (isVisited) {
                            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        } else {
                            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        }
                    }
                }
            }
        });
    }
    
    /**
     * Gestionează click-ul pe un marker
     * @param markerId ID-ul markerului pe care s-a făcut click
     */
    protected void handleMarkerClick(int markerId) {
        // Găsim locația corespunzătoare ID-ului
        String locationName = "Locație necunoscută";
        String locationDescription = "";
        
        if (regionData != null && regionData.getLocations() != null) {
            for (RegionMapData.MapLocation location : regionData.getLocations()) {
                if (location.getId() == markerId) {
                    locationName = location.getTitle();
                    locationDescription = location.getDescription();
                    break;
                }
            }
        }
        
        // Afișăm un mesaj mai descriptiv
        Toast.makeText(this, "Ai selectat: " + locationName + "\n" + locationDescription, Toast.LENGTH_LONG).show();
        
        // Verificăm dacă avem date pentru regiune și dacă există o activitate specifică pentru acest marker
        if (regionData != null && regionData.getLocations() != null) {
            for (RegionMapData.MapLocation location : regionData.getLocations()) {
                if (location.getId() == markerId && location.getTargetActivityClass() != null) {
                    try {
                        Intent intent = new Intent(this, location.getTargetActivityClass());
                        startActivity(intent);
                        break;
                    } catch (Exception e) {
                        Toast.makeText(this, "Eroare la deschiderea activității: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    
    /**
     * Pornește activitatea de poveste pentru regiunea curentă
     */
    protected void startStoryActivity() {
        if (regionData != null && regionData.getStoryActivityClass() != null) {
            try {
                Intent intent = new Intent(this, regionData.getStoryActivityClass());
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Eroare la deschiderea activității de poveste: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Nu există o activitate de poveste disponibilă pentru această regiune.", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Pornește activitatea de joc pentru regiunea curentă
     */
    protected void startGameActivity() {
        if (regionData != null && regionData.getGameActivityClass() != null) {
            try {
                Intent intent = new Intent(this, regionData.getGameActivityClass());
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Eroare la deschiderea activității de joc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Nu există o activitate de joc disponibilă pentru această regiune.", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }
    
    /**
     * Afișează/ascunde cardul de selectare a layerelor
     */
    protected void toggleLayerSelection() {
        if (layerSelectionCard != null) {
            if (layerSelectionCard.getVisibility() == View.VISIBLE) {
                layerSelectionCard.setVisibility(View.GONE);
            } else {
                layerSelectionCard.setVisibility(View.VISIBLE);
                // Ascunde cardul de filtre dacă este vizibil
                if (searchFilterCard != null) {
                    searchFilterCard.setVisibility(View.GONE);
                }
            }
        }
    }
    
    /**
     * Comută între modul 2D și 3D
     */
    protected void toggle3dMode() {
        if (googleMap != null) {
            is3dMode = !is3dMode;
            
            // Dezactivăm temporar listener-ul pentru camera move pentru a evita actualizări inutile
            googleMap.setOnCameraMoveListener(null);
            
            if (is3dMode) {
                // Activăm modul 3D (înclinare la 45 de grade)
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    new com.google.android.gms.maps.model.CameraPosition.Builder()
                        .target(googleMap.getCameraPosition().target)
                        .zoom(googleMap.getCameraPosition().zoom)
                        .tilt(45)
                        .build()
                ));
                toggle3dButton.setImageResource(android.R.drawable.ic_menu_compass);
            } else {
                // Revenim la modul 2D (fără înclinare)
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    new com.google.android.gms.maps.model.CameraPosition.Builder()
                        .target(googleMap.getCameraPosition().target)
                        .zoom(googleMap.getCameraPosition().zoom)
                        .tilt(0)
                        .build()
                ));
                toggle3dButton.setImageResource(android.R.drawable.ic_menu_mapmode);
            }
            
            // Reactivăm listener-ul pentru camera move după schimbarea modului
            if (enableScalebarUpdates) {
                googleMap.setOnCameraIdleListener(() -> updateScalebar());
            }
        }
    }
    
    /**
     * Recentrează harta pe markerul selectat sau pe centrul regiunii
     */
    protected void recenterMap() {
        if (googleMap != null) {
            // Dezactivăm temporar listener-ul pentru camera move pentru a evita actualizări inutile
            googleMap.setOnCameraMoveListener(null);
            
            if (selectedMarker != null) {
                // Recentrăm pe markerul selectat
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(selectedMarker.getPosition()));
            } else if (regionData != null) {
                // Recentrăm pe centrul regiunii
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    regionData.getCenter(), regionData.getDefaultZoom()));
            }
            
            // Reactivăm listener-ul pentru camera move după recentrare
            if (enableScalebarUpdates) {
                googleMap.setOnCameraIdleListener(() -> updateScalebar());
            }
        }
    }
    
    /**
     * Afișează/ascunde cardul de filtrare pentru căutare
     */
    protected void toggleSearchFilter() {
        if (searchFilterCard != null) {
            if (searchFilterCard.getVisibility() == View.VISIBLE) {
                searchFilterCard.setVisibility(View.GONE);
            } else {
                searchFilterCard.setVisibility(View.VISIBLE);
                // Ascunde cardul de layere dacă este vizibil
                if (layerSelectionCard != null) {
                    layerSelectionCard.setVisibility(View.GONE);
                }
            }
        }
    }
    
    /**
     * Aplică filtrele selectate pentru markere
     */
    protected void applyFilters() {
        if (searchFilterCard != null && googleMap != null) {
            // Obținem referințe la checkbox-uri
            CheckBox filterVisited = findViewById(R.id.filterVisited);
            CheckBox filterNotVisited = findViewById(R.id.filterNotVisited);
            CheckBox filterCities = findViewById(R.id.filterCities);
            CheckBox filterNature = findViewById(R.id.filterNature);
            CheckBox filterCulture = findViewById(R.id.filterCulture);
            CheckBox filterHistory = findViewById(R.id.filterHistory);
            
            // Verificăm dacă checkbox-urile există
            boolean showVisited = filterVisited != null && filterVisited.isChecked();
            boolean showNotVisited = filterNotVisited != null && filterNotVisited.isChecked();
            boolean showCities = filterCities != null && filterCities.isChecked();
            boolean showNature = filterNature != null && filterNature.isChecked();
            boolean showCulture = filterCulture != null && filterCulture.isChecked();
            boolean showHistory = filterHistory != null && (filterHistory.isChecked() || filterHistory == null);
            
            // Dacă niciun filtru nu este selectat, afișăm toate markerele
            if (!showVisited && !showNotVisited && !showCities && !showNature && !showCulture && !showHistory) {
                showVisited = true;
                showNotVisited = true;
                showCities = true;
                showNature = true;
                showCulture = true;
                showHistory = true;
            }
            
            // Aplicăm filtrele pentru fiecare marker
            for (Map.Entry<Marker, Integer> entry : markerIdMap.entrySet()) {
                Marker marker = entry.getKey();
                Integer markerId = entry.getValue();
                
                if (marker != null && regionData != null) {
                    // Verificăm dacă locația este vizitată
                    boolean isVisited = pointsManager.isLocationVisited(regionData.getRegionName().toLowerCase(), markerId);
                    
                    // Găsim locația în lista de locații pentru a obține tipul
                    int locationType = RegionMapData.LOCATION_TYPE_OTHER;
                    for (RegionMapData.MapLocation location : regionData.getLocations()) {
                        if (location.getId() == markerId) {
                            locationType = location.getLocationType();
                            break;
                        }
                    }
                    
                    // Aplicăm filtrele
                    boolean showByVisitedStatus = (isVisited && showVisited) || (!isVisited && showNotVisited);
                    boolean showByType = false;
                    
                    // Verificăm tipul locației
                    switch (locationType) {
                        case RegionMapData.LOCATION_TYPE_CITY:
                            showByType = showCities;
                            break;
                        case RegionMapData.LOCATION_TYPE_NATURE:
                            showByType = showNature;
                            break;
                        case RegionMapData.LOCATION_TYPE_CULTURE:
                            showByType = showCulture;
                            break;
                        case RegionMapData.LOCATION_TYPE_HISTORY:
                            showByType = showHistory;
                            break;
                        default:
                            showByType = true; // Arătăm locațiile fără tip specific
                            break;
                    }
                    
                    // Afișăm sau ascundem markerul în funcție de filtre
                    marker.setVisible(showByVisitedStatus && showByType);
                }
            }
            
            // Ascundem cardul de filtre după aplicare
            searchFilterCard.setVisibility(View.GONE);
            
            // Afișăm un mesaj de confirmare
            Toast.makeText(this, "Filtre aplicate", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Inițializează chip-urile pentru legendă și le face interactive
     */
    private void initializeLegendChips() {
        Chip legendChipCity = findViewById(R.id.legendChipCity);
        Chip legendChipNature = findViewById(R.id.legendChipNature);
        Chip legendChipCulture = findViewById(R.id.legendChipCulture);
        Chip legendChipHistory = findViewById(R.id.legendChipHistory);
        Chip legendChipVisited = findViewById(R.id.legendChipVisited);
        
        // Adăugăm listeners pentru chip-uri
        if (legendChipCity != null) {
            legendChipCity.setOnClickListener(v -> {
                // Selectăm/deselectăm checkbox-ul corespunzător
                CheckBox filterCities = findViewById(R.id.filterCities);
                if (filterCities != null) {
                    filterCities.setChecked(!filterCities.isChecked());
                    // Aplicăm filtrele direct, fără a afișa cardul de filtre
                    applyFiltersQuickly();
                }
            });
        }
        
        if (legendChipNature != null) {
            legendChipNature.setOnClickListener(v -> {
                // Selectăm/deselectăm checkbox-ul corespunzător
                CheckBox filterNature = findViewById(R.id.filterNature);
                if (filterNature != null) {
                    filterNature.setChecked(!filterNature.isChecked());
                    // Aplicăm filtrele direct, fără a afișa cardul de filtre
                    applyFiltersQuickly();
                }
            });
        }
        
        if (legendChipCulture != null) {
            legendChipCulture.setOnClickListener(v -> {
                // Selectăm/deselectăm checkbox-ul corespunzător
                CheckBox filterCulture = findViewById(R.id.filterCulture);
                if (filterCulture != null) {
                    filterCulture.setChecked(!filterCulture.isChecked());
                    // Aplicăm filtrele direct, fără a afișa cardul de filtre
                    applyFiltersQuickly();
                }
            });
        }
        
        if (legendChipHistory != null) {
            legendChipHistory.setOnClickListener(v -> {
                // Selectăm/deselectăm checkbox-ul corespunzător
                CheckBox filterHistory = findViewById(R.id.filterHistory);
                if (filterHistory != null) {
                    filterHistory.setChecked(!filterHistory.isChecked());
                    // Aplicăm filtrele direct, fără a afișa cardul de filtre
                    applyFiltersQuickly();
                }
            });
        }
        
        if (legendChipVisited != null) {
            legendChipVisited.setOnClickListener(v -> {
                // Selectăm/deselectăm checkbox-ul corespunzător
                CheckBox filterVisited = findViewById(R.id.filterVisited);
                if (filterVisited != null) {
                    filterVisited.setChecked(!filterVisited.isChecked());
                    // Aplicăm filtrele direct, fără a afișa cardul de filtre
                    applyFiltersQuickly();
                }
            });
        }
    }
    
    /**
     * Aplică filtrele rapid, fără a afișa mesaje sau a modifica vizibilitatea cardului de filtre
     */
    private void applyFiltersQuickly() {
        if (googleMap != null) {
            // Obținem referințe la checkbox-uri
            CheckBox filterVisited = findViewById(R.id.filterVisited);
            CheckBox filterNotVisited = findViewById(R.id.filterNotVisited);
            CheckBox filterCities = findViewById(R.id.filterCities);
            CheckBox filterNature = findViewById(R.id.filterNature);
            CheckBox filterCulture = findViewById(R.id.filterCulture);
            CheckBox filterHistory = findViewById(R.id.filterHistory);
            
            // Verificăm dacă checkbox-urile există
            boolean showVisited = filterVisited != null && filterVisited.isChecked();
            boolean showNotVisited = filterNotVisited != null && filterNotVisited.isChecked();
            boolean showCities = filterCities != null && filterCities.isChecked();
            boolean showNature = filterNature != null && filterNature.isChecked();
            boolean showCulture = filterCulture != null && filterCulture.isChecked();
            boolean showHistory = filterHistory != null && (filterHistory.isChecked() || filterHistory == null);
            
            // Dacă niciun filtru nu este selectat, afișăm toate markerele
            if (!showVisited && !showNotVisited && !showCities && !showNature && !showCulture && !showHistory) {
                showVisited = true;
                showNotVisited = true;
                showCities = true;
                showNature = true;
                showCulture = true;
                showHistory = true;
            }
            
            // Aplicăm filtrele pentru fiecare marker
            for (Map.Entry<Marker, Integer> entry : markerIdMap.entrySet()) {
                Marker marker = entry.getKey();
                Integer markerId = entry.getValue();
                
                if (marker != null && regionData != null) {
                    // Verificăm dacă locația este vizitată
                    boolean isVisited = pointsManager.isLocationVisited(regionData.getRegionName().toLowerCase(), markerId);
                    
                    // Găsim locația în lista de locații pentru a obține tipul
                    int locationType = RegionMapData.LOCATION_TYPE_OTHER;
                    for (RegionMapData.MapLocation location : regionData.getLocations()) {
                        if (location.getId() == markerId) {
                            locationType = location.getLocationType();
                            break;
                        }
                    }
                    
                    // Aplicăm filtrele
                    boolean showByVisitedStatus = (isVisited && showVisited) || (!isVisited && showNotVisited);
                    boolean showByType = false;
                    
                    // Verificăm tipul locației
                    switch (locationType) {
                        case RegionMapData.LOCATION_TYPE_CITY:
                            showByType = showCities;
                            break;
                        case RegionMapData.LOCATION_TYPE_NATURE:
                            showByType = showNature;
                            break;
                        case RegionMapData.LOCATION_TYPE_CULTURE:
                            showByType = showCulture;
                            break;
                        case RegionMapData.LOCATION_TYPE_HISTORY:
                            showByType = showHistory;
                            break;
                        default:
                            showByType = true; // Arătăm locațiile fără tip specific
                            break;
                    }
                    
                    // Afișăm sau ascundem markerul în funcție de filtre
                    marker.setVisible(showByVisitedStatus && showByType);
                }
            }
        }
    }
} 