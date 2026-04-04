package com.example.myapplication.bucovinausage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.core.domain.model.RegionMapDataProvider;
import com.example.myapplication.core.domain.model.BaseMapActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.chip.Chip;

public class BucovinaMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm datele regiunii Bucovina
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("bucovina"));
        
        // Inițializăm harta
        initializeMap();
        
        // Configurăm legendele specifice pentru Bucovina
        setupBucovinaLegends();
        
        // Actualizăm textul de instrucțiuni
        TextView instructionsText = findViewById(R.id.instructionsText);
        if (instructionsText != null) {
            instructionsText.setText("Explorează Bucovina! Descoperă mănăstirile pictate și tradițiile locale.");
        }
    }
    
    /**
     * Configurăm legendele specifice pentru Bucovina
     */
    private void setupBucovinaLegends() {
        // Obținem referințe la chip-urile din legendă
        Chip cityChip = findViewById(R.id.legendChipCity);
        Chip natureChip = findViewById(R.id.legendChipNature);
        Chip cultureChip = findViewById(R.id.legendChipCulture);
        Chip historyChip = findViewById(R.id.legendChipHistory);
        Chip visitedChip = findViewById(R.id.legendChipVisited);
        
        // Setăm textele specifice pentru Bucovina
        if (cityChip != null) {
            cityChip.setText("Orașe");
            cityChip.setChipBackgroundColorResource(R.color.bucovina_accent);
            cityChip.setOnClickListener(v -> filterMarkersByType("city"));
        }
        
        if (natureChip != null) {
            natureChip.setText("Natură");
            natureChip.setChipBackgroundColorResource(R.color.bucovina_primary);
            natureChip.setOnClickListener(v -> filterMarkersByType("nature"));
        }
        
        if (cultureChip != null) {
            cultureChip.setText("Mănăstiri");
            cultureChip.setChipBackgroundColorResource(R.color.bucovina_secondary);
            cultureChip.setOnClickListener(v -> filterMarkersByType("monastery"));
        }
        
        if (historyChip != null) {
            historyChip.setText("Tradiții");
            historyChip.setChipBackgroundColorResource(R.color.bucovina_tertiary);
            historyChip.setOnClickListener(v -> filterMarkersByType("traditions"));
        }
        
        if (visitedChip != null) {
            visitedChip.setText("Vizitate");
            visitedChip.setChipBackgroundColorResource(R.color.bucovina_quaternary);
            visitedChip.setOnClickListener(v -> filterMarkersByVisited());
        }
    }
    
    /**
     * Filtrează markerele după tip
     * @param type Tipul de markere de afișat
     */
    private void filterMarkersByType(String type) {
        Toast.makeText(this, "Filtrare după: " + type, Toast.LENGTH_SHORT).show();
        // Implementarea completă ar trebui să filtreze markerele
    }
    
    /**
     * Filtrează markerele după starea de vizitare
     */
    private void filterMarkersByVisited() {
        Toast.makeText(this, "Afișare locații vizitate", Toast.LENGTH_SHORT).show();
        // Implementarea completă ar trebui să filtreze markerele
    }
    
    @Override
    protected void addMapMarkers() {
        if (googleMap == null) return;
        
        // Adăugăm markeri pentru locațiile importante din Bucovina
        addMarker(
            "Suceava", 
            "Capitala istorică a Bucovinei", 
            new LatLng(47.6635, 26.2732), 
            BitmapDescriptorFactory.HUE_RED,
            1
        );
        
        addMarker(
            "Mănăstirea Voroneț", 
            "Capela Sixtină a Estului", 
            new LatLng(47.5178, 25.8631), 
            BitmapDescriptorFactory.HUE_AZURE,
            2
        );
        
        addMarker(
            "Mănăstirea Moldovița", 
            "Mănăstire pictată UNESCO", 
            new LatLng(47.6500, 25.5400), 
            BitmapDescriptorFactory.HUE_AZURE,
            3
        );
        
        addMarker(
            "Mănăstirea Sucevița", 
            "Mănăstire fortificată", 
            new LatLng(47.7800, 25.7100), 
            BitmapDescriptorFactory.HUE_AZURE,
            4
        );
        
        addMarker(
            "Gura Humorului", 
            "Stațiune turistică", 
            new LatLng(47.5500, 25.8900), 
            BitmapDescriptorFactory.HUE_RED,
            5
        );
        
        // Centrăm harta pe Bucovina
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
            new LatLng(47.6500, 25.8000), 
            9.0f
        ));
    }
    
    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "bucovina", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("bucovina", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Afișăm un toast cu informații
        Toast.makeText(this, "Ai descoperit un nou loc în Bucovina! +25 puncte", Toast.LENGTH_SHORT).show();
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }
    
    @Override
    protected void startStoryActivity() {
        Intent intent = new Intent(this, BucovinaStoryActivity.class);
        startActivity(intent);
    }
    
    @Override
    protected void startGameActivity() {
        Intent intent = new Intent(this, BucovinaGameActivity.class);
        startActivity(intent);
    }
} 
