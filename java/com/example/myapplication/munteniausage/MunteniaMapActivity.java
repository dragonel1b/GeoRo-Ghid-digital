package com.example.myapplication.munteniausage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.Muntenia;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.viewmodel.MunteniaViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MunteniaMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MunteniaViewModel viewModel;
    private TextView progressText;
    private GoogleMap mMap;
    private PointsManager pointsManager;
    private Set<String> visitedLocations;
    private static final String PREFS_NAME = "MunteniaMapPrefs";
    private static final String VISITED_LOCATIONS_KEY = "visitedLocations";
    private List<MunteniaLocation> locations;
    private Map<Integer, Marker> markers = new HashMap<>();

    // Coordonate pentru locații din Muntenia
    private final LatLng BUCURESTI = new LatLng(44.4268, 26.1025);
    private final LatLng PLOIESTI = new LatLng(44.9436, 26.0279);
    private final LatLng TARGOVISTE = new LatLng(44.9254, 25.4569);
    private final LatLng SINAIA = new LatLng(45.3500, 25.5667);
    private final LatLng CURTEA_DE_ARGES = new LatLng(45.1487, 24.6736);
    
    // Centrul regiunii Muntenia pentru zoom inițial
    private final LatLng MUNTENIA_CENTER = new LatLng(44.9436, 25.6279);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set theme before super.onCreate
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muntenia_map_google);

        // Initialize ViewModel
        viewModel = new MunteniaViewModel(this, "muntenia");

        // Initialize views
        progressText = findViewById(R.id.progressText);
        // legendContainer = findViewById(R.id.legendContainer); // Commented out - ID not in layout
        View backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        
        // Initialize PointsManager
        pointsManager = PointsManager.getInstance(this);

        // Load visited locations
        visitedLocations = new HashSet<>(
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getStringSet(VISITED_LOCATIONS_KEY, new HashSet<>())
        );

        // Obține fragmentul de hartă și solicită notificarea când harta este gata de utilizare
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Actualizează progresul
        updateProgress();

        // Create Muntenia locations
        createLocations();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Configurează harta
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        
        // Adaugă markeri pentru locațiile principale
        addLocationMarker(BUCURESTI, "București", 1);
        addLocationMarker(PLOIESTI, "Ploiești", 2);
        addLocationMarker(TARGOVISTE, "Târgoviște", 3);
        addLocationMarker(SINAIA, "Sinaia", 4);
        addLocationMarker(CURTEA_DE_ARGES, "Curtea de Argeș", 5);
        
        // Mută camera la centrul Munteniei
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MUNTENIA_CENTER, 8));

        // Handle marker clicks
        mMap.setOnMarkerClickListener(marker -> {
            String locationName = marker.getTitle();
            int locationId = (int) marker.getTag();
            showLocationInfo(locationName, locationId);
            return true;
        });
    }

    private void addLocationMarker(LatLng position, String title, int locationId) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title(title);
                
        // Set color based on visited status
        if (isVisited(locationId)) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
        
        Marker marker = mMap.addMarker(markerOptions);
        marker.setTag(locationId);
        
        // Store marker in our map
        markers.put(locationId, marker);
        
        // Add click listener
        mMap.setOnMarkerClickListener(marker1 -> {
            int id = (int) marker1.getTag();
            MunteniaLocation location = getLocationById(id);
            if (location != null) {
                showLocationInfo(location.getName(), location.getId());
            }
            return true;
        });
    }

    private void showLocationInfo(String locationName, int locationId) {
        // Get location by ID
        MunteniaLocation location = getLocationById(locationId);
        if (location == null) return;
        
        // Check if location is already visited
        boolean visited = isVisited(locationId);
        
        // Create dialog with location info
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(locationName)
               .setMessage(location.getDescription());
        
        if (!visited) {
            // Add option to mark as visited
            builder.setPositiveButton("Marchează ca vizitat", (dialog, id) -> {
                // Mark as visited in preferences
                markAsVisited(locationId);
                
                // Add points for the user
                pointsManager.addPoints(this, "muntenia", location.getPoints());
                
                // Update marker color
                updateMarkerColor(locationId);
                
                // Update progress
                updateProgress();
                
                Toast.makeText(this, 
                        "Ai primit " + location.getPoints() + " puncte pentru vizitarea " + locationName, 
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
        String locationKey = "location_" + locationId;
        visitedLocations.add(locationKey);
        
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(locationKey, true)
                .putStringSet(VISITED_LOCATIONS_KEY, visitedLocations)
                .apply();
                
        // Check for completion reward
        checkForCompletionReward();
    }
    
    private boolean isVisited(int locationId) {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean("location_" + locationId, false);
    }
    
    private void updateMarkerColor(int locationId) {
        Marker marker = markers.get(locationId);
        if (marker != null) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }
    }
    
    private void updateProgress() {
        int completedCount = viewModel.getCompletedLocationsCount();
        int totalLocations = 5;
        String progressMessage = String.format("Progres: %d/%d locații vizitate", completedCount, totalLocations);
        progressText.setText(progressMessage);
        
        // Dacă toate locațiile sunt completate, afișează un mesaj special
        if (completedCount == totalLocations) {
            Toast.makeText(this, "Felicitări! Ai explorat toate locațiile din Muntenia!", Toast.LENGTH_LONG).show();
        }
    }
    
    private void checkForCompletionReward() {
        if (visitedLocations.size() == locations.size()) {
            // All locations visited, give bonus
            pointsManager.addPoints(this, "muntenia", 100);
            
            // Show completion dialog
            new AlertDialog.Builder(this)
                    .setTitle("Felicitări!")
                    .setMessage("Ai vizitat toate locațiile importante din Muntenia! Ai primit un bonus de 100 de puncte.")
                    .setPositiveButton("Mulțumesc!", (dialog, id) -> dialog.dismiss())
                    .show();
            
            // Set result for parent activity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("ALL_LOCATIONS_VISITED", true);
            setResult(RESULT_OK, resultIntent);
        }
    }
    
    private MunteniaLocation getLocationById(int id) {
        for (MunteniaLocation location : locations) {
            if (location.getId() == id) {
                return location;
            }
        }
        return null;
    }
    
    private void createLocations() {
        locations = new ArrayList<>();
        
        // Add București
        locations.add(new MunteniaLocation(
                1,
                "București",
                BUCURESTI,
                "Capitala României",
                "București este capitala României și cel mai important centru politic, economic și cultural al țării. Cu o istorie bogată, care datează din secolul al XV-lea, orașul a evoluat de la o mică așezare la un centru urban modern.\n\n" +
                "Atracții principale includ Palatul Parlamentului (a doua cea mai mare clădire administrativă din lume), Ateneul Român, Arcul de Triumf, numeroase muzee și parcuri.\n\n" +
                "Orașul este cunoscut și pentru viața sa de noapte vibrantă, restaurantele excelente și pentru centrul istoric (Lipscani) care oferă o varietate de baruri, cafenele și magazine.",
                25));
        
        // Add Ploiești
        locations.add(new MunteniaLocation(
                2,
                "Ploiești",
                PLOIESTI,
                "Capitala petrolului românesc",
                "Ploiești este unul dintre cele mai importante centre industriale din România, fiind cunoscut în special pentru industria petrolieră. Este primul oraș din lume în care s-a rafinat petrol la scară industrială, începând cu 1857.\n\n" +
                "Atracțiile principale includ Muzeul Național al Petrolului, Muzeul Ceasului \"Nicolae Simache\", Muzeul de Artă și parcurile frumoase din oraș.\n\n" +
                "Orașul se află aproape de regiunea viticolă Dealu Mare, oferind posibilitatea degustării unor vinuri excelente în împrejurimi.",
                20));
        
        // Add Sinaia
        locations.add(new MunteniaLocation(
                3,
                "Sinaia",
                SINAIA,
                "Perla Carpaților",
                "Sinaia, cunoscută și ca \"Perla Carpaților\", este o renumită stațiune montană din Valea Prahovei. Situată la o altitudine de aproximativ 800-1000 m, orașul este înconjurat de peisaje montane spectaculoase.\n\n" +
                "Atracția principală este Castelul Peleș, fosta reședință de vară a regilor României, un edificiu impresionant în stil neo-renascentist german. Alte puncte de interes includ Mănăstirea Sinaia, Casino-ul Sinaia și traseele montane din împrejurimi.\n\n" +
                "În timpul iernii, Sinaia este o destinație populară pentru schi, având mai multe pârtii de diferite grade de dificultate.",
                25));
        
        // Add Pitești
        locations.add(new MunteniaLocation(
                4,
                "Pitești",
                CURTEA_DE_ARGES,
                "Orașul florilor",
                "Orașul este adesea numit \"Orașul Florilor\" datorită numeroaselor parcuri și grădini. Atracțiile principale includ Parcul Ștrand, Grădina Zoologică, Muzeul Județean Argeș și Biserica \"Sfântul Nicolae\".\n\n" +
                "Din Pitești se poate ajunge ușor la Curtea de Argeș, unde se află celebra Mănăstire Curtea de Argeș, și la Transfăgărășan, una dintre cele mai spectaculoase șosele din lume.",
                20));
        
        // Add Târgoviște
        locations.add(new MunteniaLocation(
                5,
                "Târgoviște",
                TARGOVISTE,
                "Fosta capitală a Țării Românești",
                "Târgoviște este un oraș cu o importanță istorică deosebită, fiind fosta capitală a Țării Românești între secolele XV-XVII. Orașul este legat de numele domnitorului Vlad Țepeș (Dracula), care a avut aici una dintre reședințele sale.\n\n" +
                "Principala atracție este Curtea Domnească, un complex ce include Turnul Chindiei, Ruinele Palatului Domnesc și Biserica Domnească. Alte puncte de interes sunt Muzeul de Istorie, Muzeul Tiparului și Cărții Vechi, și Muzeul Scriitorilor Dâmbovițeni.\n\n" +
                "Târgoviște păstrează farmecul unui oraș medieval, cu străzi înguste și clădiri istorice, oferind o incursiune autentică în istoria românească.",
                25));
    }

    private Muntenia getMunteniaParentActivity() {
        try {
            return (Muntenia) getParent();
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProgress();
        
        // Actualizează harta la revenire, dacă este disponibilă
        if (mMap != null) {
            mMap.clear();
            addLocationMarker(BUCURESTI, "București", 1);
            addLocationMarker(PLOIESTI, "Ploiești", 2);
            addLocationMarker(TARGOVISTE, "Târgoviște", 3);
            addLocationMarker(SINAIA, "Sinaia", 4);
            addLocationMarker(CURTEA_DE_ARGES, "Curtea de Argeș", 5);
        }
    }

    public void goBack(View view) {
        finish();
    }

    private static class MunteniaLocation {
        private final int id;
        private final String name;
        private final LatLng position;
        private final String shortDescription;
        private final String description;
        private final int points;

        public MunteniaLocation(int id, String name, LatLng position, String shortDescription, String description, int points) {
            this.id = id;
            this.name = name;
            this.position = position;
            this.shortDescription = shortDescription;
            this.description = description;
            this.points = points;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public LatLng getPosition() {
            return position;
        }
        
        public String getShortDescription() {
            return shortDescription;
        }

        public String getDescription() {
            return description;
        }

        public int getPoints() {
            return points;
        }
    }
} 