package com.example.myapplication.moldovausage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
// Import removed
import com.example.myapplication.RomApp.Iasi;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MoldovaMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private MapView mapView;
    private GoogleMap googleMap;
    private ImageView backButton;

    // Coordonatele orașelor din Moldova
    private static final LatLng CHISINAU = new LatLng(47.0105, 28.8638);
    private static final LatLng IASI = new LatLng(47.1585, 27.6014);
    private static final LatLng SUCEAVA = new LatLng(47.6551, 26.2550);
    private static final LatLng BALTI = new LatLng(47.7631, 27.9293);
    private static final LatLng SOROCA = new LatLng(48.1569, 28.2887);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moldova_map);

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
                    .position(CHISINAU)
                    .title("Chișinău")
                    .snippet("Capitala Republicii Moldova"));
    
            googleMap.addMarker(new MarkerOptions()
                    .position(IASI)
                    .title("Iași")
                    .snippet("Capitala istorică a Moldovei"));
    
            googleMap.addMarker(new MarkerOptions()
                    .position(SUCEAVA)
                    .title("Suceava")
                    .snippet("Oraș cu cetate medievală"));
    
            googleMap.addMarker(new MarkerOptions()
                    .position(BALTI)
                    .title("Bălți")
                    .snippet("Al doilea oraș ca mărime din Republica Moldova"));
    
            googleMap.addMarker(new MarkerOptions()
                    .position(SOROCA)
                    .title("Soroca")
                    .snippet("Cunoscută pentru cetatea medievală"));
    
            // Centrăm harta pe Moldova
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(IASI, 7));
        } catch (Exception e) {
            Toast.makeText(this, "Eroare la adăugarea markerelor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String title = marker.getTitle();
        if (title != null) {
            switch (title) {
                case "Chișinău":
                    // Use Toast instead of missing activity
                    Toast.makeText(this, "Activitatea pentru Chișinău va fi disponibilă în curând!", Toast.LENGTH_SHORT).show();
                    return true;
                case "Iași":
                    startActivity(new Intent(this, Iasi.class));
                    return true;
                case "Soroca":
                    // Use Toast instead of missing activity
                    Toast.makeText(this, "Activitatea pentru Soroca va fi disponibilă în curând!", Toast.LENGTH_SHORT).show();
                    return true;
                case "Suceava":
                case "Bălți":
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