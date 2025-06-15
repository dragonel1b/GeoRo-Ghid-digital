package com.example.myapplication.transilvaniausage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.model.RegionMapDataProvider;
import com.example.myapplication.model.base.BaseMapActivity;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

public class TransilvaniaMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm datele regiunii Transilvania
        setRegionData(RegionMapDataProvider.getInstance().getRegionData("transilvania"));
        
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
    protected void addMapMarkers() {
        // Adăugăm markeri pentru locațiile importante din Transilvania
        addMarker(
            "Brașov", 
            "Orașul de la poalele Tâmpei", 
            new LatLng(45.6427, 25.5887), 
            BitmapDescriptorFactory.HUE_RED,
            1
        );
        
        addMarker(
            "Cluj-Napoca", 
            "Capitala neoficială a Transilvaniei", 
            new LatLng(46.7712, 23.6236), 
            BitmapDescriptorFactory.HUE_RED,
            2
        );
        
        addMarker(
            "Sighișoara", 
            "Cetate medievală UNESCO", 
            new LatLng(46.2197, 24.7922), 
            BitmapDescriptorFactory.HUE_ORANGE,
            3
        );
        
        addMarker(
            "Castelul Bran", 
            "Cunoscut ca Castelul lui Dracula", 
            new LatLng(45.5149, 25.3672), 
            BitmapDescriptorFactory.HUE_VIOLET,
            4
        );
    }

    @Override
    protected void handleMarkerClick(int markerId) {
        // Adăugăm puncte când utilizatorul apasă pe un marker
        pointsManager.addPoints(this, "transilvania", 25);
        
        // Actualizăm textul de progres
        updateProgressText();
        
        // Apelăm implementarea din clasa părinte
        super.handleMarkerClick(markerId);
    }

    @Override
    protected void startStoryActivity() {
        Intent intent = new Intent(this, DraculaStoryActivity.class);
        startActivity(intent);
    }

    @Override
    protected void startGameActivity() {
        Intent intent = new Intent(this, TransilvaniaGameActivity.class);
        startActivity(intent);
    }
}