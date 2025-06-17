package com.example.myapplication.crisanausage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.utils.PointsManager;
import com.example.myapplication.model.RegionMapDataProvider;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import com.example.myapplication.model.base.BaseMapActivity;

public class CrisanaMapActivity extends BaseMapActivity {
    
    private MapView mapView;
    private GoogleMap googleMap;
    private PointsManager pointsManager;
    private TextView pointsText;
    private List<POILocation> locations;
    private int discoveredLocations = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm datele regiunii Crișana
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("crisana"));
        
        // Initialize points manager
        pointsManager = PointsManager.getInstance(this);
        
        // Initialize locations
        initializeLocations();
        
        // Update points display
        updatePointsDisplay();
        
        // Inițializăm harta
        initializeMap();
        
        // Configurăm legendele specifice pentru Crișana
        setupCrisanaLegends();
        
        // Actualizăm textul de instrucțiuni
        TextView instructionsText = findViewById(R.id.instructionsText);
        if (instructionsText != null) {
            instructionsText.setText("Explorează Crișana! Descoperă atracțiile și tradițiile locale.");
        }
        
        // Activăm butoanele pentru poveste și joc
        if (storyButton != null) {
            storyButton.setOnClickListener(v -> startStoryActivity());
        }
        
        if (gameButton != null) {
            gameButton.setOnClickListener(v -> startGameActivity());
        }
    }
    
    /**
     * Configurăm legendele specifice pentru Crișana
     */
    private void setupCrisanaLegends() {
        // Obținem referințe la chip-urile din legendă
        Chip cityChip = findViewById(R.id.legendChipCity);
        Chip natureChip = findViewById(R.id.legendChipNature);
        Chip cultureChip = findViewById(R.id.legendChipCulture);
        Chip historyChip = findViewById(R.id.legendChipHistory);
        Chip visitedChip = findViewById(R.id.legendChipVisited);
        
        // Setăm textele specifice pentru Crișana
        if (cityChip != null) {
            cityChip.setText("Orașe");
            cityChip.setChipBackgroundColorResource(R.color.crisana_accent);
            cityChip.setOnClickListener(v -> filterMarkersByType("city"));
        }
        
        if (natureChip != null) {
            natureChip.setText("Natură");
            natureChip.setChipBackgroundColorResource(R.color.crisana_primary);
            natureChip.setOnClickListener(v -> filterMarkersByType("nature"));
        }
        
        if (cultureChip != null) {
            cultureChip.setText("Cultură");
            cultureChip.setChipBackgroundColorResource(R.color.crisana_secondary);
            cultureChip.setOnClickListener(v -> filterMarkersByType("culture"));
        }
        
        if (historyChip != null) {
            historyChip.setText("Istorie");
            historyChip.setChipBackgroundColorResource(R.color.crisana_tertiary);
            historyChip.setOnClickListener(v -> filterMarkersByType("history"));
        }
        
        if (visitedChip != null) {
            visitedChip.setText("Vizitate");
            visitedChip.setChipBackgroundColorResource(R.color.crisana_quaternary);
            visitedChip.setOnClickListener(v -> filterMarkersByVisited());
        }
    }
    
    /**
     * Filtrează markerele după tip
     * @param type Tipul de markere de afișat
     */
    private void filterMarkersByType(String type) {
        Toast.makeText(this, "Filtrare după: " + type, Toast.LENGTH_SHORT).show();
        // Implementarea completă ar trebui să filtreze markerele
    }
    
    /**
     * Filtrează markerele după starea de vizitare
     */
    private void filterMarkersByVisited() {
        Toast.makeText(this, "Afișare locații vizitate", Toast.LENGTH_SHORT).show();
        // Implementarea completă ar trebui să filtreze markerele
    }
    
    private void initializeLocations() {
        locations = new ArrayList<>();
        
        // Add POIs for Crisana region
        locations.add(new POILocation(
                "Centrul istoric Oradea", 
                new LatLng(47.0514, 21.9233),
                "Centrul istoric al Oradiei este cunoscut pentru clădirile Art Nouveau, printre care se numără " +
                "Palatul Vulturul Negru, Palatul Moskovits și Primăria.",
                R.drawable.poi_oradea_historic_center
        ));
        
        locations.add(new POILocation(
                "Cetatea Oradea", 
                new LatLng(47.0493, 21.9410),
                "Cetatea Oradea este o impresionantă fortificație în formă de stea cu cinci colțuri, " +
                "construită în stil Vauban în secolul al XVII-lea.",
                R.drawable.poi_oradea_fortress
        ));
        
        locations.add(new POILocation(
                "Băile Felix", 
                new LatLng(47.0088, 21.9177),
                "Băile Felix reprezintă cea mai mare stațiune balneară permanentă din România, " +
                "cunoscută pentru apele termale și nuferii tropicali care cresc în aer liber chiar și iarna.",
                R.drawable.poi_baile_felix
        ));
        
        locations.add(new POILocation(
                "Peștera Urșilor", 
                new LatLng(46.5522, 22.5695),
                "Descoperită accidental în 1975, Peștera Urșilor găzduiește fosile de Ursus spelaeus " +
                "(urs de cavernă) vechi de aproximativ 15.000 de ani.",
                R.drawable.poi_bears_cave
        ));
        
        locations.add(new POILocation(
                "Muzeul Țării Crișurilor", 
                new LatLng(47.0543, 21.9276),
                "Muzeul Țării Crișurilor este unul dintre cele mai importante muzee din nord-vestul " +
                "României, adăpostit în fostul Palat al Baroc.",
                R.drawable.poi_crisuri_museum
        ));
        
        locations.add(new POILocation(
                "Cetatea Aradului", 
                new LatLng(46.1782, 21.3182),
                "Cetatea Aradului este o fortificație în formă de stea, construită între 1763 și 1783, " +
                "reprezentând un exemplu important de arhitectură militară habsburgică.",
                R.drawable.poi_arad_fortress
        ));
        
        locations.add(new POILocation(
                "Salonta - Turnul Ciunt", 
                new LatLng(46.8007, 21.6579),
                "Turnul Ciunt din Salonta, construit în secolul al XVII-lea, este singurul vestigiu " +
                "rămas din cetatea medievală și adăpostește un muzeu memorial dedicat poetului János Arany.",
                R.drawable.poi_salonta_tower
        ));
    }
    
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        
        // Set map type and UI settings
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        
        // Center the map on Crisana region
        LatLng crisanaCenter = new LatLng(46.9431, 21.9683);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(crisanaCenter, 8f));
        
        // Add markers for all POI locations
        for (POILocation location : locations) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(location.getPosition())
                    .title(location.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag(location);
        }
        
        // Set up marker click listener
        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof POILocation) {
                POILocation location = (POILocation) tag;
                showLocationDetails(location);
                return true;
            }
            return super.onMarkerClick(marker);
        });
    }
    
    private void showLocationDetails(POILocation location) {
        View detailView = getLayoutInflater().inflate(R.layout.dialog_map_location, null);
        
        TextView titleText = detailView.findViewById(R.id.locationTitle);
        TextView descriptionText = detailView.findViewById(R.id.locationDescription);
        ImageView imageView = detailView.findViewById(R.id.locationImage);
        Button discoverButton = detailView.findViewById(R.id.discoverButton);
        
        titleText.setText(location.getName());
        descriptionText.setText(location.getDescription());
        
        if (location.getImageResource() != 0) {
            imageView.setImageResource(location.getImageResource());
        } else {
            imageView.setVisibility(View.GONE);
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(detailView);
        
        AlertDialog dialog = builder.create();
        
        // Set button function
        discoverButton.setOnClickListener(v -> {
            if (!location.isDiscovered()) {
                location.setDiscovered(true);
                discoveredLocations++;
                
                // Award points for discovery
                pointsManager.addPoints(this, "crisana", 25);
                updatePointsDisplay();
                
                // Show success message
                Toast.makeText(this, "Ai descoperit o nouă locație! +25 puncte", Toast.LENGTH_SHORT).show();
                
                // Check if all locations have been discovered
                if (discoveredLocations >= locations.size()) {
                    showCompletionDialog();
                }
            } else {
                Toast.makeText(this, "Ai descoperit deja această locație!", Toast.LENGTH_SHORT).show();
            }
            
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    private void showCompletionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Felicitări!");
        builder.setMessage("Ai descoperit toate locațiile importante din Crișana! Ai primit un bonus de 100 puncte!");
        builder.setPositiveButton("Continuă explorarea", (dialog, which) -> dialog.dismiss());
        
        // Award bonus points
        pointsManager.addPoints(this, "crisana", 100);
        updatePointsDisplay();
        
        builder.create().show();
    }
    
    private void updatePointsDisplay() {
        int points = pointsManager.getRegionPoints(this, "crisana");
        TextView pointsText = findViewById(R.id.progressText);
        if (pointsText != null) {
            pointsText.setText(String.format("Puncte: %d", points));
        }
    }
    
    // MapView lifecycle methods are handled by the parent class
    
    // POI Location class to store location data
    private static class POILocation {
        private final String name;
        private final LatLng position;
        private final String description;
        private final int imageResource;
        private boolean discovered = false;
        
        POILocation(String name, LatLng position, String description, int imageResource) {
            this.name = name;
            this.position = position;
            this.description = description;
            this.imageResource = imageResource;
        }
        
        String getName() {
            return name;
        }
        
        LatLng getPosition() {
            return position;
        }
        
        String getDescription() {
            return description;
        }
        
        int getImageResource() {
            return imageResource;
        }
        
        boolean isDiscovered() {
            return discovered;
        }
        
        void setDiscovered(boolean discovered) {
            this.discovered = discovered;
        }
    }

    @Override
    protected void addMapMarkers() {
        if (googleMap == null) return;
        
        // Adăugăm markere pentru locațiile importante din Crișana
        addMarker(
            "Oradea", 
            "Capitala Crișanei", 
            new LatLng(47.0514, 21.9233), 
            BitmapDescriptorFactory.HUE_RED,
            1
        );
        
        addMarker(
            "Arad", 
            "Oraș important din Crișana", 
            new LatLng(46.1865, 21.3122), 
            BitmapDescriptorFactory.HUE_RED,
            2
        );
        
        addMarker(
            "Băile Felix", 
            "Stațiune balneară", 
            new LatLng(47.0088, 21.9177), 
            BitmapDescriptorFactory.HUE_GREEN,
            3
        );
        
        addMarker(
            "Peștera Urșilor", 
            "Atracție naturală", 
            new LatLng(46.5522, 22.5695), 
            BitmapDescriptorFactory.HUE_GREEN,
            4
        );
        
        addMarker(
            "Salonta", 
            "Oraș istoric", 
            new LatLng(46.8007, 21.6579), 
            BitmapDescriptorFactory.HUE_RED,
            5
        );
        
        // Adăugăm și markerele pentru POI-urile specifice
        for (POILocation location : locations) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(location.getPosition())
                    .title(location.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag(location);
        }
        
        // Centrăm harta pe Crișana
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
            new LatLng(46.9431, 21.9683), 
            8.0f
        ));
    }
    
    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "crisana", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("crisana", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Afișăm un toast cu informații
        Toast.makeText(this, "Ai descoperit un nou loc în Crișana! +25 puncte", Toast.LENGTH_SHORT).show();
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }

    @Override
    protected void startStoryActivity() {
        Intent intent = new Intent(this, CrisanaStoryActivity.class);
        startActivity(intent);
    }

    @Override
    protected void startGameActivity() {
        Intent intent = new Intent(this, CrisanaGameActivity.class);
        startActivity(intent);
    }
} 