package com.example.myapplication.dobrogeausage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.Craiova;
import com.example.myapplication.RomApp.Drobetaturnuseverin;
import com.example.myapplication.RomApp.Slatina;
import com.example.myapplication.RomApp.TarguJiu;
import com.example.myapplication.RomApp.Valcea;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

public class OlteniaMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private MapView mapView;
    private GoogleMap googleMap;
    private MaterialButton backButton;

    // Coordinates of Oltenia cities
    private static final LatLng CRAIOVA = new LatLng(44.3302, 23.7949);
    private static final LatLng RAMNICU_VALCEA = new LatLng(45.1000, 24.3667);
    private static final LatLng DROBETATURNUSEVERIN = new LatLng(44.6369, 22.6569);
    private static final LatLng TARGU_JIU = new LatLng(45.0333, 23.2833);
    private static final LatLng SLATINA = new LatLng(44.4333, 24.3667);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oltenia_map);

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

        // Add markers for cities
        googleMap.addMarker(new MarkerOptions()
                .position(CRAIOVA)
                .title("Craiova")
                .snippet("Cel mai important oraș al Olteniei"));

        googleMap.addMarker(new MarkerOptions()
                .position(RAMNICU_VALCEA)
                .title("Râmnicu Vâlcea")
                .snippet("Centru turistic și cultural"));

        googleMap.addMarker(new MarkerOptions()
                .position(DROBETATURNUSEVERIN)
                .title("Drobeta-Turnu Severin")
                .snippet("Vestigii istorice importante"));

        googleMap.addMarker(new MarkerOptions()
                .position(TARGU_JIU)
                .title("Târgu Jiu")
                .snippet("Orașul lui Constantin Brâncuși"));

        googleMap.addMarker(new MarkerOptions()
                .position(SLATINA)
                .title("Slatina")
                .snippet("Centru industrial important"));

        // Center map on Oltenia region (Craiova)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CRAIOVA, 9));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String title = marker.getTitle();
        if (title != null) {
            switch (title) {
                case "Craiova":
                    try {
                        // Use class loader to find the correct Craiova class
                        Class<?> craiovaClass = Class.forName("com.example.myapplication.RomApp.Craiova");
                        Intent intent = new Intent(this, craiovaClass);
                        startActivity(intent);
                    } catch (ClassNotFoundException e) {
                        Toast.makeText(this, "Nu s-a putut deschide pagina pentru Craiova", Toast.LENGTH_LONG).show();
                    }
                    return true;
                case "Râmnicu Vâlcea":
                    startActivity(new Intent(this, Valcea.class));
                    return true;
                case "Drobeta-Turnu Severin":
                    startActivity(new Intent(this, Drobetaturnuseverin.class));
                    return true;
                case "Târgu Jiu":
                    startActivity(new Intent(this, TarguJiu.class));
                    return true;
                case "Slatina":
                    startActivity(new Intent(this, Slatina.class));
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
