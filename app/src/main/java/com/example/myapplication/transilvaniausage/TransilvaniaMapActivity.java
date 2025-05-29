package com.example.myapplication.transilvaniausage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.ClujNapoca;
import com.example.myapplication.RomApp.Brasov;
import com.example.myapplication.RomApp.Sibiu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

public class TransilvaniaMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private MapView mapView;
    private GoogleMap googleMap;
    private MaterialButton backButton;

    // Coordonatele orașelor din Transilvania
    private static final LatLng CLUJ_NAPOCA = new LatLng(46.7712, 23.6236);
    private static final LatLng BRASOV = new LatLng(45.6427, 25.5887);
    private static final LatLng SIBIU = new LatLng(45.7983, 24.1256);
    private static final LatLng SIGHISOARA = new LatLng(46.2197, 24.7922);
    private static final LatLng ALBA_IULIA = new LatLng(46.0732, 23.5848);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transilvania_map);

        // Inițializăm MapView și butonul înapoi
        mapView = findViewById(R.id.mapView);
        backButton = findViewById(R.id.backButton);

        // Verificăm dacă mapView există
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        } else {
            Toast.makeText(this, "Eroare la încărcarea hărții. Vă rugăm încercați din nou.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setăm acțiunea pentru butonul înapoi
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        } else {
            Toast.makeText(this, "Eroare la inițializarea butonului. Vă rugăm încercați din nou.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        
        if (googleMap == null) {
            Toast.makeText(this, "Eroare la încărcarea hărții Google Maps. Vă rugăm încercați din nou.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        googleMap.setOnMarkerClickListener(this);
        
        try {
            // Adăugăm markerii pentru orașe
            googleMap.addMarker(new MarkerOptions()
                    .position(CLUJ_NAPOCA)
                    .title("Cluj-Napoca")
                    .snippet("Capitala neoficială a Transilvaniei"));
    
            googleMap.addMarker(new MarkerOptions()
                    .position(BRASOV)
                    .title("Brașov")
                    .snippet("Orașul de la poalele Tâmpei"));
    
            googleMap.addMarker(new MarkerOptions()
                    .position(SIBIU)
                    .title("Sibiu")
                    .snippet("Fost Capitală Culturală Europeană"));
    
            googleMap.addMarker(new MarkerOptions()
                    .position(SIGHISOARA)
                    .title("Sighișoara")
                    .snippet("Cetate medievală UNESCO"));
    
            googleMap.addMarker(new MarkerOptions()
                    .position(ALBA_IULIA)
                    .title("Alba Iulia")
                    .snippet("Orașul Marii Uniri"));
    
            // Centrăm harta pe Transilvania
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CLUJ_NAPOCA, 7));
        } catch (Exception e) {
            Toast.makeText(this, "Eroare la adăugarea markerelor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String title = marker.getTitle();
        if (title != null) {
            switch (title) {
                case "Cluj-Napoca":
                    startActivity(new Intent(this, ClujNapoca.class));
                    return true;
                case "Brașov":
                    startActivity(new Intent(this, Brasov.class));
                    return true;
                case "Sibiu":
                    startActivity(new Intent(this, Sibiu.class));
                    return true;
                case "Sighișoara":
                case "Alba Iulia":
                    Toast.makeText(this, "Activitatea pentru " + title + " va fi disponibilă în curând!", Toast.LENGTH_SHORT).show();
                    return true;
            }
        }
        return false;
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