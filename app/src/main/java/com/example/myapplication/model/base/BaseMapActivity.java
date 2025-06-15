package com.example.myapplication.model.base;

import android.content.Intent;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.HashMap;
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
    protected Button storyButton;
    protected Button gameButton;
    
    // Map state
    protected RegionMapData regionData;
    protected PointsManager pointsManager;
    protected Map<Marker, Integer> markerIdMap = new HashMap<>();
    
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
        mapView = findViewById(R.id.mapView);
        backButton = findViewById(R.id.backButton);
        storyButton = findViewById(R.id.storyButton);
        gameButton = findViewById(R.id.gameButton);
        
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
        
        // Inițializăm managerul de puncte
        pointsManager = PointsManager.getInstance(this);
        
        // Actualizăm textul de progres
        updateProgressText();
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
            mapView.onCreate(null);
            mapView.getMapAsync(this);
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
        
        // Configurăm harta
        googleMap.setOnMarkerClickListener(this);
        
        try {
            // Adăugăm markeri pentru locațiile importante
            if (regionData != null) {
                addMapMarkersFromData();
                
                // Centrăm harta pe regiunea specificată
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    regionData.getCenterLocation(), 
                    regionData.getDefaultZoom()
                ));
            } else {
                // Dacă nu avem date pentru regiune, apelăm metoda abstractă
                addMapMarkers();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Eroare la adăugarea markerelor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Adaugă markeri pe hartă din datele regiunii
     */
    private void addMapMarkersFromData() {
        if (regionData == null || regionData.getLocations() == null) return;
        
        for (RegionMapData.MapLocation location : regionData.getLocations()) {
            Marker marker = addMarker(
                location.getTitle(),
                location.getDescription(),
                location.getPosition(),
                location.getMarkerColor(),
                location.getId()
            );
        }
    }
    
    /**
     * Adaugă markeri pe hartă pentru locațiile importante din regiune
     * Această metodă poate fi suprascrisă de fiecare activitate de hartă specifică regiunii
     */
    protected void addMapMarkers() {
        // Implementare implicită goală
    }
    
    /**
     * Adaugă un marker pe hartă
     * @param title Titlul markerului
     * @param snippet Descrierea markerului
     * @param position Poziția markerului
     * @param hue Culoarea markerului
     * @param id ID-ul markerului pentru identificare
     * @return Markerul adăugat
     */
    protected Marker addMarker(String title, String snippet, LatLng position, float hue, int id) {
        if (googleMap == null) return null;
        
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(hue)));
        
        if (marker != null) {
            markerIdMap.put(marker, id);
        }
        
        return marker;
    }
    
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker != null) {
            // Afișăm informațiile despre marker
            marker.showInfoWindow();
            
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
} 