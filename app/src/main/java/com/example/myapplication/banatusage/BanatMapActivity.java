package com.example.myapplication.banatusage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.model.RegionMapDataProvider;
import com.example.myapplication.model.base.BaseMapActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.chip.Chip;

public class BanatMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm datele regiunii Banat
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("banat"));
        
        // Inițializăm harta
        initializeMap();
        
        // Configurăm legendele specifice pentru Banat
        setupBanatLegends();
        
        // Actualizăm textul de instrucțiuni
        TextView instructionsText = findViewById(R.id.instructionsText);
        if (instructionsText != null) {
            instructionsText.setText("Explorează Banatul! Descoperă diversitatea culturală și peisajele spectaculoase.");
        }
    }
    
    /**
     * Configurăm legendele specifice pentru Banat
     */
    private void setupBanatLegends() {
        // Obținem referințe la chip-urile din legendă
        Chip cityChip = findViewById(R.id.legendChipCity);
        Chip natureChip = findViewById(R.id.legendChipNature);
        Chip cultureChip = findViewById(R.id.legendChipCulture);
        Chip historyChip = findViewById(R.id.legendChipHistory);
        Chip visitedChip = findViewById(R.id.legendChipVisited);
        
        // Setăm textele specifice pentru Banat
        if (cityChip != null) {
            cityChip.setText("Orașe");
            cityChip.setChipBackgroundColorResource(R.color.banat_accent);
            cityChip.setOnClickListener(v -> filterMarkersByType("city"));
        }
        
        if (natureChip != null) {
            natureChip.setText("Natură");
            natureChip.setChipBackgroundColorResource(R.color.banat_primary);
            natureChip.setOnClickListener(v -> filterMarkersByType("nature"));
        }
        
        if (cultureChip != null) {
            cultureChip.setText("Cultură");
            cultureChip.setChipBackgroundColorResource(R.color.banat_secondary);
            cultureChip.setOnClickListener(v -> filterMarkersByType("culture"));
        }
        
        if (historyChip != null) {
            historyChip.setText("Istorie");
            historyChip.setChipBackgroundColorResource(R.color.banat_tertiary);
            historyChip.setOnClickListener(v -> filterMarkersByType("history"));
        }
        
        if (visitedChip != null) {
            visitedChip.setText("Vizitate");
            visitedChip.setChipBackgroundColorResource(R.color.banat_quaternary);
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
        
        // Adăugăm markeri pentru locațiile importante din Banat
        addMarker(
            "Timișoara", 
            "Capitala Banatului", 
            new LatLng(45.7489, 21.2087), 
            BitmapDescriptorFactory.HUE_RED,
            1
        );
        
        addMarker(
            "Reșița", 
            "Centru industrial", 
            new LatLng(45.3000, 21.8900), 
            BitmapDescriptorFactory.HUE_RED,
            2
        );
        
        addMarker(
            "Băile Herculane", 
            "Stațiune balneară", 
            new LatLng(44.8800, 22.4147), 
            BitmapDescriptorFactory.HUE_GREEN,
            3
        );
        
        addMarker(
            "Caransebeș", 
            "Oraș istoric", 
            new LatLng(45.4100, 22.2200), 
            BitmapDescriptorFactory.HUE_RED,
            4
        );
        
        addMarker(
            "Cheile Nerei", 
            "Rezervație naturală", 
            new LatLng(44.9500, 21.8000), 
            BitmapDescriptorFactory.HUE_GREEN,
            5
        );
        
        // Centrăm harta pe Banat
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
            new LatLng(45.3000, 21.8900), 
            8.0f
        ));
    }
    
    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "banat", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("banat", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Afișăm un toast cu informații
        Toast.makeText(this, "Ai descoperit un nou loc în Banat! +25 puncte", Toast.LENGTH_SHORT).show();
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }
    
    @Override
    protected void startStoryActivity() {
        Intent intent = new Intent(this, BanatStoryActivity.class);
        startActivity(intent);
    }
    
    @Override
    protected void startGameActivity() {
        Intent intent = new Intent(this, BanatGameActivity.class);
        startActivity(intent);
    }
} 