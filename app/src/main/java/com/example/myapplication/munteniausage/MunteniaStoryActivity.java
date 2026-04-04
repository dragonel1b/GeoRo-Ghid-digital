package com.example.myapplication.munteniausage;

import android.os.Bundle;
import com.example.myapplication.core.domain.model.BaseStoryActivity;

/**
 * Activitate pentru povestea regiunii Muntenia
 */
public class MunteniaStoryActivity extends BaseStoryActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setăm regiunea
        region = "muntenia";
        
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
        
        // Adăugăm noduri de poveste pentru Muntenia
        // Deocamdată lăsăm lista goală, va fi implementată ulterior
    }
} 
