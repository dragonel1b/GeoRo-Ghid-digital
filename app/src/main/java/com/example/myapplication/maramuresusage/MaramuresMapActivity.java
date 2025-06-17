package com.example.myapplication.maramuresusage;

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

public class MaramuresMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm datele regiunii Maramureș
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("maramures"));
        
        // Inițializăm harta
        initializeMap();
        
        // Configurăm legendele specifice pentru Maramureș
        setupMaramuresLegends();
        
        // Actualizăm textul de instrucțiuni
        TextView instructionsText = findViewById(R.id.instructionsText);
        if (instructionsText != null) {
            instructionsText.setText("Explorează Maramureșul! Descoperă bisericile de lemn și tradițiile locale.");
        }
    }
    
    /**
     * Configurăm legendele specifice pentru Maramureș
     */
    private void setupMaramuresLegends() {
        // Obținem referințe la chip-urile din legendă
        Chip cityChip = findViewById(R.id.legendChipCity);
        Chip natureChip = findViewById(R.id.legendChipNature);
        Chip cultureChip = findViewById(R.id.legendChipCulture);
        Chip historyChip = findViewById(R.id.legendChipHistory);
        Chip visitedChip = findViewById(R.id.legendChipVisited);
        
        // Setăm textele specifice pentru Maramureș
        if (cityChip != null) {
            cityChip.setText("Orașe");
            cityChip.setChipBackgroundColorResource(R.color.maramures_accent);
        }
        
        if (natureChip != null) {
            natureChip.setText("Natură");
            natureChip.setChipBackgroundColorResource(R.color.maramures_primary);
        }
        
        if (cultureChip != null) {
            cultureChip.setText("Tradiții");
            cultureChip.setChipBackgroundColorResource(R.color.maramures_secondary);
        }
        
        if (historyChip != null) {
            historyChip.setText("Biserici");
            historyChip.setChipBackgroundColorResource(R.color.maramures_tertiary);
        }
        
        if (visitedChip != null) {
            visitedChip.setText("Vizitate");
            visitedChip.setChipBackgroundColorResource(R.color.maramures_quaternary);
        }
    }

    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "maramures", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("maramures", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Afișăm un toast cu informații
        Toast.makeText(this, "Ai descoperit un nou loc în Maramureș! +25 puncte", Toast.LENGTH_SHORT).show();
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }

    @Override
    protected void addMapMarkers() {
        if (googleMap == null) return;
        
        // Adăugăm markeri pentru locațiile importante din Maramureș
        addMarker(
            "Baia Mare", 
            "Capitala județului Maramureș", 
            new LatLng(47.6667, 23.5833), 
            BitmapDescriptorFactory.HUE_RED,
            1
        );
        
        addMarker(
            "Sighetu Marmației", 
            "Memorialul Victimelor Comunismului", 
            new LatLng(47.9333, 23.8833), 
            BitmapDescriptorFactory.HUE_RED,
            2
        );
        
        addMarker(
            "Săpânța", 
            "Cimitirul Vesel", 
            new LatLng(47.9833, 23.7000), 
            BitmapDescriptorFactory.HUE_AZURE,
            3
        );
        
        addMarker(
            "Bârsana", 
            "Mănăstirea Bârsana", 
            new LatLng(47.8000, 24.0667), 
            BitmapDescriptorFactory.HUE_ORANGE,
            4
        );
        
        addMarker(
            "Borșa", 
            "Stațiune montană", 
            new LatLng(47.6500, 24.6500), 
            BitmapDescriptorFactory.HUE_GREEN,
            5
        );
        
        // Centrăm harta pe Maramureș
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
            new LatLng(47.8000, 24.0000), 
            8.0f
        ));
    }
    
    @Override
    protected void startStoryActivity() {
        // Redirectăm către activitatea de poveste
        Intent intent = new Intent(this, MaramuresStoryActivity.class);
        startActivity(intent);
    }
    
    @Override
    protected void startGameActivity() {
        // Redirectăm către activitatea de joc
        Intent intent = new Intent(this, MaramuresGameActivity.class);
        startActivity(intent);
    }
}