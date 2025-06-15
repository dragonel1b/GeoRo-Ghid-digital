package com.example.myapplication.moldovausage;

import android.os.Bundle;

import com.example.myapplication.model.RegionMapDataProvider;
import com.example.myapplication.model.base.BaseMapActivity;

public class MoldovaMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm datele regiunii Moldova
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("moldova"));
        
        // Inițializăm harta
        initializeMap();
    }
    
    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "Moldova", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("moldova", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }
} 