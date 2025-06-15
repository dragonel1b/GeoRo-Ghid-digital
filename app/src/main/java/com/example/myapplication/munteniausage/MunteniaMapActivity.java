package com.example.myapplication.munteniausage;

import android.content.Intent;
import android.os.Bundle;

import com.example.myapplication.R;
import com.example.myapplication.model.RegionMapDataProvider;
import com.example.myapplication.model.base.BaseMapActivity;

public class MunteniaMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muntenia_map);
        
        // Setăm datele regiunii Muntenia
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("muntenia"));
        
        // Inițializăm elementele UI
        initializeCommonViews();
        
        // Inițializăm harta
        initializeMap();
    }

    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "Muntenia", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("muntenia", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Verificăm dacă este Castelul Peleș pentru a deschide activitatea de tur
        if (markerId == 3) {
            Intent intent = new Intent(this, MunteniaTourActivity.class);
            startActivity(intent);
            return;
        }
        
        // Apelăm implementarea din clasa părinte, care va gestiona navigarea către activitățile specifice
        super.handleMarkerClick(markerId);
    }
} 