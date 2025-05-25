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
import com.example.myapplication.RomApp.PointsManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class CrisanaMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    
    private MapView mapView;
    private GoogleMap googleMap;
    private PointsManager pointsManager;
    private TextView pointsText;
    private List<POILocation> locations;
    private int discoveredLocations = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crisana_map);
        
        // Initialize points manager
        pointsManager = PointsManager.getInstance(this);
        
        // Initialize views
        pointsText = findViewById(R.id.pointsText);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        
        // Initialize locations
        initializeLocations();
        
        // Set up UI elements
        FloatingActionButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        
        // Update points display
        updatePointsDisplay();
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
            POILocation location = (POILocation) marker.getTag();
            if (location != null) {
                showLocationDetails(location);
            }
            return true;
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
                
                // Disable button after discovery
                discoverButton.setEnabled(false);
                discoverButton.setText("Descoperit");
            }
            
            dialog.dismiss();
        });
        
        // Disable button if already discovered
        if (location.isDiscovered()) {
            discoverButton.setEnabled(false);
            discoverButton.setText("Descoperit");
        }
        
        dialog.show();
    }
    
    private void showCompletionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Felicitări!");
        builder.setMessage("Ai descoperit toate locațiile importante din Crișana! " +
                "Ai primit un bonus de 100 de puncte!");
        
        // Award bonus points for completing all discoveries
        pointsManager.addPoints(this, "crisana", 100);
        updatePointsDisplay();
        
        builder.setPositiveButton("OK", null);
        builder.show();
    }
    
    private void updatePointsDisplay() {
        int points = pointsManager.getPoints(this);
        pointsText.setText(String.format("Puncte: %d", points));
    }
    
    // MapView lifecycle methods
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
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    
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
} 