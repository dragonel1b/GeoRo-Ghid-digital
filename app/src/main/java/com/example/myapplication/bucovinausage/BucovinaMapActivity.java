package com.example.myapplication.bucovinausage;

import android.os.Bundle;

import com.example.myapplication.model.RegionMapDataProvider;
import com.example.myapplication.model.base.BaseMapActivity;

public class BucovinaMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm datele regiunii Bucovina
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("bucovina"));
        
        // Inițializăm harta
        initializeMap();
    }
    
    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "Bucovina", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("bucovina", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }
} 