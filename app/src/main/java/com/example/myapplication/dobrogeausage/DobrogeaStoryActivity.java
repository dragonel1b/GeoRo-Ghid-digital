package com.example.myapplication.dobrogeausage;

import android.os.Bundle;
import com.example.myapplication.model.base.BaseStoryActivity;

/**
 * Activitate pentru povestea regiunii Dobrogea
 */
public class DobrogeaStoryActivity extends BaseStoryActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm regiunea
        region = "dobrogea";
        
        // Inițializăm povestea
        initializeStory();
        
        // Pregătim nodurile poveștii pentru navigare
        prepareStoryNodes();
        
        // Afișăm primul nod
        displayCurrentNode();
    }
    
    @Override
    protected void initializeStory() {
        // Inițializăm lista de noduri
        storyNodes = new java.util.ArrayList<>();
        
        // Adăugăm noduri de poveste pentru Dobrogea
        // Deocamdată lăsăm lista goală, va fi implementată ulterior
    }
} 