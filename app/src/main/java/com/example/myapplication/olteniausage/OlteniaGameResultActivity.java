package com.example.myapplication.olteniausage;

import android.content.Intent;
import com.example.myapplication.core.domain.model.BaseGameResultActivity;
import com.example.myapplication.R;

/**
 * Activitate rezultate pentru regiunea Oltenia
 * Extinde BaseGameResultActivity cu tema și funcționalitățile specifice Olteniei
 * Design îmbunătățit cu culori contrastante și compatibilitate completă cu baza de date
 */
public class OlteniaGameResultActivity extends BaseGameResultActivity {
    
    @Override
    protected String getRegionName() {
        return "Oltenia";
    }
    
    @Override
    protected Intent getPlayAgainIntent() {
        return new Intent(this, OlteniaGameActivity.class);
    }
    
    @Override
    protected RegionTheme getRegionTheme() {
        // Tema îmbunătățită pentru Oltenia cu culori contrastante
        // Folosim culorile specifice Olteniei pentru o experiență vizuală coerentă
        return new RegionTheme(
            R.color.oltenia_primary,           // Primary: culoarea principală Oltenia
            R.color.oltenia_primary_dark,      // Primary Dark: varianta închisă
            R.color.oltenia_accent,            // Accent: culoarea de accent
            R.color.backgroundLight,           // Background: fundal deschis
            R.color.white,                     // Card Background: alb pur
            R.color.text_primary               // Text: negru pentru contrast maxim
        );
    }
} 
