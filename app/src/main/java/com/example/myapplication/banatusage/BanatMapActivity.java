package com.example.myapplication.banatusage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.Timisoara;
import com.example.myapplication.RomApp.Resita;
import com.example.myapplication.RomApp.Lugoj;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BanatMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private MapView mapView;
    private GoogleMap googleMap;
    private FloatingActionButton backButton;

    // Coordonatele orașelor din Banat
    private static final LatLng TIMISOARA = new LatLng(45.7494, 21.2272);
    private static final LatLng RESITA = new LatLng(45.2971, 21.8908);
    private static final LatLng LUGOJ = new LatLng(45.6869, 21.9036);
    private static final LatLng CARANSEBES = new LatLng(45.4167, 22.2167);
    private static final LatLng HERCULANE = new LatLng(44.8811, 22.4144);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banat_map);

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
                    .position(TIMISOARA)
                    .title("Timișoara")
                    .snippet("Capitala Banatului"));
    
            googleMap.addMarker(new MarkerOptions()
                    .position(RESITA)
                    .title("Reșița")
                    .snippet("Centru industrial istoric"));
    
            googleMap.addMarker(new MarkerOptions()
                    .position(LUGOJ)
                    .title("Lugoj")
                    .snippet("Oraș cu bogată tradiție culturală"));
    
            googleMap.addMarker(new MarkerOptions()
                    .position(CARANSEBES)
                    .title("Caransebeș")
                    .snippet("Orașul de la confluența Timișului cu Sebeșul"));
    
            googleMap.addMarker(new MarkerOptions()
                    .position(HERCULANE)
                    .title("Băile Herculane")
                    .snippet("Stațiune balneară istorică"));
    
            // Centrăm harta pe Banat
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(TIMISOARA, 7));
        } catch (Exception e) {
            Toast.makeText(this, "Eroare la adăugarea markerelor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String title = marker.getTitle();
        if (title != null) {
            switch (title) {
                case "Timișoara":
                    startActivity(new Intent(this, Timisoara.class));
                    return true;
                case "Reșița":
                    startActivity(new Intent(this, Resita.class));
                    return true;
                case "Lugoj":
                    startActivity(new Intent(this, Lugoj.class));
                    return true;
                case "Caransebeș":
                case "Băile Herculane":
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