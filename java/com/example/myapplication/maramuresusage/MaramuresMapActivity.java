package com.example.myapplication.maramuresusage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.PointsManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaramuresMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PointsManager pointsManager;
    private Set<String> visitedLocations;
    private static final String PREFS_NAME = "MaramuresMapPrefs";
    private static final String VISITED_LOCATIONS_KEY = "visitedLocations";
    private TextView progressText;
    private LinearLayout legendContainer;
    private List<MaramuresLocation> locations;

    // Coordinate constants for Maramures attractions
    private static final LatLng BAIA_MARE = new LatLng(47.6667, 23.5833);
    private static final LatLng SIGHET = new LatLng(47.9333, 23.8833);
    private static final LatLng SAPANTA = new LatLng(47.9833, 23.7000);
    private static final LatLng BARSANA = new LatLng(47.7611, 24.1300);
    private static final LatLng IEUD = new LatLng(47.6544, 24.2603);
    private static final LatLng BORSA = new LatLng(47.6500, 24.6667);
    private static final LatLng VISEU_DE_SUS = new LatLng(47.7167, 24.4333);
    private static final LatLng MOISEI = new LatLng(47.6462, 24.5424);
    private static final LatLng DESESTI = new LatLng(47.7711, 23.8608);
    private static final LatLng BUDESTI = new LatLng(47.7172, 24.0564);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maramures_map);

        // Initialize PointsManager
        pointsManager = PointsManager.getInstance(this);

        // Set up UI elements
        progressText = findViewById(R.id.progressText);
        legendContainer = findViewById(R.id.legendContainer);
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Load visited locations
        visitedLocations = new HashSet<>(
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getStringSet(VISITED_LOCATIONS_KEY, new HashSet<>())
        );

        // Obtain the SupportMapFragment and get notified when the map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Create Maramures locations
        createLocations();
        
        // Update progress display
        updateProgressDisplay();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set map type to terrain for better mountain visualization
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        // Add markers for all attractions in Maramures
        addMarkers();

        // Set camera to show the Maramures region
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.7500, 24.1500), 9.0f));

        // Handle marker clicks
        mMap.setOnMarkerClickListener(marker -> {
            String locationName = marker.getTitle();
            int locationId = (int) marker.getTag();
            showLocationInfo(locationName, locationId);
            return true;
        });

        // Set map UI settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }

    private void addMarkers() {
        // Get SharedPreferences to check visited status
        boolean visited;

        // Marker for Baia Mare
        visited = isVisited(1);
        addMarker(BAIA_MARE, "Baia Mare", "Orașul reședință de județ", 1, visited);

        // Marker for Sighetu Marmației
        visited = isVisited(2);
        addMarker(SIGHET, "Sighetu Marmației", "Memorialul Victimelor Comunismului", 2, visited);

        // Marker for Săpânța
        visited = isVisited(3);
        addMarker(SAPANTA, "Săpânța", "Cimitirul Vesel", 3, visited);

        // Marker for Bârsana
        visited = isVisited(4);
        addMarker(BARSANA, "Bârsana", "Mănăstirea Bârsana", 4, visited);

        // Marker for Ieud
        visited = isVisited(5);
        addMarker(IEUD, "Ieud", "Biserica de lemn UNESCO", 5, visited);

        // Marker for Borșa
        visited = isVisited(6);
        addMarker(BORSA, "Borșa", "Stațiune montană", 6, visited);

        // Marker for Vișeu de Sus
        visited = isVisited(7);
        addMarker(VISEU_DE_SUS, "Vișeu de Sus", "Mocănița de pe Valea Vaserului", 7, visited);

        // Marker for Moisei
        visited = isVisited(8);
        addMarker(MOISEI, "Moisei", "Mănăstirea Moisei", 8, visited);

        // Marker for Desești
        visited = isVisited(9);
        addMarker(DESESTI, "Desești", "Biserica de lemn UNESCO", 9, visited);

        // Marker for Budești
        visited = isVisited(10);
        addMarker(BUDESTI, "Budești", "Biserica de lemn UNESCO", 10, visited);
    }

    private void addMarker(LatLng position, String title, String snippet, int locationId, boolean visited) {
        float markerColor = visited ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_RED;
        
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
                
        if (marker != null) {
            marker.setTag(locationId);
        }
    }

    private void showLocationInfo(String locationName, int locationId) {
        // Build location description based on locationId
        String locationDescription = getLocationDescription(locationId);
        
        // Check if location is already visited
        boolean visited = isVisited(locationId);
        
        // Create dialog with location info
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(locationName)
               .setMessage(locationDescription);
        
        if (!visited) {
            // Add option to mark as visited
            builder.setPositiveButton("Marchează ca vizitat", (dialog, id) -> {
                // Mark as visited in preferences
                markAsVisited(locationId);
                
                // Add points for the user
                pointsManager.addPoints(this, "maramures", 20);
                
                // Update marker color
                updateMarkerColor(locationId);
                
                // Update progress
                updateProgressDisplay();
                
                Toast.makeText(this, "Ai primit 20 de puncte pentru vizitarea " + locationName, 
                        Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("Închide", (dialog, id) -> dialog.dismiss());
        } else {
            // If already visited, just show close button
            builder.setPositiveButton("Închide", (dialog, id) -> dialog.dismiss());
        }
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void markAsVisited(int locationId) {
        // Save to SharedPreferences
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean("location_" + locationId, true)
                .apply();
                
        // Increment visited locations count
        visitedLocations.add("location_" + locationId);
        
        // Check for completion reward
        checkForCompletionReward();
    }
    
    private boolean isVisited(int locationId) {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean("location_" + locationId, false);
    }
    
    private void updateMarkerColor(int locationId) {
        // GoogleMap doesn't have a getMarkers() method, so we need to update markers in a different way
        // Refresh the markers instead
        if (mMap != null) {
            mMap.clear();
            addMarkers();
        }
    }
    
    private void updateProgressDisplay() {
        int visited = visitedLocations.size();
        int total = 10; // Assuming total locations are 10
        progressText.setText("Locații vizitate: " + visited + "/" + total);
    }
    
    private void checkForCompletionReward() {
        if (visitedLocations.size() == 10) {
            // All locations visited, give bonus
            pointsManager.addPoints(this, "maramures", 100);
            
            // Show congratulation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Felicitări!")
                    .setMessage("Ai vizitat toate locațiile din Maramureș! Ai primit un bonus de 100 de puncte.")
                    .setPositiveButton("Super!", (dialog, id) -> dialog.dismiss())
                    .show();
            
            // Save completion status
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putBoolean("map_completed", true)
                    .apply();
                    
            // Send result back to parent activity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("MAP_COMPLETED", true);
            setResult(RESULT_OK, resultIntent);
        }
    }
    
    private String getLocationDescription(int locationId) {
        switch (locationId) {
            case 1: // Baia Mare
                return "Baia Mare este reședința județului Maramureș și principalul centru urban " +
                       "al regiunii. Fostă așezare minieră cu o istorie de peste 650 de ani, " +
                       "orașul impresionează prin centrul vechi bine conservat, cu Turnul Ștefan, " +
                       "Biserica Sfântul Anton și case în stil baroc.";
            case 2: // Sighetu Marmației
                return "Sighetu Marmației este al doilea oraș ca mărime din Maramureș, situat la " +
                       "granița cu Ucraina. Cunoscut pentru Memorialul Victimelor Comunismului, " +
                       "Muzeul Satului Maramureșean și Casa Memorială Elie Wiesel.";
            case 3: // Săpânța
                return "Săpânța este faimoasă în întreaga lume pentru Cimitirul Vesel, o adevărată " +
                       "galerie de artă în aer liber cu crucile sale multicolore și epitafurile " +
                       "în versuri care descriu cu umor viața defuncților.";
            case 4: // Bârsana
                return "Bârsana este cunoscută pentru splendida sa mănăstire de maici, un ansamblu " +
                       "monastic impresionant construit în stil maramureșean, cu biserica din lemn " +
                       "inclusă în patrimoniul UNESCO. Complexul monastic include una dintre cele " +
                       "mai înalte construcții din lemn din Europa.";
            case 5: // Ieud
                return "Ieud găzduiește una dintre cele mai valoroase biserici de lemn din Maramureș, " +
                       "inclusă în patrimoniul UNESCO. În podul bisericii a fost descoperit celebrul " +
                       "Codex de la Ieud, unul dintre cele mai vechi documente scrise în limba română.";
            case 6: // Borșa
                return "Borșa este o stațiune montană situată la poalele Munților Rodnei, un important " +
                       "centru pentru sporturile de iarnă și turismul montan. Oferă acces către " +
                       "Rezervația Naturală Pietrosul Rodnei și peisaje montane spectaculoase.";
            case 7: // Vișeu de Sus
                return "Vișeu de Sus este cunoscut pentru celebra Mocăniță, o cale ferată forestieră " +
                       "cu locomotive cu abur care transportă turiști pe Valea Vaserului. Este una " +
                       "dintre ultimele căi ferate forestiere funcționale din Europa.";
            case 8: // Moisei
                return "Moisei găzduiește Mănăstirea Moisei, un important lăcaș de cult și loc de " +
                       "pelerinaj. În apropiere se află și Monumentul Victimelor Fascismului, ridicat " +
                       "în memoria victimelor masacrului din 1944.";
            case 9: // Desești
                return "Desești este un sat unde se află una dintre cele opt biserici de lemn din " +
                       "Maramureș incluse în patrimoniul UNESCO. Biserica 'Sfânta Paraschiva' se " +
                       "remarcă prin pictura interioară realizată în 1780 de Alexandru Ponehalschi.";
            case 10: // Budești
                return "Budești găzduiește Biserica 'Sfântul Nicolae', inclusă în patrimoniul UNESCO. " +
                       "Aceasta adăpostește o colecție unică de icoane pe sticlă și lemn, precum și " +
                       "cămașa de zale a legendarului haiduc Pintea Viteazul.";
            default:
                return "Informații indisponibile pentru această locație.";
        }
    }

    private void createLocations() {
        locations = new ArrayList<>();

        // Add main attractions in Maramures
        locations.add(new MaramuresLocation(
                "Baia Mare", 
                new LatLng(47.6567, 23.5787),
                "Capitala județului Maramureș și unul dintre cele mai importante centre urbane din regiune.",
                15
        ));
        
        locations.add(new MaramuresLocation(
                "Sighetu Marmației", 
                new LatLng(47.9284, 23.8891),
                "Orașul de pe malul Tisei, cunoscut pentru Memorialul Victimelor Comunismului.",
                15
        ));
        
        locations.add(new MaramuresLocation(
                "Săpânța", 
                new LatLng(47.9826, 23.6964),
                "Sat renumit pentru Cimitirul Vesel, un loc unic în lume unde moartea este tratată cu umor și culoare.",
                20
        ));
        
        locations.add(new MaramuresLocation(
                "Bârsana", 
                new LatLng(47.9373, 24.1273),
                "Sat cunoscut pentru mănăstirea impresionantă și biserica de lemn inclusă în patrimoniul UNESCO.",
                20
        ));
        
        locations.add(new MaramuresLocation(
                "Borșa", 
                new LatLng(47.6606, 24.6494),
                "Stațiune montană situată la poalele Munților Rodnei, populară pentru schi și drumeții.",
                15
        ));
        
        locations.add(new MaramuresLocation(
                "Moisei", 
                new LatLng(47.6434, 24.5406),
                "Localitate unde se află Monumentul Victimelor Fascismului și biserici de lemn vechi.",
                15
        ));
        
        locations.add(new MaramuresLocation(
                "Vișeu de Sus", 
                new LatLng(47.7117, 24.4268),
                "Cunoscut pentru Mocănița de pe Valea Vaserului, ultima cale ferată forestieră cu aburi din Europa.",
                20
        ));
        
        locations.add(new MaramuresLocation(
                "Ieud", 
                new LatLng(47.7754, 24.2631),
                "Sat cu o biserică de lemn din secolul al XVII-lea, inclusă în patrimoniul UNESCO.",
                20
        ));
    }

    // Class to store location information
    private static class MaramuresLocation {
        private final String name;
        private final LatLng position;
        private final String description;
        private final int points;

        public MaramuresLocation(String name, LatLng position, String description, int points) {
            this.name = name;
            this.position = position;
            this.description = description;
            this.points = points;
        }

        public String getName() {
            return name;
        }

        public LatLng getPosition() {
            return position;
        }

        public String getDescription() {
            return description;
        }

        public int getPoints() {
            return points;
        }
    }
} 