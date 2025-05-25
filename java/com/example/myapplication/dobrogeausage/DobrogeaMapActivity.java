package com.example.myapplication.dobrogeausage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.Constanta;
import com.example.myapplication.RomApp.Tulcea;
import com.example.myapplication.RomApp.Cernavoda;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

public class DobrogeaMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private MapView mapView;
    private GoogleMap googleMap;
    private MaterialButton backButton;

    // Coordonatele orașelor din Dobrogea
    private static final LatLng CONSTANTA = new LatLng(44.1598, 28.6348);
    private static final LatLng TULCEA = new LatLng(45.1795, 28.7967);
    private static final LatLng CERNOVODA = new LatLng(44.3386, 28.0328);
    private static final LatLng MEDGIDIA = new LatLng(44.2386, 28.2617);
    private static final LatLng MANGALIA = new LatLng(43.8153, 28.5825);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dobrogea_map);

        mapView = findViewById(R.id.mapView);
        backButton = findViewById(R.id.back_button);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        backButton.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setOnMarkerClickListener(this);
        
        // Adăugăm markerii pentru orașe
        googleMap.addMarker(new MarkerOptions()
                .position(CONSTANTA)
                .title("Constanța")
                .snippet("Orașul port al Mării Negre"));

        googleMap.addMarker(new MarkerOptions()
                .position(TULCEA)
                .title("Tulcea")
                .snippet("Poarta de intrare în Delta Dunării"));

        googleMap.addMarker(new MarkerOptions()
                .position(CERNOVODA)
                .title("Cernavodă")
                .snippet("Orașul podului peste Dunăre"));

        googleMap.addMarker(new MarkerOptions()
                .position(MEDGIDIA)
                .title("Medgidia")
                .snippet("Orașul cu tradiții agricole"));

        googleMap.addMarker(new MarkerOptions()
                .position(MANGALIA)
                .title("Mangalia")
                .snippet("Stațiune balneară istorică"));

        // Centrăm harta pe Dobrogea
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CONSTANTA, 9));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String title = marker.getTitle();
        if (title != null) {
            switch (title) {
                case "Constanța":
                    startActivity(new Intent(this, Constanta.class));
                    return true;
                case "Tulcea":
                    startActivity(new Intent(this, Tulcea.class));
                    return true;
                case "Cernavodă":
                    startActivity(new Intent(this, Cernavoda.class));
                    return true;
                case "Medgidia":
                case "Mangalia":
                    Toast.makeText(this, "Activitatea pentru " + title + " va fi disponibilă în curând!", Toast.LENGTH_SHORT).show();
                    return true;
            }
        }
        return false;
    }

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
}
