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
        
        // Activăm butoanele pentru poveste și joc
        if (storyButton != null) {
            storyButton.setOnClickListener(v -> startStoryActivity());
        }
        
        if (gameButton != null) {
            gameButton.setOnClickListener(v -> startGameActivity());
        }
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
        // Adăugăm markeri pentru locațiile importante din Crișana
        addMarker(
            "Oradea", 
            "Capitala regiunii Crișana", 
            new LatLng(47.0722, 21.9422), 
            BitmapDescriptorFactory.HUE_RED,
            1
        );
        
        addMarker(
            "Băile Felix", 
            "Stațiune balneară renumită", 
            new LatLng(47.0167, 21.9167), 
            BitmapDescriptorFactory.HUE_CYAN,
            2
        );
        
        addMarker(
            "Arad", 
            "Oraș important din Crișana", 
            new LatLng(46.1667, 21.3167), 
            BitmapDescriptorFactory.HUE_RED,
            3
        );
        
        addMarker(
            "Salonta", 
            "Orașul lui Arany János", 
            new LatLng(46.8000, 21.6500), 
            BitmapDescriptorFactory.HUE_ORANGE,
            4
        );
        
        addMarker(
            "Moneasa", 
            "Stațiune balneoclimaterică", 
            new LatLng(46.5833, 22.3000), 
            BitmapDescriptorFactory.HUE_CYAN,
            5
        );
        
        addMarker(
            "Cetatea Șoimoș", 
            "Fortificație medievală", 
            new LatLng(46.1167, 21.4667), 
            BitmapDescriptorFactory.HUE_ORANGE,
            6
        );
    }
    
    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "crisana", 25);
        
        // Actualizăm textul de progres
        updateProgressText();
        
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