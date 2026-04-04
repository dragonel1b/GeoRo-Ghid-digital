package com.example.myapplication.transilvaniausage;

import android.content.Intent;
import com.example.myapplication.core.domain.model.BaseGameResultActivity;
import com.example.myapplication.R;

/**
 * Activitate rezultate pentru regiunea Transilvania
 * Extinde BaseGameResultActivity cu tema și funcționalitățile specifice Transilvaniei
 * Design îmbunătățit cu culori contrastante și compatibilitate completă cu baza de date
 */
public class TransilvaniaGameResultActivity extends BaseGameResultActivity {
    
    @Override
    protected String getRegionName() {
        return "Transilvania";
    }
    
    @Override
    protected Intent getPlayAgainIntent() {
        return new Intent(this, TransilvaniaGameActivity.class);
    }
    
    @Override
    protected RegionTheme getRegionTheme() {
        // Tema îmbunătățită pentru Transilvania cu culori contrastante
        // Înlocuim culorile închise cu unele mai vibrante și lizibile
        return new RegionTheme(
            R.color.transilvaniaResult_primary,        // Primary: albastru regal în loc de roșu închis
            R.color.transilvaniaResult_primary_dark,   // Primary Dark: albastru navy
            R.color.transilvaniaResult_accent,         // Accent: auriu medieval
            R.color.backgroundLight,                   // Background: alb/gri deschis
            R.color.white,                             // Card Background: alb pur
            R.color.text_primary                       // Text: negru pentru contrast maxim
        );
    }
} 
