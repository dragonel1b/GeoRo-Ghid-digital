package com.example.myapplication.bucovinausage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.Bucovina;
import com.example.myapplication.RomApp.PointsManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class BucovinaMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private MapView mapView;
    private GoogleMap googleMap;
    private ImageView backButton;
    private PointsManager pointsManager;
    
    // Coordonatele orașelor din Bucovina
    private static final LatLng SUCEAVA = new LatLng(47.6635, 26.2732);
    private static final LatLng GURA_HUMORULUI = new LatLng(47.5547, 25.8896);
    private static final LatLng RADAUTI = new LatLng(47.8437, 25.9206);
    private static final LatLng CAMPULUNG_MOLDOVENESC = new LatLng(47.5262, 25.5630);
    private static final LatLng VATRA_DORNEI = new LatLng(47.3504, 25.3594);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucovina_map);
        
        // Initialize basic components
        pointsManager = PointsManager.getInstance(this);
        
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
                    .position(SUCEAVA)
                    .title("Suceava")
                    .snippet("Capitala istorică a Bucovinei"));
    
            googleMap.addMarker(new MarkerOptions()
                    .position(GURA_HUMORULUI)
                    .title("Gura Humorului")
                    .snippet("Poartă către mănăstirile pictate"));
    
            googleMap.addMarker(new MarkerOptions()
                    .position(RADAUTI)
                    .title("Rădăuți")
                    .snippet("Oraș cu bogate tradiții culturale"));
    
            googleMap.addMarker(new MarkerOptions()
                    .position(CAMPULUNG_MOLDOVENESC)
                    .title("Câmpulung Moldovenesc")
                    .snippet("Așezare pitorească în inima Bucovinei"));
    
            googleMap.addMarker(new MarkerOptions()
                    .position(VATRA_DORNEI)
                    .title("Vatra Dornei")
                    .snippet("Renumită stațiune balneoclimaterică"));
    
            // Centrăm harta pe Bucovina
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SUCEAVA, 8));
        } catch (Exception e) {
            Toast.makeText(this, "Eroare la adăugarea markerelor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String title = marker.getTitle();
        if (title != null) {
            switch (title) {
                case "Suceava":
                    try {
                        Class<?> suceavaClass = Class.forName("com.example.myapplication.RomApp.Suceava");
                        startActivity(new Intent(this, suceavaClass));
                    } catch (ClassNotFoundException e) {
                        Toast.makeText(this, "Activitatea pentru Suceava va fi disponibilă în curând!", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                case "Gura Humorului":
                case "Rădăuți":
                case "Câmpulung Moldovenesc":
                case "Vatra Dornei":
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