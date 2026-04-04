package com.example.myapplication.transilvaniausage;

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

public class TransilvaniaMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm datele regiunii Transilvania
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("transilvania"));
        
        // Inițializăm harta
        initializeMap();
        
        // Configurăm legendele specifice pentru Transilvania
        setupTransilvaniaLegends();
        
        // Actualizăm textul de instrucțiuni
        TextView instructionsText = findViewById(R.id.instructionsText);
        if (instructionsText != null) {
            instructionsText.setText("Explorează Transilvania! Descoperă cetățile medievale și legendele locale.");
        }
    }
    
    /**
     * Configurăm legendele specifice pentru Transilvania
     */
    private void setupTransilvaniaLegends() {
        // Obținem referințe la chip-urile din legendă
        Chip cityChip = findViewById(R.id.legendChipCity);
        Chip natureChip = findViewById(R.id.legendChipNature);
        Chip cultureChip = findViewById(R.id.legendChipCulture);
        Chip historyChip = findViewById(R.id.legendChipHistory);
        Chip visitedChip = findViewById(R.id.legendChipVisited);
        
        // Setăm textele specifice pentru Transilvania
        if (cityChip != null) {
            cityChip.setText("Orașe");
            cityChip.setChipBackgroundColorResource(R.color.transilvania_accent);
            cityChip.setOnClickListener(v -> filterMarkersByType("city"));
        }
        
        if (natureChip != null) {
            natureChip.setText("Natură");
            natureChip.setChipBackgroundColorResource(R.color.transilvania_primary);
            natureChip.setOnClickListener(v -> filterMarkersByType("nature"));
        }
        
        if (cultureChip != null) {
            cultureChip.setText("Castele");
            cultureChip.setChipBackgroundColorResource(R.color.transilvania_secondary);
            cultureChip.setOnClickListener(v -> filterMarkersByType("castle"));
        }
        
        if (historyChip != null) {
            historyChip.setText("Cetăți");
            historyChip.setChipBackgroundColorResource(R.color.transilvania_tertiary);
            historyChip.setOnClickListener(v -> filterMarkersByType("fortress"));
        }
        
        if (visitedChip != null) {
            visitedChip.setText("Vizitate");
            visitedChip.setChipBackgroundColorResource(R.color.transilvania_quaternary);
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
        
        // Adăugăm markeri pentru locațiile importante din Transilvania
        addMarker(
            "Brașov", 
            "Orașul de la poalele Tâmpei", 
            new LatLng(45.6427, 25.5887), 
            BitmapDescriptorFactory.HUE_RED,
            1
        );
        
        addMarker(
            "Cluj-Napoca", 
            "Capitala neoficială a Transilvaniei", 
            new LatLng(46.7712, 23.6236), 
            BitmapDescriptorFactory.HUE_RED,
            2
        );
        
        addMarker(
            "Sighișoara", 
            "Cetate medievală UNESCO", 
            new LatLng(46.2197, 24.7922), 
            BitmapDescriptorFactory.HUE_ORANGE,
            3
        );
        
        addMarker(
            "Castelul Bran", 
            "Cunoscut ca Castelul lui Dracula", 
            new LatLng(45.5149, 25.3672), 
            BitmapDescriptorFactory.HUE_VIOLET,
            4
        );
        
        addMarker(
            "Sibiu", 
            "Capitală Culturală Europeană 2007", 
            new LatLng(45.7983, 24.1469), 
            BitmapDescriptorFactory.HUE_RED,
            5
        );
        
        addMarker(
            "Alba Iulia", 
            "Cetatea Alba Carolina", 
            new LatLng(46.0686, 23.5716), 
            BitmapDescriptorFactory.HUE_ORANGE,
            6
        );
        
        // Centrăm harta pe Transilvania
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
            new LatLng(46.1667, 24.3000), 
            7.5f
        ));
    }

    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "transilvania", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("transilvania", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Afișăm un toast cu informații
        Toast.makeText(this, "Ai descoperit un nou loc în Transilvania! +25 puncte", Toast.LENGTH_SHORT).show();
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }

    @Override
    protected void startStoryActivity() {
        Intent intent = new Intent(this, DraculaStoryActivity.class);
        startActivity(intent);
    }

    @Override
    protected void startGameActivity() {
        Intent intent = new Intent(this, TransilvaniaGameActivity.class);
        startActivity(intent);
    }
}
