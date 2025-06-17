package com.example.myapplication.munteniausage;

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

public class MunteniaMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm datele regiunii Muntenia
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("muntenia"));
        
        // Inițializăm harta
        initializeMap();
        
        // Configurăm legendele specifice pentru Muntenia
        setupMunteniaLegends();
        
        // Actualizăm textul de instrucțiuni
        TextView instructionsText = findViewById(R.id.instructionsText);
        if (instructionsText != null) {
            instructionsText.setText("Explorează Muntenia! Descoperă București și Valea Prahovei.");
        }
    }
    
    /**
     * Configurăm legendele specifice pentru Muntenia
     */
    private void setupMunteniaLegends() {
        // Obținem referințe la chip-urile din legendă
        Chip cityChip = findViewById(R.id.legendChipCity);
        Chip natureChip = findViewById(R.id.legendChipNature);
        Chip cultureChip = findViewById(R.id.legendChipCulture);
        Chip historyChip = findViewById(R.id.legendChipHistory);
        Chip visitedChip = findViewById(R.id.legendChipVisited);
        
        // Setăm textele specifice pentru Muntenia
        if (cityChip != null) {
            cityChip.setText("Orașe");
            cityChip.setChipBackgroundColorResource(R.color.muntenia_accent);
        }
        
        if (natureChip != null) {
            natureChip.setText("Munți");
            natureChip.setChipBackgroundColorResource(R.color.muntenia_primary);
        }
        
        if (cultureChip != null) {
            cultureChip.setText("Cultură");
            cultureChip.setChipBackgroundColorResource(R.color.muntenia_secondary);
        }
        
        if (historyChip != null) {
            historyChip.setText("Istorie");
            historyChip.setChipBackgroundColorResource(R.color.muntenia_tertiary);
        }
        
        if (visitedChip != null) {
            visitedChip.setText("Vizitate");
            visitedChip.setChipBackgroundColorResource(R.color.muntenia_quaternary);
        }
    }

    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "muntenia", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("muntenia", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Afișăm un toast cu informații
        Toast.makeText(this, "Ai descoperit un nou loc în Muntenia! +25 puncte", Toast.LENGTH_SHORT).show();
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }

    @Override
    protected void addMapMarkers() {
        if (googleMap == null) return;
        
        // Adăugăm markeri pentru locațiile importante din Muntenia
        addMarker(
            "București", 
            "Capitala României", 
            new LatLng(44.4268, 26.1025), 
            BitmapDescriptorFactory.HUE_RED,
            1
        );
        
        addMarker(
            "Sinaia", 
            "Castelul Peleș", 
            new LatLng(45.3500, 25.5500), 
            BitmapDescriptorFactory.HUE_ORANGE,
            2
        );
        
        addMarker(
            "Ploiești", 
            "Centru petrolier", 
            new LatLng(44.9500, 26.0167), 
            BitmapDescriptorFactory.HUE_RED,
            3
        );
        
        addMarker(
            "Târgoviște", 
            "Vechea capitală a Țării Românești", 
            new LatLng(44.9333, 25.4500), 
            BitmapDescriptorFactory.HUE_ORANGE,
            4
        );
        
        addMarker(
            "Buzău", 
            "Vulcanii Noroioși", 
            new LatLng(45.1500, 26.8167), 
            BitmapDescriptorFactory.HUE_GREEN,
            5
        );
        
        // Centrăm harta pe Muntenia
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
            new LatLng(44.8000, 26.0000), 
            7.5f
        ));
    }
    
    @Override
    protected void startStoryActivity() {
        // Redirectăm către activitatea de poveste
        Intent intent = new Intent(this, MunteniaTourActivity.class);
        startActivity(intent);
    }
    
    @Override
    protected void startGameActivity() {
        // Redirectăm către activitatea de joc
        Intent intent = new Intent(this, MunteniaGameActivity.class);
        startActivity(intent);
    }
} 