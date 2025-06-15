package com.example.myapplication.olteniausage;

import android.content.Intent;
import android.os.Bundle;

import com.example.myapplication.R;
import com.example.myapplication.RomApp.Oltenia;
import com.example.myapplication.model.RegionMapDataProvider;
import com.example.myapplication.model.base.BaseMapActivity;

public class OlteniaMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm datele regiunii Oltenia
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("oltenia"));
        
        // Inițializăm harta
        initializeMap();
    }
    
    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "Oltenia", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("oltenia", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }
    
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Oltenia.class);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void addMapMarkers() {
        // Adăugăm markeri pentru locațiile importante din Oltenia
        // Implementarea specifică pentru Oltenia
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