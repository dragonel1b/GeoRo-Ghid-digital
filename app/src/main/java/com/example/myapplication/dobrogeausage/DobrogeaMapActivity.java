package com.example.myapplication.dobrogeausage;

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

public class DobrogeaMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm datele regiunii Dobrogea
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("dobrogea"));
        
        // Inițializăm harta
        initializeMap();
        
        // Configurăm legendele specifice pentru Dobrogea
        setupDobrogeaLegends();
        
        // Actualizăm textul de instrucțiuni
        TextView instructionsText = findViewById(R.id.instructionsText);
        if (instructionsText != null) {
            instructionsText.setText("Explorează Dobrogea! Descoperă Delta Dunării și litoralul Mării Negre.");
        }
        
        // Activăm butoanele pentru poveste și joc
        if (storyButton != null) {
            storyButton.setOnClickListener(v -> startStoryActivity());
        }
        
        if (gameButton != null) {
            gameButton.setOnClickListener(v -> startGameActivity());
        }
    }
    
    /**
     * Configurăm legendele specifice pentru Dobrogea
     */
    private void setupDobrogeaLegends() {
        // Obținem referințe la chip-urile din legendă
        Chip cityChip = findViewById(R.id.legendChipCity);
        Chip natureChip = findViewById(R.id.legendChipNature);
        Chip cultureChip = findViewById(R.id.legendChipCulture);
        Chip historyChip = findViewById(R.id.legendChipHistory);
        Chip visitedChip = findViewById(R.id.legendChipVisited);
        
        // Setăm textele specifice pentru Dobrogea
        if (cityChip != null) {
            cityChip.setText("Orașe");
            cityChip.setChipBackgroundColorResource(R.color.dobrogea_accent);
        }
        
        if (natureChip != null) {
            natureChip.setText("Delta");
            natureChip.setChipBackgroundColorResource(R.color.dobrogea_primary);
        }
        
        if (cultureChip != null) {
            cultureChip.setText("Plaje");
            cultureChip.setChipBackgroundColorResource(R.color.dobrogea_secondary);
        }
        
        if (historyChip != null) {
            historyChip.setText("Istorie");
            historyChip.setChipBackgroundColorResource(R.color.dobrogea_tertiary);
        }
        
        if (visitedChip != null) {
            visitedChip.setText("Vizitate");
            visitedChip.setChipBackgroundColorResource(R.color.dobrogea_quaternary);
        }
    }

    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "dobrogea", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("dobrogea", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Afișăm un toast cu informații
        Toast.makeText(this, "Ai descoperit un nou loc în Dobrogea! +25 puncte", Toast.LENGTH_SHORT).show();
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }

    @Override
    protected void addMapMarkers() {
        if (googleMap == null) return;
        
        // Adăugăm markeri pentru locațiile importante din Dobrogea
        addMarker(
            "Constanța", 
            "Cel mai mare port la Marea Neagră", 
            new LatLng(44.1667, 28.6333), 
            BitmapDescriptorFactory.HUE_RED,
            1
        );
        
        addMarker(
            "Tulcea", 
            "Poarta către Delta Dunării", 
            new LatLng(45.1667, 28.8000), 
            BitmapDescriptorFactory.HUE_RED,
            2
        );
        
        addMarker(
            "Delta Dunării", 
            "Rezervație a Biosferei UNESCO", 
            new LatLng(45.0833, 29.5000), 
            BitmapDescriptorFactory.HUE_GREEN,
            3
        );
        
        addMarker(
            "Histria", 
            "Cea mai veche așezare din România", 
            new LatLng(44.5500, 28.7667), 
            BitmapDescriptorFactory.HUE_ORANGE,
            4
        );
        
        addMarker(
            "Mamaia", 
            "Cea mai populară stațiune de pe litoral", 
            new LatLng(44.2333, 28.6333), 
            BitmapDescriptorFactory.HUE_AZURE,
            5
        );
        
        // Centrăm harta pe Dobrogea
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
            new LatLng(44.8000, 28.8000), 
            7.5f
        ));
    }
    
    @Override
    protected void startStoryActivity() {
        // Redirectăm către activitatea de poveste
        Intent intent = new Intent(this, CasinoStoryActivity.class);
        startActivity(intent);
    }
    
    @Override
    protected void startGameActivity() {
        // Redirectăm către activitatea de joc
        Intent intent = new Intent(this, DobrogeaGameActivity.class);
        startActivity(intent);
    }
}

