package com.example.myapplication.olteniausage;

import androidx.fragment.app.FragmentActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.Oltenia;
import com.example.myapplication.RomApp.PointsManager;

public class OlteniaMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private PointsManager pointsManager;
    
    // Define city coordinates
    private final LatLng CRAIOVA = new LatLng(44.3190, 23.7967);
    private final LatLng TARGU_JIU = new LatLng(45.0364, 23.2747);
    private final LatLng DROBETA = new LatLng(44.6253, 22.6599);
    private final LatLng RAMNICU_VALCEA = new LatLng(45.1006, 24.3671);
    private final LatLng SLATINA = new LatLng(44.4289, 24.3693);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oltenia_map);
        
        // Initialize PointsManager
        pointsManager = PointsManager.getInstance(this);
        
        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Add markers for Oltenia cities
        mMap.addMarker(new MarkerOptions().position(CRAIOVA).title("Craiova"));
        mMap.addMarker(new MarkerOptions().position(TARGU_JIU).title("Târgu Jiu"));
        mMap.addMarker(new MarkerOptions().position(DROBETA).title("Drobeta-Turnu Severin"));
        mMap.addMarker(new MarkerOptions().position(RAMNICU_VALCEA).title("Râmnicu Vâlcea"));
        mMap.addMarker(new MarkerOptions().position(SLATINA).title("Slatina"));
        
        // Set the map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        
        // Move camera to Oltenia region
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CRAIOVA, 7.5f));
        
        // Set marker click listener
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String title = marker.getTitle();
        if (title != null) {
            Toast.makeText(this, "Ai descoperit " + title, Toast.LENGTH_SHORT).show();
            
            // Award points for discovering cities
            pointsManager.addPoints(this, "Oltenia", 25);
            
            if (title.equals("Craiova")) {
                // Navigate directly to the correct Craiova activity
                try {
                    // Try to use the Craiova class from RomApp package
                    Class<?> craiovaClass = Class.forName("com.example.myapplication.RomApp.Craiova");
                    Intent intent = new Intent(this, craiovaClass);
                    startActivity(intent);
                } catch (ClassNotFoundException e) {
                    Toast.makeText(this, "Nu s-a putut deschide pagina pentru Craiova", Toast.LENGTH_LONG).show();
                }
                return true;
            } else if (title.equals("Târgu Jiu")) {
                // Open Targu Jiu city specific activity/game
                Toast.makeText(this, "Descoperi Târgu Jiu, orașul lui Brâncuși", Toast.LENGTH_LONG).show();
            } else if (title.equals("Drobeta-Turnu Severin")) {
                // Open Drobeta city specific activity/game
                Toast.makeText(this, "Descoperi Drobeta-Turnu Severin", Toast.LENGTH_LONG).show();
            } else if (title.equals("Râmnicu Vâlcea")) {
                // Open Ramnicu Valcea city specific activity/game
                Toast.makeText(this, "Descoperi Râmnicu Vâlcea", Toast.LENGTH_LONG).show();
            } else if (title.equals("Slatina")) {
                // Open Slatina city specific activity/game
                Toast.makeText(this, "Descoperi Slatina", Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }
    
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Oltenia.class);
        startActivity(intent);
        finish();
    }
} 