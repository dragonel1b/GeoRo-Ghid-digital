package com.example.myapplication.dobrogeausage;

import android.content.Intent;
import android.os.Bundle;

import com.example.myapplication.R;
import com.example.myapplication.model.RegionMapDataProvider;
import com.example.myapplication.model.base.BaseMapActivity;

public class DobrogeaMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm datele regiunii Dobrogea
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("dobrogea"));
        
        // Inițializăm harta
        initializeMap();
        
        // Activăm butoanele pentru poveste și joc
        if (storyButton != null) {
            storyButton.setOnClickListener(v -> startStoryActivity());
        }
        
        if (gameButton != null) {
            gameButton.setOnClickListener(v -> startGameActivity());
        }
    }

    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "dobrogea", 25);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }

    @Override
    protected void addMapMarkers() {
        // Adăugăm markeri pentru locațiile importante din Dobrogea
        // Implementarea specifică pentru Dobrogea
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
