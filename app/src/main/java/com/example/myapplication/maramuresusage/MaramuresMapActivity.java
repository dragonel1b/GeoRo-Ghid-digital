package com.example.myapplication.maramuresusage;

import android.content.Intent;
import android.os.Bundle;

import com.example.myapplication.R;
import com.example.myapplication.model.RegionMapDataProvider;
import com.example.myapplication.model.base.BaseMapActivity;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

public class MaramuresMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm datele regiunii Maramureș
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("maramures"));
        
        // Inițializăm harta
        initializeMap();
    }

    @Override
    protected void addMapMarkers() {
        // Adăugăm markeri pentru locațiile importante din Maramureș
        addMarker(
            "Baia Mare", 
            "Capitala județului Maramureș", 
            new LatLng(47.6635, 23.5861), 
            BitmapDescriptorFactory.HUE_RED,
            1
        );
        
        addMarker(
            "Sighetu Marmației", 
            "Oraș istoric important", 
            new LatLng(47.9275, 23.8890), 
            BitmapDescriptorFactory.HUE_RED,
            2
        );
        
        addMarker(
            "Săpânța", 
            "Locul Cimitirului Vesel", 
            new LatLng(47.9736, 23.6964), 
            BitmapDescriptorFactory.HUE_ORANGE,
            3
        );
        
        addMarker(
            "Biserica de lemn din Șurdești", 
            "Una dintre cele mai înalte biserici de lemn din lume", 
            new LatLng(47.6903, 23.7408), 
            BitmapDescriptorFactory.HUE_AZURE,
            4
        );
        
        addMarker(
            "Mănăstirea Bârsana", 
            "Complex monastic impresionant", 
            new LatLng(47.8111, 24.0639), 
            BitmapDescriptorFactory.HUE_AZURE,
            5
        );
        
        addMarker(
            "Mocănița de pe Valea Vaserului", 
            "Trenul cu aburi pe cale ferată forestieră", 
            new LatLng(47.7131, 24.4450), 
            BitmapDescriptorFactory.HUE_GREEN,
            6
        );
    }
    
    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "Maramureș", 25);
        
        // Marcăm locația ca vizitată
        pointsManager.markLocationAsVisited("maramures", markerId);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
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