package com.example.myapplication.olteniausage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.Oltenia;
import com.example.myapplication.model.RegionMapDataProvider;
import com.example.myapplication.model.base.BaseMapActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.chip.Chip;

public class OlteniaMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm datele regiunii Oltenia
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("oltenia"));
        
        // Inițializăm harta
        initializeMap();
        
        // Configurăm legendele specifice pentru Oltenia
        setupOlteniaLegends();
        
        // Actualizăm textul de instrucțiuni
        TextView instructionsText = findViewById(R.id.instructionsText);
        if (instructionsText != null) {
            instructionsText.setText("Explorează Oltenia! Apasă pe markeri pentru informații și apasă lung pentru a crea un traseu turistic.");
        }
    }
    
    /**
     * Configurăm legendele specifice pentru Oltenia
     */
    private void setupOlteniaLegends() {
        // Obținem referințe la chip-urile din legendă
        Chip cityChip = findViewById(R.id.legendChipCity);
        Chip natureChip = findViewById(R.id.legendChipNature);
        Chip cultureChip = findViewById(R.id.legendChipCulture);
        Chip historyChip = findViewById(R.id.legendChipHistory);
        Chip visitedChip = findViewById(R.id.legendChipVisited);
        
        // Setăm textele specifice pentru Oltenia
        if (cityChip != null) {
            cityChip.setText("Orașe");
            cityChip.setOnClickListener(v -> filterMarkersByType("city"));
        }
        
        if (natureChip != null) {
            natureChip.setText("Natură");
            natureChip.setOnClickListener(v -> filterMarkersByType("nature"));
        }
        
        if (cultureChip != null) {
            cultureChip.setText("Cultură");
            cultureChip.setOnClickListener(v -> filterMarkersByType("culture"));
        }
        
        if (historyChip != null) {
            historyChip.setText("Istorie");
            historyChip.setOnClickListener(v -> filterMarkersByType("history"));
        }
        
        if (visitedChip != null) {
            visitedChip.setText("Vizitate");
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
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "Oltenia", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("oltenia", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Animăm cardul de jos pentru feedback vizual
        animateBottomCard();
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }
    
    /**
     * Animează cardul de jos pentru feedback vizual
     */
    private void animateBottomCard() {
        View bottomCard = findViewById(R.id.bottomCard);
        if (bottomCard != null) {
            // Creăm o animație de pulsație
            Animation pulseAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            pulseAnimation.setDuration(300);
            
            // Aplicăm animația
            bottomCard.startAnimation(pulseAnimation);
        }
    }
    
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Oltenia.class);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void addMapMarkers() {
        if (googleMap == null) return;
        
        // Adăugăm markere pentru locațiile importante din Oltenia
        addMarker(
            "Craiova", 
            "Capitala Olteniei", 
            new LatLng(44.3167, 23.8000), 
            BitmapDescriptorFactory.HUE_RED,
            1
        );
        
        addMarker(
            "Târgu Jiu", 
            "Orașul lui Brâncuși", 
            new LatLng(45.0333, 23.2833), 
            BitmapDescriptorFactory.HUE_RED,
            2
        );
        
        addMarker(
            "Drobeta-Turnu Severin", 
            "Podul lui Traian", 
            new LatLng(44.6333, 22.6667), 
            BitmapDescriptorFactory.HUE_RED,
            3
        );
        
        addMarker(
            "Mănăstirea Tismana", 
            "Una dintre cele mai vechi mănăstiri din țară", 
            new LatLng(45.0667, 22.9500), 
            BitmapDescriptorFactory.HUE_AZURE,
            4
        );
        
        addMarker(
            "Horezu", 
            "Centru de ceramică tradițională", 
            new LatLng(45.1333, 24.0000), 
            BitmapDescriptorFactory.HUE_GREEN,
            5
        );
        
        // Centrăm harta pe Oltenia
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
            new LatLng(44.7500, 23.5000), 
            8.0f
        ));
    }
    
    @Override
    protected void startStoryActivity() {
        // Redirectăm către activitatea de poveste
        Intent intent = new Intent(this, OlteniaStoryActivity.class);
        startActivity(intent);
    }
    
    @Override
    protected void startGameActivity() {
        // Redirectăm către activitatea de joc
        Intent intent = new Intent(this, OlteniaGameActivity.class);
        startActivity(intent);
    }
} 