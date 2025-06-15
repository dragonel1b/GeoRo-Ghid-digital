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
        setContentView(R.layout.activity_dobrogea_map);
        
        // Setăm datele regiunii Dobrogea
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("dobrogea"));
        
        // Inițializăm elementele UI
        initializeCommonViews();
        
        // Inițializăm harta
        initializeMap();
    }

    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "Dobrogea", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("dobrogea", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Verificăm dacă este Constanța pentru a deschide povestea specială despre Cazino
        if (markerId == 1) {
            Intent intent = new Intent(this, CasinoStoryActivity.class);
            startActivity(intent);
            return;
        }
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }
}
