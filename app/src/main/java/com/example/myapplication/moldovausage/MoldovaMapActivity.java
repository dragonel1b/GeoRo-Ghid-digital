package com.example.myapplication.moldovausage;

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

public class MoldovaMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm datele regiunii Moldova
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("moldova"));
        
        // Inițializăm harta
        initializeMap();
        
        // Configurăm legendele specifice pentru Moldova
        setupMoldovaLegends();
        
        // Actualizăm textul de instrucțiuni
        TextView instructionsText = findViewById(R.id.instructionsText);
        if (instructionsText != null) {
            instructionsText.setText("Explorează Moldova! Descoperă mănăstirile pictate și dealurile viticole.");
        }
    }
    
    /**
     * Configurăm legendele specifice pentru Moldova
     */
    private void setupMoldovaLegends() {
        // Obținem referințe la chip-urile din legendă
        Chip cityChip = findViewById(R.id.legendChipCity);
        Chip natureChip = findViewById(R.id.legendChipNature);
        Chip cultureChip = findViewById(R.id.legendChipCulture);
        Chip historyChip = findViewById(R.id.legendChipHistory);
        Chip visitedChip = findViewById(R.id.legendChipVisited);
        
        // Setăm textele specifice pentru Moldova
        if (cityChip != null) {
            cityChip.setText("Orașe");
            cityChip.setChipBackgroundColorResource(R.color.moldova_accent);
            cityChip.setOnClickListener(v -> filterMarkersByType("city"));
        }
        
        if (natureChip != null) {
            natureChip.setText("Natură");
            natureChip.setChipBackgroundColorResource(R.color.moldova_primary);
            natureChip.setOnClickListener(v -> filterMarkersByType("nature"));
        }
        
        if (cultureChip != null) {
            cultureChip.setText("Mănăstiri");
            cultureChip.setChipBackgroundColorResource(R.color.moldova_secondary);
            cultureChip.setOnClickListener(v -> filterMarkersByType("monastery"));
        }
        
        if (historyChip != null) {
            historyChip.setText("Istorie");
            historyChip.setChipBackgroundColorResource(R.color.moldova_tertiary);
            historyChip.setOnClickListener(v -> filterMarkersByType("history"));
        }
        
        if (visitedChip != null) {
            visitedChip.setText("Vizitate");
            visitedChip.setChipBackgroundColorResource(R.color.moldova_quaternary);
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
        
        // Adăugăm markeri pentru locațiile importante din Moldova
        addMarker(
            "Iași", 
            "Capitala culturală a Moldovei", 
            new LatLng(47.1585, 27.6014), 
            BitmapDescriptorFactory.HUE_RED,
            1
        );
        
        addMarker(
            "Suceava", 
            "Fostă capitală a Moldovei", 
            new LatLng(47.6635, 26.2732), 
            BitmapDescriptorFactory.HUE_RED,
            2
        );
        
        addMarker(
            "Mănăstirea Voroneț", 
            "Capela Sixtină a Estului", 
            new LatLng(47.5178, 25.8631), 
            BitmapDescriptorFactory.HUE_AZURE,
            3
        );
        
        addMarker(
            "Piatra Neamț", 
            "Perla Moldovei", 
            new LatLng(46.9275, 26.3708), 
            BitmapDescriptorFactory.HUE_RED,
            4
        );
        
        addMarker(
            "Bacău", 
            "Centru economic important", 
            new LatLng(46.5670, 26.9145), 
            BitmapDescriptorFactory.HUE_RED,
            5
        );
        
        // Centrăm harta pe Moldova
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
            new LatLng(47.0000, 26.5000), 
            7.5f
        ));
    }
    
    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "moldova", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("moldova", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Afișăm un toast cu informații
        Toast.makeText(this, "Ai descoperit un nou loc în Moldova! +25 puncte", Toast.LENGTH_SHORT).show();
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }
    
    @Override
    protected void startStoryActivity() {
        Intent intent = new Intent(this, MoldovaStoryActivity.class);
        startActivity(intent);
    }
    
    @Override
    protected void startGameActivity() {
        Intent intent = new Intent(this, MoldovaGameActivity.class);
        startActivity(intent);
    }
} 
