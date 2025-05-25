package com.example.myapplication.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

/**
 * Simple launcher for the Romania Map Activity.
 * This can be used as a reference for how to integrate the map into other activities.
 */
public class RomaniaMapLauncher extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_romania_map_launcher);
        
        // Set up title and description
        TextView titleText = findViewById(R.id.titleText);
        titleText.setText("Hartă Interactivă a României");
        
        TextView descriptionText = findViewById(R.id.descriptionText);
        descriptionText.setText("Explorează regiunile istorice ale României și descoperă orașele importante din fiecare regiune. Folosește filtrul de regiuni și temele zi/noapte pentru o experiență personalizată.");
        
        // Add features list
        TextView featuresList = findViewById(R.id.featuresList);
        featuresList.setText(
            "• Regiuni interactive - apasă pe orice regiune pentru detalii\n" +
            "• Filtrare după regiuni - selectează o regiune pentru a o evidenția\n" +
            "• Marcaje orașe - vezi principalele orașe din fiecare regiune\n" +
            "• Teme Zi/Noapte - schimbă stilul hărții cu un singur click"
        );
        
        Button launchMapButton = findViewById(R.id.launchMapButton);
        launchMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RomaniaMapActivity.launch(RomaniaMapLauncher.this);
            }
        });
    }
} 