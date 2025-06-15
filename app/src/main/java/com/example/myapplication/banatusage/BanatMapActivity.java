package com.example.myapplication.banatusage;

import android.os.Bundle;

import com.example.myapplication.model.RegionMapDataProvider;
import com.example.myapplication.model.base.BaseMapActivity;

public class BanatMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm datele regiunii Banat
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("banat"));
        
        // Inițializăm harta
        initializeMap();
    }
    
    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "Banat", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("banat", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }
} 