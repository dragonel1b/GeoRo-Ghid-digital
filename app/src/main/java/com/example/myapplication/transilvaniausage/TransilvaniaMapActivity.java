package com.example.myapplication.transilvaniausage;

import android.content.Intent;
import android.os.Bundle;

import com.example.myapplication.R;
import com.example.myapplication.model.RegionMapDataProvider;
import com.example.myapplication.model.base.BaseMapActivity;

public class TransilvaniaMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transilvania_map);
        
        // Setăm datele regiunii Transilvania
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("transilvania"));
        
        // Inițializăm elementele UI
        initializeCommonViews();
        
        // Inițializăm harta
        initializeMap();
    }

    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "Transilvania", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("transilvania", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Verificăm dacă este Castelul Bran pentru a deschide povestea specială despre Dracula
        if (markerId == 6) {
            Intent intent = new Intent(this, DraculaStoryActivity.class);
            startActivity(intent);
            return;
        }
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }
}